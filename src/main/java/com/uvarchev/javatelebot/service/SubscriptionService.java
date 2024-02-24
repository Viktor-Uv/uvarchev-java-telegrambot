package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.command.SubscribeCommand;
import com.uvarchev.javatelebot.entity.Subscription;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.NewsProvider;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private UserRepository userRepository;

    StringBuilder response;
    StringBuilder helpMessage;

    /**
     * Adds a subscription for the user to one or more news providers.
     *
     * @param command the subscribe command issued by the user
     * @param user    the user who wants to subscribe
     * @return the result of the subscription as a string,
     * or a help message if the command is invalid or no options are provided
     */
    public String addSubscription(SubscribeCommand command, User user) {
        initialiseReply();

        // Get all command line arguments
        String[] commandLineArgs = command.getMsgText().toUpperCase().split("\\s+");

        // Save valid requested providers
        Set<NewsProvider> desiredProviders = extractProvidersFromRequest(commandLineArgs);

        // If no valid options were provided - prepare help message and exit
        if (desiredProviders.isEmpty()) {
            return noValidOptionsHelpMessage(command.getUserName());
        }

        // If some options were not recognised, save a help message to class variable
        checkAllOptions(commandLineArgs);

        // Add requested subscriptions
        for (NewsProvider provider : desiredProviders) {
            // Create a new subscription with the given provider
            Subscription subscription = new Subscription(user, provider);

            // Add subscription and generate response
            subscribeAndGenerateResponse(subscription);
        }

        // Join response and help messages and then exit
        return response + "\n" + helpMessage;
    }

    /**
     * Subscribes the user to a news provider and generates a response message.
     *
     * @param subscription the subscription object that contains the user and the provider
     */
    protected void subscribeAndGenerateResponse(Subscription subscription) {
        NewsProvider provider = subscription.getProvider();
        User user = subscription.getUser();

        // Check if the same subscription for the same user already exists
        Subscription userSubscription = user.getEqualSubscription(subscription);
        if (userSubscription != null) {
            // If found - check if it's currently active
            if (userSubscription.isActive()) {
                // Add response that this subscription is already active
                response.append(provider)
                        .append(" is already subscribed to")
                        .append("\n");
            } else {
                // Reactivate old subscription, save to repository and add corresponding response
                userSubscription.setActive(true);
                subscriptionRepository.save(userSubscription);
                response.append(provider)
                        .append(" is reactivated")
                        .append("\n");
            }
        } else {
            // If subscription is unique - assign it for the user, save, and add corresponding response
            user.addSubscription(subscription);
            userRepository.save(user);
            response.append(provider)
                    .append(" is added to your subscriptions")
                    .append("\n");
        }
    }

    /**
     * Extracts a set of news providers from the command line arguments.
     *
     * @param commandLineArgs the array of strings that represent the command options
     * @return a set of news providers that are valid and recognised,
     * or all providers if the option 'ALL' is present
     */
    private Set<NewsProvider> extractProvidersFromRequest(String[] commandLineArgs) {
        // If special option 'ALL' received - extract All providers
        if (Arrays.asList(commandLineArgs).contains("ALL")) {
            // Return all providers
            return Arrays.stream(NewsProvider.values())
                    .collect(Collectors.toCollection(
                            LinkedHashSet::new
                    ));
        } else {
            // Return recognised options
            return Arrays.stream(commandLineArgs)
                    .filter(cla -> !cla.startsWith("/") && isValidOption(cla))
                    .map(NewsProvider::valueOf)
                    .collect(Collectors.toCollection(
                            LinkedHashSet::new
                    ));
        }
    }

    /**
     * Checks if the given option is a valid news provider.
     *
     * @param option the string that represents the option
     * @return true if the option is a valid news provider, false otherwise
     */
    private boolean isValidOption(String option) {
        return Arrays.toString(NewsProvider.values())
                .contains(option);
    }

    /**
     * Checks if all the options in the command line arguments are valid and recognised.
     * If not, generates a help message with the unrecognised options and saves it to a class variable.
     *
     * @param commandLineArgs the array of strings that represent the command options
     */
    private void checkAllOptions(String[] commandLineArgs) {
        // If special option "ALL" received - exit
        if (Arrays.asList(commandLineArgs).contains("ALL")) {
            return;
        }

        // Collect unrecognised options
        String unrecognisedOptions = Arrays.stream(commandLineArgs)
                .filter(cla -> !cla.startsWith("/") && !isValidOption(cla))
                .collect(Collectors.joining(", "));

        // If unrecognisedOptions aren't empty,
        if (!unrecognisedOptions.isEmpty()) {
            // Generate a help message and save it to class variable
            helpMessage.append(
                    unrecognisedOptionsHelpMessage(unrecognisedOptions)
            );
        }
    }

    /**
     * Generates a help message with the unrecognised options in the command.
     *
     * @param unrecognisedOptions the string that contains the unrecognised options separated by commas
     * @return the help message as a string, with the supported options listed
     */
    private String unrecognisedOptionsHelpMessage(String unrecognisedOptions) {
        return "Some options were not recognised: " + unrecognisedOptions + "\n" +
                "Supported options are: " + loadAvailableOptions() + ".";
    }

    /**
     * Generates a help message with the username and the supported options for the subscribe command.
     *
     * @param userName the name of the user who issued the command
     * @return the help message as a string, with the usage and the supported options listed
     */
    private String noValidOptionsHelpMessage(String userName) {
        return "Unfortunately, " + userName + ", no valid options were provided.\n" +
                "Usage: /subscribe [provider]\n" +
                "Supported options are: " + loadAvailableOptions() + ".";
    }

    /**
     * Loads all the available options for the news providers as a string.
     *
     * @return the string that contains the names of all supported news providers
     * separated by commas, or the option 'ALL'
     */
    // Load all available options
    private String loadAvailableOptions() {
        return Arrays.stream(NewsProvider.values())
                .map(Enum::name)
                .collect(Collectors.joining(", ")) + ", or ALL";
    }

    /**
     * Initialises the response and help message as string builders.
     */
    private void initialiseReply() {
        response = new StringBuilder();
        helpMessage = new StringBuilder();
    }

}
