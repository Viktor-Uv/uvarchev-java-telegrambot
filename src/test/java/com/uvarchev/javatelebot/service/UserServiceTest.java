package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.command.StartCommand;
import com.uvarchev.javatelebot.command.StopCommand;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.UserRole;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testActivateExistingAuthorizedUser() {
        StartCommand startCommand = new StartCommand("User1", 1L);
        User user = new User(1L);
        user.setUserRole(UserRole.USER);

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(subscriptionService.listSubscriptions(any(), any())).thenReturn("No active subscriptions.");

        String response = userService.activateUser(startCommand, user);

        assertTrue(response.contains("nice to see you again!"));
        verify(userRepository, times(1)).save(user);
        assertEquals(UserRole.USER, user.getUserRole());
    }

    @Test
    void testActivateExistingUnauthorisedUser() {
        StartCommand startCommand = new StartCommand("User2", 2L);
        User user = new User(2L);
        user.setUserRole(UserRole.UNAUTHORISED);

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(subscriptionService.listSubscriptions(any(), any())).thenReturn("Subscription details...");

        String response = userService.activateUser(startCommand, user);

        assertTrue(response.contains("nice to see you again!"));
        verify(userRepository, times(1)).save(user);
        assertEquals(UserRole.USER, user.getUserRole()); // Ensure a role is changed to USER
    }

    @Test
    void testActivateNewUser() {
        StartCommand startCommand = new StartCommand("User3", 3L);
        User newUser = null; // Simulate new user not found in DB

        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(subscriptionService.listSubscriptions(any(), any())).thenReturn("Welcome package...");

        String response = userService.activateUser(startCommand, newUser);

        assertTrue(response.contains("nice to meet you!"));
        verify(userRepository, times(1)).save(any(User.class)); // Ensure a new user is saved
    }

    @Test
    void testDeactivateUser() {
        StopCommand stopCommand = new StopCommand("User1", 1L);

        // Simulate the expected database operations
        when(userRepository.deactivateUserByUserId(anyLong())).thenReturn(1);
        when(subscriptionRepository.deactivateAllUserSubscriptionsByUserId(anyLong())).thenReturn(1);

        // Call the method under test
        String response = userService.deactivateUser(stopCommand);

        // Verify the interaction with the repositories
        verify(userRepository, times(1)).deactivateUserByUserId(1L);
        verify(subscriptionRepository, times(1)).deactivateAllUserSubscriptionsByUserId(1L);

        // Assert the correct response is returned
        assertTrue(response.contains("Any active subscriptions were deactivated. Bye, User1, till next time!"));
    }

    @Test
    void testDeactivateUserNoActiveSubscriptions() {
        StopCommand stopCommand = new StopCommand("User2", 2L);

        // Assuming deactivation is still successful, subscription return value indicates no active subs
        when(userRepository.deactivateUserByUserId(anyLong())).thenReturn(1);
        when(subscriptionRepository.deactivateAllUserSubscriptionsByUserId(anyLong())).thenReturn(0);

        // Call the method under test
        String response = userService.deactivateUser(stopCommand);

        // Verify the interaction with the repositories even if there are no active subscriptions to deactivate
        verify(userRepository, times(1)).deactivateUserByUserId(2L);
        verify(subscriptionRepository, times(1)).deactivateAllUserSubscriptionsByUserId(2L);

        // Assert the response is accurate
        assertTrue(response.contains("Any active subscriptions were deactivated. Bye, User2, till next time!"));
    }

    @Test
    void testGetAdminStatistics() {
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByUserRoleIsNot(any())).thenReturn(4);
        when(subscriptionRepository.countAllByActiveIs(anyBoolean())).thenReturn(3);
        when(userRepository.getTotalOfArticlesReceived()).thenReturn(100L);
        when(subscriptionRepository.findDistinctTopProviders()).thenReturn(Collections.emptyList()); // Simulate no providers found
        when(subscriptionRepository.getMostRecentReadTime()).thenReturn(ZonedDateTime.now());

        String stats = userService.getAdminStatistics();

        assertNotNull(stats);
        assertTrue(stats.contains("Total number of registered users: 5"));
        assertTrue(stats.contains("Total number of active users: 4"));
        assertTrue(stats.contains("Total number of active subscriptions: 3"));
        assertTrue(stats.contains("Total number of articles sent: 100"));
        assertTrue(stats.contains("Top News Provider(s): No active subscriptions"));
        assertTrue(stats.contains("Most recent update: ")); // Check starts of date format

        // Verification of repository method usage
        verify(userRepository).count();
        verify(userRepository).countByUserRoleIsNot(any());
        verify(subscriptionRepository).countAllByActiveIs(true);
        verify(userRepository).getTotalOfArticlesReceived();
        verify(subscriptionRepository).findDistinctTopProviders();
        verify(subscriptionRepository).getMostRecentReadTime();
    }

    @Test
    void testDeactivateUserAndItsSubscriptions() {
        long userId = 1L;
        int expectedUserDeactivateCount = 1; // Assuming one user gets deactivated
        int expectedSubscriptionsDeactivateCount = 3; // Assuming three subscriptions get deactivated

        // Mocking to return specific counts reflecting the assumed operation results
        when(userRepository.deactivateUserByUserId(userId)).thenReturn(expectedUserDeactivateCount);
        when(subscriptionRepository.deactivateAllUserSubscriptionsByUserId(userId)).thenReturn(expectedSubscriptionsDeactivateCount);

        userService.deactivateUserAndItsSubscriptions(userId);

        // Verify that the repository methods were indeed called
        verify(userRepository, times(1)).deactivateUserByUserId(userId);
        verify(subscriptionRepository, times(1)).deactivateAllUserSubscriptionsByUserId(userId);

        // Assert return values
        assertEquals(expectedUserDeactivateCount,
                userRepository.deactivateUserByUserId(userId),
                "The user deactivation count does not match the expected value.");

        assertEquals(expectedSubscriptionsDeactivateCount,
                subscriptionRepository.deactivateAllUserSubscriptionsByUserId(userId),
                "The subscription deactivation count does not match the expected value.");
    }

}