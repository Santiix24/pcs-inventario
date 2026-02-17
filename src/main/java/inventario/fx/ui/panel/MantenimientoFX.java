package inventario.fx.ui.panel;
import inventario.fx.config.PortablePaths;
import inventario.fx.model.TemaManager;
import inventario.fx.model.AdminManager;
import inventario.fx.icons.IconosSVG;
import inventario.fx.ui.dialog.DialogosFX;
import inventario.fx.ui.component.NotificacionesFX;
import inventario.fx.util.AppLogger;

import inventario.fx.util.AnimacionesFX;
import inventario.fx.util.ComponentesFX;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import inventario.fx.database.DatabaseManager;
import inventario.fx.service.DatabaseMaintenanceService;

import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/**
 * Módulo de Mantenimiento del Sistema — Panel completo y funcional.
 * 
 * Secciones:
 *   1. Estado del Sistema — Indicadores en tiempo real
 *   2. Base de Datos — VACUUM, ANALYZE, REINDEX, INTEGRITY, CHECKPOINT, OPTIMIZE
 *   3. Copias de Seguridad — Crear backup manual, ver historial, restaurar
 *   4. Limpieza — Eliminar temporales, logs antiguos, backups viejos
 *
 * @author SELCOMP
 * @version 2.0
 */
public class MantenimientoFX {

    private static Stage dialog;
    private static StackPane contenedorRaiz;

    // Referencias UI actualizables
    private static Label lblDbSize;
    private static Label lblDbStatus;
    private static Label lblBackupCount;
    private static Label lblBackupLast;
    private static Label lblTempSize;
    private static VBox backupListContainer;

    // Carpeta de backups
    private static final String BACKUP_DIR = PortablePaths.getBackupsDir().toString();

