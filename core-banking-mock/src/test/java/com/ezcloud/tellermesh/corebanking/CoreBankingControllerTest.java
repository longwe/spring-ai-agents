package com.ezcloud.tellermesh.corebanking;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CoreBankingControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void bothTestCustomersHaveTwoAccounts() throws Exception {
        mvc.perform(get("/api/customers/cust-1001/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].customerId", everyItem(is("cust-1001"))));
        mvc.perform(get("/api/customers/cust-1002/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].customerId", everyItem(is("cust-1002"))));
    }

    @Test
    void unknownCustomerIs404() throws Exception {
        mvc.perform(get("/api/customers/cust-9999/accounts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString("cust-9999")));
    }

    @Test
    void balanceReturnsLedgerAndAvailable() throws Exception {
        mvc.perform(get("/api/accounts/acc-1001-chk/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ledgerBalance", is(4215.37)))
                .andExpect(jsonPath("$.availableBalance", is(4015.37)))
                .andExpect(jsonPath("$.currency", is("USD")));
    }

    @Test
    void recentTransactionsIncludeTheUnfamiliarDebit() throws Exception {
        mvc.perform(get("/api/accounts/acc-1001-chk/transactions").param("limit", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(15))))
                .andExpect(jsonPath("$[1].description", containsString("PAYPAL *DGTLSVCS")));
    }

    @Test
    void transactionLimitIsValidated() throws Exception {
        mvc.perform(get("/api/accounts/acc-1001-chk/transactions").param("limit", "500"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void singleTransactionLookup() throws Exception {
        mvc.perform(get("/api/transactions/txn-b-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId", is("acc-1002-chk")))
                .andExpect(jsonPath("$.direction", is("DEBIT")));
    }

    @Test
    void chequeBookRequestRoundTrip() throws Exception {
        String location = mvc.perform(post("/api/accounts/acc-1002-chk/cheque-book-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leaves\": 50}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status", is("SUBMITTED")))
                .andExpect(jsonPath("$.leaves", is(50)))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("CHEQUE_BOOK")));
    }

    @Test
    void chequeBookRejectedForSavings() throws Exception {
        mvc.perform(post("/api/accounts/acc-1001-sav/cheque-book-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(422));
    }
}
