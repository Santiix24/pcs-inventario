package inventario.fx.ui.panel;
import inventario.fx.model.InventarioFXBase;
import inventario.fx.model.TemaManager;
import inventario.fx.model.AdminManager;
import inventario.fx.icons.IconosSVG;
import inventario.fx.core.InventarioFX;
import inventario.fx.ui.dialog.DialogosFX;
import inventario.fx.ui.component.NotificacionesFX;

import inventario.fx.util.SVGUtil;
import inventario.fx.util.ComponentesFX;
import inventario.fx.util.AnimacionesFX;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Dashboard elegante y minimalista para visualización del inventario.
 * Diseño premium con tema oscuro y animaciones suaves.
 */
public class DashboardFX extends InventarioFXBase {

    // Colores del tema - Ahora usando TemaManager para soporte light/dark
    private static String COLOR_BG_DARK() { return TemaManager.getBgDark(); }
    private static String COLOR_BG() { return TemaManager.getBg(); }
    private static String COLOR_BG_LIGHT() { return TemaManager.getBgLight(); }
    private static String COLOR_SURFACE() { return TemaManager.getSurface(); }
    private static String COLOR_BORDER() { return TemaManager.getBorder(); }
    private static final String COLOR_PRIMARY = TemaManager.COLOR_PRIMARY;
    private static final String COLOR_PRIMARY_HOVER = TemaManager.COLOR_PRIMARY_HOVER; // Hover rojo consistente
    private static final String COLOR_SUCCESS = TemaManager.COLOR_SUCCESS;
    private static final String COLOR_INFO = TemaManager.COLOR_INFO;
    private static String COLOR_TEXT() { return TemaManager.getText(); }
    private static String COLOR_TEXT_SECONDARY() { return TemaManager.getTextSecondary(); }
    private static String COLOR_TEXT_MUTED() { return TemaManager.getTextMuted(); }

    private static Stage dashboardStage;
    private static Stage parentStage;
    private static TextField globalSearchField;
    private static TabPane mainTabPane;
    private static BorderPane rootLayout;
    private static StackPane contentArea;
    private static VBox panelSistema;
    private static StackPane panelReportes;
    private static StackPane panelEstadisticas;
    private static Button btnNavSistema;
    private static Button btnNavReportes;
    private static Button btnNavEstadisticas;
    private static String vistaActual = "sistema";
    private static Path rutaExcelGlobal;
    
    // Labels de las tarjetas para actualización en tiempo real
    private static Label lblTarjetaEquipos;
    private static Label lblTarjetaApps;

    /**
     * Invalida el caché del panel de sistema para que se reconstruya
     * la próxima vez que se muestre (tras editar/eliminar datos).
     */
    public static void invalidarPanelSistema() {
        panelSistema = null;
    }

