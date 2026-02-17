package inventario.fx.ui.dialog;
import inventario.fx.model.TemaManager;
import inventario.fx.icons.IconosSVG;

import inventario.fx.service.NavigationController;
import inventario.fx.util.AnimacionesFX;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Dialogo "Acerca de" extraido de AdminPanelFX.
 * Muestra informacion del programa, caracteristicas y tecnologias.
 */
public class AcercaDeDialogFX {

    private AcercaDeDialogFX() {}

    /**
     * Muestra el dialogo "Acerca de".
     *
     * @param owner ventana padre
     */
    public static void mostrar(Stage owner) {
        Stage dialog = NavigationController.crearDialogo(owner, "Acerca de", Modality.NONE);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + TemaManager.getBg() + "; " +
                     "-fx-background-radius: 16; " +
                     "-fx-border-color: " + TemaManager.getBorder() + "; " +
                     "-fx-border-radius: 16; " +
                     "-fx-border-width: 1; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);");
        root.setPrefWidth(520);

        // Header
        VBox headerBox = crearHeader(dialog);

        // Contenido
        VBox contenido = new VBox(20);
        contenido.setPadding(new Insets(24));
        contenido.getChildren().addAll(
            crearDescripcion(),
            crearCaracteristicas(),
            crearTecnologias(),
            crearCopyright()
        );

