package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.enums.CommandType;

public class ListSubCommand implements Command {

    private final Long userId;

    public ListSubCommand(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public CommandType getType() {
        return CommandType.LISTSUB;
    }
}