    /**
     * Limpia todas las referencias estáticas para evitar fugas de memoria
     * y problemas al cambiar de proyecto.
     */
    private static void limpiarReferenciasEstaticas() {
        // Detener animaciones de estadísticas primero
        EstadisticasFX.detenerAnimaciones();
        
        // Limpiar paneles anteriores
        if (panelSistema != null) {
            panelSistema.getChildren().clear();
            panelSistema = null;
        }
        if (panelReportes != null) {
            panelReportes.getChildren().clear();
            panelReportes = null;
        }
        if (panelEstadisticas != null) {
            panelEstadisticas.getChildren().clear();
            panelEstadisticas = null;
        }
        if (contentArea != null) {
            contentArea.getChildren().clear();
            contentArea = null;
        }
        if (rootLayout != null) {
            rootLayout.getChildren().clear();
            rootLayout = null;
        }
        
        // Resetear otras referencias
        globalSearchField = null;
        mainTabPane = null;
        btnNavSistema = null;
        btnNavReportes = null;
        btnNavEstadisticas = null;
        rutaExcelGlobal = null;
        vistaActual = "sistema";
        lblTarjetaEquipos = null;
        lblTarjetaApps = null;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // MOSTRAR DASHBOARD
    // ════════════════════════════════════════════════════════════════════════════
    
    public static void mostrarDashboard(Stage parent) {
        // Limpiar referencias anteriores para evitar problemas al cambiar de proyecto
        limpiarReferenciasEstaticas();
        
        parentStage = parent;
        
        Path rutaExcel = obtenerRutaExcel(CURRENT_PROJECT);
        if (!Files.exists(rutaExcel)) {
            DialogosFX.mostrarAlerta(parent, "Sin datos", 
                "No existe inventario para este proyecto.", Alert.AlertType.INFORMATION);
            InventarioFX.mostrarMenu(parent);
            return;
        }

        dashboardStage = new Stage();
        dashboardStage.setTitle("SELCOMP — Dashboard");
        dashboardStage.setWidth(1600);
        dashboardStage.setHeight(900);
        dashboardStage.setMinWidth(1200);
        dashboardStage.setMinHeight(700);

        // Cargar icono
        try {
            InputStream iconStream = DashboardFX.class.getResourceAsStream(RUTA_IMAGENES + ICONO);
            if (iconStream != null) {
                dashboardStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");

        // Sidebar izquierdo
        VBox sidebar = crearSidebar(rutaExcel);
        rootLayout.setLeft(sidebar);

        // Área de contenido intercambiable
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");
        
        // Guardar ruta Excel global
        rutaExcelGlobal = rutaExcel;
        
        // Crear paneles
        panelSistema = crearContenidoPrincipal(rutaExcel);
        panelReportes = GestionReportesFX.crearPanelIntegrado(dashboardStage, CURRENT_PROJECT);
        panelEstadisticas = EstadisticasFX.crearPanelEstadisticas(rutaExcel);
        
        // Mostrar panel de sistema por defecto
        contentArea.getChildren().add(panelSistema);
        vistaActual = "sistema";
        
        rootLayout.setCenter(contentArea);

        Scene scene = new Scene(rootLayout);
        // Aplicar tema usando TemaManager
        TemaManager.aplicarTema(scene);
        TemaManager.registrarEscena(scene);

        // ═══════════════════════════════════════════════════════════════
        // ATAJOS DE TECLADO GLOBALES
        // ═══════════════════════════════════════════════════════════════
        scene.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case N -> generarReporte();                    // Ctrl+N = Nuevo reporte
                    case E -> exportarExcelSinPassword(rutaExcelGlobal); // Ctrl+E = Exportar Excel
                    case T -> TemaManager.toggleTheme();           // Ctrl+T = Cambiar tema
                    case DIGIT1 -> cambiarVista("sistema");        // Ctrl+1 = Vista Sistema
                    case DIGIT2 -> cambiarVista("reportes");       // Ctrl+2 = Vista Reportes
                    case DIGIT3 -> cambiarVista("estadisticas");   // Ctrl+3 = Vista Estadísticas
                    default -> {}
                }
            }
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                // ESC = Volver al menú (con confirmación visual)
                dashboardStage.close();
                InventarioFX.mostrarMenu(parentStage);
            }
        });

        // Animación de entrada premium
        rootLayout.setOpacity(0);
        
        dashboardStage.setScene(scene);
        dashboardStage.centerOnScreen();
        dashboardStage.setOnCloseRequest(e -> {
            // Detener animaciones antes de cerrar para evitar NullPointerException
            EstadisticasFX.detenerAnimaciones();
            
            // Limpiar referencias
            limpiarReferenciasEstaticas();
            
            InventarioFX.mostrarMenu(parentStage);
        });
        dashboardStage.show();
        
        // Animación de entrada del dashboard - sidebar slide + contenido fade
        Node sidebarNode = rootLayout.getLeft();
        Node centerNode = rootLayout.getCenter();
        Node bottomNode = rootLayout.getBottom();
        
        // Sidebar desliza desde la izquierda
        if (sidebarNode != null) {
            sidebarNode.setTranslateX(-100);
            sidebarNode.setOpacity(0);
            AnimacionesFX.slideLeftFadeIn(sidebarNode, 500, -100);
        }
        
        // Contenido central: fade + slide up
        if (centerNode != null) {
            centerNode.setTranslateY(25);
            centerNode.setOpacity(0);
            AnimacionesFX.slideUpFadeIn(centerNode, 550, 25, 120);
        }
        
        // Barra de estado: fade in con delay
        if (bottomNode != null) {
            AnimacionesFX.fadeIn(bottomNode, 400, 350);
        }
        
        // Fade-in global del rootLayout
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), rootLayout);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        fadeIn.play();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SIDEBAR ELEGANTE
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSidebar(Path rutaExcel) {
        VBox sidebar = ComponentesFX.crearSidebar(260);

        // Obtener color del proyecto actual
        String colorProyecto = COLOR_PRIMARY; // Color por defecto
        try {
            String indice = CURRENT_PROJECT.split("\\.")[0];
            int idx = Integer.parseInt(indice) - 1;
            AdminManager.Proyecto proyectoActual = AdminManager.getProyectoPorIndice(idx);
            if (proyectoActual != null && proyectoActual.getColor() != null) {
                colorProyecto = proyectoActual.getColor();
            }
        } catch (Exception ignored) {}
        
        // Logo y nombre del proyecto
        VBox logoSection = ComponentesFX.crearLogoSection(colorProyecto, limpiarNombreProyecto(CURRENT_PROJECT));
        logoSection.setStyle("-fx-border-color: " + COLOR_BORDER() + "; -fx-border-width: 0 0 1 0;");

        // Navegación
        btnNavSistema = crearBotonNavegacion("Sistema", IconosSVG.computadora(COLOR_PRIMARY, 20), true);
        btnNavReportes = crearBotonNavegacion("Reportes", IconosSVG.reportes(COLOR_TEXT_SECONDARY(), 20), false);
        btnNavEstadisticas = crearBotonNavegacion("Estadísticas", IconosSVG.estadisticas(COLOR_TEXT_SECONDARY(), 20), false);

        btnNavSistema.setTooltip(crearTooltipModerno("Información del sistema · Ctrl+1"));
        btnNavReportes.setTooltip(crearTooltipModerno("Gestión de reportes · Ctrl+2"));
        btnNavEstadisticas.setTooltip(crearTooltipModerno("Gráficos y estadísticas · Ctrl+3"));

        btnNavSistema.setOnAction(e -> cambiarVista("sistema"));
        btnNavReportes.setOnAction(e -> cambiarVista("reportes"));
        btnNavEstadisticas.setOnAction(e -> cambiarVista("estadisticas"));

        VBox navSection = ComponentesFX.crearNavSection("NAVEGACIÓN", btnNavSistema, btnNavReportes, btnNavEstadisticas);

        // Acciones rápidas
        Button btnExportExcel = ComponentesFX.crearActionButton("Excel", IconosSVG.excelOriginal(22), COLOR_SUCCESS);
        Button btnExportPDF = ComponentesFX.crearActionButton("Generar Reporte", IconosSVG.documento(COLOR_INFO, 20), COLOR_INFO);

        btnExportExcel.setTooltip(crearTooltipModerno("Exportar inventario a Excel · Ctrl+E"));
        btnExportPDF.setTooltip(crearTooltipModerno("Crear reporte de mantenimiento · Ctrl+N"));

        btnExportExcel.setOnAction(e -> exportarExcelSinPassword(rutaExcel));
        btnExportPDF.setOnAction(e -> generarReporte());

        VBox actionsSection = ComponentesFX.crearNavSection("ACCIONES", btnExportExcel, btnExportPDF);
        actionsSection.setStyle("-fx-border-color: " + COLOR_BORDER() + "; -fx-border-width: 1 0 0 0;");

        // Espaciador
        Region spacer = ComponentesFX.crearSpacer();

        // Botón volver
        Button btnVolver = ComponentesFX.crearBotonVolver("Volver al menú");
        btnVolver.setTooltip(crearTooltipModerno("Volver al menú principal · Esc"));
        btnVolver.setOnAction(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), dashboardStage.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> {
                dashboardStage.close();
                InventarioFX.mostrarMenu(parentStage);
            });
            fadeOut.play();
        });

        VBox bottomSection = ComponentesFX.crearBottomSection(btnVolver);

        sidebar.getChildren().addAll(logoSection, navSection, actionsSection, spacer, bottomSection);
        return sidebar;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CONTENIDO PRINCIPAL
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearContenidoPrincipal(Path rutaExcel) {
        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");

        // Header con búsqueda
        HBox header = crearHeader();
        
        // Tarjetas de estadísticas
        HBox statsCards = crearTarjetasEstadisticas(rutaExcel);

        // Contenedor para tabs personalizadas con Inverted Borders exótico
        VBox tabsContainer = crearTabsExoticas(rutaExcel);
        VBox.setVgrow(tabsContainer, Priority.ALWAYS);

        VBox tabWrapper = new VBox(tabsContainer);
        tabWrapper.setPadding(new Insets(0, 32, 24, 32));
        VBox.setVgrow(tabWrapper, Priority.ALWAYS);

        content.getChildren().addAll(header, statsCards, tabWrapper, crearStatusBar(rutaExcel));
        return content;
    }

    /**
     * Barra de estado inferior con info contextual del proyecto y última carga.
     */
    private static HBox crearStatusBar(Path rutaExcel) {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 24, 6, 24));
        bar.setMinHeight(28);
        bar.setMaxHeight(28);
        bar.setStyle(
            "-fx-background-color: " + COLOR_BG_DARK() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 1 0 0 0;"
        );

        // Nombre del proyecto
        String nombreProyecto = limpiarNombreProyecto(CURRENT_PROJECT);
        HBox proyectoBox = new HBox(5);
        proyectoBox.setAlignment(Pos.CENTER_LEFT);
        Node iconoCarpeta = IconosSVG.carpeta(COLOR_TEXT_SECONDARY(), 13);
        Label lblProyecto = new Label(nombreProyecto);
        lblProyecto.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        lblProyecto.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        proyectoBox.getChildren().addAll(iconoCarpeta, lblProyecto);

        // Separador
        Label sep1 = new Label("│");
        sep1.setTextFill(Color.web(COLOR_BORDER()));

        // Estado del archivo
        String estadoArchivo = "Sin archivo";
        if (rutaExcel != null && java.nio.file.Files.exists(rutaExcel)) {
            try {
                java.time.LocalDateTime mod = java.time.LocalDateTime.ofInstant(
                    java.nio.file.Files.getLastModifiedTime(rutaExcel).toInstant(),
                    java.time.ZoneId.systemDefault());
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                estadoArchivo = "Actualizado: " + mod.format(fmt);
            } catch (Exception ignored) {
                estadoArchivo = "Archivo encontrado";
            }
        }
        HBox estadoBox = new HBox(5);
        estadoBox.setAlignment(Pos.CENTER_LEFT);
        Node iconoReloj = IconosSVG.reloj(COLOR_TEXT_MUTED(), 13);
        Label lblEstado = new Label(estadoArchivo);
        lblEstado.setFont(Font.font("Segoe UI", 11));
        lblEstado.setTextFill(Color.web(COLOR_TEXT_MUTED()));
        estadoBox.getChildren().addAll(iconoReloj, lblEstado);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Tema + versión
        HBox versionBox = new HBox(5);
        versionBox.setAlignment(Pos.CENTER_LEFT);
        Node iconoTema = TemaManager.isDarkMode() 
            ? IconosSVG.luna(COLOR_TEXT_MUTED(), 13) 
            : IconosSVG.sol(COLOR_TEXT_MUTED(), 13);
        Label lblVersion = new Label("v1.0");
        lblVersion.setFont(Font.font("Segoe UI", 11));
        lblVersion.setTextFill(Color.web(COLOR_TEXT_MUTED()));
        versionBox.getChildren().addAll(iconoTema, lblVersion);

        bar.getChildren().addAll(proyectoBox, sep1, estadoBox, spacer, versionBox);
        return bar;
    }

    /** Crear tabs con diseño exótico de Inverted Borders */
    private static VBox crearTabsExoticas(Path rutaExcel) {
        VBox container = new VBox(0);
        VBox.setVgrow(container, Priority.ALWAYS);
        
        // Header con las tabs
        HBox tabsHeader = new HBox(0);
        tabsHeader.setAlignment(Pos.BOTTOM_LEFT);
        
        // Contenedor del contenido
        StackPane contentPane = new StackPane();
        contentPane.setStyle(
            "-fx-background-color: " + (COLOR_SURFACE()) + ";" +
            "-fx-background-radius: 0 16 16 16;" +
            "-fx-border-color: " + (COLOR_BORDER()) + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0 16 16 16;"
        );
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        
        // Crear contenidos de las tabs
        VBox contenidoSistema = crearPlaceholderTablaVacia(
            IconosSVG.computadora(COLOR_TEXT_SECONDARY(), 48),
            "Sin datos del sistema",
            "Cargue la información del sistema\npara visualizarla aquí"
        );
        VBox contenidoApps = crearPlaceholderTablaVacia(
            IconosSVG.lista(COLOR_TEXT_SECONDARY(), 48),
            "Sin aplicaciones registradas",
            "Cargue las aplicaciones instaladas\npara visualizarlas aquí"
        );
        
        try {
            XSSFWorkbook wb = abrirCifradoProyecto(rutaExcel);
            if (wb != null) {
                Sheet hojaSystemInfo = wb.getSheet("SystemInfo");
                if (hojaSystemInfo != null && hojaSystemInfo.getLastRowNum() >= 2) {
                    contenidoSistema = crearContenidoTabla(hojaSystemInfo, rutaExcel);
                }

                Sheet hojaApps = wb.getSheet("InstalledApps");
                if (hojaApps != null && hojaApps.getLastRowNum() >= 1) {
                    contenidoApps = crearContenidoTabla(hojaApps, rutaExcel);
                }
                wb.close();
            }
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage(), e);
        }
        
        final VBox finalContenidoSistema = contenidoSistema;
        final VBox finalContenidoApps = contenidoApps;
        
        // Crear tabs con forma exótica
        StackPane tabSistema = crearTabExotica("Sistema", true, true);
        StackPane tabApps = crearTabExotica("Aplicaciones", false, false);
        
        // Mostrar contenido inicial
        contentPane.getChildren().add(finalContenidoSistema);
        
        // Acciones de las tabs
        tabSistema.setOnMouseClicked(e -> {
            actualizarTabExotica(tabSistema, true, true);
            actualizarTabExotica(tabApps, false, false);
            
            // Animación de transición
            contentPane.getChildren().clear();
            finalContenidoSistema.setOpacity(0);
            contentPane.getChildren().add(finalContenidoSistema);
            FadeTransition ft = new FadeTransition(Duration.millis(200), finalContenidoSistema);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        });
        
        tabApps.setOnMouseClicked(e -> {
            actualizarTabExotica(tabSistema, true, false);
            actualizarTabExotica(tabApps, false, true);
            
            // Animación de transición
            contentPane.getChildren().clear();
            finalContenidoApps.setOpacity(0);
            contentPane.getChildren().add(finalContenidoApps);
            FadeTransition ft = new FadeTransition(Duration.millis(200), finalContenidoApps);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        });
        
        tabsHeader.getChildren().addAll(tabSistema, tabApps);
        container.getChildren().addAll(tabsHeader, contentPane);
        
        return container;
    }
    
    /** Crear una tab con forma exótica - estilo Nike/etiqueta */
    private static StackPane crearTabExotica(String nombre, boolean esSistema, boolean seleccionada) {
        StackPane tab = new StackPane();
        tab.setCursor(javafx.scene.Cursor.HAND);
        
        // Contenedor interno con el contenido
        HBox contenido = new HBox(10);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(12, 24, 12, 18));
        
        // Icono con colores adaptados al tema
        String iconColor = seleccionada ? COLOR_PRIMARY : (COLOR_TEXT_SECONDARY());
        Node icono = esSistema ? 
            IconosSVG.computadora(iconColor, 18) : 
            IconosSVG.paquete(iconColor, 18);
        
        // Nombre con color adaptado al tema
        String textColor = seleccionada ? 
            (COLOR_TEXT()) : 
            (COLOR_TEXT_MUTED());
        
        Label lblNombre = new Label(nombre);
        lblNombre.setFont(Font.font("Segoe UI", seleccionada ? FontWeight.SEMI_BOLD : FontWeight.MEDIUM, 14));
        lblNombre.setTextFill(Color.web(textColor));
        
        contenido.getChildren().addAll(icono, lblNombre);
        
        // Estilo de la tab
        if (seleccionada) {
            String bgColor = COLOR_SURFACE();
            String borderColor = COLOR_BORDER();
            
            tab.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 16 16 0 0;" +
                "-fx-border-color: " + borderColor + " " + borderColor + " transparent " + borderColor + ";" +
                "-fx-border-width: 1 1 0 1;" +
                "-fx-border-radius: 16 16 0 0;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0," + (TemaManager.isDarkMode() ? "0.2" : "0.08") + "), 8, 0, 0, -2);"
            );
        } else {
            tab.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 16 16 0 0;"
            );
        }
        
        tab.getChildren().add(contenido);
        
        // Guardar datos para actualización
        tab.setUserData(new Object[]{nombre, esSistema, lblNombre, contenido});
        
        return tab;
    }
    
    /** Actualizar el estado visual de una tab exótica */
    private static void actualizarTabExotica(StackPane tab, boolean esSistema, boolean seleccionada) {
        Object[] data = (Object[]) tab.getUserData();
        String nombre = (String) data[0];
        Label lblNombre = (Label) data[2];
        HBox contenido = (HBox) data[3];
        
        // Recrear icono con el color correcto
        contenido.getChildren().clear();
        
        String iconColor = seleccionada ? COLOR_PRIMARY : (COLOR_TEXT_SECONDARY());
        Node icono = esSistema ? 
            IconosSVG.computadora(iconColor, 18) : 
            IconosSVG.paquete(iconColor, 18);
        
        String textColor = seleccionada ? 
            (COLOR_TEXT()) : 
            (COLOR_TEXT_MUTED());
        
        lblNombre.setFont(Font.font("Segoe UI", seleccionada ? FontWeight.SEMI_BOLD : FontWeight.MEDIUM, 14));
        lblNombre.setTextFill(Color.web(textColor));
        
        contenido.getChildren().addAll(icono, lblNombre);
        
        if (seleccionada) {
            String bgColor = COLOR_SURFACE();
            String borderColor = COLOR_BORDER();
            
            tab.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 16 16 0 0;" +
                "-fx-border-color: " + borderColor + " " + borderColor + " transparent " + borderColor + ";" +
                "-fx-border-width: 1 1 0 1;" +
                "-fx-border-radius: 16 16 0 0;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0," + (TemaManager.isDarkMode() ? "0.2" : "0.08") + "), 8, 0, 0, -2);"
            );
            
            // Animación
            ScaleTransition st = new ScaleTransition(Duration.millis(150), tab);
            st.setFromX(0.95);
            st.setFromY(0.95);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            st.play();
        } else {
            tab.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 16 16 0 0;"
            );
        }
    }
    
    /** Crear contenido de tabla para las tabs exóticas con sistema de filtros avanzado */
    private static VBox crearContenidoTabla(Sheet hoja, Path rutaExcel) {
        VBox contenido = new VBox(12);
        contenido.setPadding(new Insets(16));
        contenido.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(contenido, Priority.ALWAYS);
        
        Row headerRow = hoja.getRow(1);
        if (headerRow == null) {
            contenido.getChildren().add(new Label("Sin datos"));
            return contenido;
        }
        
        int columnas = headerRow.getLastCellNum();
        List<String> encabezados = new ArrayList<>();
        for (int i = 0; i < columnas; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(i);
            encabezados.add(cell != null ? cell.getStringCellValue() : "Columna " + (i + 1));
        }

        // Cargar datos primero para obtener valores únicos
        ObservableList<ObservableList<String>> datosOriginales = FXCollections.observableArrayList();
        int filas = hoja.getLastRowNum();
        java.time.format.DateTimeFormatter sdf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 2; i <= filas; i++) {
            Row row = hoja.getRow(i);
            if (row == null || esFilaVacia(row)) continue;  // Saltar filas nulas o vacías

            ObservableList<String> fila = FXCollections.observableArrayList();
            for (int j = 0; j < columnas; j++) {
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String valor = "";
                if (cell.getCellType() == CellType.STRING) {
                    valor = cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        valor = cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(sdf);
                    } else {
                        valor = String.valueOf((int) cell.getNumericCellValue());
                    }
                }
                fila.add(valor);
            }
            datosOriginales.add(fila);
        }

        // Obtener valores únicos por columna (máximo 8 columnas para filtros)
        List<Set<String>> valoresUnicos = new ArrayList<>();
        int maxFiltros = Math.min(8, columnas);
        for (int i = 0; i < maxFiltros; i++) {
            Set<String> valores = new java.util.TreeSet<>();
            for (ObservableList<String> fila : datosOriginales) {
                if (i < fila.size() && !fila.get(i).trim().isEmpty()) {
                    valores.add(fila.get(i));
                }
            }
            valoresUnicos.add(valores);
        }

        // ========== BARRA PRINCIPAL DE FILTROS ==========
        HBox barraPrincipal = new HBox(12);
        barraPrincipal.setAlignment(Pos.CENTER_LEFT);
        barraPrincipal.setPadding(new Insets(10, 16, 10, 16));
        barraPrincipal.setStyle(
            "-fx-background-color: " + (COLOR_SURFACE()) + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + (COLOR_BORDER()) + ";" +
            "-fx-border-radius: 10;"
        );
        
        // === FILTRO DE BÚSQUEDA INTELIGENTE ===
        TextField camposBusqueda = new TextField();
        camposBusqueda.setPromptText("🔍 Buscar en todos los campos...");
        camposBusqueda.setPrefWidth(250);
        camposBusqueda.setPrefHeight(34);
        camposBusqueda.getStyleClass().add("filter-input");
        camposBusqueda.setStyle(
            "-fx-background-color: " + (COLOR_SURFACE()) + ";" +
            "-fx-text-fill: " + (COLOR_TEXT()) + ";" +
            "-fx-prompt-text-fill: " + (COLOR_TEXT_MUTED()) + ";" +
            "-fx-border-color: " + (COLOR_BORDER()) + ";" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 6 12;" +
            "-fx-font-size: 12px;"
        );
        
        camposBusqueda.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                camposBusqueda.setStyle(
                    "-fx-background-color: " + (COLOR_BG_LIGHT()) + ";" +
                    "-fx-text-fill: " + (COLOR_TEXT()) + ";" +
                    "-fx-prompt-text-fill: " + (COLOR_TEXT_MUTED()) + ";" +
                    "-fx-border-color: " + COLOR_PRIMARY + ";" +
                    "-fx-border-radius: 6;" +
                    "-fx-background-radius: 6;" +
                    "-fx-padding: 6 12;" +
                    "-fx-font-size: 12px;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.25), 6, 0, 0, 2);"
                );
            } else {
                camposBusqueda.setStyle(
                    "-fx-background-color: " + (COLOR_SURFACE()) + ";" +
                    "-fx-text-fill: " + (COLOR_TEXT()) + ";" +
                    "-fx-prompt-text-fill: " + (COLOR_TEXT_MUTED()) + ";" +
                    "-fx-border-color: " + (COLOR_BORDER()) + ";" +
                    "-fx-border-radius: 6;" +
                    "-fx-background-radius: 6;" +
                    "-fx-padding: 6 12;" +
                    "-fx-font-size: 12px;"
                );
            }
        });
        
        // Botón Filtros con icono
        HBox btnFiltros = new HBox(8);
        btnFiltros.setAlignment(Pos.CENTER);
        btnFiltros.setPadding(new Insets(8, 16, 8, 12));
        btnFiltros.setCursor(javafx.scene.Cursor.HAND);
        btnFiltros.setStyle(
            "-fx-background-color: " + (COLOR_SURFACE()) + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 8;"
        );
        Node iconoFiltro = IconosSVG.filtro(COLOR_PRIMARY, 14);
        Label lblFiltros = new Label("Filtros");
        lblFiltros.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblFiltros.setTextFill(Color.web(COLOR_PRIMARY));
        btnFiltros.getChildren().addAll(iconoFiltro, lblFiltros);
        
        // Botón Limpiar filtros
        HBox btnLimpiar = new HBox(8);
        btnLimpiar.setAlignment(Pos.CENTER);
        btnLimpiar.setPadding(new Insets(8, 16, 8, 12));
        btnLimpiar.setCursor(javafx.scene.Cursor.HAND);
        btnLimpiar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 8;"
        );
        Label lblLimpiar = new Label("Limpiar");
        lblLimpiar.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblLimpiar.setTextFill(Color.web(COLOR_TEXT_MUTED()));
        btnLimpiar.getChildren().add(lblLimpiar);
        
        // Contador de filtros activos
        Label lblFiltrosActivos = new Label("");
        lblFiltrosActivos.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        lblFiltrosActivos.setTextFill(Color.web(COLOR_PRIMARY));
        lblFiltrosActivos.setVisible(false);
        
        // Espaciador
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
        
        // Label de contador de resultados
        Label lblResultados = new Label(datosOriginales.size() + " registros");
        lblResultados.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblResultados.setTextFill(Color.web(COLOR_TEXT_MUTED()));
        
        barraPrincipal.getChildren().addAll(camposBusqueda, btnFiltros, btnLimpiar, lblFiltrosActivos, espaciador, lblResultados);

        // ========== PANEL DE FILTROS EXPANDIBLE (múltiples filas) ==========
        VBox contenedorFiltros = new VBox(12);
        contenedorFiltros.setPadding(new Insets(14, 16, 14, 16));
        contenedorFiltros.setStyle(
            "-fx-background-color: " + (COLOR_SURFACE()) + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + (COLOR_BORDER()) + ";" +
            "-fx-border-radius: 10;"
        );
        contenedorFiltros.setVisible(false);
        contenedorFiltros.setManaged(false);
        
        // Lista de ComboBoxes para filtros
        List<ComboBox<String>> combosFiltros = new ArrayList<>();
        
        // Crear filas de filtros (4 filtros por fila)
        int filtrosPorFila = 4;
        int numFilas = (int) Math.ceil((double) maxFiltros / filtrosPorFila);
        
        for (int fila = 0; fila < numFilas; fila++) {
            HBox filaFiltros = new HBox(12);
            filaFiltros.setAlignment(Pos.CENTER_LEFT);
            
            int inicio = fila * filtrosPorFila;
            int fin = Math.min(inicio + filtrosPorFila, maxFiltros);
            int filtrosEnFila = fin - inicio;
            
            for (int i = inicio; i < fin; i++) {
                final int indice = i;
                String nombreColumna = encabezados.get(i);
                
                // Contenedor del filtro con label
                VBox filtroContainer = new VBox(3);
                filtroContainer.setAlignment(Pos.TOP_LEFT);
                HBox.setHgrow(filtroContainer, Priority.ALWAYS);
                
                // Label del filtro
                Label lblFiltro = new Label(nombreColumna.length() > 20 ? nombreColumna.substring(0, 20) + "..." : nombreColumna);
                lblFiltro.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 10));
                lblFiltro.setTextFill(Color.web(COLOR_TEXT_MUTED()));
                
                ComboBox<String> combo = new ComboBox<>();
                String textoTodos = "Todos";
                combo.getItems().add(textoTodos);
                combo.getItems().addAll(valoresUnicos.get(i));
                combo.setValue(textoTodos);
                combo.setMaxWidth(Double.MAX_VALUE);
                combo.setPrefHeight(32);
                combo.getStyleClass().add("filter-combo");
                combo.setStyle(
                    "-fx-background-color: " + (COLOR_SURFACE()) + ";" +
                    "-fx-background-radius: 6;" +
                    "-fx-border-color: " + (COLOR_BORDER()) + ";" +
                    "-fx-border-radius: 6;" +
                    "-fx-text-fill: " + (COLOR_TEXT()) + ";" +
                    "-fx-font-size: 11px;"
                );
                
                // Guardar referencia al texto "Todos" y al índice
                combo.setUserData(new Object[]{textoTodos, indice});
                combosFiltros.add(combo);
                
                filtroContainer.getChildren().addAll(lblFiltro, combo);
                filaFiltros.getChildren().add(filtroContainer);
            }
            
            // Si la fila no está completa, añadir espaciadores para mantener alineación
            if (filtrosEnFila < filtrosPorFila) {
                for (int j = filtrosEnFila; j < filtrosPorFila; j++) {
                    Region espaciadorFila = new Region();
                    HBox.setHgrow(espaciadorFila, Priority.ALWAYS);
                    filaFiltros.getChildren().add(espaciadorFila);
                }
            }
            
            contenedorFiltros.getChildren().add(filaFiltros);
        }

        // ========== CREAR TABLA CON CONTENEDOR PARA BADGES ==========
        
        // Contenedor principal con tabla y badges laterales
        StackPane tablaContenedor = new StackPane();
        VBox.setVgrow(tablaContenedor, Priority.ALWAYS);
        
        TableView<ObservableList<String>> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tabla.getStyleClass().addAll("elegant-table", "table-view");
        tabla.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;"
        );
        
        // Placeholder estilizado cuando la tabla no tiene filas
        tabla.setPlaceholder(crearPlaceholderTablaVacia(
            IconosSVG.computadora(COLOR_TEXT_SECONDARY(), 44),
            "Sin registros",
            "No se encontraron datos para mostrar"
        ));
        
        // Pane para badges laterales
        Pane badgesPane = new Pane();
        badgesPane.setMouseTransparent(true); // No interceptar clicks
        badgesPane.setStyle("-fx-background-color: transparent;");
        
        // Agregar tabla y badges al contenedor
        tablaContenedor.getChildren().addAll(tabla, badgesPane);
        StackPane.setAlignment(badgesPane, Pos.TOP_LEFT);

        // Crear columnas con iconos personalizados
        for (int i = 0; i < encabezados.size(); i++) {
            final int colIndex = i;
            String headerName = encabezados.get(i);
            String headerLower = headerName.toLowerCase();
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(headerName);
            col.setCellValueFactory(param -> {
                ObservableList<String> row = param.getValue();
                if (colIndex < row.size()) {
                    return new SimpleStringProperty(row.get(colIndex));
                }
                return new SimpleStringProperty("");
            });
            
            // Determinar tipo de columna para iconos
            final String tipoColumna = determinarTipoColumna(headerLower);
            
            // Cell factory con iconos
            col.setCellFactory(column -> new TableCell<ObservableList<String>, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.isEmpty()) {
                        setGraphic(null);
                        setText(null);
                        setStyle("");
                    } else {
                        setStyle("");
                        HBox contenido = crearCeldaConIcono(item, tipoColumna);
                        setGraphic(contenido);
                        setText(null);
                    }
                }
            });
            
            int ancho = calcularAnchoColumna(headerName);
            col.setPrefWidth(ancho);
            col.setMinWidth(ancho);
            col.getStyleClass().add("elegant-column");
            tabla.getColumns().add(col);
        }

        // FilteredList para filtrado dinámico
        FilteredList<ObservableList<String>> datosFiltrados = new FilteredList<>(datosOriginales, p -> true);
        
        // Lógica de filtrado
        Runnable aplicarFiltro = () -> {
            datosFiltrados.setPredicate(fila -> {
                // ===== FILTRO DE BÚSQUEDA GLOBAL =====
                String textoBusqueda = camposBusqueda.getText();
                if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
                    String busquedaLower = textoBusqueda.toLowerCase();
                    boolean coincideGlobal = false;
                    
                    // Buscar en todas las columnas de la fila
                    for (String valorCelda : fila) {
                        if (valorCelda != null && valorCelda.toLowerCase().contains(busquedaLower)) {
                            coincideGlobal = true;
                            break;
                        }
                    }
                    
                    if (!coincideGlobal) {
                        return false;
                    }
                }
                
                // ===== FILTROS POR COLUMNA =====
                for (ComboBox<String> combo : combosFiltros) {
                    Object[] userData = (Object[]) combo.getUserData();
                    String textoTodos = (String) userData[0];
                    int indice = (int) userData[1];
                    String valorSeleccionado = combo.getValue();
                    
                    if (!valorSeleccionado.equals(textoTodos)) {
                        if (indice < fila.size() && !fila.get(indice).equals(valorSeleccionado)) {
                            return false;
                        }
                    }
                }
                return true;
            });
            
            // Contar filtros activos
            int activos = 0;
            for (ComboBox<String> combo : combosFiltros) {
                Object[] userData = (Object[]) combo.getUserData();
                String textoTodos = (String) userData[0];
                if (!combo.getValue().equals(textoTodos)) {
                    activos++;
                }
            }
            
            // Agregar filtro de búsqueda a la cuenta si está activo
            if (camposBusqueda.getText() != null && !camposBusqueda.getText().trim().isEmpty()) {
                activos++;
            }
            
            // Mostrar contador solo si hay filtros activos
            if (activos > 0) {
                lblFiltrosActivos.setText("(" + activos + ")");
                lblFiltrosActivos.setVisible(true);
            } else {
                lblFiltrosActivos.setVisible(false);
            }
            
            // Actualizar contador con colores dinámicos
            int resultados = datosFiltrados.size();
            if (resultados == datosOriginales.size()) {
                lblResultados.setText(resultados + " registros");
            } else {
                lblResultados.setText(resultados + " de " + datosOriginales.size() + " registros");
            }
            lblResultados.setTextFill(Color.web(resultados == 0 ? TemaManager.COLOR_DANGER_HOVER : (COLOR_TEXT_MUTED())));
        };
        
        // Listeners para filtrado
        for (ComboBox<String> combo : combosFiltros) {
            combo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro.run());
        }
        
        // Listener para búsqueda inteligente
        camposBusqueda.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro.run());
        
        // Botón limpiar - resetea todos los filtros incluyendo búsqueda
        btnLimpiar.setOnMouseClicked(e -> {
            camposBusqueda.clear(); // Limpiar búsqueda
            for (ComboBox<String> combo : combosFiltros) {
                Object[] userData = (Object[]) combo.getUserData();
                combo.setValue((String) userData[0]);
            }
        });
        
        // Hover en botón limpiar
        btnLimpiar.setOnMouseEntered(e -> {
            btnLimpiar.setStyle(
                "-fx-background-color: " + TemaManager.getSurfaceHover() + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 8;"
            );
            lblLimpiar.setTextFill(Color.web(COLOR_TEXT_MUTED()));
        });
        btnLimpiar.setOnMouseExited(e -> {
            btnLimpiar.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 8;"
            );
            lblLimpiar.setTextFill(Color.web(COLOR_TEXT_MUTED()));
        });
        
        // Toggle del panel de filtros
        btnFiltros.setOnMouseClicked(e -> {
            boolean visible = !contenedorFiltros.isVisible();
            contenedorFiltros.setVisible(visible);
            contenedorFiltros.setManaged(visible);
            
            // Cambiar estilo del botón cuando está activo
            if (visible) {
                btnFiltros.setStyle(
                    "-fx-background-color: " + COLOR_PRIMARY + "20;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: " + COLOR_PRIMARY + ";" +
                    "-fx-border-radius: 8;"
                );
            } else {
                btnFiltros.setStyle(
                    "-fx-background-color: " + (COLOR_SURFACE()) + ";" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: " + COLOR_BORDER() + ";" +
                    "-fx-border-radius: 8;"
                );
            }
        });
        
        // Hover en botones
        btnFiltros.setOnMouseEntered(e -> {
            if (!contenedorFiltros.isVisible()) {
                btnFiltros.setStyle(
                    "-fx-background-color: " + TemaManager.getSurfaceHover() + ";" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: " + COLOR_BORDER() + ";" +
                    "-fx-border-radius: 8;"
                );
            }
        });
        btnFiltros.setOnMouseExited(e -> {
            if (!contenedorFiltros.isVisible()) {
                btnFiltros.setStyle(
                    "-fx-background-color: " + (COLOR_SURFACE()) + ";" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: " + COLOR_BORDER() + ";" +
                    "-fx-border-radius: 8;"
                );
            }
        });

        // Aplicar lista ordenada
        SortedList<ObservableList<String>> datosOrdenados = new SortedList<>(datosFiltrados);
        datosOrdenados.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(datosOrdenados);

        // 🔑 HABILITAR SELECCIÓN DE CELDAS INDIVIDUALES
        tabla.getSelectionModel().setCellSelectionEnabled(true);
        tabla.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        
        // 📋 Atajos de teclado para la tabla
        tabla.setOnKeyPressed(event -> {
            // Ctrl+A para seleccionar todo
            if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.A) {
                tabla.getSelectionModel().selectAll();
                event.consume();
            }
            // Ctrl+C para copiar selección de celdas al portapapeles
            else if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.C) {
                copiarCeldasSeleccionadas(tabla, tablaContenedor);
                event.consume();
            }
        });

        // ========== MENÚ CONTEXTUAL PARA EDITAR/ELIMINAR (SOLO MODO ADMIN) ==========
        tabla.setRowFactory(tv -> {
            TableRow<ObservableList<String>> row = new TableRow<>();
            
            // Solo mostrar menú contextual si está en modo admin
            if (AdminManager.isAdminMode()) {
                ContextMenu contextMenu = new ContextMenu();
                contextMenu.setStyle(
                    "-fx-background-color: " + COLOR_SURFACE() + ";" +
                    "-fx-border-color: " + COLOR_BORDER() + ";" +
                    "-fx-border-width: 1;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;"
                );
                
                MenuItem editarItem = new MenuItem("Editar registro");
                editarItem.setGraphic(IconosSVG.editar(COLOR_PRIMARY, 14));
                editarItem.setStyle("-fx-text-fill: " + COLOR_TEXT() + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px;");
                editarItem.setOnAction(e -> {
                    ObservableList<String> filaSeleccionada = row.getItem();
                    if (filaSeleccionada != null) {
                        editarRegistro(rutaExcel, hoja, encabezados, filaSeleccionada, row.getIndex() + 2, tabla); // +2 porque Excel empieza en 1 y hay header
                    }
                });
                
                // 🗑️ Eliminar solo este registro (fila individual)
                MenuItem eliminarUnoItem = new MenuItem("Eliminar este registro");
                eliminarUnoItem.setGraphic(IconosSVG.eliminar(TemaManager.COLOR_DANGER, 14));
                eliminarUnoItem.setStyle("-fx-text-fill: " + COLOR_TEXT() + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px;");
                eliminarUnoItem.setOnAction(e -> {
                    ObservableList<String> filaSeleccionada = row.getItem();
                    if (filaSeleccionada != null) {
                        eliminarRegistroIndividual(rutaExcel, hoja, datosOriginales, filaSeleccionada, row.getIndex() + 2);
                    }
                });
                
                // 🗑️ Eliminar múltiples registros seleccionados
                MenuItem eliminarSeleccionadosItem = new MenuItem("Eliminar seleccionados");
                eliminarSeleccionadosItem.setGraphic(IconosSVG.eliminar(TemaManager.COLOR_DANGER, 14));
                eliminarSeleccionadosItem.setStyle("-fx-text-fill: " + COLOR_TEXT() + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px;");
                eliminarSeleccionadosItem.setOnAction(e -> {
                    ObservableList<ObservableList<String>> filasSeleccionadas = tabla.getSelectionModel().getSelectedItems();
                    if (filasSeleccionadas != null && !filasSeleccionadas.isEmpty()) {
                        eliminarRegistrosMultiples(rutaExcel, hoja, datosOriginales, new ArrayList<>(filasSeleccionadas));
                    }
                });
                
                // 🗑️ Eliminar todo el grupo de aplicaciones del equipo
                MenuItem eliminarTodoGrupoItem = new MenuItem("Eliminar todo el equipo");
                eliminarTodoGrupoItem.setGraphic(IconosSVG.eliminar(TemaManager.COLOR_DANGER, 14));
                eliminarTodoGrupoItem.setStyle("-fx-text-fill: " + TemaManager.COLOR_DANGER + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: bold;");
                eliminarTodoGrupoItem.setOnAction(e -> {
                    ObservableList<String> filaSeleccionada = row.getItem();
                    if (filaSeleccionada != null) {
                        eliminarRegistro(rutaExcel, hoja, datosOriginales, filaSeleccionada, row.getIndex() + 2);
                    }
                });
                
                contextMenu.getItems().addAll(
                    editarItem, 
                    new SeparatorMenuItem(),
                    eliminarUnoItem, 
                    eliminarSeleccionadosItem,
                    new SeparatorMenuItem(),
                    eliminarTodoGrupoItem
                );
                
                row.setOnContextMenuRequested(e -> {
                    if (!row.isEmpty()) {
                        contextMenu.show(row, e.getScreenX(), e.getScreenY());
                    }
                });
            }
            
            // Hover y selección manejados por CSS (.table-row-cell:hover / :selected)
            // No usar estilos inline para no sobreescribir el CSS
            
            return row;
        });
        
        // 🎨 CREAR BADGES LATERALES PARA MARCADORES
        final javafx.animation.Timeline[] scrollTimer = {null};
        final boolean[] primeraActualizacion = {true}; // Flag para forzar actualización inicial
        
        // Agregar listener para detectar cambios en las filas visibles y mostrar badges
        tabla.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                actualizarBadges(tabla, badgesPane, datosOriginales, encabezados);
                
                // 🎯 CREAR BADGE INICIAL INMEDIATAMENTE
                javafx.animation.PauseTransition pausaBadge = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
                pausaBadge.setOnFinished(ev -> actualizarBadgesSuave(tabla, badgesPane, datosOriginales, encabezados));
                pausaBadge.play();
                
                // 🔄 LISTENER DE SCROLL OPTIMIZADO - Actualización inmediata con verificación final
                try {
                    javafx.scene.Node scrollBar = tabla.lookup(".scroll-bar:vertical");
                    if (scrollBar != null && scrollBar instanceof javafx.scene.control.ScrollBar) {
                        ((javafx.scene.control.ScrollBar) scrollBar).valueProperty().addListener((o, oldVal, newVal) -> {
                            // ✅ ACTUALIZACIÓN INMEDIATA - Garantiza precisión en scroll rápido
                            actualizarBadgesSuave(tabla, badgesPane, datosOriginales, encabezados);
                            
                            // Cancelar timer anterior si existe
                            if (scrollTimer[0] != null) {
                                scrollTimer[0].stop();
                            }
                            
                            // Timer de verificación final cuando se detiene el scroll
                            scrollTimer[0] = new javafx.animation.Timeline(new javafx.animation.KeyFrame(
                                Duration.millis(100),
                                e -> actualizarBadgesSuave(tabla, badgesPane, datosOriginales, encabezados)
                            ));
                            scrollTimer[0].setCycleCount(1);
                            scrollTimer[0].play();
                        });
                    }
                } catch (Exception e) {
                    System.err.println("No se pudo agregar listener de scroll: " + e.getMessage());
                }
            }
        });
        
        // Listener para actualizar badges cuando cambian los datos (con delay para asegurar renderizado)
        tabla.getItems().addListener((javafx.collections.ListChangeListener<ObservableList<String>>) c -> {
            Platform.runLater(() -> {
                // Actualización inmediata
                actualizarBadges(tabla, badgesPane, datosOriginales, encabezados);
                
                // Segunda actualización con delay por si el renderizado es lento
                javafx.animation.Timeline delayedUpdate = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), e -> 
                        actualizarBadges(tabla, badgesPane, datosOriginales, encabezados)
                    )
                );
                delayedUpdate.play();
            });
        });

        contenido.getChildren().addAll(barraPrincipal, contenedorFiltros, tablaContenedor);
        return contenido;
    }
    
    /**
     * Clase auxiliar para almacenar información de badges
     */
    private static class BadgeInfo {
        int fila;
        boolean esInicio;
        String idGrupo;
        int numeroGrupo;
        
        BadgeInfo(int fila, boolean esInicio, String idGrupo, int numeroGrupo) {
            this.fila = fila;
            this.esInicio = esInicio;
            this.idGrupo = idGrupo;
            this.numeroGrupo = numeroGrupo;
        }
    }
    
    /**
     * Dibuja líneas conectoras verticales entre badges del mismo grupo
     */
    private static void dibujarLineasConectoras(Pane badgesPane, TableView<?> tabla, java.util.List<BadgeInfo> badgesInfo) {
        // Agrupar badges por idGrupo
        java.util.Map<String, BadgeInfo> inicios = new java.util.HashMap<>();
        java.util.Map<String, BadgeInfo> fines = new java.util.HashMap<>();
        
        for (BadgeInfo info : badgesInfo) {
            if (info.esInicio) {
                inicios.put(info.idGrupo, info);
            } else {
                fines.put(info.idGrupo, info);
            }
        }
        
        // Dibujar línea para cada par INICIO-FIN
        for (String idGrupo : inicios.keySet()) {
            if (fines.containsKey(idGrupo)) {
                BadgeInfo inicio = inicios.get(idGrupo);
                BadgeInfo fin = fines.get(idGrupo);
                
                // Solo dibujar si hay espacio entre ellos
                if (fin.fila > inicio.fila + 1) {
                    double y1 = calcularPosicionFila(tabla, inicio.fila) + 20; // Centro del badge
                    double y2 = calcularPosicionFila(tabla, fin.fila) + 20;
                    
                    if (y1 > 0 && y2 > y1) {
                        // Línea vertical punteada
                        javafx.scene.shape.Line linea = new javafx.scene.shape.Line(-8, y1, -8, y2);
                        linea.setStroke(Color.web("#4caf50", 0.3));
                        linea.setStrokeWidth(2);
                        linea.getStrokeDashArray().addAll(5.0, 5.0);
                        linea.setMouseTransparent(true);
                        
                        // Agregar al fondo (detrás de badges)
                        badgesPane.getChildren().add(0, linea);
                    }
                }
            }
        }
    }
    
    /**
     * Actualiza los badges laterales para mostrar el grupo actual flotante
     */
    private static void actualizarBadges(TableView<ObservableList<String>> tabla, Pane badgesPane, 
                                        ObservableList<ObservableList<String>> datosOriginales,
                                        List<String> encabezados) {
        actualizarBadgesSuave(tabla, badgesPane, datosOriginales, encabezados);
    }
    
    /**
     * Actualiza el badge flotante que muestra el grupo actual visible.
     * Detecta la columna "ID Grupo" dinámicamente y determina qué grupo 
     * está visible en la primera fila de la tabla.
     */
    private static void actualizarBadgesSuave(TableView<ObservableList<String>> tabla, Pane badgesPane,
                                             ObservableList<ObservableList<String>> datosOriginales,
                                             List<String> encabezados) {
        try {
            // ══════════════════════════════════════════════════════════════
            // 1. BUSCAR COLUMNA "ID Grupo" DINÁMICAMENTE
            // ══════════════════════════════════════════════════════════════
            int indiceIdGrupo = -1;
            for (int i = 0; i < encabezados.size(); i++) {
                String enc = encabezados.get(i).trim();
                // Buscar variantes: "ID Grupo", "Id Grupo", "IdGrupo", "ID_Grupo"
                if (enc.equalsIgnoreCase("ID Grupo") || enc.equalsIgnoreCase("IdGrupo") 
                    || enc.equalsIgnoreCase("ID_Grupo") || enc.toLowerCase().startsWith("id grupo")) {
                    indiceIdGrupo = i;
                    break;
                }
            }
            
            // Si no hay columna de grupo, ocultar badge y salir (es la hoja SystemInfo)
            if (indiceIdGrupo == -1) {
                badgesPane.getChildren().clear();
                return;
            }
            
            // ══════════════════════════════════════════════════════════════
            // 2. DETECTAR PRIMERA FILA VISIBLE
            // ══════════════════════════════════════════════════════════════
            int primeraFilaVisible = -1;
            
            // Intentar con VirtualFlow primero (más confiable)
            try {
                VirtualFlow<?> vf = (VirtualFlow<?>) tabla.lookup(".virtual-flow");
                if (vf != null && vf.getFirstVisibleCell() != null) {
                    Object cell = vf.getFirstVisibleCell();
                    if (cell instanceof javafx.scene.control.IndexedCell) {
                        primeraFilaVisible = ((javafx.scene.control.IndexedCell<?>) cell).getIndex();
                    }
                }
            } catch (Exception ignored) {}
            
            // Fallback: recorrer table-row-cell
            if (primeraFilaVisible == -1) {
                java.util.Set<javafx.scene.Node> nodes = tabla.lookupAll(".table-row-cell");
                for (javafx.scene.Node n : nodes) {
                    if (n instanceof javafx.scene.control.TableRow) {
                        javafx.scene.control.TableRow<?> row = (javafx.scene.control.TableRow<?>) n;
                        if (!row.isEmpty() && row.getIndex() >= 0) {
                            if (primeraFilaVisible == -1 || row.getIndex() < primeraFilaVisible) {
                                primeraFilaVisible = row.getIndex();
                            }
                        }
                    }
                }
            }
            
            if (primeraFilaVisible < 0 || primeraFilaVisible >= tabla.getItems().size()) return;
            
            // ══════════════════════════════════════════════════════════════
            // 3. MAPEAR TODOS LOS GRUPOS (ID → número secuencial)
            // ══════════════════════════════════════════════════════════════
            java.util.LinkedHashMap<String, Integer> grupoNumero = new java.util.LinkedHashMap<>();
            java.util.Map<String, Integer> grupoTamanio = new java.util.HashMap<>();
            
            int contador = 0;
            for (int i = 0; i < tabla.getItems().size(); i++) {
                ObservableList<String> fila = tabla.getItems().get(i);
                if (fila.size() <= indiceIdGrupo) continue;
                String id = fila.get(indiceIdGrupo);
                if (id == null || id.trim().isEmpty()) continue;
                
                if (!grupoNumero.containsKey(id)) {
                    contador++;
                    grupoNumero.put(id, contador);
                    grupoTamanio.put(id, 0);
                }
                grupoTamanio.put(id, grupoTamanio.get(id) + 1);
            }
            
            if (grupoNumero.isEmpty()) {
                badgesPane.getChildren().clear();
                return;
            }
            
            // ══════════════════════════════════════════════════════════════
            // 4. DETERMINAR GRUPO DE LA PRIMERA FILA VISIBLE
            // ══════════════════════════════════════════════════════════════
            ObservableList<String> filaVisible = tabla.getItems().get(primeraFilaVisible);
            if (filaVisible.size() <= indiceIdGrupo) return;
            
            String idGrupoActual = filaVisible.get(indiceIdGrupo);
            if (idGrupoActual == null || idGrupoActual.trim().isEmpty()) return;
            
            Integer numGrupo = grupoNumero.get(idGrupoActual);
            Integer totalApps = grupoTamanio.get(idGrupoActual);
            if (numGrupo == null) return;
            if (totalApps == null) totalApps = 0;
            
            // ══════════════════════════════════════════════════════════════
            // 5. ACTUALIZAR O CREAR BADGE FLOTANTE
            // ══════════════════════════════════════════════════════════════
            
            // Buscar badge flotante existente
            HBox badgeFlotante = null;
            for (javafx.scene.Node n : badgesPane.getChildren()) {
                if (n instanceof HBox && n.getUserData() != null 
                    && n.getUserData().toString().startsWith("FLOTANTE")) {
                    badgeFlotante = (HBox) n;
                    break;
                }
            }
            
            // Limpiar nodos que no sean el badge flotante
            badgesPane.getChildren().removeIf(n -> {
                if (n instanceof HBox && n.getUserData() != null 
                    && n.getUserData().toString().startsWith("FLOTANTE")) {
                    return false;
                }
                return true;
            });
            
            boolean esNuevo = (badgeFlotante == null);
            if (esNuevo) {
                badgeFlotante = new HBox(8);
                badgeFlotante.setAlignment(Pos.CENTER);
                badgeFlotante.setPadding(new Insets(8, 16, 8, 16));
                badgeFlotante.setLayoutX(-15);
                badgeFlotante.setLayoutY(35);
                badgeFlotante.setMouseTransparent(false);
                badgeFlotante.setUserData("FLOTANTE::0");
                badgesPane.getChildren().add(badgeFlotante);
            }
            
            // Verificar si cambió el grupo
            String datosAnterior = badgeFlotante.getUserData().toString();
            String clave = "FLOTANTE:" + idGrupoActual + ":" + numGrupo;
            
            if (!datosAnterior.equals(clave)) {
                badgeFlotante.setUserData(clave);
                badgeFlotante.getChildren().clear();
                
                // Colores del badge adaptados al tema
                String badgeBg, badgeBorder, badgeShadow, textoColor;
                if (TemaManager.isDarkMode()) {
                    badgeBg = "linear-gradient(to right, #1a3d0a, #2d5016, #3d6020)";
                    badgeBorder = "#4caf50";
                    badgeShadow = "rgba(76, 175, 80, 0.8)";
                    textoColor = "#a0ff90";
                } else {
                    badgeBg = "linear-gradient(to right, #1A56DB, #3B82F6, #2563EB)";
                    badgeBorder = "#1A56DB";
                    badgeShadow = "rgba(26, 86, 219, 0.45)";
                    textoColor = "#FFFFFF";
                }
                
                badgeFlotante.setStyle(
                    "-fx-background-color: " + badgeBg + ";" +
                    "-fx-background-radius: 0 25 25 0;" +
                    "-fx-border-color: " + badgeBorder + ";" +
                    "-fx-border-width: 2 2 2 0;" +
                    "-fx-border-radius: 0 25 25 0;" +
                    "-fx-effect: dropshadow(gaussian, " + badgeShadow + ", 12, 0.3, 2, 2);" +
                    "-fx-cursor: hand;"
                );
                
                Node iconoSvg = IconosSVG.paquete(textoColor, 18);
                Label texto = new Label("GRUPO " + numGrupo);
                texto.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                texto.setTextFill(Color.web(textoColor));
                
                badgeFlotante.getChildren().addAll(iconoSvg, texto);
                badgeFlotante.setVisible(true);
                
                // Tooltip con info del grupo
                Tooltip tooltip = new Tooltip(
                    "Grupo " + numGrupo + "\n" +
                    "Aplicaciones: " + totalApps + "\n" +
                    "ID: " + (idGrupoActual.length() > 20 ? idGrupoActual.substring(0, 20) + "..." : idGrupoActual)
                );
                tooltip.setStyle(
                    "-fx-background-color: #2d3436; -fx-text-fill: #dfe6e9;" +
                    "-fx-font-size: 12px; -fx-padding: 8px; -fx-background-radius: 5px;"
                );
                tooltip.setShowDelay(Duration.millis(300));
                Tooltip.install(badgeFlotante, tooltip);
                
                // Animación de transición
                if (!esNuevo) {
                    FadeTransition fade = new FadeTransition(Duration.millis(200), badgeFlotante);
                    fade.setFromValue(0.5);
                    fade.setToValue(1.0);
                    
                    ScaleTransition pulse = new ScaleTransition(Duration.millis(200), badgeFlotante);
                    pulse.setFromX(0.95); pulse.setToX(1.0);
                    pulse.setFromY(0.95); pulse.setToY(1.0);
                    pulse.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                    
                    new ParallelTransition(fade, pulse).play();
                } else {
                    badgeFlotante.setOpacity(0);
                    badgeFlotante.setScaleX(0.8);
                    badgeFlotante.setScaleY(0.8);
                    
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), badgeFlotante);
                    fadeIn.setToValue(1.0);
                    
                    ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), badgeFlotante);
                    scaleIn.setToX(1.0);
                    scaleIn.setToY(1.0);
                    scaleIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                    
                    new ParallelTransition(fadeIn, scaleIn).play();
                }
            }
            
        } catch (Exception e) {
            // Silenciar errores para no afectar performance
            System.err.println("[Dashboard] Error actualizando badge grupo: " + e.getMessage());
        }
    }
    
    /**
     * Calcula la posición Y de una fila en la tabla
     */
    private static double calcularPosicionFila(TableView<?> tabla, int indiceFila) {
        try {
            java.util.Set<javafx.scene.Node> nodes = tabla.lookupAll(".table-row-cell");
            if (nodes != null) {
                for (javafx.scene.Node n : nodes) {
                    if (n instanceof javafx.scene.control.TableRow) {
                        javafx.scene.control.TableRow<?> row = (javafx.scene.control.TableRow<?>) n;
                        if (row.getIndex() == indiceFila && !row.isEmpty()) {
                            return row.getLayoutY() + 2;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Fallback
        }
        // Fallback: cálculo estimado
        return 32 + (indiceFila * 35);
    }
    
    /**
     * Crea un badge lateral flotante para marcar inicio o fin de grupo
     */
    private static void crearBadgeLateral(Pane badgesPane, TableView<?> tabla, int indiceFila, boolean esInicio, 
                                         int numeroGrupo, int totalApps, String idGrupo) {
        Platform.runLater(() -> {
            try {
                // 🎯 CALCULAR POSICIÓN REAL DE LA FILA VISIBLE
                // Buscar la TableRow real correspondiente a este índice
                javafx.scene.Node node = null;
                java.util.Set<javafx.scene.Node> nodes = tabla.lookupAll(".table-row-cell");
                if (nodes == null || nodes.isEmpty()) {
                    // Fallback: usar posición calculada
                    double altoFila = 35;
                    double yOffset = 32 + (indiceFila * altoFila);
                    crearBadgeEnPosicion(badgesPane, yOffset, esInicio, indiceFila, numeroGrupo, totalApps, idGrupo);
                    return;
                }
                
                for (javafx.scene.Node n : nodes) {
                    if (n instanceof javafx.scene.control.TableRow) {
                        javafx.scene.control.TableRow<?> row = (javafx.scene.control.TableRow<?>) n;
                        if (row.getIndex() == indiceFila && !row.isEmpty()) {
                            node = n;
                            break;
                        }
                    }
                }
                
                // Si no se encuentra la fila visible, usar posición calculada
                if (node == null) {
                    double altoFila = 35;
                    double yOffset = 32 + (indiceFila * altoFila);
                    crearBadgeEnPosicion(badgesPane, yOffset, esInicio, indiceFila, numeroGrupo, totalApps, idGrupo);
                    return;
                }
                
                double yOffset = node.getLayoutY() + 2; // Posición real de la fila + ajuste
                crearBadgeEnPosicion(badgesPane, yOffset, esInicio, indiceFila, numeroGrupo, totalApps, idGrupo);
                
            } catch (Exception e) {
                // Fallback seguro
                try {
                    double altoFila = 35;
                    double yOffset = 32 + (indiceFila * altoFila);
                    crearBadgeEnPosicion(badgesPane, yOffset, esInicio, indiceFila, numeroGrupo, totalApps, idGrupo);
                } catch (Exception ex) {
                    // Ignorar completamente si falla
                }
            }
        });
    }
    
    /**
     * Crea el badge visual en la posición especificada
     */
    private static void crearBadgeEnPosicion(Pane badgesPane, double yOffset, boolean esInicio) {
        crearBadgeEnPosicion(badgesPane, yOffset, esInicio, -1, 1, 1, "");
    }
    
    /**
     * Crea el badge visual en la posición especificada con metadatos
     */
    private static void crearBadgeEnPosicion(Pane badgesPane, double yOffset, boolean esInicio, int indiceFila) {
        crearBadgeEnPosicion(badgesPane, yOffset, esInicio, indiceFila, 1, 1, "");
    }
    
    /**
     * Crea el badge visual en la posición especificada con información completa del grupo
     */
    private static void crearBadgeEnPosicion(Pane badgesPane, double yOffset, boolean esInicio, int indiceFila,
                                            int numeroGrupo, int totalApps, String idGrupo) {
        try {
            // Crear badge MÁS GRANDE Y VISIBLE
            HBox badge = new HBox(8);
            badge.setAlignment(Pos.CENTER);
            badge.setPadding(new Insets(8, 16, 8, 16));
            badge.setLayoutX(-15);
            badge.setLayoutY(yOffset);
            
            // Guardar metadatos para actualización suave (tipo:fila:numeroGrupo:totalApps:idGrupo)
            if (indiceFila >= 0) {
                String metadatos = (esInicio ? "INICIO" : "FIN") + ":" + indiceFila + ":" + numeroGrupo + ":" + totalApps + ":" + idGrupo;
                badge.setUserData(metadatos);
            }
            
            if (esInicio) {
                // Badge de GRUPO (verde) - adaptado al tema
                String grpBg, grpBorder, grpShadow, grpTextoColor;
                if (TemaManager.isDarkMode()) {
                    grpBg = "linear-gradient(to right, #1a3d0a, #2d5016, #3d6020)";
                    grpBorder = "#4caf50";
                    grpShadow = "rgba(76, 175, 80, 0.8)";
                    grpTextoColor = "#a0ff90";
                } else {
                    grpBg = "linear-gradient(to right, #1A56DB, #3B82F6, #2563EB)";
                    grpBorder = "#1A56DB";
                    grpShadow = "rgba(26, 86, 219, 0.45)";
                    grpTextoColor = "#FFFFFF";
                }
                badge.setStyle(
                    "-fx-background-color: " + grpBg + ";" +
                    "-fx-background-radius: 0 25 25 0;" +
                    "-fx-border-color: " + grpBorder + ";" +
                    "-fx-border-width: 2 2 2 0;" +
                    "-fx-border-radius: 0 25 25 0;" +
                    "-fx-effect: dropshadow(gaussian, " + grpShadow + ", 12, 0.3, 2, 2);" +
                    "-fx-cursor: hand;"
                );
                
                Node icono = IconosSVG.paquete(grpTextoColor, 18);
                
                Label texto = new Label("GRUPO " + numeroGrupo);
                texto.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                texto.setTextFill(Color.web(grpTextoColor));
                
                badge.getChildren().addAll(icono, texto);
                
                // 💡 TOOLTIP con información del grupo
                Tooltip tooltip = new Tooltip(
                    "🎯 Grupo " + numeroGrupo + "\n" +
                    "📦 Total aplicaciones: " + totalApps + "\n" +
                    "🆔 ID: " + (idGrupo.length() > 15 ? idGrupo.substring(0, 15) + "..." : idGrupo)
                );
                tooltip.setStyle(
                    "-fx-background-color: #2d3436;" +
                    "-fx-text-fill: #dfe6e9;" +
                    "-fx-font-size: 12px;" +
                    "-fx-padding: 8px;" +
                    "-fx-background-radius: 5px;"
                );
                tooltip.setShowDelay(Duration.millis(300));
                Tooltip.install(badge, tooltip);
                
            } else {
                // Badge de FIN (rojo) - adaptado al tema
                String finBg, finBorder, finShadow, finTextoColor;
                if (TemaManager.isDarkMode()) {
                    finBg = "linear-gradient(to right, #3a1a0a, #5a2a0f, #7a3a1f)";
                    finBorder = "#ff5722";
                    finShadow = "rgba(255, 87, 34, 0.8)";
                    finTextoColor = "#ffcc80";
                } else {
                    finBg = "linear-gradient(to right, #C62828, #E53935, #D32F2F)";
                    finBorder = "#C62828";
                    finShadow = "rgba(198, 40, 40, 0.4)";
                    finTextoColor = "#FFFFFF";
                }
                badge.setStyle(
                    "-fx-background-color: " + finBg + ";" +
                    "-fx-background-radius: 0 25 25 0;" +
                    "-fx-border-color: " + finBorder + ";" +
                    "-fx-border-width: 2 2 2 0;" +
                    "-fx-border-radius: 0 25 25 0;" +
                    "-fx-effect: dropshadow(gaussian, " + finShadow + ", 12, 0.3, 2, 2);" +
                    "-fx-cursor: hand;"
                );
                
                Node iconoFin = IconosSVG.bandera(finTextoColor, 18);
                
                Label texto = new Label("FIN");
                texto.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                texto.setTextFill(Color.web(finTextoColor));
                
                badge.getChildren().addAll(iconoFin, texto);
                
                // 💡 TOOLTIP para FIN
                Tooltip tooltip = new Tooltip(
                    "🏁 Fin del Grupo " + numeroGrupo + "\n" +
                    "📦 Total aplicaciones: " + totalApps
                );
                tooltip.setStyle(
                    "-fx-background-color: #2d3436;" +
                    "-fx-text-fill: #dfe6e9;" +
                    "-fx-font-size: 12px;" +
                    "-fx-padding: 8px;" +
                    "-fx-background-radius: 5px;"
                );
                tooltip.setShowDelay(Duration.millis(300));
                Tooltip.install(badge, tooltip);
            }
            
            // 🎬 ANIMACIÓN ELASTIC POP: Scale + Fade + Slide con bounce
            badge.setOpacity(0);
            badge.setScaleX(0.7);
            badge.setScaleY(0.7);
            badge.setTranslateX(-30);
            
            // Fade in
            FadeTransition fade = new FadeTransition(Duration.millis(400), badge);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
            
            // Scale up con bounce
            ScaleTransition scaleX = new ScaleTransition(Duration.millis(400), badge);
            scaleX.setFromX(0.7);
            scaleX.setToX(1.0);
            scaleX.setInterpolator(new javafx.animation.Interpolator() {
                @Override
                protected double curve(double t) {
                    // Efecto bounce: sobrepasa ligeramente y vuelve
                    return t < 0.7 ? Math.pow(t / 0.7, 2) : 1 + 0.1 * Math.sin((t - 0.7) * Math.PI / 0.3);
                }
            });
            
            ScaleTransition scaleY = new ScaleTransition(Duration.millis(400), badge);
            scaleY.setFromY(0.7);
            scaleY.setToY(1.0);
            scaleY.setInterpolator(scaleX.getInterpolator());
            
            // Slide desde la izquierda
            TranslateTransition slide = new TranslateTransition(Duration.millis(400), badge);
            slide.setFromX(-30);
            slide.setToX(0);
            slide.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            
            // Animación paralela con efecto elastic
            ParallelTransition entrance = new ParallelTransition(fade, scaleX, scaleY, slide);
            entrance.play();
            
            // 🎯 EFECTO HOVER: Pulse suave
            badge.setOnMouseEntered(e -> {
                ScaleTransition pulseX = new ScaleTransition(Duration.millis(200), badge);
                pulseX.setToX(1.1);
                ScaleTransition pulseY = new ScaleTransition(Duration.millis(200), badge);
                pulseY.setToY(1.1);
                ParallelTransition pulse = new ParallelTransition(pulseX, pulseY);
                pulse.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                pulse.play();
            });
            
            badge.setOnMouseExited(e -> {
                ScaleTransition unpulseX = new ScaleTransition(Duration.millis(200), badge);
                unpulseX.setToX(1.0);
                ScaleTransition unpulseY = new ScaleTransition(Duration.millis(200), badge);
                unpulseY.setToY(1.0);
                ParallelTransition unpulse = new ParallelTransition(unpulseX, unpulseY);
                unpulse.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
                unpulse.play();
            });
            
            badgesPane.getChildren().add(badge);
            
        } catch (Exception e) {
            // Ignorar errores
        }
    }

    /** Contar registros de una hoja (solo filas con datos reales) */
    private static int contarRegistros(Sheet hoja) {
        return contarFilasConDatos(hoja, 2);
    }
    
    /**
     * Cuenta las filas que tienen datos reales (no vacías) en una hoja
     * @param hoja La hoja de Excel a analizar
     * @param filaInicio La fila desde donde comenzar a contar (típicamente 2 para saltar título y encabezados)
     * @return El número de filas con datos
     */
    private static int contarFilasConDatos(Sheet hoja, int filaInicio) {
        if (hoja == null) return 0;
        
        int count = 0;
        int lastRow = hoja.getLastRowNum();
        
        for (int i = filaInicio; i <= lastRow; i++) {
            Row row = hoja.getRow(i);
            if (row != null && !esFilaVacia(row)) {
                count++;
            }
        }
        
        return count;
    }

    /** Determinar el tipo de columna para mostrar iconos */
    private static String determinarTipoColumna(String headerLower) {
        if (headerLower.contains("usuario") || headerLower.contains("user")) {
            return "usuario";
        } else if (headerLower.contains("tipo") && headerLower.contains("dispositivo")) {
            return "dispositivo";
        } else if (headerLower.contains("marca") || headerLower.contains("brand")) {
            return "marca";
        } else if (headerLower.contains("hostname") || headerLower.contains("host")) {
            return "hostname";
        } else if (headerLower.contains("sistema") && headerLower.contains("operativo")) {
            return "so";
        } else if (headerLower.contains("aplicacion") || headerLower.contains("aplicación") || headerLower.contains("software") || headerLower.contains("programa")) {
            return "aplicacion";
        } else if (headerLower.equals("ip") || headerLower.contains("dirección ip") || headerLower.contains("direccion ip")) {
            return "ip";
        } else if (headerLower.contains("modelo") || headerLower.contains("model")) {
            return "modelo";
        } else if (headerLower.contains("bios")) {
            return "bios";
        } else if (headerLower.contains("versión") || headerLower.contains("version")) {
            return "version";
        } else if (headerLower.contains("fabricante") || headerLower.contains("manufacturer")) {
            return "fabricante";
        } else if (headerLower.contains("memoria") && headerLower.contains("instalada")) {
            return "memoria-instalada";
        } else if (headerLower.contains("ram") || headerLower.contains("memoria")) {
            return "memoria";
        } else if (headerLower.contains("tarjeta") && headerLower.contains("red")) {
            return "tarjeta-red";
        } else if (headerLower.contains("procesador") || headerLower.contains("cpu") || headerLower.contains("processor")) {
            return "procesador";
        } else if (headerLower.contains("disco") || headerLower.contains("almacenamiento") || headerLower.contains("storage") || headerLower.contains("hdd") || headerLower.contains("ssd")) {
            return "disco";
        }
        return "texto";
    }

    /** Crear celda con icono según el tipo de columna */
    private static HBox crearCeldaConIcono(String valor, String tipoColumna) {
        HBox contenedor = new HBox(8);
        contenedor.setAlignment(Pos.CENTER_LEFT);
        
        Node icono = null;
        String colorIcono = TemaManager.getTextMuted();
        
        switch (tipoColumna) {
            case "usuario":
                icono = IconosSVG.usuario(TemaManager.COLOR_INFO, 14);
                break;
            case "dispositivo":
                String valorLower = valor.toLowerCase();
                if (valorLower.contains("desktop") || valorLower.contains("pc") || valorLower.contains("escritorio")) {
                    icono = IconosSVG.computadora(TemaManager.COLOR_INFO, 14);
                } else if (valorLower.contains("laptop") || valorLower.contains("notebook") || valorLower.contains("portatil")) {
                    icono = IconosSVG.laptop(TemaManager.COLOR_INFO, 14);
                } else if (valorLower.contains("server") || valorLower.contains("servidor")) {
                    icono = IconosSVG.servidor(TemaManager.COLOR_INFO, 14);
                } else {
                    icono = IconosSVG.computadora(TemaManager.COLOR_INFO, 14);
                }
                break;
            case "marca":
                icono = crearBadgeMarca(valor);
                break;
            case "hostname":
                icono = IconosSVG.servidor("#6B7280", 12);
                break;
            case "so":
                icono = crearIconoSO(valor);
                break;
            case "aplicacion":
                icono = IconosSVG.paquete("#3B82F6", 12);
                break;
            case "ip":
                icono = IconosSVG.ubicacion("#6B7280", 12);
                break;
            case "modelo":
                icono = IconosSVG.lista("#6B7280", 12);
                break;
            case "bios":
                icono = crearIconoChip("#8B5CF6", 12);
                break;
            case "version":
                icono = crearIconoTag("#10B981", 12);
                break;
            case "fabricante":
                icono = IconosSVG.fabrica("#F59E0B", 12);
                break;
            case "memoria-instalada":
                icono = crearIconoRAM("#EC4899", 12);
                break;
            case "memoria":
                icono = crearIconoRAM("#EC4899", 12);
                break;
            case "tarjeta-red":
                icono = IconosSVG.ubicacion("#10B981", 12);
                break;
            case "procesador":
                icono = crearIconoCPU("#F97316", 12);
                break;
            case "disco":
                icono = crearIconoDisco("#06B6D4", 12);
                break;
            default:
                // Sin icono para texto normal
                break;
        }
        
        // Texto del valor
        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Segoe UI", 12));
        lblValor.setTextFill(Color.web(TemaManager.getTextSecondary()));
        
        if (icono != null) {
            contenedor.getChildren().addAll(icono, lblValor);
        } else {
            contenedor.getChildren().add(lblValor);
        }
        
        return contenedor;
    }

    /** Crear badge colorido para marcas */
    private static Node crearBadgeMarca(String marca) {
        String marcaUpper = marca.toUpperCase().trim();
        String colorFondo;
        String colorTexto = "#FFFFFF";
        String texto = marcaUpper.length() > 4 ? marcaUpper.substring(0, 4) : marcaUpper;
        
        // Colores por marca
        switch (marcaUpper) {
            case "HP":
            case "HEWLETT-PACKARD":
            case "HEWLETT PACKARD":
                colorFondo = "#D32F2F"; // Rojo HP
                texto = "HP";
                break;
            case "DELL":
                colorFondo = "#0076CE"; // Azul Dell
                texto = "DELL";
                break;
            case "LENOVO":
                colorFondo = "#E2231A"; // Rojo Lenovo
                texto = "LNV";
                break;
            case "ASUS":
                colorFondo = "#00539B"; // Azul Asus
                texto = "ASUS";
                break;
            case "ACER":
                colorFondo = "#83B81A"; // Verde Acer
                texto = "ACER";
                break;
            case "APPLE":
            case "MACBOOK":
                colorFondo = "#555555"; // Gris Apple
                texto = "🍎";
                break;
            case "MSI":
                colorFondo = "#FF0000"; // Rojo MSI
                texto = "MSI";
                break;
            case "SAMSUNG":
                colorFondo = "#1428A0"; // Azul Samsung
                texto = "SAM";
                break;
            case "TOSHIBA":
                colorFondo = "#FF0000"; // Rojo Toshiba
                texto = "TOSH";
                break;
            case "MICROSOFT":
                colorFondo = "#00A4EF"; // Azul Microsoft
                texto = "MS";
                break;
            default:
                colorFondo = "#4A5568"; // Gris por defecto
                texto = marcaUpper.length() > 3 ? marcaUpper.substring(0, 3) : marcaUpper;
                break;
        }
        
        // Crear badge
        StackPane badge = new StackPane();
        badge.setMinSize(32, 20);
        badge.setMaxSize(32, 20);
        badge.setStyle(
            "-fx-background-color: " + colorFondo + ";" +
            "-fx-background-radius: 4;"
        );
        
        Label lblBadge = new Label(texto);
        lblBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        lblBadge.setTextFill(Color.web(colorTexto));
        
        badge.getChildren().add(lblBadge);
        return badge;
    }

    /** Crear icono para sistema operativo */
    private static Node crearIconoSO(String so) {
        String soLower = so.toLowerCase();
        
        // Windows
        if (soLower.contains("windows")) {
            return crearIconoWindows("#00A4EF", 14);
        } 
        // macOS / Apple
        else if (soLower.contains("mac") || soLower.contains("darwin") || soLower.contains("apple") || soLower.contains("osx")) {
            return crearIconoApple("#A3A3A3", 14);
        }
        // Chrome OS
        else if (soLower.contains("chrome") || soLower.contains("chromeos") || soLower.contains("chromebook")) {
            return crearIconoChrome(14);
        }
        // Distribuciones Linux específicas
        else if (soLower.contains("ubuntu")) {
            return crearIconoLinux("#E95420", 14); // Naranja Ubuntu
        }
        else if (soLower.contains("debian")) {
            return crearIconoLinux("#A80030", 14); // Rojo Debian
        }
        else if (soLower.contains("fedora") || soLower.contains("red hat") || soLower.contains("redhat") || soLower.contains("rhel")) {
            return crearIconoLinux("#0B57A4", 14); // Azul Fedora/RedHat
        }
        else if (soLower.contains("centos")) {
            return crearIconoLinux("#932279", 14); // Morado CentOS
        }
        else if (soLower.contains("arch")) {
            return crearIconoLinux("#1793D1", 14); // Azul Arch
        }
        else if (soLower.contains("mint")) {
            return crearIconoLinux("#87CF3E", 14); // Verde Mint
        }
        else if (soLower.contains("suse") || soLower.contains("opensuse")) {
            return crearIconoLinux("#73BA25", 14); // Verde SUSE
        }
        // Linux genérico
        else if (soLower.contains("linux")) {
            return crearIconoLinux("#FCC624", 14); // Amarillo Tux
        }
        
        return null;
    }

    /** Icono de calendario estilo Lucide */
    private static Node crearIconoCalendario(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Rectángulo principal del calendario
        javafx.scene.shape.Path cuerpo = new javafx.scene.shape.Path();
        cuerpo.setStroke(Color.web(color));
        cuerpo.setStrokeWidth(2 * s);
        cuerpo.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        cuerpo.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        cuerpo.setFill(Color.TRANSPARENT);
        
        // rect x="3" y="4" width="18" height="18" rx="2" ry="2"
        cuerpo.getElements().addAll(
            new javafx.scene.shape.MoveTo(5*s, 4*s),
            new javafx.scene.shape.LineTo(19*s, 4*s),
            new javafx.scene.shape.CubicCurveTo(20.1*s, 4*s, 21*s, 4.9*s, 21*s, 6*s),
            new javafx.scene.shape.LineTo(21*s, 20*s),
            new javafx.scene.shape.CubicCurveTo(21*s, 21.1*s, 20.1*s, 22*s, 19*s, 22*s),
            new javafx.scene.shape.LineTo(5*s, 22*s),
            new javafx.scene.shape.CubicCurveTo(3.9*s, 22*s, 3*s, 21.1*s, 3*s, 20*s),
            new javafx.scene.shape.LineTo(3*s, 6*s),
            new javafx.scene.shape.CubicCurveTo(3*s, 4.9*s, 3.9*s, 4*s, 5*s, 4*s),
            new javafx.scene.shape.ClosePath()
        );
        
        // Línea horizontal debajo de las anillas
        javafx.scene.shape.Line lineaHorizontal = new javafx.scene.shape.Line(3*s, 10*s, 21*s, 10*s);
        lineaHorizontal.setStroke(Color.web(color));
        lineaHorizontal.setStrokeWidth(2 * s);
        lineaHorizontal.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        
        // Anilla izquierda
        javafx.scene.shape.Line anillaIzq = new javafx.scene.shape.Line(8*s, 2*s, 8*s, 6*s);
        anillaIzq.setStroke(Color.web(color));
        anillaIzq.setStrokeWidth(2 * s);
        anillaIzq.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        
        // Anilla derecha
        javafx.scene.shape.Line anillaDer = new javafx.scene.shape.Line(16*s, 2*s, 16*s, 6*s);
        anillaDer.setStroke(Color.web(color));
        anillaDer.setStrokeWidth(2 * s);
        anillaDer.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        
        g.getChildren().addAll(cuerpo, lineaHorizontal, anillaIzq, anillaDer);
        return g;
    }

    /** Icono simple de Windows */
    private static Node crearIconoWindows(String color, double size) {
        Group g = new Group();
        double s = size / 16.0;
        
        // Cuatro cuadrados del logo de Windows
        Rectangle r1 = new Rectangle(1*s, 1*s, 6*s, 6*s);
        r1.setFill(Color.web(color));
        r1.setArcWidth(1*s);
        r1.setArcHeight(1*s);
        
        Rectangle r2 = new Rectangle(9*s, 1*s, 6*s, 6*s);
        r2.setFill(Color.web(color));
        r2.setArcWidth(1*s);
        r2.setArcHeight(1*s);
        
        Rectangle r3 = new Rectangle(1*s, 9*s, 6*s, 6*s);
        r3.setFill(Color.web(color));
        r3.setArcWidth(1*s);
        r3.setArcHeight(1*s);
        
        Rectangle r4 = new Rectangle(9*s, 9*s, 6*s, 6*s);
        r4.setFill(Color.web(color));
        r4.setArcWidth(1*s);
        r4.setArcHeight(1*s);
        
        g.getChildren().addAll(r1, r2, r3, r4);
        return g;
    }

    /** Icono de Chrome OS - Círculo tricolor */
    private static Node crearIconoChrome(double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Círculo exterior con secciones de color
        // Rojo (arriba izquierda)
        javafx.scene.shape.Arc rojo = new javafx.scene.shape.Arc(12*s, 12*s, 9*s, 9*s, 90, 120);
        rojo.setType(javafx.scene.shape.ArcType.ROUND);
        rojo.setFill(Color.web("#EA4335"));
        
        // Verde (abajo)
        javafx.scene.shape.Arc verde = new javafx.scene.shape.Arc(12*s, 12*s, 9*s, 9*s, 210, 120);
        verde.setType(javafx.scene.shape.ArcType.ROUND);
        verde.setFill(Color.web("#34A853"));
        
        // Amarillo (arriba derecha)
        javafx.scene.shape.Arc amarillo = new javafx.scene.shape.Arc(12*s, 12*s, 9*s, 9*s, 330, 120);
        amarillo.setType(javafx.scene.shape.ArcType.ROUND);
        amarillo.setFill(Color.web("#FBBC05"));
        
        // Círculo blanco interior
        Circle blanco = new Circle(12*s, 12*s, 5*s);
        blanco.setFill(Color.web("#FFFFFF"));
        
        // Círculo azul central
        Circle azul = new Circle(12*s, 12*s, 4*s);
        azul.setFill(Color.web("#4285F4"));
        
        g.getChildren().addAll(rojo, verde, amarillo, blanco, azul);
        return g;
    }

    /** Icono de chip/BIOS - Estilo Lucide (microchip) */
    private static Node crearIconoChip(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Cuadrado central del chip
        Rectangle chip = new Rectangle(7*s, 7*s, 10*s, 10*s);
        chip.setArcWidth(2*s);
        chip.setArcHeight(2*s);
        chip.setFill(Color.TRANSPARENT);
        chip.setStroke(Color.web(color));
        chip.setStrokeWidth(2*s);
        chip.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        chip.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        
        // Pines superiores (3)
        javafx.scene.shape.Line t1 = new javafx.scene.shape.Line(10*s, 3*s, 10*s, 7*s);
        javafx.scene.shape.Line t2 = new javafx.scene.shape.Line(14*s, 3*s, 14*s, 7*s);
        // Pines inferiores (3)
        javafx.scene.shape.Line b1 = new javafx.scene.shape.Line(10*s, 17*s, 10*s, 21*s);
        javafx.scene.shape.Line b2 = new javafx.scene.shape.Line(14*s, 17*s, 14*s, 21*s);
        // Pines izquierdos (3)
        javafx.scene.shape.Line l1 = new javafx.scene.shape.Line(3*s, 10*s, 7*s, 10*s);
        javafx.scene.shape.Line l2 = new javafx.scene.shape.Line(3*s, 14*s, 7*s, 14*s);
        // Pines derechos (3)
        javafx.scene.shape.Line r1 = new javafx.scene.shape.Line(17*s, 10*s, 21*s, 10*s);
        javafx.scene.shape.Line r2 = new javafx.scene.shape.Line(17*s, 14*s, 21*s, 14*s);
        
        for (javafx.scene.shape.Line pin : new javafx.scene.shape.Line[]{t1, t2, b1, b2, l1, l2, r1, r2}) {
            pin.setStroke(Color.web(color));
            pin.setStrokeWidth(2*s);
            pin.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        }
        
        g.getChildren().addAll(chip, t1, t2, b1, b2, l1, l2, r1, r2);
        return g;
    }

    /** Icono de etiqueta/tag para versión - Mejorado */
    private static Node crearIconoTag(String color, double size) {
        Group g = new Group();
        double s = size / 16.0;
        
        // Círculo pequeño relleno (como un punto de versión)
        Circle punto = new Circle(8*s, 8*s, 4*s);
        punto.setFill(Color.web(color));
        
        // Borde exterior
        Circle borde = new Circle(8*s, 8*s, 6*s);
        borde.setFill(Color.TRANSPARENT);
        borde.setStroke(Color.web(color));
        borde.setStrokeWidth(1.5*s);
        
        g.getChildren().addAll(borde, punto);
        return g;
    }

    /** Icono de RAM/memoria - Mejorado tipo barras */
    private static Node crearIconoRAM(String color, double size) {
        Group g = new Group();
        double s = size / 16.0;
        
        // Tres barras verticales como módulos de memoria
        Rectangle b1 = new Rectangle(2*s, 4*s, 3*s, 10*s);
        b1.setFill(Color.web(color));
        b1.setArcWidth(1*s);
        b1.setArcHeight(1*s);
        
        Rectangle b2 = new Rectangle(7*s, 6*s, 3*s, 8*s);
        b2.setFill(Color.web(color));
        b2.setArcWidth(1*s);
        b2.setArcHeight(1*s);
        
        Rectangle b3 = new Rectangle(12*s, 2*s, 3*s, 12*s);
        b3.setFill(Color.web(color));
        b3.setArcWidth(1*s);
        b3.setArcHeight(1*s);
        
        g.getChildren().addAll(b1, b2, b3);
        return g;
    }

    /** Icono de CPU/procesador - Estilo Lucide */
    private static Node crearIconoCPU(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Cuadrado central (chip)
        Rectangle cpu = new Rectangle(6*s, 6*s, 12*s, 12*s);
        cpu.setArcWidth(2*s);
        cpu.setArcHeight(2*s);
        cpu.setFill(Color.TRANSPARENT);
        cpu.setStroke(Color.web(color));
        cpu.setStrokeWidth(2*s);
        cpu.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        cpu.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        
        // Núcleo interno
        Rectangle core = new Rectangle(9*s, 9*s, 6*s, 6*s);
        core.setFill(Color.TRANSPARENT);
        core.setStroke(Color.web(color));
        core.setStrokeWidth(2*s);
        
        // Pines superiores
        javafx.scene.shape.Line p1 = new javafx.scene.shape.Line(9*s, 2*s, 9*s, 6*s);
        javafx.scene.shape.Line p2 = new javafx.scene.shape.Line(15*s, 2*s, 15*s, 6*s);
        // Pines inferiores
        javafx.scene.shape.Line p3 = new javafx.scene.shape.Line(9*s, 18*s, 9*s, 22*s);
        javafx.scene.shape.Line p4 = new javafx.scene.shape.Line(15*s, 18*s, 15*s, 22*s);
        // Pines izquierdos
        javafx.scene.shape.Line p5 = new javafx.scene.shape.Line(2*s, 9*s, 6*s, 9*s);
        javafx.scene.shape.Line p6 = new javafx.scene.shape.Line(2*s, 15*s, 6*s, 15*s);
        // Pines derechos
        javafx.scene.shape.Line p7 = new javafx.scene.shape.Line(18*s, 9*s, 22*s, 9*s);
        javafx.scene.shape.Line p8 = new javafx.scene.shape.Line(18*s, 15*s, 22*s, 15*s);
        
        for (javafx.scene.shape.Line pin : new javafx.scene.shape.Line[]{p1, p2, p3, p4, p5, p6, p7, p8}) {
            pin.setStroke(Color.web(color));
            pin.setStrokeWidth(2*s);
            pin.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        }
        
        g.getChildren().addAll(cpu, core, p1, p2, p3, p4, p5, p6, p7, p8);
        return g;
    }

    /** Icono de disco/almacenamiento - Mejorado */
    private static Node crearIconoDisco(String color, double size) {
        Group g = new Group();
        double s = size / 16.0;
        
        // Cilindro del disco (dos elipses y rectángulo)
        javafx.scene.shape.Ellipse top = new javafx.scene.shape.Ellipse(8*s, 5*s, 6*s, 2.5*s);
        top.setFill(Color.web(color));
        
        Rectangle cuerpo = new Rectangle(2*s, 5*s, 12*s, 6*s);
        cuerpo.setFill(Color.web(color));
        
        javafx.scene.shape.Ellipse bottom = new javafx.scene.shape.Ellipse(8*s, 11*s, 6*s, 2.5*s);
        bottom.setFill(Color.web(color));
        
        g.getChildren().addAll(cuerpo, bottom, top);
        return g;
    }

    /** Icono de Apple - Lucide oficial */
    private static Node crearIconoApple(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial de Lucide 'apple'
        // <path d="M12 20.94c1.5 0 2.75 1.06 4 1.06 3 0 6-8 6-12.22A4.91 4.91 0 0 0 17 5c-2.22 0-4 1.44-5 2-1-.56-2.78-2-5-2a4.9 4.9 0 0 0-5 4.78C2 14 5 22 8 22c1.25 0 2.5-1.06 4-1.06Z"/>
        javafx.scene.shape.Path manzana = new javafx.scene.shape.Path();
        manzana.getElements().addAll(
            new javafx.scene.shape.MoveTo(12*s, 20.94*s),
            new javafx.scene.shape.CubicCurveTo(13.5*s, 20.94*s, 14.75*s, 22*s, 16*s, 22*s),
            new javafx.scene.shape.CubicCurveTo(19*s, 22*s, 22*s, 14*s, 22*s, 9.78*s),
            new javafx.scene.shape.CubicCurveTo(22*s, 7.13*s, 19.76*s, 5*s, 17*s, 5*s),
            new javafx.scene.shape.CubicCurveTo(14.78*s, 5*s, 13*s, 6.44*s, 12*s, 7*s),
            new javafx.scene.shape.CubicCurveTo(11*s, 6.44*s, 9.22*s, 5*s, 7*s, 5*s),
            new javafx.scene.shape.CubicCurveTo(4.24*s, 5*s, 2*s, 7.12*s, 2*s, 9.78*s),
            new javafx.scene.shape.CubicCurveTo(2*s, 14*s, 5*s, 22*s, 8*s, 22*s),
            new javafx.scene.shape.CubicCurveTo(9.25*s, 22*s, 10.5*s, 20.94*s, 12*s, 20.94*s),
            new javafx.scene.shape.ClosePath()
        );
        manzana.setFill(Color.TRANSPARENT);
        manzana.setStroke(Color.web(color));
        manzana.setStrokeWidth(2*s);
        manzana.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        manzana.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        
        // Tallo: <path d="M10 2c1 .5 2 2 2 5"/>
        javafx.scene.shape.Path tallo = new javafx.scene.shape.Path();
        tallo.getElements().addAll(
            new javafx.scene.shape.MoveTo(10*s, 2*s),
            new javafx.scene.shape.CubicCurveTo(11*s, 2.5*s, 12*s, 4*s, 12*s, 7*s)
        );
        tallo.setFill(Color.TRANSPARENT);
        tallo.setStroke(Color.web(color));
        tallo.setStrokeWidth(2*s);
        tallo.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        tallo.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(manzana, tallo);
        return g;
    }

    /** Icono de Linux/Tux - Estilo Lucide (terminal) */
    private static Node crearIconoLinux(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Usar icono de terminal como representación de Linux
        // Rectángulo de la terminal
        Rectangle terminal = new Rectangle(3*s, 4*s, 18*s, 16*s);
        terminal.setArcWidth(4*s);
        terminal.setArcHeight(4*s);
        terminal.setFill(Color.TRANSPARENT);
        terminal.setStroke(Color.web(color));
        terminal.setStrokeWidth(2*s);
        terminal.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        terminal.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        
        // Prompt > (chevron)
        javafx.scene.shape.Polyline chevron = new javafx.scene.shape.Polyline(
            7*s, 9*s,
            10*s, 12*s,
            7*s, 15*s
        );
        chevron.setFill(Color.TRANSPARENT);
        chevron.setStroke(Color.web(color));
        chevron.setStrokeWidth(2*s);
        chevron.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        chevron.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        
        // Línea del cursor _
        javafx.scene.shape.Line cursor = new javafx.scene.shape.Line(13*s, 15*s, 17*s, 15*s);
        cursor.setStroke(Color.web(color));
        cursor.setStrokeWidth(2*s);
        cursor.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        
        g.getChildren().addAll(terminal, chevron, cursor);
        return g;
    }


    /** Agregar animaciones suaves a las transiciones de tabs */
    private static void agregarAnimacionTabs(TabPane tabPane) {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getContent() != null) {
                Node content = newTab.getContent();
                content.setOpacity(0);
                content.setTranslateY(8);
                
                // Animación de fade in + slide up
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), content);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                
                TranslateTransition slideUp = new TranslateTransition(Duration.millis(200), content);
                slideUp.setFromY(8);
                slideUp.setToY(0);
                slideUp.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                
                fadeIn.play();
                slideUp.play();
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════════
    // HEADER CON BÚSQUEDA
    // ════════════════════════════════════════════════════════════════════════════
    
    private static HBox crearHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(24, 32, 20, 32));
        header.setSpacing(20);

        // Título con icono
        VBox titleBox = new VBox(4);
        
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        // Icono del dashboard
        Node iconoDashboard = IconosSVG.dashboard("#3B82F6", 28);
        
        Label title = new Label("Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(COLOR_TEXT()));
        
        titleRow.getChildren().addAll(iconoDashboard, title);

        Label subtitle = new Label("Vista general del inventario");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));

        titleBox.getChildren().addAll(titleRow, subtitle);

        // Espaciador
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Fecha actual con diseño compacto y moderno
        HBox dateBox = new HBox(6);
        dateBox.setAlignment(Pos.CENTER);
        dateBox.setPadding(new Insets(5, 10, 5, 8));
        dateBox.setCursor(javafx.scene.Cursor.HAND);
        
        String estiloBase = 
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;";
        
        String estiloHover = 
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + COLOR_PRIMARY + ";" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;";
        
        dateBox.setStyle(estiloBase);
        
        // Icono de calendario estilo Lucide (más pequeño)
        Node iconoCalendario = crearIconoCalendario(COLOR_TEXT_MUTED(), 14);
        
        // Formato de fecha: "viernes, 9 enero"
        DateTimeFormatter sdfFecha = DateTimeFormatter.ofPattern("EEEE, d MMMM", new java.util.Locale("es", "ES"));
        String fechaTexto = java.time.LocalDate.now().format(sdfFecha);
        // Primera letra mayúscula
        fechaTexto = fechaTexto.substring(0, 1).toUpperCase() + fechaTexto.substring(1);
        
        Label dateLabel = new Label(fechaTexto);
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        dateLabel.setTextFill(Color.web(COLOR_TEXT_MUTED()));
        
        dateBox.getChildren().addAll(iconoCalendario, dateLabel);
        
        // Animaciones suaves para hover
        javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(150), dateBox);
        scaleIn.setToX(1.03);
        scaleIn.setToY(1.03);
        
        javafx.animation.ScaleTransition scaleOut = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(150), dateBox);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        
        // Animación de opacidad del icono
        javafx.animation.FadeTransition fadeIconIn = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(150), iconoCalendario);
        fadeIconIn.setToValue(1.0);
        
        javafx.animation.FadeTransition fadeIconOut = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(150), iconoCalendario);
        fadeIconOut.setToValue(0.7);
        
        // Estado inicial del icono
        iconoCalendario.setOpacity(0.7);
        
        // Efecto hover con animaciones
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

    // ════════════════════════════════════════════════════════════════════════════
    // TARJETAS DE ESTADÍSTICAS
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Re-anima los conteos numéricos de las tarjetas (equipos y apps) desde 0.
     * Se llama cada vez que se vuelve a mostrar el panel de sistema.
     */
    private static void reanimarConteosTarjetas() {
        if (lblTarjetaEquipos == null || lblTarjetaApps == null) return;
        
        // Leer los valores actuales antes de resetear
        int valorEquipos = parsearNumeroLabel(lblTarjetaEquipos.getText());
        int valorApps = parsearNumeroLabel(lblTarjetaApps.getText());
        
        // Resetear a 0 y re-animar
        lblTarjetaEquipos.setText("0");
        lblTarjetaApps.setText("0");
        
        if (valorEquipos > 0) {
            ComponentesFX.animarConteoConDelay(lblTarjetaEquipos, 0, valorEquipos, 800, 250);
        }
        if (valorApps > 0) {
            ComponentesFX.animarConteoConDelay(lblTarjetaApps, 0, valorApps, 800, 350);
        }
    }

    /**
     * Actualiza las tarjetas de estadísticas en tiempo real
     */
    private static void actualizarTarjetasEstadisticas(Path rutaExcel) {
        if (lblTarjetaEquipos == null || lblTarjetaApps == null) return;
        
        Platform.runLater(() -> {
            try {
                XSSFWorkbook wb = abrirCifradoProyecto(rutaExcel);
                if (wb != null) {
                    Sheet hojaSystem = wb.getSheet("SystemInfo");
                    int totalEquipos = hojaSystem != null ? contarFilasConDatos(hojaSystem, 2) : 0;
                    
                    Sheet hojaApps = wb.getSheet("InstalledApps");
                    int totalApps = hojaApps != null ? contarFilasConDatos(hojaApps, 2) : 0;
                    
                    wb.close();
                    
                    // Animar el cambio de valores (parsear quitando separadores de miles)
                    int valorActualEquipos = parsearNumeroLabel(lblTarjetaEquipos.getText());
                    int valorActualApps = parsearNumeroLabel(lblTarjetaApps.getText());
                    
                    if (valorActualEquipos != totalEquipos) {
                        animarConteo(lblTarjetaEquipos, valorActualEquipos, totalEquipos, 400);
                    }
                    if (valorActualApps != totalApps) {
                        animarConteo(lblTarjetaApps, valorActualApps, totalApps, 400);
                    }
                }
            } catch (Exception e) {
                logger.error("Error actualizando tarjetas", e);
                System.err.println("[Dashboard] Error actualizando tarjetas: " + e.getMessage());
            }
        });
    }
    
    private static HBox crearTarjetasEstadisticas(Path rutaExcel) {
        HBox cards = new HBox(12);
        cards.setAlignment(Pos.CENTER);
        cards.setPadding(new Insets(0, 32, 20, 32));

        int totalEquipos = 0;
        int totalApps = 0;

        try {
            XSSFWorkbook wb = abrirCifradoProyecto(rutaExcel);
            if (wb != null) {
                // Contar filas reales con datos en SystemInfo (fila 0: título, fila 1: encabezados, fila 2+: datos)
                Sheet hojaSystem = wb.getSheet("SystemInfo");
                if (hojaSystem != null) {
                    totalEquipos = contarFilasConDatos(hojaSystem, 2);
                }
                
                // Contar filas reales con datos en InstalledApps
                Sheet hojaApps = wb.getSheet("InstalledApps");
                if (hojaApps != null) {
                    totalApps = contarFilasConDatos(hojaApps, 2);
                }
                wb.close();
            }
        } catch (Exception e) {
            logger.error("Error al cargar estadísticas", e);
        }

        // Obtener color del proyecto actual para la tarjeta
        String colorProyecto = COLOR_INFO; // Color por defecto
        try {
            String indice = CURRENT_PROJECT.split("\\.")[0];
            int idx = Integer.parseInt(indice) - 1;
            AdminManager.Proyecto proyectoActual = AdminManager.getProyectoPorIndice(idx);
            if (proyectoActual != null && proyectoActual.getColor() != null) {
                colorProyecto = proyectoActual.getColor();
            }
        } catch (Exception ignored) {}
        
        VBox cardEquipos = crearTarjetaModerna("Equipos", totalEquipos, IconosSVG.computadora(COLOR_PRIMARY, 20), COLOR_PRIMARY, true);
        VBox cardApps = crearTarjetaModerna("Aplicaciones", totalApps, IconosSVG.paquete(COLOR_SUCCESS, 20), COLOR_SUCCESS, false);
        VBox cardProyecto = crearTarjetaModernaTexto("Proyecto", limpiarNombreProyecto(CURRENT_PROJECT), IconosSVG.carpeta(colorProyecto, 20), colorProyecto);

        HBox.setHgrow(cardEquipos, Priority.ALWAYS);
        HBox.setHgrow(cardApps, Priority.ALWAYS);
        HBox.setHgrow(cardProyecto, Priority.ALWAYS);

        cards.getChildren().addAll(cardEquipos, cardApps, cardProyecto);
        return cards;
    }
    
    /** Tarjeta moderna con diseño igual a Estadísticas - para números */
    private static VBox crearTarjetaModerna(String titulo, int valorNumerico, Node icono, String color, boolean esEquipos) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12));
        card.setMinWidth(180);
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;"
        );
        
        // Sombra sutil con el color del icono
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web(color, 0.15));
        sombra.setRadius(12);
        sombra.setOffsetY(3);
        card.setEffect(sombra);
        
        HBox headerCard = new HBox(8);
        headerCard.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconWrapper = new StackPane(icono);
        iconWrapper.setPadding(new Insets(6));
        iconWrapper.setStyle(
            "-fx-background-color: " + color + "15;" +
            "-fx-background-radius: 8;"
        );
        
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        lblTitulo.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        headerCard.getChildren().addAll(iconWrapper, lblTitulo);
        
        Label lblValor = new Label("0");
        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblValor.setTextFill(Color.web(COLOR_TEXT()));
        lblValor.setWrapText(false);
        lblValor.setMinWidth(Region.USE_PREF_SIZE);
        
        // Guardar referencia según el tipo
        if (esEquipos) {
            lblTarjetaEquipos = lblValor;
        } else {
            lblTarjetaApps = lblValor;
        }
        
        // Animación de conteo — diferir para que el nodo ya esté en escena
        final int val = valorNumerico;
        final Label lbl = lblValor;
        Platform.runLater(() -> ComponentesFX.animarConteoConDelay(lbl, 0, val, 800, 200));
        
        card.getChildren().addAll(headerCard, lblValor);
        
        // Hover effect sutil
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_BG() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + color + "44;" +
                "-fx-border-radius: 12;"
            );
            sombra.setColor(Color.web(color, 0.25));
            sombra.setRadius(15);
            
            javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(150), card
            );
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_BG() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 12;"
            );
            sombra.setColor(Color.web(color, 0.15));
            sombra.setRadius(12);
            
            javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(150), card
            );
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        return card;
    }
    
    /** Tarjeta moderna con diseño igual a Estadísticas - para texto */
    private static VBox crearTarjetaModernaTexto(String titulo, String valor, Node icono, String color) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12));
        card.setMinWidth(180);
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;"
        );
        
        // Sombra sutil con el color del icono
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web(color, 0.15));
        sombra.setRadius(12);
        sombra.setOffsetY(3);
        card.setEffect(sombra);
        
        HBox headerCard = new HBox(8);
        headerCard.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconWrapper = new StackPane(icono);
        iconWrapper.setPadding(new Insets(6));
        iconWrapper.setStyle(
            "-fx-background-color: " + color + "15;" +
            "-fx-background-radius: 8;"
        );
        
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        lblTitulo.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        headerCard.getChildren().addAll(iconWrapper, lblTitulo);
        
        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblValor.setTextFill(Color.web(COLOR_TEXT()));
        lblValor.setWrapText(false);
        lblValor.setMinWidth(Region.USE_PREF_SIZE);
        
        card.getChildren().addAll(headerCard, lblValor);
        
        // Hover effect sutil
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_BG() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + color + "44;" +
                "-fx-border-radius: 12;"
            );
            sombra.setColor(Color.web(color, 0.25));
            sombra.setRadius(15);
            
            javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(150), card
            );
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_BG() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 12;"
            );
            sombra.setColor(Color.web(color, 0.15));
            sombra.setRadius(12);
            
            javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(150), card
            );
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        return card;
    }

    private static VBox crearTarjetaStat(String titulo, String valor, String icono, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;"
        );

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-radius: 12;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_BG() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 12;"
            );
        });

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icono con fondo
        StackPane iconBg = new StackPane();
        iconBg.setPrefSize(36, 36);
        iconBg.setStyle(
            "-fx-background-color: " + color + "20;" +
            "-fx-background-radius: 8;"
        );
        Label iconLabel = new Label(icono);
        iconLabel.setFont(Font.font(16));
        iconBg.getChildren().add(iconLabel);

        Label titleLabel = new Label(titulo);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        titleLabel.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));

        header.getChildren().addAll(iconBg, titleLabel);

        Label valueLabel = new Label(valor);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.web(COLOR_TEXT()));

        card.getChildren().addAll(header, valueLabel);
        return card;
    }

    /** Tarjeta de estadísticas con icono SVG v6.0 */
    private static VBox crearTarjetaStatSVG(String titulo, String valor, Node icono, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;"
        );

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-radius: 12;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_BG() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 12;"
            );
        });

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icono SVG con fondo
        StackPane iconBg = new StackPane();
        iconBg.setPrefSize(36, 36);
        iconBg.setMinSize(36, 36);
        iconBg.setStyle(
            "-fx-background-color: " + color + "20;" +
            "-fx-background-radius: 8;"
        );
        iconBg.getChildren().add(icono);

        Label titleLabel = new Label(titulo);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        titleLabel.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));

        header.getChildren().addAll(iconBg, titleLabel);

        Label valueLabel = new Label(valor);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.web(COLOR_TEXT()));

        card.getChildren().addAll(header, valueLabel);
        return card;
    }
    
    /** Tarjeta de estadísticas con icono SVG y animación de conteo */
    private static VBox crearTarjetaStatSVGAnimada(String titulo, int valorNumerico, Node icono, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 12;"
        );

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-radius: 12;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_BG() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 12;"
            );
        });

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icono SVG con fondo
        StackPane iconBg = new StackPane();
        iconBg.setPrefSize(36, 36);
        iconBg.setMinSize(36, 36);
        iconBg.setStyle(
            "-fx-background-color: " + color + "20;" +
            "-fx-background-radius: 8;"
        );
        iconBg.getChildren().add(icono);

        Label titleLabel = new Label(titulo);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        titleLabel.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));

        header.getChildren().addAll(iconBg, titleLabel);

        Label valueLabel = new Label("0");
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.web(COLOR_TEXT()));
        
        // Animación de conteo — diferir para que el nodo ya esté en escena
        final int val = valorNumerico;
        Platform.runLater(() -> ComponentesFX.animarConteoConDelay(valueLabel, 0, val, 800, 200));

        card.getChildren().addAll(header, valueLabel);
        return card;
    }
    
    /** Anima un conteo numérico de inicio a fin */
    private static void animarConteo(Label label, int desde, int hasta, int duracionMs) {
        ComponentesFX.animarConteo(label, desde, hasta, duracionMs);
    }

    /** Parsea el texto de un label eliminando separadores de miles */
    private static int parsearNumeroLabel(String texto) {
        try {
            return Integer.parseInt(texto.replaceAll("[^\\d-]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // TAB ELEGANTE CON TABLA
    // ════════════════════════════════════════════════════════════════════════════
    
    private static Tab crearTabElegante(Sheet hoja, String nombre, String icono) {
        Tab tab = new Tab(icono + "  " + nombre);
        tab.getStyleClass().add("elegant-tab");

        VBox contenido = new VBox(16);
        contenido.setPadding(new Insets(20, 0, 0, 0));
        contenido.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");

        // Obtener encabezados
        Row headerRow = hoja.getRow(1);
        if (headerRow == null) {
            tab.setContent(new Label("Sin datos"));
            return tab;
        }
        return crearContenidoTab(tab, hoja, contenido, headerRow);
    }

    /** Tab elegante con icono SVG v6.0 */
    private static Tab crearTabEleganteSVG(Sheet hoja, String nombre, Node icono) {
        Tab tab = new Tab();
        
        // Crear contenido del tab con icono SVG
        HBox tabHeader = new HBox(6);
        tabHeader.setAlignment(Pos.CENTER_LEFT);
        tabHeader.getChildren().addAll(icono, new Label(nombre));
        tab.setGraphic(tabHeader);
        tab.getStyleClass().add("elegant-tab");

        VBox contenido = new VBox(16);
        contenido.setPadding(new Insets(20, 0, 0, 0));
        contenido.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");

        // Obtener encabezados
        Row headerRow = hoja.getRow(1);
        if (headerRow == null) {
            tab.setContent(new Label("Sin datos"));
            return tab;
        }
        return crearContenidoTab(tab, hoja, contenido, headerRow);
    }

    /** Crear contenido común del tab */
    private static Tab crearContenidoTab(Tab tab, Sheet hoja, VBox contenido, Row headerRow) {


        int columnas = headerRow.getLastCellNum();
        List<String> encabezados = new ArrayList<>();
        for (int i = 0; i < columnas; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(i);
            encabezados.add(cell != null ? cell.getStringCellValue() : "Columna " + (i + 1));
        }

        // Crear tabla integrada sin bordes externos
        TableView<ObservableList<String>> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tabla.getStyleClass().addAll("elegant-table", "table-view");
        tabla.setStyle(
            "-fx-background-color: " + (COLOR_SURFACE()) + ";" +
            "-fx-border-color: transparent;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        );

        // Crear columnas con anchos apropiados según el encabezado
        for (int i = 0; i < encabezados.size(); i++) {
            final int colIndex = i;
            String headerName = encabezados.get(i);
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(headerName);
            col.setCellValueFactory(param -> {
                ObservableList<String> row = param.getValue();
                if (colIndex < row.size()) {
                    return new SimpleStringProperty(row.get(colIndex));
                }
                return new SimpleStringProperty("");
            });
            
            // Asignar anchos específicos según el tipo de columna
            int ancho = calcularAnchoColumna(headerName);
            col.setPrefWidth(ancho);
            col.setMinWidth(ancho);
            
            col.getStyleClass().add("elegant-column");
            tabla.getColumns().add(col);
        }

        // Cargar datos
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();
        int filas = hoja.getLastRowNum();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 2; i <= filas; i++) {
            Row row = hoja.getRow(i);
            if (row == null || esFilaVacia(row)) continue;  // Saltar filas nulas o vacías

            ObservableList<String> fila = FXCollections.observableArrayList();
            for (int j = 0; j < columnas; j++) {
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String valor = "";
                if (cell.getCellType() == CellType.STRING) {
                    valor = cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        valor = cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(sdf);
                    } else {
                        valor = String.valueOf((int) cell.getNumericCellValue());
                    }
                }
                fila.add(valor);
            }
            datos.add(fila);
        }

        // Datos ordenados
        SortedList<ObservableList<String>> sortedData = new SortedList<>(FXCollections.observableArrayList(datos));
        sortedData.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(sortedData);

        // 🔑 Habilitar selección de celdas individuales
        tabla.getSelectionModel().setCellSelectionEnabled(true);
        tabla.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        
        // 📋 Atajos de teclado
        tabla.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.A) {
                tabla.getSelectionModel().selectAll();
                event.consume();
            } else if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.C) {
                copiarCeldasTabSecundaria(tabla, contenido);
                event.consume();
            }
        });

        VBox.setVgrow(tabla, Priority.ALWAYS);
        contenido.getChildren().addAll(tabla);
        tab.setContent(contenido);

        return tab;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PANEL DE FILTROS ELEGANTE
    // ════════════════════════════════════════════════════════════════════════════
    
    private static HBox crearPanelFiltrosElegante(FilteredList<ObservableList<String>> filteredData, List<String> encabezados) {
        HBox panel = new HBox(12);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(12, 16, 12, 16));
        panel.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 8;"
        );

        Label filterIcon = new Label("⚡");
        filterIcon.setFont(Font.font(14));

        Label filterLabel = new Label("Filtros");
        filterLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        filterLabel.setTextFill(Color.web(COLOR_TEXT()));

        // Separador
        Region sep = new Region();
        sep.setPrefWidth(1);
        sep.setPrefHeight(24);
        sep.setStyle("-fx-background-color: " + COLOR_BORDER() + ";");

        // Campo búsqueda local
        TextField txtBusqueda = new TextField();
        txtBusqueda.setPromptText("Buscar en esta tabla...");
        txtBusqueda.setPrefWidth(220);
        txtBusqueda.setPrefHeight(34);
        txtBusqueda.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-text-fill: " + COLOR_TEXT() + ";" +
            "-fx-prompt-text-fill: " + COLOR_TEXT_MUTED() + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 6;" +
            "-fx-padding: 6 12;" +
            "-fx-font-size: 12px;"
        );

        // Combo columnas
        ComboBox<String> cmbColumna = new ComboBox<>();
        cmbColumna.getItems().add("Todas");
        cmbColumna.getItems().addAll(encabezados);
        cmbColumna.setValue("Todas");
        cmbColumna.setPrefWidth(160);
        cmbColumna.setPrefHeight(34);
        cmbColumna.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 6;"
        );

        // Botón limpiar
        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setPrefHeight(34);
        btnLimpiar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + COLOR_TEXT_SECONDARY() + ";" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;"
        );
        btnLimpiar.setOnMouseEntered(e -> btnLimpiar.setStyle(
            "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
            "-fx-text-fill: " + COLOR_TEXT() + ";" +
            "-fx-font-size: 12px;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        ));
        btnLimpiar.setOnMouseExited(e -> btnLimpiar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + COLOR_TEXT_SECONDARY() + ";" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;"
        ));

        btnLimpiar.setOnAction(e -> {
            txtBusqueda.clear();
            cmbColumna.setValue("Todas");
            filteredData.setPredicate(p -> true);
        });

        // Listener para filtrado
        Runnable aplicarFiltro = () -> {
            String texto = txtBusqueda.getText().toLowerCase().trim();
            String columnaSeleccionada = cmbColumna.getValue();
            int columnaIndex = columnaSeleccionada.equals("Todas") ? -1 : encabezados.indexOf(columnaSeleccionada);

            filteredData.setPredicate(row -> {
                if (texto.isEmpty()) return true;

                if (columnaIndex >= 0) {
                    if (columnaIndex < row.size()) {
                        return row.get(columnaIndex).toLowerCase().contains(texto);
                    }
                    return false;
                } else {
                    for (String celda : row) {
                        if (celda.toLowerCase().contains(texto)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        };

        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro.run());
        cmbColumna.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro.run());

        // Espaciador
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        panel.getChildren().addAll(filterIcon, filterLabel, sep, txtBusqueda, cmbColumna, spacer, btnLimpiar);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // NAVEGACIÓN ENTRE VISTAS
    // ════════════════════════════════════════════════════════════════════════════
    
    private static void cambiarVista(String vista) {
        if (vista.equals(vistaActual)) return;
        
        String vistaAnterior = vistaActual;
        vistaActual = vista;
        
        // IMPORTANTE: Detener animaciones PRIMERO, antes de cualquier transición
        if ("estadisticas".equals(vistaAnterior)) {
            EstadisticasFX.detenerAnimaciones();
        }
        
        // Determinar dirección del slide (izquierda o derecha según orden)
        int ordenAnterior = "sistema".equals(vistaAnterior) ? 0 : "reportes".equals(vistaAnterior) ? 1 : 2;
        int ordenNuevo = "sistema".equals(vista) ? 0 : "reportes".equals(vista) ? 1 : 2;
        double direccion = ordenNuevo > ordenAnterior ? 1 : -1;
        
        // Animación de salida rápida: fade + slide sutil en dirección opuesta
        FadeTransition fadeOut = new FadeTransition(Duration.millis(120), contentArea);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(AnimacionesFX.EASE_IN_OUT);
        
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(120), contentArea);
        slideOut.setToX(-20 * direccion);
        slideOut.setInterpolator(AnimacionesFX.EASE_IN_OUT);
        
        ParallelTransition exitAnim = new ParallelTransition(fadeOut, slideOut);
        exitAnim.setOnFinished(e -> {
            // Resetear todos los botones inmediatamente (sin pausa)
            actualizarEstiloBotonNav(btnNavSistema, false, "sistema");
            actualizarEstiloBotonNav(btnNavReportes, false, "reportes");
            actualizarEstiloBotonNav(btnNavEstadisticas, false, "estadisticas");
            
            // Determinar qué panel mostrar (reusar paneles cacheados, no recrear)
            Node panelAMostrar = null;
            if ("sistema".equals(vista)) {
                // Reusar panel cacheado en vez de recrearlo cada vez
                if (panelSistema == null) {
                    panelSistema = crearContenidoPrincipal(rutaExcelGlobal);
                }
                panelAMostrar = panelSistema;
                actualizarEstiloBotonNav(btnNavSistema, true, "sistema");
            } else if ("reportes".equals(vista)) {
                panelAMostrar = panelReportes;
                actualizarEstiloBotonNav(btnNavReportes, true, "reportes");
            } else if ("estadisticas".equals(vista)) {
                // Estadísticas sí se recrea para datos frescos
                panelEstadisticas = EstadisticasFX.crearPanelEstadisticas(rutaExcelGlobal);
                panelAMostrar = panelEstadisticas;
                actualizarEstiloBotonNav(btnNavEstadisticas, true, "estadisticas");
            }
            
            if (panelAMostrar != null) {
                contentArea.getChildren().setAll(panelAMostrar);
            }
            
            // Re-animar conteos numéricos al volver a sistema
            if ("sistema".equals(vista)) {
                reanimarConteosTarjetas();
            } else if ("reportes".equals(vista)) {
                GestionReportesFX.reanimarConteosTarjetas();
            }
            
            // Animación de entrada fluida: slide suave + fade-in
            contentArea.setTranslateX(20 * direccion);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), contentArea);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(AnimacionesFX.EASE_OUT_QUART);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), contentArea);
            slideIn.setToX(0);
            slideIn.setInterpolator(AnimacionesFX.EASE_OUT_QUART);
            
            ParallelTransition enterAnim = new ParallelTransition(fadeIn, slideIn);
            enterAnim.setOnFinished(ev -> contentArea.requestLayout());
            enterAnim.play();
        });
        exitAnim.play();
    }
    
    private static void actualizarEstiloBotonNav(Button btn, boolean activo, String nombreVista) {
        String color = activo ? COLOR_PRIMARY : COLOR_TEXT_SECONDARY();
        String bgColor = activo ? COLOR_PRIMARY + "15" : "transparent";
        
        btn.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + color + ";" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        
        // Actualizar icono
        HBox contenido = (HBox) btn.getGraphic();
        if (contenido != null && contenido.getChildren().size() >= 1) {
            switch (nombreVista) {
                case "sistema":
                    contenido.getChildren().set(0, IconosSVG.computadora(color, 20));
                    break;
                case "reportes":
                    contenido.getChildren().set(0, IconosSVG.reportes(color, 20));
                    break;
                case "estadisticas":
                    contenido.getChildren().set(0, IconosSVG.estadisticas(color, 20));
                    break;
            }
            // Actualizar color del texto
            if (contenido.getChildren().size() >= 2 && contenido.getChildren().get(1) instanceof Label) {
                ((Label) contenido.getChildren().get(1)).setTextFill(Color.web(color));
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // BOTONES DEL SIDEBAR
    // ════════════════════════════════════════════════════════════════════════════
    
    private static Button crearBotonNavegacion(String texto, javafx.scene.Node icono, boolean activo) {
        return ComponentesFX.crearNavButton(texto, icono, activo, COLOR_PRIMARY);
    }
    
    private static Button crearBotonNav(String texto, String icono, boolean activo) {
        Button btn = new Button(icono + "  " + texto);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setPrefHeight(42);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 16, 0, 16));
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));

        String baseStyle = activo ? 
            "-fx-background-color: " + COLOR_PRIMARY + "15;" +
            "-fx-text-fill: " + COLOR_PRIMARY + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: transparent;" +
            "-fx-cursor: hand;" :
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + COLOR_TEXT_SECONDARY() + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: transparent;" +
            "-fx-cursor: hand;";

        btn.setStyle(baseStyle);

        if (!activo) {
            btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                "-fx-text-fill: " + COLOR_TEXT() + ";" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        }

        return btn;
    }

    private static Button crearBotonAccion(String texto, String icono, String color) {
        Button btn = new Button(icono + "  " + texto);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setPrefHeight(38);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 16, 0, 16));
        btn.setFont(Font.font("Segoe UI", 12));
        
        String baseStyle = 
            "-fx-background-color: " + color + "15;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;";
        
        btn.setStyle(baseStyle);

        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + color + "30;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));

        return btn;
    }

    /** Botón de acción con icono SVG v6.0 */
    private static Button crearBotonAccionSVG(String texto, Node icono, String color) {
        return ComponentesFX.crearActionButton(texto, icono, color);
    }

    /**
     * Crea un tooltip estilizado con el tema actual.
     */
    private static Tooltip crearTooltipModerno(String texto) {
        return ComponentesFX.crearTooltip(texto);
    }

    private static Button crearBotonVolver() {
        return ComponentesFX.crearBotonVolver("Volver al menú");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════════════════════════
    
    private static void aplicarFiltroGlobal(FilteredList<ObservableList<String>> filteredData, 
                                            List<String> encabezados, String texto) {
        String busqueda = texto.toLowerCase().trim();
        filteredData.setPredicate(row -> {
            if (busqueda.isEmpty()) return true;
            for (String celda : row) {
                if (celda.toLowerCase().contains(busqueda)) {
                    return true;
                }
            }
            return false;
        });
    }

    private static void exportarExcelSinPassword(Path rutaOriginal) {
        // Pedir destino PRIMERO (antes del hilo de fondo)
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Excel sin contraseña");
        fileChooser.setInitialFileName("Inventario_" + limpiarNombreProyecto(CURRENT_PROJECT).replace(" ", "_") + "_Export.xlsx");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx")
        );
        // Directorio inicial por proyecto
        fileChooser.setInitialDirectory(inventario.fx.config.PortablePaths.getExportProyectosDir(CURRENT_PROJECT).toFile());

        File destino = fileChooser.showSaveDialog(dashboardStage);
        if (destino == null) return;

        // Crear overlay de progreso
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        VBox progressCard = new VBox(16);
        progressCard.setAlignment(Pos.CENTER);
        progressCard.setPadding(new Insets(32));
        progressCard.setMaxWidth(320);
        progressCard.setMaxHeight(180);
        progressCard.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 8);"
        );

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(48, 48);
        spinner.setStyle("-fx-progress-color: " + TemaManager.COLOR_PRIMARY + ";");

        Label lblProgreso = new Label("Descifrando archivo...");
        lblProgreso.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        lblProgreso.setTextFill(Color.web(TemaManager.getText()));

        progressCard.getChildren().addAll(spinner, lblProgreso);
        overlay.getChildren().add(progressCard);

        // Agregar overlay al contentArea
        contentArea.getChildren().add(overlay);

        // Ejecutar en background
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Descifrando archivo...");
                XSSFWorkbook wb = abrirCifradoProyecto(rutaOriginal);
                if (wb == null) throw new Exception("No se pudo abrir el archivo");

                updateMessage("Guardando archivo...");
                try (FileOutputStream fos = new FileOutputStream(destino)) {
                    wb.write(fos);
                }
                wb.close();
                return null;
            }
        };

        lblProgreso.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            contentArea.getChildren().remove(overlay);
            NotificacionesFX.success(contentArea, "Exportación completada",
                "Archivo exportado exitosamente: " + destino.getName());
        });

        task.setOnFailed(e -> {
            contentArea.getChildren().remove(overlay);
            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
            NotificacionesFX.error(contentArea, "Error de exportación",
                "Error al exportar: " + errorMsg);
        });

        new Thread(task, "ExportExcel-Thread").start();
    }

    private static void generarReporte() {
        // Establecer la imagen y nombre del proyecto actual antes de abrir el formulario
        try {
            // Obtener datos del proyecto sin recargar toda la lista para preservar el orden
            String indice = CURRENT_PROJECT.split("\\.")[0];
            int idx = Integer.parseInt(indice) - 1;
            AdminManager.Proyecto proyectoActual = AdminManager.getProyectoPorIndice(idx);
            if (proyectoActual != null) {
                String imagenPath = proyectoActual.getImagenPath();
                System.setProperty("reporte.proyectoImagenPath", imagenPath != null ? imagenPath : "");
                System.setProperty("reporte.proyectoNombre", CURRENT_PROJECT);
                System.out.println("[DashboardFX] Imagen del proyecto establecida: " + imagenPath);
                System.out.println("[DashboardFX] Nombre del proyecto establecido: " + CURRENT_PROJECT);
            } else {
                System.setProperty("reporte.proyectoImagenPath", "");
                System.setProperty("reporte.proyectoNombre", CURRENT_PROJECT);
            }
        } catch (Exception e) {
            System.setProperty("reporte.proyectoImagenPath", "");
            System.setProperty("reporte.proyectoNombre", CURRENT_PROJECT);
            System.err.println("[DashboardFX] Error obteniendo imagen del proyecto: " + e.getMessage());
        }
        
        // Mostrar formulario para llenar datos del reporte
        ReporteFormularioFX.mostrarParaGestion(dashboardStage, (datos) -> {
            if (datos != null) {
                System.out.println("[DashboardFX] Reporte recibido del formulario, agregando a GestionReportesFX");
                
                // Agregar el reporte al sistema de gestión
                GestionReportesFX.agregarNuevoReporte(datos);
                
                System.out.println("[DashboardFX] Reporte guardado exitosamente");
                
                // Cambiar a la vista de reportes para que el usuario vea el nuevo reporte
                cambiarVista("reportes");
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CALCULAR ANCHO DE COLUMNA SEGÚN EL TIPO DE DATO
    // ════════════════════════════════════════════════════════════════════════════
    
    private static int calcularAnchoColumna(String headerName) {
        if (headerName == null) return 120;
        
        String header = headerName.toLowerCase().trim();
        
        // 🆕 Nuevas columnas para grupos
        if (header.contains("id") && header.contains("grupo")) {
            return 160; // ID Grupo: "20260119-143025-123"
        }
        
        if (header.equals("nº") || header.equals("n°") || header.equals("no.")) {
            return 50; // Número secuencial pequeño
        }
        
        // Columnas de fecha/hora - necesitan espacio para el formato completo
        if (header.contains("fecha")) {
            return 160;
        }
        
        // Columnas de usuario
        if (header.contains("usuario")) {
            return 140;
        }
        
        // Hostname/Nombre equipo
        if (header.contains("hostname") || header.contains("nombre")) {
            return 150;
        }
        
        // Sistema operativo
        if (header.contains("sistema") || header.contains("operativo")) {
            return 180;
        }
        
        // Modelo
        if (header.contains("modelo")) {
            return 160;
        }
        
        // Marca
        if (header.contains("marca")) {
            return 100;
        }
        
        // Tipo dispositivo
        if (header.contains("tipo") || header.contains("dispos")) {
            return 120;
        }   
        
        // BIOS
        if (header.contains("bios")) {
            return 180;
        }
        
        // CPU/Procesador
        if (header.contains("cpu") || header.contains("procesador")) {
            return 200;
        }
        
        // GPU/Gráfica
        if (header.contains("gpu") || header.contains("gráfica") || header.contains("grafica")) {
            return 200;
        }
        
        // RAM/Memoria
        if (header.contains("ram") || header.contains("memoria")) {
            return 120;
        }
        
        // Discos
        if (header.contains("disco")) {
            return 180;
        }
        
        // Número de discos u otros números
        if (header.contains("n°") || header.contains("no.") || header.contains("num") || header.contains("cantidad")) {
            return 80;
        }
        
        // MAC Address
        if (header.contains("mac")) {
            return 160;
        }
        
        // IP
        if (header.contains("ip")) {
            return 130;
        }
        
        // Serial
        if (header.contains("serial")) {
            return 160;
        }
        
        // Default para otras columnas
        return 140;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // FUNCIONES DE EDICIÓN Y ELIMINACIÓN DE REGISTROS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Busca el ID Grupo de la primera aplicación que coincida con el hostname dado
     */
    private static String buscarIdGrupoPorHostname(Path rutaExcel, String hostname) {
        if (hostname == null || hostname.trim().isEmpty()) {
            return null;
        }
        
        XSSFWorkbook wb = null;
        try {
            wb = abrirCifradoProyecto(rutaExcel);
            if (wb == null) return null;
            
            // Buscar hoja de aplicaciones
            Sheet hojaApps = wb.getSheet("Aplicaciones");
            if (hojaApps == null) {
                hojaApps = wb.getSheet("InstalledApps");
            }
            if (hojaApps == null) return null;
            
            // Obtener columnas
            Row header = hojaApps.getRow(1);
            if (header == null) return null;
            
            Map<String, Integer> cols = new HashMap<>();
            for (int c = 0; c < header.getLastCellNum(); c++) {
                org.apache.poi.ss.usermodel.Cell cell = header.getCell(c);
                if (cell != null) {
                    cols.put(cell.getStringCellValue().trim(), c);
                }
            }
            
            int colIdGrupo = cols.getOrDefault("ID Grupo", 0);
            int colHostname = cols.getOrDefault("Hostname", 4);
            
            // Buscar la primera fila que coincida con el hostname
            for (int i = 2; i <= hojaApps.getLastRowNum(); i++) {
                Row r = hojaApps.getRow(i);
                if (r != null) {
                    org.apache.poi.ss.usermodel.Cell cH = r.getCell(colHostname);
                    org.apache.poi.ss.usermodel.Cell cId = r.getCell(colIdGrupo);
                    
                    if (cH != null && cId != null) {
                        String hApp = cH.toString().trim();
                        if (hApp.equals(hostname)) {
                            String idGrupo = cId.toString().trim();
                            return idGrupo;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Dashboard] Error buscando ID Grupo: " + e.getMessage());
        } finally {
            if (wb != null) {
                try { wb.close(); } catch (Exception ignored) {}
            }
        }
        
        return null;
    }

    /**
     * Elimina SOLO un registro individual (una fila) sin tocar aplicaciones ni grupos.
     * Para eliminar solo esta línea de la tabla.
     */
    // MÉTODO REESCRITO DESDE CERO: Elimina una fila individual de Excel y de la tabla, robusto y seguro
    private static void eliminarRegistroIndividual(Path rutaExcel, Sheet hoja, 
                                                   ObservableList<ObservableList<String>> datosOriginales, 
                                                   ObservableList<String> filaSeleccionada, int numeroFila) {
        StringBuilder preview = new StringBuilder();
        int maxCampos = Math.min(5, filaSeleccionada.size());
        for (int i = 0; i < maxCampos; i++) {
            if (i > 0) preview.append("\n");
            preview.append(filaSeleccionada.get(i));
        }
        if (filaSeleccionada.size() > 5) preview.append("\n...");
        
        boolean confirmar = DialogosFX.confirmarEliminacion(
            dashboardStage,
            "Eliminar registro",
            "¿Eliminar este registro individual?",
            preview.toString()
        );
        
        if (confirmar) {
            try (XSSFWorkbook wb = abrirCifradoProyecto(rutaExcel)) {
                    if (wb == null) {
                        NotificacionesFX.error(contentArea, "Error", "No se pudo abrir el archivo.");
                        return;
                    }
                    Sheet hojaActualizada = wb.getSheet(hoja.getSheetName());
                    if (hojaActualizada == null) {
                        NotificacionesFX.error(contentArea, "Error", "No se encontró la hoja: " + hoja.getSheetName());
                        return;
                    }
                    int lastRow = hojaActualizada.getLastRowNum();
                    if (numeroFila < 2 || numeroFila > lastRow) {
                        NotificacionesFX.error(contentArea, "Error", "Fila fuera de rango: " + numeroFila);
                        return;
                    }
                    Row filaExcel = hojaActualizada.getRow(numeroFila);
                    if (filaExcel == null) {
                        NotificacionesFX.error(contentArea, "Error", "La fila seleccionada no existe.");
                        return;
                    }
                    // Eliminar la fila físicamente
                    hojaActualizada.removeRow(filaExcel);
                    if (numeroFila < lastRow) {
                        hojaActualizada.shiftRows(numeroFila + 1, lastRow, -1);
                    }
                    // Guardar cambios
                    guardarCifradoProyecto(rutaExcel, wb);
                } catch (Exception ex) {
                    logger.error("Error al eliminar el registro: " + ex.getMessage(), ex);
                    NotificacionesFX.error(contentArea, "Error", "Error al eliminar el registro: " + ex.getMessage());
                    return;
                }
            // Eliminar de la tabla en memoria
            Platform.runLater(() -> {
                datosOriginales.remove(filaSeleccionada);
                actualizarTarjetasEstadisticas(rutaExcel);
                EstadisticasFX.actualizarAhora();
                invalidarPanelSistema();
            });
            
            NotificacionesFX.success(contentArea, "Registro Eliminado",
                "El registro ha sido eliminado correctamente.");
        }
    }

    /**
     * Elimina un registro de la hoja de Excel (TODO EL EQUIPO con sus aplicaciones)
     */
    private static void eliminarRegistro(Path rutaExcel, Sheet hoja, ObservableList<ObservableList<String>> datosOriginales, 
                                         ObservableList<String> filaSeleccionada, int numeroFila) {
        
        // Determinar si es hoja Sistema o Aplicaciones
        final boolean esHojaSistema = hoja.getSheetName().equalsIgnoreCase("SystemInfo") || hoja.getSheetName().equalsIgnoreCase("Sistema");
        final boolean esHojaAplicaciones = hoja.getSheetName().equalsIgnoreCase("Aplicaciones");
        
        // Buscar el ID Grupo para eliminar las aplicaciones
        final String idGrupoParaEliminar;
        final String hostnameEquipo;
        
        if (esHojaSistema && filaSeleccionada.size() >= 3) {
            // Para Sistema: extraer hostname y buscar el ID Grupo en la hoja de aplicaciones
            hostnameEquipo = filaSeleccionada.get(2);
            idGrupoParaEliminar = buscarIdGrupoPorHostname(rutaExcel, hostnameEquipo);
            System.out.println("[Dashboard] Sistema: hostname='" + hostnameEquipo + "', ID Grupo encontrado='" + idGrupoParaEliminar + "'");
        } else if (esHojaAplicaciones && !filaSeleccionada.isEmpty() && filaSeleccionada.get(0) != null && !filaSeleccionada.get(0).trim().isEmpty()) {
            // Para Aplicaciones: el ID Grupo está en la columna 0
            idGrupoParaEliminar = filaSeleccionada.get(0);
            hostnameEquipo = filaSeleccionada.size() > 4 ? filaSeleccionada.get(4) : "desconocido";
            System.out.println("[Dashboard] Aplicación: ID Grupo='" + idGrupoParaEliminar + "'");
        } else {
            idGrupoParaEliminar = null;
            hostnameEquipo = null;
        }
        
        // Determinar si tiene grupo de aplicaciones
        final boolean tieneGrupo = (idGrupoParaEliminar != null && !idGrupoParaEliminar.isEmpty());
        
        String tituloConfirmacion;
        String preguntaConfirmacion;
        String detallesConfirmacion = null;
        
        if (esHojaSistema && tieneGrupo) {
            // Es un registro del sistema con posibles aplicaciones
            // Contar cuántas aplicaciones tiene este ID Grupo
            long totalAplicaciones = 0;
            XSSFWorkbook wbTemp = null;
            try {
                wbTemp = abrirCifradoProyecto(rutaExcel);
                
                if (wbTemp != null) {
                    // Intentar varios nombres posibles para la hoja de aplicaciones
                    Sheet hojaAppsTemp = wbTemp.getSheet("Aplicaciones");
                    if (hojaAppsTemp == null) {
                        hojaAppsTemp = wbTemp.getSheet("InstalledApps");
                    }
                    
                    if (hojaAppsTemp != null) {
                        Row headerTemp = hojaAppsTemp.getRow(1);
                        
                        if (headerTemp != null) {
                            Map<String, Integer> colsTemp = new HashMap<>();
                            for (int c = 0; c < headerTemp.getLastCellNum(); c++) {
                                org.apache.poi.ss.usermodel.Cell cell = headerTemp.getCell(c);
                                if (cell != null) {
                                    colsTemp.put(cell.getStringCellValue().trim(), c);
                                }
                            }
                            
                            int colIdGrupo = colsTemp.getOrDefault("ID Grupo", 0);
                            
                            // Contar aplicaciones con el mismo ID Grupo
                            for (int i = 2; i <= hojaAppsTemp.getLastRowNum(); i++) {
                                Row r = hojaAppsTemp.getRow(i);
                                if (r != null) {
                                    org.apache.poi.ss.usermodel.Cell cId = r.getCell(colIdGrupo);
                                    if (cId != null) {
                                        String idApp = cId.toString().trim();
                                        if (idApp.equals(idGrupoParaEliminar)) {
                                            totalAplicaciones++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[Dashboard] Error al contar aplicaciones: " + e.getMessage());
            } finally {
                // CRÍTICO: Cerrar el workbook temporal ANTES de continuar
                if (wbTemp != null) {
                    try { 
                        wbTemp.close();
                    } catch (Exception e) {
                        System.err.println("[Dashboard] Error al cerrar workbook temporal: " + e.getMessage());
                    }
                }
            }
            
            tituloConfirmacion = "Eliminar equipo";
            preguntaConfirmacion = "¿Eliminar este equipo y sus aplicaciones?";
            detallesConfirmacion = "Este registro tiene " + totalAplicaciones + " aplicaciones asociadas (ID Grupo: " + idGrupoParaEliminar + ").\n\n• 1 registro del equipo\n• " + totalAplicaciones + " aplicaciones";
        } else if (esHojaAplicaciones && tieneGrupo) {
            // Registro de aplicación individual con ID de grupo
            tituloConfirmacion = "Eliminar aplicación";
            preguntaConfirmacion = "¿Eliminar esta aplicación?";
            detallesConfirmacion = "Grupo: " + idGrupoParaEliminar;
        } else {
            // Registro sin grupo o es de la hoja Aplicaciones
            tituloConfirmacion = "Eliminar registro";
            preguntaConfirmacion = "¿Está seguro de eliminar este registro?";
            
            // Mostrar preview de los datos a eliminar
            StringBuilder preview = new StringBuilder();
            for (String valor : filaSeleccionada) {
                if (preview.length() > 0) preview.append("\n");
                preview.append(valor);
                if (preview.length() > 200) {
                    preview.append("\n...");
                    break;
                }
            }
            detallesConfirmacion = preview.toString();
        }
        
        boolean confirmar = DialogosFX.confirmarEliminacion(
            dashboardStage,
            tituloConfirmacion,
            preguntaConfirmacion,
            detallesConfirmacion
        );
        
        if (confirmar) {
                XSSFWorkbook wb = null;
                try {
                    // Abrir el workbook
                    wb = abrirCifradoProyecto(rutaExcel);
                    if (wb == null) {
                        NotificacionesFX.error(contentArea, "Error",
                            "No se pudo abrir el archivo.");
                        return;
                    }
                    
                    // Obtener la hoja actualizada
                    Sheet hojaActualizada = wb.getSheet(hoja.getSheetName());
                    
                    int registrosEliminados = 1; // Al menos el registro actual
                    
                    // Si es hoja Sistema, eliminar también las apps asociadas por hostname + fecha
                    if (esHojaSistema && tieneGrupo) {
                        // Buscar y eliminar todas las aplicaciones con el mismo hostname y fecha
                        Sheet hojaAplicaciones = wb.getSheet("Aplicaciones");
                        if (hojaAplicaciones == null) {
                            hojaAplicaciones = wb.getSheet("InstalledApps");
                        }
                        
                        if (hojaAplicaciones != null) {
                            // Obtener índices de columnas desde el header de Aplicaciones
                            Row headerApps = hojaAplicaciones.getRow(1);
                            if (headerApps != null) {
                                Map<String, Integer> columnasApps = new HashMap<>();
                                for (int c = 0; c < headerApps.getLastCellNum(); c++) {
                                    org.apache.poi.ss.usermodel.Cell cell = headerApps.getCell(c);
                                    if (cell != null) {
                                        columnasApps.put(cell.getStringCellValue().trim(), c);
                                    }
                                }
                                
                                int colIdGrupo = columnasApps.getOrDefault("ID Grupo", 0);
                                
                                // Encontrar todas las filas con el mismo ID Grupo
                                List<Integer> filasAppAEliminar = new ArrayList<>();
                                
                                for (int i = 2; i <= hojaAplicaciones.getLastRowNum(); i++) { // Empezar en 2 (saltar título y header)
                                    Row filaApp = hojaAplicaciones.getRow(i);
                                    if (filaApp != null) {
                                        org.apache.poi.ss.usermodel.Cell celdaId = filaApp.getCell(colIdGrupo);
                                        
                                        if (celdaId != null) {
                                            String idApp = celdaId.toString().trim();
                                            if (idApp.equals(idGrupoParaEliminar)) {
                                                filasAppAEliminar.add(i);
                                            }
                                        }
                                    }
                                }
                                
                                // Eliminar filas de mayor a menor para evitar problemas con índices
                                Collections.reverse(filasAppAEliminar);
                                for (int numeroFilaApp : filasAppAEliminar) {
                                    if (numeroFilaApp <= hojaAplicaciones.getLastRowNum()) {
                                        Row filaApp = hojaAplicaciones.getRow(numeroFilaApp);
                                        if (filaApp != null) {
                                            hojaAplicaciones.removeRow(filaApp);
                                            if (numeroFilaApp < hojaAplicaciones.getLastRowNum()) {
                                                hojaAplicaciones.shiftRows(numeroFilaApp + 1, hojaAplicaciones.getLastRowNum(), -1);
                                            }
                                            registrosEliminados++;
                                        }
                                    }
                                }
                                
                                System.out.println("[Dashboard] Eliminadas " + filasAppAEliminar.size() + " aplicaciones del equipo: " + hostnameEquipo + " (ID Grupo: " + idGrupoParaEliminar + ")");
                            }
                        }
                    }
                    
                    // Eliminar la fila del equipo
                    if (numeroFila <= hojaActualizada.getLastRowNum()) {
                        hojaActualizada.removeRow(hojaActualizada.getRow(numeroFila));
                        
                        // Shift remaining rows up
                        if (numeroFila < hojaActualizada.getLastRowNum()) {
                            hojaActualizada.shiftRows(numeroFila + 1, hojaActualizada.getLastRowNum(), -1);
                        }
                    }
                    
                    // Guardar el workbook cifrado con validación
                    try {
                        System.out.println("[Dashboard] Guardando cambios en: " + rutaExcel);
                        guardarCifradoProyecto(rutaExcel, wb);
                        System.out.println("[Dashboard] ✓ Datos guardados exitosamente");
                    } catch (Exception saveEx) {
                        logger.error("[Dashboard] ✗ ERROR al guardar: " + saveEx.getMessage(), saveEx);
                        throw saveEx; // Re-lanzar para que se maneje arriba
                    }
                    
                    // Cerrar después de guardar
                    wb.close();
                    
                    // ⚡ RECARGAR DATOS EN TIEMPO REAL
                    Platform.runLater(() -> {
                        try {
                            // Limpiar datos actuales
                            datosOriginales.clear();
                            
                            // Recargar desde el Excel actualizado
                            XSSFWorkbook wbReload = abrirCifradoProyecto(rutaExcel);
                            if (wbReload != null) {
                                Sheet hojaReload = wbReload.getSheet(hoja.getSheetName());
                                if (hojaReload != null) {
                                    java.time.format.DateTimeFormatter sdfReload = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    int columnasReload = hojaReload.getRow(1).getLastCellNum();
                                    
                                    for (int i = 2; i <= hojaReload.getLastRowNum(); i++) {
                                        Row rowReload = hojaReload.getRow(i);
                                        if (rowReload == null) continue;
                                        
                                        ObservableList<String> filaReload = FXCollections.observableArrayList();
                                        for (int j = 0; j < columnasReload; j++) {
                                            org.apache.poi.ss.usermodel.Cell cellReload = rowReload.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                                            String valorReload = "";
                                            if (cellReload.getCellType() == CellType.STRING) {
                                                valorReload = cellReload.getStringCellValue();
                                            } else if (cellReload.getCellType() == CellType.NUMERIC) {
                                                if (DateUtil.isCellDateFormatted(cellReload)) {
                                                    valorReload = cellReload.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(sdfReload);
                                                } else {
                                                    valorReload = String.valueOf((int) cellReload.getNumericCellValue());
                                                }
                                            }
                                            filaReload.add(valorReload);
                                        }
                                        datosOriginales.add(filaReload);
                                    }
                                }
                                wbReload.close();
                            }
                            
                            // ⚡ ACTUALIZAR TARJETAS DE ESTADÍSTICAS
                            actualizarTarjetasEstadisticas(rutaExcel);
                            
                            // ⚡ FORZAR ACTUALIZACIÓN INMEDIATA DEL RANKING
                            EstadisticasFX.actualizarAhora();
                            
                            // Invalidar caché del panel sistema para reconstruirlo con datos frescos
                            invalidarPanelSistema();
                            
                        } catch (Exception ex) {
                            System.err.println("[Dashboard] Error recargando datos: " + ex.getMessage());
                        }
                    });
                    
                    // Mostrar notificación con detalles
                    String detallesExito;
                    if (registrosEliminados > 1) {
                        detallesExito = "• 1 registro del equipo\n• " + (registrosEliminados - 1) + " aplicaciones asociadas";
                    } else {
                        detallesExito = "• 1 registro eliminado";
                    }
                    
                    NotificacionesFX.success(contentArea, "Eliminación completada",
                        "Total: " + registrosEliminados + " registro" + (registrosEliminados != 1 ? "s" : "") + " eliminado" + (registrosEliminados != 1 ? "s" : ""));
                    
                } catch (Exception ex) {
                    logger.error("Error al eliminar el registro: " + ex.getMessage(), ex);
                    NotificacionesFX.error(contentArea, "Error",
                        "Error al eliminar el registro: " + ex.getMessage());
                } finally {
                    // Asegurarse de cerrar el workbook
                    if (wb != null) {
                        try {
                            wb.close();
                        } catch (Exception e) {
                            System.err.println("[Dashboard] Error cerrando workbook: " + e.getMessage());
                        }
                    }
                }
        }
    }

    /**
     * Copia las celdas seleccionadas al portapapeles con feedback visual
     */
    private static void copiarCeldasSeleccionadas(TableView<ObservableList<String>> tabla, StackPane contenedor) {
        ObservableList<TablePosition> celdasSeleccionadas = tabla.getSelectionModel().getSelectedCells();
        
        if (celdasSeleccionadas == null || celdasSeleccionadas.isEmpty()) {
            return; // No hay nada seleccionado
        }
        
        // Organizar celdas por fila y columna para copiar en formato tabla
        java.util.Map<Integer, java.util.Map<Integer, String>> matrizCeldas = new java.util.TreeMap<>();
        
        for (TablePosition pos : celdasSeleccionadas) {
            int fila = pos.getRow();
            int columna = pos.getColumn();
            
            if (fila >= 0 && columna >= 0 && fila < tabla.getItems().size()) {
                ObservableList<String> filaData = tabla.getItems().get(fila);
                if (columna < filaData.size()) {
                    matrizCeldas.computeIfAbsent(fila, k -> new java.util.TreeMap<>())
                              .put(columna, filaData.get(columna));
                }
            }
        }
        
        // Construir texto para portapapeles (formato TSV)
        StringBuilder contenidoCopiado = new StringBuilder();
        for (java.util.Map.Entry<Integer, java.util.Map<Integer, String>> filaEntry : matrizCeldas.entrySet()) {
            java.util.Map<Integer, String> columnasEnFila = filaEntry.getValue();
            java.util.List<String> valoresColumnas = new java.util.ArrayList<>(columnasEnFila.values());
            contenidoCopiado.append(String.join("\t", valoresColumnas));
            contenidoCopiado.append("\n");
        }
        
        // Copiar al portapapeles
        ClipboardContent content = new ClipboardContent();
        content.putString(contenidoCopiado.toString());
        Clipboard.getSystemClipboard().setContent(content);
        
        // 🎨 Feedback visual
        int numCeldas = celdasSeleccionadas.size();
        Label labelCopiado = new Label("✓ " + numCeldas + " celda" + (numCeldas != 1 ? "s" : "") + " copiada" + (numCeldas != 1 ? "s" : ""));
        labelCopiado.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-background-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
        );
        labelCopiado.setOpacity(0);
        
        contenedor.getChildren().add(labelCopiado);
        StackPane.setAlignment(labelCopiado, Pos.BOTTOM_CENTER);
        StackPane.setMargin(labelCopiado, new Insets(0, 0, 30, 0));
        
        // Animación
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), labelCopiado);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(200), labelCopiado);
        slideUp.setFromY(20);
        slideUp.setToY(0);
        
        ParallelTransition entrada = new ParallelTransition(fadeIn, slideUp);
        PauseTransition pausa = new PauseTransition(Duration.seconds(1.5));
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), labelCopiado);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> contenedor.getChildren().remove(labelCopiado));
        
        SequentialTransition secuencia = new SequentialTransition(entrada, pausa, fadeOut);
        secuencia.play();
    }

    /**
     * Copia las celdas seleccionadas de tablas secundarias al portapapeles
     */
    private static void copiarCeldasTabSecundaria(TableView<ObservableList<String>> tabla, VBox contenedor) {
        ObservableList<TablePosition> celdasSeleccionadas = tabla.getSelectionModel().getSelectedCells();
        
        if (celdasSeleccionadas == null || celdasSeleccionadas.isEmpty()) {
            return;
        }
        
        // Organizar celdas por fila y columna
        java.util.Map<Integer, java.util.Map<Integer, String>> matrizCeldas = new java.util.TreeMap<>();
        
        for (TablePosition pos : celdasSeleccionadas) {
            int fila = pos.getRow();
            int columna = pos.getColumn();
            
            if (fila >= 0 && columna >= 0 && fila < tabla.getItems().size()) {
                ObservableList<String> filaData = tabla.getItems().get(fila);
                if (columna < filaData.size()) {
                    matrizCeldas.computeIfAbsent(fila, k -> new java.util.TreeMap<>())
                              .put(columna, filaData.get(columna));
                }
            }
        }
        
        // Construir contenido para portapapeles
        StringBuilder contenidoCopiado = new StringBuilder();
        for (java.util.Map.Entry<Integer, java.util.Map<Integer, String>> filaEntry : matrizCeldas.entrySet()) {
            java.util.Map<Integer, String> columnasEnFila = filaEntry.getValue();
            java.util.List<String> valoresColumnas = new java.util.ArrayList<>(columnasEnFila.values());
            contenidoCopiado.append(String.join("\t", valoresColumnas));
            contenidoCopiado.append("\n");
        }
        
        // Copiar al portapapeles
        ClipboardContent content = new ClipboardContent();
        content.putString(contenidoCopiado.toString());
        Clipboard.getSystemClipboard().setContent(content);
        
        // Feedback visual
        int numCeldas = celdasSeleccionadas.size();
        Label labelCopiado = new Label("✓ " + numCeldas + " celda" + (numCeldas != 1 ? "s" : "") + " copiada" + (numCeldas != 1 ? "s" : ""));
        labelCopiado.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-background-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
        );
        labelCopiado.setOpacity(0);
        labelCopiado.setMaxWidth(Double.MAX_VALUE);
        labelCopiado.setAlignment(Pos.CENTER);
        
        contenedor.getChildren().add(labelCopiado);
        VBox.setMargin(labelCopiado, new Insets(0, 20, 20, 20));
        
        // Animación
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), labelCopiado);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(0.95);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), labelCopiado);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        
        ParallelTransition entrada = new ParallelTransition(fadeIn, scaleIn);
        PauseTransition pausa = new PauseTransition(Duration.seconds(1.5));
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), labelCopiado);
        fadeOut.setFromValue(0.95);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> contenedor.getChildren().remove(labelCopiado));
        
        SequentialTransition secuencia = new SequentialTransition(entrada, pausa, fadeOut);
        secuencia.play();
    }

    /**
     * Edita un registro de la hoja de Excel
     */
    private static void editarRegistro(Path rutaExcel, Sheet hoja, List<String> encabezados, 
                                       ObservableList<String> filaSeleccionada, int numeroFila,
                                       TableView<ObservableList<String>> tabla) {
        // Crear diálogo de edición con diseño moderno
        Stage dialogoEdicion = new Stage();
        dialogoEdicion.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogoEdicion.initOwner(dashboardStage);
        dialogoEdicion.setTitle("Editar Registro");
        try {
            java.io.InputStream iconStream = DashboardFX.class.getResourceAsStream("/images/Selcomp_logito.png");
            if (iconStream != null) {
                dialogoEdicion.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }
        
        // Contenedor principal
        VBox contenedorPrincipal = new VBox();
        contenedorPrincipal.setStyle("-fx-background-color: " + COLOR_BG() + ";");
        
        // ═══════════════════════════════════════════════════════════════════════
        // HEADER - Barra superior con título y botón cerrar
        // ═══════════════════════════════════════════════════════════════════════
        HBox header = new HBox();
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        // Icono de editar
        StackPane iconoHeader = new StackPane();
        iconoHeader.setPrefSize(40, 40);
        javafx.scene.shape.Circle iconoBg = new javafx.scene.shape.Circle(20);
        iconoBg.setFill(Color.web(COLOR_PRIMARY + "20"));
        javafx.scene.Node iconoEditar = IconosSVG.editar(COLOR_PRIMARY, 20);
        iconoHeader.getChildren().addAll(iconoBg, iconoEditar);
        
        // Título
        Label titulo = new Label("Editar Registro");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.web(COLOR_TEXT()));
        HBox.setMargin(titulo, new Insets(0, 0, 0, 12));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(iconoHeader, titulo, spacer);
        
        // ═══════════════════════════════════════════════════════════════════════
        // BODY - Área de contenido con scroll
        // ═══════════════════════════════════════════════════════════════════════
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
            "-fx-background: " + COLOR_BG() + ";" +
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-border-color: transparent;"
        );
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Contenedor de campos
        VBox contenedorCampos = new VBox(16);
        contenedorCampos.setPadding(new Insets(20, 24, 20, 24));
        
        // Crear campos de texto mejorados
        List<TextField> campos = new ArrayList<>();
        for (int i = 0; i < encabezados.size(); i++) {
            VBox campoBox = new VBox(6);
            
            // Label del campo con icono opcional
            HBox labelBox = new HBox(6);
            labelBox.setAlignment(Pos.CENTER_LEFT);
            
            Label lblCampo = new Label(encabezados.get(i));
            lblCampo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            lblCampo.setTextFill(Color.web(COLOR_TEXT()));
            lblCampo.setStyle("-fx-text-transform: uppercase; -fx-letter-spacing: 0.5px;");
            
            labelBox.getChildren().add(lblCampo);
            
            // TextField con diseño mejorado
            TextField txtCampo = new TextField();
            if (i < filaSeleccionada.size()) {
                txtCampo.setText(filaSeleccionada.get(i));
            }
            txtCampo.setPrefHeight(44);
            txtCampo.setFont(Font.font("Segoe UI", 14));
            txtCampo.setStyle(
                "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                "-fx-text-fill: " + COLOR_TEXT() + ";" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 12 0 12;" +
                "-fx-font-size: 14px;"
            );
            
            // Efecto focus mejorado
            txtCampo.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (isNowFocused) {
                    txtCampo.setStyle(
                        "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                        "-fx-text-fill: " + COLOR_TEXT() + ";" +
                        "-fx-border-color: " + COLOR_PRIMARY + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 0 12 0 12;" +
                        "-fx-font-size: 14px;"
                    );
                } else {
                    txtCampo.setStyle(
                        "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                        "-fx-text-fill: " + COLOR_TEXT() + ";" +
                        "-fx-border-color: " + COLOR_BORDER() + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 0 12 0 12;" +
                        "-fx-font-size: 14px;"
                    );
                }
            });
            
            campos.add(txtCampo);
            
            campoBox.getChildren().addAll(labelBox, txtCampo);
            contenedorCampos.getChildren().add(campoBox);
        }
        
        scrollPane.setContent(contenedorCampos);
        
        // ═══════════════════════════════════════════════════════════════════════
        // FOOTER - Barra inferior con botones de acción
        // ═══════════════════════════════════════════════════════════════════════
        HBox footer = new HBox(12);
        footer.setPadding(new Insets(16, 24, 20, 24));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 1 0 0 0;"
        );
        
        // Botón Cancelar
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(120);
        btnCancelar.setPrefHeight(42);
        btnCancelar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnCancelar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + COLOR_TEXT() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        
        btnCancelar.setOnMouseEntered(e -> {
            btnCancelar.setStyle(
                "-fx-background-color: " + COLOR_BG_LIGHT() + ";" +
                "-fx-text-fill: " + COLOR_TEXT() + ";" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            );
        });
        
        btnCancelar.setOnMouseExited(e -> {
            btnCancelar.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + COLOR_TEXT() + ";" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            );
        });
        
        btnCancelar.setOnAction(e -> dialogoEdicion.close());
        
        // Botón Guardar  
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setGraphic(IconosSVG.check("#FFFFFF", 16));
        btnGuardar.setPrefWidth(120);
        btnGuardar.setPrefHeight(42);
        btnGuardar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnGuardar.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + ";" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        
        btnGuardar.setOnMouseEntered(e -> {
            btnGuardar.setStyle(
                "-fx-background-color: derive(" + COLOR_PRIMARY + ", -10%);" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.02;" +
                "-fx-scale-y: 1.02;"
            );
        });
        
        btnGuardar.setOnMouseExited(e -> {
            btnGuardar.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY + ";" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            );
        });
        
        btnGuardar.setOnAction(e -> {
            try {
                // Abrir el workbook
                XSSFWorkbook wb = abrirCifradoProyecto(rutaExcel);
                if (wb == null) {
                    NotificacionesFX.error(contentArea, "Error",
                        "No se pudo abrir el archivo.");
                    return;
                }
                
                // Obtener la hoja actualizada
                Sheet hojaActualizada = wb.getSheet(hoja.getSheetName());
                Row fila = hojaActualizada.getRow(numeroFila);
                
                if (fila == null) {
                    fila = hojaActualizada.createRow(numeroFila);
                }
                
                // Actualizar los valores de las celdas
                for (int i = 0; i < campos.size(); i++) {
                    org.apache.poi.ss.usermodel.Cell celda = fila.getCell(i);
                    if (celda == null) {
                        celda = fila.createCell(i);
                    }
                    celda.setCellValue(campos.get(i).getText());
                    
                    // Actualizar también en la lista observable
                    if (i < filaSeleccionada.size()) {
                        filaSeleccionada.set(i, campos.get(i).getText());
                    }
                }
                
                // Guardar el workbook cifrado con validación
                try {
                    System.out.println("[Dashboard] Guardando edición en: " + rutaExcel);
                    guardarCifradoProyecto(rutaExcel, wb);
                    System.out.println("[Dashboard] ✓ Edición guardada exitosamente");
                } catch (Exception saveEx) {
                    logger.error("[Dashboard] ✗ ERROR al guardar edición: " + saveEx.getMessage(), saveEx);
                    throw saveEx;
                }
                wb.close();
                
                dialogoEdicion.close();
                
                // Refrescar la tabla para mostrar los cambios
                Platform.runLater(() -> {
                    if (tabla != null) {
                        tabla.refresh();
                    }
                });
                
                // Mostrar notificación de éxito
                NotificacionesFX.success(contentArea, "Registro Actualizado",
                    "El registro se actualizó correctamente.");
                
            } catch (Exception ex) {
                logger.error("Error al guardar los cambios: " + ex.getMessage(), ex);
                NotificacionesFX.error(contentArea, "Error",
                    "Error al guardar los cambios: " + ex.getMessage());
            }
        });
        
        footer.getChildren().addAll(btnCancelar, btnGuardar);
        
        // Ensamblar el diálogo
        contenedorPrincipal.getChildren().addAll(header, scrollPane, footer);
        
        Scene escena = new Scene(contenedorPrincipal, 550, 650);
        dialogoEdicion.setScene(escena);
        dialogoEdicion.setOnShown(e -> inventario.fx.util.AnimacionesFX.animarEntradaDialogo(contenedorPrincipal));
        dialogoEdicion.showAndWait();
    }

    /**
     * Elimina múltiples registros seleccionados (solo las filas, sin tocar aplicaciones relacionadas)
     */
    private static void eliminarRegistrosMultiples(Path rutaExcel, Sheet hoja, 
                                                   ObservableList<ObservableList<String>> datosOriginales,
                                                   List<ObservableList<String>> filasSeleccionadas) {
        if (filasSeleccionadas == null || filasSeleccionadas.isEmpty()) {
            NotificacionesFX.warning(contentArea,
                "Advertencia", "No hay registros seleccionados.");
            return;
        }
        
        boolean confirmar = DialogosFX.confirmarEliminacionMultiple(dashboardStage, filasSeleccionadas.size());
        
        if (confirmar) {
                XSSFWorkbook wb = null;
                try {
                    wb = abrirCifradoProyecto(rutaExcel);
                    if (wb == null) {
                        NotificacionesFX.error(contentArea, "Error",
                            "No se pudo abrir el archivo.");
                        return;
                    }
                    
                    Sheet hojaActualizada = wb.getSheet(hoja.getSheetName());
                    if (hojaActualizada == null) {
                        NotificacionesFX.error(contentArea, "Error",
                            "No se encontró la hoja: " + hoja.getSheetName());
                        return;
                    }
                    
                    // Crear lista de números de fila a eliminar (ordenar de mayor a menor)
                    List<Integer> filasAEliminar = new ArrayList<>();
                    for (ObservableList<String> fila : filasSeleccionadas) {
                        int indice = datosOriginales.indexOf(fila);
                        if (indice >= 0) {
                            filasAEliminar.add(indice + 2); // +2 por título y header
                        }
                    }
                    
                    // Ordenar de mayor a menor para no afectar los índices al eliminar
                    filasAEliminar.sort(Collections.reverseOrder());
                    
                    System.out.println("[Dashboard] Eliminando " + filasAEliminar.size() + " filas: " + filasAEliminar);
                    
                    // Eliminar filas una por una
                    int eliminadas = 0;
                    for (int numeroFila : filasAEliminar) {
                        if (numeroFila >= 2 && numeroFila <= hojaActualizada.getLastRowNum()) {
                            Row filaExcel = hojaActualizada.getRow(numeroFila);
                            if (filaExcel != null) {
                                hojaActualizada.removeRow(filaExcel);
                                
                                // Shift rows up
                                if (numeroFila < hojaActualizada.getLastRowNum()) {
                                    hojaActualizada.shiftRows(numeroFila + 1, hojaActualizada.getLastRowNum(), -1);
                                }
                                eliminadas++;
                            }
                        }
                    }
                    
                    System.out.println("[Dashboard] Eliminadas " + eliminadas + " filas exitosamente");
                    
                    // Guardar el workbook cifrado
                    try {
                        System.out.println("[Dashboard] Guardando eliminación múltiple en: " + rutaExcel);
                        guardarCifradoProyecto(rutaExcel, wb);
                        System.out.println("[Dashboard] ✓ Datos guardados exitosamente");
                    } catch (Exception saveEx) {
                        logger.error("[Dashboard] ✗ ERROR al guardar: " + saveEx.getMessage(), saveEx);
                        throw saveEx;
                    }
                    
                    wb.close();
                    
                    // ⚡ ACTUALIZAR INTERFAZ EN TIEMPO REAL
                    Platform.runLater(() -> {
                        try {
                            // Remover de la lista observable
                            datosOriginales.removeAll(filasSeleccionadas);
                            
                            // Actualizar estadísticas
                            actualizarTarjetasEstadisticas(rutaExcel);
                            EstadisticasFX.actualizarAhora();
                            invalidarPanelSistema();
                            
                        } catch (Exception ex) {
                            System.err.println("[Dashboard] Error actualizando interfaz: " + ex.getMessage());
                        }
                    });
                    
                    // Mostrar confirmación
                    NotificacionesFX.success(contentArea, "Registros Eliminados",
                        eliminadas + " registros eliminados correctamente.");
                    
                } catch (Exception ex) {
                    logger.error("Error al eliminar registros: " + ex.getMessage(), ex);
                    NotificacionesFX.error(contentArea, "Error",
                        "Error al eliminar registros: " + ex.getMessage());
                } finally {
                    // Asegurar cierre del workbook
                    if (wb != null) {
                        try {
                            wb.close();
                        } catch (Exception e) {
                            System.err.println("[Dashboard] Error cerrando workbook: " + e.getMessage());
                        }
                    }
                }
            }
    }
    
    /**
     * Verifica si una fila está vacía (sin datos significativos)
     */
    private static boolean esFilaVacia(Row row) {
        if (row == null) return true;
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(i);
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

    // ═══════════════════════════════════════════════════════════════════════
    // PLACEHOLDER VACÍO PARA TABLAS Y PANELES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Crea un placeholder estilizado para estados vacíos (tablas sin datos, paneles sin contenido).
     * Incluye un ícono con animación flotante, un título y un subtítulo.
     */
    private static VBox crearPlaceholderTablaVacia(Node icono, String titulo, String subtitulo) {
        VBox placeholder = new VBox(16);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(48, 20, 48, 20));
        placeholder.setStyle("-fx-background-color: transparent;");

        // Círculo de fondo con acento sutil
        StackPane iconoStack = new StackPane();
        Circle bgCircle = new Circle(46, Color.web(COLOR_PRIMARY, 0.12));
        iconoStack.getChildren().addAll(bgCircle, icono);

        // Animación flotante sutil
        TranslateTransition flotar = new TranslateTransition(Duration.millis(1800), iconoStack);
        flotar.setByY(-6);
        flotar.setCycleCount(Animation.INDEFINITE);
        flotar.setAutoReverse(true);
        flotar.play();

        // Pulso suave en el círculo
        ScaleTransition pulso = new ScaleTransition(Duration.millis(2200), bgCircle);
        pulso.setFromX(1); pulso.setFromY(1);
        pulso.setToX(1.12); pulso.setToY(1.12);
        pulso.setCycleCount(Animation.INDEFINITE);
        pulso.setAutoReverse(true);
        pulso.play();

        // Título
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitulo.setTextFill(Color.web(COLOR_TEXT()));

        // Subtítulo
        Label lblSubtitulo = new Label(subtitulo);
        lblSubtitulo.setFont(Font.font("Segoe UI", 13));
        lblSubtitulo.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        lblSubtitulo.setStyle("-fx-text-alignment: center;");
        lblSubtitulo.setAlignment(Pos.CENTER);

        placeholder.getChildren().addAll(iconoStack, lblTitulo, lblSubtitulo);
        
        // ★ Cache para rendimiento en equipos viejos
        iconoStack.setCache(true);
        iconoStack.setCacheHint(javafx.scene.CacheHint.SPEED);
        
        return placeholder;
    }
}
