package com.cadiducho.zincite.api.command.args;

@FunctionalInterface
public interface ArgumentType<T> {

    T parse(String str) throws CommandParseException;
}
