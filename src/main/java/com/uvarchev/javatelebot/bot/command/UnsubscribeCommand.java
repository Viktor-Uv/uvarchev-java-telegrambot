package com.uvarchev.javatelebot.bot.command;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.service.CommandHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;

@AllArgsConstructor
@Getter
public class UnsubscribeCommand implements Command {

    private String msgText;
    private String userName;
    private Long userId;

    @Override
    public CommandType getType() {
        return CommandType.UNSUBSCRIBE;
    }

    @Override
    public String execute() {
        return new CommandHandler().processAndRespond(this);
    }

}
