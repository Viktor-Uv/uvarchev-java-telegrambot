package com.uvarchev.javatelebot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExceptionServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private ExceptionService exceptionService;

    @Test
    void handleTelegramApiRequestException_userBlockedAndDeactivated() {
        int errorCode = 403;
        String errorMessage = "Forbidden: bot was blocked by the user";
        long userId = 123;

        exceptionService.handleTelegramApiRequestException(errorCode, errorMessage, userId);

        verify(userService, times(1)).deactivateUserAndItsSubscriptions(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void handleTelegramApiRequestException_userNotBlocked_doNothing() {
        int errorCode = 400;
        String errorMessage = "Chat not Found";
        long userId = 456;

        exceptionService.handleTelegramApiRequestException(errorCode, errorMessage, userId);

        verifyNoInteractions(userService);
    }

}