        // Boton cerrar
        HBox botones = new HBox();
        botones.setAlignment(Pos.CENTER);
        botones.setPadding(new Insets(0, 24, 24, 24));

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setPrefWidth(120);
        btnCerrar.setPrefHeight(38);
        btnCerrar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnCerrar.setTextFill(Color.WHITE);
        btnCerrar.setStyle(
            "-fx-background-color: #6366F1;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btnCerrar.setOnMouseEntered(e -> btnCerrar.setStyle(
            "-fx-background-color: #5558E6;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        btnCerrar.setOnMouseExited(e -> btnCerrar.setStyle(
            "-fx-background-color: #6366F1;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        btnCerrar.setOnAction(e -> dialog.close());
        botones.getChildren().add(btnCerrar);

        root.getChildren().addAll(headerBox, contenido, botones);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        // Animacion de entrada premium
        root.setOpacity(0);
        root.setScaleX(0.8);
        root.setScaleY(0.8);
        root.setTranslateY(15);
        
        FadeTransition fade = new FadeTransition(Duration.millis(350), root);
        fade.setToValue(1);
        fade.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), root);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(AnimacionesFX.EASE_OUT_BACK);
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(350), root);
        slideUp.setToY(0);
        slideUp.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        new ParallelTransition(fade, scale, slideUp).play();

        dialog.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SECCIONES DEL DIALOGO
    // ═══════════════════════════════════════════════════════════════════

    private static VBox crearHeader(Stage dialog) {
        VBox headerBox = new VBox(0);
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(24, 24, 20, 24));

        StackPane iconContainer = new StackPane(IconosSVG.info("#6366F1", 28));
        iconContainer.setStyle("-fx-background-color: #6366F115; " +
                               "-fx-background-radius: 14; " +
                               "-fx-padding: 14;");

        VBox titleBox = new VBox(4);
        Label titulo = new Label("SELCOMP - Inventario PC");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titulo.setTextFill(Color.web(TemaManager.getText()));

        Label subtitulo = new Label("Version 1.0 - JavaFX Edition");
        subtitulo.setFont(Font.font("Segoe UI", 13));
        subtitulo.setTextFill(Color.web(TemaManager.getTextMuted()));
        titleBox.getChildren().addAll(titulo, subtitulo);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnCerrar = inventario.fx.util.ComponentesFX.crearBotonCerrar(dialog::close, 36);

        header.getChildren().addAll(iconContainer, titleBox, btnCerrar);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + TemaManager.getBorder() + ";");
        headerBox.getChildren().addAll(header, sep);
        return headerBox;
    }

    private static VBox crearDescripcion() {
        VBox box = new VBox(8);
        Label titulo = new Label("Descripcion");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titulo.setTextFill(Color.web(TemaManager.getText()));

        Label desc = new Label(
            "Sistema integral de gestion de inventario de equipos de computo. " +
            "Permite registrar, administrar y generar reportes detallados de todos " +
            "los activos tecnologicos de la organizacion."
        );
        desc.setFont(Font.font("Segoe UI", 13));
        desc.setTextFill(Color.web(TemaManager.getTextMuted()));
        desc.setWrapText(true);

        box.getChildren().addAll(titulo, desc);
        return box;
    }

    private static VBox crearCaracteristicas() {
        VBox box = new VBox(8);
        Label titulo = new Label("Caracteristicas Principales");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titulo.setTextFill(Color.web(TemaManager.getText()));

        String[][] items = {
            {"computadora", "Gestion completa de proyectos e inventarios"},
            {"pdf",         "Generacion de reportes PDF con plantillas personalizadas"},
            {"escudo",      "Sistema de seguridad con usuarios y permisos"},
            {"descargar",   "Backup y restauracion de datos"},
            {"rayo",        "Optimizacion de base de datos"},
            {"luna",        "Temas claro y oscuro"},
            {"excel",       "Importacion masiva desde Excel"},
            {"lista",       "Registro detallado de actividad"}
        };

        VBox lista = new VBox(10);
        for (String[] item : items) {
            HBox fila = new HBox(10);
            fila.setAlignment(Pos.CENTER_LEFT);
            javafx.scene.Node icono = crearIconoCaracteristica(item[0]);
            Label lbl = new Label(item[1]);
            lbl.setFont(Font.font("Segoe UI", 12));
            lbl.setTextFill(Color.web(TemaManager.getTextMuted()));
            fila.getChildren().addAll(new StackPane(icono), lbl);
            lista.getChildren().add(fila);
        }

        box.getChildren().addAll(titulo, lista);
        return box;
    }

    private static javafx.scene.Node crearIconoCaracteristica(String tipo) {
        String color = "#10B981";
        return switch (tipo) {
            case "computadora" -> IconosSVG.computadora(color, 16);
            case "pdf"         -> IconosSVG.pdf(color, 16);
            case "escudo"      -> IconosSVG.escudo(color, 16);
            case "descargar"   -> IconosSVG.descargar(color, 16);
            case "rayo"        -> IconosSVG.rayo(color, 16);
            case "luna"        -> IconosSVG.luna(color, 16);
            case "excel"       -> IconosSVG.excel(color, 16);
            case "lista"       -> IconosSVG.lista(color, 16);
            default            -> IconosSVG.info(color, 16);
        };
    }

    private static VBox crearTecnologias() {
        VBox box = new VBox(8);
        Label titulo = new Label("Tecnologias");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titulo.setTextFill(Color.web(TemaManager.getText()));

        HBox techIcons = new HBox(15);
        techIcons.setAlignment(Pos.CENTER_LEFT);

        String[] techs = {"JavaFX 21", "SQLite", "Apache POI", "iText PDF"};
        for (String tech : techs) {
            Label techLabel = new Label(tech);
            techLabel.setStyle(
                "-fx-background-color: " + (TemaManager.isDarkMode() ? "#1E293B" : "#E0E7FF") + "; " +
                "-fx-text-fill: " + (TemaManager.isDarkMode() ? "#A5B4FC" : "#4F46E5") + "; " +
                "-fx-background-radius: 6; -fx-padding: 6 12; " +
                "-fx-font-size: 11px; -fx-font-weight: bold;"
            );
            techIcons.getChildren().add(techLabel);
        }

        box.getChildren().addAll(titulo, techIcons);
        return box;
    }

    private static VBox crearCopyright() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16, 0, 0, 0));
        box.setStyle("-fx-border-color: " + TemaManager.getBorder() + "; -fx-border-width: 1 0 0 0;");

        Label copyright = new Label("\u00a9 2026 SELCOMP. Todos los derechos reservados.");
        copyright.setFont(Font.font("Segoe UI", 11));
        copyright.setTextFill(Color.web(TemaManager.getTextMuted()));

        HBox devBox = new HBox(6);
        devBox.setAlignment(Pos.CENTER);
        Label dev = new Label("Desarrollado con el \u2764\ufe0f");
        dev.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        dev.setTextFill(Color.web(TemaManager.getTextMuted()));
        StackPane heart = new StackPane(IconosSVG.estrella("#EF4444", 14));
        devBox.getChildren().addAll(dev, heart);

        box.getChildren().addAll(copyright, devBox);
        return box;
    }
}
