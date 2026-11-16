package com.ezcloud.tellermesh.gateway.corebanking;

import java.math.BigDecimal;
import java.time.Instant;

public record Balance(
        String accountId,
        String currency,
        BigDecimal ledgerBalance,
        BigDecimal availableBalance,
        Instant asOf) {
}
