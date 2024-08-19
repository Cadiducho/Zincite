package com.cadiducho.zincite.api.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Clase para gestionar los módulos de Zincite
 */
@Log
@RequiredArgsConstructor
public class ModuleManager {
    
    @Getter private final List<ZinciteModule> modules = new ArrayList<>();

    @Getter private final File modulesFolder;

    /**
     * Registrar un módulo para su posterior inicialización
     * @param module El módulo a registrar
     */
    public void registerModule(ZinciteModule module) {
        modules.add(module);
    }

    /**
     * Cargar los módulos desde la carpeta de módulos
     * @throws IOException Si ocurre un error al cargar los módulos
     * @throws ClassNotFoundException Si no se encuentra la clase del módulo
     * @throws IllegalAccessException Si no se puede acceder a la clase del módulo
     * @throws InstantiationException Si no se puede instanciar la clase del módulo
     * @throws NoSuchMethodException Si no se encuentra el constructor de la clase del módulo
     * @throws InvocationTargetException Si ocurre un error al invocar el constructor de la clase del módulo
     */
    public void loadModules() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        log.info("Cargando módulos...");
        if (Files.notExists(modulesFolder.toPath())) {
            Files.createDirectories(modulesFolder.toPath());
        }

        final File[] files = modulesFolder.listFiles(pathname -> !pathname.isDirectory() && pathname.getName().endsWith(".jar"));
        if (files == null) {
            return;
        }

        final URL[] urls = new URL[files.length];

        for (int i = 0; i < files.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }

        try (URLClassLoader classLoader = new URLClassLoader(urls)) {
            for (File file : files) {
                try (JarFile jarFile = new JarFile(file)) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry jarEntry = entries.nextElement();
                        if (jarEntry.getName().endsWith(".class")) {
                            String className = jarEntry.getName().replace("/", ".").replace(".class", "");
                            Class<?> targetClass = classLoader.loadClass(className);
                            if (ZinciteModule.class.isAssignableFrom(targetClass)) {
                                ZinciteModule module = (ZinciteModule) targetClass.getDeclaredConstructor().newInstance();
                                modules.add(module);
                            }
                        }
                    }
                }
            }
        }

        modules.forEach(ZinciteModule::onLoad);
        log.info("Módulos cargados.");
    }
    
    /**
     * Obten un modulo por su id
     *
     * @param id la id para buscar
     * @return el modulo, o Optional.empty() si no ha sido encontrado
     */
    public Optional<ZinciteModule> getModule(String id) {
        for (ZinciteModule mod : modules) {
            if (mod.getName().equalsIgnoreCase(id)) {
                return Optional.of(mod);
            }
        }
        return Optional.empty();
    }
}
