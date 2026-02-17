package inventario.fx.ui.component;
import inventario.fx.model.TemaManager;
import inventario.fx.icons.IconosSVG;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.Map;
import java.util.WeakHashMap;
import inventario.fx.util.AnimacionesFX;

/**
 * Sistema centralizado de notificaciones toast compactas y modernas.
 * Las notificaciones se muestran DENTRO de la ventana en la esquina inferior derecha.
 * Soporta apilamiento vertical de múltiples notificaciones simultáneas.
 * Con degradados sutiles y efectos glow en modo oscuro para mejor visibilidad.
 */
public class NotificacionesFX {
    
    private static final int DURACION_AUTO_CIERRE = 4000; // 4 segundos
    private static final int DURACION_ANIMACION = 300; // 300ms
    private static final int MAX_NOTIFICACIONES = 5; // Máximo visible a la vez
    
    // Contenedor de stack por cada StackPane padre (WeakHashMap evita memory leak)
    private static final Map<StackPane, VBox> stackContainers = new WeakHashMap<>();
    
    public static void success(StackPane contenedor, String titulo, String mensaje) {
        mostrarNotificacion(contenedor, titulo, mensaje, TemaManager.COLOR_SUCCESS);
    }
    
    public static void error(StackPane contenedor, String titulo, String mensaje) {
        mostrarNotificacion(contenedor, titulo, mensaje, TemaManager.COLOR_DANGER);
    }
    
    public static void warning(StackPane contenedor, String titulo, String mensaje) {
        mostrarNotificacion(contenedor, titulo, mensaje, TemaManager.COLOR_WARNING);
    }
    
    public static void info(StackPane contenedor, String titulo, String mensaje) {
        mostrarNotificacion(contenedor, titulo, mensaje, TemaManager.COLOR_INFO);
    }
    
    /**
     * Obtiene o crea el contenedor VBox para apilar notificaciones.
     */
    private static VBox obtenerStack(StackPane contenedor) {
        return stackContainers.computeIfAbsent(contenedor, k -> {
            VBox stack = new VBox(8);
            stack.setAlignment(Pos.BOTTOM_RIGHT);
            stack.setPickOnBounds(false);
            stack.setMouseTransparent(false);
            stack.setMaxWidth(420);
            stack.setPadding(new Insets(0, 20, 20, 0));
            StackPane.setAlignment(stack, Pos.BOTTOM_RIGHT);
            contenedor.getChildren().add(stack);
            return stack;
        });
    }
    
    public static void mostrarNotificacion(StackPane contenedor, String titulo, String mensaje, String color) {
        Platform.runLater(() -> {
            VBox stack = obtenerStack(contenedor);
            
            // Limitar cantidad de notificaciones visibles
            while (stack.getChildren().size() >= MAX_NOTIFICACIONES) {
                stack.getChildren().remove(0);
            }
            
            HBox container = new HBox(14);
            container.setMaxWidth(400);
            container.setMinHeight(60);
            container.setMaxHeight(90);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(14, 18, 14, 18));
            
            String bgColor;
            String borderColor;
            String shadowColor;
            
            if (TemaManager.isDarkMode()) {
                bgColor = TemaManager.getSurface();
                borderColor = color;
                shadowColor = "rgba(0, 0, 0, 0.4)";
            } else {
                bgColor = TemaManager.getSurface();
                borderColor = color + "60";
                shadowColor = "rgba(0, 0, 0, 0.15)";
            }
            
            container.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-width: 1.5;" +
                "-fx-effect: dropshadow(gaussian, " + shadowColor + ", 20, 0.15, 0, 4);"
            );
            
            SVGPath iconoLucide;
            String colorUpper = color.toUpperCase();
            // Detectar tipo por color (compatible con TemaManager y colores legacy)
            if (colorUpper.contains("00C8") || colorUpper.contains("10B9") || colorUpper.contains("22C5")) {
                iconoLucide = IconosSVG.crearIconoCheckCircle(color, 20); // success
            } else if (colorUpper.contains("FF52") || colorUpper.contains("EF44") || colorUpper.contains("DC26")) {
                iconoLucide = IconosSVG.crearIconoXCircle(color, 20); // danger/error
            } else if (colorUpper.contains("FFB3") || colorUpper.contains("F59E") || colorUpper.contains("F97")) {
                iconoLucide = IconosSVG.crearIconoAlertTriangle(color, 20); // warning
            } else {
                iconoLucide = IconosSVG.crearIconoInfo(color, 20); // info/default
            }
            
            StackPane iconWrapper = new StackPane(iconoLucide);
            iconWrapper.setPrefSize(42, 42);
            
            String iconBgColor = color + "20";
            iconWrapper.setStyle(
                "-fx-background-color: " + iconBgColor + ";" +
                "-fx-background-radius: 21;"
            );
            
            VBox textos = new VBox(2);
            textos.setAlignment(Pos.CENTER_LEFT);
            textos.setMaxWidth(280);
            
            Label lblTitulo = new Label(titulo);
            lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            lblTitulo.setTextFill(Color.web(TemaManager.getText()));
            lblTitulo.setMaxWidth(280);
            lblTitulo.setStyle("-fx-text-overrun: ellipsis;");
            
            Label lblMensaje = new Label(mensaje);
            lblMensaje.setFont(Font.font("Segoe UI", 12));
            lblMensaje.setTextFill(Color.web(TemaManager.getTextMuted()));
            lblMensaje.setWrapText(true);
            lblMensaje.setMaxHeight(45);
            lblMensaje.setMaxWidth(280);
            lblMensaje.setStyle("-fx-text-overrun: ellipsis;");
            
            textos.getChildren().addAll(lblTitulo, lblMensaje);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button btnCerrar = new Button();
            btnCerrar.setGraphic(IconosSVG.crearIconoX(TemaManager.getTextMuted(), 18));
            btnCerrar.setPrefSize(28, 28);
            btnCerrar.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 0;"
            );
            
