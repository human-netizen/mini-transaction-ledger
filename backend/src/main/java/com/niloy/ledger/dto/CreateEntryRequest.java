package com.niloy.ledger.dto;

import com.niloy.ledger.entity.EntryType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateEntryRequest(
        @NotNull EntryType type,
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank @Size(max = 255) String description
) {
}
