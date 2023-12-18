package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.enums.CommandType;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class UnrecognisedCommand implements Command {

    @Override
    public CommandType getType() {
        return CommandType.UNRECOGNISED;
    }

    @Override
    // '/any_unrecognised_command'
    // Reply with unrecognised command error message. List all available commands
    public void execute(Update update, Telebot telebot) {
        // Get user details
        Long userId = update.getMessage().getFrom().getId();
        String firstName = update.getMessage().getChat().getFirstName();

        // Generate reply and send it
        telebot.sendMessage(
                userId.toString(),
                generateReply(firstName),
                update.getMessage().getMessageId()
        );
    }

    private String generateReply(String firstName) {
        // Load all available commands
        String supportedCommands =
                Arrays
                        .stream(
                                CommandType.values()
                        )
                        .filter(
                                commandType -> !commandType.equals(CommandType.UNRECOGNISED)
                        )
                        .map(
                                commandType -> commandType.name().toLowerCase()
                        )
                        .collect(
                                Collectors.joining(", /")
                        );

        // Generate reply
        return "Sorry, " + firstName + ", command was not recognised.\n" +
                "Currently supported commands are: /" + supportedCommands + ".";
    }
}
