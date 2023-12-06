package com.uvarchev.javatelebot.bot;

import com.uvarchev.javatelebot.command.StartCommand;
import com.uvarchev.javatelebot.command.StopCommand;
import com.uvarchev.javatelebot.service.TelebotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Telebot extends TelegramLongPollingBot {

    @Autowired
    private TelebotService botService;

    private final TelebotConfig config;

    public Telebot(TelebotConfig config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            // Check if message starts with some command
            if (messageText.startsWith("/")) {
                // Get chatId and user's data (user for replies)
                Long chatId = update.getMessage().getChatId();
                Long userId = update.getMessage().getFrom().getId();
                String firstName = update.getMessage().getChat().getFirstName();

                // Extract input command (without "/")
                String commandString = messageText.substring(1).toUpperCase();

                // Process input command and generate answer
                String answer = switch (commandString) {
                    case "START" -> {
                        StartCommand startCommand = new StartCommand();
                        yield botService.startCommandReceived(startCommand, userId, firstName);
                    }
                    case "STOP" -> {
                        StopCommand stopCommand = new StopCommand();
                        yield botService.stopCommandReceived(stopCommand, userId, firstName);
                    }
                    default ->
                        // Command is not recognised
                        botService.unrecognisedCommandReceived();
                };

                // Reply with generated answer:
                sendMessage(chatId, answer);
            }
        }
    }

    public void sendMessage(long chatId, String answer) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(answer);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
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
