package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.command.StartCommand;
import com.uvarchev.javatelebot.command.StopCommand;
import com.uvarchev.javatelebot.command.UnrecognisedCommand;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TelebotService {

    @Autowired
    private UserRepository userRepository;

    // '/any_unrecognised_command'
    public String processCommand(UnrecognisedCommand command) {
        return "Sorry, " + command.getFirstName() + ", command was not recognised";
    }

    // '/start'
    // Register new user or reactivate old, but inactive user
    public String processCommand(StartCommand command) {
        Optional<User> optional = userRepository.findById(command.getUserId());
        if (optional.isPresent()) {
            User oldUser = optional.get();
            oldUser.setActive(true);
            userRepository.save(oldUser);
            return "Hi, " + command.getFirstName() + ", nice to see you again!";
        } else {
            userRepository.save(new User(command.getUserId()));
            return "Hi, " + command.getFirstName() + ", nice to meet you!";
        }
    }

    // '/stop'
    // Set leaving user inactive
    public String processCommand(StopCommand command) {
        Optional<User> optional = userRepository.findById(command.getUserId());
        if (optional.isPresent()) {
            User leavingUser = optional.get();
            leavingUser.setActive(false);
            userRepository.save(leavingUser);
            return "Updates are stopped. Bye, " + command.getFirstName() + ", till next time!";
        } else {
            return "Bye, " + command.getFirstName() + ", till next time!";
        }

    }
}
