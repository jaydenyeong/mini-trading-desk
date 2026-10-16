package com.minitradingdesk.instrument;

import java.math.BigDecimal;
import java.time.Instant;

public record InstrumentDto(
        Long Id,
        String symbol,
        String name,
        int lotSize,
        BigDecimal lastPrice,
        Instant lastPriceTime,
        boolean halted) {
    public static InstrumentDto from(Instrument instrument) {
        return new InstrumentDto(
                instrument.getId(),
                instrument.getSymbol(),
                instrument.getName(),
                instrument.getLotSize(),
                instrument.getLastPrice(),
                instrument.getLastPriceTime(),
                instrument.isHalted()
        );
    }
}