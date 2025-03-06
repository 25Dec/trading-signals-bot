package com.tnhandev.notification_service.service;

import com.tnhandev.notification_service.component.MyTelegramBot;
import com.tnhandev.notification_service.model.Notification;
import com.tnhandev.notification_service.repository.NotificationRepository;
import com.tnhandev.proto.NotificationRequest;
import com.tnhandev.proto.NotificationResponse;
import com.tnhandev.proto.NotificationServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@GrpcService
public class NotificationService extends NotificationServiceGrpc.NotificationServiceImplBase {
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private NotificationRepository notificationRepository;
    private MyTelegramBot telegramBot;

    public NotificationService(NotificationRepository notificationRepository, MyTelegramBot telegramBot) {
        this.notificationRepository = notificationRepository;
        this.telegramBot = telegramBot;
    }

    @Override
    public void sendTelegram(NotificationRequest request, StreamObserver<NotificationResponse> responseObserver) {
        String action = request.getAction().toString();
        String symbol = request.getSymbol();

        LocalDateTime time = Instant.ofEpochMilli(request.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        String formattedTime = formatter.format(time);
        telegramBot.sendMessage(action, symbol, formattedTime);

        notificationRepository.save(new Notification(request.getAction().toString(), symbol, time));

        responseObserver.onNext(NotificationResponse.newBuilder()
                                        .setSuccess(true)
                                        .build());
        responseObserver.onCompleted();
    }
}
