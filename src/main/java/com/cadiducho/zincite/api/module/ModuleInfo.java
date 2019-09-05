package com.cadiducho.zincite.api.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para dotar de descripción a un {@link ZinciteModule}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {

    /**
     * Nombre del módulo
     * @return El nombre
     */
    String name();

    /**
     * Una breve descripción de qué hace el módulo
     * @return su descripción
     */
    String description();
}