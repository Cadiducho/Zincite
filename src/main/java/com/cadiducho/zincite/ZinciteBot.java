package com.cadiducho.zincite;

import com.cadiducho.telegrambotapi.TelegramBot;
import com.cadiducho.telegrambotapi.handlers.ExceptionHandler;
import com.cadiducho.zincite.api.command.CommandManager;
import com.cadiducho.zincite.api.module.ModuleManager;
import com.cadiducho.zincite.api.module.ZinciteModule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;

@Log
public class ZinciteBot {

    /**
     * The Module manager
     */
    @Getter private final ModuleManager moduleManager;

    /**
     * The (Telegram) Command manager
     */
    @Getter private final CommandManager commandManager;

    /**
     * The Console manager
     */
    private final ConsoleManager consoleManager;

    /**
     * The Zincite configuration
     */
    @Getter private final ZinciteConfig config;

    /**
     * The telegram token
     */
    @Getter private final String token;

    /**
     * The owner Telegram ID, if is set
     */
    @Getter private final Long ownerId;

    /**
     * The bot version, for log purposes mainly
     */
    @Getter private final String version;

    /**
     * Instance to handle exceptions on fetching Telegram API
     */
    @Setter private ExceptionHandler exceptionHandler;

    @Getter private TelegramBot telegramBot;
    @Getter private static ZinciteBot instance;

    /**
     * Create a ZinciteBot
     * @param config The Zincite configuration
     */
    public ZinciteBot(ZinciteConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        if (config.token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        this.config = config;
        instance = this;

        this.token = config.token;
        this.ownerId = config.ownerId;
        this.version = config.version;

        this.consoleManager = new ConsoleManager(instance, config.enableFileLog);
        this.consoleManager.startFile(config.logsPath + "/log-%D.txt");

        this.moduleManager = new ModuleManager(new File(config.modulesPath));
        this.commandManager = new CommandManager(instance);

        this.telegramBot = new TelegramBot(token);
    }

    /**
     * Startup Zincite server.
     * This includes load modules and start polling Telegram Bot API
     */
    public void startServer() {
        consoleManager.startConsole(config.enableConsoleReader, config.enableFileLog);
        log.info("Servidor arrancado");

        try {
            moduleManager.loadModules();
        } catch (Exception ex) {
            log.warning("Can't load modules!");
            log.warning(ex.getMessage());
        }

        UpdatesHandler events = new UpdatesHandler(telegramBot, instance);
        telegramBot.getUpdatesPoller().setHandler(events);
        telegramBot.getUpdatesPoller().setExceptionHandler(exceptionHandler);

        telegramBot.startUpdatesPoller();
        commandManager.registerCommandsToTelegramHelp();

        log.info("Zincite bot v" + this.version + " iniciado completamente");
    }

    /**
     * Shutdown Zincite server.
     * All modules will be unloaded and Zincite will stop fetching Telegram Bot API
     */
    public void shutdown() {
        telegramBot.stopUpdatesPoller();
        moduleManager.getModules().forEach(ZinciteModule::onClose);

        log.info("Closing Zincite bot...");
        System.exit(0);
    }
}
