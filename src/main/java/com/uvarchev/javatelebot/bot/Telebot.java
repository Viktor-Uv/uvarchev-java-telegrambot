package com.uvarchev.javatelebot.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class Telebot extends TelegramLongPollingBot {

    final TelebotConfig config;

    public Telebot(TelebotConfig config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("Got update");
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

}
