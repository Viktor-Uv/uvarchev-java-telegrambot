package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.enums.UserRole;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommandProcessor {

    @Autowired
    private UserRepository userRepository;

    private final Map<CommandType, Command> commandMap;

    @Autowired
    // Spring to inject a list of all implementations of Command interface
    public CommandProcessor(List<Command> commands) {
        // Initialize commandMap with key:CommandType & value:CommandImplementation
        this.commandMap = commands.stream()
                .collect(
                        // Convert list of commands into a Map
                        Collectors.toMap(
                                // Each Command implementation's type becomes a key
                                Command::getType,
                                // Each Command implementation's instant itself becomes a value
                                Function.identity()
                        )
                );
    }

    public void processCommand(Update update, Telebot telebot) {
        // Extract input text, capitalise it, and get first word
        String messageText = update.getMessage().getText().toUpperCase().split("\\s+")[0];

        // Identify command type from user's message
        CommandType commandType = identifyCommand(messageText);

        // If User has insufficient Access Level to execute the requested command, change command to UNRECOGNISED
        if (
                isInsufficientRights(
                        update.getMessage().getFrom().getId(),
                        commandType.getRequiredAccessLevel()
                )
        ) {
            commandType = CommandType.UNRECOGNISED;
        }

        // Get Command instance from identified command type
        Command command = commandMap.get(commandType);

        // Execute command via interface
        command.execute(update, telebot);
    }

    private CommandType identifyCommand(String text) {
        return switch (text) {
            case "/START" -> CommandType.START;
            case "/STOP" -> CommandType.STOP;
            case "/SUBSCRIBE" -> CommandType.SUBSCRIBE;
            case "/UNSUBSCRIBE" -> CommandType.UNSUBSCRIBE;
            case "/SUBSCRIPTIONS" -> CommandType.SUBSCRIPTIONS;
            case "/STATISTICS" -> CommandType.STATISTICS;
            default -> CommandType.UNRECOGNISED;
        };
    }

    private boolean isInsufficientRights(Long userId, int requiredAccLevel) {
        // Get user's actual access level
        int userAccLevel = userRepository.findById(userId)
                .map(User::getUserRole)
                .orElse(UserRole.GUEST)
                .getAccessLevel();

        // Compare to the required access level
        return userAccLevel < requiredAccLevel;
    }
}
