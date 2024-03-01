package com.uvarchev.javatelebot.periodic;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.dto.Reply;
import com.uvarchev.javatelebot.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * A component class that handles the scheduling tasks.
 */
@Slf4j
@Component
public class Scheduler {

    @Autowired
    private Telebot telebot;
    @Autowired
    private SchedulerService schedulerService;

    /**
     * A scheduled task that runs every hour to get and send news updates to subscribers.
     */
    @Scheduled(cron = "0 0 * * * *")
    private void getAndSendNewsUpdate() {
        // Get a Reply object Queue with generated messages and addresses
        Queue<Reply> replyQueue = schedulerService.fetchScheduledNewsUpdate();

        // Validate received replyQueue
        if (replyQueue == null) {
            // Update log and exit scheduler
            log.info("Scheduled task completed, no active subscriptions for any active user found");
            return;
        } else if (replyQueue.isEmpty()) {
            // Update log and exit scheduler
            log.info("Scheduled task completed, no new articles were found");
            return;
        }

        // Save current time
        final ZonedDateTime currentTime = ZonedDateTime.now();

        // Keep track of failed attempt addresses
        List<Long> failedAttempts = new LinkedList<>();

        // Keep track of how many articles did each user receive during one session
        Map<Long, Long> articlesReceivedCount = new HashMap<>();

        // Save successfully sent subscription ids
        Set<Long> receivedSubscriptionIds = new HashSet<>();

        // For each reply object
        while (!replyQueue.isEmpty()) {
            // Try to send reply via Telegram and update subscription's last read id
            processReply(replyQueue.poll(), failedAttempts, articlesReceivedCount, receivedSubscriptionIds);
        }

        // Increment articles received count in the database for each user who received updates
        schedulerService.incrementReplyCount(articlesReceivedCount);
        // Update LastReadId for each subscription sent
        schedulerService.updateSubscriptionListLastReadTime(receivedSubscriptionIds, currentTime);

        // Update log
        log.info("Scheduled task completed, new articles were successfully sent to subscribers");
    }

    @Scheduled(cron = "5 0 8 * * *")
    private void sendDailyStatistics() {
        // Get daily statistics in the form of Reply
        Queue<Reply> replies = schedulerService.getDailyStatistics();

        if (replies.isEmpty()) {
            // If an empty list received - log and exit
            log.info("Daily Statistics. No administrators found to send messages to");
            return;
        }

        // For each reply object
        while (!replies.isEmpty()) {
            // Send a reply via Telegram
            telebot.sendMessage(replies.poll());
        }

        log.info("Daily Statistics was sent to all administrators");
    }

    /**
     * Processes a single reply object and sends it to the user via Telegram.
     * Skips sending of the updates to a recipients that failed to receive an update.
     *
     * @param reply                   The reply object containing the user id, message, and subscription id.
     * @param failedAttempts          The list of user ids that failed to receive a message.
     * @param articlesReceivedCount   The map of user ids and the number of articles they received.
     * @param receivedSubscriptionIds The list of subscription ids that were successfully sent to the users.
     */
    private void processReply(
            Reply reply,
            List<Long> failedAttempts,
            Map<Long, Long> articlesReceivedCount,
            Set<Long> receivedSubscriptionIds
    ) {
        // Check if the address is amongst the failed attempt addresses
        if (failedAttempts.contains(reply.getUserId())) {
            // Skip sending update to userId from a failed attempts list
            return;
        }

        // Send a message to the user and check if it was successfully sent
        if (telebot.sendMessage(reply)) {
            // Increment user's articles received count
            incrementArticlesReceived(articlesReceivedCount, reply.getUserId());
            // Save subscription id
            receivedSubscriptionIds.add(reply.getSubscriptionId());
        } else {
            // Stop sending next updates during this session to this user by remembering its userId
            failedAttempts.add(reply.getUserId());
        }
    }

    /**
     * A helper method that increments the number of articles received by a user in a map.
     *
     * @param map    the map that stores the user ids and the number of articles received
     * @param userId the user id to be incremented
     */
    private void incrementArticlesReceived(Map<Long, Long> map, Long userId) {
        map.compute(
                userId,
                (key, value) -> (value == null) ? 1 : ++value
        );
    }

}
