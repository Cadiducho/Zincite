package com.cadiducho.zincite.api.command.args;

public class CommandParseException extends Exception {

    public CommandParseException(Throwable throwable) {
        super(throwable);
    }

    public CommandParseException(String str) {
        super(str);
    }
}
