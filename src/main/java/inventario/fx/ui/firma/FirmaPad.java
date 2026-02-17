package inventario.fx.ui.firma;
import inventario.fx.model.TemaManager;
import inventario.fx.util.AppLogger;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Componente de firma digital con canvas para dibujar con mouse o touch.
 * Diseño moderno y profesional compatible con tema oscuro/claro.
 */
public class FirmaPad extends VBox {
    
    private Canvas canvas;
    private GraphicsContext gc;
    private String title;
    private List<Point> currentStroke;
    private List<List<Point>> allStrokes;
    private File savedImageFile;
    
    // Dimensiones del canvas
    private static final double CANVAS_WIDTH = 350;
    private static final double CANVAS_HEIGHT = 150;
    private static final double STROKE_WIDTH = 2.0;
    
    // Clase interna para puntos
    private static class Point {
        double x, y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    
    /**
     * Constructor del componente de firma.
     * @param title Título de la firma (ej: "TÉCNICO", "FUNCIONARIO")
     */
    public FirmaPad(String title) {
        this.title = title;
        this.allStrokes = new ArrayList<>();
        
        setAlignment(Pos.CENTER);
        setSpacing(12);
        setPrefWidth(CANVAS_WIDTH + 40);
        
        crearInterfaz();
    }
    
    private void crearInterfaz() {
        // Título
        Label lblTitle = new Label(title);
        lblTitle.setStyle(
            "-fx-font-family: 'Segoe UI';" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: " + TemaManager.COLOR_INFO + ";"
        );
        
        // Canvas container con borde
        VBox canvasContainer = new VBox();
        canvasContainer.setAlignment(Pos.CENTER);
        canvasContainer.setPrefWidth(CANVAS_WIDTH);
        canvasContainer.setPrefHeight(CANVAS_HEIGHT);
        canvasContainer.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        );
        
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        // Configurar canvas
        limpiarCanvas();
        configurarEventos();
        
        canvasContainer.getChildren().add(canvas);
        
        // Botones de acciones
        HBox acciones = new HBox(8);
        acciones.setAlignment(Pos.CENTER);
        
        Button btnLimpiar = crearBoton("Limpiar", TemaManager.COLOR_DANGER, TemaManager.COLOR_DANGER_HOVER);
        btnLimpiar.setOnAction(e -> limpiar());
        
        Button btnCargar = crearBoton("Cargar Imagen", "#8B5CF6", "#7C3AED");
        btnCargar.setOnAction(e -> cargarImagen());
        
        Button btnAmpliar = crearBoton("Ampliar", "#10B981", "#059669");
        btnAmpliar.setOnAction(e -> abrirEditorAmpliado());
        
        acciones.getChildren().addAll(btnLimpiar, btnCargar, btnAmpliar);
        
        getChildren().addAll(lblTitle, canvasContainer, acciones);
    }
    
    private void configurarEventos() {
        canvas.setOnMousePressed(e -> {
            currentStroke = new ArrayList<>();
            currentStroke.add(new Point(e.getX(), e.getY()));
        });
        
        canvas.setOnMouseDragged(e -> {
            if (currentStroke != null) {
                double x = e.getX();
                double y = e.getY();
                
                // Obtener el último punto
                Point lastPoint = currentStroke.get(currentStroke.size() - 1);
                
                // Dibujar línea suave
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(STROKE_WIDTH);
                gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                gc.strokeLine(lastPoint.x, lastPoint.y, x, y);
                
                currentStroke.add(new Point(x, y));
            }
        });
        
        canvas.setOnMouseReleased(e -> {
            if (currentStroke != null && currentStroke.size() > 0) {
                allStrokes.add(currentStroke);
                currentStroke = null;
            }
        });
    }
    
