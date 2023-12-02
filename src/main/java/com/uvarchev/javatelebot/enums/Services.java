package com.uvarchev.javatelebot.enums;

public enum Services {
    NEWS("category"),
    WEATHER("location");

    private final String parameter;

    Services(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}
