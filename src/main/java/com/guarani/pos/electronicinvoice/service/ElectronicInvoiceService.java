package com.guarani.pos.electronicinvoice.service;

import static com.guarani.pos.sale.security.SalePermission.VENTA_TICKET_VER;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.service.AuthorizationService;
import com.guarani.pos.billing.model.BillingConfig;
import com.guarani.pos.electronicinvoice.dto.ElectronicInvoiceResponse;
import com.guarani.pos.electronicinvoice.model.ElectronicInvoice;
import com.guarani.pos.electronicinvoice.repository.ElectronicInvoiceRepository;
import com.guarani.pos.electronicinvoice.util.Mod11;
import com.guarani.pos.sale.model.Sale;

@Service
public class ElectronicInvoiceService {

    private static final DateTimeFormatter CDC_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ElectronicInvoiceRepository electronicInvoiceRepository;
    private final AuthorizationService authorizationService;
    private final ElectronicInvoiceXmlBuilder xmlBuilder;

    public ElectronicInvoiceService(ElectronicInvoiceRepository electronicInvoiceRepository,
                                    AuthorizationService authorizationService,
                                    ElectronicInvoiceXmlBuilder xmlBuilder) {
        this.electronicInvoiceRepository = electronicInvoiceRepository;
        this.authorizationService = authorizationService;
        this.xmlBuilder = xmlBuilder;
    }

    @Transactional
    public void createDraftForSale(Sale sale, BillingConfig config) {
        if (!"ELECTRONICO".equalsIgnoreCase(sale.getFiscalDocumentType())) {
            return;
        }

        if (electronicInvoiceRepository.findBySale_IdAndCompany_Id(sale.getId(), sale.getCompany().getId()).isPresent()) {
            return;
        }

        String sequentialNumber = extractSequentialNumber(sale.getFiscalInvoiceNumber());
        String series = trimToNull(config.getElectronicSeries());
        String securityCode = generateSecurityCode();
        String cdcBase = buildCdcBase(
                "01",
                config.getRuc(),
                config.getEstablishmentCode(),
                config.getExpeditionPoint(),
                sequentialNumber,
                config.getTaxpayerType(),
                sale.getFecha().toLocalDate(),
                "1",
                securityCode
        );
        String cdcCheckDigit = String.valueOf(Mod11.calculate(cdcBase));
        String cdc = cdcBase + cdcCheckDigit;

        ElectronicInvoice draft = new ElectronicInvoice();
        draft.setCompany(sale.getCompany());
        draft.setSale(sale);
        draft.setBillingConfig(config);
        draft.setDocumentTypeCode("1");
        draft.setDocumentTypeDescription("Factura electrónica");
        draft.setTaxpayerType(config.getTaxpayerType());
        draft.setSeries(series);
        draft.setDocumentNumber(sequentialNumber);
        draft.setSecurityCode(securityCode);
        draft.setCdcCheckDigit(cdcCheckDigit);
        draft.setCdc(cdc);
        draft.setEnvironment(normalizeEnvironment(config.getSifenEnvironment()));
        draft.setStatus("DRAFT_PENDING_SIGNATURE");
        draft.setStatusDetail("Documento base generado. Pendiente de firma digital, DigestValue y envio a SIFEN.");
        draft.setUnsignedXml(xmlBuilder.buildUnsignedXml(sale, config, draft, sale.getDetails()));
        draft.setQrPayload(buildQrPayload(draft, sale, config));
        draft.setQrUrl(buildQrUrl(draft.getQrPayload()));

        electronicInvoiceRepository.save(draft);
    }

