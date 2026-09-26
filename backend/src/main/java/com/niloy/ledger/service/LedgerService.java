package com.niloy.ledger.service;

import com.niloy.ledger.dto.CreateEntryRequest;
import com.niloy.ledger.dto.EntryResponse;
import com.niloy.ledger.entity.Account;
import com.niloy.ledger.entity.EntryType;
import com.niloy.ledger.entity.LedgerEntry;
import com.niloy.ledger.exception.AccountNotFoundException;
import com.niloy.ledger.exception.InsufficientFundsException;
import com.niloy.ledger.repository.AccountRepository;
import com.niloy.ledger.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public EntryResponse recordEntry(Long accountId, CreateEntryRequest request) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        BigDecimal currentBalance = account.getBalance();
        BigDecimal amount = request.amount();

        if (request.type() == EntryType.DEBIT && currentBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(currentBalance, amount);
        }

        BigDecimal newBalance = (request.type() == EntryType.CREDIT)
                ? currentBalance.add(amount)
                : currentBalance.subtract(amount);

        account.setBalance(newBalance);

        LedgerEntry entry = new LedgerEntry(
                account, request.type(), amount, request.description().trim(), newBalance);
        LedgerEntry saved = ledgerEntryRepository.save(entry);

        log.info("Recorded {} of {} on account {}, new balance {}",
                request.type(), amount, accountId, newBalance);

        return EntryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<EntryResponse> getStatement(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        return ledgerEntryRepository.findByAccountIdOrderByCreatedAtAscIdAsc(accountId)
                .stream()
                .map(EntryResponse::from)
                .toList();
    }
}
