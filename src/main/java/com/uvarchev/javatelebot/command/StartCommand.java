package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.enums.CommandType;

public class StartCommand implements Command {
    private final Long userId;
    private final String firstName;

    public StartCommand(Long userId, String firstName) {
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
        return CommandType.START;
    }
}
