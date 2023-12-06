package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.enums.CommandType;

public class UnrecognisedCommand implements Command {
    private final String firstName;

    public UnrecognisedCommand(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    @Override
    public CommandType getType() {
        return null;
    }
}
