package com.guarani.pos.purchase.dto;

import java.math.BigDecimal;

public record PurchaseDetailResponse(
        Long productId,
        String productCode,
        String productName,
        BigDecimal quantity,
        BigDecimal receivedQuantity,
        BigDecimal costPrice,
        BigDecimal subtotal
) {
}
