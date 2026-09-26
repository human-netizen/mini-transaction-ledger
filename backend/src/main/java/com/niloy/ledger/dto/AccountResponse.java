package com.niloy.ledger.dto;

import com.niloy.ledger.entity.Account;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        Long id,
        String holderName,
        BigDecimal balance,
        Instant createdAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getHolderName(),
                account.getBalance(),
                account.getCreatedAt());
    }
}
