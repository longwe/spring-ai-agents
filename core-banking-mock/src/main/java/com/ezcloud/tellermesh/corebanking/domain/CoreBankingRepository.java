package com.ezcloud.tellermesh.corebanking.domain;

import com.ezcloud.tellermesh.corebanking.domain.Models.Account;
import com.ezcloud.tellermesh.corebanking.domain.Models.Balance;
import com.ezcloud.tellermesh.corebanking.domain.Models.ServiceRequest;
import com.ezcloud.tellermesh.corebanking.domain.Models.Transaction;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CoreBankingRepository {

    private final JdbcClient jdbc;

    public CoreBankingRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public boolean customerExists(String customerId) {
        return jdbc.sql("SELECT count(*) FROM customers WHERE customer_id = ?")
                .param(customerId)
                .query(Integer.class)
                .single() > 0;
    }

    public List<Account> accountsForCustomer(String customerId) {
        return jdbc.sql("""
                        SELECT account_id, customer_id, account_number, type, currency, status, opened_on
                        FROM accounts WHERE customer_id = ? ORDER BY account_id""")
                .param(customerId)
                .query(CoreBankingRepository::account)
                .list();
    }

    public Optional<Account> account(String accountId) {
        return jdbc.sql("""
                        SELECT account_id, customer_id, account_number, type, currency, status, opened_on
                        FROM accounts WHERE account_id = ?""")
                .param(accountId)
                .query(CoreBankingRepository::account)
                .optional();
    }

    public Optional<Balance> balance(String accountId) {
        return jdbc.sql("""
                        SELECT account_id, currency, ledger_balance, available_balance
                        FROM accounts WHERE account_id = ?""")
                .param(accountId)
                .query((rs, n) -> new Balance(
                        rs.getString("account_id"),
                        rs.getString("currency"),
                        rs.getBigDecimal("ledger_balance"),
                        rs.getBigDecimal("available_balance"),
                        Instant.now()))
                .optional();
    }

    public List<Transaction> transactions(String accountId, LocalDate from, LocalDate to, int limit) {
        return jdbc.sql("""
                        SELECT * FROM transactions
                        WHERE account_id = :accountId
                          AND posted_at >= :from
                          AND posted_at < :toExclusive
                        ORDER BY posted_at DESC
                        LIMIT :limit""")
                .param("accountId", accountId)
                .param("from", from.atStartOfDay().atOffset(ZoneOffset.UTC))
                .param("toExclusive", to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .param("limit", limit)
                .query(CoreBankingRepository::transaction)
                .list();
    }

    public Optional<Transaction> transaction(String transactionId) {
        return jdbc.sql("SELECT * FROM transactions WHERE transaction_id = ?")
                .param(transactionId)
                .query(CoreBankingRepository::transaction)
                .optional();
    }

    @Transactional
    public ServiceRequest createChequeBookRequest(String accountId, int leaves, String deliveryAddress) {
        String requestId = "SR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        jdbc.sql("""
                        INSERT INTO service_requests (request_id, account_id, type, status, leaves, delivery_address)
                        VALUES (?, ?, 'CHEQUE_BOOK', 'SUBMITTED', ?, ?)""")
                .params(requestId, accountId, leaves, deliveryAddress)
                .update();
        return serviceRequest(requestId).orElseThrow();
    }

    public Optional<ServiceRequest> serviceRequest(String requestId) {
        return jdbc.sql("SELECT * FROM service_requests WHERE request_id = ?")
                .param(requestId)
                .query((rs, n) -> new ServiceRequest(
                        rs.getString("request_id"),
                        rs.getString("account_id"),
                        rs.getString("type"),
                        rs.getString("status"),
                        (Integer) rs.getObject("leaves"),
                        rs.getString("delivery_address"),
                        rs.getTimestamp("created_at").toInstant()))
                .optional();
    }

    private static Account account(ResultSet rs, int rowNum) throws SQLException {
        return new Account(
                rs.getString("account_id"),
                rs.getString("customer_id"),
                rs.getString("account_number"),
                rs.getString("type"),
                rs.getString("currency"),
                rs.getString("status"),
                rs.getDate("opened_on").toLocalDate());
    }

    private static Transaction transaction(ResultSet rs, int rowNum) throws SQLException {
        return new Transaction(
                rs.getString("transaction_id"),
                rs.getString("account_id"),
                rs.getTimestamp("posted_at").toInstant(),
                rs.getBigDecimal("amount"),
                rs.getString("direction"),
                rs.getString("description"),
                rs.getString("merchant"),
                rs.getString("category"),
                rs.getString("channel"),
                rs.getString("reference"));
    }
}
