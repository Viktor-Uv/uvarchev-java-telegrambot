package com.uvarchev.javatelebot.repository;

import com.uvarchev.javatelebot.entity.Subscription;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {

    @Query(
            value = "SELECT s " +
                    " FROM Subscription s " +
                    " WHERE s.user.telegramId = :userId " +
                    " AND s.isActive = true"
    )
    Iterable<Subscription> findByUserIdAndActiveIsTrue(Long userId);

    /**
     * Deactivates a user's subscription by setting its isActive field to false in the database.
     *
     * @param subId  the ID of the subscription to deactivate
     * @param userId the ID of the user who owns the subscription
     * @return the number of rows affected by the update query
     */
    @Transactional
    @Modifying
    @Query(
            value = "UPDATE Subscription s " +
                    " SET s.isActive = false " +
                    " WHERE s.id = :subId " +
                    " AND s.user.telegramId = :userId " +
                    " AND s.isActive = true"
    )
    int deactivateById(Long subId, Long userId);

    @Query(
            value = "SELECT s " +
                    " FROM Subscription s " +
                    " INNER JOIN User u " +
                    " ON s.user.telegramId = u.telegramId" +
                    " AND u.isActive = true" +
                    " WHERE s.isActive = true"
    )
    List<Subscription> findAllActiveSubscriptions();

}