    // ════════════════════════════════════════════════════════════════════════
    //  PUNTO DE ENTRADA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Muestra el panel de mantenimiento como diálogo independiente.
     */
    public static void mostrar(Stage owner) {
        if (dialog != null && dialog.isShowing()) {
            dialog.toFront();
            dialog.requestFocus();
            return;
        }

        dialog = new Stage();
        dialog.initModality(Modality.NONE);
        dialog.initOwner(owner);
        dialog.setTitle("Mantenimiento del Sistema");
        dialog.initStyle(StageStyle.TRANSPARENT);

        final double[] xOff = {0}, yOff = {0};

        // Root
        contenedorRaiz = new StackPane();
        contenedorRaiz.setStyle("-fx-background-color: transparent;");
        contenedorRaiz.setPadding(new Insets(20));
        VBox root = new VBox(0);
        root.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 2;"
        );
        root.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.2)));
        root.setCache(true);
        root.setCacheHint(CacheHint.SPEED);
        root.setPrefSize(1020, 720);
        root.setMaxSize(1020, 720);

        // ── HEADER ──
        HBox header = crearHeader(xOff, yOff);
        root.getChildren().add(header);

        // ── CONTENIDO en ScrollPane ──
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox body = new VBox(22);
        body.setPadding(new Insets(26, 32, 32, 32));

        // Secciones — construir en segundo plano para no bloquear apertura
        VBox secEstado = crearSeccionEstado();
        VBox secBD = crearSeccionBaseDatos();
        VBox secBackups = crearSeccionBackups();
        VBox secLimpieza = crearSeccionLimpieza();

        secEstado.setCache(true);
        secEstado.setCacheHint(CacheHint.SPEED);
        secBD.setCache(true);
        secBD.setCacheHint(CacheHint.SPEED);

        body.getChildren().addAll(secEstado, secBD, secBackups, secLimpieza);

        scroll.setContent(body);
        root.getChildren().add(scroll);

        contenedorRaiz.getChildren().add(root);

        Scene scene = new Scene(contenedorRaiz);
        scene.setFill(Color.TRANSPARENT);
        TemaManager.aplicarTema(scene);
        TemaManager.registrarEscena(scene);
        dialog.setScene(scene);

        // Animación de entrada premium
        root.setOpacity(0);
        root.setScaleX(0.95);
        root.setScaleY(0.95);
        root.setTranslateY(10);
        
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
        
        // Animar secciones con entrada escalonada
        AnimacionesFX.entradaEscalonada(
            java.util.Arrays.asList(secEstado, secBD, secBackups, secLimpieza),
            400, 80, 18);

        dialog.show();

        // Cargar datos en segundo plano
        cargarEstadoAsync();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ════════════════════════════════════════════════════════════════════════

    private static HBox crearHeader(double[] xOff, double[] yOff) {
        HBox header = new HBox(18);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(28, 32, 24, 32));
        header.setCursor(Cursor.MOVE);
        header.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 20 20 0 0;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-width: 0 0 1.5 0;"
        );

        header.setOnMousePressed(e -> { xOff[0] = e.getSceneX(); yOff[0] = e.getSceneY(); });
        header.setOnMouseDragged(e -> {
            dialog.setX(e.getScreenX() - xOff[0]);
            dialog.setY(e.getScreenY() - yOff[0]);
        });

        // Icono
        StackPane iconBox = new StackPane(IconosSVG.herramienta("#10B981", 28));
        iconBox.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 16;" +
            "-fx-border-color: " + TemaManager.COLOR_SUCCESS + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 16;"
        );
        iconBox.setPrefSize(60, 60);
        iconBox.setMinSize(60, 60);

        VBox titleBox = new VBox(5);
        Label titulo = new Label("Mantenimiento del Sistema");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.web(TemaManager.getText()));
        Label sub = new Label("Optimización, respaldos y estado del sistema");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(TemaManager.getTextMuted()));
        titleBox.getChildren().addAll(titulo, sub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Botón cerrar
        Button btnCerrar = crearBotonCerrar();

        header.getChildren().addAll(iconBox, titleBox, btnCerrar);
        return header;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SECCIÓN 1: ESTADO DEL SISTEMA
    // ════════════════════════════════════════════════════════════════════════

    private static VBox crearSeccionEstado() {
        VBox seccion = new VBox(14);

        HBox tituloRow = crearTituloSeccion("Estado del Sistema", "#3B82F6", IconosSVG.dashboard("#3B82F6", 20));
        seccion.getChildren().add(tituloRow);

        HBox cards = new HBox(14);
        cards.setAlignment(Pos.CENTER);

        // Card: Base de datos
        lblDbSize = new Label("...");
        lblDbStatus = new Label("...");
        VBox cardDb = crearStatCard("Base de Datos", lblDbSize, lblDbStatus,
                IconosSVG.servidor("#8B5CF6", 22), "#8B5CF6");

        // Card: Backups
        lblBackupCount = new Label("...");
        lblBackupLast = new Label("...");
        VBox cardBk = crearStatCard("Copias de Seguridad", lblBackupCount, lblBackupLast,
                IconosSVG.guardar("#10B981", 22), "#10B981");

        // Card: Temporales
        lblTempSize = new Label("...");
        Label lblTempDetail = new Label("Archivos temporales");
        VBox cardTmp = crearStatCard("Limpieza", lblTempSize, lblTempDetail,
                IconosSVG.eliminar("#F59E0B", 22), "#F59E0B");

        HBox.setHgrow(cardDb, Priority.ALWAYS);
        HBox.setHgrow(cardBk, Priority.ALWAYS);
        HBox.setHgrow(cardTmp, Priority.ALWAYS);

        cards.getChildren().addAll(cardDb, cardBk, cardTmp);
        seccion.getChildren().add(cards);

        return seccion;
    }

    private static VBox crearStatCard(String titulo, Label lblValor, Label lblDetalle,
                                       Node icono, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18));
        card.setStyle(cardStyle(color, false));

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        StackPane iconW = new StackPane(icono);
        iconW.setStyle(
            "-fx-background-color: " + color + "15;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10;"
        );
        iconW.setPrefSize(44, 44);
        iconW.setMinSize(44, 44);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblTitulo.setTextFill(Color.web(TemaManager.getTextMuted()));

        top.getChildren().addAll(iconW, lblTitulo);

        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblValor.setTextFill(Color.web(TemaManager.getText()));

        lblDetalle.setFont(Font.font("Segoe UI", 11));
        lblDetalle.setTextFill(Color.web(TemaManager.getTextMuted()));

        card.getChildren().addAll(top, lblValor, lblDetalle);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle(cardStyle(color, true)));
        card.setOnMouseExited(e -> card.setStyle(cardStyle(color, false)));

        return card;
    }

    private static String cardStyle(String color, boolean hover) {
        return ComponentesFX.cardStyle(color, hover);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SECCIÓN 2: BASE DE DATOS
    // ════════════════════════════════════════════════════════════════════════

    private static VBox crearSeccionBaseDatos() {
        VBox seccion = new VBox(14);

        HBox tituloRow = crearTituloSeccion("Base de Datos", "#8B5CF6", IconosSVG.servidor("#8B5CF6", 20));
        seccion.getChildren().add(tituloRow);

        // Grid 2×3 de operaciones
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        String[][] ops = {
            {"VACUUM",     "Compactar y recuperar espacio en disco",     "#8B5CF6", "VACUUM"},
            {"ANALYZE",    "Actualizar estadísticas de consultas",       "#10B981", "ANALYZE"},
            {"OPTIMIZE",   "Optimizar índices de búsqueda",             "#F59E0B", "PRAGMA optimize"},
            {"REINDEX",    "Reconstruir índices dañados",               "#EC4899", "REINDEX"},
            {"CHECKPOINT", "Guardar cambios pendientes al disco",       "#6366F1", "PRAGMA wal_checkpoint(TRUNCATE)"},
            {"INTEGRITY",  "Verificar integridad de los datos",         "#EF4444", "PRAGMA integrity_check"}
        };

        for (int i = 0; i < ops.length; i++) {
            String[] op = ops[i];
            HBox card = crearOpCard(op[0], op[1], op[2], op[3]);
            GridPane.setHgrow(card, Priority.ALWAYS);
            card.setMaxWidth(Double.MAX_VALUE);
            grid.add(card, i % 3, i / 3);
        }

        // Hacer columnas responsivas
        for (int c = 0; c < 3; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        // Botón ejecutar todo
        Button btnAll = crearBotonAccion("Ejecutar Mantenimiento Completo",
                IconosSVG.rayo("#FFFFFF", 18), "#10B981");
        btnAll.setOnAction(e -> ejecutarMantenimientoCompleto());

        seccion.getChildren().addAll(grid, btnAll);
        return seccion;
    }

    /**
     * Card de operación individual de BD — ejecutar al hacer clic.
     */
    private static HBox crearOpCard(String nombre, String desc, String color, String sql) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setCursor(Cursor.HAND);
        card.setStyle(cardStyle(color, false));

        // Badge
        StackPane badge = new StackPane();
        badge.setPrefSize(8, 8);
        badge.setMinSize(8, 8);
        badge.setMaxSize(8, 8);
        badge.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");

        VBox txt = new VBox(3);
        HBox.setHgrow(txt, Priority.ALWAYS);
        Label lbl = new Label(nombre);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web(TemaManager.getText()));
        Label d = new Label(desc);
        d.setFont(Font.font("Segoe UI", 11));
        d.setTextFill(Color.web(TemaManager.getTextMuted()));
        d.setWrapText(true);
        txt.getChildren().addAll(lbl, d);

        // Icono play
        StackPane playIcon = new StackPane(IconosSVG.rayo(color, 16));

        card.getChildren().addAll(badge, txt, playIcon);

        card.setOnMouseEntered(e -> card.setStyle(cardStyle(color, true)));
        card.setOnMouseExited(e -> card.setStyle(cardStyle(color, false)));

        card.setOnMouseClicked(e -> ejecutarOperacionIndividual(nombre, sql, color));

        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SECCIÓN 3: COPIAS DE SEGURIDAD
    // ════════════════════════════════════════════════════════════════════════

    private static VBox crearSeccionBackups() {
        VBox seccion = new VBox(14);

        HBox tituloRow = crearTituloSeccion("Copias de Seguridad", "#10B981", IconosSVG.guardar("#10B981", 20));

        // Botones de acción al lado del título
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCrear = crearBotonAccionCompacto("Crear Backup", IconosSVG.descargar("#FFFFFF", 14), "#10B981");
        btnCrear.setOnAction(e -> ejecutarBackupManual());

        Button btnRestaurar = crearBotonAccionCompacto("Restaurar", IconosSVG.subir("#FFFFFF", 14), "#F59E0B");
        btnRestaurar.setOnAction(e -> seleccionarYRestaurar());

        HBox acciones = new HBox(8);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        acciones.getChildren().addAll(btnCrear, btnRestaurar);

        tituloRow.getChildren().addAll(spacer, acciones);
        seccion.getChildren().add(tituloRow);

        // Lista de backups
        backupListContainer = new VBox(8);
        backupListContainer.setStyle(
            "-fx-background-color: " + TemaManager.getSurface() + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 16;"
        );

        // Placeholder
        Label loading = new Label("Cargando backups...");
        loading.setFont(Font.font("Segoe UI", 12));
        loading.setTextFill(Color.web(TemaManager.getTextMuted()));
        backupListContainer.getChildren().add(loading);

        seccion.getChildren().add(backupListContainer);

        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SECCIÓN 4: LIMPIEZA
    // ════════════════════════════════════════════════════════════════════════

    private static VBox crearSeccionLimpieza() {
        VBox seccion = new VBox(14);

        HBox tituloRow = crearTituloSeccion("Limpieza del Sistema", "#F59E0B", IconosSVG.eliminar("#F59E0B", 20));
        seccion.getChildren().add(tituloRow);

        HBox cards = new HBox(12);
        cards.setAlignment(Pos.CENTER);

        // Card: Limpiar temporales
        VBox cardTemp = crearLimpiezaCard(
            "Archivos Temporales",
            "Elimina archivos .tmp, respaldos pre-restauración y cachés del sistema",
            "#F59E0B",
            IconosSVG.eliminar("#F59E0B", 20),
            () -> limpiarTemporales()
        );

        // Card: Limpiar backups viejos
        VBox cardOld = crearLimpiezaCard(
            "Backups Antiguos",
            "Conserva solo los últimos 5 backups y elimina los más antiguos",
            "#EF4444",
            IconosSVG.calendario("#EF4444", 20),
            () -> limpiarBackupsAntiguos()
        );

        // Card: Limpiar WAL
        VBox cardWal = crearLimpiezaCard(
            "Archivos WAL",
            "Limpia registros de escritura adelantada de la base de datos",
            "#6366F1",
            IconosSVG.servidor("#6366F1", 20),
            () -> limpiarWAL()
        );

        // Card: Reiniciar Sistema (limpieza total)
        VBox cardReset = crearLimpiezaCard(
            "Reiniciar Sistema",
            "Elimina TODOS los datos: proyectos, reportes, logs, backups y Excel. Solo conserva la contraseña",
            "#DC2626",
            IconosSVG.advertencia("#DC2626", 20),
            () -> reiniciarSistemaCompleto()
        );

        HBox.setHgrow(cardTemp, Priority.ALWAYS);
        HBox.setHgrow(cardOld, Priority.ALWAYS);
        HBox.setHgrow(cardWal, Priority.ALWAYS);
        HBox.setHgrow(cardReset, Priority.ALWAYS);

        cards.getChildren().addAll(cardTemp, cardOld, cardWal, cardReset);
        seccion.getChildren().add(cards);

        return seccion;
    }

    private static VBox crearLimpiezaCard(String titulo, String desc, String color,
                                           Node icono, Runnable accion) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle(cardStyle(color, false));

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        StackPane iconW = new StackPane(icono);
        iconW.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 10; -fx-padding: 10;");
        iconW.setPrefSize(44, 44);
        iconW.setMinSize(44, 44);

        Label lbl = new Label(titulo);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lbl.setTextFill(Color.web(TemaManager.getText()));
        top.getChildren().addAll(iconW, lbl);

        Label d = new Label(desc);
        d.setFont(Font.font("Segoe UI", 12));
        d.setTextFill(Color.web(TemaManager.getTextMuted()));
        d.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btn = new Button("Ejecutar");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(36);
        btn.setCursor(Cursor.HAND);
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + color + "DD;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnAction(e -> accion.run());

        card.getChildren().addAll(top, d, spacer, btn);

        card.setOnMouseEntered(e -> card.setStyle(cardStyle(color, true)));
        card.setOnMouseExited(e -> card.setStyle(cardStyle(color, false)));

        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EJECUCIÓN: OPERACIÓN INDIVIDUAL DE BD
    // ════════════════════════════════════════════════════════════════════════

    private static void ejecutarOperacionIndividual(String nombre, String sql, String color) {
        // Confirmación rápida
        if (!DialogosFX.confirmarAccion(dialog, "Ejecutar " + nombre,
                "¿Desea ejecutar la operación " + nombre + "?\n\nEsto puede tardar unos segundos.")) {
            return;
        }

        // Diálogo de progreso compacto
        Stage progress = crearMiniProgreso("Ejecutando " + nombre + "...");
        progress.show();

        new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

            DatabaseMaintenanceService.ResultadoOperacion res =
                    DatabaseMaintenanceService.ejecutarOperacion(nombre, sql);

            final boolean success = res.exito;
            final String msg = res.mensaje + " (" + res.duracionMs + "ms)";


            Platform.runLater(() -> {
                progress.close();
                NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                        success ? nombre + " Completado" : nombre + " — Error",
                        msg,
                        success ? TemaManager.COLOR_SUCCESS : TemaManager.COLOR_DANGER);
                cargarEstadoAsync();
            });
        }, "Mant-OperacionDB").start();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EJECUCIÓN: MANTENIMIENTO COMPLETO
    // ════════════════════════════════════════════════════════════════════════

    private static void ejecutarMantenimientoCompleto() {
        if (!DialogosFX.confirmarAccion(dialog, "Mantenimiento Completo",
                "Se ejecutarán las 6 operaciones de mantenimiento.\n\n" +
                "Tiempo estimado: 10-30 segundos.\n¿Continuar?")) {
            return;
        }

        String[][] ops = DatabaseMaintenanceService.getOperaciones();

        // Diálogo con lista de progreso
        Stage progressDialog = new Stage();
        progressDialog.initModality(Modality.WINDOW_MODAL);
        progressDialog.initOwner(dialog);
        progressDialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(0);
        root.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);"
        );
        root.setPrefWidth(540);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(22, 24, 18, 24));
        header.setStyle("-fx-border-color: " + TemaManager.getBorder() + "; -fx-border-width: 0 0 1 0;");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(28, 28);
        Label lblHeader = new Label("Ejecutando mantenimiento...");
        lblHeader.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
        lblHeader.setTextFill(Color.web(TemaManager.getText()));
        Label lblCounter = new Label("0 / " + ops.length);
        lblCounter.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblCounter.setTextFill(Color.web(TemaManager.COLOR_SUCCESS));
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(spinner, lblHeader, sp, lblCounter);

        ProgressBar bar = new ProgressBar(0);
        bar.setPrefHeight(6);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: " + TemaManager.COLOR_SUCCESS + ";");

        // Filas de operaciones
        VBox opsList = new VBox(8);
        opsList.setPadding(new Insets(18, 24, 18, 24));

        Label[] statusLabels = new Label[ops.length];
        StackPane[] statusIcons = new StackPane[ops.length];
        Label[] timeLabels = new Label[ops.length];

        for (int i = 0; i < ops.length; i++) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 14, 10, 14));
            row.setStyle("-fx-background-color: " + TemaManager.getSurface() + "; -fx-background-radius: 8;");

            StackPane b = new StackPane();
            b.setPrefSize(8, 8); b.setMinSize(8, 8); b.setMaxSize(8, 8);
            b.setStyle("-fx-background-color: " + ops[i][2] + "; -fx-background-radius: 4;");

            Label n = new Label(ops[i][0]);
            n.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            n.setTextFill(Color.web(TemaManager.getText()));
            n.setMinWidth(90);

            statusLabels[i] = new Label("Pendiente");
            statusLabels[i].setFont(Font.font("Segoe UI", 11));
            statusLabels[i].setTextFill(Color.web(TemaManager.getTextMuted()));
            HBox.setHgrow(statusLabels[i], Priority.ALWAYS);

            timeLabels[i] = new Label("");
            timeLabels[i].setFont(Font.font("Segoe UI", 11));
            timeLabels[i].setTextFill(Color.web(TemaManager.getTextMuted()));
            timeLabels[i].setMinWidth(50);

            statusIcons[i] = new StackPane();
            statusIcons[i].setPrefSize(20, 20);
            statusIcons[i].setMinSize(20, 20);
            Label dot = new Label("○");
            dot.setTextFill(Color.web(TemaManager.getTextMuted()));
            statusIcons[i].getChildren().add(dot);

            row.getChildren().addAll(b, n, statusLabels[i], timeLabels[i], statusIcons[i]);
            opsList.getChildren().add(row);
        }

        root.getChildren().addAll(header, bar, opsList);

        StackPane wrapperProgress = new StackPane(root);
        wrapperProgress.setStyle("-fx-background-color: transparent;");
        wrapperProgress.setPadding(new Insets(20));

        Scene scene = new Scene(wrapperProgress);
        scene.setFill(Color.TRANSPARENT);
        progressDialog.setScene(scene);
        progressDialog.show();

        // Ejecutar todas las operaciones
        new Thread(() -> {
            int exitosos = 0, fallidos = 0;
            StringBuilder resumen = new StringBuilder();

            for (int i = 0; i < ops.length; i++) {
                final int idx = i;
                String opNombre = ops[i][0];
                String opSQL = ops[i][3];
                String opColor = ops[i][2];
                String opMsg = ops[i][1];

                Platform.runLater(() -> {
                    lblHeader.setText(opMsg);
                    lblCounter.setText((idx + 1) + " / " + ops.length);
                    bar.setProgress((double) idx / ops.length);
                    statusLabels[idx].setText("Ejecutando...");
                    statusLabels[idx].setTextFill(Color.web(opColor));
                    statusIcons[idx].getChildren().clear();
                    ProgressIndicator mini = new ProgressIndicator();
                    mini.setPrefSize(18, 18);
                    mini.setMaxSize(18, 18);
                    statusIcons[idx].getChildren().add(mini);
                });

                try { Thread.sleep(80); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

                DatabaseMaintenanceService.ResultadoOperacion resultado =
                        DatabaseMaintenanceService.ejecutarOperacion(opNombre, opSQL);

                if (resultado.exito) {
                    resumen.append("✓ ").append(opNombre).append(": ").append(resultado.mensaje).append("\n");
                    exitosos++;
                    final long dur = resultado.duracionMs;

                    Platform.runLater(() -> {
                        statusLabels[idx].setText("Completado");
                        statusLabels[idx].setTextFill(Color.web(TemaManager.COLOR_SUCCESS));
                        timeLabels[idx].setText(dur + "ms");
                        timeLabels[idx].setTextFill(Color.web(TemaManager.COLOR_SUCCESS));
                        statusIcons[idx].getChildren().clear();
                        statusIcons[idx].getChildren().add(IconosSVG.verificar(TemaManager.COLOR_SUCCESS, 18));
                    });
                } else {
                    resumen.append("✗ ").append(opNombre).append(": ").append(resultado.mensaje).append("\n");
                    fallidos++;
                    final long dur = resultado.duracionMs;

                    Platform.runLater(() -> {
                        statusLabels[idx].setText("Error");
                        statusLabels[idx].setTextFill(Color.web(TemaManager.COLOR_DANGER));
                        timeLabels[idx].setText(dur + "ms");
                        timeLabels[idx].setTextFill(Color.web(TemaManager.COLOR_DANGER));
                        statusIcons[idx].getChildren().clear();
                        statusIcons[idx].getChildren().add(IconosSVG.cerrar(TemaManager.COLOR_DANGER, 18));
                    });
                }
            }

            final int ok = exitosos;
            final int fail = fallidos;

            Platform.runLater(() -> {
                bar.setProgress(1.0);
                lblHeader.setText("Mantenimiento completado");
                lblCounter.setText(ok + " / " + ops.length + " exitosos");
                spinner.setProgress(1.0);
            });

            try { Thread.sleep(1200); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

            Platform.runLater(() -> {
                progressDialog.close();
                String tit = fail == 0 ? "Mantenimiento Completado" : "Mantenimiento Parcial";
                String msg = ok + " operaciones exitosas" + (fail > 0 ? ", " + fail + " con errores" : "");
                NotificacionesFX.mostrarNotificacion(contenedorRaiz, tit, msg,
                        fail == 0 ? TemaManager.COLOR_SUCCESS : TemaManager.COLOR_WARNING);
                cargarEstadoAsync();
            });
        }, "Mant-OptimizacionCompleta").start();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EJECUCIÓN: BACKUP MANUAL
    // ════════════════════════════════════════════════════════════════════════

    private static void ejecutarBackupManual() {
        Stage progress = crearMiniProgreso("Creando copia de seguridad...");
        progress.show();

        new Thread(() -> {
            try {
                // Asegurar carpeta de backups existe
                Path backupBaseDir = Paths.get(BACKUP_DIR);
                if (!Files.exists(backupBaseDir)) {
                    Files.createDirectories(backupBaseDir);
                }

                Platform.runLater(() -> actualizarMiniProgreso(progress, "Preparando checkpoint de base de datos..."));

                // Crear backup directo — sin depender de AdminManager/BackupManager
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
                Path backupDir = backupBaseDir.resolve("backup_" + timestamp);
                Files.createDirectories(backupDir);

                int archivosCopiados = 0;

                // ── 1. CHECKPOINT WAL — forzar escritura de cambios pendientes ──
                try (Connection conn = DatabaseManager.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA wal_checkpoint(TRUNCATE)");
                    System.out.println("[Backup] WAL checkpoint completado");
                } catch (Exception walEx) {
                    System.err.println("[Backup] Advertencia WAL: " + walEx.getMessage());
                }

                // ── 2. COPIAR BASE DE DATOS ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Copiando base de datos..."));
                String dbDir = PortablePaths.getDatabaseDir().toString();
                Path dbSource = Paths.get(dbDir, "inventario.db");
                if (Files.exists(dbSource)) {
                    long dbSize = Files.size(dbSource);
                    Files.copy(dbSource, backupDir.resolve("inventario.db"), StandardCopyOption.REPLACE_EXISTING);
                    // Verificar copia
                    long copiedSize = Files.size(backupDir.resolve("inventario.db"));
                    if (copiedSize != dbSize) {
                        throw new IOException("Verificación fallida: DB original=" + dbSize + " copia=" + copiedSize);
                    }
                    archivosCopiados++;
                    System.out.println("[Backup] DB copiada: " + formatSize(dbSize));
                } else {
                    System.err.println("[Backup] ADVERTENCIA: No se encontró inventario.db en " + dbDir);
                }

                // ── 3. COPIAR ARCHIVOS EXCEL ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Copiando archivos Excel..."));
                try {
                    String carpetaProyectos = AdminManager.getRutasTrabajo().getCarpetaProyectos();
                    if (carpetaProyectos != null) {
                        Path proyectos = Paths.get(carpetaProyectos);
                        if (Files.exists(proyectos)) {
                            Path excelDir = backupDir.resolve("excel");
                            Files.createDirectories(excelDir);
                            try (Stream<Path> files = Files.list(proyectos)) {
                                List<Path> excels = files
                                    .filter(p -> p.toString().toLowerCase().endsWith(".xlsx"))
                                    .toList();
                                for (Path src : excels) {
                                    Files.copy(src, excelDir.resolve(src.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                                    archivosCopiados++;
                                }
                                System.out.println("[Backup] " + excels.size() + " archivos Excel copiados");
                            }
                        }
                    }
                } catch (Exception excelEx) {
                    System.err.println("[Backup] Advertencia Excel: " + excelEx.getMessage());
                }

                // ── 4. COPIAR CLAVES DE ENCRIPTACIÓN ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Copiando claves de seguridad..."));
                Path securityDir = PortablePaths.getSecurityDir();
                if (Files.exists(securityDir)) {
                    Path targetSec = backupDir.resolve("security");
                    Files.createDirectories(targetSec);
                    try (Stream<Path> files = Files.list(securityDir)) {
                        List<Path> secFiles = files.toList();
                        for (Path src : secFiles) {
                            Files.copy(src, targetSec.resolve(src.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                            archivosCopiados++;
                        }
                        System.out.println("[Backup] " + secFiles.size() + " claves de seguridad copiadas");
                    }
                }

                // ── 5. COPIAR CONFIGURACIÓN ──
                Path configFile = PortablePaths.getApplicationProperties();
                if (Files.exists(configFile)) {
                    Files.copy(configFile, backupDir.resolve("application.properties"), StandardCopyOption.REPLACE_EXISTING);
                    archivosCopiados++;
                    System.out.println("[Backup] Configuración copiada");
                }

                // ── 6. COPIAR REPORTES DE MANTENIMIENTO (.dat) ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Copiando reportes de mantenimiento..."));
                Path reportesDat = PortablePaths.getReportesFile();
                Path reportesSource = Files.exists(reportesDat) ? reportesDat : null;
                if (reportesSource != null) {
                    Files.copy(reportesSource, backupDir.resolve("reportes_mantenimiento.dat"), StandardCopyOption.REPLACE_EXISTING);
                    archivosCopiados++;
                    System.out.println("[Backup] Reportes de mantenimiento copiados: " + formatSize(Files.size(reportesSource)));
                    // También copiar .bak si existe
                    Path bakFile = Paths.get(reportesSource.toString() + ".bak");
                    if (Files.exists(bakFile)) {
                        Files.copy(bakFile, backupDir.resolve("reportes_mantenimiento.dat.bak"), StandardCopyOption.REPLACE_EXISTING);
                        archivosCopiados++;
                    }
                } else {
                    System.out.println("[Backup] No se encontró reportes_mantenimiento.dat (puede ser normal si no hay reportes)");
                }

                // ── 7. COPIAR BORRADORES ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Copiando borradores..."));
                Path borradoresDir = PortablePaths.getBorradoresDir();
                if (Files.exists(borradoresDir) && Files.isDirectory(borradoresDir)) {
                    Path targetBorradores = backupDir.resolve("borradores");
                    Files.createDirectories(targetBorradores);
                    try (Stream<Path> borradorFiles = Files.list(borradoresDir)) {
                        List<Path> borradores = borradorFiles.filter(Files::isRegularFile).toList();
                        for (Path b : borradores) {
                            Files.copy(b, targetBorradores.resolve(b.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                            archivosCopiados++;
                        }
                        System.out.println("[Backup] " + borradores.size() + " borradores copiados");
                    }
                }

                // ── 8. COPIAR FIRMAS DIGITALES ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Copiando firmas digitales..."));
                Path firmasDir = PortablePaths.getFirmasDir();
                if (Files.exists(firmasDir) && Files.isDirectory(firmasDir)) {
                    Path targetFirmas = backupDir.resolve("firmas");
                    Files.createDirectories(targetFirmas);
                    try (Stream<Path> firmaFiles = Files.list(firmasDir)) {
                        List<Path> firmas = firmaFiles.filter(Files::isRegularFile).toList();
                        for (Path f : firmas) {
                            Files.copy(f, targetFirmas.resolve(f.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                            archivosCopiados++;
                        }
                        System.out.println("[Backup] " + firmas.size() + " firmas digitales copiadas");
                    }
                }

                // ── 9. COPIAR MASTER KEY Y CONFIG ADICIONAL ──
                Path masterKey = PortablePaths.getMasterKeyFile();
                if (Files.exists(masterKey)) {
                    Files.copy(masterKey, backupDir.resolve("master.key"), StandardCopyOption.REPLACE_EXISTING);
                    archivosCopiados++;
                    System.out.println("[Backup] Master key copiada");
                }
                Path configProps = PortablePaths.getConfigProperties();
                if (Files.exists(configProps)) {
                    Files.copy(configProps, backupDir.resolve("config.properties"), StandardCopyOption.REPLACE_EXISTING);
                    archivosCopiados++;
                    System.out.println("[Backup] config.properties copiado");
                }

                // ── 10. VERIFICAR BACKUP ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Verificando backup..."));
                long totalSize = dirSize(backupDir);
                boolean tieneDB = Files.exists(backupDir.resolve("inventario.db"));

                if (!tieneDB) {
                    // Falló lo más importante — eliminar carpeta y reportar error
                    deleteDir(backupDir);
                    throw new IOException("No se pudo copiar la base de datos. Backup cancelado.");
                }

                final int totalArchivos = archivosCopiados;
                final long totalTamano = totalSize;
                final Path finalPath = backupDir;

                System.out.println("[Backup] === BACKUP COMPLETADO ===");
                System.out.println("[Backup] Carpeta: " + backupDir);
                System.out.println("[Backup] Archivos: " + totalArchivos);
                System.out.println("[Backup] Tamaño: " + formatSize(totalTamano));

                Platform.runLater(() -> {
                    progress.close();
                    NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                            "Backup Creado Exitosamente",
                            totalArchivos + " archivos • " + formatSize(totalTamano) + "\n" + finalPath.getFileName(),
                            TemaManager.COLOR_SUCCESS);
                    cargarEstadoAsync();
                });

            } catch (Exception ex) {
                AppLogger.getLogger(MantenimientoFX.class).error("[Backup] ERROR: " + ex.getMessage(), ex);
                Platform.runLater(() -> {
                    progress.close();
                    NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                            "Error al Crear Backup", ex.getMessage(), TemaManager.COLOR_DANGER);
                });
            }
        }, "Mant-CrearBackup").start();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EJECUCIÓN: RESTAURAR BACKUP
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Restaurar desde explorador de archivos (botón header).
     */
    private static void seleccionarYRestaurar() {
        Path backupBasePath = Paths.get(BACKUP_DIR);

        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Seleccione la carpeta del backup a restaurar");
        if (Files.exists(backupBasePath)) {
            dc.setInitialDirectory(backupBasePath.toFile());
        }

        File selected = dc.showDialog(dialog);
        if (selected == null) return;

        // Verificar que sea un backup válido
        if (!Files.exists(selected.toPath().resolve("inventario.db"))) {
            NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                    "Backup Inválido",
                    "La carpeta seleccionada no contiene inventario.db",
                    TemaManager.COLOR_DANGER);
            return;
        }

        restaurarBackupDirecto(selected.toPath());
    }

    /**
     * Restaurar un backup específico dado su path — método central de restauración.
     * Usado tanto por el botón "Restaurar" en cada fila como por seleccionarYRestaurar().
     */
    private static void restaurarBackupDirecto(Path backupPath) {
        // Verificar que el backup existe y tiene DB
        if (!Files.exists(backupPath) || !Files.exists(backupPath.resolve("inventario.db"))) {
            NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                    "Backup Inválido", "No se encontró inventario.db en el backup", TemaManager.COLOR_DANGER);
            return;
        }

        String nombreBackup = backupPath.getFileName().toString().replace("backup_", "").replace("_", " ");
        if (!DialogosFX.confirmarAccion(dialog, "Restaurar Backup",
                "⚠ ADVERTENCIA: Esto sobrescribirá TODOS los datos actuales.\n\n" +
                "Backup: " + nombreBackup + "\n\n" +
                "Se creará un respaldo de seguridad antes de restaurar.\n" +
                "¿Desea continuar?")) {
            return;
        }

        Stage progress = crearMiniProgreso("Restaurando backup...");
        progress.show();

        new Thread(() -> {
            String dbDir = PortablePaths.getDatabaseDir().toString();
            Path dbPath = Paths.get(dbDir, "inventario.db");
            int excelCount = 0;
            int securityCount = 0;

            try {
                // ── 1. CERRAR CONEXIONES ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Cerrando conexiones de base de datos..."));
                System.out.println("[Restore] Cerrando DatabaseManager...");
                try { DatabaseManager.shutdown(); } catch (Exception ex) { System.err.println("[Restore] Error cerrando DB: " + ex.getMessage()); }
                Thread.sleep(500);

                // ── 2. BACKUP DE SEGURIDAD DE LA DB ACTUAL ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Creando respaldo de seguridad..."));
                if (Files.exists(dbPath)) {
                    String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    Path safety = Paths.get(dbDir, "inventario_pre_restore_" + ts + ".db");
                    Files.copy(dbPath, safety, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[Restore] Backup de seguridad: " + safety.getFileName());
                }

                // ── 3. RESTAURAR CLAVES DE ENCRIPTACIÓN (PRIMERO — necesarias para la DB) ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Restaurando claves de seguridad..."));
                Path secBackup = backupPath.resolve("security");
                if (Files.exists(secBackup)) {
                    Path targetSec = PortablePaths.getSecurityDir();
                    Files.createDirectories(targetSec);
                    try (Stream<Path> files = Files.list(secBackup)) {
                        List<Path> secFiles = files.toList();
                        for (Path src : secFiles) {
                            Files.copy(src, targetSec.resolve(src.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                            securityCount++;
                        }
                    }
                    System.out.println("[Restore] " + securityCount + " claves restauradas");
                }

                // ── 4. RESTAURAR BASE DE DATOS ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Restaurando base de datos..."));
                Path dbBackup = backupPath.resolve("inventario.db");
                long backupSize = Files.size(dbBackup);

                // Eliminar WAL/SHM residuales antes de copiar
                Files.deleteIfExists(Paths.get(dbDir, "inventario.db-wal"));
                Files.deleteIfExists(Paths.get(dbDir, "inventario.db-shm"));

                Files.copy(dbBackup, dbPath, StandardCopyOption.REPLACE_EXISTING);

                // Verificar copia
                long restoredSize = Files.size(dbPath);
                if (restoredSize != backupSize) {
                    throw new IOException("Verificación fallida: backup=" + backupSize + " restaurado=" + restoredSize);
                }
                System.out.println("[Restore] DB restaurada: " + formatSize(restoredSize));

                // ── 5. RESTAURAR ARCHIVOS EXCEL ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Restaurando archivos Excel..."));
                Path excelBackup = backupPath.resolve("excel");
                if (Files.exists(excelBackup)) {
                    try {
                        String carpetaProyectos = AdminManager.getRutasTrabajo().getCarpetaProyectos();
                        if (carpetaProyectos != null) {
                            Path targetDir = Paths.get(carpetaProyectos);
                            Files.createDirectories(targetDir);
                            try (Stream<Path> files = Files.list(excelBackup)) {
                                List<Path> excels = files.filter(p -> p.toString().endsWith(".xlsx")).toList();
                                for (Path excel : excels) {
                                    Files.copy(excel, targetDir.resolve(excel.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                                    excelCount++;
                                }
                            }
                            System.out.println("[Restore] " + excelCount + " archivos Excel restaurados");
                        }
                    } catch (Exception excelEx) {
                        System.err.println("[Restore] Advertencia Excel: " + excelEx.getMessage());
                    }
                }

                // ── 6. RESTAURAR CONFIGURACIÓN ──
                Path configBackup = backupPath.resolve("application.properties");
                if (Files.exists(configBackup)) {
                    Path configTarget = PortablePaths.getApplicationProperties();
                    Files.createDirectories(configTarget.getParent());
                    Files.copy(configBackup, configTarget, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[Restore] Configuración restaurada");
                }

                // ── 7. RESTAURAR REPORTES DE MANTENIMIENTO (.dat) ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Restaurando reportes de mantenimiento..."));
                Path reportesDatBackup = backupPath.resolve("reportes_mantenimiento.dat");
                if (Files.exists(reportesDatBackup)) {
                    Path target = PortablePaths.getReportesFile();
                    
                    // Backup de seguridad del actual
                    if (Files.exists(target)) {
                        String ts2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        Path preRestore = Paths.get(target.toString() + ".pre-restore." + ts2);
                        Files.copy(target, preRestore, StandardCopyOption.REPLACE_EXISTING);
                    }
                    
                    Files.copy(reportesDatBackup, target, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[Restore] Reportes de mantenimiento restaurados: " + formatSize(Files.size(target)));
                    
                    // Restaurar .bak si existe
                    Path bakBackup = backupPath.resolve("reportes_mantenimiento.dat.bak");
                    if (Files.exists(bakBackup)) {
                        Files.copy(bakBackup, Paths.get(target.toString() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    System.out.println("[Restore] No se encontró reportes_mantenimiento.dat en el backup");
                }

                // ── 8. RESTAURAR BORRADORES ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Restaurando borradores..."));
                Path borradoresBackup = backupPath.resolve("borradores");
                if (Files.exists(borradoresBackup)) {
                    Path borradoresTarget = PortablePaths.getBorradoresDir();
                    Files.createDirectories(borradoresTarget);
                    int borradoresCount = 0;
                    try (Stream<Path> bFiles = Files.list(borradoresBackup)) {
                        List<Path> bList = bFiles.filter(Files::isRegularFile).toList();
                        for (Path b : bList) {
                            Files.copy(b, borradoresTarget.resolve(b.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                            borradoresCount++;
                        }
                    }
                    System.out.println("[Restore] " + borradoresCount + " borradores restaurados");
                }

                // ── 9. RESTAURAR FIRMAS DIGITALES ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Restaurando firmas digitales..."));
                int firmasCount = 0;
                Path firmasBackup = backupPath.resolve("firmas");
                if (Files.exists(firmasBackup)) {
                    Path firmasTarget = PortablePaths.getFirmasDir();
                    Files.createDirectories(firmasTarget);
                    try (Stream<Path> fFiles = Files.list(firmasBackup)) {
                        List<Path> fList = fFiles.filter(Files::isRegularFile).toList();
                        for (Path f : fList) {
                            Files.copy(f, firmasTarget.resolve(f.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                            firmasCount++;
                        }
                    }
                    System.out.println("[Restore] " + firmasCount + " firmas digitales restauradas");
                }

                // ── 10. RESTAURAR MASTER KEY Y CONFIG ADICIONAL ──
                Path masterKeyBackup = backupPath.resolve("master.key");
                if (Files.exists(masterKeyBackup)) {
                    Files.copy(masterKeyBackup, PortablePaths.getMasterKeyFile(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[Restore] Master key restaurada");
                }
                Path configPropsBackup = backupPath.resolve("config.properties");
                if (Files.exists(configPropsBackup)) {
                    Files.copy(configPropsBackup, PortablePaths.getConfigProperties(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[Restore] config.properties restaurado");
                }

                // ── 11. REINICIALIZAR SISTEMA ──
                Platform.runLater(() -> actualizarMiniProgreso(progress, "Reiniciando sistema..."));
                DatabaseManager.initialize();
                Thread.sleep(300);
                AdminManager.recargarProyectos();
                Thread.sleep(300);

                final int totalExcel = excelCount;
                final int totalSecurity = securityCount;
                final int totalFirmas = firmasCount;

                System.out.println("[Restore] === RESTAURACIÓN COMPLETADA ===");
                System.out.println("[Restore] DB: " + formatSize(restoredSize));
                System.out.println("[Restore] Excel: " + totalExcel);
                System.out.println("[Restore] Claves: " + totalSecurity);
                System.out.println("[Restore] Firmas: " + totalFirmas);

                Platform.runLater(() -> {
                    progress.close();
                    NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                            "Restauración Completada",
                            "DB restaurada (" + formatSize(restoredSize) + ")\n" +
                            totalExcel + " Excel • " + totalSecurity + " claves • " + totalFirmas + " firmas",
                            TemaManager.COLOR_SUCCESS);
                    cargarEstadoAsync();
                });

            } catch (Exception ex) {
                AppLogger.getLogger(MantenimientoFX.class).error("[Restore] ERROR: " + ex.getMessage(), ex);
                Platform.runLater(() -> {
                    progress.close();
                    // Intentar reinicializar de todas formas
                    try { DatabaseManager.initialize(); } catch (Exception dbEx) { System.err.println("[Restore] Error reinicializando DB: " + dbEx.getMessage()); }
                    NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                            "Error en Restauración", ex.getMessage(), TemaManager.COLOR_DANGER);
                });
            }
        }, "Mant-RestaurarBackup").start();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EJECUCIÓN: LIMPIEZA
    // ════════════════════════════════════════════════════════════════════════

    private static void limpiarTemporales() {
        if (!DialogosFX.confirmarAccion(dialog, "Limpiar Temporales",
                "Se eliminarán archivos temporales (.tmp), copias pre-restauración\n" +
                "y cachés del sistema.\n\n¿Continuar?")) {
            return;
        }

        new Thread(() -> {
            int eliminados = 0;
            long liberado = 0;

            String dbDir = PortablePaths.getDatabaseDir().toString();
            Path dbPath = Paths.get(dbDir);

            try {
                if (Files.exists(dbPath)) {
                    try (Stream<Path> files = Files.list(dbPath)) {
                        List<Path> temps = files.filter(p -> {
                            String name = p.getFileName().toString();
                            return name.endsWith(".tmp") ||
                                   name.contains("pre_restore_") ||
                                   name.endsWith(".bak");
                        }).toList();

                        for (Path f : temps) {
                            try {
                                liberado += Files.size(f);
                                Files.delete(f);
                                eliminados++;
                            } catch (IOException ignored) { AppLogger.getLogger(MantenimientoFX.class).warn("Error eliminando temporal: " + ignored.getMessage()); }
                        }
                    }
                }
            } catch (IOException ignored) {}

            final int count = eliminados;
            final long freed = liberado;

            Platform.runLater(() -> {
                NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                        "Limpieza Completada",
                        count + " archivos eliminados • " + formatSize(freed) + " liberados",
                        TemaManager.COLOR_SUCCESS);
                cargarEstadoAsync();
            });
        }, "Mant-LimpiarTemporales").start();
    }

    private static void limpiarBackupsAntiguos() {
        if (!DialogosFX.confirmarAccion(dialog, "Limpiar Backups Antiguos",
                "Se conservarán los últimos 5 backups y se eliminarán\nlos más antiguos.\n\n¿Continuar?")) {
            return;
        }

        new Thread(() -> {
            int eliminados = 0;
            long liberado = 0;

            try {
                Path backupDir = Paths.get(BACKUP_DIR);
                if (Files.exists(backupDir)) {
                    try (Stream<Path> dirs = Files.list(backupDir)) {
                        List<Path> backups = dirs
                            .filter(Files::isDirectory)
                            .filter(p -> p.getFileName().toString().startsWith("backup_"))
                            .sorted(Comparator.<Path, String>comparing(p -> p.getFileName().toString()).reversed())
                            .toList();

                        if (backups.size() > 5) {
                            for (int i = 5; i < backups.size(); i++) {
                                try {
                                    liberado += dirSize(backups.get(i));
                                    deleteDir(backups.get(i));
                                    eliminados++;
                                } catch (IOException ignored) { AppLogger.getLogger(MantenimientoFX.class).warn("Error eliminando backup antiguo: " + ignored.getMessage()); }
                            }
                        }
                    }
                }
            } catch (IOException ignored) {}

            final int count = eliminados;
            final long freed = liberado;

            Platform.runLater(() -> {
                String msg = count > 0
                        ? count + " backups eliminados • " + formatSize(freed) + " liberados"
                        : "No hay backups antiguos para eliminar";
                NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                        "Limpieza de Backups", msg,
                        count > 0 ? TemaManager.COLOR_SUCCESS : TemaManager.COLOR_INFO);
                cargarEstadoAsync();
            });
        }, "Mant-LimpiarBackups").start();
    }

    private static void limpiarWAL() {
        if (!DialogosFX.confirmarAccion(dialog, "Limpiar WAL",
                "Se ejecutará CHECKPOINT para vaciar el archivo WAL\n" +
                "y luego se eliminará.\n\n¿Continuar?")) {
            return;
        }

        new Thread(() -> {
            boolean ok = false;
            long freed = 0;

            try {
                // Checkpoint primero
                try (Connection conn = DatabaseManager.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA wal_checkpoint(TRUNCATE)");
                }

                // Eliminar WAL y SHM
                String dbDir = PortablePaths.getDatabaseDir().toString();
                Path wal = Paths.get(dbDir, "inventario.db-wal");
                Path shm = Paths.get(dbDir, "inventario.db-shm");

                if (Files.exists(wal)) { freed += Files.size(wal); Files.deleteIfExists(wal); }
                if (Files.exists(shm)) { freed += Files.size(shm); Files.deleteIfExists(shm); }

                ok = true;
            } catch (Exception ignored) { AppLogger.getLogger(MantenimientoFX.class).warn("Error limpiando WAL: " + ignored.getMessage()); }

            final boolean success = ok;
            final long f = freed;

            Platform.runLater(() -> {
                NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                        success ? "WAL Limpiado" : "Error",
                        success ? "Archivos WAL eliminados • " + formatSize(f) + " liberados"
                                : "No se pudo limpiar el WAL",
                        success ? TemaManager.COLOR_SUCCESS : TemaManager.COLOR_DANGER);
                cargarEstadoAsync();
            });
        }, "Mant-CompactarWAL").start();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CARGA DE ESTADO ASYNC
    // ════════════════════════════════════════════════════════════════════════

    private static void cargarEstadoAsync() {
        new Thread(() -> {
            // Info de la DB
            String dbSizeStr = "No disponible";
            String dbStatusStr = "Desconocido";
            try {
                String dbDir = PortablePaths.getDatabaseDir().toString();
                Path dbFile = Paths.get(dbDir, "inventario.db");
                if (Files.exists(dbFile)) {
                    long size = Files.size(dbFile);
                    // Incluir WAL
                    Path wal = Paths.get(dbDir, "inventario.db-wal");
                    if (Files.exists(wal)) size += Files.size(wal);
                    dbSizeStr = formatSize(size);
                }

                // Quick check con quick_check en vez de integrity_check (mucho más rápido)
                try (Connection conn = DatabaseManager.getConnection();
                     Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("PRAGMA quick_check")) {
                    String r = rs.next() ? rs.getString(1) : "error";
                    dbStatusStr = "ok".equalsIgnoreCase(r) ? "✓ Integridad OK" : "⚠ " + r;
                }
            } catch (Exception e) {
                dbStatusStr = "Error de conexión";
            }

            // Info de backups
            int backupCount = 0;
            String lastBackup = "Ninguno";
            List<BackupEntry> entries = new ArrayList<>();
            try {
                Path bdir = Paths.get(BACKUP_DIR);
                if (Files.exists(bdir)) {
                    try (Stream<Path> dirs = Files.list(bdir)) {
                        List<Path> bkList = dirs
                            .filter(Files::isDirectory)
                            .filter(p -> p.getFileName().toString().startsWith("backup_"))
                            .sorted(Comparator.<Path, String>comparing(p -> p.getFileName().toString()).reversed())
                            .toList();

                        backupCount = bkList.size();

                        // Solo cargar detalles de los primeros 10 (los que se muestran)
                        int limit = Math.min(bkList.size(), 10);
                        for (int i = 0; i < limit; i++) {
                            entries.add(new BackupEntry(bkList.get(i)));
                        }

                        if (!bkList.isEmpty()) {
                            String ts = bkList.get(0).getFileName().toString().replace("backup_", "");
                            lastBackup = ts.replace("_", " ");
                        }
                    }
                }
            } catch (IOException ignored) {}

            // Info temporales
            String tmpStr = "0 B";
            try {
                String dbDir = PortablePaths.getDatabaseDir().toString();
                Path dbPath = Paths.get(dbDir);
                if (Files.exists(dbPath)) {
                    long tmpSize = 0;
                    int tmpCount = 0;
                    try (Stream<Path> files = Files.list(dbPath)) {
                        List<Path> temps = files.filter(p -> {
                            String name = p.getFileName().toString();
                            return name.endsWith(".tmp") || name.contains("pre_restore_") ||
                                   name.endsWith(".bak") || name.endsWith("-wal") || name.endsWith("-shm");
                        }).toList();
                        for (Path t : temps) {
                            tmpSize += Files.size(t);
                            tmpCount++;
                        }
                    }
                    tmpStr = tmpCount + " archivos • " + formatSize(tmpSize);
                }
            } catch (IOException ignored) {}

            // Actualizar UI
            final String fDbSize = dbSizeStr;
            final String fDbStatus = dbStatusStr;
            final int fBkCount = backupCount;
            final String fLastBk = lastBackup;
            final String fTmp = tmpStr;
            final List<BackupEntry> fEntries = entries;

            Platform.runLater(() -> {
                if (lblDbSize != null) lblDbSize.setText(fDbSize);
                if (lblDbStatus != null) lblDbStatus.setText(fDbStatus);
                if (lblBackupCount != null) lblBackupCount.setText(fBkCount + " backups");
                if (lblBackupLast != null) lblBackupLast.setText("Último: " + fLastBk);
                if (lblTempSize != null) lblTempSize.setText(fTmp);

                if (backupListContainer != null) {
                    actualizarListaBackups(fEntries);
                }
            });
        }, "Mant-CargarEstado").start();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LISTA DE BACKUPS
    // ════════════════════════════════════════════════════════════════════════

    private static void actualizarListaBackups(List<BackupEntry> entries) {
        backupListContainer.getChildren().clear();

        if (entries.isEmpty()) {
            HBox empty = new HBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(20));
            StackPane ic = new StackPane(IconosSVG.info("#6366F1", 20));
            Label lbl = new Label("No hay copias de seguridad disponibles. Crea tu primer backup.");
            lbl.setFont(Font.font("Segoe UI", 13));
            lbl.setTextFill(Color.web(TemaManager.getTextMuted()));
            empty.getChildren().addAll(ic, lbl);
            backupListContainer.getChildren().add(empty);
            return;
        }

        // Header de la tabla
        HBox tableHeader = new HBox(12);
        tableHeader.setPadding(new Insets(8, 12, 8, 12));
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        Label hFecha = new Label("FECHA");
        hFecha.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        hFecha.setTextFill(Color.web(TemaManager.getTextMuted()));
        hFecha.setPrefWidth(180);

        Label hArchivos = new Label("ARCHIVOS");
        hArchivos.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        hArchivos.setTextFill(Color.web(TemaManager.getTextMuted()));
        hArchivos.setPrefWidth(80);

        Label hTamano = new Label("TAMAÑO");
        hTamano.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        hTamano.setTextFill(Color.web(TemaManager.getTextMuted()));
        hTamano.setPrefWidth(100);

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Label hAcciones = new Label("ACCIONES");
        hAcciones.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        hAcciones.setTextFill(Color.web(TemaManager.getTextMuted()));

        tableHeader.getChildren().addAll(hFecha, hArchivos, hTamano, hSpacer, hAcciones);
        backupListContainer.getChildren().add(tableHeader);

        // Mostrar máximo 10 backups
        int max = Math.min(entries.size(), 10);
        for (int i = 0; i < max; i++) {
            BackupEntry entry = entries.get(i);
            boolean isFirst = (i == 0);

            HBox row = new HBox(12);
            row.setPadding(new Insets(10, 12, 10, 12));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                "-fx-background-color: " + (i % 2 == 0 ? "transparent" : TemaManager.getBg()) + ";" +
                "-fx-background-radius: 8;"
            );

            // Badge "Más reciente"
            VBox fechaBox = new VBox(2);
            Label fecha = new Label(entry.timestamp.replace("_", " "));
            fecha.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
            fecha.setTextFill(Color.web(TemaManager.getText()));
            fechaBox.getChildren().add(fecha);
            if (isFirst) {
                Label badge = new Label("Más reciente");
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
                badge.setTextFill(Color.web(TemaManager.COLOR_SUCCESS));
                badge.setStyle("-fx-background-color: " + TemaManager.COLOR_SUCCESS + "15; -fx-padding: 2 6; -fx-background-radius: 4;");
                fechaBox.getChildren().add(badge);
            }
            fechaBox.setPrefWidth(180);

            Label archivos = new Label(String.valueOf(entry.fileCount));
            archivos.setFont(Font.font("Segoe UI", 12));
            archivos.setTextFill(Color.web(TemaManager.getTextMuted()));
            archivos.setPrefWidth(80);

            Label tamano = new Label(entry.sizeFormatted);
            tamano.setFont(Font.font("Segoe UI", 12));
            tamano.setTextFill(Color.web(TemaManager.getTextMuted()));
            tamano.setPrefWidth(100);

            Region rowSpacer = new Region();
            HBox.setHgrow(rowSpacer, Priority.ALWAYS);

            // ── BOTÓN RESTAURAR ──
            Button btnRestaurar = new Button();
            HBox restContent = new HBox(4);
            restContent.setAlignment(Pos.CENTER);
            Label restLabel = new Label("Restaurar");
            restLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
            restLabel.setTextFill(Color.web(TemaManager.COLOR_SUCCESS));
            restContent.getChildren().addAll(new StackPane(IconosSVG.subir(TemaManager.COLOR_SUCCESS, 13)), restLabel);
            btnRestaurar.setGraphic(restContent);
            btnRestaurar.setPrefHeight(32);
            btnRestaurar.setStyle(
                "-fx-background-color: " + TemaManager.COLOR_SUCCESS + "15;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: " + TemaManager.COLOR_SUCCESS + "40;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 0 10;"
            );
            btnRestaurar.setTooltip(new Tooltip("Restaurar este backup"));
            btnRestaurar.setOnMouseEntered(e -> btnRestaurar.setStyle(
                "-fx-background-color: " + TemaManager.COLOR_SUCCESS + "30; -fx-background-radius: 8; -fx-cursor: hand;" +
                "-fx-border-color: " + TemaManager.COLOR_SUCCESS + "; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 0 10;"
            ));
            btnRestaurar.setOnMouseExited(e -> btnRestaurar.setStyle(
                "-fx-background-color: " + TemaManager.COLOR_SUCCESS + "15; -fx-background-radius: 8; -fx-cursor: hand;" +
                "-fx-border-color: " + TemaManager.COLOR_SUCCESS + "40; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 0 10;"
            ));
            final Path restorePath = entry.path;
            btnRestaurar.setOnAction(e -> restaurarBackupDirecto(restorePath));

            // ── BOTÓN ELIMINAR ──
            Button btnEliminar = new Button();
            btnEliminar.setGraphic(new StackPane(IconosSVG.eliminar(TemaManager.COLOR_DANGER, 14)));
            btnEliminar.setPrefSize(32, 32);
            btnEliminar.setStyle(
                "-fx-background-color: " + TemaManager.COLOR_DANGER + "15;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: transparent;"
            );
            btnEliminar.setTooltip(new Tooltip("Eliminar este backup"));
            btnEliminar.setOnMouseEntered(e -> btnEliminar.setStyle(
                "-fx-background-color: " + TemaManager.COLOR_DANGER + "35; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: transparent;"
            ));
            btnEliminar.setOnMouseExited(e -> btnEliminar.setStyle(
                "-fx-background-color: " + TemaManager.COLOR_DANGER + "15; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: transparent;"
            ));
            final Path entryPath = entry.path;
            btnEliminar.setOnAction(e -> eliminarBackup(entryPath));

            // ── Acciones box ──
            HBox accionesBox = new HBox(6);
            accionesBox.setAlignment(Pos.CENTER_RIGHT);
            accionesBox.getChildren().addAll(btnRestaurar, btnEliminar);

            row.getChildren().addAll(fechaBox, archivos, tamano, rowSpacer, accionesBox);
            backupListContainer.getChildren().add(row);
        }

        if (entries.size() > 10) {
            Label more = new Label("... y " + (entries.size() - 10) + " backups más");
            more.setFont(Font.font("Segoe UI", 11));
            more.setTextFill(Color.web(TemaManager.getTextMuted()));
            more.setPadding(new Insets(8, 0, 0, 12));
            backupListContainer.getChildren().add(more);
        }
    }

    private static void eliminarBackup(Path backupPath) {
        if (!DialogosFX.confirmarAccion(dialog, "Eliminar Backup",
                "¿Eliminar este backup?\n\n" + backupPath.getFileName() + "\n\nEsta acción no se puede deshacer.")) {
            return;
        }

        new Thread(() -> {
            try {
                deleteDir(backupPath);
                Platform.runLater(() -> {
                    NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                            "Backup Eliminado",
                            backupPath.getFileName().toString(),
                            TemaManager.COLOR_SUCCESS);
                    cargarEstadoAsync();
                });
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                            "Error", "No se pudo eliminar: " + ex.getMessage(), TemaManager.COLOR_DANGER);
                });
            }
        }, "Mant-EliminarBackup").start();
    }

    /**
     * Reinicia el sistema completo: elimina TODOS los datos excepto la contraseña.
     * Borra: proyectos, reportes, inventarios, logs, backups, Excel cifrados,
     * firmas, borradores y archivos de configuración.
     */
    private static void reiniciarSistemaCompleto() {
        // Primer diálogo de confirmación
        Alert confirm1 = new Alert(Alert.AlertType.WARNING);
        confirm1.setTitle("\u26a0 Reiniciar Sistema");
        confirm1.setHeaderText("\u00bfEst\u00e1 seguro de reiniciar TODO el sistema?");
        confirm1.setContentText(
            "Esta acci\u00f3n ELIMINAR\u00c1 permanentemente:\n\n" +
            "  \u2022 Todos los proyectos y sus datos\n" +
            "  \u2022 Todos los reportes de mantenimiento\n" +
            "  \u2022 Todos los inventarios registrados\n" +
            "  \u2022 Todos los logs de auditor\u00eda y acceso\n" +
            "  \u2022 Todos los backups\n" +
            "  \u2022 Todos los archivos Excel cifrados\n" +
            "  \u2022 Firmas, borradores y configuraci\u00f3n\n\n" +
            "Solo se conservar\u00e1 la contrase\u00f1a de administrador.\n\n" +
            "\u00a1ESTA ACCI\u00d3N NO SE PUEDE DESHACER!"
        );
        confirm1.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result1 = confirm1.showAndWait();
        if (result1.isEmpty() || result1.get() != ButtonType.OK) return;

        // Segundo diálogo de confirmación (doble seguridad)
        TextInputDialog confirm2 = new TextInputDialog();
        confirm2.setTitle("Confirmaci\u00f3n Final");
        confirm2.setHeaderText("Escriba REINICIAR para confirmar");
        confirm2.setContentText("Texto de confirmaci\u00f3n:");
        
        Optional<String> result2 = confirm2.showAndWait();
        if (result2.isEmpty() || !"REINICIAR".equals(result2.get().trim())) {
            NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                "Cancelado", "No se escribi\u00f3 REINICIAR. Operaci\u00f3n cancelada.", TemaManager.COLOR_WARNING);
            return;
        }

        new Thread(() -> {
            try {
                System.out.println("[Mantenimiento] \ud83d\uddd1 Iniciando reinicio completo del sistema...");

                // 1. Limpiar TODAS las tablas de la base de datos (excepto esquema)
                try (var conn = inventario.fx.database.DatabaseManager.getConnection();
                     var stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM reportes");
                    stmt.executeUpdate("DELETE FROM inventarios");
                    stmt.executeUpdate("DELETE FROM logs_auditoria");
                    stmt.executeUpdate("DELETE FROM logs_acceso");
                    stmt.executeUpdate("DELETE FROM configuracion");
                    stmt.executeUpdate("DELETE FROM empresa");
                    stmt.executeUpdate("DELETE FROM proyectos");
                    stmt.executeUpdate("VACUUM");
                    System.out.println("[Mantenimiento]   \u2705 Base de datos limpiada");
                }

                // 2. Eliminar archivos Excel cifrados
                Path proyectosDir = inventario.fx.config.PortablePaths.getProyectosDir();
                if (Files.exists(proyectosDir)) {
                    try (var archivos = Files.list(proyectosDir)) {
                        archivos.forEach(f -> {
                            try { Files.deleteIfExists(f); } catch (Exception ignored) {}
                        });
                    }
                    System.out.println("[Mantenimiento]   \u2705 Excel cifrados eliminados");
                }

                // 3. Eliminar backups
                Path backupsDir = inventario.fx.config.PortablePaths.getBackupsDir();
                if (Files.exists(backupsDir)) {
                    borrarContenidoRecursivo(backupsDir);
                    System.out.println("[Mantenimiento]   \u2705 Backups eliminados");
                }

                // 4. Eliminar firmas
                Path firmasDir = inventario.fx.config.PortablePaths.getFirmasDir();
                if (Files.exists(firmasDir)) {
                    borrarContenidoRecursivo(firmasDir);
                    System.out.println("[Mantenimiento]   \u2705 Firmas eliminadas");
                }

                // 5. Eliminar borradores
                Path borradoresDir = inventario.fx.config.PortablePaths.getBorradoresDir();
                if (Files.exists(borradoresDir)) {
                    borrarContenidoRecursivo(borradoresDir);
                    System.out.println("[Mantenimiento]   \u2705 Borradores eliminados");
                }

                // 6. Eliminar logs
                Path logsDir = inventario.fx.config.PortablePaths.getLogsDir();
                if (Files.exists(logsDir)) {
                    borrarContenidoRecursivo(logsDir);
                    System.out.println("[Mantenimiento]   \u2705 Logs eliminados");
                }

                // 7. Eliminar reportes de mantenimiento (.dat)
                Files.deleteIfExists(inventario.fx.config.PortablePaths.getReportesFile());
                Files.deleteIfExists(inventario.fx.config.PortablePaths.getReportesBak());
                System.out.println("[Mantenimiento]   \u2705 Archivos de reportes eliminados");

                // 8. Eliminar config.properties (NO las claves de cifrado ni master.key)
                Files.deleteIfExists(inventario.fx.config.PortablePaths.getConfigProperties());
                System.out.println("[Mantenimiento]   \u2705 Configuraci\u00f3n limpiada");

                // 9. Eliminar exportaciones
                Path exportDir = inventario.fx.config.PortablePaths.getExportacionesDir();
                if (Files.exists(exportDir)) {
                    borrarContenidoRecursivo(exportDir);
                    System.out.println("[Mantenimiento]   \u2705 Exportaciones eliminadas");
                }

                System.out.println("[Mantenimiento] \u2705 Sistema reiniciado completamente");

                javafx.application.Platform.runLater(() -> {
                    NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                        "\u2705 Sistema Reiniciado",
                        "Todos los datos han sido eliminados. Reinicie la aplicaci\u00f3n.",
                        TemaManager.COLOR_SUCCESS);
                    cargarEstadoAsync();
                });

            } catch (Exception e) {
                System.err.println("[Mantenimiento] Error en reinicio: " + e.getMessage());
                javafx.application.Platform.runLater(() ->
                    NotificacionesFX.mostrarNotificacion(contenedorRaiz,
                        "Error", "Error al reiniciar: " + e.getMessage(), TemaManager.COLOR_DANGER));
            }
        }, "Reinicio-Sistema").start();
    }

    /** Elimina todo el contenido de una carpeta (archivos y subcarpetas) sin borrar la carpeta misma. */
    private static void borrarContenidoRecursivo(Path carpeta) {
        try (var walker = Files.walk(carpeta)) {
            walker.sorted(java.util.Comparator.reverseOrder())
                  .filter(p -> !p.equals(carpeta))
                  .forEach(p -> {
                      try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                  });
        } catch (Exception ignored) {}
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILIDADES UI
    // ════════════════════════════════════════════════════════════════════════

    private static HBox crearTituloSeccion(String texto, String color, Node icono) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 0, 4, 0));

        StackPane iconW = new StackPane(icono);
        iconW.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 8; -fx-padding: 6;");
        iconW.setPrefSize(34, 34);
        iconW.setMinSize(34, 34);

        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lbl.setTextFill(Color.web(TemaManager.getText()));

        row.getChildren().addAll(iconW, lbl);
        return row;
    }

    private static Button crearBotonAccion(String texto, Node icono, String color) {
        Button btn = new Button();
        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER);
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lbl.setTextFill(Color.WHITE);
        content.getChildren().addAll(new StackPane(icono), lbl);

        btn.setGraphic(content);
        btn.setPrefHeight(46);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setCursor(Cursor.HAND);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, " + color + "50, 8, 0, 0, 2);"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + color + "DD;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, " + color + "80, 12, 0, 0, 4);"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, " + color + "50, 8, 0, 0, 2);"
        ));
        return btn;
    }

    private static Button crearBotonAccionCompacto(String texto, Node icono, String color) {
        Button btn = new Button();
        HBox content = new HBox(6);
        content.setAlignment(Pos.CENTER);
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        lbl.setTextFill(Color.WHITE);
        content.getChildren().addAll(new StackPane(icono), lbl);

        btn.setGraphic(content);
        btn.setPrefHeight(32);
        btn.setCursor(Cursor.HAND);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 0 12;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + color + "DD;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 0 12;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 0 12;"
        ));
        return btn;
    }

    private static Button crearBotonCerrar() {
        return inventario.fx.util.ComponentesFX.crearBotonCerrar(dialog::close, 40);
    }

    /**
     * Diálogo de progreso compacto con spinner y mensaje actualizable.
     */
    private static Stage crearMiniProgreso(String textoInicial) {
        Stage pd = new Stage();
        pd.initModality(Modality.NONE);
        pd.initOwner(dialog);
        pd.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(28));
        root.setStyle(
            "-fx-background-color: " + TemaManager.getBg() + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + TemaManager.getBorder() + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);"
        );
        root.setPrefWidth(380);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(50, 50);

        Label lbl = new Label(textoInicial);
        lbl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lbl.setTextFill(Color.web(TemaManager.getText()));
        lbl.setWrapText(true);
        lbl.setAlignment(Pos.CENTER);

        root.getChildren().addAll(spinner, lbl);
        root.setUserData(lbl); // para actualizaciones

        StackPane wrapperMini = new StackPane(root);
        wrapperMini.setStyle("-fx-background-color: transparent;");
        wrapperMini.setPadding(new Insets(20));

        Scene scene = new Scene(wrapperMini);
        scene.setFill(Color.TRANSPARENT);
        pd.setScene(scene);

        return pd;
    }

    private static void actualizarMiniProgreso(Stage pd, String texto) {
        if (pd != null && pd.getScene() != null) {
            StackPane w = (StackPane) pd.getScene().getRoot();
            VBox root = (VBox) w.getChildren().get(0);
            Label lbl = (Label) root.getUserData();
            if (lbl != null) lbl.setText(texto);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILIDADES IO
    // ════════════════════════════════════════════════════════════════════════

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private static long dirSize(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(Files::isRegularFile)
                       .mapToLong(p -> { try { return Files.size(p); } catch (IOException e) { return 0; } })
                       .sum();
        }
    }

    private static void deleteDir(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(p -> { try { Files.delete(p); } catch (IOException ignored) {} });
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CLASE INTERNA: BackupEntry
    // ════════════════════════════════════════════════════════════════════════

    private static class BackupEntry {
        final Path path;
        final String timestamp;
        final int fileCount;
        final long size;
        final String sizeFormatted;

        BackupEntry(Path path) {
            this.path = path;
            this.timestamp = path.getFileName().toString().replace("backup_", "");

            // Listado ligero: solo archivos directos + subdirectorios de 1 nivel
            int fc = 0;
            long s = 0;
            try (Stream<Path> list = Files.list(path)) {
                List<Path> items = list.toList();
                for (Path item : items) {
                    if (Files.isRegularFile(item)) {
                        fc++;
                        try { s += Files.size(item); } catch (IOException ignored) {}
                    } else if (Files.isDirectory(item)) {
                        try (Stream<Path> sub = Files.list(item)) {
                            List<Path> subFiles = sub.filter(Files::isRegularFile).toList();
                            fc += subFiles.size();
                            for (Path sf : subFiles) {
                                try { s += Files.size(sf); } catch (IOException ignored) {}
                            }
                        } catch (IOException ignored) {}
                    }
                }
            } catch (IOException ignored) {}

            this.fileCount = fc;
            this.size = s;
            this.sizeFormatted = formatSize(s);
        }
    }
}
