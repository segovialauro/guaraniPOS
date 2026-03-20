package com.guarani.pos.sale.dto;

import java.math.BigDecimal;

public record SaleTicketDetailResponse(
        String productCode,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String vatType
) {
}
