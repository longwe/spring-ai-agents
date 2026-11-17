package com.ezcloud.tellermesh.gateway.tools;

import com.ezcloud.tellermesh.gateway.corebanking.CoreBankingClient;
import com.ezcloud.tellermesh.gateway.corebanking.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AccountTools {

    private static final Logger log = LoggerFactory.getLogger(AccountTools.class);

    private final CoreBankingClient coreBankingClient;

    public AccountTools(CoreBankingClient coreBankingClient) {
        this.coreBankingClient = coreBankingClient;
    }

    @Tool(description = """
            Get the current balance of one bank account. Returns the available balance \
            (what the customer can spend now) and the ledger balance (which still includes \
            pending debits). Quote the available balance unless the customer asks for the ledger balance.""")
    public String getBalance(
            @ToolParam(description = "Account ID, for example acc-1001-chk") String accountRef) {
        log.info("Tool getBalance called with accountRef={}", accountRef);
        return coreBankingClient.balance(accountRef)
                .map(b -> "Account %s: available balance %s %s, ledger balance %s %s, as of %s"
                        .formatted(b.accountId(), b.availableBalance(), b.currency(),
                                b.ledgerBalance(), b.currency(), b.asOf()))
                .orElse("No account found with ID " + accountRef);
    }

    @Tool(description = """
            List recent transactions on one bank account, newest first. Each transaction has the \
            posting time, an amount that is always positive, a direction (DEBIT = money out, \
            CREDIT = money in), a description, merchant, category and channel. Use this for \
            questions about spending, deposits or specific payments. Without dates it covers \
            the last 90 days. An empty list means the account is unknown or had no activity \
            in that period.""")
    public List<Transaction> getRecentTransactions(
            @ToolParam(description = "Account ID, for example acc-1001-chk") String accountRef,
            @ToolParam(required = false, description = "Earliest posting date, ISO format YYYY-MM-DD")
            LocalDate from,
            @ToolParam(required = false, description = "Latest posting date, ISO format YYYY-MM-DD")
            LocalDate to,
            @ToolParam(required = false, description = "Maximum number of transactions, 1 to 200 (default 50)")
            Integer limit) {
        log.info("Tool getRecentTransactions called with accountRef={}, from={}, to={}, limit={}",
                accountRef, from, to, limit);
        return coreBankingClient.transactions(accountRef, from, to, limit);
    }
}
