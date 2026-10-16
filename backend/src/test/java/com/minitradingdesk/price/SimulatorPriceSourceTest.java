package com.minitradingdesk.price;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.minitradingdesk.instrument.Instrument;
import com.minitradingdesk.instrument.InstrumentRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SimulatorPriceSourceTest {
    private final InstrumentRepository repository = mock(InstrumentRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneOffset.UTC);
    private final SimulatorPriceSource simulator = new SimulatorPriceSource(repository, clock, new Random(42));

    @Test
    void nextPriceMovesAtMostOneTenthOfAPercentAndKeepsFiveDecimals() {
        BigDecimal start = new BigDecimal("1.10000");
        BigDecimal maxMove = new BigDecimal("0.00111");

        for (int i = 0; i < 1_000; i++) {
            BigDecimal next = simulator.nextPrice(start);

            assertThat(next.scale()).isEqualTo(5);
            assertThat(next.subtract(start).abs()).isLessThanOrEqualTo(maxMove);
            assertThat(next).isPositive();
        }
    }

    @Test
    void priceNeverReachesZeroEvenWhenTiny() {
        BigDecimal price = new BigDecimal("0.00001");

        for (int i = 0; i < 1_000; i++) {
            price = simulator.nextPrice(price);
            assertThat(price).isPositive();
        }
    }

    @Test
    void tickUpdatesEveryInstrumentPriceAndTimeAndSaves() {
        Instrument eurusd = new Instrument("EURUSD", "Euro / US Dollar", 100,
                new BigDecimal("1.08500"), Instant.parse("2026-10-12T09:00:00Z"));
        when(repository.findAll()).thenReturn(List.of(eurusd));

        simulator.tick();

        assertThat(eurusd.getLastPriceTime()).isEqualTo(clock.instant());
        assertThat(eurusd.getLastPrice().scale()).isEqualTo(5);
        verify(repository).saveAll(List.of(eurusd));
    }

    @Test
    void nameIsSimulator() {
        assertThat(simulator.name()).isEqualTo("simulator");
    }
}