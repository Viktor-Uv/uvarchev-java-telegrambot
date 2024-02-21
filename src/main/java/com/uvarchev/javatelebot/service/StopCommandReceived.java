package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.bot.command.Command;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class StopCommandReceived {
    @Autowired
    private UserRepository userRepository;

    public void execute(Update update, Telebot telebot) {
        // Get user details
        Long userId = update.getMessage().getFrom().getId();
        String firstName = update.getMessage().getChat().getFirstName();

//        // Generate reply and send it
//        telebot.sendMessage(
//                userId.toString(),
//                generateReply(userId, firstName),
//                update.getMessage().getMessageId()
//        );
    }

    private String generateReply(Long userId, String firstName) {
        return userRepository
                // Try to get user from repository
                .findById(userId)
                .map(
                        // if user was found - set inactive and lower UserRole to GUEST
                        leavingUser -> {
                            userRepository.deactivateById(userId);
                            return "Updates are stopped. Bye, " + firstName + ", till next time!";
                        }
                )
                .orElse(
                        // otherwise just say bye
                        "Bye, " + firstName + ", till next time!"
                );
    }
}
