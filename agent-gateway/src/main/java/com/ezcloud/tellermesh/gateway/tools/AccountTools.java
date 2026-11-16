package com.ezcloud.tellermesh.gateway.tools;

import com.ezcloud.tellermesh.gateway.corebanking.CoreBankingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

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
}
