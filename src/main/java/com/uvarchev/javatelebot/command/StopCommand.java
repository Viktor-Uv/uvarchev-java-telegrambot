package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.enums.CommandType;

public class StopCommand implements Command {
    @Override
    public CommandType getType() {
        return CommandType.STOP;
    }
}
