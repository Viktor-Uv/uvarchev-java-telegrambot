package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.command.StartCommand;
import com.uvarchev.javatelebot.command.StopCommand;
import org.springframework.stereotype.Service;

@Service
public class TelebotService {

    public String unrecognisedCommandReceived() {
        return "Sorry, command was not recognised";
    }

    public String startCommandReceived(StartCommand command, Long userId, String firstName) {

        return "Hi, " + firstName + ", nice to meet you!";
    }

    public String stopCommandReceived(StopCommand command, Long userId, String firstName){
        return "Bye, " + firstName + ", till next time!";
    }
}
