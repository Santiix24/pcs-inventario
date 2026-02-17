package inventario.fx.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger personalizado para la aplicación de inventario.
 * Proporciona logging con timestamps, niveles y emojis informativos.
 * Delega al sistema de logging estándar de Java (java.util.logging).
 *
 * <p>Uso:
 * <pre>
 *   private static final AppLogger logger = AppLogger.getLogger(MiClase.class);
 *   logger.info("Mensaje informativo");
 *   logger.error("Error", exception);
 *   logger.guardadoExitoso("Proyecto X");
 * </pre>
 *
 * @author SELCOMP
 * @version 1.0
 * @since 2026-01-14
 */
public class AppLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private final String className;
    private final java.util.logging.Logger julLogger;

    private AppLogger(Class<?> clazz) {
        this.className = clazz.getSimpleName();
        this.julLogger = java.util.logging.Logger.getLogger(clazz.getName());
    }

    /**
     * Obtiene una instancia del logger para la clase especificada.
     *
     * @param clazz Clase para la cual se crea el logger
     * @return Instancia de AppLogger
     */
    public static AppLogger getLogger(Class<?> clazz) {
        return new AppLogger(clazz);
    }

    /**
     * Registra un mensaje de nivel INFO.
     *
     * @param message Mensaje a registrar
     */
    public void info(String message) {
        String formatted = formatMessage("INFO", message);
        System.out.println(formatted);
        julLogger.info(message);
    }

    /**
     * Registra un mensaje de nivel WARNING.
     *
     * @param message Mensaje a registrar
     */
    public void warn(String message) {
        String formatted = formatMessage("WARN", message);
        System.out.println(formatted);
        julLogger.warning(message);
    }

    /**
     * Registra un mensaje de nivel WARNING con excepción.
     *
     * @param message Mensaje a registrar
     * @param throwable Excepción asociada
     */
    public void warn(String message, Throwable throwable) {
        String formatted = formatMessage("WARN", message);
        System.out.println(formatted);
        if (throwable != null) {
            System.out.println("  Causa: " + throwable.getMessage());
        }
        julLogger.log(java.util.logging.Level.WARNING, message, throwable);
    }

    /**
     * Registra un mensaje de nivel ERROR.
     *
     * @param message Mensaje a registrar
     */
    public void error(String message) {
        String formatted = formatMessage("ERROR", message);
        System.err.println(formatted);
        julLogger.severe(message);
    }

    /**
     * Registra un mensaje de nivel ERROR con excepción.
     *
     * @param message Mensaje a registrar
     * @param throwable Excepción asociada
     */
    public void error(String message, Throwable throwable) {
        String formatted = formatMessage("ERROR", message);
        System.err.println(formatted);
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            System.err.println("  " + sw.toString().split("\n")[0]);
        }
        julLogger.log(java.util.logging.Level.SEVERE, message, throwable);
    }

    /**
     * Registra un mensaje de nivel DEBUG.
     *
     * @param message Mensaje a registrar
     */
    public void debug(String message) {
        String formatted = formatMessage("DEBUG", message);
        System.out.println(formatted);
        julLogger.fine(message);
    }

    /**
     * Registra un guardado exitoso con formato especial.
     *
     * @param detalle Detalle del guardado
     */
    public void guardadoExitoso(String detalle) {
        info("💾 Guardado exitoso: " + detalle);
    }

    /**
     * Registra una eliminación exitosa con formato especial.
     *
     * @param detalle Detalle de la eliminación
     */
    public void eliminadoExitoso(String detalle) {
        info("🗑️ Eliminado exitoso: " + detalle);
    }

    /**
     * Registra una operación de restauración exitosa.
     *
     * @param detalle Detalle de la restauración
     */
    public void restauradoExitoso(String detalle) {
        info("♻️ Restaurado exitoso: " + detalle);
    }

    /**
     * Formatea un mensaje con timestamp, nivel y nombre de clase.
     */
    private String formatMessage(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String threadName = Thread.currentThread().getName();
        return String.format("%s %-5s [%s] %s - %s", timestamp, level, threadName, className, message);
    }
}
