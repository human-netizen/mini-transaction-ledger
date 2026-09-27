import { Component, OnInit, inject } from '@angular/core';
import { AccountService } from '../../services/account.service';

@Component({
  selector: 'app-account-list',
  imports: [],
  templateUrl: './account-list.html',
  styleUrl: './account-list.css',
})
export class AccountList implements OnInit {
  private readonly accountService = inject(AccountService);

  ngOnInit(): void {
    this.accountService.getAll().subscribe((accounts) => console.log('Accounts:', accounts));
  }
}
