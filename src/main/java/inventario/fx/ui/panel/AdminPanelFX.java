package inventario.fx.ui.panel;
import inventario.fx.util.AppLogger;
import inventario.fx.config.PortablePaths;
import inventario.fx.model.TemaManager;
import inventario.fx.model.AdminManager;
import inventario.fx.model.InventarioFXBase;
import inventario.fx.icons.IconosSVG;
import inventario.fx.ui.dialog.AcercaDeDialogFX;
import inventario.fx.ui.component.NotificacionesFX;

import inventario.fx.service.NavigationController;
import inventario.fx.service.ProyectoFileService;
import inventario.fx.util.ComponentesFX;
import inventario.fx.util.AnimacionesFX;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import org.apache.poi.poifs.crypt.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;

import inventario.fx.database.DatabaseManager;

/**
 * Panel de Administración - Ventana Independiente
 * Gestión completa de proyectos con diseño moderno tipo dashboard.
 */
public class AdminPanelFX {
    
    private static Stage adminStage;
    private static Stage parentStage;
    private static VBox listaProyectosContainer;
    private static Label lblTotalProyectos;
    private static ScrollPane scrollProyectos;
    private static StackPane contenedorRaiz; // Contenedor raíz para notificaciones internas
    private static VBox mainContentRef; // Referencia para actualizar encabezado de tabla
    
    // Para arrastrar la ventana
    private static double xOffset = 0;
    private static double yOffset = 0;
    
    // Callback para importacin
    private static Runnable onImportCallback;
    
    // Debounce para búsqueda
    private static javafx.animation.PauseTransition debounceTimer;
    
    // Estado de ordenamiento
    private static String sortColumn = "nombre";
    private static boolean sortAscending = true;
    
    // Colores del tema
    private static Color COLOR_TEXT() { return Color.web(TemaManager.getText()); }
    private static Color COLOR_TEXT_MUTED() { return Color.web(TemaManager.getTextMuted()); }
    
    /**
     * Muestra el panel de Administración como ventana independiente
     */
    public static void mostrar(Stage parent) {
        parentStage = parent;
        
        // Si ya existe, traerla al frente
        if (adminStage != null && adminStage.isShowing()) {
            adminStage.toFront();
            adminStage.requestFocus();
            return;
        }
        
        adminStage = new Stage();
        adminStage.setTitle("Panel de Administración");
        try {
            java.io.InputStream iconStream = AdminPanelFX.class.getResourceAsStream("/images/Selcomp_logito.png");
            if (iconStream != null) {
                adminStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono del panel admin: " + e.getMessage());
        }
        
        // Hacer la ventana redimensionable
        adminStage.setResizable(true);
        adminStage.setMinWidth(1000);
        adminStage.setMinHeight(700);
        
        // StackPane raíz para permitir notificaciones superpuestas
        contenedorRaiz = new StackPane();
        
        // Layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + TemaManager.getBg() + ";");
        // ========================================================================
        // BARRA DE TÍTULO PERSONALIZADA
        // ========================================================================

        HBox titleBar = crearBarraTitulo();
        root.setTop(titleBar);
        
        // ========================================================================
        // CONTENIDO PRINCIPAL (SIDEBAR + MAIN)
        // ========================================================================
        HBox contenidoPrincipal = new HBox(0);
        
        // Panel lateral izquierdo (sidebar)
        VBox sidebar = crearSidebar();
        
        // Área principal de contenido
        VBox mainContent = crearAreaPrincipal();
        HBox.setHgrow(mainContent, Priority.ALWAYS);
        
        contenidoPrincipal.getChildren().addAll(sidebar, mainContent);
        root.setCenter(contenidoPrincipal);
        
        // Agregar BorderPane al StackPane raíz
        contenedorRaiz.getChildren().add(root);
        
        // ========================================================================
        // ESCENA Y VENTANA
        // ========================================================================
        Scene scene = new Scene(contenedorRaiz, 1350, 900);
        TemaManager.aplicarTema(scene);
        
        adminStage.setScene(scene);
        adminStage.centerOnScreen();
        
        // Animacion de entrada premium
        root.setOpacity(0);
        root.setScaleX(0.9);
        root.setScaleY(0.9);
        root.setTranslateY(15);
        
        adminStage.show();
        
        FadeTransition fade = new FadeTransition(Duration.millis(450), root);
        fade.setToValue(1);
        fade.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        ScaleTransition scale = new ScaleTransition(Duration.millis(500), root);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(AnimacionesFX.EASE_OUT_BACK);
        TranslateTransition slide = new TranslateTransition(Duration.millis(400), root);
        slide.setToY(0);
        slide.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        new ParallelTransition(fade, scale, slide).play();
        
        // Cargar proyectos inicial
        actualizarListaProyectos();
        
        // ========================================================================
        // ATAJOS DE TECLADO
        // ========================================================================
        scene.setOnKeyPressed(ev -> {
            if (ev.isControlDown()) {
                switch (ev.getCode()) {
                    case N -> mostrarFormularioProyecto(null);
                    case I -> importarProyectoExcel();
                    default -> {}
                }
            } else if (ev.getCode() == KeyCode.ESCAPE) {
                cerrarPanel();
                if (parentStage != null) { parentStage.show(); parentStage.toFront(); }
            }
        });
    }
    
    /**
     * Crea la barra de título personalizada
     */
    private static HBox crearBarraTitulo() {
        HBox titleBar = new HBox(16);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 24, 16, 24));
        titleBar.setStyle("-fx-background-color: " + TemaManager.getSurface() + ";");
        
        // Icono y título
        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(22);
        iconBg.setFill(new javafx.scene.paint.LinearGradient(
            0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new javafx.scene.paint.Stop(0, Color.web("#E63946")),
            new javafx.scene.paint.Stop(1, Color.web("#9D174D"))
        ));
        iconContainer.getChildren().addAll(iconBg, IconosSVG.admin("#FFFFFF", 24));
        
        VBox titleInfo = new VBox(2);
        Label titulo = new Label("Panel de Administración");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titulo.setTextFill(COLOR_TEXT());
        
        HBox statusRow = new HBox(12);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Circle statusDot = new Circle(5);
        statusDot.setFill(Color.web("#10B981"));
        Label statusLabel = new Label("Sesión activa");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        statusLabel.setTextFill(Color.web("#10B981"));
        
