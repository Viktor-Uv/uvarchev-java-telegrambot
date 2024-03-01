package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.dto.Reply;
import com.uvarchev.javatelebot.service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CommandProcessor {

    @Autowired
    private CommandService commandService;

    private Long userId;
    private String msgText;
    private String userName;

    /**
     * Processes an update from Telegram and returns a reply object.
     *
     * @param update The update object from Telegram.
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
     * Generates a reply text based on the command extracted from the user's message text.
     *
     * @param msgText The text string sent by the user.
     * @return The reply text to be sent to the user.
     */
    private String generateReply(String msgText) {
        // Extract command
        Command command = createCommand(
                msgText.toUpperCase().split("\\s+")[0]
        );

        // Reply
        return command.execute(commandService);
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
            case "/SUBSCRIPTIONS" -> new SubscriptionsCommand(userName, userId);
            case "/STATISTICS" -> new StatisticsCommand(userName, userId);
            default -> new UnrecognisedCommand(userName, userId);
        };
    }

}
