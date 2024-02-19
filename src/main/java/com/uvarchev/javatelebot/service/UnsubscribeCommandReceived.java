package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.bot.command.Command;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UnsubscribeCommandReceived implements Command {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    public CommandType getType() {
        return CommandType.UNSUBSCRIBE;
    }

    @Override
    // '/removeSubId <subscription_id>'
    // Deactivates subscription with the given ID
    public void execute(Update update, Telebot telebot) {
        // Get user details
        Long userId = update.getMessage().getFrom().getId();

        // Get command options
        String[] commandLineArgs = update.getMessage().getText().toUpperCase().split("\\s+");

        // Generate reply and send it
        telebot.sendMessage(
                userId.toString(),
                generateReply(userId, commandLineArgs),
                update.getMessage().getMessageId()
        );
    }

    private String generateReply(Long userId, String[] commandLineArgs) {
        // Return error msg in case of incorrect command usage
        if (commandLineArgs.length != 2) {
            return "Sorry, but correct usage is: /unsubscribe <subscription_id>\n" +
                    "You can use command /subscriptions to find desired subscription ID";
        }

        // Try to convert provided parameter into a Long
        long subId;
        try {
            subId = Long.parseLong(commandLineArgs[1]);
        } catch (NumberFormatException ignored) {
            return "Sorry, but the <subscription_id> provided is not a valid number";
        }

        // Try to deactivate subscription
        if (subscriptionRepository.deactivateById(subId, userId) == 0) {
            return "Sorry, but subscription with the ID " + subId + " was not found";
        } else {
            return "Subscription with ID " + subId + " has been successfully deactivated";
        }
    }
}
