package com.uvarchev.javatelebot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uvarchev.javatelebot.enums.ServiceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    private ServiceType service; // subscription service

    @OneToMany(
            mappedBy = "subscription", // marks Subscription as main in subscription-parameter relationship
            orphanRemoval = true, // if subscription has been removed - remove its parameters
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            } // update subscription's parameters
    )
    private Set<Parameter> parameters = new HashSet<>(); // subscription options

    @Column(name = "sub_is_active", columnDefinition = "TINYINT(1)", nullable = false)
    private boolean isActive;

    @Column(name = "last_read_id", nullable = false)
    private String lastReadId; // most recent update that bot sent to subscriber

    public Subscription(
            User user,
            ServiceType service
    ) {
        this.isActive = true;
        // Save current time in ISO-8601 format (ex. 2011-12-03T10:15:30+01:00):
        this.lastReadId = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        this.user = user;
        this.service = service;
    }

    public Subscription(
            User user,
            ServiceType service,
            Set<Parameter> parameters
    ) {
        this(user, service);
        this.parameters = parameters;
    }

    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public String toString() {
        return this.getId() + ", " +
                this.getService() + ", " +
                this.getParameters();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return service == that.service
                && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service);
    }
}
