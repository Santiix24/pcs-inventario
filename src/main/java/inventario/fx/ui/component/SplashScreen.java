package inventario.fx.ui.component;
import inventario.fx.model.TemaManager;
import inventario.fx.core.InventarioFX;
import inventario.fx.util.AppLogger;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.InputStream;

import inventario.fx.util.AnimacionesFX;

/**
 * Splash Screen elegante con animaciones suaves y adaptación a temas claro/oscuro.
 * Duración total: 3000ms (3 segundos)
 * - Logo: zoom + fade-in durante 800ms
 * - Progress bar: se llena en 2500ms
 * - Fade-out final y transición a ventana principal
 */
public class SplashScreen {

    private static final int SPLASH_DURATION = 3000; // 3 segundos total
    private static final int LOGO_ANIMATION_DURATION = 800; // 800ms para logo
    private static final int PROGRESS_DURATION = 2500; // 2.5 segundos para barra
    
    private Stage splashStage;
    private Stage mainStage;
    private StackPane root;
    private ImageView logoImageView;
    private ProgressBar progressBar;
    private VBox container;
    
    public SplashScreen(Stage mainStage) {
        this.mainStage = mainStage;
        this.splashStage = new Stage();
        initializeSplashScreen();
    }
    
    /**
     * Inicializa y configura el splash screen
     */
    private void initializeSplashScreen() {
        splashStage.initStyle(StageStyle.TRANSPARENT); // Sin bordes, transparente
        splashStage.initOwner(mainStage);
        
        // Root container con fondo transparente y padding para esquinas redondeadas
        root = new StackPane();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(20));
        
