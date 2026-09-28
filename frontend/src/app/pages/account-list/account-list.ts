import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Account } from '../../models/account.model';
import { AccountService } from '../../services/account.service';
import { toErrorMessage } from '../../services/api-error';

@Component({
  selector: 'app-account-list',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  templateUrl: './account-list.html',
  styleUrl: './account-list.css',
})
export class AccountList implements OnInit {
  private readonly accountService = inject(AccountService);

  readonly accounts = signal<Account[]>([]);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly search = signal('');

  readonly filteredAccounts = computed(() => {
    const term = this.search().trim().toLowerCase();
    return this.accounts().filter((account) => account.holderName.toLowerCase().includes(term));
  });

  readonly form = new FormGroup({
    holderName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
  });

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.accountService.getAll().subscribe({
      next: (accounts) => this.accounts.set(accounts),
      error: (err) => this.errorMessage.set(toErrorMessage(err)),
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.accountService.create(this.form.getRawValue()).subscribe({
      next: (account) => {
        this.successMessage.set(`Account created for ${account.holderName}.`);
        this.form.reset();
        this.submitting.set(false);
        this.loadAccounts();
      },
      error: (err) => {
        this.errorMessage.set(toErrorMessage(err));
        this.submitting.set(false);
      },
    });
  }
}
