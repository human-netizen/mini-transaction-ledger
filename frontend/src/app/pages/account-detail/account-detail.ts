import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Account } from '../../models/account.model';
import { CreateEntryRequest, EntryType, LedgerEntry } from '../../models/ledger-entry.model';
import { AccountService } from '../../services/account.service';
import { LedgerService } from '../../services/ledger.service';
import { toErrorMessage } from '../../services/api-error';

@Component({
  selector: 'app-account-detail',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe, DatePipe],
  templateUrl: './account-detail.html',
  styleUrl: './account-detail.css',
})
export class AccountDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly accountService = inject(AccountService);
  private readonly ledgerService = inject(LedgerService);

  private readonly accountId = Number(this.route.snapshot.paramMap.get('id'));

  readonly account = signal<Account | null>(null);
  readonly entries = signal<LedgerEntry[]>([]);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly submitting = signal(false);

  readonly form = new FormGroup({
    type: new FormControl<EntryType>('CREDIT', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    description: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
  });

  ngOnInit(): void {
    this.loadAccount();
    this.loadEntries();
  }

  loadAccount(): void {
    this.accountService.getById(this.accountId).subscribe({
      next: (account) => this.account.set(account),
      error: (err) => this.errorMessage.set(toErrorMessage(err)),
    });
  }

  loadEntries(): void {
    this.ledgerService.getEntries(this.accountId).subscribe({
      next: (entries) => this.entries.set(entries),
      error: (err) => this.errorMessage.set(toErrorMessage(err)),
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request: CreateEntryRequest = {
      type: value.type,
      amount: value.amount!,
      description: value.description,
    };

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.ledgerService.addEntry(this.accountId, request).subscribe({
      next: (entry) => {
        this.successMessage.set(`${entry.type} of ${entry.amount.toFixed(2)} recorded.`);
        this.form.reset();
        this.submitting.set(false);
        this.loadAccount();
        this.loadEntries();
      },
      error: (err) => {
        this.errorMessage.set(toErrorMessage(err));
        this.submitting.set(false);
      },
    });
  }
}
