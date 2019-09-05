package com.cadiducho.zincite.api.module;

import com.cadiducho.zincite.ZinciteBot;
import com.cadiducho.telegrambotapi.Chat;
import com.cadiducho.telegrambotapi.Update;
import com.cadiducho.telegrambotapi.User;

import java.util.List;

/**
 * Modulo para funcionalidades del framework
 * @author Cadiducho
 */
public interface ZinciteModule {

    ZinciteBot FRAMEWORK_BOT = ZinciteBot.getInstance();

    /**
     * Devuelve si el módulo está activado en el servidor
     * @return enabled
     */
    default boolean isEnabled() {
        return FRAMEWORK_BOT.getModuleManager().getModules().contains(this);
    }

    /**
     * Retorna el nombre del módulo
     * @return nombre
     */
    default String getName() {
        if (getClass().isAnnotationPresent(ModuleInfo.class)) {
            ModuleInfo moduleInfo = getClass().getAnnotation(ModuleInfo.class);
            return moduleInfo.name();
        } else {
            return null;
        }
    }

    /**
     * Devuelve una corta descripción del módulo
     *
     * @return La descripción
     */
    default String getDescription() {
        if (getClass().isAnnotationPresent(ModuleInfo.class)) {
            ModuleInfo moduleInfo = getClass().getAnnotation(ModuleInfo.class);
            return moduleInfo.description();
        } else {
            return null;
        }
    }

    default void onLoad() {
    }

    default void onClose() {
    }

    default void onNewChatMembers(Chat chat, List<User> newChatMembers) {
    }

    default void onLeftChatMember(Chat chat, User leftChatMember) {
    }
    
    default void onPostCommand(Update update, boolean success) {
    }
}