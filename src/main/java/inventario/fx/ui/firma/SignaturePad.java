package inventario.fx.ui.firma;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Componente de firma digital con soporte táctil y mouse.
 * Permite firmar directamente o cargar una imagen de firma.
 * Genera imágenes PNG de alta calidad con trazos suaves y anti-aliasing.
 */
public class SignaturePad extends VBox {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private boolean isDrawing = false;
    private double lastX, lastY;
    private final List<Point> currentStroke = new ArrayList<>();
    private final List<List<Point>> allStrokes = new ArrayList<>();
    private String savedImagePath = null;
    private boolean hasSignature = false;
    
    // Configuración de dibujo
    private static final double LINE_WIDTH = 2.5;
    private static final Color STROKE_COLOR = Color.rgb(0, 0, 0);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    
    // Tamaño del canvas
    private static final int CANVAS_WIDTH = 400;
    private static final int CANVAS_HEIGHT = 150;
    
    // Factor de escala para mayor calidad (renderizado interno)
    private static final int SCALE_FACTOR = 3;

    /**
     * Clase interna para representar un punto del trazo
     */
    private static class Point {
        final double x, y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Constructor del componente SignaturePad
     */
    public SignaturePad(String titulo) {
        super(8);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: " + TemaManager.getSurface() + ";" +
                 "-fx-background-radius: 8;" +
                 "-fx-border-color: " + TemaManager.getBorder() + ";" +
                 "-fx-border-radius: 8;" +
                 "-fx-border-width: 1;");

        // Título
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblTitulo.setTextFill(Color.web(TemaManager.COLOR_PRIMARY));

        // Canvas para firma
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        canvas.setStyle("-fx-cursor: crosshair;" +
                       "-fx-background-color: white;" +
                       "-fx-border-color: " + TemaManager.getBorder() + ";" +
                       "-fx-border-width: 1;" +
                       "-fx-border-radius: 4;");
        
        // Configurar anti-aliasing para trazos suaves
        gc.setImageSmoothing(true);
        limpiarCanvas();

        // Eventos del canvas
        configurarEventosCanvas();

        // Botones de acción
        HBox botonesBox = new HBox(8);
        botonesBox.setAlignment(Pos.CENTER);

        Button btnLimpiar = crearBoton("Limpiar", "#EF4444");
        btnLimpiar.setOnAction(e -> limpiar());

        Button btnCargar = crearBoton("Cargar Imagen", TemaManager.COLOR_INFO);
        btnCargar.setOnAction(e -> cargarImagen());

        botonesBox.getChildren().addAll(btnLimpiar, btnCargar);

        getChildren().addAll(lblTitulo, canvas, botonesBox);
    }

    /**
     * Configura los eventos del canvas para dibujo con mouse y táctil
     */
    private void configurarEventosCanvas() {
        // Mouse pressed - iniciar trazo
        canvas.setOnMousePressed(e -> {
            isDrawing = true;
            lastX = e.getX();
            lastY = e.getY();
            currentStroke.clear();
            currentStroke.add(new Point(lastX, lastY));
        });

        // Mouse dragged - dibujar trazo suave
        canvas.setOnMouseDragged(e -> {
            if (isDrawing) {
                double x = e.getX();
                double y = e.getY();
                
                // Dibujar línea suave con anti-aliasing
                gc.setStroke(STROKE_COLOR);
                gc.setLineWidth(LINE_WIDTH);
                gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                gc.strokeLine(lastX, lastY, x, y);
                
                currentStroke.add(new Point(x, y));
                lastX = x;
                lastY = y;
                hasSignature = true;
            }
        });

        // Mouse released - finalizar trazo
        canvas.setOnMouseReleased(e -> {
            if (isDrawing && !currentStroke.isEmpty()) {
                allStrokes.add(new ArrayList<>(currentStroke));
                isDrawing = false;
            }
        });

        // Soporte táctil (pantallas touch)
        canvas.setOnTouchPressed(e -> {
            isDrawing = true;
            lastX = e.getTouchPoint().getX();
            lastY = e.getTouchPoint().getY();
            currentStroke.clear();
            currentStroke.add(new Point(lastX, lastY));
            e.consume();
        });

        canvas.setOnTouchMoved(e -> {
            if (isDrawing) {
                double x = e.getTouchPoint().getX();
                double y = e.getTouchPoint().getY();
                
                gc.setStroke(STROKE_COLOR);
                gc.setLineWidth(LINE_WIDTH);
                gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                gc.strokeLine(lastX, lastY, x, y);
                
                currentStroke.add(new Point(x, y));
                lastX = x;
                lastY = y;
                hasSignature = true;
            }
            e.consume();
        });

        canvas.setOnTouchReleased(e -> {
            if (isDrawing && !currentStroke.isEmpty()) {
                allStrokes.add(new ArrayList<>(currentStroke));
                isDrawing = false;
            }
            e.consume();
        });
    }

