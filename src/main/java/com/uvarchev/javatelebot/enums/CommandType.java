package com.uvarchev.javatelebot.enums;

// Valid commands
public enum CommandType {
    // Unauthorised user's commands
    START(0),

    // User commands
    STOP(1),
    SUBSCRIBE(1),
    UNSUBSCRIBE(1),
    SUBSCRIPTIONS(1),

    // Administrator commands
    STATISTICS(2),

    // Technical options, used if none of the above commands were received
    UNRECOGNISED(9);

    private final int requiredAccessLevel;

    CommandType(int requiredAccessLevel) {
        this.requiredAccessLevel = requiredAccessLevel;
    }

    public int getRequiredAccessLevel() {
        return requiredAccessLevel;
    }

}
