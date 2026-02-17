package inventario.fx.service;

import inventario.fx.model.TemaManager;
import inventario.fx.util.AnimacionesFX;

import javafx.animation.*;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Controlador centralizado de navegacion y creacion de ventanas/dialogos.
 * Estandariza los patrones de Stage/Scene que se repiten en todo el proyecto.
 */
public class NavigationController {

    private NavigationController() {}

    // ═══════════════════════════════════════════════════════════════════
    //  CREAR DIALOGOS / STAGES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Crea un Stage tipo dialogo con borde transparente (para dialogos custom).
     * Patron mas usado en la app: sin decoracion, modal, fondo transparente.
     */
    public static Stage crearDialogo(Stage owner, String titulo) {
        Stage dialog = new Stage();
        dialog.setTitle(titulo);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.TRANSPARENT);
        return dialog;
    }

    /**
     * Crea un Stage tipo dialogo con modalidad especifica.
     */
    public static Stage crearDialogo(Stage owner, String titulo, Modality modalidad) {
        Stage dialog = new Stage();
        dialog.setTitle(titulo);
        dialog.initOwner(owner);
        dialog.initModality(modalidad);
        dialog.initStyle(StageStyle.TRANSPARENT);
        return dialog;
    }

    /**
     * Crea un Stage sin decoracion (UNDECORATED).
     * Usado para formularios y paneles que tienen barra de titulo custom.
     */
    public static Stage crearVentanaSinBorde(Stage owner, String titulo) {
        Stage stage = new Stage();
        stage.setTitle(titulo);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.initStyle(StageStyle.UNDECORATED);
        return stage;
    }

    /**
     * Crea una Scene con fondo transparente y aplica el tema actual.
     * Patron comun para dialogos con esquinas redondeadas.
     */
    public static Scene crearSceneTransparente(Node root, double width, double height) {
        javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane(root);
        wrapper.setStyle("-fx-background-color: transparent;");
        wrapper.setPadding(new javafx.geometry.Insets(20));
        Scene scene = new Scene(wrapper, width, height);
        scene.setFill(Color.TRANSPARENT);
        TemaManager.aplicarTema(scene);
        return scene;
    }

    /**
     * Crea una Scene con fondo transparente sin dimensiones fijas.
     */
    public static Scene crearSceneTransparente(Node root) {
        javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane(root);
        wrapper.setStyle("-fx-background-color: transparent;");
        wrapper.setPadding(new javafx.geometry.Insets(20));
        Scene scene = new Scene(wrapper);
        scene.setFill(Color.TRANSPARENT);
        TemaManager.aplicarTema(scene);
        return scene;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TRANSICIONES DE NAVEGACION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Muestra un Stage con transicion fade-in.
     */
    public static void mostrarConFadeIn(Stage stage, Node root) {
        mostrarConFadeIn(stage, root, 300);
    }

    /**
     * Muestra un Stage con transicion fade-in y duracion personalizada.
     */
    public static void mostrarConFadeIn(Stage stage, Node root, int duracionMs) {
        root.setOpacity(0);
        root.setScaleX(0.96);
        root.setScaleY(0.96);
        stage.show();
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(duracionMs), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(duracionMs + 50), root);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        new ParallelTransition(fadeIn, scaleIn).play();
    }

    /**
     * Cierra un Stage con transicion fade-out y ejecuta una accion al terminar.
     */
    public static void cerrarConFadeOut(Stage stage, Runnable onFinished) {
        cerrarConFadeOut(stage, 200, onFinished);
    }

    /**
     * Cierra un Stage con fade-out, duracion personalizada y accion al terminar.
     */
    public static void cerrarConFadeOut(Stage stage, int duracionMs, Runnable onFinished) {
        if (stage == null || stage.getScene() == null) {
            if (onFinished != null) onFinished.run();
            return;
        }
        Node root = stage.getScene().getRoot();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(duracionMs), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
        
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(duracionMs), root);
        scaleOut.setToX(0.96);
        scaleOut.setToY(0.96);
        scaleOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
        
        ParallelTransition exit = new ParallelTransition(fadeOut, scaleOut);
        exit.setOnFinished(e -> {
            stage.close();
            if (onFinished != null) onFinished.run();
        });
        exit.play();
    }

    /**
     * Navega de un Stage a otro con transicion fade.
     * Cierra el Stage actual y muestra/abre el parent.
     */
    public static void volverA(Stage stageActual, Stage stageDestino) {
        cerrarConFadeOut(stageActual, () -> {
            if (stageDestino != null) {
                stageDestino.show();
                stageDestino.toFront();
            }
        });
    }
}
