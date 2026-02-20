package inventario.fx.ui.panel;
import inventario.fx.model.TemaManager;
import inventario.fx.model.AdminManager;
import inventario.fx.icons.IconosSVG;
import inventario.fx.ui.dialog.DialogosFX;
import inventario.fx.ui.component.NotificacionesFX;
import inventario.fx.excel.TemplateBuilder;
import inventario.fx.excel.ExcelToPdfConverter;
import inventario.fx.util.AppLogger;
import inventario.fx.util.ScreenUtils;

import inventario.fx.util.ComponentesFX;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import inventario.fx.util.AnimacionesFX;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║          🎯 GESTIÓN DE REPORTES - INTERFAZ ULTRA MODERNA                  ║
 * ║                  Con selección única y diseño de cards                    ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class GestionReportesFX {

    // ═══════════════════════════════════════════════════════════════════════
    // 🎨 PALETA DE COLORES - USANDO TEMA MANAGER (SOPORTE LIGHT/DARK)
    // ═══════════════════════════════════════════════════════════════════════
    
    private static String COLOR_BG_DARK() { return TemaManager.getBgDark(); }
    private static String COLOR_BG() { return TemaManager.getBg(); }
    private static String COLOR_BG_LIGHT() { return TemaManager.getBgLight(); }
    private static String COLOR_SURFACE() { return TemaManager.getSurface(); }
    private static String COLOR_BORDER() { return TemaManager.getBorder(); }
    private static final String COLOR_PRIMARY = TemaManager.COLOR_PRIMARY;
    private static final String ACCENT = TemaManager.COLOR_PRIMARY;
    private static final String SUCCESS = TemaManager.COLOR_SUCCESS;
    private static final String DANGER = TemaManager.COLOR_DANGER;
    private static final String WARNING = TemaManager.COLOR_WARNING;
    private static String TEXT_DARK() { return TemaManager.getText(); }
    private static String TEXT_LIGHT() { return TemaManager.getTextSecondary(); }
    private static String BORDER() { return TemaManager.getBorder(); }
    private static String CARD_SELECTED() { return TemaManager.getSurfaceHover(); }

    // ═══════════════════════════════════════════════════════════════════════
    // 📦 ESTADO
    // ═══════════════════════════════════════════════════════════════════════
    
    private static Stage ventana;
    private static Stage ventanaParent;
    private static ObservableList<ReporteItem> reportes;
    private static ObjectProperty<ReporteItem> seleccionActual = new SimpleObjectProperty<>(null);
    private static ObservableList<ReporteItem> seleccionMultiple = FXCollections.observableArrayList();
    private static VBox listaCards;
    private static VBox panelDetalle;
    private static VBox contenidoDetalle; // Contenido scrolleable del panel detalle
    private static HBox headerDetalleFijo; // Header fijo con título y botones
    private static HBox footerDetalleFijo; // Footer fijo con botones de exportar
    private static ScrollPane scrollDetalle; // ScrollPane para el panel detalle
    private static ScrollPane scrollCards;
    private static StackPane contenedorPrincipal;
    private static StackPane contenedorIntegrado;
    
    // Labels de las stat cards para re-animar conteos
    private static Label lblStatTotal;
    private static Label lblStatPreventivos;
    private static Label lblStatCorrectivos;
    private static Label lblStatLogicos;
    private static int valorStatTotal, valorStatPreventivos, valorStatCorrectivos, valorStatLogicos;
    
    public static StackPane getContenedor() {
        return contenedorPrincipal != null ? contenedorPrincipal : contenedorIntegrado;
    }
    private static HBox barraAccionesMasivas;
    private static Label lblContadorSeleccion;
    private static StackPane checkboxSeleccionarTodos;
    private static boolean todosSeleccionados = false;
    private static String filtroTipoActual = "TODOS"; // Filtro de tipo activo
    private static TextField campoBusquedaGlobal; // Referencia al campo de búsqueda
    private static String proyectoActualFiltro = null; // Proyecto para filtrar reportes
    
    private static final String DATA_FILE = "reportes_mantenimiento.dat";

    /**
     * Re-anima los conteos numéricos de las stat cards desde 0.
     * Se llama cada vez que se vuelve a mostrar la pestaña de reportes.
     */
    public static void reanimarConteosTarjetas() {
        if (lblStatTotal != null && valorStatTotal > 0) {
            lblStatTotal.setText("0");
            ComponentesFX.animarConteoConDelay(lblStatTotal, 0, valorStatTotal, 800, 200);
        }
        if (lblStatPreventivos != null && valorStatPreventivos > 0) {
            lblStatPreventivos.setText("0");
            ComponentesFX.animarConteoConDelay(lblStatPreventivos, 0, valorStatPreventivos, 800, 300);
        }
        if (lblStatCorrectivos != null && valorStatCorrectivos > 0) {
            lblStatCorrectivos.setText("0");
            ComponentesFX.animarConteoConDelay(lblStatCorrectivos, 0, valorStatCorrectivos, 800, 400);
        }
        if (lblStatLogicos != null && valorStatLogicos > 0) {
            lblStatLogicos.setText("0");
            ComponentesFX.animarConteoConDelay(lblStatLogicos, 0, valorStatLogicos, 800, 500);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 📋 MODELO
    // ═══════════════════════════════════════════════════════════════════════
    
    public static class ReporteItem implements Serializable {
        private static final long serialVersionUID = 3L;
        
        private String id;
        private ReporteFormularioFX.DatosReporte datos;
        private String fechaCreacion;
        private String horaCreacion;
        
        public ReporteItem(ReporteFormularioFX.DatosReporte datos) {
            this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.datos = datos;
            this.fechaCreacion = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            this.horaCreacion = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        
        // Constructor para edición (conserva id y fechas)
        public ReporteItem(ReporteFormularioFX.DatosReporte datos, String id, String fechaCreacion, String horaCreacion) {
            this.id = id;
            this.datos = datos;
            this.fechaCreacion = fechaCreacion;
            this.horaCreacion = horaCreacion;
        }
        
        public String getId() { return id; }
        public ReporteFormularioFX.DatosReporte getDatos() { return datos; }
        public String getFechaCreacion() { return fechaCreacion; }
        public String getHoraCreacion() { return horaCreacion != null ? horaCreacion : ""; }
        
        public String getTicket() { return datos.ticket != null ? datos.ticket : "S/N"; }
        public String getNombre() { return datos.nombre != null ? datos.nombre : "-"; }
        public String getTecnico() { return datos.tecnico != null ? datos.tecnico : "-"; }
        public String getTipo() { return datos.tipoSolicitud != null ? datos.tipoSolicitud : "-"; }
        public String getDispositivo() { return datos.tipoDispositivo != null ? datos.tipoDispositivo : "-"; }
        public String getProyecto() { return datos.proyectoNombre != null ? datos.proyectoNombre : "-"; }
        public String getFecha() {
            if (datos.dia != null && datos.mes != null && datos.anio != null)
                return datos.dia + "/" + datos.mes + "/" + datos.anio;
            return fechaCreacion;
        }
        public String getFechaHora() {
            String fecha = getFecha();
            String hora = getHoraCreacion();
            return hora.isEmpty() ? fecha : fecha + " " + hora;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 🚀 ENTRADA - VENTANA MODAL (DEPRECATED - usar crearPanelIntegrado)
    // ═══════════════════════════════════════════════════════════════════════
    
    public static void mostrar(Stage parent) {
        cargarDatos();
        ventanaParent = parent;
        
        ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.initOwner(parent);
        ventana.setTitle("Centro de Reportes");
        ventana.setWidth(ScreenUtils.w(1400, 1100));
        ventana.setHeight(ScreenUtils.h(850, 700));
        ventana.setMinWidth(1100);
        ventana.setMinHeight(700);

        try {
            InputStream icon = GestionReportesFX.class.getResourceAsStream("/images/icon.png");
            if (icon != null) ventana.getIcons().add(new Image(icon));
        } catch (Exception ignored) {}

        // Contenedor con fondo oscuro
        contenedorPrincipal = new StackPane();
        
        // Fondo oscuro sólido
        Rectangle fondoOscuro = new Rectangle();
        fondoOscuro.widthProperty().bind(contenedorPrincipal.widthProperty());
        fondoOscuro.heightProperty().bind(contenedorPrincipal.heightProperty());
        fondoOscuro.setFill(Color.web(COLOR_BG_DARK()));
        
        // Círculos decorativos de fondo (más sutiles)
        StackPane circulos = crearCirculosDecorativos();
        
        // Layout principal
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(24));
        
        layout.setTop(crearHeaderModerno());
        layout.setCenter(crearContenidoDividido());
        
        contenedorPrincipal.getChildren().addAll(fondoOscuro, circulos, layout);
        
        Scene scene = new Scene(contenedorPrincipal);
        
    
        
        ventana.setScene(scene);
        
        // Animación entrada premium
        layout.setOpacity(0);
        layout.setScaleX(0.9);
        layout.setScaleY(0.9);
        layout.setTranslateY(15);
        
        ventana.show();
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(450), layout);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(500), layout);
        scaleIn.setFromX(0.9);
        scaleIn.setFromY(0.9);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setInterpolator(AnimacionesFX.EASE_OUT_BACK);
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), layout);
        slideIn.setToY(0);
        slideIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        new ParallelTransition(fadeIn, scaleIn, slideIn).play();
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // 🚀 PANEL INTEGRADO EN DASHBOARD
    // ═══════════════════════════════════════════════════════════════════════
    
    public static StackPane crearPanelIntegrado(Stage parent) {
        return crearPanelIntegrado(parent, null);
    }
    
    public static StackPane crearPanelIntegrado(Stage parent, String proyectoFiltro) {
        proyectoActualFiltro = proyectoFiltro; // Establecer filtro ANTES de cargar datos
        System.out.println("[GestionReportesFX] Panel integrado creado con filtro de proyecto: " + proyectoFiltro);
        
        // SIEMPRE recargar datos cuando cambia el proyecto para mantener independencia
        cargarDatos();
        
        ventanaParent = parent;
        seleccionActual.set(null);
        seleccionMultiple.clear();
        
        VBox contenedor = new VBox(0);
        contenedor.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");
        VBox.setVgrow(contenedor, Priority.ALWAYS);
        
        // Header integrado
        HBox header = crearHeaderIntegrado();
        
        // Dashboard de estadísticas rápidas
        HBox dashboardStats = crearDashboardStats();
        
        // Contenido principal
        HBox contenido = crearContenidoDividido();
        contenido.setPadding(new Insets(0, 32, 24, 32));
        VBox.setVgrow(contenido, Priority.ALWAYS);
        
        contenedor.getChildren().addAll(header, dashboardStats, contenido);
        
        // Barra flotante de acciones (overlay)
        barraAccionesMasivas = crearBarraFlotante();
        barraAccionesMasivas.setVisible(false);
        barraAccionesMasivas.setTranslateY(80); // Empezar fuera de vista
        
        contenedorIntegrado = new StackPane(contenedor, barraAccionesMasivas);
        contenedorIntegrado.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");
        StackPane.setAlignment(barraAccionesMasivas, Pos.BOTTOM_CENTER);
        StackPane.setMargin(barraAccionesMasivas, new Insets(0, 0, 30, 0));
        
        return contenedorIntegrado;
    }
    
    private static HBox crearBarraFlotante() {
        HBox barra = new HBox(16);
        barra.setAlignment(Pos.CENTER);
        barra.setPadding(new Insets(16, 30, 16, 30));
        barra.setMaxWidth(600);
        barra.setMaxHeight(68);
        barra.setStyle(TemaManager.getFloatingBarStyle() + 
            "-fx-background-radius: 16;" +
            "-fx-border-radius: 16;");
        
        // Sombra elegante y suave
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.4));
        sombra.setRadius(30);
        sombra.setSpread(0.0);
        sombra.setOffsetY(10);
        barra.setEffect(sombra);
        
        // Transición suave al mostrar/ocultar
        barra.setOpacity(0);
        barra.setScaleX(0.9);
        barra.setScaleY(0.9);
        
        // Contador con estilo mejorado
        Label lblContador = new Label("0");
        lblContador.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblContador.setTextFill(Color.web(ACCENT));
        lblContador.setMinWidth(30);
        lblContador.setAlignment(Pos.CENTER);
        lblContador.setStyle("-fx-background-color: " + ACCENT + "20; -fx-background-radius: 8; -fx-padding: 2 8;");
        
        Label lblTexto = new Label("seleccionados");
        lblTexto.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblTexto.setTextFill(TemaManager.getTextSecondaryColor());
        
        // Separador elegante
        Region sep1 = new Region();
        sep1.setPrefWidth(2);
        sep1.setPrefHeight(28);
        sep1.setStyle("-fx-background-color: linear-gradient(to bottom, transparent, " + TemaManager.getBorder() + ", transparent); -fx-background-radius: 1;");
        
        // Botones con iconos grandes y tooltips
        Button btnPDF = crearBotonIconoBarra(IconosSVG.pdfBlanco(24), COLOR_PRIMARY, "Exportar a PDF");
        btnPDF.setOnAction(e -> exportarSeleccionMasiva("PDF"));
        
        Button btnExcel = crearBotonIconoBarra(IconosSVG.excelBlanco(24), "#21A366", "Exportar a Excel");
        btnExcel.setOnAction(e -> exportarSeleccionMasiva("EXCEL"));
        
        Button btnEliminar = crearBotonIconoBarra(IconosSVG.eliminar("#FFFFFF", 22), "#4B5563", "Eliminar seleccionados");
        btnEliminar.setOnAction(e -> eliminarSeleccionMasiva());
        
        // Botón cerrar (X) estilizado - diseño unificado
        Button btnCerrar = inventario.fx.util.ComponentesFX.crearBotonCerrar(() -> limpiarSeleccionMultiple(), 36);
        btnCerrar.setTooltip(ComponentesFX.crearTooltip("Limpiar selección"));
        HBox.setMargin(btnCerrar, new Insets(0, 0, 0, 8));
        
        barra.getChildren().addAll(lblContador, lblTexto, sep1, btnPDF, btnExcel, btnEliminar, btnCerrar);
        
        // Actualizar contador
        seleccionMultiple.addListener((javafx.collections.ListChangeListener<ReporteItem>) c -> {
            lblContador.setText(String.valueOf(seleccionMultiple.size()));
        });
        
        return barra;
    }
    
    private static Button crearBotonIcono(Node icono, String color, String textoTooltip) {
        Button btn = new Button();
        btn.setGraphic(icono);
        btn.setPadding(new Insets(10));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        // Tooltip estilizado
        btn.setTooltip(ComponentesFX.crearTooltip(textoTooltip));
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: derive(" + color + ", -20%);" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.1, 100);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 100);
        });
        return btn;
    }
    
    private static Button crearBotonIconoBarra(Node icono, String color, String textoTooltip) {
        Button btn = new Button();
        btn.setGraphic(icono);
        btn.setPadding(new Insets(10, 18, 10, 18));
        btn.setMinWidth(52);
        btn.setMinHeight(44);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, " + color + "40, 8, 0, 0, 2);"
        );
        
        // Tooltip elegante
        btn.setTooltip(ComponentesFX.crearTooltip(textoTooltip));
        
        // Hover con transición suave
        btn.setOnMouseEntered(e -> {
            AnimacionesFX.hoverIn(btn, 1.1, 150);
            btn.setStyle(
                "-fx-background-color: derive(" + color + ", 15%);" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, " + color + "55, 14, 0, 0, 4);"
            );
        });
        btn.setOnMouseExited(e -> {
            AnimacionesFX.hoverOut(btn, 150);
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, " + color + "40, 8, 0, 0, 2);"
            );
        });
        return btn;
    }
    
    private static void toggleSeleccionarTodos() {
        // Obtener solo los reportes visibles (filtrados)
        java.util.List<ReporteItem> reportesFiltrados = obtenerReportesFiltrados();
        
        if (seleccionMultiple.containsAll(reportesFiltrados) && !reportesFiltrados.isEmpty()) {
            // Deseleccionar todos los filtrados
            seleccionMultiple.removeAll(reportesFiltrados);
            todosSeleccionados = false;
        } else {
            // Seleccionar todos los filtrados
            for (ReporteItem r : reportesFiltrados) {
                if (!seleccionMultiple.contains(r)) {
                    seleccionMultiple.add(r);
                }
            }
            todosSeleccionados = true;
        }
        actualizarEstadoCheckboxTodos();
        aplicarFiltros(); // Usar aplicarFiltros para mantener el filtro activo
        actualizarBarraAcciones();
    }
    
    // Obtener los reportes que pasan los filtros actuales
    private static java.util.List<ReporteItem> obtenerReportesFiltrados() {
        java.util.List<ReporteItem> filtrados = new java.util.ArrayList<>();
        String textoBusqueda = campoBusquedaGlobal != null ? campoBusquedaGlobal.getText().toLowerCase().trim() : "";
        
        for (ReporteItem r : reportes) {
            // IMPORTANTE: Filtrar por proyecto actual
            boolean pasaFiltroProyecto = proyectoActualFiltro == null || 
                                         r.getProyecto().equals(proyectoActualFiltro);
            
            boolean pasaFiltroTipo = filtroTipoActual.equals("TODOS") || 
                                     r.getTipo().toUpperCase().contains(filtroTipoActual);
            
            boolean pasaFiltroBusqueda = textoBusqueda.isEmpty() ||
                r.getTicket().toLowerCase().contains(textoBusqueda) ||
                r.getNombre().toLowerCase().contains(textoBusqueda) ||
                r.getTecnico().toLowerCase().contains(textoBusqueda) ||
                r.getTipo().toLowerCase().contains(textoBusqueda);
            
            // Solo agregar si pasa TODOS los filtros incluyendo el de proyecto
            if (pasaFiltroProyecto && pasaFiltroTipo && pasaFiltroBusqueda) {
                filtrados.add(r);
            }
        }
        return filtrados;
    }
    
    private static void actualizarEstadoCheckboxTodos() {
        if (checkboxSeleccionarTodos == null) return;
        
        Object[] refs = (Object[]) checkboxSeleccionarTodos.getUserData();
        if (refs == null) return;
        
        Rectangle fondo = (Rectangle) refs[0];
        Node checkIcon = (Node) refs[1];
        Rectangle minusIcon = (Rectangle) refs[2];
        
        // Obtener el total de reportes filtrados (no todos los reportes)
        java.util.List<ReporteItem> reportesFiltrados = obtenerReportesFiltrados();
        int seleccionados = seleccionMultiple.size();
        int total = reportesFiltrados.size();
        
        // IMPORTANTE: Resetear rotación del checkIcon para evitar bugs visuales
        checkIcon.setRotate(0);
        checkIcon.setScaleX(1);
        checkIcon.setScaleY(1);
        minusIcon.setScaleX(1);
        minusIcon.setScaleY(1);
        
        if (seleccionados == 0) {
            // Ninguno seleccionado
            fondo.setFill(Color.TRANSPARENT);
            checkIcon.setOpacity(0);
            minusIcon.setOpacity(0);
            todosSeleccionados = false;
        } else if (seleccionados == total && total > 0) {
            // Todos seleccionados
            fondo.setFill(Color.web(ACCENT));
            fondo.setStroke(Color.web(ACCENT));
            checkIcon.setOpacity(1);
            minusIcon.setOpacity(0);
            todosSeleccionados = true;
        } else {
            // Parcialmente seleccionado
            fondo.setFill(Color.web(ACCENT, 0.5));
            fondo.setStroke(Color.web(ACCENT));
            checkIcon.setOpacity(0);
            minusIcon.setOpacity(1);
            todosSeleccionados = false;
        }
    }
    
    private static void limpiarSeleccionMultiple() {
        seleccionMultiple.clear();
        todosSeleccionados = false;
        actualizarEstadoCheckboxTodos();
        actualizarBarraAcciones();
        actualizarListaCards();
    }
    
    private static HBox crearHeaderIntegrado() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 32, 16, 32));
        header.setSpacing(20);
        
        // Título con icono (estilo consistente con Dashboard y Estadísticas)
        VBox titleBox = new VBox(4);
        
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        // Icono de reportes
        Node iconoReportes = IconosSVG.reportes(ACCENT, 28);
        
        Label title = new Label("Reportes");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(TEXT_DARK()));
        
        titleRow.getChildren().addAll(iconoReportes, title);

        Label subtitle = new Label("Gestiona y exporta tus reportes de mantenimiento");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.web(TEXT_LIGHT()));

        titleBox.getChildren().addAll(titleRow, subtitle);
        
        // Animación de entrada del header usando AnimacionesFX
        AnimacionesFX.slideLeftFadeIn(titleBox, 450, 100);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Botón nuevo reporte mejorado con glow
        Button btnNuevo = crearBotonConIconoAnimado("Nuevo Reporte", IconosSVG.mas("#FFFFFF", 18), SUCCESS);
        btnNuevo.setOnAction(e -> crearNuevoReporte());
        
        // Animación del botón
        AnimacionesFX.slideUpFadeIn(btnNuevo, 350, 300);
        
        header.getChildren().addAll(titleBox, spacer, btnNuevo);
        return header;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // 📊 DASHBOARD DE ESTADÍSTICAS RÁPIDAS
    // ═══════════════════════════════════════════════════════════════════════
    
    private static HBox crearDashboardStats() {
        HBox dashboard = new HBox(18);
        dashboard.setAlignment(Pos.CENTER_LEFT);
        dashboard.setPadding(new Insets(0, 32, 20, 32));
        
        // Calcular estadísticas SOLO de reportes filtrados del proyecto actual
        java.util.List<ReporteItem> reportesFiltrados = obtenerReportesFiltrados();
        int totalReportes = reportesFiltrados.size();
        long preventivos = reportesFiltrados.stream().filter(r -> r.getTipo().toUpperCase().contains("PREVENTIVO")).count();
        long correctivos = reportesFiltrados.stream().filter(r -> r.getTipo().toUpperCase().contains("CORRECTIVO")).count();
        long logicos = reportesFiltrados.stream().filter(r -> r.getTipo().toUpperCase().contains("LÓGICO")).count();
        
        // Cards de estadísticas — cada card maneja su propia animación internamente
        StackPane cardTotal = crearStatCard(IconosSVG.documento(ACCENT, 24), "Total", String.valueOf(totalReportes), ACCENT, 0);
        StackPane cardPreventivos = crearStatCard(IconosSVG.check(SUCCESS, 24), "Preventivos", String.valueOf(preventivos), SUCCESS, 1);
        StackPane cardCorrectivos = crearStatCard(IconosSVG.herramienta(WARNING, 24), "Correctivos", String.valueOf(correctivos), WARNING, 2);
        StackPane cardLogicos = crearStatCard(IconosSVG.servidor(COLOR_PRIMARY, 24), "Lógicos", String.valueOf(logicos), COLOR_PRIMARY, 3);
        
        HBox.setHgrow(cardTotal, Priority.ALWAYS);
        HBox.setHgrow(cardPreventivos, Priority.ALWAYS);
        HBox.setHgrow(cardCorrectivos, Priority.ALWAYS);
        HBox.setHgrow(cardLogicos, Priority.ALWAYS);
        dashboard.getChildren().addAll(cardTotal, cardPreventivos, cardCorrectivos, cardLogicos);
        
        // Animación de entrada escalonada usando AnimacionesFX
        AnimacionesFX.entradaEscalonadaScale(
            java.util.Arrays.asList(cardTotal, cardPreventivos, cardCorrectivos, cardLogicos), 
            350, 80);
        
        // Actualizar estadísticas cuando cambie la lista
        reportes.addListener((javafx.collections.ListChangeListener<ReporteItem>) c -> {
            actualizarDashboardStats(dashboard);
        });
        
        return dashboard;
    }
    
    private static void actualizarDashboardStats(HBox dashboard) {
        // Calcular estadísticas SOLO de reportes filtrados del proyecto actual
        java.util.List<ReporteItem> reportesFiltrados = obtenerReportesFiltrados();
        int totalReportes = reportesFiltrados.size();
        long preventivos = reportesFiltrados.stream().filter(r -> r.getTipo().toUpperCase().contains("PREVENTIVO")).count();
        long correctivos = reportesFiltrados.stream().filter(r -> r.getTipo().toUpperCase().contains("CORRECTIVO")).count();
        long logicos = reportesFiltrados.stream().filter(r -> r.getTipo().toUpperCase().contains("LÓGICO")).count();
        
        // Actualizar valores de las cards (ahora son StackPane wrappers)
        if (dashboard.getChildren().size() >= 4) {
            actualizarStatCardValor((StackPane) dashboard.getChildren().get(0), String.valueOf(totalReportes));
            actualizarStatCardValor((StackPane) dashboard.getChildren().get(1), String.valueOf(preventivos));
            actualizarStatCardValor((StackPane) dashboard.getChildren().get(2), String.valueOf(correctivos));
            actualizarStatCardValor((StackPane) dashboard.getChildren().get(3), String.valueOf(logicos));
        }
    }
    
    private static void actualizarStatCardValor(StackPane wrapper, String nuevoValor) {
        // Obtener la HBox card del wrapper
        if (wrapper.getChildren().isEmpty()) return;
        Node cardNode = wrapper.getChildren().get(0);
        if (!(cardNode instanceof HBox)) return;
        HBox card = (HBox) cardNode;
        
        // Buscar el Label del valor dentro del VBox
        for (Node node : card.getChildren()) {
            if (node instanceof VBox) {
                VBox vbox = (VBox) node;
                for (Node child : vbox.getChildren()) {
                    if (child instanceof Label) {
                        Label lbl = (Label) child;
                        if (lbl.getFont().getSize() >= 20) {
                            int nuevoNum = 0;
                            try { nuevoNum = Integer.parseInt(nuevoValor); } catch (NumberFormatException e) {}
                            int valorActual = 0;
                            try { valorActual = Integer.parseInt(lbl.getText().replace(",", "")); } catch (NumberFormatException e) {}
                            if (valorActual != nuevoNum) {
                                animarConteoStatCard(lbl, valorActual, nuevoNum, 800);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
    
    private static StackPane crearStatCard(Node icono, String titulo, String valor, String color,
                                             int cardIndex) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 22, 16, 18));
        card.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;"
        );
        
        // Icono con fondo circular animado
        StackPane iconWrapper = new StackPane();
        iconWrapper.setPadding(new Insets(10));
        iconWrapper.setStyle(
            "-fx-background-color: " + color + "18;" +
            "-fx-background-radius: 12;"
        );
        iconWrapper.getChildren().add(icono);
        
        // Texto con layout mejorado
        VBox textoBox = new VBox(4);
        
        // Valor numérico - empieza en "0", se anima después
        Label lblValor = new Label("0");
        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblValor.setTextFill(Color.web(TEXT_DARK()));
        lblValor.setMinWidth(40);
        
        int valorNumerico = 0;
        try {
            valorNumerico = Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            lblValor.setText(valor);
        }
        
        // Guardar referencia al label y valor según el cardIndex
        switch (cardIndex) {
            case 0: lblStatTotal = lblValor; valorStatTotal = valorNumerico; break;
            case 1: lblStatPreventivos = lblValor; valorStatPreventivos = valorNumerico; break;
            case 2: lblStatCorrectivos = lblValor; valorStatCorrectivos = valorNumerico; break;
            case 3: lblStatLogicos = lblValor; valorStatLogicos = valorNumerico; break;
        }
        
        // Animación de conteo interna con delay sincronizado al fade-in
        // Fade: delay = 100 + cardIndex*80, duración = 400ms → visible en ~(500 + cardIndex*80)ms
        // +350ms extra para que el usuario vea el "0" antes de que empiece el conteo
        final int conteoDelay = 850 + cardIndex * 100;
        final int valorFinal = valorNumerico;
        if (valorFinal > 0) {
            ComponentesFX.animarConteoConDelay(lblValor, 0, valorFinal, 800, conteoDelay);
        }
        
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblTitulo.setTextFill(Color.web(TEXT_LIGHT()));
        
        textoBox.getChildren().addAll(lblValor, lblTitulo);
        
        card.getChildren().addAll(iconWrapper, textoBox);
        
        // Sombra sutil con color del icono
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web(color, 0.18));
        shadow.setRadius(12);
        shadow.setOffsetY(4);
        card.setEffect(shadow);
        
        // Hover effects con transiciones suaves que NO afectan el layout
        final ScaleTransition[] cardScaleAnim = new ScaleTransition[1];
        final ScaleTransition[] iconScaleAnim = new ScaleTransition[1];
        
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_SURFACE() + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + color + "55;" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1.5;" +
                "-fx-cursor: hand;"
            );
            shadow.setColor(Color.web(color, 0.35));
            shadow.setRadius(18);
            AnimacionesFX.hoverIn(card, 1.02, 150);
            AnimacionesFX.hoverIn(iconWrapper, 1.1, 150);
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_SURFACE() + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1;"
            );
            shadow.setColor(Color.web(color, 0.18));
            shadow.setRadius(12);
            AnimacionesFX.hoverOut(card, 150);
            AnimacionesFX.hoverOut(iconWrapper, 150);
        });
        
        // Envolver en un contenedor flexible para que el scale NO afecte el layout
        StackPane wrapper = new StackPane(card);
        wrapper.setMinWidth(150);
        wrapper.setPrefWidth(180);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setMinHeight(90);
        wrapper.setPrefHeight(Region.USE_COMPUTED_SIZE);
        
        // La animación de entrada se maneja en entradaEscalonadaScale
        
        return wrapper;
    }
    
    /** Anima un conteo numérico de inicio a fin en las tarjetas de estadísticas */
    private static void animarConteoStatCard(Label label, int desde, int hasta, int duracionMs) {
        ComponentesFX.animarConteo(label, desde, hasta, duracionMs);
    }
    
    private static Button crearBotonConIconoAnimado(String texto, Node icono, String color) {
        Button btn = new Button(texto);
        btn.setGraphic(icono);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setPadding(new Insets(12, 20, 12, 16));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        btn.setGraphicTextGap(8);
        
        // Glow del botón
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(color, 0.5));
        glow.setRadius(12);
        glow.setSpread(0.1);
        btn.setEffect(glow);
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: derive(" + color + ", 15%);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            glow.setRadius(18);
            glow.setColor(Color.web(color, 0.7));
            AnimacionesFX.hoverIn(btn, 1.05, 120);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            glow.setRadius(12);
            glow.setColor(Color.web(color, 0.5));
            AnimacionesFX.hoverOut(btn, 120);
        });
        
        return btn;
    }
    
    public static void crearNuevoReporteDesdeExterno() {
        if (ventanaParent != null) {
            crearNuevoReporte();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 🎨 ELEMENTOS DECORATIVOS
    // ═══════════════════════════════════════════════════════════════════════
    
    private static StackPane crearCirculosDecorativos() {
        StackPane pane = new StackPane();
        
        Circle c1 = new Circle(200, Color.web(COLOR_PRIMARY, 0.03));
        c1.setTranslateX(-400);
        c1.setTranslateY(-200);
        
        Circle c2 = new Circle(300, Color.web(COLOR_PRIMARY, 0.02));
        c2.setTranslateX(500);
        c2.setTranslateY(300);
        
        Circle c3 = new Circle(150, Color.web(COLOR_PRIMARY, 0.04));
        c3.setTranslateX(200);
        c3.setTranslateY(-350);
        
        // Animación suave
        animarCirculo(c1, -10, 10, 4000);
        animarCirculo(c2, 15, -15, 5000);
        animarCirculo(c3, -8, 8, 3500);
        
        pane.getChildren().addAll(c1, c2, c3);
        pane.setMouseTransparent(true);
        return pane;
    }
    
    private static void animarCirculo(Circle c, double dx, double dy, int duracion) {
        TranslateTransition t = new TranslateTransition(Duration.millis(duracion), c);
        t.setByX(dx);
        t.setByY(dy);
        t.setCycleCount(Animation.INDEFINITE);
        t.setAutoReverse(true);
        t.setInterpolator(Interpolator.EASE_BOTH);
        t.play();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 🎯 HEADER GLASSMORPHISM
    // ═══════════════════════════════════════════════════════════════════════
    
    private static HBox crearHeaderModerno() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 28, 20, 28));
        header.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;"
        );
        
        // Logo animado con icono SVG
        StackPane logo = new StackPane();
        Circle logoBg = new Circle(28, Color.web(COLOR_PRIMARY));
        Node logoIcon = IconosSVG.reportes("#FFFFFF", 32);
        logo.getChildren().addAll(logoBg, logoIcon);
        
        DropShadow logoShadow = new DropShadow(15, Color.web(COLOR_PRIMARY, 0.3));
        logo.setEffect(logoShadow);
        
        // Título
        VBox titulo = new VBox(2);
        Label lblTitulo = new Label("Centro de Reportes");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitulo.setTextFill(Color.web(TEXT_DARK()));
        
        Label lblSubtitulo = new Label("Selecciona un reporte para ver detalles y exportar");
        lblSubtitulo.setFont(Font.font("Segoe UI", 13));
        lblSubtitulo.setTextFill(Color.web(TEXT_LIGHT()));
        
        titulo.getChildren().addAll(lblTitulo, lblSubtitulo);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Botón nuevo con icono
        Button btnNuevo = crearBotonConIcono("Nuevo Reporte", IconosSVG.mas(SUCCESS, 18), SUCCESS);
        btnNuevo.setOnAction(e -> crearNuevoReporte());
        
        // Botón cerrar con estilo unificado
        Button btnCerrar = inventario.fx.util.ComponentesFX.crearBotonCerrar(ventana::close, 44);

        header.getChildren().addAll(logo, titulo, spacer, btnNuevo, btnCerrar);
        
        VBox.setMargin(header, new Insets(0, 0, 20, 0));
        return header;
    }
    
    private static Button crearBotonGlass(String texto, String color) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btn.setPadding(new Insets(14, 28, 14, 28));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 14;" +
            "-fx-cursor: hand;"
        );
        
        DropShadow sombra = new DropShadow(12, Color.web(color, 0.5));
        btn.setEffect(sombra);
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: derive(" + color + ", 15%);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;"
            );
            sombra.setRadius(18);
            AnimacionesFX.hoverIn(btn, 1.05, 120);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;"
            );
            sombra.setRadius(12);
            AnimacionesFX.hoverOut(btn, 120);
        });
        
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 📄 CONTENIDO DIVIDIDO (LISTA + DETALLE)
    // ═══════════════════════════════════════════════════════════════════════
    
    private static HBox crearContenidoDividido() {
        HBox contenido = new HBox(24);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        HBox.setHgrow(contenido, Priority.ALWAYS);
        
        // Panel izquierdo - Lista de reportes (ancho fijo)
        VBox panelLista = crearPanelLista();
        panelLista.setPrefWidth(480);
        panelLista.setMinWidth(400);
        panelLista.setMaxWidth(520); // Limitar para evitar crecimiento excesivo
        
        // Panel derecho - Detalle del reporte seleccionado
        panelDetalle = crearPanelDetalle();
        HBox.setHgrow(panelDetalle, Priority.ALWAYS);
        panelDetalle.setMinWidth(500); // Ancho mínimo para evitar colapso
        
        contenido.getChildren().addAll(panelLista, panelDetalle);
        return contenido;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 📋 PANEL LISTA DE REPORTES - DISEÑO MEJORADO
    // ═══════════════════════════════════════════════════════════════════════
    
    private static VBox crearPanelLista() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(22));
        panel.setStyle(TemaManager.getPanelStyle());
        
        // Sombra elegante con profundidad
        DropShadow panelShadow = new DropShadow();
        panelShadow.setColor(Color.web("#000000", TemaManager.isDarkMode() ? 0.5 : 0.15));
        panelShadow.setRadius(28);
        panelShadow.setOffsetY(10);
        panelShadow.setSpread(0.02);
        panel.setEffect(panelShadow);
        
        // ═══════════════════════════════════════════════════════════════════
        // HEADER DEL PANEL - Título + Badge contador
        // ═══════════════════════════════════════════════════════════════════
        HBox headerLista = new HBox(12);
        headerLista.setAlignment(Pos.CENTER_LEFT);
        headerLista.setPadding(new Insets(0, 0, 4, 0));
        
        // Icono con fondo circular animado
        StackPane iconWrapper = new StackPane();
        iconWrapper.setPadding(new Insets(10));
        iconWrapper.setStyle(
            "-fx-background-color: " + ACCENT + "20;" +
            "-fx-background-radius: 12;"
        );
        Node iconLista = IconosSVG.lista(ACCENT, 22);
        iconWrapper.getChildren().add(iconLista);
        
        // Glow sutil
        DropShadow iconGlow = new DropShadow();
        iconGlow.setColor(Color.web(ACCENT, 0.3));
        iconGlow.setRadius(8);
        iconWrapper.setEffect(iconGlow);
        
        Label lblLista = new Label("Mis Reportes");
        lblLista.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblLista.setTextFill(Color.web(TEXT_DARK()));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Badge contador con animación de pulso
        HBox badgeContador = new HBox();
        badgeContador.setAlignment(Pos.CENTER);
        badgeContador.setPadding(new Insets(6, 14, 6, 14));
        badgeContador.setStyle(
            "-fx-background-color: " + ACCENT + ";" +
            "-fx-background-radius: 16;"
        );
        
        // Sombra del badge
        DropShadow badgeShadow = new DropShadow();
        badgeShadow.setColor(Color.web(ACCENT, 0.4));
        badgeShadow.setRadius(8);
        badgeShadow.setSpread(0.1);
        badgeContador.setEffect(badgeShadow);
        
        Label lblContador = new Label();
        lblContador.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblContador.setTextFill(Color.WHITE);
        lblContador.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(
            () -> reportes.size() + " reporte" + (reportes.size() != 1 ? "s" : ""),
            reportes
        ));
        badgeContador.getChildren().add(lblContador);
        
        headerLista.getChildren().addAll(iconWrapper, lblLista, spacer, badgeContador);
        
        // ═══════════════════════════════════════════════════════════════════
        // CAMPO DE BÚSQUEDA MEJORADO CON FILTROS
        // ═══════════════════════════════════════════════════════════════════
        VBox seccionBusqueda = new VBox(10);
        
        // Campo de búsqueda con estilo moderno
        HBox buscarBox = new HBox(10);
        buscarBox.setAlignment(Pos.CENTER_LEFT);
        buscarBox.setPadding(new Insets(10, 14, 10, 14));
        buscarBox.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 10;"
        );
        
        Node iconoBuscar = IconosSVG.buscar(TEXT_LIGHT(), 16);
        
        campoBusquedaGlobal = new TextField();
        campoBusquedaGlobal.setPromptText("Buscar reporte...");
        campoBusquedaGlobal.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXT_DARK() + ";" +
            "-fx-prompt-text-fill: " + TEXT_LIGHT() + ";" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: transparent;" +
            "-fx-border-width: 0;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 0;"
        );
        HBox.setHgrow(campoBusquedaGlobal, Priority.ALWAYS);
        
        // Botón limpiar búsqueda
        Button btnLimpiar = new Button("✕");
        btnLimpiar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXT_LIGHT() + ";" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 2 6;"
        );
        btnLimpiar.setVisible(false);
        btnLimpiar.setOnAction(e -> {
            campoBusquedaGlobal.clear();
            campoBusquedaGlobal.requestFocus();
        });
        
        // Focus effect
        campoBusquedaGlobal.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                buscarBox.setStyle(
                    "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: " + ACCENT + ";" +
                    "-fx-border-radius: 10;" +
                    "-fx-border-width: 2;"
                );
            } else {
                buscarBox.setStyle(
                    "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: " + COLOR_BORDER() + ";" +
                    "-fx-border-radius: 10;"
                );
            }
        });
        
        // Mostrar/ocultar botón limpiar
        campoBusquedaGlobal.textProperty().addListener((o, old, nuevo) -> {
            btnLimpiar.setVisible(!nuevo.isEmpty());
            aplicarFiltros();
        });
        
        buscarBox.getChildren().addAll(iconoBuscar, campoBusquedaGlobal, btnLimpiar);
        
        // ═══════════════════════════════════════════════════════════════════
        // FILTROS POR TIPO
        // ═══════════════════════════════════════════════════════════════════
        HBox filtrosBox = new HBox(8);
        filtrosBox.setAlignment(Pos.CENTER_LEFT);
        
        // Chips de filtro
        ToggleGroup grupoFiltros = new ToggleGroup();
        
        ToggleButton filtroTodos = crearChipFiltro("Todos", "TODOS", grupoFiltros, true);
        ToggleButton filtroPreventivo = crearChipFiltro("Preventivo", "PREVENTIVO", grupoFiltros, false);
        ToggleButton filtroCorrectivo = crearChipFiltro("Correctivo", "CORRECTIVO", grupoFiltros, false);
        ToggleButton filtroLogico = crearChipFiltro("Lógico", "LÓGICO", grupoFiltros, false);
        
        filtrosBox.getChildren().addAll(filtroTodos, filtroPreventivo, filtroCorrectivo, filtroLogico);
        
        seccionBusqueda.getChildren().addAll(buscarBox, filtrosBox);
        
        // Lista de cards
        listaCards = new VBox(12);
        listaCards.setPadding(new Insets(6, 6, 6, 6)); // Espacio para sombras y bordes hover
        
        scrollCards = new ScrollPane(listaCards);
        scrollCards.setFitToWidth(true);
        scrollCards.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;" +
            "-fx-control-inner-background: transparent;"
        );
        scrollCards.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollCards, Priority.ALWAYS);
        
        // Barra de seleccionar todos
        HBox barraSeleccion = crearBarraSeleccionarTodos();
        
        // Animación de entrada del panel
        panel.setOpacity(0);
        panel.setTranslateY(15);
        
        FadeTransition fadePanel = new FadeTransition(Duration.millis(350), panel);
        fadePanel.setFromValue(0);
        fadePanel.setToValue(1);
        fadePanel.setDelay(Duration.millis(150));
        fadePanel.play();
        
        TranslateTransition slidePanel = new TranslateTransition(Duration.millis(350), panel);
        slidePanel.setFromY(15);
        slidePanel.setToY(0);
        slidePanel.setDelay(Duration.millis(150));
        slidePanel.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        slidePanel.play();
        
        actualizarListaCards();
        
        panel.getChildren().addAll(headerLista, seccionBusqueda, barraSeleccion, scrollCards);
        return panel;
    }
    
    // Crear chip de filtro con estilo toggle
    private static ToggleButton crearChipFiltro(String texto, String valor, ToggleGroup grupo, boolean seleccionado) {
        ToggleButton chip = new ToggleButton(texto);
        chip.setToggleGroup(grupo);
        chip.setSelected(seleccionado);
        chip.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        chip.setPadding(new Insets(5, 12, 5, 12));
        chip.setCursor(javafx.scene.Cursor.HAND);
        
        // Color según tipo
        String colorChip = valor.equals("TODOS") ? ACCENT :
                          valor.equals("PREVENTIVO") ? SUCCESS :
                          valor.equals("CORRECTIVO") ? DANGER : COLOR_PRIMARY;
        
        actualizarEstiloChip(chip, seleccionado, colorChip);
        
        chip.selectedProperty().addListener((obs, old, selected) -> {
            actualizarEstiloChip(chip, selected, colorChip);
            if (selected) {
                filtroTipoActual = valor;
                aplicarFiltros();
            }
        });
        
        // Hover effect
        chip.setOnMouseEntered(e -> {
            if (!chip.isSelected()) {
                chip.setStyle(
                    "-fx-background-color: " + colorChip + "20;" +
                    "-fx-background-radius: 15;" +
                    "-fx-border-color: " + colorChip + ";" +
                    "-fx-border-radius: 15;" +
                    "-fx-text-fill: " + colorChip + ";"
                );
            }
            AnimacionesFX.hoverIn(chip, 1.05, 120);
        });
        chip.setOnMouseExited(e -> {
            if (!chip.isSelected()) {
                actualizarEstiloChip(chip, false, colorChip);
            }
            AnimacionesFX.hoverOut(chip, 120);
        });
        
        return chip;
    }
    
    private static void actualizarEstiloChip(ToggleButton chip, boolean seleccionado, String color) {
        if (seleccionado) {
            chip.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 15;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-radius: 15;" +
                "-fx-text-fill: white;"
            );
        } else {
            chip.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 15;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 15;" +
                "-fx-text-fill: " + TEXT_LIGHT() + ";"
            );
        }
    }
    
    private static void aplicarFiltros() {
        String textoBusqueda = campoBusquedaGlobal != null ? campoBusquedaGlobal.getText().toLowerCase().trim() : "";
        
        listaCards.getChildren().clear();
        
        if (reportes.isEmpty()) {
            listaCards.getChildren().add(crearPlaceholderVacio());
            return;
        }
        
        int contador = 0;
        for (ReporteItem r : reportes) {
            // Filtro por tipo
            boolean pasaFiltroTipo = filtroTipoActual.equals("TODOS") || 
                                     r.getTipo().toUpperCase().contains(filtroTipoActual);
            
            // Filtro por texto
            boolean pasaFiltroBusqueda = textoBusqueda.isEmpty() ||
                r.getTicket().toLowerCase().contains(textoBusqueda) ||
                r.getNombre().toLowerCase().contains(textoBusqueda) ||
                r.getTecnico().toLowerCase().contains(textoBusqueda) ||
                r.getTipo().toLowerCase().contains(textoBusqueda);
            
            if (pasaFiltroTipo && pasaFiltroBusqueda) {
                listaCards.getChildren().add(crearCardReporte(r));
                contador++;
            }
        }
        
        if (contador == 0) {
            VBox noResultados = new VBox(12);
            noResultados.setAlignment(Pos.CENTER);
            noResultados.setPadding(new Insets(40));
            
            Node icono = IconosSVG.buscar(TEXT_LIGHT(), 36);
            
            String mensaje = textoBusqueda.isEmpty() ? 
                "No hay reportes de tipo " + filtroTipoActual.toLowerCase() :
                "Sin resultados para \"" + textoBusqueda + "\"";
            
            Label lbl = new Label(mensaje);
            lbl.setFont(Font.font("Segoe UI", 13));
            lbl.setTextFill(Color.web(TEXT_LIGHT()));
            
            noResultados.getChildren().addAll(icono, lbl);
            listaCards.getChildren().add(noResultados);
        }
    }
    
    private static HBox crearBarraSeleccionarTodos() {
        HBox barra = new HBox(12);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(10, 12, 10, 12));
        barra.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 8;"
        );
        
        // Checkbox grande de seleccionar todos
        checkboxSeleccionarTodos = crearCheckboxGrande();
        
        Label lblSeleccionar = new Label("Seleccionar todos");
        lblSeleccionar.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblSeleccionar.setTextFill(Color.web(TEXT_DARK()));
        lblSeleccionar.setCursor(javafx.scene.Cursor.HAND);
        lblSeleccionar.setOnMouseClicked(e -> toggleSeleccionarTodos());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Contador de seleccionados
        lblContadorSeleccion = new Label("");
        lblContadorSeleccion.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblContadorSeleccion.setTextFill(Color.web(ACCENT));
        
        // Actualizar texto del contador
        seleccionMultiple.addListener((javafx.collections.ListChangeListener<ReporteItem>) c -> {
            int count = seleccionMultiple.size();
            if (count == 0) {
                lblContadorSeleccion.setText("");
            } else {
                lblContadorSeleccion.setText(count + " seleccionado" + (count != 1 ? "s" : ""));
            }
            actualizarEstadoCheckboxTodos();
        });
        
        barra.getChildren().addAll(checkboxSeleccionarTodos, lblSeleccionar, spacer, lblContadorSeleccion);
        return barra;
    }
    
    private static StackPane crearCheckboxGrande() {
        StackPane container = new StackPane();
        container.setPrefSize(32, 32);
        container.setCursor(javafx.scene.Cursor.HAND);
        
        // Fondo del checkbox con glassmorphism
        Rectangle fondo = new Rectangle(24, 24);
        fondo.setArcWidth(8);
        fondo.setArcHeight(8);
        fondo.setFill(Color.web(COLOR_SURFACE(), 0.6));
        fondo.setStroke(Color.web(ACCENT, 0.3));
        fondo.setStrokeWidth(2.5);
        
        // Efecto de resplandor suave
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(ACCENT, 0.3));
        glow.setRadius(8);
        glow.setSpread(0.3);
        fondo.setEffect(glow);
        
        // Icono de check
        Node checkIcon = IconosSVG.check("#FFFFFF", 20);
        checkIcon.setOpacity(0);
        checkIcon.setScaleX(0.5);
        checkIcon.setScaleY(0.5);
        
        // Icono de menos (parcial)
        Rectangle minusIcon = new Rectangle(12, 3);
        minusIcon.setArcWidth(2);
        minusIcon.setArcHeight(2);
        minusIcon.setFill(Color.WHITE);
        minusIcon.setOpacity(0);
        minusIcon.setScaleX(0.5);
        minusIcon.setScaleY(0.5);
        
        container.getChildren().addAll(fondo, minusIcon, checkIcon);
        container.setUserData(new Object[]{fondo, checkIcon, minusIcon});
        
        container.setOnMouseClicked(e -> {
            e.consume();
            toggleSeleccionarTodos();
        });
        
        // Hover effect con animaciones fluidas
        container.setOnMouseEntered(e -> {
            AnimacionesFX.hoverIn(container, 1.1, 200);
            
            java.util.List<ReporteItem> filtrados = obtenerReportesFiltrados();
            if (seleccionMultiple.size() != filtrados.size()) {
                Timeline strokeAnim = new Timeline(
                    new KeyFrame(Duration.millis(200),
                        new KeyValue(fondo.strokeProperty(), Color.web(ACCENT, 1.0), AnimacionesFX.EASE_OUT_CUBIC)
                    )
                );
                strokeAnim.play();
            }
        });
        container.setOnMouseExited(e -> {
            AnimacionesFX.hoverOut(container, 200);
            
            java.util.List<ReporteItem> filtrados = obtenerReportesFiltrados();
            Color targetColor;
            if (seleccionMultiple.size() == 0) {
                targetColor = Color.web(ACCENT, 0.3);
            } else if (seleccionMultiple.containsAll(filtrados) && !filtrados.isEmpty()) {
                targetColor = Color.web(ACCENT, 1.0);
            } else {
                targetColor = Color.web(ACCENT, 0.7);
            }
            Timeline strokeAnim = new Timeline(
                new KeyFrame(Duration.millis(200),
                    new KeyValue(fondo.strokeProperty(), targetColor, AnimacionesFX.EASE_IN_CUBIC)
                )
            );
            strokeAnim.play();
        });
        
        return container;
    }
    
    private static void actualizarListaCards() {
        listaCards.getChildren().clear();
        
        System.out.println("[GestionReportesFX] Actualizando lista. Total reportes: " + reportes.size());
        System.out.println("[GestionReportesFX] Filtro de proyecto activo: " + proyectoActualFiltro);
        
        // Filtrar reportes por proyecto si hay filtro activo
        List<ReporteItem> reportesFiltrados = proyectoActualFiltro != null 
            ? reportes.stream()
                .filter(r -> {
                    String proyectoReporte = r.getProyecto();
                    boolean coincide = proyectoActualFiltro.equals(proyectoReporte);
                    System.out.println("[GestionReportesFX] Reporte ticket=" + r.getTicket() + 
                                     ", proyecto='" + proyectoReporte + 
                                     "', coincide con filtro='" + proyectoActualFiltro + "': " + coincide);
                    return coincide;
                })
                .collect(java.util.stream.Collectors.toList())
            : new ArrayList<>(reportes);
        
        System.out.println("[GestionReportesFX] Reportes filtrados: " + reportesFiltrados.size());
        
        if (reportesFiltrados.isEmpty()) {
            listaCards.getChildren().add(crearPlaceholderVacio());
        } else {
            int delay = 0;
            for (ReporteItem reporte : reportesFiltrados) {
                VBox card = crearCardReporte(reporte);
                
                // Animación de entrada escalonada suave
                card.setOpacity(0);
                card.setTranslateY(30);
                card.setScaleX(0.95);
                card.setScaleY(0.95);
                
                FadeTransition fadeCard = new FadeTransition(Duration.millis(350), card);
                fadeCard.setFromValue(0);
                fadeCard.setToValue(1);
                fadeCard.setDelay(Duration.millis(delay));
                fadeCard.play();
                
                TranslateTransition slideCard = new TranslateTransition(Duration.millis(400), card);
                slideCard.setFromY(30);
                slideCard.setToY(0);
                slideCard.setDelay(Duration.millis(delay));
                slideCard.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                slideCard.play();
                
                ScaleTransition scaleCard = new ScaleTransition(Duration.millis(400), card);
                scaleCard.setFromX(0.95);
                scaleCard.setFromY(0.95);
                scaleCard.setToX(1);
                scaleCard.setToY(1);
                scaleCard.setDelay(Duration.millis(delay));
                scaleCard.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                scaleCard.play();
                
                listaCards.getChildren().add(card);
                delay += 40; // Escalonado rápido
            }
        }
    }
    
    private static VBox crearCardReporte(ReporteItem reporte) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(18, 20, 18, 20));
        
        // Estado inicial
        boolean inicialSeleccionado = seleccionMultiple.contains(reporte) || seleccionActual.get() == reporte;
        aplicarEstiloCard(card, inicialSeleccionado, false);
        
        // ═══════════════════════════════════════════════════════════════════
        // HEADER - Checkbox + Badge tipo + Fecha/Hora
        // ═══════════════════════════════════════════════════════════════════
        HBox headerCard = new HBox(12);
        headerCard.setAlignment(Pos.CENTER_LEFT);
        
        // Checkbox personalizado con diseño moderno
        StackPane checkboxContainer = crearCheckboxModerno(reporte);
        CheckBox checkBox = (CheckBox) checkboxContainer.getUserData();
        
        // Badge tipo con color y efecto glow según tipo
        String badgeColor = reporte.getTipo().toUpperCase().contains("CORRECTIVO") ? WARNING : 
                           reporte.getTipo().toUpperCase().contains("LÓGICO") ? COLOR_PRIMARY : SUCCESS;
        
        // Texto del badge más corto y claro
        String tipoCorto = reporte.getTipo().toUpperCase()
            .replace("MANTENIMIENTO ", "")
            .replace("INSTALACIÓN", "INSTALACIÓN")
            .replace("DIAGNÓSTICO", "DIAGNÓSTICO");
        
        Label badgeTipo = new Label(tipoCorto);
        badgeTipo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badgeTipo.setTextFill(Color.WHITE);
        badgeTipo.setPadding(new Insets(5, 12, 5, 12));
        badgeTipo.setStyle(
            "-fx-background-color: " + badgeColor + ";" +
            "-fx-background-radius: 14;"
        );
        
        // Glow sutil en el badge
        DropShadow badgeGlow = new DropShadow();
        badgeGlow.setColor(Color.web(badgeColor, 0.4));
        badgeGlow.setRadius(6);
        badgeGlow.setSpread(0.1);
        badgeTipo.setEffect(badgeGlow);
        
        Region spacerCard = new Region();
        HBox.setHgrow(spacerCard, Priority.ALWAYS);
        
        // Fecha y hora - diseño más elegante con fondo
        VBox fechaHoraBox = new VBox(3);
        fechaHoraBox.setAlignment(Pos.CENTER_RIGHT);
        fechaHoraBox.setPadding(new Insets(4, 8, 4, 8));
        fechaHoraBox.setStyle(
            "-fx-background-color: " + COLOR_BG_DARK() + "40;" +
            "-fx-background-radius: 8;"
        );
        
        HBox fechaBox = new HBox(5);
        fechaBox.setAlignment(Pos.CENTER_RIGHT);
        Node iconoCalendario = IconosSVG.calendario(ACCENT, 13);
        fechaBox.getChildren().addAll(iconoCalendario, crearLabelMini(reporte.getFecha()));
        
        if (!reporte.getHoraCreacion().isEmpty()) {
            HBox horaBox = new HBox(5);
            horaBox.setAlignment(Pos.CENTER_RIGHT);
            Node iconoReloj = IconosSVG.reloj(TEXT_LIGHT(), 12);
            horaBox.getChildren().addAll(iconoReloj, crearLabelMini(reporte.getHoraCreacion()));
            fechaHoraBox.getChildren().addAll(fechaBox, horaBox);
        } else {
            fechaHoraBox.getChildren().add(fechaBox);
        }
        
        headerCard.getChildren().addAll(checkboxContainer, badgeTipo, spacerCard, fechaHoraBox);
        
        // ═══════════════════════════════════════════════════════════════════
        // CONTENIDO PRINCIPAL - Ticket prominente
        // ═══════════════════════════════════════════════════════════════════
        Label lblTicket = new Label("#" + reporte.getTicket());
        lblTicket.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblTicket.setTextFill(Color.web(TEXT_DARK()));
        
        // ═══════════════════════════════════════════════════════════════════
        // INFO DEL REPORTE - Con iconos y mejor layout
        // ═══════════════════════════════════════════════════════════════════
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(4, 0, 0, 0));
        
        // Fila de usuario
        HBox nombreBox = new HBox(10);
        nombreBox.setAlignment(Pos.CENTER_LEFT);
        StackPane iconoUsuarioWrapper = new StackPane();
        iconoUsuarioWrapper.setPadding(new Insets(4));
        iconoUsuarioWrapper.setStyle("-fx-background-color: " + ACCENT + "15; -fx-background-radius: 6;");
        iconoUsuarioWrapper.getChildren().add(IconosSVG.usuario(ACCENT, 14));
        nombreBox.getChildren().addAll(iconoUsuarioWrapper, crearLabelInfo(reporte.getNombre(), TEXT_DARK()));
        
        // Fila de dispositivo
        HBox dispositivoBox = new HBox(10);
        dispositivoBox.setAlignment(Pos.CENTER_LEFT);
        StackPane iconoDispWrapper = new StackPane();
        iconoDispWrapper.setPadding(new Insets(4));
        iconoDispWrapper.setStyle("-fx-background-color: " + TEXT_LIGHT() + "20; -fx-background-radius: 6;");
        iconoDispWrapper.getChildren().add(IconosSVG.computadora(TEXT_LIGHT(), 14));
        dispositivoBox.getChildren().addAll(iconoDispWrapper, crearLabelInfo(reporte.getDispositivo(), TEXT_LIGHT()));
        
        infoBox.getChildren().addAll(nombreBox, dispositivoBox);
        
        card.getChildren().addAll(headerCard, lblTicket, infoBox);
        
        // Clic en checkbox = selección múltiple con animación
        checkBox.setOnAction(e -> {
            boolean seleccionado = checkBox.isSelected();
            
            // Animación del checkbox
            ScaleTransition checkPulse = new ScaleTransition(Duration.millis(100), checkboxContainer);
            checkPulse.setToX(1.2);
            checkPulse.setToY(1.2);
            checkPulse.setAutoReverse(true);
            checkPulse.setCycleCount(2);
            checkPulse.play();
            
            if (seleccionado) {
                if (!seleccionMultiple.contains(reporte)) {
                    seleccionMultiple.add(reporte);
                }
            } else {
                seleccionMultiple.remove(reporte);
            }
            
            actualizarCheckboxModerno(checkboxContainer, seleccionado);
            boolean esSeleccionadoCard = seleccionado || seleccionActual.get() == reporte;
            aplicarEstiloCard(card, esSeleccionadoCard, false);
            actualizarBarraAcciones();
        });
        
        // Evitar que el clic en checkbox propague al card
        checkboxContainer.setOnMouseClicked(e -> e.consume());
        
        // Listener de selección simple (ver detalles)
        seleccionActual.addListener((o, old, nuevo) -> {
            boolean esSeleccionadoMultiple = seleccionMultiple.contains(reporte);
            boolean esSeleccionadoSimple = nuevo == reporte;
            boolean esSeleccionado = esSeleccionadoMultiple || esSeleccionadoSimple;
            aplicarEstiloCard(card, esSeleccionado, false);
        });
        
        // Actualizar estilo cuando cambie selección múltiple
        seleccionMultiple.addListener((javafx.collections.ListChangeListener<ReporteItem>) c -> {
            boolean esSeleccionadoMultiple = seleccionMultiple.contains(reporte);
            boolean esSeleccionadoSimple = seleccionActual.get() == reporte;
            boolean esSeleccionado = esSeleccionadoMultiple || esSeleccionadoSimple;
            checkBox.setSelected(esSeleccionadoMultiple);
            actualizarCheckboxModerno(checkboxContainer, esSeleccionadoMultiple);
            aplicarEstiloCard(card, esSeleccionado, false);
        });
        
        // ========== MENÚ CONTEXTUAL PARA EDITAR/ELIMINAR ==========
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 1;" +
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;"
        );
        
        MenuItem editarItem = new MenuItem("Editar reporte");
        editarItem.setGraphic(IconosSVG.editar(SUCCESS, 14));
        editarItem.setStyle("-fx-text-fill: " + TEXT_DARK() + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px;");
        editarItem.setOnAction(e -> editarReporte(reporte));
        
        MenuItem eliminarItem = new MenuItem("Eliminar reporte");
        eliminarItem.setGraphic(IconosSVG.eliminar(DANGER, 14));
        eliminarItem.setStyle("-fx-text-fill: " + TEXT_DARK() + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px;");
        eliminarItem.setOnAction(e -> eliminarReporte(reporte));
        
        contextMenu.getItems().addAll(editarItem, eliminarItem);
        
        card.setOnContextMenuRequested(e -> {
            contextMenu.show(card, e.getScreenX(), e.getScreenY());
        });
        
        // Clic en card = ver detalles (selección simple) con animación
        card.setOnMouseClicked(e -> {
            // Animación de selección - pulso elegante
            ScaleTransition pulseDown = new ScaleTransition(Duration.millis(80), card);
            pulseDown.setToX(0.97);
            pulseDown.setToY(0.97);
            pulseDown.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            
            ScaleTransition pulseUp = new ScaleTransition(Duration.millis(150), card);
            pulseUp.setToX(1.0);
            pulseUp.setToY(1.0);
            pulseUp.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            
            SequentialTransition pulse = new SequentialTransition(pulseDown, pulseUp);
            pulse.play();
            
            seleccionActual.set(reporte);
            actualizarPanelDetalle(reporte);
        });
        
        // Hover - siempre aplicar efecto hover, incluso si está seleccionado
        card.setOnMouseEntered(e -> {
            boolean esSeleccionado = seleccionMultiple.contains(reporte) || seleccionActual.get() == reporte;
            aplicarEstiloCard(card, esSeleccionado, true); // hover = true siempre
            actualizarCheckboxModerno(checkboxContainer, true);
            AnimacionesFX.hoverIn(card, 1.02, 150);
        });
        card.setOnMouseExited(e -> {
            boolean esSeleccionadoMultiple = seleccionMultiple.contains(reporte);
            boolean esSeleccionadoSimple = seleccionActual.get() == reporte;
            boolean esSeleccionado = esSeleccionadoMultiple || esSeleccionadoSimple;
            aplicarEstiloCard(card, esSeleccionado, false); // hover = false al salir
            actualizarCheckboxModerno(checkboxContainer, esSeleccionadoMultiple);
            AnimacionesFX.hoverOut(card, 150);
        });
        
        return card;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // 🎨 CHECKBOX MODERNO PERSONALIZADO
    // ═══════════════════════════════════════════════════════════════════════
    
    private static StackPane crearCheckboxModerno(ReporteItem reporte) {
        StackPane container = new StackPane();
        container.setPrefSize(34, 34);
        container.setMinSize(34, 34);
        container.setMaxSize(34, 34);
        container.setCursor(javafx.scene.Cursor.HAND);
        
        // Fondo del checkbox con glassmorphism
        javafx.scene.shape.Rectangle fondo = new javafx.scene.shape.Rectangle(26, 26);
        fondo.setArcWidth(8);
        fondo.setArcHeight(8);
        fondo.setFill(Color.web(COLOR_SURFACE(), 0.5));
        fondo.setStroke(Color.web(ACCENT, 0.25));
        fondo.setStrokeWidth(2.5);
        
        // Efecto de resplandor
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(ACCENT, 0.25));
        glow.setRadius(6);
        glow.setSpread(0.2);
        fondo.setEffect(glow);
        
        // Icono de check (oculto inicialmente)
        Node checkIcon = IconosSVG.check("#FFFFFF", 20);
        checkIcon.setOpacity(0);
        checkIcon.setScaleX(0.3);
        checkIcon.setScaleY(0.3);
        checkIcon.setRotate(0); // Iniciar sin rotación
        
        // CheckBox invisible para manejar el estado
        CheckBox checkBox = new CheckBox();
        checkBox.setOpacity(0);
        checkBox.setSelected(seleccionMultiple.contains(reporte));
        
        container.getChildren().addAll(fondo, checkIcon, checkBox);
        container.setUserData(checkBox);
        
        // Aplicar estado inicial
        if (seleccionMultiple.contains(reporte)) {
            fondo.setFill(Color.web(ACCENT));
            fondo.setStroke(Color.web(ACCENT));
            checkIcon.setOpacity(1);
            checkIcon.setScaleX(1);
            checkIcon.setScaleY(1);
            checkIcon.setRotate(0); // Asegurar que no tenga rotación
        }
        
        // Click en el contenedor activa el checkbox
        container.setOnMouseClicked(e -> {
            e.consume();
            checkBox.setSelected(!checkBox.isSelected());
            checkBox.fireEvent(new javafx.event.ActionEvent());
        });
        
        // Hover effect con animaciones fluidas y modernas
        container.setOnMouseEntered(e -> {
            // Escala suave del contenedor
            AnimacionesFX.hoverIn(container, 1.12, 200);
            
            if (!checkBox.isSelected()) {
                // Animación del borde con curva suave
                javafx.animation.Timeline strokeAnim = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(Duration.millis(250),
                        new javafx.animation.KeyValue(fondo.strokeProperty(), Color.web(ACCENT, 1.0), AnimacionesFX.EASE_OUT_CUBIC)
                    )
                );
                strokeAnim.play();
                
                // Animación del resplandor
                DropShadow currentGlow = (DropShadow) fondo.getEffect();
                javafx.animation.Timeline glowAnim = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(Duration.millis(250),
                        new javafx.animation.KeyValue(currentGlow.radiusProperty(), 10, AnimacionesFX.EASE_OUT_CUBIC),
                        new javafx.animation.KeyValue(currentGlow.colorProperty(), Color.web(ACCENT, 0.5), AnimacionesFX.EASE_OUT_CUBIC)
                    )
                );
                glowAnim.play();
            }
        });
        container.setOnMouseExited(e -> {
            // Retorno suave de la escala
            AnimacionesFX.hoverOut(container, 200);
            
            if (!checkBox.isSelected()) {
                // Retorno del borde
                javafx.animation.Timeline strokeAnim = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(Duration.millis(200),
                        new javafx.animation.KeyValue(fondo.strokeProperty(), Color.web(ACCENT, 0.25), AnimacionesFX.EASE_IN_CUBIC)
                    )
                );
                strokeAnim.play();
                
                // Retorno del resplandor
                DropShadow currentGlow = (DropShadow) fondo.getEffect();
                javafx.animation.Timeline glowAnim = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(Duration.millis(200),
                        new javafx.animation.KeyValue(currentGlow.radiusProperty(), 6, AnimacionesFX.EASE_IN_CUBIC),
                        new javafx.animation.KeyValue(currentGlow.colorProperty(), Color.web(ACCENT, 0.25), AnimacionesFX.EASE_IN_CUBIC)
                    )
                );
                glowAnim.play();
            }
        });
        
        return container;
    }
    
    private static void actualizarCheckboxModerno(StackPane container, boolean seleccionado) {
        if (container.getChildren().size() < 2) return;
        
        javafx.scene.shape.Rectangle fondo = (javafx.scene.shape.Rectangle) container.getChildren().get(0);
        Node checkIcon = container.getChildren().get(1);
        
        // IMPORTANTE: Siempre resetear la rotación primero
        double rotacionActual = checkIcon.getRotate();
        
        if (seleccionado) {
            // Si ya está visible, no animar (evitar animaciones duplicadas)
            if (checkIcon.getOpacity() > 0.9) {
                checkIcon.setRotate(0);
                checkIcon.setScaleX(1);
                checkIcon.setScaleY(1);
                return;
            }
            
            // Animación suave del fondo con gradiente
            javafx.animation.Timeline fillAnim = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.millis(300),
                    new javafx.animation.KeyValue(fondo.fillProperty(), Color.web(ACCENT), AnimacionesFX.EASE_OUT_CUBIC)
                )
            );
            fillAnim.play();
            
            // Animación del borde
            javafx.animation.Timeline strokeAnim = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.millis(300),
                    new javafx.animation.KeyValue(fondo.strokeProperty(), Color.web(ACCENT), javafx.animation.Interpolator.EASE_BOTH)
                )
            );
            strokeAnim.play();
            
            // Resetear antes de animar
            checkIcon.setRotate(0);
            
            // Animación fluida del check solo con fade y escala (SIN rotación para evitar bugs)
            ParallelTransition checkAnim = new ParallelTransition();
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), checkIcon);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(350), checkIcon);
            scale.setFromX(0.3);
            scale.setFromY(0.3);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            
            checkAnim.getChildren().addAll(fadeIn, scale);
            checkAnim.play();
        } else {
            // Animación suave de deselección con glassmorphism
            javafx.animation.Timeline fillAnim = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.millis(250),
                    new javafx.animation.KeyValue(fondo.fillProperty(), Color.web(COLOR_SURFACE(), 0.5), AnimacionesFX.EASE_IN_CUBIC)
                )
            );
            fillAnim.play();
            
            javafx.animation.Timeline strokeAnim = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.millis(250),
                    new javafx.animation.KeyValue(fondo.strokeProperty(), Color.web(ACCENT, 0.25), AnimacionesFX.EASE_IN_CUBIC)
                )
            );
            strokeAnim.play();
            
            // Animación de salida del check SIN rotación (solo fade y escala)
            ParallelTransition checkOutAnim = new ParallelTransition();
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), checkIcon);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), checkIcon);
            scale.setToX(0.3);
            scale.setToY(0.3);
            scale.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
            
            checkOutAnim.getChildren().addAll(fadeOut, scale);
            checkOutAnim.setOnFinished(ev -> checkIcon.setRotate(0)); // Asegurar reset al terminar
            checkOutAnim.play();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // 🎨 ESTILOS DE CARD CON EFECTOS
    // ═══════════════════════════════════════════════════════════════════════
    
    private static void aplicarEstiloCard(VBox card, boolean seleccionado, boolean hover) {
        String bgBase = TemaManager.isDarkMode() ? 
            "linear-gradient(to bottom, " + TemaManager.getSurface() + ", " + TemaManager.getBgLight() + ")" : 
            TemaManager.getSurface();
        String bgHover = TemaManager.isDarkMode() ? 
            "linear-gradient(to bottom, " + TemaManager.getSurfaceHover() + ", " + TemaManager.getSurface() + ")" : 
            TemaManager.getSurfaceHover();
        String bgSelected = TemaManager.isDarkMode() ? 
            "linear-gradient(to bottom, " + TemaManager.getSurfaceHover() + ", " + TemaManager.getSurface() + ")" : 
            TemaManager.getSurfaceHover();
        String borderBase = TemaManager.getBorder();
        String borderHover = TemaManager.getBorderLight();
        
        if (seleccionado && hover) {
            card.setStyle(
                "-fx-background-color: " + bgSelected + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + ACCENT + ";" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 2;" +
                "-fx-cursor: hand;"
            );
            DropShadow shadowIntense = new DropShadow();
            shadowIntense.setColor(Color.web("#000000", TemaManager.isDarkMode() ? 0.7 : 0.2));
            shadowIntense.setRadius(20);
            shadowIntense.setOffsetY(10);
            card.setEffect(shadowIntense);
        } else if (seleccionado) {
            card.setStyle(
                "-fx-background-color: " + bgSelected + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + ACCENT + ";" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 2;" +
                "-fx-cursor: hand;"
            );
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#000000", TemaManager.isDarkMode() ? 0.6 : 0.15));
            shadow.setRadius(16);
            shadow.setOffsetY(8);
            card.setEffect(shadow);
        } else if (hover) {
            card.setStyle(
                "-fx-background-color: " + bgHover + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + borderHover + ";" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1;" +
                "-fx-cursor: hand;"
            );
            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.web("#000000", TemaManager.isDarkMode() ? 0.5 : 0.12));
            hoverShadow.setRadius(14);
            hoverShadow.setOffsetY(6);
            card.setEffect(hoverShadow);
        } else {
            card.setStyle(
                "-fx-background-color: " + bgBase + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + borderBase + ";" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1;" +
                "-fx-cursor: hand;"
            );
            DropShadow baseShadow = new DropShadow();
            baseShadow.setColor(Color.web("#000000", TemaManager.isDarkMode() ? 0.35 : 0.08));
            baseShadow.setRadius(10);
            baseShadow.setOffsetY(4);
            card.setEffect(baseShadow);
        }
    }
    
    private static void actualizarBarraAcciones() {
        boolean haySeleccion = !seleccionMultiple.isEmpty();
        
        if (haySeleccion && !barraAccionesMasivas.isVisible()) {
            // Mostrar con animación fluida y suave
            barraAccionesMasivas.setVisible(true);
            
            // Animación combinada de slide + fade + scale
            ParallelTransition showAnimation = new ParallelTransition();
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), barraAccionesMasivas);
            slideIn.setFromY(60);
            slideIn.setToY(0);
            slideIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC); // Ease-out suave
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), barraAccionesMasivas);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), barraAccionesMasivas);
            scaleIn.setFromX(0.85);
            scaleIn.setFromY(0.85);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            scaleIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            
            showAnimation.getChildren().addAll(slideIn, fadeIn, scaleIn);
            showAnimation.play();
            
        } else if (!haySeleccion && barraAccionesMasivas.isVisible()) {
            // Ocultar con animación fluida
            ParallelTransition hideAnimation = new ParallelTransition();
            
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), barraAccionesMasivas);
            slideOut.setFromY(0);
            slideOut.setToY(50);
            slideOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), barraAccionesMasivas);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
            
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(300), barraAccionesMasivas);
            scaleOut.setFromX(1.0);
            scaleOut.setFromY(1.0);
            scaleOut.setToX(0.9);
            scaleOut.setToY(0.9);
            scaleOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
            
            hideAnimation.getChildren().addAll(slideOut, fadeOut, scaleOut);
            hideAnimation.setOnFinished(e -> barraAccionesMasivas.setVisible(false));
            hideAnimation.play();
        }
    }
    
    private static VBox crearPlaceholderVacio() {
        VBox placeholder = new VBox(20);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(60, 20, 60, 20));
        
        // Icono animado con SVG
        StackPane iconoStack = new StackPane();
        
        Circle bgCircle = new Circle(50, Color.web(ACCENT, 0.15));
        Node icono = IconosSVG.documento(TEXT_DARK(), 48);
        
        iconoStack.getChildren().addAll(bgCircle, icono);
        
        // ★ Cache para rendimiento en equipos viejos
        iconoStack.setCache(true);
        iconoStack.setCacheHint(javafx.scene.CacheHint.SPEED);
        
        // Animación flotante
        TranslateTransition flotar = new TranslateTransition(Duration.millis(1500), iconoStack);
        flotar.setByY(-8);
        flotar.setCycleCount(Animation.INDEFINITE);
        flotar.setAutoReverse(true);
        flotar.play();
        
        // Pulso
        ScaleTransition pulso = new ScaleTransition(Duration.millis(2000), bgCircle);
        pulso.setFromX(1); pulso.setFromY(1);
        pulso.setToX(1.15); pulso.setToY(1.15);
        pulso.setCycleCount(Animation.INDEFINITE);
        pulso.setAutoReverse(true);
        pulso.play();
        
        Label lblVacio = new Label("Sin reportes");
        lblVacio.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblVacio.setTextFill(Color.web(TEXT_DARK()));
        
        Label lblDesc = new Label("Crea tu primer reporte\nde mantenimiento");
        lblDesc.setFont(Font.font("Segoe UI", 13));
        lblDesc.setTextFill(Color.web(TEXT_LIGHT()));
        lblDesc.setStyle("-fx-text-alignment: center;");
        
        Button btnCrear = crearBotonConIcono("Crear reporte", IconosSVG.mas("#FFFFFF", 18), ACCENT);
        btnCrear.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnCrear.setPadding(new Insets(12, 24, 12, 24));
        btnCrear.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        DropShadow btnSombra = new DropShadow(8, Color.web(COLOR_PRIMARY, 0.3));
        btnCrear.setEffect(btnSombra);
        btnCrear.setOnAction(e -> crearNuevoReporte());
        
        placeholder.getChildren().addAll(iconoStack, lblVacio, lblDesc, btnCrear);
        return placeholder;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 📊 PANEL DETALLE
    // ═══════════════════════════════════════════════════════════════════════
    
    private static VBox crearPanelDetalle() {
        VBox panel = new VBox(0);
        panel.setStyle(TemaManager.getPanelStyle());
        
        // Sombra elegante
        DropShadow panelShadow = new DropShadow();
        panelShadow.setColor(Color.web("#000000", TemaManager.isDarkMode() ? 0.5 : 0.15));
        panelShadow.setRadius(25);
        panelShadow.setOffsetY(8);
        panel.setEffect(panelShadow);
        
        // ═══════════════════════════════════════════════════════════════
        // HEADER FIJO (título + botones) - Oculto inicialmente
        // ═══════════════════════════════════════════════════════════════
        headerDetalleFijo = new HBox(10);
        headerDetalleFijo.setAlignment(Pos.CENTER_LEFT);
        headerDetalleFijo.setPadding(new Insets(12, 16, 12, 16));
        headerDetalleFijo.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 16 16 0 0;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 0 0 1 0;"
        );
        headerDetalleFijo.setVisible(false);
        headerDetalleFijo.setManaged(false);
        
        // ═══════════════════════════════════════════════════════════════
        // CONTENIDO SCROLLEABLE
        // ═══════════════════════════════════════════════════════════════
        contenidoDetalle = new VBox(16);
        contenidoDetalle.setPadding(new Insets(16));
        
        scrollDetalle = new ScrollPane(contenidoDetalle);
        scrollDetalle.setFitToWidth(true);
        scrollDetalle.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollDetalle.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollDetalle.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;"
        );
        VBox.setVgrow(scrollDetalle, Priority.ALWAYS);
        
        // ═══════════════════════════════════════════════════════════════
        // FOOTER FIJO (exportar) - Oculto inicialmente
        // ═══════════════════════════════════════════════════════════════
        footerDetalleFijo = new HBox(12);
        footerDetalleFijo.setAlignment(Pos.CENTER);
        footerDetalleFijo.setPadding(new Insets(10, 16, 10, 16));
        footerDetalleFijo.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 0 0 16 16;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 1 0 0 0;"
        );
        footerDetalleFijo.setVisible(false);
        footerDetalleFijo.setManaged(false);
        
        panel.getChildren().addAll(headerDetalleFijo, scrollDetalle, footerDetalleFijo);
        
        // Animación de entrada
        panel.setOpacity(0);
        panel.setTranslateY(25);
        panel.setScaleX(0.97);
        panel.setScaleY(0.97);
        
        FadeTransition fadePanel = new FadeTransition(Duration.millis(400), panel);
        fadePanel.setFromValue(0);
        fadePanel.setToValue(1);
        fadePanel.setDelay(Duration.millis(200));
        fadePanel.play();
        
        TranslateTransition slidePanel = new TranslateTransition(Duration.millis(450), panel);
        slidePanel.setFromY(25);
        slidePanel.setToY(0);
        slidePanel.setDelay(Duration.millis(200));
        slidePanel.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        slidePanel.play();
        
        ScaleTransition scalePanel = new ScaleTransition(Duration.millis(450), panel);
        scalePanel.setFromX(0.97);
        scalePanel.setFromY(0.97);
        scalePanel.setToX(1);
        scalePanel.setToY(1);
        scalePanel.setDelay(Duration.millis(200));
        scalePanel.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        scalePanel.play();
        
        // Estado inicial - sin selección
        contenidoDetalle.getChildren().add(crearEstadoSinSeleccion());
        
        return panel;
    }
    
    private static VBox crearEstadoSinSeleccion() {
        VBox estado = new VBox(24);
        estado.setAlignment(Pos.CENTER);
        VBox.setVgrow(estado, Priority.ALWAYS);
        
        // Spacer superior para bajar el contenido
        Region spacerTop = new Region();
        VBox.setVgrow(spacerTop, Priority.ALWAYS);
        
        // Icono grande con SVG de Lucide (archivo/documento)
        StackPane iconoGrande = new StackPane();
        
        Circle c1 = new Circle(70, Color.web(ACCENT, 0.06));
        Circle c2 = new Circle(52, Color.web(ACCENT, 0.10));
        Circle c3 = new Circle(36, Color.web(ACCENT, 0.15));
        
        Node icono = IconosSVG.documento(ACCENT, 44);
        
        iconoGrande.getChildren().addAll(c1, c2, c3, icono);
        
        // ★ Cache para rendimiento en equipos viejos
        iconoGrande.setCache(true);
        iconoGrande.setCacheHint(javafx.scene.CacheHint.SPEED);
        
        // Animación suave de respiración
        ScaleTransition scale1 = new ScaleTransition(Duration.millis(3000), c1);
        scale1.setFromX(1); scale1.setFromY(1);
        scale1.setToX(1.15); scale1.setToY(1.15);
        scale1.setCycleCount(Animation.INDEFINITE);
        scale1.setAutoReverse(true);
        scale1.setInterpolator(Interpolator.EASE_BOTH);
        scale1.play();
        
        ScaleTransition scale2 = new ScaleTransition(Duration.millis(3000), c2);
        scale2.setFromX(1); scale2.setFromY(1);
        scale2.setToX(1.12); scale2.setToY(1.12);
        scale2.setCycleCount(Animation.INDEFINITE);
        scale2.setAutoReverse(true);
        scale2.setInterpolator(Interpolator.EASE_BOTH);
        scale2.setDelay(Duration.millis(200));
        scale2.play();
        
        // Fade suave del icono
        FadeTransition fade = new FadeTransition(Duration.millis(2500), icono);
        fade.setFromValue(0.7);
        fade.setToValue(1.0);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);
        fade.setInterpolator(Interpolator.EASE_BOTH);
        fade.play();
        
        Label lblTitulo = new Label("Selecciona un reporte");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web(TEXT_DARK()));
        
        Label lblDesc = new Label("Haz clic en cualquier reporte de la lista\npara ver sus detalles y opciones de exportación");
        lblDesc.setFont(Font.font("Segoe UI", 14));
        lblDesc.setTextFill(Color.web(TEXT_LIGHT()));
        lblDesc.setStyle("-fx-text-alignment: center;");
        
        // Tips con iconos SVG (solo PDF y Excel)
        HBox tips = new HBox(30);
        tips.setAlignment(Pos.CENTER);
        tips.setPadding(new Insets(20, 0, 0, 0));
        
        tips.getChildren().addAll(
            crearMiniTipConIcono(IconosSVG.pdf(ACCENT, 26), "PDF"),
            crearMiniTipConIcono(IconosSVG.excel(SUCCESS, 26), "Excel")
        );
        
        // Spacer inferior para balancear
        Region spacerBottom = new Region();
        VBox.setVgrow(spacerBottom, Priority.ALWAYS);
        
        estado.getChildren().addAll(spacerTop, iconoGrande, lblTitulo, lblDesc, tips, spacerBottom);
        return estado;
    }
    
    private static VBox crearMiniTipConIcono(Node icono, String texto) {
        VBox tip = new VBox(8);
        tip.setAlignment(Pos.CENTER);
        tip.setPadding(new Insets(16, 24, 16, 24));
        String baseStyle = TemaManager.isDarkMode() ?
            "-fx-background-color: linear-gradient(to bottom, " + TemaManager.getSurface() + ", " + TemaManager.getBgLight() + ");" :
            "-fx-background-color: " + TemaManager.getSurface() + ";";
        tip.setStyle(
            baseStyle +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );
        
        // Sombra sutil
        DropShadow tipShadow = new DropShadow();
        tipShadow.setColor(Color.web("#000000", TemaManager.isDarkMode() ? 0.3 : 0.1));
        tipShadow.setRadius(10);
        tipShadow.setOffsetY(4);
        tip.setEffect(tipShadow);
        
        Label textLbl = new Label(texto);
        textLbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        textLbl.setTextFill(Color.web(TEXT_LIGHT()));
        
        tip.getChildren().addAll(icono, textLbl);
        
        // Hover con animación
        tip.setOnMouseEntered(e -> {
            String hoverStyle = TemaManager.isDarkMode() ?
                "-fx-background-color: linear-gradient(to bottom, " + TemaManager.getSurfaceHover() + ", " + TemaManager.getSurface() + ");" :
                "-fx-background-color: " + TemaManager.getSurfaceHover() + ";";
            tip.setStyle(
                hoverStyle +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + TemaManager.getBorderLight() + ";" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;"
            );
            AnimacionesFX.hoverIn(tip, 1.05, 150);
        });
        tip.setOnMouseExited(e -> {
            String exitStyle = TemaManager.isDarkMode() ?
                "-fx-background-color: linear-gradient(to bottom, " + TemaManager.getSurface() + ", " + TemaManager.getBgLight() + ");" :
                "-fx-background-color: " + TemaManager.getSurface() + ";";
            tip.setStyle(
                exitStyle +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + TemaManager.getBorder() + ";" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;"
            );
            AnimacionesFX.hoverOut(tip, 150);
        });
        
        return tip;
    }
    
    private static VBox crearMiniTip(String icono, String texto) {
        VBox tip = new VBox(4);
        tip.setAlignment(Pos.CENTER);
        tip.setPadding(new Insets(12, 20, 12, 20));
        tip.setStyle("-fx-background-color: " + COLOR_SURFACE() + "; -fx-background-radius: 8; -fx-border-color: " + COLOR_BORDER() + "; -fx-border-radius: 8;");
        
        Label iconLbl = new Label(icono);
        iconLbl.setFont(Font.font(18));
        
        Label textLbl = new Label(texto);
        textLbl.setFont(Font.font("Segoe UI", 11));
        textLbl.setTextFill(Color.web(TEXT_LIGHT()));
        
        tip.getChildren().addAll(iconLbl, textLbl);
        return tip;
    }
    
    private static void actualizarPanelDetalle(ReporteItem reporte) {
        contenidoDetalle.getChildren().clear();
        headerDetalleFijo.getChildren().clear();
        footerDetalleFijo.getChildren().clear();
        
        if (reporte == null) {
            // Ocultar header y footer cuando no hay selección
            headerDetalleFijo.setVisible(false);
            headerDetalleFijo.setManaged(false);
            footerDetalleFijo.setVisible(false);
            footerDetalleFijo.setManaged(false);
            contenidoDetalle.getChildren().add(crearEstadoSinSeleccion());
            return;
        }
        
        // Mostrar header y footer
        headerDetalleFijo.setVisible(true);
        headerDetalleFijo.setManaged(true);
        footerDetalleFijo.setVisible(true);
        footerDetalleFijo.setManaged(true);
        
        // Scroll al inicio cuando se selecciona un nuevo reporte
        scrollDetalle.setVvalue(0);
        
        ReporteFormularioFX.DatosReporte d = reporte.getDatos();
        
        // ═══════════════════════════════════════════════════════════════
        // HEADER FIJO: Ticket + Badge + Botones (compacto)
        // ═══════════════════════════════════════════════════════════════
        
        // Ticket compacto
        Label lblTicket = new Label("Reporte #" + reporte.getTicket());
        lblTicket.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTicket.setTextFill(Color.web(TEXT_DARK()));
        
        // Badge tipo compacto
        String badgeColor = reporte.getTipo().toUpperCase().contains("CORRECTIVO") ? DANGER : 
                           reporte.getTipo().toUpperCase().contains("LÓGICO") ? COLOR_PRIMARY : SUCCESS;
        String tipoCorto = reporte.getTipo().toUpperCase().replace("MANTENIMIENTO ", "");
        
        Label badgeTipo = new Label(tipoCorto);
        badgeTipo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        badgeTipo.setTextFill(Color.WHITE);
        badgeTipo.setPadding(new Insets(3, 8, 3, 8));
        badgeTipo.setStyle("-fx-background-color: " + badgeColor + "; -fx-background-radius: 8;");
        
        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);
        
        // Botones compactos
        Button btnEditar = crearBotonAccionCompacto(IconosSVG.editar(SUCCESS, 14), "Editar", SUCCESS);
        btnEditar.setOnAction(e -> editarReporte(reporte));
        
        Button btnEliminar = crearBotonAccionCompacto(IconosSVG.eliminar(DANGER, 14), "Eliminar", DANGER);
        btnEliminar.setOnAction(e -> eliminarReporte(reporte));
        
        headerDetalleFijo.getChildren().addAll(lblTicket, badgeTipo, spacerHeader, btnEditar, btnEliminar);
        
        // ═══════════════════════════════════════════════════════════════
        // FOOTER FIJO: Botones de exportación (compacto)
        // ═══════════════════════════════════════════════════════════════
        
        Label lblExportar = new Label("Exportar:");
        lblExportar.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblExportar.setTextFill(Color.web(TEXT_LIGHT()));
        
        Button btnPDF = crearBotonExportarCompacto(IconosSVG.pdf("#FFFFFF", 16), "PDF", TemaManager.COLOR_DANGER);
        Button btnExcel = crearBotonExportarCompacto(IconosSVG.excelOriginal(16), "Excel", "#21A366");
        
        btnPDF.setOnAction(e -> exportarReporte(reporte, "PDF"));
        btnExcel.setOnAction(e -> exportarReporte(reporte, "EXCEL"));
        
        footerDetalleFijo.getChildren().addAll(lblExportar, btnPDF, btnExcel);
        
        // ═══════════════════════════════════════════════════════════════
        // CONTENIDO SCROLLEABLE
        // ═══════════════════════════════════════════════════════════════
        VBox seccionGeneral = crearSeccionInfo("INFORMACIÓN GENERAL");
        
        // Grid de 2 columnas
        HBox gridGeneral = new HBox(16);
        gridGeneral.setAlignment(Pos.TOP_LEFT);
        
        VBox col1 = new VBox(0);
        col1.setPrefWidth(250);
        col1.getChildren().add(crearCampoInfo("Fecha y Hora", IconosSVG.calendario(ACCENT, 16), 
            reporte.getFecha() + " - " + (reporte.getHoraCreacion().isEmpty() ? "--:--" : reporte.getHoraCreacion())));
        
        VBox col2 = new VBox(0);
        col2.setPrefWidth(250);
        col2.getChildren().add(crearCampoInfo("Usuario", IconosSVG.usuario(ACCENT, 16), reporte.getNombre()));
        
        HBox.setHgrow(col1, Priority.ALWAYS);
        HBox.setHgrow(col2, Priority.ALWAYS);
        gridGeneral.getChildren().addAll(col1, col2);
        
        HBox gridGeneral2 = new HBox(16);
        
        VBox col3 = new VBox(0);
        col3.setPrefWidth(250);
        col3.getChildren().add(crearCampoInfo("Dispositivo", IconosSVG.computadora(ACCENT, 16), reporte.getDispositivo()));
        
        VBox col4 = new VBox(0);
        col4.setPrefWidth(250);
        col4.getChildren().add(crearCampoInfo("Técnico Asignado", IconosSVG.herramienta(ACCENT, 16), reporte.getTecnico()));
        
        HBox.setHgrow(col3, Priority.ALWAYS);
        HBox.setHgrow(col4, Priority.ALWAYS);
        gridGeneral2.getChildren().addAll(col3, col4);
        
        seccionGeneral.getChildren().addAll(gridGeneral, gridGeneral2);
        
        // ═══════════════════════════════════════════════════════════════
        // SECCIÓN: UBICACIÓN
        // ═══════════════════════════════════════════════════════════════
        VBox seccionUbicacion = crearSeccionInfo("UBICACIÓN");
        
        HBox gridUbicacion = new HBox(16);
        
        VBox colSede = new VBox(0);
        colSede.setPrefWidth(250);
        colSede.getChildren().add(crearCampoInfo("Sede", IconosSVG.ubicacion(ACCENT, 16), d.sede != null ? d.sede : "-"));
        
        VBox colCiudad = new VBox(0);
        colCiudad.setPrefWidth(250);
        colCiudad.getChildren().add(crearCampoInfo("Ciudad", IconosSVG.ciudad(ACCENT, 16), d.ciudad != null ? d.ciudad : "-"));
        
        HBox.setHgrow(colSede, Priority.ALWAYS);
        HBox.setHgrow(colCiudad, Priority.ALWAYS);
        gridUbicacion.getChildren().addAll(colSede, colCiudad);
        
        seccionUbicacion.getChildren().add(gridUbicacion);
        
        // ═══════════════════════════════════════════════════════════════
        // SECCIÓN: PROCEDIMIENTO REALIZADO (trabajoRealizado)
        // ═══════════════════════════════════════════════════════════════
        VBox seccionDescripcion = null;
        if (d.trabajoRealizado != null && !d.trabajoRealizado.isEmpty()) {
            seccionDescripcion = crearSeccionInfo("PROCEDIMIENTO REALIZADO");
            
            VBox campoDesc = new VBox(4);
            campoDesc.setPadding(new Insets(12));
            campoDesc.setStyle(
                "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 8;"
            );
            
            Label lblDescripcion = new Label(d.trabajoRealizado);
            lblDescripcion.setFont(Font.font("Segoe UI", 13));
            lblDescripcion.setTextFill(Color.web(TEXT_DARK()));
            lblDescripcion.setWrapText(true);
            lblDescripcion.setMaxWidth(Double.MAX_VALUE); // Permite expandirse
            lblDescripcion.setMinHeight(Region.USE_PREF_SIZE); // No colapsar altura
            
            campoDesc.getChildren().add(lblDescripcion);
            seccionDescripcion.getChildren().add(campoDesc);
        }
        
        // ═══════════════════════════════════════════════════════════════
        // SECCIÓN: OBSERVACIONES (observaciones)
        // ═══════════════════════════════════════════════════════════════
        VBox seccionNotas = null;
        if (d.observaciones != null && !d.observaciones.isEmpty()) {
            seccionNotas = crearSeccionInfo("OBSERVACIONES");
            
            VBox campoNotas = new VBox(4);
            campoNotas.setPadding(new Insets(12));
            campoNotas.setStyle(
                "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 8;"
            );
            
            Label lblNotas = new Label(d.observaciones);
            lblNotas.setFont(Font.font("Segoe UI", 13));
            lblNotas.setTextFill(Color.web(TEXT_DARK()));
            lblNotas.setWrapText(true);
            lblNotas.setMaxWidth(Double.MAX_VALUE); // Permite expandirse
            lblNotas.setMinHeight(Region.USE_PREF_SIZE); // No colapsar altura
            
            campoNotas.getChildren().add(lblNotas);
            seccionNotas.getChildren().add(campoNotas);
        }
        
        // Agregar secciones al contenidoDetalle
        VBox.setVgrow(seccionGeneral, Priority.NEVER);
        VBox.setVgrow(seccionUbicacion, Priority.NEVER);
        
        // Solo agregar las secciones de contenido (sin header ni exportación)
        contenidoDetalle.getChildren().add(seccionGeneral);
        contenidoDetalle.getChildren().add(seccionUbicacion);
        if (seccionDescripcion != null) contenidoDetalle.getChildren().add(seccionDescripcion);
        if (seccionNotas != null) contenidoDetalle.getChildren().add(seccionNotas);
    }
    
    // Botón de acción compacto para el header fijo
    private static Button crearBotonAccionCompacto(Node icono, String texto, String color) {
        Button btn = new Button();
        HBox content = new HBox(5);
        content.setAlignment(Pos.CENTER);
        
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        lbl.setTextFill(Color.web(color));
        
        content.getChildren().addAll(icono, lbl);
        btn.setGraphic(content);
        btn.setPadding(new Insets(6, 12, 6, 12));
        btn.setStyle(
            "-fx-background-color: " + color + "18;" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: transparent;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + "30;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: transparent;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.08, 120);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + "18;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: transparent;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 120);
        });
        
        return btn;
    }
    
    // Botón de exportar compacto para el footer fijo
    private static Button crearBotonExportarCompacto(Node icono, String texto, String color) {
        Button btn = new Button();
        HBox content = new HBox(5);
        content.setAlignment(Pos.CENTER);
        
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        lbl.setTextFill(Color.WHITE);
        
        content.getChildren().addAll(icono, lbl);
        btn.setGraphic(content);
        btn.setPadding(new Insets(6, 14, 6, 14));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: transparent;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: derive(" + color + ", -15%);" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: transparent;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.05, 100);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: transparent;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 100);
        });
        
        return btn;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // 🎨 COMPONENTES PARA PANEL DE DETALLE MEJORADO
    // ═══════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionInfo(String titulo) {
        VBox seccion = new VBox(12);
        
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        lblTitulo.setTextFill(Color.web(TEXT_LIGHT()));
        
        seccion.getChildren().add(lblTitulo);
        return seccion;
    }
    
    private static VBox crearCampoInfo(String etiqueta, Node icono, String valor) {
        VBox campo = new VBox(4);
        campo.setPadding(new Insets(12));
        campo.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 8;"
        );
        
        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.setFont(Font.font("Segoe UI", 11));
        lblEtiqueta.setTextFill(Color.web(ACCENT));
        
        HBox valorBox = new HBox(6);
        valorBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblValor.setTextFill(Color.web(TEXT_DARK()));
        
        valorBox.getChildren().addAll(icono, lblValor);
        campo.getChildren().addAll(lblEtiqueta, valorBox);
        
        return campo;
    }
    
    private static void agregarCampoDetalle(GridPane grid, int row, String label, String valor) {
        Label lblLabel = new Label(label);
        lblLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblLabel.setTextFill(Color.web(TEXT_LIGHT()));
        
        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblValor.setTextFill(Color.web(TEXT_DARK()));
        
        grid.add(lblLabel, 0, row);
        grid.add(lblValor, 1, row);
    }
    
    private static Button crearBotonAccion(String icono, String texto, String color) {
        Button btn = new Button(icono + " " + texto);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btn.setPadding(new Insets(10, 18, 10, 18));
        btn.setStyle(
            "-fx-background-color: " + color + "20;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.05, 120);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + "20;" +
                "-fx-text-fill: " + color + ";" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 120);
        });
        return btn;
    }
    
    private static Button crearBotonExportar(String icono, String texto, String color) {
        VBox contenido = new VBox(6);
        contenido.setAlignment(Pos.CENTER);
        
        Label iconLbl = new Label(icono);
        iconLbl.setFont(Font.font(28));
        
        Label textLbl = new Label(texto);
        textLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        textLbl.setTextFill(Color.web(color));
        
        contenido.getChildren().addAll(iconLbl, textLbl);
        
        Button btn = new Button();
        btn.setGraphic(contenido);
        btn.setPrefSize(110, 90);
        btn.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        
        DropShadow sombra = new DropShadow(8, Color.web(color, 0.2));
        btn.setEffect(sombra);
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;" +
                "-fx-cursor: hand;"
            );
            textLbl.setTextFill(Color.WHITE);
            sombra.setRadius(15);
            sombra.setColor(Color.web(color, 0.4));
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;" +
                "-fx-cursor: hand;"
            );
            textLbl.setTextFill(Color.web(color));
            sombra.setRadius(8);
            sombra.setColor(Color.web(color, 0.2));
        });
        
        return btn;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // 🎨 MÉTODOS AUXILIARES PARA ICONOS SVG
    // ═══════════════════════════════════════════════════════════════════════
    
    private static Label crearLabelMini(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", 11));
        lbl.setTextFill(Color.web(TEXT_LIGHT()));
        return lbl;
    }
    
    private static Label crearLabelInfo(String texto, String color) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", 12));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }
    
    private static Button crearBotonConIcono(String texto, Node icono, String color) {
        Button btn = new Button();
        HBox contenido = new HBox(8);
        contenido.setAlignment(Pos.CENTER);
        Label textoLabel = new Label(texto);
        textoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        textoLabel.setTextFill(Color.WHITE);
        contenido.getChildren().addAll(icono, textoLabel);
        btn.setGraphic(contenido);
        btn.setPadding(new Insets(12, 24, 12, 24));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: derive(" + color + ", -15%);" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.05, 120);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 120);
        });
        return btn;
    }
    
    private static Button crearBotonAccionSVG(Node icono, String texto, String color) {
        Button btn = new Button();
        HBox contenido = new HBox(6);
        contenido.setAlignment(Pos.CENTER);
        Label textoLabel = new Label(texto);
        textoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        textoLabel.setTextFill(Color.web(color));
        contenido.getChildren().addAll(icono, textoLabel);
        btn.setGraphic(contenido);
        btn.setPadding(new Insets(10, 18, 10, 18));
        btn.setStyle(
            "-fx-background-color: " + color + "20;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> {
            textoLabel.setTextFill(Color.WHITE);
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.05, 120);
        });
        btn.setOnMouseExited(e -> {
            textoLabel.setTextFill(Color.web(color));
            btn.setStyle(
                "-fx-background-color: " + color + "20;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 120);
        });
        return btn;
    }
    
    private static void agregarCampoDetalleConIcono(GridPane grid, int row, Node icono, String label, String valor) {
        HBox labelBox = new HBox(8);
        labelBox.setAlignment(Pos.CENTER_LEFT);
        Label lblLabel = new Label(label);
        lblLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblLabel.setTextFill(Color.web(TEXT_LIGHT()));
        labelBox.getChildren().addAll(icono, lblLabel);
        
        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblValor.setTextFill(Color.web(TEXT_DARK()));
        
        grid.add(labelBox, 0, row);
        grid.add(lblValor, 1, row);
    }
    
    private static Button crearBotonExportarConIcono(Node icono, String texto, String color) {
        VBox contenido = new VBox(10);
        contenido.setAlignment(Pos.CENTER);
        
        Label textLbl = new Label(texto);
        textLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        textLbl.setTextFill(Color.web(color));
        
        contenido.getChildren().addAll(icono, textLbl);
        
        Button btn = new Button();
        btn.setGraphic(contenido);
        btn.setPrefSize(130, 110);
        String btnBaseStyle = TemaManager.isDarkMode() ?
            "-fx-background-color: linear-gradient(to bottom, " + COLOR_BG_LIGHT() + ", " + TemaManager.getBg() + ");" :
            "-fx-background-color: " + TemaManager.getSurface() + ";";
        btn.setStyle(
            btnBaseStyle +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        
        DropShadow sombra = new DropShadow(10, Color.web(color, 0.25));
        sombra.setOffsetY(4);
        btn.setEffect(sombra);
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + color + ", derive(" + color + ", -20%));" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 1;" +
                "-fx-cursor: hand;"
            );
            textLbl.setTextFill(Color.WHITE);
            sombra.setRadius(18);
            sombra.setColor(Color.web(color, 0.5));
            AnimacionesFX.hoverIn(btn, 1.05, 150);
        });
        btn.setOnMouseExited(e -> {
            String exitStyle = TemaManager.isDarkMode() ?
                "-fx-background-color: linear-gradient(to bottom, " + COLOR_BG_LIGHT() + ", " + TemaManager.getBg() + ");" :
                "-fx-background-color: " + TemaManager.getSurface() + ";";
            btn.setStyle(
                exitStyle +
                "-fx-background-radius: 16;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 1;" +
                "-fx-cursor: hand;"
            );
            textLbl.setTextFill(Color.web(color));
            sombra.setRadius(10);
            sombra.setColor(Color.web(color, 0.25));
            AnimacionesFX.hoverOut(btn, 150);
        });
        
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 🔧 ACCIONES
    // ═══════════════════════════════════════════════════════════════════════
    
    private static Stage obtenerVentanaActiva() {
        return ventana != null ? ventana : ventanaParent;
    }
    
    // Método público para agregar un reporte desde fuera (usado por DashboardFX)
    /**
     * Elimina TODOS los reportes asociados a un proyecto dado.
     * Se usa cuando se elimina un proyecto para limpieza completa.
     * 
     * @param nombreProyecto Nombre del proyecto (formato "N. NombreProyecto")
     * @return Cantidad de reportes eliminados
     */
    public static int eliminarReportesPorProyecto(String nombreProyecto) {
        if (nombreProyecto == null || nombreProyecto.isEmpty()) return 0;
        
        try {
            Path archivo = inventario.fx.config.PortablePaths.getReportesFile();
            
            if (!Files.exists(archivo)) {
                System.out.println("[GestionReportesFX] No hay archivo de reportes, nada que eliminar");
                return 0;
            }
            
            // Leer todos los reportes
            List<ReporteItem> todosLosReportes;
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(archivo))) {
                @SuppressWarnings("unchecked")
                List<ReporteItem> lista = (List<ReporteItem>) ois.readObject();
                todosLosReportes = new ArrayList<>(lista);
            }
            
            int totalAntes = todosLosReportes.size();
            
            // Extraer solo el nombre sin el índice para comparación más robusta
            // Ej: "1. Antonio" -> "Antonio"
            String nombreSinIndice = nombreProyecto.replaceFirst("^\\d+\\.\\s*", "");
            
            // Eliminar reportes que coincidan (por nombre completo O por nombre sin índice)
            todosLosReportes.removeIf(r -> {
                String proyecto = r.getProyecto();
                if (proyecto == null) return false;
                String proyectoSinIndice = proyecto.replaceFirst("^\\d+\\.\\s*", "");
                return proyecto.equals(nombreProyecto) || proyectoSinIndice.equals(nombreSinIndice);
            });
            
            int eliminados = totalAntes - todosLosReportes.size();
            
            if (eliminados > 0) {
                // Crear backup antes de guardar
                Path backup = inventario.fx.config.PortablePaths.getReportesBak();
                try {
                    Files.copy(archivo, backup, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    System.err.println("[GestionReportesFX] Advertencia creando backup: " + e.getMessage());
                }
                
                // Guardar con escritura atómica
                Path tempFile = archivo.getParent().resolve(DATA_FILE + ".tmp");
                try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(tempFile))) {
                    oos.writeObject(todosLosReportes);
                    oos.flush();
                }
                
                try {
                    Files.move(tempFile, archivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                    Files.move(tempFile, archivo, StandardCopyOption.REPLACE_EXISTING);
                }
                
                // Verificar que se guardó correctamente
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(archivo))) {
                    @SuppressWarnings("unchecked")
                    List<ReporteItem> verificacion = (List<ReporteItem>) ois.readObject();
                    if (verificacion.size() != todosLosReportes.size()) {
                        System.err.println("[GestionReportesFX] ADVERTENCIA: Verificación post-guardado falló. Esperado: " 
                            + todosLosReportes.size() + " Actual: " + verificacion.size());
                    }
                }
                
                // Actualizar lista en memoria si está visible
                if (reportes != null) {
                    reportes.removeIf(r -> {
                        String proyecto = r.getProyecto();
                        if (proyecto == null) return false;
                        String proyectoSinIndice = proyecto.replaceFirst("^\\d+\\.\\s*", "");
                        return proyecto.equals(nombreProyecto) || proyectoSinIndice.equals(nombreSinIndice);
                    });
                }
                
                System.out.println("[GestionReportesFX] \u2705 " + eliminados + " reportes eliminados del proyecto '" + nombreProyecto + "'");
            } else {
                System.out.println("[GestionReportesFX] No se encontraron reportes del proyecto '" + nombreProyecto + "'");
            }
            
            return eliminados;
        } catch (Exception e) {
            AppLogger.getLogger(GestionReportesFX.class).error("[GestionReportesFX] Error eliminando reportes por proyecto: " + e.getMessage(), e);
            return 0;
        }
    }
    
    public static void agregarNuevoReporte(ReporteFormularioFX.DatosReporte datos) {
        if (datos != null) {
            System.out.println("[GestionReportesFX] agregarNuevoReporte() - Recibiendo datos del reporte");
            
            // Asegurar que la lista esté inicializada
            if (reportes == null) {
                cargarDatos();
            }
            
            ReporteItem nuevo = new ReporteItem(datos);
            System.out.println("[GestionReportesFX] Nuevo reporte creado - Ticket: " + nuevo.getTicket() + ", Proyecto: " + nuevo.getProyecto());
            reportes.add(0, nuevo);
            guardarDatos();
            
            // Actualizar vista solo si ya está creada
            if (listaCards != null) {
                actualizarListaCards();
                seleccionActual.set(nuevo);
                if (panelDetalle != null) {
                    actualizarPanelDetalle(nuevo);
                }
            }
            
            System.out.println("[GestionReportesFX] Reporte agregado exitosamente. Total reportes: " + reportes.size());
            
            StackPane cont = getContenedor();
            if (cont != null) {
                NotificacionesFX.success(cont, "¡Reporte Guardado!", "El reporte se ha guardado correctamente.");
            }
        }
    }
    
    private static void crearNuevoReporte() {
        // Establecer la imagen y nombre del proyecto actual antes de abrir el formulario
        try {
            // Obtener datos del proyecto sin recargar toda la lista para preservar el orden
            // Usar proyectoActualFiltro que ya contiene el nombre completo del proyecto
            String currentProject = proyectoActualFiltro != null ? proyectoActualFiltro : System.getProperty("inventario.proyectoActual", "");
            
            int idx = 0;
            try {
                String indice = currentProject.split("\\.")[0];
                idx = Integer.parseInt(indice) - 1;
            } catch (Exception ignored) {}
            
            AdminManager.Proyecto proyectoActual = AdminManager.getProyectoPorIndice(idx);
            if (proyectoActual != null) {
                String imagenPath = proyectoActual.getImagenPath();
                System.setProperty("reporte.proyectoImagenPath", imagenPath != null ? imagenPath : "");
                System.setProperty("reporte.proyectoNombre", currentProject);
                System.out.println("[GestionReportesFX] Imagen del proyecto establecida: " + imagenPath);
                System.out.println("[GestionReportesFX] Nombre del proyecto establecido: " + currentProject);
            } else {
                System.setProperty("reporte.proyectoImagenPath", "");
                System.setProperty("reporte.proyectoNombre", currentProject);
            }
        } catch (Exception e) {
            System.setProperty("reporte.proyectoImagenPath", "");
            System.setProperty("reporte.proyectoNombre", proyectoActualFiltro != null ? proyectoActualFiltro : "");
        }
        
        ReporteFormularioFX.mostrarParaGestion(obtenerVentanaActiva(), datos -> {
            agregarNuevoReporte(datos);
        });
    }
    
    private static void editarReporte(ReporteItem item) {
        ReporteFormularioFX.mostrarParaEdicion(obtenerVentanaActiva(), item.getDatos(), datos -> {
            if (datos != null) {
                int idx = reportes.indexOf(item);
                if (idx >= 0) {
                    // Conservar el ID y fechas originales
                    ReporteItem editado = new ReporteItem(datos, item.getId(), item.getFechaCreacion(), item.getHoraCreacion());
                    reportes.set(idx, editado);
                    guardarDatos();
                    actualizarListaCards();
                    seleccionActual.set(editado);
                    actualizarPanelDetalle(editado);
                    System.out.println("[GestionReportesFX] Reporte editado y guardado: " + editado.getTicket());
                    
                    StackPane cont = getContenedor();
                    if (cont != null) {
                        NotificacionesFX.success(cont, "Reporte Editado", "Reporte #" + editado.getTicket() + " actualizado correctamente.");
                    }
                }
            }
        });
    }
    
    private static void eliminarReporte(ReporteItem item) {
        boolean confirmar = DialogosFX.confirmarEliminacion(
            obtenerVentanaActiva(),
            "Eliminar reporte",
            "¿Eliminar reporte #" + item.getTicket() + "?",
            null
        );
        
        if (confirmar) {
            reportes.remove(item);
            guardarDatos();
            actualizarListaCards();
            seleccionActual.set(null);
            actualizarPanelDetalle(null);
            
            StackPane contenedor = contenedorPrincipal != null ? contenedorPrincipal : contenedorIntegrado;
            if (contenedor != null) {
                NotificacionesFX.success(contenedor, "Reporte eliminado", "Reporte #" + item.getTicket() + " eliminado correctamente.");
            }
        }
    }
    
    private static void eliminarSeleccionMasiva() {
        if (seleccionMultiple.isEmpty()) {
            StackPane contenedorToast = contenedorPrincipal != null ? contenedorPrincipal : contenedorIntegrado;
            if (contenedorToast != null) {
                NotificacionesFX.warning(contenedorToast, "Sin selección", "Selecciona al menos un reporte.");
            }
            return;
        }
        
        int cantidad = seleccionMultiple.size();
        boolean confirmar = DialogosFX.confirmarEliminacion(
            obtenerVentanaActiva(),
            "Eliminar reportes",
            "¿Eliminar " + cantidad + " reporte" + (cantidad != 1 ? "s" : "") + "?",
            null
        );
        
        if (confirmar) {
            reportes.removeAll(new ArrayList<>(seleccionMultiple));
            guardarDatos();
            limpiarSeleccionMultiple();
            
            StackPane contenedor = contenedorPrincipal != null ? contenedorPrincipal : contenedorIntegrado;
            if (contenedor != null) {
                NotificacionesFX.success(contenedor, "Eliminación completada", cantidad + " reporte" + (cantidad != 1 ? "s eliminados" : " eliminado") + " correctamente.");
            }
        }
    }
    
    private static void exportarSeleccionMasiva(String formato) {
        if (seleccionMultiple.isEmpty()) {
            StackPane contenedorToast = contenedorPrincipal != null ? contenedorPrincipal : contenedorIntegrado;
            if (contenedorToast != null) {
                NotificacionesFX.warning(contenedorToast, "Sin selección", "Selecciona al menos un reporte.");
            }
            return;
        }
        
        // Si solo hay uno, exportar directamente
        if (seleccionMultiple.size() == 1) {
            exportarReporte(seleccionMultiple.get(0), formato);
            return;
        }
        
        // Guardar en carpeta elegida por el usuario
        String proyectoMasivo = seleccionMultiple.get(0).getProyecto();
        File carpetaSugerida = inventario.fx.config.PortablePaths.getExportReportesDir(proyectoMasivo).toFile();
        
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Seleccionar carpeta para guardar " + seleccionMultiple.size() + " reportes");
        dirChooser.setInitialDirectory(carpetaSugerida);
        
        File carpeta = dirChooser.showDialog(obtenerVentanaActiva());
        if (carpeta == null) return; // Usuario canceló
        
        {
            // Crear lista inmutable de reportes a exportar
            List<ReporteItem> reportesAExportar = new ArrayList<>(seleccionMultiple);
            int totalReportes = reportesAExportar.size();
            String ext = "PDF".equals(formato) ? ".pdf" : ".xlsx";
            
            // Crear ventana de progreso mejorada
            Stage ventanaProgreso = crearVentanaProgreso(totalReportes, formato);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> datos = (Map<String, Object>) ventanaProgreso.getUserData();
            Button btnCancelar = (Button) datos.get("btnCancelar");
            Label lblBtnCancelar = (Label) datos.get("lblBtnCancelar");
            
            // Control de cancelación
            final boolean[] cancelado = {false};
            btnCancelar.setOnAction(e -> {
                cancelado[0] = true;
                btnCancelar.setDisable(true);
                lblBtnCancelar.setText("Cancelando...");
            });
            
            File carpetaFinal = carpeta;
            
            // Ejecutar en hilo secundario
            javafx.concurrent.Task<int[]> tareaExportacion = new javafx.concurrent.Task<int[]>() {
                @Override
                protected int[] call() throws Exception {
                    int exitosos = 0;
                    int fallidos = 0;
                    
                    for (int i = 0; i < reportesAExportar.size(); i++) {
                        if (cancelado[0]) break;
                        
                        ReporteItem item = reportesAExportar.get(i);
                        final int indice = i + 1;
                        
                        // Fase 1: Preparando — el timer continuo empieza a avanzar
                        javafx.application.Platform.runLater(() -> {
                            actualizarVentanaProgresoConFase(ventanaProgreso, indice, totalReportes, item, "preparando");
                        });
                        Thread.sleep(200);
                        
                        try {
                            String nombreArchivo = "Reporte_" + item.getTicket() + "_" + 
                                item.getFechaCreacion().replace("/", "-") + ext;
                            File destino = new File(carpetaFinal, nombreArchivo);
                            
                            int contador = 1;
                            while (destino.exists()) {
                                nombreArchivo = "Reporte_" + item.getTicket() + "_" + 
                                    item.getFechaCreacion().replace("/", "-") + "_" + contador + ext;
                                destino = new File(carpetaFinal, nombreArchivo);
                                contador++;
                            }
                            
                            // Fase 2: Generando — la barra sigue avanzando sola
                            // mientras ejecutarExportacion() trabaja (drift lento automático)
                            javafx.application.Platform.runLater(() -> {
                                actualizarVentanaProgresoConFase(ventanaProgreso, indice, totalReportes, item, "generando");
                            });
                            
                            ejecutarExportacion(item, destino, formato);
                            
                            // Fase 3: Guardando — la barra alcanza rápido el objetivo
                            javafx.application.Platform.runLater(() -> {
                                actualizarVentanaProgresoConFase(ventanaProgreso, indice, totalReportes, item, "guardando");
                            });
                            Thread.sleep(150);
                            
                            exitosos++;
                            
                            // Fase 4: Completado
                            javafx.application.Platform.runLater(() -> {
                                actualizarVentanaProgresoConFase(ventanaProgreso, indice, totalReportes, item, "exportando");
                            });
                            Thread.sleep(100);
                            
                        } catch (Exception e) {
                            fallidos++;
                            System.err.println("Error exportando reporte " + item.getTicket() + ": " + e.getMessage());
                        }
                    }
                    
                    return new int[]{exitosos, fallidos, cancelado[0] ? 1 : 0};
                }
            };
            
            tareaExportacion.setOnSucceeded(e -> {
                int[] resultado = tareaExportacion.getValue();
                int exitosos = resultado[0];
                int fallidos = resultado[1];
                boolean fueCancelado = resultado[2] == 1;
                
                finalizarVentanaProgreso(ventanaProgreso, exitosos, fallidos, fueCancelado, carpetaFinal);
            });
            
            tareaExportacion.setOnFailed(e -> {
                ventanaProgreso.close();
                Throwable ex = tareaExportacion.getException();
                String mensajeError = ex != null ? ex.getMessage() : "Error desconocido";
                StackPane contenedor = contenedorPrincipal != null ? contenedorPrincipal : contenedorIntegrado;
                if (contenedor != null) {
                    NotificacionesFX.error(contenedor, "Error de exportación masiva", "Ocurrió un error durante la exportación: " + mensajeError);
                }
            });
            
            // Mostrar ventana e iniciar tarea
            ventanaProgreso.show();
            new Thread(tareaExportacion, "Reportes-ExportacionMasiva").start();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // 📊 VENTANA DE PROGRESO
    // ═══════════════════════════════════════════════════════════════════════
    
    private static Stage crearVentanaProgreso(int totalReportes, String formato) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.initOwner(obtenerVentanaActiva());
        ventana.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        ventana.setResizable(false);
        
        try {
            InputStream icon = GestionReportesFX.class.getResourceAsStream("/images/icon.png");
            if (icon != null) ventana.getIcons().add(new Image(icon));
        } catch (Exception ignored) {}
        
        String colorFormato = "PDF".equals(formato) ? ACCENT : SUCCESS;
        
        // Contenedor principal
        VBox contenedor = new VBox(24);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPadding(new Insets(40, 50, 40, 50));
        contenedor.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;"
        );
        
        // Sombra
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.3));
        sombra.setRadius(20);
        contenedor.setEffect(sombra);
        
        // ── Icono con círculos animados (igual al individual) ──
        StackPane iconoContainer = new StackPane();
        iconoContainer.setPrefSize(60, 60);
        
        Circle circulo1 = new Circle(30, Color.web(colorFormato, 0.15));
        Circle circulo2 = new Circle(24, Color.web(colorFormato, 0.25));
        
        // Animación de pulso en círculos
        ScaleTransition pulso1 = new ScaleTransition(Duration.millis(1000), circulo1);
        pulso1.setFromX(1); pulso1.setFromY(1);
        pulso1.setToX(1.2); pulso1.setToY(1.2);
        pulso1.setCycleCount(Animation.INDEFINITE);
        pulso1.setAutoReverse(true);
        pulso1.play();
        
        ScaleTransition pulso2 = new ScaleTransition(Duration.millis(800), circulo2);
        pulso2.setFromX(1); pulso2.setFromY(1);
        pulso2.setToX(1.15); pulso2.setToY(1.15);
        pulso2.setCycleCount(Animation.INDEFINITE);
        pulso2.setAutoReverse(true);
        pulso2.setDelay(Duration.millis(150));
        pulso2.play();
        
        // Icono con rotación (más grande)
        Node iconoFormato = "PDF".equals(formato) ? 
            IconosSVG.pdf(colorFormato, 34) : IconosSVG.excel(colorFormato, 34);
        RotateTransition rotacion = new RotateTransition(Duration.millis(2000), iconoFormato);
        rotacion.setByAngle(360);
        rotacion.setCycleCount(Animation.INDEFINITE);
        rotacion.setInterpolator(Interpolator.LINEAR);
        rotacion.play();
        
        iconoContainer.getChildren().addAll(circulo1, circulo2, iconoFormato);
        
        // ★ Cache para rendimiento en equipos viejos
        iconoContainer.setCache(true);
        iconoContainer.setCacheHint(javafx.scene.CacheHint.SPEED);
        
        // ── Texto principal (más grande) ──
        Label lblTitulo = new Label("Exportando a " + formato + "...");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        lblTitulo.setTextFill(Color.web(TEXT_DARK()));
        
        Label lblSubtitulo = new Label("Por favor, espera un momento");
        lblSubtitulo.setFont(Font.font("Segoe UI", 14));
        lblSubtitulo.setTextFill(Color.web(TEXT_LIGHT()));
        
        // ── Barra de progreso ──
        StackPane barraContainer = new StackPane();
        barraContainer.setMaxWidth(380);
        
        Rectangle barraFondo = new Rectangle(380, 7);
        barraFondo.setArcWidth(7);
        barraFondo.setArcHeight(7);
        barraFondo.setFill(Color.web(COLOR_BG_DARK()));
        
        Rectangle barraProgreso = new Rectangle(0, 7);
        barraProgreso.setArcWidth(7);
        barraProgreso.setArcHeight(7);
        barraProgreso.setFill(Color.web(colorFormato));
        StackPane.setAlignment(barraProgreso, Pos.CENTER_LEFT);
        
        // Glow sutil en la barra
        DropShadow glowBarra = new DropShadow();
        glowBarra.setColor(Color.web(colorFormato, 0.5));
        glowBarra.setRadius(6);
        barraProgreso.setEffect(glowBarra);
        
        barraContainer.getChildren().addAll(barraFondo, barraProgreso);
        
        // ── Porcentaje y estado (más grande) ──
        Label lblPorcentaje = new Label("0%");
        lblPorcentaje.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblPorcentaje.setTextFill(Color.web(colorFormato));
        
        Label lblEstado = new Label("Preparando exportación...");
        lblEstado.setFont(Font.font("Segoe UI", 13));
        lblEstado.setTextFill(Color.web(TEXT_LIGHT()));
        
        Label lblDetalle = new Label("");
        lblDetalle.setFont(Font.font("Segoe UI", 12));
        lblDetalle.setTextFill(Color.web(TEXT_LIGHT()));
        
        // ── Botón cancelar ──
        Button btnCancelar = new Button();
        Label lblBtnCancelar = new Label("Cancelar");
        lblBtnCancelar.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblBtnCancelar.setTextFill(Color.web(TEXT_LIGHT()));
        btnCancelar.setGraphic(lblBtnCancelar);
        btnCancelar.setPadding(new Insets(10, 28, 10, 28));
        btnCancelar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btnCancelar.setOnMouseEntered(e -> {
            btnCancelar.setStyle(
                "-fx-background-color: " + DANGER + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + DANGER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-cursor: hand;"
            );
            lblBtnCancelar.setTextFill(Color.WHITE);
            AnimacionesFX.hoverIn(btnCancelar, 1.05, 120);
        });
        btnCancelar.setOnMouseExited(e -> {
            btnCancelar.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-cursor: hand;"
            );
            lblBtnCancelar.setTextFill(Color.web(TEXT_LIGHT()));
            AnimacionesFX.hoverOut(btnCancelar, 120);
        });
        
        contenedor.getChildren().addAll(
            iconoContainer, lblTitulo, lblSubtitulo,
            barraContainer, lblPorcentaje, lblEstado, lblDetalle,
            btnCancelar
        );
        
        StackPane root = new StackPane(contenedor);
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(20));
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        ventana.setScene(scene);
        
        // Guardar referencias en userData
        Map<String, Object> datos = new HashMap<>();
        datos.put("barraProgreso", barraProgreso);
        datos.put("lblPorcentaje", lblPorcentaje);
        datos.put("lblEstado", lblEstado);
        datos.put("lblDetalle", lblDetalle);
        datos.put("lblTitulo", lblTitulo);
        datos.put("lblSubtitulo", lblSubtitulo);
        datos.put("btnCancelar", btnCancelar);
        datos.put("lblBtnCancelar", lblBtnCancelar);
        datos.put("contenedor", contenedor);
        datos.put("iconoContainer", iconoContainer);
        datos.put("formato", formato);
        datos.put("pulso1", pulso1);
        datos.put("pulso2", pulso2);
        datos.put("rotacion", rotacion);
        
        // ═══ SISTEMA DE PROGRESO CONTINUO (nunca se detiene) ═══
        double[] progresoDisplay = {0.0};
        double[] progresoObjetivo = {0.0};
        String[] textoEstado = {"Preparando exportación..."};
        String[] textoDetalle = {""};
        datos.put("progresoDisplay", progresoDisplay);
        datos.put("progresoObjetivo", progresoObjetivo);
        datos.put("textoEstado", textoEstado);
        datos.put("textoDetalle", textoDetalle);
        
        // Timer continuo cada 50ms — la barra SIEMPRE se mueve
        Timeline timerContinuo = new Timeline();
        timerContinuo.setCycleCount(Animation.INDEFINITE);
        timerContinuo.getKeyFrames().add(new KeyFrame(Duration.millis(50), ev -> {
            double display = progresoDisplay[0];
            double objetivo = progresoObjetivo[0];
            double diff = objetivo - display;
            
            double incremento;
            if (diff > 0.03) {
                incremento = diff * 0.12;
            } else if (diff > 0.005) {
                incremento = diff * 0.08;
            } else {
                incremento = 0.0008 + Math.random() * 0.0012;
                if (display >= objetivo + 0.05) {
                    incremento = 0.0001;
                }
            }
            
            progresoDisplay[0] = Math.min(display + incremento, 0.995);
            barraProgreso.setWidth(380 * progresoDisplay[0]);
            
            int pct = (int) (progresoDisplay[0] * 100);
            lblPorcentaje.setText(pct + "%");
            lblEstado.setText(textoEstado[0]);
            lblDetalle.setText(textoDetalle[0]);
        }));
        timerContinuo.play();
        datos.put("timerContinuo", timerContinuo);
        
        ventana.setUserData(datos);
        
        // Animación de entrada
        contenedor.setOpacity(0);
        contenedor.setScaleX(0.8);
        contenedor.setScaleY(0.8);
        
        ventana.setOnShown(e -> {
            ventana.centerOnScreen();
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), contenedor);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), contenedor);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            
            new ParallelTransition(fadeIn, scaleIn).play();
        });
        
        // Prevenir cierre con X mientras exporta
        ventana.setOnCloseRequest(e -> e.consume());
        
        return ventana;
    }
    
    /**
     * Actualiza el objetivo de progreso. El timer continuo se encarga de
     * mover la barra suavemente hacia este valor.
     */
    private static void actualizarVentanaProgreso(Stage ventana, int actual, int total, ReporteItem item) {
        actualizarVentanaProgresoConFase(ventana, actual, total, item, "exportando");
    }
    
    /**
     * Establece el objetivo de progreso y el texto de estado.
     * La barra se mueve sola gracias al timer continuo — nunca se traba.
     */
    private static void actualizarVentanaProgresoConFase(Stage ventana, int actual, int total, ReporteItem item, String fase) {
        @SuppressWarnings("unchecked")
        Map<String, Object> datos = (Map<String, Object>) ventana.getUserData();
        
        double[] progresoObjetivo = (double[]) datos.get("progresoObjetivo");
        String[] textoEstado = (String[]) datos.get("textoEstado");
        String[] textoDetalle = (String[]) datos.get("textoDetalle");
        
        // Progreso base y objetivo para este archivo
        double base = (double) (actual - 1) / total;
        double techo = (double) actual / total;
        
        // Sub-fases dentro de cada archivo
        double fraccion;
        switch (fase) {
            case "preparando":
                fraccion = 0.2;
                textoEstado[0] = "Preparando reporte " + actual + " de " + total + "...";
                break;
            case "generando":
                fraccion = 0.5;
                textoEstado[0] = "Generando archivo " + actual + " de " + total + "...";
                break;
            case "guardando":
                fraccion = 0.85;
                textoEstado[0] = "Guardando archivo " + actual + " de " + total + "...";
                break;
            default: // completado
                fraccion = 1.0;
                textoEstado[0] = "Exportado " + actual + " de " + total;
                break;
        }
        
        progresoObjetivo[0] = base + (techo - base) * fraccion;
        textoDetalle[0] = "#" + item.getTicket() + " - " + item.getNombre();
    }
    
    /**
     * Muestra el diálogo de resultado al finalizar la exportación masiva.
     * Diseño estilo "¡Reporte Guardado!" con check verde y fondo oscuro.
     */
    private static void finalizarVentanaProgreso(Stage ventana, int exitosos, int fallidos, boolean cancelado, File carpeta) {
        @SuppressWarnings("unchecked")
        Map<String, Object> datos = (Map<String, Object>) ventana.getUserData();
        
        final ScaleTransition animPulso1 = (ScaleTransition) datos.get("pulso1");
        final ScaleTransition animPulso2 = (ScaleTransition) datos.get("pulso2");
        final Timeline timerContinuo = (Timeline) datos.get("timerContinuo");
        final RotateTransition rotacion = (RotateTransition) datos.get("rotacion");
        
        // Detener TODAS las animaciones
        if (animPulso1 != null) animPulso1.stop();
        if (animPulso2 != null) animPulso2.stop();
        if (timerContinuo != null) timerContinuo.stop();
        if (rotacion != null) rotacion.stop();
        
        // Cerrar ventana de progreso
        ventana.close();
        
        // Mostrar toast según resultado
        StackPane contenedor = contenedorPrincipal != null ? contenedorPrincipal : contenedorIntegrado;
        if (contenedor != null) {
            if (cancelado) {
                NotificacionesFX.warning(contenedor, "Exportación Cancelada",
                    exitosos + " reporte" + (exitosos != 1 ? "s exportados" : " exportado") + " antes de cancelar.");
            } else if (fallidos == 0) {
                NotificacionesFX.successConAccion(contenedor, "¡Exportación Completada!",
                    exitosos + " reporte" + (exitosos != 1 ? "s exportados" : " exportado") + " correctamente.",
                    "📂 Abrir carpeta",
                    () -> {
                        try { java.awt.Desktop.getDesktop().open(carpeta); } 
                        catch (Exception ex) { System.err.println("Error abriendo carpeta: " + ex.getMessage()); }
                    });
            } else {
                NotificacionesFX.warning(contenedor, "Exportación con Errores",
                    exitosos + " exportados, " + fallidos + " fallidos.");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 📤 EXPORTACIÓN
    // ═══════════════════════════════════════════════════════════════════════
    
    private static void exportarReporte(ReporteItem item, String formato) {
        String ext = "PDF".equals(formato) ? ".pdf" : "EXCEL".equals(formato) ? ".xlsx" : ".csv";
        String nombreArchivo = "Reporte_" + item.getTicket() + "_" + item.getFechaCreacion().replace("/", "-") + ext;
        
        // Directorio inicial sugerido (carpeta del proyecto)
        java.nio.file.Path dirProyecto = inventario.fx.config.PortablePaths.getExportReportesDir(item.getProyecto());
        
        // Mostrar diálogo "Guardar como" para que el usuario elija dónde guardar
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar reporte " + formato);
        fileChooser.setInitialFileName(nombreArchivo);
        fileChooser.setInitialDirectory(dirProyecto.toFile());
        
        if ("PDF".equals(formato)) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        } else {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx"));
        }
        
        File archivo = fileChooser.showSaveDialog(obtenerVentanaActiva());
        if (archivo == null) return; // Usuario canceló
        
        {
            // Mostrar indicador de carga
            Stage ventanaCarga = mostrarIndicadorCargaExportacion(formato);
            
            File archivoFinal = archivo;
            
            // Ejecutar exportación en hilo secundario
            javafx.concurrent.Task<Boolean> tareaExportacion = new javafx.concurrent.Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    ejecutarExportacion(item, archivoFinal, formato);
                    return true;
                }
            };
            
            tareaExportacion.setOnSucceeded(e -> {
                ventanaCarga.close();
                StackPane contenedor = contenedorPrincipal != null ? contenedorPrincipal : contenedorIntegrado;
                if (contenedor != null) {
                    File carpetaDestino = archivoFinal.getParentFile();
                    NotificacionesFX.successConAccion(contenedor, "¡Reporte Guardado!",
                        "Guardado en: " + archivoFinal.getName(),
                        "📂 Abrir carpeta",
                        () -> {
                            try { java.awt.Desktop.getDesktop().open(carpetaDestino); } 
                            catch (Exception ex) { System.err.println("Error abriendo carpeta: " + ex.getMessage()); }
                        });
                }
            });
            
            tareaExportacion.setOnFailed(e -> {
                ventanaCarga.close();
                Throwable ex = tareaExportacion.getException();
                String mensajeError = ex != null ? ex.getMessage() : "Error desconocido";
                StackPane contenedor = contenedorPrincipal != null ? contenedorPrincipal : contenedorIntegrado;
                if (contenedor != null) {
                    NotificacionesFX.error(contenedor, "Error de exportación", "No se pudo exportar el reporte a " + formato + ": " + mensajeError);
                }
            });
            
            new Thread(tareaExportacion, "Reportes-ExportarIndividual").start();
        }
    }
    
    private static Stage mostrarIndicadorCargaExportacion(String formato) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.initOwner(obtenerVentanaActiva());
        ventana.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        ventana.setResizable(false);
        
        // Contenedor principal
        VBox contenedor = new VBox(24);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPadding(new Insets(40, 50, 40, 50));
        contenedor.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;"
        );
        
        // Sombra
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.3));
        sombra.setRadius(20);
        contenedor.setEffect(sombra);
        
        // Icono animado
        StackPane iconoContainer = new StackPane();
        iconoContainer.setPrefSize(60, 60);
        
        // Círculos animados
        Circle circulo1 = new Circle(30, Color.web(ACCENT, 0.15));
        Circle circulo2 = new Circle(24, Color.web(ACCENT, 0.25));
        
        // Animación de pulso
        ScaleTransition pulso = new ScaleTransition(Duration.millis(1000), circulo1);
        pulso.setFromX(1); pulso.setFromY(1);
        pulso.setToX(1.2); pulso.setToY(1.2);
        pulso.setCycleCount(Animation.INDEFINITE);
        pulso.setAutoReverse(true);
        pulso.play();
        
        // Animación de rotación para el icono
        Node icono = "PDF".equals(formato) ? IconosSVG.pdf(ACCENT, 28) : IconosSVG.excel(ACCENT, 28);
        RotateTransition rotacion = new RotateTransition(Duration.millis(2000), icono);
        rotacion.setByAngle(360);
        rotacion.setCycleCount(Animation.INDEFINITE);
        rotacion.setInterpolator(Interpolator.LINEAR);
        rotacion.play();
        
        iconoContainer.getChildren().addAll(circulo1, circulo2, icono);
        
        // ★ Cache para rendimiento en equipos viejos
        iconoContainer.setCache(true);
        iconoContainer.setCacheHint(javafx.scene.CacheHint.SPEED);
        // Texto
        Label lblExportando = new Label("Exportando a " + formato + "...");
        lblExportando.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 18));
        lblExportando.setTextFill(Color.web(TEXT_DARK()));
        
        Label lblEspera = new Label("Por favor, espera un momento");
        lblEspera.setFont(Font.font("Segoe UI", 13));
        lblEspera.setTextFill(Color.web(TEXT_LIGHT()));
        
        contenedor.getChildren().addAll(iconoContainer, lblExportando, lblEspera);
        
        StackPane root = new StackPane(contenedor);
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(20));
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        ventana.setScene(scene);
        ventana.show();
        ventana.centerOnScreen();
        
        return ventana;
    }
    
    private static void ejecutarExportacion(ReporteItem item, File destino, String formato) throws Exception {
        ReporteFormularioFX.DatosReporte d = item.getDatos();
        
        // Re-obtener la imagen actual del proyecto (por si se configuró después de guardar el reporte)
        if (d.proyectoNombre != null && !d.proyectoNombre.isEmpty()) {
            try {
                // Obtener datos del proyecto sin recargar toda la lista para preservar el orden
                int idx = 0;
                try {
                    String indice = d.proyectoNombre.split("\\.")[0];
                    idx = Integer.parseInt(indice) - 1;
                } catch (Exception ignored) {}
                
                AdminManager.Proyecto proyecto = AdminManager.getProyectoPorIndice(idx);
                if (proyecto != null && proyecto.getImagenPath() != null && !proyecto.getImagenPath().isEmpty()) {
                    d.proyectoImagenPath = proyecto.getImagenPath();
                    System.out.println("[GestionReportesFX] Imagen actualizada desde Admin: " + d.proyectoImagenPath);
                }
            } catch (Exception e) {
                System.err.println("[GestionReportesFX] Error al obtener imagen del proyecto: " + e.getMessage());
            }
        }
        
        // DEBUG: Verificar si la imagen está disponible
        System.out.println("[GestionReportesFX] Exportando reporte - Imagen proyecto: " + 
            (d.proyectoImagenPath != null && !d.proyectoImagenPath.isEmpty() ? d.proyectoImagenPath : "NO TIENE"));
        
        configurarPropiedades(d);
        
        switch (formato) {
            case "PDF":
                File temp = File.createTempFile("rep_", ".xlsx");
                System.setProperty("reporte.rutaDestino", temp.getAbsolutePath());
                new TemplateBuilder().buildTemplate();
                ExcelToPdfConverter.convertirExcelAPdf(temp.getAbsolutePath(), destino.getAbsolutePath());
                temp.delete();
                break;
            case "EXCEL":
                System.setProperty("reporte.rutaDestino", destino.getAbsolutePath());
                new TemplateBuilder().buildTemplate();
                break;
            case "CSV":
                try (PrintWriter w = new PrintWriter(destino)) {
                    w.println("Campo,Valor");
                    w.println("Ticket," + escaparCSV(d.ticket));
                    w.println("Tipo," + escaparCSV(d.tipoSolicitud));
                    w.println("Fecha," + d.dia + "/" + d.mes + "/" + d.anio);
                    w.println("Usuario," + escaparCSV(d.nombre));
                    w.println("Técnico," + escaparCSV(d.tecnico));
                    w.println("Dispositivo," + escaparCSV(d.tipoDispositivo));
                    w.println("Trabajo," + escaparCSV(d.trabajoRealizado));
                    w.println("Observaciones," + escaparCSV(d.observaciones));
                }
                break;
        }
    }
    
    private static void configurarPropiedades(ReporteFormularioFX.DatosReporte d) {
        System.setProperty("reporte.tipoSolicitud", d.tipoSolicitud != null ? d.tipoSolicitud : "");
        System.setProperty("reporte.dia", d.dia != null ? d.dia : "");
        System.setProperty("reporte.mes", d.mes != null ? d.mes : "");
        System.setProperty("reporte.anio", d.anio != null ? d.anio : "");
        System.setProperty("reporte.ticket", d.ticket != null ? d.ticket : "");
        System.setProperty("reporte.ciudad", d.ciudad != null ? d.ciudad : "");
        System.setProperty("reporte.direccion", d.direccion != null ? d.direccion : "");
        System.setProperty("reporte.nombre", d.nombre != null ? d.nombre : "");
        System.setProperty("reporte.correo", d.correo != null ? d.correo : "");
        System.setProperty("reporte.tecnico", d.tecnico != null ? d.tecnico : "");
        System.setProperty("reporte.sede", d.sede != null ? d.sede : "");
        System.setProperty("reporte.tipoDispositivo", d.tipoDispositivo != null ? d.tipoDispositivo : "");
        System.setProperty("reporte.marca", d.marca != null ? d.marca : "");
        System.setProperty("reporte.modelo", d.modelo != null ? d.modelo : "");
        System.setProperty("reporte.serial", d.serial != null ? d.serial : "");
        System.setProperty("reporte.placa", d.placa != null ? d.placa : "");
        System.setProperty("reporte.condicionesHW", d.condiciones != null ? d.condiciones : "");
        System.setProperty("reporte.pcEnciende", d.pcEnciende != null ? d.pcEnciende : "");
        System.setProperty("reporte.discoDuro", d.discoDuro != null ? d.discoDuro : "");
        System.setProperty("reporte.cdDvd", d.cddvd != null ? d.cddvd : "");
        System.setProperty("reporte.botonesPC", d.botonesPC != null ? d.botonesPC : "");
        System.setProperty("reporte.condicionesPC", d.condicionesPC != null ? d.condicionesPC : "");
        System.setProperty("reporte.procesador", d.procesador != null ? d.procesador : "");
        System.setProperty("reporte.memoriaRAM", d.memoriaRAM != null ? d.memoriaRAM : "");
        System.setProperty("reporte.discoDuroCapacidad", d.discoDuroCapacidad != null ? d.discoDuroCapacidad : "");
        System.setProperty("reporte.monitorEnciende", d.monitorEnciende != null ? d.monitorEnciende : "");
        System.setProperty("reporte.pantalla", d.pantalla != null ? d.pantalla : "");
        System.setProperty("reporte.onlyOne", d.onlyOne != null ? d.onlyOne : "");
        System.setProperty("reporte.botonesMonitor", d.botonesMonitor != null ? d.botonesMonitor : "");
        System.setProperty("reporte.condicionesMonitor", d.condicionesMonitor != null ? d.condicionesMonitor : "");
        System.setProperty("reporte.tecladoEnciende", d.tecladoEnciende != null ? d.tecladoEnciende : "");
        System.setProperty("reporte.tecladoFunciona", d.tecladoFunciona != null ? d.tecladoFunciona : "");
        System.setProperty("reporte.botonesTeclado", d.botonesTeclado != null ? d.botonesTeclado : "");
        System.setProperty("reporte.condicionesTeclado", d.condicionesTeclado != null ? d.condicionesTeclado : "");
        System.setProperty("reporte.mouseEnciende", d.mouseEnciende != null ? d.mouseEnciende : "");
        System.setProperty("reporte.mouseFunciona", d.mouseFunciona != null ? d.mouseFunciona : "");
        System.setProperty("reporte.botonesMouse", d.botonesMouse != null ? d.botonesMouse : "");
        System.setProperty("reporte.condicionesMouse", d.condicionesMouse != null ? d.condicionesMouse : "");
        System.setProperty("reporte.programasBasicos", d.programasBasicos != null ? d.programasBasicos : "");
        System.setProperty("reporte.otrosProgramas", d.otrosProgramas != null ? d.otrosProgramas : "");
        System.setProperty("reporte.procedimiento", d.trabajoRealizado != null ? d.trabajoRealizado : "");
        System.setProperty("reporte.observaciones", d.observaciones != null ? d.observaciones : "");
        System.setProperty("reporte.firmaTecnico", d.firmaTecnico != null ? d.firmaTecnico : "");
        System.setProperty("reporte.cedulaTecnico", d.cedulaTecnico != null ? d.cedulaTecnico : "");
        System.setProperty("reporte.firmaFuncionario", d.firmaFuncionario != null ? d.firmaFuncionario : "");
        System.setProperty("reporte.cedulaFuncionario", d.cedulaFuncionario != null ? d.cedulaFuncionario : "");
        // IMPORTANTE: Rutas de imágenes de firmas
        System.setProperty("reporte.firmaTecnicoImagenPath", d.firmaTecnicoImagenPath != null ? d.firmaTecnicoImagenPath : "");
        System.setProperty("reporte.firmaFuncionarioImagenPath", d.firmaFuncionarioImagenPath != null ? d.firmaFuncionarioImagenPath : "");
        // Proyecto asociado
        System.setProperty("reporte.proyectoNombre", d.proyectoNombre != null ? d.proyectoNombre : "");
        // Imagen del proyecto
        System.setProperty("reporte.proyectoImagenPath", d.proyectoImagenPath != null ? d.proyectoImagenPath : "");
        
        System.out.println("[GestionReportesFX] Propiedades configuradas - Proyecto: " + (d.proyectoNombre != null ? d.proyectoNombre : "NINGUNO"));
    }
    
    private static String escaparCSV(String v) {
        if (v == null) return "";
        return v.contains(",") || v.contains("\"") || v.contains("\n") ? "\"" + v.replace("\"", "\"\"") + "\"" : v;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PERSISTENCIA
    // ═══════════════════════════════════════════════════════════════════════
    
    private static void cargarDatos() {
        reportes = FXCollections.observableArrayList();
        try {
            Path path = inventario.fx.config.PortablePaths.getReportesFile();
            System.out.println("[GestionReportesFX] Buscando reportes en: " + path);
            if (Files.exists(path)) {
                System.out.println("[GestionReportesFX] Archivo encontrado, cargando...");
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
                    @SuppressWarnings("unchecked")
                    List<ReporteItem> list = (List<ReporteItem>) ois.readObject();
                    
                    // FILTRAR POR PROYECTO ACTUAL DESDE EL INICIO
                    if (proyectoActualFiltro != null && !proyectoActualFiltro.isEmpty()) {
                        List<ReporteItem> reportesFiltrados = list.stream()
                            .filter(r -> proyectoActualFiltro.equals(r.getProyecto()))
                            .collect(java.util.stream.Collectors.toList());
                        reportes.addAll(reportesFiltrados);
                        System.out.println("[GestionReportesFX] Reportes filtrados por proyecto '" + proyectoActualFiltro + "': " + reportesFiltrados.size() + " de " + list.size() + " totales");
                    } else {
                        reportes.addAll(list);
                        System.out.println("[GestionReportesFX] Reportes cargados (sin filtro): " + reportes.size());
                    }
                }
            } else {
                System.out.println("[GestionReportesFX] No existe archivo de reportes");
            }
        } catch (Exception e) {
            AppLogger.getLogger(GestionReportesFX.class).error("[GestionReportesFX] Error cargando datos: " + e.getMessage(), e);
        }
    }
    
    private static void guardarDatos() {
        try {
            Path dir = inventario.fx.config.PortablePaths.getDataRoot();
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path archivo = dir.resolve(DATA_FILE);
            Path backup = dir.resolve(DATA_FILE + ".bak");
            
            // IMPORTANTE: Cargar TODOS los reportes existentes, no solo los del proyecto actual
            List<ReporteItem> todosLosReportes = new ArrayList<>();
            boolean lecturExistentesOk = false;
            if (Files.exists(archivo)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(archivo))) {
                    @SuppressWarnings("unchecked")
                    List<ReporteItem> existentes = (List<ReporteItem>) ois.readObject();
                    todosLosReportes.addAll(existentes);
                    lecturExistentesOk = true;
                } catch (Exception e) {
                    System.err.println("[GestionReportesFX] Error leyendo reportes existentes: " + e.getMessage());
                    // Si no pudimos leer el archivo existente Y hay un filtro de proyecto activo,
                    // NO continuar para evitar perder reportes de otros proyectos
                    if (proyectoActualFiltro != null && !proyectoActualFiltro.isEmpty()) {
                        System.err.println("[GestionReportesFX] ABORTANDO guardado: no se pudo leer archivo existente con filtro activo."
                            + " Esto evita perder reportes de otros proyectos.");
                        return;
                    }
                }
            }
            
            // Remover reportes del proyecto actual (solo si logramos leer los existentes o no había archivo)
            if (proyectoActualFiltro != null && !proyectoActualFiltro.isEmpty()) {
                todosLosReportes.removeIf(r -> proyectoActualFiltro.equals(r.getProyecto()));
            }
            
            // Agregar los reportes actuales del proyecto
            todosLosReportes.addAll(reportes);
            
            // Crear backup del archivo actual ANTES de sobreescribir
            if (Files.exists(archivo)) {
                try {
                    Files.copy(archivo, backup, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    System.err.println("[GestionReportesFX] Advertencia: no se pudo crear backup: " + e.getMessage());
                }
            }
            
            // Guardar en archivo temporal primero (escritura atómica)
            Path tempFile = dir.resolve(DATA_FILE + ".tmp");
            System.out.println("[GestionReportesFX] Guardando " + todosLosReportes.size() + " reportes totales (" + reportes.size() + " del proyecto actual) en: " + archivo);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(tempFile))) {
                oos.writeObject(todosLosReportes);
                oos.flush();
            }
            
            // Mover archivo temporal al destino final (operación atómica en la mayoría de SO)
            Files.move(tempFile, archivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            // Verificar integridad del guardado
            try (ObjectInputStream verifyOis = new ObjectInputStream(Files.newInputStream(archivo))) {
                @SuppressWarnings("unchecked")
                List<ReporteItem> verificacion = (List<ReporteItem>) verifyOis.readObject();
                if (verificacion.size() != todosLosReportes.size()) {
                    System.err.println("[GestionReportesFX] \u26a0\ufe0f VERIFICACIÓN FALLIDA: guardados=" + verificacion.size() + " esperados=" + todosLosReportes.size());
                    // Restaurar desde backup
                    if (Files.exists(backup)) {
                        Files.copy(backup, archivo, StandardCopyOption.REPLACE_EXISTING);
                        System.err.println("[GestionReportesFX] Archivo restaurado desde backup tras verificación fallida");
                    }
                } else {
                    System.out.println("[GestionReportesFX] \u2705 Verificación post-guardado OK: " + verificacion.size() + " reportes");
                }
            } catch (Exception verifyEx) {
                System.err.println("[GestionReportesFX] \u26a0\ufe0f Error en verificación post-guardado: " + verifyEx.getMessage());
            }
            
            System.out.println("[GestionReportesFX] Datos guardados exitosamente");
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            // Fallback: si ATOMIC_MOVE no está soportado, usar REPLACE_EXISTING
            try {
                Path dir = inventario.fx.config.PortablePaths.getDataRoot();
                Path tempFile = dir.resolve(DATA_FILE + ".tmp");
                Path archivo = dir.resolve(DATA_FILE);
                Files.move(tempFile, archivo, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[GestionReportesFX] Datos guardados (fallback sin move atómico)");
            } catch (Exception e2) {
                System.err.println("[GestionReportesFX] Error en fallback de guardado: " + e2.getMessage());
            }
        } catch (Exception e) {
            AppLogger.getLogger(GestionReportesFX.class).error("[GestionReportesFX] Error guardando datos: " + e.getMessage(), e);
            // Intentar restaurar desde backup si el guardado falló
            try {
                Path dir = inventario.fx.config.PortablePaths.getDataRoot();
                Path archivo = dir.resolve(DATA_FILE);
                Path backup = dir.resolve(DATA_FILE + ".bak");
                if (Files.exists(backup) && !Files.exists(archivo)) {
                    Files.copy(backup, archivo);
                    System.out.println("[GestionReportesFX] Archivo restaurado desde backup tras error");
                }
            } catch (Exception e2) {
                System.err.println("[GestionReportesFX] Error restaurando backup: " + e2.getMessage());
            }
        }
    }
}
