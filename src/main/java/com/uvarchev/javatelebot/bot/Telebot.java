package com.uvarchev.javatelebot.bot;

import com.uvarchev.javatelebot.enums.BotCommands;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Telebot extends TelegramLongPollingBot {

    private final TelebotConfig config;

    public Telebot(TelebotConfig config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            // Get chatId and user's name
            Long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getChat().getFirstName();

            // Check if the received message starts with a command
            if (messageText.startsWith("/")) {
                // Extract input command
                String commandString = messageText.substring(1);

                // Validate input command
                if (isRecognisedCommand(commandString, chatId)) {
                    // Convert commandString to enum constant
                    BotCommands command = BotCommands.valueOf(commandString.toUpperCase());

                    // Process command
                    switch (command) {
                        case START -> startCommandReceived(chatId, firstName);
                        case STOP -> stopCommandReceived(chatId, firstName);
                    }
                } else {
                    // Reply that command is not recognised
                    unrecognisedCommandReceived(chatId);
                }


            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = "Hi, " + firstName + ", nice to meet you!";
        sendMessage(chatId, answer);
    }

    private void stopCommandReceived(long chatId, String firstName) {
        String answer = "Bye, " + firstName + ", till next time!";
        sendMessage(chatId, answer);
    }

    private void unrecognisedCommandReceived(long chatId) {
        sendMessage(chatId, "Sorry, command was not recognised");
    }

    private boolean isRecognisedCommand(String commandString, long chatId) {
        try {
            BotCommands.valueOf(commandString.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Unrecognised command from chat ID " + chatId + ". " + e.getMessage());
            return false;
        }
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

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
