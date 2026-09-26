package com.niloy.ledger.controller;

import com.niloy.ledger.dto.CreateEntryRequest;
import com.niloy.ledger.dto.EntryResponse;
import com.niloy.ledger.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts/{accountId}/entries")
@RequiredArgsConstructor
public class LedgerEntryController {

    private final LedgerService ledgerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntryResponse recordEntry(@PathVariable("accountId") Long accountId,
                                     @Valid @RequestBody CreateEntryRequest request) {
        return ledgerService.recordEntry(accountId, request);
    }

    @GetMapping
    public List<EntryResponse> getStatement(@PathVariable("accountId") Long accountId) {
        return ledgerService.getStatement(accountId);
    }
}
