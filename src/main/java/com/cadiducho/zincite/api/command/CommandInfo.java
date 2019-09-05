package com.cadiducho.zincite.api.command;

import com.cadiducho.zincite.api.command.args.Argument;
import com.cadiducho.zincite.api.module.ZinciteModule;

import java.lang.annotation.*;

/**
 * Anotación para construir los parámetros de un {@link BotCommand}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {

    /**
     * {@link ZinciteModule} al que pertenece el comando
     * @return Módulo al que pertenece el comando
     */
    Class<? extends ZinciteModule> module() default ZinciteModule.class;

    /**
     * Lista de argumentos del comando. Ver {@link Argument}
     * @return lista de argumentos
     */
    Argument[] arguments() default {};

    /**
     * Lista de alias por las que ese comando se puede ejecutar
     * @return lista de alias
     */
    String[] aliases();

    /**
     * Descripción breve de lo que hace el comando
     * @return descripción deol comando
     */
    String description() default "";
}