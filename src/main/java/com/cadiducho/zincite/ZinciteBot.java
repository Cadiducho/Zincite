package com.cadiducho.zincite;

import com.cadiducho.zincite.api.command.CommandManager;
import com.cadiducho.zincite.api.module.ZinciteModule;
import com.cadiducho.zincite.api.module.ModuleManager;
import com.cadiducho.telegrambotapi.TelegramBot;
import com.cadiducho.telegrambotapi.handlers.ExceptionHandler;
import lombok.Getter;
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

    @Getter private TelegramBot telegramBot;
    @Getter private static ZinciteBot instance;
    
    public ZinciteBot(String token) {
        this(token, null, "1.0");
    }

    public ZinciteBot(String token, Long ownerId, String version) {
        instance = this;
        this.moduleManager = new ModuleManager(instance, new File("modules"));
        this.commandManager = new CommandManager();

        this.telegramBot = new TelegramBot(token);
        this.ownerId = ownerId;
        this.version = version;
    }
    
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

        ExceptionHandler exceptionHandler = new TelegramExceptionHandler(telegramBot, ownerId);
        telegramBot.getUpdatesPoller().setExceptionHandler(exceptionHandler);

        telegramBot.startUpdatesPoller();

        log.info("Bot iniciado completamente");
    }
    
    public void shutdown() {
        telegramBot.stopUpdatesPoller();
        moduleManager.getModules().forEach(ZinciteModule::onClose);


        log.info("Terminando...");
        System.exit(0);
    }
}
