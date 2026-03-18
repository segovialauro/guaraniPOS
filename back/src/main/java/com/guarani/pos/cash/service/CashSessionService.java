package com.guarani.pos.cash.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.auth.service.AuthorizationService;
import com.guarani.pos.cash.dto.CashCloseReportResponse;
import com.guarani.pos.cash.dto.CashCloseRequest;
import com.guarani.pos.cash.dto.CashMovementCancelRequest;
import com.guarani.pos.cash.dto.CashMovementRequest;
import com.guarani.pos.cash.dto.CashMovementResponse;
import com.guarani.pos.cash.dto.CashMovementUpdateRequest;
import com.guarani.pos.cash.dto.CashOpenRequest;
import com.guarani.pos.cash.dto.CashSessionResponse;
import com.guarani.pos.cash.model.CashMovement;
import com.guarani.pos.cash.model.CashMovementStatus;
import com.guarani.pos.cash.model.CashMovementType;
import com.guarani.pos.cash.model.CashSession;
import com.guarani.pos.cash.repository.CashMovementRepository;
import com.guarani.pos.cash.repository.CashSessionRepository;
import com.guarani.pos.cash.security.CashPermission;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.sale.repository.SalePaymentRepository;
import com.guarani.pos.sale.repository.SaleRepository;
import com.guarani.pos.subscription.model.SubscriptionPlan;
import com.guarani.pos.subscription.service.SubscriptionAccessService;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class CashSessionService {

	private final CashSessionRepository cashSessionRepository;
	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;
	private final SaleRepository saleRepository;
	private final SalePaymentRepository salePaymentRepository;
	private final CashMovementRepository cashMovementRepository;
	private final AuthorizationService authorizationService;
	private final SubscriptionAccessService subscriptionAccessService;

	public CashSessionService(CashSessionRepository cashSessionRepository, CompanyRepository companyRepository,
			UserRepository userRepository, SaleRepository saleRepository, SalePaymentRepository salePaymentRepository,
			CashMovementRepository cashMovementRepository, AuthorizationService authorizationService,
			SubscriptionAccessService subscriptionAccessService) {
		this.cashSessionRepository = cashSessionRepository;
		this.companyRepository = companyRepository;
		this.userRepository = userRepository;
		this.saleRepository = saleRepository;
		this.salePaymentRepository = salePaymentRepository;
		this.cashMovementRepository = cashMovementRepository;
		this.authorizationService = authorizationService;
		this.subscriptionAccessService = subscriptionAccessService;
	}

	@Transactional(readOnly = true)
	public CashSessionResponse getCurrent(Long companyId, Long userId) {
		return cashSessionRepository
				.findFirstByCompany_IdAndUser_IdAndEstadoOrderByOpenedAtDesc(companyId, userId, "ABIERTA")
				.map(this::refreshAndMap).orElse(null);
	}

	@Transactional
	public CashSessionResponse open(Long companyId, Long userId, CashOpenRequest request) {

		authorizationService.checkPermission(userId, CashPermission.CAJA_ABRIR);

		cashSessionRepository.findFirstByCompany_IdAndUser_IdAndEstadoOrderByOpenedAtDesc(companyId, userId, "ABIERTA")
				.ifPresent(c -> {
					throw new IllegalArgumentException("El usuario ya tiene una caja abierta.");
				});

		long openCashCount = cashSessionRepository.countByCompany_IdAndEstado(companyId, "ABIERTA");
		subscriptionAccessService.validateCanOpenCashSession(companyId, openCashCount);
		subscriptionAccessService.validateElectronicInvoiceEnabled(companyId);
		
		SubscriptionPlan plan = subscriptionAccessService.getActivePlan(companyId);
		if (openCashCount >= plan.getMaxOpenCashSessions()) {
		        throw new IllegalStateException(
		            "Tu plan permite máximo " + plan.getMaxOpenCashSessions() + " cajas abiertas"
		        );
		    }
		
		Company company = companyRepository.findById(companyId)
				.orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		CashSession cash = new CashSession();
		cash.setCompany(company);
		cash.setUser(user);
		cash.setOpeningAmount(request.openingAmount());
		cash.setEstado("ABIERTA");
		cash.setObservacion(request.observation());

		return toResponse(cashSessionRepository.save(cash));
	}

	@Transactional
	public CashSessionResponse close(Long companyId, Long userId, CashCloseRequest request) {
		authorizationService.checkPermission(userId, CashPermission.CAJA_CERRAR);
		
		CashSession cash = cashSessionRepository
				.findFirstByCompany_IdAndUser_IdAndEstadoOrderByOpenedAtDesc(companyId, userId, "ABIERTA")
				.orElseThrow(() -> new IllegalArgumentException("No hay caja abierta para el usuario."));

		cash = refreshTotals(cash);

		BigDecimal expectedCash = calculateExpectedCash(cash);

		cash.setCashCounted(request.cashCounted());
		cash.setDifference(request.cashCounted().subtract(expectedCash));
		cash.setClosedAt(LocalDateTime.now());
		cash.setEstado("CERRADA");

		if (request.observation() != null && !request.observation().isBlank()) {
			cash.setObservacion(request.observation());
		}

		return toResponse(cashSessionRepository.save(cash));
	}

	private CashSessionResponse refreshAndMap(CashSession cash) {
		CashSession refreshed = refreshTotals(cash);
		return toResponse(refreshed);
	}

	private CashSession refreshTotals(CashSession cash) {
		LocalDateTime from = cash.getOpenedAt();
		LocalDateTime to = LocalDateTime.now();

		BigDecimal efectivo = nvl(salePaymentRepository.sumByCompanyPeriodAndPaymentMethod(cash.getCompany().getId(),
				from, to, "EFECTIVO"));
		BigDecimal transferencia = nvl(salePaymentRepository
				.sumByCompanyPeriodAndPaymentMethod(cash.getCompany().getId(), from, to, "TRANSFERENCIA"));
		BigDecimal debito = nvl(salePaymentRepository.sumByCompanyPeriodAndPaymentMethod(cash.getCompany().getId(),
				from, to, "TARJETA_DEBITO"));
		BigDecimal credito = nvl(salePaymentRepository.sumByCompanyPeriodAndPaymentMethod(cash.getCompany().getId(),
				from, to, "TARJETA_CREDITO"));
		BigDecimal qr = nvl(
				salePaymentRepository.sumByCompanyPeriodAndPaymentMethod(cash.getCompany().getId(), from, to, "QR"));
		BigDecimal total = nvl(saleRepository.sumByCompanyAndDateTimePeriod(cash.getCompany().getId(), from, to));

		cash.setCashSystem(efectivo);
		cash.setTransferSystem(transferencia);
		cash.setDebitCardSystem(debito);
		cash.setCreditCardSystem(credito);
		cash.setQrSystem(qr);
		cash.setTotalSystem(total);

		return cash;
	}

	private BigDecimal nvl(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private BigDecimal getManualIncomeTotal(Long cashSessionId) {
		return nvl(cashMovementRepository.sumByCashSessionAndTypeAndStatus(cashSessionId, CashMovementType.INGRESO,
				CashMovementStatus.ACTIVO));
	}

	private BigDecimal getManualWithdrawalTotal(Long cashSessionId) {
		return nvl(cashMovementRepository.sumByCashSessionAndTypeAndStatus(cashSessionId, CashMovementType.RETIRO,
				CashMovementStatus.ACTIVO));
	}

	private BigDecimal calculateExpectedCash(CashSession cash) {
		BigDecimal opening = nvl(cash.getOpeningAmount());
		BigDecimal cashSystem = nvl(cash.getCashSystem());
		BigDecimal manualIncome = getManualIncomeTotal(cash.getId());
		BigDecimal manualWithdrawal = getManualWithdrawalTotal(cash.getId());

		return opening.add(cashSystem).add(manualIncome).subtract(manualWithdrawal);
	}

	private CashSessionResponse toResponse(CashSession cash) {
		BigDecimal manualIncomeTotal = getManualIncomeTotal(cash.getId());
		BigDecimal manualWithdrawalTotal = getManualWithdrawalTotal(cash.getId());
		BigDecimal expectedCash = nvl(cash.getOpeningAmount()).add(nvl(cash.getCashSystem())).add(manualIncomeTotal)
				.subtract(manualWithdrawalTotal);

		return new CashSessionResponse(cash.getId(), cash.getOpenedAt(), cash.getClosedAt(),
				nvl(cash.getOpeningAmount()), nvl(cash.getCashSystem()), nvl(cash.getTransferSystem()),
				nvl(cash.getDebitCardSystem()), nvl(cash.getCreditCardSystem()), nvl(cash.getQrSystem()),
				nvl(cash.getTotalSystem()), manualIncomeTotal, manualWithdrawalTotal, expectedCash,
				nvl(cash.getCashCounted()), nvl(cash.getDifference()), cash.getEstado(), cash.getObservacion());
	}

	@Transactional(readOnly = true)
	public List<CashSessionResponse> getHistory(Long companyId, LocalDate from, LocalDate to, String status) {
		LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
		LocalDateTime toDateTime = to != null ? to.atTime(23, 59, 59) : null;

		boolean hasStatus = status != null && !status.isBlank();
		boolean hasFrom = fromDateTime != null;
		boolean hasTo = toDateTime != null;

		List<CashSession> result;

		if (hasStatus && hasFrom && hasTo) {
			result = cashSessionRepository.findAllByCompany_IdAndEstadoAndOpenedAtBetweenOrderByOpenedAtDesc(companyId,
					status, fromDateTime, toDateTime);
		} else if (hasStatus) {
			result = cashSessionRepository.findAllByCompany_IdAndEstadoOrderByOpenedAtDesc(companyId, status);
		} else if (hasFrom && hasTo) {
			result = cashSessionRepository.findAllByCompany_IdAndOpenedAtBetweenOrderByOpenedAtDesc(companyId,
					fromDateTime, toDateTime);
		} else {
			result = cashSessionRepository.findAllByCompany_IdOrderByOpenedAtDesc(companyId);
		}

		return result.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public CashCloseReportResponse getCloseReport(Long companyId, Long userId, Long cashSessionId) {
		authorizationService.checkPermission(userId, CashPermission.CAJA_REPORTE_VER);

		CashSession cash = cashSessionRepository.findById(cashSessionId)
				.orElseThrow(() -> new IllegalArgumentException("Caja no encontrada."));

		if (!cash.getCompany().getId().equals(companyId)) {
			throw new IllegalArgumentException("No tienes permisos para ver esta caja.");
		}

		BigDecimal manualIncomeTotal = getManualIncomeTotal(cash.getId());
		BigDecimal manualWithdrawalTotal = getManualWithdrawalTotal(cash.getId());

		BigDecimal expectedCash = nvl(cash.getOpeningAmount()).add(nvl(cash.getCashSystem())).add(manualIncomeTotal)
				.subtract(manualWithdrawalTotal);

		BigDecimal cashCounted = nvl(cash.getCashCounted());
		BigDecimal difference = nvl(cash.getDifference());

		String username = "-";
		if (cash.getUser() != null) {
			username = cash.getUser().getFullName();
		}

		return new CashCloseReportResponse(cash.getId(), cash.getCompany().getName(), username, cash.getOpenedAt(),
				cash.getClosedAt(), cash.getEstado(), cash.getObservacion(), nvl(cash.getOpeningAmount()),
				nvl(cash.getCashSystem()), nvl(cash.getTransferSystem()), nvl(cash.getDebitCardSystem()),
				nvl(cash.getCreditCardSystem()), nvl(cash.getQrSystem()), nvl(cash.getTotalSystem()), manualIncomeTotal,
				manualWithdrawalTotal, expectedCash, cashCounted, difference);
	}

	@Transactional(readOnly = true)
	public byte[] generateCloseReportPdf(Long companyId, Long userId, Long cashSessionId) {
		authorizationService.checkPermission(userId, CashPermission.CAJA_REPORTE_DESCARGAR);

		CashCloseReportResponse report = getCloseReport(companyId, userId, cashSessionId);

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Document document = new Document(PageSize.A4, 36, 36, 36, 36);
			PdfWriter.getInstance(document, out);
			document.open();

			Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
			Font subtitleFont = new Font(Font.HELVETICA, 12, Font.BOLD);
			Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
			Font smallFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

			Paragraph companyHeader = new Paragraph(safe(report.companyName()),
					new Font(Font.HELVETICA, 14, Font.BOLD));
			companyHeader.setSpacingAfter(8f);
			document.add(companyHeader);

			Paragraph title = new Paragraph("Reporte de cierre de caja", titleFont);
			title.setSpacingAfter(12f);
			document.add(title);

			document.add(new Paragraph("Caja ID: " + report.id(), normalFont));
			document.add(new Paragraph("Usuario responsable: " + safe(report.username()), normalFont));
			document.add(new Paragraph("Apertura: " + formatDateTime(report.openedAt()), normalFont));
			document.add(new Paragraph("Cierre: " + formatDateTime(report.closedAt()), normalFont));
			document.add(new Paragraph("Estado: " + safe(report.status()), normalFont));
			document.add(new Paragraph(" ", normalFont));

			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(100);
			table.setSpacingBefore(10f);
			table.setSpacingAfter(10f);
			table.setWidths(new float[] { 3f, 2f });

			addRow(table, "Monto apertura", formatMoney(report.openingAmount()));
			addRow(table, "Efectivo sistema", formatMoney(report.cashSystem()));
			addRow(table, "Transferencia", formatMoney(report.transferSystem()));
			addRow(table, "Tarjeta débito", formatMoney(report.debitCardSystem()));
			addRow(table, "Tarjeta crédito", formatMoney(report.creditCardSystem()));
			addRow(table, "QR", formatMoney(report.qrSystem()));
			addRow(table, "Total sistema", formatMoney(report.totalSystem()));
			addRow(table, "Ingresos manuales", formatMoney(report.manualIncomeTotal()));
			addRow(table, "Retiros manuales", formatMoney(report.manualWithdrawalTotal()));
			addRow(table, "Efectivo esperado", formatMoney(report.expectedCash()));
			addRow(table, "Efectivo contado", formatMoney(report.cashCounted()));
			addRow(table, "Diferencia", formatMoney(report.difference()));

			document.add(table);

			Font badgeFont = new Font(Font.HELVETICA, 12, Font.BOLD, differenceColor(report.difference()));
			Paragraph badge = new Paragraph("Resultado del arqueo: " + differenceLabel(report.difference()), badgeFont);
			badge.setSpacingBefore(8f);
			badge.setSpacingAfter(8f);
			document.add(badge);

			if (report.observation() != null && !report.observation().isBlank()) {
				Paragraph obsTitle = new Paragraph("Observación", subtitleFont);
				obsTitle.setSpacingBefore(10f);
				obsTitle.setSpacingAfter(6f);
				document.add(obsTitle);

				document.add(new Paragraph(report.observation(), smallFont));
			}

			Paragraph conformidad = new Paragraph("Conformidad", subtitleFont);
			conformidad.setSpacingBefore(20f);
			conformidad.setSpacingAfter(10f);
			document.add(conformidad);

			PdfPTable signTable = new PdfPTable(2);
			signTable.setWidthPercentage(100);
			signTable.setSpacingBefore(20f);
			signTable.setWidths(new float[] { 1f, 1f });

			signTable.addCell(buildSignatureCell("Firma cajero/a"));
			signTable.addCell(buildSignatureCell("Firma supervisor/a"));

			document.add(signTable);

			document.close();
			return out.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("No se pudo generar el PDF del cierre de caja.", e);
		}
	}

	@Transactional(readOnly = true)
	public List<CashMovementResponse> getCurrentMovements(Long companyId, Long userId){
		
		CashSession cash = cashSessionRepository
				.findFirstByCompany_IdAndUser_IdAndEstadoOrderByOpenedAtDesc(companyId, userId, "ABIERTA")
				.orElseThrow(() -> new IllegalArgumentException("No hay una caja abierta."));

		return cashMovementRepository.findAllByCashSession_IdOrderByCreatedAtDesc(cash.getId()).stream()
				.map(this::toMovementResponse).toList();
	}

	@Transactional
	public CashMovementResponse registerMovement(Long companyId, Long userId, CashMovementRequest request) {
		authorizationService.checkPermission(userId, CashPermission.CAJA_MOVIMIENTO_CREAR);

		CashSession cash = cashSessionRepository
		        .findFirstByCompany_IdAndUser_IdAndEstadoOrderByOpenedAtDesc(companyId, userId, "ABIERTA")
		        .orElseThrow(() -> new IllegalArgumentException("No hay una caja abierta para el usuario."));

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		CashMovement movement = new CashMovement();
		movement.setCashSession(cash);
		movement.setCompany(cash.getCompany());
		movement.setUser(user);
		movement.setType(request.type());
		movement.setAmount(request.amount());
		movement.setDescription(request.description());
		movement.setStatus(CashMovementStatus.ACTIVO);

		return toMovementResponse(cashMovementRepository.save(movement));
	}

	@Transactional
	public CashMovementResponse updateMovement(Long companyId, Long userId, Long movementId,
			CashMovementUpdateRequest request) {
		authorizationService.checkPermission(userId, CashPermission.CAJA_MOVIMIENTO_EDITAR);

		CashMovement movement = cashMovementRepository.findByIdAndCompany_Id(movementId, companyId)
				.orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado."));

		if (!"ABIERTA".equalsIgnoreCase(movement.getCashSession().getEstado())) {
			throw new IllegalArgumentException("Solo se pueden editar movimientos de una caja abierta.");
		}

		if (movement.getStatus() == CashMovementStatus.ANULADO) {
			throw new IllegalArgumentException("No se puede editar un movimiento anulado.");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		movement.setType(request.type());
		movement.setAmount(request.amount());
		movement.setDescription(request.description());
		movement.setUpdatedAt(LocalDateTime.now());
		movement.setUpdatedBy(user);

		return toMovementResponse(cashMovementRepository.save(movement));
	}

	@Transactional
	public CashMovementResponse cancelMovement(Long companyId, Long userId, Long movementId,
			CashMovementCancelRequest request) {
		authorizationService.checkPermission(userId, CashPermission.CAJA_MOVIMIENTO_ANULAR);

		CashMovement movement = cashMovementRepository.findByIdAndCompany_Id(movementId, companyId)
				.orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado."));

		if (!"ABIERTA".equalsIgnoreCase(movement.getCashSession().getEstado())) {
			throw new IllegalArgumentException("Solo se pueden anular movimientos de una caja abierta.");
		}

		if (movement.getStatus() == CashMovementStatus.ANULADO) {
			throw new IllegalArgumentException("El movimiento ya está anulado.");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		movement.setStatus(CashMovementStatus.ANULADO);
		movement.setCanceledAt(LocalDateTime.now());
		movement.setCanceledBy(user);
		movement.setCancellationReason(request.reason());

		return toMovementResponse(cashMovementRepository.save(movement));
	}

	private String formatMoney(BigDecimal value) {
		BigDecimal safeValue = value != null ? value : BigDecimal.ZERO;
		return "Gs. " + NumberFormat.getNumberInstance(new Locale("es", "PY")).format(safeValue);
	}

	private String formatDateTime(LocalDateTime value) {
		if (value == null) {
			return "-";
		}
		return value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
	}

	private String safe(String value) {
		return value != null ? value : "-";
	}

	private String differenceLabel(BigDecimal difference) {
		BigDecimal safe = difference != null ? difference : BigDecimal.ZERO;

		int cmp = safe.compareTo(BigDecimal.ZERO);
		if (cmp < 0) {
			return "FALTANTE";
		}
		if (cmp > 0) {
			return "SOBRANTE";
		}
		return "CUADRE EXACTO";
	}

	private Color differenceColor(BigDecimal difference) {
		BigDecimal safe = difference != null ? difference : BigDecimal.ZERO;

		int cmp = safe.compareTo(BigDecimal.ZERO);
		if (cmp < 0) {
			return new Color(185, 28, 28); // rojo
		}
		if (cmp > 0) {
			return new Color(234, 88, 12); // naranja
		}
		return new Color(22, 163, 74); // verde
	}

	private void addRow(PdfPTable table, String label, String value) {
		table.addCell(label);
		table.addCell(value);
	}

	private PdfPCell buildSignatureCell(String label) {
		PdfPCell cell = new PdfPCell();
		cell.setBorder(PdfPCell.NO_BORDER);
		cell.setPaddingTop(25f);
		cell.setPaddingBottom(10f);

		Paragraph line = new Paragraph("______________________________");
		Paragraph text = new Paragraph(label);

		cell.addElement(line);
		cell.addElement(text);
		return cell;
	}

	private CashMovementResponse toMovementResponse(CashMovement m) {
		String username = m.getUser() != null ? m.getUser().getFullName() : "-";
		String updatedByUsername = m.getUpdatedBy() != null ? m.getUpdatedBy().getFullName() : null;
		String canceledByUsername = m.getCanceledBy() != null ? m.getCanceledBy().getFullName() : null;

		return new CashMovementResponse(m.getId(), m.getCashSession().getId(), m.getCreatedAt(), m.getType(),
				m.getAmount(), m.getDescription(), username, m.getStatus(), m.getUpdatedAt(), updatedByUsername,
				m.getCanceledAt(), canceledByUsername, m.getCancellationReason());
	}

}
