package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.dto.Reply;
import com.uvarchev.javatelebot.entity.Subscription;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.UserRole;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenNoAdministratorsFound_thenEmptyQueue() {
        // Arrange
        when(userRepository.getUsersByUserRole(UserRole.ADMIN)).thenReturn(new LinkedList<>());

        // Act
        Queue<Reply> replies = schedulerService.getDailyStatistics();

        // Assert
        assertTrue(replies.isEmpty(), "Queue should be empty when no administrators are found.");
    }

    @Test
    void whenAdministratorsFoundButEmptyStatisticsMessage_thenRepliesWithEmptyMessage() {
        // Arrange
        User admin = new User();
        admin.setTelegramId(123L);

        List<User> admins = Arrays.asList(admin);

        when(userRepository.getUsersByUserRole(UserRole.ADMIN)).thenReturn(admins);
        when(userService.getAdminStatistics()).thenReturn("");

        // Act
        Queue<Reply> replies = schedulerService.getDailyStatistics();

        // Assert
        assertFalse(replies.isEmpty(), "Queue should contain messages.");
        assertEquals("", replies.poll().getMessageBody(), "Message should be empty.");
    }

    @Test
    void whenAdministratorsFoundWithStatisticsMessage_thenRepliesContainStatistics() {
        // Arrange
        User admin1 = new User();
        admin1.setTelegramId(123L);

        User admin2 = new User();
        admin2.setTelegramId(456L);

        List<User> admins = Arrays.asList(admin1, admin2);
        String statisticsMessage = "Daily Stats: 100 messages";

        when(userRepository.getUsersByUserRole(UserRole.ADMIN)).thenReturn(admins);
        when(userService.getAdminStatistics()).thenReturn(statisticsMessage);

        // Act
        Queue<Reply> replies = schedulerService.getDailyStatistics();

        // Assert
        assertEquals(2, replies.size(), "Queue should contain messages for each administrator.");
        replies.forEach(reply -> assertEquals(statisticsMessage, reply.getMessageBody(), "Messages should contain the statistics."));
    }

    @Test
    void updateSubscriptionListLastReadTime_updatesLastReadIdForAllProvidedSubscriptions() {
        // Mocking the subscriptions to be updated
        Subscription sub1 = new Subscription();
        Subscription sub2 = new Subscription();
        Set<Long> ids = new HashSet<>();
        ids.add(1L);
        ids.add(2L);
        ZonedDateTime updateTime = ZonedDateTime.now();

        // Assuming findAllById would return the given subscriptions
        when(subscriptionRepository.findAllById(ids)).thenReturn(Arrays.asList(sub1, sub2));

        schedulerService.updateSubscriptionListLastReadTime(ids, updateTime);

        // Verify saveAll is called with updated subscriptions
        verify(subscriptionRepository).saveAll(argThat(subscriptions -> {
            // Casting for simplicity; use a safer check in production code
            List<Subscription> subs = (List<Subscription>) subscriptions;
            return subs.stream().allMatch(subscription -> updateTime.equals(subscription.getLastReadId()));
        }));
    }

    @Test
    void incrementReplyCount_incrementsArticlesReceivedForAllProvidedUsers() {
        // Assuming users with initial articles received
        User user1 = new User(); user1.setArticlesReceived(10);
        User user2 = new User(); user2.setArticlesReceived(5);
        Map<Long, Long> increments = new HashMap<>();
        increments.put(user1.getTelegramId(), 2L);
        increments.put(user2.getTelegramId(), 3L);

        // Assuming findAllById would return the given users
        when(userRepository.findAllById(increments.keySet())).thenReturn(Arrays.asList(user1, user2));

        schedulerService.incrementReplyCount(increments);

        // Verify saveAll is called with users having updated articlesReceived counts
        verify(userRepository).saveAll(argThat(users -> {
            // Casting for simplicity; use a safer check in production code
            List<User> userUpdates = (List<User>) users;
            return userUpdates.stream().allMatch(user ->
                    increments.get(user.getTelegramId()) + (user.getTelegramId().equals(user1.getTelegramId()) ? 10 : 5) == user.getArticlesReceived()
            );
        }));
    }

}
