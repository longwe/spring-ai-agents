#!/usr/bin/env bash
# Phase 0 exit check: the stack starts, both test customers can sign in,
# and the core banking mock returns their accounts and balances.
#   ./scripts/smoke.sh            start (or reuse) the stack, then check
#   ./scripts/smoke.sh --no-up    only check a stack that is already running
set -euo pipefail

KC=${KC:-http://localhost:8180}
MOCK=${MOCK:-http://localhost:8090}
REALM=tellermesh
CLIENT=tellermesh-dev-cli

pass() { printf '  \033[32mPASS\033[0m %s\n' "$1"; }
fail() { printf '  \033[31mFAIL\033[0m %s\n' "$1"; exit 1; }
json_field() { sed -n "s/.*\"$1\" *: *\"\\([^\"]*\\)\".*/\\1/p"; }

if [[ "${1:-}" != "--no-up" ]]; then
  echo "Starting stack (first run builds the mock image and can take a few minutes)..."
  docker compose up -d --build --wait
fi

echo "Checking services"
curl -fsS "$MOCK/actuator/health" | grep -q '"UP"' && pass "core-banking-mock healthy" || fail "core-banking-mock not healthy"
curl -fsS "$KC/realms/$REALM/.well-known/openid-configuration" >/dev/null && pass "Keycloak realm '$REALM' published" || fail "Keycloak realm missing"

for user in alice bob; do
  echo "Customer: $user"
  token=$(curl -fsS -X POST "$KC/realms/$REALM/protocol/openid-connect/token" \
      -d grant_type=password -d client_id=$CLIENT -d scope=openid \
      -d username=$user -d password=$user-dev-pass | json_field access_token)
  [[ -n "$token" ]] && pass "signed in" || fail "no token for $user"

  customer_id=$(curl -fsS -H "Authorization: Bearer $token" \
      "$KC/realms/$REALM/protocol/openid-connect/userinfo" | json_field customer_id)
  [[ -n "$customer_id" ]] && pass "token carries customer_id=$customer_id" || fail "customer_id claim missing"

  accounts=$(curl -fsS "$MOCK/api/customers/$customer_id/accounts")
  count=$(grep -o '"accountId"' <<<"$accounts" | wc -l | tr -d ' ')
  [[ "$count" -ge 1 ]] && pass "$count accounts returned" || fail "no accounts for $customer_id"

  first_account=$(json_field accountId <<<"$(tr ',' '\n' <<<"$accounts" | grep accountId | head -1)")
  curl -fsS "$MOCK/api/accounts/$first_account/balance" | grep -q availableBalance \
      && pass "balance for $first_account" || fail "balance lookup failed"
  txns=$(curl -fsS "$MOCK/api/accounts/$first_account/transactions" | grep -o '"transactionId"' | wc -l | tr -d ' ')
  [[ "$txns" -ge 1 ]] && pass "$txns recent transactions" || fail "no transactions"
done

echo "Phase 0 smoke test passed."
