package com.cadiducho.zincite.api.command;

import com.cadiducho.telegrambotapi.*;
import com.cadiducho.telegrambotapi.exception.TelegramException;
import com.cadiducho.zincite.ZinciteBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;

/**
 * Class to handle and add all his commands
 *
 * @author Cadiducho
 */
@Log
@RequiredArgsConstructor
public class CommandManager {

    private final ZinciteBot zincite;
    private final Map<String, BotCommand> commandMap = new HashMap<>();
    private final Map<String, CallbackListenerMethodInstance> callbackListenersMap = new HashMap<>();

    /**
     * Registrar un comando y, si contiene, sus listener de CallbackQuery
     * @param cmd El comando a registrar
     */
    public void register(BotCommand cmd) {
        cmd.getAliases().forEach(alias -> commandMap.put(alias.toLowerCase(), cmd));

        //Comprobar si tiene Listeners en su interior, y registrarlos
        if (cmd instanceof CallbackListener) {
            registerCallbackQueryListener((CallbackListener) cmd);
        }
    }

    /**
     * Registrar un nuevo listener de CallbackQuery
     * @param listener El listener a registrar
     */
    public void registerCallbackQueryListener(CallbackListener listener) {
        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(ListenTo.class)) {
                ListenTo listenTo = method.getAnnotation(ListenTo.class);
                CallbackListenerMethodInstance instance = new CallbackListenerMethodInstance(listener, method); //necesitamos guardar el método y su instancia de clase para ejecutarla mediante reflection
                callbackListenersMap.put(listenTo.value(), instance);
            }
        }
    }

    public Optional<BotCommand> getCommand(String alias) {
        return Optional.ofNullable(commandMap.get(alias));
    }

    public Optional<CallbackListenerMethodInstance> getCallbackListener(String query) {
        return Optional.ofNullable(callbackListenersMap.get(query));
    }

    /**
     * Ejecutar un comando
     *
     * @param bot Bot que recibe la update
     * @param update Update del comando
     * @return Verdadero si se ha ejecutado, falso si no
     * @throws com.cadiducho.telegrambotapi.exception.TelegramException Excepcion
     */
    public boolean onCmd(TelegramBot bot, Update update) throws TelegramException {
        Instant now = Instant.now();
        Message message = update.getMessage();
        User from = update.getMessage().getFrom();

        log.info((from.getUsername() == null ? from.getFirstName() : ("@" + from.getUsername())) +
                        "#" + message.getChat().getId() +
                        ": " + message.getText());

        String[] rawcmd = message.getText().split(" ");
        if (rawcmd.length == 0) {
            return false;
        }

        String sentLabel = rawcmd[0].toLowerCase().replace("@" + bot.getMe().getUsername().toLowerCase(), "");
        Optional<BotCommand> target = getCommand(sentLabel);
        if (!target.isPresent()) {
            // no encontrado por primer alias, buscar frase entera
            target = getCommand(message.getText().toLowerCase());
            if (!target.isPresent()) {
                return false; // ni alias ni frase entera
            }
        }

        CommandContext context = new CommandContext(target.get().getArguments(), Arrays.copyOfRange(rawcmd, 1, rawcmd.length));

        log.info(" # Ejecutando '" + target.get().getName() + "'");
        target.get().execute(message.getChat(), from, context, message.getMessageId(), message.getReplyToMessage(), now);

        return true;
    }

    public void onCallbackQuery(CallbackQuery callbackQuery) {
        User from = callbackQuery.getFrom();

        log.info("InlineCallbackQuery: " +
                (from.getUsername() == null ? from.getFirstName() : ("@" + from.getUsername())) +
                "#" + (callbackQuery.getMessage() != null ? callbackQuery.getMessage().getChat().getId() : "") +
                ": " + callbackQuery.getData());

        String callbackQueryDataName = callbackQuery.getData().split("#")[0];
        Optional<CallbackListenerMethodInstance> target = getCallbackListener(callbackQueryDataName);
        if (target.isPresent()) {
            CallbackListenerMethodInstance cblMethod = target.get();
            try {
                log.info(" # Ejecutando callback listener para '" + callbackQuery.getData() + "'");
                cblMethod.method.invoke(cblMethod.listenerInstance, callbackQuery);
            } catch (InvocationTargetException invocationException) {
                if (invocationException.getCause() instanceof TelegramException) { // los métodos de listener pueden lanzar TelegramException
                    log.severe("Error respondiendo a un CallbackQuery en la API de Telegram: ");
                    log.severe(invocationException.getCause().getMessage());
                }
            } catch (IllegalAccessException e) {
                log.severe("Error accediendo a un CallbackListener: ");
                log.severe(e.getMessage());
            }
        }
    }

    public void registerCommandsToTelegramHelp() {
        List<com.cadiducho.telegrambotapi.BotCommand> telegramCommandList = new ArrayList<>();
        for (Map.Entry<String, BotCommand> entry : commandMap.entrySet()) {
            String label = entry.getKey();
            BotCommand cmd = entry.getValue();
            if (cmd.isHidden()) continue; // Si el comando es oculto, no publicarlo en la lista
            if (!cmd.getName().startsWith("/")) continue; // Si no comienza por / no va a ser reconocido por los clientes de telegram como un comando

            com.cadiducho.telegrambotapi.BotCommand telegramCommand = new com.cadiducho.telegrambotapi.BotCommand();
            telegramCommand.setCommand(label);
            String description = cmd.getDescription();
            if (description.length() < 4) {
                description = "Comando sin descripción";
                log.warning("El comando '" + label + "' no tiene descripción.");
            }
            telegramCommand.setDescription(description);
            telegramCommandList.add(telegramCommand);
        }
        try {
            zincite.getTelegramBot().setMyCommands(telegramCommandList);
        } catch (TelegramException e) {
            log.warning("Se ha intentado registrar la lista de comandos a la ayuda de las apps de Telegram pero ha ocurrido un error: ");
            log.warning(e.getMessage());
        }
    }

    @RequiredArgsConstructor
    private class CallbackListenerMethodInstance {
        private final CallbackListener listenerInstance;
        private final Method method;
    }
}
