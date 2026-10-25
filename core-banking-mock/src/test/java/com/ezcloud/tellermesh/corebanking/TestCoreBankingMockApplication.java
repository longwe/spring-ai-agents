package com.ezcloud.tellermesh.corebanking;

import org.springframework.boot.SpringApplication;

/** Runs the mock against a throwaway Postgres container: no docker-compose needed. */
public class TestCoreBankingMockApplication {

    public static void main(String[] args) {
        SpringApplication.from(CoreBankingMockApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
