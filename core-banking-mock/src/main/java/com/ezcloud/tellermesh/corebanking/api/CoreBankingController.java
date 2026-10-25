package com.ezcloud.tellermesh.corebanking.api;

import com.ezcloud.tellermesh.corebanking.domain.CoreBankingRepository;
import com.ezcloud.tellermesh.corebanking.domain.Models.Account;
import com.ezcloud.tellermesh.corebanking.domain.Models.Balance;
import com.ezcloud.tellermesh.corebanking.domain.Models.ServiceRequest;
import com.ezcloud.tellermesh.corebanking.domain.Models.Transaction;
import com.ezcloud.tellermesh.corebanking.domain.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Core banking API. Deliberately has no authentication: in TellerMesh only the MCP
 * servers call it, and they enforce which customer may see which account.
 */
@RestController
@RequestMapping("/api")
@Validated
public class CoreBankingController {

    private final CoreBankingRepository repository;

    public CoreBankingController(CoreBankingRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/customers/{customerId}/accounts")
    public List<Account> accounts(@PathVariable String customerId) {
        if (!repository.customerExists(customerId)) {
            throw new NotFoundException("Customer", customerId);
        }
        return repository.accountsForCustomer(customerId);
    }

    @GetMapping("/accounts/{accountId}")
    public Account account(@PathVariable String accountId) {
        return repository.account(accountId).orElseThrow(() -> new NotFoundException("Account", accountId));
    }

    @GetMapping("/accounts/{accountId}/balance")
    public Balance balance(@PathVariable String accountId) {
        return repository.balance(accountId).orElseThrow(() -> new NotFoundException("Account", accountId));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public List<Transaction> transactions(
            @PathVariable String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        account(accountId);
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(90);
        return repository.transactions(accountId, start, end, limit);
    }

    @GetMapping("/transactions/{transactionId}")
    public Transaction transaction(@PathVariable String transactionId) {
        return repository.transaction(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction", transactionId));
    }

    @PostMapping("/accounts/{accountId}/cheque-book-requests")
    public ResponseEntity<ServiceRequest> requestChequeBook(
            @PathVariable String accountId, @Valid @RequestBody ChequeBookRequest body) {
        Account account = account(accountId);
        if (!"CHECKING".equals(account.type())) {
            throw new IllegalArgumentException("Cheque books are only available for checking accounts");
        }
        ServiceRequest created = repository.createChequeBookRequest(accountId, body.leavesOrDefault(), body.deliveryAddress());
        return ResponseEntity.created(URI.create("/api/service-requests/" + created.requestId())).body(created);
    }

    @GetMapping("/service-requests/{requestId}")
    public ServiceRequest serviceRequest(@PathVariable String requestId) {
        return repository.serviceRequest(requestId)
                .orElseThrow(() -> new NotFoundException("Service request", requestId));
    }

    public record ChequeBookRequest(
            @Min(25) @Max(100) Integer leaves,
            @Size(max = 200) String deliveryAddress) {

        int leavesOrDefault() {
            return leaves != null ? leaves : 25;
        }
    }
}
