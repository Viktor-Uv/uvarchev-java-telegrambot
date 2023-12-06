package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.enums.CommandType;

public class UnrecognisedCommand implements Command {
    @Override
    public CommandType getType() {
        return null;
    }
}
