package inventario.fx.ui.firma;
import inventario.fx.ui.dialog.DialogosFX;
import inventario.fx.icons.IconosSVG;
import inventario.fx.model.TemaManager;
import inventario.fx.util.AppLogger;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import inventario.fx.model.TemaManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel moderno y profesional para captura de firma digital.
 * Permite dibujar con mouse/táctil o cargar imagen de archivo.
 * Compatible con modo claro/oscuro.
 */
public class FirmaDigitalPanel extends VBox {
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final List<Point> currentStroke = new ArrayList<>();
    private final List<List<Point>> allStrokes = new ArrayList<>();
    private Image loadedImage = null;
    private boolean hayFirma = false;
    private final String titulo;
    
    private static final double CANVAS_WIDTH = 400;
    private static final double CANVAS_HEIGHT = 200;
    private static final double STROKE_WIDTH = 2.0;
    private static final Color STROKE_COLOR = Color.BLACK;
    private static final Color CANVAS_BG = Color.WHITE;
    
    
    private static class Point {
        double x, y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    
    public FirmaDigitalPanel(String titulo) {
        this.titulo = titulo;
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: " + TemaManager.getSurface() + "; " +
                     "-fx-background-radius: 8px; " +
                     "-fx-border-color: " + TemaManager.getBorder() + "; " +
                     "-fx-border-radius: 8px; " +
                     "-fx-border-width: 1px;");
        
        // Título
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblTitulo.setTextFill(Color.web(TemaManager.COLOR_PRIMARY));
        
        // Canvas para dibujar
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        // Estilo del canvas
        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle("-fx-background-color: white; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-border-color: " + TemaManager.getBorder() + "; " +
                                "-fx-border-radius: 6px; " +
                                "-fx-border-width: 2px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        canvasContainer.setMaxSize(CANVAS_WIDTH + 4, CANVAS_HEIGHT + 4);
        
        limpiarCanvas();
        configurarEventosCanvas();
        
        // Botones de acción
        HBox botonesBox = new HBox(10);
        botonesBox.setAlignment(Pos.CENTER);
        
        Button btnLimpiar = crearBoton("Limpiar", IconosSVG.eliminar("#FFFFFF", 16));
        btnLimpiar.setOnAction(e -> limpiar());
        
        Button btnCargar = crearBoton("Cargar Imagen", IconosSVG.imagen("#FFFFFF", 16));
        btnCargar.setOnAction(e -> cargarImagen());
        
        botonesBox.getChildren().addAll(btnLimpiar, btnCargar);
        
        // Indicador de estado
        Label lblEstado = new Label("Dibuje su firma o cargue una imagen");
        lblEstado.setFont(Font.font("Segoe UI", 11));
        lblEstado.setTextFill(Color.web(TemaManager.getTextMuted()));
        
        this.getChildren().addAll(lblTitulo, canvasContainer, botonesBox, lblEstado);
    }
    
    private void configurarEventosCanvas() {
        canvas.setCursor(Cursor.CROSSHAIR);
        
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            currentStroke.clear();
            currentStroke.add(new Point(e.getX(), e.getY()));
            hayFirma = true;
            loadedImage = null; // Invalida imagen cargada si se dibuja
        });
        
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            double x = Math.max(0, Math.min(e.getX(), CANVAS_WIDTH));
            double y = Math.max(0, Math.min(e.getY(), CANVAS_HEIGHT));
            
