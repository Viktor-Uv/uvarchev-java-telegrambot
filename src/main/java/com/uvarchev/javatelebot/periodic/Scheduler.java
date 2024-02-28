package com.uvarchev.javatelebot.periodic;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.dto.Reply;
import com.uvarchev.javatelebot.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

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

        // For each reply object
        while (!replyQueue.isEmpty()) {
            // Try to send reply via Telegram and update subscription's last read id
            processReply(replyQueue.poll(), currentTime, failedAttempts);
        }

        // Update log
        log.info("Scheduled task completed, new articles were successfully sent to subscribers");
    }

    /**
     * Processes a single reply object and sends it to the user via Telegram.
     * Skips sending of the updates to a recipients that failed to receive an update.
     * Log the failed attempt.
     *
     * @param reply          the reply object to be sent
     * @param currentTime    the current time of the scheduler
     * @param failedAttempts a list of user ids that failed to receive the update
     */
    private void processReply(Reply reply, ZonedDateTime currentTime, List<Long> failedAttempts) {
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
    }

}
