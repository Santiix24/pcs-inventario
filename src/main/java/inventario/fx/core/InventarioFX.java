package inventario.fx.core;
import inventario.fx.config.PortablePaths;
import inventario.fx.model.InventarioFXBase;
import inventario.fx.model.TemaManager;
import inventario.fx.model.AdminManager;
import inventario.fx.icons.IconosSVG;
import inventario.fx.ui.dialog.AcercaDeDialogFX;
import inventario.fx.ui.dialog.DialogosFX;
import inventario.fx.ui.panel.AdminPanelFX;
import inventario.fx.ui.panel.DashboardFX;

import inventario.fx.util.AppLogger;
import inventario.fx.util.SVGUtil;
import inventario.fx.util.ComponentesFX;
import inventario.fx.util.ScreenUtils;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import inventario.fx.util.AnimacionesFX;

/**
 * Interfaz principal ultra moderna y elegante.
 * Diseño premium con panel lateral deslizante.
 */
public class InventarioFX extends InventarioFXBase {

    // Colores del tema - Usando TemaManager para soporte light/dark
    public static Color COLOR_PRIMARY = Color.web(TemaManager.COLOR_PRIMARY);
    public static Color COLOR_SUCCESS = Color.web(TemaManager.COLOR_SUCCESS);
    public static Color COLOR_BG_DARK() { return TemaManager.getBgDarkColor(); }
    public static Color COLOR_BG() { return TemaManager.getBgColor(); }
    public static Color COLOR_BG_LIGHT() { return TemaManager.getBgLightColor(); }
    public static Color COLOR_SURFACE() { return TemaManager.getSurfaceColor(); }
    public static Color COLOR_BORDER() { return TemaManager.getBorderColor(); }
    public static Color COLOR_TEXT() { return TemaManager.getTextColor(); }
    public static Color COLOR_TEXT_MUTED() { return TemaManager.getTextMutedColor(); }

    private static Stage mainStage;
    private static Stage loadingStage;
    private static StackPane rootStack;
    private static VBox sidePanel;
    private static VBox projectsListContainer; // Referencia a la lista de proyectos del panel lateral
    private static boolean sidePanelOpen = false;
    private static String pendingAction = null;

    // ════════════════════════════════════════════════════════════════════════════
    // UTILIDAD: ÍCONO SELCOMP EN CUALQUIER STAGE
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Aplica el ícono oficial de SELCOMP al stage dado.
     * Debe llamarse ANTES de {@code stage.show()} para que el taskbar lo muestre.
     */
    public static void aplicarIconoApp(Stage stage) {
        if (stage == null) return;
        try {
            InputStream iconStream = InventarioFX.class.getResourceAsStream(RUTA_IMAGENES + ICONO);
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                stage.getIcons().clear();
                stage.getIcons().add(icon);
            }
        } catch (Exception ignored) {}
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SPLASH SCREEN MINIMALISTA PREMIUM
    // ════════════════════════════════════════════════════════════════════════════

