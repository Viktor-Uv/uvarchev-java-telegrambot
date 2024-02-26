package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.bot.command.StartCommand;
import com.uvarchev.javatelebot.bot.command.StatisticsCommand;
import com.uvarchev.javatelebot.bot.command.StopCommand;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.NewsProvider;
import com.uvarchev.javatelebot.enums.UserRole;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    /**
     * Activates a user by checking if they already exist in the database or creating a new one if not.
     * The user role is set to USER for returning UNAUTHORISED users.
     * A relevant greeting message is returned.
     *
     * @param command a StartCommand object that contains the user id and username
     * @param user    a User object that represents the user to activate or create
     * @return a reply String that greets the user and indicates whether they are new or returning
     */
    public String activateUser(StartCommand command, User user) {
        // Create reply draft
        String reply = "Hi, " + command.getUserName();

        // Generate the rest of reply based on whether the user was found or not
        if (user != null) {
            // If found - set UserRole to USER if it was UNAUTHORISED and complete the reply
            if (user.getUserRole().equals(UserRole.UNAUTHORISED)) {
                user.setUserRole(UserRole.USER);
            }
            reply += ", nice to see you again!";
        } else {
            // Otherwise - create a new user and complete the reply
            user = new User(command.getUserId());
            reply += ", nice to meet you!";
        }

        // Save user and return reply
        userRepository.save(user);
        return reply;
    }

    /**
     * Deactivates the user and all their subscriptions by the given command.
     *
     * @param command a stop command that contains the user id and name
     * @return a string containing a farewell message to the user
     */
    public String deactivateUser(StopCommand command) {
        // Lower the UserRole to UNAUTHORISED
        userRepository.deactivateUserByUserId(command.getUserId());

        // Deactivate all subscriptions of the leaving user
        subscriptionRepository.deactivateAllUserSubscriptionsByUserId(command.getUserId());

        // Return composed reply
        return "Updates are stopped. Bye, " + command.getUserName() + ", till next time!";
    }

    /**
     * Returns a string containing a statistic report of the users and subscriptions.
     *
     * @return a string containing the statistic report
     */
    public String getAdminStatistics() {
        // Count total registered users
        long totalUsersCount = countTotalUsers();

        // Count total active users
        int activeUsersCount = countActiveUsers();

        // Count total active subscriptions
        int activeSubscriptionsCount = countActiveSubscriptions();

        // Number of articles sent to all users
        long articlesSentCount = countArticlesSent();

        // Top news provider
        String topProviders = getTopProviders();

        // Most recent update time
        String lastUpdateTime = getLastUpdateTime();

        // Compose reply and exit
        return composeReply(
                totalUsersCount,
                activeUsersCount,
                activeSubscriptionsCount,
                articlesSentCount,
                topProviders,
                lastUpdateTime
        );
    }

    /**
     * Returns the number of rows in the user table.
     *
     * @return the total number of users stored in the database
     */
    private long countTotalUsers() {
        return userRepository.count();
    }

    /**
     * Returns the number of non-UNAUTHORISED users stored in the database.
     *
     * @return the number of active users in the database
     */
    private int countActiveUsers() {
        return userRepository.countByUserRoleIsNot(UserRole.UNAUTHORISED);
    }

    /**
     * Counts the number of active subscriptions in the repository.
     *
     * @return the number of subscriptions with active status set to true
     */
    private int countActiveSubscriptions() {
        return subscriptionRepository.countAllByActiveIs(true);
    }

    /**
     * Returns the total number of articles sent to all users.
     *
     * @return a long value representing the count of articles sent
     */
    private long countArticlesSent() {
        return userRepository.getTotalOfArticlesReceived();
    }

    /**
     * Returns a comma-separated list with the names of the top news providers in the repository.
     *
     * @return a string containing the names of the top providers,
     * or "No active subscriptions" if none are found
     */
    private String getTopProviders() {
        String topProviders = subscriptionRepository.findDistinctTopProviders().stream()
                .map(NewsProvider::name)
                .collect(Collectors.joining(", "));

        // Check if any providers were received
        if (!topProviders.isEmpty()) {
            return topProviders;
        } else {
            return "No active subscriptions";
        }
    }

    /**
     * Returns the last update time of the subscriptions in UTC format.
     *
     * @return a String value representing the date and time of the last update
     */
    private String getLastUpdateTime() {
        return subscriptionRepository.getMostRecentReadTime()
                .format(
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm z")
                                .withZone(ZoneId.of("UTC"))
                );
    }

    /**
     * Returns a string containing a statistic report of the users and subscriptions.
     *
     * @param countTotalUsers          the total number of registered users
     * @param activeUsersCount         the number of users who are currently active
     * @param activeSubscriptionsCount the number of subscriptions that are currently active
     * @param articlesSentCount        the total number of articles sent by all users
     * @param topProviders             the name of the news providers that has the most subscriptions
     * @param lastUpdateTime           the date and time of the most recent update sent
     * @return a string containing the statistic report
     */
    private String composeReply(
            long countTotalUsers,
            int activeUsersCount,
            int activeSubscriptionsCount,
            long articlesSentCount,
            String topProviders,
            String lastUpdateTime
    ) {
        return "-- Statistic report --" + "\n" +
                "Total number of registered users: " + countTotalUsers + "\n" +
                "Total number of active users: " + activeUsersCount + "\n" +
                "Total number of active subscriptions: " + activeSubscriptionsCount + "\n" +
                "Total number of articles sent: " + articlesSentCount + "\n" +
                "Top News Provider(s): " + topProviders + "\n" +
                "Most recent update: " + lastUpdateTime;
    }

}
