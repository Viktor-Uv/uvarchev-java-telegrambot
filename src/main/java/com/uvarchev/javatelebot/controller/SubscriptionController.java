package com.uvarchev.javatelebot.controller;

import com.uvarchev.javatelebot.repository.ParameterRepository;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import com.uvarchev.javatelebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionController {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParameterRepository parameterRepository;

}
