package com.ezcloud.tellermesh.gateway.corebanking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class CoreBankingClient {

    private final RestClient restClient;

    public CoreBankingClient(RestClient.Builder builder,
                             @Value("${tellermesh.core-banking.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public Optional<Balance> balance(String accountId) {
        try {
            var balance = restClient.get()
                    .uri("/api/accounts/{accountId}/balance", accountId)
                    .retrieve()
                    .body(Balance.class);
            return Optional.ofNullable(balance);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    /**
     * Transactions for one account, newest first. {@code from}, {@code to} and {@code limit} may be
     * null, in which case the core banking defaults apply (last 90 days, 50 rows). Returns an empty
     * list when the account is unknown or has no activity in the window.
     */
    public List<Transaction> transactions(String accountId, LocalDate from, LocalDate to, Integer limit) {
        try {
            var transactions = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/accounts/{accountId}/transactions")
                            .queryParamIfPresent("from", Optional.ofNullable(from))
                            .queryParamIfPresent("to", Optional.ofNullable(to))
                            .queryParamIfPresent("limit", Optional.ofNullable(limit))
                            .build(accountId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Transaction>>() {});
            return Optional.ofNullable(transactions).orElse(List.of());
        } catch (HttpClientErrorException.NotFound e) {
            return List.of();
        }
    }

}
