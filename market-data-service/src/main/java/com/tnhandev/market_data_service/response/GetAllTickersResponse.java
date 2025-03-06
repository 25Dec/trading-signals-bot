package com.tnhandev.market_data_service.response;

import com.tnhandev.market_data_service.model.Ticker;
import lombok.Getter;

import java.util.List;

@Getter
public class GetAllTickersResponse {
    private boolean success;
    private int code;
    private List<Ticker> data;
}
