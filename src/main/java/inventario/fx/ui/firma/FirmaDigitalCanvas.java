package inventario.fx.ui.firma;
import inventario.fx.model.TemaManager;
import inventario.fx.icons.IconosSVG;
import inventario.fx.util.AppLogger;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente de firma digital con canvas para dibujar firmas con mouse/táctil
 * y opción para cargar imágenes. Diseño moderno y profesional.
 */
public class FirmaDigitalCanvas extends VBox {
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final List<Point> currentPath;
    private final List<List<Point>> allPaths;
    private boolean drawing = false;
    private String imagenCargadaPath = null;
    private Image imagenCargada = null;
    
    private static final double CANVAS_WIDTH = 400;
    private static final double CANVAS_HEIGHT = 180;
    private static final double LINE_WIDTH = 2.5;
    private static final double SMOOTHING_FACTOR = 0.5; // Factor de suavizado para curvas Bézier
    
    // Variables para suavizado de trazos
    private Point lastPoint = null;
    private Point lastMidPoint = null;
    
    // Callbacks
    private Runnable onCambioCallback;
    
    /**
     * Punto en el canvas para trazos suaves
     */
    private static class Point {
        double x, y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    
    public FirmaDigitalCanvas() {
        this.currentPath = new ArrayList<>();
        this.allPaths = new ArrayList<>();
        
        setSpacing(10);
        setAlignment(Pos.CENTER);
        
        // Canvas para dibujar
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        // Estilo del canvas
        canvas.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 6;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);"
        );
        canvas.setCursor(Cursor.CROSSHAIR);
        
        // Configurar trazo negro suave
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(LINE_WIDTH);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        
        // Fondo blanco inicial
        limpiarCanvas();
        
        // Eventos de dibujo
        canvas.setOnMousePressed(e -> {
            if (imagenCargada == null) {
                drawing = true;
                currentPath.clear();
                lastPoint = new Point(e.getX(), e.getY());
                lastMidPoint = null;
                currentPath.add(lastPoint);
            }
        });
        
        canvas.setOnMouseDragged(e -> {
            if (drawing && imagenCargada == null) {
                Point newPoint = new Point(e.getX(), e.getY());
                
                // Calcular punto medio entre el último punto y el nuevo
                Point midPoint = new Point(
                    (lastPoint.x + newPoint.x) * SMOOTHING_FACTOR,
                    (lastPoint.y + newPoint.y) * SMOOTHING_FACTOR
                );
                
                // Dibujar curva suave de Bézier cuadrática
                if (lastMidPoint != null) {
                    gc.beginPath();
                    gc.moveTo(lastMidPoint.x, lastMidPoint.y);
                    gc.quadraticCurveTo(lastPoint.x, lastPoint.y, midPoint.x, midPoint.y);
                    gc.stroke();
                } else {
                    // Primera línea del trazo
                    gc.beginPath();
                    gc.moveTo(lastPoint.x, lastPoint.y);
                    gc.lineTo(midPoint.x, midPoint.y);
                    gc.stroke();
                }
                
                currentPath.add(newPoint);
                lastPoint = newPoint;
                lastMidPoint = midPoint;
            }
        });
        
        canvas.setOnMouseReleased(e -> {
            if (drawing) {
                // Terminar el trazo dibujando hasta el último punto
                if (lastMidPoint != null && lastPoint != null) {
                    gc.beginPath();
                    gc.moveTo(lastMidPoint.x, lastMidPoint.y);
                    gc.lineTo(lastPoint.x, lastPoint.y);
                    gc.stroke();
                }
                
                drawing = false;
                if (!currentPath.isEmpty()) {
                    allPaths.add(new ArrayList<>(currentPath));
                    if (onCambioCallback != null) {
                        onCambioCallback.run();
                    }
                }
                lastPoint = null;
                lastMidPoint = null;
            }
        });
        
        // Soporte táctil
        canvas.setOnTouchPressed(e -> {
            if (imagenCargada == null) {
                drawing = true;
                currentPath.clear();
                lastPoint = new Point(e.getTouchPoint().getX(), e.getTouchPoint().getY());
                lastMidPoint = null;
                currentPath.add(lastPoint);
            }
        });
        
