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

    @Column(name = "user_is_active", columnDefinition = "TINYINT(1)", nullable = false)
    private boolean isActive; // TODO for removal

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", columnDefinition = "VARCHAR(255) DEFAULT 'GUEST'")
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

    public User(Long telegramId) {
        this.telegramId = telegramId;
        this.isActive = true;
        this.userRole = UserRole.USER;
    }

    public void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public Subscription getEqualSubscription(Subscription subscription) {
        return getSubscriptions().stream()
                .filter(s -> s.equals(subscription))
                .findFirst()
                .orElse(null);
    }

    public Subscription getEqualActiveSubscription(Subscription subscription) {
        return getSubscriptions().stream()
                .filter(s -> s.equals(subscription))
                .filter(Subscription::isActive)
                .findFirst()
                .orElse(null);
    }

    public Set<Subscription> getAllActiveSubscriptions() {
        return getSubscriptions().stream()
                .filter(Subscription::isActive)
                .collect(Collectors.toSet());
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
