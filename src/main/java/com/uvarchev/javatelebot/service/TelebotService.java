package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.command.*;
import com.uvarchev.javatelebot.entity.Parameter;
import com.uvarchev.javatelebot.entity.Subscription;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.enums.ServiceType;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class TelebotService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    // '/any_unrecognised_command'
    // Reply with unrecognised command error. Remind of available commands
    public String processCommand(UnrecognisedCommand command) {
        String supportedCommands = Arrays.stream(
                        CommandType.values()
                )
                .map(commandType -> commandType.name().toLowerCase())
                .collect(Collectors.joining(", /")); // Load all available commands
        return "Sorry, " + command.getFirstName() + ", command was not recognised.\n" +
                "Currently supported commands are: /" + supportedCommands + ".";
    }

    // '/start'
    // Register new user or reactivate old, but inactive user
    public String processCommand(StartCommand command) {
        return userRepository.findById(
                        command.getUserId()
                )
                .map(
                        // if user was found - set active and save to repo
                        oldUser -> {
                            oldUser.setActive(true);
                            userRepository.save(oldUser);
                            return "Hi, " + command.getFirstName() + ", nice to see you again!";
                        }
                )
                .orElseGet(
                        // otherwise - create new user and save to repo
                        () -> {
                            userRepository.save(
                                    new User(command.getUserId())
                            );
                            return "Hi, " + command.getFirstName() + ", nice to meet you!";
                        }
                );
    }

    // '/stop'
    // Set leaving user inactive
    public String processCommand(StopCommand command) {
        return userRepository.findById(
                        command.getUserId()
                )
                .map(
                        // if user was found - set inactive and save to repo
                        leavingUser -> {
                            leavingUser.setActive(false);
                            userRepository.save(leavingUser);
                            return "Updates are stopped. Bye, " + command.getFirstName() + ", till next time!";
                        }
                )
                .orElse(
                        // otherwise just say bye
                        "Bye, " + command.getFirstName() + ", till next time!"
                );
    }

    // '/addSub <service>'
    // Add subscription for user
    @Transactional
    public String processCommand(AddSubCommand command) {
        // Check proper usage for Weather service
        if (command.getServiceType().equals(ServiceType.WEATHER)
                && command.getOption() == null) {
            return "Sorry, but correct usage is: /addSub <location>";
        }

        // Get user from repository, or create new user
        User user = userRepository.findById(
                        command.getUserId()
                )
                .orElseGet(
                        () -> new User(command.getUserId())
                );

        // Create new subscription
        Subscription subscription = new Subscription(user, command.getServiceType());
        // If command has any option - create Parameter and add it to Subscription
        String optional;
        if (command.getOption() != null) {
            subscription.addParameter(
                    new Parameter(
                            subscription,
                            command.getServiceType().getParameter(),
                            command.getOption()
                    )
            );
            optional = " - " + command.getOption();
        } else {
            optional = "";
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
                            return "Subscription for '" + command.getServiceType() +
                                    optional + "' added successfully!";
                        }
                );
    }

    // '/listSub'
    // Lists all active subscriptions
    @Transactional
    public String processCommand(ListSubCommand command) {
        StringBuilder response = new StringBuilder();

        subscriptionRepository
                .findByUserIdAndActive(command.getUserId())
                .forEach(
                        subscription -> response.append("\n").append(subscription)
                );

        if (response.isEmpty()) {
            return "You don't have any active subscriptions";
        } else {
            response.insert(
                    0,
                    "List of your active subscriptions:\n" +
                            "ID | Name | Parameters"
            );
            return response.toString();
        }
    }

    // '/removeSubId <subscription_id>'
    // Deactivates subscription with the given ID
    public String processCommand(RemoveSubId command) {
        // Check if id supplied is correct
        Long subId = null;
        try {
            subId = Long.parseLong(command.getSubscriptionId());
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
        }
        if (subId == null) {
            return "Sorry, but the <subscription_id> provided is not a valid number";
        }

        // Try to deactivate subscription
        if (subscriptionRepository.deactivateById(subId) == 0) {
            return "Sorry, but subscription with the <subscription_id> provided was not found";
        } else {
            return "Subscription with ID " + subId + " has been successfully removed";
        }
    }
}
