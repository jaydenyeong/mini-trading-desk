package com.minitradingdesk.order;

import java.util.Set;

public enum OrderStatus {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED;

    public static final Set<OrderStatus> OPEN = Set.of(NEW, PARTIALLY_FILLED);

    public boolean isOpen() {
        return OPEN.contains(this);
    }
}