package com.uvarchev.javatelebot.bot;

import com.uvarchev.javatelebot.command.StartCommand;
import com.uvarchev.javatelebot.command.StopCommand;
import com.uvarchev.javatelebot.command.UnrecognisedCommand;
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

            // Check if message text starts with some command
            if (update.getMessage().getText().startsWith("/")) {
                // Get chatId and user's data (used for replies)
                Long chatId = update.getMessage().getChatId();

                // Generate answer and reply:
                String answer = getAnswer(update);
                sendMessage(chatId, answer);
            }
        }
    }

    private String getAnswer(Update update) {
        // Get user details
        Long userId = update.getMessage().getFrom().getId();
        String firstName = update.getMessage().getChat().getFirstName();

        // Extract input command and capitalise (command without "/")
        String commandString = update.getMessage().getText().substring(1).toUpperCase();

        // Process input command and generate answer
        return switch (commandString) {
            case "START" -> {
                StartCommand command = new StartCommand(userId, firstName);
                yield botService.processCommand(command);
            }
            case "STOP" -> {
                StopCommand command = new StopCommand(userId, firstName);
                yield botService.processCommand(command);
            }
            default -> {
                UnrecognisedCommand command = new UnrecognisedCommand();
                yield botService.processCommand(command);
            }
        };
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
