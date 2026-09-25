package com.niloy.ledger.repository;

import com.niloy.ledger.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
