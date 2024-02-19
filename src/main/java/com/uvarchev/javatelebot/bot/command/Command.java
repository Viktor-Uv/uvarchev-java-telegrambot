package com.uvarchev.javatelebot.bot.command;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.enums.CommandType;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {
    CommandType getType();
    void execute(Update update, Telebot telebot);
}
