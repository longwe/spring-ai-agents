package com.ezcloud.tellermesh.corebanking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Stand-in for the bank's core systems. It trusts its callers: authorization is
 * enforced by the MCP servers in front of it, exactly as a real core system sits
 * behind the bank's internal firewall.
 */
@SpringBootApplication
public class CoreBankingMockApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBankingMockApplication.class, args);
    }
}
