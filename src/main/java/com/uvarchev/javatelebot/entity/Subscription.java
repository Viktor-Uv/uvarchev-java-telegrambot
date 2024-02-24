package com.uvarchev.javatelebot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uvarchev.javatelebot.enums.NewsProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // linking User and Subscription
    @JsonIgnore // to avoid unintended recursion
    private User user;

    @Enumerated(EnumType.STRING) // store enum as a String in the database
    @Column(name = "subs_service")
    private NewsProvider provider; // news service provider

    @Column(name = "is_active", columnDefinition = "TINYINT(1)", nullable = false)
    private boolean isActive;

    @Column(name = "last_read_id", nullable = false)
    private ZonedDateTime lastReadId; // most recent update that bot sent to subscriber

    public Subscription(
            User user,
            NewsProvider provider
    ) {
        this.isActive = true;
        // Save current time in ISO-8601 format (ex. 2011-12-03T10:15:30Z):
        this.lastReadId = ZonedDateTime.now();
        this.user = user;
        this.provider = provider;
    }

    public String toString() {
        return this.getId() + ", " +
                this.getProvider();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(user, that.user) && provider == that.provider;
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, provider);
    }
}
