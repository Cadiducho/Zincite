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
     * @param token Telegram bot token
     */
    public ZinciteBot(String token) {
        this(token, null, "1.0");
    }

    /**
     * Create a ZinciteBot
     * @param token Telegram bot token
     * @param ownerId Telegram ID of the owner
     */
    public ZinciteBot(String token, Long ownerId) {
        this(token, ownerId, "1.0");
    }

    /**
     * Create a ZinciteBot
     * @param token Telegram bot token
     * @param ownerId Telegram owner's user id
     * @param version Version of the bot
     */
    public ZinciteBot(String token, Long ownerId, String version) {
        this(token, ownerId, version, "logs", "modules");
    }

    /**
     * Create a ZinciteBot
     * @param token Telegram bot token
     * @param ownerId Telegram owner's user id
     * @param version Version of the bot
     * @param logsPath Path where logs are stored
     * @param modulesPath Path where modules are stored
     */
    public ZinciteBot(String token, Long ownerId, String version, String logsPath, String modulesPath) {
        instance = this;

        this.token = token;
        this.ownerId = ownerId;
        this.version = version;

        this.consoleManager = new ConsoleManager(instance);
        this.consoleManager.startFile(logsPath + "/log-%D.txt");
        if (token == null) {
            log.warning("Token cannot be null");
        }
        if (version == null) {
            log.warning("Version cannot be null");
        }

        this.moduleManager = new ModuleManager(new File(modulesPath));
        this.commandManager = new CommandManager(instance);

        this.telegramBot = new TelegramBot(token);
    }

    /**
     * Startup Zincite server.
     * This includes load modules and start polling Telegram Bot API
     */
    public void startServer() {
        consoleManager.startConsole();
        log.info("Servidor arrancado");

        try {
            moduleManager.loadModules();
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
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