    /**
     * Limpia el canvas y elimina la firma
     */
    private void limpiarCanvas() {
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Dibujar línea guía sutil
        gc.setStroke(Color.rgb(200, 200, 200));
        gc.setLineWidth(1);
        gc.strokeLine(20, CANVAS_HEIGHT - 30, CANVAS_WIDTH - 20, CANVAS_HEIGHT - 30);
        
        // Texto guía
        gc.setFill(Color.rgb(180, 180, 180));
        gc.setFont(Font.font("Segoe UI", 11));
        gc.fillText("Firme aquí con el mouse o el dedo", CANVAS_WIDTH / 2 - 110, CANVAS_HEIGHT / 2);
    }

    /**
     * Limpia la firma y reinicia el canvas
     */
    public void limpiar() {
        allStrokes.clear();
        currentStroke.clear();
        limpiarCanvas();
        savedImagePath = null;
        hasSignature = false;
    }

    /**
     * Carga una imagen de firma desde el disco
     */
    private void cargarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Firma");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("JPG", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                
                // Limpiar canvas
                gc.setFill(BACKGROUND_COLOR);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                
                // Dibujar imagen ajustada al canvas manteniendo aspecto
                double imgWidth = image.getWidth();
                double imgHeight = image.getHeight();
                double canvasWidth = canvas.getWidth();
                double canvasHeight = canvas.getHeight();
                
                double scale = Math.min(canvasWidth / imgWidth, canvasHeight / imgHeight);
                double scaledWidth = imgWidth * scale;
                double scaledHeight = imgHeight * scale;
                double x = (canvasWidth - scaledWidth) / 2;
                double y = (canvasHeight - scaledHeight) / 2;
                
                gc.drawImage(image, x, y, scaledWidth, scaledHeight);
                