    private void limpiarCanvas() {
        gc.setFill(TemaManager.getSurfaceColor());
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    public void limpiar() {
        allStrokes.clear();
        limpiarCanvas();
        savedImageFile = null;
    }
    
    private void redibujar() {
        limpiarCanvas();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(STROKE_WIDTH);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        
        for (List<Point> stroke : allStrokes) {
            if (stroke.size() < 2) continue;
            for (int i = 1; i < stroke.size(); i++) {
                Point p1 = stroke.get(i - 1);
                Point p2 = stroke.get(i);
                gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }
    
    private void cargarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Firma");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                Image img = new Image(file.toURI().toString());
                limpiarCanvas();
                
                // Calcular escala para ajustar la imagen al canvas
                double scale = Math.min(
                    canvas.getWidth() / img.getWidth(),
                    canvas.getHeight() / img.getHeight()
                );
                double w = img.getWidth() * scale;
                double h = img.getHeight() * scale;
                double x = (canvas.getWidth() - w) / 2;
                double y = (canvas.getHeight() - h) / 2;
                
                gc.drawImage(img, x, y, w, h);
                
                // Guardar referencia
                savedImageFile = file;
                allStrokes.clear(); // Limpiar trazos si se carga imagen
                
            } catch (Exception e) {
                System.err.println("Error al cargar imagen: " + e.getMessage());
            }
        }
    }
    
