package com.minitradingdesk.order;

import com.minitradingdesk.account.Account;
import com.minitradingdesk.instrument.Instrument;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders") // "order" is a reserved word in SQL
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 6)
    private OrderType type;

    @Column(nullable = false)
    private long quantity;

    @Column(precision = 19, scale = 5)
    private BigDecimal price;

    @Column(name = "filled_quantity", nullable = false)
    private long filledQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Order() {
        // for JPA
    }

    public Order(Account account, Instrument instrument, OrderSide side, OrderType type,
                 long quantity, BigDecimal price, Instant now) {
        this.account = account;
        this.instrument = instrument;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.filledQuantity = 0;
        this.status = OrderStatus.NEW;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public long getRemainingQuantity() {
        return quantity - filledQuantity;
    }

    public boolean isOpen() {
        return status.isOpen();
    }

    public void reject(String reason, Instant now) {
        this.status = OrderStatus.REJECTED;
        this.rejectReason = reason;
        this.updatedAt = now;
    }

    public void changeStatus(OrderStatus newStatus, Instant now) {
        this.status = newStatus;
        this.updatedAt = now;
    }

    public void addFill(long fillQuantity, Instant now) {
        if (fillQuantity <= 0 || fillQuantity > getRemainingQuantity()) {
            throw new IllegalArgumentException(
                    "Fill quantity " + fillQuantity + " is invalid; remaining is " + getRemainingQuantity());
        }
        this.filledQuantity += fillQuantity;
        this.status = filledQuantity == quantity ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
        this.updatedAt = now;
    }

    public void amend(long newQuantity, BigDecimal newPrice, Instant now) {
        this.quantity = newQuantity;
        this.price = newPrice;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public long getFilledQuantity() {
        return filledQuantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}