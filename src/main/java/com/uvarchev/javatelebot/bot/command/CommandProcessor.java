package com.uvarchev.javatelebot.bot.command;

import com.uvarchev.javatelebot.dto.Reply;
import com.uvarchev.javatelebot.service.CommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CommandProcessor {

    @Autowired
    private CommandHandler commandHandler;

    private Long userId;
    private String msgText;
    private String userName;

    /**
     * @return Reply object with address, reply reference and reply text
     */
    public Reply processUpdate(Update update) {
        userId = update.getMessage().getFrom().getId();
        msgText = update.getMessage().getText();
        userName = update.getMessage().getChat().getFirstName();

        return new Reply(
                userId,
                update.getMessage().getMessageId(),
                generateReply(msgText)
        );
    }

    /**
     * @param msgText text string, sent by the User
     * @return String (reply text)
     */
    private String generateReply(String msgText) {
        // Extract command
        Command command = createCommand(
                msgText.toUpperCase().split("\\s+")[0]
        );

        // Reply
        return command.execute(commandHandler);
    }

    /**
     * Selects required command based on the first word of the users input.
     * If the required pattern doesn't match, an UNRECOGNIZED command will be selected.
     *
     * @param firstWord The first complete word from the user's input
     * @return Commands implementation, selected based on the user's input
     */
    private Command createCommand(String firstWord) {
        return switch (firstWord) {
            case "/START" -> new StartCommand(userName, userId);
            case "/STOP" -> new StopCommand(userName, userId);
            case "/SUBSCRIBE" -> new SubscribeCommand(msgText, userName, userId);
            case "/UNSUBSCRIBE" -> new UnsubscribeCommand(msgText, userName, userId);
            case "/SUBSCRIPTIONS" -> new SubscriptionsCommand(msgText, userName, userId);
            case "/STATISTICS" -> new StatisticsCommand(msgText, userName, userId);
            default -> new UnrecognisedCommand(userName, userId);
        };
    }


//    @Autowired
//    private UserRepository userRepository;
//
//    private final Map<CommandType, Command> commandMap;
//
//    @Autowired
//    // Spring to inject a list of all implementations of Command interface
//    public CommandProcessor(List<Command> commands) {
//        // Initialize commandMap with key:CommandType & value:CommandImplementation
//        this.commandMap = commands.stream()
//                .collect(
//                        // Convert list of commands into a Map
//                        Collectors.toMap(
//                                // Each Command implementation's type becomes a key
//                                Command::getType,
//                                // Each Command implementation's instant itself becomes a value
//                                Function.identity()
//                        )
//                );
//    }
//
//    public void processCommand(Update update, Telebot telebot) {
//        // Extract input text, capitalise it, and get first word
//        String messageText = update.getMessage().getText().toUpperCase().split("\\s+")[0];
//
//        // Identify command type from user's message
//        CommandType commandType = identifyCommand(messageText);
//
//        // If User has insufficient Access Level to execute the requested command, change command to UNRECOGNISED
//        if (
//                isInsufficientRights(
//                        update.getMessage().getFrom().getId(),
//                        commandType.getRequiredAccessLevel()
//                )
//        ) {
//            commandType = CommandType.UNRECOGNISED;
//        }
//
//        // Get Command instance from identified command type
//        Command command = commandMap.get(commandType);
//
//        // Execute command via interface
//        command.execute(update, telebot);
//    }
//

//
//    private boolean isInsufficientRights(Long userId, int requiredAccLevel) {
//        // Get user's actual access level
//        int userAccLevel = userRepository.findById(userId)
//                .map(User::getUserRole)
//                .orElse(UserRole.GUEST)
//                .getAccessLevel();
//
//        // Compare to the required access level
//        return userAccLevel < requiredAccLevel;
//    }
}
