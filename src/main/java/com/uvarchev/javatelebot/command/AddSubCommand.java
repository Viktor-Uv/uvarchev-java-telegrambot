package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.enums.CommandType;
import com.uvarchev.javatelebot.enums.ServiceType;

public class AddSubCommand implements Command {

    private final Long userId;
    private final ServiceType serviceType;
    private String option;

    public AddSubCommand(Long userId, ServiceType serviceType) {
        this.userId = userId;
        this.serviceType = serviceType;
    }

    public AddSubCommand(Long userId, ServiceType serviceType, String option) {
        this(userId, serviceType);
        this.option = option;
    }

    public Long getUserId() {
        return userId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getOption() {
        return option;
    }

    @Override
    public CommandType getType() {
        return CommandType.ADDSUB;
    }

    public static ServiceType validateOption(String option) {
        try {
            return ServiceType.valueOf(option);
        } catch (Exception ignored) {
            return null;
        }
    }
}
