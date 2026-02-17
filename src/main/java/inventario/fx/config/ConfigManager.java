package inventario.fx.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Gestor centralizado de configuración de la aplicación.
 * Maneja rutas, opciones de backup, base de datos y preferencias generales.
 * Implementa patrón Singleton.
 *
 * <p>Configuración por defecto:
 * <ul>
 *   <li>backup.path → ~/.inventario/backups</li>
 *   <li>backup.enabled → true</li>
 *   <li>backup.keepLast → 7</li>
 *   <li>db.path → ~/.inventario/inventario.db</li>
 *   <li>db.enabled → true</li>
 *   <li>workspace.projects → ~/Inventario/Proyectos</li>
 * </ul>
 *
 * @author SELCOMP
 * @version 1.0
 * @since 2026-01-14
 */
public class ConfigManager {

    private static ConfigManager instance;
    private Properties props;
    private Path configFilePath;

    private static final String CONFIG_DIR = PortablePaths.getDataRoot().toString();
    private static final String CONFIG_FILE = "config.properties";

    private ConfigManager() {
        this.configFilePath = PortablePaths.getConfigProperties();
        this.props = new Properties();
        cargarDefaults();
        cargar();
    }

    /**
     * Obtiene la instancia única del ConfigManager.
     *
     * @return Instancia singleton
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Carga valores por defecto.
     */
    private void cargarDefaults() {
        props.setProperty("backup.path", PortablePaths.getBackupsDir().toString());
        props.setProperty("backup.enabled", "true");
        props.setProperty("backup.keepLast", "7");
        props.setProperty("db.path", PortablePaths.getDatabaseFile().toString());
        props.setProperty("db.enabled", "true");
        props.setProperty("workspace.projects", PortablePaths.getProyectosDir().toString());
        props.setProperty("app.theme", "light");
        props.setProperty("app.language", "es");
    }

    /**
     * Carga la configuración desde archivo.
     */
    private void cargar() {
        try {
            if (Files.exists(configFilePath)) {
                try (InputStream is = Files.newInputStream(configFilePath)) {
                    Properties fileProps = new Properties();
                    fileProps.load(is);
                    // Sobreescribir defaults con valores del archivo
                    fileProps.forEach((k, v) -> props.setProperty(k.toString(), v.toString()));
                }
                System.out.println("[ConfigManager] ✅ Configuración cargada desde: " + configFilePath);
            } else {
                // Crear archivo con defaults
                guardar();
                System.out.println("[ConfigManager] 📝 Archivo de configuración creado con defaults: " + configFilePath);
            }
        } catch (IOException e) {
            System.err.println("[ConfigManager] ⚠️ Error cargando configuración: " + e.getMessage());
        }
    }

    /**
     * Guarda la configuración actual al archivo.
     */
    private void guardar() {
        try {
            Files.createDirectories(configFilePath.getParent());
            try (OutputStream os = Files.newOutputStream(configFilePath)) {
                props.store(os, "Inventario SELCOMP - Configuración");
            }
        } catch (IOException e) {
            System.err.println("[ConfigManager] ❌ Error guardando configuración: " + e.getMessage());
        }
    }

    /**
     * Recarga la configuración desde archivo.
     */
    public void recargar() {
        cargarDefaults();
        cargar();
        System.out.println("[ConfigManager] 🔄 Configuración recargada");
    }

    /**
     * Obtiene un valor de configuración como String.
     *
     * @param key Clave de configuración
     * @return Valor de la configuración
     */
    public String getString(String key) {
        return props.getProperty(key, "");
    }

    /**
     * Obtiene un valor de configuración como String con valor por defecto.
     *
     * @param key          Clave de configuración
     * @param defaultValue Valor por defecto
     * @return Valor de la configuración o el default
     */
    public String getString(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    /**
     * Obtiene un valor de configuración como boolean.
     *
     * @param key Clave de configuración
     * @return Valor boolean de la configuración (default: false)
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(props.getProperty(key, "false"));
    }

    /**
     * Obtiene un valor de configuración como int.
     *
     * @param key          Clave de configuración
     * @param defaultValue Valor por defecto
     * @return Valor int de la configuración
     */
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Establece un valor de configuración y guarda.
     *
     * @param key   Clave
     * @param value Valor
     */
    public void set(String key, String value) {
        props.setProperty(key, value);
        guardar();
    }

    /**
     * Obtiene la ruta del archivo de configuración.
     *
     * @return Ruta absoluta del archivo de configuración
     */
    public String getConfigFilePath() {
        return configFilePath.toString();
    }
}
