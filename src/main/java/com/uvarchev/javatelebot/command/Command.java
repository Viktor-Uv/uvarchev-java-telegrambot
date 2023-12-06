package com.uvarchev.javatelebot.command;

import com.uvarchev.javatelebot.enums.CommandType;

public interface Command {
    CommandType getType();
}
