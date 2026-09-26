package com.niloy.ledger.dto;

import com.niloy.ledger.entity.EntryType;
import com.niloy.ledger.entity.LedgerEntry;

import java.math.BigDecimal;
import java.time.Instant;

public record EntryResponse(
        Long id,
        EntryType type,
        BigDecimal amount,
        String description,
        BigDecimal balanceAfter,
        Instant createdAt
) {
    public static EntryResponse from(LedgerEntry entry) {
        return new EntryResponse(
                entry.getId(),
                entry.getType(),
                entry.getAmount(),
                entry.getDescription(),
                entry.getBalanceAfter(),
                entry.getCreatedAt());
    }
}
