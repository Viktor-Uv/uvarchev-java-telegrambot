package com.uvarchev.javatelebot.enums;

// Valid commands
public enum CommandType {
    START,
    STOP,
    SUBSCRIBE,
    UNSUBSCRIBE,
    LIST,

    // Special option, it is used if none of the above commands were received
    UNRECOGNISED
}
