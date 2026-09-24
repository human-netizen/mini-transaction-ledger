# Design: Mini Transaction Ledger

This is the plan I wrote before writing any code: what the app does, the rules it follows, the data model and the API contract.

## 1. User stories

1. As a user, I can **create an account** with a holder name. A new account starts with a balance of 0.
2. As a user, I can **see all accounts** with their current balances.
3. As a user, I can **record an entry** on an account: type (CREDIT or DEBIT), amount and description.
4. As a user, I can **see an account's statement**: every entry, oldest first, with the running balance after each one.
5. As a user, I get a **clear error message** when my input is invalid or a debit is bigger than the balance.

## 2. Scope

| In scope | Out of scope (future work) |
|---|---|
| Accounts: create, list, view | Login / authentication |
| Entries: record, list (statement) | Editing or deleting entries (see BR4) |
| Running balance | Multiple currencies |
| Validation and error messages | Transfers between accounts |
| Docker for everything | Pagination, search |

## 3. Business rules

| ID | Rule |
|---|---|
| BR1 | The amount must be **greater than 0** and have **at most 2 decimal places**. |
| BR2 | A **CREDIT adds** to the balance, a **DEBIT subtracts** from it. |
| BR3 | A debit that would make the balance **negative is rejected**. No overdrafts. |
| BR4 | Entries are **immutable**. The ledger is append-only: a mistake is fixed with a new reversing entry, never by editing or deleting. |
| BR5 | Each entry stores `balance_after` (a snapshot of the running balance) and the account stores its current `balance`. Both are written in **one database transaction**. |

Credit/debit follow the customer's point of view, like a bank statement: credit = money in, debit = money out.

## 4. Data model

```
accounts                               ledger_entries
────────────────────                   ─────────────────────────────
id           BIGINT PK      1 ─────< * id             BIGINT PK
holder_name  VARCHAR(100)              account_id     BIGINT FK → accounts.id
balance      NUMERIC(19,2)             type           VARCHAR(10)  'CREDIT' | 'DEBIT'
created_at   TIMESTAMPTZ               amount         NUMERIC(19,2)
                                       description    VARCHAR(255)
                                       balance_after  NUMERIC(19,2)
                                       created_at     TIMESTAMPTZ
```

- One account has many entries; every entry belongs to exactly one account.
- `NUMERIC(19,2)` stores exact decimals. Money is never stored as float/double.
- `ledger_entries.account_id` gets an index, because every statement query filters on it.

## 5. Key decision: how to get the running balance

| Option | How | Pros | Cons |
|---|---|---|---|
| A. Compute on read | Sum all entries every time a statement is loaded | Always consistent, no extra columns | Gets slower as the ledger grows |
| **B. Store a snapshot (chosen)** | When an entry is saved, compute the new balance, store it in `balance_after` and update `accounts.balance` | Cheap reads; the statement is a historical record, like a real bank statement | Needs a transaction and a row lock to stay correct under concurrent requests |

I chose **B** because this is how banks keep statements, reads stay cheap, and it forced me to handle transactions and locking properly.

## 6. API contract

Base path: `/api`. The prefix lets the dev proxy and Nginx send `/api/*` to the backend and everything else to Angular.

| Method | Path | Request body | Success | Errors |
|---|---|---|---|---|
| POST | `/api/accounts` | `{ "holderName": "Rahim" }` | **201** + Account | 400 |
| GET | `/api/accounts` | - | **200** + Account[] | - |
| GET | `/api/accounts/{id}` | - | **200** + Account | 404 |
| POST | `/api/accounts/{id}/entries` | `{ "type": "DEBIT", "amount": 30.00, "description": "Rent" }` | **201** + Entry | 400, 404, 422 |
| GET | `/api/accounts/{id}/entries` | - | **200** + Entry[] (oldest first) | 404 |

**Account**
```json
{ "id": 1, "holderName": "Rahim", "balance": 70.00, "createdAt": "2026-09-25T10:15:30Z" }
```

**Entry**
```json
{ "id": 2, "type": "DEBIT", "amount": 30.00, "description": "Rent",
  "balanceAfter": 70.00, "createdAt": "2026-09-25T10:16:02Z" }
```

**Error** (Problem Details, RFC 9457, supported by Spring out of the box):
```json
{ "type": "about:blank", "title": "Unprocessable Content", "status": 422,
  "detail": "Insufficient funds: balance is 70.00, debit is 1000.00",
  "instance": "/api/accounts/1/entries" }
```
Validation errors also carry an `errors` map, e.g. `"errors": { "amount": "must be greater than 0" }`.

| Code | Meaning in this app |
|---|---|
| 200 | A read succeeded |
| 201 | An account or entry was created |
| 400 | The request is invalid (bad JSON, empty name, negative amount) |
| 404 | The account id doesn't exist |
| 422 | The request is valid but a business rule rejects it (insufficient funds) |
| 500 | A bug, should never happen |

400 vs 422: a negative amount can never be accepted (400). A debit of 50 is fine with a balance of 100 but rejected with a balance of 20, so it depends on the current state (422).

## 7. Screens

**`/accounts`**
```
 Mini Ledger
 ─────────────────────────────────────────────
 Accounts
 Holder name: [______________]   [Create account]

  ID   Holder     Balance
  1    Rahim     1,250.00    View →
  2    Karim        70.00    View →
```

**`/accounts/1`**
```
 ← Back to accounts
 Rahim   Balance: 1,250.00
 ─────────────────────────────────────────────
 Type [CREDIT ▾]  Amount [_____]  Description [__________]  [Add entry]

  Date            Description   Debit     Credit     Balance
  25 Sep 10:01    Salary                  1,500.00   1,500.00
  25 Sep 10:05    Rent          250.00               1,250.00
```

The statement has separate Debit and Credit columns, like a real bank statement.