                hasSignature = true;
                allStrokes.clear(); // Limpiar trazos previos al cargar imagen
                
            } catch (Exception e) {
                System.err.println("Error al cargar imagen de firma: " + e.getMessage());
            }
        }
    }

    /**
     * Crea un botón estilizado
     */
    private Button crearBoton(String texto, String color) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        btn.setPrefHeight(32);
        btn.setMinWidth(120);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 6 16;"
        );
        
        btn.setOnMouseEntered(e -> btn.setOpacity(0.9));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        
        return btn;
    }

    /**
     * Verifica si hay una firma
     */
    public boolean hasSignature() {
        return hasSignature;
    }

    /**
     * Guarda la firma como imagen PNG de alta calidad y retorna la ruta
     */
    public String guardarFirma() {
        if (!hasSignature) {
            return null;
        }

        try {
            // Crear directorio de firmas si no existe
            Path firmasDir = Paths.get("firmas_temp");
            if (!Files.exists(firmasDir)) {
                Files.createDirectories(firmasDir);
            }

            // Generar nombre único para la imagen
            String filename = "firma_" + UUID.randomUUID().toString() + ".png";
            Path imagePath = firmasDir.resolve(filename);

            // Renderizar canvas a imagen de alta calidad
            WritableImage writableImage = renderizarAltaCalidad();
            
            // Convertir a BufferedImage
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
            
            // Guardar como PNG
            ImageIO.write(bufferedImage, "PNG", imagePath.toFile());
            
            savedImagePath = imagePath.toAbsolutePath().toString();
            System.out.println("✓ Firma guardada: " + savedImagePath);
            
            return savedImagePath;

        } catch (IOException e) {
            AppLogger.getLogger(SignaturePad.class).error("Error al guardar firma: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Renderiza el canvas a una imagen de alta calidad con anti-aliasing
     */
    private WritableImage renderizarAltaCalidad() {
        // Crear imagen con escala superior para mejor calidad
        int width = CANVAS_WIDTH * SCALE_FACTOR;
        int height = CANVAS_HEIGHT * SCALE_FACTOR;
        
        Canvas tempCanvas = new Canvas(width, height);
        GraphicsContext tempGc = tempCanvas.getGraphicsContext2D();
        
        // Habilitar anti-aliasing
        tempGc.setImageSmoothing(true);
        
        // Fondo blanco
        tempGc.setFill(BACKGROUND_COLOR);
        tempGc.fillRect(0, 0, width, height);
        
        // Redibujar todos los trazos con mayor escala
        tempGc.setStroke(STROKE_COLOR);
        tempGc.setLineWidth(LINE_WIDTH * SCALE_FACTOR);
        tempGc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        tempGc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        
        for (List<Point> stroke : allStrokes) {
            if (stroke.size() < 2) continue;
            
            for (int i = 0; i < stroke.size() - 1; i++) {
                Point p1 = stroke.get(i);
                Point p2 = stroke.get(i + 1);
                tempGc.strokeLine(
                    p1.x * SCALE_FACTOR, 
                    p1.y * SCALE_FACTOR, 
                    p2.x * SCALE_FACTOR, 
                    p2.y * SCALE_FACTOR
                );
            }
        }
        
        // Si no hay trazos pero hay contenido (imagen cargada), copiar del canvas actual
        if (allStrokes.isEmpty() && hasSignature) {
            WritableImage snapshot = new WritableImage(CANVAS_WIDTH, CANVAS_HEIGHT);
            canvas.snapshot(null, snapshot);
            
            // Escalar la imagen al tamaño objetivo
            tempGc.drawImage(snapshot, 0, 0, width, height);
        }
        
        WritableImage image = new WritableImage(width, height);
        tempCanvas.snapshot(null, image);
        return image;
    }

    /**
     * Obtiene la ruta de la imagen guardada
     */
    public String getImagePath() {
        return savedImagePath;
    }

    /**
     * Carga una firma existente desde una ruta
     */
    public void cargarFirmaDesdeRuta(String rutaImagen) {
        if (rutaImagen == null || rutaImagen.isEmpty()) {
            return;
        }

        try {
            File file = new File(rutaImagen);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                
                // Limpiar canvas
                gc.setFill(BACKGROUND_COLOR);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                
                // Dibujar imagen ajustada
                double imgWidth = image.getWidth();
                double imgHeight = image.getHeight();
                double canvasWidth = canvas.getWidth();
                double canvasHeight = canvas.getHeight();
                
                double scale = Math.min(canvasWidth / imgWidth, canvasHeight / imgHeight);
                double scaledWidth = imgWidth * scale;
                double scaledHeight = imgHeight * scale;
                double x = (canvasWidth - scaledWidth) / 2;
                double y = (canvasHeight - scaledHeight) / 2;
                
                gc.drawImage(image, x, y, scaledWidth, scaledHeight);
                
                hasSignature = true;
                savedImagePath = rutaImagen;
                allStrokes.clear();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar firma desde ruta: " + e.getMessage());
        }
    }
}
