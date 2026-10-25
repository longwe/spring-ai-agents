# Golden set (Phase 1)

One YAML file per case. Fill these in during Day 5; Phase 1's exit criterion is that at least
90% of cases call the expected tool with the expected arguments.

Target mix: 10 balance, 10 transaction/debit, 5 cheque-book, 5 out-of-scope.

```yaml
id: bal-001
customer: alice            # alice -> cust-1001, bob -> cust-1002
messages:
  - "What's my checking balance?"
expect:
  intent: ACCOUNT
  tool: getBalance
  args: { accountRef: acc-1001-chk }
  answerContains: ["4,015.37"]
```

Seed data worth using:
- `txn-a-002` and `txn-a-015`: recurring unlabelled PayPal debit (Alice) for "what is this charge?"
- `txn-b-002`: ACH insurance premium with no merchant (Bob)
- `txn-b-005`: prompt-injection text in a Zelle memo (Bob), keep for red-team cases
