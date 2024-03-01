package com.uvarchev.javatelebot.entity;

import com.uvarchev.javatelebot.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private Long telegramId; // Use Telegram ID as user's primary identifier

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", columnDefinition = "VARCHAR(255) DEFAULT 'UNAUTHORISED'")
    private UserRole userRole;

    @OneToMany(
            mappedBy = "user", // marks User as main in user-subscription relationship
            orphanRemoval = true, // if a user has been removed - remove its subscriptions
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            } // update user's subscriptions
    )
    private Set<Subscription> subscriptions = new HashSet<>(); // user's subscriptions

    @Column(name = "articles_received")
    private long articlesReceived;

    public User(Long telegramId) {
        this.telegramId = telegramId;
        this.userRole = UserRole.USER;
    }

    /**
     * Adds a subscription object to the subscription list.
     *
     * @param subscription The subscription object to be added.
     */
    public void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
    }

    /**
     * Returns the first subscription object from the subscription list that has the same
     * userId and provider as the given subscription object, or null if none is found.
     *
     * @param subscription The subscription object to be compared with the subscriptions list.
     * @return The equal subscription object from the list, or null if not found.
     */
    public Subscription getEqualSubscription(Subscription subscription) {
        return getSubscriptions().stream()
                .filter(s -> s.equals(subscription))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the first subscription object from the subscription list that has the same
     * userId and provider as the given subscription object and is active, or null if none is found.
     *
     * @param subscription The subscription object to be compared with the subscriptions list.
     * @return The equal active subscription object from the list, or null if not found.
     */
    public Subscription getEqualActiveSubscription(Subscription subscription) {
        return getSubscriptions().stream()
                .filter(s -> s.equals(subscription))
                .filter(Subscription::isActive)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a set of all active subscriptions from the subscription list.
     *
     * @return The set of active subscriptions.
     */
    public Set<Subscription> getAllActiveSubscriptions() {
        return getSubscriptions().stream()
                .filter(Subscription::isActive)
                .collect(Collectors.toSet());
    }

    /**
     * Increments the articlesReceived count by a given value.
     *
     * @param articlesReceived The value to be added to the articlesReceived.
     */
    public void incrementArticlesReceivedByValue(long articlesReceived) {
        this.articlesReceived += articlesReceived;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(telegramId, user.telegramId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(telegramId);
    }
}
