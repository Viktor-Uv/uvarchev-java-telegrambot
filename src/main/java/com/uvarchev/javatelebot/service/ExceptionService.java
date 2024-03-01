package com.uvarchev.javatelebot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExceptionService {

    @Autowired
    private UserService userService;

    /**
     * A method that handles the Telegram API request exception by deactivating the user
     * and its subscriptions if the user has blocked the bot. And logging the error.
     *
     * @param errorCode    the error code returned by the Telegram API
     * @param errorMessage the error message returned by the Telegram API
     * @param userId       the user id of the user who caused the exception
     */
    public void handleTelegramApiRequestException(int errorCode, String errorMessage, long userId) {
        // Check if user has blocked the bot
        if (errorCode == 403 && errorMessage.equals("Forbidden: bot was blocked by the user")) {
            // Deactivate user and it's subscriptions
            userService.deactivateUserAndItsSubscriptions(userId);
        }

        // Log the error
        log.error("Failed to send message to id " + userId + ". " + errorCode + " " + errorMessage);
    }

}
