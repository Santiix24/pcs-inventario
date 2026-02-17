package inventario.fx.ui.firma;
import inventario.fx.model.TemaManager;
import inventario.fx.icons.IconosSVG;

import javafx.embed.swing.SwingFXUtils;
import inventario.fx.util.AnimacionesFX;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de captura de firma digital de alta calidad.
 * Soporta dibujo con mouse/táctil y carga de imágenes.
 * Diseño moderno con iconografía de Lucide.
 */
public class SignaturePadFX extends VBox {
    
    // Configuración del canvas
    private static final int CANVAS_WIDTH = 380;
    private static final int CANVAS_HEIGHT = 180;
    private static final double LINE_WIDTH = 2.5;
    private static final Color INK_COLOR = Color.web("#2c3e50");
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    
    // Componentes
    private Canvas canvas;
    private GraphicsContext gc;
    private List<Point> currentStroke;
    private List<List<Point>> allStrokes;
    private boolean isEmpty = true;
    private Image loadedImage = null;
    
    // Título del panel
    private String title;
    
    /**
     * Punto en el canvas
     */
    private static class Point {
        double x, y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    
    /**
     * Constructor
     * @param title Título del panel (ej: "Firma Técnico")
     */
    public SignaturePadFX(String title) {
        this.title = title;
        this.allStrokes = new ArrayList<>();
        this.currentStroke = new ArrayList<>();
        
        setupUI();
        setupCanvasEvents();
    }
    
    /**
     * Configura la interfaz de usuario
     */
    private void setupUI() {
        setSpacing(12);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(15));
        setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );
        
        // Título
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblTitle.setTextFill(Color.web(TemaManager.COLOR_PRIMARY));
        
        // Canvas para firmar
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        clearCanvas();
        
