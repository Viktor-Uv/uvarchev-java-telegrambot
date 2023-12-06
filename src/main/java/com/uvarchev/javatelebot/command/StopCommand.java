package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.enums.CommandType;

public class StopCommand implements Command {
    private final Long userId;
    private final String firstName;

    public StopCommand(Long userId, String firstName) {
        this.userId = userId;
        this.firstName = firstName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    @Override
    public CommandType getType() {
        return CommandType.STOP;
    }
}
