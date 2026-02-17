package inventario.fx.ui.component;

import inventario.fx.util.AppLogger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Versión simplificada del splash screen para debugging.
 */
public class SimpleSplash {
    
    public static void show(Runnable onComplete) {
        try {
            Platform.runLater(() -> {
                try {
                    System.out.println("🎬 [SimpleSplash] Iniciando...");
                    
                    Stage stage = new Stage();
                    stage.initStyle(StageStyle.UNDECORATED);
                    
                    VBox root = new VBox();
                    root.setAlignment(Pos.CENTER);
                    root.setStyle("-fx-background-color: white;");
                    
                    Label label = new Label("SELCOMP - Cargando...");
                    label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
                    
                    root.getChildren().add(label);
                    
                    Scene scene = new Scene(root, 400, 200);
                    stage.setScene(scene);
                    stage.centerOnScreen();
                    stage.show();
                    
                    System.out.println("✅ [SimpleSplash] Mostrado correctamente");
                    
                    // Cerrar después de 2 segundos
                    javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(Duration.millis(2000), e -> {
                            stage.close();
                            System.out.println("🏁 [SimpleSplash] Finalizado");
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        })
                    );
                    timeline.play();
                    
                } catch (Exception e) {
                    AppLogger.getLogger(SimpleSplash.class).error("❌ [SimpleSplash] Error: " + e.getMessage(), e);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.getLogger(SimpleSplash.class).error("❌ [SimpleSplash] Error crítico: " + e.getMessage(), e);
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
}