    private void abrirEditorAmpliado() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(getScene().getWindow());
        stage.setTitle("Editor de Firma - " + title);
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + TemaManager.getBg() + ";");
        
        // Canvas ampliado
        Canvas bigCanvas = new Canvas(600, 300);
        GraphicsContext bigGc = bigCanvas.getGraphicsContext2D();
        
        VBox canvasBox = new VBox();
        canvasBox.setAlignment(Pos.CENTER);
        canvasBox.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10;"
        );
        canvasBox.getChildren().add(bigCanvas);
        
        // Limpiar canvas ampliado
        bigGc.setFill(TemaManager.getSurfaceColor());
        bigGc.fillRect(0, 0, bigCanvas.getWidth(), bigCanvas.getHeight());
        
        // Escalar y redibujar trazos actuales
        double scaleX = bigCanvas.getWidth() / canvas.getWidth();
        double scaleY = bigCanvas.getHeight() / canvas.getHeight();
        
        bigGc.setStroke(Color.BLACK);
        bigGc.setLineWidth(STROKE_WIDTH * 1.5);
        bigGc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        bigGc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        
        for (List<Point> stroke : allStrokes) {
            if (stroke.size() < 2) continue;
            for (int i = 1; i < stroke.size(); i++) {
                Point p1 = stroke.get(i - 1);
                Point p2 = stroke.get(i);
                bigGc.strokeLine(p1.x * scaleX, p1.y * scaleY, p2.x * scaleX, p2.y * scaleY);
            }
        }
        
        // Eventos del canvas ampliado
        List<Point> tempStroke = new ArrayList<>();
        
        bigCanvas.setOnMousePressed(e -> {
            tempStroke.clear();
            tempStroke.add(new Point(e.getX(), e.getY()));
        });
        
        bigCanvas.setOnMouseDragged(e -> {
            if (!tempStroke.isEmpty()) {
                Point last = tempStroke.get(tempStroke.size() - 1);
                bigGc.strokeLine(last.x, last.y, e.getX(), e.getY());
                tempStroke.add(new Point(e.getX(), e.getY()));
            }
        });
        
        bigCanvas.setOnMouseReleased(e -> {
            if (!tempStroke.isEmpty()) {
                // Escalar de vuelta al tamaño original y agregar
                List<Point> scaledStroke = new ArrayList<>();
                for (Point p : tempStroke) {
                    scaledStroke.add(new Point(p.x / scaleX, p.y / scaleY));
                }
                allStrokes.add(scaledStroke);
                tempStroke.clear();
            }
        });
        
        // Botones
        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);
        
        Button btnLimpiarBig = crearBoton("Limpiar", TemaManager.COLOR_DANGER, TemaManager.COLOR_DANGER_HOVER);
        btnLimpiarBig.setOnAction(e -> {
            bigGc.setFill(TemaManager.getSurfaceColor());
            bigGc.fillRect(0, 0, bigCanvas.getWidth(), bigCanvas.getHeight());
            allStrokes.clear();
        });
        
        Button btnGuardar = crearBoton("Guardar", "#10B981", "#059669");
        btnGuardar.setOnAction(e -> {
            redibujar();
            stage.close();
        });
        
        Button btnCancelar = crearBoton("Cancelar", "#6B7280", "#4B5563");
        btnCancelar.setOnAction(e -> stage.close());
        
        botones.getChildren().addAll(btnLimpiarBig, btnGuardar, btnCancelar);
        
        root.getChildren().addAll(canvasBox, botones);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
    private Button crearBoton(String texto, String colorNormal, String colorHover) {
        Button btn = new Button(texto);
        btn.setStyle(
            "-fx-background-color: " + colorNormal + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 6 12;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + colorHover + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 6 12;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + colorNormal + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 6 12;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        
        return btn;
    }
    
    /**
     * Verifica si el canvas tiene contenido (firma dibujada o imagen cargada).
     */
    public boolean tieneContenido() {
        return !allStrokes.isEmpty() || savedImageFile != null;
    }
    
    /**
     * Guarda la firma como archivo PNG y retorna la ruta.
     * @param fileName Nombre del archivo sin extensión
     * @return Ruta del archivo guardado o null si está vacío
     */
    public String guardarComoImagen(String fileName) {
        if (!tieneContenido()) {
            return null;
        }
        
        try {
            // Crear directorio temporal si no existe
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "firmas_reportes");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            File outputFile = new File(tempDir, fileName + ".png");
            
            // Crear imagen de alta calidad (2x resolución)
            int width = (int) canvas.getWidth() * 2;
            int height = (int) canvas.getHeight() * 2;
            
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2d = bufferedImage.createGraphics();
            
            // Configurar renderizado de alta calidad
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, 
                                java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, 
                                java.awt.RenderingHints.VALUE_STROKE_PURE);
            
            // Fondo blanco
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            
            // Dibujar trazos escalados
            g2d.setColor(java.awt.Color.BLACK);
            g2d.setStroke(new java.awt.BasicStroke(
                (float)(STROKE_WIDTH * 2), 
                java.awt.BasicStroke.CAP_ROUND, 
                java.awt.BasicStroke.JOIN_ROUND
            ));
            
            for (List<Point> stroke : allStrokes) {
                if (stroke.size() < 2) continue;
                for (int i = 1; i < stroke.size(); i++) {
                    Point p1 = stroke.get(i - 1);
                    Point p2 = stroke.get(i);
                    g2d.drawLine(
                        (int)(p1.x * 2), (int)(p1.y * 2), 
                        (int)(p2.x * 2), (int)(p2.y * 2)
                    );
                }
            }
            
            g2d.dispose();
            
            // Guardar imagen
            ImageIO.write(bufferedImage, "PNG", outputFile);
            
            System.out.println("✓ Firma guardada: " + outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();
            
        } catch (IOException e) {
            AppLogger.getLogger(FirmaPad.class).error("Error al guardar firma: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Carga una firma desde un archivo.
     */
    public void cargarDesdeArchivo(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        
        File file = new File(filePath);
        if (file.exists()) {
            try {
                Image img = new Image(file.toURI().toString());
                limpiarCanvas();
                
                double scale = Math.min(
                    canvas.getWidth() / img.getWidth(),
                    canvas.getHeight() / img.getHeight()
                );
                double w = img.getWidth() * scale;
                double h = img.getHeight() * scale;
                double x = (canvas.getWidth() - w) / 2;
                double y = (canvas.getHeight() - h) / 2;
                
                gc.drawImage(img, x, y, w, h);
                savedImageFile = file;
                allStrokes.clear();
                
            } catch (Exception e) {
                System.err.println("Error al cargar firma desde archivo: " + e.getMessage());
            }
        }
    }
}
