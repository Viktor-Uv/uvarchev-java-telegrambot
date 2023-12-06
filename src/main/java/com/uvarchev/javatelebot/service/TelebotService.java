package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.command.StartCommand;
import com.uvarchev.javatelebot.command.StopCommand;
import com.uvarchev.javatelebot.command.UnrecognisedCommand;
import org.springframework.stereotype.Service;

@Service
public class TelebotService {

    public String processCommand(UnrecognisedCommand command) {
        return "Sorry, command was not recognised";
    }

    public String processCommand(StartCommand command) {
        return "Hi, " + command.getFirstName() + ", nice to meet you!";
    }

    public String processCommand(StopCommand command) {
        return "Bye, " + command.getFirstName() + ", till next time!";
    }
}
