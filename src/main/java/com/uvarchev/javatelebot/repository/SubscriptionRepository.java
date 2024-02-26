package com.uvarchev.javatelebot.repository;

import com.uvarchev.javatelebot.entity.Subscription;
import com.uvarchev.javatelebot.enums.NewsProvider;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {

    @Transactional
    @Modifying
    @Query(
            value = "UPDATE Subscription s " +
                    " SET s.isActive = false " +
                    " WHERE s.user.telegramId = :userId"
    )
    int deactivateAllUserSubscriptionsByUserId(Long userId);

    /**
     * Counts subscriptions by active status.
     *
     * @param activeStatus the active status to filter by
     * @return the number of users with the given active status
     */
    @Query(
            value = "SELECT count(s) " +
                    " FROM Subscription s " +
                    " WHERE s.isActive = :activeStatus "
    )
    int countAllByActiveIs(boolean activeStatus);

    @Query(
            value = "SELECT s " +
                    " FROM Subscription s " +
                    " INNER JOIN User u " +
                    " ON s.user.telegramId = u.telegramId " +
                    " AND u.isActive = true " +
                    " WHERE s.isActive = true"
    )
    List<Subscription> findAllActiveSubscriptions();

    /**
     * Returns a list of news providers that have the most active subscriptions in the database.
     * The query selects the providers that have the same number of active subscriptions as
     * the maximum count of any provider in the table.
     *
     * @return a list of news providers with the most active subscriptions
     */
    @Query( // Get providers with the maximum number of occurrences
            value = "SELECT s.provider " +
                    " FROM Subscription s " +
                    " WHERE s.isActive " +
                    " GROUP BY s.provider " +
                    " HAVING count(s.provider) = ( " +
                    // Get the maximum number of occurrences
                    "     SELECT max(occurrences.count) " +
                    "     FROM ( " +
                    // Get the number of occurrences for each provider actively subscribed to
                    "         SELECT count(s.provider) AS count " +
                    "         FROM Subscription s " +
                    "         WHERE s.isActive " +
                    "         GROUP BY s.provider " +
                    "         ) AS occurrences " +
                    "     )"
    )
    List<NewsProvider> findDistinctTopProviders();

    @Query(
            value = "SELECT max(s.lastReadId) " +
                    " FROM Subscription s "
    )
    ZonedDateTime getMostRecentReadTime();

}
