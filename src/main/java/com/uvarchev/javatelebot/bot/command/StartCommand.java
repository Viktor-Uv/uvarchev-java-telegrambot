package com.uvarchev.javatelebot.bot.command;

import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.service.CommandHandler;
import com.uvarchev.javatelebot.service.StartCommandReceived;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StartCommand implements Command {

    private String userName;
    private Long userId;

    @Override
    public CommandType getType() {
        return CommandType.START;
    }

    @Override
    public String execute(CommandHandler commandHandler) {
        return commandHandler.processAndRespond(this);
    }
}
