package com.uvarchev.javatelebot.bot;

import com.uvarchev.javatelebot.bot.command.CommandProcessor;
import com.uvarchev.javatelebot.dto.Reply;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Controller
public class Telebot extends TelegramLongPollingBot {

    @Autowired
    private CommandProcessor commandProcessor;

    private final TelebotConfig config;

    public Telebot(TelebotConfig config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Process the Update
            Reply reply = commandProcessor.processUpdate(update);

            // Send the Reply
            sendMessage(reply);
        }
    }

    public void sendMessage(Reply reply) {
        SendMessage sendMessage = new SendMessage(
                reply.getUserId().toString(),
                reply.getMessageBody()
        );
        sendMessage.setReplyToMessageId(
                reply.getMsgId()
        );

        try {
            execute(sendMessage);
        } catch (TelegramApiRequestException e) {
            // Handle a case when user has stopped & blocked the bot
            if (e.getErrorCode().equals(403)) {
                // Set user inactive
//                userRepository.deactivateById(
//                        reply.getUserId()
//                );
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

}
