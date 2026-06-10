package com.guarani.pos.electronicinvoice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ElectronicInvoiceResponse(
        Long id,
        Long saleId,
        String operationNumber,
        LocalDateTime saleDate,
        String customerName,
        BigDecimal total,
        String fiscalInvoiceNumber,
        String series,
        String cdc,
        String status,
        String statusDetail,
        String environment,
        String transactionNumber,
        String batchNumber,
        String processingStatusCode,
        String processingStatusMessage,
        String qrPayload,
        String qrUrl,
        String unsignedXml
) {
}
