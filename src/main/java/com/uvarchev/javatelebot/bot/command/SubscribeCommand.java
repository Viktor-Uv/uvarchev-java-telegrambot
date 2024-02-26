package com.uvarchev.javatelebot.bot.command;

import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.service.CommandService;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SubscribeCommand implements Command {

    private String msgText;
    private String userName;
    private Long userId;

    @Override
    public CommandType getType() {
        return CommandType.SUBSCRIBE;
    }

    @Override
    public String execute(CommandService commandService) {
        return commandService.processAndRespond(this);
    }

}
