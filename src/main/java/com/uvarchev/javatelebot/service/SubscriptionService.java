package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.command.SubscribeCommand;
import com.uvarchev.javatelebot.command.SubscriptionsCommand;
import com.uvarchev.javatelebot.command.UnsubscribeCommand;
import com.uvarchev.javatelebot.entity.Subscription;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.CommandType;
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

    /**
     * Adds a subscription for the user to one or more news providers.
     *
     * @param command the subscribe command issued by the user
     * @param user    the user who wants to subscribe
     * @return the result of the operation as a string,
     * or a help message if the command is invalid or no options are provided
     */
    public String addSubscription(SubscribeCommand command, User user) {
        return processDesiredSubscriptions(
                user, command.getType(), command.getMsgText(), command.getUserName()
        );
    }

    /**
     * Processes an unsubscribe command and deactivates the subscription
     * with the given ID if the user has any active subscriptions.
     *
     * @param command the unsubscribe command issued by the user
     * @param user    the user object that represents the user who wants to unsubscribe
     * @return the result of the operation as a string, or a help message if
     * the command is invalid or no options are provided or there are no active subscriptions
     */
    public String deactivateSubscription(UnsubscribeCommand command, User user) {
        // Check if user has any active subscriptions
        if (user.getAllActiveSubscriptions().isEmpty()) {
            // If nothing found - reply with a corresponding message and exit
            return "Unfortunately, " + command.getUserName() +
                    ", you currently don't have any active subscriptions";
        } else {
            return processDesiredSubscriptions(
                    user, command.getType(), command.getMsgText(), command.getUserName()
            );
        }
    }

    /**
     * Lists all active subscriptions for the user and also shows available subscriptions.
     *
     * @param user the user whose subscription list is being requested
     * @return a string that lists all the user's active subscriptions and available ones
     */
    public String listSubscriptions(SubscriptionsCommand command, User user) {
        Set<Subscription> userSubscriptions = user.getAllActiveSubscriptions();
        StringBuilder response = new StringBuilder();

        if (userSubscriptions.isEmpty()) {
            response.append("Unfortunately, ")
                    .append(command.getUserName())
                    .append(", you currently don't have any active subscriptions.\n");
        } else {
            response.append("Your active subscriptions are:\n");
            userSubscriptions.forEach(
                    s -> response.append("- ")
                            .append(s.getProvider().toString())
                            .append("\n")
            );
        }

        // Get all available providers
        Set<NewsProvider> availableProviders = Arrays.stream(NewsProvider.values())
                .collect(Collectors.toCollection(
                        LinkedHashSet::new
                ));

        // Remove user's subscriptions from the availableProviders
        userSubscriptions.forEach(
                s -> availableProviders.remove(s.getProvider())
        );

        if (!availableProviders.isEmpty()) {
            response.append("\nAvailable subscriptions for you:\n")
                    .append(availableProviders.stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(", "))
                    ).append(".");
        } else {
            response.append("\nYou have subscribed to all available providers.\n");
        }

        return response.toString();
    }

    /**
     * Processes the desired subscriptions for the user based on the command type and the message text.
     *
     * @param user        the user object that represents the user who wants to subscribe or unsubscribe
     * @param commandType the type of the command issued by the user
     * @param msgText     the message text that contains the command options
     * @param userName    the name of the user who issued the command
     * @return a string that indicates the result of the subscription process,
     * or a help message if the command is invalid or no options are provided
     */
    private String processDesiredSubscriptions(
            User user,
            CommandType commandType,
            String msgText,
            String userName
    ) {
        // Get all command line arguments
        String[] commandLineArgs = extractCommandLineArgs(msgText);

        // Save valid requested providers
        Set<NewsProvider> providers = extractProvidersFromRequest(commandLineArgs);

        // If no valid options were provided - prepare help message and exit
        if (providers.isEmpty()) {
            return prepareNoValidOptionsResponse(userName, commandType, user);
        }

        // Initiate new String builders for response and help messages
        StringBuilder response = new StringBuilder();
        StringBuilder helpMessage = new StringBuilder();

        // If some options were not recognised, save a help message to class variable
        checkAllOptions(commandLineArgs, helpMessage);

        // Add/remove requested subscriptions
        boolean isSubscribeCommand = commandType.equals(CommandType.SUBSCRIBE);
        for (NewsProvider provider : providers) {
            if (isSubscribeCommand) {
                response.append(subscribeUserToProvider(user, provider));
            } else {
                response.append(unsubscribeUserFromProvider(user, provider));
            }
        }

        // If there were some unrecognised options received - add list of currently available providers
        if (!helpMessage.isEmpty()) {
            helpMessage.append(
                    isSubscribeCommand ? loadAllAvailableProviders() : loadAvailableUserOptions(user)
            );
        }

        // Join response and help messages and then exit
        return response + "\n" + helpMessage;
    }

    /**
     * Subscribes the user to a news provider and generates a response message.
     *
     * @param user     the user object that represents the user who wants to subscribe
     * @param provider the news provider object that represents the provider to subscribe to
     * @return a string that indicates the result of the subscription
     */
    private String subscribeUserToProvider(
            User user,
            NewsProvider provider
    ) {
        // Check if such subscription for the user already exists
        Subscription userSubscription = user.getEqualSubscription(new Subscription(user, provider));
        if (userSubscription != null) {
            // If found - check if it's currently active
            if (userSubscription.isActive()) {
                // Add response that this subscription is already active
                return provider + " is already subscribed to\n";
            } else {
                // Reactivate old subscription, save to repository and add corresponding response
                userSubscription.setActive(true);
                subscriptionRepository.save(userSubscription);
                return provider + " is reactivated\n";
            }
        } else {
            // If subscription is unique - assign it for the user, save, and add corresponding response
            user.addSubscription(new Subscription(user, provider));
            userRepository.save(user);
            return provider + " is added to your subscriptions\n";
        }
    }

    /**
     * Unsubscribes the user from a news provider and generates a response message.
     *
     * @param user     the user object that represents the user who wants to unsubscribe
     * @param provider the news provider object that represents the provider to unsubscribe from
     * @return a string that indicates the result of the unsubscription
     */
    private String unsubscribeUserFromProvider(
            User user,
            NewsProvider provider
    ) {
        // Check if such subscription for the user exists and is active
        Subscription userSubscription = user.getEqualActiveSubscription(new Subscription(user, provider));
        if (userSubscription != null) {
            // If found - deactivate it, save, and add response
            userSubscription.setActive(false);
            subscriptionRepository.save(userSubscription);
            return provider + " is deactivated\n";
        } else {
            // Add response that this provider is not yet subscribed to
            return provider + " is not yet subscribed to\n";
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
                    .filter(arg -> !arg.startsWith("/") && isValidOption(arg))
                    .map(NewsProvider::valueOf)
                    .collect(Collectors.toCollection(
                            LinkedHashSet::new
                    ));
        }
    }

    /**
     * Checks if the given option is a valid news provider by trying to convert it to an enum value.
     *
     * @param option the string that represents the option
     * @return true if the option is a valid news provider, false otherwise
     */
    private boolean isValidOption(String option) {
        try {
            NewsProvider.valueOf(option);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    /**
     * Checks if all the options in the command line arguments are valid and recognised.
     * If not, it generates a help message with the unrecognised options and appends
     * it to the helpMessage.
     *
     * @param commandLineArgs the array of strings that represent the command options
     * @param helpMessage     the string builder that contains the help message to be modified
     */
    private void checkAllOptions(String[] commandLineArgs, StringBuilder helpMessage) {
        // If special option "ALL" received - exit
        if (Arrays.asList(commandLineArgs).contains("ALL")) {
            return;
        }

        // Collect unrecognised options
        String unrecognisedOptions = Arrays.stream(commandLineArgs)
                .filter(arg -> !arg.startsWith("/") && !isValidOption(arg))
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
     * Prepares a help message with the username, the command type, and the available options for the user.
     *
     * @param userName    the name of the user who issued the command
     * @param commandType the type of the command issued by the user
     * @param user        the user object that represents the user who wants to subscribe or unsubscribe
     * @return a string that contains the help message and the available options
     */
    private String prepareNoValidOptionsResponse(String userName, CommandType commandType, User user) {
        // Draft reply
        String availableOptions;
        boolean isSubscribeCommand = commandType.equals(CommandType.SUBSCRIBE);

        // Generate available options based on the type of command initiated
        if (isSubscribeCommand) {
            availableOptions = loadAllAvailableProviders();
        } else {
            availableOptions = loadAvailableUserOptions(user);
        }

        return noValidOptionsHelpMessage(
                userName,
                commandType.toString().toLowerCase()
        ) + availableOptions;
    }

    /**
     * Generates a help message with the unrecognised options in the command.
     *
     * @param unrecognisedOptions the string that contains the unrecognised options separated by commas
     * @return the help message as a string, with the supported options listed
     */
    private String unrecognisedOptionsHelpMessage(String unrecognisedOptions) {
        return "Some options were not recognised: " + unrecognisedOptions + "\n";
    }

    /**
     * Generates a help message with the username, the command name, and the usage of the command.
     *
     * @param userName    the name of the user who issued the command
     * @param commandName the name of the command issued by the user
     * @return a string that contains the help message and the usage of the command
     */
    private String noValidOptionsHelpMessage(String userName, String commandName) {
        return "Unfortunately, " + userName + ", no valid options were provided.\n" +
                "Usage: /" + commandName + " [provider]\n";
    }

    /**
     * Returns a string with the supported options for the news providers.
     *
     * @return a help message that contains the names of all supported news providers
     * separated by commas, and followed by ", or ALL."
     */
    private String loadAllAvailableProviders() {
        return "Supported options are: " +
                Arrays.stream(NewsProvider.values())
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")) + ", or ALL.";
    }

    /**
     * Returns a String with the available options based on users active subscriptions.
     * If the user has one or more active subscriptions, returns a comma-separated list
     * of the providers' names, followed by a special option 'ALL'.
     *
     * @param user the user whose options are to be loaded
     * @return a String with the available user options, or an empty String if none
     */
    private String loadAvailableUserOptions(User user) {
        // Generate String with all active user subscriptions
        String availableOptions = user.getAllActiveSubscriptions().stream()
                .map(Subscription::getProvider)
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        // Check if there are any active subscriptions present
        if (availableOptions.isEmpty()) {
            // If there are no active subscriptions - exit without a message
            return "";
        } else {
            // If active subscriptions present - generate a list with a special option 'ALL'
            return "Available options are: " + availableOptions + ", or ALL.";
        }
    }

    /**
     * Extracts an array of command line arguments from a message string.
     *
     * @param message the message string that contains the command and the options
     * @return an array of strings that represent the command and the options in upper case
     */
    private String[] extractCommandLineArgs(String message) {
        // Split String at any of the whitespace characters
        return message.toUpperCase().split("\\s+");
    }

}
