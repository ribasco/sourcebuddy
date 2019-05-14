package com.ibasco.sourcebuddy.components.rcon.parsers;

import com.ibasco.sourcebuddy.domain.ManagedServer;

import java.util.function.BiFunction;

public interface RconResultParser<T> extends BiFunction<ManagedServer, String, T> {

}
