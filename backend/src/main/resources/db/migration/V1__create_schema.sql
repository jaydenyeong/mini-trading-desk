CREATE TABLE users (
                       id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       username      VARCHAR(50)  NOT NULL UNIQUE,
                       password_hash VARCHAR(100) NOT NULL,
                       role          VARCHAR(20)  NOT NULL CHECK (role IN ('TRADER', 'ADMIN'))
);

CREATE TABLE accounts (
                          id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          user_id      BIGINT         NOT NULL UNIQUE REFERENCES users (id),
                          cash_balance NUMERIC(19, 5) NOT NULL CHECK (cash_balance >= 0)
);

CREATE TABLE instruments (
                             id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             symbol          VARCHAR(20)    NOT NULL UNIQUE,
                             name            VARCHAR(100)   NOT NULL,
                             lot_size        INTEGER        NOT NULL DEFAULT 100 CHECK (lot_size > 0),
                             last_price      NUMERIC(19, 5) NOT NULL CHECK (last_price > 0),
                             last_price_time TIMESTAMPTZ    NOT NULL,
                             halted          BOOLEAN        NOT NULL DEFAULT FALSE
);

CREATE TABLE orders (
                        id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        account_id      BIGINT         NOT NULL REFERENCES accounts (id),
                        instrument_id   BIGINT         NOT NULL REFERENCES instruments (id),
                        side            VARCHAR(4)     NOT NULL CHECK (side IN ('BUY', 'SELL')),
                        order_type      VARCHAR(6)     NOT NULL CHECK (order_type IN ('MARKET', 'LIMIT')),
                        quantity        BIGINT         NOT NULL CHECK (quantity > 0),
                        price           NUMERIC(19, 5) CHECK (price > 0),
                        filled_quantity BIGINT         NOT NULL DEFAULT 0,
                        status          VARCHAR(20)    NOT NULL
                            CHECK (status IN ('NEW', 'PARTIALLY_FILLED', 'FILLED', 'CANCELLED', 'REJECTED')),
                        reject_reason   VARCHAR(255),
                        created_at      TIMESTAMPTZ    NOT NULL,
                        updated_at      TIMESTAMPTZ    NOT NULL,
                        version         BIGINT         NOT NULL DEFAULT 0,
                        CHECK (filled_quantity >= 0 AND filled_quantity <= quantity)
);

-- Blotter: "my orders today". Phase 12 scenario 2 drops this index on purpose.
CREATE INDEX idx_orders_account_created ON orders (account_id, created_at);
-- Fill simulator: "all open orders". Partial index = only open rows are indexed, so it stays tiny.
CREATE INDEX idx_orders_open ON orders (id) WHERE status IN ('NEW', 'PARTIALLY_FILLED');

CREATE TABLE executions (
                            id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            order_id    BIGINT         NOT NULL REFERENCES orders (id),
                            quantity    BIGINT         NOT NULL CHECK (quantity > 0),
                            price       NUMERIC(19, 5) NOT NULL CHECK (price > 0),
                            executed_at TIMESTAMPTZ    NOT NULL
);
CREATE INDEX idx_executions_order ON executions (order_id);

CREATE TABLE positions (
                           id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           account_id    BIGINT         NOT NULL REFERENCES accounts (id),
                           instrument_id BIGINT         NOT NULL REFERENCES instruments (id),
                           quantity      BIGINT         NOT NULL CHECK (quantity >= 0),
                           average_cost  NUMERIC(19, 5) NOT NULL CHECK (average_cost >= 0),
                           UNIQUE (account_id, instrument_id)
);

CREATE TABLE audit_log (
                           id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           order_id   BIGINT       NOT NULL REFERENCES orders (id),
                           old_status VARCHAR(20),
                           new_status VARCHAR(20)  NOT NULL,
                           reason     VARCHAR(255) NOT NULL,
                           created_at TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_audit_order ON audit_log (order_id);