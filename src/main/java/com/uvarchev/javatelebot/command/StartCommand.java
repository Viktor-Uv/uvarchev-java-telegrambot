package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class StartCommand implements Command {
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
        return userRepository
                // Try to get user from repository
                .findById(userId)
                .map(
                        // if user was found - set as active and save back to repo
                        oldUser -> {
                            oldUser.setActive(true);
                            userRepository.save(oldUser);
                            return "Hi, " + firstName + ", nice to see you again!";
                        }
                )
                .orElseGet(
                        // otherwise - create new user and save to repo
                        () -> {
                            userRepository.save(
                                    new User(userId)
                            );
                            return "Hi, " + firstName + ", nice to meet you!";
                        }
                );
    }
}
