package com.uvarchev.javatelebot.bot.command;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.enums.CommandType;
import org.telegram.telegrambots.meta.api.objects.Update;

public class SubscribeCommand implements Command {
    @Override
    public CommandType getType() {
        return CommandType.SUBSCRIBE;
    }

    @Override
    public void execute(Update update, Telebot telebot) {

    }
}