            String hoverBg = TemaManager.isDarkMode() ? color + "25" : color + "15";
            btnCerrar.setOnMouseEntered(e -> {
                btnCerrar.setStyle(
                    "-fx-background-color: " + hoverBg + ";" +
                    "-fx-cursor: hand;" +
                    "-fx-background-radius: 14;" +
                    "-fx-padding: 0;"
                );
                AnimacionesFX.hoverIn(btnCerrar, 1.1, 120);
            });
            btnCerrar.setOnMouseExited(e -> {
                btnCerrar.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-cursor: hand;" +
                    "-fx-background-radius: 14;" +
                    "-fx-padding: 0;"
                );
                AnimacionesFX.hoverOut(btnCerrar, 120);
            });
            
            container.getChildren().addAll(iconWrapper, textos, spacer, btnCerrar);
            
            // Animación de entrada
            container.setTranslateX(40);
            container.setOpacity(0);
            
            Runnable cerrarConAnimacion = () -> {
                TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), container);
                slideOut.setToX(40);
                slideOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
                
                FadeTransition fadeOut = new FadeTransition(Duration.millis(250), container);
                fadeOut.setToValue(0);
                fadeOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
                
                ParallelTransition salida = new ParallelTransition(slideOut, fadeOut);
                salida.setOnFinished(ev -> {
                    stack.getChildren().remove(container);
                    // Limpiar stack si está vacío
                    if (stack.getChildren().isEmpty()) {
                        contenedor.getChildren().remove(stack);
                        stackContainers.remove(contenedor);
                    }
                });
                salida.play();
            };
            
            btnCerrar.setOnAction(e -> cerrarConAnimacion.run());
            
            PauseTransition autoCerrar = new PauseTransition(Duration.millis(DURACION_AUTO_CIERRE));
            autoCerrar.setOnFinished(e -> cerrarConAnimacion.run());
            
            stack.getChildren().add(container);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(DURACION_ANIMACION), container);
            slideIn.setFromX(40);
            slideIn.setToX(0);
            slideIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(DURACION_ANIMACION), container);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            
            new ParallelTransition(slideIn, fadeIn).play();
            autoCerrar.play();
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LOADING OVERLAY — Indicador de operación en progreso
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Muestra un overlay semi-transparente con spinner y mensaje sobre el contenedor.
     * Retorna un Runnable que al ejecutarse cierra el overlay con animación.
     *
     * Uso:
     *   Runnable cerrar = NotificacionesFX.loading(contenedor, "Generando reporte...");
     *   // ... operación larga ...
     *   Platform.runLater(cerrar);
     */
    public static Runnable loading(StackPane contenedor, String mensaje) {
        // Overlay fondo oscuro
        Region fondo = new Region();
        fondo.setStyle("-fx-background-color: rgba(0,0,0,0.45); -fx-background-radius: 0;");
        fondo.setMouseTransparent(false);

        // Card central
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(32, 48, 32, 48));
        card.setMaxWidth(320);
        card.setMaxHeight(180);
        card.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 30, 0, 0, 8);"
        );

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(44, 44);
        spinner.setMaxSize(44, 44);
        spinner.setStyle("-fx-progress-color: " + TemaManager.COLOR_PRIMARY + ";");

        Label lbl = new Label(mensaje);
        lbl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        lbl.setTextFill(Color.web(TemaManager.getText()));
        lbl.setWrapText(true);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle("-fx-text-alignment: center;");

        card.getChildren().addAll(spinner, lbl);

        StackPane overlay = new StackPane(fondo, card);
        overlay.setAlignment(Pos.CENTER);

        Platform.runLater(() -> {
            contenedor.getChildren().add(overlay);
            // Fade in
            overlay.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(200), overlay);
            fade.setToValue(1);
            fade.play();
        });

        // Retorna Runnable para cerrar
        return () -> Platform.runLater(() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), overlay);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> contenedor.getChildren().remove(overlay));
            fadeOut.play();
        });
    }
}
