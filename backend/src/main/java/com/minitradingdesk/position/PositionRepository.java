package com.minitradingdesk.position;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByAccountIdAndInstrumentId(Long accountId, Long instrumentId);
}