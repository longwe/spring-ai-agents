package com.ezcloud.tellermesh.corebanking.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Read models returned by the core banking mock. */
public final class Models {

    private Models() {
    }

    public record Account(
            String accountId,
            String customerId,
            String accountNumber,
            String type,
            String currency,
            String status,
            LocalDate openedOn) {
    }

    public record Balance(
            String accountId,
            String currency,
            BigDecimal ledgerBalance,
            BigDecimal availableBalance,
            Instant asOf) {
    }

    public record Transaction(
            String transactionId,
            String accountId,
            Instant postedAt,
            BigDecimal amount,
            String direction,
            String description,
            String merchant,
            String category,
            String channel,
            String reference) {
    }

    public record ServiceRequest(
            String requestId,
            String accountId,
            String type,
            String status,
            Integer leaves,
            String deliveryAddress,
            Instant createdAt) {
    }
}
