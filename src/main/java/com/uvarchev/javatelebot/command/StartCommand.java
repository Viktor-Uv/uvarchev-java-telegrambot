package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.enums.CommandType;

public class StartCommand implements Command {
    @Override
    public CommandType getType() {
        return CommandType.START;
    }
}
