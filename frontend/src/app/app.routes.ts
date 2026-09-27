import { Routes } from '@angular/router';
import { AccountList } from './pages/account-list/account-list';
import { AccountDetail } from './pages/account-detail/account-detail';

export const routes: Routes = [
  { path: '', redirectTo: 'accounts', pathMatch: 'full' },
  { path: 'accounts', component: AccountList },
  { path: 'accounts/:id', component: AccountDetail },
  { path: '**', redirectTo: 'accounts' },
];
