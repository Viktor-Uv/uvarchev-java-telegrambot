package com.uvarchev.javatelebot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "user_is_active", columnDefinition = "TINYINT(1) DEFAULT 1", nullable = false)
    private boolean isActive; // Defined by "/start" and "/stop" commands

    @OneToMany(
            mappedBy = "user", // marks User as main in user-subscription relationship
            orphanRemoval = true, // if user has been removed - remove its subscriptions
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            } // update user's subscriptions
    )
    private Set<Subscription> subscriptions; // user's subscriptions

    public User(Long telegramId) {
        this.telegramId = telegramId;
    }
}
