package com.tnhandev.notification_service.component;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class MyTelegramBot {
    private TelegramBot bot;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    public MyTelegramBot() {
        this.bot = new TelegramBot(botToken);
    }

    public boolean sendMessage(String action, String symbol, String time) {
        String myAction = "";

        if (action.equals("Long"))
            myAction = "🟢 Tín hiệu Long cho ";
        else if (action.equals("Short"))
            myAction = "🔴 Tín hiệu Short cho ";

        String message = myAction + symbol + "Thông báo này lúc: " + time + " - Có hiệu lực 20p";

        SendMessage request = new SendMessage(chatId, message)
                .parseMode(ParseMode.HTML)
                .disableNotification(true)
                .replyMarkup(new ForceReply());

        SendResponse isSuccess = bot.execute(request);
        
        return isSuccess.isOk();
    }
}
