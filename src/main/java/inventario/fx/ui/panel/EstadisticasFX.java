package inventario.fx.ui.panel;
import inventario.fx.model.InventarioFXBase;
import inventario.fx.model.TemaManager;
import inventario.fx.icons.IconosSVG;
import inventario.fx.util.AppLogger;

import inventario.fx.util.ComponentesFX;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import inventario.fx.util.AnimacionesFX;

/**
 * Módulo de Estadísticas con gráficos animados.
 * Procesa datos de equipos y aplicaciones para visualización analítica.
 */
public class EstadisticasFX extends InventarioFXBase {

    // Colores del tema - Usando TemaManager para soporte light/dark
    private static String COLOR_BG_DARK() { return TemaManager.getBgDark(); }
    private static String COLOR_BG() { return TemaManager.getBg(); }
    private static String COLOR_SURFACE() { return TemaManager.getSurface(); }
    private static String COLOR_BORDER() { return TemaManager.getBorder(); }
    private static String COLOR_TEXT() { return TemaManager.getText(); }
    private static String COLOR_TEXT_SECONDARY() { return TemaManager.getTextSecondary(); }
    private static String COLOR_TEXT_MUTED() { return TemaManager.getTextMuted(); }
    
    // Colores para gráficos (paleta moderna) - Usando TemaManager
    private static final String[] CHART_COLORS = TemaManager.CHART_COLORS;
    
    // Colores específicos para el gráfico de línea
    private static final String LINE_COLOR = TemaManager.LINE_COLOR;
    private static final String LINE_GLOW = TemaManager.LINE_GLOW;
    
    // Lista para rastrear y detener AnimationTimers activos
    private static final java.util.List<AnimationTimer> activeTimers = new java.util.ArrayList<>();
    
    // Lista para rastrear y detener todas las animaciones (FadeTransition, etc.)
    private static final java.util.List<Animation> activeAnimations = new java.util.ArrayList<>();
    
    // Referencias para actualización en tiempo real
    private static Path rutaExcelActual = null;
    private static VBox contentActual = null;
    private static HBox tarjetasResumenActual = null;
    private static HBox graficosRow1Actual = null;
    private static HBox graficosRow2Actual = null;
    
    /**
     * Fuerza una actualización inmediata de todas las estadísticas.
     * Úsalo cuando se modifica el archivo Excel externamente.
     */
    public static void actualizarAhora() {
        if (rutaExcelActual == null || contentActual == null) {
            System.out.println("⚠ No hay estadísticas activas para actualizar");
            return;
        }
        
        javafx.application.Platform.runLater(() -> {
            try {
                System.out.println("⟳ Actualización forzada iniciada...");
                actualizarEstadisticas(rutaExcelActual, contentActual, tarjetasResumenActual, graficosRow1Actual, graficosRow2Actual);
            } catch (Exception e) {
                System.err.println("Error en actualización forzada: " + e.getMessage());
            }
        });
    }
    
    /**
     * Método interno para actualizar todas las estadísticas
     */
    private static void actualizarEstadisticas(Path rutaExcel, VBox content, HBox tarjetasResumen, HBox graficosRow1, HBox graficosRow2) {
        // Cargar datos actualizados
        DatosEstadisticas datosNuevos = cargarDatos(rutaExcel);
        System.out.println("✓ Actualización completada: equipos=" + datosNuevos.totalEquipos + ", apps=" + datosNuevos.totalApps);
        
        // Actualizar tarjetas de resumen
        HBox nuevasTarjetas = crearTarjetasResumen(datosNuevos);
        int indexTarjetas = content.getChildren().indexOf(tarjetasResumen);
        if (indexTarjetas >= 0) {
            content.getChildren().set(indexTarjetas, nuevasTarjetas);
            tarjetasResumenActual = nuevasTarjetas;
        }
        
        // Recrear primera fila de gráficos
        HBox nuevaRow1 = new HBox(12);
        nuevaRow1.setAlignment(Pos.CENTER);
        nuevaRow1.setFillHeight(true);
        
        VBox nuevoGraficoLinea = crearGraficoLinea(datosNuevos);
        HBox.setHgrow(nuevoGraficoLinea, Priority.ALWAYS);
        VBox.setVgrow(nuevoGraficoLinea, Priority.ALWAYS);
        nuevoGraficoLinea.setMaxHeight(Double.MAX_VALUE);
        
        VBox nuevoGraficoPie = crearGraficoCircular(datosNuevos);
        nuevaRow1.getChildren().addAll(nuevoGraficoLinea, nuevoGraficoPie);
        
        int indexRow1 = content.getChildren().indexOf(graficosRow1);
        if (indexRow1 >= 0) {
            content.getChildren().set(indexRow1, nuevaRow1);
            graficosRow1Actual = nuevaRow1;
        }
        
        // Recrear segunda fila de gráficos
        HBox nuevaRow2 = new HBox(12);
        nuevaRow2.setAlignment(Pos.CENTER);
        nuevaRow2.setFillHeight(true);
        
        VBox nuevoGraficoBarras = crearGraficoBarras(datosNuevos);
        HBox.setHgrow(nuevoGraficoBarras, Priority.ALWAYS);
        
        VBox nuevoGraficoEstado = crearGraficoEstadoEquipos(datosNuevos);
        nuevaRow2.getChildren().addAll(nuevoGraficoBarras, nuevoGraficoEstado);
        
        int indexRow2 = content.getChildren().indexOf(graficosRow2);
        if (indexRow2 >= 0) {
            content.getChildren().set(indexRow2, nuevaRow2);
            VBox.setVgrow(nuevaRow2, Priority.ALWAYS);
            graficosRow2Actual = nuevaRow2;
        }
    }
    
    /**
     * Detiene todos los AnimationTimers y animaciones activas.
     * Debe llamarse antes de crear nuevas estadísticas o al cerrar el dashboard.
     */
    public static void detenerAnimaciones() {
        // Detener todos los AnimationTimers
        for (AnimationTimer timer : activeTimers) {
            try {
                timer.stop();
            } catch (Exception ignored) {}
        }
        activeTimers.clear();
        
        // Detener todas las animaciones (FadeTransition, etc.)
        for (Animation anim : activeAnimations) {
            try {
                anim.stop();
            } catch (Exception ignored) {}
        }
        activeAnimations.clear();
    }
    
    /**
     * Registra una animación para que pueda ser detenida posteriormente.
     */
    private static void registrarAnimacion(Animation anim) {
        activeAnimations.add(anim);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PANEL PRINCIPAL DE ESTADÍSTICAS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Crea el panel principal de estadísticas con gráficos animados.
     */
    public static StackPane crearPanelEstadisticas(Path rutaExcel) {
        // Detener animaciones anteriores antes de crear nuevas
        detenerAnimaciones();
        
        // DEBUG: Imprimir ruta del Excel que se está cargando
        System.out.println("=== ESTADISTICAS: Cargando desde: " + rutaExcel + " ===");
        
        StackPane container = new StackPane();
        container.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");
        // Asegurar que el contenedor no afecte el layout del sidebar
        container.setMinSize(0, 0);
        container.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        container.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        // Layout principal sin scroll - todo debe caber en pantalla
        VBox content = new VBox(12);
        content.setPadding(new Insets(12, 20, 16, 20));
        content.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");
        content.setFillWidth(true);
        
        // Header
        HBox header = crearHeaderEstadisticas();
        
        // Cargar datos
        DatosEstadisticas datos = cargarDatos(rutaExcel);
        
        // DEBUG: Imprimir datos cargados
        System.out.println("=== Datos cargados: equipos=" + datos.totalEquipos + ", apps=" + datos.totalApps + " ===");
        
        // Guardar referencias para actualización en tiempo real
        rutaExcelActual = rutaExcel;
        contentActual = content;
        
        // Tarjetas de resumen
        HBox tarjetasResumen = crearTarjetasResumen(datos);
        tarjetasResumenActual = tarjetasResumen;
        
        // Gráficos principales - Primera fila
        HBox graficosRow1 = new HBox(12);
        graficosRow1.setAlignment(Pos.CENTER);
        graficosRow1.setFillHeight(true);
        
        // Gráfico de línea - Evolución de equipos
        VBox graficoLinea = crearGraficoLinea(datos);
        HBox.setHgrow(graficoLinea, Priority.ALWAYS);
        VBox.setVgrow(graficoLinea, Priority.ALWAYS);
        graficoLinea.setMaxHeight(Double.MAX_VALUE);
        
        // Gráfico circular - Distribución de SO
        VBox graficoPie = crearGraficoCircular(datos);
        
        graficosRow1.getChildren().addAll(graficoLinea, graficoPie);
        graficosRow1Actual = graficosRow1;
        
        // Segunda fila de gráficos
        HBox graficosRow2 = new HBox(12);
        graficosRow2.setAlignment(Pos.CENTER);
        graficosRow2.setFillHeight(true);
        
        // Gráfico de barras - Top aplicaciones
        VBox graficoBarras = crearGraficoBarras(datos);
        HBox.setHgrow(graficoBarras, Priority.ALWAYS);
        
        // Gráfico circular - Estado de equipos (Marca)
        VBox graficoEstado = crearGraficoEstadoEquipos(datos);
        
        graficosRow2.getChildren().addAll(graficoBarras, graficoEstado);
        graficosRow2Actual = graficosRow2;
        
        content.getChildren().addAll(header, tarjetasResumen, graficosRow1, graficosRow2);
        
        // Hacer que el contenido crezca para llenar el espacio disponible
        VBox.setVgrow(graficosRow1, Priority.ALWAYS);
        VBox.setVgrow(graficosRow2, Priority.ALWAYS);
        
        // Añadir directamente al contenedor sin scroll
        container.getChildren().add(content);
        
        // Animación de entrada escalonada por secciones
        header.setOpacity(0);
        tarjetasResumen.setOpacity(0);
        graficosRow1.setOpacity(0);
        graficosRow2.setOpacity(0);
        
        AnimacionesFX.slideUpFadeIn(header, 400, 0);
        AnimacionesFX.slideUpFadeIn(tarjetasResumen, 450, 100);
        AnimacionesFX.slideUpFadeIn(graficosRow1, 450, 200);
        AnimacionesFX.slideUpFadeIn(graficosRow2, 450, 350);
        
        return container;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // HEADER
    // ════════════════════════════════════════════════════════════════════════════

    private static HBox crearHeaderEstadisticas() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 12, 8, 12));
        header.setSpacing(20);
        
        VBox titleBox = new VBox(4);
        
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(
            IconosSVG.estadisticas("#3B82F6", 28),
            crearLabel("Estadísticas", 24, FontWeight.BOLD, COLOR_TEXT())
        );
        
        Label subtitle = crearLabel("Análisis visual de equipos y aplicaciones", 13, FontWeight.NORMAL, COLOR_TEXT_SECONDARY());
        
