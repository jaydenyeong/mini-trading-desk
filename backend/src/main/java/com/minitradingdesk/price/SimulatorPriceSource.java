package com.minitradingdesk.price;

import com.minitradingdesk.instrument.Instrument;
import com.minitradingdesk.instrument.InstrumentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "price.source", havingValue = "simulator", matchIfMissing = true)
public class SimulatorPriceSource implements PriceSource {
    private static final Logger log = LoggerFactory.getLogger(SimulatorPriceSource.class);
    private static final double MAX_STEP = 0.001;
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.00001");

    private final InstrumentRepository instruments;
    private final Clock clock;
    private final Random random;

    @Autowired
    public SimulatorPriceSource(InstrumentRepository instruments, Clock clock) {
        this(instruments, clock, new Random());
    }

    SimulatorPriceSource(InstrumentRepository instruments, Clock clock, Random random) {
        this.instruments = instruments;
        this.clock = clock;
        this.random = random;
    }

    @Override
    public String name() {
        return "simulator";
    }

    @Scheduled(fixedRateString = "${price.simulator.interval-ms:2000}")
    public void tick() {
        List<Instrument> all = instruments.findAll();
        Instant now = clock.instant();
        for (Instrument instrument : all) {
            instrument.updatePrice(nextPrice(instrument.getLastPrice()), now);
        }
        instruments.saveAll(all);
        log.debug("Simulated new prices for {} instruments", all.size());
    }

    BigDecimal nextPrice(BigDecimal current) {
        double change = (random.nextDouble() * 2 - 1) * MAX_STEP;
        BigDecimal next = current
                .multiply(BigDecimal.valueOf(1 + change))
                .setScale(5, RoundingMode.HALF_UP);
        return next.signum() > 0 ? next : MIN_PRICE;
    }
}