package com.uvarchev.javatelebot.enums;

public enum ServiceType {
    NEWS("category"),
    WEATHER("location");

    private final String parameter;

    ServiceType(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}
