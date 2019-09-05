package com.cadiducho.zincite.api.command.args;

public class StringArgumentType implements ArgumentType<String> {

    @Override
    public String parse(String str) {
        return str;
    }
}
