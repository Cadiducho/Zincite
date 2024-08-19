package com.cadiducho.zincite;

import lombok.Builder;

/**
 * Zincite configuration class
 */
@Builder
public class ZinciteConfig {
    /**
     * The Telegram bot token
     */
    public String token;

    /**
     * The owner Telegram ID, if is set. Used to send exceptions to owner
     */
    public Long ownerId;

    /**
     * The bot version
     */
    @Builder.Default public String version = "1.0";

    /**
     * The path to the logs folder
     */
    @Builder.Default public String logsPath = "logs";

    /**
     * The path to the modules folder
     */
    @Builder.Default public String modulesPath = "modules";

    /**
     * Enable if you want to use the console reader. Console will capture input from System.in
     */
    @Builder.Default public boolean enableConsoleReader = true;

    /**
     * Enable if you want to log to a file
     */
    @Builder.Default public boolean enableFileLog = false;
}