            if (!currentStroke.isEmpty()) {
                Point lastPoint = currentStroke.get(currentStroke.size() - 1);
                
                // Dibujar línea suave
                gc.setStroke(STROKE_COLOR);
                gc.setLineWidth(STROKE_WIDTH);
                gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                gc.strokeLine(lastPoint.x, lastPoint.y, x, y);
                
                currentStroke.add(new Point(x, y));
            }
        });
        
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (!currentStroke.isEmpty()) {
                allStrokes.add(new ArrayList<>(currentStroke));
                currentStroke.clear();
            }
        });
    }
    
    private void limpiarCanvas() {
        gc.setFill(CANVAS_BG);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Línea de guía sutil
        gc.setStroke(Color.web(TemaManager.getBorderLight(), 0.5));
        gc.setLineWidth(1);
        gc.setLineDashes(5, 5);
        gc.strokeLine(20, CANVAS_HEIGHT - 40, CANVAS_WIDTH - 20, CANVAS_HEIGHT - 40);
        gc.setLineDashes(0);
    }
    
    public void limpiar() {
        allStrokes.clear();
        currentStroke.clear();
        loadedImage = null;
        hayFirma = false;
        limpiarCanvas();
    }
    
    private void cargarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Firma");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );
        
        Stage stage = (Stage) this.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                Image img = new Image(file.toURI().toString());
                loadedImage = img;
                hayFirma = true;
                allStrokes.clear(); // Limpiar trazos dibujados
                
                limpiarCanvas();
                
                // Dibujar imagen centrada y ajustada
                double aspectRatio = img.getWidth() / img.getHeight();
                double drawWidth, drawHeight;
                
                if (aspectRatio > CANVAS_WIDTH / CANVAS_HEIGHT) {
                    drawWidth = CANVAS_WIDTH - 40;
                    drawHeight = drawWidth / aspectRatio;
                } else {
                    drawHeight = CANVAS_HEIGHT - 40;
                    drawWidth = drawHeight * aspectRatio;
                }
                
                double x = (CANVAS_WIDTH - drawWidth) / 2;
                double y = (CANVAS_HEIGHT - drawHeight) / 2;
                
                gc.drawImage(img, x, y, drawWidth, drawHeight);
                
            } catch (Exception ex) {
                AppLogger.getLogger(FirmaDigitalPanel.class).error("Error: " + ex.getMessage(), ex);
                DialogosFX.mostrarAlerta((Stage) this.getScene().getWindow(), "Error al cargar imagen", "No se pudo cargar la imagen seleccionada.", javafx.scene.control.Alert.AlertType.ERROR);
            }
        }
    }
    
    public boolean tieneFirma() {
        return hayFirma;
    }
    
    /**
     * Exporta la firma como imagen PNG de alta calidad.
     * @return File temporal con la imagen, o null si no hay firma
     */
    public File exportarComoImagen() {
        if (!hayFirma) {
            return null;
        }
        
        try {
            // Crear imagen de alta resolución (2x para mejor calidad)
            int scale = 2;
            WritableImage writableImage = new WritableImage(
                (int)(CANVAS_WIDTH * scale), 
                (int)(CANVAS_HEIGHT * scale)
            );
            
            Canvas tempCanvas = new Canvas(CANVAS_WIDTH * scale, CANVAS_HEIGHT * scale);
            GraphicsContext tempGc = tempCanvas.getGraphicsContext2D();
            
            // Fondo blanco
            tempGc.setFill(CANVAS_BG);
            tempGc.fillRect(0, 0, CANVAS_WIDTH * scale, CANVAS_HEIGHT * scale);
            
            if (loadedImage != null) {
                // Dibujar imagen cargada
                double aspectRatio = loadedImage.getWidth() / loadedImage.getHeight();
                double drawWidth, drawHeight;
                
                if (aspectRatio > CANVAS_WIDTH / CANVAS_HEIGHT) {
                    drawWidth = (CANVAS_WIDTH - 40) * scale;
                    drawHeight = drawWidth / aspectRatio;
                } else {
                    drawHeight = (CANVAS_HEIGHT - 40) * scale;
                    drawWidth = drawHeight * aspectRatio;
                }
                
                double x = ((CANVAS_WIDTH - drawWidth / scale) / 2) * scale;
                double y = ((CANVAS_HEIGHT - drawHeight / scale) / 2) * scale;
                
                tempGc.drawImage(loadedImage, x, y, drawWidth, drawHeight);
                
            } else {
                // Redibujar todos los trazos con alta calidad
                tempGc.setStroke(STROKE_COLOR);
                tempGc.setLineWidth(STROKE_WIDTH * scale);
                tempGc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                tempGc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                
                for (List<Point> stroke : allStrokes) {
                    if (stroke.size() > 1) {
                        tempGc.beginPath();
                        tempGc.moveTo(stroke.get(0).x * scale, stroke.get(0).y * scale);
                        for (int i = 1; i < stroke.size(); i++) {
                            tempGc.lineTo(stroke.get(i).x * scale, stroke.get(i).y * scale);
                        }
                        tempGc.stroke();
                    }
                }
            }
            
            tempCanvas.snapshot(null, writableImage);
            
            // Convertir a BufferedImage y guardar
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
            
            // Crear archivo temporal
            File tempFile = File.createTempFile("firma_" + titulo.toLowerCase().replace(" ", "_") + "_", ".png");
            tempFile.deleteOnExit();
            
            ImageIO.write(bufferedImage, "PNG", tempFile);
            
            return tempFile;
            
        } catch (IOException e) {
            AppLogger.getLogger(FirmaDigitalPanel.class).error("Error: " + e.getMessage(), e);
            DialogosFX.mostrarAlerta((Stage) this.getScene().getWindow(), "Error al exportar firma", "No se pudo generar la imagen de la firma.", javafx.scene.control.Alert.AlertType.ERROR);
            return null;
        }
    }
    
    private Button crearBoton(String texto, javafx.scene.Node icono) {
        Button btn = new Button();
        
        HBox contenido = new HBox(8);
        contenido.setAlignment(Pos.CENTER);
        
        if (icono != null) {
            contenido.getChildren().add(icono);
        }
        
        Label lblTexto = new Label(texto);
        lblTexto.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        lblTexto.setTextFill(Color.WHITE);
        contenido.getChildren().add(lblTexto);
        
        btn.setGraphic(contenido);
        btn.setStyle(
            "-fx-background-color: " + TemaManager.COLOR_PRIMARY + ";" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + TemaManager.COLOR_PRIMARY_HOVER + ";" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + TemaManager.COLOR_PRIMARY + ";" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;"
        ));
        
        return btn;
    }
}
