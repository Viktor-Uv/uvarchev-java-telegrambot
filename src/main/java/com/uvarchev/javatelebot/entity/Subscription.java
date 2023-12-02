package com.uvarchev.javatelebot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uvarchev.javatelebot.enums.Services;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Services service; // subscription service

    @OneToMany(
            mappedBy = "subscription", // marks Subscription as main in subscription-parameter relationship
            orphanRemoval = true, // if subscription has been removed - remove its parameters
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            } // update subscription's parameters
    )
    private Set<Parameter> parameters; // subscription options

    @Column(name = "sub_is_active", columnDefinition = "TINYINT(1) DEFAULT 1", nullable = false)
    private boolean isActive;

    public Subscription(
            User user,
            Services service,
            Set<Parameter> parameters
    ) {
        this.user = user;
        this.service = service;
        this.parameters = parameters;
    }
}
