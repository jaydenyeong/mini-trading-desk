package com.minitradingdesk.instrument;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    List<Instrument> findAllByOrderBySymbolAsc();

    Optional<Instrument> findBySymbol(String symbol);
}