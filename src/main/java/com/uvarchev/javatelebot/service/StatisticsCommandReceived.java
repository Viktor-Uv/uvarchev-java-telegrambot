package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.bot.command.Command;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class StatisticsCommandReceived {

    @Autowired
    private UserRepository userRepository;

    private String respond(Long userId, String firstName) {
        return "Apps Statistics";
    }

//    public void execute(Update update, Telebot telebot) {
//        // Get user details
//        Long userId = update.getMessage().getFrom().getId();
//        String firstName = update.getMessage().getChat().getFirstName();
//
////        // Generate reply and send it
////        telebot.sendMessage(
////                userId.toString(),
////                generateReply(userId, firstName),
////                update.getMessage().getMessageId()
////        );
//    }


}
