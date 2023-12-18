package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.enums.CommandType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommandProcessor {
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

    private CommandType identifyCommand(String text) {
        return switch (text) {
            case "/START" -> CommandType.START;
            case "/STOP" -> CommandType.STOP;
            case "/SUBSCRIBE" -> CommandType.SUBSCRIBE;
            case "/UNSUBSCRIBE" -> CommandType.UNSUBSCRIBE;
            case "/LIST" -> CommandType.LIST;
            default -> CommandType.UNRECOGNISED;
        };
    }

    public void processCommand(Update update, Telebot telebot) {
        // Extract input text, capitalise it, and get first word
        String messageText = update.getMessage().getText().toUpperCase().split("\\s+")[0];

        // Identify command type from user's message
        CommandType commandType = identifyCommand(messageText);

        // Get Command instance from identified command type
        Command command = commandMap.get(commandType);

        // Execute command via interface
        command.execute(update, telebot);
    }
}
