package com.tnhandev.rsi_divergence_service.service;

import com.tnhandev.proto.*;
import com.tnhandev.proto.common.Action;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;

@GrpcService
public class RSIDivergenceService extends RSIDivergenceServiceGrpc.RSIDivergenceServiceImplBase {
    @GrpcClient("market-data-service")
    private MarketDataServiceGrpc.MarketDataServiceBlockingStub marketDataClient;

    @Override
    public void calculateRSI(CalculateRSIRequest request, StreamObserver<CalculateRSIResponse> responseObserver) {
        List<Double> closePrices = request.getClosePricesList();
        int period = request.getPeriod();
        List<Double> rsiValues = new ArrayList<>();

        if (closePrices.size() < period)
            return;

        double avgGain = 0.0;
        double avgLoss = 0.0;

        for (int i = 1; i < period; i++) {
            double change = closePrices.get(i) - closePrices.get(i - 1);
            if (change > 0)
                avgGain += change;
            else
                avgLoss += Math.abs(change);
        }

        avgGain /= period;
        avgLoss /= period;

        for (int i = period; i < closePrices.size(); i++) {
            double change = closePrices.get(i) - closePrices.get(i - 1);
            if (change > 0) {
                avgGain = (avgGain * (period - 1) + change) / period;
                avgLoss = (avgLoss * (period - 1)) / period;
            }
            else {
                avgGain = (avgGain * (period - 1)) / period;
                avgLoss = (avgLoss * (period - 1) + Math.abs(change)) / period;
            }

            double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
            double rsi = 100 - (100 / (1 + rs));
            rsiValues.add(rsi);
        }

        responseObserver.onNext(CalculateRSIResponse.newBuilder()
                                        .setSymbol(request.getSymbol())
                                        .addAllRsiValues(rsiValues)
                                        .build());
        responseObserver.onCompleted();
    }

    @Override
    public void detectDivergence(DetectDivergenceRequest request, StreamObserver<DetectDivergenceResponse> responseObserver) {
        List<Double> rsiValues = request.getRsiValuesList();
        List<Double> closePrices = request.getClosePricesList();
        int size = rsiValues.size();

        if (size < 2) {
            responseObserver.onCompleted();
            return;
        }

        for (int i = 1; i < size; i++) {
            boolean priceHigher = closePrices.get(i) > closePrices.get(i - 1);
            boolean rsiLower = rsiValues.get(i) < rsiValues.get(i - 1);

            if (priceHigher && rsiLower)
                responseObserver.onNext(DetectDivergenceResponse.newBuilder()
                                                .setAction(Action.SHORT)
                                                .build());

            boolean priceLower = closePrices.get(i) < closePrices.get(i - 1);
            boolean rsiHigher = rsiValues.get(i) > rsiValues.get(i - 1);

            if (priceLower && rsiHigher)
                responseObserver.onNext(DetectDivergenceResponse.newBuilder()
                                                .setAction(Action.LONG)
                                                .build());
        }
        responseObserver.onCompleted();
    }
}
