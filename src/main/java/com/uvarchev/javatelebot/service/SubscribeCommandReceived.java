package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.bot.command.Command;
import com.uvarchev.javatelebot.entity.Parameter;
import com.uvarchev.javatelebot.entity.Subscription;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.enums.ServiceType;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class SubscribeCommandReceived {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Transactional
    //
    //
    public void execute(Update update, Telebot telebot) {
        // Get user details
        Long userId = update.getMessage().getFrom().getId();

        // Get command options
        String[] commandLineArgs = update.getMessage().getText().toUpperCase().split("\\s+");

//        // Generate reply and send it
//        telebot.sendMessage(
//                userId.toString(),
//                generateReply(userId, commandLineArgs),
//                update.getMessage().getMessageId()
//        );
    }

    private String generateReply(Long userId, String[] commandLineArgs) {
        // Validate service option (2nd word in command line)
        ServiceType desiredService = validateOption(commandLineArgs);

        // if wrong, or no service name received - generate error msg
        if (desiredService == null) {
            // Load all available services
            String availableServices = Arrays.stream(ServiceType.values())
                    .map(commandType -> commandType.name().toLowerCase())
                    .collect(Collectors.joining(", "));

            // Generate error msg with all available services
            return "Sorry, correct subscription service name is required.\n" +
                    "Usage: /subscribe <desired_service> <option>\n" +
                    "Currently supported services are: " + availableServices + ".";
        }

        // Check proper usage of Weather service (3rd word in command line)
        String parameter = commandLineArgs.length < 3 ? null : commandLineArgs[2];
        if (
                desiredService.equals(ServiceType.WEATHER)
                        && parameter == null
        ) {
            // if no option were received - generate error msg
            return "Sorry, but correct usage is: /subscribe <location>";
        }

        // Get user from repository
        User user = userRepository
                .findById(userId)
                .orElse(null);
        if (user == null) {
            return "Sorry, but your user ID was not found. Use '/start' to get yourself registered";
        }

        // Create new subscription
        Subscription subscription = new Subscription(user, desiredService);

        // If command has any option - create Parameter and add it to Subscription
        String optionalParameter;
        if (parameter != null) {
            subscription.addParameter(
                    new Parameter(
                            subscription,
                            desiredService.getParameter(),
                            parameter
                    )
            );
            optionalParameter = " - " + parameter;
        } else {
            optionalParameter = "";
        }

        return user.getSubscriptions().stream()
                // Trying to find similar subscription
                .filter(sub -> sub.equals(subscription))
                .findFirst()
                .map(
                        // If same subscription was found,
                        sub -> {
                            if (sub.isActive()) {
                                // and it's currently active - send Error msg
                                return "Sorry, but same subscription is already active";
                            } else {
                                // Otherwise reactivate old subscription
                                sub.setActive(true);
                                subscriptionRepository.save(sub);
                                return "Previous inactive subscription was reactivated successfully";
                            }
                        }
                )
                .orElseGet(
                        () -> {
                            // If it's unique - save new subscription for user and then save user
                            user.addSubscription(subscription);
                            userRepository.save(user);
                            return "Subscription for '" + desiredService +
                                    optionalParameter + "' added successfully!";
                        }
                );
    }

    private static ServiceType validateOption(String[] options) {
        if (options.length < 2) {
            return null;
        }

        try {
            return ServiceType.valueOf(options[1]);
        } catch (Exception ignored) {
            return null;
        }
    }
}
