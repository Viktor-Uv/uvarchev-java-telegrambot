package com.uvarchev.javatelebot.repository;

import com.uvarchev.javatelebot.entity.Subscription;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {

    @Query(
            value = "SELECT s FROM Subscription s WHERE s.user.telegramId = :userId AND s.isActive = true"
    )
    Iterable<Subscription> findByUserIdAndActive(Long userId);

    @Transactional
    @Modifying
    @Query(
            value = "UPDATE Subscription s SET s.isActive = false WHERE s.id = :id"
    )
    int deactivateById(Long id);
}
