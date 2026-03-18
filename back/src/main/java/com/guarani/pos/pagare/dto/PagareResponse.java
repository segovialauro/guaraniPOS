package com.guarani.pos.pagare.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PagareResponse(
        Long id,
        String pagareNumber,
        Long customerId,
        String customerName,
        Long saleId,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal amount,
        BigDecimal balance,
        String status,
        String observation,
        List<PagarePaymentResponse> payments
) {
}
