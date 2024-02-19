package com.uvarchev.javatelebot.bot.command;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.enums.CommandType;
import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;

@AllArgsConstructor
public class SubscribeCommand implements Command {

    private String msgText;
    private String userName;
    private Long userId;


    @Override
    public CommandType getType() {
        return CommandType.SUBSCRIBE;
    }

    @Override
    public String execute() {
        return null;
    }

}