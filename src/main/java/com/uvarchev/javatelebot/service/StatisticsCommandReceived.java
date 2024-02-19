package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.bot.command.Command;
import com.uvarchev.javatelebot.enums.CommandType;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StatisticsCommandReceived implements Command {
    @Override
    public CommandType getType() {
        return CommandType.STATISTICS;
    }

    @Override
    // '/statistics'
    // Applications statistics for Administrators
    public void execute(Update update, Telebot telebot) {
        // Get user details
        Long userId = update.getMessage().getFrom().getId();
        String firstName = update.getMessage().getChat().getFirstName();

        // Generate reply and send it
        telebot.sendMessage(
                userId.toString(),
                generateReply(userId, firstName),
                update.getMessage().getMessageId()
        );
    }

    private String generateReply(Long userId, String firstName) {
        // TODO statistics for administrators
        return "Apps Statistics";
    }
}
