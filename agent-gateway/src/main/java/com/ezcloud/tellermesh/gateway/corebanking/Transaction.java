package com.ezcloud.tellermesh.gateway.corebanking;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(String transactionId, String accountId, Instant postedAt, BigDecimal amount,
                          String direction, String description, String merchant, String category,
                          String channel, String reference) {
}
