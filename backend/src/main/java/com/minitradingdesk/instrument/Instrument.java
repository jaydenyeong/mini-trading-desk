package com.minitradingdesk.instrument;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "lot_size", nullable = false)
    private int lotSize;

    @Column(name = "last_price", nullable = false, precision = 19, scale = 5)
    private BigDecimal lastPrice;

    @Column(name = "last_price_time", nullable = false)
    private Instant lastPriceTime;

    @Column(nullable = false)
    private boolean halted;

    protected Instrument() {
        // for JPA
    }

    public Instrument(String symbol, String name, int lotSize, BigDecimal lastPrice, Instant lastPriceTime) {
        this.symbol = symbol;
        this.name = name;
        this.lotSize = lotSize;
        this.lastPrice = lastPrice;
        this.lastPriceTime = lastPriceTime;
    }

    public void updatePrice(BigDecimal price, Instant time) {
        this.lastPrice = price;
        this.lastPriceTime = time;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public int getLotSize() {
        return lotSize;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public Instant getLastPriceTime() {
        return lastPriceTime;
    }

    public boolean isHalted() {
        return halted;
    }

    public void setHalted(boolean halted) {
        this.halted = halted;
    }
}