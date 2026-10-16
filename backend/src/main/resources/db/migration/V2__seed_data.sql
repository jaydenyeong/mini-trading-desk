CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (username, password_hash, role) VALUES
                                                      ('trader1', crypt('password', gen_salt('bf', 10)), 'TRADER'),
                                                      ('trader2', crypt('password', gen_salt('bf', 10)), 'TRADER'),
                                                      ('admin',   crypt('password', gen_salt('bf', 10)), 'ADMIN');

INSERT INTO accounts (user_id, cash_balance)
SELECT id, 1000000.00000 FROM users WHERE role = 'TRADER';

INSERT INTO instruments (symbol, name, lot_size, last_price, last_price_time) VALUES
                                                                                  ('EURUSD', 'Euro / US Dollar',               100,    1.08500, now()),
                                                                                  ('GBPUSD', 'British Pound / US Dollar',      100,    1.27000, now()),
                                                                                  ('USDJPY', 'US Dollar / Japanese Yen',       100,  149.50000, now()),
                                                                                  ('USDCHF', 'US Dollar / Swiss Franc',        100,    0.86000, now()),
                                                                                  ('AUDUSD', 'Australian Dollar / US Dollar',  100,    0.66000, now()),
                                                                                  ('USDCAD', 'US Dollar / Canadian Dollar',    100,    1.36000, now()),
                                                                                  ('NZDUSD', 'New Zealand Dollar / US Dollar', 100,    0.60000, now()),
                                                                                  ('EURGBP', 'Euro / British Pound',           100,    0.85500, now()),
                                                                                  ('EURJPY', 'Euro / Japanese Yen',            100,  162.20000, now()),
                                                                                  ('GBPJPY', 'British Pound / Japanese Yen',   100,  189.80000, now()),
                                                                                  ('XAUUSD', 'Gold / US Dollar',               100, 2650.00000, now()),
                                                                                  ('XAGUSD', 'Silver / US Dollar',             100,   31.00000, now());