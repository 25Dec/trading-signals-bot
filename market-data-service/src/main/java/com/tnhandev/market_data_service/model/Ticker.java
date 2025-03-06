package com.tnhandev.market_data_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Ticker {
    @Id
    private Integer id;
    private String symbol;
}
