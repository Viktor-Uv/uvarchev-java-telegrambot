package com.uvarchev.javatelebot.controller;

import com.uvarchev.javatelebot.repository.ParameterRepository;
import com.uvarchev.javatelebot.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParameterController {
    @Autowired
    private ParameterRepository parameterRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

}
