package com.uvarchev.javatelebot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parameters")
@Getter
@Setter
@NoArgsConstructor
public class Parameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "subscription_id", nullable = false) // linking Subscription and Parameter
    @JsonIgnore // to avoid unintended recursion
    private Subscription subscription;

    @Column(name = "param_name")
    private String name;
    @Column(name = "param_value")
    private String value;

    public Parameter(
            Subscription subscription,
            String name,
            String value

    ) {
        this.subscription = subscription;
        this.name = name;
        this.value = value;
    }
}
