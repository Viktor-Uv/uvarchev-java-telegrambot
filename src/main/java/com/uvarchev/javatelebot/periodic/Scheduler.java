package com.uvarchev.javatelebot.periodic;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.dto.Reply;
import com.uvarchev.javatelebot.entity.User;
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
        List<Long> failedAttempts = new ArrayList<>();

        // Keep track of how many articles did each user receive during one session
        Map<Long, Long> articlesReceived = new HashMap<>();

        // For each reply object
        while (!replyQueue.isEmpty()) {
            // Try to send reply via Telegram and update subscription's last read id
            processReply(replyQueue.poll(), currentTime, failedAttempts, articlesReceived);
        }

        // Update log
        log.info("Scheduled task completed, new articles were successfully sent to subscribers");

        // Increment articles received count in the database for each user who received updates
        schedulerService.incrementReplyCount(articlesReceived);
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
            // Try to send a reply via Telegram
            processReply(replies.poll());
        }

        log.info("Daily Statistics was sent to all administrators");
    }

    /**
     * Processes a single reply object and sends it to the user via Telegram.
     * Skips sending of the updates to a recipients that failed to receive an update.
     * Log the failed attempt.
     *
     * @param reply            the reply object to be sent
     * @param currentTime      the current time of the scheduler
     * @param failedAttempts   a list of user ids that failed to receive the update
     * @param articlesReceived a map that stores the user ids and the number of articles received
     */
    private void processReply(
            Reply reply,
            ZonedDateTime currentTime,
            List<Long> failedAttempts,
            Map<Long, Long> articlesReceived
    ) {
        // Check if the address is amongst the failed attempt addresses
        if (failedAttempts.contains(reply.getUserId())) {
            // Skip sending update to userId from a failed attempts list
            return;
        }

        // Try to send a message to the user
        try {
            telebot.sendMessage(reply);
        } catch (RuntimeException e) {
            // Stop sending next updates during this session to this user by remembering its userId
            failedAttempts.add(reply.getUserId());
            log.warn("Attempt sending update to userId " + reply.getUserId() + " has failed");
            return;
        }

        // Upon success - update last read time for the individual subscription
        schedulerService.updateSubscriptionListLastReadTime(
                reply.getSubscriptionId(),
                currentTime
        );

        // Increment user's articles received count
        incrementArticlesReceived(articlesReceived, reply.getUserId());
    }

    /**
     * A helper method that tries to send a reply object to the user via Telegram and logs any failure.
     *
     * @param reply the reply object to be sent
     */
    private void processReply(Reply reply) {
        // Try to send a message to the user
        try {
            telebot.sendMessage(reply);
        } catch (RuntimeException ignored) {
            log.warn("Attempt sending update to userId " + reply.getUserId() + " has failed");
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
