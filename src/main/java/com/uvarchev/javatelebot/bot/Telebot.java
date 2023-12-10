package com.uvarchev.javatelebot.bot;

import com.uvarchev.javatelebot.command.*;
import com.uvarchev.javatelebot.enums.ServiceType;
import com.uvarchev.javatelebot.service.TelebotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.stream.Collectors;

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

                // Generate reply and reply:
                String reply = getAnswer(update);
                sendMessage(update, reply);
            }
        }
    }

    private String getAnswer(Update update) {
        // Get user details
        Long userId = update.getMessage().getFrom().getId();
        String firstName = update.getMessage().getChat().getFirstName();

        // Extract input command, capitalise it, and split by words (command without "/")
        String[] commandString = update.getMessage().getText()
                .substring(1).toUpperCase().split("\\s+");

        // Process input command and generate answer
        return switch (commandString[0]) {
            case "START" -> {
                StartCommand command = new StartCommand(userId, firstName);
                yield botService.processCommand(command);
            }
            case "STOP" -> {
                StopCommand command = new StopCommand(userId, firstName);
                yield botService.processCommand(command);
            }
            case "ADDSUB" -> {
                // Check usage
                if (commandString.length > 1) {
                    // IF correct option name was received,
                    ServiceType desiredService = AddSubCommand.validateOption(commandString[1]);
                    if (desiredService != null) {
                        // AND this option name is valid,
                        // THEN subscribe user
                        AddSubCommand command;
                        if (commandString.length > 2) {
                            command = new AddSubCommand(userId, desiredService, commandString[2]);
                        } else {
                            command = new AddSubCommand(userId, desiredService);
                        }
                        yield botService.processCommand(command);
                    }
                }
                // If any condition not satisfied - generate error message
                String availableServices = Arrays.stream(ServiceType.values())
                        // Load all available services
                        .map(commandType -> commandType.name().toLowerCase())
                        .collect(Collectors.joining(", "));
                yield "Sorry, correct subscription service name was not provided.\n" +
                        "Usage: /addSub <desired_service> <option>\n" +
                        "Currently supported services are: " + availableServices + ".";
            }
            case "LISTSUB" -> {
                ListSubCommand command = new ListSubCommand(userId);
                yield botService.processCommand(command);
            }
            default -> {
                UnrecognisedCommand command = new UnrecognisedCommand(firstName);
                yield botService.processCommand(command);
            }
            case "REMOVESUBID" -> {
                // If wrong usage - generate error message
                if (commandString.length != 2) {
                    yield "Sorry, but correct usage is: /removeSubId <subscription_id>\n" +
                            "You can use /listSub command to find desired subscription id";
                }
                RemoveSubId command = new RemoveSubId(userId, commandString[1]);
                yield botService.processCommand(command);
            }
        };
    }

    public void sendMessage(Update update, String reply) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        SendMessage sendMessage = new SendMessage(chatId, reply);
        sendMessage.setReplyToMessageId(update.getMessage().getMessageId());

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
