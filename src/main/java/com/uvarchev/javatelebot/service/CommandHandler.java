package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.command.*;
import org.springframework.stereotype.Service;

@Service
public class CommandHandler {

    /**
     * Registers new user or reactivates old, but inactive user
     * @Usage: /start
     */
    public final String processAndRespond(StartCommand command) {
        // TODO Start command logic
        return command.getMsgText();
    }

    /**
     * Sets leaving user inactive
     * @Usage: /stop
     */
    public String processAndRespond(StopCommand command) {
        // TODO Stop command logic
        return command.getMsgText();
    }

    /**
     * Adds subscription for a user
     * @Usage: /subscribe [service]
     */
    public String processAndRespond(SubscribeCommand command) {
        // TODO Subscribe command logic
        return command.getMsgText();
    }

    /**
     * Deactivates subscription with the given ID
     * @Usage: /unsubscribe [subscription_id]
     */
    public String processAndRespond(UnsubscribeCommand command) {
        // TODO Unsubscribe command logic
        return command.getMsgText();
    }

    /**
     * Lists all active subscriptions
     * @Usage: /subscriptions
     */
    public String processAndRespond(SubscriptionsCommand command) {
        // TODO List Subscriptions command logic
        return command.getMsgText();
    }

    /**
     * Shows application statistics for Administrators
     * @Usage: /statistics
     */
    public String processAndRespond(StatisticsCommand command) {
        // TODO Statistics for administrators logic
        return command.getMsgText();
    }

    /**
     * Informs that command wasn't unrecognised, list all available commands
     * @Usage: ANY_OTHER_COMMAND
     */
    public String processAndRespond(UnrecognisedCommand command) {
        // TODO Unrecognised Command logic
        return "UNRECOGNISED: " + command.getMsgText();
    }

}
