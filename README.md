# TellerMesh

A secure multi-agent banking assistant built with Spring AI 2.0 and the Model Context Protocol.
A coordinator agent routes each question to a least-privilege specialist (Accounts, Transactions,
Service), and the specialists reach core banking only through JWT-secured MCP servers.

Design and roadmap: *Secure Multi-Agent Banking Assistant: Design and Spring AI Implementation Plan*.

## Status

Phase 0 (foundations): repo, core-banking mock, local stack and CI. Agents and MCP servers are
empty modules until Phases 1 to 3.

| Module | Purpose | Phase |
| --- | --- | --- |
| `core-banking-mock` | Stand-in core banking API on Postgres, seeded with two customers | 0 |
| `common-contracts` | Tool DTOs, error codes, audit event schema | 2 to 3 |
| `common-security` | JWT helpers, PII redaction, token vault | 4 |
| `agent-gateway` | Chat API, coordinator and specialist agents | 1 to 2 |
| `mcp-accounts`, `mcp-transactions`, `mcp-service` | MCP servers wrapping core banking | 3 |
| `eval-suite` | Golden set, red-team prompts, evaluators | 1 onward |

## Prerequisites

- JDK 21+, Maven 3.9+ (or the wrapper, below)
- Docker (Docker Desktop or Colima) with Compose v2
- Optional: Ollama installed natively for local models (faster than the container on a Mac)

## Quick start

```bash
mvn -N wrapper:wrapper          # once, to add ./mvnw
./mvnw verify                   # builds all modules; mock tests use Testcontainers
./scripts/smoke.sh              # starts the stack and runs the Phase 0 exit check
```

Run only the mock against a throwaway database, no compose needed:
`TestCoreBankingMockApplication` (in `core-banking-mock/src/test`), or
`./mvnw -pl core-banking-mock spring-boot:test-run`.

## Local services

| Service | URL | Credentials (dev only) |
| --- | --- | --- |
| Core banking mock | http://localhost:8090/api | none (internal service) |
| Keycloak admin | http://localhost:8180/admin | `admin` / `admin` |
| Keycloak realm | `tellermesh` | `alice` / `alice-dev-pass`, `bob` / `bob-dev-pass` |
| Postgres | localhost:5433 (override with `POSTGRES_PORT`) | `tellermesh` / `tellermesh` (DBs `corebanking`, `tellermesh`) |
| Redis | localhost:6379 | none |
| Ollama (profile `llm`) | http://localhost:11434 | none |

Test customers: Alice = `cust-1001`, Bob = `cust-1002`. Tokens carry a `customer_id` claim.
The realm defines `acr` levels `password` = 1 and `otp` = 2 for step-up in Phase 4.

## Core banking mock API

| Method | Path |
| --- | --- |
| GET | `/api/customers/{customerId}/accounts` |
| GET | `/api/accounts/{accountId}` |
| GET | `/api/accounts/{accountId}/balance` |
| GET | `/api/accounts/{accountId}/transactions?from=&to=&limit=` (default last 90 days, max 200) |
| GET | `/api/transactions/{transactionId}` |
| POST | `/api/accounts/{accountId}/cheque-book-requests` `{"leaves": 25, "deliveryAddress": "..."}` |
| GET | `/api/service-requests/{requestId}` |

The mock has no authentication by design: only MCP servers call it, and they decide who may see
which account. All identifiers in the seed data are fictional.

## Reset

```bash
docker compose down -v          # drops the Postgres volume; seeds re-run on next start
```
