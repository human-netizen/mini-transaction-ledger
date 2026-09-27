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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    LedgerService ledgerService;

    private Account accountWithBalance(String balance) {
        Account account = new Account("Rahim");
        account.setBalance(new BigDecimal(balance));
        return account;
    }

    private void saveReturnsWhatItReceives() {
        when(ledgerEntryRepository.save(any(LedgerEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void credit_increasesBalance() {
        Account account = accountWithBalance("0.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        saveReturnsWhatItReceives();

        EntryResponse result = ledgerService.recordEntry(1L,
                new CreateEntryRequest(EntryType.CREDIT, new BigDecimal("100.00"), "Salary"));

        assertThat(result.balanceAfter()).isEqualByComparingTo("100.00");
        assertThat(account.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void debit_reducesBalance_andStoresRunningBalance() {
        Account account = accountWithBalance("100.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        saveReturnsWhatItReceives();

        EntryResponse result = ledgerService.recordEntry(1L,
                new CreateEntryRequest(EntryType.DEBIT, new BigDecimal("30.00"), "Rent"));

        assertThat(result.balanceAfter()).isEqualByComparingTo("70.00");
        assertThat(account.getBalance()).isEqualByComparingTo("70.00");
    }

    @Test
    void debit_exactlyEqualToBalance_isAllowed() {
        Account account = accountWithBalance("50.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        saveReturnsWhatItReceives();

        EntryResponse result = ledgerService.recordEntry(1L,
                new CreateEntryRequest(EntryType.DEBIT, new BigDecimal("50.00"), "All out"));

        assertThat(result.balanceAfter()).isEqualByComparingTo("0.00");
    }

    @Test
    void debit_moreThanBalance_throwsInsufficientFunds_andSavesNothing() {
        Account account = accountWithBalance("20.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, () ->
                ledgerService.recordEntry(1L,
                        new CreateEntryRequest(EntryType.DEBIT, new BigDecimal("80.00"), "Too much")));

        verify(ledgerEntryRepository, never()).save(any());
        assertThat(account.getBalance()).isEqualByComparingTo("20.00");
    }

    @Test
    void unknownAccount_throwsAccountNotFound() {
        when(accountRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                ledgerService.recordEntry(99L,
                        new CreateEntryRequest(EntryType.CREDIT, new BigDecimal("10.00"), "x")));
    }
}
