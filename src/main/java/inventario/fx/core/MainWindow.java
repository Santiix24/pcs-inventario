package inventario.fx.core;
import inventario.fx.model.TemaManager;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Ventana principal de la aplicación con diseño premium y adaptación automática al tema.
 * Se muestra después del splash screen con una transición suave.
 */
public class MainWindow {
    
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    
    /**
     * Muestra la ventana principal con efectos suaves y tema adaptativo
     * @param primaryStage Stage principal de la aplicación
     */
    public void show(Stage primaryStage) {
        
        // ═══════════════════════════════════════════════════════════════════
        // CREAR FONDO CON GRADIENTE SEGÚN TEMA
        // ═══════════════════════════════════════════════════════════════════
        
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));
        root.setSpacing(20);
        
        // Aplicar fondo degradado usando TemaManager
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " +
            TemaManager.getBgLight() + ", " +
            TemaManager.getBg() + ");" +
            "-fx-background-radius: 0;"
        );
        
        // ═══════════════════════════════════════════════════════════════════
        // LABELS DE BIENVENIDA CON TIPOGRAFÍA PREMIUM
        // ═══════════════════════════════════════════════════════════════════
        
        // Título principal
        Label welcomeLabel = new Label("Bienvenido a SELCOMP");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        welcomeLabel.setTextFill(TemaManager.getTextColor());
        welcomeLabel.setStyle(
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 2);"
        );
        
        // Subtítulo
        Label readyLabel = new Label("Tu aplicación está lista");
        readyLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        readyLabel.setTextFill(TemaManager.getTextSecondaryColor());
        readyLabel.setStyle(
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 4, 0, 0, 1);"
        );
        
        // Agregar elementos al contenedor
        root.getChildren().addAll(welcomeLabel, readyLabel);
        
        // ═══════════════════════════════════════════════════════════════════
        // CONFIGURAR ESCENA Y STAGE
        // ═══════════════════════════════════════════════════════════════════
        
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        primaryStage.setTitle("SELCOMP - Sistema de Inventario");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        
        // Cargar icono de la aplicación
        try {
            java.io.InputStream iconStream = getClass().getResourceAsStream("/images/Selcomp_logito.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo cargar el icono: " + e.getMessage());
        }
        
        // ═══════════════════════════════════════════════════════════════════
        // ANIMACIÓN DE ENTRADA CON FADE-IN SUAVE
        // ═══════════════════════════════════════════════════════════════════
        
        // Inicialmente invisible para el fade-in
        root.setOpacity(0.0);
        
        // Mostrar la ventana
        primaryStage.show();
        
        // Centrar en pantalla
        centerOnScreen(primaryStage);
        
        // Animación de entrada suave premium
        root.setScaleX(0.97);
        root.setScaleY(0.97);
        
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
        
        javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(550), root);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
        
        new javafx.animation.ParallelTransition(fadeIn, scaleIn).play();
        
        // ═══════════════════════════════════════════════════════════════════
        // CONFIGURACIÓN ADICIONAL
        // ═══════════════════════════════════════════════════════════════════
        
        // Manejar cierre de la aplicación
        primaryStage.setOnCloseRequest(e -> {
            System.out.println("👋 Cerrando SELCOMP - Hasta luego!");
            javafx.application.Platform.exit();
        });
        
        System.out.println("✅ Ventana principal mostrada (tema: " + (TemaManager.isDarkMode() ? "oscuro" : "claro") + ")");
    }
    
    /**
     * Centra la ventana en la pantalla
     */
    private void centerOnScreen(Stage stage) {
        // Obtener dimensiones de la pantalla
        double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
        
        // Calcular posición centrada
        double centerX = (screenWidth - WINDOW_WIDTH) / 2;
        double centerY = (screenHeight - WINDOW_HEIGHT) / 2;
        
        stage.setX(centerX);
        stage.setY(centerY);
    }
    

}