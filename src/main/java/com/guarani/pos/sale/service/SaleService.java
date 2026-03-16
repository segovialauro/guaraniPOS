package com.guarani.pos.sale.service;

import static com.guarani.pos.sale.security.SalePermission.VENTA_CREAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_TICKET_DESCARGAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_TICKET_VER;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.auth.service.AuthorizationService;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.customer.model.Customer;
import com.guarani.pos.customer.repository.CustomerRepository;
import com.guarani.pos.product.model.Product;
import com.guarani.pos.product.repository.ProductRepository;
import com.guarani.pos.sale.dto.SaleCreateRequest;
import com.guarani.pos.sale.dto.SaleDetailResponse;
import com.guarani.pos.sale.dto.SaleItemRequest;
import com.guarani.pos.sale.dto.SaleResponse;
import com.guarani.pos.sale.dto.SaleTicketDetailResponse;
import com.guarani.pos.sale.dto.SaleTicketResponse;
import com.guarani.pos.sale.model.Sale;
import com.guarani.pos.sale.model.SaleDetail;
import com.guarani.pos.sale.repository.SaleRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class SaleService {

	private final SaleRepository saleRepository;
	private final ProductRepository productRepository;
	private final CustomerRepository customerRepository;
	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;
	private final AuthorizationService authorizationService;

	public SaleService(SaleRepository saleRepository, ProductRepository productRepository,
			CustomerRepository customerRepository, CompanyRepository companyRepository, UserRepository userRepository,
			AuthorizationService authorizationService) {
		this.saleRepository = saleRepository;
		this.productRepository = productRepository;
		this.customerRepository = customerRepository;
		this.companyRepository = companyRepository;
		this.userRepository = userRepository;
		this.authorizationService = authorizationService;
	}

	@Transactional(readOnly = true)
	public List<SaleResponse> findRecent(Long companyId) {
		return saleRepository.findTop20ByCompanyIdOrderByFechaDesc(companyId).stream().map(this::toResponse).toList();
	}

	@Transactional
	public SaleResponse create(Long companyId, Long userId, SaleCreateRequest request) {
		authorizationService.checkPermission(userId, VENTA_CREAR);
		Company company = companyRepository.findById(companyId)
				.orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		Customer customer = null;
		if (request.customerId() != null) {
			customer = customerRepository.findByIdAndCompanyId(request.customerId(), companyId)
					.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));
		}

		Sale sale = new Sale();
		sale.setCompany(company);
		sale.setCustomer(customer);
		sale.setCreatedBy(user);
		sale.setMetodoPago(request.paymentMethod().trim());
		sale.setEstado("CONFIRMADA");
		sale.setObservacion(request.observation());
		sale.setNumeroOperacion(generateOperationNumber(companyId));

		BigDecimal total = BigDecimal.ZERO;

		for (SaleItemRequest item : request.items()) {
			Product product = productRepository.findByIdAndCompanyId(item.productId(), companyId)
					.orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.productId()));

			if (!product.isActivo()) {
				throw new IllegalArgumentException("El producto está inactivo: " + product.getNombre());
			}

			if (product.getStockActual().compareTo(item.quantity()) < 0) {
				throw new IllegalArgumentException("Stock insuficiente para: " + product.getNombre());
			}

			BigDecimal subtotal = product.getPrecioVenta().multiply(item.quantity());

			SaleDetail detail = new SaleDetail();
			detail.setProduct(product);
			detail.setProductoCodigo(product.getCodigo());
			detail.setProductoNombre(product.getNombre());
			detail.setCantidad(item.quantity());
			detail.setPrecioUnitario(product.getPrecioVenta());
			detail.setSubtotal(subtotal);

			sale.addDetail(detail);

			product.setStockActual(product.getStockActual().subtract(item.quantity()));
			total = total.add(subtotal);
		}

		sale.setTotal(total);

		Sale saved = saleRepository.save(sale);
		return toResponse(saved);
	}

	private String generateOperationNumber(Long companyId) {
		long next = saleRepository.countByCompanyId(companyId) + 1;
		String period = YearMonth.now().toString().replace("-", "");
		return "V-" + period + "-" + new DecimalFormat("000000").format(next);
	}

	private SaleResponse toResponse(Sale sale) {
		return new SaleResponse(sale.getId(), sale.getNumeroOperacion(), sale.getFecha(),
				sale.getCustomer() != null ? sale.getCustomer().getId() : null,
				sale.getCustomer() != null ? sale.getCustomer().getNombre() : null, sale.getMetodoPago(),
				sale.getEstado(), sale.getTotal(),
				sale.getDetails().stream()
						.map(d -> new SaleDetailResponse(d.getProduct().getId(), d.getProductoCodigo(),
								d.getProductoNombre(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
						.toList());
	}
	
	@Transactional(readOnly = true)
	public SaleTicketResponse getTicket(Long companyId, Long userId, Long saleId) {
	    authorizationService.checkPermission(userId, VENTA_TICKET_VER);

	    Sale sale = saleRepository.findByIdAndCompany_Id(saleId, companyId)
	            .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada."));

	    String companyName = sale.getCompany() != null ? sale.getCompany().getName() : "-";
	    String companyRuc = sale.getCompany() != null ? sale.getCompany().getRuc() : "-";
	    String customerName = sale.getCustomer() != null ? sale.getCustomer().getNombre() : "Cliente contado";
	    String cashierName = sale.getCreatedBy() != null ? sale.getCreatedBy().getFullName() : "-";

	    return new SaleTicketResponse(
	            sale.getId(),
	            sale.getNumeroOperacion(),
	            companyName,
	            companyRuc,
	            customerName,
	            cashierName,
	            sale.getFecha(),
	            sale.getMetodoPago(),
	            sale.getEstado(),
	            sale.getObservacion(),
	            sale.getTotal(),
	            sale.getDetails().stream()
	                    .map(d -> new SaleTicketDetailResponse(
	                            d.getProductoCodigo(),
	                            d.getProductoNombre(),
	                            d.getCantidad(),
	                            d.getPrecioUnitario(),
	                            d.getSubtotal()
	                    ))
	                    .toList()
	    );
	}
	
	@Transactional(readOnly = true)
	public byte[] generateTicketPdf(Long companyId, Long userId, Long saleId) {
	    authorizationService.checkPermission(userId, VENTA_TICKET_DESCARGAR);

	    SaleTicketResponse ticket = getTicket(companyId, userId, saleId);

	    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
	        Document document = new Document(PageSize.A6, 24, 24, 24, 24);
	        PdfWriter.getInstance(document, out);
	        document.open();

	        Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
	        Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
	        Font boldFont = new Font(Font.HELVETICA, 9, Font.BOLD);

	        Paragraph company = new Paragraph(safe(ticket.companyName()), titleFont);
	        company.setAlignment(Paragraph.ALIGN_CENTER);
	        company.setSpacingAfter(4f);
	        document.add(company);

	        Paragraph ruc = new Paragraph("RUC: " + safe(ticket.companyRuc()), normalFont);
	        ruc.setAlignment(Paragraph.ALIGN_CENTER);
	        ruc.setSpacingAfter(8f);
	        document.add(ruc);

	        Paragraph op = new Paragraph("Ticket: " + safe(ticket.operationNumber()), boldFont);
	        op.setSpacingAfter(4f);
	        document.add(op);

	        document.add(new Paragraph("Fecha: " + formatDateTime(ticket.date()), normalFont));
	        document.add(new Paragraph("Cajero: " + safe(ticket.cashierName()), normalFont));
	        document.add(new Paragraph("Cliente: " + safe(ticket.customerName()), normalFont));
	        document.add(new Paragraph("Pago: " + safe(ticket.paymentMethod()), normalFont));
	        document.add(new Paragraph("Estado: " + safe(ticket.status()), normalFont));
	        document.add(new Paragraph(" ", normalFont));

	        PdfPTable table = new PdfPTable(2);
	        table.setWidthPercentage(100);
	        table.setWidths(new float[]{4f, 2f});

	        for (SaleTicketDetailResponse item : ticket.items()) {
	            String left = item.quantity().stripTrailingZeros().toPlainString()
	                    + " x " + safe(item.productName());
	            table.addCell(left);
	            table.addCell(formatMoney(item.subtotal()));
	        }

	        document.add(table);

	        Paragraph total = new Paragraph("TOTAL: " + formatMoney(ticket.total()), boldFont);
	        total.setAlignment(Paragraph.ALIGN_RIGHT);
	        total.setSpacingBefore(8f);
	        document.add(total);

	        if (ticket.observation() != null && !ticket.observation().isBlank()) {
	            document.add(new Paragraph(" ", normalFont));
	            document.add(new Paragraph("Obs: " + ticket.observation(), normalFont));
	        }

	        Paragraph footer = new Paragraph("Gracias por su compra", normalFont);
	        footer.setAlignment(Paragraph.ALIGN_CENTER);
	        footer.setSpacingBefore(12f);
	        document.add(footer);

	        document.close();
	        return out.toByteArray();
	    } catch (Exception e) {
	        throw new IllegalStateException("No se pudo generar el ticket PDF.", e);
	    }
	}
	
	private String safe(String value) {
	    return value != null && !value.isBlank() ? value : "-";
	}

	private String formatDateTime(java.time.LocalDateTime value) {
	    if (value == null) {
	        return "-";
	    }
	    return value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
	}

	private String formatMoney(BigDecimal value) {
	    BigDecimal safeValue = value != null ? value : BigDecimal.ZERO;
	    return "Gs. " + NumberFormat.getNumberInstance(new Locale("es", "PY")).format(safeValue);
	}
}