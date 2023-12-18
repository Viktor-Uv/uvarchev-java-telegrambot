package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.entity.Subscription;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class PeriodicTask {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private final DateTimeFormatter customFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss O")
            .withZone(ZoneId.of("UTC"));

    private final Telebot telebot;

    @Autowired
    public PeriodicTask(Telebot telebot) {
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
                    ZonedDateTime.now().format(customFormatter) +
                            ": Scheduled task completed, no active subscriptions for any active user found"
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
                    ZonedDateTime.now().format(customFormatter) +
                            ": Scheduled task completed, no new articles were found"
            );

            // Update last read time
            subscriptions.forEach(
                    sub -> {
                        sub.setLastReadId(ZonedDateTime.now());
                        subscriptionRepository.save(sub);
                    }
            );

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

                    // Send message to each user
                    individualList
                            .forEach(
                                    news -> telebot.sendMessage(
                                            sub.getUser().getTelegramId().toString(),
                                            news.toString(),
                                            0
                                    )
                            );

                    // Update last read time
                    sub.setLastReadId(ZonedDateTime.now());
                    subscriptionRepository.save(sub);
                }
        );

        log.info(
                ZonedDateTime.now().format(customFormatter) +
                        ": Scheduled task completed, new articles were successfully sent to subscribers"
        );
    }
}
