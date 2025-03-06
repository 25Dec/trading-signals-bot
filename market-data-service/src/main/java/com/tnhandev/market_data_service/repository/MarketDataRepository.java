package com.tnhandev.market_data_service.repository;

import com.tnhandev.market_data_service.model.Ticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketDataRepository extends JpaRepository<Ticker, Integer> {
}
