package inventario.fx.ui.firma;
import inventario.fx.model.TemaManager;
import inventario.fx.util.AppLogger;

import javafx.embed.swing.SwingFXUtils;
import inventario.fx.util.AnimacionesFX;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente de firma digital con diseño moderno y profesional.
 * Permite firmar con mouse/táctil o cargar imagen de firma.
 * Compatible con temas oscuro y claro.
 */
public class FirmaPadFX extends VBox {
    
    private static final double CANVAS_WIDTH = 400;
    private static final double CANVAS_HEIGHT = 200;
    private static final double STROKE_WIDTH = 2.5;
    private static final int DPI_SCALE = 3; // Factor de escala para alta resolución (3x = 1200x600px)
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final List<Point> currentStroke;
    private boolean isEmpty = true;
    private String titulo;
    private File archivoFirmaTemp;
    
    private static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
    }
    
    /**
     * Constructor del componente de firma.
     * @param titulo Título de la firma (ej: "FIRMA TÉCNICO", "FIRMA FUNCIONARIO")
     */
    public FirmaPadFX(String titulo) {
        this.titulo = titulo;
        this.currentStroke = new ArrayList<>();
        
        // Canvas de alta resolución (3x para calidad óptima)
        canvas = new Canvas(CANVAS_WIDTH * DPI_SCALE, CANVAS_HEIGHT * DPI_SCALE);
        canvas.setScaleX(1.0 / DPI_SCALE);
        canvas.setScaleY(1.0 / DPI_SCALE);
        canvas.setWidth(CANVAS_WIDTH);
        canvas.setHeight(CANVAS_HEIGHT);
        
        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(STROKE_WIDTH * DPI_SCALE);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        
        configurarComponente();
        configurarEventosFirma();
        limpiarCanvas();
    }
    
    private void configurarComponente() {
        setSpacing(12);
        setPadding(new Insets(15));
        setAlignment(Pos.CENTER);
        setStyle(getCardStyle());
        
        // Título
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTitulo.setTextFill(Color.web(TemaManager.getText()));
        
        // Contenedor del canvas con borde
        VBox canvasContainer = new VBox(canvas);
        canvasContainer.setAlignment(Pos.CENTER);
        canvasContainer.setStyle(getCanvasStyle());
        canvasContainer.setPadding(new Insets(5));
        
        // Botones de acción
        HBox botonesBox = new HBox(10);
        botonesBox.setAlignment(Pos.CENTER);
        
        Button btnLimpiar = crearBoton("Limpiar", "#EF4444", "#DC2626");
        btnLimpiar.setOnAction(e -> limpiarCanvas());
        
        Button btnCargar = crearBoton("Cargar Imagen", TemaManager.COLOR_PRIMARY, TemaManager.COLOR_PRIMARY_DARK);
        btnCargar.setOnAction(e -> cargarImagenFirma());
        
        botonesBox.getChildren().addAll(btnLimpiar, btnCargar);
        
        // Instrucciones
        Label lblInstrucciones = new Label("Firme en el recuadro o cargue una imagen");
        lblInstrucciones.setFont(Font.font("Segoe UI", 10));
        lblInstrucciones.setTextFill(Color.web(TemaManager.getTextMuted()));
        
        getChildren().addAll(lblTitulo, canvasContainer, botonesBox, lblInstrucciones);
    }
    
    private void configurarEventosFirma() {
        canvas.setCursor(Cursor.CROSSHAIR);
        
        canvas.setOnMousePressed(e -> {
            currentStroke.clear();
            currentStroke.add(new Point(e.getX() * DPI_SCALE, e.getY() * DPI_SCALE));
            isEmpty = false;
        });
        
        canvas.setOnMouseDragged(e -> {
            if (!currentStroke.isEmpty()) {
                Point ultimoPunto = currentStroke.get(currentStroke.size() - 1);
                double x = e.getX() * DPI_SCALE;
                double y = e.getY() * DPI_SCALE;
                
                // Dibujar línea suave con anti-aliasing
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(STROKE_WIDTH * DPI_SCALE);
                gc.strokeLine(ultimoPunto.x, ultimoPunto.y, x, y);
                
                currentStroke.add(new Point(x, y));
            }
        });
        
        canvas.setOnMouseReleased(e -> {
            currentStroke.clear();
        });
        
        // Soporte táctil
        canvas.setOnTouchPressed(e -> {
            currentStroke.clear();
            currentStroke.add(new Point(
                e.getTouchPoint().getX() * DPI_SCALE, 
                e.getTouchPoint().getY() * DPI_SCALE
            ));
            isEmpty = false;
        });
        
        canvas.setOnTouchMoved(e -> {
            if (!currentStroke.isEmpty()) {
                Point ultimoPunto = currentStroke.get(currentStroke.size() - 1);
                double x = e.getTouchPoint().getX() * DPI_SCALE;
                double y = e.getTouchPoint().getY() * DPI_SCALE;
                
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(STROKE_WIDTH * DPI_SCALE);
                gc.strokeLine(ultimoPunto.x, ultimoPunto.y, x, y);
                
                currentStroke.add(new Point(x, y));
            }
        });
        
        canvas.setOnTouchReleased(e -> {
            currentStroke.clear();
        });
    }
    
    private void limpiarCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        isEmpty = true;
        
        // Limpiar archivo temporal si existe
        if (archivoFirmaTemp != null && archivoFirmaTemp.exists()) {
            archivoFirmaTemp.delete();
            archivoFirmaTemp = null;
        }
    }
    
    private void cargarImagenFirma() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Firma");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg")
        );
        
        File archivo = fileChooser.showOpenDialog(getScene().getWindow());
        if (archivo != null) {
            try {
                Image imagen = new Image(archivo.toURI().toString());
                
                // Limpiar canvas primero
                limpiarCanvas();
                
                // Calcular dimensiones para ajustar la imagen manteniendo proporción
                double imgWidth = imagen.getWidth();
                double imgHeight = imagen.getHeight();
                double canvasW = canvas.getWidth();
                double canvasH = canvas.getHeight();
                
                double escalaX = canvasW / imgWidth;
                double escalaY = canvasH / imgHeight;
                double escala = Math.min(escalaX, escalaY);
                
                double nuevoAncho = imgWidth * escala;
                double nuevoAlto = imgHeight * escala;
                
                // Centrar imagen
                double x = (canvasW - nuevoAncho) / 2;
                double y = (canvasH - nuevoAlto) / 2;
                
                gc.drawImage(imagen, x, y, nuevoAncho, nuevoAlto);
                isEmpty = false;
                
                System.out.println("✓ Imagen de firma cargada: " + archivo.getName());
            } catch (Exception e) {
                System.err.println("Error al cargar imagen de firma: " + e.getMessage());
            }
        }
    }
    
    /**
     * Guarda la firma como PNG de alta calidad y retorna la ruta del archivo.
     * @return Ruta del archivo PNG generado o null si no hay firma
     */
    public String guardarFirma() {
        if (isEmpty) {
            return null;
        }
        
        try {
            // Crear directorio temporal para firmas si no existe
            Path dirFirmas = Files.createTempDirectory("firmas_reportes_");
            
            // Capturar canvas como imagen
            WritableImage writableImage = new WritableImage(
                (int)canvas.getWidth(), 
                (int)canvas.getHeight()
            );
            canvas.snapshot(null, writableImage);
            
            // Convertir a BufferedImage de alta calidad
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
            
            // Guardar como PNG con máxima compresión
            String nombreArchivo = "firma_" + System.currentTimeMillis() + ".png";
            archivoFirmaTemp = new File(dirFirmas.toFile(), nombreArchivo);
            
            ImageIO.write(bufferedImage, "PNG", archivoFirmaTemp);
            
            System.out.println("✓ Firma guardada: " + archivoFirmaTemp.getAbsolutePath());
            System.out.println("  Resolución: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight() + "px");
            
            return archivoFirmaTemp.getAbsolutePath();
            
        } catch (IOException e) {
            AppLogger.getLogger(FirmaPadFX.class).error("Error al guardar firma: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Verifica si el pad de firma está vacío.
     */
    public boolean estaVacio() {
        return isEmpty;
    }
    
    private Button crearBoton(String texto, String colorNormal, String colorHover) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        btn.setTextFill(Color.WHITE);
        btn.setStyle(
            "-fx-background-color: " + colorNormal + ";" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 16;"
        );
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + colorHover + ";" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 8 16;"
            );
            AnimacionesFX.hoverIn(btn, 1.05, 120);
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + colorNormal + ";" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 8 16;"
            );
            AnimacionesFX.hoverOut(btn, 120);
        });
        
        return btn;
    }
    
    private String getCardStyle() {
        return "-fx-background-color: " + TemaManager.getBg() + ";" +
               "-fx-background-radius: 8;" +
               "-fx-border-color: " + TemaManager.getBorder() + ";" +
               "-fx-border-radius: 8;" +
               "-fx-border-width: 1;";
    }
    
    private String getCanvasStyle() {
        return "-fx-background-color: white;" +
               "-fx-background-radius: 4;" +
               "-fx-border-color: " + TemaManager.getBorder() + ";" +
               "-fx-border-radius: 4;" +
               "-fx-border-width: 2;";
    }
    
}
