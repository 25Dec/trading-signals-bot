package com.tnhandev.market_data_service.response;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class GetKlineDataByTickerResponse {
    private boolean success;
    private int code;
    private Map<String, List<Number>> data;
}
