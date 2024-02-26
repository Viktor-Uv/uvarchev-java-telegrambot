package com.uvarchev.javatelebot.periodic;

import com.uvarchev.javatelebot.bot.Telebot;
import com.uvarchev.javatelebot.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Scheduler {

    @Autowired
    private Telebot telebot;
    @Autowired
    private SchedulerService schedulerService;



}
