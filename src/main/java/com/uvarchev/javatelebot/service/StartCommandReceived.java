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

@Component
public class StartCommandReceived implements Command {
    @Autowired
    private UserRepository userRepository;

    @Override
    public CommandType getType() {
        return CommandType.START;
    }

    @Override
    // '/start'
    // Register new user or reactivate old, but inactive user
    public void execute(Update update, Telebot telebot) {
        // Get user details
        Long userId = update.getMessage().getFrom().getId();
        String firstName = update.getMessage().getChat().getFirstName();

        // Generate reply and send it
        telebot.sendMessage(
                userId.toString(),
                generateReply(userId, firstName),
                update.getMessage().getMessageId()
        );
    }

    private String generateReply(Long userId, String firstName) {
        String reply;

        // Try to get user from repository
        User user = userRepository
                .findById(userId)
                .orElse(null);

        // Generate a reply based on whether the user was found or not
        // If user exists - set as active again and increase UserRole to USER
        // Otherwise - create new user
        if (user != null) {
            reply = "Hi, " + firstName + ", nice to see you again!";
            user.setActive(true);
            user.setUserRole(UserRole.USER);
        } else {
            reply = "Hi, " + firstName + ", nice to meet you!";
            user = new User(userId);
        }

        // Save user and return reply
        userRepository.save(user);
        return reply;
    }
}
