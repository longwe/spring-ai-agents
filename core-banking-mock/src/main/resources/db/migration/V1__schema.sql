CREATE TABLE customers (
    customer_id  VARCHAR(32) PRIMARY KEY,
    full_name    VARCHAR(120) NOT NULL,
    email        VARCHAR(160) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE accounts (
    account_id         VARCHAR(32) PRIMARY KEY,
    customer_id        VARCHAR(32) NOT NULL REFERENCES customers (customer_id),
    account_number     VARCHAR(20) NOT NULL UNIQUE,
    type               VARCHAR(16) NOT NULL CHECK (type IN ('CHECKING', 'SAVINGS')),
    currency           CHAR(3)     NOT NULL DEFAULT 'USD',
    status             VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    ledger_balance     NUMERIC(14, 2) NOT NULL,
    available_balance  NUMERIC(14, 2) NOT NULL,
    opened_on          DATE NOT NULL
);
CREATE INDEX idx_accounts_customer ON accounts (customer_id);

CREATE TABLE transactions (
    transaction_id  VARCHAR(32) PRIMARY KEY,
    account_id      VARCHAR(32) NOT NULL REFERENCES accounts (account_id),
    posted_at       TIMESTAMPTZ NOT NULL,
    amount          NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    direction       VARCHAR(6)  NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')),
    description     VARCHAR(255) NOT NULL,
    merchant        VARCHAR(120),
    category        VARCHAR(40),
    channel         VARCHAR(20) NOT NULL,
    reference       VARCHAR(40)
);
CREATE INDEX idx_transactions_account_posted ON transactions (account_id, posted_at DESC);

CREATE TABLE service_requests (
    request_id        VARCHAR(32) PRIMARY KEY,
    account_id        VARCHAR(32) NOT NULL REFERENCES accounts (account_id),
    type              VARCHAR(24) NOT NULL,
    status            VARCHAR(16) NOT NULL,
    leaves            INTEGER,
    delivery_address  VARCHAR(200),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
