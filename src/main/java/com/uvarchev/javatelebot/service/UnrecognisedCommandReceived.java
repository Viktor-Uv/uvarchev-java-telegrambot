package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.bot.command.Command;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.enums.UserRole;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class UnrecognisedCommandReceived implements Command {

    @Autowired
    private UserRepository userRepository;

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

        // Get UserRole to be able to show all available commands to the particular User in the help message
        UserRole userRole = userRepository.findById(userId)
                .map(User::getUserRole)
                .orElse(UserRole.GUEST);

        // Generate reply and send it
        telebot.sendMessage(
                userId.toString(),
                generateReply(firstName, userRole),
                update.getMessage().getMessageId()
        );
    }

    private String generateReply(String firstName, UserRole userRole) {
        // Load all available commands
        String supportedCommands =
                Arrays
                        .stream(
                                CommandType.values()
                        )
                        .filter(
                                command -> isEnoughRights(userRole, command)
                        )
                        .map(
                                commandType -> commandType.name().toLowerCase()
                        )
                        .collect(
                                Collectors.joining(", /")
                        );

        // Generate reply
        return "Sorry, " + firstName + ", command was not recognised.\n" +
                "Currently available commands are: /" + supportedCommands + ".";
    }

    private boolean isEnoughRights(UserRole userRole, CommandType commandType) {
        return userRole.getAccessLevel() >= commandType.getRequiredAccessLevel();
    }
}
