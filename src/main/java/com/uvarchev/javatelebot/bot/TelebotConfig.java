package com.uvarchev.javatelebot.bot;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class TelebotConfig {
    @Value("${TELEBOT_TOKEN}")
    private String botToken;

    @Value("${TELEBOT_NAME}")
    private String botUsername;
}