        // Contenedor principal con bordes redondeados
        container = new VBox(25);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50, 60, 50, 60));
        container.setMaxWidth(500);
        container.setMaxHeight(300);
        
        // Aplicar estilos según el tema (claro/oscuro)
        updateThemeStyles();
        
        // Configurar logo
        setupLogo();
        
        // Configurar título
        Label titleLabel = setupTitle();
        
        // Configurar barra de progreso
        setupProgressBar();
        
        // Agregar componentes al contenedor
        container.getChildren().addAll(logoImageView, titleLabel, progressBar);
        root.getChildren().add(container);
        
        // Crear escena con tamaño especificado: 600x400px
        Scene scene = new Scene(root, 600, 400);
        scene.setFill(Color.TRANSPARENT); // Fondo transparente para efectos
        
        splashStage.setScene(scene);
        splashStage.centerOnScreen();
        
        // Inicializar opacidad en 0 para fade-in inicial
        root.setOpacity(0.0);
    }
    
    /**
     * Actualiza estilos según el tema actual (claro/oscuro)
     */
    private void updateThemeStyles() {
        String backgroundColor = TemaManager.getSurface();
        String borderColor = TemaManager.getBorderLight();
        
        container.setStyle(
            "-fx-background-color: " + backgroundColor + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 25, 0, 0, 5);"
        );
    }
    
    /**
     * Configura el logo con efectos iniciales
     */
    private void setupLogo() {
        try {
            // Cargar imagen del logo
            InputStream logoStream = getClass().getResourceAsStream("/images/Selcomp.png");
            if (logoStream != null) {
                Image logoImage = new Image(logoStream);
                logoImageView = new ImageView(logoImage);
                logoImageView.setFitWidth(120);
                logoImageView.setFitHeight(120);
                logoImageView.setPreserveRatio(true);
                logoImageView.setSmooth(true);
                
                // Efectos iniciales: invisible y pequeño para animación
                logoImageView.setOpacity(0.0);
                logoImageView.setScaleX(0.3);
                logoImageView.setScaleY(0.3);
                
                // Sombra sutil
                DropShadow logoShadow = new DropShadow();
                logoShadow.setColor(Color.web("#00000020"));
                logoShadow.setRadius(10);
                logoImageView.setEffect(logoShadow);
                
            } else {
                // Logo de fallback si no se encuentra la imagen
                logoImageView = createFallbackLogo();
            }
        } catch (Exception e) {
            System.err.println("Error cargando logo: " + e.getMessage());
            logoImageView = createFallbackLogo();
        }
    }
    
    /**
     * Crea un logo de respaldo si no se puede cargar la imagen
     */
    private ImageView createFallbackLogo() {
        // Crear un logo simple con texto
        Label fallbackLabel = new Label("SELCOMP");
        fallbackLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        fallbackLabel.setTextFill(Color.web(TemaManager.COLOR_PRIMARY));
        
        // Convertir a ImageView (simplificado)
        ImageView fallback = new ImageView();
        fallback.setFitWidth(120);
        fallback.setFitHeight(120);
        fallback.setOpacity(0.0);
        fallback.setScaleX(0.3);
        fallback.setScaleY(0.3);
        
        return fallback;
    }
    
    /**
     * Configura el título de la aplicación
     */
    private Label setupTitle() {
        Label titleLabel = new Label("INVENTARIO PC");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        
        // Color del texto según tema
        titleLabel.setTextFill(Color.web(TemaManager.getText()));
        titleLabel.setOpacity(0.0); // Inicia invisible
        
        return titleLabel;
    }
    
    /**
     * Configura la barra de progreso con estilos personalizados
     */
    private void setupProgressBar() {
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(8);
        
        // Estilos personalizados para la barra de progreso
        String trackColor = TemaManager.getBg();
        String barColor = TemaManager.COLOR_PRIMARY; // Rojo como solicitado
        
        progressBar.setStyle(
            "-fx-accent: " + barColor + ";" +
            "-fx-control-inner-background: " + trackColor + ";" +
            "-fx-background-radius: 4;" +
            "-fx-background-insets: 0;"
        );
        
        progressBar.setOpacity(0.0); // Inicia invisible
    }
    
    /**
     * Muestra el splash screen con todas las animaciones
     */
    public void show() {
        splashStage.show();
        startAnimationSequence();
    }
    
    /**
     * Inicia la secuencia completa de animaciones
     */
    private void startAnimationSequence() {
        // 1. Fade-in inicial del contenedor (250ms) con ease-out
        FadeTransition containerFadeIn = new FadeTransition(Duration.millis(250), root);
        containerFadeIn.setFromValue(0.0);
        containerFadeIn.setToValue(1.0);
        containerFadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        // Container scale sutil para efecto de aparición
        ScaleTransition containerScale = new ScaleTransition(Duration.millis(350), container);
        container.setScaleX(0.95);
        container.setScaleY(0.95);
        containerScale.setFromX(0.95);
        containerScale.setFromY(0.95);
        containerScale.setToX(1.0);
        containerScale.setToY(1.0);
        containerScale.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        ParallelTransition containerEntry = new ParallelTransition(containerFadeIn, containerScale);
        
        // 2. Animación del logo: zoom + fade-in + bounce sutil (800ms)
        ScaleTransition logoScale = new ScaleTransition(Duration.millis(LOGO_ANIMATION_DURATION), logoImageView);
        logoScale.setFromX(0.3);
        logoScale.setFromY(0.3);
        logoScale.setToX(1.0);
        logoScale.setToY(1.0);
        logoScale.setInterpolator(AnimacionesFX.EASE_OUT_BACK); // Bounce sutil
        
        FadeTransition logoFadeIn = new FadeTransition(Duration.millis(LOGO_ANIMATION_DURATION), logoImageView);
        logoFadeIn.setFromValue(0.0);
        logoFadeIn.setToValue(1.0);
        logoFadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        // Slide up sutil del logo
        TranslateTransition logoSlide = new TranslateTransition(Duration.millis(LOGO_ANIMATION_DURATION), logoImageView);
        logoImageView.setTranslateY(15);
        logoSlide.setFromY(15);
        logoSlide.setToY(0);
        logoSlide.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        // Ejecutar animaciones del logo en paralelo
        ParallelTransition logoAnimation = new ParallelTransition(logoScale, logoFadeIn, logoSlide);
        
        // 3. Fade-in + slide del título (300ms) - después del logo
        Node titleNode = container.getChildren().get(1);
        titleNode.setTranslateY(8);
        FadeTransition titleFadeIn = new FadeTransition(Duration.millis(300), titleNode);
        titleFadeIn.setFromValue(0.0);
        titleFadeIn.setToValue(0.8);
        titleFadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        TranslateTransition titleSlide = new TranslateTransition(Duration.millis(300), titleNode);
        titleSlide.setFromY(8);
        titleSlide.setToY(0);
        titleSlide.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        ParallelTransition titleEntry = new ParallelTransition(titleFadeIn, titleSlide);
        
        // 4. Fade-in de la barra de progreso (250ms)
        progressBar.setTranslateY(6);
        FadeTransition progressFadeIn = new FadeTransition(Duration.millis(250), progressBar);
        progressFadeIn.setFromValue(0.0);
        progressFadeIn.setToValue(1.0);
        progressFadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        TranslateTransition progressSlide = new TranslateTransition(Duration.millis(250), progressBar);
        progressSlide.setFromY(6);
        progressSlide.setToY(0);
        progressSlide.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        ParallelTransition progressEntry = new ParallelTransition(progressFadeIn, progressSlide);
        
        // 5. Animación de llenado de la barra de progreso con ease-out (2500ms)
        Timeline progressTimeline = new Timeline();
        progressTimeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(PROGRESS_DURATION), 
                new KeyValue(progressBar.progressProperty(), 1.0, AnimacionesFX.EASE_OUT_QUART))
        );
        
        // 6. Secuencia de animaciones
        SequentialTransition sequence = new SequentialTransition(
            containerEntry,
            new PauseTransition(Duration.millis(80)),
            logoAnimation,
            titleEntry,
            progressEntry,
            progressTimeline
        );
        
        // Al completar todas las animaciones, iniciar fade-out y mostrar ventana principal
        sequence.setOnFinished(e -> fadeOutAndShowMainWindow());
        
        sequence.play();
    }
    
    /**
     * Realiza el fade-out final y muestra la ventana principal
     */
    private void fadeOutAndShowMainWindow() {
        // Pequeña pausa antes del fade-out
        PauseTransition pause = new PauseTransition(Duration.millis(200));
        pause.setOnFinished(e -> {
            // Fade-out + scale down del splash screen (350ms)
            FadeTransition fadeOut = new FadeTransition(Duration.millis(350), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setInterpolator(AnimacionesFX.EASE_IN_OUT);
            
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(350), container);
            scaleOut.setToX(0.96);
            scaleOut.setToY(0.96);
            scaleOut.setInterpolator(AnimacionesFX.EASE_IN_OUT);
            
            ParallelTransition exit = new ParallelTransition(fadeOut, scaleOut);
            
            exit.setOnFinished(event -> {
                // Cerrar splash screen
                splashStage.close();
                
                // Mostrar ventana principal
                Platform.runLater(() -> {
                    try {
                        InventarioFX.mostrarMenu(mainStage);
                    } catch (Exception ex) {
                        AppLogger.getLogger(SplashScreen.class).error("Error al mostrar ventana principal: " + ex.getMessage(), ex);
                    }
                });
            });
            
            exit.play();
        });
        
        pause.play();
    }
    
    /**
     * Cierra el splash screen inmediatamente
     */
    public void close() {
        if (splashStage != null && splashStage.isShowing()) {
            splashStage.close();
        }
    }
}