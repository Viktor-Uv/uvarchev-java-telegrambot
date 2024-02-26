package com.uvarchev.javatelebot.bot.command;

import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.service.CommandService;

public interface Command {
    CommandType getType();
    String execute(CommandService commandService);
}
