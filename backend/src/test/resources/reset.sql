DELETE FROM audit_log;
DELETE FROM executions;
DELETE FROM orders;
DELETE FROM positions;
UPDATE accounts SET cash_balance = 1000000.00000;
UPDATE instruments
SET halted          = FALSE,
    last_price_time = now(),
    last_price      = CASE symbol
                          WHEN 'EURUSD' THEN 1.08500
                          WHEN 'GBPUSD' THEN 1.27000
                          WHEN 'USDJPY' THEN 149.50000
                          WHEN 'USDCHF' THEN 0.86000
                          WHEN 'AUDUSD' THEN 0.66000
                          WHEN 'USDCAD' THEN 1.36000
                          WHEN 'NZDUSD' THEN 0.60000
                          WHEN 'EURGBP' THEN 0.85500
                          WHEN 'EURJPY' THEN 162.20000
                          WHEN 'GBPJPY' THEN 189.80000
                          WHEN 'XAUUSD' THEN 2650.00000
                          WHEN 'XAGUSD' THEN 31.00000
                          ELSE last_price
        END;