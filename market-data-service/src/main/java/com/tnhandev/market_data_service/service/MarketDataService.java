package com.tnhandev.market_data_service.service;

import com.google.protobuf.Empty;
import com.tnhandev.market_data_service.repository.MarketDataRepository;
import com.tnhandev.proto.GetAllTickersResponse;
import com.tnhandev.proto.GetKlineDataByTickerRequest;
import com.tnhandev.proto.GetKlineDataByTickerResponse;
import com.tnhandev.proto.MarketDataServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@GrpcService
public class MarketDataService extends MarketDataServiceGrpc.MarketDataServiceImplBase {
    private final RestTemplate restTemplate;
    private final MarketDataRepository marketDataRepository;
    private final String baseUrl = "https://contract.mexc.com/api/v1/contract";

    public MarketDataService(MarketDataRepository marketDataRepository) {
        this.restTemplate = new RestTemplate();
        this.marketDataRepository = marketDataRepository;
    }

    @Override
    public void getAllTickers(Empty request, StreamObserver<GetAllTickersResponse> responseObserver) {
        List<com.tnhandev.market_data_service.model.Ticker> repoTickers = marketDataRepository.findAll();
        List<com.tnhandev.market_data_service.model.Ticker> modelTickers = new ArrayList<>();
        List<com.tnhandev.proto.common.Ticker> protoTickers = new ArrayList<>();

        if (!repoTickers.isEmpty()) {
            repoTickers.forEach(ticker -> {
                com.tnhandev.proto.common.Ticker protoTicker = com.tnhandev.proto.common.Ticker.newBuilder()
                        .setId(ticker.getId())
                        .setSymbol(ticker.getSymbol())
                        .build();
                protoTickers.add(protoTicker);
            });
            responseObserver.onNext(GetAllTickersResponse.newBuilder().addAllTickers(protoTickers).build());
            responseObserver.onCompleted();
            return;
        }

        com.tnhandev.market_data_service.response.GetAllTickersResponse response = restTemplate.getForObject(baseUrl + "/detail", com.tnhandev.market_data_service.response.GetAllTickersResponse.class);


        if (response != null && response.isSuccess() && response.getData() != null) {
            response.getData().forEach(res -> {
                Integer id = res.getId();
                String symbol = res.getSymbol();

                com.tnhandev.market_data_service.model.Ticker modelTicker = new com.tnhandev.market_data_service.model.Ticker();
                modelTicker.setId(id);
                modelTicker.setSymbol(symbol);
                modelTickers.add(modelTicker);

                com.tnhandev.proto.common.Ticker protoTicker = com.tnhandev.proto.common.Ticker.newBuilder()
                        .setId(id)
                        .setSymbol(symbol)
                        .build();
                protoTickers.add(protoTicker);
            });
            marketDataRepository.saveAll(modelTickers);
        }

        responseObserver.onNext(GetAllTickersResponse.newBuilder().addAllTickers(protoTickers).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getKlineDataByTicker(GetKlineDataByTickerRequest request, StreamObserver<GetKlineDataByTickerResponse> responseObserver) {
        String symbol = request.getSymbol();
        String interval = request.getInterval().toString();

        com.tnhandev.market_data_service.response.GetKlineDataByTickerResponse response =
                restTemplate.getForObject(baseUrl + "/kline/" + symbol + "?interval=" + interval, com.tnhandev.market_data_service.response.GetKlineDataByTickerResponse.class);

        List<Long> time = new ArrayList<>();
        List<Double> open = new ArrayList<>();
        List<Double> close = new ArrayList<>();
        List<Double> high = new ArrayList<>();
        List<Double> low = new ArrayList<>();

        if (response != null && response.isSuccess() && response.getData() != null) {
            time.addAll(response.getData().get("time").stream()
                                .map(Number::longValue)
                                .toList());
            open.addAll(response.getData().get("open").stream()
                                .map(Number::doubleValue)
                                .toList());
            close.addAll(response.getData().get("close").stream()
                                 .map(Number::doubleValue)
                                 .toList());
            high.addAll(response.getData().get("high").stream()
                                .map(Number::doubleValue)
                                .toList());
            low.addAll(response.getData().get("low").stream()
                               .map(Number::doubleValue)
                               .toList());
        }

        responseObserver.onNext(GetKlineDataByTickerResponse.newBuilder()
                                        .addAllTimes(time)
                                        .addAllOpenPrices(open)
                                        .addAllClosePrices(close)
                                        .addAllHighPrices(high)
                                        .addAllLowPrices(low)
                                        .build());
        responseObserver.onCompleted();
    }
}
