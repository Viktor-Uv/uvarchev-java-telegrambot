package com.uvarchev.javatelebot.entity;

import com.uvarchev.javatelebot.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

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
    private boolean isActive; // Defined by "/start" and "/stop" commands

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", columnDefinition = "VARCHAR(255) DEFAULT 'GUEST'")
    private UserRole userRole;

    @OneToMany(
            mappedBy = "user", // marks User as main in user-subscription relationship
            orphanRemoval = true, // if user has been removed - remove its subscriptions
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
}
