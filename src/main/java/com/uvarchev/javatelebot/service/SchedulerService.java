package com.uvarchev.javatelebot.service;

import com.uvarchev.javatelebot.dto.News;
import com.uvarchev.javatelebot.dto.Reply;
import com.uvarchev.javatelebot.entity.Subscription;
import com.uvarchev.javatelebot.entity.User;
import com.uvarchev.javatelebot.enums.NewsProvider;
import com.uvarchev.javatelebot.enums.UserRole;
import com.uvarchev.javatelebot.network.ApiClient;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A service class that handles the scheduling and distribution of news updates to subscribers.
 */
@Service
@Transactional
public class SchedulerService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    /**
     * Fetches the scheduled news updates for all active subscriptions and returns them as a queue of replies.
     *
     * @return a queue of replies containing the news updates, or null if there are no active subscriptions
     */
    public Queue<Reply> fetchScheduledNewsUpdate() {
        // Collect list of all active subscriptions
        List<Subscription> subscriptions = subscriptionRepository.findAllActiveSubscriptions();

        // Check if the list of subscriptions is empty
        if (subscriptions.isEmpty()) {
            // Return null to indicate that the list of subscriptions is empty
            return null;
        }

        // Save current time
        final ZonedDateTime currentTime = ZonedDateTime.now();

        // Get the latest article delivered time among active subscriptions or current time
        ZonedDateTime oldestRead = Optional.ofNullable(
                        subscriptionRepository.getOldestReadTimeFromActiveSubscriptions()
                )
                .orElse(currentTime);

        // From subscriptions collect all distinct news providers separated by commas
        String newsProviders = getDistinctNewsProvidersFromActiveSubscriptions(subscriptions);

        // Create an API client
        ApiClient client = new ApiClient(
                newsProviders,
                oldestRead.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"))
        );

        // Download news in reverse date order
        Stack<News> newsList = client.getNews();

        // Create a Queue of Reply objects for News distribution
        Queue<Reply> replies = new LinkedList<>();

        // Process each article in the received newsList
        while (!newsList.isEmpty()) {
            // Prepare article for the corresponding subscriber
            distributeArticles(newsList.pop(), subscriptions, replies);
        }

        return replies;
    }

    /**
     * A method that returns a queue of Reply objects with the daily statistics message for all admin users.
     *
     * @return a queue of Reply objects or an empty queue if no admin users are found
     */
    public Queue<Reply> getDailyStatistics() {
        // Get a list administrators
        List<User> admins = userRepository.getUsersByUserRole(UserRole.ADMIN);

        // Check if no administrators were found
        if (admins == null) {
            return new LinkedList<>();
        }

        // Get a statistics message
        String statisticsMessage = userService.getAdminStatistics();

        // Create a list of Replies from a list of admins and return
        return admins.stream()
                .map(admin -> new Reply(admin.getTelegramId(), statisticsMessage))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Gets the distinct news providers from a list of active subscriptions
     * and returns them as a comma-separated string.
     *
     * @param subscriptions a list of active subscriptions
     * @return a string of distinct news providers
     */
    private String getDistinctNewsProvidersFromActiveSubscriptions(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .map(Subscription::getProvider)
                .distinct()
                .map(NewsProvider::getApiName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Distributes an article to the relevant subscriptions and adds the replies to a queue.
     *
     * @param article       a news article
     * @param subscriptions a list of active subscriptions
     * @param replies       a queue of replies
     */
    private void distributeArticles(News article, List<Subscription> subscriptions, Queue<Reply> replies) {
        // Filter article through each active subscription and offer Reply to the replies
        subscriptions.stream()
                .filter(subscription ->
                        matchesProvider(subscription, article) &&
                                isPublishedAfterRead(subscription, article)
                )
                .forEach(subscription -> createAndOfferReply(subscription, article, replies));
    }

    /**
     * Checks if a subscription matches the provider of an article.
     *
     * @param subscription a subscription
     * @param article      a news article
     * @return true if the subscription and the article have the same provider, false otherwise
     */
    private boolean matchesProvider(Subscription subscription, News article) {
        // Providers for both article and subscription are matching
        return subscription.getProvider().getApiName()
                .equals(article.getProvider());
    }

    /**
     * Checks if an article is published after the last read time of a subscription.
     *
     * @param subscription a subscription
     * @param article      a news article
     * @return true if the article is published after the last read time of the subscription, false otherwise
     */
    private boolean isPublishedAfterRead(Subscription subscription, News article) {
        // Articles published time is after subscription's last read time
        ZonedDateTime publishedAt = ZonedDateTime.parse(article.getPublishedAt());
        ZonedDateTime lastRead = subscription.getLastReadId();
        return publishedAt.isAfter(lastRead);
    }

    /**
     * Creates a reply object based on a subscription and an article and adds it to a queue of replies.
     *
     * @param subscription a subscription
     * @param article      a news article
     * @param replies      a queue of replies
     */
    private void createAndOfferReply(Subscription subscription, News article, Queue<Reply> replies) {
        // Create Reply abject based on Subscription and add to the queue of replies
        replies.offer(new Reply(
                subscription.getUser().getTelegramId(),
                article.toString(),
                subscription.getId()
        ));
    }

    /**
     * Updates the last read time for each subscription stored in a list
     * and saves the updated subscriptions to the database.
     *
     * @param ids         The list of subscription ids to be updated.
     * @param currentTime The current time of the scheduler.
     */
    public void updateSubscriptionListLastReadTime(
            Set<Long> ids,
            ZonedDateTime currentTime
    ) {
        // Get a list of subscriptions from db
        Iterable<Subscription> subscriptionList = subscriptionRepository.findAllById(ids);

        // For each subscription in a subscription list
        for (Subscription subscription : subscriptionList) {
            // Update LastReadId
            subscription.setLastReadId(currentTime);
        }

        // Save updated subscription list to db
        subscriptionRepository.saveAll(subscriptionList);
    }

    /**
     * A method that increments the number of articles received by each user in a map
     * and saves the updated users to the database.
     *
     * @param articlesReceived a map that stores the user ids and the number of articles received
     */
    public void incrementReplyCount(Map<Long, Long> articlesReceived) {
        // Get a list of user ids from Map
        List<Long> ids = new LinkedList<>(articlesReceived.keySet());

        // Get a list of users from db
        Iterable<User> userList = userRepository.findAllById(ids);

        // For each user in a user list
        for (User user : userList) {
            // Increment articles received
            user.incrementArticlesReceivedByValue(
                    articlesReceived.get(user.getTelegramId())
            );
        }

        // Save updated user list to db
        userRepository.saveAll(userList);
    }

}
