package com.guarani.pos.sale.dto;

import java.math.BigDecimal;

public record SaleDetailResponse(
        Long productId,
        String productCode,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal grossSubtotal,
        BigDecimal discountAmount,
        BigDecimal returnedQuantity,
        BigDecimal returnedAmount,
        BigDecimal subtotal,
        String vatType
) {
}
