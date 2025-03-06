package com.tnhandev.notification_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String action;
    private String symbol;
    private LocalDateTime time;

    public Notification(String action, String symbol, LocalDateTime time) {
        this.action = action;
        this.symbol = symbol;
        this.time = time;
    }
}
