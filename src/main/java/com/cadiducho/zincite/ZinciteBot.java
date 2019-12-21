package com.cadiducho.zincite;

import com.cadiducho.zincite.api.command.CommandManager;
import com.cadiducho.zincite.api.module.ZinciteModule;
import com.cadiducho.zincite.api.module.ModuleManager;
import com.cadiducho.telegrambotapi.TelegramBot;
import com.cadiducho.telegrambotapi.handlers.ExceptionHandler;
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
     * The owner Telegram ID, if is set
     */
    @Getter private Long ownerId;

    /**
     * The bot version, for log purposes mainly
     */
    @Getter private String version;

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
     * @param ownerId Telegram owner's user id
     * @param version Version of the bot
     */
    public ZinciteBot(String token, Long ownerId, String version) {
        instance = this;
        if (token == null) {
            System.err.println("Token cannot be null");
        }
        if (version == null) {
            System.err.println("Version cannot be null");
        }

        this.moduleManager = new ModuleManager(new File("modules"));
        this.commandManager = new CommandManager();

        this.telegramBot = new TelegramBot(token);
        this.ownerId = ownerId;
        this.version = version;
    }

    /**
     * Startup Zincite server.
     * This includes load modules and start polling Telegram Bot API
     */
    public void startServer() {
        log.info("Servidor arrancado");

        try {
            moduleManager.loadModules();
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            log.warning("Can't load modules!");
            log.warning(ex.getMessage());
        }

        UpdatesHandler events = new UpdatesHandler(telegramBot, instance);
        telegramBot.getUpdatesPoller().setHandler(events);

        if (exceptionHandler == null) {
            this.exceptionHandler = new DefaultZinciteExceptionHandler(telegramBot, ownerId);
        }
        telegramBot.getUpdatesPoller().setExceptionHandler(exceptionHandler);

        telegramBot.startUpdatesPoller();

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
