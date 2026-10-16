package com.minitradingdesk.account;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, Long>{
    @Query("select a from Account a join a.user u where u.username = :username")
    Optional<Account> findByUsername(String username);
}