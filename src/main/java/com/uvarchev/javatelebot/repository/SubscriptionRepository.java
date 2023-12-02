package com.uvarchev.javatelebot.repository;

import com.uvarchev.javatelebot.entity.Subscription;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {
}