        canvas.setOnTouchMoved(e -> {
            if (drawing && imagenCargada == null) {
                Point newPoint = new Point(e.getTouchPoint().getX(), e.getTouchPoint().getY());
                
                // Calcular punto medio para suavizado
                Point midPoint = new Point(
                    (lastPoint.x + newPoint.x) * SMOOTHING_FACTOR,
                    (lastPoint.y + newPoint.y) * SMOOTHING_FACTOR
                );
                
                // Dibujar curva suave de Bézier cuadrática
                if (lastMidPoint != null) {
                    gc.beginPath();
                    gc.moveTo(lastMidPoint.x, lastMidPoint.y);
                    gc.quadraticCurveTo(lastPoint.x, lastPoint.y, midPoint.x, midPoint.y);
                    gc.stroke();
                } else {
                    gc.beginPath();
                    gc.moveTo(lastPoint.x, lastPoint.y);
                    gc.lineTo(midPoint.x, midPoint.y);
                    gc.stroke();
                }
                
                currentPath.add(newPoint);
                lastPoint = newPoint;
                lastMidPoint = midPoint;
                e.consume();
            }
        });
        
        canvas.setOnTouchReleased(e -> {
            if (drawing) {
                // Terminar el trazo táctil dibujando hasta el último punto
                if (lastMidPoint != null && lastPoint != null) {
                    gc.beginPath();
                    gc.moveTo(lastMidPoint.x, lastMidPoint.y);
                    gc.lineTo(lastPoint.x, lastPoint.y);
                    gc.stroke();
                }
                
                drawing = false;
                if (!currentPath.isEmpty()) {
                    allPaths.add(new ArrayList<>(currentPath));
                    if (onCambioCallback != null) {
                        onCambioCallback.run();
                    }
                }
                lastPoint = null;
                lastMidPoint = null;
            }
        });
        
        // Botones de control
        HBox controles = crearControles();
        