        titleBox.getChildren().addAll(titleRow, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Fecha actual con diseño compacto y moderno
        HBox dateBox = new HBox(6);
        dateBox.setAlignment(Pos.CENTER);
        dateBox.setPadding(new Insets(5, 10, 5, 8));
        dateBox.setCursor(Cursor.HAND);
        
        String estiloBase = 
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;";
        
        String estiloHover = 
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: #3B82F6;" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;";
        
        dateBox.setStyle(estiloBase);
        
        // Icono de calendario estilo Lucide
        Node iconoCalendario = crearIconoCalendario(COLOR_TEXT_MUTED(), 14);
        
        // Formato de fecha: "Viernes, 9 enero"
        java.time.format.DateTimeFormatter sdf = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", new java.util.Locale("es", "ES"));
        String fechaTexto = java.time.LocalDate.now().format(sdf);
        fechaTexto = fechaTexto.substring(0, 1).toUpperCase() + fechaTexto.substring(1);
        
        Label dateLabel = crearLabel(fechaTexto, 11, FontWeight.NORMAL, COLOR_TEXT_MUTED());
        
        dateBox.getChildren().addAll(iconoCalendario, dateLabel);
        
        // Animaciones suaves para hover
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), dateBox);
        scaleIn.setToX(1.03);
        scaleIn.setToY(1.03);
        
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), dateBox);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        
        FadeTransition fadeIconIn = new FadeTransition(Duration.millis(150), iconoCalendario);
        fadeIconIn.setToValue(1.0);
        
        FadeTransition fadeIconOut = new FadeTransition(Duration.millis(150), iconoCalendario);
        fadeIconOut.setToValue(0.7);
        
        iconoCalendario.setOpacity(0.7);
        
        dateBox.setOnMouseEntered(e -> {
            dateBox.setStyle(estiloHover);
            dateLabel.setTextFill(Color.web(COLOR_TEXT()));
            scaleIn.playFromStart();
            fadeIconIn.playFromStart();
        });
        
        dateBox.setOnMouseExited(e -> {
            dateBox.setStyle(estiloBase);
            dateLabel.setTextFill(Color.web(COLOR_TEXT_MUTED()));
            scaleOut.playFromStart();
            fadeIconOut.playFromStart();
        });
        
        header.getChildren().addAll(titleBox, spacer, dateBox);
        return header;
    }
    
    /** Icono de calendario estilo Lucide */
    private static Node crearIconoCalendario(String color, double size) {
        javafx.scene.Group g = new javafx.scene.Group();
        double s = size / 24.0;
        
        // Rectángulo principal del calendario
        javafx.scene.shape.Path cuerpo = new javafx.scene.shape.Path();
        cuerpo.setStroke(Color.web(color));
        cuerpo.setStrokeWidth(2 * s);
        cuerpo.setStrokeLineCap(StrokeLineCap.ROUND);
        cuerpo.setStrokeLineJoin(StrokeLineJoin.ROUND);
        cuerpo.setFill(Color.TRANSPARENT);
        
        cuerpo.getElements().addAll(
            new MoveTo(5*s, 4*s),
            new LineTo(19*s, 4*s),
            new CubicCurveTo(20.1*s, 4*s, 21*s, 4.9*s, 21*s, 6*s),
            new LineTo(21*s, 20*s),
            new CubicCurveTo(21*s, 21.1*s, 20.1*s, 22*s, 19*s, 22*s),
            new LineTo(5*s, 22*s),
            new CubicCurveTo(3.9*s, 22*s, 3*s, 21.1*s, 3*s, 20*s),
            new LineTo(3*s, 6*s),
            new CubicCurveTo(3*s, 4.9*s, 3.9*s, 4*s, 5*s, 4*s),
            new ClosePath()
        );
        
        Line lineaHorizontal = new Line(3*s, 10*s, 21*s, 10*s);
        lineaHorizontal.setStroke(Color.web(color));
        lineaHorizontal.setStrokeWidth(2 * s);
        lineaHorizontal.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line anillaIzq = new Line(8*s, 2*s, 8*s, 6*s);
        anillaIzq.setStroke(Color.web(color));
        anillaIzq.setStrokeWidth(2 * s);
        anillaIzq.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line anillaDer = new Line(16*s, 2*s, 16*s, 6*s);
        anillaDer.setStroke(Color.web(color));
        anillaDer.setStrokeWidth(2 * s);
        anillaDer.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(cuerpo, lineaHorizontal, anillaIzq, anillaDer);
        return g;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // TARJETAS DE RESUMEN
    // ════════════════════════════════════════════════════════════════════════════

    private static HBox crearTarjetasResumen(DatosEstadisticas datos) {
        HBox cards = new HBox(12);
        cards.setAlignment(Pos.CENTER_LEFT);
        
        javafx.scene.Node card1 = crearTarjetaResumen("Registros PC", String.valueOf(datos.totalEquipos), 
                IconosSVG.computadora("#3B82F6", 20), "#3B82F6", "");
        javafx.scene.Node card2 = crearTarjetaResumen("Total Apps", String.valueOf(datos.totalApps), 
                IconosSVG.paquete("#10B981", 20), "#10B981", "");
        javafx.scene.Node card3 = crearTarjetaResumen("SO Principal", datos.soPrincipal, 
                IconosSVG.laptop("#F59E0B", 20), "#F59E0B", "");
        javafx.scene.Node card4 = crearTarjetaResumen("Último Scan", datos.ultimoScan, 
                IconosSVG.calendario("#8B5CF6", 20), "#8B5CF6", "");
        
        cards.getChildren().addAll(card1, card2, card3, card4);
        
        // Animación escalonada de tarjetas
        AnimacionesFX.entradaEscalonadaScale(
            java.util.Arrays.asList(card1, card2, card3, card4), 300, 80);
        
        return cards;
    }

    private static VBox crearTarjetaResumen(String titulo, String valor, Node icono, String color, String cambio) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12));
        card.setMinWidth(180);
        card.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;"
        );
        
        // Sombra sutil
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web(color, 0.1));
        sombra.setRadius(10);
        sombra.setOffsetY(2);
        card.setEffect(sombra);
        
        HBox headerCard = new HBox(8);
        headerCard.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconWrapper = new StackPane(icono);
        iconWrapper.setPadding(new Insets(6));
        iconWrapper.setStyle(
            "-fx-background-color: " + color + "15;" +
            "-fx-background-radius: 8;"
        );
        
        Label lblTitulo = crearLabel(titulo, 11, FontWeight.MEDIUM, COLOR_TEXT_SECONDARY());
        headerCard.getChildren().addAll(iconWrapper, lblTitulo);
        
        // Intentar parsear como número para animar
        Label lblValor;
        try {
            int valorNumerico = Integer.parseInt(valor);
            lblValor = crearLabel("0", 22, FontWeight.BOLD, COLOR_TEXT());
            lblValor.setWrapText(false);
            lblValor.setMinWidth(Region.USE_PREF_SIZE);
            // Animación de conteo
            animarConteo(lblValor, 0, valorNumerico, 800);
        } catch (NumberFormatException e) {
            // Si no es número, mostrar texto directamente
            lblValor = crearLabel(valor, 22, FontWeight.BOLD, COLOR_TEXT());
            lblValor.setWrapText(false);
            lblValor.setMinWidth(Region.USE_PREF_SIZE);
        }
        
        card.getChildren().addAll(headerCard, lblValor);
        
        // Animación hover sutil
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_SURFACE() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + color + "44;" +
                "-fx-border-radius: 12;"
            );
            
            javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(150), card
            );
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_SURFACE() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 12;"
            );
            
            javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(150), card
            );
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return card;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GRÁFICO DE LÍNEA - DARK UI PREMIUM (DATOS REALES)
    // ════════════════════════════════════════════════════════════════════════════

    private static final String CYAN_BRIGHT = TemaManager.LINE_COLOR;
    private static final String CYAN_GLOW = "#00B4D8";

    private static VBox crearGraficoLinea(DatosEstadisticas datos) {
        VBox container = new VBox(0);
        container.setPadding(new Insets(12));
        container.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;"
        );
        VBox.setVgrow(container, Priority.ALWAYS);
        
        // Header minimalista y compacto
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 8, 0));
        
        VBox titleSection = new VBox(2);
        Label titulo = crearLabel("Evolución de Inventario", 12, FontWeight.MEDIUM, COLOR_TEXT());
        
        HBox valorRow = new HBox(6);
        valorRow.setAlignment(Pos.BASELINE_LEFT);
        Label lblValor = crearLabel(String.valueOf(datos.totalEquipos), 24, FontWeight.BOLD, CYAN_BRIGHT);
        lblValor.setEffect(crearGlowCyan());
        Label lblUnidad = crearLabel("registros", 10, FontWeight.NORMAL, COLOR_TEXT_MUTED());
        lblUnidad.setPadding(new Insets(0, 0, 4, 0));
        valorRow.getChildren().addAll(lblValor, lblUnidad);
        
        titleSection.getChildren().addAll(titulo, valorRow);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Fecha último registro
        VBox fechaSection = new VBox(2);
        fechaSection.setAlignment(Pos.CENTER_RIGHT);
        Label lblFechaLabel = crearLabel("Último scan", 9, FontWeight.NORMAL, COLOR_TEXT_MUTED());
        Label lblFecha = crearLabel(datos.ultimoScan.length() > 10 ? datos.ultimoScan.substring(0, 10) : datos.ultimoScan, 11, FontWeight.MEDIUM, CYAN_BRIGHT);
        fechaSection.getChildren().addAll(lblFechaLabel, lblFecha);
        
        headerBox.getChildren().addAll(titleSection, spacer, fechaSection);
        
        // Área del gráfico con scroll horizontal profesional y responsivo
        Pane chartArea = crearGraficoLineaCustom(datos);
        chartArea.setMinHeight(200);
        chartArea.setPrefHeight(400);
        chartArea.setMaxHeight(Double.MAX_VALUE);
        // El ancho del gráfico depende de la cantidad de fechas
        int minPoints = 8;
        int totalPoints = Math.max(datos.evolucionPorDia.size(), minPoints);
        double minWidth = 600;
        double widthPerPoint = 120; // más espacio por punto
        final double chartWidth = Math.max(minWidth, totalPoints * widthPerPoint);
        chartArea.setMinWidth(chartWidth);
        chartArea.setPrefWidth(chartWidth);
        chartArea.setMaxWidth(chartWidth);
        VBox.setVgrow(chartArea, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(chartArea);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);
        scrollPane.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-background: transparent; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 0; " +
            "-fx-background-insets: 0; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setMinHeight(200);
        scrollPane.setPrefHeight(400);
        scrollPane.setMaxHeight(Double.MAX_VALUE);
        
        // Eliminar completamente el viewport border
        scrollPane.viewportBoundsProperty().addListener((obs, old, bounds) -> {
            scrollPane.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-background: transparent; " +
                "-fx-border-color: transparent; " +
                "-fx-padding: 0; " +
                "-fx-background-insets: 0; " +
                "-fx-viewport-border-width: 0;"
            );
        });
        
        // Estilo sutil para la barra de scroll (semi-transparente y delgada)
        scrollPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                // Eliminar viewport border
                scrollPane.lookupAll(".viewport").forEach(viewport -> {
                    viewport.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-padding: 0;");
                });
                
                scrollPane.lookupAll(".scroll-bar").forEach(node -> {
                    if (node instanceof javafx.scene.control.ScrollBar) {
                        javafx.scene.control.ScrollBar sb = (javafx.scene.control.ScrollBar) node;
                        if (sb.getOrientation() == javafx.geometry.Orientation.HORIZONTAL) {
                            sb.setStyle(
                                "-fx-background-color: rgba(20, 20, 20, 0.5); " +
                                "-fx-pref-height: 10; " +
                                "-fx-max-height: 10;"
                            );
                            sb.lookupAll(".thumb").forEach(thumb -> {
                                thumb.setStyle(
                                    "-fx-background-color: linear-gradient(to right, " + CYAN_BRIGHT + " 0%, #0EA5E9 100%); " +
                                    "-fx-background-radius: 5; " +
                                    "-fx-border-radius: 5; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0, 212, 255, 0.6), 4, 0, 0, 0);"
                                );
                            });
                            sb.lookupAll(".track").forEach(track -> {
                                track.setStyle(
                                    "-fx-background-color: rgba(255, 255, 255, 0.1); " +
                                    "-fx-background-radius: 5;"
                                );
                            });
                        } else {
                            // Ocultar completamente la barra vertical
                            sb.setVisible(false);
                            sb.setManaged(false);
                            sb.setMaxWidth(0);
                            sb.setPrefWidth(0);
                            sb.setStyle("-fx-background-color: transparent; -fx-opacity: 0; -fx-pref-width: 0; -fx-max-width: 0;");
                        }
                    }
                });
                // Ocultar cualquier esquina de scroll
                scrollPane.lookupAll(".corner").forEach(corner -> {
                    corner.setStyle("-fx-background-color: transparent; -fx-opacity: 0;");
                    ((javafx.scene.layout.Region) corner).setMaxSize(0, 0);
                    ((javafx.scene.layout.Region) corner).setPrefSize(0, 0);
                    corner.setVisible(false);
                    corner.setManaged(false);
                });
            }
        });

        // Scroll con la rueda del mouse SIEMPRE horizontal (sutil pero visible)
        scrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() != 0 && !e.isControlDown()) {
                double delta = e.getDeltaY();
                double factor = 0.015; // sutil pero funcional
                double newH = scrollPane.getHvalue() - delta * factor;
                scrollPane.setHvalue(Math.max(0, Math.min(1, newH)));
                e.consume();
            }
        });
        // Fondo igual al panel
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        chartArea.setStyle("-fx-background-color: " + COLOR_SURFACE() + ";");

        // REDIBUJADO EN TIEMPO REAL al cambiar tamaño de ventana
        scrollPane.heightProperty().addListener((obs, oldH, newH) -> {
            if (newH.doubleValue() > 0 && chartArea.getScene() != null) {
                javafx.application.Platform.runLater(() -> {
                    chartArea.setPrefHeight(newH.doubleValue());
                    List<Map.Entry<String, Integer>> dataList = new ArrayList<>(datos.evolucionPorDia.entrySet());
                    if (!dataList.isEmpty()) {
                        dibujarGraficoLinea(chartArea, dataList);
                    }
                });
            }
        });
        
        scrollPane.widthProperty().addListener((obs, oldW, newW) -> {
            if (newW.doubleValue() > 0 && chartArea.getScene() != null) {
                javafx.application.Platform.runLater(() -> {
                    // Si el ancho calculado es menor que el viewport, expandir para llenar
                    double viewportWidth = newW.doubleValue();
                    if (chartWidth < viewportWidth) {
                        chartArea.setPrefWidth(viewportWidth);
                        chartArea.setMinWidth(viewportWidth);
                        chartArea.setMaxWidth(viewportWidth);
                        List<Map.Entry<String, Integer>> dataList = new ArrayList<>(datos.evolucionPorDia.entrySet());
                        if (!dataList.isEmpty()) {
                            dibujarGraficoLinea(chartArea, dataList);
                        }
                    }
                });
            }
        });

        container.getChildren().setAll(headerBox, scrollPane);
        return container;
    }
    
    private static Pane crearGraficoLineaCustom(DatosEstadisticas datos) {
        Pane chartPane = new Pane();
        chartPane.setStyle("-fx-background-color: transparent;");
        
        // Convertir datos a lista
        List<Map.Entry<String, Integer>> dataList = new ArrayList<>(datos.evolucionPorDia.entrySet());
        if (dataList.isEmpty()) {
            Label sinDatos = crearLabel("Sin datos de evolución", 12, FontWeight.NORMAL, COLOR_TEXT_MUTED());
            sinDatos.setLayoutX(150);
            sinDatos.setLayoutY(100);
            chartPane.getChildren().add(sinDatos);
            return chartPane;
        }
        
        // Bandera para evitar múltiples invocaciones
        final boolean[] dibujado = {false};
        
        // Usar AnimationTimer para verificar cuando el panel tenga tamaño
        AnimationTimer renderTimer = new AnimationTimer() {
            private int frameCount = 0;
            private final int maxFrames = 120; // Intentar durante ~2 segundos
            
            @Override
            public void handle(long now) {
                frameCount++;
                if (dibujado[0] || frameCount > maxFrames) {
                    this.stop();
                    activeTimers.remove(this);
                    return;
                }
                
                // Verificar si el panel tiene tamaño válido y está en escena
                if (chartPane.getScene() != null && 
                    chartPane.getWidth() > 0 && 
                    chartPane.getHeight() > 0) {
                    dibujado[0] = true;
                    this.stop();
                    activeTimers.remove(this);
                    System.out.println("=== GRAFICO LINEA: Renderizando frame " + frameCount + 
                        " - Size: " + chartPane.getWidth() + "x" + chartPane.getHeight() + " ===");
                    dibujarGraficoLinea(chartPane, dataList);
                }
            }
        };
        
        // Registrar y iniciar el timer
        activeTimers.add(renderTimer);
        renderTimer.start();
        
        return chartPane;
    }
    
    private static void dibujarGraficoLinea(Pane pane, List<Map.Entry<String, Integer>> dataList) {
        // Verificar si el panel sigue en la escena
        if (pane.getScene() == null) return;
        
        pane.getChildren().clear();
        
        double width = pane.getWidth();
        double height = pane.getHeight();
        if (width <= 0 || height <= 0) return;
        
        double paddingLeft = 45;
        double paddingRight = 20;
        double paddingTop = 20;
        double paddingBottom = 35;
        
        double chartWidth = width - paddingLeft - paddingRight;
        double chartHeight = height - paddingTop - paddingBottom;
        
        // Encontrar valores min/max
        int maxVal = dataList.stream().mapToInt(Map.Entry::getValue).max().orElse(1);
        int minVal = 0;
        maxVal = Math.max(maxVal + (maxVal / 5), 1); // Agregar 20% de margen arriba
        
        // Calcular puntos
        List<Double> xPoints = new ArrayList<>();
        List<Double> yPoints = new ArrayList<>();
        
        for (int i = 0; i < dataList.size(); i++) {
            double x = paddingLeft + (chartWidth * i / Math.max(1, dataList.size() - 1));
            double y = paddingTop + chartHeight - (chartHeight * dataList.get(i).getValue() / maxVal);
            xPoints.add(x);
            yPoints.add(y);
        }
        
        // Si solo hay un punto, centrarlo
        if (dataList.size() == 1) {
            xPoints.set(0, paddingLeft + chartWidth / 2);
        }
        
        // === ÁREA CON GRADIENTE ===
        javafx.scene.shape.Path areaPath = new javafx.scene.shape.Path();
        areaPath.setStroke(null);
        
        // Crear gradiente vertical
        LinearGradient areaGradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(CYAN_BRIGHT, 0.3)),
            new Stop(0.5, Color.web(CYAN_BRIGHT, 0.1)),
            new Stop(1, Color.web(CYAN_BRIGHT, 0.0))
        );
        areaPath.setFill(areaGradient);
        
        // Construir path del área
        areaPath.getElements().add(new MoveTo(xPoints.get(0), paddingTop + chartHeight));
        areaPath.getElements().add(new LineTo(xPoints.get(0), yPoints.get(0)));
        for (int i = 1; i < xPoints.size(); i++) {
            areaPath.getElements().add(new LineTo(xPoints.get(i), yPoints.get(i)));
        }
        areaPath.getElements().add(new LineTo(xPoints.get(xPoints.size() - 1), paddingTop + chartHeight));
        areaPath.getElements().add(new ClosePath());
        
        // Animación del área
        areaPath.setOpacity(0);
        FadeTransition fadeArea = new FadeTransition(Duration.millis(800), areaPath);
        fadeArea.setFromValue(0);
        fadeArea.setToValue(1);
        fadeArea.setDelay(Duration.millis(400));
        fadeArea.play();
        
        pane.getChildren().add(areaPath);
        
        // === LÍNEA PRINCIPAL ===
        javafx.scene.shape.Path linePath = new javafx.scene.shape.Path();
        linePath.setStroke(Color.web(CYAN_BRIGHT));
        linePath.setStrokeWidth(2);
        linePath.setStrokeLineCap(StrokeLineCap.ROUND);
        linePath.setStrokeLineJoin(StrokeLineJoin.ROUND);
        linePath.setFill(null);
        
        // Glow effect
        DropShadow lineGlow = new DropShadow();
        lineGlow.setColor(Color.web(CYAN_BRIGHT, 0.6));
        lineGlow.setRadius(8);
        lineGlow.setSpread(0.3);
        linePath.setEffect(lineGlow);
        
        linePath.getElements().add(new MoveTo(xPoints.get(0), yPoints.get(0)));
        for (int i = 1; i < xPoints.size(); i++) {
            linePath.getElements().add(new LineTo(xPoints.get(i), yPoints.get(i)));
        }
        
        pane.getChildren().add(linePath);
        
        // Animación de la línea (dibujar progresivamente)
        animarLineaProgresiva(linePath, 800);
        
        // === LÍNEAS GUÍA INTERACTIVAS (ocultas inicialmente) ===
        Line guiaVertical = new Line();
        guiaVertical.setStroke(Color.web(CYAN_BRIGHT, 0.4));
        guiaVertical.setStrokeWidth(1);
        guiaVertical.getStrokeDashArray().addAll(4.0, 4.0);
        guiaVertical.setVisible(false);
        
        Line guiaHorizontal = new Line();
        guiaHorizontal.setStroke(Color.web(CYAN_BRIGHT, 0.4));
        guiaHorizontal.setStrokeWidth(1);
        guiaHorizontal.getStrokeDashArray().addAll(4.0, 4.0);
        guiaHorizontal.setVisible(false);
        
        pane.getChildren().addAll(guiaVertical, guiaHorizontal);
        
        // === PUNTO DE HOVER ===
        Circle hoverPoint = new Circle(6);
        hoverPoint.setFill(Color.web(CYAN_BRIGHT));
        hoverPoint.setStroke(TemaManager.getBgDarkerColor());
        hoverPoint.setStrokeWidth(2);
        hoverPoint.setVisible(false);
        
        DropShadow pointGlow = new DropShadow();
        pointGlow.setColor(Color.web(CYAN_BRIGHT, 0.8));
        pointGlow.setRadius(12);
        pointGlow.setSpread(0.4);
        hoverPoint.setEffect(pointGlow);
        
        pane.getChildren().add(hoverPoint);
        
        // === TOOLTIP FLOTANTE ===
        VBox tooltipBox = new VBox(2);
        tooltipBox.setAlignment(Pos.CENTER);
        tooltipBox.setPadding(new Insets(8, 12, 8, 12));
        tooltipBox.setStyle(
            "-fx-background-color: rgba(0, 212, 255, 0.15);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + CYAN_BRIGHT + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );
        tooltipBox.setVisible(false);
        
        Label tooltipValor = crearLabel("", 14, FontWeight.BOLD, CYAN_BRIGHT);
        Label tooltipFecha = crearLabel("", 10, FontWeight.NORMAL, COLOR_TEXT_MUTED());
        tooltipBox.getChildren().addAll(tooltipValor, tooltipFecha);
        
        pane.getChildren().add(tooltipBox);
        
        // === PUNTOS DE DATOS (pequeños, visibles siempre) ===
        for (int i = 0; i < xPoints.size(); i++) {
            Circle punto = new Circle(xPoints.get(i), yPoints.get(i), 4);
            punto.setFill(Color.web(CYAN_BRIGHT));
            punto.setStroke(Color.web(COLOR_SURFACE()));
            punto.setStrokeWidth(2);
            
            // Animación de aparición
            punto.setOpacity(0);
            FadeTransition fadePunto = new FadeTransition(Duration.millis(300), punto);
            fadePunto.setFromValue(0);
            fadePunto.setToValue(1);
            fadePunto.setDelay(Duration.millis(600 + i * 100));
            fadePunto.play();
            
            pane.getChildren().add(punto);
        }
        
        // === INTERACTIVIDAD FLUIDA ===
        final List<Map.Entry<String, Integer>> finalDataList = dataList;
        final List<Double> finalXPoints = xPoints;
        final List<Double> finalYPoints = yPoints;
        final double fChartHeight = chartHeight;
        final double fChartWidth = chartWidth;
        final double fPaddingTop = paddingTop;
        final double fPaddingLeft = paddingLeft;
        final double fWidth = width;
        
        // Variables para animaciones fluidas
        final int[] lastIdx = {-1};
        
        // Transiciones para movimiento suave
        TranslateTransition moveHoverPoint = new TranslateTransition(Duration.millis(80), hoverPoint);
        moveHoverPoint.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        FadeTransition fadeInGuiaV = new FadeTransition(Duration.millis(150), guiaVertical);
        FadeTransition fadeInGuiaH = new FadeTransition(Duration.millis(150), guiaHorizontal);
        FadeTransition fadeInHover = new FadeTransition(Duration.millis(150), hoverPoint);
        FadeTransition fadeInTooltip = new FadeTransition(Duration.millis(150), tooltipBox);
        
        // Configurar opacidad inicial
        guiaVertical.setOpacity(0);
        guiaHorizontal.setOpacity(0);
        hoverPoint.setOpacity(0);
        tooltipBox.setOpacity(0);
        guiaVertical.setVisible(true);
        guiaHorizontal.setVisible(true);
        hoverPoint.setVisible(true);
        tooltipBox.setVisible(true);
        
        pane.setOnMouseMoved(e -> {
            double mouseX = e.getX();
            
            // Encontrar el punto más cercano
            int closestIdx = -1;
            double closestDist = Double.MAX_VALUE;
            
            for (int i = 0; i < finalXPoints.size(); i++) {
                double dist = Math.abs(mouseX - finalXPoints.get(i));
                if (dist < closestDist && dist < 60) {
                    closestDist = dist;
                    closestIdx = i;
                }
            }
            
            if (closestIdx >= 0) {
                double px = finalXPoints.get(closestIdx);
                double py = finalYPoints.get(closestIdx);
                
                // Solo animar si cambió el punto
                boolean changed = (closestIdx != lastIdx[0]);
                lastIdx[0] = closestIdx;
                
                // Mover líneas guía suavemente
                guiaVertical.setStartX(px);
                guiaVertical.setStartY(fPaddingTop);
                guiaVertical.setEndX(px);
                guiaVertical.setEndY(fPaddingTop + fChartHeight);
                
                guiaHorizontal.setStartX(fPaddingLeft);
                guiaHorizontal.setStartY(py);
                guiaHorizontal.setEndX(fPaddingLeft + fChartWidth);
                guiaHorizontal.setEndY(py);
                
                // Mover punto con transición suave
                if (changed) {
                    // Animación de escala del punto al cambiar
                    ScaleTransition scalePop = new ScaleTransition(Duration.millis(100), hoverPoint);
                    scalePop.setFromX(1.0);
                    scalePop.setFromY(1.0);
                    scalePop.setToX(1.3);
                    scalePop.setToY(1.3);
                    scalePop.setAutoReverse(true);
                    scalePop.setCycleCount(2);
                    scalePop.play();
                }
                
                hoverPoint.setCenterX(px);
                hoverPoint.setCenterY(py);
                
                // Fade in elementos
                if (guiaVertical.getOpacity() < 1) {
                    fadeInGuiaV.setFromValue(guiaVertical.getOpacity());
                    fadeInGuiaV.setToValue(1);
                    fadeInGuiaV.play();
                    
                    fadeInGuiaH.setFromValue(guiaHorizontal.getOpacity());
                    fadeInGuiaH.setToValue(1);
                    fadeInGuiaH.play();
                    
                    fadeInHover.setFromValue(hoverPoint.getOpacity());
                    fadeInHover.setToValue(1);
                    fadeInHover.play();
                    
                    fadeInTooltip.setFromValue(tooltipBox.getOpacity());
                    fadeInTooltip.setToValue(1);
                    fadeInTooltip.play();
                }
                
                // Actualizar tooltip
                tooltipValor.setText(finalDataList.get(closestIdx).getValue() + " registros");
                tooltipFecha.setText(finalDataList.get(closestIdx).getKey());
                
                // Posicionar tooltip con offset dinámico
                double tooltipX = px + 15;
                double tooltipY = py - 45;
                if (tooltipX + 110 > fWidth) tooltipX = px - 120;
                if (tooltipY < 5) tooltipY = py + 20;
                
                tooltipBox.setLayoutX(tooltipX);
                tooltipBox.setLayoutY(tooltipY);
                
            } else {
                // Fade out si no hay punto cercano
                if (guiaVertical.getOpacity() > 0) {
                    fadeOutElementos(guiaVertical, guiaHorizontal, hoverPoint, tooltipBox);
                }
                lastIdx[0] = -1;
            }
        });
        
        pane.setOnMouseExited(e -> {
            fadeOutElementos(guiaVertical, guiaHorizontal, hoverPoint, tooltipBox);
            lastIdx[0] = -1;
        });
        
        // === ETIQUETAS DEL EJE Y (discretas) ===
        int numLabelsY = 5;
        for (int i = 0; i <= numLabelsY; i++) {
            double val = minVal + (maxVal - minVal) * i / numLabelsY;
            double y = paddingTop + chartHeight - (chartHeight * i / numLabelsY);
            
            Label lblY = crearLabel(String.valueOf((int) val), 9, FontWeight.NORMAL, COLOR_TEXT_MUTED());
            lblY.setLayoutX(5);
            lblY.setLayoutY(y - 6);
            pane.getChildren().add(lblY);
        }
        
        // === ETIQUETAS DEL EJE X (fechas discretas) ===
        int step = Math.max(1, dataList.size() / 5);
        for (int i = 0; i < dataList.size(); i += step) {
            Label lblX = crearLabel(dataList.get(i).getKey(), 9, FontWeight.NORMAL, COLOR_TEXT_MUTED());
            lblX.setLayoutX(xPoints.get(i) - 15);
            lblX.setLayoutY(paddingTop + chartHeight + 8);
            pane.getChildren().add(lblX);
        }
        // Siempre mostrar el último
        if (dataList.size() > 1 && (dataList.size() - 1) % step != 0) {
            int lastIdxLabel = dataList.size() - 1;
            Label lblXLast = crearLabel(dataList.get(lastIdxLabel).getKey(), 9, FontWeight.NORMAL, COLOR_TEXT_MUTED());
            lblXLast.setLayoutX(xPoints.get(lastIdxLabel) - 15);
            lblXLast.setLayoutY(paddingTop + chartHeight + 8);
            pane.getChildren().add(lblXLast);
        }
    }
    
    private static void fadeOutElementos(Node... nodes) {
        for (Node node : nodes) {
            // Verificar si el nodo sigue en la escena
            if (node.getScene() == null) continue;
            
            FadeTransition fade = new FadeTransition(Duration.millis(200), node);
            fade.setFromValue(node.getOpacity());
            fade.setToValue(0);
            fade.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            fade.play();
        }
    }
    
    private static void animarLineaProgresiva(javafx.scene.shape.Path path, double durationMs) {
        double length = calcularLongitudPath(path);
        
        // Evitar error "dash lengths all zero" si el path está vacío o tiene longitud 0
        if (length <= 0) {
            length = 1.0; // Valor mínimo seguro
        }
        
        path.getStrokeDashArray().clear(); // Limpiar valores anteriores
        path.getStrokeDashArray().add(length);
        path.setStrokeDashOffset(length);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(path.strokeDashOffsetProperty(), length)),
            new KeyFrame(Duration.millis(durationMs), new KeyValue(path.strokeDashOffsetProperty(), 0, AnimacionesFX.EASE_OUT_CUBIC))
        );
        timeline.play();
    }
    
    private static double calcularLongitudPath(javafx.scene.shape.Path path) {
        double length = 0;
        double lastX = 0, lastY = 0;
        
        for (PathElement elem : path.getElements()) {
            if (elem instanceof MoveTo) {
                lastX = ((MoveTo) elem).getX();
                lastY = ((MoveTo) elem).getY();
            } else if (elem instanceof LineTo) {
                double x = ((LineTo) elem).getX();
                double y = ((LineTo) elem).getY();
                length += Math.sqrt(Math.pow(x - lastX, 2) + Math.pow(y - lastY, 2));
                lastX = x;
                lastY = y;
            }
        }
        return length;
    }
    
    private static DropShadow crearGlowCyan() {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(CYAN_BRIGHT, 0.5));
        glow.setRadius(10);
        glow.setSpread(0.2);
        glow.setBlurType(BlurType.GAUSSIAN);
        return glow;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GRÁFICO CIRCULAR - DISTRIBUCIÓN SO (ESTILO MODERNO)
    // ════════════════════════════════════════════════════════════════════════════

    private static VBox crearGraficoCircular(DatosEstadisticas datos) {
        VBox container = new VBox(8);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(12));
        container.setPrefWidth(300);
        container.setMinWidth(300);
        container.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;"
        );
        
        Label titulo = crearLabel("Distribución por SO", 13, FontWeight.BOLD, COLOR_TEXT());
        titulo.setAlignment(Pos.CENTER);
        
        // Crear gráfico de dona moderno
        StackPane donutChart = crearDonutChartModerno(datos.distribucionSO, true);
        
        // Leyenda moderna
        VBox leyenda = crearLeyendaModerna(datos.distribucionSO);
        
        container.getChildren().addAll(titulo, donutChart, leyenda);
        
        return container;
    }
    
    private static StackPane crearDonutChartModerno(Map<String, Integer> distribucion, boolean mostrarPorcentaje) {
        StackPane container = new StackPane();
        container.setPrefSize(160, 160);
        container.setMinSize(160, 160);
        container.setMaxSize(160, 160);
        container.setAlignment(Pos.CENTER);
        
        if (distribucion.isEmpty()) {
            Label sinDatos = crearLabel("Sin datos", 11, FontWeight.NORMAL, COLOR_TEXT_MUTED());
            container.getChildren().add(sinDatos);
            return container;
        }
        
        double total = distribucion.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) total = 1;
        
        // Centro fijo
        double centerX = 80;
        double centerY = 80;
        double outerRadius = 70;
        double innerRadius = 46;
        
        // Pane con tamaño fijo
        Pane arcsPane = new Pane();
        arcsPane.setPrefSize(160, 160);
        arcsPane.setMinSize(160, 160);
        arcsPane.setMaxSize(160, 160);
        
        // Preparar datos de segmentos
        java.util.List<double[]> segmentosData = new java.util.ArrayList<>();
        java.util.List<javafx.scene.shape.Path> segmentos = new java.util.ArrayList<>();
        java.util.List<String> colores = new java.util.ArrayList<>();
        
        double startAngle = 90;
        int idx = 0;
        
        // Texto central - crearlo antes para poder actualizarlo en hover
        VBox centro = new VBox(2);
        centro.setAlignment(Pos.CENTER);
        centro.setMouseTransparent(true);
        
        final int totalInt = (int)total;
        Label lblTotal = crearLabel("0", 26, FontWeight.BOLD, CYAN_BRIGHT);
        lblTotal.setEffect(crearGlowCyan());
        Label lblSubtitulo = crearLabel("Total", 10, FontWeight.NORMAL, COLOR_TEXT_MUTED());
        
        centro.getChildren().addAll(lblTotal, lblSubtitulo);
        
        // Animación de conteo del número central
        animarConteo(lblTotal, 0, totalInt, 800);
        
        for (Map.Entry<String, Integer> entry : distribucion.entrySet()) {
            double extent = (entry.getValue() / total) * 360;
            String color = CHART_COLORS[idx % CHART_COLORS.length];
            
            // Crear segmento (inicialmente vacío)
            javafx.scene.shape.Path segmento = crearSegmentoDonut(centerX, centerY, innerRadius, outerRadius, startAngle, 0);
            segmento.setFill(Color.web(color));
            segmento.setStroke(Color.web(COLOR_SURFACE()));
            segmento.setStrokeWidth(1.5);
            
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web(color, 0.3));
            shadow.setRadius(8);
            shadow.setSpread(0.1);
            segmento.setEffect(shadow);
            
            arcsPane.getChildren().add(segmento);
            segmentos.add(segmento);
            colores.add(color);
            segmentosData.add(new double[]{startAngle, extent});
            
            // Hover interactivo - actualiza el centro
            final javafx.scene.shape.Path finalSegmento = segmento;
            final String itemName = entry.getKey();
            final int itemValue = entry.getValue();
            final int itemPercentInt = (int)Math.round((entry.getValue() / total) * 100);
            final Color originalColor = Color.web(color);
            final Color hoverColor = originalColor.brighter();
            final DropShadow originalShadow = shadow;
            final String colorStr = color;
            
            // Efecto glow suave para hover
            DropShadow hoverGlow = new DropShadow();
            hoverGlow.setColor(Color.web(color, 0.6));
            hoverGlow.setRadius(12);
            hoverGlow.setSpread(0.2);
            
            // Variable para rastrear el valor actual mostrado
            final int[] valorActualMostrado = {totalInt};
            
            segmento.setOnMouseEntered(e -> {
                // Animación suave de escala
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), finalSegmento);
                scaleUp.setToX(1.04);
                scaleUp.setToY(1.04);
                scaleUp.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                scaleUp.play();
                
                finalSegmento.setFill(hoverColor);
                finalSegmento.setEffect(hoverGlow);
                finalSegmento.toFront();
                container.setCursor(javafx.scene.Cursor.HAND);
                
                // Actualizar texto central con animación de conteo
                int valorAnterior = valorActualMostrado[0];
                valorActualMostrado[0] = itemValue;
                animarConteoInmediato(lblTotal, valorAnterior, itemValue, 300);
                lblTotal.setTextFill(Color.web(colorStr));
                DropShadow glow = new DropShadow();
                glow.setColor(Color.web(colorStr, 0.6));
                glow.setRadius(10);
                glow.setSpread(0.3);
                lblTotal.setEffect(glow);
                lblSubtitulo.setText(itemName + " (" + itemPercentInt + "%)");
            });
            
            segmento.setOnMouseExited(e -> {
                // Animación suave de regreso
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), finalSegmento);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                scaleDown.play();
                
                finalSegmento.setFill(originalColor);
                finalSegmento.setEffect(originalShadow);
                container.setCursor(javafx.scene.Cursor.DEFAULT);
                
                // Restaurar texto central al total con animación
                int valorAnterior = valorActualMostrado[0];
                valorActualMostrado[0] = totalInt;
                animarConteoInmediato(lblTotal, valorAnterior, totalInt, 300);
                lblTotal.setTextFill(Color.web(CYAN_BRIGHT));
                lblTotal.setEffect(crearGlowCyan());
                lblSubtitulo.setText("Total");
            });
            
            startAngle -= extent;
            idx++;
        }
        
        // Animación fluida de barrido: todos los segmentos crecen juntos
        final double duracionTotal = 800; // ms - animación suave
        final long[] animStartTime = {-1};
        
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (arcsPane.getScene() == null) {
                    this.stop();
                    return;
                }
                
                if (animStartTime[0] == -1) {
                    animStartTime[0] = now;
                }
                
                double elapsedMs = (now - animStartTime[0]) / 1_000_000.0;
                double progress = Math.min(elapsedMs / duracionTotal, 1.0);
                
                // Easing suave (ease-out-quart para más suavidad)
                double easedProgress = 1 - Math.pow(1 - progress, 4);
                double currentTotalAngle = 360 * easedProgress;
                
                // Actualizar cada segmento según el progreso global
                double acumulado = 0;
                for (int i = 0; i < segmentos.size(); i++) {
                    double[] data = segmentosData.get(i);
                    double segStartAngle = data[0];
                    double segExtent = data[1];
                    
                    // Calcular cuánto de este segmento mostrar
                    double segStart = acumulado;
                    double visibleExtent = 0;
                    if (currentTotalAngle > segStart) {
                        visibleExtent = Math.min(currentTotalAngle - segStart, segExtent);
                    }
                    
                    actualizarSegmentoDonut(segmentos.get(i), centerX, centerY, innerRadius, outerRadius, segStartAngle, visibleExtent);
                    acumulado += segExtent;
                }
                
                if (progress >= 1.0) {
                    this.stop();
                }
            }
        };
        activeTimers.add(timer);
        timer.start();
        
        // Fade del texto central sincronizado
        centro.setOpacity(0);
        centro.setScaleX(0.8);
        centro.setScaleY(0.8);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), centro);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setDelay(Duration.millis(500));
        
        FadeTransition fadeInCentro = new FadeTransition(Duration.millis(400), centro);
        fadeInCentro.setFromValue(0);
        fadeInCentro.setToValue(1);
        fadeInCentro.setDelay(Duration.millis(500));
        
        scaleIn.play();
        fadeInCentro.play();
        
        container.getChildren().addAll(arcsPane, centro);
        
        return container;
    }
    
    private static VBox crearLeyendaModerna(Map<String, Integer> distribucion) {
        VBox leyenda = new VBox(4);
        leyenda.setAlignment(Pos.CENTER_LEFT);
        leyenda.setPadding(new Insets(8, 4, 0, 4));
        
        int total = distribucion.values().stream().mapToInt(Integer::intValue).sum();
        int idx = 0;
        
        for (Map.Entry<String, Integer> entry : distribucion.entrySet()) {
            HBox item = new HBox(8);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(4, 8, 4, 8));
            item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03);" +
                "-fx-background-radius: 6;"
            );
            
            // Indicador de color con glow
            Circle dot = new Circle(4);
            String color = CHART_COLORS[idx % CHART_COLORS.length];
            dot.setFill(Color.web(color));
            DropShadow dotGlow = new DropShadow();
            dotGlow.setColor(Color.web(color, 0.5));
            dotGlow.setRadius(4);
            dot.setEffect(dotGlow);
            
            Label lblNombre = crearLabel(entry.getKey(), 11, FontWeight.MEDIUM, COLOR_TEXT());
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            int porcentaje = total > 0 ? (int)Math.round((entry.getValue() * 100.0 / total)) : 0;
            Label lblValor = crearLabel(entry.getValue() + " (" + porcentaje + "%)", 11, FontWeight.BOLD, color);
            
            item.getChildren().addAll(dot, lblNombre, spacer, lblValor);
            
            // Animación de entrada escalonada
            item.setOpacity(0);
            item.setTranslateX(-20);
            
            FadeTransition fadeItem = new FadeTransition(Duration.millis(300), item);
            fadeItem.setFromValue(0);
            fadeItem.setToValue(1);
            fadeItem.setDelay(Duration.millis(400 + idx * 80));
            
            TranslateTransition slideItem = new TranslateTransition(Duration.millis(300), item);
            slideItem.setFromX(-20);
            slideItem.setToX(0);
            slideItem.setDelay(Duration.millis(400 + idx * 80));
            slideItem.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            
            fadeItem.play();
            slideItem.play();
            
            // Hover effect
            item.setOnMouseEntered(e -> {
                item.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.08);" +
                    "-fx-background-radius: 6;"
                );
                AnimacionesFX.hoverIn(item, 1.03, 120);
            });
            item.setOnMouseExited(e -> {
                item.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.03);" +
                    "-fx-background-radius: 6;"
                );
                AnimacionesFX.hoverOut(item, 120);
            });
            
            leyenda.getChildren().add(item);
            idx++;
        }
        
        return leyenda;
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES PARA GRÁFICO DONUT
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Anima un conteo numérico de inicio a fin con delay (para carga inicial)
     */
    private static void animarConteo(Label label, int desde, int hasta, int duracionMs) {
        ComponentesFX.animarConteo(label, desde, hasta, duracionMs);
    }
    
    /**
     * Anima un conteo numérico inmediatamente (para hover)
     */
    private static void animarConteoInmediato(Label label, int desde, int hasta, int duracionMs) {
        // Calcular duración proporcional al tamaño del cambio
        int diferencia = Math.abs(hasta - desde);
        int duracionFinal;
        if (diferencia <= 20) {
            duracionFinal = 200;
        } else if (diferencia <= 50) {
            duracionFinal = 350;
        } else if (diferencia <= 200) {
            duracionFinal = 500;
        } else {
            duracionFinal = 700;
        }
        
        final int duracion = duracionFinal;
        final long[] startTime = {-1};
        AnimationTimer contador = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (startTime[0] == -1) {
                    startTime[0] = now;
                }
                
                double elapsedMs = (now - startTime[0]) / 1_000_000.0;
                double progress = Math.min(elapsedMs / duracion, 1.0);
                
                // Easing ease-out-cubic para animación suave
                double easedProgress = 1 - Math.pow(1 - progress, 3);
                
                int valorActual = desde + (int)((hasta - desde) * easedProgress);
                label.setText(String.valueOf(valorActual));
                
                if (progress >= 1.0) {
                    label.setText(String.valueOf(hasta));
                    this.stop();
                }
            }
        };
        activeTimers.add(contador);
        contador.start();
    }
    
    /**
     * Crea un segmento de donut (rosquilla) usando Path
     */
    private static javafx.scene.shape.Path crearSegmentoDonut(double cx, double cy, double innerR, double outerR, double startAngle, double extent) {
        javafx.scene.shape.Path path = new javafx.scene.shape.Path();
        actualizarSegmentoDonut(path, cx, cy, innerR, outerR, startAngle, extent);
        return path;
    }
    
    /**
     * Actualiza la geometría de un segmento donut
     */
    private static void actualizarSegmentoDonut(javafx.scene.shape.Path path, double cx, double cy, double innerR, double outerR, double startAngle, double extent) {
        path.getElements().clear();
        
        if (extent < 0.1) {
            return; // No dibujar si es muy pequeño
        }
        
        // Caso especial: círculo completo (o casi completo)
        if (extent >= 359.9) {
            // Dibujar un anillo completo usando dos semicírculos desde el mismo ángulo inicial
            double startRad = Math.toRadians(startAngle);
            double oppositeRad = Math.toRadians(startAngle - 180);
            
            // Punto inicial en el ángulo de inicio
            double startOuterX = cx + outerR * Math.cos(startRad);
            double startOuterY = cy - outerR * Math.sin(startRad);
            
            // Punto opuesto (180° después)
            double oppositeOuterX = cx + outerR * Math.cos(oppositeRad);
            double oppositeOuterY = cy - outerR * Math.sin(oppositeRad);
            
            // Puntos interiores
            double startInnerX = cx + innerR * Math.cos(startRad);
            double startInnerY = cy - innerR * Math.sin(startRad);
            double oppositeInnerX = cx + innerR * Math.cos(oppositeRad);
            double oppositeInnerY = cy - innerR * Math.sin(oppositeRad);
            
            // Arco exterior: dos semicírculos
            path.getElements().add(new MoveTo(startOuterX, startOuterY));
            path.getElements().add(new ArcTo(outerR, outerR, 0, oppositeOuterX, oppositeOuterY, true, true));
            path.getElements().add(new ArcTo(outerR, outerR, 0, startOuterX, startOuterY, true, true));
            // Línea al arco interior
            path.getElements().add(new LineTo(startInnerX, startInnerY));
            // Arco interior (dirección opuesta)
            path.getElements().add(new ArcTo(innerR, innerR, 0, oppositeInnerX, oppositeInnerY, true, false));
            path.getElements().add(new ArcTo(innerR, innerR, 0, startInnerX, startInnerY, true, false));
            path.getElements().add(new ClosePath());
            return;
        }
        
        double startRad = Math.toRadians(startAngle);
        double endRad = Math.toRadians(startAngle - extent);
        
        // Puntos del arco exterior
        double outerStartX = cx + outerR * Math.cos(startRad);
        double outerStartY = cy - outerR * Math.sin(startRad);
        double outerEndX = cx + outerR * Math.cos(endRad);
        double outerEndY = cy - outerR * Math.sin(endRad);
        
        // Puntos del arco interior
        double innerStartX = cx + innerR * Math.cos(endRad);
        double innerStartY = cy - innerR * Math.sin(endRad);
        double innerEndX = cx + innerR * Math.cos(startRad);
        double innerEndY = cy - innerR * Math.sin(startRad);
        
        boolean largeArc = extent > 180;
        
        // Construir el path: arco exterior -> línea -> arco interior -> cerrar
        path.getElements().add(new MoveTo(outerStartX, outerStartY));
        path.getElements().add(new ArcTo(outerR, outerR, 0, outerEndX, outerEndY, largeArc, true));
        path.getElements().add(new LineTo(innerStartX, innerStartY));
        path.getElements().add(new ArcTo(innerR, innerR, 0, innerEndX, innerEndY, largeArc, false));
        path.getElements().add(new ClosePath());
    }
    
    /**
     * Función de easing cúbico para animación fluida
     */
    private static double easeOutCubic(double t) {
        return 1 - Math.pow(1 - t, 3);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GRÁFICO DE BARRAS - TOP APLICACIONES
    // ════════════════════════════════════════════════════════════════════════════

    private static VBox crearGraficoBarras(DatosEstadisticas datos) {
        VBox container = new VBox(8);
        container.setPadding(new Insets(16, 16, 20, 16));
        container.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;"
        );
        
        // Header moderno
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 8, 0));
        
        // Icono de apps
        StackPane iconWrapper = new StackPane(IconosSVG.paquete("#3B82F6", 18));
        iconWrapper.setPadding(new Insets(6));
        iconWrapper.setStyle(
            "-fx-background-color: #3B82F615;" +
            "-fx-background-radius: 8;"
        );
        
        Label titulo = crearLabel("Apps Más Comunes", 13, FontWeight.BOLD, COLOR_TEXT());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Badge con total
        HBox badgeTotal = new HBox(4);
        badgeTotal.setAlignment(Pos.CENTER);
        badgeTotal.setPadding(new Insets(4, 10, 4, 10));
        badgeTotal.setStyle(
            "-fx-background-color: #3B82F620;" +
            "-fx-background-radius: 12;"
        );
        Label lblTotal = crearLabel(datos.totalApps + " únicas", 10, FontWeight.BOLD, "#3B82F6");
        badgeTotal.getChildren().add(lblTotal);
        
        headerBox.getChildren().addAll(iconWrapper, titulo, spacer, badgeTotal);
        
        // Contenedor de barras con espaciado mejorado
        VBox barrasContainer = new VBox(6);
        barrasContainer.setPadding(new Insets(4, 0, 0, 0));
        
        if (datos.topApps.isEmpty()) {
            Label sinDatos = crearLabel("Sin aplicaciones registradas", 11, FontWeight.NORMAL, COLOR_TEXT_SECONDARY());
            sinDatos.setPadding(new Insets(20, 0, 20, 0));
            barrasContainer.setAlignment(Pos.CENTER);
            barrasContainer.getChildren().add(sinDatos);
        } else {
            int maxValor = datos.topApps.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            
            int count = 0;
            for (Map.Entry<String, Integer> entry : datos.topApps.entrySet()) {
                if (count >= 5) break;
                
                HBox barraRow = crearBarraHorizontalModerna(
                    entry.getKey(), 
                    entry.getValue(), 
                    maxValor,
                    CHART_COLORS[count % CHART_COLORS.length],
                    count * 80,
                    datos.totalEquipos
                );
                barrasContainer.getChildren().add(barraRow);
                count++;
            }
        }
        
        container.getChildren().addAll(headerBox, barrasContainer);
        
        return container;
    }
    
    private static HBox crearBarraHorizontalModerna(String nombre, int valor, int maxValor, String color, int delay, int totalEquipos) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 10, 6, 10));
        row.setStyle(
            "-fx-background-color: rgba(255,255,255,0.02);" +
            "-fx-background-radius: 8;"
        );
        
        // Nombre de la app con tooltip
        String nombreCorto = nombre.length() > 20 ? nombre.substring(0, 17) + "..." : nombre;
        Label lblNombre = crearLabel(nombreCorto, 11, FontWeight.MEDIUM, COLOR_TEXT());
        lblNombre.setMinWidth(140);
        lblNombre.setMaxWidth(140);
        
        // Barra de progreso visual mejorada
        double porcentaje = maxValor > 0 ? (valor * 100.0 / maxValor) : 0;
        
        StackPane barraContainer = new StackPane();
        barraContainer.setAlignment(Pos.CENTER_LEFT);
        barraContainer.setMinHeight(22);
        barraContainer.setPrefHeight(22);
        HBox.setHgrow(barraContainer, Priority.ALWAYS);
        
        // Fondo de la barra - se expande completamente
        Region fondo = new Region();
        fondo.setPrefHeight(18);
        fondo.setMaxHeight(18);
        fondo.prefWidthProperty().bind(barraContainer.widthProperty());
        fondo.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-background-radius: 9;"
        );
        
        // Barra con gradiente
        Region barra = new Region();
        barra.setPrefHeight(18);
        barra.setMaxHeight(18);
        barra.setMaxWidth(0);
        barra.setStyle(
            "-fx-background-color: linear-gradient(to right, " + color + "99, " + color + ");" +
            "-fx-background-radius: 9;"
        );
        StackPane.setAlignment(barra, Pos.CENTER_LEFT);
        
        // Glow de la barra
        DropShadow barGlow = new DropShadow();
        barGlow.setColor(Color.web(color, 0.4));
        barGlow.setRadius(8);
        barGlow.setSpread(0.1);
        barra.setEffect(barGlow);
        
        // Animación de la barra - usa porcentaje del ancho del contenedor
        final long[] startTime = {-1};
        final double[] anchoFinalCalculado = {-1};
        
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (barra.getScene() == null) {
                    this.stop();
                    return;
                }
                
                // Esperar a que el contenedor tenga un ancho válido
                if (barraContainer.getWidth() <= 0) {
                    return;
                }
                
                // Calcular el ancho final una sola vez
                if (anchoFinalCalculado[0] == -1) {
                    anchoFinalCalculado[0] = barraContainer.getWidth() * (porcentaje / 100.0);
                }
                
                if (startTime[0] == -1) startTime[0] = now;
                double elapsedMs = (now - startTime[0]) / 1_000_000.0;
                if (elapsedMs < delay) return;
                double animProgress = (elapsedMs - delay) / 500.0;
                if (animProgress >= 1.0) {
                    barra.setMaxWidth(anchoFinalCalculado[0]);
                    this.stop();
                    return;
                }
                barra.setMaxWidth(anchoFinalCalculado[0] * easeOutCubic(animProgress));
            }
        };
        activeTimers.add(timer);
        timer.start();
        
        barraContainer.getChildren().addAll(fondo, barra);
        
        // Badge de equipos mejorado
        HBox equiposBadge = new HBox(2);
        equiposBadge.setAlignment(Pos.CENTER);
        equiposBadge.setPadding(new Insets(3, 8, 3, 8));
        equiposBadge.setMinWidth(55);
        equiposBadge.setStyle(
            "-fx-background-color: " + color + "25;" +
            "-fx-background-radius: 10;"
        );
        
        Label lblEquipos = crearLabel(valor + " eq.", 10, FontWeight.BOLD, color);
        equiposBadge.getChildren().add(lblEquipos);
        
        // Porcentaje
        double porcTotal = totalEquipos > 0 ? (valor * 100.0 / totalEquipos) : 0;
        Label lblPorcentaje = crearLabel(String.format("%.0f%%", porcTotal), 10, FontWeight.MEDIUM, COLOR_TEXT_MUTED());
        lblPorcentaje.setMinWidth(32);
        lblPorcentaje.setAlignment(Pos.CENTER_RIGHT);
        
        // Hover effect mejorado
        row.setOnMouseEntered(e -> {
            row.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                "-fx-background-radius: 8;"
            );
            barGlow.setRadius(12);
            barGlow.setColor(Color.web(color, 0.6));
            equiposBadge.setStyle(
                "-fx-background-color: " + color + "35;" +
                "-fx-background-radius: 10;"
            );
            AnimacionesFX.hoverIn(row, 1.02, 150);
        });
        
        row.setOnMouseExited(e -> {
            row.setStyle(
                "-fx-background-color: rgba(255,255,255,0.02);" +
                "-fx-background-radius: 8;"
            );
            barGlow.setRadius(8);
            barGlow.setColor(Color.web(color, 0.4));
            equiposBadge.setStyle(
                "-fx-background-color: " + color + "25;" +
                "-fx-background-radius: 10;"
            );
            AnimacionesFX.hoverOut(row, 150);
        });
        
        // Tooltip informativo
        Tooltip tooltip = new Tooltip(
            nombre + "\nInstalada en " + valor + " de " + totalEquipos + " equipos (" + String.format("%.1f%%", porcTotal) + ")"
        );
        tooltip.setStyle(
            "-fx-background-color: rgba(20,20,25,0.95);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 8 12;" +
            "-fx-background-radius: 8;"
        );
        Tooltip.install(row, tooltip);
        
        // Animación de entrada
        row.setOpacity(0);
        row.setTranslateX(-10);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), row);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.millis(delay));
        fadeIn.play();
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), row);
        slideIn.setFromX(-10);
        slideIn.setToX(0);
        slideIn.setDelay(Duration.millis(delay));
        slideIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        slideIn.play();
        
        row.getChildren().addAll(lblNombre, barraContainer, equiposBadge, lblPorcentaje);
        
        return row;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // GRÁFICO CIRCULAR - DISTRIBUCIÓN POR MARCA (DATOS REALES)
    // ════════════════════════════════════════════════════════════════════════════

    private static final String[] MARCA_COLORS = {
        "#FF6B35",  // Naranja vibrante
        "#00D4FF",  // Cyan
        "#9D4EDD",  // Púrpura
        "#00F5D4",  // Turquesa
        "#FFE66D",  // Amarillo
        "#FF006E",  // Magenta
        "#7209B7",  // Violeta
        "#4CC9F0"   // Azul claro
    };

    private static VBox crearGraficoEstadoEquipos(DatosEstadisticas datos) {
        VBox container = new VBox(8);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(12));
        container.setPrefWidth(300);
        container.setMinWidth(300);
        container.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;"
        );
        
        Label titulo = crearLabel("Distribución por Marca", 13, FontWeight.BOLD, COLOR_TEXT());
        titulo.setAlignment(Pos.CENTER);
        
        // Usar datos reales de marcas
        Map<String, Integer> distribucion = datos.distribucionMarca.isEmpty() ? 
            datos.distribucionTipo : datos.distribucionMarca;
        
        if (distribucion.isEmpty()) {
            Label sinDatos = crearLabel("Sin datos disponibles", 14, FontWeight.NORMAL, COLOR_TEXT_SECONDARY());
            container.getChildren().addAll(titulo, sinDatos);
            return container;
        }
        
        // Crear gráfico donut moderno (mismo estilo que SO)
        StackPane donutChart = crearDonutChartMarca(distribucion);
        
        // Leyenda moderna (mismo estilo que SO)
        VBox leyenda = crearLeyendaMarca(distribucion);
        
        container.getChildren().addAll(titulo, donutChart, leyenda);
        
        return container;
    }

    /**
     * Crea un gráfico donut para distribución por marca
     * Mismo estilo que el de Sistema Operativo
     */
    private static StackPane crearDonutChartMarca(Map<String, Integer> distribucion) {
        StackPane container = new StackPane();
        container.setPrefSize(160, 160);
        container.setMinSize(160, 160);
        container.setMaxSize(160, 160);
        container.setAlignment(Pos.CENTER);
        
        double total = distribucion.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) total = 1;
        
        // Centro fijo (igual que SO)
        double centerX = 80;
        double centerY = 80;
        double outerRadius = 70;
        double innerRadius = 46;
        
        // Pane con tamaño fijo
        Pane arcsPane = new Pane();
        arcsPane.setPrefSize(160, 160);
        arcsPane.setMinSize(160, 160);
        arcsPane.setMaxSize(160, 160);
        
        // Preparar datos de segmentos
        java.util.List<double[]> segmentosData = new java.util.ArrayList<>();
        java.util.List<javafx.scene.shape.Path> segmentos = new java.util.ArrayList<>();
        java.util.List<String> colores = new java.util.ArrayList<>();
        
        double startAngle = 90;
        int idx = 0;
        
        // Texto central - crearlo antes para poder actualizarlo en hover
        VBox centro = new VBox(2);
        centro.setAlignment(Pos.CENTER);
        centro.setMouseTransparent(true);
        
        final int totalInt = (int)total;
        Label lblTotal = crearLabel("0", 26, FontWeight.BOLD, MARCA_COLORS[0]);
        DropShadow glowNaranja = new DropShadow();
        glowNaranja.setColor(Color.web(MARCA_COLORS[0], 0.5));
        glowNaranja.setRadius(8);
        glowNaranja.setSpread(0.2);
        lblTotal.setEffect(glowNaranja);
        
        Label lblSubtitulo = crearLabel("Total", 10, FontWeight.NORMAL, COLOR_TEXT_MUTED());
        
        centro.getChildren().addAll(lblTotal, lblSubtitulo);
        
        // Animación de conteo del número central
        animarConteo(lblTotal, 0, totalInt, 800);
        
        for (Map.Entry<String, Integer> entry : distribucion.entrySet()) {
            double extent = (entry.getValue() / total) * 360;
            String color = MARCA_COLORS[idx % MARCA_COLORS.length];
            
            javafx.scene.shape.Path segmento = crearSegmentoDonut(centerX, centerY, innerRadius, outerRadius, startAngle, 0);
            segmento.setFill(Color.web(color));
            segmento.setStroke(Color.web(COLOR_SURFACE()));
            segmento.setStrokeWidth(1.5);
            
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web(color, 0.3));
            shadow.setRadius(8);
            shadow.setSpread(0.1);
            segmento.setEffect(shadow);
            
            arcsPane.getChildren().add(segmento);
            segmentos.add(segmento);
            colores.add(color);
            segmentosData.add(new double[]{startAngle, extent});
            
            // Hover interactivo - actualiza el centro
            final javafx.scene.shape.Path finalSegmento = segmento;
            final String itemName = entry.getKey();
            final int itemValue = entry.getValue();
            final int itemPercentInt = (int)Math.round((entry.getValue() / total) * 100);
            final Color originalColor = Color.web(color);
            final Color hoverColor = originalColor.brighter();
            final DropShadow originalShadow = shadow;
            final String colorStr = color;
            
            // Efecto glow suave para hover
            DropShadow hoverGlow = new DropShadow();
            hoverGlow.setColor(Color.web(color, 0.6));
            hoverGlow.setRadius(12);
            hoverGlow.setSpread(0.2);
            
            // Variable para rastrear el valor actual mostrado
            final int[] valorActualMostrado = {totalInt};
            
            segmento.setOnMouseEntered(e -> {
                // Animación suave de escala
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), finalSegmento);
                scaleUp.setToX(1.04);
                scaleUp.setToY(1.04);
                scaleUp.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                scaleUp.play();
                
                finalSegmento.setFill(hoverColor);
                finalSegmento.setEffect(hoverGlow);
                finalSegmento.toFront();
                container.setCursor(Cursor.HAND);
                
                // Actualizar texto central con animación de conteo
                int valorAnterior = valorActualMostrado[0];
                valorActualMostrado[0] = itemValue;
                animarConteoInmediato(lblTotal, valorAnterior, itemValue, 300);
                lblTotal.setTextFill(Color.web(colorStr));
                DropShadow glow = new DropShadow();
                glow.setColor(Color.web(colorStr, 0.6));
                glow.setRadius(10);
                glow.setSpread(0.3);
                lblTotal.setEffect(glow);
                lblSubtitulo.setText(itemName + " (" + itemPercentInt + "%)");
            });
            
            segmento.setOnMouseExited(e -> {
                // Animación suave de regreso
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), finalSegmento);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                scaleDown.play();
                
                finalSegmento.setFill(originalColor);
                finalSegmento.setEffect(originalShadow);
                container.setCursor(Cursor.DEFAULT);
                
                // Restaurar texto central al total con animación
                int valorAnterior = valorActualMostrado[0];
                valorActualMostrado[0] = totalInt;
                animarConteoInmediato(lblTotal, valorAnterior, totalInt, 300);
                lblTotal.setTextFill(Color.web(MARCA_COLORS[0]));
                lblTotal.setEffect(glowNaranja);
                lblSubtitulo.setText("Total");
            });
            
            startAngle -= extent;
            idx++;
        }
        
        // Animación fluida de barrido
        final double duracionTotal = 800;
        final long[] animStartTime = {-1};
        
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (arcsPane.getScene() == null) {
                    this.stop();
                    return;
                }
                
                if (animStartTime[0] == -1) {
                    animStartTime[0] = now;
                }
                
                double elapsedMs = (now - animStartTime[0]) / 1_000_000.0;
                double progress = Math.min(elapsedMs / duracionTotal, 1.0);
                
                // Easing suave
                double easedProgress = 1 - Math.pow(1 - progress, 4);
                double currentTotalAngle = 360 * easedProgress;
                
                double acumulado = 0;
                for (int i = 0; i < segmentos.size(); i++) {
                    double[] data = segmentosData.get(i);
                    double segStartAngle = data[0];
                    double segExtent = data[1];
                    
                    double segStart = acumulado;
                    double visibleExtent = 0;
                    if (currentTotalAngle > segStart) {
                        visibleExtent = Math.min(currentTotalAngle - segStart, segExtent);
                    }
                    
                    actualizarSegmentoDonut(segmentos.get(i), centerX, centerY, innerRadius, outerRadius, segStartAngle, visibleExtent);
                    acumulado += segExtent;
                }
                
                if (progress >= 1.0) {
                    this.stop();
                }
            }
        };
        activeTimers.add(timer);
        timer.start();
        
        // Animación del texto central con scale + fade
        centro.setOpacity(0);
        centro.setScaleX(0.8);
        centro.setScaleY(0.8);
        
        FadeTransition fadeInCentro = new FadeTransition(Duration.millis(400), centro);
        fadeInCentro.setFromValue(0);
        fadeInCentro.setToValue(1);
        fadeInCentro.setDelay(Duration.millis(300));
        
        ScaleTransition scaleCentro = new ScaleTransition(Duration.millis(400), centro);
        scaleCentro.setFromX(0.8);
        scaleCentro.setFromY(0.8);
        scaleCentro.setToX(1.0);
        scaleCentro.setToY(1.0);
        scaleCentro.setDelay(Duration.millis(300));
        scaleCentro.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        fadeInCentro.play();
        scaleCentro.play();
        
        container.getChildren().addAll(arcsPane, centro);
        
        return container;
    }
    
    /**
     * Crea leyenda moderna para el gráfico de marcas
     * Mismo estilo que la leyenda de SO
     */
    private static VBox crearLeyendaMarca(Map<String, Integer> distribucion) {
        VBox leyenda = new VBox(4);
        leyenda.setAlignment(Pos.CENTER_LEFT);
        leyenda.setPadding(new Insets(8, 4, 0, 4));
        
        int total = distribucion.values().stream().mapToInt(Integer::intValue).sum();
        int idx = 0;
        
        // Limitar a las 4 marcas principales (igual que SO)
        List<Map.Entry<String, Integer>> topMarcas = distribucion.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(4)
            .collect(java.util.stream.Collectors.toList());
        
        for (Map.Entry<String, Integer> entry : topMarcas) {
            HBox item = new HBox(8);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(4, 8, 4, 8));
            item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03);" +
                "-fx-background-radius: 6;"
            );
            
            // Indicador de color con glow
            Circle dot = new Circle(4);
            String color = MARCA_COLORS[idx % MARCA_COLORS.length];
            dot.setFill(Color.web(color));
            DropShadow dotGlow = new DropShadow();
            dotGlow.setColor(Color.web(color, 0.5));
            dotGlow.setRadius(4);
            dot.setEffect(dotGlow);
            
            Label lblNombre = crearLabel(entry.getKey(), 11, FontWeight.MEDIUM, COLOR_TEXT());
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            int porcentaje = total > 0 ? (int)Math.round((entry.getValue() * 100.0 / total)) : 0;
            Label lblValor = crearLabel(entry.getValue() + " (" + porcentaje + "%)", 11, FontWeight.BOLD, color);
            
            item.getChildren().addAll(dot, lblNombre, spacer, lblValor);
            
            // Hover effect
            final String finalColor = color;
            item.setOnMouseEntered(e -> {
                item.setStyle(
                    "-fx-background-color: " + finalColor + "15;" +
                    "-fx-background-radius: 6;"
                );
                AnimacionesFX.hoverIn(item, 1.03, 120);
            });
            
            item.setOnMouseExited(e -> {
                item.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.03);" +
                    "-fx-background-radius: 6;"
                );
                AnimacionesFX.hoverOut(item, 120);
            });
            
            // Animación de entrada más rápida
            item.setTranslateX(-10);
            item.setOpacity(0);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), item);
            slideIn.setFromX(-10);
            slideIn.setToX(0);
            slideIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            slideIn.setDelay(Duration.millis(200 + idx * 50));
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), item);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(200 + idx * 50));
            
            slideIn.play();
            fadeIn.play();
            
            leyenda.getChildren().add(item);
            idx++;
        }
        
        return leyenda;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CARGA DE DATOS
    // ════════════════════════════════════════════════════════════════════════════

    private static DatosEstadisticas cargarDatos(Path rutaExcel) {
        DatosEstadisticas datos = new DatosEstadisticas();
        
        try {
            XSSFWorkbook wb = abrirCifradoProyecto(rutaExcel);
            if (wb != null) {
                // Procesar SystemInfo
                Sheet hojaSystem = wb.getSheet("SystemInfo");
                if (hojaSystem != null && hojaSystem.getLastRowNum() >= 2) {
                    // Obtener índices de columnas desde el header
                    Row headerRow = hojaSystem.getRow(1);
                    Map<String, Integer> columnIndex = new HashMap<>();
                    if (headerRow != null) {
                        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                            Cell cell = headerRow.getCell(c);
                            if (cell != null) {
                                columnIndex.put(cell.getStringCellValue().trim(), c);
                            }
                        }
                    }
                    
                    int colFecha = columnIndex.getOrDefault("Fecha", 0);
                    int colSO = columnIndex.getOrDefault("Sistema Operativo", 3);
                    int colHostname = columnIndex.getOrDefault("Hostname", 2);
                    int colMarca = columnIndex.getOrDefault("Marca", 5);
                    int colTipo = columnIndex.getOrDefault("Tipo Dispositivo", 6);
                    int colCPU = columnIndex.getOrDefault("CPU", 8);
                    int colRAM = columnIndex.getOrDefault("RAM", 10);
                    
                    // Contar solo filas con datos reales
                    datos.totalEquipos = 0;
                    
                    Set<String> hostnames = new HashSet<>();
                    String ultimaFecha = "";
                    Map<String, Integer> conteoPorDia = new TreeMap<>(); // TreeMap para ordenar por fecha
                    
                    // Recorrer todos los equipos
                    for (int i = 2; i <= hojaSystem.getLastRowNum(); i++) {
                        Row row = hojaSystem.getRow(i);
                        if (row == null || esFilaVacia(row)) continue;
                        
                        // Incrementar contador de equipos
                        datos.totalEquipos++;
                        
                        // Sistema Operativo
                        String os = obtenerValorCelda(row.getCell(colSO));
                        if (!os.isEmpty()) {
                            String osSimplificado = simplificarNombreOS(os);
                            datos.distribucionSO.merge(osSimplificado, 1, Integer::sum);
                        }
                        
                        // Marca del equipo
                        String marca = obtenerValorCelda(row.getCell(colMarca));
                        if (!marca.isEmpty()) {
                            datos.distribucionMarca.merge(marca, 1, Integer::sum);
                        }
                        
                        // Tipo de dispositivo
                        String tipo = obtenerValorCelda(row.getCell(colTipo));
                        if (!tipo.isEmpty()) {
                            datos.distribucionTipo.merge(tipo, 1, Integer::sum);
                        }
                        
                        // Hostname (para contar únicos)
                        String hostname = obtenerValorCelda(row.getCell(colHostname));
                        if (!hostname.isEmpty()) {
                            hostnames.add(hostname.toLowerCase());
                        }
                        
                        // Fecha (obtener la más reciente y agrupar por día)
                        String fecha = obtenerValorCelda(row.getCell(colFecha));
                        if (!fecha.isEmpty()) {
                            ultimaFecha = fecha;
                            // Extraer fecha para evolución
                            String diaKey = extraerFechaDia(fecha);
                            if (!diaKey.isEmpty()) {
                                conteoPorDia.merge(diaKey, 1, Integer::sum);
                            }
                        }
                    }
                    
                    datos.equiposUnicos = hostnames.size();
                    datos.ultimoScan = ultimaFecha.isEmpty() ? "N/A" : ultimaFecha;
                    
                    // Evolución por día - tomar los últimos 10 días con datos
                    List<Map.Entry<String, Integer>> listaDias = new ArrayList<>(conteoPorDia.entrySet());
                    int inicio = Math.max(0, listaDias.size() - 10);
                    for (int i = inicio; i < listaDias.size(); i++) {
                        Map.Entry<String, Integer> entry = listaDias.get(i);
                        // Convertir clave de ordenación a formato legible
                        String diaLegible = formatearDiaLegible(entry.getKey());
                        datos.evolucionPorDia.put(diaLegible, entry.getValue());
                    }
                    
                    // SO principal
                    datos.soPrincipal = datos.distribucionSO.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("N/A");
                }
                
                // Procesar InstalledApps
                Sheet hojaApps = wb.getSheet("InstalledApps");
                if (hojaApps != null && hojaApps.getLastRowNum() >= 1) {
                    // Obtener índices de columnas desde el header (fila 1, no 0)
                    Row headerRow = hojaApps.getRow(1);
                    Map<String, Integer> columnIndex = new HashMap<>();
                    if (headerRow != null) {
                        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                            Cell cell = headerRow.getCell(c);
                            if (cell != null) {
                                columnIndex.put(cell.getStringCellValue().trim(), c);
                            }
                        }
                    }
                    
                    int colApp = columnIndex.getOrDefault("Aplicación", 4);
                    int colFabricante = columnIndex.getOrDefault("Fabricante", 6);
                    
                    Map<String, Integer> conteoApps = new HashMap<>();
                    Map<String, Integer> conteoFabricantes = new HashMap<>();
                    Set<String> appsUnicas = new HashSet<>();
                    
                    // Comenzar desde fila 2 para saltar encabezados
                    for (int i = 2; i <= hojaApps.getLastRowNum(); i++) {
                        Row row = hojaApps.getRow(i);
                        if (row == null || esFilaVacia(row)) continue;
                        
                        // Nombre de la aplicación
                        String appName = obtenerValorCelda(row.getCell(colApp));
                        if (!appName.isEmpty()) {
                            conteoApps.merge(appName, 1, Integer::sum);
                            appsUnicas.add(appName.toLowerCase());
                            datos.totalAppsInstalaciones++;
                        }
                        
                        // Fabricante
                        String fabricante = obtenerValorCelda(row.getCell(colFabricante));
                        if (!fabricante.isEmpty()) {
                            String fabSimplificado = simplificarFabricante(fabricante);
                            conteoFabricantes.merge(fabSimplificado, 1, Integer::sum);
                        }
                    }
                    
                    datos.totalApps = appsUnicas.size();
                    
                    // Top apps ordenadas con prioridad por tipo de usuario
                    datos.topApps = new LinkedHashMap<>();
                    conteoApps.entrySet().stream()
                        .sorted((e1, e2) -> {
                            // Calcular puntuación combinando cantidad e importancia
                            double puntuacion1 = calcularPuntuacionApp(e1.getKey(), e1.getValue());
                            double puntuacion2 = calcularPuntuacionApp(e2.getKey(), e2.getValue());
                            return Double.compare(puntuacion2, puntuacion1); // Orden descendente
                        })
                        .limit(10)
                        .forEach(e -> datos.topApps.put(e.getKey(), e.getValue()));
                    
                    // Top fabricantes
                    datos.topFabricantes = new LinkedHashMap<>();
                    conteoFabricantes.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(8)
                        .forEach(e -> datos.topFabricantes.put(e.getKey(), e.getValue()));
                }
                
                wb.close();
            }
        } catch (Exception e) {
            AppLogger.getLogger(EstadisticasFX.class).error("Error: " + e.getMessage(), e);
        }
        
        // Si no hay datos, establecer valores por defecto
        if (datos.totalEquipos == 0) {
            datos.soPrincipal = "Sin datos";
            datos.ultimoScan = "N/A";
        }
        
        return datos;
    }
    
    /**
     * Calcula la puntuación de una aplicación considerando:
     * - Cantidad de instalaciones
     * - Relevancia para usuarios de oficina y estudiantes
     */
    private static double calcularPuntuacionApp(String nombreApp, int cantidadInstalaciones) {
        String appLower = nombreApp.toLowerCase();
        
        // Puntuación base: cantidad de instalaciones
        double puntuacion = cantidadInstalaciones * 10.0;
        
        // MULTIPLICADORES POR CATEGORÍA (priorizando usuarios de oficina y estudiantes)
        
        // Office y Productividad - Máxima prioridad (x3.5)
        if (appLower.contains("office") || 
            appLower.contains("word") || 
            appLower.contains("excel") || 
            appLower.contains("powerpoint") || 
            appLower.contains("outlook") || 
            appLower.contains("onenote") || 
            appLower.contains("access") || 
            appLower.contains("publisher") ||
            appLower.contains("libreoffice") ||
            appLower.contains("openoffice")) {
            puntuacion *= 3.5;
        }
        
        // Comunicación y Colaboración (x3.2)
        else if (appLower.contains("teams") || 
                 appLower.contains("zoom") || 
                 appLower.contains("skype") || 
                 appLower.contains("slack") ||
                 appLower.contains("webex") ||
                 appLower.contains("google meet") ||
                 appLower.contains("discord")) {
            puntuacion *= 3.2;
        }
        
        // Navegadores Web (x3.0)
        else if (appLower.contains("chrome") || 
                 appLower.contains("firefox") || 
                 appLower.contains("edge") || 
                 appLower.contains("opera") || 
                 appLower.contains("brave") ||
                 appLower.contains("safari")) {
            puntuacion *= 3.0;
        }
        
        // Aplicaciones Educativas para Niños (x3.0)
        else if (appLower.contains("educati") || 
                 appLower.contains("school") || 
                 appLower.contains("student") || 
                 appLower.contains("learn") || 
                 appLower.contains("classroom") || 
                 appLower.contains("scratch") || 
                 appLower.contains("minecraft") || 
                 appLower.contains("math") || 
                 appLower.contains("reading") ||
                 appLower.contains("quiz") ||
                 appLower.contains("canvas") ||
                 appLower.contains("moodle") ||
                 appLower.contains("kahoot") ||
                 appLower.contains("duolingo")) {
            puntuacion *= 3.0;
        }
        
        // Lectores PDF y Documentos (x2.8)
        else if (appLower.contains("adobe reader") || 
                 appLower.contains("acrobat") || 
                 appLower.contains("pdf") || 
                 appLower.contains("foxit") || 
                 appLower.contains("nitro") ||
                 appLower.contains("sumatra")) {
            puntuacion *= 2.8;
        }
        
        // Compresores y Utilidades Básicas (x2.5)
        else if (appLower.contains("winrar") || 
                 appLower.contains("7-zip") || 
                 appLower.contains("winzip") || 
                 appLower.contains("notepad++") || 
                 appLower.contains("paint") ||
                 appLower.contains("calculator")) {
            puntuacion *= 2.5;
        }
        
        // Multimedia Básica (x2.3)
        else if (appLower.contains("vlc") || 
                 appLower.contains("media player") || 
                 appLower.contains("spotify") || 
                 appLower.contains("itunes") ||
                 appLower.contains("windows media") ||
                 appLower.contains("quicktime")) {
            puntuacion *= 2.3;
        }
        
        // Almacenamiento en la Nube (x2.2)
        else if (appLower.contains("dropbox") || 
                 appLower.contains("onedrive") || 
                 appLower.contains("google drive") || 
                 appLower.contains("icloud") ||
                 appLower.contains("box sync")) {
            puntuacion *= 2.2;
        }
        
        // Aplicaciones de Seguridad Básicas (x2.0)
        else if (appLower.contains("antivirus") || 
                 appLower.contains("defender") || 
                 appLower.contains("kaspersky") || 
                 appLower.contains("mcafee") || 
                 appLower.contains("norton") ||
                 appLower.contains("avast") ||
                 appLower.contains("avg")) {
            puntuacion *= 2.0;
        }
        
        // Aplicaciones especializadas simples (x1.7)
        else if (appLower.contains("photoshop") || 
                 appLower.contains("illustrator") || 
                 appLower.contains("gimp") || 
                 appLower.contains("paint.net") ||
                 appLower.contains("canva")) {
            puntuacion *= 1.7;
        }
        
        // Herramientas de Desarrollo (x1.5) - Menor prioridad
        else if (appLower.contains("visual studio") || 
                 appLower.contains("eclipse") || 
                 appLower.contains("intellij") || 
                 appLower.contains("git") || 
                 appLower.contains("python") ||
                 appLower.contains("java development")) {
            puntuacion *= 1.5;
        }
        
        // Aplicaciones de Administración (x1.3) - Menor prioridad
        else if (appLower.contains("server") || 
                 appLower.contains("sql") || 
                 appLower.contains("database") || 
                 appLower.contains("oracle") || 
                 appLower.contains("vmware") || 
                 appLower.contains("docker") ||
                 appLower.contains("powershell")) {
            puntuacion *= 1.3;
        }
        
        return puntuacion;
    }
    
    private static String simplificarFabricante(String fabricante) {
        if (fabricante == null || fabricante.isEmpty()) return "Otros";
        fabricante = fabricante.toLowerCase();
        
        if (fabricante.contains("microsoft")) return "Microsoft";
        if (fabricante.contains("google")) return "Google";
        if (fabricante.contains("adobe")) return "Adobe";
        if (fabricante.contains("oracle") || fabricante.contains("java")) return "Oracle";
        if (fabricante.contains("apple")) return "Apple";
        if (fabricante.contains("intel")) return "Intel";
        if (fabricante.contains("nvidia")) return "NVIDIA";
        if (fabricante.contains("amd")) return "AMD";
        if (fabricante.contains("mozilla")) return "Mozilla";
        if (fabricante.contains("zoom")) return "Zoom";
        if (fabricante.contains("slack")) return "Slack";
        if (fabricante.contains("vmware")) return "VMware";
        if (fabricante.contains("cisco")) return "Cisco";
        if (fabricante.contains("dell")) return "Dell";
        if (fabricante.contains("hp") || fabricante.contains("hewlett")) return "HP";
        if (fabricante.contains("lenovo")) return "Lenovo";
        
        // Si el nombre es muy largo, truncar
        if (fabricante.length() > 20) {
            return fabricante.substring(0, 17) + "...";
        }
        
        return fabricante.substring(0, 1).toUpperCase() + fabricante.substring(1);
    }

    private static String simplificarNombreOS(String os) {
        if (os == null || os.isEmpty()) return "Otro";
        os = os.toLowerCase();
        
        if (os.contains("windows 11")) return "Windows 11";
        if (os.contains("windows 10")) return "Windows 10";
        if (os.contains("windows 7")) return "Windows 7";
        if (os.contains("windows")) return "Windows";
        if (os.contains("mac") || os.contains("darwin")) return "macOS";
        if (os.contains("linux") || os.contains("ubuntu")) return "Linux";
        
        return "Otro";
    }
    
    private static String extraerFechaDia(String fecha) {
        if (fecha == null || fecha.isEmpty()) return "";
        
        try {
            int dia = -1, mes = -1, anio = -1;
            
            // Intentar formato yyyy-MM-dd HH:mm:ss o yyyy-MM-dd
            if (fecha.contains("-") && fecha.indexOf("-") == 4) {
                String fechaParte = fecha.split(" ")[0]; // Quitar hora si existe
                String[] partes = fechaParte.split("-");
                if (partes.length >= 3) {
                    anio = Integer.parseInt(partes[0].trim());
                    mes = Integer.parseInt(partes[1].trim());
                    dia = Integer.parseInt(partes[2].trim());
                }
            }
            // Intentar formato dd/MM/yyyy
            else if (fecha.contains("/")) {
                String fechaParte = fecha.split(" ")[0];
                String[] partes = fechaParte.split("/");
                if (partes.length >= 3) {
                    dia = Integer.parseInt(partes[0].trim());
                    mes = Integer.parseInt(partes[1].trim());
                    String anioStr = partes[2].trim();
                    if (anioStr.length() == 4) {
                        anio = Integer.parseInt(anioStr);
                    } else if (anioStr.length() == 2) {
                        anio = 2000 + Integer.parseInt(anioStr);
                    }
                }
            }
            
            if (dia > 0 && dia <= 31 && mes > 0 && mes <= 12 && anio > 2000) {
                // Formato para ordenar: yyyy-MM-dd
                return String.format("%d-%02d-%02d", anio, mes, dia);
            }
        } catch (Exception e) {
            // Error parseando fecha
        }
        
        return "";
    }
    
    private static String formatearDiaLegible(String fechaOrden) {
        // Convertir yyyy-MM-dd a formato legible: "29 Dic"
        if (fechaOrden == null || fechaOrden.length() < 10) return fechaOrden;
        
        try {
            String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
            String[] partes = fechaOrden.split("-");
            if (partes.length >= 3) {
                int mes = Integer.parseInt(partes[1]) - 1;
                int dia = Integer.parseInt(partes[2]);
                if (mes >= 0 && mes < 12) {
                    return dia + " " + meses[mes];
                }
            }
        } catch (Exception e) {
            // Error
        }
        return fechaOrden;
    }

    private static String obtenerValorCelda(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════════════════════════

    private static Label crearLabel(String texto, double size, FontWeight weight, String color) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", weight, size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private static void animarEntrada(Node node) {
        node.setOpacity(0);
        node.setTranslateY(20);
        
        FadeTransition fade = new FadeTransition(Duration.millis(500), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        TranslateTransition translate = new TranslateTransition(Duration.millis(500), node);
        translate.setFromY(20);
        translate.setToY(0);
        translate.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.play();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CLASE DE DATOS
    // ════════════════════════════════════════════════════════════════════════════

    private static class DatosEstadisticas {
        int totalEquipos = 0;
        int equiposUnicos = 0;
        int totalApps = 0;
        int totalAppsInstalaciones = 0;
        String soPrincipal = "N/A";
        String ultimoScan = "N/A";
        
        Map<String, Integer> distribucionSO = new LinkedHashMap<>();
        Map<String, Integer> distribucionMarca = new LinkedHashMap<>();
        Map<String, Integer> distribucionTipo = new LinkedHashMap<>();
        Map<String, Integer> topApps = new LinkedHashMap<>();
        Map<String, Integer> topFabricantes = new LinkedHashMap<>();
        
        // Evolución por día (datos reales)
        Map<String, Integer> evolucionPorDia = new LinkedHashMap<>();
    }
    
    /**
     * Verifica si una fila está vacía (sin datos significativos)
     */
    private static boolean esFilaVacia(Row row) {
        if (row == null) return true;
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String valor = "";
                try {
                    if (cell.getCellType() == CellType.STRING) {
                        valor = cell.getStringCellValue();
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        valor = String.valueOf(cell.getNumericCellValue());
                    }
                } catch (Exception ignored) {}
                
                if (valor != null && !valor.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
