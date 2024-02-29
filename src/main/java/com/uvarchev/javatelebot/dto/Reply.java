package com.uvarchev.javatelebot.dto;

import lombok.Getter;

@Getter
public class Reply {
    private Long userId;
    private int msgId;
    private String messageBody;
    private Long subscriptionId;

    public Reply(Long userId, int msgId, String messageBody) {
        this.userId = userId;
        this.msgId = msgId;
        this.messageBody = messageBody;
    }

    public Reply(Long userId, String messageBody, Long subscriptionId) {
        this.userId = userId;
        this.messageBody = messageBody;
        this.subscriptionId = subscriptionId;
    }

    public Reply(Long userId, String messageBody) {
        this.userId = userId;
        this.messageBody = messageBody;
    }
}
