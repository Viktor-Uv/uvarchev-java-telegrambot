package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.command.StartCommand;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.UserRole;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Activates a user by checking if they already exist in the database or creating a new one if not.
     * The user role is set to USER for returning users.
     * A greeting message is returned based on the user status.
     * @param command a StartCommand object that contains the user id and username
     * @param user a User object that represents the user to activate or create
     * @return a reply String that greets the user and indicates whether they are new or returning
     */
    public String activateUser(StartCommand command, User user) {
        // Create reply draft
        String reply = "Hi, " + command.getUserName();

        // Generate the rest of reply based on whether the user was found or not
        if (user != null) {
            // If found - raise UserRole to USER and complete the reply
            user.setUserRole(UserRole.USER);
            reply += ", nice to see you again!";
        } else {
            // Otherwise - create a new user and complete the reply
            user = new User(command.getUserId());
            reply += ", nice to meet you!";
        }

        // Save user and return reply
        userRepository.save(user);
        return reply;
    }

}