        getChildren().addAll(canvas, controles);
    }
    
    /**
     * Crea la barra de controles (Limpiar, Cargar Imagen)
     */
    private HBox crearControles() {
        HBox controles = new HBox(10);
        controles.setAlignment(Pos.CENTER);
        controles.setPadding(new Insets(5, 0, 0, 0));
        
        // Botón Limpiar
        Button btnLimpiar = new Button();
        btnLimpiar.setGraphic(IconosSVG.papelera(TemaManager.getTextSecondary(), 16));
        btnLimpiar.setText("Limpiar");
        btnLimpiar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TemaManager.getTextSecondary() + ";" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 6 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        btnLimpiar.setOnMouseEntered(e -> btnLimpiar.setStyle(
            "-fx-background-color: " + TemaManager.getBgLight() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 6 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        btnLimpiar.setOnMouseExited(e -> btnLimpiar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TemaManager.getTextSecondary() + ";" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 6 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        btnLimpiar.setOnAction(e -> limpiar());
        
        // Botón Cargar Imagen
        Button btnCargar = new Button();
        btnCargar.setGraphic(IconosSVG.imagen(TemaManager.getTextSecondary(), 16));
        btnCargar.setText("Cargar Imagen");
        btnCargar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TemaManager.getTextSecondary() + ";" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 6 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        btnCargar.setOnMouseEntered(e -> btnCargar.setStyle(
            "-fx-background-color: " + TemaManager.getBgLight() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 6 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        btnCargar.setOnMouseExited(e -> btnCargar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TemaManager.getTextSecondary() + ";" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 6 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        btnCargar.setOnAction(e -> cargarImagen());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        controles.getChildren().addAll(btnLimpiar, btnCargar, spacer);
        return controles;
    }
    
    /**
     * Limpia el canvas y resetea todo
     */
    public void limpiar() {
        allPaths.clear();
        currentPath.clear();
        imagenCargada = null;
        imagenCargadaPath = null;
        limpiarCanvas();
        if (onCambioCallback != null) {
            onCambioCallback.run();
        }
    }
    
    /**
     * Limpia el canvas visualmente
     */
    private void limpiarCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }
    
    /**
     * Abre diálogo para cargar imagen de firma
     */
    private void cargarImagen() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar Imagen de Firma");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        
        Stage stage = (Stage) getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        
        if (file != null && file.exists()) {
            try {
                Image img = new Image(file.toURI().toString());
                
                // Limpiar canvas y dibujar imagen
                limpiarCanvas();
                allPaths.clear();
                
                // Escalar imagen para que quepa en el canvas manteniendo proporción
                double imgWidth = img.getWidth();
                double imgHeight = img.getHeight();
                double scale = Math.min(CANVAS_WIDTH / imgWidth, CANVAS_HEIGHT / imgHeight);
                
                double newWidth = imgWidth * scale * 0.9; // 90% del tamaño para padding
                double newHeight = imgHeight * scale * 0.9;
                
                double x = (CANVAS_WIDTH - newWidth) / 2;
                double y = (CANVAS_HEIGHT - newHeight) / 2;
                
                gc.drawImage(img, x, y, newWidth, newHeight);
                
                imagenCargada = img;
                imagenCargadaPath = file.getAbsolutePath();
                
                if (onCambioCallback != null) {
                    onCambioCallback.run();
                }
                
            } catch (Exception ex) {
                AppLogger.getLogger(FirmaDigitalCanvas.class).error("Error al cargar imagen: " + ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Verifica si hay una firma (dibujada o cargada)
     */
    public boolean tieneFirma() {
        return !allPaths.isEmpty() || imagenCargada != null;
    }
    
    /**
     * Guarda la firma como imagen PNG de ALTA RESOLUCIÓN y retorna la ruta
     */
    public String guardarFirma(String nombreArchivo) {
        System.out.println("[FirmaDigital] guardarFirma llamado para: " + nombreArchivo);
        System.out.println("[FirmaDigital] tieneFirma(): " + tieneFirma());
        System.out.println("[FirmaDigital] allPaths.size(): " + allPaths.size());
        System.out.println("[FirmaDigital] imagenCargada != null: " + (imagenCargada != null));
        
        if (!tieneFirma()) {
            System.out.println("[FirmaDigital] No hay firma para guardar");
            return null;
        }
        
        try {
            // Crear directorio de firmas si no existe
            Path dirFirmas = inventario.fx.config.PortablePaths.getFirmasDir();
            Files.createDirectories(dirFirmas);
            System.out.println("[FirmaDigital] Directorio de firmas: " + dirFirmas.toAbsolutePath());
            
            // RESOLUCIÓN ULTRA ALTA: 4x para evitar pixelación en Excel
            int highResWidth = (int)(CANVAS_WIDTH * 4);
            int highResHeight = (int)(CANVAS_HEIGHT * 4);
            double escala = 4.0;
            
            // Crear imagen de alta resolución con transparencia
            BufferedImage bImage = new BufferedImage(
                highResWidth, 
                highResHeight, 
                BufferedImage.TYPE_INT_ARGB
            );
            
            java.awt.Graphics2D g2d = bImage.createGraphics();
            
            // Configurar para máxima calidad
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, 
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, 
                java.awt.RenderingHints.VALUE_STROKE_PURE);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            
            // Fondo transparente
            g2d.setComposite(java.awt.AlphaComposite.Clear);
            g2d.fillRect(0, 0, highResWidth, highResHeight);
            g2d.setComposite(java.awt.AlphaComposite.Src);
            
            // Configurar trazo negro de alta calidad
            g2d.setColor(java.awt.Color.BLACK);
            float strokeWidth = (float)(LINE_WIDTH * escala);
            g2d.setStroke(new java.awt.BasicStroke(
                strokeWidth, 
                java.awt.BasicStroke.CAP_ROUND, 
                java.awt.BasicStroke.JOIN_ROUND
            ));
            
            // Si hay imagen cargada, dibujarla escalada
            if (imagenCargada != null) {
                BufferedImage temp = SwingFXUtils.fromFXImage(imagenCargada, null);
                g2d.drawImage(temp, 0, 0, highResWidth, highResHeight, null);
            }
            
            // Dibujar todos los trazos escalados con suavizado de Bézier
            for (List<Point> path : allPaths) {
                if (path.size() < 2) continue;
                
                java.awt.geom.Path2D.Double awtPath = new java.awt.geom.Path2D.Double();
                Point firstPoint = path.get(0);
                awtPath.moveTo(firstPoint.x * escala, firstPoint.y * escala);
                
                if (path.size() == 2) {
                    // Solo dos puntos, dibujar línea recta
                    Point p = path.get(1);
                    awtPath.lineTo(p.x * escala, p.y * escala);
                } else {
                    // Múltiples puntos, aplicar suavizado con curvas de Bézier cuadráticas
                    Point prevPoint = firstPoint;
                    for (int i = 1; i < path.size() - 1; i++) {
                        Point currentPoint = path.get(i);
                        Point nextPoint = path.get(i + 1);
                        
                        // Punto medio hacia el siguiente punto
                        double midX = (currentPoint.x + nextPoint.x) * SMOOTHING_FACTOR;
                        double midY = (currentPoint.y + nextPoint.y) * SMOOTHING_FACTOR;
                        
                        // Curva cuadrática usando el punto actual como control
                        awtPath.quadTo(
                            currentPoint.x * escala, 
                            currentPoint.y * escala,
                            midX * escala, 
                            midY * escala
                        );
                        
                        prevPoint = currentPoint;
                    }
                    
                    // Dibujar hasta el último punto
                    Point lastPathPoint = path.get(path.size() - 1);
                    awtPath.lineTo(lastPathPoint.x * escala, lastPathPoint.y * escala);
                }
                
                g2d.draw(awtPath);
            }
            
            g2d.dispose();
            
            // Guardar como PNG con máxima calidad
            File outputFile = dirFirmas.resolve(nombreArchivo + ".png").toFile();
            
            // Configurar escritura PNG de alta calidad
            javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
            javax.imageio.ImageWriteParam writeParam = writer.getDefaultWriteParam();
            
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile);
                 javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {
                writer.setOutput(ios);
                writer.write(null, new javax.imageio.IIOImage(bImage, null, null), writeParam);
                writer.dispose();
            }
            
            System.out.println("[FirmaDigital] ✓ Firma guardada exitosamente en: " + outputFile.getAbsolutePath());
            System.out.println("[FirmaDigital] ✓ Archivo existe: " + outputFile.exists() + ", tamaño: " + outputFile.length() + " bytes");
            System.out.println("[FirmaDigital] ✓ PNG ULTRA ALTA RESOLUCIÓN (" + highResWidth + "x" + highResHeight + ") sin pixelación");
            return outputFile.getAbsolutePath();
            
        } catch (IOException e) {
            AppLogger.getLogger(FirmaDigitalCanvas.class).error("Error al guardar firma: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Carga una firma desde una ruta de archivo
     */
    public void cargarFirmaDesdeArchivo(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return;
        }
        
        try {
            File file = new File(rutaArchivo);
            if (file.exists()) {
                Image img = new Image(file.toURI().toString());
                
                limpiarCanvas();
                allPaths.clear();
                
                // Escalar y centrar imagen
                double imgWidth = img.getWidth();
                double imgHeight = img.getHeight();
                double scale = Math.min(CANVAS_WIDTH / imgWidth, CANVAS_HEIGHT / imgHeight);
                
                double newWidth = imgWidth * scale * 0.9;
                double newHeight = imgHeight * scale * 0.9;
                
                double x = (CANVAS_WIDTH - newWidth) / 2;
                double y = (CANVAS_HEIGHT - newHeight) / 2;
                
                gc.drawImage(img, x, y, newWidth, newHeight);
                
                imagenCargada = img;
                imagenCargadaPath = rutaArchivo;
            }
        } catch (Exception e) {
            AppLogger.getLogger(FirmaDigitalCanvas.class).error("Error al cargar firma desde archivo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Establece callback para notificar cambios
     */
    public void setOnCambio(Runnable callback) {
        this.onCambioCallback = callback;
    }
}
