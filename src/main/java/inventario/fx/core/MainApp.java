package inventario.fx.core;

import inventario.fx.util.AppLogger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación JavaFX.
 * Punto de entrada para la versión JavaFX del inventario SELCOMP.
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        // Configurar el stage principal
        stage.setTitle("SELCOMP - INVENTARIO PC");
        stage.setResizable(true);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        // Cargar icono de la aplicación
        try {
            java.io.InputStream iconStream = getClass().getResourceAsStream("/images/Selcomp_logito.png");
            if (iconStream != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }

        // Banner de inicio en consola
        System.out.println("=".repeat(50));
        System.out.println("🚀 SELCOMP - INVENTARIO PC (JavaFX)");
        System.out.println("=".repeat(50));
        System.out.println("✓ Sistema iniciado correctamente");
        System.out.println("=".repeat(50));

        // Mostrar pantalla de bienvenida (splash)
        Stage splashStage = new Stage();
        splashStage.initOwner(stage);
        InventarioFX.mostrarSplash(splashStage, stage);
    }

    @Override
    public void stop() {
        // Limpiar recursos al cerrar la aplicación
        Platform.exit();
        System.exit(0);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        // Desactivar logs de debug de JNA
        System.setProperty("jna.debug_load", "false");
        
        // ═══════════════════════════════════════════════════════════════════
        // INICIALIZACIÓN PORTABLE: Crear estructura de carpetas junto al EXE
        // ═══════════════════════════════════════════════════════════════════
        try {
            inventario.fx.config.PortablePaths.inicializar();
            inventario.fx.config.PortablePaths.migrarDatosAntiguos();
        } catch (Exception e) {
            AppLogger.getLogger(MainApp.class).error("❌ Error inicializando rutas portables: " + e.getMessage(), e);
        }
        
        // ═══════════════════════════════════════════════════════════════════
        // MODO SIMULACIÓN: Activar con argumento --simular=ID_PERFIL
        // Ejemplos:
        //   mvn javafx:run -Djavafx.args="--simular=mac_m2_sonoma"
        //   mvn javafx:run -Djavafx.args="--simular=linux_ubuntu_desktop"
        // ═══════════════════════════════════════════════════════════════════
        for (String arg : args) {
            if (arg.startsWith("--simular=")) {
                String perfil = arg.substring("--simular=".length());
                if (SimuladorEntorno.activar(perfil)) {
                    System.out.println("✅ Aplicación ejecutándose en MODO SIMULACIÓN");
                } else {
                    System.out.println("⚠️ Perfil no encontrado: " + perfil);
                    System.out.println("Perfiles disponibles: " + SimuladorEntorno.listarPerfiles());
                }
            }
        }
        
        // Configurar manejador de excepciones global para capturar errores no manejados.
        // Filtra errores conocidos de JavaFX que no afectan funcionalidad.
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // Silenciar errores específicos de synchronizeSceneNodes que no afectan funcionalidad
            if (throwable instanceof NullPointerException) {
                String stackTrace = getStackTraceAsString(throwable);
                if (stackTrace.contains("synchronizeSceneNodes") || 
                    stackTrace.contains("Scene$ScenePulseListener")) {
                    return;
                }
            }
            // Silenciar error de dash lengths cuando hay animaciones de línea en progreso
            if (throwable instanceof IllegalArgumentException) {
                String msg = throwable.getMessage();
                if (msg != null && msg.contains("dash lengths")) {
                    return;
                }
            }
            // Para otros errores, loggear con AppLogger
            inventario.fx.util.AppLogger.getLogger(MainApp.class)
                .error("Error no capturado en hilo [" + thread.getName() + "]: " + throwable.getMessage(), throwable);
        });
        
        // Lanzar la aplicación JavaFX
        launch(args);
    }
    
    /**
     * Convierte un Throwable a String para análisis.
     */
    private static String getStackTraceAsString(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}