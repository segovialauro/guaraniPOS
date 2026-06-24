package com.guarani.pos.company.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClientLicenseHistoryResponse(
        Long historyId,
        LocalDate previousDueDate,
        LocalDate newDueDate,
        LocalDateTime changedAt,
        String changedByCedula,
        String changedByFullName) {
}
