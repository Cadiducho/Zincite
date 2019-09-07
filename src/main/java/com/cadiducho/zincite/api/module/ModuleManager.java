package com.cadiducho.zincite.api.module;

import com.cadiducho.zincite.ZinciteBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Log
@RequiredArgsConstructor
public class ModuleManager {
    
    @Getter private final List<ZinciteModule> modules = new ArrayList<>();
    
    private final ZinciteBot server;
    @Getter private final File modulesFolder;

    /**
     * Registrar un módulo para su posterior inicialización
     * @param module El módulo a registrar
     */
    public void registerModule(ZinciteModule module) {
        modules.add(module);
    }
    
    public void loadModules() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
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

        final URLClassLoader classLoader = new URLClassLoader(urls);

        for (final File file : files) {
            final JarFile jarFile = new JarFile(file);
            final Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                final JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    //server.debugLog(jarEntry.getName().replace("/", ".").substring(0, jarEntry.getName().length() - 6));
                    Class<?> targetClass = classLoader.loadClass(jarEntry.getName().replace("/", ".").substring(0, jarEntry.getName().length() - 6));
                    if (ZinciteModule.class.isAssignableFrom(targetClass)) {
                        final ZinciteModule module = (ZinciteModule) targetClass.newInstance();
                        modules.add(module);
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
