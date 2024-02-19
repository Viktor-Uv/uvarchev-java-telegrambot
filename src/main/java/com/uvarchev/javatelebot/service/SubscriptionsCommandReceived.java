package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.bot.command.Command;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class SubscriptionsCommandReceived {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Transactional
    // '/subscriptions'
    // Lists all active subscriptions
    public void execute(Update update, Telebot telebot) {
        // Get user details
        Long userId = update.getMessage().getFrom().getId();

//        // Generate reply and send it
//        telebot.sendMessage(
//                userId.toString(),
//                generateReply(userId),
//                update.getMessage().getMessageId()
//        );
    }

    private String generateReply(Long userId) {
        StringBuilder response = new StringBuilder();

        subscriptionRepository
                .findByUserIdAndActiveIsTrue(userId)
                .forEach(
                        subscription -> response.append("\n").append(subscription)
                );

        if (response.isEmpty()) {
            return "You don't have any active subscriptions";
        } else {
            response.insert(
                    0,
                    "List of your active subscriptions:\n" +
                            "ID | Name | Parameters"
            );
            return response.toString();
        }
    }
}