        // Borde del canvas
        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;"
        );
        
        // Botones de acción
        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER);
        
        Button btnClear = createActionButton("Limpiar", IconosSVG.eliminar(TemaManager.getTextMuted(), 16));
        Button btnLoad = createActionButton("Cargar", IconosSVG.carpeta(TemaManager.getTextMuted(), 16));
        
        btnClear.setOnAction(e -> clear());
        btnLoad.setOnAction(e -> loadImageFromFile());
        
        buttonsBox.getChildren().addAll(btnClear, btnLoad);
        
        // Instrucciones
        Label lblInstrucciones = new Label("Firme con el mouse o cargue una imagen");
        lblInstrucciones.setFont(Font.font("Segoe UI", 9));
        lblInstrucciones.setTextFill(Color.web(TemaManager.getTextMuted()));
        
        getChildren().addAll(lblTitle, canvasContainer, buttonsBox, lblInstrucciones);
    }
    
    /**
     * Crea un botón de acción estilizado
     */
    private Button createActionButton(String text, javafx.scene.Node icon) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        btn.setGraphic(icon);
        btn.setTextFill(Color.web(TemaManager.getText()));
        btn.setCursor(Cursor.HAND);
        btn.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 6 12;" +
            "-fx-graphic-text-gap: 6;"
        );
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + TemaManager.getBorder() + ";" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: " + TemaManager.COLOR_PRIMARY + ";" +
                "-fx-border-radius: 6;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 6 12;" +
                "-fx-graphic-text-gap: 6;"
            );
            AnimacionesFX.hoverIn(btn, 1.05, 120);
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + TemaManager.getBg() + ";" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: " + TemaManager.getBorder() + ";" +
                "-fx-border-radius: 6;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 6 12;" +
                "-fx-graphic-text-gap: 6;"
            );
            AnimacionesFX.hoverOut(btn, 120);
        });
        
        return btn;
    }
    
    /**
     * Configura los eventos del canvas
     */
    private void setupCanvasEvents() {
        canvas.setOnMousePressed(e -> {
            currentStroke = new ArrayList<>();
            Point p = new Point(e.getX(), e.getY());
            currentStroke.add(p);
            isEmpty = false;
            loadedImage = null; // Limpiar imagen cargada al dibujar
        });
        
        canvas.setOnMouseDragged(e -> {
            Point p = new Point(e.getX(), e.getY());
            currentStroke.add(p);
            
            // Dibujar en tiempo real
            if (currentStroke.size() > 1) {
                Point prev = currentStroke.get(currentStroke.size() - 2);
                gc.setStroke(INK_COLOR);
                gc.setLineWidth(LINE_WIDTH);
                gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                gc.strokeLine(prev.x, prev.y, p.x, p.y);
            }
        });
        
        canvas.setOnMouseReleased(e -> {
            if (!currentStroke.isEmpty()) {
                allStrokes.add(new ArrayList<>(currentStroke));
                currentStroke.clear();
            }
        });
    }
    
    /**
     * Limpia el canvas
     */
    private void clearCanvas() {
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }
    
    /**
     * Limpia la firma
     */
    public void clear() {
        allStrokes.clear();
        currentStroke.clear();
        clearCanvas();
        isEmpty = true;
        loadedImage = null;
    }
    
    /**
     * Redibuja todos los trazos
     */
    private void redrawAllStrokes() {
        clearCanvas();
        
        gc.setStroke(INK_COLOR);
        gc.setLineWidth(LINE_WIDTH);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        
        for (List<Point> stroke : allStrokes) {
            if (stroke.size() > 1) {
                for (int i = 1; i < stroke.size(); i++) {
                    Point prev = stroke.get(i - 1);
                    Point curr = stroke.get(i);
                    gc.strokeLine(prev.x, prev.y, curr.x, curr.y);
                }
            }
        }
    }
    
    /**
     * Carga una imagen desde un archivo
     */
    private void loadImageFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen de firma");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                Image img = new Image(file.toURI().toString());
                loadImage(img);
            } catch (Exception e) {
                System.err.println("Error al cargar imagen: " + e.getMessage());
            }
        }
    }
    
    /**
     * Carga una imagen en el canvas
     */
    public void loadImage(Image img) {
        loadedImage = img;
        allStrokes.clear();
        clearCanvas();
        
        // Calcular dimensiones preservando aspect ratio
        double imgWidth = img.getWidth();
        double imgHeight = img.getHeight();
        double scale = Math.min(CANVAS_WIDTH / imgWidth, CANVAS_HEIGHT / imgHeight);
        double scaledWidth = imgWidth * scale;
        double scaledHeight = imgHeight * scale;
        double x = (CANVAS_WIDTH - scaledWidth) / 2;
        double y = (CANVAS_HEIGHT - scaledHeight) / 2;
        
        gc.drawImage(img, x, y, scaledWidth, scaledHeight);
        isEmpty = false;
    }
    
    /**
     * Verifica si el pad está vacío
     */
    public boolean isEmpty() {
        return isEmpty;
    }
    
    /**
     * Obtiene la firma como imagen de alta calidad
     */
    public Image getSignatureImage() {
        if (isEmpty) {
            return null;
        }
        
        // Crear imagen con alta resolución (2x para mejor calidad)
        final int SCALE = 2;
        final int HIGH_RES_WIDTH = CANVAS_WIDTH * SCALE;
        final int HIGH_RES_HEIGHT = CANVAS_HEIGHT * SCALE;
        
        WritableImage snapshot = new WritableImage(HIGH_RES_WIDTH, HIGH_RES_HEIGHT);
        Canvas tempCanvas = new Canvas(HIGH_RES_WIDTH, HIGH_RES_HEIGHT);
        GraphicsContext tempGc = tempCanvas.getGraphicsContext2D();
        
        // Fondo blanco
        tempGc.setFill(BACKGROUND_COLOR);
        tempGc.fillRect(0, 0, HIGH_RES_WIDTH, HIGH_RES_HEIGHT);
        
        if (loadedImage != null) {
            // Si hay imagen cargada, escalarla
            double imgWidth = loadedImage.getWidth();
            double imgHeight = loadedImage.getHeight();
            double scale = Math.min(HIGH_RES_WIDTH / imgWidth, HIGH_RES_HEIGHT / imgHeight);
            double scaledWidth = imgWidth * scale;
            double scaledHeight = imgHeight * scale;
            double x = (HIGH_RES_WIDTH - scaledWidth) / 2;
            double y = (HIGH_RES_HEIGHT - scaledHeight) / 2;
            
            tempGc.drawImage(loadedImage, x, y, scaledWidth, scaledHeight);
        } else {
            // Si hay trazos, dibujarlos con alta calidad
            tempGc.setStroke(INK_COLOR);
            tempGc.setLineWidth(LINE_WIDTH * SCALE);
            tempGc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            tempGc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
            
            for (List<Point> stroke : allStrokes) {
                if (stroke.size() > 1) {
                    for (int i = 1; i < stroke.size(); i++) {
                        Point prev = stroke.get(i - 1);
                        Point curr = stroke.get(i);
                        tempGc.strokeLine(
                            prev.x * SCALE, prev.y * SCALE,
                            curr.x * SCALE, curr.y * SCALE
                        );
                    }
                }
            }
        }
        
        tempCanvas.snapshot(null, snapshot);
        return snapshot;
    }
    
    /**
     * Guarda la firma como archivo PNG de alta calidad
     */
    public File saveSignatureToFile(String fileName) throws IOException {
        if (isEmpty) {
            return null;
        }
        
        Image signatureImage = getSignatureImage();
        if (signatureImage == null) {
            return null;
        }
        
        // Crear directorio temporal si no existe
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "inventario_firmas");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        
        // Crear archivo
        File file = new File(tempDir, fileName + ".png");
        
        // Convertir a BufferedImage y guardar como PNG
        BufferedImage bImage = SwingFXUtils.fromFXImage(signatureImage, null);
        ImageIO.write(bImage, "png", file);
        
        return file;
    }
    
    /**
     * Carga una firma desde un archivo
     */
    public void loadSignatureFromFile(File file) {
        if (file != null && file.exists()) {
            try {
                Image img = new Image(file.toURI().toString());
                loadImage(img);
            } catch (Exception e) {
                System.err.println("Error al cargar firma: " + e.getMessage());
            }
        }
    }
}
