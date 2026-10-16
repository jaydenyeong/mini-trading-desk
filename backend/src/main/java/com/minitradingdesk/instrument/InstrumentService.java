package com.minitradingdesk.instrument;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstrumentService {
    private final InstrumentRepository instruments;

    public InstrumentService(InstrumentRepository instruments) {
        this.instruments = instruments;
    }

    @Transactional(readOnly = true)
    public List<InstrumentDto> listInstruments() {
        return instruments.findAllByOrderBySymbolAsc().stream()
                .map(InstrumentDto::from)
                .toList();
    }
}