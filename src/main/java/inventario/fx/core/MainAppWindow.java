package inventario.fx.core;
import inventario.fx.model.TemaManager;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import inventario.fx.util.AnimacionesFX;

/**
 * Ventana principal de la aplicación SELCOMP Inventario PC.
 * Se muestra después del splash screen con una transición suave.
 */
public class MainAppWindow {
    
    private Stage primaryStage;
    private BorderPane root;
    
    /**
     * Constructor que inicializa la ventana principal
     */
    public MainAppWindow() {
        initializeMainWindow();
    }
    
    /**
     * Inicializa los componentes de la ventana principal
     */
    private void initializeMainWindow() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + TemaManager.getBg() + ";");
        
        setupHeader();
        setupCenter();
        setupStatusBar();
    }
    
    /**
     * Configura el header de la aplicación
     */
    private void setupHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        // Título principal
        Label titleLabel = new Label("SELCOMP - INVENTARIO PC");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(TemaManager.COLOR_PRIMARY));
        
        // Subtítulo
        Label subtitleLabel = new Label("Sistema de Gestión de Inventario");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(javafx.scene.paint.Color.web(TemaManager.getTextSecondary()));
        
        VBox titleContainer = new VBox(5);
        titleContainer.getChildren().addAll(titleLabel, subtitleLabel);
        
        header.getChildren().add(titleContainer);
        root.setTop(header);
    }
    
    /**
     * Configura el área central con los controles principales
     */
    private void setupCenter() {
        VBox centerContainer = new VBox(30);
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.setPadding(new Insets(50));
        
        // Mensaje de bienvenida
        Label welcomeLabel = new Label("¡Bienvenido al Sistema de Inventario!");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 20));
        welcomeLabel.setTextFill(javafx.scene.paint.Color.web(TemaManager.getText()));
        
        Label descriptionLabel = new Label("Gestiona y controla el inventario de equipos de manera eficiente");
        descriptionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        descriptionLabel.setTextFill(javafx.scene.paint.Color.web(TemaManager.getTextSecondary()));
        
        // Botones principales
        HBox buttonsContainer = new HBox(20);
        buttonsContainer.setAlignment(Pos.CENTER);
        
        Button btnNuevoInventario = createStyledButton("Nuevo Inventario", true);
        Button btnAbrirExistente = createStyledButton("Abrir Existente", false);
        Button btnConfiguracion = createStyledButton("Configuración", false);
        
        // Eventos de botones - conectar con la funcionalidad existente
        btnNuevoInventario.setOnAction(e -> {
            Platform.runLater(() -> {
                try {
                    primaryStage.close();
                    // Conectar con la ventana principal existente de InventarioFX
                    InventarioFX.mostrarMenu(new Stage());
                } catch (Exception ex) {
                    System.err.println("Error al abrir inventario: " + ex.getMessage());
                }
            });
        });
        
        btnAbrirExistente.setOnAction(e -> {
            Platform.runLater(() -> {
                try {
                    primaryStage.close();
                    InventarioFX.mostrarMenu(new Stage());
                } catch (Exception ex) {
                    System.err.println("Error al abrir inventario existente: " + ex.getMessage());
                }
            });
        });
        
        btnConfiguracion.setOnAction(e -> {
            System.out.println("Abriendo configuración...");
        });
        
        buttonsContainer.getChildren().addAll(btnNuevoInventario, btnAbrirExistente, btnConfiguracion);
        
        centerContainer.getChildren().addAll(welcomeLabel, descriptionLabel, buttonsContainer);
        root.setCenter(centerContainer);
    }
    
    /**
     * Crea un botón estilizado según el tema actual
     */
    private Button createStyledButton(String text, boolean isPrimary) {
        Button button = new Button(text);
        button.setPrefWidth(160);
        button.setPrefHeight(45);
        button.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        if (isPrimary) {
            // Botón primario
            button.setStyle(
                "-fx-background-color: " + TemaManager.COLOR_PRIMARY + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(230, 57, 70, 0.3), 8, 0, 0, 2);"
            );
            
            // Efectos hover
            button.setOnMouseEntered(e -> {
                button.setStyle(
                    "-fx-background-color: " + TemaManager.COLOR_PRIMARY_HOVER + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(230, 57, 70, 0.4), 12, 0, 0, 4);"
                );
                AnimacionesFX.hoverIn(button, 1.05, 150);
            });
            
            button.setOnMouseExited(e -> {
                button.setStyle(
                    "-fx-background-color: " + TemaManager.COLOR_PRIMARY + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(230, 57, 70, 0.3), 8, 0, 0, 2);"
                );
                AnimacionesFX.hoverOut(button, 150);
            });
        } else {
            // Botón secundario
            String bgColor = TemaManager.getSurface();
            String textColor = TemaManager.getText();
            String borderColor = TemaManager.getBorder();
            
            button.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: " + textColor + ";" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-width: 1;" +
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-cursor: hand;"
            );
            
            // Efectos hover
            button.setOnMouseEntered(e -> {
                String hoverBg = TemaManager.getSurfaceHover();
                button.setStyle(
                    "-fx-background-color: " + hoverBg + ";" +
                    "-fx-text-fill: " + textColor + ";" +
                    "-fx-border-color: " + TemaManager.COLOR_PRIMARY + ";" +
                    "-fx-border-width: 1;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-cursor: hand;"
                );
                AnimacionesFX.hoverIn(button, 1.05, 150);
            });
            
            button.setOnMouseExited(e -> {
                button.setStyle(
                    "-fx-background-color: " + bgColor + ";" +
                    "-fx-text-fill: " + textColor + ";" +
                    "-fx-border-color: " + borderColor + ";" +
                    "-fx-border-width: 1;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-cursor: hand;"
                );
                AnimacionesFX.hoverOut(button, 150);
            });
        }
        
        return button;
    }
    
    /**
     * Configura la barra de estado en la parte inferior
     */
    private void setupStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(10, 20, 10, 20));
        statusBar.setStyle(
            "-fx-background-color: " + TemaManager.getBgLight() + ";" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-width: 1 0 0 0;"
        );
        
        Label statusLabel = new Label("Sistema iniciado correctamente");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(javafx.scene.paint.Color.web(TemaManager.getTextSecondary()));
        
        statusBar.getChildren().add(statusLabel);
        root.setBottom(statusBar);
    }
    
    /**
     * Muestra la ventana principal con animación de fade-in
     */
    public void show(Stage stage) {
        this.primaryStage = stage;
        
        // Configurar stage
        stage.setTitle("SELCOMP - INVENTARIO PC");
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        // Aplicar estilos según el tema
        Scene scene = new Scene(root);
        String themeCSS = TemaManager.isDarkMode() ? 
            "/styles/inventario-dark.css" : "/styles/inventario-light.css";
        
        try {
            scene.getStylesheets().add(getClass().getResource(themeCSS).toExternalForm());
        } catch (Exception e) {
            System.err.println("No se pudo cargar el CSS del tema: " + e.getMessage());
        }
        
        stage.setScene(scene);
        stage.centerOnScreen();
        
        // Animación de entrada con fade-in
        root.setOpacity(0.0);
        stage.show();
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    /**
     * Cierra la ventana principal
     */
    public void close() {
        if (primaryStage != null) {
            primaryStage.close();
        }
    }
}