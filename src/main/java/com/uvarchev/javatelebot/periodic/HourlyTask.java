package com.uvarchev.javatelebot.periodic;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.entity.Subscription;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import com.uvarchev.javatelebot.network.ApiClient;
import com.uvarchev.javatelebot.dto.News;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class HourlyTask {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private final Telebot telebot;

    @Autowired
    public HourlyTask(Telebot telebot) {
        this.telebot = telebot;
    }

    // Scheduled hourly task
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    protected void sendHourlyUpdates() {
        // Collect all active subscriptions
        List<Subscription> subscriptions = subscriptionRepository.findAllActiveSubscriptions();
        if (subscriptions.isEmpty()) {
            log.info(
                    "Scheduled task completed, no active subscriptions for any active user found"
            );
            return;
        }

        ZonedDateTime oldestRead = subscriptions.stream()
                .map(Subscription::getLastReadId)
                .min(ChronoZonedDateTime::compareTo)
                .orElse(ZonedDateTime.now());

        ApiClient client = new ApiClient(
                oldestRead.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
                )
        );
        List<News> newsList = client.getNews();

        // If no news was returned
        if (newsList.isEmpty()) {
            log.info(
                    "Scheduled task completed, no new articles were found"
            );

//            // Update last read time
//            subscriptions.forEach(
//                    sub -> {
//                        sub.setLastReadId(ZonedDateTime.now());
//                        subscriptionRepository.save(sub);
//                    }
//            );

            return;
        }

        subscriptions.forEach(
                sub -> {
                    // Create individual lists for each subscriber
                    List<News> individualList = newsList.stream()
                            .filter(
                                    news -> ZonedDateTime.parse(news.getPublishedAt())
                                            .isAfter(
                                                    sub.getLastReadId()
                                            )
                            )
                            .toList();

//                    // Send message to each user
//                    individualList
//                            .forEach(
//                                    news -> telebot.sendMessage(
//                                            sub.getUser().getTelegramId().toString(),
//                                            news.toString(),
//                                            0
//                                    )
//                            );

                    // If user has blocked the bot, he will become inactive in sendMessage() method,
                    // this is being checked here
//                    if (sub.getUser().isActive()) {
//                        // If User is still active - update his subscription's last read time
//                        sub.setLastReadId(ZonedDateTime.now());
//                        subscriptionRepository.save(sub);
//                    }
                }
        );

        log.info(
                "Scheduled task completed, new articles were successfully sent to subscribers"
        );
    }
}
