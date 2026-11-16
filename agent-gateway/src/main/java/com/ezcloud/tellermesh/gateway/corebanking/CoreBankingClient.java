package com.ezcloud.tellermesh.gateway.corebanking;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
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
}
