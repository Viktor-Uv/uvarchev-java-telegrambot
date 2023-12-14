package com.uvarchev.javatelebot.bot;

import com.uvarchev.javatelebot.command.*;
import com.uvarchev.javatelebot.service.ApiClient;
import com.uvarchev.javatelebot.service.News;
import com.uvarchev.javatelebot.entity.Subscription;
import com.uvarchev.javatelebot.enums.ServiceType;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import com.uvarchev.javatelebot.service.TelebotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Telebot extends TelegramLongPollingBot {

    @Autowired
    private TelebotService botService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private final TelebotConfig config;

    public Telebot(TelebotConfig config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {

            // Check if message text starts with some command
            if (update.getMessage().getText().startsWith("/")) {

                // Generate answer and reply:
                sendMessage(
                        update.getMessage().getChatId().toString(),
                        getAnswer(update),
                        update.getMessage().getMessageId()
                );
            }
        }
    }

    private String getAnswer(Update update) {
        // Get user details
        Long userId = update.getMessage().getFrom().getId();
        String firstName = update.getMessage().getChat().getFirstName();

        // Extract input command, capitalise it, and split by words (command without "/")
        String[] commandString = update.getMessage().getText()
                .substring(1).toUpperCase().split("\\s+");

        // Process input command and generate answer
        return switch (commandString[0]) {
            case "START" -> {
                StartCommand command = new StartCommand(userId, firstName);
                yield botService.processCommand(command);
            }
            case "STOP" -> {
                StopCommand command = new StopCommand(userId, firstName);
                yield botService.processCommand(command);
            }
            case "ADDSUB" -> {
                // Check usage
                if (commandString.length > 1) {
                    // IF correct option name was received,
                    ServiceType desiredService = AddSubCommand.validateOption(commandString[1]);
                    if (desiredService != null) {
                        // AND this option name is valid,
                        // THEN subscribe user
                        AddSubCommand command;
                        if (commandString.length > 2) {
                            command = new AddSubCommand(userId, desiredService, commandString[2]);
                        } else {
                            command = new AddSubCommand(userId, desiredService);
                        }
                        yield botService.processCommand(command);
                    }
                }
                // If any condition not satisfied - generate error message
                String availableServices = Arrays.stream(ServiceType.values())
                        // Load all available services
                        .map(commandType -> commandType.name().toLowerCase())
                        .collect(Collectors.joining(", "));
                yield "Sorry, correct subscription service name was not provided.\n" +
                        "Usage: /addSub <desired_service> <option>\n" +
                        "Currently supported services are: " + availableServices + ".";
            }
            case "LISTSUB" -> {
                ListSubCommand command = new ListSubCommand(userId);
                yield botService.processCommand(command);
            }
            default -> {
                UnrecognisedCommand command = new UnrecognisedCommand(firstName);
                yield botService.processCommand(command);
            }
            case "REMOVESUBID" -> {
                // If wrong usage - generate error message
                if (commandString.length != 2) {
                    yield "Sorry, but correct usage is: /removeSubId <subscription_id>\n" +
                            "You can use /listSub command to find desired subscription id";
                }
                RemoveSubId command = new RemoveSubId(userId, commandString[1]);
                yield botService.processCommand(command);
            }
        };
    }

    public void sendMessage(String address, String messageBody, int msgIdToReplyTo) {
        SendMessage sendMessage = new SendMessage(address, messageBody);
        sendMessage.setReplyToMessageId(msgIdToReplyTo);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    // Scheduled hourly task
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    protected void broadcast() {
        // Collect all active subscriptions
        List<Subscription> subscriptions = subscriptionRepository.findAllActiveSubscriptions();
        if (subscriptions.isEmpty()) {
            log.info(
                    ZonedDateTime.now() +
                            ": Scheduled task finished, no active subscriptions for any active user found"
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

        if (newsList.isEmpty()) {
            log.info(
                    ZonedDateTime.now() +
                            ": Scheduled task finished, no new articles were found"
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
                                    news -> sendMessage(
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
                ZonedDateTime.now() +
                        ": Scheduled task finished, new articles were successfully sent to subscribers"
        );

    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

}
