package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.enums.CommandType;

public class RemoveSubId implements Command {

    private final Long userId;
    private final String subscriptionId;

    public RemoveSubId(Long userId, String subscriptionId) {
        this.userId = userId;
        this.subscriptionId = subscriptionId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public CommandType getType() {
        return CommandType.REMOVESUBID;
    }
}
