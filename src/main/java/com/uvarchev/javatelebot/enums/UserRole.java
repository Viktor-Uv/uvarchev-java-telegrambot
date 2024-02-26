package com.uvarchev.javatelebot.enums;

public enum UserRole {
    UNAUTHORISED(0),
    USER(1),
    ADMIN(2);

    private final int accessLevel;

    UserRole(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public int getAccessLevel() {
        return accessLevel;
    }
}
