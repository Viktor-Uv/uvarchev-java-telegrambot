package com.uvarchev.javatelebot.bot;

import com.uvarchev.javatelebot.command.CommandProcessor;
import com.uvarchev.javatelebot.dto.Reply;
import com.uvarchev.javatelebot.service.ExceptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Slf4j
@Controller
public class Telebot extends TelegramLongPollingBot {

    @Autowired
    private CommandProcessor commandProcessor;
    @Autowired
    private ExceptionService exceptionService;

    private final TelebotConfig config;

    public Telebot(TelebotConfig config) {
        this.config = config;
    }

    /**
     * Receives an update from Telegram and sends a reply if the update has a message with text.
     *
     * @param update The update object from Telegram.
     */
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

    /**
     * A method
     * that sends a reply object to the user via Telegram and returns a boolean
     * indicating the success or failure of the operation.
     * If sending has failed - process the error in Exception Service.
     *
     * @param reply the reply object to be sent
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendMessage(Reply reply) {
        SendMessage sendMessage = new SendMessage(
                reply.getUserId().toString(),
                reply.getMessageBody()
        );
        sendMessage.setReplyToMessageId(
                reply.getMsgId()
        );

        try {
            execute(sendMessage);
            return true;
        } catch (TelegramApiRequestException e) {
            exceptionService.handleTelegramApiRequestException(
                    e.getErrorCode(), e.getApiResponse(), reply.getUserId()
            );
        } catch (TelegramApiException e) {
            log.warn(
                    "Failed to send message to id " + reply.getUserId() + ". " + e.getMessage()
            );
        }
        return false;
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
