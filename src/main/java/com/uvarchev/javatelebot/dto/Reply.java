package com.uvarchev.javatelebot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Reply {
    private Long userId;
    private int msgId;
    private String messageBody;

    public Reply(Long userId, int msgId, String messageBody) {
        this.userId = userId;
        this.msgId = msgId;
        this.messageBody = messageBody;
    }
}
