package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.command.SubscribeCommand;
import com.uvarchev.javatelebot.command.SubscriptionsCommand;
import com.uvarchev.javatelebot.command.UnsubscribeCommand;
import com.uvarchev.javatelebot.entity.Subscription;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.NewsProvider;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddValidSubscriptionForNewUser() {
        // Setup
        User user = new User(12345L);
        SubscribeCommand command = new SubscribeCommand("/subscribe NASA", "TestUser", 12345L);

        when(userRepository.findById(anyLong())).thenReturn(java.util.Optional.of(user));

        // Execute
        String response = subscriptionService.addSubscription(command, user);

        // Verify
        verify(userRepository, times(1)).save(any(User.class));

        // Since processing involves lots of string building, we're checking for a key part of the response
        assertTrue(response.contains("is added to your subscriptions"));
    }

    @Test
    void testAddingMultipleValidSubscriptionsForUser() {
        // Setup
        User user = new User(12345L);
        SubscribeCommand command = new SubscribeCommand("/subscribe NASA SPACENEWS", "TestUser", 12345L);

        // Execute
        String response = subscriptionService.addSubscription(command, user);

        // Verify
        assertTrue(response.contains("NASA is added to your subscriptions"), "NASA subscription should be added successfully.");
        assertTrue(response.contains("SPACENEWS is added to your subscriptions"), "SPACENEWS subscription should be added successfully.");
    }

    @Test
    void testAddingSubscriptionWithNoProvidersSpecified() {
        // Setup
        User user = new User(12345L);
        SubscribeCommand command = new SubscribeCommand("/subscribe", "TestUser", 12345L);

        // Execute
        String response = subscriptionService.addSubscription(command, user);

        // Verify
        assertTrue(response.contains("no valid options were provided"), "Should inform user about no providers specified.");
    }

    @Test
    void testDeactivatingAnExistingActiveSubscription() {
        // Setup
        User user = new User(12345L);
        Subscription subscription = new Subscription(user, NewsProvider.NASA);
        subscription.setActive(true);
        user.addSubscription(subscription);
        UnsubscribeCommand command = new UnsubscribeCommand("/unsubscribe NASA", "TestUser", 12345L);

        when(userRepository.findById(anyLong())).thenReturn(java.util.Optional.of(user));

        // Execute
        String response = subscriptionService.deactivateSubscription(command, user);

        // Verify
        assertFalse(subscription.isActive(), "Subscription should be deactivated.");
        assertTrue(response.contains("is deactivated"), "Response should indicate deactivation.");
    }

    @Test
    void testAttemptToDeactivateNonExistentSubscription() {
        // Setup
        User user = new User(12345L); // No subscriptions attached to this user
        UnsubscribeCommand command = new UnsubscribeCommand("/unsubscribe NASA", "TestUser", 12345L);

        when(userRepository.findById(anyLong())).thenReturn(java.util.Optional.of(user));

        // Execute
        String response = subscriptionService.deactivateSubscription(command, user);

        // Verify
        assertTrue(response.contains("you currently don't have any active subscriptions"), "Response should indicate the subscription does not exist.");
    }

    @Test
    void testDeactivatingSubscriptionWhenNoActiveSubscriptionsPresent() {
        // Setup
        User user = new User(12345L);
        // Assuming a user initially has no active subscriptions
        UnsubscribeCommand command = new UnsubscribeCommand("/unsubscribe NASA", "TestUser", 12345L);

        when(userRepository.findById(anyLong())).thenReturn(java.util.Optional.of(user));

        // Execute
        String response = subscriptionService.deactivateSubscription(command, user);

        // Verify
        assertTrue(response.contains("you currently don't have any active subscriptions"), "Response should indicate no active subscriptions.");
    }

    @Test
    void testUserHasActiveSubscriptions() {
        // Setup
        User user = new User(12345L);
        Subscription subscription = new Subscription(user, NewsProvider.NASA);
        subscription.setActive(true);
        user.addSubscription(subscription);
        SubscriptionsCommand command = new SubscriptionsCommand("TestUser", 12345L);

        // Execute
        String response = subscriptionService.listSubscriptions(command, user);

        // Verify
        assertTrue(response.contains("Your active subscriptions are:"), "Should list active subscriptions.");
        assertTrue(response.contains("NASA"), "Should mention NASA as an active subscription.");
    }

    @Test
    void testUserHasNoActiveSubscriptions() {
        // Setup
        User user = new User(12345L); // No active subscriptions
        SubscriptionsCommand command = new SubscriptionsCommand("TestUser", 12345L);

        // Execute
        String response = subscriptionService.listSubscriptions(command, user);

        // Verify
        assertTrue(response.contains("you currently don't have any active subscriptions"), "Should inform user about no active subscriptions.");
    }

    @Test
    void testListingAvailableSubscriptions() {
        // Setup
        User user = new User(12345L);
        // Assuming there are more providers in NewsProvider enum
        SubscriptionsCommand command = new SubscriptionsCommand("TestUser", 12345L);

        // Execute
        String response = subscriptionService.listSubscriptions(command, user);

        // Verify
        assertTrue(response.contains("Available subscriptions for you:"), "Should list available subscriptions for the user.");
    }


}