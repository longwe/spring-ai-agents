# ADR 0001: Stack and versions

Date: 2026-10-25
Status: Accepted

## Decision

- Java 21, Spring Boot 4.1.x, Spring AI 2.0.x (BOM `org.springframework.ai:spring-ai-bom`).
- MCP security from `org.springaicommunity:mcp-*-security` 0.1.x, the line that targets Spring AI 2.0.
- Maven multi-module repo, one deployable per agent gateway and per MCP server.
- Keycloak for local identity; the bank's IdP in production.
- Postgres for core-banking mock data, chat memory and audit; Redis for the PII token vault and rate limits.

## Why

Spring AI 2.0 (GA June 12, 2026) requires Spring Boot 4 and ships MCP Java SDK 2.0 with MCP
annotations in core, plus a per-request tool-call limit on `ToolCallingAdvisor` (2.0.1).
Starting on 1.1.x would mean a Boot 3 to 4 migration mid-project.

## Consequences

- Jackson 3 (`tools.jackson`) and JSpecify null-safety throughout.
- Spring AI 2.1 is in milestones (Boot 4.2); upgrade per minor release behind the eval gate.