    public static void mostrarSplash(Stage splashStage, Stage mainStage) {
        InventarioFX.mainStage = mainStage;
        splashStage.initStyle(StageStyle.TRANSPARENT);
        // Ícono SELCOMP en el splash (de lo contrario Windows muestra el café de Java)
        aplicarIconoApp(splashStage);
        aplicarIconoApp(mainStage);

        StackPane root = new StackPane();
        root.setAlignment(Pos.CENTER);
        
        String bgColor = TemaManager.getBgLight();
        
        root.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 0;"
        );

        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40, 50, 30, 50));

        // Logo SELCOMP
        ImageView logoImage = new ImageView();
        try {
            InputStream logoStream = InventarioFX.class.getResourceAsStream("/images/Selcomp.png");
            if (logoStream != null) {
                Image img = new Image(logoStream);
                logoImage.setImage(img);
                logoImage.setFitWidth(320);
                logoImage.setFitHeight(320);
                logoImage.setPreserveRatio(true);
                logoImage.setSmooth(true);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar Selcomp.png: " + e.getMessage());
            StackPane fallback = new StackPane();
            Circle circle = new Circle(160, Color.web("#dc3545"));
            Label text = new Label("SELCOMP");
            text.setFont(Font.font("Segoe UI", FontWeight.BOLD, 52));
            text.setTextFill(Color.WHITE);
            fallback.getChildren().addAll(circle, text);
            container.getChildren().add(fallback);
        }

        // Barra de progreso
        ProgressBar progress = new ProgressBar(0);
        progress.setPrefWidth(320);
        progress.setMinHeight(10);
        progress.setMaxHeight(10);
        
        String progressBgColor = TemaManager.getSurface();
        progress.setStyle(
            "-fx-accent: " + TemaManager.COLOR_PRIMARY + ";" +
            "-fx-control-inner-background: " + progressBgColor + ";" +
            "-fx-background-radius: 5;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 0;" +
            "-fx-pref-height: 10px; -fx-min-height: 10px; -fx-max-height: 10px;" +
            "-fx-background-color: " + progressBgColor + ";" +
            "-fx-border-width: 0; -fx-border-insets: 0;"
        );

        // Etiqueta de estado de carga
        Label lblEstado = new Label("Iniciando...");
        lblEstado.setFont(Font.font("Segoe UI", 11));
        lblEstado.setTextFill(TemaManager.getTextMutedColor());
        lblEstado.setOpacity(0);

        container.getChildren().addAll(logoImage, progress, lblEstado);

        // Sombra
        DropShadow shadow = new DropShadow();
        shadow.setRadius(40);
        shadow.setSpread(0.1);
        shadow.setColor(Color.rgb(0, 0, 0, TemaManager.isDarkMode() ? 0.5 : 0.15));
        shadow.setOffsetY(10);
        root.setEffect(shadow);

        root.getChildren().add(container);

        StackPane wrapperSplash = new StackPane(root);
        wrapperSplash.setStyle("-fx-background-color: transparent;");
        wrapperSplash.setPadding(new Insets(20));

        Scene scene = new Scene(wrapperSplash, 520, 420);
        scene.setFill(Color.TRANSPARENT);
        splashStage.setScene(scene);
        splashStage.centerOnScreen();

        // Estados iniciales
        root.setOpacity(0);
        logoImage.setScaleX(0.5);
        logoImage.setScaleY(0.5);
        progress.setOpacity(0);
        
        splashStage.show();

        // Animaciones de entrada
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);

        ScaleTransition zoomPop = new ScaleTransition(Duration.millis(500), logoImage);
        zoomPop.setFromX(0.5);
        zoomPop.setFromY(0.5);
        zoomPop.setToX(1.0);
        zoomPop.setToY(1.0);
        zoomPop.setInterpolator(new Interpolator() {
            @Override
            protected double curve(double t) {
                return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
            }
        });

        FadeTransition progressFadeIn = new FadeTransition(Duration.millis(200), progress);
        progressFadeIn.setFromValue(0);
        progressFadeIn.setToValue(1);
        progressFadeIn.setDelay(Duration.millis(250));

        FadeTransition estadoFadeIn = new FadeTransition(Duration.millis(200), lblEstado);
        estadoFadeIn.setFromValue(0);
        estadoFadeIn.setToValue(1);
        estadoFadeIn.setDelay(Duration.millis(300));

        ParallelTransition entryAnim = new ParallelTransition(fadeIn, zoomPop, progressFadeIn, estadoFadeIn);
        entryAnim.play();

        // Inicialización real en hilo de fondo
        new Thread(() -> {
            try {
                // Paso 1: Verificar configuración (20%)
                actualizarProgreso(progress, lblEstado, 0.0, "Verificando configuraci\u00f3n...");
                Thread.sleep(200);
                try {
                    inventario.fx.config.ConfigManager.getInstance();
                } catch (Exception ignored) { AppLogger.getLogger(InventarioFX.class).warn("Error inicializando configuración: " + ignored.getMessage()); }
                actualizarProgreso(progress, lblEstado, 0.20, "Configuraci\u00f3n cargada");

                // Paso 2: Inicializar base de datos (45%)
                actualizarProgreso(progress, lblEstado, 0.25, "Conectando base de datos...");
                Thread.sleep(150);
                try {
                    inventario.fx.database.DatabaseManager.getConnection().close();
                } catch (Exception ignored) { AppLogger.getLogger(InventarioFX.class).warn("Error conectando a base de datos: " + ignored.getMessage()); }
                actualizarProgreso(progress, lblEstado, 0.45, "Base de datos lista");

                // Paso 3: Cargar proyectos (65%)
                actualizarProgreso(progress, lblEstado, 0.50, "Cargando proyectos...");
                Thread.sleep(150);
                try {
                    AdminManager.getProyectosActivos();
                } catch (Exception ignored) { AppLogger.getLogger(InventarioFX.class).warn("Error pre-cargando proyectos: " + ignored.getMessage()); }
                actualizarProgreso(progress, lblEstado, 0.65, "Proyectos cargados");

                // Paso 4: Preparar interfaz (85%)
                actualizarProgreso(progress, lblEstado, 0.70, "Preparando interfaz...");
                Thread.sleep(200);
                actualizarProgreso(progress, lblEstado, 0.85, "Interfaz lista");

                // Paso 5: Listo (100%)
                Thread.sleep(150);
                actualizarProgreso(progress, lblEstado, 1.0, "Listo");
                Thread.sleep(300);

            } catch (Exception e) {
                System.err.println("Error en inicializacion: " + e.getMessage());
                actualizarProgreso(progress, lblEstado, 1.0, "Iniciando...");
            }

            // Transición a menú principal
            Platform.runLater(() -> {
                mostrarMenu(mainStage);
                
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
                
                ScaleTransition scaleOut = new ScaleTransition(Duration.millis(500), container);
                scaleOut.setToX(0.95);
                scaleOut.setToY(0.95);
                scaleOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
                
                ParallelTransition ghostExit = new ParallelTransition(fadeOut, scaleOut);
                ghostExit.setOnFinished(ev -> splashStage.close());
                ghostExit.play();
            });
        }, "App-Inicializacion").start();
    }

    /**
     * Actualiza la barra de progreso y el texto de estado desde cualquier hilo.
     */
    private static void actualizarProgreso(ProgressBar progress, Label lblEstado, double valor, String texto) {
        Platform.runLater(() -> {
            // Animar suavemente el progreso
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200), 
                    new KeyValue(progress.progressProperty(), valor, Interpolator.EASE_BOTH))
            );
            timeline.play();
            lblEstado.setText(texto);
        });
    }

    // ════════════════════════════════════════════════════════════════════════════
    // MENÚ PRINCIPAL ULTRA MODERNO
    // ════════════════════════════════════════════════════════════════════════════

    public static void mostrarMenu(Stage stage) {
        mainStage = stage;
        stage.setTitle("SELCOMP — Inventario");

        // Root con stack para panel lateral
        rootStack = new StackPane();
        rootStack.setStyle("-fx-background-color: " + TemaManager.getBgDark() + ";");

        // Contenido principal
        BorderPane mainContent = new BorderPane();
        mainContent.setStyle("-fx-background-color: " + TemaManager.getBgDark() + ";");

        // === SIDEBAR IZQUIERDO ===
        VBox sidebar = crearSidebarMenu();
        mainContent.setLeft(sidebar);

        // === CONTENIDO CENTRAL ===
        VBox centerContent = crearContenidoCentral();
        mainContent.setCenter(centerContent);

        rootStack.getChildren().add(mainContent);
        
        // Toggle de tema en esquina superior derecha
        StackPane toggleTema = crearToggleTema();
        StackPane.setAlignment(toggleTema, Pos.TOP_RIGHT);
        StackPane.setMargin(toggleTema, new Insets(12, 16, 0, 0));
        rootStack.getChildren().add(toggleTema);

        // Crear panel lateral de proyectos (inicialmente oculto)
        crearPanelLateralProyectos();
        
        // Registrar listener para actualizar automáticamente cuando cambien los proyectos
        AdminManager.addListener(new AdminManager.AdminListener() {
            @Override
            public void onAdminModeChanged(boolean isAdmin) {
                // Actualizar botón admin en sidebar cuando cambia el estado
                Platform.runLater(() -> {
                    BorderPane bp = (BorderPane) rootStack.getChildren().get(0);
                    if (bp != null) {
                        bp.setLeft(crearSidebarMenu());
                    }
                });
            }
            
            @Override
            public void onProjectsChanged() {
                Platform.runLater(() -> actualizarListaProyectosPanelLateral());
            }
        });

        Scene scene = new Scene(rootStack,
                ScreenUtils.w(800),
                ScreenUtils.h(600));
        // Aplicar tema usando TemaManager
        TemaManager.aplicarTema(scene);
        TemaManager.registrarEscena(scene);

        // Atajos de teclado globales del menú principal
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.isControlDown()) {
                switch (e.getCode()) {
                    case G: abrirPanelConAccion("generar"); e.consume(); break;
                    case D: abrirPanelConAccion("dashboard"); e.consume(); break;
                    case T: TemaManager.toggleTheme(); e.consume(); break;
                    default: break;
                }
                if (e.isShiftDown() && e.getCode() == KeyCode.A) {
                    mostrarPanelAdministracion(); e.consume();
                }
            } else if (e.getCode() == KeyCode.F1) {
                AcercaDeDialogFX.mostrar(mainStage); e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cerrarPanelLateral(); e.consume();
            }
        });

        // Cargar icono usando el helper centralizado
        aplicarIconoApp(stage);

        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.centerOnScreen();
        stage.show();

        // Animación de entrada premium: sidebar slide + contenido fade/slide
        BorderPane bp = (BorderPane) rootStack.getChildren().get(0);
        javafx.scene.Node sidebarNode = bp.getLeft();
        javafx.scene.Node centerNode = bp.getCenter();
        
        // Sidebar: desliza desde la izquierda
        if (sidebarNode != null) {
            sidebarNode.setTranslateX(-80);
            sidebarNode.setOpacity(0);
            TranslateTransition sideSlide = new TranslateTransition(Duration.millis(500), sidebarNode);
            sideSlide.setToX(0);
            sideSlide.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            FadeTransition sideFade = new FadeTransition(Duration.millis(400), sidebarNode);
            sideFade.setToValue(1);
            sideFade.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            new ParallelTransition(sideSlide, sideFade).play();
        }
        
        // Contenido central: slide up + fade
        if (centerNode != null) {
            centerNode.setTranslateY(30);
            centerNode.setOpacity(0);
            TranslateTransition centerSlide = new TranslateTransition(Duration.millis(600), centerNode);
            centerSlide.setToY(0);
            centerSlide.setDelay(Duration.millis(150));
            centerSlide.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            FadeTransition centerFade = new FadeTransition(Duration.millis(500), centerNode);
            centerFade.setToValue(1);
            centerFade.setDelay(Duration.millis(150));
            centerFade.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            new ParallelTransition(centerSlide, centerFade).play();
        }
        
        // Toggle tema: fade in con delay
        if (rootStack.getChildren().size() > 1) {
            javafx.scene.Node toggle = rootStack.getChildren().get(rootStack.getChildren().size() - 1);
            AnimacionesFX.fadeIn(toggle, 400, 400);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SIDEBAR DEL MENÚ
    // ════════════════════════════════════════════════════════════════════════════

    private static VBox crearSidebarMenu() {
        VBox sidebar = ComponentesFX.crearSidebar(220);

        // Logo header
        VBox logoSection = ComponentesFX.crearLogoSection(TemaManager.COLOR_PRIMARY);

        // Navegación
        Button btnGenerar = ComponentesFX.crearSidebarButton("Generar Inventario", IconosSVG.estadisticas("#E63946", 20), true);
        Button btnDashboard = ComponentesFX.crearSidebarButton("Ver Dashboard", IconosSVG.graficoLinea("#888888", 20), false);
        Button btnLimpiar = ComponentesFX.crearSidebarButton("Limpiar Datos", IconosSVG.eliminar("#888888", 20), false);

        // Tooltips con atajos de teclado
        btnGenerar.setTooltip(ComponentesFX.crearTooltip("Escanear y registrar equipo · Ctrl+G"));
        btnDashboard.setTooltip(ComponentesFX.crearTooltip("Ver datos del inventario · Ctrl+D"));
        btnLimpiar.setTooltip(ComponentesFX.crearTooltip("Eliminar datos del proyecto actual"));

        // Eventos - abren el panel lateral primero
        btnGenerar.setOnAction(e -> abrirPanelConAccion("generar"));
        btnDashboard.setOnAction(e -> abrirPanelConAccion("dashboard"));
        btnLimpiar.setOnAction(e -> abrirPanelConAccion("limpiar"));

        VBox navSection = ComponentesFX.crearNavSection("ACCIONES", btnGenerar, btnDashboard, btnLimpiar);

        // Espaciador
        Region spacer = ComponentesFX.crearSpacer();

        // Sección inferior
        Button btnAdmin = ComponentesFX.crearSidebarButton(
            AdminManager.isAdminMode() ? "⚙ Admin ON" : "Administrar",
            IconosSVG.admin(AdminManager.isAdminMode() ? "#E63946" : "#888888", 20),
            AdminManager.isAdminMode()
        );
        btnAdmin.setTooltip(ComponentesFX.crearTooltip("Panel de administración · Ctrl+Shift+A"));
        btnAdmin.setOnAction(e -> mostrarPanelAdministracion());

        Button btnSalir = ComponentesFX.crearSidebarButton("Salir", IconosSVG.salir("#888888", 20), false);
        btnSalir.setTooltip(ComponentesFX.crearTooltip("Cerrar la aplicación"));
        btnSalir.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        Label version = ComponentesFX.crearVersionLabel("v1.0 © 2026");

        VBox bottomSection = ComponentesFX.crearBottomSection(btnAdmin, btnSalir, version);

        sidebar.getChildren().addAll(logoSection, navSection, spacer, bottomSection);
        return sidebar;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CONTENIDO CENTRAL
    // ════════════════════════════════════════════════════════════════════════════

    private static VBox crearContenidoCentral() {
        VBox center = new VBox(0);
        center.setAlignment(Pos.CENTER);
        center.setStyle("-fx-background-color: " + TemaManager.getBgDark() + ";");
        VBox.setVgrow(center, Priority.ALWAYS);

        // Hero section
        VBox heroSection = new VBox(28);
        heroSection.setAlignment(Pos.CENTER);
        heroSection.setPadding(new Insets(40, 50, 50, 50));
        VBox.setVgrow(heroSection, Priority.ALWAYS);

        // ═══════════════════════════════════════════════════════════════════
        // ICONO HERO INTERACTIVO
        // ═══════════════════════════════════════════════════════════════════
        StackPane iconContainer = crearIconoHeroInteractivo();

        // Contenedor de texto
        VBox textContainer = new VBox(10);
        textContainer.setAlignment(Pos.CENTER);
        
        Label heroTitle = new Label("Sistema de Inventario");
        heroTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        heroTitle.setTextFill(COLOR_TEXT());

        Label heroSubtitle = new Label("Gestiona el inventario de equipos de forma rápida y eficiente");
        heroSubtitle.setFont(Font.font("Segoe UI", 13));
        heroSubtitle.setTextFill(COLOR_TEXT_MUTED());
        heroSubtitle.setWrapText(true);
        heroSubtitle.setPrefWidth(300);           // fuerza wrapping real (single-line natural ≈ 420px)
        heroSubtitle.setMaxWidth(Double.MAX_VALUE); // deja que el layout administre el ancho
        heroSubtitle.setAlignment(Pos.CENTER);
        heroSubtitle.setStyle("-fx-text-alignment: center;");
        
        textContainer.getChildren().addAll(heroTitle, heroSubtitle);

        // Tarjetas de estadísticas con efecto flip
        HBox cardsRow = new HBox(14);
        cardsRow.setAlignment(Pos.CENTER);
        cardsRow.setPadding(new Insets(16, 0, 0, 0));

        // Card 1: Proyectos - muestra cantidad en tiempo real
        StackPane card1 = crearFlipCard(
            "Proyectos", 
            String.valueOf(AdminManager.getProyectosActivos().size()), 
            IconosSVG.carpeta("#3B82F6", 26), 
            "#3B82F6",
            "Total activos",
            "Proyectos con datos\nde inventario listos",
            () -> String.valueOf(AdminManager.getProyectosActivos().size())
        );
        
        // Card 2: Rápido - muestra tiempo promedio
        StackPane card2 = crearFlipCard(
            "Rápido", 
            "< 30s", 
            IconosSVG.rayo("#F59E0B", 26), 
            "#F59E0B",
            "Escaneo veloz",
            "Recopila información\ndel sistema en segundos",
            null
        );
        
        // Card 3: Seguro - cifrado
        StackPane card3 = crearFlipCard(
            "Seguro", 
            "Cifrado", 
            IconosSVG.escudo("#10B981", 26), 
            "#10B981",
            "AES-256 + SHA",
            "Datos protegidos\ncon encriptación militar",
            null
        );

        cardsRow.getChildren().addAll(card1, card2, card3);

        // Indicador de acción — siempre visible en el home
        boolean sinProyectos = AdminManager.getProyectosActivos().isEmpty();
        HBox instructionBox = new HBox(10);
        instructionBox.setAlignment(Pos.CENTER);
        instructionBox.setPadding(new Insets(24, 20, 0, 20));
        instructionBox.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 25;" +
            "-fx-padding: 10 20 10 20;"
        );
        instructionBox.setMaxWidth(340);

        Label arrow = new Label("←");
        arrow.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        arrow.setTextFill(COLOR_PRIMARY);

        TranslateTransition arrowAnim = new TranslateTransition(Duration.millis(800), arrow);
        arrowAnim.setFromX(0);
        arrowAnim.setToX(-5);
        arrowAnim.setCycleCount(Animation.INDEFINITE);
        arrowAnim.setAutoReverse(true);
        arrowAnim.setInterpolator(Interpolator.EASE_BOTH);
        arrowAnim.play();

        Label instruction = new Label(sinProyectos
            ? "Ve a Administrar para crear tu primer proyecto"
            : "Selecciona una acción del menú lateral");
        instruction.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        instruction.setTextFill(COLOR_TEXT_MUTED());

        instructionBox.getChildren().addAll(arrow, instruction);
        javafx.scene.Node instructionNode = instructionBox;

        heroSection.getChildren().addAll(iconContainer, textContainer, cardsRow, instructionNode);
        
        // Animación escalonada de entrada para los elementos del hero
        AnimacionesFX.slideUpFadeIn(iconContainer, 500, 20, 0);
        AnimacionesFX.slideUpFadeIn(textContainer, 500, 20, 100);
        AnimacionesFX.slideUpFadeIn(cardsRow, 500, 20, 200);
        AnimacionesFX.slideUpFadeIn(instructionNode, 400, 15, 350);
        
        center.getChildren().add(heroSection);

        return center;
    }
    
    /**
     * Crea una tarjeta con efecto flip 3D que muestra información detallada al pasar el cursor.
     * La cara frontal muestra icono y valor, la trasera muestra información detallada.
     */
    private static StackPane crearFlipCard(String titulo, String valor, javafx.scene.Node icono, 
            String accentColor, String tituloBack, String descripcionBack, 
            java.util.function.Supplier<String> valorDinamico) {
        
        final double CARD_WIDTH = 115;
        final double CARD_HEIGHT = 105;
        
        // Contenedor principal con perspectiva
        StackPane cardContainer = new StackPane();
        cardContainer.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        cardContainer.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        cardContainer.setCursor(javafx.scene.Cursor.HAND);
        
        // ═══════════════════════════════════════════════════════════════════
        // CARA FRONTAL - Icono, valor y título
        // ═══════════════════════════════════════════════════════════════════
        VBox frontFace = new VBox(4);
        frontFace.setAlignment(Pos.CENTER);
        frontFace.setPadding(new Insets(14, 12, 14, 12));
        frontFace.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        frontFace.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        frontFace.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 1.5;"
        );
        
        // Efecto de brillo sutil
        DropShadow frontShadow = new DropShadow();
        frontShadow.setRadius(8);
        frontShadow.setColor(Color.web(accentColor + "30"));
        frontShadow.setOffsetY(3);
        frontFace.setEffect(frontShadow);
        
        // Contenedor del icono con fondo circular
        StackPane iconBg = new StackPane();
        Circle iconCircle = new Circle(22);
        iconCircle.setFill(Color.web(accentColor + "18"));
        iconBg.getChildren().addAll(iconCircle, icono);
        
        // Valor (número o texto principal)
        Label valorLabel = new Label(valor);
        valorLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        valorLabel.setTextFill(Color.web(accentColor));
        
        // Título
        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 10));
        tituloLabel.setTextFill(COLOR_TEXT_MUTED());
        
        frontFace.getChildren().addAll(iconBg, valorLabel, tituloLabel);
        
        // ═══════════════════════════════════════════════════════════════════
        // CARA TRASERA - Información detallada
        // ═══════════════════════════════════════════════════════════════════
        VBox backFace = new VBox(6);
        backFace.setAlignment(Pos.CENTER);
        backFace.setPadding(new Insets(12, 10, 12, 10));
        backFace.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        backFace.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        backFace.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + accentColor + ", derive(" + accentColor + ", -20%));" +
            "-fx-background-radius: 18;"
        );
        
        // Sombra para la parte trasera
        DropShadow backShadow = new DropShadow();
        backShadow.setRadius(12);
        backShadow.setColor(Color.web(accentColor + "60"));
        backShadow.setOffsetY(4);
        backFace.setEffect(backShadow);
        
        // Icono pequeño en la esquina
        Label iconoBack = new Label("✦");
        iconoBack.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        iconoBack.setTextFill(Color.web("#FFFFFF90"));
        
        // Título de la cara trasera
        Label tituloBackLabel = new Label(tituloBack);
        tituloBackLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        tituloBackLabel.setTextFill(Color.WHITE);
        tituloBackLabel.setWrapText(true);
        tituloBackLabel.setAlignment(Pos.CENTER);
        tituloBackLabel.setStyle("-fx-text-alignment: center;");
        
        // Separador sutil
        Region separator = new Region();
        separator.setPrefSize(40, 1);
        separator.setMaxSize(40, 1);
        separator.setStyle("-fx-background-color: #FFFFFF50; -fx-background-radius: 1;");
        
        // Descripción
        Label descripcionLabel = new Label(descripcionBack);
        descripcionLabel.setFont(Font.font("Segoe UI", 9));
        descripcionLabel.setTextFill(Color.web("#FFFFFFCC"));
        descripcionLabel.setWrapText(true);
        descripcionLabel.setAlignment(Pos.CENTER);
        descripcionLabel.setStyle("-fx-text-alignment: center;");
        descripcionLabel.setMaxWidth(CARD_WIDTH - 16);
        
        backFace.getChildren().addAll(iconoBack, tituloBackLabel, separator, descripcionLabel);
        
        // Inicialmente la cara trasera está oculta (rotada 180°)
        backFace.setScaleX(-1); // Espejado para que se vea correctamente al voltear
        backFace.setOpacity(0);
        backFace.setVisible(false);
        
        cardContainer.getChildren().addAll(backFace, frontFace);
        
        // ═══════════════════════════════════════════════════════════════════
        // ANIMACIÓN DE FLIP 3D
        // ═══════════════════════════════════════════════════════════════════
        final boolean[] isFlipped = {false};
        final Timeline flipToBack = new Timeline();
        final Timeline flipToFront = new Timeline();
        
        // Animación para voltear hacia atrás (mostrar back)
        flipToBack.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO,
                new KeyValue(frontFace.scaleXProperty(), 1),
                new KeyValue(frontFace.opacityProperty(), 1),
                new KeyValue(backFace.scaleXProperty(), -1),
                new KeyValue(backFace.opacityProperty(), 0)
            ),
            new KeyFrame(Duration.millis(150),
                new KeyValue(frontFace.scaleXProperty(), 0, AnimacionesFX.EASE_IN_CUBIC),
                new KeyValue(frontFace.opacityProperty(), 0.5)
            ),
            new KeyFrame(Duration.millis(151),
                e -> {
                    frontFace.setVisible(false);
                    backFace.setVisible(true);
                }
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(backFace.scaleXProperty(), 1, AnimacionesFX.EASE_OUT_CUBIC),
                new KeyValue(backFace.opacityProperty(), 1)
            )
        );
        
        // Animación para voltear hacia adelante (mostrar front)
        flipToFront.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO,
                new KeyValue(backFace.scaleXProperty(), 1),
                new KeyValue(backFace.opacityProperty(), 1),
                new KeyValue(frontFace.scaleXProperty(), 0),
                new KeyValue(frontFace.opacityProperty(), 0.5)
            ),
            new KeyFrame(Duration.millis(150),
                new KeyValue(backFace.scaleXProperty(), -1, AnimacionesFX.EASE_IN_CUBIC),
                new KeyValue(backFace.opacityProperty(), 0)
            ),
            new KeyFrame(Duration.millis(151),
                e -> {
                    backFace.setVisible(false);
                    frontFace.setVisible(true);
                }
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(frontFace.scaleXProperty(), 1, AnimacionesFX.EASE_OUT_CUBIC),
                new KeyValue(frontFace.opacityProperty(), 1)
            )
        );
        
        // Eventos de mouse
        cardContainer.setOnMouseEntered(e -> {
            if (!isFlipped[0]) {
                isFlipped[0] = true;
                flipToFront.stop();
                flipToBack.playFromStart();
                
                // Efecto de elevación
                ScaleTransition lift = new ScaleTransition(Duration.millis(200), cardContainer);
                lift.setToX(1.08);
                lift.setToY(1.08);
                lift.play();
            }
        });
        
        cardContainer.setOnMouseExited(e -> {
            if (isFlipped[0]) {
                isFlipped[0] = false;
                flipToBack.stop();
                flipToFront.playFromStart();
                
                // Volver al tamaño normal
                ScaleTransition lower = new ScaleTransition(Duration.millis(200), cardContainer);
                lower.setToX(1.0);
                lower.setToY(1.0);
                lower.play();
            }
        });
        
        // Actualización en tiempo real si hay un proveedor de valor dinámico
        if (valorDinamico != null) {
            Timeline updateTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> { // 5s para reducir carga de CPU vs 2s anterior
                    String nuevoValor = valorDinamico.get();
                    if (!valorLabel.getText().equals(nuevoValor)) {
                        // Animación de cambio de valor
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), valorLabel);
                        fadeOut.setToValue(0.3);
                        fadeOut.setOnFinished(ev -> {
                            valorLabel.setText(nuevoValor);
                            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), valorLabel);
                            fadeIn.setToValue(1);
                            fadeIn.play();
                        });
                        fadeOut.play();
                    }
                })
            );
            updateTimeline.setCycleCount(Animation.INDEFINITE);
            updateTimeline.play();
        }
        
        return cardContainer;
    }
    
    // Método legacy para compatibilidad (ya no se usa en el hero)
    private static VBox crearMiniCardMejorada(String titulo, String valor, javafx.scene.Node icono, String accentColor) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setPrefWidth(105);
        card.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-cursor: hand;"
        );
        
        // Efecto hover
        String normalStyle = card.getStyle();
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: " + TemaManager.getBgLight() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + accentColor + "40;" +
            "-fx-border-radius: 16;" +
            "-fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(normalStyle));

        // Contenedor del icono
        StackPane iconContainer = new StackPane(icono);
        iconContainer.setPadding(new Insets(0, 0, 4, 0));

        Label valorLabel = new Label(valor);
        valorLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        valorLabel.setTextFill(COLOR_TEXT());

        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Segoe UI", 10));
        tituloLabel.setTextFill(COLOR_TEXT_MUTED());

        card.getChildren().addAll(iconContainer, valorLabel, tituloLabel);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // ICONO HERO INTERACTIVO CON ANIMACIONES
    // ════════════════════════════════════════════════════════════════════════════
    
    private static StackPane crearIconoHeroInteractivo() {
        StackPane container = new StackPane();
        container.setPrefSize(220, 220);
        container.setMaxSize(220, 220);
        container.setCursor(javafx.scene.Cursor.HAND);
        
        // ═══ ANILLOS ORBITALES ═══
        // Anillo exterior (más tenue)
        Circle ring3 = new Circle(100);
        ring3.setFill(Color.TRANSPARENT);
        ring3.setStroke(Color.web("#E6394618"));
        ring3.setStrokeWidth(1);
        ring3.getStrokeDashArray().addAll(8.0, 6.0);
        
        // Anillo medio
        Circle ring2 = new Circle(78);
        ring2.setFill(Color.TRANSPARENT);
        ring2.setStroke(Color.web("#E6394625"));
        ring2.setStrokeWidth(1.5);
        ring2.getStrokeDashArray().addAll(5.0, 5.0);
        
        // ═══ FONDO PRINCIPAL ═══
        Circle bgGlow = new Circle(60);
        bgGlow.setFill(Color.web("#E6394612"));
        
        Circle bgMain = new Circle(50);
        bgMain.setFill(Color.web("#E6394620"));
        bgMain.setStroke(Color.web("#E6394640"));
        bgMain.setStrokeWidth(2);
        
        // ═══ ICONO CENTRAL ═══
        javafx.scene.Node iconoLaptop = IconosSVG.laptop("#E63946", 52);
        
        // ═══ PARTÍCULAS DECORATIVAS ═══
        Circle particle1 = new Circle(3);
        particle1.setFill(Color.web("#E63946"));
        particle1.setTranslateX(85);
        particle1.setTranslateY(-25);
        particle1.setOpacity(0.6);
        
        Circle particle2 = new Circle(2);
        particle2.setFill(Color.web("#E63946"));
        particle2.setTranslateX(-72);
        particle2.setTranslateY(45);
        particle2.setOpacity(0.4);
        
        Circle particle3 = new Circle(2.5);
        particle3.setFill(Color.web("#E63946"));
        particle3.setTranslateX(50);
        particle3.setTranslateY(72);
        particle3.setOpacity(0.5);
        
        // ═══ ICONOS DE HERRAMIENTAS (ORBITAN AL HACER HOVER) ═══
        double orbitRadius = 88;
        String[] toolColors = {"#E63946", "#4CAF50", "#2196F3", "#FF9800", "#9C27B0", "#00BCD4"};
        
        // Crear 6 iconos de herramientas posicionados en círculo
        javafx.scene.Node toolCpu = IconosSVG.computadora(toolColors[0], 18);
        javafx.scene.Node toolRam = IconosSVG.servidor(toolColors[1], 18);
        javafx.scene.Node toolDisk = IconosSVG.herramienta(toolColors[2], 18);
        javafx.scene.Node toolNet = IconosSVG.documento(toolColors[3], 18);
        javafx.scene.Node toolApp = IconosSVG.paquete(toolColors[4], 18);
        javafx.scene.Node toolGpu = IconosSVG.estadisticas(toolColors[5], 18);
        
        // Envolver cada icono en un contenedor circular con fondo
        StackPane[] toolIcons = new StackPane[6];
        javafx.scene.Node[] icons = {toolCpu, toolRam, toolDisk, toolNet, toolApp, toolGpu};
        
        for (int t = 0; t < 6; t++) {
            Circle toolBg = new Circle(16);
            toolBg.setFill(Color.web(toolColors[t] + "25"));
            toolBg.setStroke(Color.web(toolColors[t] + "50"));
            toolBg.setStrokeWidth(1);
            
            toolIcons[t] = new StackPane(toolBg, icons[t]);
            toolIcons[t].setOpacity(0);
            toolIcons[t].setScaleX(0);
            toolIcons[t].setScaleY(0);
            
            // Posicionar en círculo (60° entre cada uno)
            double angle = Math.toRadians(t * 60 - 90);
            toolIcons[t].setTranslateX(Math.cos(angle) * orbitRadius);
            toolIcons[t].setTranslateY(Math.sin(angle) * orbitRadius);
        }
        
        container.getChildren().addAll(ring3, ring2, bgGlow, bgMain, iconoLaptop, particle1, particle2, particle3);
        container.getChildren().addAll(toolIcons);
        
        // ★ Cachear nodos animados: GPU rasteriza el nodo una vez y lo transforma en hardware
        container.setCache(true);
        container.setCacheHint(javafx.scene.CacheHint.SPEED);
        // Anillos usan ROTATE (óptimo para rotaciones — evita re-rasterización por ángulo)
        ring3.setCache(true);
        ring3.setCacheHint(javafx.scene.CacheHint.ROTATE);
        ring2.setCache(true);
        ring2.setCacheHint(javafx.scene.CacheHint.ROTATE);
        // Fondo y partículas usan SPEED (óptimo para fade/translate)
        bgGlow.setCache(true);
        bgGlow.setCacheHint(javafx.scene.CacheHint.SPEED);
        bgMain.setCache(true);
        bgMain.setCacheHint(javafx.scene.CacheHint.SPEED);
        particle1.setCache(true);
        particle1.setCacheHint(javafx.scene.CacheHint.SPEED);
        particle2.setCache(true);
        particle2.setCacheHint(javafx.scene.CacheHint.SPEED);
        particle3.setCache(true);
        particle3.setCacheHint(javafx.scene.CacheHint.SPEED);
        
        // ═══════════════════════════════════════════════════════════════════
        // ANIMACIONES CONTINUAS
        // ═══════════════════════════════════════════════════════════════════
        
        // Rotación lenta del anillo exterior
        RotateTransition rotateRing3 = new RotateTransition(Duration.seconds(20), ring3);
        rotateRing3.setByAngle(360);
        rotateRing3.setCycleCount(Animation.INDEFINITE);
        rotateRing3.setInterpolator(Interpolator.LINEAR);
        rotateRing3.play();
        
        // Rotación inversa del anillo medio
        RotateTransition rotateRing2 = new RotateTransition(Duration.seconds(15), ring2);
        rotateRing2.setByAngle(-360);
        rotateRing2.setCycleCount(Animation.INDEFINITE);
        rotateRing2.setInterpolator(Interpolator.LINEAR);
        rotateRing2.play();
        
        // Flotación suave del contenedor
        TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(3), container);
        floatAnim.setFromY(0);
        floatAnim.setToY(-8);
        floatAnim.setCycleCount(Animation.INDEFINITE);
        floatAnim.setAutoReverse(true);
        floatAnim.setInterpolator(Interpolator.EASE_BOTH);
        floatAnim.play();
        
        // Parpadeo de partículas
        FadeTransition fadeP1 = new FadeTransition(Duration.seconds(2), particle1);
        fadeP1.setFromValue(0.6);
        fadeP1.setToValue(0.2);
        fadeP1.setCycleCount(Animation.INDEFINITE);
        fadeP1.setAutoReverse(true);
        fadeP1.play();
        
        FadeTransition fadeP2 = new FadeTransition(Duration.seconds(2.5), particle2);
        fadeP2.setFromValue(0.4);
        fadeP2.setToValue(0.1);
        fadeP2.setCycleCount(Animation.INDEFINITE);
        fadeP2.setAutoReverse(true);
        fadeP2.setDelay(Duration.millis(500));
        fadeP2.play();
        
        FadeTransition fadeP3 = new FadeTransition(Duration.seconds(1.8), particle3);
        fadeP3.setFromValue(0.5);
        fadeP3.setToValue(0.15);
        fadeP3.setCycleCount(Animation.INDEFINITE);
        fadeP3.setAutoReverse(true);
        fadeP3.setDelay(Duration.millis(300));
        fadeP3.play();
        
        // ═══════════════════════════════════════════════════════════════════
        // ANIMACIONES DE HOVER — ICONOS DE HERRAMIENTAS APARECEN
        // ═══════════════════════════════════════════════════════════════════
        
        // Almacenar micro-flotaciones para poder detenerlas al salir (evita memory leak)
        final TranslateTransition[] microFloats = new TranslateTransition[6];
        
        container.setOnMouseEntered(e -> {
            // Escalar todo el contenedor
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), container);
            scaleUp.setToX(1.08);
            scaleUp.setToY(1.08);
            scaleUp.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            scaleUp.play();
            
            // Acelerar rotación de anillos
            rotateRing3.setRate(3);
            rotateRing2.setRate(3);
            
            // Iluminar fondo principal
            Timeline glowAnim = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(bgMain.fillProperty(), bgMain.getFill()),
                    new KeyValue(bgMain.strokeProperty(), bgMain.getStroke())),
                new KeyFrame(Duration.millis(300), 
                    new KeyValue(bgMain.fillProperty(), Color.web("#E6394635"), AnimacionesFX.EASE_OUT_CUBIC),
                    new KeyValue(bgMain.strokeProperty(), Color.web("#E6394680"), AnimacionesFX.EASE_OUT_CUBIC))
            );
            glowAnim.play();
            
            // Expandir glow
            ScaleTransition glowExpand = new ScaleTransition(Duration.millis(300), bgGlow);
            glowExpand.setToX(1.2);
            glowExpand.setToY(1.2);
            glowExpand.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            glowExpand.play();
            
            // Hacer partículas más visibles
            particle1.setOpacity(1);
            particle2.setOpacity(0.8);
            particle3.setOpacity(0.9);
            
            // ═══ MOSTRAR ICONOS DE HERRAMIENTAS con efecto escalonado ═══
            for (int t = 0; t < 6; t++) {
                final StackPane tool = toolIcons[t];
                final int idx = t;
                
                // Detener micro-flotación anterior si existe (evita acumulación)
                if (microFloats[idx] != null) {
                    microFloats[idx].stop();
                }
                
                // Resetear posición base
                double angle3 = Math.toRadians(idx * 60 - 90);
                double baseX = Math.cos(angle3) * orbitRadius;
                double baseY = Math.sin(angle3) * orbitRadius;
                tool.setTranslateX(baseX);
                tool.setTranslateY(baseY);
                
                // Fade + Scale in con delay escalonado
                FadeTransition fadeIn = new FadeTransition(Duration.millis(350), tool);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                fadeIn.setDelay(Duration.millis(idx * 60));
                
                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), tool);
                scaleIn.setFromX(0);
                scaleIn.setFromY(0);
                scaleIn.setToX(1);
                scaleIn.setToY(1);
                scaleIn.setInterpolator(AnimacionesFX.EASE_OUT_BACK);
                scaleIn.setDelay(Duration.millis(idx * 60));
                
                fadeIn.play();
                scaleIn.play();
                
                // Micro-flotación individual de cada icono (guardada para cleanup)
                TranslateTransition microFloat = new TranslateTransition(Duration.millis(1500 + idx * 200), tool);
                microFloat.setFromY(baseY - 3);
                microFloat.setToY(baseY + 3);
                microFloat.setCycleCount(Animation.INDEFINITE);
                microFloat.setAutoReverse(true);
                microFloat.setInterpolator(Interpolator.EASE_BOTH);
                microFloat.setDelay(Duration.millis(idx * 100 + 400));
                microFloat.play();
                microFloats[idx] = microFloat; // ← guardar referencia para poder detener
            }
        });
        
        container.setOnMouseExited(e -> {
            // Volver a escala normal
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), container);
            scaleDown.setToX(1);
            scaleDown.setToY(1);
            scaleDown.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            scaleDown.play();
            
            // Velocidad normal de anillos
            rotateRing3.setRate(1);
            rotateRing2.setRate(1);
            
            // Restaurar fondo
            Timeline glowRestore = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(bgMain.fillProperty(), bgMain.getFill()),
                    new KeyValue(bgMain.strokeProperty(), bgMain.getStroke())),
                new KeyFrame(Duration.millis(300), 
                    new KeyValue(bgMain.fillProperty(), Color.web("#E6394620"), AnimacionesFX.EASE_OUT_CUBIC),
                    new KeyValue(bgMain.strokeProperty(), Color.web("#E6394640"), AnimacionesFX.EASE_OUT_CUBIC))
            );
            glowRestore.play();
            
            // Contraer glow
            ScaleTransition glowContract = new ScaleTransition(Duration.millis(300), bgGlow);
            glowContract.setToX(1);
            glowContract.setToY(1);
            glowContract.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
            glowContract.play();
            
            // ═══ OCULTAR ICONOS DE HERRAMIENTAS ═══
            for (int t = 0; t < 6; t++) {
                final StackPane tool = toolIcons[t];
                final int idx = t;
                double angle2 = Math.toRadians(idx * 60 - 90);
                
                // ★ Detener micro-flotación ANTES de animar salida (fix memory leak)
                if (microFloats[idx] != null) {
                    microFloats[idx].stop();
                    microFloats[idx] = null;
                }
                
                // Resetear posición base
                tool.setTranslateX(Math.cos(angle2) * orbitRadius);
                tool.setTranslateY(Math.sin(angle2) * orbitRadius);
                
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), tool);
                fadeOut.setToValue(0);
                fadeOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
                fadeOut.setDelay(Duration.millis(idx * 30));
                
                ScaleTransition scaleOut = new ScaleTransition(Duration.millis(250), tool);
                scaleOut.setToX(0);
                scaleOut.setToY(0);
                scaleOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);
                scaleOut.setDelay(Duration.millis(idx * 30));
                
                fadeOut.play();
                scaleOut.play();
            }
        });
        
        // Click - efecto ripple
        container.setOnMouseClicked(e -> {
            // Efecto de pulso rápido
            ScaleTransition pulse = new ScaleTransition(Duration.millis(150), bgMain);
            pulse.setToX(1.3);
            pulse.setToY(1.3);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);
            pulse.play();
            
            // Rotación rápida
            RotateTransition quickRotate = new RotateTransition(Duration.millis(500), iconoLaptop);
            quickRotate.setByAngle(360);
            quickRotate.setInterpolator(Interpolator.EASE_BOTH);
            quickRotate.play();
        });
        
        return container;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PANEL LATERAL DE PROYECTOS (DESLIZANTE)
    // ════════════════════════════════════════════════════════════════════════════

    private static void crearPanelLateralProyectos() {
        // Overlay oscuro
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        overlay.setVisible(false);
        overlay.setOnMouseClicked(e -> cerrarPanelLateral());

        // Panel lateral
        sidePanel = new VBox(0);
        sidePanel.setPrefWidth(380);
        sidePanel.setMaxWidth(380);
        sidePanel.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-width: 0 0 0 1;"
        );
        sidePanel.setTranslateX(400); // Fuera de vista inicialmente
        sidePanel.setMouseTransparent(true); // No interceptar clics cuando está oculto

        // Header del panel
        HBox panelHeader = new HBox();
        panelHeader.setAlignment(Pos.CENTER_LEFT);
        panelHeader.setPadding(new Insets(24, 24, 24, 24));
        panelHeader.setStyle("-fx-border-color: " + TemaManager.getBorder() + "; -fx-border-width: 0 0 1 0;");

        Label panelTitle = new Label("Seleccionar Proyecto");
        panelTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 18));
        panelTitle.setTextFill(COLOR_TEXT());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCerrar = inventario.fx.util.ComponentesFX.crearBotonCerrar(() -> cerrarPanelLateral(), 36);

        panelHeader.getChildren().addAll(panelTitle, spacer, btnCerrar);

        // Lista de proyectos
        projectsListContainer = new VBox(8);
        projectsListContainer.setPadding(new Insets(16, 16, 16, 16));

        ScrollPane scrollPane = new ScrollPane(projectsListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Cargar proyectos iniciales
        actualizarListaProyectosPanelLateral();

        sidePanel.getChildren().addAll(panelHeader, scrollPane);

        // Alineación a la derecha
        StackPane.setAlignment(sidePanel, Pos.CENTER_RIGHT);
        StackPane.setAlignment(overlay, Pos.CENTER);

        // Agregar al root pero ocultos
        overlay.setOpacity(0);
        overlay.setMouseTransparent(true); // MUY IMPORTANTE: no interceptar clics cuando está oculto
        rootStack.getChildren().addAll(overlay, sidePanel);

        // Guardar referencias
        sidePanel.setUserData(overlay);
    }
    
    /**
     * Actualiza la lista de proyectos en el panel lateral.
     * Este método se llama automáticamente cuando se agregan, editan o eliminan proyectos.
     */
    private static void actualizarListaProyectosPanelLateral() {
        if (projectsListContainer == null) {
            System.out.println("[InventarioFX] projectsListContainer es null, no se puede actualizar");
            return;
        }
        
        projectsListContainer.getChildren().clear();
        
        java.util.List<AdminManager.Proyecto> proyectos = AdminManager.getProyectosActivos();
        System.out.println("[InventarioFX] Actualizando panel lateral con " + proyectos.size() + " proyectos");

        if (proyectos.isEmpty()) {
            // ── Empty state premium con iconos Lucide ──
            StackPane emptyWrapper = new StackPane();
            emptyWrapper.setPadding(new Insets(4));

            VBox emptyState = new VBox(0);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setStyle(
                "-fx-background-color: " + TemaManager.getSurface() + ";" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: " + TemaManager.getBorder() + ";" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;"
            );

            // Franja superior acento con gradiente
            StackPane topAccent = new StackPane();
            topAccent.setPrefHeight(86);
            topAccent.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #E6394618, #E6394605);" +
                "-fx-background-radius: 18 18 0 0;"
            );
            StackPane iconCircle = new StackPane();
            Circle iconBg = new Circle(28);
            iconBg.setFill(Color.web(TemaManager.COLOR_PRIMARY + "22"));
            iconBg.setStroke(Color.web(TemaManager.COLOR_PRIMARY + "55"));
            iconBg.setStrokeWidth(1.5);
            DropShadow iconGlow = new DropShadow();
            iconGlow.setColor(Color.web(TemaManager.COLOR_PRIMARY + "55"));
            iconGlow.setRadius(14);
            iconBg.setEffect(iconGlow);
            javafx.scene.Node iconoSVG = IconosSVG.carpetaMas(TemaManager.COLOR_PRIMARY, 24);
            iconCircle.getChildren().addAll(iconBg, iconoSVG);
            topAccent.getChildren().add(iconCircle);

            // Cuerpo del card
            VBox bodyBox = new VBox(14);
            bodyBox.setAlignment(Pos.CENTER);
            bodyBox.setPadding(new Insets(18, 22, 22, 22));

            Label emptyTitle = new Label("Sin proyectos aún");
            emptyTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            emptyTitle.setTextFill(COLOR_TEXT());

            Label emptyDesc = new Label("Crea un proyecto para empezar a registrar el inventario de equipos.");
            emptyDesc.setFont(Font.font("Segoe UI", 11.5));
            emptyDesc.setTextFill(COLOR_TEXT_MUTED());
            emptyDesc.setWrapText(true);
            emptyDesc.setMaxWidth(300);
            emptyDesc.setAlignment(Pos.CENTER);
            emptyDesc.setStyle("-fx-text-alignment: center;");

            // Pasos rápidos
            VBox stepsBox = new VBox(8);
            stepsBox.setMaxWidth(300);
            stepsBox.setStyle(
                "-fx-background-color: " + TemaManager.getBgLight() + ";" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 12;"
            );
            stepsBox.getChildren().addAll(
                crearPasoEmpty("1", IconosSVG.admin(TemaManager.COLOR_PRIMARY, 14), "Ve a Administrar"),
                crearPasoEmpty("2", IconosSVG.agregar("#10B981", 14), "Crea un nuevo proyecto"),
                crearPasoEmpty("3", IconosSVG.estadisticas("#3B82F6", 14), "¡Genera el inventario!")
            );

            // Botón con ícono
            HBox btnContent = new HBox(8);
            btnContent.setAlignment(Pos.CENTER);
            Label btnIcon = new Label();
            btnIcon.setGraphic(IconosSVG.admin("#FFFFFF", 14));
            Label btnLabel = new Label("Ir a Administrar");
            btnLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            btnLabel.setTextFill(Color.WHITE);
            btnContent.getChildren().addAll(btnIcon, btnLabel);

            Button btnIrAdmin = new Button();
            btnIrAdmin.setGraphic(btnContent);
            btnIrAdmin.setPrefHeight(36);
            btnIrAdmin.setPrefWidth(200);
            btnIrAdmin.setStyle(
                "-fx-background-color: " + TemaManager.COLOR_PRIMARY + ";" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            btnIrAdmin.setOnMouseEntered(ev -> {
                btnIrAdmin.setStyle("-fx-background-color: #FF4D5A; -fx-background-radius: 10; -fx-cursor: hand;");
                AnimacionesFX.hoverIn(btnIrAdmin, 1.04, 120);
            });
            btnIrAdmin.setOnMouseExited(ev -> {
                btnIrAdmin.setStyle("-fx-background-color: " + TemaManager.COLOR_PRIMARY + "; -fx-background-radius: 10; -fx-cursor: hand;");
                AnimacionesFX.hoverOut(btnIrAdmin, 120);
            });
            btnIrAdmin.setOnAction(ev -> {
                cerrarPanelLateral();
                mostrarPanelAdministracion();
            });

            bodyBox.getChildren().addAll(emptyTitle, emptyDesc, stepsBox, btnIrAdmin);
            emptyState.getChildren().addAll(topAccent, bodyBox);
            emptyWrapper.getChildren().add(emptyState);
            projectsListContainer.getChildren().add(emptyWrapper);
            AnimacionesFX.slideUpFadeIn(emptyWrapper, 350, 15, 0);
            System.out.println("[InventarioFX] Panel lateral actualizado con estado vacío");
            return;
        }

        for (int i = 0; i < proyectos.size(); i++) {
            final AdminManager.Proyecto proyecto = proyectos.get(i);
            final String nombreFormateado = (i + 1) + ". " + proyecto.getNombre();
            final int indiceProyecto = i;
            final String proyectoId = proyecto.getId();
            System.out.println("[InventarioFX] Agregando proyecto: " + nombreFormateado);
            VBox projectItem = crearItemProyectoAdmin(proyecto, i);
            projectItem.setOnMouseClicked(e -> {
                CURRENT_PROJECT = nombreFormateado;
                // Obtener datos actualizados del proyecto específico sin recargar toda la lista
                // Esto preserva el orden de los proyectos
                AdminManager.Proyecto proyectoActualizado = AdminManager.getProyectoPorId(proyectoId);
                if (proyectoActualizado == null) {
                    proyectoActualizado = AdminManager.getProyectoPorIndice(indiceProyecto);
                }
                // Guardar la imagen del proyecto seleccionado para el reporte
                String imagenPath = proyectoActualizado != null ? proyectoActualizado.getImagenPath() : "";
                System.setProperty("reporte.proyectoImagenPath", imagenPath != null ? imagenPath : "");
                System.setProperty("inventario.proyectoActual", nombreFormateado);
                System.out.println("[InventarioFX] Proyecto seleccionado: " + nombreFormateado + ", imagen: " + imagenPath);
                cerrarPanelLateral();
                ejecutarAccionPendiente();
            });
            projectsListContainer.getChildren().add(projectItem);
        }
        
        System.out.println("[InventarioFX] Panel lateral actualizado con " + projectsListContainer.getChildren().size() + " items");
    }

    /** Fila de paso numerado para el empty state del panel lateral. */
    private static HBox crearPasoEmpty(String num, javafx.scene.Node icono, String texto) {
        HBox fila = new HBox(10);
        fila.setAlignment(Pos.CENTER_LEFT);

        // Número
        StackPane numCircle = new StackPane();
        Circle bg = new Circle(11);
        bg.setFill(Color.web(TemaManager.COLOR_PRIMARY + "25"));
        bg.setStroke(Color.web(TemaManager.COLOR_PRIMARY + "55"));
        bg.setStrokeWidth(1);
        Label numLabel = new Label(num);
        numLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        numLabel.setTextFill(Color.web(TemaManager.COLOR_PRIMARY));
        numCircle.getChildren().addAll(bg, numLabel);

        // Icono
        StackPane iconPane = new StackPane(icono);
        iconPane.setPrefSize(20, 20);

        // Texto
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11.5));
        lbl.setTextFill(COLOR_TEXT());

        fila.getChildren().addAll(numCircle, iconPane, lbl);
        return fila;
    }

    private static VBox crearItemProyecto(String nombre, int index) {
        VBox item = new VBox(4);
        item.setPadding(new Insets(16, 16, 16, 16));
        item.setStyle(
            "-fx-background-color: " + TemaManager.getBgLight() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Número con color según índice
        String[] colores = {"#E63946", "#00C853", "#2196F3", "#FF9800", "#9C27B0", "#00BCD4", "#FF5722", "#4CAF50"};
        String color = colores[index % colores.length];

        StackPane numCircle = new StackPane();
        Circle circleBg = new Circle(16);
        circleBg.setFill(Color.web(color + "30"));
        Label numLabel = new Label(String.valueOf(index + 1));
        numLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        numLabel.setTextFill(Color.web(color));
        numCircle.getChildren().addAll(circleBg, numLabel);

        // Nombre del proyecto
        String nombreCorto = nombre;
        if (nombre.contains(" - ")) {
            nombreCorto = nombre.substring(nombre.indexOf(" - ") + 3);
        } else if (nombre.contains(". ")) {
            nombreCorto = nombre.substring(nombre.indexOf(". ") + 2);
        }

        Label lblNombre = new Label(nombreCorto);
        lblNombre.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        lblNombre.setTextFill(COLOR_TEXT());
        lblNombre.setWrapText(true);

        header.getChildren().addAll(numCircle, lblNombre);

        // Indicador de flecha
        Label arrow = new Label("→");
        arrow.setFont(Font.font("Segoe UI", 14));
        arrow.setTextFill(Color.web(color));
        arrow.setOpacity(0);

        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        content.getChildren().addAll(header, sp, arrow);

        item.getChildren().add(content);

        // Hover effects
        item.setOnMouseEntered(e -> {
            item.setStyle(
                "-fx-background-color: " + TemaManager.getSurfaceHover() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;"
            );
            arrow.setOpacity(1);
            AnimacionesFX.hoverIn(item, 1.02, 150);
        });
        item.setOnMouseExited(e -> {
            item.setStyle(
                "-fx-background-color: " + TemaManager.getBgLight() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;"
            );
            arrow.setOpacity(0);
            AnimacionesFX.hoverOut(item, 150);
        });

        return item;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // ITEM DE PROYECTO CON SOPORTE ADMIN
    // ════════════════════════════════════════════════════════════════════════════

    private static VBox crearItemProyectoAdmin(AdminManager.Proyecto proyecto, int index) {
        VBox item = new VBox(6);
        item.setPadding(new Insets(14, 16, 14, 16));
        
        String color = proyecto.getColor() != null ? proyecto.getColor() : AdminManager.getColorProyecto(index);
        
        item.setStyle(
            "-fx-background-color: " + TemaManager.getBgLight() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );

        // Fila principal
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Número con color del proyecto
        StackPane numCircle = new StackPane();
            Circle circleBg = new Circle(16);
            // Fondo sólido con el color del proyecto
            circleBg.setFill(Color.web(color));
            // Borde semitransparente para dar profundidad
            circleBg.setStroke(Color.web(color + "55"));
            circleBg.setStrokeWidth(2);
        Label numLabel = new Label(String.valueOf(index + 1));
        numLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        numLabel.setTextFill(Color.web(color));
        numCircle.getChildren().addAll(circleBg, numLabel);

        // Información del proyecto
        VBox infoBox = new VBox(2);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        Label lblNombre = new Label(proyecto.getNombre());
        lblNombre.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        lblNombre.setTextFill(COLOR_TEXT());
        lblNombre.setWrapText(true);
        
        if (proyecto.getDescripcion() != null && !proyecto.getDescripcion().isEmpty()) {
            Label lblDesc = new Label(proyecto.getDescripcion());
            lblDesc.setFont(Font.font("Segoe UI", 11));
            lblDesc.setTextFill(COLOR_TEXT_MUTED());
            infoBox.getChildren().addAll(lblNombre, lblDesc);
        } else {
            infoBox.getChildren().add(lblNombre);
        }

        // Flecha indicadora
        Label arrow = new Label("→");
        arrow.setFont(Font.font("Segoe UI", 14));
        arrow.setTextFill(Color.web(color));
        arrow.setOpacity(0);

        header.getChildren().addAll(numCircle, infoBox, arrow);
        item.getChildren().add(header);

        // Hover effects
        String normalStyle = item.getStyle();
        item.setOnMouseEntered(e -> {
            item.setStyle(
                "-fx-background-color: " + TemaManager.getSurfaceHover() + ";" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;"
            );
            arrow.setOpacity(1);
            AnimacionesFX.hoverIn(item, 1.02, 150);
        });
        item.setOnMouseExited(e -> {
            item.setStyle(normalStyle);
            arrow.setOpacity(0);
            AnimacionesFX.hoverOut(item, 150);
        });

        return item;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PANEL DE ADMINISTRACIÓN
    // ════════════════════════════════════════════════════════════════════════════

    private static void mostrarPanelAdministracion() {
        if (!AdminManager.isAdminMode()) {
            if (AdminManager.needsInitialPasswordSetup()) {
                // Primera ejecución: configurar contraseña
                mostrarDialogoSetupPassword();
            } else {
                // Mostrar diálogo de login
                mostrarDialogoLoginAdmin();
            }
        } else {
            // Ya está en modo admin, mostrar panel de gestión (ventana independiente)
            AdminPanelFX.mostrar(mainStage);
        }
    }

    /** Muestra un error en errorLabel y redimensiona el diálogo para que se vea completo. */
    private static void mostrarErrorSetup(Label errorLabel, Stage dialog, String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        Platform.runLater(() -> {
            dialog.sizeToScene();
            dialog.centerOnScreen();
        });
    }

    /**
     * Diálogo de configuración inicial de contraseña (primera ejecución).
     * Obliga al administrador a establecer una contraseña segura antes de continuar.
     */
    private static void mostrarDialogoSetupPassword() {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initOwner(mainStage);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(24));

        VBox container = new VBox(0);
        container.setAlignment(Pos.CENTER);
        container.setPrefWidth(420);
        container.setMaxWidth(420);
        container.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 22;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 22;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setRadius(40);
        shadow.setColor(Color.rgb(0, 0, 0, 0.45));
        shadow.setOffsetY(12);
        container.setEffect(shadow);

        // ── Franja superior verde ──
        StackPane topBand = new StackPane();
        topBand.setPrefHeight(100);
        topBand.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #10B98118, #10B98108);" +
            "-fx-background-radius: 22 22 0 0;"
        );

        StackPane iconCircle = new StackPane();
        Circle iconBg = new Circle(32);
        iconBg.setFill(Color.web("#10B98130"));
        iconBg.setStroke(Color.web("#10B98170"));
        iconBg.setStrokeWidth(1.5);
        DropShadow iconGlow = new DropShadow();
        iconGlow.setColor(Color.web("#10B98155"));
        iconGlow.setRadius(16);
        iconBg.setEffect(iconGlow);
        iconCircle.getChildren().addAll(iconBg, IconosSVG.candado("#10B981", 30));
        topBand.getChildren().add(iconCircle);

        // ── Cuerpo ──
        VBox body = new VBox(16);
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(24, 32, 32, 32));

        Label titulo = new Label("Configuración Inicial");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titulo.setTextFill(COLOR_TEXT());

        // Chip informativo
        HBox chipBox = new HBox(8);
        chipBox.setAlignment(Pos.CENTER);
        chipBox.setStyle(
            "-fx-background-color: #10B98118;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 6 14;"
        );
        Label chipIcon = new Label();
        chipIcon.setGraphic(IconosSVG.escudo("#10B981", 13));
        Label chipTxt = new Label("Primera configuración de seguridad");
        chipTxt.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        chipTxt.setTextFill(Color.web("#10B981"));
        chipBox.getChildren().addAll(chipIcon, chipTxt);

        Label subtitulo = new Label("Establece una contraseña segura para el modo administrador.");
        subtitulo.setFont(Font.font("Segoe UI", 12));
        subtitulo.setTextFill(COLOR_TEXT_MUTED());
        subtitulo.setWrapText(true);
        subtitulo.setMaxWidth(340);
        subtitulo.setAlignment(Pos.CENTER);
        subtitulo.setStyle("-fx-text-alignment: center;");

        // Requisitos de contraseña
        VBox reqBox = new VBox(6);
        reqBox.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 12 16;"
        );
        Label reqTitle = new Label("Requisitos de la contraseña:");
        reqTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        reqTitle.setTextFill(COLOR_TEXT_MUTED());
        reqBox.getChildren().add(reqTitle);
        String[] reqs = {"Mínimo 8 caracteres", "Al menos una mayúscula (A-Z)", "Al menos una minúscula (a-z)", "Al menos un número (0-9)", "Al menos un símbolo (!@#$...)"};
        String[] reqColors = {"#F59E0B", "#3B82F6", "#10B981", "#8B5CF6", "#E63946"};
        for (int r = 0; r < reqs.length; r++) {
            HBox reqRow = new HBox(8);
            reqRow.setAlignment(Pos.CENTER_LEFT);
            Circle dot = new Circle(3.5);
            dot.setFill(Color.web(reqColors[r]));
            Label reqLbl = new Label(reqs[r]);
            reqLbl.setFont(Font.font("Segoe UI", 11));
            reqLbl.setTextFill(COLOR_TEXT_MUTED());
            reqRow.getChildren().addAll(dot, reqLbl);
            reqBox.getChildren().add(reqRow);
        }

        // Campos contraseña
        String fieldStyle =
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-prompt-text-fill: " + TemaManager.getTextMuted() + ";" +
            "-fx-background-radius: 10; -fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 10; -fx-font-size: 13px; -fx-padding: 10 16;";

        VBox field1Box = new VBox(6);
        Label lbl1 = new Label("Nueva contraseña");
        lbl1.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        lbl1.setTextFill(COLOR_TEXT_MUTED());
        PasswordField passNueva = new PasswordField();
        passNueva.setPromptText("Escribe tu contraseña aquí");
        passNueva.setPrefHeight(42);
        passNueva.setMaxWidth(Double.MAX_VALUE);
        passNueva.setStyle(fieldStyle);
        field1Box.getChildren().addAll(lbl1, passNueva);

        VBox field2Box = new VBox(6);
        Label lbl2 = new Label("Confirmar contraseña");
        lbl2.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        lbl2.setTextFill(COLOR_TEXT_MUTED());
        PasswordField passConfirmar = new PasswordField();
        passConfirmar.setPromptText("Repite la contraseña");
        passConfirmar.setPrefHeight(42);
        passConfirmar.setMaxWidth(Double.MAX_VALUE);
        passConfirmar.setStyle(fieldStyle);
        field2Box.getChildren().addAll(lbl2, passConfirmar);

        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        errorLabel.setTextFill(Color.web("#EF4444"));
        errorLabel.setWrapText(true);
        errorLabel.setPrefWidth(340);
        errorLabel.setGraphic(IconosSVG.error("#EF4444", 14));
        errorLabel.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        errorLabel.setGraphicTextGap(6);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER);
        botones.setPadding(new Insets(4, 0, 0, 0));

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(130);
        btnCancelar.setPrefHeight(42);
        btnCancelar.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        btnCancelar.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 11; -fx-cursor: hand;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 11; -fx-border-width: 1;"
        );
        btnCancelar.setOnAction(e -> dialog.close());

        Button btnGuardar = new Button("Configurar");
        btnGuardar.setGraphic(IconosSVG.check("#FFFFFF", 14));
        btnGuardar.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        btnGuardar.setGraphicTextGap(6);
        btnGuardar.setPrefWidth(150);
        btnGuardar.setPrefHeight(42);
        btnGuardar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnGuardar.setTextFill(Color.WHITE);
        btnGuardar.setStyle(
            "-fx-background-color: #10B981;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 11; -fx-cursor: hand;"
        );
        btnGuardar.setOnMouseEntered(ev -> btnGuardar.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-background-radius: 11; -fx-cursor: hand;"));
        btnGuardar.setOnMouseExited(ev -> btnGuardar.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 11; -fx-cursor: hand;"));

        btnGuardar.setOnAction(e -> {
            String nueva = passNueva.getText();
            String confirmar = passConfirmar.getText();
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            if (nueva.isEmpty() || confirmar.isEmpty()) {
                mostrarErrorSetup(errorLabel, dialog, "Todos los campos son obligatorios");
                return;
            }
            if (!nueva.equals(confirmar)) {
                mostrarErrorSetup(errorLabel, dialog, "Las contraseñas no coinciden");
                return;
            }

            try {
                inventario.fx.security.SecurityManager.ResultadoValidacion resultado =
                    AdminManager.setupInitialPassword(nueva);
                if (resultado.isValida()) {
                    dialog.close();
                    AdminManager.LoginResult loginResult = AdminManager.login(nueva);
                    if (loginResult.isExito()) {
                        mostrarMenu(mainStage);
                        Platform.runLater(() -> AdminPanelFX.mostrar(mainStage));
                    }
                } else {
                    mostrarErrorSetup(errorLabel, dialog, resultado.getMensaje());
                }
            } catch (Exception ex) {
                mostrarErrorSetup(errorLabel, dialog, "Error inesperado: " + ex.getMessage());
            }
        });

        passConfirmar.setOnAction(e -> btnGuardar.fire());

        botones.getChildren().addAll(btnCancelar, btnGuardar);
        body.getChildren().addAll(titulo, chipBox, subtitulo, reqBox, field1Box, field2Box, errorLabel, botones);
        container.getChildren().addAll(topBand, body);
        root.getChildren().add(container);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        TemaManager.aplicarTema(scene);
        dialog.setScene(scene);
        dialog.sizeToScene();
        dialog.centerOnScreen();

        container.setOpacity(0);
        container.setScaleX(0.92);
        container.setScaleY(0.92);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), container);
        fadeIn.setToValue(1);
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(220), container);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        new ParallelTransition(fadeIn, scaleIn).play();

        dialog.show();
        dialog.sizeToScene();
        dialog.centerOnScreen();
        passNueva.requestFocus();
    }

    private static void mostrarDialogoLoginAdmin() {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initOwner(mainStage);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(20));

        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(32));
        container.setMaxWidth(360);
        container.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 20;"
        );

        // Efecto sombra
        DropShadow shadow = new DropShadow();
        shadow.setRadius(30);
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setOffsetY(10);
        container.setEffect(shadow);

        // Icono de admin
        StackPane iconCircle = new StackPane();
        Circle bgCircle = new Circle(35);
        bgCircle.setFill(Color.web("#E6394620"));
        iconCircle.getChildren().addAll(bgCircle, IconosSVG.admin("#E63946", 36));

        // Título
        Label titulo = new Label("Modo Administrador");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titulo.setTextFill(COLOR_TEXT());

        Label subtitulo = new Label("Ingresa la contraseña para gestionar proyectos");
        subtitulo.setFont(Font.font("Segoe UI", 12));
        subtitulo.setTextFill(COLOR_TEXT_MUTED());
        subtitulo.setWrapText(true);
        subtitulo.setStyle("-fx-text-alignment: center;");

        // Campo de contraseña con ojito
        HBox passwordContainer = new HBox(0);
        passwordContainer.setAlignment(Pos.CENTER_LEFT);
        passwordContainer.setMaxWidth(280);
        passwordContainer.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 10;"
        );
        passwordContainer.setPrefHeight(45);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contrasena");
        passwordField.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-prompt-text-fill: " + TemaManager.getTextMuted() + ";" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 12 16 12 16;"
        );
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        
        TextField txtPasswordVisible = new TextField();
        txtPasswordVisible.setPromptText("Contrasena");
        txtPasswordVisible.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-prompt-text-fill: " + TemaManager.getTextMuted() + ";" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 12 16 12 16;"
        );
        txtPasswordVisible.setVisible(false);
        txtPasswordVisible.setManaged(false);
        HBox.setHgrow(txtPasswordVisible, Priority.ALWAYS);
        txtPasswordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        
        // Boton ojo
        Button btnOjo = new Button();
        StackPane ojoIcon = new StackPane(IconosSVG.ojoCerrado("#64748B", 18));
        btnOjo.setGraphic(ojoIcon);
        btnOjo.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 8 12;");
        btnOjo.setOnAction(ev -> {
            boolean mostrar = !txtPasswordVisible.isVisible();
            passwordField.setVisible(!mostrar);
            passwordField.setManaged(!mostrar);
            txtPasswordVisible.setVisible(mostrar);
            txtPasswordVisible.setManaged(mostrar);
            ojoIcon.getChildren().clear();
            if (mostrar) {
                ojoIcon.getChildren().add(IconosSVG.ojo("#10B981", 18));
            } else {
                ojoIcon.getChildren().add(IconosSVG.ojoCerrado("#64748B", 18));
            }
        });
        btnOjo.setOnMouseEntered(ev -> btnOjo.setStyle("-fx-background-color: " + TemaManager.getBorder() + "; -fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 8;"));
        btnOjo.setOnMouseExited(ev -> btnOjo.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 8 12;"));
        
        passwordContainer.getChildren().addAll(passwordField, txtPasswordVisible, btnOjo);
        
        // Efecto hover en container
        passwordContainer.setOnMouseEntered(ev -> passwordContainer.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #E63946;" +
            "-fx-border-radius: 10;"
        ));
        passwordContainer.setOnMouseExited(ev -> passwordContainer.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 10;"
        ));

        // Label de error
        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        errorLabel.setTextFill(Color.web("#EF4444"));
        errorLabel.setWrapText(true);
        errorLabel.setPrefWidth(280);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setGraphic(IconosSVG.error("#EF4444", 13));
        errorLabel.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        errorLabel.setGraphicTextGap(5);

        // Botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(120);
        btnCancelar.setPrefHeight(42);
        btnCancelar.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        btnCancelar.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        btnCancelar.setOnAction(e -> dialog.close());

        Button btnIngresar = new Button("Ingresar");
        btnIngresar.setPrefWidth(120);
        btnIngresar.setPrefHeight(42);
        btnIngresar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnIngresar.setStyle(
            "-fx-background-color: #E63946;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        btnIngresar.setOnAction(e -> {
            AdminManager.LoginResult result = AdminManager.login(passwordField.getText());
            if (result.isExito()) {
                dialog.close();
                // Refrescar UI
                mostrarMenu(mainStage);
                // Mostrar panel de gestión (ventana independiente)
                Platform.runLater(() -> AdminPanelFX.mostrar(mainStage));
            } else {
                errorLabel.setText(result.getMensaje());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                passwordField.clear();
                Platform.runLater(() -> { dialog.sizeToScene(); dialog.centerOnScreen(); });
                
                // Si está bloqueado, deshabilitar botón temporalmente
                if (result.getSegundosBloqueo() > 0) {
                    btnIngresar.setDisable(true);
                    btnIngresar.setText("Bloqueado (" + result.getSegundosBloqueo() + "s)");
                    javafx.animation.Timeline countdown = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(Duration.seconds(1), ev2 -> {
                            long restante = inventario.fx.security.SecurityManager.getTiempoBloqueoRestante("admin_login");
                            if (restante <= 0) {
                                btnIngresar.setDisable(false);
                                btnIngresar.setText("Ingresar");
                                errorLabel.setText("");
                                errorLabel.setVisible(false);
                                errorLabel.setManaged(false);
                            } else {
                                btnIngresar.setText("Bloqueado (" + restante + "s)");
                            }
                        })
                    );
                    countdown.setCycleCount((int) result.getSegundosBloqueo() + 1);
                    countdown.play();
                }
                
                // Efecto de shake
                TranslateTransition shake = new TranslateTransition(Duration.millis(50), container);
                shake.setFromX(0);
                shake.setByX(10);
                shake.setCycleCount(6);
                shake.setAutoReverse(true);
                shake.play();
            }
        });

        // Enter para ingresar
        passwordField.setOnAction(e -> btnIngresar.fire());
        txtPasswordVisible.setOnAction(e -> btnIngresar.fire());

        botones.getChildren().addAll(btnCancelar, btnIngresar);

        container.getChildren().addAll(iconCircle, titulo, subtitulo, passwordContainer, errorLabel, botones);
        root.getChildren().add(container);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        TemaManager.aplicarTema(scene);
        dialog.setScene(scene);
        dialog.sizeToScene();
        dialog.centerOnScreen();

        // Animación de entrada
        container.setOpacity(0);
        container.setScaleX(0.9);
        container.setScaleY(0.9);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), container);
        fadeIn.setToValue(1);
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), container);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        new ParallelTransition(fadeIn, scaleIn).play();

        dialog.show();
        passwordField.requestFocus();
    }

    private static void mostrarVentanaGestionProyectos() {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initOwner(mainStage);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        root.setPadding(new Insets(30));

        VBox container = new VBox(0);
        container.setMaxWidth(950);
        container.setMaxHeight(780);
        container.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 24;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 24;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setRadius(60);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        shadow.setOffsetY(15);
        container.setEffect(shadow);

        // ═══ HEADER GRANDE Y MODERNO ═══
        VBox headerSection = new VBox(0);
        
        HBox headerTop = new HBox(20);
        headerTop.setAlignment(Pos.CENTER_LEFT);
        headerTop.setPadding(new Insets(32, 40, 24, 40));

        // Icono grande con gradiente
        StackPane iconCircle = new StackPane();
        Circle bgC = new Circle(36);
        bgC.setFill(new javafx.scene.paint.LinearGradient(
            0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new javafx.scene.paint.Stop(0, Color.web("#E63946")),
            new javafx.scene.paint.Stop(1, Color.web("#9D174D"))
        ));
        iconCircle.getChildren().addAll(bgC, IconosSVG.admin("#FFFFFF", 36));

        VBox titleBox = new VBox(6);
        Label titulo = new Label("Panel de Administración");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titulo.setTextFill(COLOR_TEXT());
        
        HBox statusBadge = new HBox(10);
        statusBadge.setAlignment(Pos.CENTER_LEFT);
        Circle statusDot = new Circle(6);
        statusDot.setFill(Color.web("#10B981"));
        Label subtitulo = new Label("Sesión de administrador activa");
        subtitulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        subtitulo.setTextFill(Color.web("#10B981"));
        statusBadge.getChildren().addAll(statusDot, subtitulo);
        titleBox.getChildren().addAll(titulo, statusBadge);

        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);

        Button btnCerrar = inventario.fx.util.ComponentesFX.crearBotonCerrar(dialog::close, 44);

        headerTop.getChildren().addAll(iconCircle, titleBox, spacerH, btnCerrar);
        
        // ═══ TARJETAS DE ESTADÍSTICAS ═══
        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(0, 40, 24, 40));
        statsRow.setAlignment(Pos.CENTER_LEFT);
        
        int totalProyectos = AdminManager.getProyectosActivos().size();
        
        VBox statCard1 = crearStatCardAdmin(String.valueOf(totalProyectos), "Proyectos Activos", "#3B82F6", IconosSVG.carpeta("#3B82F6", 24));
        VBox statCard2 = crearStatCardAdmin(String.valueOf(AdminManager.COLORES_PROYECTO.length), "Colores Disponibles", "#8B5CF6", IconosSVG.estadisticas("#8B5CF6", 24));
        VBox statCard3 = crearStatCardAdmin(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM")), "Última Sesión", "#10B981", IconosSVG.reloj("#10B981", 24));
        
        HBox.setHgrow(statCard1, Priority.ALWAYS);
        HBox.setHgrow(statCard2, Priority.ALWAYS);
        HBox.setHgrow(statCard3, Priority.ALWAYS);
        
        statsRow.getChildren().addAll(statCard1, statCard2, statCard3);
        
        headerSection.getChildren().addAll(headerTop, statsRow);
        headerSection.setStyle("-fx-background-color: " + TemaManager.getSurface() + "; -fx-background-radius: 24 24 0 0;");

        // ═══ TOOLBAR MEJORADO ═══
        HBox toolbar = new HBox(16);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(24, 40, 20, 40));
        
        Label lblSeccion = new Label("Gestión de Proyectos");
        lblSeccion.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblSeccion.setTextFill(COLOR_TEXT());

        Region spacerTool = new Region();
        HBox.setHgrow(spacerTool, Priority.ALWAYS);

        Button btnNuevo = crearBotonToolbarGrande("+ Nuevo Proyecto", "#10B981");
        
        // Menú de importación mejorado
        MenuButton btnImportar = new MenuButton("Importar");
        btnImportar.setGraphic(IconosSVG.importar("#FFFFFF", 18));
        btnImportar.setGraphicTextGap(10);
        btnImportar.setPadding(new Insets(14, 24, 14, 20));
        btnImportar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnImportar.setStyle(
            "-fx-background-color: #3B82F6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );
        
        MenuItem itemExcel = new MenuItem("Importar proyecto (1 Excel = 1 Proyecto)");
        MenuItem itemCarpeta = new MenuItem("Importar masivo (carpeta con varios Excel)");
        btnImportar.getItems().addAll(itemExcel, itemCarpeta);
        
        Button btnLogout = crearBotonToolbarGrande("Cerrar Sesión", "#EF4444");

        toolbar.getChildren().addAll(lblSeccion, spacerTool, btnNuevo, btnImportar, btnLogout);

        // ═══ LISTA DE PROYECTOS MEJORADA ═══
        VBox listaSection = new VBox(0);
        listaSection.setPadding(new Insets(0, 40, 32, 40));
        VBox.setVgrow(listaSection, Priority.ALWAYS);
        
        // Encabezado de columnas
        HBox headerColumnas = new HBox(0);
        headerColumnas.setPadding(new Insets(16, 24, 16, 24));
        headerColumnas.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 16 16 0 0;" +
            "-fx-border-color: transparent transparent " + TemaManager.getBorder() + " transparent;" +
            "-fx-border-width: 0 0 2 0;"
        );
        
        Label colProyecto = new Label("PROYECTO");
        colProyecto.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        colProyecto.setTextFill(COLOR_TEXT_MUTED());
        colProyecto.setPrefWidth(320);
        
        Label colDescripcion = new Label("DESCRIPCIÓN");
        colDescripcion.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        colDescripcion.setTextFill(COLOR_TEXT_MUTED());
        HBox.setHgrow(colDescripcion, Priority.ALWAYS);
        
        Label colAcciones = new Label("ACCIONES");
        colAcciones.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        colAcciones.setTextFill(COLOR_TEXT_MUTED());
        colAcciones.setPrefWidth(180);
        colAcciones.setAlignment(Pos.CENTER_RIGHT);
        
        headerColumnas.getChildren().addAll(colProyecto, colDescripcion, colAcciones);

        VBox listaContenedor = new VBox(0);
        listaContenedor.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 0 0 16 16;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-width: 0 1 1 1;" +
            "-fx-border-radius: 0 0 16 16;"
        );

        ScrollPane scroll = new ScrollPane(listaContenedor);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(420);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Definir actualizarLista
        Runnable actualizarLista = () -> {
            listaContenedor.getChildren().clear();
            java.util.List<AdminManager.Proyecto> proyectos = AdminManager.getProyectosActivos();
            for (int i = 0; i < proyectos.size(); i++) {
                AdminManager.Proyecto p = proyectos.get(i);
                listaContenedor.getChildren().add(crearItemProyectoEditableMejorado(p, i, dialog, listaContenedor));
            }
        };

        // Conexión de eventos de importación
        itemExcel.setOnAction(e -> mostrarDialogoImportarProyectoExcel(dialog, actualizarLista));
        itemCarpeta.setOnAction(e -> mostrarDialogoImportarMasivoExcel(dialog, actualizarLista));

        // Llenar lista inicial
        actualizarLista.run();
        
        listaSection.getChildren().addAll(headerColumnas, scroll);

        // Eventos
        btnNuevo.setOnAction(e -> mostrarDialogoProyecto(null, dialog, actualizarLista));

        btnLogout.setOnAction(e -> {
            AdminManager.logout();
            dialog.close();
            mostrarMenu(mainStage);
        });

        container.getChildren().addAll(headerSection, toolbar, listaSection);
        root.getChildren().add(container);
        
        // Click fuera para cerrar
        root.setOnMouseClicked(e -> {
            if (e.getTarget() == root) dialog.close();
        });

        Scene scene = new Scene(root,
                ScreenUtils.w(1050, 900),
                ScreenUtils.h(880, 700));
        scene.setFill(Color.TRANSPARENT);
        TemaManager.aplicarTema(scene);
        dialog.setScene(scene);
        dialog.centerOnScreen();

        // Animación de entrada
        container.setOpacity(0);
        container.setScaleX(0.92);
        container.setScaleY(0.92);
        FadeTransition fi = new FadeTransition(Duration.millis(280), container);
        fi.setToValue(1);
        ScaleTransition si = new ScaleTransition(Duration.millis(280), container);
        si.setToX(1);
        si.setToY(1);
        si.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        new ParallelTransition(fi, si).play();

        dialog.show();
    }
    
    private static VBox crearStatCardAdmin(String valor, String label, String color, javafx.scene.Node icono) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setStyle(
            "-fx-background-color: " + color + "15;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + color + "30;" +
            "-fx-border-radius: 16;"
        );
        
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(icono);
        
        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblValor.setTextFill(Color.web(color));
        header.getChildren().add(lblValor);
        
        Label lblLabel = new Label(label);
        lblLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblLabel.setTextFill(COLOR_TEXT_MUTED());
        
        card.getChildren().addAll(header, lblLabel);
        return card;
    }
    
    private static Button crearBotonToolbarGrande(String texto, String color) {
        Button btn = new Button(texto);
        btn.setPadding(new Insets(14, 24, 14, 24));
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: derive(" + color + ", -15%);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }
    
    private static HBox crearItemProyectoEditableMejorado(AdminManager.Proyecto proyecto, int index, Stage parentDialog, VBox listaContenedor) {
        HBox item = new HBox(20);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(20, 24, 20, 24));
        
        String color = proyecto.getColor() != null ? proyecto.getColor() : AdminManager.getColorProyecto(index);
        
        String bgColor = index % 2 == 0 ? TemaManager.getBgLight() : TemaManager.getSurface();
        item.setStyle("-fx-background-color: " + bgColor + ";");

        // Número e indicador de color
        HBox numIndicator = new HBox(16);
        numIndicator.setAlignment(Pos.CENTER_LEFT);
        numIndicator.setPrefWidth(320);
        
        Label numLabel = new Label(String.format("%02d", index + 1));
        numLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        numLabel.setTextFill(COLOR_TEXT_MUTED());
        numLabel.setPrefWidth(36);
        
        Rectangle colorIndicator = new Rectangle(6, 50);
        colorIndicator.setFill(Color.web(color));
        colorIndicator.setArcWidth(6);
        colorIndicator.setArcHeight(6);

        VBox infoBox = new VBox(6);
        Label nombre = new Label(proyecto.getNombre());
        nombre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        nombre.setTextFill(COLOR_TEXT());
        nombre.setMaxWidth(250);
        nombre.setWrapText(true);
        
        Label fechaLabel = new Label("Creado: " + (proyecto.getFechaCreacion() != null ? proyecto.getFechaCreacion() : "--"));
        fechaLabel.setFont(Font.font("Segoe UI", 12));
        fechaLabel.setTextFill(COLOR_TEXT_MUTED());
        
        infoBox.getChildren().addAll(nombre, fechaLabel);
        numIndicator.getChildren().addAll(numLabel, colorIndicator, infoBox);

        // Descripción
        VBox descBox = new VBox(6);
        HBox.setHgrow(descBox, Priority.ALWAYS);
        
        Label desc = new Label(proyecto.getDescripcion() != null && !proyecto.getDescripcion().isEmpty() 
            ? proyecto.getDescripcion() : "Sin descripción");
        desc.setFont(Font.font("Segoe UI", 15));
        desc.setTextFill(proyecto.getDescripcion() != null && !proyecto.getDescripcion().isEmpty() 
            ? COLOR_TEXT() : COLOR_TEXT_MUTED());
        desc.setWrapText(true);
        
        // Badge de color
        HBox colorBadge = new HBox(8);
        colorBadge.setAlignment(Pos.CENTER_LEFT);
        Circle colorDot = new Circle(7);
        colorDot.setFill(Color.web(color));
        colorDot.setStroke(Color.web(color + "60"));
        colorDot.setStrokeWidth(2);
        Label colorLabel = new Label(color);
        colorLabel.setFont(Font.font("Consolas", 11));
        colorLabel.setTextFill(COLOR_TEXT_MUTED());
        colorBadge.getChildren().addAll(colorDot, colorLabel);
        
        descBox.getChildren().addAll(desc, colorBadge);

        // Botones de acción mejorados
        HBox acciones = new HBox(10);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        acciones.setPrefWidth(180);

        Button btnExcel = crearBotonAccionAdmin(IconosSVG.excel("#10B981", 20), "#10B981", "Abrir Excel");
        btnExcel.setOnAction(e -> {
            String nombreFormateado = (index + 1) + ". " + proyecto.getNombre();
            Path rutaExcel = obtenerRutaExcel(nombreFormateado);
            if (!Files.exists(rutaExcel)) {
                DialogosFX.mostrarAlerta(parentDialog, "Archivo no encontrado",
                    "No existe inventario para este proyecto.\nGenera uno primero.", Alert.AlertType.WARNING);
                return;
            }
            try {
                java.awt.Desktop.getDesktop().open(rutaExcel.toFile());
            } catch (Exception ex) {
                DialogosFX.mostrarAlerta(parentDialog, "Error",
                    "No se pudo abrir.\n¿Tienes Excel instalado?\n\nEl archivo está protegido con contraseña.", Alert.AlertType.ERROR);
            }
        });

        Button btnEditar = crearBotonAccionAdmin(IconosSVG.editar("#3B82F6", 20), "#3B82F6", "Editar proyecto");
        btnEditar.setOnAction(e -> {
            Runnable actualizar = () -> {
                listaContenedor.getChildren().clear();
                java.util.List<AdminManager.Proyecto> proyectos = AdminManager.getProyectosActivos();
                for (int i = 0; i < proyectos.size(); i++) {
                    AdminManager.Proyecto p = proyectos.get(i);
                    listaContenedor.getChildren().add(crearItemProyectoEditableMejorado(p, i, parentDialog, listaContenedor));
                }
            };
            mostrarDialogoProyecto(proyecto, parentDialog, actualizar);
        });

        Button btnEliminar = crearBotonAccionAdmin(IconosSVG.papelera("#EF4444", 20), "#EF4444", "Eliminar proyecto");
        btnEliminar.setOnAction(e -> {
            // Buscar archivo Excel asociado
            int indiceProyecto = index + 1;
            String nombreProyecto = proyecto.getNombre();
            String nombreFormateado = sanitizarNombreArchivo(nombreProyecto);
            Path rutaExcel = null;
            
            try {
                Path carpeta = obtenerCarpetaEjecutable();
                java.util.Optional<Path> archivoEncontrado = Files.list(carpeta)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return fileName.startsWith("inventario_" + indiceProyecto + " -") && 
                               fileName.endsWith(".xlsx");
                    })
                    .findFirst();
                
                if (archivoEncontrado.isPresent()) {
                    rutaExcel = archivoEncontrado.get();
                }
            } catch (Exception ex) {
                System.err.println("[Eliminar] Error buscando archivo Excel: " + ex.getMessage());
            }
            
            String mensajeConfirmacion = "¿Estás seguro de eliminar \"" + proyecto.getNombre() + "\"?\n\n" +
                "Esta acción eliminará:\n" +
                "• El proyecto de la base de datos\n" +
                "• Todos los reportes de mantenimiento del proyecto\n" +
                "• Todos los borradores asociados\n" +
                (rutaExcel != null ? "• El archivo Excel: " + rutaExcel.getFileName() + "\n" : "") +
                "\nEsta acción no se puede deshacer.";
            
            boolean confirmar = DialogosFX.confirmarAccion(parentDialog, "Eliminar Proyecto", mensajeConfirmacion);
            if (confirmar) {
                // Eliminar archivo Excel si existe
                if (rutaExcel != null && Files.exists(rutaExcel)) {
                    try {
                        Files.delete(rutaExcel);
                        System.out.println("[Eliminar] ✅ Archivo Excel eliminado: " + rutaExcel);
                    } catch (Exception ex) {
                        System.err.println("[Eliminar] ❌ Error eliminando archivo Excel: " + ex.getMessage());
                        DialogosFX.mostrarAlerta(parentDialog, "Error al Eliminar Excel", 
                            "No se pudo eliminar el archivo Excel:\n" + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
                
                // Eliminar proyecto de la base de datos
                AdminManager.eliminarProyecto(proyecto.getId());
                
                // Actualizar lista
                listaContenedor.getChildren().clear();
                java.util.List<AdminManager.Proyecto> proyectos = AdminManager.getProyectosActivos();
                for (int i = 0; i < proyectos.size(); i++) {
                    AdminManager.Proyecto p = proyectos.get(i);
                    listaContenedor.getChildren().add(crearItemProyectoEditableMejorado(p, i, parentDialog, listaContenedor));
                }
            }
        });

        acciones.getChildren().addAll(btnExcel, btnEditar, btnEliminar);

        item.getChildren().addAll(numIndicator, descBox, acciones);

        // Hover mejorado
        String normalStyle = item.getStyle();
        item.setOnMouseEntered(e -> {
            item.setStyle(
                "-fx-background-color: " + color + "18;" +
                "-fx-border-color: " + color + "50;" +
                "-fx-border-width: 0 0 0 5;"
            );
            AnimacionesFX.hoverIn(item, 1.01, 200);
        });
        item.setOnMouseExited(e -> {
            item.setStyle(normalStyle);
            AnimacionesFX.hoverOut(item, 200);
        });

        return item;
    }
    
    private static Button crearBotonAccionAdmin(javafx.scene.Node icono, String color, String tooltip) {
        Button btn = new Button();
        btn.setGraphic(icono);
        btn.setPrefSize(46, 46);
        btn.setStyle(
            "-fx-background-color: " + color + "20;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + "40;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, " + color + "60, 8, 0, 0, 2);"
            );
            AnimacionesFX.hoverIn(btn, 1.15, 120);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + "20;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 120);
        });
        javafx.scene.control.Tooltip.install(btn, new javafx.scene.control.Tooltip(tooltip));
        return btn;
    }

    private static Button crearBotonToolbar(String texto, javafx.scene.Node icono, String color) {
        Button btn = new Button(texto);
        btn.setGraphic(icono);
        btn.setGraphicTextGap(8);
        btn.setPadding(new Insets(8, 16, 8, 14));
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        btn.setStyle(
            "-fx-background-color: " + color + "20;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + color + "35;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + "20;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }

    private static HBox crearItemProyectoEditable(AdminManager.Proyecto proyecto, int index, Stage parentDialog, VBox listaContenedor) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(14, 16, 14, 16));
        
        String color = proyecto.getColor() != null ? proyecto.getColor() : AdminManager.getColorProyecto(index);
        
        item.setStyle(
            "-fx-background-color: " + TemaManager.getBgLight() + ";" +
            "-fx-background-radius: 12;"
        );

        // Indicador de color
        Rectangle colorIndicator = new Rectangle(4, 36);
        colorIndicator.setFill(Color.web(color));
        colorIndicator.setArcWidth(4);
        colorIndicator.setArcHeight(4);

        // Info
        VBox infoBox = new VBox(2);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nombre = new Label(proyecto.getNombre());
        nombre.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        nombre.setTextFill(COLOR_TEXT());

        Label desc = new Label(proyecto.getDescripcion() != null ? proyecto.getDescripcion() : "Sin descripción");
        desc.setFont(Font.font("Segoe UI", 11));
        desc.setTextFill(COLOR_TEXT_MUTED());

        infoBox.getChildren().addAll(nombre, desc);

        // Botones de acción
        HBox acciones = new HBox(6);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        // Botón Abrir Excel
        Button btnExcel = new Button();
        btnExcel.setGraphic(IconosSVG.excel("#10B981", 16));
        btnExcel.setPrefSize(32, 32);
        btnExcel.setStyle(
            "-fx-background-color: #10B98120;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        javafx.scene.control.Tooltip.install(btnExcel, new javafx.scene.control.Tooltip("Abrir en Excel"));
        btnExcel.setOnAction(e -> {
            String nombreFormateado = (index + 1) + ". " + proyecto.getNombre();
            Path rutaExcel = obtenerRutaExcel(nombreFormateado);
            if (!Files.exists(rutaExcel)) {
                DialogosFX.mostrarAlerta(parentDialog, "Archivo no encontrado",
                    "No existe inventario para este proyecto.\nGenera uno primero.", Alert.AlertType.WARNING);
                return;
            }
            try {
                java.awt.Desktop.getDesktop().open(rutaExcel.toFile());
            } catch (Exception ex) {
                DialogosFX.mostrarAlerta(parentDialog, "Error",
                    "No se pudo abrir.\n¿Tienes Excel instalado?\n\nEl archivo está protegido con contraseña.", Alert.AlertType.ERROR);
            }
        });

        Button btnEditar = new Button();
        btnEditar.setGraphic(IconosSVG.editar("#3B82F6", 16));
        btnEditar.setPrefSize(32, 32);
        btnEditar.setStyle(
            "-fx-background-color: #3B82F620;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        javafx.scene.control.Tooltip.install(btnEditar, new javafx.scene.control.Tooltip("Editar proyecto"));
        btnEditar.setOnAction(e -> {
            Runnable actualizar = () -> {
                listaContenedor.getChildren().clear();
                java.util.List<AdminManager.Proyecto> proyectos = AdminManager.getProyectosActivos();
                for (int i = 0; i < proyectos.size(); i++) {
                    AdminManager.Proyecto p = proyectos.get(i);
                    listaContenedor.getChildren().add(crearItemProyectoEditable(p, i, parentDialog, listaContenedor));
                }
            };
            mostrarDialogoProyecto(proyecto, parentDialog, actualizar);
        });

        Button btnEliminar = new Button();
        btnEliminar.setGraphic(IconosSVG.papelera("#EF4444", 16));
        btnEliminar.setPrefSize(32, 32);
        btnEliminar.setStyle(
            "-fx-background-color: #EF444420;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        javafx.scene.control.Tooltip.install(btnEliminar, new javafx.scene.control.Tooltip("Eliminar proyecto"));
        btnEliminar.setOnAction(e -> {
            boolean confirmar = DialogosFX.confirmarAccion(parentDialog, "Eliminar Proyecto",
                "¿Estás seguro de eliminar \"" + proyecto.getNombre() + "\"?\n\n" +
                "Se eliminarán también todos los reportes y borradores asociados.\n\n" +
                "Esta acción no se puede deshacer.");
            if (confirmar) {
                AdminManager.eliminarProyecto(proyecto.getId());
                // Actualizar lista
                listaContenedor.getChildren().clear();
                java.util.List<AdminManager.Proyecto> proyectos = AdminManager.getProyectosActivos();
                for (int i = 0; i < proyectos.size(); i++) {
                    AdminManager.Proyecto p = proyectos.get(i);
                    listaContenedor.getChildren().add(crearItemProyectoEditable(p, i, parentDialog, listaContenedor));
                }
            }
        });

        acciones.getChildren().addAll(btnExcel, btnEditar, btnEliminar);

        item.getChildren().addAll(colorIndicator, infoBox, acciones);

        // Hover
        String normalStyle = item.getStyle();
        item.setOnMouseEntered(e -> item.setStyle(
            "-fx-background-color: " + TemaManager.getSurfaceHover() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + color + "40;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        ));
        item.setOnMouseExited(e -> item.setStyle(normalStyle));

        return item;
    }

    private static void mostrarDialogoProyecto(AdminManager.Proyecto proyectoExistente, Stage parentDialog, Runnable onSave) {
        boolean esNuevo = proyectoExistente == null;
        // IMPORTANTE: Crear una copia para editar, no modificar el original directamente
        AdminManager.Proyecto proyecto = esNuevo ? new AdminManager.Proyecto() : proyectoExistente.copy();

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initOwner(parentDialog);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        root.setPadding(new Insets(40));

        VBox container = new VBox(0);
        container.setAlignment(Pos.TOP_CENTER);
        container.setMaxWidth(580);
        container.setMaxHeight(680);
        container.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 24;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 24;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setRadius(50);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setOffsetY(15);
        container.setEffect(shadow);

        // ═══ HEADER ═══
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(32, 36, 24, 36));
        header.setStyle("-fx-background-color: " + TemaManager.getSurface() + "; -fx-background-radius: 24 24 0 0;");
        
        // Icono con color según acción
        StackPane iconCircle = new StackPane();
        Circle bgIcon = new Circle(28);
        bgIcon.setFill(Color.web(esNuevo ? "#10B98130" : "#3B82F630"));
        iconCircle.getChildren().addAll(bgIcon, esNuevo ? IconosSVG.agregar("#10B981", 28) : IconosSVG.editar("#3B82F6", 28));
        
        VBox titleBox = new VBox(4);
        Label titulo = new Label(esNuevo ? "Crear Nuevo Proyecto" : "Editar Proyecto");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titulo.setTextFill(COLOR_TEXT());
        
        Label subtitulo = new Label(esNuevo ? "Agrega un nuevo proyecto a tu lista" : "Modifica los datos del proyecto");
        subtitulo.setFont(Font.font("Segoe UI", 13));
        subtitulo.setTextFill(COLOR_TEXT_MUTED());
        titleBox.getChildren().addAll(titulo, subtitulo);
        
        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);
        
        Button btnCerrarX = inventario.fx.util.ComponentesFX.crearBotonCerrar(dialog::close, 40);
        
        header.getChildren().addAll(iconCircle, titleBox, spacerHeader, btnCerrarX);

        // ═══ CONTENIDO ═══
        VBox contenido = new VBox(24);
        contenido.setPadding(new Insets(28, 36, 28, 36));

        // Campo Nombre
        VBox campoNombre = new VBox(10);
        
        HBox labelNombre = new HBox(6);
        labelNombre.setAlignment(Pos.CENTER_LEFT);
        Label lblNombre = new Label("Nombre del Proyecto");
        lblNombre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblNombre.setTextFill(COLOR_TEXT());
        Label asterisco = new Label("*");
        asterisco.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        asterisco.setTextFill(Color.web("#EF4444"));
        labelNombre.getChildren().addAll(lblNombre, asterisco);
        
        TextField txtNombre = new TextField(proyecto.getNombre() != null ? proyecto.getNombre() : "");
        txtNombre.setPromptText("Ej: Secretaría de Educación");
        txtNombre.setPrefHeight(52);
        txtNombre.setFont(Font.font("Segoe UI", 15));
        txtNombre.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 14;" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-padding: 14 18 14 18;"
        );
        txtNombre.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                txtNombre.setStyle(
                    "-fx-background-color: " + TemaManager.getSurface() + ";" +
                    "-fx-background-radius: 14;" +
                    "-fx-border-color: #3B82F6;" +
                    "-fx-border-radius: 14;" +
                    "-fx-border-width: 2;" +
                    "-fx-text-fill: " + TemaManager.getText() + ";" +
                    "-fx-padding: 14 18 14 18;"
                );
            } else {
                txtNombre.setStyle(
                    "-fx-background-color: " + TemaManager.getSurface() + ";" +
                    "-fx-background-radius: 14;" +
                    "-fx-border-color: " + TemaManager.getBorder() + ";" +
                    "-fx-border-radius: 14;" +
                    "-fx-text-fill: " + TemaManager.getText() + ";" +
                    "-fx-padding: 14 18 14 18;"
                );
            }
        });
        campoNombre.getChildren().addAll(labelNombre, txtNombre);

        // Campo Descripción
        VBox campoDesc = new VBox(10);
        Label lblDesc = new Label("Descripción");
        lblDesc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblDesc.setTextFill(COLOR_TEXT());
        
        TextField txtDesc = new TextField(proyecto.getDescripcion() != null ? proyecto.getDescripcion() : "");
        txtDesc.setPromptText("Ej: Mesa de servicios, Outsourcing TI...");
        txtDesc.setPrefHeight(52);
        txtDesc.setFont(Font.font("Segoe UI", 15));
        txtDesc.setStyle(txtNombre.getStyle());
        txtDesc.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                txtDesc.setStyle(
                    "-fx-background-color: " + TemaManager.getSurface() + ";" +
                    "-fx-background-radius: 14;" +
                    "-fx-border-color: #3B82F6;" +
                    "-fx-border-radius: 14;" +
                    "-fx-border-width: 2;" +
                    "-fx-text-fill: " + TemaManager.getText() + ";" +
                    "-fx-padding: 14 18 14 18;"
                );
            } else {
                txtDesc.setStyle(
                    "-fx-background-color: " + TemaManager.getSurface() + ";" +
                    "-fx-background-radius: 14;" +
                    "-fx-border-color: " + TemaManager.getBorder() + ";" +
                    "-fx-border-radius: 14;" +
                    "-fx-text-fill: " + TemaManager.getText() + ";" +
                    "-fx-padding: 14 18 14 18;"
                );
            }
        });
        campoDesc.getChildren().addAll(lblDesc, txtDesc);

        // Campo Imagen
        VBox campoImagen = new VBox(10);
        Label lblImagen = new Label("Logo/Imagen del Proyecto");
        lblImagen.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblImagen.setTextFill(COLOR_TEXT());
        
        HBox selectorImagen = new HBox(12);
        selectorImagen.setAlignment(Pos.CENTER_LEFT);
        selectorImagen.setPrefHeight(52);
        selectorImagen.setPadding(new Insets(10, 14, 10, 14));
        selectorImagen.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 14;"
        );
        
        Label lblRutaImagen = new Label(proyecto.getImagenPath() != null && !proyecto.getImagenPath().isEmpty() 
            ? proyecto.getImagenPath() : "No se ha seleccionado ninguna imagen");
        lblRutaImagen.setFont(Font.font("Segoe UI", 13));
        lblRutaImagen.setTextFill(proyecto.getImagenPath() != null && !proyecto.getImagenPath().isEmpty() 
            ? COLOR_TEXT() : COLOR_TEXT_MUTED());
        lblRutaImagen.setMaxWidth(350);
        
        Region spacerImagen = new Region();
        HBox.setHgrow(spacerImagen, Priority.ALWAYS);
        
        Button btnSeleccionarImagen = new Button("Seleccionar");
        btnSeleccionarImagen.setPrefHeight(36);
        btnSeleccionarImagen.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnSeleccionarImagen.setStyle(
            "-fx-background-color: #3B82F6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        btnSeleccionarImagen.setOnMouseEntered(e -> btnSeleccionarImagen.setStyle(
            "-fx-background-color: #2563EB;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        ));
        btnSeleccionarImagen.setOnMouseExited(e -> btnSeleccionarImagen.setStyle(
            "-fx-background-color: #3B82F6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        ));
        
        btnSeleccionarImagen.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Seleccionar Imagen del Proyecto");
            fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new javafx.stage.FileChooser.ExtensionFilter("Todos los archivos", "*.*")
            );
            java.io.File archivoSeleccionado = fileChooser.showOpenDialog(dialog);
            if (archivoSeleccionado != null) {
                proyecto.setImagenPath(archivoSeleccionado.getAbsolutePath());
                lblRutaImagen.setText(archivoSeleccionado.getAbsolutePath());
                lblRutaImagen.setTextFill(COLOR_TEXT());
            }
        });
        
        selectorImagen.getChildren().addAll(lblRutaImagen, spacerImagen, btnSeleccionarImagen);
        campoImagen.getChildren().addAll(lblImagen, selectorImagen);

        // Selector de color mejorado
        VBox campoColor = new VBox(12);
        
        HBox labelColor = new HBox(12);
        labelColor.setAlignment(Pos.CENTER_LEFT);
        Label lblColor = new Label("Color del Proyecto");
        lblColor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblColor.setTextFill(COLOR_TEXT());
        
        // Preview del color seleccionado
        Circle previewColor = new Circle(12);
        String colorActual = proyecto.getColor() != null ? proyecto.getColor() : AdminManager.getSiguienteColor();
        previewColor.setFill(Color.web(colorActual));
        previewColor.setStroke(Color.web(colorActual + "60"));
        previewColor.setStrokeWidth(3);
        
        Label lblColorHex = new Label(colorActual);
        lblColorHex.setFont(Font.font("Consolas", 12));
        lblColorHex.setTextFill(COLOR_TEXT_MUTED());
        
        labelColor.getChildren().addAll(lblColor, previewColor, lblColorHex);
        
        final String[] colorSeleccionado = {colorActual};

        // Grid de colores más grande
        VBox coloresContainer = new VBox(10);
        coloresContainer.setPadding(new Insets(16));
        coloresContainer.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 14;"
        );
        
        FlowPane coloresGrid = new FlowPane(12, 12);
        coloresGrid.setAlignment(Pos.CENTER);
        
        java.util.List<Circle> todosCirculos = new java.util.ArrayList<>();
        
        Runnable actualizarSeleccion = () -> {
            for (Circle ci : todosCirculos) {
                String colorCirculo = "#" + ci.getFill().toString().substring(2, 8).toUpperCase();
                boolean seleccionado = colorCirculo.equalsIgnoreCase(colorSeleccionado[0]);
                ci.setStroke(seleccionado ? Color.WHITE : Color.TRANSPARENT);
                ci.setScaleX(seleccionado ? 1.2 : 1.0);
                ci.setScaleY(seleccionado ? 1.2 : 1.0);
                if (seleccionado) {
                    ci.setEffect(new javafx.scene.effect.DropShadow(8, Color.web(colorCirculo)));
                } else {
                    ci.setEffect(null);
                }
            }
            previewColor.setFill(Color.web(colorSeleccionado[0]));
            previewColor.setStroke(Color.web(colorSeleccionado[0] + "60"));
            lblColorHex.setText(colorSeleccionado[0]);
        };

        String[] colores = AdminManager.COLORES_PROYECTO;
        for (String c : colores) {
            Circle colorCircle = new Circle(18);
            colorCircle.setFill(Color.web(c));
            colorCircle.setStroke(c.equalsIgnoreCase(colorActual) ? Color.WHITE : Color.TRANSPARENT);
            colorCircle.setStrokeWidth(3);
            colorCircle.setCursor(javafx.scene.Cursor.HAND);
            if (c.equalsIgnoreCase(colorActual)) {
                colorCircle.setScaleX(1.2);
                colorCircle.setScaleY(1.2);
                colorCircle.setEffect(new javafx.scene.effect.DropShadow(8, Color.web(c)));
            }
            
            todosCirculos.add(colorCircle);
            
            colorCircle.setOnMouseClicked(e -> {
                colorSeleccionado[0] = c;
                actualizarSeleccion.run();
            });
            
            colorCircle.setOnMouseEntered(e -> {
                if (!c.equalsIgnoreCase(colorSeleccionado[0])) {
                    AnimacionesFX.hoverIn(colorCircle, 1.1, 120);
                }
            });
            colorCircle.setOnMouseExited(e -> {
                if (!c.equalsIgnoreCase(colorSeleccionado[0])) {
                    AnimacionesFX.hoverOut(colorCircle, 120);
                }
            });
            
            coloresGrid.getChildren().add(colorCircle);
        }
        
        // Botón de color personalizado
        StackPane btnColorCustom = new StackPane();
        btnColorCustom.setPrefSize(36, 36);
        btnColorCustom.setStyle(
            "-fx-background-color: linear-gradient(to right, #FF0000, #FFFF00, #00FF00, #00FFFF, #0000FF, #FF00FF);" +
            "-fx-background-radius: 18;" +
            "-fx-cursor: hand;"
        );
        Label plusLabel = new Label("+");
        plusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        plusLabel.setTextFill(Color.WHITE);
        plusLabel.setEffect(new javafx.scene.effect.DropShadow(3, Color.BLACK));
        btnColorCustom.getChildren().add(plusLabel);
        
        javafx.scene.control.Tooltip.install(btnColorCustom, new javafx.scene.control.Tooltip("Elegir color personalizado"));
        
        btnColorCustom.setOnMouseClicked(e -> {
            javafx.scene.control.ColorPicker picker = new javafx.scene.control.ColorPicker(Color.web(colorSeleccionado[0]));
            picker.setStyle("-fx-color-label-visible: false;");
            
            javafx.scene.control.Dialog<Color> colorDialog = new javafx.scene.control.Dialog<>();
            colorDialog.setTitle("Seleccionar Color Personalizado");
            colorDialog.initOwner(dialog);
            colorDialog.initStyle(javafx.stage.StageStyle.UTILITY);
            
            VBox dialogContent = new VBox(20);
            dialogContent.setPadding(new Insets(24));
            dialogContent.setAlignment(Pos.CENTER);
            dialogContent.setStyle("-fx-background-color: " + TemaManager.getBg() + ";");
            
            Label lblTitulo = new Label("Elige tu color personalizado");
            lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            lblTitulo.setTextFill(COLOR_TEXT());
            
            Circle preview = new Circle(40);
            preview.setFill(Color.web(colorSeleccionado[0]));
            preview.setStroke(Color.WHITE);
            preview.setStrokeWidth(4);
            preview.setEffect(new javafx.scene.effect.DropShadow(10, Color.web(colorSeleccionado[0])));
            
            picker.setOnAction(ev -> {
                preview.setFill(picker.getValue());
                preview.setEffect(new javafx.scene.effect.DropShadow(10, picker.getValue()));
            });
            
            dialogContent.getChildren().addAll(lblTitulo, preview, picker);
            
            colorDialog.getDialogPane().setContent(dialogContent);
            colorDialog.getDialogPane().getButtonTypes().addAll(
                javafx.scene.control.ButtonType.OK, 
                javafx.scene.control.ButtonType.CANCEL
            );
            
            colorDialog.setResultConverter(btn -> {
                if (btn == javafx.scene.control.ButtonType.OK) {
                    return picker.getValue();
                }
                return null;
            });
            
            colorDialog.showAndWait().ifPresent(color -> {
                String hex = String.format("#%02X%02X%02X",
                    (int)(color.getRed() * 255),
                    (int)(color.getGreen() * 255),
                    (int)(color.getBlue() * 255));
                colorSeleccionado[0] = hex;
                for (Circle ci : todosCirculos) {
                    ci.setStroke(Color.TRANSPARENT);
                    ci.setScaleX(1.0);
                    ci.setScaleY(1.0);
                    ci.setEffect(null);
                }
                previewColor.setFill(Color.web(hex));
                previewColor.setStroke(Color.web(hex + "60"));
                lblColorHex.setText(hex);
            });
        });
        
        coloresGrid.getChildren().add(btnColorCustom);
        coloresContainer.getChildren().add(coloresGrid);
        campoColor.getChildren().addAll(labelColor, coloresContainer);

        contenido.getChildren().addAll(campoNombre, campoDesc, campoImagen, campoColor);

        // ═══ BOTONES ═══
        HBox botones = new HBox(16);
        botones.setAlignment(Pos.CENTER_RIGHT);
        botones.setPadding(new Insets(20, 36, 28, 36));
        botones.setStyle("-fx-border-color: " + TemaManager.getBorder() + "; -fx-border-width: 1 0 0 0;");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(140);
        btnCancelar.setPrefHeight(48);
        btnCancelar.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        btnCancelar.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );
        btnCancelar.setOnMouseEntered(e -> btnCancelar.setStyle(
            "-fx-background-color: " + TemaManager.getSurfaceHover() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        ));
        btnCancelar.setOnMouseExited(e -> btnCancelar.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        ));
        btnCancelar.setOnAction(e -> dialog.close());

        Button btnGuardar = new Button(esNuevo ? "Crear Proyecto" : "Guardar Cambios");
        btnGuardar.setPrefWidth(180);
        btnGuardar.setPrefHeight(48);
        btnGuardar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnGuardar.setStyle(
            "-fx-background-color: #10B981;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle(
            "-fx-background-color: #059669;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        ));
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle(
            "-fx-background-color: #10B981;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        ));
        
        // Label de error
        Label lblError = new Label();
        lblError.setFont(Font.font("Segoe UI", 12));
        lblError.setTextFill(Color.web("#EF4444"));
        lblError.setVisible(false);
        
        // Guardar nombre original para detectar cambios al actualizar (evita pérdida de datos)
        final String nombreOriginal = proyecto.getNombre();
        
        btnGuardar.setOnAction(e -> {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                txtNombre.setStyle(
                    "-fx-background-color: " + TemaManager.getSurface() + ";" +
                    "-fx-background-radius: 14;" +
                    "-fx-border-color: #EF4444;" +
                    "-fx-border-radius: 14;" +
                    "-fx-border-width: 2;" +
                    "-fx-text-fill: " + TemaManager.getText() + ";" +
                    "-fx-padding: 14 18 14 18;"
                );
                lblError.setText("El nombre del proyecto es obligatorio");
                lblError.setVisible(true);
                return;
            }

            proyecto.setNombre(nombre);
            proyecto.setDescripcion(txtDesc.getText().trim());
            proyecto.setColor(colorSeleccionado[0]);
            // Nota: imagenPath ya se actualiza en tiempo real al seleccionar imagen

            boolean guardadoExitoso = false;
            if (esNuevo) {
                guardadoExitoso = AdminManager.agregarProyecto(proyecto);
            } else {
                // Pasar el nombre original para renombrar correctamente el archivo Excel
                guardadoExitoso = AdminManager.actualizarProyectoConNombreAnterior(proyecto, nombreOriginal);
            }

            if (guardadoExitoso) {
                dialog.close();
                if (onSave != null) {
                    onSave.run();
                }
            } else {
                lblError.setText("❌ Error al guardar el proyecto. Verifica la conexión a la base de datos.");
                lblError.setVisible(true);
            }
        });

        Region spacerBtn = new Region();
        HBox.setHgrow(spacerBtn, Priority.ALWAYS);
        
        botones.getChildren().addAll(lblError, spacerBtn, btnCancelar, btnGuardar);

        container.getChildren().addAll(header, contenido, botones);
        root.getChildren().add(container);
        
        // Click fuera para cerrar
        root.setOnMouseClicked(e -> {
            if (e.getTarget() == root) dialog.close();
        });

        Scene scene = new Scene(root,
                ScreenUtils.w(680, 500),
                ScreenUtils.h(750, 600));
        scene.setFill(Color.TRANSPARENT);
        TemaManager.aplicarTema(scene);
        dialog.setScene(scene);
        dialog.centerOnScreen();

        // Animación
        container.setOpacity(0);
        container.setScaleX(0.9);
        container.setScaleY(0.9);
        FadeTransition fi = new FadeTransition(Duration.millis(250), container);
        fi.setToValue(1);
        ScaleTransition si = new ScaleTransition(Duration.millis(250), container);
        si.setToX(1);
        si.setToY(1);
        si.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        new ParallelTransition(fi, si).play();

        dialog.show();
        txtNombre.requestFocus();
    }

    private static void abrirPanelConAccion(String accion) {
        pendingAction = accion;
        abrirPanelLateral();
    }

    private static void abrirPanelLateral() {
        if (sidePanelOpen) return;
        sidePanelOpen = true;

        Pane overlay = (Pane) sidePanel.getUserData();
        overlay.setVisible(true);
        overlay.setMouseTransparent(false); // Permitir clics en el overlay para cerrarlo
        sidePanel.setMouseTransparent(false); // Permitir clics en el panel

        // Animación del overlay
        FadeTransition fadeOverlay = new FadeTransition(Duration.millis(200), overlay);
        fadeOverlay.setFromValue(0);
        fadeOverlay.setToValue(1);

        // Animación del panel
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), sidePanel);
        slideIn.setFromX(400);
        slideIn.setToX(0);
        slideIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);

        new ParallelTransition(fadeOverlay, slideIn).play();
    }

    private static void cerrarPanelLateral() {
        if (!sidePanelOpen) return;
        sidePanelOpen = false;

        Pane overlay = (Pane) sidePanel.getUserData();

        FadeTransition fadeOverlay = new FadeTransition(Duration.millis(200), overlay);
        fadeOverlay.setFromValue(1);
        fadeOverlay.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), sidePanel);
        slideOut.setFromX(0);
        slideOut.setToX(400);
        slideOut.setInterpolator(AnimacionesFX.EASE_IN_CUBIC);

        ParallelTransition anim = new ParallelTransition(fadeOverlay, slideOut);
        anim.setOnFinished(e -> {
            overlay.setVisible(false);
            overlay.setMouseTransparent(true); // No interceptar clics cuando está oculto
            sidePanel.setMouseTransparent(true); // No interceptar clics cuando está oculto
        });
        anim.play();
    }

    private static void ejecutarAccionPendiente() {
        if (pendingAction == null) return;

        String accion = pendingAction;
        pendingAction = null;

        Platform.runLater(() -> {
            switch (accion) {
                case "generar":
                    // Verificar si el archivo Excel está bloqueado antes de generar
                    Path rutaExcel = obtenerRutaExcel(CURRENT_PROJECT);
                    if (Files.exists(rutaExcel) && isFileLocked(rutaExcel)) {
                        // Obtener solo el nombre del archivo
                        String nombreArchivo = rutaExcel.getFileName().toString();
                        
                        // Mostrar advertencia con cuenta regresiva de 35 segundos
                        boolean archivoLiberado = DialogosFX.mostrarAdvertenciaExcelAbierto(
                            mainStage,
                            nombreArchivo,
                            35, 
                            () -> !isFileLocked(rutaExcel)
                        );
                        
                        if (!archivoLiberado) {
                            // El usuario canceló o no cerró el archivo a tiempo
                            return;
                        }
                    }
                    
                    mainStage.hide();
                    mostrarPantallaCarga("Generando inventario...", () -> mainStage.show());
                    break;
                case "dashboard":
                    mainStage.hide();
                    DashboardFX.mostrarDashboard(mainStage);
                    break;
                case "excel":
                    abrirEnExcel();
                    break;
                case "limpiar":
                    limpiarDatos();
                    break;
            }
        });
    }

    private static void abrirEnExcel() {
        Path rutaExcel = obtenerRutaExcel(CURRENT_PROJECT);
        if (!Files.exists(rutaExcel)) {
            DialogosFX.mostrarAlerta(mainStage, "Archivo no encontrado",
                "No existe el archivo.\nGenera un inventario primero.", Alert.AlertType.WARNING);
            return;
        }
        try {
            java.awt.Desktop.getDesktop().open(rutaExcel.toFile());
        } catch (Exception ex) {
            DialogosFX.mostrarAlerta(mainStage, "Error",
                "No se pudo abrir.\n¿Tienes Excel instalado?\n\nEl archivo está protegido con contraseña.", Alert.AlertType.ERROR);
        }
    }

    private static void limpiarDatos() {
        Path rutaExcel = obtenerRutaExcel(CURRENT_PROJECT);
        if (!Files.exists(rutaExcel)) {
            DialogosFX.mostrarAlerta(mainStage, "Vacío",
                "El inventario ya está vacío.", Alert.AlertType.INFORMATION);
            return;
        }

        boolean confirmar = DialogosFX.confirmarAccion(mainStage, "Confirmar eliminación",
            "¿Eliminar permanentemente el inventario de\n" + limpiarNombreProyecto(CURRENT_PROJECT) + "?");

        if (!confirmar) return;

        try {
            Files.deleteIfExists(rutaExcel);
            DialogosFX.mostrarAlerta(mainStage, "Eliminado",
                "Inventario eliminado correctamente.", Alert.AlertType.CONFIRMATION);
        } catch (Exception ex) {
            DialogosFX.mostrarAlerta(mainStage, "Error",
                "No se pudo eliminar. ¿Está abierto en Excel?", Alert.AlertType.ERROR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // COMPONENTES AUXILIARES
    // ════════════════════════════════════════════════════════════════════════════

    private static VBox crearMiniCard(String titulo, String valor, String icono) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16, 24, 16, 24));
        card.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 12;"
        );

        Label iconLabel = new Label(icono);
        iconLabel.setFont(Font.font(20));

        Label valorLabel = new Label(valor);
        valorLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        valorLabel.setTextFill(COLOR_TEXT());

        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Segoe UI", 11));
        tituloLabel.setTextFill(COLOR_TEXT_MUTED());

        card.getChildren().addAll(iconLabel, valorLabel, tituloLabel);
        return card;
    }

    private static Button crearBotonSidebar(String texto, String icono, boolean activo) {
        Button btn = new Button(icono + "  " + texto);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setPrefHeight(44);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 16, 0, 16));
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));

        String baseStyle = activo ?
            "-fx-background-color: #E6394620;" +
            "-fx-text-fill: #E63946;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" :
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TemaManager.getTextMuted() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;";

        btn.setStyle(baseStyle);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + TemaManager.getSurface() + ";" +
                "-fx-text-fill: " + TemaManager.getText() + ";" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.03, 150);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
            AnimacionesFX.hoverOut(btn, 150);
        });

        return btn;
    }

    private static Button crearBotonSidebarSVG(String texto, javafx.scene.Node icono, boolean activo) {
        return ComponentesFX.crearSidebarButton(texto, icono, activo);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // BOTÓN CIRCULAR ANIMADO PARA CAMBIO DE TEMA CON EXPANSIÓN CIRCULAR
    // ════════════════════════════════════════════════════════════════════════════

    private static StackPane crearToggleTema() {
        // Dimensiones del botón circular
        double buttonSize = 44;
        
        boolean isDark = TemaManager.isDarkMode();
        
        // Contenedor principal del botón
        StackPane button = new StackPane();
        button.setPrefSize(buttonSize, buttonSize);
        button.setMaxSize(buttonSize, buttonSize);
        button.setCursor(javafx.scene.Cursor.HAND);
        
        // Fondo circular del botón
        Circle bgCircle = new Circle(buttonSize / 2);
        bgCircle.setFill(Color.web(TemaManager.getBorderLight()));
        
        // Sombra sutil
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.web("#000000", TemaManager.isDarkMode() ? 0.4 : 0.15));
        shadow.setOffsetY(2);
        bgCircle.setEffect(shadow);
        
        // Contenedor de iconos (para rotación)
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(buttonSize, buttonSize);
        
        // Icono del sol
        javafx.scene.Group sunIcon = crearIconoSolMinimalista();
        sunIcon.setOpacity(isDark ? 0 : 1);
        sunIcon.setScaleX(isDark ? 0.5 : 1);
        sunIcon.setScaleY(isDark ? 0.5 : 1);
        
        // Icono de la luna  
        javafx.scene.Group moonIcon = crearIconoLunaMinimalista(isDark);
        moonIcon.setOpacity(isDark ? 1 : 0);
        moonIcon.setScaleX(isDark ? 1 : 0.5);
        moonIcon.setScaleY(isDark ? 1 : 0.5);
        
        iconContainer.getChildren().addAll(sunIcon, moonIcon);
        
        button.getChildren().addAll(bgCircle, iconContainer);
        
        // Animación al hacer clic
        button.setOnMouseClicked(e -> {
            boolean willBeDark = !TemaManager.isDarkMode();
            
            // Obtener posición del botón en la escena para centrar la expansión
            javafx.geometry.Bounds buttonBounds = button.localToScene(button.getBoundsInLocal());
            double centerX = buttonBounds.getCenterX();
            double centerY = buttonBounds.getCenterY();
            
            // Calcular el radio máximo necesario para cubrir toda la pantalla
            double sceneWidth = rootStack.getScene().getWidth();
            double sceneHeight = rootStack.getScene().getHeight();
            double maxRadius = Math.sqrt(
                Math.pow(Math.max(centerX, sceneWidth - centerX), 2) +
                Math.pow(Math.max(centerY, sceneHeight - centerY), 2)
            ) + 50; // Margen extra
            
            // Crear el círculo de expansión
            Circle expandCircle = new Circle(0);
            // Color del futuro tema (antes de toggleTheme): coincide con TemaManager.getBgLight()
            expandCircle.setFill(Color.web(willBeDark ? "#1A1A1A" : "#FAFAFA"));
            expandCircle.setTranslateX(centerX - sceneWidth / 2);
            expandCircle.setTranslateY(centerY - sceneHeight / 2);
            expandCircle.setMouseTransparent(true);
            
            // Agregar el círculo al rootStack
            rootStack.getChildren().add(expandCircle);
            
            // Duraciones
            Duration expandDuration = Duration.millis(500);
            Duration iconDuration = Duration.millis(300);
            
            // Animación de expansión del círculo
            Timeline expandAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(expandCircle.radiusProperty(), 0)),
                new KeyFrame(expandDuration,
                    new KeyValue(expandCircle.radiusProperty(), maxRadius, AnimacionesFX.EASE_OUT_CUBIC))
            );
            
            // Animación de rotación del contenedor de iconos
            RotateTransition rotateTransition = new RotateTransition(iconDuration, iconContainer);
            rotateTransition.setByAngle(willBeDark ? 180 : -180);
            rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
            
            // Animación del color de fondo del botón
            Timeline bgColorAnim = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(bgCircle.fillProperty(), bgCircle.getFill())),
                new KeyFrame(iconDuration, 
                    // Color del futuro tema (antes de toggleTheme): coincide con TemaManager.getBorderLight()
                    new KeyValue(bgCircle.fillProperty(), Color.web(willBeDark ? "#3A3A3A" : "#F0F0F0"), Interpolator.EASE_BOTH))
            );
            
            // Animación de fade y scale para el sol
            Timeline sunAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(sunIcon.opacityProperty(), sunIcon.getOpacity()),
                    new KeyValue(sunIcon.scaleXProperty(), sunIcon.getScaleX()),
                    new KeyValue(sunIcon.scaleYProperty(), sunIcon.getScaleY())),
                new KeyFrame(iconDuration,
                    new KeyValue(sunIcon.opacityProperty(), willBeDark ? 0 : 1, Interpolator.EASE_BOTH),
                    new KeyValue(sunIcon.scaleXProperty(), willBeDark ? 0.5 : 1, Interpolator.EASE_BOTH),
                    new KeyValue(sunIcon.scaleYProperty(), willBeDark ? 0.5 : 1, Interpolator.EASE_BOTH))
            );
            
            // Animación de fade y scale para la luna
            Timeline moonAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(moonIcon.opacityProperty(), moonIcon.getOpacity()),
                    new KeyValue(moonIcon.scaleXProperty(), moonIcon.getScaleX()),
                    new KeyValue(moonIcon.scaleYProperty(), moonIcon.getScaleY())),
                new KeyFrame(iconDuration,
                    new KeyValue(moonIcon.opacityProperty(), willBeDark ? 1 : 0, Interpolator.EASE_BOTH),
                    new KeyValue(moonIcon.scaleXProperty(), willBeDark ? 1 : 0.5, Interpolator.EASE_BOTH),
                    new KeyValue(moonIcon.scaleYProperty(), willBeDark ? 1 : 0.5, Interpolator.EASE_BOTH))
            );
            
            // Animaciones de iconos en paralelo
            ParallelTransition iconAnimations = new ParallelTransition(
                rotateTransition, bgColorAnim, sunAnim, moonAnim
            );
            
            // Cuando la expansión termina, cambiar el tema MIENTRAS el círculo aún cubre todo
            expandAnim.setOnFinished(ev -> {
                // Cambiar el tema primero (mientras el círculo cubre la pantalla)
                TemaManager.toggleTheme();
                
                // Limpiar todos los elementos excepto el círculo de expansión
                // El círculo está al final de la lista
                int circleIndex = rootStack.getChildren().indexOf(expandCircle);
                rootStack.getChildren().clear();
                rootStack.getChildren().add(expandCircle); // Mantener el círculo al frente temporalmente
                
                // Reconstruir la UI con el nuevo tema ANTES de remover el círculo
                BorderPane newMainContent = new BorderPane();
                newMainContent.setStyle("-fx-background-color: " + TemaManager.getBgDark() + ";");
                newMainContent.setLeft(crearSidebarMenu());
                newMainContent.setCenter(crearContenidoCentral());
                
                // Insertar el contenido principal al inicio
                rootStack.getChildren().add(0, newMainContent);
                
                // Crear nuevo toggle de tema
                StackPane nuevoToggle = crearToggleTema();
                StackPane.setAlignment(nuevoToggle, Pos.TOP_RIGHT);
                StackPane.setMargin(nuevoToggle, new Insets(12, 16, 0, 0));
                rootStack.getChildren().add(1, nuevoToggle);
                
                // Recrear el panel lateral de proyectos
                crearPanelLateralProyectos();
                
                // Aplicar el nuevo tema a la escena
                TemaManager.aplicarTema(rootStack.getScene());
                
                // El círculo ahora está al final, hacer fade out para revelar la nueva UI
                Platform.runLater(() -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), expandCircle);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
                    fadeOut.setOnFinished(ev2 -> {
                        rootStack.getChildren().remove(expandCircle);
                    });
                    fadeOut.play();
                });
            });
            
            // Ejecutar animaciones
            iconAnimations.play();
            expandAnim.play();
        });
        
        // Hover effect sutil
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), button);
            scaleUp.setToX(1.08);
            scaleUp.setToY(1.08);
            scaleUp.play();
        });
        
        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), button);
            scaleDown.setToX(1);
            scaleDown.setToY(1);
            scaleDown.play();
        });
        
        // Contenedor posicionado - IMPORTANTE: pickOnBounds=false para no bloquear clics
        StackPane container = new StackPane(button);
        container.setAlignment(Pos.TOP_RIGHT);
        container.setPadding(new Insets(0));
        container.setPickOnBounds(false); // Solo el botón recibe clics, no todo el contenedor
        
        return container;
    }
    
    private static javafx.scene.Group crearIconoSolMinimalista() {
        javafx.scene.Group sun = new javafx.scene.Group();
        
        // Centro del sol (círculo gris)
        Circle center = new Circle(5);
        center.setFill(TemaManager.getTextMutedColor());
        
        // Rayos del sol (8 rayos)
        for (int i = 0; i < 8; i++) {
            double angle = i * 45;
            double startRadius = 8;
            double endRadius = 12;
            
            double startX = Math.cos(Math.toRadians(angle)) * startRadius;
            double startY = Math.sin(Math.toRadians(angle)) * startRadius;
            double endX = Math.cos(Math.toRadians(angle)) * endRadius;
            double endY = Math.sin(Math.toRadians(angle)) * endRadius;
            
            javafx.scene.shape.Line ray = new javafx.scene.shape.Line(startX, startY, endX, endY);
            ray.setStroke(TemaManager.getTextMutedColor());
            ray.setStrokeWidth(2);
            ray.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            
            sun.getChildren().add(ray);
        }
        
        sun.getChildren().add(center);
        return sun;
    }
    
    private static javafx.scene.Group crearIconoLunaMinimalista(boolean isDark) {
        javafx.scene.Group moon = new javafx.scene.Group();
        
        // Luna creciente - círculo principal
        Circle mainCircle = new Circle(10);
        mainCircle.setFill(TemaManager.getTextSecondaryColor());
        
        // Círculo para crear el efecto de luna creciente (recorte visual)
        Circle cutoutCircle = new Circle(8);
        cutoutCircle.setFill(Color.web(TemaManager.getBorderLight()));
        cutoutCircle.setTranslateX(5);
        cutoutCircle.setTranslateY(-4);
        
        moon.getChildren().addAll(mainCircle, cutoutCircle);
        return moon;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PANTALLA DE CARGA
    // ════════════════════════════════════════════════════════════════════════════

    public static void mostrarPantallaCarga(String mensaje, Runnable alCompletar) {
        loadingStage = new Stage(StageStyle.TRANSPARENT);
        loadingStage.initOwner(mainStage);   // agrupar con la ventana principal en taskbar
        aplicarIconoApp(loadingStage);       // ícono SELCOMP en lugar de Java
        loadingStage.setWidth(400);
        loadingStage.setHeight(240);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(20));

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 40, 36, 40));
        card.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 20;"
        );

        StackPane spinnerContainer = crearSpinnerModerno();

        Label lblEstado = new Label("Iniciando...");
        lblEstado.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        lblEstado.setTextFill(COLOR_TEXT());

        // Barra de progreso real
        javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(0);
        progressBar.setPrefWidth(280);
        progressBar.setPrefHeight(6);
        progressBar.setStyle(
            "-fx-accent: " + COLOR_PRIMARY + ";" +
            "-fx-background-radius: 3;" +
            "-fx-background-color: " + TemaManager.getBorder() + ";"
        );

        Label lblDetalle = new Label("Esto puede tomar unos segundos");
        lblDetalle.setFont(Font.font("Segoe UI", 12));
        lblDetalle.setTextFill(COLOR_TEXT_MUTED());

        card.getChildren().addAll(spinnerContainer, lblEstado, progressBar, lblDetalle);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(40);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        card.setEffect(shadow);

        root.getChildren().add(card);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        loadingStage.setScene(scene);
        loadingStage.centerOnScreen();
        loadingStage.show();

        if (mensaje.contains("Generando")) {
            final InfoPC[] infoHolder = new InfoPC[1];
            // Total de pasos: 10 en recopilarInfo + 2 en guardar = 12
            final int TOTAL_PASOS = 12;
            final int[] pasoActual = {0};

            Task<Boolean> taskGenerar = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    // Recopilar info con progreso real
                    java.util.function.Consumer<String> progreso = (msg) -> {
                        pasoActual[0]++;
                        final int paso = pasoActual[0];
                        updateMessage(msg);
                        updateProgress(paso, TOTAL_PASOS);
                    };
                    
                    infoHolder[0] = recopilarInfoConProgreso(progreso);
                    
                    // Fase de guardado
                    updateMessage("Escribiendo datos en Excel...");
                    updateProgress(11, TOTAL_PASOS);
                    
                    boolean resultado = guardarEnExcelCifradoProyecto(infoHolder[0]);
                    
                    updateMessage("Cifrando y guardando archivo...");
                    updateProgress(12, TOTAL_PASOS);
                    
                    return resultado;
                }
            };

            // Vincular la UI al progreso real del Task
            lblEstado.textProperty().bind(taskGenerar.messageProperty());
            progressBar.progressProperty().bind(taskGenerar.progressProperty());

            taskGenerar.setOnSucceeded(e -> {
                lblEstado.textProperty().unbind();
                progressBar.progressProperty().unbind();
                progressBar.setProgress(1.0);
                lblEstado.setText("¡Completado!");
                
                boolean exito = taskGenerar.getValue();
                InfoPC info = infoHolder[0];

                // Breve pausa para mostrar "Completado" antes de cerrar
                PauseTransition pausaFinal = new PauseTransition(Duration.millis(400));
                pausaFinal.setOnFinished(ev -> {
                    cerrarPantallaCarga(() -> {
                        DialogosFX.mostrarResultadoGeneracion(mainStage, exito, info);
                        if (alCompletar != null) alCompletar.run();
                    });
                });
                pausaFinal.play();
            });

            taskGenerar.setOnFailed(e -> {
                lblEstado.textProperty().unbind();
                progressBar.progressProperty().unbind();
                cerrarPantallaCarga(() -> {
                    DialogosFX.mostrarAlerta(mainStage, "Error",
                        "Error al generar el inventario.", Alert.AlertType.ERROR);
                    if (alCompletar != null) alCompletar.run();
                });
            });

            new Thread(taskGenerar, "Inventario-GenerarExcel").start();
        } else {
            // Para cargas no-Generando, usar mensajes cíclicos como fallback
            String[] mensajes = {"Procesando...", "Cargando datos...", "Casi listo..."};
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
                int idx = (int) ((System.currentTimeMillis() / 1500) % mensajes.length);
                lblEstado.setText(mensajes[idx]);
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
            progressBar.setProgress(-1); // Indeterminate for non-generating tasks
            
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> {
                timeline.stop();
                cerrarPantallaCarga(alCompletar);
            });
            delay.play();
        }
    }

    private static void cerrarPantallaCarga(Runnable alCompletar) {
        if (loadingStage != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(200), loadingStage.getScene().getRoot());
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                loadingStage.close();
                loadingStage = null;
                if (alCompletar != null) {
                    Platform.runLater(alCompletar);
                }
            });
            fade.play();
        } else if (alCompletar != null) {
            Platform.runLater(alCompletar);
        }
    }

    private static StackPane crearSpinnerModerno() {
        StackPane container = new StackPane();
        container.setPrefSize(50, 50);
        container.setAlignment(Pos.CENTER);

        // Solo 3 puntos minimalistas en línea horizontal
        HBox dotsRow = new HBox(14);
        dotsRow.setAlignment(Pos.CENTER);
        
        Circle[] dots = new Circle[3];
        
        for (int i = 0; i < 3; i++) {
            dots[i] = new Circle(6);
            dots[i].setFill(COLOR_PRIMARY);
            dots[i].setOpacity(0.3);
            dotsRow.getChildren().add(dots[i]);
        }

        // Animación secuencial de cada punto
        for (int i = 0; i < 3; i++) {
            final Circle dot = dots[i];
            
            // Animación de opacidad
            FadeTransition fade = new FadeTransition(Duration.millis(500), dot);
            fade.setFromValue(0.3);
            fade.setToValue(1.0);
            fade.setCycleCount(Animation.INDEFINITE);
            fade.setAutoReverse(true);
            fade.setDelay(Duration.millis(i * 150));
            fade.play();
            
            // Animación de escala sutil
            ScaleTransition scale = new ScaleTransition(Duration.millis(500), dot);
            scale.setFromX(1);
            scale.setFromY(1);
            scale.setToX(1.2);
            scale.setToY(1.2);
            scale.setCycleCount(Animation.INDEFINITE);
            scale.setAutoReverse(true);
            scale.setDelay(Duration.millis(i * 150));
            scale.play();
        }

        // ★ Cachear spinner para rendimiento
        container.setCache(true);
        container.setCacheHint(javafx.scene.CacheHint.SPEED);
        container.getChildren().add(dotsRow);
        return container;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // UTILIDADES PÚBLICAS
    // ════════════════════════════════════════════════════════════════════════════

    public static Button crearBotonPrimario(String texto, String icono) {
        Button btn = new Button(icono + "  " + texto);
        btn.setPrefWidth(280);
        btn.setPrefHeight(48);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        btn.setStyle(
            "-fx-background-color: #E63946;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: #FF4D5A;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.04, 150);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: #E63946;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 150);
        });

        return btn;
    }

    public static Button crearBotonSecundario(String texto, String icono) {
        Button btn = new Button(icono + "  " + texto);
        btn.setPrefWidth(280);
        btn.setPrefHeight(44);
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        String secBaseStyle = 
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-text-fill: " + TemaManager.getTextSecondary() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;";
        btn.setStyle(secBaseStyle);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + TemaManager.getSurfaceHover() + ";" +
                "-fx-text-fill: " + TemaManager.getText() + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + TemaManager.getBorderLight() + ";" +
                "-fx-border-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.03, 150);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(secBaseStyle);
            AnimacionesFX.hoverOut(btn, 150);
        });

        return btn;
    }

    public static Button crearBotonVerde(String texto, double ancho, double alto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(ancho);
        btn.setPrefHeight(alto);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        btn.setStyle(
            "-fx-background-color: #00C853;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: #00E676;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.04, 150);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: #00C853;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 150);
        });

        return btn;
    }

    public static Button crearBotonRojo(String texto, double ancho, double alto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(ancho);
        btn.setPrefHeight(alto);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        btn.setStyle(
            "-fx-background-color: #E63946;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: #FF4D5A;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.04, 150);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: #E63946;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 150);
        });

        return btn;
    }

    public static Button crearBotonGris(String texto, double ancho, double alto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(ancho);
        btn.setPrefHeight(alto);
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        String grisBaseStyle = 
            "-fx-background-color: " + TemaManager.getBorder() + ";" +
            "-fx-text-fill: " + TemaManager.getTextSecondary() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;";
        btn.setStyle(grisBaseStyle);

        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + TemaManager.getBorderLight() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(grisBaseStyle));

        return btn;
    }

    public static ImageView cargarImagen(String ruta, double ancho, double alto) {
        try {
            InputStream stream = InventarioFX.class.getResourceAsStream(ruta);
            if (stream != null) {
                return new ImageView(new Image(stream, ancho, alto, true, true));
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // IMPORTACIÓN DE PROYECTOS (1 Excel = 1 Proyecto con todos sus equipos)
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Importa UN archivo Excel como UN proyecto completo.
     * El Excel contiene los equipos/datos del proyecto.
     * El nombre del proyecto se extrae del nombre del archivo o se pide al usuario.
     */
    private static void mostrarDialogoImportarProyectoExcel(Stage parentDialog, Runnable onImport) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo Excel del proyecto a importar");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx", "*.xls"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        
        // Intentar carpeta Music
        Path rutaMusic = inventario.fx.config.PortablePaths.getProyectosDir();
        if (Files.exists(rutaMusic)) {
            fileChooser.setInitialDirectory(rutaMusic.toFile());
        } else {
            fileChooser.setInitialDirectory(inventario.fx.config.PortablePaths.getBase().toFile());
        }
        
        File archivo = fileChooser.showOpenDialog(parentDialog);
        if (archivo == null) return;
        
        // Extraer nombre del proyecto del archivo
        String nombreArchivo = archivo.getName();
        String nombreProyecto = extraerNombreProyecto(nombreArchivo);
        int numEquipos = contarEquiposEnExcel(archivo);
        
        // Mostrar diálogo para confirmar/editar nombre del proyecto
        mostrarDialogoConfirmarProyectoImportado(parentDialog, archivo, nombreProyecto, numEquipos, onImport);
    }

    /**
     * Extrae el nombre del proyecto de un nombre de archivo Excel
     */
    private static String extraerNombreProyecto(String nombreArchivo) {
        String nombre = nombreArchivo
            .replaceAll("(?i)\\.xlsx?$", "") // Quitar extensión
            .replaceAll("^Inventario[_\\s-]*\\d*[_\\s-]*", "") // Quitar prefijo Inventario_N - 
            .replaceAll("^\\d+[_\\s-]+", "") // Quitar números iniciales
            .replace("_", " ") // Guiones bajos a espacios
            .replaceAll("\\s+", " ") // Múltiples espacios a uno
            .trim();
        
        if (nombre.isEmpty()) {
            nombre = nombreArchivo.replaceAll("(?i)\\.xlsx?$", "").replace("_", " ").trim();
        }
        
        return nombre;
    }

    /**
     * Cuenta los equipos (filas de datos) en un archivo Excel
     */
    private static int contarEquiposEnExcel(File archivo) {
        try (FileInputStream fis = new FileInputStream(archivo);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            // Buscar hoja SystemInfo o la primera hoja
            Sheet sheet = workbook.getSheet("SystemInfo");
            if (sheet == null) sheet = workbook.getSheetAt(0);
            
            // Contar filas con datos (ignorando encabezados)
            int count = 0;
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                // Verificar que tenga datos en alguna celda
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(0);
                String valor = getCellStringValue(cell);
                if (valor != null && !valor.trim().isEmpty()) {
                    // Ignorar encabezados típicos
                    String lower = valor.toLowerCase();
                    if (!lower.contains("fecha") && !lower.contains("inventario") && 
                        !lower.contains("usuario") && !lower.contains("hostname")) {
                        count++;
                    }
                }
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Diálogo para confirmar la importación de UN proyecto desde Excel
     */
    private static void mostrarDialogoConfirmarProyectoImportado(Stage parentDialog, File archivoExcel, 
            String nombreSugerido, int numEquipos, Runnable onImport) {
        
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initOwner(parentDialog);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(20));

        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(28));
        container.setMaxWidth(450);
        container.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 16;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setRadius(30);
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setOffsetY(8);
        container.setEffect(shadow);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconCircle = new StackPane();
        Circle bgC = new Circle(24);
        bgC.setFill(Color.web("#10B98120"));
        iconCircle.getChildren().addAll(bgC, IconosSVG.importar("#10B981", 28));
        
        VBox titleBox = new VBox(4);
        Label titulo = new Label("Importar Proyecto");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titulo.setTextFill(COLOR_TEXT());
        
        Label subtitulo = new Label("El Excel se convertirá en un proyecto");
        subtitulo.setFont(Font.font("Segoe UI", 12));
        subtitulo.setTextFill(COLOR_TEXT_MUTED());
        titleBox.getChildren().addAll(titulo, subtitulo);
        
        header.getChildren().addAll(iconCircle, titleBox);

        // Info del archivo
        VBox infoBox = new VBox(12);
        infoBox.setPadding(new Insets(16));
        infoBox.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 10;"
        );
        
        Label lblArchivo = new Label("📄 " + archivoExcel.getName());
        lblArchivo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblArchivo.setTextFill(COLOR_TEXT());
        
        Label lblEquipos = new Label("💻 " + numEquipos + " registro(s) detectado(s)");
        lblEquipos.setFont(Font.font("Segoe UI", 12));
        lblEquipos.setTextFill(Color.web("#3B82F6"));
        
        infoBox.getChildren().addAll(lblArchivo, lblEquipos);

        // Campo nombre del proyecto
        VBox nombreBox = new VBox(6);
        Label lblNombre = new Label("Nombre del proyecto:");
        lblNombre.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblNombre.setTextFill(COLOR_TEXT());
        
        TextField txtNombre = new TextField(nombreSugerido);
        txtNombre.setPromptText("Ej: Secretaría de Educación");
        txtNombre.setFont(Font.font("Segoe UI", 14));
        txtNombre.setPrefHeight(42);
        txtNombre.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        nombreBox.getChildren().addAll(lblNombre, txtNombre);

        // Campo descripción (opcional)
        VBox descBox = new VBox(6);
        Label lblDesc = new Label("Descripción (opcional):");
        lblDesc.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblDesc.setTextFill(COLOR_TEXT());
        
        TextField txtDesc = new TextField();
        txtDesc.setPromptText("Ej: Mesa de servicios");
        txtDesc.setFont(Font.font("Segoe UI", 13));
        txtDesc.setPrefHeight(38);
        txtDesc.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        descBox.getChildren().addAll(lblDesc, txtDesc);

        // Verificar si existe
        Label lblAviso = new Label();
        lblAviso.setFont(Font.font("Segoe UI", 11));
        lblAviso.setWrapText(true);
        
        txtNombre.textProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null && !nuevo.trim().isEmpty()) {
                boolean existe = AdminManager.getProyectos().stream()
                    .anyMatch(p -> p.getNombre().equalsIgnoreCase(nuevo.trim()));
                if (existe) {
                    lblAviso.setText("⚠ Ya existe un proyecto con este nombre. Se actualizará.");
                    lblAviso.setTextFill(Color.web("#F59E0B"));
                } else {
                    lblAviso.setText("✓ Se creará un nuevo proyecto");
                    lblAviso.setTextFill(Color.web("#10B981"));
                }
            } else {
                lblAviso.setText("");
            }
        });
        // Disparar validación inicial
        txtNombre.setText(txtNombre.getText());

        // Botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER_RIGHT);
        botones.setPadding(new Insets(10, 0, 0, 0));
        
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(100);
        btnCancelar.setPrefHeight(40);
        btnCancelar.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        btnCancelar.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btnCancelar.setOnAction(e -> dialog.close());
        
        Button btnImportar = new Button("📥 Importar");
        btnImportar.setPrefWidth(130);
        btnImportar.setPrefHeight(40);
        btnImportar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnImportar.setStyle(
            "-fx-background-color: #10B981;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btnImportar.setOnAction(e -> {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                DialogosFX.mostrarAlerta(dialog, "Error", "El nombre del proyecto es obligatorio", Alert.AlertType.WARNING);
                return;
            }
            
            // Importar el proyecto
            importarProyectoDesdeExcel(dialog, archivoExcel, nombre, txtDesc.getText().trim(), onImport);
            dialog.close();
        });
        
        botones.getChildren().addAll(btnCancelar, btnImportar);

        container.getChildren().addAll(header, infoBox, nombreBox, descBox, lblAviso, botones);
        root.getChildren().add(container);

        Scene scene = new Scene(root,
                ScreenUtils.w(490, 400),
                ScreenUtils.h(480, 380));
        scene.setFill(Color.TRANSPARENT);
        TemaManager.aplicarTema(scene);
        dialog.setScene(scene);
        dialog.centerOnScreen();

        // Animación
        container.setOpacity(0);
        container.setScaleX(0.95);
        container.setScaleY(0.95);
        FadeTransition fi = new FadeTransition(Duration.millis(200), container);
        fi.setToValue(1);
        ScaleTransition si = new ScaleTransition(Duration.millis(200), container);
        si.setToX(1);
        si.setToY(1);
        new ParallelTransition(fi, si).play();

        dialog.show();
        txtNombre.requestFocus();
        txtNombre.selectAll();
    }

    /**
     * Ejecuta la importación del proyecto: crea el proyecto y copia los DATOS del Excel
     * al formato cifrado del sistema.
     */
    private static void importarProyectoDesdeExcel(Stage parentDialog, File archivoOrigen, 
            String nombreProyecto, String descripcion, Runnable onImport) {
        
        // Verificar si el proyecto ya existe
        AdminManager.Proyecto proyectoExistente = AdminManager.getProyectos().stream()
            .filter(p -> p.getNombre().equalsIgnoreCase(nombreProyecto))
            .findFirst()
            .orElse(null);
        
        boolean esNuevo = (proyectoExistente == null);
        
        // Crear o obtener el proyecto
        AdminManager.Proyecto proyecto;
        if (esNuevo) {
            proyecto = new AdminManager.Proyecto();
            proyecto.setNombre(nombreProyecto);
            proyecto.setDescripcion(descripcion);
            proyecto.setColor(AdminManager.COLORES_PROYECTO[AdminManager.getProyectos().size() % AdminManager.COLORES_PROYECTO.length]);
            AdminManager.agregarProyecto(proyecto);
            System.out.println("[Importar] ✓ Proyecto nuevo creado y guardado en SQLite: " + nombreProyecto);
        } else {
            proyecto = proyectoExistente;
            if (descripcion != null && !descripcion.isEmpty()) {
                proyecto.setDescripcion(descripcion);
                AdminManager.actualizarProyecto(proyecto);
            }
            System.out.println("[Importar] ✓ Proyecto existente actualizado en SQLite: " + nombreProyecto);
        }
        
        // Construir el nombre del archivo destino
        try {
            int indiceProyecto = AdminManager.getIndiceProyecto(proyecto) + 1;
            
            // Limpiar nombre del proyecto (quitar caracteres especiales)
            String nombreLimpio = nombreProyecto
                .replaceAll("[\\\\/:*?\"<>|]", "")  // Caracteres no válidos en Windows
                .replace(" ", "_")
                .trim();
            
            // Formato: Inventario_N - NombreProyecto.xlsx
            String nombreArchivo = "Inventario_" + indiceProyecto + " - " + nombreLimpio + ".xlsx";
            Path destino = obtenerCarpetaEjecutable().resolve(nombreArchivo);
            
            System.out.println("[Importar] Archivo origen: " + archivoOrigen.getAbsolutePath());
            System.out.println("[Importar] Archivo destino: " + destino.toAbsolutePath());
            
            // Importar datos del Excel origen al formato del sistema Y a SQLite
            int registrosImportados = importarDatosExcelAlSistema(archivoOrigen, destino, nombreProyecto, proyecto.getId());
            
            System.out.println("[Importar] Proyecto " + (esNuevo ? "creado" : "actualizado") + 
                ": " + nombreProyecto + " -> " + destino + " (" + registrosImportados + " registros)");
            
            // CRÍTICO: Ejecutar callback para actualizar UI ANTES de mostrar alerta
            if (onImport != null) {
                Platform.runLater(() -> {
                    onImport.run();
                    System.out.println("[Importar] ✓ Lista de proyectos actualizada en UI");
                });
            }
            
            String mensaje = esNuevo 
                ? "✓ Proyecto '" + nombreProyecto + "' creado exitosamente.\n\n" +
                  "📄 Archivo: " + nombreArchivo + "\n" +
                  "💻 Registros importados: " + registrosImportados
                : "✓ Proyecto '" + nombreProyecto + "' actualizado.\n\n" +
                  "📄 Archivo: " + nombreArchivo + "\n" +
                  "💻 Registros importados: " + registrosImportados;
            
            DialogosFX.mostrarAlerta(parentDialog, "Importación exitosa", mensaje, Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            AppLogger.getLogger(InventarioFX.class).error("Error al importar el archivo: " + e.getMessage(), e);
            DialogosFX.mostrarAlerta(parentDialog, "Error",
                "Error al importar el archivo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Importa un archivo Excel al sistema.
     * Si el archivo ya está en formato de la aplicación (cifrado), simplemente lo copia.
     * Si es un Excel externo, lo transforma al formato del sistema.
     * Incluye tanto la hoja SystemInfo (equipos) como InstalledApps (aplicaciones).
     * ADEMÁS: Guarda los datos de inventario en la tabla SQLite.
     */
    private static int importarDatosExcelAlSistema(File archivoOrigen, Path archivoDestino, String nombreProyecto, String proyectoId) throws Exception {
        
        // Primero verificar si es un archivo del sistema (cifrado)
        boolean esArchivoDelSistema = false;
        int totalRegistros = 0;
        
        try {
            XSSFWorkbook wbTest = abrirCifradoProyecto(archivoOrigen.toPath());
            if (wbTest != null) {
                esArchivoDelSistema = true;
                // Contar registros
                Sheet hoja = wbTest.getSheet("SystemInfo");
                if (hoja == null && wbTest.getNumberOfSheets() > 0) {
                    hoja = wbTest.getSheetAt(0);
                }
                if (hoja != null) {
                    totalRegistros = Math.max(0, hoja.getLastRowNum() - 1); // -1 por encabezados
                }
                wbTest.close();
                System.out.println("[Importar] Archivo reconocido como formato del sistema (cifrado)");
            }
        } catch (Exception e) {
            System.out.println("[Importar] Archivo no cifrado o formato diferente: " + e.getMessage());
        }
        
        if (esArchivoDelSistema) {
            // === ARCHIVO DEL SISTEMA: Simplemente copiar ===
            System.out.println("[Importar] Copiando archivo directamente: " + archivoOrigen.getName() + " -> " + archivoDestino);
            Files.copy(archivoOrigen.toPath(), archivoDestino, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[Importar] Archivo copiado exitosamente. Registros: " + totalRegistros);
            return totalRegistros;
        }
        
        // === ARCHIVO EXTERNO: Transformar al formato del sistema ===
        System.out.println("[Importar] Transformando archivo externo al formato del sistema...");
        
        XSSFWorkbook wbOrigen;
        try (FileInputStream fis = new FileInputStream(archivoOrigen)) {
            wbOrigen = new XSSFWorkbook(fis);
        } catch (Exception e) {
            throw new Exception("No se pudo abrir el archivo Excel: " + e.getMessage());
        }
        
        // Crear nuevo workbook destino
        XSSFWorkbook wbDestino = new XSSFWorkbook();
        
        // ========== IMPORTAR HOJA SYSTEMINFO (EQUIPOS) ==========
        Sheet hojaOrigenSys = wbOrigen.getSheet("SystemInfo");
        if (hojaOrigenSys == null && wbOrigen.getNumberOfSheets() > 0) {
            hojaOrigenSys = wbOrigen.getSheetAt(0);
        }
        
        int filasCopiadas = 0;
        
        if (hojaOrigenSys != null) {
            Sheet hojaDestino = wbDestino.createSheet("SystemInfo");
            crearEncabezadosConProyecto(wbDestino, hojaDestino, nombreProyecto);
            
            // Detectar columnas del archivo origen
            Map<String, Integer> columnasOrigen = detectarColumnasExcel(hojaOrigenSys);
            System.out.println("[Importar] Columnas SystemInfo detectadas: " + columnasOrigen);
            
            // Mapeo de columnas destino
            Map<String, Integer> columnasDestino = new HashMap<>();
            Row headerDestino = hojaDestino.getRow(1);
            for (int c = 0; c < headerDestino.getLastCellNum(); c++) {
                org.apache.poi.ss.usermodel.Cell cell = headerDestino.getCell(c);
                if (cell != null) {
                    columnasDestino.put(cell.getStringCellValue().trim(), c);
                }
            }
            
            // Estilo para las celdas
            CellStyle estilo = wbDestino.createCellStyle();
            estilo.setWrapText(true);
            estilo.setVerticalAlignment(VerticalAlignment.TOP);
            
            // Encontrar fila de inicio de datos (después de encabezados)
            int filaInicioDatos = detectarFilaInicioDatos(hojaOrigenSys);
            System.out.println("[Importar] SystemInfo - Inicio de datos en fila: " + filaInicioDatos);
            
            // Copiar datos de equipos Y guardar en SQLite
            System.out.println("[Importar] Guardando equipos en SQLite...");
            
            // IMPORTANTE: Usar una sola conexión para todos los inserts (más eficiente)
            java.sql.Connection conn = null;
            java.sql.PreparedStatement stmt = null;
            
            try {
                conn = inventario.fx.database.DatabaseManager.getConnection();
                stmt = conn.prepareStatement(
                    "INSERT INTO inventarios (id, proyecto_id, fecha, usuario, hostname, sistema, " +
                    "fabricante, modelo, serie, placa, procesador, tarjeta_grafica, memoria_ram, " +
                    "disco_duro, num_discos, ip, fecha_escaneo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                
                for (int i = filaInicioDatos; i <= hojaOrigenSys.getLastRowNum(); i++) {
                    Row filaOrigen = hojaOrigenSys.getRow(i);
                    if (filaOrigen == null) continue;
                    
                    // Saltar filas que parezcan encabezados
                    if (esFilaDeEncabezados(filaOrigen)) {
                        System.out.println("[Importar] Saltando fila de encabezados: " + i);
                        continue;
                    }
                    
                    // Verificar que tenga al menos un dato significativo
                    boolean tieneData = false;
                    for (int c = 0; c < Math.min(5, filaOrigen.getLastCellNum()); c++) {
                        String val = getCellStringValue(filaOrigen.getCell(c));
                        if (val != null && !val.trim().isEmpty()) {
                            tieneData = true;
                            break;
                        }
                    }
                    if (!tieneData) continue;
                    
                    // Extraer datos del equipo para SQLite (SOLO COLUMNAS, SIN JSON)
                    String fecha = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("Fecha", 0)));
                    String usuario = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("Usuario Sistema", 1)));
                    String hostname = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("Hostname", 2)));
                    String tipo = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("Tipo", 3)));
                    String marca = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("Marca", 4)));
                    String modelo = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("Modelo", 5)));
                    String serie = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("Serie", 6)));
                    String placa = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("Placa", 7)));
                    String cpu = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("CPU", 8)));
                    String gpu = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("GPU", 9)));
                    String ram = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("RAM", 10)));
                    String discos = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("Discos", 11)));
                    String numDiscos = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("Nº Discos", 12)));
                    String ip = getCellStringValue(filaOrigen.getCell(columnasOrigen.getOrDefault("IP", 13)));
                    
                    // Log de datos extraídos para debugging (solo primer equipo)
                    if (filasCopiadas == 0) {
                        System.out.println("[Importar] Primer equipo - Datos extraídos:");
                        System.out.println("  - Fecha: " + (fecha != null ? fecha : "N/A"));
                        System.out.println("  - Usuario: " + (usuario != null ? usuario : "N/A"));
                        System.out.println("  - Hostname: " + (hostname != null ? hostname : "N/A"));
                        System.out.println("  - Sistema: " + (tipo != null ? tipo : "N/A"));
                        System.out.println("  - Marca: " + (marca != null ? marca : "N/A"));
                        System.out.println("  - Modelo: " + (modelo != null ? modelo : "N/A"));
                        System.out.println("  - CPU: " + (cpu != null ? cpu : "N/A"));
                        System.out.flush();
                    }
                    
                    // Preparar INSERT para este equipo
                    try {
                        String equipoId = java.util.UUID.randomUUID().toString().substring(0, 8);
                        stmt.setString(1, equipoId);
                        stmt.setString(2, proyectoId);
                        stmt.setString(3, fecha != null ? fecha : "");
                        stmt.setString(4, usuario != null ? usuario : "");
                        stmt.setString(5, hostname != null ? hostname : "");
                        stmt.setString(6, tipo != null ? tipo : "");
                        stmt.setString(7, marca != null ? marca : "");
                        stmt.setString(8, modelo != null ? modelo : "");
                        stmt.setString(9, serie != null ? serie : "");
                        stmt.setString(10, placa != null ? placa : "");
                        stmt.setString(11, cpu != null ? cpu : "");
                        stmt.setString(12, gpu != null ? gpu : "");
                        stmt.setString(13, ram != null ? ram : "");
                        stmt.setString(14, discos != null ? discos : "");
                        stmt.setString(15, numDiscos != null ? numDiscos : "");
                        stmt.setString(16, ip != null ? ip : "");
                        stmt.setString(17, fecha != null && !fecha.isEmpty() ? fecha : 
                            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        
                        stmt.addBatch(); // Agregar a batch para ejecutar todos juntos
                        
                        if (filasCopiadas == 0) {
                            System.out.println("[Importar] ✓ Primer equipo preparado para inserción");
                            System.out.flush();
                        }
                    } catch (Exception e) {
                        AppLogger.getLogger(InventarioFX.class).error("[Importar] ✗ Error preparando equipo " + (filasCopiadas + 1) + ": " + e.getMessage(), e);
                    }
                    
                    // Copiar a Excel destino
                    Row filaDestino = hojaDestino.createRow(hojaDestino.getLastRowNum() + 1);
                    filaDestino.setHeight((short) 900);
                    
                    for (String columna : ENCABEZADOS_SISTEMA) {
                        copiarColumna(filaOrigen, filaDestino, columnasOrigen, columnasDestino, columna, estilo);
                    }
                    filasCopiadas++;
                }
                
                // Ejecutar TODOS los inserts de una vez (batch)
                if (filasCopiadas > 0) {
                    System.out.println("[Importar] Ejecutando batch insert de " + filasCopiadas + " equipos...");
                    System.out.flush();
                    int[] results = stmt.executeBatch();
                    int exitosos = 0;
                    for (int r : results) {
                        if (r > 0) exitosos++;
                    }
                    System.out.println("[Importar] ✓ " + exitosos + " de " + filasCopiadas + " equipos guardados exitosamente en SQLite");
                    System.out.flush();
                }
                
            } catch (Exception e) {
                AppLogger.getLogger(InventarioFX.class).error("[Importar] ✗ ERROR en batch insert: " + e.getMessage(), e);
            } finally {
                // Cerrar recursos
                if (stmt != null) try { stmt.close(); } catch (Exception e) {}
                if (conn != null) try { conn.close(); } catch (Exception e) {}
            }
            
            // Ajustar hoja SystemInfo
            autoSize(hojaDestino, ENCABEZADOS_SISTEMA.length);
            hojaDestino.setAutoFilter(new CellRangeAddress(1, 1, 0, ENCABEZADOS_SISTEMA.length - 1));
            hojaDestino.createFreezePane(0, 2);
        }
        
        // ========== IMPORTAR HOJA INSTALLEDAPPS (APLICACIONES) ==========
        Sheet hojaOrigenApps = wbOrigen.getSheet("InstalledApps");
        int appsCopiadas = 0;
        
        if (hojaOrigenApps != null && hojaOrigenApps.getLastRowNum() > 0) {
            System.out.println("[Importar] Procesando hoja de aplicaciones InstalledApps...");
            
            Sheet hojaAppsDestino = wbDestino.createSheet("InstalledApps");
            
            // Crear encabezados para aplicaciones
            crearEncabezadosAplicaciones(wbDestino, hojaAppsDestino, nombreProyecto);
            
            // Detectar columnas de aplicaciones
            Map<String, Integer> colsAppsOrigen = detectarColumnasAplicaciones(hojaOrigenApps);
            System.out.println("[Importar] Columnas Aplicaciones detectadas: " + colsAppsOrigen);
            
            // Mapeo columnas destino apps
            Map<String, Integer> colsAppsDestino = new HashMap<>();
            Row headerApps = hojaAppsDestino.getRow(1);
            for (int c = 0; c < headerApps.getLastCellNum(); c++) {
                org.apache.poi.ss.usermodel.Cell cell = headerApps.getCell(c);
                if (cell != null) {
                    colsAppsDestino.put(cell.getStringCellValue().trim(), c);
                }
            }
            
            CellStyle estiloApps = wbDestino.createCellStyle();
            estiloApps.setWrapText(true);
            estiloApps.setVerticalAlignment(VerticalAlignment.TOP);
            
            // Copiar datos de aplicaciones
            int filaInicioApps = detectarFilaInicioDatos(hojaOrigenApps);
            System.out.println("[Importar] InstalledApps - Inicio de datos en fila: " + filaInicioApps);
            
            for (int i = filaInicioApps; i <= hojaOrigenApps.getLastRowNum(); i++) {
                Row filaOrigen = hojaOrigenApps.getRow(i);
                if (filaOrigen == null) continue;
                
                // Saltar filas de encabezados
                if (esFilaDeEncabezados(filaOrigen)) {
                    System.out.println("[Importar] Saltando fila de encabezados apps: " + i);
                    continue;
                }
                
                // Verificar que tenga datos de aplicación
                String app = getCellStringValue(filaOrigen.getCell(colsAppsOrigen.getOrDefault("Aplicación", 4)));
                if (app == null || app.trim().isEmpty()) continue;
                
                // Saltar si parece un encabezado
                String appLower = app.toLowerCase().trim();
                if (appLower.equals("aplicación") || appLower.equals("aplicacion") || 
                    appLower.equals("programa") || appLower.equals("software")) continue;
                
                Row filaDestino = hojaAppsDestino.createRow(hojaAppsDestino.getLastRowNum() + 1);
                filaDestino.setHeight((short) 600);
                
                for (String columna : ENCABEZADOS_APLICACIONES) {
                    copiarColumna(filaOrigen, filaDestino, colsAppsOrigen, colsAppsDestino, columna, estiloApps);
                }
                appsCopiadas++;
            }
            
            // Ajustar hoja de aplicaciones
            autoSize(hojaAppsDestino, ENCABEZADOS_APLICACIONES.length);
            hojaAppsDestino.setAutoFilter(new CellRangeAddress(1, 1, 0, ENCABEZADOS_APLICACIONES.length - 1));
            hojaAppsDestino.createFreezePane(0, 2);
            
            System.out.println("[Importar] Aplicaciones importadas: " + appsCopiadas);
        }
        
        wbOrigen.close();
        
        // Guardar con cifrado
        try {
            guardarExcelCifrado(wbDestino, archivoDestino);
        } finally {
            wbDestino.close();
        }
        
        System.out.println("[Importar] Archivo transformado. Equipos: " + filasCopiadas + ", Apps: " + appsCopiadas);
        return filasCopiadas;
    }

    /**
     * Crea encabezados para la hoja de aplicaciones
     */
    private static void crearEncabezadosAplicaciones(XSSFWorkbook wb, Sheet hoja, String nombreProyecto) {
        Row titulo = hoja.createRow(0);
        titulo.setHeight((short) 1000);
        org.apache.poi.ss.usermodel.Cell cellTitulo = titulo.createCell(0);
        
        String tituloTexto = nombreProyecto.toUpperCase();
        if (tituloTexto.matches("^\\d+\\.\\s*.*")) {
            tituloTexto = tituloTexto.replaceFirst("^\\d+\\.\\s*", "");
        }
        cellTitulo.setCellValue("APLICACIONES INSTALADAS - " + tituloTexto);

        CellStyle estiloTitulo = wb.createCellStyle();
        XSSFFont fontTitulo = wb.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 14);
        fontTitulo.setColor(IndexedColors.WHITE.getIndex());
        estiloTitulo.setFont(fontTitulo);
        estiloTitulo.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        estiloTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloTitulo.setAlignment(HorizontalAlignment.CENTER);
        estiloTitulo.setVerticalAlignment(VerticalAlignment.CENTER);
        cellTitulo.setCellStyle(estiloTitulo);

        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, ENCABEZADOS_APLICACIONES.length - 1));

        Row header = hoja.createRow(1);
        header.setHeight((short) 500);
        CellStyle estiloHeader = wb.createCellStyle();
        estiloHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloHeader.setAlignment(HorizontalAlignment.CENTER);
        estiloHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBold(true);
        estiloHeader.setFont(fontHeader);

        for (int i = 0; i < ENCABEZADOS_APLICACIONES.length; i++) {
            org.apache.poi.ss.usermodel.Cell c = header.createCell(i);
            c.setCellValue(ENCABEZADOS_APLICACIONES[i]);
            c.setCellStyle(estiloHeader);
        }
    }

    /**
     * Detecta columnas de la hoja de aplicaciones
     */
    private static Map<String, Integer> detectarColumnasAplicaciones(Sheet hoja) {
        Map<String, Integer> columnas = new HashMap<>();
        
        // Buscar fila de encabezados (puede estar en fila 0, 1 o 2)
        for (int fila = 0; fila <= Math.min(5, hoja.getLastRowNum()); fila++) {
            Row row = hoja.getRow(fila);
            if (row == null) continue;
            
            Map<String, Integer> columnasTemp = new HashMap<>();
            int columnasEncontradas = 0;
            
            for (int col = 0; col < row.getLastCellNum(); col++) {
                String valor = getCellStringValue(row.getCell(col));
                if (valor == null || valor.trim().isEmpty()) continue;
                
                String lower = valor.toLowerCase().trim();
                
                // Detectar cada tipo de columna
                if (lower.equals("fecha") || (lower.contains("fecha") && !lower.contains("instalac"))) {
                    if (!columnasTemp.containsKey("Fecha")) {
                        columnasTemp.put("Fecha", col);
                        columnasEncontradas++;
                    }
                }
                if (lower.contains("usuario") && lower.contains("sistema")) {
                    columnasTemp.put("Usuario Sistema", col);
                    columnasEncontradas++;
                } else if (lower.equals("usuario") || lower.equals("user")) {
                    if (!columnasTemp.containsKey("Usuario Sistema")) {
                        columnasTemp.put("Usuario Sistema", col);
                        columnasEncontradas++;
                    }
                }
                if (lower.equals("hostname") || lower.contains("host") || lower.contains("equipo")) {
                    if (!columnasTemp.containsKey("Hostname")) {
                        columnasTemp.put("Hostname", col);
                        columnasEncontradas++;
                    }
                }
                if (lower.equals("ip") || lower.contains("direcc")) {
                    if (!columnasTemp.containsKey("IP")) {
                        columnasTemp.put("IP", col);
                        columnasEncontradas++;
                    }
                }
                if (lower.equals("aplicación") || lower.equals("aplicacion") || 
                    lower.contains("programa") || lower.contains("software") || lower.contains("app")) {
                    if (!columnasTemp.containsKey("Aplicación")) {
                        columnasTemp.put("Aplicación", col);
                        columnasEncontradas++;
                    }
                }
                if (lower.equals("versión") || lower.equals("version") || lower.contains("versi")) {
                    if (!columnasTemp.containsKey("Versión")) {
                        columnasTemp.put("Versión", col);
                        columnasEncontradas++;
                    }
                }
                if (lower.contains("fabric") || lower.contains("vendor") || lower.contains("publisher") || lower.contains("proveedor")) {
                    if (!columnasTemp.containsKey("Fabricante")) {
                        columnasTemp.put("Fabricante", col);
                        columnasEncontradas++;
                    }
                }
                if (lower.contains("fecha") && lower.contains("instalac")) {
                    columnasTemp.put("Fecha Instalación", col);
                    columnasEncontradas++;
                }
            }
            
            // Si encontramos al menos 3 columnas (incluyendo Aplicación), es la fila de encabezados
            if (columnasEncontradas >= 3 && columnasTemp.containsKey("Aplicación")) {
                System.out.println("[Importar] Fila de encabezados Apps encontrada en fila " + fila);
                return columnasTemp;
            }
        }
        
        // Si no encontramos encabezados, asumir formato estándar
        System.out.println("[Importar] Usando formato estándar de aplicaciones");
        columnas.put("Fecha", 0);
        columnas.put("Usuario Sistema", 1);
        columnas.put("Hostname", 2);
        columnas.put("IP", 3);
        columnas.put("Aplicación", 4);
        columnas.put("Versión", 5);
        columnas.put("Fabricante", 6);
        columnas.put("Fecha Instalación", 7);
        
        return columnas;
    }

    /**
     * Detecta las columnas del Excel de origen basándose en los encabezados
     */
    private static Map<String, Integer> detectarColumnasExcel(Sheet hoja) {
        Map<String, Integer> columnas = new HashMap<>();
        
        // Buscar fila de encabezados (puede estar en fila 0, 1, 2, etc.)
        for (int fila = 0; fila <= Math.min(10, hoja.getLastRowNum()); fila++) {
            Row row = hoja.getRow(fila);
            if (row == null) continue;
            
            Map<String, Integer> columnasTemp = new HashMap<>();
            int columnasEncontradas = 0;
            
            for (int col = 0; col < row.getLastCellNum(); col++) {
                String valor = getCellStringValue(row.getCell(col));
                if (valor != null && !valor.trim().isEmpty()) {
                    String normalizado = normalizarNombreColumna(valor.trim());
                    // Solo agregar si es un encabezado conocido del sistema
                    for (String enc : ENCABEZADOS_SISTEMA) {
                        if (enc.equals(normalizado) && !columnasTemp.containsKey(normalizado)) {
                            columnasTemp.put(normalizado, col);
                            columnasEncontradas++;
                            break;
                        }
                    }
                }
            }
            
            // Si encontramos al menos 3 columnas del sistema, es la fila de encabezados
            if (columnasEncontradas >= 3) {
                System.out.println("[Importar] Fila de encabezados encontrada en fila " + fila);
                return columnasTemp;
            }
        }
        
        System.out.println("[Importar] No se encontró fila de encabezados válida");
        return columnas;
    }

    /**
     * Normaliza nombres de columnas para hacer match más flexible
     */
    private static String normalizarNombreColumna(String nombre) {
        if (nombre == null) return "";
        
        // Mapear variantes comunes
        String lower = nombre.toLowerCase().trim();
        
        if (lower.contains("fecha")) return "Fecha";
        if (lower.contains("usuario") && lower.contains("sistema")) return "Usuario Sistema";
        if (lower.equals("usuario") || lower.equals("user")) return "Usuario Sistema";
        if (lower.contains("hostname") || lower.contains("host") || lower.contains("equipo")) return "Hostname";
        if (lower.contains("sistema") && lower.contains("operativo")) return "Sistema Operativo";
        if (lower.equals("so") || lower.equals("os")) return "Sistema Operativo";
        if (lower.contains("modelo")) return "Modelo";
        if (lower.contains("marca") || lower.contains("fabricante")) return "Marca";
        if (lower.contains("tipo") && lower.contains("dispositivo")) return "Tipo Dispositivo";
        if (lower.contains("bios")) return "BIOS";
        if (lower.contains("cpu") || lower.contains("procesador")) return "CPU";
        if (lower.contains("gpu") || lower.contains("gráfica") || lower.contains("grafica") || lower.contains("video")) return "GPU";
        if (lower.contains("ram") || lower.contains("memoria")) return "RAM";
        if (lower.contains("disco") && !lower.contains("nº") && !lower.contains("num")) return "Discos";
        if (lower.contains("nº") || (lower.contains("num") && lower.contains("disco"))) return "Nº Discos";
        if (lower.contains("ip") || lower.contains("dirección")) return "IP";
        
        return nombre.trim();
    }

    /**
     * Verifica si una fila contiene encabezados (no datos)
     */
    private static boolean esFilaDeEncabezados(Row fila) {
        if (fila == null) return false;
        
        int encabezadosEncontrados = 0;
        
        for (int c = 0; c < Math.min(8, fila.getLastCellNum()); c++) {
            String val = getCellStringValue(fila.getCell(c));
            if (val == null || val.trim().isEmpty()) continue;
            
            String lower = val.toLowerCase().trim();
            
            // Verificar si es un nombre de encabezado conocido
            if (lower.equals("fecha") || 
                lower.equals("usuario sistema") || lower.equals("usuario") ||
                lower.equals("hostname") || lower.equals("host") ||
                lower.equals("sistema operativo") || lower.equals("so") ||
                lower.equals("modelo") || lower.equals("marca") ||
                lower.equals("tipo dispositivo") || lower.equals("bios") ||
                lower.equals("cpu") || lower.equals("gpu") ||
                lower.equals("ram") || lower.equals("discos") ||
                lower.equals("nº discos") || lower.equals("ip") ||
                lower.equals("aplicación") || lower.equals("aplicacion") ||
                lower.equals("versión") || lower.equals("version") ||
                lower.equals("fabricante") || lower.equals("fecha instalación") ||
                lower.contains("inventario de equipos") ||
                lower.contains("aplicaciones instaladas")) {
                encabezadosEncontrados++;
            }
        }
        
        // Si encontramos 3 o más palabras de encabezado, es una fila de encabezados
        return encabezadosEncontrados >= 3;
    }

    /**
     * Detecta la fila donde empiezan los datos (después de encabezados)
     */
    private static int detectarFilaInicioDatos(Sheet hoja) {
        for (int i = 0; i <= Math.min(10, hoja.getLastRowNum()); i++) {
            Row row = hoja.getRow(i);
            if (row == null) continue;
            
            String cell0 = getCellStringValue(row.getCell(0));
            if (cell0 != null) {
                String lower = cell0.toLowerCase();
                // Si es un encabezado, los datos empiezan en la siguiente fila
                if (lower.contains("fecha") || lower.contains("usuario") || 
                    lower.contains("hostname") || lower.contains("inventario")) {
                    return i + 1;
                }
            }
        }
        return 1; // Por defecto, fila 1 (después de posible título)
    }

    /**
     * Copia el valor de una columna de origen a destino
     */
    private static void copiarColumna(Row filaOrigen, Row filaDestino, 
            Map<String, Integer> columnasOrigen, Map<String, Integer> columnasDestino, 
            String nombreColumna, CellStyle estilo) {
        
        Integer colOrigen = columnasOrigen.get(nombreColumna);
        Integer colDestino = columnasDestino.get(nombreColumna);
        
        if (colDestino == null) return;
        
        String valor = "";
        if (colOrigen != null) {
            valor = getCellStringValue(filaOrigen.getCell(colOrigen));
        }
        
        org.apache.poi.ss.usermodel.Cell cellDestino = filaDestino.createCell(colDestino);
        cellDestino.setCellValue(valor != null ? valor : "");
        if (estilo != null) cellDestino.setCellStyle(estilo);
    }

    /**
     * Crea encabezados para un nuevo Excel de inventario
     */
    private static void crearEncabezadosConProyecto(XSSFWorkbook wb, Sheet hoja, String nombreProyecto) {
        Row titulo = hoja.createRow(0);
        titulo.setHeight((short) 1200);
        org.apache.poi.ss.usermodel.Cell cellTitulo = titulo.createCell(0);
        
        // Usar el nombre tal cual (ya viene limpio de la importación)
        String tituloTexto = nombreProyecto.toUpperCase();
        // Quitar prefijo numérico si existe (ej: "1. Proyecto" -> "Proyecto")
        if (tituloTexto.matches("^\\d+\\.\\s*.*")) {
            tituloTexto = tituloTexto.replaceFirst("^\\d+\\.\\s*", "");
        }
        cellTitulo.setCellValue("INVENTARIO DE EQUIPOS - " + tituloTexto);

        CellStyle estiloTitulo = wb.createCellStyle();
        XSSFFont fontTitulo = wb.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 16);
        fontTitulo.setColor(IndexedColors.WHITE.getIndex());
        estiloTitulo.setFont(fontTitulo);
        estiloTitulo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estiloTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloTitulo.setAlignment(HorizontalAlignment.CENTER);
        estiloTitulo.setVerticalAlignment(VerticalAlignment.CENTER);
        cellTitulo.setCellStyle(estiloTitulo);

        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, ENCABEZADOS_SISTEMA.length - 1));

        Row header = hoja.createRow(1);
        header.setHeight((short) 600);
        CellStyle estiloHeader = wb.createCellStyle();
        estiloHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloHeader.setAlignment(HorizontalAlignment.CENTER);
        estiloHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBold(true);
        estiloHeader.setFont(fontHeader);

        for (int i = 0; i < ENCABEZADOS_SISTEMA.length; i++) {
            org.apache.poi.ss.usermodel.Cell c = header.createCell(i);
            c.setCellValue(ENCABEZADOS_SISTEMA[i]);
            c.setCellStyle(estiloHeader);
        }
    }

    /**
     * Guarda un workbook con cifrado
     */
    private static void guardarExcelCifrado(XSSFWorkbook wb, Path destino) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);

        EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
        Encryptor encryptor = info.getEncryptor();
        encryptor.confirmPassword(getExcelPassword());

        POIFSFileSystem fs = new POIFSFileSystem();
        OutputStream os = encryptor.getDataStream(fs);
        os.write(bos.toByteArray());
        os.close();

        try (FileOutputStream fos = new FileOutputStream(destino.toFile())) {
            fs.writeFilesystem(fos);
        }
        fs.close();

        // 🔒 Ocultar y proteger el archivo cifrado
        PortablePaths.protegerArchivo(destino);
    }

    /**
     * Importación MASIVA: Seleccionar carpeta y cada Excel se convierte en un proyecto
     */
    private static void mostrarDialogoImportarMasivoExcel(Stage parentDialog, Runnable onImport) {
        // Mostrar información
        boolean continuar = DialogosFX.confirmarAccion(parentDialog, "Importación Masiva",
            "📁 IMPORTAR VARIOS PROYECTOS:\n\n" +
            "Selecciona una carpeta que contenga archivos Excel.\n" +
            "Cada archivo .xlsx se convertirá en un proyecto.\n\n" +
            "📄 Ejemplo:\n" +
            "   CAR.xlsx → Proyecto 'CAR'\n" +
            "   MinSalud.xlsx → Proyecto 'MinSalud'\n" +
            "   FNA.xlsx → Proyecto 'FNA'\n\n" +
            "💡 Los colores se asignan automáticamente.\n" +
            "💡 Si el proyecto existe, se actualiza.\n\n" +
            "¿Seleccionar carpeta?");
        
        if (!continuar) return;
        
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Seleccionar carpeta con archivos Excel");
        
        Path rutaMusic = inventario.fx.config.PortablePaths.getProyectosDir();
        if (Files.exists(rutaMusic)) {
            dirChooser.setInitialDirectory(rutaMusic.toFile());
        } else {
            dirChooser.setInitialDirectory(inventario.fx.config.PortablePaths.getBase().toFile());
        }
        
        File carpeta = dirChooser.showDialog(parentDialog);
        if (carpeta == null) return;
        
        // Buscar archivos Excel
        List<File> archivosExcel = new ArrayList<>();
        List<String[]> infoProyectos = new ArrayList<>(); // [nombreArchivo, nombreProyecto, numEquipos]
        
        try {
            Files.list(carpeta.toPath())
                .filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    return (name.endsWith(".xlsx") || name.endsWith(".xls")) && !name.startsWith("~");
                })
                .sorted()
                .forEach(p -> {
                    File archivo = p.toFile();
                    archivosExcel.add(archivo);
                    String nombreProyecto = extraerNombreProyecto(archivo.getName());
                    int numEquipos = contarEquiposEnExcel(archivo);
                    infoProyectos.add(new String[]{archivo.getName(), nombreProyecto, String.valueOf(numEquipos)});
                });
        } catch (Exception e) {
            DialogosFX.mostrarAlerta(parentDialog, "Error",
                "Error al leer la carpeta:\n" + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }
        
        if (archivosExcel.isEmpty()) {
            DialogosFX.mostrarAlerta(parentDialog, "Sin archivos",
                "No se encontraron archivos Excel en la carpeta seleccionada.", Alert.AlertType.INFORMATION);
            return;
        }
        
        // Mostrar preview y confirmar
        mostrarDialogoConfirmarImportacionMasiva(parentDialog, archivosExcel, infoProyectos, onImport);
    }

    /**
     * Diálogo para confirmar importación masiva
     */
    private static void mostrarDialogoConfirmarImportacionMasiva(Stage parentDialog, 
            List<File> archivos, List<String[]> infoProyectos, Runnable onImport) {
        
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initOwner(parentDialog);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(20));

        VBox container = new VBox(16);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(24));
        container.setMaxWidth(520);
        container.setMaxHeight(550);
        container.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 16;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setRadius(30);
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setOffsetY(8);
        container.setEffect(shadow);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconCircle = new StackPane();
        Circle bgC = new Circle(22);
        bgC.setFill(Color.web("#3B82F620"));
        iconCircle.getChildren().addAll(bgC, IconosSVG.importar("#3B82F6", 26));
        
        VBox titleBox = new VBox(2);
        Label titulo = new Label("Importación Masiva");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titulo.setTextFill(COLOR_TEXT());
        
        Label subtitulo = new Label(archivos.size() + " archivo(s) Excel encontrado(s)");
        subtitulo.setFont(Font.font("Segoe UI", 12));
        subtitulo.setTextFill(Color.web("#10B981"));
        titleBox.getChildren().addAll(titulo, subtitulo);
        
        header.getChildren().addAll(iconCircle, titleBox);

        // Lista de archivos
        VBox listaPreview = new VBox(8);
        listaPreview.setPadding(new Insets(8));
        
        int colorIndex = AdminManager.getProyectos().size();
        for (String[] info : infoProyectos) {
            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(12, 14, 12, 14));
            item.setStyle(
                "-fx-background-color: " + TemaManager.getSurface() + ";" +
                "-fx-background-radius: 8;"
            );
            
            String color = AdminManager.COLORES_PROYECTO[colorIndex % AdminManager.COLORES_PROYECTO.length];
            Rectangle colorIndicator = new Rectangle(4, 36);
            colorIndicator.setFill(Color.web(color));
            colorIndicator.setArcWidth(4);
            colorIndicator.setArcHeight(4);
            
            VBox infoBox = new VBox(3);
            Label nombre = new Label(info[1]); // nombreProyecto
            nombre.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            nombre.setTextFill(COLOR_TEXT());
            
            Label archivo = new Label("📄 " + info[0]); // nombreArchivo
            archivo.setFont(Font.font("Segoe UI", 10));
            archivo.setTextFill(COLOR_TEXT_MUTED());
            
            Label equipos = new Label("💻 " + info[2] + " registro(s)");
            equipos.setFont(Font.font("Segoe UI", 10));
            equipos.setTextFill(Color.web("#3B82F6"));
            
            HBox detailRow = new HBox(10);
            detailRow.getChildren().addAll(archivo, equipos);
            
            infoBox.getChildren().addAll(nombre, detailRow);
            item.getChildren().addAll(colorIndicator, infoBox);
            listaPreview.getChildren().add(item);
            
            colorIndex++;
        }
        
        ScrollPane scrollPreview = new ScrollPane(listaPreview);
        scrollPreview.setFitToWidth(true);
        scrollPreview.setPrefHeight(320);
        scrollPreview.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPreview, Priority.ALWAYS);

        // Botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(100);
        btnCancelar.setPrefHeight(40);
        btnCancelar.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        btnCancelar.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-text-fill: " + TemaManager.getText() + ";" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btnCancelar.setOnAction(e -> dialog.close());
        
        Button btnImportar = new Button("📥 Importar Todo (" + archivos.size() + ")");
        btnImportar.setPrefWidth(160);
        btnImportar.setPrefHeight(40);
        btnImportar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnImportar.setStyle(
            "-fx-background-color: #10B981;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btnImportar.setOnAction(e -> {
            dialog.close();
            ejecutarImportacionMasiva(parentDialog, archivos, infoProyectos, onImport);
        });
        
        botones.getChildren().addAll(btnCancelar, btnImportar);

        container.getChildren().addAll(header, scrollPreview, botones);
        root.getChildren().add(container);

        Scene scene = new Scene(root,
                ScreenUtils.w(560, 460),
                ScreenUtils.h(590, 480));
        scene.setFill(Color.TRANSPARENT);
        TemaManager.aplicarTema(scene);
        dialog.setScene(scene);
        dialog.centerOnScreen();

        // Animación
        container.setOpacity(0);
        container.setScaleX(0.95);
        container.setScaleY(0.95);
        FadeTransition fi = new FadeTransition(Duration.millis(200), container);
        fi.setToValue(1);
        ScaleTransition si = new ScaleTransition(Duration.millis(200), container);
        si.setToX(1);
        si.setToY(1);
        new ParallelTransition(fi, si).play();

        dialog.show();
    }

    /**
     * Ejecuta la importación masiva de proyectos
     */
    private static void ejecutarImportacionMasiva(Stage parentDialog, List<File> archivos, 
            List<String[]> infoProyectos, Runnable onImport) {
        
        int importados = 0;
        int actualizados = 0;
        List<String> errores = new ArrayList<>();
        
        for (int i = 0; i < archivos.size(); i++) {
            File archivo = archivos.get(i);
            String nombreProyecto = infoProyectos.get(i)[1];
            
            try {
                // Verificar si existe
                AdminManager.Proyecto existente = AdminManager.getProyectos().stream()
                    .filter(p -> p.getNombre().equalsIgnoreCase(nombreProyecto))
                    .findFirst()
                    .orElse(null);
                
                AdminManager.Proyecto proyecto;
                boolean esNuevo = (existente == null);
                
                if (esNuevo) {
                    proyecto = new AdminManager.Proyecto();
                    proyecto.setNombre(nombreProyecto);
                    proyecto.setDescripcion("");
                    proyecto.setColor(AdminManager.COLORES_PROYECTO[(AdminManager.getProyectos().size()) % AdminManager.COLORES_PROYECTO.length]);
                    AdminManager.agregarProyecto(proyecto);
                    importados++;
                } else {
                    proyecto = existente;
                    actualizados++;
                }
                
                // Copiar archivo
                int indice = AdminManager.getIndiceProyecto(proyecto) + 1;
                String nombreArchivo = "Inventario_" + indice + " - " + 
                    limpiarNombreProyecto(nombreProyecto) + ".xlsx";
                Path destino = obtenerCarpetaEjecutable().resolve(nombreArchivo);
                Files.copy(archivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
                
                System.out.println("[Masivo] " + (esNuevo ? "+" : "~") + " " + nombreProyecto);
                
            } catch (Exception e) {
                errores.add(nombreProyecto + ": " + e.getMessage());
            }
        }
        
        // Actualizar UI
        if (onImport != null) {
            Platform.runLater(() -> {
                onImport.run();
                System.out.println("[Masivo] ✓ Lista de proyectos actualizada en UI");
            });
        }
        
        // Mostrar resumen
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("✓ Importación masiva completada\n\n");
        if (importados > 0) mensaje.append("📥 ").append(importados).append(" proyecto(s) nuevos\n");
        if (actualizados > 0) mensaje.append("🔄 ").append(actualizados).append(" proyecto(s) actualizados\n");
        if (!errores.isEmpty()) {
            mensaje.append("\n⚠ Errores:\n");
            for (int i = 0; i < Math.min(3, errores.size()); i++) {
                mensaje.append("• ").append(errores.get(i)).append("\n");
            }
        }
        
        DialogosFX.mostrarAlerta(parentDialog, "Importación completada", mensaje.toString(), Alert.AlertType.INFORMATION);
    }

    /**
     * Detecta la fila de encabezados en un Excel
     */
    private static int detectarFilaEncabezados(Sheet sheet) {
        for (int i = 0; i <= Math.min(10, sheet.getLastRowNum()); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            String cell0 = getCellStringValue(row.getCell(0));
            if (cell0 != null) {
                String lower = cell0.toLowerCase();
                if (lower.contains("nombre") || lower.contains("proyecto") || 
                    lower.contains("name") || lower.equals("id")) {
                    return i;
                }
            }
        }
        return 0; // Por defecto, primera fila es encabezado
    }

    /**
     * Obtiene el valor de una celda como String
     */
    private static String getCellStringValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    public static String toHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
}
