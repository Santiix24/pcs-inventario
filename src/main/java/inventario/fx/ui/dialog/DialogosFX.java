package inventario.fx.ui.dialog;
import inventario.fx.model.InventarioFXBase;
import inventario.fx.model.TemaManager;
import inventario.fx.icons.IconosSVG;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicReference;

import inventario.fx.util.ComponentesFX;

/**
 * Diálogos elegantes y minimalistas para la aplicación.
 * Diseño premium con animaciones suaves y tema oscuro.
 */
public class DialogosFX extends InventarioFXBase {

    // Colores del tema - Usando TemaManager para soporte light/dark
    private static String COLOR_BG_DARK() { return TemaManager.getBgDark(); }
    private static String COLOR_BG() { return TemaManager.getBg(); }
    private static String COLOR_BG_LIGHT() { return TemaManager.getBgLight(); }
    private static String COLOR_BORDER() { return TemaManager.getBorder(); }
    private static final String COLOR_PRIMARY = TemaManager.COLOR_PRIMARY;
    private static final String COLOR_SUCCESS = TemaManager.COLOR_SUCCESS;
    private static String COLOR_TEXT() { return TemaManager.getText(); }
    private static String COLOR_TEXT_SECONDARY() { return TemaManager.getTextSecondary(); }
    private static String COLOR_TEXT_MUTED() { return TemaManager.getTextMuted(); }

    // ════════════════════════════════════════════════════════════════════════════
    // SELECTOR DE PROYECTOS (SIN OPCIÓN DE NUEVO)
    // ════════════════════════════════════════════════════════════════════════════
    
    public static String seleccionarProyectoDialog(Stage owner) {
        return seleccionarProyecto(owner);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SELECTOR DE PROYECTOS (CON OPCIÓN DE NUEVO)
    // ════════════════════════════════════════════════════════════════════════════
    
    public static String seleccionarProyectoDialogAllowNew(Stage owner) {
        return seleccionarProyecto(owner);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SELECTOR DE PROYECTOS ELEGANTE
    // ════════════════════════════════════════════════════════════════════════════
    
    public static String seleccionarProyecto(Stage owner) {
        AtomicReference<String> resultado = new AtomicReference<>(null);
        
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.setTitle("Seleccionar Proyecto");
        dialog.initOwner(owner);
        
        // Variables para arrastrar
        final double[] dragOffset = {0, 0};
        
        // Wrapper transparente para sombra
        StackPane rootWrapper = new StackPane();
        rootWrapper.setStyle("-fx-background-color: transparent;");
        rootWrapper.setPadding(new Insets(30));
        
        // Card principal
        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 44, 36, 44));
        card.setMaxWidth(450);
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;"
        );
        
        // Header (también sirve para arrastrar)
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setCursor(javafx.scene.Cursor.MOVE);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        // Permitir arrastrar desde el header
        header.setOnMousePressed(e -> {
            dragOffset[0] = e.getSceneX();
            dragOffset[1] = e.getSceneY();
        });
        header.setOnMouseDragged(e -> {
            dialog.setX(e.getScreenX() - dragOffset[0]);
            dialog.setY(e.getScreenY() - dragOffset[1]);
        });
        
