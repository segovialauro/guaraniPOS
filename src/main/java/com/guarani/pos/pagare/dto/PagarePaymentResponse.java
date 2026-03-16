package com.guarani.pos.pagare.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagarePaymentResponse(
        Long id,
        LocalDateTime paymentDate,
        BigDecimal amount,
        String paymentMethod,
        String observation
) {
}