    @Transactional(readOnly = true)
    public List<ElectronicInvoiceResponse> findRecent(Long companyId, Long userId) {
        authorizationService.checkPermission(companyId, userId, VENTA_TICKET_VER);
        return electronicInvoiceRepository.findTop50ByCompany_IdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ElectronicInvoiceResponse findBySale(Long companyId, Long userId, Long saleId) {
        authorizationService.checkPermission(companyId, userId, VENTA_TICKET_VER);
        ElectronicInvoice invoice = electronicInvoiceRepository.findBySale_IdAndCompany_Id(saleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("No existe factura electronica para la venta indicada."));
        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public Optional<ElectronicInvoice> findEntityBySale(Long companyId, Long saleId) {
        return electronicInvoiceRepository.findBySale_IdAndCompany_Id(saleId, companyId);
    }

    private ElectronicInvoiceResponse toResponse(ElectronicInvoice invoice) {
        Sale sale = invoice.getSale();
        return new ElectronicInvoiceResponse(
                invoice.getId(),
                sale.getId(),
                sale.getNumeroOperacion(),
                sale.getFecha(),
                sale.getCustomer() != null ? sale.getCustomer().getNombre() : "Cliente contado",
                sale.getTotal(),
                sale.getFiscalInvoiceNumber(),
                invoice.getSeries(),
                invoice.getCdc(),
                invoice.getStatus(),
                invoice.getStatusDetail(),
                invoice.getEnvironment(),
                invoice.getTransactionNumber(),
                invoice.getBatchNumber(),
                invoice.getProcessingStatusCode(),
                invoice.getProcessingStatusMessage(),
                invoice.getQrPayload(),
                invoice.getQrUrl(),
                invoice.getUnsignedXml()
        );
    }

    private String buildQrPayload(ElectronicInvoice invoice, Sale sale, BillingConfig config) {
        String receiverId = resolveReceiverTaxId(sale);
        String digestPlaceholder = sha256Hex(invoice.getCdc());
        String issueDateHex = HexFormat.of().formatHex(
                sale.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")).getBytes(StandardCharsets.UTF_8)
        );
        String digestHex = HexFormat.of().formatHex(digestPlaceholder.getBytes(StandardCharsets.UTF_8));
        String baseParams = "nVersion=150"
                + "&Id=" + invoice.getCdc()
                + "&dFeEmiDE=" + issueDateHex
                + "&dRucRec=" + receiverId.replaceAll("[^A-Za-z0-9]", "")
                + "&dTotGralOpe=" + normalizeAmount(sale.getTotal())
                + "&dTotIVA=" + normalizeAmount(calculateVatTotal(sale))
                + "&cItems=" + sale.getDetails().size()
                + "&DigestValue=" + digestHex
                + "&IdCSC=" + nullSafe(config.getQrSecurityCodeId(), "0000");
        String csc = nullSafe(config.getQrSecurityCode(), "PENDIENTECSC00000000000000000000");
        String hash = sha256Hex(baseParams + csc);
        return baseParams + "&cHashQR=" + hash;
    }

    private String buildQrUrl(String qrPayload) {
        String baseUrl = "https://ekuatia.set.gov.py/consultas/qr?";
        return baseUrl + qrPayload;
    }

    private String buildCdcBase(String documentTypeCode,
                                String ruc,
                                String establishmentCode,
                                String expeditionPoint,
                                String documentNumber,
                                String taxpayerType,
                                LocalDate issueDate,
                                String emissionTypeCode,
                                String securityCode) {
        String normalizedRuc = normalizeRucNumber(ruc);
        String rucNumber = leftPadDigits(extractRucNumber(normalizedRuc), 8);
        String rucDv = extractRucDv(ruc);
        return leftPadDigits(documentTypeCode, 2)
                + rucNumber
                + leftPadDigits(rucDv, 1)
                + leftPadDigits(establishmentCode, 3)
                + leftPadDigits(expeditionPoint, 3)
                + leftPadDigits(documentNumber, 7)
                + leftPadDigits(taxpayerType, 1)
                + issueDate.format(CDC_DATE)
                + leftPadDigits(emissionTypeCode, 1)
                + leftPadDigits(securityCode, 9);
    }

    private String extractSequentialNumber(String invoiceNumber) {
        if (invoiceNumber == null || !invoiceNumber.matches("^\\d{3}-\\d{3}-\\d{7}$")) {
            throw new IllegalArgumentException("El numero de factura electronica debe tener formato 001-001-0000001.");
        }
        return invoiceNumber.substring(invoiceNumber.lastIndexOf('-') + 1);
    }

    private String generateSecurityCode() {
        int value = ThreadLocalRandom.current().nextInt(1, 1_000_000_000);
        return String.format(Locale.ROOT, "%09d", value);
    }

    private String normalizeEnvironment(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? "TEST" : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeRucNumber(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9-]", "");
    }

    private String resolveReceiverTaxId(Sale sale) {
        if (sale.getCustomer() == null) {
            return "0";
        }

        String receiverId = trimToNull(sale.getCustomer().getRuc());
        if (receiverId == null) {
            receiverId = trimToNull(sale.getCustomer().getDocumento());
        }
        return receiverId == null ? "0" : receiverId;
    }

    private String extractRucNumber(String value) {
        int dashIndex = value.indexOf('-');
        return dashIndex >= 0 ? value.substring(0, dashIndex) : value.substring(0, Math.max(0, value.length() - 1));
    }

    private String extractRucDv(String value) {
        if (value == null || value.isBlank()) {
            return "0";
        }
        int dashIndex = value.lastIndexOf('-');
        if (dashIndex >= 0 && dashIndex < value.length() - 1) {
            return value.substring(dashIndex + 1).trim();
        }
        return value.substring(value.length() - 1);
    }

    private String leftPadDigits(String value, int length) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        return "0".repeat(Math.max(0, length - digits.length())) + digits;
    }

    private String nullSafe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeAmount(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    private BigDecimal calculateVatTotal(Sale sale) {
        return sale.getDetails().stream()
                .map(this::calculateVatAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateVatAmount(com.guarani.pos.sale.model.SaleDetail detail) {
        BigDecimal subtotal = detail.getSubtotal() == null ? BigDecimal.ZERO : detail.getSubtotal();
        return switch (nullSafe(detail.getVatType(), "IVA_10").toUpperCase(Locale.ROOT)) {
            case "IVA_5" -> subtotal.multiply(BigDecimal.valueOf(5))
                    .divide(BigDecimal.valueOf(105), 2, RoundingMode.HALF_UP);
            case "EXENTO" -> BigDecimal.ZERO;
            default -> subtotal.multiply(BigDecimal.TEN)
                    .divide(BigDecimal.valueOf(110), 2, RoundingMode.HALF_UP);
        };
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se pudo calcular SHA-256.", ex);
        }
    }
}