        Label titulo = new Label("Seleccionar Proyecto");
        titulo.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 26));
        titulo.setTextFill(Color.web(COLOR_TEXT()));
        
        Label subtitulo = new Label("Elige un proyecto para continuar");
        subtitulo.setFont(Font.font("Segoe UI", 13));
        subtitulo.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        
        header.getChildren().addAll(titulo, subtitulo);
        
        // Lista de proyectos
        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(280);
        listView.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;"
        );
        
        // Cargar proyectos
        listView.getItems().addAll(PROYECTOS_NOMBRE);
        
        // Estilizar celdas
        listView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox cellBox = new HBox(12);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    cellBox.setPadding(new Insets(8, 16, 8, 16));
                    
                    // Avatar con inicial usando ComponentesFX
                    StackPane avatar = ComponentesFX.crearAvatar(
                        item.substring(0, 1).toUpperCase(), COLOR_PRIMARY, "32"
                    );
                    
                    // Nombre del proyecto
                    Label nombre = new Label(item);
                    nombre.setFont(Font.font("Segoe UI", 14));
                    nombre.setTextFill(Color.web(COLOR_TEXT()));
                    
                    cellBox.getChildren().addAll(avatar, nombre);
                    setGraphic(cellBox);
                    
                    // Estilos via clases CSS
                    getStyleClass().add("list-cell-hoverable");
                    setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 4;"
                    );
                }
            }
        });
        
        // Botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER);
        
        Button btnCancelar = crearBotonSecundario("Cancelar", 120);
        Button btnSeleccionar = crearBotonPrimario("Seleccionar", 140);
        btnSeleccionar.setDisable(true);
        
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            btnSeleccionar.setDisable(nuevo == null);
        });
        
        btnCancelar.setOnAction(e -> dialog.close());
        btnSeleccionar.setOnAction(e -> {
            resultado.set(listView.getSelectionModel().getSelectedItem());
            dialog.close();
        });
        
        // Doble clic para seleccionar
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                resultado.set(listView.getSelectionModel().getSelectedItem());
                dialog.close();
            }
        });
        
        botones.getChildren().addAll(btnCancelar, btnSeleccionar);
        
        card.getChildren().addAll(header, listView, botones);
        
        // Sombra elegante
        DropShadow shadow = new DropShadow();
        shadow.setRadius(60);
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setOffsetY(8);
        card.setEffect(shadow);
        
        rootWrapper.getChildren().add(card);
        
        Scene scene = new Scene(rootWrapper, 560, 540);
        scene.setFill(Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        
        dialog.showAndWait();
        return resultado.get();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // DIÁLOGO DE ENTRADA DE TEXTO
    // ════════════════════════════════════════════════════════════════════════════
    
    public static String pedirTexto(Stage owner, String titulo, String mensaje) {
        AtomicReference<String> resultado = new AtomicReference<>(null);
        
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.setTitle(titulo);
        dialog.initOwner(owner);
        
        // Variables para arrastrar
        final double[] dragOffset = {0, 0};
        
        // Root transparente como wrapper
        StackPane rootWrapper = new StackPane();
        rootWrapper.setStyle("-fx-background-color: transparent;");
        rootWrapper.setPadding(new Insets(30));
        
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 44, 36, 44));
        card.setMaxWidth(420);
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;"
        );
        
        // Permitir arrastrar la ventana
        card.setOnMousePressed(e -> {
            dragOffset[0] = e.getSceneX();
            dragOffset[1] = e.getSceneY();
        });
        card.setOnMouseDragged(e -> {
            dialog.setX(e.getScreenX() - dragOffset[0]);
            dialog.setY(e.getScreenY() - dragOffset[1]);
        });
        
        // Icono centrado correctamente
        StackPane iconContainer = new StackPane();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setPrefSize(60, 60);
        iconContainer.setMinSize(60, 60);
        iconContainer.setMaxSize(60, 60);
        Circle iconBg = new Circle(30);
        iconBg.setFill(Color.web(COLOR_PRIMARY + "25"));
        javafx.scene.Node iconoEditar = IconosSVG.editar(COLOR_PRIMARY, 28);
        StackPane.setAlignment(iconBg, Pos.CENTER);
        iconContainer.getChildren().addAll(iconBg, iconoEditar);
        
        // Título
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 24));
        lblTitulo.setTextFill(Color.web(COLOR_TEXT()));
        
        // Mensaje
        Label lblMensaje = new Label(mensaje);
        lblMensaje.setFont(Font.font("Segoe UI", 13));
        lblMensaje.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        lblMensaje.setWrapText(true);
        
        // Campo de texto
        TextField textField = new TextField();
        textField.setPromptText("Ingresa el texto...");
        textField.setPrefHeight(44);
        textField.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-text-fill: " + COLOR_TEXT() + ";" +
            "-fx-prompt-text-fill: " + COLOR_TEXT_MUTED() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 10 16;" +
            "-fx-font-size: 14px;"
        );
        
        // Botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER);
        
        Button btnCancelar = crearBotonSecundario("Cancelar", 110);
        Button btnAceptar = crearBotonPrimario("Aceptar", 110);
        
        btnCancelar.setOnAction(e -> dialog.close());
        btnAceptar.setOnAction(e -> {
            String texto = textField.getText().trim();
            if (!texto.isEmpty()) {
                resultado.set(texto);
            }
            dialog.close();
        });
        
        textField.setOnAction(e -> btnAceptar.fire());
        
        botones.getChildren().addAll(btnCancelar, btnAceptar);
        
        card.getChildren().addAll(iconContainer, lblTitulo, lblMensaje, textField, botones);
        
        // Sombra elegante
        DropShadow shadow = new DropShadow();
        shadow.setRadius(60);
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setOffsetY(8);
        card.setEffect(shadow);
        
        rootWrapper.getChildren().add(card);
        
        Scene scene = new Scene(rootWrapper, 510, 400);
        scene.setFill(Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        
        dialog.setOnShown(e -> {
            textField.requestFocus();
            inventario.fx.util.AnimacionesFX.animarEntradaDialogo(card);
        });
        dialog.showAndWait();
        return resultado.get();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // ALERTAS ELEGANTES CON ANIMACIONES PREMIUM
    // ════════════════════════════════════════════════════════════════════════════
    
    public static void mostrarAlerta(Stage owner, String titulo, String mensaje, Alert.AlertType tipo) {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.setTitle(titulo);
        dialog.initOwner(owner);
        
        // Variables para arrastrar
        final double[] dragOffset = {0, 0};
        
        // Root transparente como wrapper
        StackPane rootWrapper = new StackPane();
        rootWrapper.setStyle("-fx-background-color: transparent;");
        rootWrapper.setPadding(new Insets(30));
        
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 44, 36, 44));
        card.setMaxWidth(420);
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;"
        );
        
        // Permitir arrastrar la ventana
        card.setOnMousePressed(e -> {
            dragOffset[0] = e.getSceneX();
            dragOffset[1] = e.getSceneY();
        });
        card.setOnMouseDragged(e -> {
            dialog.setX(e.getScreenX() - dragOffset[0]);
            dialog.setY(e.getScreenY() - dragOffset[1]);
        });
        
        // Determinar color e icono según contexto
        String color = COLOR_PRIMARY;
        String iconoTipo = determinarTipoIcono(titulo, mensaje, tipo);
        
        switch (tipo) {
            case INFORMATION:
                color = TemaManager.COLOR_INFO;
                break;
            case WARNING:
                color = TemaManager.COLOR_WARNING;
                break;
            case ERROR:
                color = COLOR_PRIMARY;
                break;
            case CONFIRMATION:
                color = COLOR_SUCCESS;
                break;
            default:
                break;
        }
        
        final String finalColor = color;
        
        // ═══════════════════════════════════════════════════════════════════
        // CONTENEDOR DE ICONO ANIMADO - MÁS GRANDE Y PROMINENTE
        // ═══════════════════════════════════════════════════════════════════
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(100, 100);
        
        // Círculos de fondo con animación - más grandes y visibles
        Circle outerRing = new Circle(50);
        outerRing.setFill(Color.TRANSPARENT);
        outerRing.setStroke(Color.web(color + "50"));
        outerRing.setStrokeWidth(3);
        
        Circle middleRing = new Circle(42);
        middleRing.setFill(Color.web(color + "20"));
        
        Circle innerCircle = new Circle(34);
        innerCircle.setFill(Color.web(color + "35"));
        
        // Crear icono animado según contexto
        StackPane iconShape = crearIconoContextual(iconoTipo, color);
        
        iconContainer.getChildren().addAll(outerRing, middleRing, innerCircle, iconShape);
        
        // ═══════════════════════════════════════════════════════════════════
        // ANIMACIÓN DEL ICONO
        // ═══════════════════════════════════════════════════════════════════
        
        // Animación de entrada del contenedor
        ScaleTransition iconPop = new ScaleTransition(Duration.millis(400), iconContainer);
        iconPop.setFromX(0);
        iconPop.setFromY(0);
        iconPop.setToX(1);
        iconPop.setToY(1);
        iconPop.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
        
        // Pulso suave del anillo exterior
        ScaleTransition ringPulse = new ScaleTransition(Duration.seconds(1.5), outerRing);
        ringPulse.setFromX(1);
        ringPulse.setFromY(1);
        ringPulse.setToX(1.15);
        ringPulse.setToY(1.15);
        ringPulse.setCycleCount(Animation.INDEFINITE);
        ringPulse.setAutoReverse(true);
        ringPulse.setInterpolator(Interpolator.EASE_BOTH);
        
        FadeTransition ringFade = new FadeTransition(Duration.seconds(1.5), outerRing);
        ringFade.setFromValue(1);
        ringFade.setToValue(0.4);
        ringFade.setCycleCount(Animation.INDEFINITE);
        ringFade.setAutoReverse(true);
        
        // Título
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 22));
        lblTitulo.setTextFill(Color.web(COLOR_TEXT()));
        
        // Mensaje
        Label lblMensaje = new Label(mensaje);
        lblMensaje.setFont(Font.font("Segoe UI", 14));
        lblMensaje.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(340);
        lblMensaje.setAlignment(Pos.CENTER);
        
        // Botón
        Button btnCerrar = new Button("Entendido");
        btnCerrar.setPrefWidth(150);
        btnCerrar.setPrefHeight(46);
        btnCerrar.setStyle(
            "-fx-background-color: " + finalColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 14;" +
            "-fx-cursor: hand;"
        );
        btnCerrar.setOnAction(e -> {
            // Animación de salida
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), card);
            fadeOut.setToValue(0);
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), card);
            scaleOut.setToX(0.9);
            scaleOut.setToY(0.9);
            ParallelTransition exitAnim = new ParallelTransition(fadeOut, scaleOut);
            exitAnim.setOnFinished(ev -> dialog.close());
            exitAnim.play();
        });
        
        // Hover del botón
        btnCerrar.setOnMouseEntered(e -> {
            btnCerrar.setStyle(
                "-fx-background-color: derive(" + finalColor + ", -10%);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition hover = new ScaleTransition(Duration.millis(100), btnCerrar);
            hover.setToX(1.02);
            hover.setToY(1.02);
            hover.play();
        });
        btnCerrar.setOnMouseExited(e -> {
            btnCerrar.setStyle(
                "-fx-background-color: " + finalColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition hover = new ScaleTransition(Duration.millis(100), btnCerrar);
            hover.setToX(1);
            hover.setToY(1);
            hover.play();
        });
        
        card.getChildren().addAll(iconContainer, lblTitulo, lblMensaje, btnCerrar);
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(60);
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setOffsetY(8);
        card.setEffect(shadow);
        
        // Preparar animación de entrada
        card.setOpacity(0);
        card.setScaleX(0.7);
        card.setScaleY(0.7);
        card.setTranslateY(20);
        
        rootWrapper.getChildren().add(card);
        
        Scene scene = new Scene(rootWrapper, 480, 400);
        scene.setFill(Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        
        // Variable final para usar en lambda
        final String finalIconoTipo = iconoTipo;
        
        // Ejecutar animaciones al mostrar
        dialog.setOnShown(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), card);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), card);
            scaleIn.setFromX(0.7);
            scaleIn.setFromY(0.7);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_BACK);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), card);
            slideIn.setToY(0);
            slideIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ParallelTransition entryAnim = new ParallelTransition(fadeIn, scaleIn, slideIn);
            entryAnim.setOnFinished(ev -> {
                iconPop.play();
                ringPulse.play();
                ringFade.play();
                
                // Animar el icono interior
                animarIconoContextual(iconShape, finalIconoTipo);
            });
            entryAnim.play();
        });
        
        dialog.showAndWait();
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // DETERMINAR TIPO DE ICONO SEGÚN CONTEXTO
    // ════════════════════════════════════════════════════════════════════════════
    
    private static String determinarTipoIcono(String titulo, String mensaje, Alert.AlertType tipo) {
        String tituloLower = titulo.toLowerCase();
        String mensajeLower = mensaje.toLowerCase();
        
        // Iconos específicos según el contexto del mensaje
        if (tituloLower.contains("sin datos") || tituloLower.contains("vacío") || tituloLower.contains("vacio")) {
            return "EMPTY_FOLDER";
        }
        if (tituloLower.contains("no encontrado") || mensajeLower.contains("no existe")) {
            return "SEARCH_NOT_FOUND";
        }
        if (tituloLower.contains("eliminado") || mensajeLower.contains("eliminado")) {
            return "TRASH_SUCCESS";
        }
        if (tituloLower.contains("exporta") || mensajeLower.contains("exporta")) {
            return "EXPORT_SUCCESS";
        }
        if (tituloLower.contains("generado") || tituloLower.contains("guardado")) {
            return "FILE_SUCCESS";
        }
        if (tipo == Alert.AlertType.ERROR) {
            return "ERROR";
        }
        if (tipo == Alert.AlertType.WARNING) {
            return "WARNING";
        }
        if (tipo == Alert.AlertType.CONFIRMATION) {
            return "SUCCESS";
        }
        return "INFO";
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // CREAR ICONO CONTEXTUAL - ICONOS ESPECÍFICOS PARA CADA SITUACIÓN
    // ════════════════════════════════════════════════════════════════════════════
    
    private static StackPane crearIconoContextual(String iconoTipo, String color) {
        StackPane container = new StackPane();
        container.setPrefSize(40, 40);
        container.setAlignment(Pos.CENTER);
        
        javafx.scene.Node icon;
        
        switch (iconoTipo) {
            case "EMPTY_FOLDER":
                // Carpeta vacía - Lucide folder
                icon = IconosSVG.carpeta(color, 36);
                break;
                
            case "SEARCH_NOT_FOUND":
                // Lupa con X - búsqueda sin resultados
                javafx.scene.shape.SVGPath iconPath = new javafx.scene.shape.SVGPath();
                iconPath.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14zm-2.5-4h5v1h-5z");
                iconPath.setScaleX(1.7);
                iconPath.setScaleY(1.7);
                iconPath.setFill(Color.web(color));
                icon = iconPath;
                break;
                
            case "TRASH_SUCCESS":
                // Papelera - Lucide trash-2
                icon = IconosSVG.eliminar(color, 36);
                break;
                
            case "EXPORT_SUCCESS":
                // Icono de exportación/descarga exitosa
                javafx.scene.shape.SVGPath exportPath = new javafx.scene.shape.SVGPath();
                exportPath.setContent("M19 12v7H5v-7H3v7c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-7h-2zm-6 .67l2.59-2.58L17 11.5l-5 5-5-5 1.41-1.41L11 12.67V3h2v9.67z");
                exportPath.setScaleX(1.6);
                exportPath.setScaleY(1.6);
                exportPath.setFill(Color.web(color));
                icon = exportPath;
                break;
                
            case "FILE_SUCCESS":
                // Documento con check
                javafx.scene.shape.SVGPath filePath = new javafx.scene.shape.SVGPath();
                filePath.setContent("M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm-2 16l-4-4 1.41-1.41L12 15.17l4.59-4.59L18 12l-6 6zm0-8V3.5L17.5 9H12z");
                filePath.setScaleX(1.6);
                filePath.setScaleY(1.6);
                filePath.setFill(Color.web(color));
                icon = filePath;
                break;
                
            case "SUCCESS":
                // Checkmark
                javafx.scene.shape.SVGPath checkPath = new javafx.scene.shape.SVGPath();
                checkPath.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
                checkPath.setScaleX(1.8);
                checkPath.setScaleY(1.8);
                checkPath.setFill(Color.web(color));
                icon = checkPath;
                break;
                
            case "WARNING":
                // Triángulo de advertencia
                javafx.scene.shape.SVGPath warnPath = new javafx.scene.shape.SVGPath();
                warnPath.setContent("M12 5.99L19.53 19H4.47L12 5.99M12 2L1 21h22L12 2zm1 14h-2v2h2v-2zm0-6h-2v4h2v-4z");
                warnPath.setScaleX(1.6);
                warnPath.setScaleY(1.6);
                warnPath.setFill(Color.web(color));
                icon = warnPath;
                break;
                
            case "ERROR":
                // X de error
                javafx.scene.shape.SVGPath errorPath = new javafx.scene.shape.SVGPath();
                errorPath.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
                errorPath.setScaleX(1.8);
                errorPath.setScaleY(1.8);
                errorPath.setFill(Color.web(color));
                icon = errorPath;
                break;
                
            case "INFO":
            default:
                // Icono de información
                javafx.scene.shape.SVGPath infoPath = new javafx.scene.shape.SVGPath();
                infoPath.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
                infoPath.setScaleX(1.6);
                infoPath.setScaleY(1.6);
                infoPath.setFill(Color.web(color));
                icon = infoPath;
                break;
        }
        
        icon.setOpacity(0);
        
        // Agregar efecto de brillo/glow para que destaque más
        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
        glow.setColor(Color.web(color));
        glow.setRadius(15);
        glow.setSpread(0.4);
        icon.setEffect(glow);
        
        container.getChildren().add(icon);
        
        return container;
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // ANIMAR ICONO CONTEXTUAL
    // ════════════════════════════════════════════════════════════════════════════
    
    private static void animarIconoContextual(StackPane iconPane, String iconoTipo) {
        if (iconPane.getChildren().isEmpty()) return;
        
        javafx.scene.Node icon = iconPane.getChildren().get(0);
        
        switch (iconoTipo) {
            case "EMPTY_FOLDER":
            case "SEARCH_NOT_FOUND":
                // Animación suave con ligero rebote
                FadeTransition emptyFade = new FadeTransition(Duration.millis(300), icon);
                emptyFade.setFromValue(0);
                emptyFade.setToValue(1);
                
                ScaleTransition emptyScale = new ScaleTransition(Duration.millis(500), icon);
                emptyScale.setFromX(0.3);
                emptyScale.setFromY(0.3);
                emptyScale.setToX(1.6);
                emptyScale.setToY(1.6);
                emptyScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                // Ligera inclinación
                RotateTransition emptyRotate = new RotateTransition(Duration.millis(400), icon);
                emptyRotate.setFromAngle(-10);
                emptyRotate.setToAngle(0);
                
                new ParallelTransition(emptyFade, emptyScale, emptyRotate).play();
                break;
                
            case "TRASH_SUCCESS":
            case "EXPORT_SUCCESS":
            case "FILE_SUCCESS":
            case "SUCCESS":
                // Animación de éxito con checkmark
                FadeTransition successFade = new FadeTransition(Duration.millis(200), icon);
                successFade.setFromValue(0);
                successFade.setToValue(1);
                
                ScaleTransition successScale = new ScaleTransition(Duration.millis(400), icon);
                successScale.setFromX(0.5);
                successScale.setFromY(0.5);
                double targetScale = iconoTipo.equals("SUCCESS") ? 1.8 : 1.6;
                successScale.setToX(targetScale);
                successScale.setToY(targetScale);
                successScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                RotateTransition successRotate = new RotateTransition(Duration.millis(400), icon);
                successRotate.setFromAngle(-20);
                successRotate.setToAngle(0);
                successRotate.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                new ParallelTransition(successFade, successScale, successRotate).play();
                break;
                
            case "WARNING":
                // Animación de sacudida para warning
                FadeTransition warnFade = new FadeTransition(Duration.millis(300), icon);
                warnFade.setFromValue(0);
                warnFade.setToValue(1);
                
                ScaleTransition warnScale = new ScaleTransition(Duration.millis(300), icon);
                warnScale.setFromX(0.3);
                warnScale.setFromY(0.3);
                warnScale.setToX(1.6);
                warnScale.setToY(1.6);
                warnScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                Timeline shake = new Timeline(
                    new KeyFrame(Duration.millis(0), new KeyValue(icon.translateXProperty(), 0)),
                    new KeyFrame(Duration.millis(100), new KeyValue(icon.translateXProperty(), 5)),
                    new KeyFrame(Duration.millis(200), new KeyValue(icon.translateXProperty(), -5)),
                    new KeyFrame(Duration.millis(300), new KeyValue(icon.translateXProperty(), 3)),
                    new KeyFrame(Duration.millis(400), new KeyValue(icon.translateXProperty(), -3)),
                    new KeyFrame(Duration.millis(500), new KeyValue(icon.translateXProperty(), 0))
                );
                
                ParallelTransition warnAnim = new ParallelTransition(warnFade, warnScale);
                warnAnim.setOnFinished(e -> shake.play());
                warnAnim.play();
                break;
                
            case "ERROR":
                // Animación de error con rotación
                FadeTransition errFade = new FadeTransition(Duration.millis(200), icon);
                errFade.setFromValue(0);
                errFade.setToValue(1);
                
                ScaleTransition errScale = new ScaleTransition(Duration.millis(400), icon);
                errScale.setFromX(0);
                errScale.setFromY(0);
                errScale.setToX(1.8);
                errScale.setToY(1.8);
                errScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                RotateTransition errRotate = new RotateTransition(Duration.millis(400), icon);
                errRotate.setFromAngle(90);
                errRotate.setToAngle(0);
                errRotate.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                new ParallelTransition(errFade, errScale, errRotate).play();
                break;
                
            default:
                // Animación por defecto
                FadeTransition defFade = new FadeTransition(Duration.millis(300), icon);
                defFade.setFromValue(0);
                defFade.setToValue(1);
                
                ScaleTransition defScale = new ScaleTransition(Duration.millis(300), icon);
                defScale.setFromX(0.5);
                defScale.setFromY(0.5);
                defScale.setToX(1.6);
                defScale.setToY(1.6);
                
                new ParallelTransition(defFade, defScale).play();
                break;
        }
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // CREAR ICONO ANIMADO SEGÚN TIPO (LEGACY - mantener para compatibilidad)
    
    private static StackPane crearIconoAnimado(Alert.AlertType tipo, String color) {
        StackPane container = new StackPane();
        container.setPrefSize(50, 50);
        container.setAlignment(Pos.CENTER);
        
        switch (tipo) {
            case CONFIRMATION:
                // Checkmark (chulito) animado - solo para confirmaciones exitosas
                javafx.scene.shape.SVGPath checkPath = new javafx.scene.shape.SVGPath();
                checkPath.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
                checkPath.setFill(Color.web(color));
                checkPath.setScaleX(1.8);
                checkPath.setScaleY(1.8);
                checkPath.setOpacity(0);
                container.getChildren().add(checkPath);
                break;
            
            case INFORMATION:
                // Icono de información (i) - para mensajes informativos
                javafx.scene.shape.SVGPath infoPath = new javafx.scene.shape.SVGPath();
                infoPath.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
                infoPath.setFill(Color.web(color));
                infoPath.setScaleX(1.6);
                infoPath.setScaleY(1.6);
                infoPath.setOpacity(0);
                container.getChildren().add(infoPath);
                break;
                
            case WARNING:
                // Exclamación animada
                javafx.scene.shape.SVGPath warningPath = new javafx.scene.shape.SVGPath();
                warningPath.setContent("M12 5.99L19.53 19H4.47L12 5.99M12 2L1 21h22L12 2zm1 14h-2v2h2v-2zm0-6h-2v4h2v-4z");
                warningPath.setFill(Color.web(color));
                warningPath.setScaleX(1.6);
                warningPath.setScaleY(1.6);
                warningPath.setOpacity(0);
                container.getChildren().add(warningPath);
                break;
                
            case ERROR:
                // X (error) animado
                javafx.scene.shape.SVGPath errorPath = new javafx.scene.shape.SVGPath();
                errorPath.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
                errorPath.setFill(Color.web(color));
                errorPath.setScaleX(1.8);
                errorPath.setScaleY(1.8);
                errorPath.setOpacity(0);
                container.getChildren().add(errorPath);
                break;
                
            default:
                // Icono por defecto - información
                javafx.scene.shape.SVGPath defaultPath = new javafx.scene.shape.SVGPath();
                defaultPath.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
                defaultPath.setFill(Color.web(color));
                defaultPath.setScaleX(1.6);
                defaultPath.setScaleY(1.6);
                defaultPath.setOpacity(0);
                container.getChildren().add(defaultPath);
                break;
        }
        
        return container;
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // ANIMAR ICONO INTERIOR
    // ════════════════════════════════════════════════════════════════════════════
    
    private static void animarIconoInterior(StackPane iconPane, Alert.AlertType tipo) {
        if (iconPane.getChildren().isEmpty()) return;
        
        javafx.scene.Node icon = iconPane.getChildren().get(0);
        
        switch (tipo) {
            case CONFIRMATION:
                // Animación especial para el checkmark - efecto de dibujo con rotación
                FadeTransition checkFade = new FadeTransition(Duration.millis(200), icon);
                checkFade.setFromValue(0);
                checkFade.setToValue(1);
                
                ScaleTransition checkScale = new ScaleTransition(Duration.millis(400), icon);
                checkScale.setFromX(0.5);
                checkScale.setFromY(0.5);
                checkScale.setToX(1.8);
                checkScale.setToY(1.8);
                checkScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                // Rotación sutil de entrada
                RotateTransition checkRotate = new RotateTransition(Duration.millis(400), icon);
                checkRotate.setFromAngle(-30);
                checkRotate.setToAngle(0);
                checkRotate.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                new ParallelTransition(checkFade, checkScale, checkRotate).play();
                break;
            
            case INFORMATION:
                // Animación suave para información - aparición con pulso
                FadeTransition infoFade = new FadeTransition(Duration.millis(300), icon);
                infoFade.setFromValue(0);
                infoFade.setToValue(1);
                
                ScaleTransition infoScale = new ScaleTransition(Duration.millis(400), icon);
                infoScale.setFromX(0.3);
                infoScale.setFromY(0.3);
                infoScale.setToX(1.6);
                infoScale.setToY(1.6);
                infoScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                new ParallelTransition(infoFade, infoScale).play();
                break;
                
            case WARNING:
                // Animación de sacudida para warning
                FadeTransition warnFade = new FadeTransition(Duration.millis(300), icon);
                warnFade.setFromValue(0);
                warnFade.setToValue(1);
                
                ScaleTransition warnScale = new ScaleTransition(Duration.millis(300), icon);
                warnScale.setFromX(0.3);
                warnScale.setFromY(0.3);
                warnScale.setToX(1.6);
                warnScale.setToY(1.6);
                warnScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                // Shake animation
                Timeline shake = new Timeline(
                    new KeyFrame(Duration.millis(0), new KeyValue(icon.translateXProperty(), 0)),
                    new KeyFrame(Duration.millis(100), new KeyValue(icon.translateXProperty(), 5)),
                    new KeyFrame(Duration.millis(200), new KeyValue(icon.translateXProperty(), -5)),
                    new KeyFrame(Duration.millis(300), new KeyValue(icon.translateXProperty(), 3)),
                    new KeyFrame(Duration.millis(400), new KeyValue(icon.translateXProperty(), -3)),
                    new KeyFrame(Duration.millis(500), new KeyValue(icon.translateXProperty(), 0))
                );
                
                ParallelTransition warnAnim = new ParallelTransition(warnFade, warnScale);
                warnAnim.setOnFinished(e -> shake.play());
                warnAnim.play();
                break;
                
            case ERROR:
                // Animación de error - aparición con rebote
                FadeTransition errFade = new FadeTransition(Duration.millis(200), icon);
                errFade.setFromValue(0);
                errFade.setToValue(1);
                
                ScaleTransition errScale = new ScaleTransition(Duration.millis(400), icon);
                errScale.setFromX(0);
                errScale.setFromY(0);
                errScale.setToX(1.8);
                errScale.setToY(1.8);
                errScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                // Rotación para la X
                RotateTransition errRotate = new RotateTransition(Duration.millis(400), icon);
                errRotate.setFromAngle(90);
                errRotate.setToAngle(0);
                errRotate.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                new ParallelTransition(errFade, errScale, errRotate).play();
                break;
                
            default:
                FadeTransition defFade = new FadeTransition(Duration.millis(300), icon);
                defFade.setFromValue(0);
                defFade.setToValue(1);
                
                ScaleTransition defScale = new ScaleTransition(Duration.millis(300), icon);
                defScale.setFromX(0.5);
                defScale.setFromY(0.5);
                defScale.setToX(1.6);
                defScale.setToY(1.6);
                
                new ParallelTransition(defFade, defScale).play();
                break;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // DIÁLOGO DE CONFIRMACIÓN
    // ════════════════════════════════════════════════════════════════════════════
    
    public static boolean confirmarAccion(Stage owner, String titulo, String mensaje) {
        AtomicReference<Boolean> resultado = new AtomicReference<>(false);
        
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        
        // Root sin transparencia de fondo
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 44, 36, 44));
        card.setMaxWidth(420);
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 24;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 24;" +
            "-fx-border-width: 1;"
        );
        
        String warningColor = TemaManager.COLOR_WARNING;
        
        // ═══════════════════════════════════════════════════════════════════
        // CONTENEDOR DE ICONO ANIMADO MEJORADO
        // ═══════════════════════════════════════════════════════════════════
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(80, 80);
        
        // Círculos de fondo con animación
        Circle outerRing = new Circle(40);
        outerRing.setFill(Color.TRANSPARENT);
        outerRing.setStroke(Color.web(warningColor + "30"));
        outerRing.setStrokeWidth(2);
        
        Circle middleRing = new Circle(32);
        middleRing.setFill(Color.web(warningColor + "15"));
        
        Circle innerCircle = new Circle(26);
        innerCircle.setFill(Color.web(warningColor + "25"));
        
        // Icono SVG de signo de interrogación mejorado
        javafx.scene.shape.SVGPath questionIcon = new javafx.scene.shape.SVGPath();
        questionIcon.setContent("M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z");
        questionIcon.setFill(Color.web(warningColor));
        questionIcon.setScaleX(1.6);
        questionIcon.setScaleY(1.6);
        questionIcon.setOpacity(0);
        
        iconContainer.getChildren().addAll(outerRing, middleRing, innerCircle, questionIcon);
        
        // ═══════════════════════════════════════════════════════════════════
        // ANIMACIONES
        // ═══════════════════════════════════════════════════════════════════
        
        // Pulso suave del anillo exterior
        ScaleTransition ringPulse = new ScaleTransition(Duration.seconds(1.5), outerRing);
        ringPulse.setFromX(1);
        ringPulse.setFromY(1);
        ringPulse.setToX(1.15);
        ringPulse.setToY(1.15);
        ringPulse.setCycleCount(Animation.INDEFINITE);
        ringPulse.setAutoReverse(true);
        ringPulse.setInterpolator(Interpolator.EASE_BOTH);
        
        FadeTransition ringFade = new FadeTransition(Duration.seconds(1.5), outerRing);
        ringFade.setFromValue(1);
        ringFade.setToValue(0.4);
        ringFade.setCycleCount(Animation.INDEFINITE);
        ringFade.setAutoReverse(true);
        
        // Título
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 22));
        lblTitulo.setTextFill(Color.web(COLOR_TEXT()));
        
        // Mensaje
        Label lblMensaje = new Label(mensaje);
        lblMensaje.setFont(Font.font("Segoe UI", 14));
        lblMensaje.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(340);
        lblMensaje.setAlignment(Pos.CENTER);
        
        // Botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER);
        
        Button btnCancelar = crearBotonSecundario("Cancelar", 120);
        Button btnConfirmar = crearBotonPrimario("Confirmar", 120);
        
        btnCancelar.setOnAction(e -> {
            // Animación de salida
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), card);
            fadeOut.setToValue(0);
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), card);
            scaleOut.setToX(0.9);
            scaleOut.setToY(0.9);
            ParallelTransition exitAnim = new ParallelTransition(fadeOut, scaleOut);
            exitAnim.setOnFinished(ev -> {
                resultado.set(false);
                dialog.close();
            });
            exitAnim.play();
        });
        
        btnConfirmar.setOnAction(e -> {
            // Animación de salida
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), card);
            fadeOut.setToValue(0);
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), card);
            scaleOut.setToX(0.9);
            scaleOut.setToY(0.9);
            ParallelTransition exitAnim = new ParallelTransition(fadeOut, scaleOut);
            exitAnim.setOnFinished(ev -> {
                resultado.set(true);
                dialog.close();
            });
            exitAnim.play();
        });
        
        botones.getChildren().addAll(btnCancelar, btnConfirmar);
        
        card.getChildren().addAll(iconContainer, lblTitulo, lblMensaje, botones);
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(70);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        shadow.setOffsetY(8);
        card.setEffect(shadow);
        
        // Preparar animación de entrada
        card.setOpacity(0);
        card.setScaleX(0.7);
        card.setScaleY(0.7);
        card.setTranslateY(20);
        
        root.getChildren().add(card);
        
        Scene scene = new Scene(root, 420, 340);
        scene.setFill(Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        
        // Ejecutar animaciones al mostrar
        dialog.setOnShown(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), card);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), card);
            scaleIn.setFromX(0.7);
            scaleIn.setFromY(0.7);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_BACK);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), card);
            slideIn.setToY(0);
            slideIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ParallelTransition entryAnim = new ParallelTransition(fadeIn, scaleIn, slideIn);
            entryAnim.setOnFinished(ev -> {
                // Animar el icono
                FadeTransition iconFade = new FadeTransition(Duration.millis(300), questionIcon);
                iconFade.setFromValue(0);
                iconFade.setToValue(1);
                
                ScaleTransition iconScale = new ScaleTransition(Duration.millis(400), questionIcon);
                iconScale.setFromX(0.5);
                iconScale.setFromY(0.5);
                iconScale.setToX(1.6);
                iconScale.setToY(1.6);
                iconScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_ELASTIC);
                
                new ParallelTransition(iconFade, iconScale).play();
                ringPulse.play();
                ringFade.play();
            });
            entryAnim.play();
        });
        
        dialog.showAndWait();
        return resultado.get();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CONFIRMACIÓN DE ELIMINACIÓN CON DETALLES
    // ════════════════════════════════════════════════════════════════════════════
    
    public static boolean confirmarEliminacion(Stage owner, String titulo, String pregunta, String detalles) {
        AtomicReference<Boolean> resultado = new AtomicReference<>(false);
        
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 44, 36, 44));
        card.setMaxWidth(450);
        
        String dangerColor = COLOR_PRIMARY; // #E63946
        
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + dangerColor + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 2;"
        );
        
        // Icono de advertencia
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(90, 90);
        
        Circle outerRing = new Circle(45);
        outerRing.setFill(Color.TRANSPARENT);
        outerRing.setStroke(Color.web(dangerColor + "40"));
        outerRing.setStrokeWidth(3);
        
        Circle middleRing = new Circle(37);
        middleRing.setFill(Color.web(dangerColor + "20"));
        
        Circle innerCircle = new Circle(30);
        innerCircle.setFill(Color.web(dangerColor + "30"));
        
        javafx.scene.shape.SVGPath alertIcon = new javafx.scene.shape.SVGPath();
        alertIcon.setContent("M12 5.99L19.53 19H4.47L12 5.99M12 2L1 21h22L12 2zm1 14h-2v2h2v-2zm0-6h-2v4h2v-4z");
        alertIcon.setFill(Color.web(dangerColor));
        alertIcon.setScaleX(1.7);
        alertIcon.setScaleY(1.7);
        alertIcon.setOpacity(0);
        
        iconContainer.getChildren().addAll(outerRing, middleRing, innerCircle, alertIcon);
        
        // Animación del icono
        ScaleTransition ringPulse = new ScaleTransition(Duration.seconds(1.5), outerRing);
        ringPulse.setFromX(1);
        ringPulse.setFromY(1);
        ringPulse.setToX(1.12);
        ringPulse.setToY(1.12);
        ringPulse.setCycleCount(Animation.INDEFINITE);
        ringPulse.setAutoReverse(true);
        ringPulse.setInterpolator(Interpolator.EASE_BOTH);
        
        FadeTransition ringFade = new FadeTransition(Duration.seconds(1.5), outerRing);
        ringFade.setFromValue(1);
        ringFade.setToValue(0.4);
        ringFade.setCycleCount(Animation.INDEFINITE);
        ringFade.setAutoReverse(true);
        
        // Título
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 23));
        lblTitulo.setTextFill(Color.web(COLOR_TEXT()));
        
        // Pregunta
        Label lblPregunta = new Label(pregunta);
        lblPregunta.setFont(Font.font("Segoe UI", 15));
        lblPregunta.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        lblPregunta.setWrapText(true);
        lblPregunta.setMaxWidth(370);
        lblPregunta.setAlignment(Pos.CENTER);
        
        // Detalles en caja de advertencia
        VBox detallesBox = null;
        if (detalles != null && !detalles.isEmpty()) {
            detallesBox = new VBox(8);
            detallesBox.setAlignment(Pos.CENTER_LEFT);
            detallesBox.setPadding(new Insets(14, 18, 14, 18));
            detallesBox.setMaxWidth(370);
            detallesBox.setStyle(
                "-fx-background-color: " + dangerColor + "15;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + dangerColor + "40;" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1.5;"
            );
            
            Label lblDetalles = new Label(detalles);
            lblDetalles.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            lblDetalles.setTextFill(Color.web(dangerColor));
            lblDetalles.setWrapText(true);
            lblDetalles.setMaxWidth(340);
            
            detallesBox.getChildren().add(lblDetalles);
        }
        
        // Advertencia adicional
        Label lblAdvertencia = new Label("Esta acción no se puede deshacer");
        lblAdvertencia.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblAdvertencia.setTextFill(Color.web(COLOR_TEXT_SECONDARY() + "99"));
        lblAdvertencia.setStyle("-fx-font-style: italic;");
        
        // Botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER);
        
        Button btnCancelar = crearBotonSecundario("Cancelar", 130);
        
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setPrefWidth(130);
        btnEliminar.setPrefHeight(42);
        btnEliminar.setStyle(
            "-fx-background-color: " + dangerColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        btnEliminar.setOnMouseEntered(e -> {
            btnEliminar.setStyle(
                "-fx-background-color: derive(" + dangerColor + ", -15%);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition hover = new ScaleTransition(Duration.millis(100), btnEliminar);
            hover.setToX(1.03);
            hover.setToY(1.03);
            hover.play();
        });
        
        btnEliminar.setOnMouseExited(e -> {
            btnEliminar.setStyle(
                "-fx-background-color: " + dangerColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition hover = new ScaleTransition(Duration.millis(100), btnEliminar);
            hover.setToX(1);
            hover.setToY(1);
            hover.play();
        });
        
        btnCancelar.setOnAction(e -> {
            resultado.set(false);
            cerrarDialogo(dialog, card);
        });
        
        btnEliminar.setOnAction(e -> {
            resultado.set(true);
            cerrarDialogo(dialog, card);
        });
        
        botones.getChildren().addAll(btnCancelar, btnEliminar);
        
        // Construir card
        if (detallesBox != null) {
            card.getChildren().addAll(iconContainer, lblTitulo, lblPregunta, detallesBox, lblAdvertencia, botones);
        } else {
            card.getChildren().addAll(iconContainer, lblTitulo, lblPregunta, lblAdvertencia, botones);
        }
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(70);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        shadow.setOffsetY(8);
        card.setEffect(shadow);
        
        card.setOpacity(0);
        card.setScaleX(0.7);
        card.setScaleY(0.7);
        card.setTranslateY(18);
        
        root.getChildren().add(card);
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        
        dialog.setOnShown(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), card);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), card);
            scaleIn.setFromX(0.7);
            scaleIn.setFromY(0.7);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_BACK);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), card);
            slideIn.setToY(0);
            slideIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ParallelTransition entryAnim = new ParallelTransition(fadeIn, scaleIn, slideIn);
            entryAnim.setOnFinished(ev -> {
                // Animar icono
                FadeTransition iconFade = new FadeTransition(Duration.millis(300), alertIcon);
                iconFade.setFromValue(0);
                iconFade.setToValue(1);
                
                ScaleTransition iconScale = new ScaleTransition(Duration.millis(400), alertIcon);
                iconScale.setFromX(0.5);
                iconScale.setFromY(0.5);
                iconScale.setToX(1.7);
                iconScale.setToY(1.7);
                iconScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_ELASTIC);
                
                // Sacudida sutil
                Timeline shake = new Timeline(
                    new KeyFrame(Duration.millis(0), new KeyValue(alertIcon.translateXProperty(), 0)),
                    new KeyFrame(Duration.millis(80), new KeyValue(alertIcon.translateXProperty(), 4)),
                    new KeyFrame(Duration.millis(160), new KeyValue(alertIcon.translateXProperty(), -4)),
                    new KeyFrame(Duration.millis(240), new KeyValue(alertIcon.translateXProperty(), 3)),
                    new KeyFrame(Duration.millis(320), new KeyValue(alertIcon.translateXProperty(), -2)),
                    new KeyFrame(Duration.millis(400), new KeyValue(alertIcon.translateXProperty(), 0))
                );
                
                ParallelTransition iconAnim = new ParallelTransition(iconFade, iconScale);
                iconAnim.setOnFinished(ev2 -> shake.play());
                iconAnim.play();
                
                ringPulse.play();
                ringFade.play();
            });
            entryAnim.play();
        });
        
        dialog.showAndWait();
        return resultado.get();
    }

    /**
     * Diálogo de confirmación para eliminación múltiple con diseño moderno
     */
    public static boolean confirmarEliminacionMultiple(Stage owner, int cantidad) {
        AtomicReference<Boolean> resultado = new AtomicReference<>(false);
        
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 44, 36, 44));
        card.setMaxWidth(450);
        
        String dangerColor = COLOR_PRIMARY; // #E63946
        
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + dangerColor + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 2;"
        );
        
        // Icono de advertencia
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(90, 90);
        
        Circle outerRing = new Circle(45);
        outerRing.setFill(Color.TRANSPARENT);
        outerRing.setStroke(Color.web(dangerColor + "40"));
        outerRing.setStrokeWidth(3);
        
        Circle middleRing = new Circle(37);
        middleRing.setFill(Color.web(dangerColor + "20"));
        
        Circle innerCircle = new Circle(30);
        innerCircle.setFill(Color.web(dangerColor + "30"));
        
        javafx.scene.shape.SVGPath alertIcon = new javafx.scene.shape.SVGPath();
        alertIcon.setContent("M12 5.99L19.53 19H4.47L12 5.99M12 2L1 21h22L12 2zm1 14h-2v2h2v-2zm0-6h-2v4h2v-4z");
        alertIcon.setFill(Color.web(dangerColor));
        alertIcon.setScaleX(1.7);
        alertIcon.setScaleY(1.7);
        alertIcon.setOpacity(0);
        
        iconContainer.getChildren().addAll(outerRing, middleRing, innerCircle, alertIcon);
        
        // Animación del icono
        ScaleTransition ringPulse = new ScaleTransition(Duration.seconds(1.5), outerRing);
        ringPulse.setFromX(1);
        ringPulse.setFromY(1);
        ringPulse.setToX(1.12);
        ringPulse.setToY(1.12);
        ringPulse.setCycleCount(Animation.INDEFINITE);
        ringPulse.setAutoReverse(true);
        ringPulse.setInterpolator(Interpolator.EASE_BOTH);
        
        FadeTransition ringFade = new FadeTransition(Duration.seconds(1.5), outerRing);
        ringFade.setFromValue(1);
        ringFade.setToValue(0.4);
        ringFade.setCycleCount(Animation.INDEFINITE);
        ringFade.setAutoReverse(true);
        
        // Título
        Label lblTitulo = new Label("Confirmar eliminación múltiple");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 23));
        lblTitulo.setTextFill(Color.web(COLOR_TEXT()));
        
        // Pregunta
        Label lblPregunta = new Label("¿Eliminar " + cantidad + " registros seleccionados?");
        lblPregunta.setFont(Font.font("Segoe UI", 15));
        lblPregunta.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        lblPregunta.setWrapText(true);
        lblPregunta.setMaxWidth(370);
        lblPregunta.setAlignment(Pos.CENTER);
        
        // Detalles en caja de advertencia
        VBox detallesBox = new VBox(8);
        detallesBox.setAlignment(Pos.CENTER_LEFT);
        detallesBox.setPadding(new Insets(14, 18, 14, 18));
        detallesBox.setMaxWidth(370);
        detallesBox.setStyle(
            "-fx-background-color: " + dangerColor + "15;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + dangerColor + "40;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1.5;"
        );
        
        Label lblDetalles = new Label("Se eliminarán solo las filas seleccionadas.\nEsta acción no se puede deshacer.");
        lblDetalles.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblDetalles.setTextFill(Color.web(dangerColor));
        lblDetalles.setWrapText(true);
        lblDetalles.setMaxWidth(340);
        
        detallesBox.getChildren().add(lblDetalles);
        
        // Botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER);
        
        Button btnCancelar = crearBotonSecundario("Cancelar", 130);
        
        Button btnAceptar = new Button("Aceptar");
        btnAceptar.setPrefWidth(130);
        btnAceptar.setPrefHeight(42);
        btnAceptar.setStyle(
            "-fx-background-color: " + dangerColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        btnAceptar.setOnMouseEntered(e -> {
            btnAceptar.setStyle(
                "-fx-background-color: derive(" + dangerColor + ", -15%);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition hover = new ScaleTransition(Duration.millis(100), btnAceptar);
            hover.setToX(1.03);
            hover.setToY(1.03);
            hover.play();
        });
        
        btnAceptar.setOnMouseExited(e -> {
            btnAceptar.setStyle(
                "-fx-background-color: " + dangerColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition hover = new ScaleTransition(Duration.millis(100), btnAceptar);
            hover.setToX(1);
            hover.setToY(1);
            hover.play();
        });
        
        btnCancelar.setOnAction(e -> {
            resultado.set(false);
            cerrarDialogo(dialog, card);
        });
        
        btnAceptar.setOnAction(e -> {
            resultado.set(true);
            cerrarDialogo(dialog, card);
        });
        
        botones.getChildren().addAll(btnCancelar, btnAceptar);
        
        // Construir card
        card.getChildren().addAll(iconContainer, lblTitulo, lblPregunta, detallesBox, botones);
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(70);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        shadow.setOffsetY(8);
        card.setEffect(shadow);
        
        card.setOpacity(0);
        card.setScaleX(0.7);
        card.setScaleY(0.7);
        card.setTranslateY(18);
        
        root.getChildren().add(card);
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        
        dialog.setOnShown(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), card);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), card);
            scaleIn.setFromX(0.7);
            scaleIn.setFromY(0.7);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_BACK);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), card);
            slideIn.setToY(0);
            slideIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ParallelTransition entryAnim = new ParallelTransition(fadeIn, scaleIn, slideIn);
            entryAnim.setOnFinished(ev -> {
                // Animar icono
                FadeTransition iconFade = new FadeTransition(Duration.millis(300), alertIcon);
                iconFade.setFromValue(0);
                iconFade.setToValue(1);
                
                ScaleTransition iconScale = new ScaleTransition(Duration.millis(400), alertIcon);
                iconScale.setFromX(0.5);
                iconScale.setFromY(0.5);
                iconScale.setToX(1.7);
                iconScale.setToY(1.7);
                iconScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_ELASTIC);
                
                // Sacudida sutil
                Timeline shake = new Timeline(
                    new KeyFrame(Duration.millis(0), new KeyValue(alertIcon.translateXProperty(), 0)),
                    new KeyFrame(Duration.millis(80), new KeyValue(alertIcon.translateXProperty(), 4)),
                    new KeyFrame(Duration.millis(160), new KeyValue(alertIcon.translateXProperty(), -4)),
                    new KeyFrame(Duration.millis(240), new KeyValue(alertIcon.translateXProperty(), 3)),
                    new KeyFrame(Duration.millis(320), new KeyValue(alertIcon.translateXProperty(), -2)),
                    new KeyFrame(Duration.millis(400), new KeyValue(alertIcon.translateXProperty(), 0))
                );
                
                ParallelTransition iconAnim = new ParallelTransition(iconFade, iconScale);
                iconAnim.setOnFinished(ev2 -> shake.play());
                iconAnim.play();
                
                ringPulse.play();
                ringFade.play();
            });
            entryAnim.play();
        });
        
        dialog.showAndWait();
        return resultado.get();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // MOSTRAR ÉXITO CON DETALLES
    // ════════════════════════════════════════════════════════════════════════════
    
    public static void mostrarExito(Stage owner, String titulo, String mensaje, String detalles) {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 44, 36, 44));
        card.setMaxWidth(440);
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 24;" +
            "-fx-border-color: " + COLOR_SUCCESS + "40;" +
            "-fx-border-radius: 24;" +
            "-fx-border-width: 2;"
        );
        
        // Icono de éxito
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(90, 90);
        
        Circle outerRing = new Circle(45);
        outerRing.setFill(Color.TRANSPARENT);
        outerRing.setStroke(Color.web(COLOR_SUCCESS + "50"));
        outerRing.setStrokeWidth(3);
        
        Circle middleRing = new Circle(37);
        middleRing.setFill(Color.web(COLOR_SUCCESS + "25"));
        
        Circle innerCircle = new Circle(30);
        innerCircle.setFill(Color.web(COLOR_SUCCESS));
        
        javafx.scene.shape.SVGPath checkIcon = new javafx.scene.shape.SVGPath();
        checkIcon.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
        checkIcon.setFill(Color.WHITE);
        checkIcon.setScaleX(2);
        checkIcon.setScaleY(2);
        checkIcon.setOpacity(0);
        
        iconContainer.getChildren().addAll(outerRing, middleRing, innerCircle, checkIcon);
        
        // Animaciones del icono
        ScaleTransition ringPulse = new ScaleTransition(Duration.seconds(1.5), outerRing);
        ringPulse.setFromX(1);
        ringPulse.setToX(1.15);
        ringPulse.setFromY(1);
        ringPulse.setToY(1.15);
        ringPulse.setCycleCount(Animation.INDEFINITE);
        ringPulse.setAutoReverse(true);
        
        FadeTransition ringFade = new FadeTransition(Duration.seconds(1.5), outerRing);
        ringFade.setFromValue(1);
        ringFade.setToValue(0.4);
        ringFade.setCycleCount(Animation.INDEFINITE);
        ringFade.setAutoReverse(true);
        
        // Título
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 23));
        lblTitulo.setTextFill(Color.web(COLOR_TEXT()));
        
        // Mensaje
        Label lblMensaje = new Label(mensaje);
        lblMensaje.setFont(Font.font("Segoe UI", 14));
        lblMensaje.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(370);
        lblMensaje.setAlignment(Pos.CENTER);
        
        // Detalles en caja de información
        VBox detallesBox = null;
        if (detalles != null && !detalles.isEmpty()) {
            detallesBox = new VBox(10);
            detallesBox.setAlignment(Pos.CENTER_LEFT);
            detallesBox.setPadding(new Insets(16, 20, 16, 20));
            detallesBox.setMaxWidth(370);
            detallesBox.setStyle(
                "-fx-background-color: " + COLOR_SUCCESS + "15;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + COLOR_SUCCESS + "40;" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1.5;"
            );
            
            // Parsear detalles (puede ser multi-línea con •)
            String[] lineas = detalles.split("\n");
            for (String linea : lineas) {
                HBox lineaBox = new HBox(8);
                lineaBox.setAlignment(Pos.CENTER_LEFT);
                
                if (linea.trim().startsWith("•")) {
                    Label lblBullet = new Label("✓");
                    lblBullet.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                    lblBullet.setTextFill(Color.web(COLOR_SUCCESS));
                    
                    Label lblTexto = new Label(linea.trim().substring(1).trim());
                    lblTexto.setFont(Font.font("Segoe UI", 13));
                    lblTexto.setTextFill(Color.web(COLOR_TEXT()));
                    lblTexto.setWrapText(true);
                    lblTexto.setMaxWidth(330);
                    
                    lineaBox.getChildren().addAll(lblBullet, lblTexto);
                } else {
                    Label lblTexto = new Label(linea.trim());
                    lblTexto.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                    lblTexto.setTextFill(Color.web(COLOR_TEXT()));
                    lblTexto.setWrapText(true);
                    lblTexto.setMaxWidth(340);
                    
                    lineaBox.getChildren().add(lblTexto);
                }
                
                detallesBox.getChildren().add(lineaBox);
            }
        }
        
        // Botón
        Button btnAceptar = new Button("Perfecto");
        btnAceptar.setPrefWidth(160);
        btnAceptar.setPrefHeight(46);
        btnAceptar.setStyle(
            "-fx-background-color: " + COLOR_SUCCESS + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );
        
        btnAceptar.setOnMouseEntered(e -> {
            btnAceptar.setStyle(
                "-fx-background-color: derive(" + COLOR_SUCCESS + ", -10%);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition hover = new ScaleTransition(Duration.millis(100), btnAceptar);
            hover.setToX(1.03);
            hover.setToY(1.03);
            hover.play();
        });
        
        btnAceptar.setOnMouseExited(e -> {
            btnAceptar.setStyle(
                "-fx-background-color: " + COLOR_SUCCESS + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition hover = new ScaleTransition(Duration.millis(100), btnAceptar);
            hover.setToX(1);
            hover.setToY(1);
            hover.play();
        });
        
        btnAceptar.setOnAction(e -> cerrarDialogo(dialog, card));
        
        // Construir card
        if (detallesBox != null) {
            card.getChildren().addAll(iconContainer, lblTitulo, lblMensaje, detallesBox, btnAceptar);
        } else {
            card.getChildren().addAll(iconContainer, lblTitulo, lblMensaje, btnAceptar);
        }
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(70);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        shadow.setOffsetY(8);
        card.setEffect(shadow);
        
        card.setOpacity(0);
        card.setScaleX(0.7);
        card.setScaleY(0.7);
        card.setTranslateY(18);
        
        root.getChildren().add(card);
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        
        dialog.setOnShown(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), card);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), card);
            scaleIn.setFromX(0.7);
            scaleIn.setFromY(0.7);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_BACK);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), card);
            slideIn.setToY(0);
            slideIn.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ParallelTransition entryAnim = new ParallelTransition(fadeIn, scaleIn, slideIn);
            entryAnim.setOnFinished(ev -> {
                // Animar checkmark
                FadeTransition iconFade = new FadeTransition(Duration.millis(200), checkIcon);
                iconFade.setFromValue(0);
                iconFade.setToValue(1);
                
                ScaleTransition iconScale = new ScaleTransition(Duration.millis(500), checkIcon);
                iconScale.setFromX(0);
                iconScale.setFromY(0);
                iconScale.setToX(2);
                iconScale.setToY(2);
                iconScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_ELASTIC);
                
                RotateTransition iconRotate = new RotateTransition(Duration.millis(500), checkIcon);
                iconRotate.setFromAngle(-30);
                iconRotate.setToAngle(0);
                iconRotate.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
                
                new ParallelTransition(iconFade, iconScale, iconRotate).play();
                
                ringPulse.play();
                ringFade.play();
            });
            entryAnim.play();
        });
        
        dialog.showAndWait();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // HELPER: CERRAR DIÁLOGO CON ANIMACIÓN
    // ════════════════════════════════════════════════════════════════════════════
    
    private static void cerrarDialogo(Stage dialog, VBox card) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), card);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_IN_CUBIC);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(250), card);
        scaleOut.setToX(0.85);
        scaleOut.setToY(0.85);
        scaleOut.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_IN_CUBIC);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), card);
        slideOut.setToY(12);
        slideOut.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_IN_CUBIC);
        ParallelTransition exitAnim = new ParallelTransition(fadeOut, scaleOut, slideOut);
        exitAnim.setOnFinished(ev -> dialog.close());
        exitAnim.play();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // RESULTADO DE GENERACIÓN DE INVENTARIO - CON ANIMACIONES
    // ════════════════════════════════════════════════════════════════════════════
    
    public static void mostrarResultadoGeneracion(Stage owner, boolean exito, InfoPC info) {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 50, 40, 50));
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 24;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 24;" +
            "-fx-border-width: 1;"
        );
        
        // Determinar color según estado
        String color = exito ? COLOR_SUCCESS : COLOR_PRIMARY;
        
        // ═══════════════════════════════════════════════════════════════════
        // ICONO ANIMADO
        // ═══════════════════════════════════════════════════════════════════
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(80, 80);
        
        // Círculo de fondo con animación
        Circle bgCircle = new Circle(36);
        bgCircle.setFill(Color.web(color));
        bgCircle.setScaleX(0);
        bgCircle.setScaleY(0);
        
        // Crear el icono SVG
        javafx.scene.shape.SVGPath iconPath = new javafx.scene.shape.SVGPath();
        if (exito) {
            // Checkmark
            iconPath.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
        } else {
            // X para error
            iconPath.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        }
        iconPath.setFill(Color.WHITE);
        iconPath.setScaleX(1.8);
        iconPath.setScaleY(1.8);
        iconPath.setOpacity(0);
        
        iconContainer.getChildren().addAll(bgCircle, iconPath);
        
        // Título
        Label titulo = new Label(exito ? "Inventario Generado" : "Error en Generación");
        titulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 24));
        titulo.setTextFill(Color.web(COLOR_TEXT()));
        titulo.setOpacity(0);
        
        // Info del sistema (si fue exitoso)
        VBox infoBox = new VBox(12);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(16, 20, 16, 20));
        infoBox.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 14;"
        );
        infoBox.setOpacity(0);
        
        if (exito && info != null) {
            agregarInfoItemSVG(infoBox, IconosSVG.servidor(color, 20), "Hostname", info.hostname != null ? info.hostname : "N/A");
            agregarInfoItemSVG(infoBox, IconosSVG.usuario(color, 20), "Usuario", info.userName != null ? info.userName : "N/A");
            agregarInfoItemSVG(infoBox, IconosSVG.fabrica(color, 20), "Marca", info.manufacturer != null ? info.manufacturer : "N/A");
            agregarInfoItemSVG(infoBox, IconosSVG.computadora(color, 20), "Sistema", info.sistema != null ? info.sistema : "N/A");
        } else {
            Label errorMsg = new Label("No se pudo completar la generación del inventario.\nVerifique los permisos y vuelva a intentar.");
            errorMsg.setFont(Font.font("Segoe UI", 13));
            errorMsg.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
            errorMsg.setWrapText(true);
            infoBox.getChildren().add(errorMsg);
        }
        
        // Botón
        Button btnCerrar = new Button(exito ? "Perfecto" : "Cerrar");
        btnCerrar.setPrefWidth(180);
        btnCerrar.setPrefHeight(48);
        final String finalColor = color;
        btnCerrar.setStyle(
            "-fx-background-color: " + finalColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 14;" +
            "-fx-cursor: hand;"
        );
        btnCerrar.setOpacity(0);
        btnCerrar.setOnAction(e -> {
            // Animación de salida
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), card);
            fadeOut.setToValue(0);
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), card);
            scaleOut.setToX(0.95);
            scaleOut.setToY(0.95);
            ParallelTransition exit = new ParallelTransition(fadeOut, scaleOut);
            exit.setOnFinished(ev -> dialog.close());
            exit.play();
        });
        
        // Hover del botón
        btnCerrar.setOnMouseEntered(e -> {
            btnCerrar.setStyle(
                "-fx-background-color: derive(" + finalColor + ", 10%);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;"
            );
        });
        btnCerrar.setOnMouseExited(e -> {
            btnCerrar.setStyle(
                "-fx-background-color: " + finalColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;"
            );
        });
        
        card.getChildren().addAll(iconContainer, titulo, infoBox, btnCerrar);
        
        // Sombra elegante
        DropShadow shadow = new DropShadow();
        shadow.setRadius(60);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        shadow.setOffsetY(8);
        card.setEffect(shadow);
        
        // Estado inicial para animación
        card.setOpacity(0);
        card.setScaleX(0.75);
        card.setScaleY(0.75);
        card.setTranslateY(20);
        
        root.getChildren().add(card);
        
        Scene scene = new Scene(root, 420, 420);
        scene.setFill(Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        
        // ═══════════════════════════════════════════════════════════════════
        // ANIMACIONES DE ENTRADA
        // ═══════════════════════════════════════════════════════════════════
        dialog.setOnShown(e -> {
            // Entrada del card
            FadeTransition cardFade = new FadeTransition(Duration.millis(350), card);
            cardFade.setFromValue(0);
            cardFade.setToValue(1);
            cardFade.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ScaleTransition cardScale = new ScaleTransition(Duration.millis(450), card);
            cardScale.setFromX(0.75);
            cardScale.setFromY(0.75);
            cardScale.setToX(1);
            cardScale.setToY(1);
            cardScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_BACK);
            
            TranslateTransition cardSlide = new TranslateTransition(Duration.millis(350), card);
            cardSlide.setToY(0);
            cardSlide.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            // Animación del círculo de fondo
            ScaleTransition circlePop = new ScaleTransition(Duration.millis(400), bgCircle);
            circlePop.setFromX(0);
            circlePop.setFromY(0);
            circlePop.setToX(1);
            circlePop.setToY(1);
            circlePop.setDelay(Duration.millis(200));
            circlePop.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_BACK);
            
            // Animación del icono (checkmark o X)
            FadeTransition iconFade = new FadeTransition(Duration.millis(300), iconPath);
            iconFade.setFromValue(0);
            iconFade.setToValue(1);
            iconFade.setDelay(Duration.millis(400));
            
            ScaleTransition iconScale = new ScaleTransition(Duration.millis(400), iconPath);
            iconScale.setFromX(0.5);
            iconScale.setFromY(0.5);
            iconScale.setToX(1.8);
            iconScale.setToY(1.8);
            iconScale.setDelay(Duration.millis(400));
            iconScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_ELASTIC);
            
            // Si es éxito, añadir rotación sutil al checkmark
            RotateTransition iconRotate = null;
            if (exito) {
                iconRotate = new RotateTransition(Duration.millis(400), iconPath);
                iconRotate.setFromAngle(-20);
                iconRotate.setToAngle(0);
                iconRotate.setDelay(Duration.millis(400));
                iconRotate.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            }
            
            // Animación del título
            FadeTransition titleFade = new FadeTransition(Duration.millis(300), titulo);
            titleFade.setFromValue(0);
            titleFade.setToValue(1);
            titleFade.setDelay(Duration.millis(500));
            
            // Animación de la info
            FadeTransition infoFade = new FadeTransition(Duration.millis(300), infoBox);
            infoFade.setFromValue(0);
            infoFade.setToValue(1);
            infoFade.setDelay(Duration.millis(600));
            
            // Animación del botón
            FadeTransition btnFade = new FadeTransition(Duration.millis(300), btnCerrar);
            btnFade.setFromValue(0);
            btnFade.setToValue(1);
            btnFade.setDelay(Duration.millis(700));
            
            // Ejecutar todas las animaciones
            ParallelTransition allAnims;
            if (iconRotate != null) {
                allAnims = new ParallelTransition(cardFade, cardScale, cardSlide, circlePop, iconFade, iconScale, iconRotate, titleFade, infoFade, btnFade);
            } else {
                allAnims = new ParallelTransition(cardFade, cardScale, cardSlide, circlePop, iconFade, iconScale, titleFade, infoFade, btnFade);
            }
            allAnims.play();
        });
        
        dialog.showAndWait();
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════════════════════════
    
    private static void agregarInfoItem(VBox container, String icono, String label, String valor) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Label lblIcono = new Label(icono);
        lblIcono.setFont(Font.font("Segoe UI", 14));
        
        Label lblLabel = new Label(label + ":");
        lblLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblLabel.setTextFill(Color.web(TemaManager.getTextSecondary()));
        lblLabel.setMinWidth(70);
        
        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Segoe UI", 12));
        lblValor.setTextFill(Color.web(TemaManager.getText()));
        
        item.getChildren().addAll(lblIcono, lblLabel, lblValor);
        container.getChildren().add(item);
    }
    
    private static void agregarInfoItemSVG(VBox container, javafx.scene.Node icono, String label, String valor) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconWrapper = new StackPane(icono);
        iconWrapper.setMinSize(20, 20);
        iconWrapper.setMaxSize(20, 20);
        
        Label lblLabel = new Label(label + ":");
        lblLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblLabel.setTextFill(Color.web(TemaManager.getTextSecondary()));
        lblLabel.setMinWidth(70);
        
        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Segoe UI", 12));
        lblValor.setTextFill(Color.web(TemaManager.getText()));
        
        item.getChildren().addAll(iconWrapper, lblLabel, lblValor);
        container.getChildren().add(item);
    }
    
    private static Button crearBotonPrimario(String texto, double ancho) {
        Button btn = new Button(texto);
        btn.setPrefWidth(ancho);
        btn.setPrefHeight(42);
        btn.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: derive(" + COLOR_PRIMARY + ", -10%);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        ));
        
        return btn;
    }
    
    private static Button crearBotonSecundario(String texto, double ancho) {
        Button btn = new Button(texto);
        btn.setPrefWidth(ancho);
        btn.setPrefHeight(42);
        btn.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-text-fill: " + COLOR_TEXT_SECONDARY() + ";" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + COLOR_BORDER() + ";" +
            "-fx-text-fill: " + COLOR_TEXT() + ";" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-text-fill: " + COLOR_TEXT_SECONDARY() + ";" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        ));
        
        return btn;
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // ADVERTENCIA DE EXCEL ABIERTO - CON CUENTA REGRESIVA
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Muestra un diálogo de advertencia cuando el archivo Excel está abierto.
     * Incluye una cuenta regresiva de segundos para cerrar el archivo.
     * @param owner Stage padre
     * @param nombreArchivo Nombre del archivo Excel que debe cerrarse
     * @param segundosEspera Segundos de cuenta regresiva
     * @param checkArchivoLiberado Función que retorna true si el archivo ya está libre
     * @return true si el archivo fue liberado antes del tiempo límite, false si no
     */
    public static boolean mostrarAdvertenciaExcelAbierto(Stage owner, String nombreArchivo, int segundosEspera, 
                                                          java.util.function.BooleanSupplier checkArchivoLiberado) {
        final boolean[] archivoLiberado = {false};
        final boolean[] dialogoCerrado = {false};
        
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 50, 40, 50));
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 24;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 24;" +
            "-fx-border-width: 1;"
        );
        
        // Color de advertencia (naranja/ámbar)
        String colorWarning = TemaManager.COLOR_WARNING;
        
        // ═══════════════════════════════════════════════════════════════════
        // ICONO ANIMADO DE ADVERTENCIA
        // ═══════════════════════════════════════════════════════════════════
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(80, 80);
        
        // Círculo de fondo
        Circle bgCircle = new Circle(36);
        bgCircle.setFill(Color.web(colorWarning));
        bgCircle.setScaleX(0);
        bgCircle.setScaleY(0);
        
        // Icono de advertencia (!)
        javafx.scene.shape.SVGPath iconPath = new javafx.scene.shape.SVGPath();
        iconPath.setContent("M12 2L1 21h22L12 2zm0 3.99L19.53 19H4.47L12 5.99zM11 10v4h2v-4h-2zm0 6v2h2v-2h-2z");
        iconPath.setFill(Color.WHITE);
        iconPath.setScaleX(1.8);
        iconPath.setScaleY(1.8);
        iconPath.setOpacity(0);
        
        iconContainer.getChildren().addAll(bgCircle, iconPath);
        
        // Título
        Label titulo = new Label("Archivo Excel Abierto");
        titulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 24));
        titulo.setTextFill(Color.web(COLOR_TEXT()));
        titulo.setOpacity(0);
        
        // Box con información
        VBox infoBox = new VBox(12);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(16, 20, 16, 20));
        infoBox.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 14;"
        );
        infoBox.setOpacity(0);
        
        Label msgPrincipal = new Label("Por favor cierre el siguiente archivo:");
        msgPrincipal.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        msgPrincipal.setTextFill(Color.web(COLOR_TEXT()));
        
        // Nombre del archivo destacado
        Label lblNombreArchivo = new Label("📄 " + nombreArchivo);
        lblNombreArchivo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblNombreArchivo.setTextFill(Color.web(colorWarning));
        lblNombreArchivo.setWrapText(true);
        lblNombreArchivo.setMaxWidth(280);
        lblNombreArchivo.setAlignment(Pos.CENTER);
        
        // Contador visual
        Label lblTiempo = new Label(String.valueOf(segundosEspera));
        lblTiempo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        lblTiempo.setTextFill(Color.web(colorWarning));
        
        Label lblSegundos = new Label("segundos restantes");
        lblSegundos.setFont(Font.font("Segoe UI", 12));
        lblSegundos.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        
        infoBox.getChildren().addAll(msgPrincipal, lblNombreArchivo, lblTiempo, lblSegundos);
        
        // Barra de progreso
        ProgressBar progressBar = new ProgressBar(1.0);
        progressBar.setPrefWidth(280);
        progressBar.setPrefHeight(6);
        progressBar.setStyle(
            "-fx-accent: " + colorWarning + ";" +
            "-fx-background-radius: 3;" +
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";"
        );
        progressBar.setOpacity(0);
        
        // Botón cancelar
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(180);
        btnCancelar.setPrefHeight(48);
        btnCancelar.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-text-fill: " + COLOR_TEXT_SECONDARY() + ";" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 14;" +
            "-fx-cursor: hand;"
        );
        btnCancelar.setOpacity(0);
        
        // Hover del botón
        btnCancelar.setOnMouseEntered(e -> {
            btnCancelar.setStyle(
                "-fx-background-color: " + COLOR_BORDER() + ";" +
                "-fx-text-fill: " + COLOR_TEXT() + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 14;" +
                "-fx-cursor: hand;"
            );
        });
        btnCancelar.setOnMouseExited(e -> {
            btnCancelar.setStyle(
                "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                "-fx-text-fill: " + COLOR_TEXT_SECONDARY() + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 14;" +
                "-fx-cursor: hand;"
            );
        });
        
        card.getChildren().addAll(iconContainer, titulo, infoBox, progressBar, btnCancelar);
        
        // Sombra elegante
        DropShadow shadow = new DropShadow();
        shadow.setRadius(60);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        shadow.setOffsetY(8);
        card.setEffect(shadow);
        
        // Estado inicial
        card.setOpacity(0);
        card.setScaleX(0.75);
        card.setScaleY(0.75);
        card.setTranslateY(20);
        
        root.getChildren().add(card);
        
        Scene scene = new Scene(root, 420, 440);
        scene.setFill(Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        
        // Timeline para cuenta regresiva
        final int[] tiempoRestante = {segundosEspera};
        Timeline countdown = new Timeline();
        countdown.setCycleCount(segundosEspera);
        countdown.getKeyFrames().add(new KeyFrame(Duration.seconds(1), ev -> {
            tiempoRestante[0]--;
            
            // Verificar si el archivo fue liberado
            if (checkArchivoLiberado.getAsBoolean()) {
                archivoLiberado[0] = true;
                countdown.stop();
                cerrarDialogoAdvertencia(dialog, card, dialogoCerrado);
                return;
            }
            
            lblTiempo.setText(String.valueOf(tiempoRestante[0]));
            double progress = (double) tiempoRestante[0] / segundosEspera;
            progressBar.setProgress(progress);
            
            // Animación de pulso cuando quedan pocos segundos
            if (tiempoRestante[0] <= 10) {
                ScaleTransition pulse = new ScaleTransition(Duration.millis(200), lblTiempo);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.1);
                pulse.setToY(1.1);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(2);
                pulse.play();
                
                // Cambiar color a rojo cuando queden pocos segundos
                if (tiempoRestante[0] <= 5) {
                    lblTiempo.setTextFill(Color.web(COLOR_PRIMARY));
                }
            }
            
            if (tiempoRestante[0] <= 0) {
                countdown.stop();
                cerrarDialogoAdvertencia(dialog, card, dialogoCerrado);
            }
        }));
        
        // Botón cancelar
        btnCancelar.setOnAction(e -> {
            countdown.stop();
            cerrarDialogoAdvertencia(dialog, card, dialogoCerrado);
        });
        
        // Animaciones de entrada
        dialog.setOnShown(e -> {
            // Entrada del card
            FadeTransition cardFade = new FadeTransition(Duration.millis(350), card);
            cardFade.setFromValue(0);
            cardFade.setToValue(1);
            cardFade.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            ScaleTransition cardScale = new ScaleTransition(Duration.millis(450), card);
            cardScale.setFromX(0.75);
            cardScale.setFromY(0.75);
            cardScale.setToX(1);
            cardScale.setToY(1);
            cardScale.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_BACK);
            
            TranslateTransition cardSlide = new TranslateTransition(Duration.millis(350), card);
            cardSlide.setToY(0);
            cardSlide.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_CUBIC);
            
            // Animación del círculo
            ScaleTransition circlePop = new ScaleTransition(Duration.millis(400), bgCircle);
            circlePop.setDelay(Duration.millis(150));
            circlePop.setFromX(0);
            circlePop.setFromY(0);
            circlePop.setToX(1);
            circlePop.setToY(1);
            circlePop.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_OUT_BACK);
            
            // Icono aparece
            FadeTransition iconFade = new FadeTransition(Duration.millis(300), iconPath);
            iconFade.setDelay(Duration.millis(350));
            iconFade.setToValue(1);
            
            // Título
            FadeTransition tituloFade = new FadeTransition(Duration.millis(300), titulo);
            tituloFade.setDelay(Duration.millis(200));
            tituloFade.setToValue(1);
            
            // Info box
            FadeTransition infoFade = new FadeTransition(Duration.millis(300), infoBox);
            infoFade.setDelay(Duration.millis(300));
            infoFade.setToValue(1);
            
            // Progress bar
            FadeTransition progressFade = new FadeTransition(Duration.millis(300), progressBar);
            progressFade.setDelay(Duration.millis(350));
            progressFade.setToValue(1);
            
            // Botón
            FadeTransition btnFade = new FadeTransition(Duration.millis(300), btnCancelar);
            btnFade.setDelay(Duration.millis(400));
            btnFade.setToValue(1);
            
            ParallelTransition entrada = new ParallelTransition(
                cardFade, cardScale, cardSlide, circlePop, iconFade, tituloFade, infoFade, progressFade, btnFade
            );
            entrada.setOnFinished(ev -> countdown.play());
            entrada.play();
        });
        
        dialog.showAndWait();
        return archivoLiberado[0];
    }
    
    private static void cerrarDialogoAdvertencia(Stage dialog, VBox card, boolean[] dialogoCerrado) {
        if (dialogoCerrado[0]) return;
        dialogoCerrado[0] = true;
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), card);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_IN_CUBIC);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(250), card);
        scaleOut.setToX(0.85);
        scaleOut.setToY(0.85);
        scaleOut.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_IN_CUBIC);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), card);
        slideOut.setToY(12);
        slideOut.setInterpolator(inventario.fx.util.AnimacionesFX.EASE_IN_CUBIC);
        ParallelTransition exit = new ParallelTransition(fadeOut, scaleOut, slideOut);
        exit.setOnFinished(ev -> dialog.close());
        exit.play();
    }
}