        // botón cerrar sesión con icono de power (Lucide)
        Button btnLogout = new Button();
        btnLogout.setGraphic(IconosSVG.power("#EF4444", 18));
        btnLogout.setTooltip(new Tooltip("Cerrar sesión de admin"));
        btnLogout.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-padding: 6;" +
            "-fx-cursor: hand;"
        );
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle(
            "-fx-background-color: #EF444420;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 6;" +
            "-fx-cursor: hand;"
        ));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-padding: 6;" +
            "-fx-cursor: hand;"
        ));
        btnLogout.setOnAction(e -> cerrarSesion());
        
        statusRow.getChildren().addAll(statusDot, statusLabel, btnLogout);
        
        titleInfo.getChildren().addAll(titulo, statusRow);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Logo SELCOMP en la parte derecha del header
        javafx.scene.image.ImageView logoSVG = inventario.fx.util.SVGUtil.loadSVG("/icons/LogoSelcompSVG.svg", 220);
        if (logoSVG != null) {
            logoSVG.setOpacity(0.85);
            titleBar.getChildren().addAll(iconContainer, titleInfo, spacer, logoSVG);
        } else {
            titleBar.getChildren().addAll(iconContainer, titleInfo, spacer);
        }
        return titleBar;
    }
    
    /**
     * Crea un boton de control de ventana
     */
    private static Button crearBotonVentana(String texto, String color, Runnable accion) {
        Button btn = new Button(texto);
        btn.setPrefSize(38, 38);
        btn.setMinSize(38, 38);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        btn.setStyle(
            "-fx-background-color: " + color + "25;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + "25;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;"
        ));
        btn.setOnAction(e -> accion.run());
        return btn;
    }
    
    /**
     * Crea el sidebar izquierdo con diseño mejorado
     */
    private static VBox crearSidebar() {
        VBox sidebar = ComponentesFX.crearSidebar(280);
        
        // Contenedor con scroll para todas las secciones
        VBox contenidoSidebar = new VBox(8);
        contenidoSidebar.setPadding(new Insets(16, 12, 16, 12));
        
        // ••• CARD DE ESTADSTICAS •••
        // Iniciar con "..." y cargar el contador async para no bloquear UI
        VBox cardProyectos = crearStatCard(
            "...",
            "Proyectos Activos",
            "#3B82F6",
            IconosSVG.carpeta("#3B82F6", 24)
        );
        lblTotalProyectos = (Label) ((HBox) cardProyectos.getChildren().get(0)).getChildren().get(1);
        
        // Cargar contador en hilo separado
        new Thread(() -> {
            int count = AdminManager.getProyectosActivos().size();
            Platform.runLater(() -> {
                if (lblTotalProyectos != null) {
                    animarConteo(lblTotalProyectos, 0, count, 800);
                }
            });
        }, "Admin-ContadorProyectos").start();
        
        // SECCION: PROYECTOS
        String sidebarIcon = TemaManager.getTextMuted();
        String accentColor = "#10B981";
        
        VBox seccionAcciones = crearSeccionSidebar("PROYECTOS",
            crearItemSidebar("Nuevo Proyecto", accentColor, c -> IconosSVG.agregar(c, 18), 
                () -> mostrarFormularioProyecto(null)),
            crearItemSidebar("Importar Excel", sidebarIcon, c -> IconosSVG.importar(c, 18), 
                () -> importarProyectoExcel())
        );
        
        // SECCION: SEGURIDAD
        VBox seccionSeguridad = crearSeccionSidebar("SEGURIDAD",
            crearItemSidebar("Cambiar Clave", sidebarIcon, c -> IconosSVG.candado(c, 18), 
                () -> mostrarCambiarContrasena()),
            crearItemSidebar("Cambiar Clave Excel", sidebarIcon, c -> IconosSVG.excel(c, 18), 
                () -> mostrarCambiarPasswordExcel()),
            crearItemSidebar("Registro Actividad", sidebarIcon, c -> IconosSVG.lista(c, 18), 
                () -> mostrarLogAccesos())
        );  
        
        // SECCION: SISTEMA
        VBox seccionSistema = crearSeccionSidebar("SISTEMA",
            crearItemSidebar("Mantenimiento", sidebarIcon, c -> IconosSVG.herramienta(c, 18), 
                () -> MantenimientoFX.mostrar(adminStage)),
            crearItemSidebar("Acerca de", sidebarIcon, c -> IconosSVG.info(c, 18), 
                () -> mostrarAcercaDe())
        );
        
        // Agregar todo al contenido del sidebar
        contenidoSidebar.getChildren().addAll(cardProyectos, seccionAcciones, seccionSeguridad, seccionSistema);
        
        // Scroll para el sidebar
        ScrollPane scrollSidebar = new ScrollPane(contenidoSidebar);
        scrollSidebar.setFitToWidth(true);
        scrollSidebar.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollSidebar.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollSidebar, Priority.ALWAYS);
        
        // Seccion inferior
        Button btnVolver = ComponentesFX.crearBotonVolver("Volver al Menú");
        VBox bottomSection = ComponentesFX.crearBottomSection(btnVolver);
        btnVolver.setOnAction(e -> {
            cerrarPanel();
            // Volver al menú principal
            if (parentStage != null) {
                parentStage.show();
                parentStage.toFront();
            }
        });
        
        sidebar.getChildren().addAll(scrollSidebar, bottomSection);
        return sidebar;
    }
    
    /**
     * Crea una seccion del sidebar con diseno de tarjeta
     */
    private static VBox crearSeccionSidebar(String titulo, javafx.scene.Node... items) {
        VBox seccion = new VBox(2);
        String secBg = TemaManager.getSurface();
        String secBorder = TemaManager.getBorderLight();
        seccion.setStyle(
            "-fx-background-color: " + secBg + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + secBorder + ";" +
            "-fx-border-radius: 12;"
        );
        
        // Header de la seccion
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 14, 8, 14));
        
        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblTitulo.setTextFill(COLOR_TEXT_MUTED());
        
        header.getChildren().add(lblTitulo);
        seccion.getChildren().add(header);
        
        // Agregar items
        for (javafx.scene.Node item : items) {
            seccion.getChildren().add(item);
        }
        
        return seccion;
    }
    
    /**
     * Crea un item del sidebar estilizado
     */
    private static HBox crearItemSidebar(String texto, String color, java.util.function.Function<String, javafx.scene.Node> iconFactory, Runnable accion) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 14, 10, 14));
        item.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        
        // Wrapper del icono con fondo sutil
        StackPane iconWrapper = new StackPane(iconFactory.apply(color));
        iconWrapper.setPrefSize(32, 32);
        iconWrapper.setMinSize(32, 32);
        String wrapperBg = TemaManager.getBgLight();
        iconWrapper.setStyle(
            "-fx-background-color: " + wrapperBg + ";" +
            "-fx-background-radius: 8;"
        );
        
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lbl.setTextFill(Color.web(TemaManager.getText()));
        
        item.getChildren().addAll(iconWrapper, lbl);
        
        // Hover: fondo primary con opacidad + iconos y texto cambian a primary
        String hoverColor = TemaManager.COLOR_PRIMARY;
        item.setOnMouseEntered(e -> {
            item.setStyle(
                "-fx-background-color: " + TemaManager.COLOR_PRIMARY + "18;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            );
            iconWrapper.getChildren().setAll(iconFactory.apply(hoverColor));
            iconWrapper.setStyle(
                "-fx-background-color: " + TemaManager.COLOR_PRIMARY + "20;" +
                "-fx-background-radius: 8;"
            );
            lbl.setTextFill(Color.web(hoverColor));
        });
        item.setOnMouseExited(e -> {
            item.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            );
            iconWrapper.getChildren().setAll(iconFactory.apply(color));
            iconWrapper.setStyle(
                "-fx-background-color: " + wrapperBg + ";" +
                "-fx-background-radius: 8;"
            );
            lbl.setTextFill(Color.web(TemaManager.getText()));
        });
        item.setOnMouseClicked(e -> accion.run());
        
        return item;
    }
    
    /**
     * Crea una tarjeta de estadisticas
     */
    private static VBox crearStatCard(String valor, String label, String color, javafx.scene.Node icono) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16));
        
        // Sombra sutil
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web(color, 0.1));
        sombra.setRadius(8);
        sombra.setOffsetY(2);
        card.setEffect(sombra);
        
        String statBg = TemaManager.getSurface();
        String statBorder = TemaManager.getBorderLight();
        card.setStyle(
            "-fx-background-color: " + statBg + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + statBorder + ";" +
            "-fx-border-radius: 14;"
        );
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Wrapper para el icono con fondo semitransparente
        StackPane iconWrapper = new StackPane(icono);
        iconWrapper.setPadding(new Insets(8));
        iconWrapper.setStyle(
            "-fx-background-color: " + color + "15;" +
            "-fx-background-radius: 10;"
        );
        header.getChildren().add(iconWrapper);
        
        Label lblValor = new Label("0");
        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblValor.setTextFill(Color.web(TemaManager.getText()));
        header.getChildren().add(lblValor);
        
        // Animacion de conteo
        int valorNumerico = 0;
        try {
            valorNumerico = Integer.parseInt(valor);
        } catch (NumberFormatException ignored) {}
        animarConteo(lblValor, 0, valorNumerico, 800);
        
        Label lblLabel = new Label(label);
        lblLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblLabel.setTextFill(COLOR_TEXT_MUTED());
        
        card.getChildren().addAll(header, lblLabel);
        
        // Hover effect premium
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + statBg + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + color + "50;" +
                "-fx-border-radius: 14;"
            );
            sombra.setColor(Color.web(color, 0.2));
            sombra.setRadius(12);
            sombra.setOffsetY(4);
            AnimacionesFX.hoverIn(card);
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: " + statBg + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + statBorder + ";" +
                "-fx-border-radius: 14;"
            );
            sombra.setColor(Color.web(color, 0.1));
            sombra.setRadius(8);
            sombra.setOffsetY(2);
            AnimacionesFX.hoverOut(card);
        });
        
        return card;
    }
    
    /**
     * Anima un conteo numrico de inicio a fin
     */
    private static void animarConteo(Label label, int desde, int hasta, int duracionMs) {
        ComponentesFX.animarConteo(label, desde, hasta, duracionMs);
    }
    
    /**
     * Crea un boton para el sidebar estilo dashboard
     */
    private static Button crearBotonSidebar(String texto, String color, Runnable accion) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPadding(new Insets(14, 16, 14, 16));
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 12;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + color + "15;" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 12;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 12;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnAction(e -> accion.run());
        return btn;
    }
    
    // crearBotonVolverDashboard eliminado: ahora usa ComponentesFX.crearBotonVolver()
    
    /**
     * Crea el área principal de contenido
     */
    private static VBox crearAreaPrincipal() {
        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: " + TemaManager.getBgLight() + ";");
        
        // CONTENEDOR PRINCIPAL — una sola card que agrupa todo
        String cardBg = TemaManager.getSurface();
        String cardBorder = TemaManager.getBorderLight();
        
        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: " + cardBg + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + cardBorder + ";" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);"
        );
        VBox.setMargin(card, new Insets(20, 24, 20, 24));
        VBox.setVgrow(card, Priority.ALWAYS);

        // — Header dentro de la card —
        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(20, 24, 16, 24));
        
        VBox titleBlock = new VBox(2);
        Label lblTitulo = new Label("Gesti\u00f3n de Proyectos");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitulo.setTextFill(COLOR_TEXT());
        Label lblSubtitulo = new Label("Administra y gestiona tus proyectos");
        lblSubtitulo.setFont(Font.font("Segoe UI", 12));
        lblSubtitulo.setTextFill(COLOR_TEXT_MUTED());
        titleBlock.getChildren().addAll(lblTitulo, lblSubtitulo);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        // Barra de busqueda compacta
        HBox searchContainer = new HBox(8);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setPadding(new Insets(6, 12, 6, 12));
        searchContainer.setPrefHeight(34);
        searchContainer.setMaxWidth(260);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        String searchBg = TemaManager.getBgLight();
        searchContainer.setStyle(
            "-fx-background-color: " + searchBg + ";" +
            "-fx-background-radius: 8;"
        );
        
        StackPane searchIcon = new StackPane();
        searchIcon.getChildren().add(IconosSVG.buscar(TemaManager.getTextMuted(), 16));
        
        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar proyectos...");
        txtBuscar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-prompt-text-fill: " + TemaManager.getTextMuted() + ";" +
            "-fx-font-size: 13px;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 0;"
        );
        HBox.setHgrow(txtBuscar, Priority.ALWAYS);
        
        Button btnClear = new Button();
        btnClear.setGraphic(IconosSVG.cerrar(TemaManager.getTextMuted(), 12));
        btnClear.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;");
        btnClear.setVisible(false);
        btnClear.setOnAction(e -> txtBuscar.clear());
        btnClear.setOnMouseEntered(e -> btnClear.setStyle(
            "-fx-background-color: " + TemaManager.getBorder() + "; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4;"));
        btnClear.setOnMouseExited(e -> btnClear.setStyle(
            "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;"));
        
        txtBuscar.textProperty().addListener((obs, old, nuevo) -> {
            btnClear.setVisible(nuevo != null && !nuevo.isEmpty());
            if (debounceTimer != null) debounceTimer.stop();
            debounceTimer = new javafx.animation.PauseTransition(Duration.millis(300));
            debounceTimer.setOnFinished(ev -> filtrarProyectos(nuevo));
            debounceTimer.play();
        });
        
        searchContainer.getChildren().addAll(searchIcon, txtBuscar, btnClear);
        
        headerRow.getChildren().addAll(titleBlock, headerSpacer, searchContainer);

        // — Encabezado de columnas (dentro de la card) —
        HBox tableHeader = crearEncabezadoTabla();
        
        // ••• LISTA DE PROYECTOS •••
        listaProyectosContainer = new VBox(0);
        listaProyectosContainer.setPadding(new Insets(0, 0, 8, 0));
        listaProyectosContainer.setStyle("-fx-background-color: transparent;");
        
        scrollProyectos = new ScrollPane(listaProyectosContainer);
        scrollProyectos.setFitToWidth(true);
        scrollProyectos.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollProyectos, Priority.ALWAYS);
        
        // ── Drag & Drop: arrastrar archivos Excel a la lista para importarlos ──
        scrollProyectos.setOnDragOver(ev -> {
            if (ev.getDragboard().hasFiles()) {
                boolean tieneExcel = ev.getDragboard().getFiles().stream()
                    .anyMatch(f -> f.getName().matches("(?i).*\\.xlsx?$"));
                if (tieneExcel) ev.acceptTransferModes(TransferMode.COPY);
            }
            ev.consume();
        });
        scrollProyectos.setOnDragDropped(ev -> {
            if (ev.getDragboard().hasFiles()) {
                List<File> excels = ev.getDragboard().getFiles().stream()
                    .filter(f -> f.getName().matches("(?i).*\\.xlsx?$"))
                    .collect(java.util.stream.Collectors.toList());
                if (!excels.isEmpty()) {
                    ev.setDropCompleted(true);
                    for (File archivo : excels) {
                        importarExcelDesdeArchivo(archivo);
                    }
                }
            }
            ev.consume();
        });
        
        mainContent.getChildren().add(card);
        card.getChildren().addAll(headerRow, tableHeader, scrollProyectos);
        mainContentRef = mainContent;
        return mainContent;
    }
    
    /**
     * Crea el encabezado de la tabla con columnas ordenables
     */
    private static HBox crearEncabezadoTabla() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(10, 24, 10, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        String borderColor = TemaManager.getBorderLight();
        header.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 1 0;"
        );
        
        Label col1 = crearLabelColumna("#", 38);
        Label col2 = crearLabelColumnaSortable("Proyecto", 240, "nombre");
        Label col3 = crearLabelColumnaSortable("Descripci\u00f3n", 320, "descripcion");
        Label col4 = crearLabelColumna("Acciones", 200);
        col4.setAlignment(Pos.CENTER);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(col1, col2, col3, spacer, col4);
        return header;
    }
    
    /**
     * Crea una etiqueta de columna que permite ordenar al hacer clic
     */
    private static Label crearLabelColumnaSortable(String texto, double width, String columna) {
        String indicador = sortColumn.equals(columna) ? (sortAscending ? " ▲" : " ▼") : "";
        Label lbl = new Label(texto + indicador);
        lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        lbl.setTextFill(sortColumn.equals(columna) 
            ? Color.web("#3B82F6") : Color.web(TemaManager.getTextMuted()));
        lbl.setCursor(Cursor.HAND);
        if (width > 0) {
            lbl.setPrefWidth(width);
            lbl.setMinWidth(width);
        }
        lbl.setOnMouseClicked(e -> {
            if (sortColumn.equals(columna)) {
                sortAscending = !sortAscending;
            } else {
                sortColumn = columna;
                sortAscending = true;
            }
            // Reconstruir encabezado con indicadores actualizados
            if (mainContentRef != null && mainContentRef.getChildren().size() >= 2) {
                mainContentRef.getChildren().set(1, crearEncabezadoTabla());
            }
            actualizarListaProyectos();
        });
        lbl.setOnMouseEntered(e -> lbl.setTextFill(Color.web("#3B82F6")));
        lbl.setOnMouseExited(e -> lbl.setTextFill(sortColumn.equals(columna) 
            ? Color.web("#3B82F6") : Color.web(TemaManager.getTextMuted())));
        return lbl;
    }
    
    /**
     * Crea una etiqueta de columna
     */
    private static Label crearLabelColumna(String texto, double width) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        lbl.setTextFill(Color.web(TemaManager.getTextMuted()));
        if (width > 0) {
            lbl.setPrefWidth(width);
            lbl.setMinWidth(width);
        }
        return lbl;
    }
    
    /**
     * Actualiza la lista de proyectos - versin async
     */
    public static void actualizarListaProyectos() {
        if (listaProyectosContainer == null) return;
        
        // Mostrar indicador de carga primero
        Platform.runLater(() -> {
            listaProyectosContainer.getChildren().clear();
            VBox loadingBox = new VBox(12);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPadding(new Insets(60));
            ProgressIndicator spinner = new ProgressIndicator();
            spinner.setPrefSize(40, 40);
            Label lblCargando = new Label("Cargando proyectos...");
            lblCargando.setTextFill(COLOR_TEXT_MUTED());
            lblCargando.setFont(Font.font("Segoe UI", 12));
            loadingBox.getChildren().addAll(spinner, lblCargando);
            listaProyectosContainer.getChildren().add(loadingBox);
        });
        
        // Cargar proyectos en hilo separado
        new Thread(() -> {
            List<AdminManager.Proyecto> proyectos = AdminManager.getProyectosActivos();
            
            // Aplicar ordenamiento
            proyectos.sort((a, b) -> {
                String valA, valB;
                if ("descripcion".equals(sortColumn)) {
                    valA = a.getDescripcion() != null ? a.getDescripcion() : "";
                    valB = b.getDescripcion() != null ? b.getDescripcion() : "";
                } else {
                    valA = a.getNombre() != null ? a.getNombre() : "";
                    valB = b.getNombre() != null ? b.getNombre() : "";
                }
                int cmp = valA.compareToIgnoreCase(valB);
                return sortAscending ? cmp : -cmp;
            });
            
            Platform.runLater(() -> {
                listaProyectosContainer.getChildren().clear();
                
                if (proyectos.isEmpty()) {
                    // Mostrar estado vacío
                    VBox emptyState = crearEstadoVacio();
                    listaProyectosContainer.getChildren().add(emptyState);
                } else {
                    for (int i = 0; i < proyectos.size(); i++) {
                        AdminManager.Proyecto p = proyectos.get(i);
                        HBox fila = crearFilaProyecto(p, i);
                        listaProyectosContainer.getChildren().add(fila);
                    }
                }
                
                // Actualizar contador
                if (lblTotalProyectos != null) {
                    lblTotalProyectos.setText(String.valueOf(proyectos.size()));
                }
            });
        }, "Admin-CargarProyectos").start();
    }
    
    /**
     * Crea un indicador de carga unificado para toda la aplicación
     */
    private static VBox crearIndicadorCarga(String mensaje) {
        VBox loading = new VBox(16);
        loading.setAlignment(Pos.CENTER);
        loading.setPadding(new Insets(60));
        
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(40, 40);
        
        Label lblMensaje = new Label(mensaje);
        lblMensaje.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        lblMensaje.setTextFill(COLOR_TEXT_MUTED());
        
        loading.getChildren().addAll(spinner, lblMensaje);
        return loading;
    }
    
    /**
     * Crea el estado vacío cuando no hay proyectos
     */
        private static VBox crearEstadoVacio() {
        VBox emptyState = new VBox(24);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(80, 40, 80, 40));
        
        // Contenedor circular para el icono con fondo sutil
        StackPane iconContainer = new StackPane();
        Circle circleBg = new Circle(60);
        circleBg.setFill(Color.web(TemaManager.isDarkMode() ? "#3B82F620" : "#3B82F615"));
        circleBg.setStroke(Color.web(TemaManager.isDarkMode() ? "#3B82F640" : "#3B82F630"));
        circleBg.setStrokeWidth(2);
        
        iconContainer.getChildren().addAll(circleBg, IconosSVG.carpetaMas("#3B82F6", 48));
        
        Label titulo = new Label("No hay proyectos creados");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titulo.setTextFill(COLOR_TEXT());
        
        Label desc = new Label("Comienza creando tu primer proyecto\npara organizar tu inventario");
        desc.setFont(Font.font("Segoe UI", 15));
        desc.setTextFill(COLOR_TEXT_MUTED());
        desc.setTextAlignment(TextAlignment.CENTER);
        
        // BotÃ³n de acciÃ³n principal
        Button btnCrear = new Button("Crear Proyecto");
        btnCrear.setGraphic(IconosSVG.agregar("#FFFFFF", 16));
        btnCrear.setStyle(
            "-fx-background-color: #3B82F6;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 14 28;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.3), 8, 0, 0, 2);"
        );
        btnCrear.setOnMouseEntered(e -> btnCrear.setStyle(
            "-fx-background-color: #2563EB;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 14 28;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.5), 12, 0, 0, 4);"
        ));
        btnCrear.setOnMouseExited(e -> btnCrear.setStyle(
            "-fx-background-color: #3B82F6;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 14 28;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.3), 8, 0, 0, 2);"
        ));
        btnCrear.setOnAction(e -> mostrarFormularioProyecto(null));
        
        emptyState.getChildren().addAll(iconContainer, titulo, desc, btnCrear);
        return emptyState;
    }

    
    /**
     * Crea una fila de proyecto para la tabla
     */
    private static HBox crearFilaProyecto(AdminManager.Proyecto proyecto, int index) {
        HBox fila = new HBox(16);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(14, 24, 14, 24));
        
        String color = proyecto.getColor() != null ? proyecto.getColor() : AdminManager.getColorProyecto(index);
        
        // Estilo plano — solo separador inferior
        String borderColor = TemaManager.getBorderLight();
        String bgHover = TemaManager.getSurfaceHover();
        
        fila.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent transparent " + borderColor + " transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        // Columna: Número — badge con color del proyecto
        StackPane numBadge = new StackPane();
        numBadge.setPrefSize(38, 38);
        numBadge.setMinSize(38, 38);
        numBadge.setStyle(
            "-fx-background-color: " + color + "18;" +
            "-fx-background-radius: 10;"
        );
        Label numLabel = new Label(String.format("%02d", index + 1));
        numLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        numLabel.setTextFill(Color.web(color));
        numBadge.getChildren().add(numLabel);
        
        // Columna: Proyecto (indicador de color + nombre)
        HBox colProyecto = new HBox(12);
        colProyecto.setAlignment(Pos.CENTER_LEFT);
        colProyecto.setPrefWidth(240);
        colProyecto.setMinWidth(240);
        
        Rectangle colorIndicator = new Rectangle(4, 34);
        colorIndicator.setFill(Color.web(color));
        colorIndicator.setArcWidth(4);
        colorIndicator.setArcHeight(4);
        
        Label nombre = new Label(proyecto.getNombre());
        nombre.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        nombre.setTextFill(COLOR_TEXT());
        nombre.setMaxWidth(200);
        nombre.setWrapText(true);
        
        colProyecto.getChildren().addAll(colorIndicator, nombre);
        
        // Columna: Descripción
        Label desc = new Label(proyecto.getDescripcion() != null && !proyecto.getDescripcion().isEmpty() 
            ? proyecto.getDescripcion() : "Sin descripción");
        desc.setFont(Font.font("Segoe UI", 13));
        desc.setTextFill(proyecto.getDescripcion() != null && !proyecto.getDescripcion().isEmpty() 
            ? COLOR_TEXT() : COLOR_TEXT_MUTED());
        desc.setPrefWidth(320);
        desc.setMinWidth(320);
        desc.setWrapText(true);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Columna: Acciones — estilo ghost uniforme
        String iconColor = TemaManager.getTextMuted();
        String dangerColor = "#EF4444";
        
        HBox colAcciones = new HBox(4);
        colAcciones.setAlignment(Pos.CENTER);
        colAcciones.setPrefWidth(200);
        colAcciones.setMinWidth(200);
        
        Button btnExcel = crearBotonAccionGhost(IconosSVG.excel("#20744a", 15), "Abrir Excel", "#20744a", () -> {
            abrirExcelProyecto(proyecto, index);
        });
        
        Button btnDescargar = crearBotonAccionGhost(IconosSVG.descargar(iconColor, 15), "Descargar Excel", iconColor, () -> {
            descargarExcelProyecto(proyecto, index);
        });
        
        Button btnDuplicar = crearBotonAccionGhost(IconosSVG.copiar(iconColor, 15), "Duplicar", iconColor, () -> {
            duplicarProyecto(proyecto);
        });
        
        Button btnEditar = crearBotonAccionGhost(IconosSVG.editar(iconColor, 15), "Editar", iconColor, () -> {
            mostrarFormularioProyecto(proyecto);
        });
        
        // Separador visual antes de eliminar
        Region sepAcciones = new Region();
        sepAcciones.setPrefWidth(4);
        
        Button btnEliminar = crearBotonAccionGhost(IconosSVG.papelera(dangerColor, 15), "Eliminar", dangerColor, () -> {
            confirmarEliminarProyecto(proyecto, index);
        });
        
        colAcciones.getChildren().addAll(btnExcel, btnDescargar, btnDuplicar, btnEditar, sepAcciones, btnEliminar);
        
        fila.getChildren().addAll(numBadge, colProyecto, desc, spacer, colAcciones);
        
        // Hover effect
        fila.setOnMouseEntered(e -> fila.setStyle(
            "-fx-background-color: " + bgHover + ";" +
            "-fx-border-color: transparent transparent " + borderColor + " transparent;" +
            "-fx-border-width: 0 0 1 0;"
        ));
        fila.setOnMouseExited(e -> fila.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent transparent " + borderColor + " transparent;" +
            "-fx-border-width: 0 0 1 0;"
        ));
        
        return fila;
    }
    
    /**
     * Crea un boton de accion estilo ghost (transparente, solo icono, hover sutil)
     */
    private static Button crearBotonAccionGhost(javafx.scene.Node icono, String tooltip, String iconColor, Runnable accion) {
        Button btn = new Button();
        btn.setGraphic(icono);
        btn.setPrefSize(32, 32);
        btn.setMinSize(32, 32);
        String hoverBg = TemaManager.getSurfaceHover();
        String baseStyle = "-fx-background-color: transparent;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;";
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + hoverBg + ";" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        btn.setOnAction(e -> accion.run());
        Tooltip.install(btn, new Tooltip(tooltip));
        return btn;
    }
    
    /**
     * Crea un boton de accion para el header (Nuevo, Importar)
     */
    private static Button crearBotonHeaderAccion(String texto, javafx.scene.Node icono, String color, Runnable accion) {
        Button btn = new Button(texto);
        btn.setGraphic(icono);
        btn.setPrefHeight(38);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        String baseStyle = "-fx-background-color: linear-gradient(to bottom, derive(" + color + ", 6%), " + color + ");" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 18;" +
            "-fx-effect: dropshadow(gaussian, " + color + "40, 6, 0, 0, 2);";
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, derive(" + color + ", -2%), derive(" + color + ", -12%));" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 18;" +
            "-fx-translate-y: -1;" +
            "-fx-effect: dropshadow(gaussian, " + color + "60, 12, 0, 0, 4);"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        btn.setOnAction(e -> accion.run());
        return btn;
    }
    
    /**
     * Crea un boton de accion estilo dashboard (usado en otros dialogos)
     */
    private static Button crearBotonAccionDashboard(javafx.scene.Node icono, String tooltip, String color, Runnable accion) {
        Button btn = new Button();
        btn.setGraphic(icono);
        btn.setPrefSize(34, 34);
        btn.setMinSize(34, 34);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: derive(" + color + ", 20%);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, " + color + "60, 8, 0, 0, 2);"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnAction(e -> accion.run());
        Tooltip.install(btn, new Tooltip(tooltip));
        return btn;
    }
    
    /**
     * Filtra proyectos por texto de búsqueda - versin async
     */
    private static void filtrarProyectos(String filtro) {
        if (listaProyectosContainer == null) return;
        
        // Mostrar indicador mientras carga
        listaProyectosContainer.getChildren().clear();
        listaProyectosContainer.getChildren().add(crearIndicadorCarga("Buscando..."));
        
        // Cargar y filtrar en hilo separado
        final String filtroFinal = filtro;
        new Thread(() -> {
            List<AdminManager.Proyecto> proyectos = AdminManager.getProyectosActivos();
            
            Platform.runLater(() -> {
                listaProyectosContainer.getChildren().clear();
                
                int visibleIndex = 0;
                for (int i = 0; i < proyectos.size(); i++) {
                    AdminManager.Proyecto p = proyectos.get(i);
                    boolean coincide = filtroFinal == null || filtroFinal.isEmpty() ||
                        p.getNombre().toLowerCase().contains(filtroFinal.toLowerCase()) ||
                        (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(filtroFinal.toLowerCase()));
                    
                    if (coincide) {
                        listaProyectosContainer.getChildren().add(crearFilaProyecto(p, visibleIndex));
                        visibleIndex++;
                    }
                }
                
                if (visibleIndex == 0 && filtroFinal != null && !filtroFinal.isEmpty()) {
                    VBox noResults = new VBox(16);
                    noResults.setAlignment(Pos.CENTER);
                    noResults.setPadding(new Insets(60, 40, 60, 40));
                    
                    Label emoji = new Label("”");
                    emoji.setFont(Font.font(48));
                    
                    Label msg = new Label("No se encontraron proyectos con: \"" + filtroFinal + "\"");
                    msg.setFont(Font.font("Segoe UI", 14));
                    msg.setTextFill(COLOR_TEXT_MUTED());
                    
                    noResults.getChildren().addAll(emoji, msg);
                    listaProyectosContainer.getChildren().add(noResults);
                }
            });
        }, "Admin-FiltrarProyectos").start();
    }
    
    /**
     * Muestra el formulario para crear/editar proyecto - DISEÑO MODERNO NUEVO
     */
    private static void mostrarFormularioProyecto(AdminManager.Proyecto proyectoExistente) {
        boolean esNuevo = proyectoExistente == null;
        // IMPORTANTE: Crear una copia para editar, no modificar el original directamente
        AdminManager.Proyecto proyecto = esNuevo ? new AdminManager.Proyecto() : proyectoExistente.copy();
        
        // Crear ventana de formulario (ventana normal, no diálogo modal)
        Stage formStage = new Stage();
        formStage.initStyle(StageStyle.TRANSPARENT);
        formStage.setTitle(esNuevo ? "Nuevo Proyecto" : "Editar Proyecto");
        formStage.initOwner(adminStage);
        
        // Variables para arrastrar la ventana
        final double[] dragOffset = {0, 0};
        
        // ═══════════════════════════════════════════════════════════════════════════
        // ROOT - Contenedor principal con diseño de dos columnas
        // ═══════════════════════════════════════════════════════════════════════════
        HBox root = new HBox(0);
        root.setMaxWidth(720);
        root.setMaxHeight(580);
        root.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 20;"
        );
        
        Rectangle clipForm = new Rectangle(720, 580);
        clipForm.setArcWidth(40);
        clipForm.setArcHeight(40);
        root.setClip(clipForm);
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(50);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        root.setEffect(shadow);
        
        // ═══════════════════════════════════════════════════════════════════════════
        // PANEL IZQUIERDO - Visual decorativo con preview del color
        // ═══════════════════════════════════════════════════════════════════════════
        // Si es nuevo proyecto, asignar color aleatorio; si existe, usar su color
        String colorInicial = proyecto.getColor() != null && !proyecto.getColor().isEmpty() 
            ? proyecto.getColor() 
            : (esNuevo ? AdminManager.getColorAleatorio() : "#10B981");
        final String[] colorSeleccionado = {colorInicial};
        
        VBox panelIzq = new VBox(0);
        panelIzq.setPrefWidth(260);
        panelIzq.setMinWidth(260);
        panelIzq.setStyle("-fx-background-color: " + colorInicial + ";");
        panelIzq.setAlignment(Pos.CENTER);
        panelIzq.setCursor(Cursor.MOVE);
        
        // Permitir arrastrar la ventana desde el panel izquierdo
        panelIzq.setOnMousePressed(e -> {
            dragOffset[0] = e.getSceneX();
            dragOffset[1] = e.getSceneY();
        });
        panelIzq.setOnMouseDragged(e -> {
            formStage.setX(e.getScreenX() - dragOffset[0]);
            formStage.setY(e.getScreenY() - dragOffset[1]);
        });
        
        // Patrón decorativo de círculos
        StackPane decoracion = new StackPane();
        decoracion.setPrefSize(260, 580);
        
        // Círculos de fondo decorativos
        Circle circulo1 = new Circle(120);
        circulo1.setFill(Color.web("#FFFFFF", 0.08));
        circulo1.setTranslateX(-80);
        circulo1.setTranslateY(-150);
        
        Circle circulo2 = new Circle(80);
        circulo2.setFill(Color.web("#FFFFFF", 0.06));
        circulo2.setTranslateX(100);
        circulo2.setTranslateY(180);
        
        Circle circulo3 = new Circle(50);
        circulo3.setFill(Color.web("#FFFFFF", 0.1));
        circulo3.setTranslateX(60);
        circulo3.setTranslateY(-80);
        
        // Contenido central del panel izquierdo
        VBox contenidoIzq = new VBox(20);
        contenidoIzq.setAlignment(Pos.CENTER);
        contenidoIzq.setPadding(new Insets(40, 30, 40, 30));
        
        // Icono grande con Lucide (editar o estrella)
        StackPane iconoGrande = new StackPane();
        iconoGrande.setAlignment(Pos.CENTER);
        iconoGrande.setPrefSize(100, 100);
        iconoGrande.setMinSize(100, 100);
        iconoGrande.setMaxSize(100, 100);
        
        Circle bgIcono = new Circle(50);
        bgIcono.setFill(Color.web("#FFFFFF", 0.2));
        bgIcono.setStroke(Color.web("#FFFFFF", 0.4));
        bgIcono.setStrokeWidth(3);
        
        javafx.scene.Node iconoLucide = esNuevo 
            ? IconosSVG.estrella("#FFFFFF", 48) 
            : IconosSVG.editar("#FFFFFF", 48);
        StackPane.setAlignment(iconoLucide, Pos.CENTER);
        
        iconoGrande.getChildren().addAll(bgIcono, iconoLucide);
        
        // Título en panel izquierdo
        Label tituloIzq = new Label(esNuevo ? "Nuevo\nProyecto" : "Editar\nProyecto");
        tituloIzq.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        tituloIzq.setTextFill(Color.WHITE);
        tituloIzq.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        tituloIzq.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.3)));
        
        Label subtituloIzq = new Label(esNuevo ? "Configura tu proyecto\npersonalizado" : "Modifica la\nconfiguración");
        subtituloIzq.setFont(Font.font("Segoe UI", 14));
        subtituloIzq.setTextFill(Color.web("#FFFFFF", 0.85));
        subtituloIzq.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        // Preview de imagen del proyecto
        StackPane imgPreviewContainer = new StackPane();
        imgPreviewContainer.setAlignment(Pos.CENTER);
        imgPreviewContainer.setPrefSize(160, 160);
        imgPreviewContainer.setMaxSize(160, 160);
        imgPreviewContainer.setMinSize(160, 160);
        imgPreviewContainer.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: rgba(255,255,255,0.4);" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 2;"
        );
        
        ImageView imgPreviewIzq = new ImageView();
        imgPreviewIzq.setFitWidth(140);
        imgPreviewIzq.setFitHeight(140);
        imgPreviewIzq.setPreserveRatio(true);
        StackPane.setAlignment(imgPreviewIzq, Pos.CENTER);
        
        // Placeholder con icono Lucide en lugar de emoji
        javafx.scene.Node imgPlaceholderIzq = IconosSVG.imagen("#FFFFFF", 56);
        StackPane.setAlignment(imgPlaceholderIzq, Pos.CENTER);
        
        // Wrapper para el placeholder con opacidad
        StackPane placeholderWrapper = new StackPane(imgPlaceholderIzq);
        placeholderWrapper.setOpacity(0.6);
        StackPane.setAlignment(placeholderWrapper, Pos.CENTER);
        
        imgPreviewContainer.getChildren().addAll(placeholderWrapper, imgPreviewIzq);
        
        contenidoIzq.getChildren().addAll(iconoGrande, tituloIzq, subtituloIzq, imgPreviewContainer);
        decoracion.getChildren().addAll(circulo1, circulo2, circulo3, contenidoIzq);
        panelIzq.getChildren().add(decoracion);
        
        // ═══════════════════════════════════════════════════════════════════════════
        // PANEL DERECHO - Formulario
        // ═══════════════════════════════════════════════════════════════════════════
        VBox panelDer = new VBox(0);
        panelDer.setPrefWidth(460);
        panelDer.setStyle("-fx-background-color: " + TemaManager.getBg() + ";");
        
        // Header del formulario
        HBox headerForm = new HBox();
        headerForm.setAlignment(Pos.CENTER_RIGHT);
        headerForm.setPadding(new Insets(16, 20, 8, 20));
        
        // Botón cerrar con icono Lucide
        Button btnCerrar = crearBotonCerrarLucide(formStage);
        headerForm.getChildren().add(btnCerrar);
        
        // Contenedor scrollable para el formulario
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        
        VBox formContent = new VBox(18);
        formContent.setPadding(new Insets(0, 28, 20, 28));
        formContent.setStyle("-fx-background-color: transparent;");
        
        // ═══════════════════════════════════════════════════════════════════════════
        // CAMPO: Nombre del Proyecto
        // ═══════════════════════════════════════════════════════════════════════════
        VBox campoNombre = new VBox(6);
        HBox labelNombreBox = new HBox(6);
        labelNombreBox.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconNombre = new StackPane(IconosSVG.editar(TemaManager.getText(), 16));
        
        Label lblNombre = new Label("Nombre del Proyecto");
        lblNombre.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        lblNombre.setTextFill(COLOR_TEXT());
        
        Label asterisco = new Label("*");
        asterisco.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        asterisco.setTextFill(Color.web("#EF4444"));
        
        labelNombreBox.getChildren().addAll(iconNombre, lblNombre, asterisco);
        
        TextField txtNombre = new TextField(proyecto.getNombre() != null ? proyecto.getNombre() : "");
        txtNombre.setPromptText("Ej: Secretaría de Educación");
        txtNombre.setPrefHeight(46);
        txtNombre.setFont(Font.font("Segoe UI", 14));
        txtNombre.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 12;" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-padding: 12 16;"
        );
        
        // Focus effect
        txtNombre.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                txtNombre.setStyle(txtNombre.getStyle().replace(TemaManager.getBorder(), colorSeleccionado[0]));
            } else {
                txtNombre.setStyle(txtNombre.getStyle().replace(colorSeleccionado[0], TemaManager.getBorder()));
            }
        });
        
        campoNombre.getChildren().addAll(labelNombreBox, txtNombre);
        
        // ═══════════════════════════════════════════════════════════════════════════
        // CAMPO: Descripción
        // ═══════════════════════════════════════════════════════════════════════════
        VBox campoDesc = new VBox(6);
        HBox labelDescBox = new HBox(6);
        labelDescBox.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconDesc = new StackPane(IconosSVG.documento(TemaManager.getText(), 16));
        
        Label lblDesc = new Label("Descripción");
        lblDesc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        lblDesc.setTextFill(COLOR_TEXT());
        
        Label lblDescOpcional = new Label("(opcional)");
        lblDescOpcional.setFont(Font.font("Segoe UI", 11));
        lblDescOpcional.setTextFill(COLOR_TEXT_MUTED());
        
        labelDescBox.getChildren().addAll(iconDesc, lblDesc, lblDescOpcional);
        
        TextField txtDesc = new TextField(proyecto.getDescripcion() != null ? proyecto.getDescripcion() : "");
        txtDesc.setPromptText("Ej: Mesa de servicios TI");
        txtDesc.setPrefHeight(46);
        txtDesc.setFont(Font.font("Segoe UI", 14));
        txtDesc.setStyle(txtNombre.getStyle());
        
        campoDesc.getChildren().addAll(labelDescBox, txtDesc);
        
        // ═══════════════════════════════════════════════════════════════════════════
        // CAMPO: Selección de Color - Diseño de tarjetas
        // ═══════════════════════════════════════════════════════════════════════════
        VBox campoColor = new VBox(10);
        
        HBox labelColorBox = new HBox(6);
        labelColorBox.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconColor = new StackPane(IconosSVG.paleta(TemaManager.getText(), 16));
        
        Label lblColor = new Label("Color del Proyecto");
        lblColor.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        lblColor.setTextFill(COLOR_TEXT());
        
        labelColorBox.getChildren().addAll(iconColor, lblColor);
        
        // Grid de colores con diseño de tarjetas
        FlowPane coloresGrid = new FlowPane(6, 6);
        coloresGrid.setPadding(new Insets(14));
        coloresGrid.setAlignment(Pos.CENTER);
        coloresGrid.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 14;"
        );
        
        java.util.List<StackPane> todosColores = new java.util.ArrayList<>();
        
        // ColorPicker oculto
        ColorPicker colorPicker = new ColorPicker(Color.web(colorInicial));
        colorPicker.setVisible(false);
        colorPicker.setManaged(false);
        
        final StackPane[] customColorBtnRef = {null};
        
        // Función para actualizar el panel izquierdo con el color seleccionado
        Runnable actualizarPanelColor = () -> {
            panelIzq.setStyle("-fx-background-color: " + colorSeleccionado[0] + ";");
        };
        
        Runnable actualizarSeleccionColor = () -> {
            for (StackPane sp : todosColores) {
                Circle c = (Circle) sp.getChildren().get(0);
                Object userData = c.getUserData();
                if (userData != null && userData.equals("rainbow")) continue;
                
                String colorCirculo = "#" + c.getFill().toString().substring(2, 8).toUpperCase();
                boolean seleccionado = colorCirculo.equalsIgnoreCase(colorSeleccionado[0]);
                
                if (seleccionado) {
                    c.setStroke(Color.WHITE);
                    c.setStrokeWidth(3);
                    sp.setScaleX(1.15);
                    sp.setScaleY(1.15);
                    sp.setEffect(new DropShadow(10, Color.web(colorCirculo)));
                } else {
                    c.setStroke(Color.TRANSPARENT);
                    sp.setScaleX(1.0);
                    sp.setScaleY(1.0);
                    sp.setEffect(null);
                }
            }
            actualizarPanelColor.run();
        };
        
        // Crear círculos de colores
        for (String c : AdminManager.COLORES_PROYECTO) {
            StackPane colorWrapper = new StackPane();
            colorWrapper.setCursor(javafx.scene.Cursor.HAND);
            
            Circle colorCircle = new Circle(16);
            colorCircle.setFill(Color.web(c));
            colorCircle.setStroke(c.equalsIgnoreCase(colorInicial) ? Color.WHITE : Color.TRANSPARENT);
            colorCircle.setStrokeWidth(3);
            
            if (c.equalsIgnoreCase(colorInicial)) {
                colorWrapper.setScaleX(1.15);
                colorWrapper.setScaleY(1.15);
                colorWrapper.setEffect(new DropShadow(10, Color.web(c)));
            }
            
            colorWrapper.getChildren().add(colorCircle);
            todosColores.add(colorWrapper);
            
            final String colorFinal = c;
            colorWrapper.setOnMouseClicked(e -> {
                colorSeleccionado[0] = colorFinal;
                actualizarSeleccionColor.run();
            });
            
            colorWrapper.setOnMouseEntered(e -> {
                if (!colorFinal.equalsIgnoreCase(colorSeleccionado[0])) {
                    colorWrapper.setScaleX(1.1);
                    colorWrapper.setScaleY(1.1);
                }
            });
            colorWrapper.setOnMouseExited(e -> {
                if (!colorFinal.equalsIgnoreCase(colorSeleccionado[0])) {
                    colorWrapper.setScaleX(1.0);
                    colorWrapper.setScaleY(1.0);
                }
            });
            
            coloresGrid.getChildren().add(colorWrapper);
        }
        
        // Botón de color personalizado
        StackPane customColorBtn = new StackPane();
        customColorBtn.setCursor(javafx.scene.Cursor.HAND);
        customColorBtnRef[0] = customColorBtn;
        
        Circle rainbowCircle = new Circle(16);
        rainbowCircle.setUserData("rainbow");
        javafx.scene.paint.Stop[] stops = new javafx.scene.paint.Stop[] {
            new javafx.scene.paint.Stop(0.0, Color.web("#FF0000")),
            new javafx.scene.paint.Stop(0.17, Color.web("#FF8000")),
            new javafx.scene.paint.Stop(0.33, Color.web("#FFFF00")),
            new javafx.scene.paint.Stop(0.50, Color.web("#00FF00")),
            new javafx.scene.paint.Stop(0.67, Color.web("#00FFFF")),
            new javafx.scene.paint.Stop(0.83, Color.web("#0080FF")),
            new javafx.scene.paint.Stop(1.0, Color.web("#FF00FF"))
        };
        rainbowCircle.setFill(new javafx.scene.paint.LinearGradient(0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE, stops));
        rainbowCircle.setStroke(Color.TRANSPARENT);
        rainbowCircle.setStrokeWidth(3);
        
        Label plusLabel = new Label("+");
        plusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        plusLabel.setTextFill(Color.WHITE);
        plusLabel.setEffect(new DropShadow(2, Color.BLACK));
        
        customColorBtn.getChildren().addAll(rainbowCircle, plusLabel);
        
        customColorBtn.setOnMouseEntered(e -> {
            customColorBtn.setScaleX(1.1);
            customColorBtn.setScaleY(1.1);
        });
        customColorBtn.setOnMouseExited(e -> {
            if (rainbowCircle.getStroke() == Color.TRANSPARENT) {
                customColorBtn.setScaleX(1.0);
                customColorBtn.setScaleY(1.0);
            }
        });
        customColorBtn.setOnMouseClicked(e -> colorPicker.show());
        
        colorPicker.setOnAction(e -> {
            Color c = colorPicker.getValue();
            String hex = String.format("#%02X%02X%02X", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
            colorSeleccionado[0] = hex;
            AdminManager.agregarColorReciente(hex);
            actualizarSeleccionColor.run();
            rainbowCircle.setStroke(Color.WHITE);
            customColorBtn.setScaleX(1.15);
            customColorBtn.setScaleY(1.15);
            customColorBtn.setEffect(new DropShadow(10, c));
        });
        
        coloresGrid.getChildren().addAll(customColorBtn, colorPicker);
        campoColor.getChildren().addAll(labelColorBox, coloresGrid);
        
        // ═══════════════════════════════════════════════════════════════════════════
        // CAMPO: Imagen del Proyecto - Diseño de tarjeta con drag & drop visual
        // ═══════════════════════════════════════════════════════════════════════════
        VBox campoImagen = new VBox(10);
        
        HBox labelImgBox = new HBox(6);
        labelImgBox.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconImg = new StackPane(IconosSVG.imagen(TemaManager.getText(), 16));
        
        Label lblImg = new Label("Logo del Proyecto");
        lblImg.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        lblImg.setTextFill(COLOR_TEXT());
        
        Label lblImgOpcional = new Label("(opcional)");
        lblImgOpcional.setFont(Font.font("Segoe UI", 11));
        lblImgOpcional.setTextFill(COLOR_TEXT_MUTED());
        
        labelImgBox.getChildren().addAll(iconImg, lblImg, lblImgOpcional);
        
        // Contenedor de imagen estilo tarjeta
        VBox imagenCard = new VBox(10);
        imagenCard.setAlignment(Pos.CENTER);
        imagenCard.setPadding(new Insets(20));
        imagenCard.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 14;" +
            "-fx-border-style: dashed;" +
            "-fx-border-width: 2;"
        );
        
        final String[] imagenSeleccionada = {proyecto.getImagenPath()};
        
        // Preview de la imagen
        ImageView imgPreview = new ImageView();
        imgPreview.setFitWidth(80);
        imgPreview.setFitHeight(80);
        imgPreview.setPreserveRatio(true);
        
        StackPane imgStack = new StackPane();
        imgStack.setPrefSize(80, 80);
        imgStack.setAlignment(Pos.CENTER);
        
        // Placeholder con icono Lucide en lugar de emoji
        javafx.scene.Node imgPlaceholderIcon = IconosSVG.imagen(TemaManager.getTextMuted(), 40);
        StackPane imgPlaceholder = new StackPane(imgPlaceholderIcon);
        imgPlaceholder.setAlignment(Pos.CENTER);
        StackPane.setAlignment(imgPlaceholder, Pos.CENTER);
        
        imgStack.getChildren().addAll(imgPlaceholder, imgPreview);
        
        Label lblDropHint = new Label("Haz clic para seleccionar una imagen");
        lblDropHint.setFont(Font.font("Segoe UI", 12));
        lblDropHint.setTextFill(COLOR_TEXT_MUTED());
        
        Label lblFormatos = new Label("PNG, JPG, GIF • Aparecerá en el Excel");
        lblFormatos.setFont(Font.font("Segoe UI", 10));
        lblFormatos.setTextFill(COLOR_TEXT_MUTED());
        
        HBox botonesImg = new HBox(10);
        botonesImg.setAlignment(Pos.CENTER);
        
        Button btnSelImg = new Button("Seleccionar");
        btnSelImg.setGraphic(IconosSVG.carpeta("#FFFFFF", 14));
        btnSelImg.setPrefHeight(34);
        btnSelImg.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        btnSelImg.setStyle(
            "-fx-background-color: " + colorSeleccionado[0] + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 16;"
        );
        
        Button btnQuitarImg = new Button("Quitar");
        btnQuitarImg.setGraphic(IconosSVG.papelera("#EF4444", 14));
        btnQuitarImg.setPrefHeight(34);
        btnQuitarImg.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        btnQuitarImg.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #EF4444;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #EF4444;" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 16;"
        );
        btnQuitarImg.setVisible(false);
        
        // Cargar imagen existente
        if (imagenSeleccionada[0] != null && !imagenSeleccionada[0].isEmpty()) {
            try {
                File imgFile = new File(imagenSeleccionada[0]);
                if (imgFile.exists()) {
                    imgPreview.setImage(new Image(imgFile.toURI().toString()));
                    imgPreviewIzq.setImage(new Image(imgFile.toURI().toString()));
                    imgPlaceholder.setVisible(false);
                    placeholderWrapper.setVisible(false);
                    lblDropHint.setText(imgFile.getName());
                    btnQuitarImg.setVisible(true);
                }
            } catch (Exception ex) { /* usar placeholder */ }
        }
        
        btnSelImg.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleccionar logo del proyecto");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File archivo = fc.showOpenDialog(formStage);
            if (archivo != null) {
                try {
                    Image img = new Image(archivo.toURI().toString());
                    imgPreview.setImage(img);
                    imgPreviewIzq.setImage(img);
                    imgPlaceholder.setVisible(false);
                    placeholderWrapper.setVisible(false);
                    imagenSeleccionada[0] = archivo.getAbsolutePath();
                    lblDropHint.setText(archivo.getName());
                    btnQuitarImg.setVisible(true);
                } catch (Exception ex) {
                    mostrarMensaje("Error", "No se pudo cargar la imagen", "#EF4444");
                }
            }
        });
        
        btnQuitarImg.setOnAction(ev -> {
            imgPreview.setImage(null);
            imgPreviewIzq.setImage(null);
            imgPlaceholder.setVisible(true);
            placeholderWrapper.setVisible(true);
            imagenSeleccionada[0] = "";
            lblDropHint.setText("Haz clic para seleccionar una imagen");
            btnQuitarImg.setVisible(false);
        });
        
        // Hacer click en la tarjeta tambin abre el selector
        imagenCard.setOnMouseClicked(e -> btnSelImg.fire());
        
        // ── Drag & Drop real para imágenes ──
        imagenCard.setOnDragOver(ev -> {
            if (ev.getDragboard().hasFiles()) {
                boolean tieneImagen = ev.getDragboard().getFiles().stream()
                    .anyMatch(f -> f.getName().matches("(?i).*\\.(png|jpg|jpeg|gif|bmp)$"));
                if (tieneImagen) {
                    ev.acceptTransferModes(TransferMode.COPY);
                    imagenCard.setStyle(
                        "-fx-background-color: " + colorSeleccionado[0] + "15;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: " + colorSeleccionado[0] + ";" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-style: dashed;" +
                        "-fx-border-width: 3;"
                    );
                }
            }
            ev.consume();
        });
        imagenCard.setOnDragExited(ev -> imagenCard.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 14;" +
            "-fx-border-style: dashed;" +
            "-fx-border-width: 2;"
        ));
        imagenCard.setOnDragDropped(ev -> {
            boolean success = false;
            if (ev.getDragboard().hasFiles()) {
                File archivo = ev.getDragboard().getFiles().stream()
                    .filter(f -> f.getName().matches("(?i).*\\.(png|jpg|jpeg|gif|bmp)$"))
                    .findFirst().orElse(null);
                if (archivo != null) {
                    try {
                        Image img = new Image(archivo.toURI().toString());
                        imgPreview.setImage(img);
                        imgPreviewIzq.setImage(img);
                        imgPlaceholder.setVisible(false);
                        placeholderWrapper.setVisible(false);
                        imagenSeleccionada[0] = archivo.getAbsolutePath();
                        lblDropHint.setText(archivo.getName());
                        btnQuitarImg.setVisible(true);
                        success = true;
                    } catch (Exception ex) {
                        mostrarMensaje("Error", "No se pudo cargar la imagen", "#EF4444");
                    }
                }
            }
            ev.setDropCompleted(success);
            ev.consume();
        });
        
        imagenCard.setOnMouseEntered(e -> imagenCard.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + colorSeleccionado[0] + ";" +
            "-fx-border-radius: 14;" +
            "-fx-border-style: dashed;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;"
        ));
        imagenCard.setOnMouseExited(e -> imagenCard.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 14;" +
            "-fx-border-style: dashed;" +
            "-fx-border-width: 2;"
        ));
        
        botonesImg.getChildren().addAll(btnSelImg, btnQuitarImg);
        imagenCard.getChildren().addAll(imgStack, lblDropHint, lblFormatos, botonesImg);
        campoImagen.getChildren().addAll(labelImgBox, imagenCard);
        
        // Agregar todos los campos al formulario
        formContent.getChildren().addAll(campoNombre, campoDesc, campoColor, campoImagen);
        scroll.setContent(formContent);
        
        // ═══════════════════════════════════════════════════════════════════════════
        // FOOTER - Botones de acción
        // ═══════════════════════════════════════════════════════════════════════════
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 28, 20, 28));
        footer.setStyle("-fx-border-color: " + TemaManager.getBorder() + "; -fx-border-width: 1 0 0 0;");
        
        Label lblError = new Label();
        lblError.setFont(Font.font("Segoe UI", 12));
        lblError.setTextFill(Color.web("#EF4444"));
        lblError.setVisible(false);
        
        Region spacerFooter = new Region();
        HBox.setHgrow(spacerFooter, Priority.ALWAYS);
        
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(110);
        btnCancelar.setPrefHeight(42);
        btnCancelar.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        btnCancelar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        );
        btnCancelar.setOnMouseEntered(e -> btnCancelar.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        ));
        btnCancelar.setOnMouseExited(e -> btnCancelar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        ));
        btnCancelar.setOnAction(e -> formStage.close());
        
        Button btnGuardar = new Button(esNuevo ? "Crear Proyecto" : "Guardar");
        btnGuardar.setGraphic(esNuevo ? new StackPane(IconosSVG.estrella("#FFFFFF", 16)) : new StackPane(IconosSVG.guardar("#FFFFFF", 16)));
        btnGuardar.setPrefWidth(150);
        btnGuardar.setPrefHeight(42);
        btnGuardar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnGuardar.setStyle(
            "-fx-background-color: " + colorSeleccionado[0] + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        // Actualizar color del botón guardar cuando cambie el color seleccionado
        Runnable actualizarBtnGuardar = () -> {
            btnGuardar.setStyle(
                "-fx-background-color: " + colorSeleccionado[0] + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            btnSelImg.setStyle(
                "-fx-background-color: " + colorSeleccionado[0] + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 8 16;"
            );
        };
        
        // Modificar actualizarSeleccionColor para tambin actualizar el botón
        for (StackPane sp : todosColores) {
            sp.setOnMouseClicked(e -> {
                Circle c = (Circle) sp.getChildren().get(0);
                if (c.getUserData() != null && c.getUserData().equals("rainbow")) return;
                String colorCirculo = "#" + c.getFill().toString().substring(2, 8).toUpperCase();
                colorSeleccionado[0] = colorCirculo;
                actualizarSeleccionColor.run();
                actualizarBtnGuardar.run();
            });
        }
        
        colorPicker.setOnAction(e -> {
            Color c = colorPicker.getValue();
            String hex = String.format("#%02X%02X%02X", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
            colorSeleccionado[0] = hex;
            AdminManager.agregarColorReciente(hex);
            actualizarSeleccionColor.run();
            actualizarBtnGuardar.run();
            rainbowCircle.setStroke(Color.WHITE);
            customColorBtn.setScaleX(1.15);
            customColorBtn.setScaleY(1.15);
            customColorBtn.setEffect(new DropShadow(10, c));
        });
        
        // Guardar nombre original para detectar cambios (importante para renombrar Excel)
        final String nombreOriginal = proyecto.getNombre();
        
        btnGuardar.setOnAction(e -> {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                lblError.setText("El nombre es obligatorio");
                lblError.setVisible(true);
                txtNombre.setStyle(txtNombre.getStyle() + "-fx-border-color: #EF4444;");
                return;
            }
            
            proyecto.setNombre(nombre);
            proyecto.setDescripcion(txtDesc.getText().trim());
            proyecto.setColor(colorSeleccionado[0]);
            proyecto.setImagenPath(imagenSeleccionada[0]);
            
            btnGuardar.setDisable(true);
            btnGuardar.setText("⏳ Guardando...");
            
            new Thread(() -> {
                boolean guardadoExitoso = false;
                final boolean esNuevoProyecto = esNuevo;
                final String nombreProyecto = proyecto.getNombre();
                
                if (esNuevo) {
                    guardadoExitoso = AdminManager.agregarProyecto(proyecto);
                } else {
                    // Usar actualizarProyectoConNombreAnterior para renombrar Excel correctamente
                    guardadoExitoso = AdminManager.actualizarProyectoConNombreAnterior(proyecto, nombreOriginal);
                }
                
                if (guardadoExitoso) {
                    Platform.runLater(() -> {
                        formStage.close();
                        actualizarListaProyectos();
                        
                        // Mostrar notificación de éxito
                        if (esNuevoProyecto) {
                            mostrarMensaje("✓ Proyecto Creado", 
                                "El proyecto '" + nombreProyecto + "' se creó exitosamente.", 
                                "#10B981");
                        } else {
                            mostrarMensaje("✓ Proyecto Actualizado", 
                                "El proyecto '" + nombreProyecto + "' se actualizó exitosamente.", 
                                "#10B981");
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        btnGuardar.setDisable(false);
                        btnGuardar.setText(esNuevo ? "Crear Proyecto" : "Guardar Cambios");
                        btnGuardar.setGraphic(esNuevo ? new StackPane(IconosSVG.estrella("#FFFFFF", 16)) : new StackPane(IconosSVG.guardar("#FFFFFF", 16)));
                        lblError.setText("Error al guardar el proyecto");
                        lblError.setVisible(true);
                    });
                }
            }, "Admin-GuardarProyecto").start();
        });
        
        footer.getChildren().addAll(lblError, spacerFooter, btnCancelar, btnGuardar);
        
        panelDer.getChildren().addAll(headerForm, scroll, footer);
        root.getChildren().addAll(panelIzq, panelDer);
        
        // ═══════════════════════════════════════════════════════════════════════════
        // SCENE Y ANIMACIONES - Ventana normal (no diálogo)
        // ═══════════════════════════════════════════════════════════════════════════
        
        // Añadir sombra elegante al borde
        DropShadow windowShadow = new DropShadow();
        windowShadow.setRadius(25);
        windowShadow.setColor(Color.rgb(0, 0, 0, 0.4));
        windowShadow.setOffsetY(5);
        root.setEffect(windowShadow);
        
        StackPane wrapperForm = new StackPane(root);
        wrapperForm.setStyle("-fx-background-color: transparent;");
        wrapperForm.setPadding(new Insets(20));
        
        Scene scene = new Scene(wrapperForm, 760, 620);
        scene.setFill(Color.TRANSPARENT);
        TemaManager.aplicarTema(scene);
        formStage.setScene(scene);
        formStage.centerOnScreen();
        
        // Animación de entrada premium
        root.setOpacity(0);
        root.setScaleX(0.8);
        root.setScaleY(0.8);
        root.setTranslateY(15);
        
        formStage.show();
        
        FadeTransition fade = new FadeTransition(Duration.millis(400), root);
        fade.setToValue(1);
        fade.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        ScaleTransition scale = new ScaleTransition(Duration.millis(450), root);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(AnimacionesFX.EASE_OUT_BACK);
        TranslateTransition slide = new TranslateTransition(Duration.millis(350), root);
        slide.setToY(0);
        slide.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        new ParallelTransition(fade, scale, slide).play();
        
        txtNombre.requestFocus();
    }
    
    /**
     * Abre el archivo Excel del proyecto
     */
    private static void abrirExcelProyecto(AdminManager.Proyecto proyecto, int index) {
        new Thread(() -> {
            try {
                java.nio.file.Path rutaExcel = ProyectoFileService.resolverRutaExcel(proyecto, index);
                if (rutaExcel == null || !Files.exists(rutaExcel)) {
                    Platform.runLater(() -> mostrarMensaje("Archivo no encontrado", 
                        "No existe inventario para \"" + proyecto.getNombre() + "\".\nGenera uno primero desde el menú principal.", 
                        "#F59E0B"));
                    return;
                }
                java.awt.Desktop.getDesktop().open(rutaExcel.toFile());
            } catch (Exception e) {
                Platform.runLater(() -> mostrarMensaje("Error", "No se pudo abrir el archivo Excel.\n" + e.getMessage(), "#EF4444"));
            }
        }, "Admin-AbrirExcel").start();
    }
    
    /**
     * Confirma y elimina un proyecto
     */
    private static void confirmarEliminarProyecto(AdminManager.Proyecto proyecto, int index) {
        // Buscar archivo Excel asociado
        new Thread(() -> {
            java.nio.file.Path rutaExcel = ProyectoFileService.resolverRutaExcel(proyecto, index);
            String nombreArchivo = rutaExcel != null ? rutaExcel.getFileName().toString() : "";
            final boolean excelEncontrado = rutaExcel != null && Files.exists(rutaExcel);
            final java.nio.file.Path rutaExcelFinal = rutaExcel;
            final String nombreArchivoFinal = nombreArchivo;
            
            // Mostrar diálogo de confirmación en el hilo de JavaFX
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.initOwner(adminStage);
                alert.setTitle("Eliminar Proyecto");
                alert.setHeaderText("¿Eliminar \"" + proyecto.getNombre() + "\"?");
                
                String mensaje = "Esta acción eliminará:\n" +
                                "• El proyecto de la base de datos\n" +
                                "• Todos los reportes de mantenimiento del proyecto\n" +
                                "• Todos los borradores asociados\n";
                if (excelEncontrado) {
                    mensaje += "• El archivo Excel: " + nombreArchivoFinal + "\n";
                }
                mensaje += "\nEsta acción no se puede deshacer.";
                
                alert.setContentText(mensaje);
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        new Thread(() -> {
                            boolean exito = true;
                            
                            // Eliminar archivo Excel si existe
                            if (excelEncontrado) {
                                try {
                                    Files.delete(rutaExcelFinal);
                                    System.out.println("[AdminPanel] ✅ Archivo Excel eliminado exitosamente: " + rutaExcelFinal);
                                } catch (Exception ex) {
                                    System.err.println("[AdminPanel] ❌ Error eliminando Excel: " + ex.getMessage());
                                    exito = false;
                                    Platform.runLater(() -> mostrarMensaje("Error", 
                                        "No se pudo eliminar el archivo Excel: " + ex.getMessage(), 
                                        "#EF4444"));
                                }
                            } else {
                                System.out.println("[AdminPanel] ℹ️ No se encontró archivo Excel para eliminar");
                            }
                            
                            // Eliminar proyecto de la base de datos
                            AdminManager.eliminarProyecto(proyecto.getId());
                            
                            final boolean exitoFinal = exito;
                            Platform.runLater(() -> {
                                actualizarListaProyectos();
                                if (exitoFinal) {
                                    mostrarMensaje("Proyecto Eliminado", 
                                        "El proyecto \"" + proyecto.getNombre() + "\" ha sido eliminado" +
                                        (excelEncontrado ? " junto con su archivo Excel." : "."),
                                        "#10B981");
                                }
                            });
                        }, "Admin-EliminarProyecto").start();
                    }
                });
            });
        }, "Admin-VerificarEliminar").start();
    }
    
    /**
     * Muestra un mensaje Toast compacto en la esquina inferior derecha DENTRO de la ventana
     */
    private static void mostrarMensaje(String titulo, String mensaje, String color) {
        if (contenedorRaiz == null) {
            System.err.println("[AdminPanel] contenedorRaiz no inicializado");
            return;
        }
        NotificacionesFX.mostrarNotificacion(contenedorRaiz, titulo, mensaje, color);
    }
    
    /**
     * Importa proyecto desde Excel - abre FileChooser directamente
     */
    private static void importarProyectoExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo Excel del proyecto");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx", "*.xls"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        
        // Directorio inicial
        File musicDir = inventario.fx.config.PortablePaths.getProyectosDir().toFile();
        if (musicDir.exists()) {
            fileChooser.setInitialDirectory(musicDir);
        }
        
        File archivo = fileChooser.showOpenDialog(adminStage);
        if (archivo != null) {
            // Extraer nombre del proyecto del archivo
            String nombreArchivo = archivo.getName();
            String nombreProyecto = nombreArchivo
                .replaceAll("(?i)\\.xlsx?$", "")
                .replaceAll("^\\d+[_\\s.-]+", "")
                .replaceAll("^Inventario_", "")
                .replace("_", " ")
                .trim();
            
            if (nombreProyecto.isEmpty()) {
                nombreProyecto = nombreArchivo.replaceAll("(?i)\\.xlsx?$", "");
            }
            
            // Crear proyecto con color aleatorio
            AdminManager.Proyecto nuevoProyecto = new AdminManager.Proyecto();
            nuevoProyecto.setNombre(nombreProyecto);
            nuevoProyecto.setDescripcion("Importado de archivo Excel");
            nuevoProyecto.setColor(AdminManager.getColorAleatorio());
            
            AdminManager.agregarProyecto(nuevoProyecto);
            
            // Mostrar mensaje de progreso
            mostrarMensaje("Importando...", "Procesando archivo Excel. Por favor espere...", "#3B82F6");
            
            // Ejecutar importacin y cifrado en hilo separado para no bloquear la UI
            final File archivoFinal = archivo;
            final String nombreProyectoFinal = nombreProyecto;
            final AdminManager.Proyecto proyectoFinal = nuevoProyecto;
            
            new Thread(() -> {
                try {
                    int indiceProyecto = AdminManager.getIndiceProyecto(proyectoFinal) + 1;
                    String nombreLimpio = InventarioFXBase.sanitizarNombreArchivo(nombreProyectoFinal);
                    String nombreArchivoDestino = "Inventario_" + indiceProyecto + " - " + nombreLimpio + ".xlsx";
                    java.nio.file.Path destino = InventarioFXBase.obtenerCarpetaEjecutable().resolve(nombreArchivoDestino);
                    
                    System.out.println("[AdminPanel] Importando Excel: " + archivoFinal.getAbsolutePath());
                    System.out.println("[AdminPanel] Destino: " + destino.toAbsolutePath());
                    
                    // Importar y cifrar el Excel con la contraseña del sistema
                    String password = AdminManager.getExcelPassword();
                    boolean cifradoOk = importarYCifrarExcel(archivoFinal.toPath(), destino, password);
                    
                    final String destinoStr = destino.getParent().toString();
                    final String nombreArchivoDestinoFinal = nombreArchivoDestino;
                    
                    Platform.runLater(() -> {
                        if (cifradoOk) {
                            System.out.println("[AdminPanel] Archivo importado y cifrado exitosamente");
                            actualizarListaProyectos();
                            
                            mostrarMensaje("Proyecto Importado", 
                                "Proyecto creado: " + nombreProyectoFinal + "\n\n" +
                                "Archivo: " + nombreArchivoDestinoFinal + "\n" +
                                "Ubicación: " + destinoStr + "\n" +
                                "Cifrado con la contraseña del sistema\n\n" +
                                "Los datos del Excel están listos para verse en el Dashboard.", 
                                "#10B981");
                        } else {
                            mostrarMensaje("Error de Importacin", 
                                "Se cre el proyecto pero no se pudo cifrar el archivo.", 
                                "#EF4444");
                        }
                    });
                } catch (Exception e) {
                    AppLogger.getLogger(AdminPanelFX.class).error("Error: " + e.getMessage(), e);
                    final String errorMsg = e.getMessage();
                    Platform.runLater(() -> {
                        mostrarMensaje("Error de Importacin", 
                            "Se cre el proyecto pero hubo un error al copiar elArchivo:\n" + errorMsg, 
                            "#EF4444");
                    });
                }
            }, "Admin-CopiarExcelImportado").start();
        }
    }
    
    /**
     * Cierra la sesin de administrador
        // Mostrar confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.initOwner(adminStage);
        confirmacion.setTitle("Importación Masiva");
        confirmacion.setHeaderText("Importar varios proyectos");
        confirmacion.setContentText(
            "Selecciona una carpeta con archivos Excel.\n" +
            "Cada archivo .xlsx se convertirá en un proyecto.\n\n" +
            "Ejemplo:\n" +
            "  • CAR.xlsx → Proyecto 'CAR'\n" +
            "  • MinSalud.xlsx → Proyecto 'MinSalud'\n\n" +
            "¿Continuar?"
        );
        
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Seleccionar carpeta con archivos Excel");
        
        File musicDir = inventario.fx.config.PortablePaths.getProyectosDir().toFile();
        if (musicDir.exists()) {
            dirChooser.setInitialDirectory(musicDir);
        }
        
        File carpeta = dirChooser.showDialog(adminStage);
        if (carpeta == null) return;
        
        // Buscar archivos Excel
        File[] archivos = carpeta.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return (lower.endsWith(".xlsx") || lower.endsWith(".xls")) && !name.startsWith("~");
        });
        
        if (archivos == null || archivos.length == 0) {
            mostrarMensaje("Sin archivos", 
                "No se encontraron archivos Excel en la carpeta seleccionada.", 
                "#F59E0B");
            return;
        }
        
        // Mostrar indicador de progreso
        mostrarMensaje("Importando...", 
            "Procesando " + archivos.length + " archivo(s).\nPor favor espere...", 
            "#3B82F6");
        
        // Ejecutar importacin en hilo separado para no bloquear la UI
        final File[] archivosFinales = archivos;
        new Thread(() -> {
            int importados = 0;
            int errores = 0;
            StringBuilder detalles = new StringBuilder();
            
            for (File archivo : archivosFinales) {
                String nombreArchivo = archivo.getName();
                String nombreProyecto = nombreArchivo
                    .replaceAll("(?i)\\.xlsx?$", "")
                    .replaceAll("^\\d+[_\\s.-]+", "")
                    .replaceAll("^Inventario_", "")
                    .replace("_", " ")
                    .trim();
                
                if (nombreProyecto.isEmpty()) {
                    nombreProyecto = nombreArchivo.replaceAll("(?i)\\.xlsx?$", "");
                }
                
                // Crear proyecto con color aleatorio
                AdminManager.Proyecto nuevoProyecto = new AdminManager.Proyecto();
                nuevoProyecto.setNombre(nombreProyecto);
                nuevoProyecto.setDescripcion("Importado desde carpeta");
                nuevoProyecto.setColor(AdminManager.getColorAleatorio());
                
                AdminManager.agregarProyecto(nuevoProyecto);
                
                // Importar y cifrar el archivo Excel
                try {
                    int indiceProyecto = AdminManager.getIndiceProyecto(nuevoProyecto) + 1;
                    String nombreLimpio = InventarioFXBase.sanitizarNombreArchivo(nombreProyecto);
                    String nombreArchivoDestino = "Inventario_" + indiceProyecto + " - " + nombreLimpio + ".xlsx";
                    java.nio.file.Path destino = InventarioFXBase.obtenerCarpetaEjecutable().resolve(nombreArchivoDestino);
                    
                    String password = AdminManager.getExcelPassword();
                    boolean cifradoOk = importarYCifrarExcel(archivo.toPath(), destino, password);
                    
                    if (cifradoOk) {
                        importados++;
                        detalles.append("- ").append(nombreProyecto).append("\n");
                        System.out.println("[AdminPanel] Importado y cifrado: " + archivo.getName() + " -> " + nombreArchivoDestino);
                    } else {
                        throw new Exception("Fallo al cifrar");
                    }
                } catch (Exception e) {
                    errores++;
                    detalles.append("- ").append(nombreProyecto).append(" (error)\n");
                    System.err.println("[AdminPanel] Error al importar " + archivo.getName() + ": " + e.getMessage());
                }
            }
            
            final int importadosFinal = importados;
            final int erroresFinal = errores;
            final String detallesFinal = detalles.toString();
            final int totalArchivos = archivosFinales.length;
            
            Platform.runLater(() -> {
                actualizarListaProyectos();
                
                String mensaje = "Se importaron " + importadosFinal + " de " + totalArchivos + " proyectos.";
                if (importadosFinal > 0) {
                    mensaje += "\n” Todos cifrados con la contraseña del sistema.";
                }
                if (erroresFinal > 0) {
                    mensaje += "\n\nú ï¸ " + erroresFinal + " archivo(s) con errores.";
                }
                mensaje += "\n\nDetalles:\n" + detallesFinal;
                
                mostrarMensaje("Importacin Completa", mensaje, erroresFinal > 0 ? "#F59E0B" : "#10B981");
            });
        }, "Admin-ImportarProyectos").start();
    }
    
    /**
     * Cierra la sesin de administrador
     */
    private static void cerrarSesion() {
        // Confirmar cierre de sesión
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Sesión");
        alert.setHeaderText("¿Desea cerrar la sesión de administrador?");
        alert.setContentText("Volverá al menú principal.");
        alert.initOwner(adminStage);
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            AdminManager.logout();
            cerrarPanel();
            // Volver al menú principal
            if (parentStage != null) {
                parentStage.show();
                parentStage.toFront();
            }
        }
    }
    
    // •••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••
    // M‰TODOS DE SEGURIDAD
    // •••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••
    
    /**
     * Muestra diálogo para cambiar contraseña de admin
     */
    private static void mostrarCambiarContrasena() {
        Stage dialog = new Stage();
        dialog.initOwner(adminStage);
        dialog.initStyle(StageStyle.TRANSPARENT);
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + TemaManager.getSurface() + "; -fx-background-radius: 20; -fx-border-color: " + TemaManager.getBorder() + "; -fx-border-radius: 20; -fx-border-width: 1;");
        root.setEffect(new DropShadow(40, Color.rgb(0, 0, 0, 0.4)));
        root.setPrefWidth(480);
        
        // Header moderno con gradiente azul
        HBox header = new HBox(16);
        header.setPadding(new Insets(28, 32, 24, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #3B82F615, #6366F110, transparent); -fx-background-radius: 20 20 0 0;");
        
        // Icono con fondo circular azul
        StackPane iconBg = new StackPane();
        iconBg.setPrefSize(56, 56);
        iconBg.setMinSize(56, 56);
        Circle iconCircle = new Circle(28);
        iconCircle.setFill(Color.web("#3B82F620"));
        iconBg.getChildren().addAll(iconCircle, IconosSVG.candado("#3B82F6", 28));
        
        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label lblTitulo = new Label("Cambiar Contrasena");
        lblTitulo.setTextFill(Color.web(TemaManager.getText()));
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        Label lblSubtitulo = new Label("Actualiza la clave del panel de administración");
        lblSubtitulo.setTextFill(COLOR_TEXT_MUTED());
        lblSubtitulo.setFont(Font.font("Segoe UI", 13));
        titleBox.getChildren().addAll(lblTitulo, lblSubtitulo);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        // Boton cerrar rojo con Lucide
        Button btnCerrar = crearBotonCerrarLucide(dialog);
        
        header.getChildren().addAll(iconBg, titleBox, btnCerrar);
        
        // Hacer header arrastrable
        header.setOnMousePressed(ev -> { xOffset = ev.getSceneX(); yOffset = ev.getSceneY(); });
        header.setOnMouseDragged(ev -> { dialog.setX(ev.getScreenX() - xOffset); dialog.setY(ev.getScreenY() - yOffset); });
        
        // Contenido con cards
        VBox content = new VBox(16);
        content.setPadding(new Insets(8, 32, 24, 32));
        
        // Card de info
        HBox infoCard = new HBox(12);
        infoCard.setAlignment(Pos.CENTER_LEFT);
        infoCard.setPadding(new Insets(14, 18, 14, 18));
        infoCard.setStyle("-fx-background-color: #3B82F615; -fx-background-radius: 12; -fx-border-color: #3B82F630; -fx-border-radius: 12;");
        infoCard.getChildren().add(IconosSVG.info("#3B82F6", 20));
        Label lblInfo = new Label("Esta contraseña protege el acceso al panel de administración del sistema.");
        lblInfo.setTextFill(Color.web("#3B82F6"));
        lblInfo.setFont(Font.font("Segoe UI", 12));
        lblInfo.setWrapText(true);
        HBox.setHgrow(lblInfo, Priority.ALWAYS);
        infoCard.getChildren().add(lblInfo);
        
        // Seccion de campos con titulo
        VBox camposSection = new VBox(12);
        HBox camposTitleBox = new HBox(10);
        camposTitleBox.setAlignment(Pos.CENTER_LEFT);
        camposTitleBox.setPadding(new Insets(8, 0, 0, 0));
        StackPane camposIcon = new StackPane(IconosSVG.candado(TemaManager.getText(), 16));
        Label lblCamposTitulo = new Label("Cambiar contrasena");
        lblCamposTitulo.setTextFill(Color.web(TemaManager.getText()));
        lblCamposTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        camposTitleBox.getChildren().addAll(camposIcon, lblCamposTitulo);
        
        // Campos de contrasena con ojito integrado
        HBox passActual = crearCampoPasswordConOjo("Contrasena actual");
        HBox passNueva = crearCampoPasswordConOjo("Nueva contrasena (minimo 8 caracteres)");
        HBox passConfirmar = crearCampoPasswordConOjo("Confirmar nueva contrasena");
        
        Label lblError = new Label();
        lblError.setTextFill(Color.web("#EF4444"));
        lblError.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblError.setVisible(false);
        lblError.setWrapText(true);
        
        // Indicador de fortaleza
        Label lblFortaleza = new Label();
        lblFortaleza.setFont(Font.font("Segoe UI", 11));
        lblFortaleza.setVisible(false);
        
        camposSection.getChildren().addAll(camposTitleBox, passActual, passNueva, passConfirmar, lblFortaleza, lblError);
        
        content.getChildren().addAll(infoCard, camposSection);
        
        // Footer con botones modernos
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20, 32, 24, 32));
        footer.setStyle("-fx-background-color: " + TemaManager.getBg() + "80; -fx-background-radius: 0 0 20 20;");
        
        Button btnCancelar = crearBotonLogAccion(IconosSVG.cerrar("#64748B", 14), "Cancelar", "#64748B");
        btnCancelar.setOnAction(e -> dialog.close());
        
        Button btnGuardar = crearBotonLogAccion(IconosSVG.check("#10B981", 14), "Guardar", "#10B981");
        btnGuardar.setStyle("-fx-background-color: #10B981; -fx-background-radius: 10; -fx-cursor: hand;");
        ((Label)((HBox)btnGuardar.getGraphic()).getChildren().get(1)).setTextFill(Color.WHITE);
        ((HBox)btnGuardar.getGraphic()).getChildren().set(0, IconosSVG.check("#FFFFFF", 14));
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle("-fx-background-color: #059669; -fx-background-radius: 10; -fx-cursor: hand;"));
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle("-fx-background-color: #10B981; -fx-background-radius: 10; -fx-cursor: hand;"));
        
        btnGuardar.setOnAction(e -> {
            String actual = getPasswordText(passActual);
            String nueva = getPasswordText(passNueva);
            String confirmar = getPasswordText(passConfirmar);
            
            if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
                lblError.setText("Todos los campos son obligatorios");
                lblError.setVisible(true);
                return;
            }
            if (!nueva.equals(confirmar)) {
                lblError.setText("Las contrasenas nuevas no coinciden");
                lblError.setVisible(true);
                return;
            }
            
            // Validar fortaleza con SecurityManager
            inventario.fx.security.SecurityManager.ResultadoValidacion validacion = 
                inventario.fx.security.SecurityManager.validarFortaleza(nueva);
            if (!validacion.isValida()) {
                lblError.setText(validacion.getMensaje());
                lblError.setVisible(true);
                return;
            }
            
            btnGuardar.setDisable(true);
            
            new Thread(() -> {
                inventario.fx.security.SecurityManager.ResultadoValidacion resultado = 
                    AdminManager.cambiarContrasena(actual, nueva);
                Platform.runLater(() -> {
                    if (resultado.isValida()) {
                        dialog.close();
                        mostrarMensaje("Contrasena Cambiada", "La contrasena se actualizo correctamente", "#10B981");
                    } else {
                        btnGuardar.setDisable(false);
                        lblError.setText(resultado.getMensaje());
                        lblError.setVisible(true);
                    }
                });
            }, "Admin-CambiarContrasena").start();
        });
        
        footer.getChildren().addAll(btnCancelar, btnGuardar);
        root.getChildren().addAll(header, content, footer);
        
        StackPane wrapperContrasena = new StackPane(root);
        wrapperContrasena.setStyle("-fx-background-color: transparent;");
        wrapperContrasena.setPadding(new Insets(20));
        
        Scene scene = new Scene(wrapperContrasena);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.setOnShown(e -> AnimacionesFX.animarEntradaDialogo(root));
        dialog.showAndWait();
    }
    
    /**
     * Muestra dialogo para cambiar la contrasena de cifrado de archivos Excel
     */
    private static void mostrarCambiarPasswordExcel() {
        Stage dialog = new Stage();
        dialog.initOwner(adminStage);
        dialog.initStyle(StageStyle.TRANSPARENT);
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + TemaManager.getSurface() + "; -fx-background-radius: 20; -fx-border-color: " + TemaManager.getBorder() + "; -fx-border-radius: 20; -fx-border-width: 1;");
        root.setEffect(new DropShadow(40, Color.rgb(0, 0, 0, 0.4)));
        root.setPrefWidth(480);
        
        // Header moderno con gradiente verde
        HBox header = new HBox(16);
        header.setPadding(new Insets(28, 32, 24, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #10B98115, #05966910, transparent); -fx-background-radius: 20 20 0 0;");
        
        // Icono con fondo circular
        StackPane iconBg = new StackPane();
        iconBg.setPrefSize(56, 56);
        iconBg.setMinSize(56, 56);
        Circle iconCircle = new Circle(28);
        iconCircle.setFill(Color.web("#10B98120"));
        iconBg.getChildren().addAll(iconCircle, IconosSVG.excel("#10B981", 28));
        
        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label lblTitulo = new Label("Contrasena de Excel");
        lblTitulo.setTextFill(Color.web(TemaManager.getText()));
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        Label lblSubtitulo = new Label("Protege los archivos Excel generados");
        lblSubtitulo.setTextFill(COLOR_TEXT_MUTED());
        lblSubtitulo.setFont(Font.font("Segoe UI", 13));
        titleBox.getChildren().addAll(lblTitulo, lblSubtitulo);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        // Boton cerrar rojo con Lucide
        Button btnCerrar = crearBotonCerrarLucide(dialog);
        
        header.getChildren().addAll(iconBg, titleBox, btnCerrar);
        
        // Hacer header arrastrable
        header.setOnMousePressed(ev -> { xOffset = ev.getSceneX(); yOffset = ev.getSceneY(); });
        header.setOnMouseDragged(ev -> { dialog.setX(ev.getScreenX() - xOffset); dialog.setY(ev.getScreenY() - yOffset); });
        
        // Determinar estado actual de forma sincrona
        boolean tienePersonalizada = AdminManager.tieneExcelPasswordPersonalizada();
        
        // Content con padding
        VBox content = new VBox(20);
        content.setPadding(new Insets(8, 32, 12, 32));
        
        // Estado actual con icono y descripcion mejorada
        HBox estadoBox = new HBox(16);
        estadoBox.setAlignment(Pos.CENTER_LEFT);
        estadoBox.setPadding(new Insets(20, 24, 20, 24));
        final String colorCard = tienePersonalizada ? "#10B981" : "#3B82F6";
        estadoBox.setStyle("-fx-background-color: " + colorCard + "15; -fx-background-radius: 14; -fx-border-color: " + colorCard + "30; -fx-border-width: 1.5; -fx-border-radius: 14;");
        
        // Animacion hover premium
        estadoBox.setOnMouseEntered(e -> AnimacionesFX.hoverIn(estadoBox));
        estadoBox.setOnMouseExited(e -> AnimacionesFX.hoverOut(estadoBox));
        
        StackPane iconoEstado = new StackPane();
        iconoEstado.setPrefSize(44, 44);
        Circle estadoCircle = new Circle(22);
        estadoCircle.setFill(Color.web(colorCard + "25"));
        estadoCircle.setStroke(Color.web(colorCard + "50"));
        estadoCircle.setStrokeWidth(2);
        iconoEstado.getChildren().addAll(estadoCircle, IconosSVG.excel(colorCard, 22));
        
        VBox estadoInfo = new VBox(4);
        Label lblEstado = new Label(tienePersonalizada ? "Contrasena Personalizada Activa" : "Usando Contrasena por Defecto");
        lblEstado.setTextFill(Color.web(colorCard));
        lblEstado.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        Label lblEstadoDesc = new Label(tienePersonalizada ? 
            "Al cambiar la clave, se actualizarán automáticamente todos los archivos Excel existentes." : 
            "Al establecer una clave personalizada, se aplicará a todos los archivos Excel del sistema.");
        lblEstadoDesc.setTextFill(COLOR_TEXT_MUTED());
        lblEstadoDesc.setFont(Font.font("Segoe UI", 12));
        lblEstadoDesc.setWrapText(true);
        
        estadoInfo.getChildren().addAll(lblEstado, lblEstadoDesc);
        
        estadoBox.getChildren().addAll(iconoEstado, estadoInfo);
        
        // Seccion de campos con titulo
        VBox camposSection = new VBox(12);
        HBox camposTitleBox = new HBox(10);
        camposTitleBox.setAlignment(Pos.CENTER_LEFT);
        StackPane camposIcon = new StackPane(IconosSVG.candado(TemaManager.getText(), 16));
        Label lblCampos = new Label("Cambiar contrasena");
        lblCampos.setTextFill(Color.web(TemaManager.getText()));
        lblCampos.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        camposTitleBox.getChildren().addAll(camposIcon, lblCampos);
        
        // Campos con ojito integrado
        HBox passNueva = crearCampoPasswordConOjo("Nueva contrasena (minimo 6 caracteres)");
        HBox passConfirmar = crearCampoPasswordConOjo("Confirmar nueva contrasena");
        
        Label lblError = new Label();
        lblError.setTextFill(Color.web("#EF4444"));
        lblError.setFont(Font.font("Segoe UI", 12));
        lblError.setVisible(false);
        lblError.setWrapText(true);
        
        camposSection.getChildren().addAll(camposTitleBox, passNueva, passConfirmar, lblError);
        
        content.getChildren().addAll(estadoBox, camposSection);
        
        // Footer con botones modernos
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER_RIGHT);
        botones.setPadding(new Insets(20, 32, 24, 32));
        botones.setStyle("-fx-background-color: " + TemaManager.getBg() + "80; -fx-background-radius: 0 0 20 20;");
        
        // (Botón "Restaurar" eliminado por solicitud de UI)
        
        Region spacerBtn = new Region();
        HBox.setHgrow(spacerBtn, Priority.ALWAYS);
        
        Button btnCancelar = crearBotonLogAccion(IconosSVG.cerrar("#64748B", 14), "Cancelar", "#64748B");
        btnCancelar.setOnAction(ev -> dialog.close());
        
        Button btnGuardar = crearBotonLogAccion(IconosSVG.check("#10B981", 14), "Guardar", "#10B981");
        btnGuardar.setStyle("-fx-background-color: #10B981; -fx-background-radius: 10; -fx-cursor: hand;");
        ((Label)((HBox)btnGuardar.getGraphic()).getChildren().get(1)).setTextFill(Color.WHITE);
        ((HBox)btnGuardar.getGraphic()).getChildren().set(0, IconosSVG.check("#FFFFFF", 14));
        btnGuardar.setOnMouseEntered(ev -> btnGuardar.setStyle("-fx-background-color: #059669; -fx-background-radius: 10; -fx-cursor: hand;"));
        btnGuardar.setOnMouseExited(ev -> btnGuardar.setStyle("-fx-background-color: #10B981; -fx-background-radius: 10; -fx-cursor: hand;"));
        
        btnGuardar.setOnAction(ev -> {
            String nueva = getPasswordText(passNueva);
            String confirmar = getPasswordText(passConfirmar);
            
            // Validaciones
            if (nueva.isEmpty() || confirmar.isEmpty()) {
                lblError.setText("Todos los campos son obligatorios");
                lblError.setVisible(true);
                return;
            }
            
            if (nueva.length() < 6) {
                lblError.setText("La nueva contrasena debe tener al menos 6 caracteres");
                lblError.setVisible(true);
                return;
            }
            
            if (!nueva.equals(confirmar)) {
                lblError.setText("Las contrasenas no coinciden");
                lblError.setVisible(true);
                return;
            }
            
            // Guardar la contrasena anterior para re-cifrar
            final String passwordVieja = AdminManager.getExcelPassword();
            final String passwordNueva2 = nueva;
            
            // Cambiar contrasena directamente
            AdminManager.setExcelPassword(nueva);
            dialog.close();
            
            // Mostrar mensaje de procesamiento
            mostrarMensaje("Procesando...", "Buscando archivos Excel existentes...", "#3B82F6");
            
            // Buscar archivos Excel en hilo separado para no bloquear la UI
            new Thread(() -> {
                List<java.nio.file.Path> archivosExcel = buscarArchivosExcelInventario();
                
                Platform.runLater(() -> {
                    if (!archivosExcel.isEmpty()) {
                        final int totalArchivos = archivosExcel.size();
                        mostrarConfirmacionRecifrar(passwordVieja, passwordNueva2, totalArchivos, archivosExcel);
                    } else {
                        mostrarMensaje("Contrasena Actualizada", 
                            "La contrasena de Excel ha sido cambiada exitosamente.\n" +
                            "Los nuevos archivos Excel usaran esta contrasena.", "#10B981");
                    }
                });
            }, "Admin-BuscarExcelRecifrar").start();
        });
        
        botones.getChildren().addAll(spacerBtn, btnCancelar, btnGuardar);
        
        root.getChildren().addAll(header, content, botones);
        
        StackPane wrapperExcel = new StackPane(root);
        wrapperExcel.setStyle("-fx-background-color: transparent;");
        wrapperExcel.setPadding(new Insets(20));
        
        Scene scene = new Scene(wrapperExcel);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.setOnShown(e -> AnimacionesFX.animarEntradaDialogo(root));
        dialog.showAndWait();
    }
    
    /**
     * Busca archivos Excel de inventario en múltiples ubicaciones
     */
    private static List<java.nio.file.Path> buscarArchivosExcelInventario() {
        List<java.nio.file.Path> archivos = new ArrayList<>();
        Set<String> archivosUnicos = new HashSet<>();
        
        // Ubicaciones a buscar
        List<java.nio.file.Path> carpetas = new ArrayList<>();
        
        // 1. Carpeta actual de trabajo
        carpetas.add(inventario.fx.config.PortablePaths.getProyectosDir());
        
        // 2. Carpeta del ejecutable
        try {
            carpetas.add(InventarioFXBase.obtenerCarpetaEjecutable());
        } catch (Exception ignored) {}
        
        // 3. Carpeta de proyectos configurada
        try {
            String rutaProyectos = AdminManager.getRutasTrabajo().getCarpetaProyectos();
            if (rutaProyectos != null && !rutaProyectos.isEmpty()) {
                carpetas.add(Paths.get(rutaProyectos));
            }
        } catch (Exception ignored) {}
        
        for (java.nio.file.Path carpeta : carpetas) {
            try {
                if (Files.exists(carpeta) && Files.isDirectory(carpeta)) {
                    Files.list(carpeta)
                        .filter(p -> {
                            String nombre = p.getFileName().toString().toLowerCase();
                            return nombre.startsWith("inventario_") && nombre.endsWith(".xlsx");
                        })
                        .forEach(p -> {
                            String nombreArchivo = p.getFileName().toString();
                            if (!archivosUnicos.contains(nombreArchivo)) {
                                archivosUnicos.add(nombreArchivo);
                                archivos.add(p);
                            }
                        });
                }
            } catch (Exception e) {
                System.err.println("Error buscando en " + carpeta + ": " + e.getMessage());
            }
        }
        
        System.out.println("[DEBUG] Archivos Excel encontrados: " + archivos.size());
        archivos.forEach(a -> System.out.println("  - " + a.toAbsolutePath()));
        
        return archivos;
    }
    
    /**
     * Muestra confirmacin y recifra automáticamente los archivos Excel existentes
     */
    private static void mostrarConfirmacionRecifrar(String passwordAnterior, String passwordNueva, int totalArchivos, List<java.nio.file.Path> archivosExcel) {
        Stage dialog = new Stage();
        dialog.initOwner(adminStage);
        dialog.initStyle(StageStyle.TRANSPARENT);
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + TemaManager.getSurface() + "; -fx-background-radius: 16; -fx-border-color: " + TemaManager.getBorder() + "; -fx-border-radius: 16; -fx-border-width: 1;");
        root.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.5)));
        root.setPrefWidth(580);
        
        StackPane wrapper = new StackPane(root);
        wrapper.setStyle("-fx-background-color: transparent;");
        wrapper.setPadding(new Insets(20));
        
        // Header
        HBox header = new HBox(14);
        header.setPadding(new Insets(24, 28, 20, 28));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #8B5CF620, transparent); -fx-background-radius: 16 16 0 0;");
        
        StackPane iconBg = new StackPane();
        iconBg.setPrefSize(48, 48);
        iconBg.setStyle("-fx-background-color: #8B5CF625; -fx-background-radius: 12;");
        iconBg.getChildren().add(IconosSVG.excel("#8B5CF6", 26));
        
        VBox titleBox = new VBox(2);
        Label lblTitulo = new Label("Actualizar Archivos Existentes");
        lblTitulo.setTextFill(Color.web(TemaManager.getText()));
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        Label lblSubtitulo = new Label("Se encontraron " + totalArchivos + " archivo(s) Excel");
        lblSubtitulo.setTextFill(COLOR_TEXT_MUTED());
        lblSubtitulo.setFont(Font.font("Segoe UI", 12));
        titleBox.getChildren().addAll(lblTitulo, lblSubtitulo);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        // Botón cerrar rojo con Lucide
        Button btnCerrarDialog = crearBotonCerrarLucide(dialog);
        
        header.getChildren().addAll(iconBg, titleBox, btnCerrarDialog);
        
        // Contenido
        VBox content = new VBox(20);
        content.setPadding(new Insets(16, 32, 28, 32));
        
        Label lblPregunta = new Label("¿Deseas actualizar los archivos Excel existentes con la nueva contraseña?");
        lblPregunta.setTextFill(Color.web(TemaManager.getText()));
        lblPregunta.setFont(Font.font("Segoe UI", 13));
        lblPregunta.setWrapText(true);
        
        // Progreso (oculto inicialmente)
        VBox progresoBox = new VBox(8);
        progresoBox.setVisible(false);
        progresoBox.setManaged(false);
        
        Label lblProgreso = new Label("Procesando archivos...");
        lblProgreso.setTextFill(Color.web(TemaManager.getText()));
        
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #8B5CF6;");
        
        Label lblDetalle = new Label("");
        lblDetalle.setTextFill(COLOR_TEXT_MUTED());
        lblDetalle.setFont(Font.font("Segoe UI", 11));
        
        progresoBox.getChildren().addAll(lblProgreso, progressBar, lblDetalle);
        
        content.getChildren().addAll(lblPregunta, progresoBox);
        
        // Botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER_RIGHT);
        botones.setPadding(new Insets(20, 28, 24, 28));
        botones.setStyle("-fx-border-color: " + TemaManager.getBorder() + "; -fx-border-width: 1 0 0 0;");
        
        Button btnNo = crearBotonDialogo("No, solo nuevos", "#6B7280", false);
        btnNo.setOnAction(e -> {
            dialog.close();
            mostrarMensaje("Contraseña Actualizada", 
                "La contraseña ha sido cambiada.\nLos archivos existentes mantienen su contraseña anterior.", "#10B981");
        });
        
        Button btnSi = crearBotonDialogo("Sí, actualizar todos", "#8B5CF6", true);
        btnSi.setOnAction(e -> {
            // Mostrar progreso
            lblPregunta.setVisible(false);
            lblPregunta.setManaged(false);
            progresoBox.setVisible(true);
            progresoBox.setManaged(true);
            btnSi.setDisable(true);
            btnNo.setDisable(true);
            
            // Ejecutar recifrado en hilo separado usando la lista ya detectada
            new Thread(() -> {
                int exitosos = 0;
                int fallidos = 0;
                
                System.out.println("[DEBUG] Iniciando recifrado de " + archivosExcel.size() + " archivos");
                
                for (int i = 0; i < archivosExcel.size(); i++) {
                    java.nio.file.Path archivo = archivosExcel.get(i);
                    String nombreArchivo = archivo.getFileName().toString();
                    
                    final int idx = i;
                    final double progress = (double) (i + 1) / archivosExcel.size();
                    
                    Platform.runLater(() -> {
                        lblProgreso.setText("Procesando " + (idx + 1) + " de " + archivosExcel.size());
                        progressBar.setProgress(progress);
                        lblDetalle.setText(nombreArchivo);
                    });
                    
                    System.out.println("[DEBUG] Procesando: " + archivo.toAbsolutePath());
                    
                    try {
                        if (recifrarArchivoExcel(archivo, passwordAnterior, passwordNueva)) {
                            exitosos++;
                            System.out.println("[DEBUG] ✓“ Recifrado exitoso: " + nombreArchivo);
                        } else {
                            fallidos++;
                            System.out.println("[DEBUG] ✓— Recifrado fallido: " + nombreArchivo);
                        }
                    } catch (Exception ex) {
                        fallidos++;
                        AppLogger.getLogger(AdminPanelFX.class).error("[DEBUG] Error recifrado: " + nombreArchivo + " - " + ex.getMessage(), ex);
                    }
                    
                    try { Thread.sleep(100); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                }
                
                final int exitososFinal = exitosos;
                final int fallidosFinal = fallidos;
                
                System.out.println("[DEBUG] Recifrado terminado. Exitosos: " + exitosos + ", Fallidos: " + fallidos);
                
                Platform.runLater(() -> {
                    dialog.close();
                    
                    if (fallidosFinal == 0 && exitososFinal > 0) {
                        mostrarMensaje("áActualizacin Completa!", 
                            "Contraseña cambiada y " + exitososFinal + " archivo(s) actualizados correctamente.", "#10B981");
                    } else if (exitososFinal > 0) {
                        mostrarMensaje("Actualizacin Parcial", 
                            "Contraseña cambiada.\n" + exitososFinal + " archivo(s) actualizados, " + fallidosFinal + " no pudieron ser actualizados.", "#F59E0B");
                    } else {
                        mostrarMensaje("Contraseña Cambiada", 
                            "La contraseña ha sido cambiada pero los archivos existentes no pudieron ser actualizados.\n" +
                            "Es posible que tengan una contraseña diferente o no estn cifrados.", "#F59E0B");
                    }
                });
            }, "Admin-RecifrarArchivos").start();
        });
        
        botones.getChildren().addAll(btnNo, btnSi);
        
        root.getChildren().addAll(header, content, botones);
        
        // Hacer arrastrable
        root.setOnMousePressed(ev -> { xOffset = ev.getSceneX(); yOffset = ev.getSceneY(); });
        root.setOnMouseDragged(ev -> { dialog.setX(ev.getScreenX() - xOffset); dialog.setY(ev.getScreenY() - yOffset); });
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.setOnShown(e -> AnimacionesFX.animarEntradaDialogo(root));
        dialog.showAndWait();
    }
    
    /**
     * Muestra diálogo para recifrar archivos Excel existentes con nueva contraseña
     */
    private static void mostrarDialogoRecifrarExcel(String passwordAnterior, String passwordNueva) {
        Stage dialog = new Stage();
        dialog.initOwner(adminStage);
        dialog.initStyle(StageStyle.TRANSPARENT);
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + TemaManager.getSurface() + "; -fx-background-radius: 16; -fx-border-color: " + TemaManager.getBorder() + "; -fx-border-radius: 16; -fx-border-width: 1;");
        root.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.5)));
        root.setPrefWidth(580);
        
        StackPane wrapper = new StackPane(root);
        wrapper.setStyle("-fx-background-color: transparent;");
        wrapper.setPadding(new Insets(20));
        
        // Header
        HBox header = new HBox(14);
        header.setPadding(new Insets(24, 28, 20, 28));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #8B5CF620, transparent); -fx-background-radius: 16 16 0 0;");
        
        StackPane iconBg = new StackPane();
        iconBg.setPrefSize(48, 48);
        iconBg.setStyle("-fx-background-color: #8B5CF625; -fx-background-radius: 12;");
        iconBg.getChildren().add(IconosSVG.excel("#8B5CF6", 26));
        
        VBox titleBox = new VBox(2);
        Label lblTitulo = new Label("Recifrar Archivos Excel");
        lblTitulo.setTextFill(Color.web(TemaManager.getText()));
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        Label lblSubtitulo = new Label("Actualizar contraseña de archivos Excel existentes");
        lblSubtitulo.setTextFill(COLOR_TEXT_MUTED());
        lblSubtitulo.setFont(Font.font("Segoe UI", 12));
        titleBox.getChildren().addAll(lblTitulo, lblSubtitulo);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Botón cerrar rojo con Lucide
        Button btnCerrar = crearBotonCerrarLucide(dialog);
        header.getChildren().addAll(iconBg, titleBox, spacer, btnCerrar);
        
        // Contenido
        VBox content = new VBox(20);
        content.setPadding(new Insets(16, 32, 28, 32));
        
        // Buscar archivos Excel existentes
        List<java.nio.file.Path> archivosExcel = new ArrayList<>();
        try {
            java.nio.file.Path carpeta = InventarioFXBase.obtenerCarpetaEjecutable();
            if (Files.exists(carpeta)) {
                archivosExcel = Files.list(carpeta)
                    .filter(p -> {
                        String nombre = p.getFileName().toString().toLowerCase();
                        return nombre.startsWith("inventario_") && nombre.endsWith(".xlsx");
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
        } catch (Exception ex) {
            AppLogger.getLogger(AdminPanelFX.class).error("Error: " + ex.getMessage(), ex);
        }
        
        Label lblInfo = new Label("Se encontraron " + archivosExcel.size() + " archivo(s) Excel de inventario:");
        lblInfo.setTextFill(Color.web(TemaManager.getText()));
        lblInfo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        
        // Lista de archivos con checkboxes
        VBox listaArchivos = new VBox(8);
        listaArchivos.setStyle("-fx-background-color: " + TemaManager.getBg() + "; -fx-background-radius: 10;");
        listaArchivos.setPadding(new Insets(12));
        listaArchivos.setMaxHeight(200);
        
        ScrollPane scrollArchivos = new ScrollPane(listaArchivos);
        scrollArchivos.setFitToWidth(true);
        scrollArchivos.setMaxHeight(200);
        scrollArchivos.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        List<CheckBox> checkboxes = new ArrayList<>();
        List<java.nio.file.Path> archivosFinales = archivosExcel;
        
        if (archivosExcel.isEmpty()) {
            Label lblNoArchivos = new Label("No se encontraron archivos Excel de inventario");
            lblNoArchivos.setTextFill(COLOR_TEXT_MUTED());
            listaArchivos.getChildren().add(lblNoArchivos);
        } else {
            for (java.nio.file.Path archivo : archivosExcel) {
                HBox filaArchivo = new HBox(10);
                filaArchivo.setAlignment(Pos.CENTER_LEFT);
                
                CheckBox chk = new CheckBox();
                chk.setSelected(true);
                chk.setUserData(archivo);
                checkboxes.add(chk);
                
                Label lblArchivo = new Label(archivo.getFileName().toString());
                lblArchivo.setTextFill(Color.web(TemaManager.getText()));
                lblArchivo.setFont(Font.font("Segoe UI", 12));
                
                try {
                    long size = Files.size(archivo);
                    String sizeStr = size > 1024*1024 ? 
                        String.format("%.1f MB", size / (1024.0 * 1024)) : 
                        String.format("%.1f KB", size / 1024.0);
                    Label lblSize = new Label("(" + sizeStr + ")");
                    lblSize.setTextFill(COLOR_TEXT_MUTED());
                    lblSize.setFont(Font.font("Segoe UI", 10));
                    filaArchivo.getChildren().addAll(chk, lblArchivo, lblSize);
                } catch (Exception ex) {
                    filaArchivo.getChildren().addAll(chk, lblArchivo);
                }
                
                listaArchivos.getChildren().add(filaArchivo);
            }
        }
        
        // Seleccionar/Deseleccionar todos
        HBox seleccionBox = new HBox(12);
        seleccionBox.setAlignment(Pos.CENTER_LEFT);
        
        Button btnSelTodos = new Button("Seleccionar todos");
        btnSelTodos.setStyle("-fx-background-color: transparent; -fx-text-fill: #3B82F6; -fx-cursor: hand; -fx-underline: true;");
        btnSelTodos.setOnAction(e -> checkboxes.forEach(c -> c.setSelected(true)));
        
        Button btnDeselTodos = new Button("Deseleccionar todos");
        btnDeselTodos.setStyle("-fx-background-color: transparent; -fx-text-fill: #3B82F6; -fx-cursor: hand; -fx-underline: true;");
        btnDeselTodos.setOnAction(e -> checkboxes.forEach(c -> c.setSelected(false)));
        
        seleccionBox.getChildren().addAll(btnSelTodos, btnDeselTodos);
        
        // rea de progreso (oculta inicialmente)
        VBox progresoBox = new VBox(8);
        progresoBox.setVisible(false);
        progresoBox.setManaged(false);
        progresoBox.setPadding(new Insets(12));
        progresoBox.setStyle("-fx-background-color: " + TemaManager.getBg() + "; -fx-background-radius: 10;");
        
        Label lblProgreso = new Label("Procesando...");
        lblProgreso.setTextFill(Color.web(TemaManager.getText()));
        lblProgreso.setFont(Font.font("Segoe UI", 12));
        
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #8B5CF6;");
        
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(5);
        logArea.setStyle("-fx-control-inner-background: " + TemaManager.getSurface() + "; -fx-text-fill: " + TemaManager.getText() + ";");
        
        progresoBox.getChildren().addAll(lblProgreso, progressBar, logArea);
        
        // Advertencia
        HBox warningBox = new HBox(10);
        warningBox.setPadding(new Insets(12));
        warningBox.setStyle("-fx-background-color: #EF444420; -fx-background-radius: 10;");
        
        Label lblWarning = new Label("ú ï¸ Asegúrate de que la contraseña anterior sea correcta.\nLos archivos con contraseña incorrecta no podrán ser recifrados.");
        lblWarning.setTextFill(Color.web("#EF4444"));
        lblWarning.setFont(Font.font("Segoe UI", 11));
        lblWarning.setWrapText(true);
        warningBox.getChildren().add(lblWarning);
        
        content.getChildren().addAll(lblInfo, scrollArchivos, seleccionBox, progresoBox, warningBox);
        
        // Botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER_RIGHT);
        botones.setPadding(new Insets(20, 28, 24, 28));
        botones.setStyle("-fx-border-color: " + TemaManager.getBorder() + "; -fx-border-width: 1 0 0 0;");
        
        Region spacerBtn = new Region();
        HBox.setHgrow(spacerBtn, Priority.ALWAYS);
        
        Button btnCancelar = crearBotonDialogo("Cancelar", "#DC2626", false);
        btnCancelar.setOnAction(e -> dialog.close());
        
        Button btnRecifrar = crearBotonDialogo("Recifrar Seleccionados", "#8B5CF6", true);
        btnRecifrar.setDisable(archivosExcel.isEmpty());
        
        btnRecifrar.setOnAction(e -> {
            List<java.nio.file.Path> seleccionados = checkboxes.stream()
                .filter(CheckBox::isSelected)
                .map(c -> (java.nio.file.Path) c.getUserData())
                .collect(java.util.stream.Collectors.toList());
            
            if (seleccionados.isEmpty()) {
                mostrarMensaje("Sin Seleccin", "Selecciona al menos un archivo para recifrar", "#F59E0B");
                return;
            }
            
            // Mostrar progreso
            progresoBox.setVisible(true);
            progresoBox.setManaged(true);
            btnRecifrar.setDisable(true);
            btnCancelar.setDisable(true);
            
            // Ejecutar en hilo separado
            new Thread(() -> {
                int total = seleccionados.size();
                int exitosos = 0;
                int fallidos = 0;
                StringBuilder log = new StringBuilder();
                
                for (int i = 0; i < total; i++) {
                    java.nio.file.Path archivo = seleccionados.get(i);
                    String nombreArchivo = archivo.getFileName().toString();
                    
                    final int idx = i;
                    final double progress = (double) (i + 1) / total;
                    
                    Platform.runLater(() -> {
                        lblProgreso.setText("Procesando " + (idx + 1) + " de " + total + ": " + nombreArchivo);
                        progressBar.setProgress(progress);
                    });
                    
                    try {
                        boolean exito = recifrarArchivoExcel(archivo, passwordAnterior, passwordNueva);
                        if (exito) {
                            exitosos++;
                            log.append("✓“ ").append(nombreArchivo).append(" - OK\n");
                        } else {
                            fallidos++;
                            log.append("✓— ").append(nombreArchivo).append(" - Contraseña incorrecta\n");
                        }
                    } catch (Exception ex) {
                        fallidos++;
                        log.append("✓— ").append(nombreArchivo).append(" - Error: ").append(ex.getMessage()).append("\n");
                    }
                    
                    final String logText = log.toString();
                    Platform.runLater(() -> logArea.setText(logText));
                    
                    try { Thread.sleep(100); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                }
                
                final int exitososFinal = exitosos;
                final int fallidosFinal = fallidos;
                
                Platform.runLater(() -> {
                    lblProgreso.setText("\u00a1Completado!");
                    progressBar.setProgress(1.0);
                    btnCancelar.setDisable(false);
                    btnCancelar.setText("Cerrar");
                    
                    if (fallidosFinal == 0) {
                        mostrarMensaje("Recifrado Exitoso", 
                            "Se recifaron " + exitososFinal + " archivo(s) correctamente.", "#10B981");
                    } else if (exitososFinal > 0) {
                        mostrarMensaje("Recifrado Parcial", 
                            exitososFinal + " archivo(s) recifrados, " + fallidosFinal + " con errores.", "#F59E0B");
                    } else {
                        mostrarMensaje("Error de Recifrado", 
                            "No se pudo recifrar ningún archivo. Verifica la contraseña anterior.", "#EF4444");
                    }
                });
            }, "Admin-DesencriptarBatch").start();
        });
        
        botones.getChildren().addAll(spacerBtn, btnCancelar, btnRecifrar);
        
        root.getChildren().addAll(header, content, botones);
        
        // Hacer arrastrable
        root.setOnMousePressed(ev -> { xOffset = ev.getSceneX(); yOffset = ev.getSceneY(); });
        root.setOnMouseDragged(ev -> { dialog.setX(ev.getScreenX() - xOffset); dialog.setY(ev.getScreenY() - yOffset); });
        
        Scene scene = new Scene(wrapper);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.setOnShown(e -> AnimacionesFX.animarEntradaDialogo(root));
        dialog.showAndWait();
    }
    
    /**
     * Recifra un archivo Excel con nueva contraseña
     */
    private static boolean recifrarArchivoExcel(java.nio.file.Path archivo, String passwordAnterior, String passwordNueva) {
        java.nio.file.Path temp = null;
        try {
            // Abrir con contraseña anterior
            POIFSFileSystem fs = new POIFSFileSystem(archivo.toFile(), true);
            EncryptionInfo info = new EncryptionInfo(fs);
            Decryptor d = info.getDecryptor();
            
            if (!d.verifyPassword(passwordAnterior)) {
                fs.close();
                return false;
            }
            
            InputStream dataStream = d.getDataStream(fs);
            XSSFWorkbook wb = new XSSFWorkbook(dataStream);
            dataStream.close();
            fs.close();
            
            // Guardar con nueva contraseña
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                wb.write(bos);
            } finally {
                wb.close();
            }
            
            EncryptionInfo infoNueva = new EncryptionInfo(EncryptionMode.agile);
            Encryptor encryptor = infoNueva.getEncryptor();
            encryptor.confirmPassword(passwordNueva);
            
            POIFSFileSystem fsNuevo = new POIFSFileSystem();
            OutputStream os = encryptor.getDataStream(fsNuevo);
            os.write(bos.toByteArray());
            os.close();
            
            // Guardar en archivo temporal
            temp = Files.createTempFile("recifrar_", ".xlsx");
            try (FileOutputStream fos = new FileOutputStream(temp.toFile())) {
                fsNuevo.writeFilesystem(fos);
            }
            fsNuevo.close();
            
            // Reemplazar archivo original
            Files.copy(temp, archivo, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(temp);
            
            // 🔒 Mantener oculto y protegido el archivo recifrado
            PortablePaths.protegerArchivo(archivo);
            
            return true;
            
        } catch (Exception e) {
            AppLogger.getLogger(AdminPanelFX.class).error("Error: " + e.getMessage(), e);
            if (temp != null) {
                try { Files.deleteIfExists(temp); } catch (Exception ignored) {}
            }
            return false;
        }
    }
    
    /**
     * Importa un archivo Excel y lo cifra con la contraseña del sistema.
     * Intenta abrir el Excel sin contraseña o con contraseñas comunes.
     * 
     * @param origen Ruta del archivo Excel a importar
     * @param destino Ruta donde guardar el archivo cifrado
     * @param passwordNueva Contraseña para cifrar el archivo
     * @return true si la importacin fue exitosa
     */
    private static boolean importarYCifrarExcel(java.nio.file.Path origen, java.nio.file.Path destino, String passwordNueva) {
        java.nio.file.Path temp = null;
        XSSFWorkbook wb = null;
        
        try {
            System.out.println("[AdminPanel] Intentando abrir Excel: " + origen);
            
            // Intentar abrir el Excel
            wb = abrirExcelParaImportar(origen);
            
            if (wb == null) {
                System.err.println("[AdminPanel] No se pudo abrir el Excel para importar");
                return false;
            }
            
            System.out.println("[AdminPanel] Excel abierto correctamente, cifrando...");
            
            // Escribir workbook a bytes
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            wb.close();
            wb = null;
            
            // Cifrar con la nueva contraseña
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor encryptor = info.getEncryptor();
            encryptor.confirmPassword(passwordNueva);
            
            POIFSFileSystem fsNuevo = new POIFSFileSystem();
            OutputStream os = encryptor.getDataStream(fsNuevo);
            os.write(bos.toByteArray());
            os.close();
            
            // Guardar en archivo temporal primero
            temp = Files.createTempFile("importar_cifrar_", ".xlsx");
            try (FileOutputStream fos = new FileOutputStream(temp.toFile())) {
                fsNuevo.writeFilesystem(fos);
            }
            fsNuevo.close();
            
            // Mover a destino final
            Files.copy(temp, destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(temp);
            
            // 🔒 Ocultar y proteger el archivo cifrado en el sistema de archivos
            PortablePaths.protegerArchivo(destino);
            System.out.println("[AdminPanel] Excel importado y cifrado correctamente: " + destino);
            return true;
            
        } catch (Exception e) {
            AppLogger.getLogger(AdminPanelFX.class).error("[AdminPanel] Error al importar y cifrar: " + e.getMessage(), e);
            if (temp != null) {
                try { Files.deleteIfExists(temp); } catch (Exception ignored) {}
            }
            if (wb != null) {
                try { wb.close(); } catch (Exception ignored) {}
            }
            return false;
        }
    }
    
    /**
     * Intenta abrir un archivo Excel para importacin.
     * Prueba: 1) Sin contraseña, 2) Con contraseña del sistema
     */
    private static XSSFWorkbook abrirExcelParaImportar(java.nio.file.Path archivo) {
        // Solo probar sin contraseña y con la contraseña configurada del sistema
        String[] passwords = {"", AdminManager.getExcelPassword()};
        
        for (String pass : passwords) {
            try {
                XSSFWorkbook wb;
                
                if (pass.isEmpty()) {
                    // Intentar abrir sin contraseña (Excel no cifrado)
                    try (FileInputStream fis = new FileInputStream(archivo.toFile())) {
                        wb = new XSSFWorkbook(fis);
                        System.out.println("[AdminPanel] Excel abierto sin contraseña");
                        return wb;
                    }
                } else {
                    // Intentar abrir con contraseña (Excel cifrado)
                    try {
                        POIFSFileSystem fs = new POIFSFileSystem(archivo.toFile(), true);
                        EncryptionInfo info = new EncryptionInfo(fs);
                        Decryptor d = info.getDecryptor();
                        
                        if (d.verifyPassword(pass)) {
                            InputStream dataStream = d.getDataStream(fs);
                            wb = new XSSFWorkbook(dataStream);
                            dataStream.close();
                            fs.close();
                            System.out.println("[AdminPanel] Excel abierto con contraseña" + (pass.equals(AdminManager.getExcelPassword()) ? " del sistema" : ""));
                            return wb;
                        }
                        fs.close();
                    } catch (Exception ignored) {
                        // Esta contraseña no funcion, probar siguiente
                    }
                }
            } catch (Exception ignored) {
                // Este intento fall, probar siguiente
            }
        }
        
        System.err.println("[AdminPanel] No se pudo abrir el Excel con ninguna contraseña conocida");
        return null;
    }
    
    /**
     * Muestra diálogo para cambiar clave de cifrado - FUNCIONAL
     */
    private static void mostrarCambiarClaveCifrado() {
        Stage dialog = new Stage();
        dialog.initOwner(adminStage);
        dialog.initStyle(StageStyle.TRANSPARENT);
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + TemaManager.getSurface() + "; -fx-background-radius: 16; -fx-border-color: " + TemaManager.getBorder() + "; -fx-border-radius: 16; -fx-border-width: 1;");
        root.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.5)));
        root.setPrefWidth(580);
        root.setMinHeight(520);
        
        StackPane wrapper = new StackPane(root);
        wrapper.setStyle("-fx-background-color: transparent;");
        wrapper.setPadding(new Insets(20));
        
        // Header con gradiente
        HBox header = new HBox(14);
        header.setPadding(new Insets(24, 28, 20, 28));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #EF444420, transparent); -fx-background-radius: 16 16 0 0;");
        
        StackPane iconBg = new StackPane();
        iconBg.setPrefSize(48, 48);
        iconBg.setStyle("-fx-background-color: #EF444425; -fx-background-radius: 12;");
        iconBg.getChildren().add(IconosSVG.escudo("#EF4444", 26));
        
        VBox titleBox = new VBox(2);
        Label lblTitulo = new Label("Clave de Cifrado");
        lblTitulo.setTextFill(Color.web(TemaManager.getText()));
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        Label lblSubtitulo = new Label("Protege los datos sensibles de la aplicacin");
        lblSubtitulo.setTextFill(COLOR_TEXT_MUTED());
        lblSubtitulo.setFont(Font.font("Segoe UI", 12));
        titleBox.getChildren().addAll(lblTitulo, lblSubtitulo);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnCerrar = inventario.fx.util.ComponentesFX.crearBotonCerrar(dialog::close, 36);
        
        header.getChildren().addAll(iconBg, titleBox, spacer, btnCerrar);
        
        // Contenido con indicador de carga
        VBox loadingContent = new VBox(16);
        loadingContent.setAlignment(Pos.CENTER);
        loadingContent.setPadding(new Insets(60, 28, 60, 28));
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(40, 40);
        Label lblCargando = new Label("³ Verificando Configuración...");
        lblCargando.setTextFill(COLOR_TEXT_MUTED());
        loadingContent.getChildren().addAll(spinner, lblCargando);
        
        root.getChildren().addAll(header, loadingContent);
        
        // Hacer arrastrable
        root.setOnMousePressed(ev -> { xOffset = ev.getSceneX(); yOffset = ev.getSceneY(); });
        root.setOnMouseDragged(ev -> { dialog.setX(ev.getScreenX() - xOffset); dialog.setY(ev.getScreenY() - yOffset); });
        
        Scene scene = new Scene(wrapper);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        
        // Cargar estado en hilo separado
        new Thread(() -> {
            boolean tieneClave = AdminManager.tieneClaveConfigrada();
            
            Platform.runLater(() -> {
                root.getChildren().remove(loadingContent);
                
                VBox content = new VBox(20);
                content.setPadding(new Insets(16, 32, 28, 32));
                content.setAlignment(Pos.TOP_LEFT);
                
                // Estado actual
                HBox estadoBox = new HBox(12);
                estadoBox.setAlignment(Pos.CENTER_LEFT);
                estadoBox.setPadding(new Insets(12, 16, 12, 16));
                estadoBox.setStyle("-fx-background-color: " + (tieneClave ? "#10B98120" : "#F59E0B20") + "; -fx-background-radius: 8;");
                
                Circle indicador = new Circle(5);
                indicador.setFill(Color.web(tieneClave ? "#10B981" : "#F59E0B"));
                
                Label lblEstado = new Label(tieneClave ? "Clave de cifrado configurada" : "Sin clave de cifrado configurada");
                lblEstado.setTextFill(Color.web(tieneClave ? "#10B981" : "#F59E0B"));
                lblEstado.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
                estadoBox.getChildren().addAll(indicador, lblEstado);
                
                // Advertencia
                HBox advertencia = new HBox(12);
                advertencia.setPadding(new Insets(12));
                advertencia.setStyle("-fx-background-color: #EF444415; -fx-background-radius: 8;");
                advertencia.setAlignment(Pos.CENTER_LEFT);
                
                Label lblAdv = new Label("ú ï¸ Esta clave se usará para proteger datos sensibles.\nGuárdela en un lugar seguro, no podrá recuperarse.");
                lblAdv.setTextFill(Color.web("#EF4444"));
                lblAdv.setFont(Font.font("Segoe UI", 11));
                lblAdv.setWrapText(true);
                advertencia.getChildren().add(lblAdv);
                
                PasswordField claveActual = crearPasswordField("Clave actual (dejar vacío si es primera vez)");
                PasswordField claveNueva = crearPasswordField("Nueva clave de cifrado (mínimo 8 caracteres)");
                PasswordField confirmarClave = crearPasswordField("Confirmar nueva clave");
                
                Label lblError = new Label();
                lblError.setTextFill(Color.web("#EF4444"));
                lblError.setFont(Font.font("Segoe UI", 12));
                lblError.setVisible(false);
                
                HBox botones = new HBox(12);
                botones.setAlignment(Pos.CENTER_RIGHT);
                botones.setPadding(new Insets(16, 0, 0, 0));
                
                Button btnCancelar = crearBotonDialogo("Cancelar", "#DC2626", false);
                btnCancelar.setOnAction(e -> dialog.close());
                
                Button btnGuardar = crearBotonDialogo("Guardar Clave", "#EF4444", true);
                btnGuardar.setOnAction(e -> {
                    String actual = claveActual.getText();
                    String nueva = claveNueva.getText();
                    String confirmar = confirmarClave.getText();
                    
                    // Validaciones
                    if (tieneClave && !AdminManager.verificarClaveCifrado(actual)) {
                        lblError.setText("La clave actual es incorrecta");
                        lblError.setVisible(true);
                        return;
                    }
                    
                    if (nueva.length() < 8) {
                        lblError.setText("La clave debe tener al menos 8 caracteres");
                        lblError.setVisible(true);
                        return;
                    }
                    
                    if (!nueva.equals(confirmar)) {
                        lblError.setText("Las claves no coinciden");
                        lblError.setVisible(true);
                        return;
                    }
                    
                    // Guardar clave
                    AdminManager.guardarClaveCifrado(nueva);
                    dialog.close();
                    mostrarMensaje("Clave Configurada", "La clave de cifrado se ha guardado correctamente", "#10B981");
                });
                
                botones.getChildren().addAll(btnCancelar, btnGuardar);
                content.getChildren().addAll(estadoBox, advertencia, claveActual, claveNueva, confirmarClave, lblError, botones);
                root.getChildren().add(content);
            });
        }, "Admin-CargarEstadoClave").start();
        
        dialog.setOnShown(e -> AnimacionesFX.animarEntradaDialogo(root));
        dialog.showAndWait();
    }
    
    /**
     * Muestra el log de accesos REAL con datos persistentes
     */
    private static void mostrarLogAccesos() {
        Stage dialog = new Stage();
        dialog.initOwner(adminStage);
        dialog.initStyle(StageStyle.TRANSPARENT);
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + TemaManager.getSurface() + "; -fx-background-radius: 20; -fx-border-color: " + TemaManager.getBorder() + "; -fx-border-radius: 20; -fx-border-width: 1;");
        root.setEffect(new DropShadow(40, Color.rgb(0, 0, 0, 0.4)));
        root.setPrefWidth(1100);
        root.setPrefHeight(750);
        
        // Header moderno con icono Lucide
        HBox header = new HBox(16);
        header.setPadding(new Insets(24, 32, 20, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #6366F115, #8B5CF610, transparent); -fx-background-radius: 20 20 0 0;");
        
        // Icono con fondo circular
        StackPane iconBg = new StackPane();
        iconBg.setPrefSize(52, 52);
        iconBg.setMinSize(52, 52);
        Circle iconCircle = new Circle(26);
        iconCircle.setFill(Color.web("#6366F120"));
        iconBg.getChildren().addAll(iconCircle, IconosSVG.lista("#6366F1", 26));
        
        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label lblTituloHeader = new Label("Registro de Actividad");
        lblTituloHeader.setTextFill(Color.web(TemaManager.getText()));
        lblTituloHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        Label lblSubtituloHeader = new Label("Monitorea todas las acciones del sistema");
        lblSubtituloHeader.setTextFill(COLOR_TEXT_MUTED());
        lblSubtituloHeader.setFont(Font.font("Segoe UI", 13));
        titleBox.getChildren().addAll(lblTituloHeader, lblSubtituloHeader);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        // Boton cerrar rojo con icono Lucide
        Button btnCerrarX = crearBotonCerrarLucide(dialog);
        
        header.getChildren().addAll(iconBg, titleBox, btnCerrarX);
        
        // Hacer header arrastrable
        header.setOnMousePressed(ev -> { xOffset = ev.getSceneX(); yOffset = ev.getSceneY(); });
        header.setOnMouseDragged(ev -> { dialog.setX(ev.getScreenX() - xOffset); dialog.setY(ev.getScreenY() - yOffset); });
        
        // Contenido de carga
        VBox loadingContent = new VBox(20);
        loadingContent.setAlignment(Pos.CENTER);
        loadingContent.setPadding(new Insets(100));
        VBox.setVgrow(loadingContent, Priority.ALWAYS);
        
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(60, 60);
        spinner.setStyle("-fx-accent: #6366F1;");
        Label lblCargando = new Label("Cargando historial de actividad...");
        lblCargando.setTextFill(COLOR_TEXT_MUTED());
        lblCargando.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        loadingContent.getChildren().addAll(spinner, lblCargando);
        
        root.getChildren().addAll(header, loadingContent);
        
        StackPane wrapperLog = new StackPane(root);
        wrapperLog.setStyle("-fx-background-color: transparent;");
        wrapperLog.setPadding(new Insets(20));
        
        Scene scene = new Scene(wrapperLog);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        
        // Cargar datos en hilo separado
        new Thread(() -> {
            List<AdminManager.LogAcceso> todosLogs = AdminManager.getLogsAcceso();
            
            int seguridad = contarLogsPorCategoria(todosLogs, "Seguridad");
            int proyectos = contarLogsPorCategoria(todosLogs, "Proyecto");
            int configuracion = contarLogsPorCategoria(todosLogs, "Configuración");
            int sistema = contarLogsPorCategoria(todosLogs, "Sistema") + contarLogsPorCategoria(todosLogs, "Mantenimiento");
            
            Platform.runLater(() -> {
                root.getChildren().remove(loadingContent);
                
                // Cards de estadisticas con iconos Lucide
                HBox statsRow = new HBox(16);
                statsRow.setPadding(new Insets(16, 32, 24, 32));
                statsRow.setAlignment(Pos.CENTER_LEFT);
                statsRow.getChildren().addAll(
                    crearStatCardLog(IconosSVG.lista("#6366F1", 20), "Total", todosLogs.size(), "#6366F1"),
                    crearStatCardLog(IconosSVG.candado("#EF4444", 20), "Seguridad", seguridad, "#EF4444"),
                    crearStatCardLog(IconosSVG.carpeta("#10B981", 20), "Proyectos", proyectos, "#10B981"),
                    crearStatCardLog(IconosSVG.herramienta("#F59E0B", 20), "Config", configuracion, "#F59E0B"),
                    crearStatCardLog(IconosSVG.servidor("#8B5CF6", 20), "Sistema", sistema, "#8B5CF6")
                );
                
                // Barra de busqueda moderna
                HBox searchBar = new HBox(16);
                searchBar.setPadding(new Insets(0, 32, 16, 32));
                searchBar.setAlignment(Pos.CENTER_LEFT);
                
                // Campo de busqueda con icono
                HBox searchContainer = new HBox(10);
                searchContainer.setAlignment(Pos.CENTER_LEFT);
                searchContainer.setPadding(new Insets(10, 16, 10, 16));
                searchContainer.setStyle("-fx-background-color: " + TemaManager.getBg() + "; -fx-background-radius: 12; -fx-border-color: " + TemaManager.getBorder() + "; -fx-border-radius: 12;");
                searchContainer.getChildren().add(IconosSVG.buscar(TemaManager.getTextMuted(), 16));
                
                TextField txtBuscar = new TextField();
                txtBuscar.setPromptText("Buscar en registros...");
                txtBuscar.setPrefWidth(280);
                txtBuscar.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TemaManager.getText() + "; -fx-padding: 0;");
                searchContainer.getChildren().add(txtBuscar);
                HBox.setHgrow(searchContainer, Priority.ALWAYS);
                
                // Filtro categoria
                ComboBox<String> cmbFiltro = new ComboBox<>();
                cmbFiltro.getItems().addAll("Todos", "Seguridad", "Proyecto", "Configuración", "Sistema");
                cmbFiltro.setValue("Todos");
                cmbFiltro.setStyle("-fx-background-color: " + TemaManager.getBg() + "; -fx-border-color: " + TemaManager.getBorder() + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 6 12;");
                cmbFiltro.setPrefWidth(140);
                
                // Cantidad
                ComboBox<String> cmbCantidad = new ComboBox<>();
                cmbCantidad.getItems().addAll("50", "100", "200", "Todo");
                cmbCantidad.setValue("100");
                cmbCantidad.setStyle("-fx-background-color: " + TemaManager.getBg() + "; -fx-border-color: " + TemaManager.getBorder() + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 6 12;");
                cmbCantidad.setPrefWidth(100);
                
                searchBar.getChildren().addAll(searchContainer, cmbFiltro, cmbCantidad);
                
                // Contenedor de logs con cards
                VBox logsContainer = new VBox(8);
                logsContainer.setPadding(new Insets(8));
                
                ScrollPane scroll = new ScrollPane(logsContainer);
                scroll.setFitToWidth(true);
                scroll.setPadding(new Insets(0, 24, 0, 24));
                scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
                VBox.setVgrow(scroll, Priority.ALWAYS);
                
                // Funcion para actualizar logs
                Runnable actualizarLogs = () -> {
                    logsContainer.getChildren().clear();
                    
                    String filtro = cmbFiltro.getValue();
                    String busqueda = txtBuscar.getText().toLowerCase().trim();
                    int cantidad = cmbCantidad.getValue().equals("Todo") ? todosLogs.size() : Integer.parseInt(cmbCantidad.getValue());
                    
                    List<AdminManager.LogAcceso> logsFiltrados = new ArrayList<>();
                    for (AdminManager.LogAcceso log : todosLogs) {
                        boolean pasaFiltro = filtro.equals("Todos") || log.getAccion().contains(filtro);
                        boolean pasaBusqueda = busqueda.isEmpty() || 
                            log.getDetalle().toLowerCase().contains(busqueda) ||
                            log.getAccion().toLowerCase().contains(busqueda);
                        
                        if (pasaFiltro && pasaBusqueda) {
                            logsFiltrados.add(log);
                        }
                        if (logsFiltrados.size() >= cantidad) break;
                    }
                    
                    if (logsFiltrados.isEmpty()) {
                        VBox emptyState = new VBox(16);
                        emptyState.setAlignment(Pos.CENTER);
                        emptyState.setPadding(new Insets(60));
                        emptyState.getChildren().addAll(
                            IconosSVG.documento(TemaManager.getTextMuted(), 48),
                            new Label("No hay registros")
                        );
                        Color emptyTextColor = TemaManager.getTextMutedColor();
                        ((Label)emptyState.getChildren().get(1)).setTextFill(emptyTextColor);
                        ((Label)emptyState.getChildren().get(1)).setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 16));
                        logsContainer.getChildren().add(emptyState);
                    } else {
                        for (AdminManager.LogAcceso log : logsFiltrados) {
                            logsContainer.getChildren().add(crearCardLog(log));
                        }
                    }
                };
                
                cmbFiltro.setOnAction(e -> actualizarLogs.run());
                cmbCantidad.setOnAction(e -> actualizarLogs.run());
                txtBuscar.textProperty().addListener((obs, old, nuevo) -> actualizarLogs.run());
                
                actualizarLogs.run();
                
                // Footer con botones modernos
                HBox footer = new HBox(12);
                footer.setAlignment(Pos.CENTER);
                footer.setPadding(new Insets(20, 32, 24, 32));
                footer.setStyle("-fx-background-color: " + TemaManager.getBg() + "80; -fx-background-radius: 0 0 20 20;");
                
                Button btnLimpiar = crearBotonLogAccion(IconosSVG.papelera("#EF4444", 16), "Limpiar", "#EF4444");
                btnLimpiar.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.initOwner(dialog);
                    confirm.setTitle("Limpiar Logs");
                    confirm.setHeaderText("Eliminar todos los registros?");
                    confirm.setContentText("Se eliminaran " + todosLogs.size() + " registros.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            AdminManager.limpiarLogs();
                            dialog.close();
                            mostrarMensaje("Completado", "Registros eliminados", "#10B981");
                        }
                    });
                });
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Button btnCSV = crearBotonLogAccion(IconosSVG.descargar("#10B981", 16), "CSV", "#10B981");
                btnCSV.setOnAction(e -> exportarLogsFormato(dialog, todosLogs, "csv"));
                
                Button btnTXT = crearBotonLogAccion(IconosSVG.documento("#3B82F6", 16), "TXT", "#3B82F6");
                btnTXT.setOnAction(e -> exportarLogsFormato(dialog, todosLogs, "txt"));
                
                Button btnCerrar = crearBotonLogAccion(IconosSVG.cerrar("#64748B", 16), "Cerrar", "#64748B");
                btnCerrar.setOnAction(e -> dialog.close());
                
                footer.getChildren().addAll(btnLimpiar, spacer, btnCSV, btnTXT, btnCerrar);
                
                root.getChildren().addAll(statsRow, searchBar, scroll, footer);
            });
        }, "Admin-CargarLogs").start();
        
        dialog.setOnShown(e -> AnimacionesFX.animarEntradaDialogo(root));
        dialog.showAndWait();
    }
    
    // Card de estadistica para logs con animacion
    private static HBox crearStatCardLog(javafx.scene.Node icono, String label, int valor, String color) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 18, 12, 14));
        card.setStyle("-fx-background-color: " + color + "12; -fx-background-radius: 14; -fx-border-color: transparent; -fx-border-radius: 14; -fx-border-width: 1;");
        
        StackPane iconContainer = new StackPane(icono);
        iconContainer.setPrefSize(36, 36);
        iconContainer.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 10;");
        
        VBox textos = new VBox(2);
        Label lblValor = new Label(String.valueOf(valor));
        lblValor.setTextFill(Color.web(color));
        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        Label lblLabel = new Label(label);
        lblLabel.setTextFill(COLOR_TEXT_MUTED());
        lblLabel.setFont(Font.font("Segoe UI", 11));
        textos.getChildren().addAll(lblValor, lblLabel);
        
        card.getChildren().addAll(iconContainer, textos);
        
        // Animacion hover premium
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 14; -fx-border-color: " + color + "50; -fx-border-radius: 14; -fx-border-width: 1;");
            AnimacionesFX.hoverIn(card);
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: " + color + "12; -fx-background-radius: 14; -fx-border-color: transparent; -fx-border-radius: 14; -fx-border-width: 1;");
            AnimacionesFX.hoverOut(card);
        });
        
        return card;
    }
    
    // Card individual para cada log
    private static HBox crearCardLog(AdminManager.LogAcceso log) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-border-color: " + TemaManager.getBorder() + "; -fx-border-radius: 12; -fx-border-width: 1.5;");
        
        // Icono segun categoria - determinar color
        final String colorCard;
        javafx.scene.Node icono;
        if (log.getAccion().contains("Seguridad")) {
            colorCard = "#EF4444";
            icono = IconosSVG.candado(colorCard, 18);
        } else if (log.getAccion().contains("Proyecto")) {
            colorCard = "#10B981";
            icono = IconosSVG.carpeta(colorCard, 18);
        } else if (log.getAccion().contains("Configuración") || log.getAccion().contains("rutas")) {
            colorCard = "#F59E0B";
            icono = IconosSVG.herramienta(colorCard, 18);
        } else if (log.getAccion().contains("Sistema") || log.getAccion().contains("Mantenimiento")) {
            colorCard = "#8B5CF6";
            icono = IconosSVG.servidor(colorCard, 18);
        } else {
            colorCard = "#3B82F6";
            icono = IconosSVG.info(colorCard, 18);
        }
        
        StackPane iconBg = new StackPane(icono);
        iconBg.setPrefSize(36, 36);
        iconBg.setMinSize(36, 36);
        iconBg.setStyle("-fx-background-color: " + colorCard + "15; -fx-background-radius: 10;");
        
        // Info principal
        VBox infoBox = new VBox(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        HBox accionRow = new HBox(10);
        accionRow.setAlignment(Pos.CENTER_LEFT);
        Label lblAccion = new Label(log.getAccion());
        lblAccion.setTextFill(Color.web(colorCard));
        lblAccion.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        // Forzar color CSS explícito para evitar que la hoja de estilos deje el texto en blanco
        lblAccion.setStyle("-fx-text-fill: " + colorCard + ";");

        Label lblDetalle = new Label(log.getDetalle());
        Color detalleColor = TemaManager.getTextColor();
        lblDetalle.setTextFill(detalleColor);
        lblDetalle.setFont(Font.font("Segoe UI", 13));
        lblDetalle.setStyle("-fx-text-fill: " + TemaManager.getText() + ";");
        accionRow.getChildren().addAll(lblAccion, lblDetalle);
        
        HBox metaRow = new HBox(16);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label lblFecha = new Label(log.getFecha());
        Color metaColor = TemaManager.getTextMutedColor();
        lblFecha.setTextFill(metaColor);
        lblFecha.setFont(Font.font("Consolas", 11));
        Label lblUsuario = new Label(log.getUsuario());
        lblUsuario.setTextFill(metaColor);
        lblUsuario.setFont(Font.font("Segoe UI", 11));
        Label lblIP = new Label(log.getIp() != null ? log.getIp() : "");
        lblIP.setTextFill(metaColor);
        lblIP.setFont(Font.font("Consolas", 10));
        if (!TemaManager.isDarkMode()) {
            lblFecha.setStyle("-fx-text-fill: #6B7280;");
            lblUsuario.setStyle("-fx-text-fill: #6B7280;");
            lblIP.setStyle("-fx-text-fill: #6B7280;");
        }
        metaRow.getChildren().addAll(lblFecha, lblUsuario, lblIP);
        
        infoBox.getChildren().addAll(accionRow, metaRow);
        
        card.getChildren().addAll(iconBg, infoBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: " + colorCard + "08; -fx-background-radius: 12; -fx-border-color: " + colorCard + "40; -fx-border-radius: 12; -fx-border-width: 1.5;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-border-color: " + TemaManager.getBorder() + "; -fx-border-radius: 12; -fx-border-width: 1.5;"));
        
        return card;
    }
    
    // Boton de accion para logs
    private static Button crearBotonLogAccion(javafx.scene.Node icono, String texto, String color) {
        Button btn = new Button();
        HBox content = new HBox(8);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(icono, new Label(texto));
        ((Label)content.getChildren().get(1)).setTextFill(Color.web(color));
        ((Label)content.getChildren().get(1)).setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        btn.setGraphic(content);
        btn.setPadding(new Insets(10, 20, 10, 16));
        btn.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 10; -fx-border-color: transparent; -fx-border-width: 0; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-insets: 0; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + color + "25; -fx-background-radius: 10; -fx-border-color: transparent; -fx-border-width: 0; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-insets: 0; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 10; -fx-border-color: transparent; -fx-border-width: 0; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-insets: 0; -fx-cursor: hand;"));
        btn.setFocusTraversable(false);
        return btn;
    }
    
    // Exportar logs a formato
    private static void exportarLogsFormato(Stage dialog, List<AdminManager.LogAcceso> logs, String formato) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar Logs");
        fc.setInitialFileName("logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "." + formato);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(formato.toUpperCase(), "*." + formato));
        // Directorio inicial: Exportaciones/Logs/
        fc.setInitialDirectory(inventario.fx.config.PortablePaths.getExportLogsDir().toFile());
        File archivo = fc.showSaveDialog(dialog);
        
        if (archivo != null) {
            new Thread(() -> {
                try {
                    if (formato.equals("csv")) {
                        StringBuilder csv = new StringBuilder();
                        csv.append("Fecha,Accion,Detalle,Usuario,IP\n");
                        for (AdminManager.LogAcceso log : logs) {
                            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                                log.getFecha(), log.getAccion(), log.getDetalle(), log.getUsuario(), 
                                log.getIp() != null ? log.getIp() : ""));
                        }
                        Files.writeString(archivo.toPath(), csv.toString());
                    } else {
                        Files.writeString(archivo.toPath(), AdminManager.exportarLogs());
                    }
                    Platform.runLater(() -> mostrarMensaje("Exportado", "Archivo guardado", "#10B981"));
                } catch (Exception ex) {
                    Platform.runLater(() -> mostrarMensaje("Error", "No se pudo guardar", "#EF4444"));
                }
            }, "Admin-ExportarLogs").start();
        }
    }
    
    /**
     * Cuenta logs por categoría
     */
    private static int contarLogsPorCategoria(List<AdminManager.LogAcceso> logs, String categoria) {
        int count = 0;
        for (AdminManager.LogAcceso log : logs) {
            if (log.getAccion().contains(categoria)) count++;
        }
        return count;
    }
    
    /**
     * Cierra el panel de Administración
     */
    private static void cerrarPanel() {
        if (adminStage != null) {
            // Animación de salida
            javafx.scene.Node root = adminStage.getScene().getRoot();
            
            FadeTransition fade = new FadeTransition(Duration.millis(200), root);
            fade.setToValue(0);
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), root);
            scale.setToX(0.95);
            scale.setToY(0.95);
            
            ParallelTransition exit = new ParallelTransition(fade, scale);
            exit.setOnFinished(e -> {
                adminStage.close();
                adminStage = null;
            });
            exit.play();
        }
    }
    
    /**
     * Verifica si el panel está abierto
     */
    public static boolean isOpen() {
        return adminStage != null && adminStage.isShowing();
    }
    
    /**
     * Obtiene el Stage del panel
     */
    public static Stage getStage() {
        return adminStage;
    }
    
    // ==================== NUEVAS FUNCIONALIDADES ====================
    
    /**
     * Duplica un proyecto existente (copia nombre, descripción, color e imagen)
     */
    private static void duplicarProyecto(AdminManager.Proyecto original) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(adminStage);
        confirm.setTitle("Duplicar Proyecto");
        confirm.setHeaderText("Duplicar \"" + original.getNombre() + "\"");
        confirm.setContentText("Se creará una copia del proyecto con el nombre:\n\"" + 
            original.getNombre() + " (copia)\"\n\n¿Continuar?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    AdminManager.Proyecto copia = new AdminManager.Proyecto();
                    copia.setNombre(original.getNombre() + " (copia)");
                    copia.setDescripcion(original.getDescripcion());
                    copia.setColor(original.getColor());
                    copia.setImagenPath(original.getImagenPath());
                    
                    boolean ok = AdminManager.agregarProyecto(copia, true);
                    Platform.runLater(() -> {
                        if (ok) {
                            actualizarListaProyectos();
                            mostrarMensaje("Proyecto Duplicado", 
                                "Se creó \"" + copia.getNombre() + "\" correctamente.", "#10B981");
                        } else {
                            mostrarMensaje("Error", "No se pudo duplicar el proyecto.", "#EF4444");
                        }
                    });
                }, "Admin-DuplicarProyecto").start();
            }
        });
    }
    
    /**
     * Descarga (exporta) el archivo Excel del proyecto a una ubicación elegida por el usuario
     */
    private static void descargarExcelProyecto(AdminManager.Proyecto proyecto, int index) {
        new Thread(() -> {
            try {
                String nombreLimpio = proyecto.getNombre()
                    .replaceAll("[\\\\/:*?\"<>|]", "")
                    .replace(" ", "_")
                    .trim();
                
                int indiceProyecto = index + 1;
                String nombreArchivo = "Inventario_" + indiceProyecto + " - " + nombreLimpio + ".xlsx";
                java.nio.file.Path rutaExcel = inventario.fx.config.PortablePaths.getProyectosDir().resolve(nombreArchivo);
                
                // Buscar con descripción si no existe
                if (!Files.exists(rutaExcel) && proyecto.getDescripcion() != null && !proyecto.getDescripcion().isEmpty()) {
                    String descLimpia = proyecto.getDescripcion()
                        .replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", "_").trim();
                    nombreArchivo = "Inventario_" + indiceProyecto + " - " + nombreLimpio + "_" + descLimpia + ".xlsx";
                    rutaExcel = inventario.fx.config.PortablePaths.getProyectosDir().resolve(nombreArchivo);
                }
                
                // Buscar por patrón
                if (!Files.exists(rutaExcel)) {
                    java.nio.file.Path carpeta = inventario.fx.config.PortablePaths.getProyectosDir();
                    try (java.util.stream.Stream<java.nio.file.Path> archivos = Files.list(carpeta)) {
                        java.util.Optional<java.nio.file.Path> encontrado = archivos
                            .filter(p -> p.getFileName().toString().toLowerCase().startsWith("inventario_" + indiceProyecto + " -"))
                            .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xlsx"))
                            .findFirst();
                        if (encontrado.isPresent()) rutaExcel = encontrado.get();
                    }
                }
                
                if (!Files.exists(rutaExcel)) {
                    Platform.runLater(() -> mostrarMensaje("Archivo no encontrado",
                        "No existe inventario para \"" + proyecto.getNombre() + "\".\nGenera uno primero desde el menú principal.",
                        "#F59E0B"));
                    return;
                }
                
                final java.nio.file.Path rutaFinal = rutaExcel;
                final String nombreFinal = rutaExcel.getFileName().toString();
                
                Platform.runLater(() -> {
                    javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                    fileChooser.setTitle("Guardar Excel - " + proyecto.getNombre());
                    fileChooser.setInitialFileName(nombreFinal);
                    fileChooser.getExtensionFilters().add(
                        new javafx.stage.FileChooser.ExtensionFilter("Excel", "*.xlsx"));
                    
                    // Carpeta por defecto: Exportaciones/NombreProyecto/
                    fileChooser.setInitialDirectory(
                        inventario.fx.config.PortablePaths.getExportProyectosDir(proyecto.getNombre()).toFile());
                    
                    File destino = fileChooser.showSaveDialog(adminStage);
                    if (destino != null) {
                        new Thread(() -> {
                            try {
                                Files.copy(rutaFinal, destino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                Platform.runLater(() -> mostrarMensaje("Descarga Completa",
                                    "\"" + nombreFinal + "\" guardado en:\n" + destino.getParent(), "#10B981"));
                            } catch (Exception ex) {
                                Platform.runLater(() -> mostrarMensaje("Error",
                                    "No se pudo guardar el archivo.\n" + ex.getMessage(), "#EF4444"));
                            }
                        }, "Admin-CopiarExcelDestino").start();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> mostrarMensaje("Error",
                    "Error al preparar descarga.\n" + e.getMessage(), "#EF4444"));
            }
        }, "Admin-DescargarExcel").start();
    }
    
    /**
     * Importa un archivo Excel directamente (usado por Drag & Drop)
     */
    private static void importarExcelDesdeArchivo(File archivo) {
        String nombreArchivo = archivo.getName();
        String nombreProyecto = nombreArchivo
            .replaceAll("(?i)\\.xlsx?$", "")
            .replaceAll("^\\d+[_\\s.-]+", "")
            .replaceAll("^Inventario_", "")
            .replace("_", " ")
            .trim();
        
        if (nombreProyecto.isEmpty()) {
            nombreProyecto = nombreArchivo.replaceAll("(?i)\\.xlsx?$", "");
        }
        
        AdminManager.Proyecto nuevoProyecto = new AdminManager.Proyecto();
        nuevoProyecto.setNombre(nombreProyecto);
        nuevoProyecto.setDescripcion("Importado por Drag & Drop");
        nuevoProyecto.setColor(AdminManager.getColorAleatorio());
        
        AdminManager.agregarProyecto(nuevoProyecto);
        
        mostrarMensaje("Importando...", "Procesando: " + nombreArchivo, "#3B82F6");
        
        final String nombreProyectoFinal = nombreProyecto;
        final AdminManager.Proyecto proyectoFinal = nuevoProyecto;
        
        new Thread(() -> {
            try {
                int indiceProyecto = AdminManager.getIndiceProyecto(proyectoFinal) + 1;
                String nombreLimpio = InventarioFXBase.sanitizarNombreArchivo(nombreProyectoFinal);
                String nombreDestino = "Inventario_" + indiceProyecto + " - " + nombreLimpio + ".xlsx";
                java.nio.file.Path destino = InventarioFXBase.obtenerCarpetaEjecutable().resolve(nombreDestino);
                
                String password = AdminManager.getExcelPassword();
                boolean ok = importarYCifrarExcel(archivo.toPath(), destino, password);
                
                Platform.runLater(() -> {
                    actualizarListaProyectos();
                    if (ok) {
                        mostrarMensaje("Importado", "\"" + nombreProyectoFinal + "\" importado y cifrado.", "#10B981");
                    } else {
                        mostrarMensaje("Error", "No se pudo cifrar el archivo importado.", "#EF4444");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> mostrarMensaje("Error", "Error al importar: " + e.getMessage(), "#EF4444"));
            }
        }, "Admin-VincularExcel").start();
    }
    
    // ==================== MÉTODOS AUXILIARES DE UI ====================
    
    /**
     * Crea un botón de cerrar rojo con icono Lucide, animación de escala y hover unificado.
     * Delega a ComponentesFX.crearBotonCerrar() para mantener estilo consistente.
     */
    private static Button crearBotonCerrarLucide(Stage stage) {
        return inventario.fx.util.ComponentesFX.crearBotonCerrar(stage::close, 40);
    }
    
    /**
     * Crea un campo de texto para diálogos
     */
    private static TextField crearTextField(String placeholder, String valor) {
        TextField field = new TextField(valor);
        field.setPromptText(placeholder);
        field.setStyle("-fx-background-color: " + TemaManager.getBg() + "; " +
                      "-fx-text-fill: " + TemaManager.getText() + "; " +
                      "-fx-prompt-text-fill: " + TemaManager.getTextMuted() + "; " +
                      "-fx-border-color: " + TemaManager.getBorder() + "; " +
                      "-fx-border-radius: 8; -fx-background-radius: 8; " +
                      "-fx-padding: 10 12; -fx-font-size: 13px;");
        field.setPrefHeight(40);
        return field;
    }
    
    /**
     * Crea un campo de contraseña para diálogos
     */
    private static PasswordField crearPasswordField(String placeholder) {
        PasswordField field = new PasswordField();
        field.setPromptText(placeholder);
        field.setStyle("-fx-background-color: " + TemaManager.getBg() + "; " +
                      "-fx-text-fill: " + TemaManager.getText() + "; " +
                      "-fx-prompt-text-fill: " + TemaManager.getTextMuted() + "; " +
                      "-fx-border-color: " + TemaManager.getBorder() + "; " +
                      "-fx-border-radius: 8; -fx-background-radius: 8; " +
                      "-fx-padding: 10 12; -fx-font-size: 13px;");
        field.setPrefHeight(40);
        return field;
    }
    
    /**
     * Crea un campo de contraseña con botón de ojo para mostrar/ocultar
     */
    private static HBox crearCampoPasswordConOjo(String placeholder) {
        HBox container = new HBox(0);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setStyle("-fx-background-color: " + TemaManager.getBg() + "; " +
                          "-fx-border-color: " + TemaManager.getBorder() + "; " +
                          "-fx-border-radius: 10; -fx-background-radius: 10;");
        container.setPrefHeight(44);
        
        PasswordField passField = new PasswordField();
        passField.setPromptText(placeholder);
        passField.setStyle("-fx-background-color: transparent; " +
                          "-fx-text-fill: " + TemaManager.getText() + "; " +
                          "-fx-prompt-text-fill: " + TemaManager.getTextMuted() + "; " +
                          "-fx-border-color: transparent; " +
                          "-fx-padding: 10 12; -fx-font-size: 13px;");
        HBox.setHgrow(passField, Priority.ALWAYS);
        
        TextField txtField = new TextField();
        txtField.setPromptText(placeholder);
        txtField.setStyle("-fx-background-color: transparent; " +
                         "-fx-text-fill: " + TemaManager.getText() + "; " +
                         "-fx-prompt-text-fill: " + TemaManager.getTextMuted() + "; " +
                         "-fx-border-color: transparent; " +
                         "-fx-padding: 10 12; -fx-font-size: 13px;");
        txtField.setVisible(false);
        txtField.setManaged(false);
        HBox.setHgrow(txtField, Priority.ALWAYS);
        
        // Binding bidireccional
        txtField.textProperty().bindBidirectional(passField.textProperty());
        
        // Botón ojo (inicia con ojoCerrado porque la contraseña está oculta)
        Button btnOjo = new Button();
        StackPane ojoIcon = new StackPane(IconosSVG.ojoCerrado("#64748B", 18));
        btnOjo.setGraphic(ojoIcon);
        btnOjo.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 8 12;");
        btnOjo.setOnAction(ev -> {
            boolean mostrarTexto = !txtField.isVisible();
            passField.setVisible(!mostrarTexto);
            passField.setManaged(!mostrarTexto);
            txtField.setVisible(mostrarTexto);
            txtField.setManaged(mostrarTexto);
            // Cambiar icono según estado: ojo abierto cuando se muestra, cerrado cuando se oculta
            ojoIcon.getChildren().clear();
            if (mostrarTexto) {
                ojoIcon.getChildren().add(IconosSVG.ojo("#10B981", 18));
            } else {
                ojoIcon.getChildren().add(IconosSVG.ojoCerrado("#64748B", 18));
            }
        });
        btnOjo.setOnMouseEntered(ev -> btnOjo.setStyle("-fx-background-color: " + TemaManager.getBorder() + "; -fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 8;"));
        btnOjo.setOnMouseExited(ev -> btnOjo.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 8 12;"));
        
        container.getChildren().addAll(passField, txtField, btnOjo);
        
        // Guardar referencia al PasswordField en el userData para poder acceder al texto
        container.setUserData(passField);
        
        // Efecto hover en el container
        container.setOnMouseEntered(ev -> container.setStyle("-fx-background-color: " + TemaManager.getBg() + "; " +
                          "-fx-border-color: #3B82F6; " +
                          "-fx-border-radius: 10; -fx-background-radius: 10;"));
        container.setOnMouseExited(ev -> container.setStyle("-fx-background-color: " + TemaManager.getBg() + "; " +
                          "-fx-border-color: " + TemaManager.getBorder() + "; " +
                          "-fx-border-radius: 10; -fx-background-radius: 10;"));
        
        return container;
    }
    
    /** Obtiene el texto de un campo de password con ojo */
    private static String getPasswordText(HBox campoConOjo) {
        PasswordField pf = (PasswordField) campoConOjo.getUserData();
        return pf != null ? pf.getText() : "";
    }
    
    
    private static Button crearBotonDialogo(String texto, String color, boolean isPrimary) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        btn.setPrefHeight(38);
        btn.setMinWidth(100);
        btn.setCursor(Cursor.HAND);
        
        if (isPrimary) {
            String baseStyle = "-fx-background-color: linear-gradient(to bottom, derive(" + color + ", 6%), " + color + "); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 8 22; " +
                        "-fx-effect: dropshadow(gaussian, " + color + "50, 8, 0, 0, 2);";
            btn.setStyle(baseStyle);
            btn.setOnMouseEntered(e -> btn.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, derive(" + color + ", -2%), derive(" + color + ", -10%)); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 8 22; " +
                        "-fx-translate-y: -1; " +
                        "-fx-effect: dropshadow(gaussian, " + color + "60, 12, 0, 0, 4);"));
            btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        } else {
            String normalStyle = "-fx-background-color: transparent; " +
                        "-fx-text-fill: " + color + "; " +
                        "-fx-border-color: " + color + "; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-width: 1.5; " +
                        "-fx-padding: 8 22;";
            btn.setStyle(normalStyle);
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + color + "; " +
                                                    "-fx-text-fill: white; " +
                                                    "-fx-border-radius: 10; " +
                                                    "-fx-background-radius: 10; " +
                                                    "-fx-padding: 8 22; " +
                                                    "-fx-translate-y: -1;"));
            btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        }
        
        return btn;
    }
    
    
    /**
     * Helper para crear celda con estilo
     */
    private static void crearCelda(org.apache.poi.ss.usermodel.Row row, int col, String valor, 
                                   org.apache.poi.ss.usermodel.CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(col);
        cell.setCellValue(valor != null ? valor : "");
        cell.setCellStyle(style);
    }
    
    /**
     * Importa inventarios desde un archivo Excel cifrado a la base de datos SQLite
     * usando batch insert para mayor eficiencia
     * 
     * @param excelPath Ruta al archivo Excel cifrado
     * @param proyectoId ID del proyecto al que pertenecen los inventarios
     * @param password Contraseña para descifrar el Excel
     * @return Número de equipos importados exitosamente
     */
    private static int importarInventariosDesdeExcel(java.nio.file.Path excelPath, 
                                                      String proyectoId, String password) {
        int equiposImportados = 0;
        
        try (java.io.FileInputStream fis = new java.io.FileInputStream(excelPath.toFile());
             org.apache.poi.poifs.filesystem.POIFSFileSystem poifs = 
                 new org.apache.poi.poifs.filesystem.POIFSFileSystem(fis)) {
            
            // Descifrar Excel
            org.apache.poi.poifs.crypt.EncryptionInfo info = 
                new org.apache.poi.poifs.crypt.EncryptionInfo(poifs);
            org.apache.poi.poifs.crypt.Decryptor decryptor = 
                org.apache.poi.poifs.crypt.Decryptor.getInstance(info);
            
            if (!decryptor.verifyPassword(password)) {
                System.err.println("    ✗ Contraseña incorrecta para Excel");
                return 0;
            }
            
            // Abrir workbook descifrado
            try (java.io.InputStream dataStream = decryptor.getDataStream(poifs);
                 org.apache.poi.xssf.usermodel.XSSFWorkbook wb = 
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook(dataStream)) {
                
                // Buscar hoja SystemInfo
                org.apache.poi.ss.usermodel.Sheet hoja = wb.getSheet("SystemInfo");
                if (hoja == null) {
                    System.err.println("    ✗ No se encontró hoja 'SystemInfo' en Excel");
                    return 0;
                }
                
                int filaInicio = 1; // Saltar encabezados
                int filaFin = hoja.getLastRowNum();
                int totalFilas = filaFin - filaInicio + 1;
                
                System.out.println("    → Filas a importar: " + totalFilas);
                System.out.flush();
                
                if (totalFilas <= 0) {
                    System.out.println("    ⚠ No hay datos para importar");
                    return 0;
                }
                
                // BATCH INSERT para mayor eficiencia
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO inventarios (proyecto_id, fecha, usuario, hostname, sistema, " +
                         "fabricante, modelo, serie, placa, procesador, tarjeta_grafica, " +
                         "memoria_ram, disco_duro, num_discos, ip, fecha_escaneo) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    
                    int batchCount = 0;
                    
                    for (int i = filaInicio; i <= filaFin; i++) {
                        org.apache.poi.ss.usermodel.Row fila = hoja.getRow(i);
                        if (fila == null) continue;
                        
                        // Leer columnas del Excel
                        String fecha = getCellValue(fila, 0);
                        String usuario = getCellValue(fila, 1);
                        String hostname = getCellValue(fila, 2);
                        String sistema = getCellValue(fila, 3);
                        String fabricante = getCellValue(fila, 4);
                        String modelo = getCellValue(fila, 5);
                        String serie = getCellValue(fila, 6);
                        String placa = getCellValue(fila, 7);
                        String procesador = getCellValue(fila, 8);
                        String tarjetaGrafica = getCellValue(fila, 9);
                        String ram = getCellValue(fila, 10);
                        String discoDuro = getCellValue(fila, 11);
                        String numDiscos = getCellValue(fila, 12);
                        String ip = getCellValue(fila, 13);
                        
                        // Validar que al menos tenga sistema o modelo
                        if ((sistema == null || sistema.trim().isEmpty()) && 
                            (modelo == null || modelo.trim().isEmpty())) {
                            continue; // Saltar filas vacías
                        }
                        
                        // Agregar al batch
                        stmt.setString(1, proyectoId);
                        stmt.setString(2, fecha != null ? fecha : "");
                        stmt.setString(3, usuario != null ? usuario : "");
                        stmt.setString(4, hostname != null ? hostname : "");
                        stmt.setString(5, sistema != null ? sistema : "");
                        stmt.setString(6, fabricante != null ? fabricante : "");
                        stmt.setString(7, modelo != null ? modelo : "");
                        stmt.setString(8, serie != null ? serie : "");
                        stmt.setString(9, placa != null ? placa : "");
                        stmt.setString(10, procesador != null ? procesador : "");
                        stmt.setString(11, tarjetaGrafica != null ? tarjetaGrafica : "");
                        stmt.setString(12, ram != null ? ram : "");
                        stmt.setString(13, discoDuro != null ? discoDuro : "");
                        stmt.setString(14, numDiscos != null ? numDiscos : "1");
                        stmt.setString(15, ip != null ? ip : "");
                        stmt.setString(16, java.time.LocalDateTime.now().toString());
                        
                        stmt.addBatch();
                        batchCount++;
                        
                        // Log del primer equipo
                        if (batchCount == 1) {
                            System.out.println("    → Primer equipo:");
                            System.out.println("        Sistema: " + sistema);
                            System.out.println("        Modelo: " + modelo);
                            System.out.println("        CPU: " + procesador);
                            System.out.flush();
                        }
                    }
                    
                    if (batchCount > 0) {
                        System.out.println("    → Ejecutando batch insert de " + batchCount + " equipos...");
                        System.out.flush();
                        
                        int[] results = stmt.executeBatch();
                        equiposImportados = results.length;
                        
                        System.out.println("    ✓ " + equiposImportados + " equipos insertados en SQLite");
                        System.out.flush();
                    }
                    
                } catch (SQLException e) {
                    AppLogger.getLogger(AdminPanelFX.class).error("Error SQL al insertar: " + e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            AppLogger.getLogger(AdminPanelFX.class).error("Error al importar desde Excel: " + e.getMessage(), e);
        }
        
        return equiposImportados;
    }
    
    /**
     * Helper para obtener valor de celda como String
     */
    private static String getCellValue(org.apache.poi.ss.usermodel.Row row, int columnIndex) {
        if (row == null) return "";
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";
        
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        return cell.getLocalDateTimeCellValue().toString();
                    } else {
                        return String.valueOf((long)cell.getNumericCellValue());
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    return cell.getCellFormula();
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Obtiene inventarios de un proyecto desde la base de datos
     */
    private static List<Map<String, String>> obtenerInventarioPorProyecto(String proyectoId) {
        List<Map<String, String>> inventarios = new ArrayList<>();
        
        System.out.println("\n  [DB] Consultando inventarios para proyecto: " + proyectoId);
        System.out.flush();
        
        try (Connection conn = DatabaseManager.getConnection()) {
            
            // PRIMERO: Verificar que la tabla inventarios existe
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name='inventarios'")) {
                
                if (!rs.next()) {
                    System.err.println("  [DB] ✗ ERROR: Tabla 'inventarios' no existe en la base de datos");
                    System.err.flush();
                    return inventarios;
                }
            }
            
            // SEGUNDO: Consultar los inventarios (SIN JSON - SOLO COLUMNAS)
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id, proyecto_id, fecha, usuario, hostname, sistema, fabricante, modelo, " +
                 "serie, placa, procesador, tarjeta_grafica, memoria_ram, disco_duro, num_discos, " +
                 "ip, fecha_escaneo FROM inventarios WHERE proyecto_id = ? ORDER BY fecha_escaneo DESC")) {
                
                stmt.setString(1, proyectoId);
                java.sql.ResultSet rs = stmt.executeQuery();
                
                int count = 0;
                while (rs.next()) {
                    Map<String, String> equipo = new HashMap<>();
                    equipo.put("id", rs.getString("id"));
                    equipo.put("proyecto_id", rs.getString("proyecto_id"));
                    equipo.put("fecha", rs.getString("fecha"));
                    equipo.put("usuario", rs.getString("usuario"));
                    equipo.put("hostname", rs.getString("hostname"));
                    equipo.put("sistema", rs.getString("sistema"));
                    equipo.put("fabricante", rs.getString("fabricante"));
                    equipo.put("modelo", rs.getString("modelo"));
                    equipo.put("serie", rs.getString("serie"));
                    equipo.put("placa", rs.getString("placa"));
                    equipo.put("procesador", rs.getString("procesador"));
                    equipo.put("tarjeta_grafica", rs.getString("tarjeta_grafica"));
                    equipo.put("memoria_ram", rs.getString("memoria_ram"));
                    equipo.put("disco_duro", rs.getString("disco_duro"));
                    equipo.put("num_discos", rs.getString("num_discos"));
                    equipo.put("ip", rs.getString("ip"));
                    equipo.put("fecha_escaneo", rs.getString("fecha_escaneo"));
                    inventarios.add(equipo);
                    count++;
                    
                    // Log del primer equipo para debugging
                    if (count == 1) {
                        System.out.println("  [DB] → Primer equipo leído:");
                        System.out.println("      Sistema: " + rs.getString("sistema"));
                        System.out.println("      Modelo: " + rs.getString("modelo"));
                        System.out.println("      CPU: " + rs.getString("procesador"));
                        System.out.println("      RAM: " + rs.getString("memoria_ram"));
                        System.out.println("      Usuario: " + rs.getString("usuario"));
                        System.out.println("      Hostname: " + rs.getString("hostname"));
                        System.out.flush();
                    }
                }
                
                System.out.println("  [DB] ✓ Registros obtenidos: " + count);
                System.out.flush();
            }
            
        } catch (Exception e) {
            AppLogger.getLogger(AdminPanelFX.class).error("[DB] ERROR: " + e.getMessage(), e);
        }
        
        return inventarios;
    }
    
    /**
     * Parsea el JSON de datos completos a un mapa simple
     */
    // ==================== SISTEMA: ACERCA DE ====================
    
    /**
     * Muestra informacion acerca del programa (delegado a AcercaDeDialogFX)
     */
    private static void mostrarAcercaDe() {
        AcercaDeDialogFX.mostrar(adminStage);
    }
}
