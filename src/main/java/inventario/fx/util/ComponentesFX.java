package inventario.fx.util;

import inventario.fx.icons.IconosSVG;
import inventario.fx.model.TemaManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.InputStream;

/**
 * Fábrica de componentes UI reutilizables para la aplicación SELCOMP.
 * Centraliza la creación de sidebars, botones, cards y otros elementos
 * para evitar duplicación de código y garantizar consistencia visual.
 * 
 * @author SELCOMP
 * @version 1.0
 */
public class ComponentesFX {

    // ═══════════════════════════════════════════════════════════════════════════
    // LOGO CON FALLBACK (SVG → PNG → Texto)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea el logo de SELCOMP con cadena de fallback: SVG → PNG → círculo+texto.
     * @param accentColor Color del círculo fallback (ej: TemaManager.COLOR_PRIMARY)
     * @param size Ancho del logo (ej: 180)
     * @return HBox con el logo
     */
    public static HBox crearLogo(String accentColor, double size) {
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        ImageView logoSVG = SVGUtil.loadSVG("/icons/LogoSelcompSVG.svg", (int) size);
        if (logoSVG != null) {
            logoBox.getChildren().add(logoSVG);
            return logoBox;
        }

        // Fallback 1: PNG
        try {
            InputStream logoStream = ComponentesFX.class.getResourceAsStream("/images/Selcomp.png");
            if (logoStream != null) {
                Image logoImg = new Image(logoStream);
                ImageView logoView = new ImageView(logoImg);
                logoView.setFitWidth(size);
                logoView.setPreserveRatio(true);
                logoView.setSmooth(true);
                logoBox.getChildren().add(logoView);
                return logoBox;
            }
        } catch (Exception ignored) {}

        // Fallback 2: Círculo + texto
        crearLogoTexto(logoBox, accentColor);
        return logoBox;
    }

    private static void crearLogoTexto(HBox logoBox, String accentColor) {
        StackPane logoCircle = new StackPane();
        Circle circle = new Circle(20);
        circle.setFill(Color.web(accentColor));
        Label logoLetter = new Label("S");
        logoLetter.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        logoLetter.setTextFill(Color.WHITE);
        logoCircle.getChildren().addAll(circle, logoLetter);

        Label brandName = new Label("SELCOMP");
        brandName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        brandName.setTextFill(Color.web(TemaManager.getText()));

        logoBox.getChildren().addAll(logoCircle, brandName);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un sidebar estándar con la clase CSS .sidebar.
     * @param width Ancho preferido (220, 260, 280)
     * @return VBox configurado como sidebar
     */
    public static VBox crearSidebar(double width) {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(width);
        sidebar.setMinWidth(width);
        sidebar.setMaxWidth(width);
        sidebar.getStyleClass().add("sidebar");
        return sidebar;
    }

    /**
     * Crea una sección de logo para sidebar.
     * @param accentColor Color del acento para el logo fallback
     * @return VBox con el logo
     */
    public static VBox crearLogoSection(String accentColor) {
        return crearLogoSection(accentColor, null);
    }

    /**
     * Crea una sección de logo para sidebar con subtítulo opcional.
     * @param accentColor Color del acento para el logo fallback
     * @param subtitulo Texto bajo el logo (ej: nombre del proyecto), null para omitir
     * @return VBox con el logo y subtítulo
     */
    public static VBox crearLogoSection(String accentColor, String subtitulo) {
        VBox logoSection = new VBox(4);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        logoSection.setPadding(new Insets(24, 20, 24, 20));

        HBox logo = crearLogo(accentColor, 180);
        logoSection.getChildren().add(logo);

        if (subtitulo != null && !subtitulo.isEmpty()) {
            Label lblSubtitulo = new Label(subtitulo);
            lblSubtitulo.setFont(Font.font("Segoe UI", 12));
            lblSubtitulo.setTextFill(Color.web(TemaManager.getTextSecondary()));
            lblSubtitulo.setPadding(new Insets(4, 0, 0, 0));
            logoSection.getChildren().add(lblSubtitulo);
        }

        return logoSection;
    }

    /**
     * Crea un título de sección (ej: "NAVEGACIÓN", "ACCIONES").
     * Usa la clase CSS .section-title.
     */
    public static Label crearTituloSeccion(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("section-title");
        label.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 10));
        return label;
    }

    /**
     * Crea una sección de navegación para sidebar con título y botones.
     */
    public static VBox crearNavSection(String titulo, Button... botones) {
        VBox section = new VBox(4);
        section.setPadding(new Insets(16, 12, 16, 12));

        Label navTitle = crearTituloSeccion(titulo);
        section.getChildren().add(navTitle);
        section.getChildren().addAll(botones);

        return section;
    }

    /**
     * Crea la sección inferior del sidebar con border-top.
     * Usa la clase CSS .sidebar-bottom.
     */
    public static VBox crearBottomSection(Node... children) {
        VBox bottomSection = new VBox(8);
        bottomSection.setPadding(new Insets(16, 12, 20, 12));
        bottomSection.getStyleClass().add("sidebar-bottom");
        bottomSection.getChildren().addAll(children);
        return bottomSection;
    }

    /**
     * Crea un espaciador vertical flexible (Region con VGrow.ALWAYS).
     */
    public static Region crearSpacer() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BOTONES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un botón de sidebar/navegación con icono SVG.
     * Usa la clase CSS .sidebar-btn (con pseudo-clases :hover).
     * Si activo, agrega la clase .active.
     * 
     * @param texto Texto del botón
     * @param icono Nodo SVG del ícono
     * @param activo Si está seleccionado/activo
     * @return Button configurado
     */
    public static Button crearSidebarButton(String texto, Node icono, boolean activo) {
        Button btn = new Button(texto);
        btn.setGraphic(icono);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setPrefHeight(44);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setGraphicTextGap(12);
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));

        btn.getStyleClass().add("sidebar-btn");
        if (activo) {
            btn.getStyleClass().add("active");
        }

        return btn;
    }

    /**
     * Crea un botón de navegación con icono SVG (variante con HBox interno y Label separado).
     * Util cuando se necesita controlar color del Label independientemente.
     */
    public static Button crearNavButton(String texto, Node icono, boolean activo, String accentColor) {
        Button btn = new Button();

        HBox contenido = new HBox(10);
        contenido.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lbl.setTextFill(Color.web(activo ? accentColor : TemaManager.getTextSecondary()));
        contenido.getChildren().addAll(icono, lbl);

        btn.setGraphic(contenido);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setPrefHeight(42);
        btn.setAlignment(Pos.CENTER_LEFT);

        btn.getStyleClass().add("sidebar-btn");
        if (activo) {
            btn.getStyleClass().add("active");
        }

        return btn;
    }

    /**
     * Crea un botón de acción coloreado (para sidebar: Excel, Reportes, etc).
     * Usa la clase CSS .action-btn. El color de fondo se aplica con estilo inline
     * mínimo (solo el color dinámico que no puede estar en CSS).
     */
    public static Button crearActionButton(String texto, Node icono, String color) {
        Button btn = new Button();

        HBox contenido = new HBox(10);
        contenido.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(texto);
        lbl.setTextFill(Color.web(color));
        lbl.setFont(Font.font("Segoe UI", 12));
        contenido.getChildren().addAll(icono, lbl);

        btn.setGraphic(contenido);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setPrefHeight(38);
        btn.setAlignment(Pos.CENTER_LEFT);

        btn.getStyleClass().add("action-btn");
        // Color dinámico que no puede ser CSS puro
        btn.setStyle("-fx-background-color: " + color + "15;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + color + "30;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "15;"));

        return btn;
    }

    /**
     * Crea un botón "Volver" con flecha e interacción hover.
     * Usa la clase CSS .back-btn.
     *
     * @param texto Texto del botón (ej: "Volver al menú")
     * @return Button configurado
     */
    public static Button crearBotonVolver(String texto) {
        Button btn = new Button();

        HBox contenido = new HBox(8);
        contenido.setAlignment(Pos.CENTER_LEFT);
        Node flechaIcon = IconosSVG.flechaIzquierda(TemaManager.getTextSecondary(), 18);
        Label textoBtn = new Label(texto);
        textoBtn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        textoBtn.setTextFill(Color.web(TemaManager.getTextSecondary()));
        contenido.getChildren().addAll(flechaIcon, textoBtn);

        btn.setGraphic(contenido);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setPrefHeight(42);
        btn.setAlignment(Pos.CENTER_LEFT);

        btn.getStyleClass().add("back-btn");

        btn.setOnMouseEntered(e -> {
            contenido.getChildren().set(0, IconosSVG.flechaIzquierda("#FFFFFF", 18));
            textoBtn.setTextFill(Color.WHITE);
        });
        btn.setOnMouseExited(e -> {
            contenido.getChildren().set(0, IconosSVG.flechaIzquierda(TemaManager.getTextSecondary(), 18));
            textoBtn.setTextFill(Color.web(TemaManager.getTextSecondary()));
        });

        return btn;
    }

    /**
     * Crea un label de versión para el sidebar.
     */
    public static Label crearVersionLabel(String version) {
        Label lbl = new Label(version);
        lbl.setFont(Font.font("Segoe UI", 10));
        lbl.setTextFill(Color.web(TemaManager.getTextMuted()));
        lbl.setPadding(new Insets(8, 0, 0, 8));
        return lbl;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STAT CARDS / TARJETAS DE ESTADÍSTICAS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea una tarjeta de estadísticas moderna con animación de conteo.
     * Usa la clase CSS .stat-card.
     * 
     * @param titulo Título de la tarjeta
     * @param valor Valor a mostrar (se anima el conteo)
     * @param icono Nodo SVG del ícono
     * @param color Color del acento (para ícono wrapper y hover)
     * @return VBox configurado como stat card
     */
    public static VBox crearStatCard(String titulo, int valor, Node icono, String color) {
        return crearStatCardInternal(titulo, String.valueOf(valor), icono, color, valor, true);
    }

    /**
     * Crea una tarjeta de estadísticas moderna con texto (sin animación de conteo).
     */
    public static VBox crearStatCard(String titulo, String valor, Node icono, String color) {
        return crearStatCardInternal(titulo, valor, icono, color, 0, false);
    }

    private static VBox crearStatCardInternal(String titulo, String valorTexto, Node icono, 
                                               String color, int valorNumerico, boolean animar) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12));
        card.setMinWidth(180);
        card.getStyleClass().add("stat-card");

        // Sombra sutil con el color del ícono
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web(color, 0.15));
        sombra.setRadius(12);
        sombra.setOffsetY(3);
        card.setEffect(sombra);

        // Header con ícono y título
        HBox headerCard = new HBox(8);
        headerCard.setAlignment(Pos.CENTER_LEFT);

        StackPane iconWrapper = new StackPane(icono);
        iconWrapper.setPadding(new Insets(6));
        iconWrapper.getStyleClass().add("icon-wrapper");
        iconWrapper.setStyle("-fx-background-color: " + color + "15;");

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        lblTitulo.setTextFill(Color.web(TemaManager.getTextSecondary()));
        headerCard.getChildren().addAll(iconWrapper, lblTitulo);

        // Valor
        Label lblValor = new Label(animar ? "0" : valorTexto);
        lblValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblValor.setTextFill(Color.web(TemaManager.getText()));
        lblValor.setWrapText(false);
        lblValor.setMinWidth(Region.USE_PREF_SIZE);

        if (animar && valorNumerico > 0) {
            animarConteo(lblValor, 0, valorNumerico, 800);
        }

        card.getChildren().addAll(headerCard, lblValor);

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-border-color: " + color + "44;");
            sombra.setColor(Color.web(color, 0.25));
            sombra.setRadius(15);
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), card);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle("");
            sombra.setColor(Color.web(color, 0.15));
            sombra.setRadius(12);
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return card;
    }

    /**
     * Devuelve el Label de valor de una stat card para actualizar su texto externamente.
     * Busca el Label por índice (posición 1 = valor).
     */
    public static Label getValorLabel(VBox statCard) {
        if (statCard.getChildren().size() > 1 && statCard.getChildren().get(1) instanceof Label) {
            return (Label) statCard.getChildren().get(1);
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TOOLTIPS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un tooltip estilizado con el tema actual.
     */
    public static Tooltip crearTooltip(String texto) {
        Tooltip tooltip = new Tooltip(texto);
        tooltip.setFont(Font.font("Segoe UI", 12));
        tooltip.setShowDelay(Duration.millis(400));
        tooltip.setHideDelay(Duration.millis(200));
        tooltip.setStyle(TemaManager.getTooltipStyle());
        return tooltip;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Anima un conteo numérico en una Label, de {@code desde} a {@code hasta}.
     * Usa KeyFrame con ActionEvent handler directo (el mecanismo más seguro de JavaFX).
     * <p>
     * <b>Números pequeños (≤ 10):</b> Cada paso se muestra individualmente.
     * <b>Números grandes (> 10):</b> ~40 pasos con easing ease-out-cubic.
     * <p>
     * La referencia del Timeline se guarda en label.getProperties() para prevenir GC.
     */
    public static void animarConteo(Label label, int desde, int hasta, int duracionMs) {
        // Cancelar animación anterior en este label
        Object prev = label.getProperties().remove("_countAnim");
        if (prev instanceof javafx.animation.Timeline) {
            ((javafx.animation.Timeline) prev).stop();
        }

        if (desde == hasta) {
            label.setText(String.valueOf(hasta));
            return;
        }

        label.setText(String.valueOf(desde));

        int diferencia = Math.abs(hasta - desde);
        int dir = (hasta > desde) ? 1 : -1;

        javafx.animation.Timeline tl = new javafx.animation.Timeline();

        if (diferencia <= 10) {
            // ═══ NÚMEROS PEQUEÑOS: un ActionEvent por cada paso ═══
            int msPorPaso = diferencia <= 3 ? 300 : diferencia <= 6 ? 220 : 160;
            for (int s = 1; s <= diferencia; s++) {
                final int valor = desde + s * dir;
                tl.getKeyFrames().add(new javafx.animation.KeyFrame(
                    Duration.millis(s * msPorPaso),
                    evt -> label.setText(String.valueOf(valor))
                ));
            }
        } else {
            // ═══ NÚMEROS GRANDES: animación fluida con easing ═══
            int dur;
            if (diferencia <= 50) dur = 700;
            else if (diferencia <= 200) dur = 1000;
            else if (diferencia <= 500) dur = 1400;
            else dur = 1800;

            int totalSteps = 40;
            for (int f = 1; f <= totalSteps; f++) {
                double progress = (double) f / totalSteps;
                double eased = 1 - Math.pow(1 - progress, 3);
                final int valor = desde + (int) Math.round((hasta - desde) * eased);
                tl.getKeyFrames().add(new javafx.animation.KeyFrame(
                    Duration.millis(progress * dur),
                    evt -> label.setText(String.valueOf(valor))
                ));
            }
        }

        tl.setOnFinished(evt -> {
            label.setText(String.valueOf(hasta));
            label.getProperties().remove("_countAnim");
        });

        // Ref fuerte en el label para prevenir GC
        label.getProperties().put("_countAnim", tl);
        tl.play();
    }

    /**
     * Anima un conteo con retraso inicial integrado en el Timeline.
     * Todo en un solo Timeline (delay + animación) para máxima robustez.
     */
    public static void animarConteoConDelay(Label label, int desde, int hasta, int duracionMs, int delayMs) {
        // Cancelar animación anterior
        Object prev = label.getProperties().remove("_countAnim");
        if (prev instanceof javafx.animation.Timeline) {
            ((javafx.animation.Timeline) prev).stop();
        }

        if (desde == hasta) {
            label.setText(String.valueOf(hasta));
            return;
        }

        label.setText(String.valueOf(desde));

        int diferencia = Math.abs(hasta - desde);
        int dir = (hasta > desde) ? 1 : -1;

        javafx.animation.Timeline tl = new javafx.animation.Timeline();

        if (diferencia <= 10) {
            int msPorPaso = diferencia <= 3 ? 300 : diferencia <= 6 ? 220 : 160;
            for (int s = 1; s <= diferencia; s++) {
                final int valor = desde + s * dir;
                tl.getKeyFrames().add(new javafx.animation.KeyFrame(
                    Duration.millis(delayMs + s * msPorPaso),
                    evt -> label.setText(String.valueOf(valor))
                ));
            }
        } else {
            int dur;
            if (diferencia <= 50) dur = 700;
            else if (diferencia <= 200) dur = 1000;
            else if (diferencia <= 500) dur = 1400;
            else dur = 1800;

            int totalSteps = 40;
            for (int f = 1; f <= totalSteps; f++) {
                double progress = (double) f / totalSteps;
                double eased = 1 - Math.pow(1 - progress, 3);
                final int valor = desde + (int) Math.round((hasta - desde) * eased);
                tl.getKeyFrames().add(new javafx.animation.KeyFrame(
                    Duration.millis(delayMs + progress * dur),
                    evt -> label.setText(String.valueOf(valor))
                ));
            }
        }

        tl.setOnFinished(evt -> {
            label.setText(String.valueOf(hasta));
            label.getProperties().remove("_countAnim");
        });

        label.getProperties().put("_countAnim", tl);
        tl.play();
    }

    /**
     * Genera estilo inline de card con opción de hover.
     * Útil como puente hasta migrar completamente a CSS.
     */
    public static String cardStyle(String color, boolean hover) {
        return "-fx-background-color: " + TemaManager.getSurface() + ";" +
               "-fx-background-radius: 12;" +
               "-fx-border-color: " + (hover ? color + "60" : TemaManager.getBorder()) + ";" +
               "-fx-border-radius: 12;" +
               "-fx-border-width: 1.5;" +
               "-fx-effect: dropshadow(gaussian, " + 
               (hover ? color + "30" : "rgba(0,0,0,0.06)") + ", " +
               (hover ? "12" : "6") + ", 0, 0, " + (hover ? "4" : "2") + ");";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BREADCRUMBS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea una barra de breadcrumbs para navegación contextual.
     * Ejemplo: Proyecto X > Dashboard > Estadísticas
     *
     * @param items Textos del path (ej: "Proyecto ACME", "Dashboard", "Estadísticas")
     * @return HBox con los breadcrumbs estilizados
     */
    public static HBox crearBreadcrumbs(String... items) {
        HBox bar = new HBox();
        bar.getStyleClass().add("breadcrumb-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < items.length; i++) {
            Label item = new Label(items[i]);
            if (i == items.length - 1) {
                item.getStyleClass().addAll("breadcrumb-item", "active");
            } else {
                item.getStyleClass().add("breadcrumb-item");
            }
            bar.getChildren().add(item);

            if (i < items.length - 1) {
                Label sep = new Label("›");
                sep.getStyleClass().add("breadcrumb-separator");
                bar.getChildren().add(sep);
            }
        }

        return bar;
    }

    /**
     * Crea breadcrumbs con acciones click en items no-activos.
     * @param acciones Array de Runnable (uno por item, null para el activo/último)
     */
    public static HBox crearBreadcrumbs(String[] items, Runnable[] acciones) {
        HBox bar = crearBreadcrumbs(items);
        int itemIdx = 0;
        for (Node child : bar.getChildren()) {
            if (child.getStyleClass().contains("breadcrumb-item") && 
                !child.getStyleClass().contains("active")) {
                final int idx = itemIdx;
                if (acciones != null && idx < acciones.length && acciones[idx] != null) {
                    child.setOnMouseClicked(e -> acciones[idx].run());
                }
            }
            if (child.getStyleClass().contains("breadcrumb-item")) {
                itemIdx++;
            }
        }
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EMPTY STATE / ESTADO VACÍO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un estado vacío moderno con ícono, título, descripción y botón de acción.
     * Ideal para tablas vacías, listas sin datos, primer uso, etc.
     *
     * @param icono Nodo SVG del ícono grande (ej: IconosSVG.documento(..., 64))
     * @param titulo Título principal (ej: "Sin registros")
     * @param descripcion Texto explicativo
     * @param textoAccion Texto del botón (null para omitir botón)
     * @param accion Runnable del botón (null si no hay botón)
     * @return VBox estilizado como empty state
     */
    public static VBox crearEmptyState(Node icono, String titulo, String descripcion,
                                        String textoAccion, Runnable accion) {
        VBox emptyState = new VBox(12);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setAlignment(Pos.CENTER);

        // Ícono
        if (icono != null) {
            StackPane iconContainer = new StackPane(icono);
            iconContainer.getStyleClass().add("empty-state-icon");
            iconContainer.setMaxWidth(80);
            iconContainer.setMaxHeight(80);
            emptyState.getChildren().add(iconContainer);
        }

        // Título
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("empty-state-title");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        lblTitulo.setTextFill(Color.web(TemaManager.getText()));
        emptyState.getChildren().add(lblTitulo);

        // Descripción
        if (descripcion != null && !descripcion.isEmpty()) {
            Label lblDesc = new Label(descripcion);
            lblDesc.getStyleClass().add("empty-state-description");
            lblDesc.setFont(Font.font("Segoe UI", 13));
            lblDesc.setTextFill(Color.web(TemaManager.getTextMuted()));
            lblDesc.setWrapText(true);
            lblDesc.setTextAlignment(TextAlignment.CENTER);
            lblDesc.setMaxWidth(360);
            emptyState.getChildren().add(lblDesc);
        }

        // Botón de acción
        if (textoAccion != null && accion != null) {
            Button btnAccion = new Button(textoAccion);
            btnAccion.getStyleClass().add("button-primary");
            btnAccion.setOnAction(e -> accion.run());
            HBox actionBox = new HBox(btnAccion);
            actionBox.setAlignment(Pos.CENTER);
            actionBox.getStyleClass().add("empty-state-action");
            emptyState.getChildren().add(actionBox);
        }

        // Animación de entrada (fade + slide up sutil)
        animarEntrada(emptyState, 300, 20);

        return emptyState;
    }

    /**
     * Empty state simplificado (solo ícono + título + descripción).
     */
    public static VBox crearEmptyState(Node icono, String titulo, String descripcion) {
        return crearEmptyState(icono, titulo, descripcion, null, null);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NOTIFICATION BADGE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un badge de notificación con número.
     * @param count Número a mostrar (si > 99 muestra "99+")
     * @return Label estilizado como badge circular
     */
    public static Label crearNotificationBadge(int count) {
        String texto = count > 99 ? "99+" : String.valueOf(count);
        Label badge = new Label(texto);
        badge.getStyleClass().add("notification-badge");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badge.setTextFill(Color.WHITE);
        badge.setAlignment(Pos.CENTER);
        if (count > 99) {
            badge.setMinWidth(26);
        }
        return badge;
    }

    /**
     * Crea un punto de notificación (sin número).
     * @return Region estilizado como dot
     */
    public static Region crearNotificationDot() {
        Region dot = new Region();
        dot.getStyleClass().add("notification-dot");
        return dot;
    }

    /**
     * Envuelve un nodo con un badge de notificación superpuesto.
     * @param contenido Nodo base (ej: botón, ícono)
     * @param count Número del badge (0 para solo dot)
     * @return StackPane con el contenido y el badge posicionado
     */
    public static StackPane conBadge(Node contenido, int count) {
        StackPane wrapper = new StackPane();
        wrapper.getChildren().add(contenido);

        if (count > 0) {
            Label badge = crearNotificationBadge(count);
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            StackPane.setMargin(badge, new Insets(-6, -6, 0, 0));
            wrapper.getChildren().add(badge);
        } else {
            Region dot = crearNotificationDot();
            StackPane.setAlignment(dot, Pos.TOP_RIGHT);
            StackPane.setMargin(dot, new Insets(-2, -2, 0, 0));
            wrapper.getChildren().add(dot);
        }

        return wrapper;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STATUS BAR
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea una barra de estado inferior estilizada.
     * @param items Pares de Label que representan cada sección de la barra
     * @return HBox estilizado como status bar
     */
    public static HBox crearStatusBar(Node... items) {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("status-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getChildren().addAll(items);
        return bar;
    }

    /**
     * Crea un item de status bar con dot de estado + texto.
     * @param texto Texto del item
     * @param estado "success", "warning", o "error"
     * @return HBox con dot + label
     */
    public static HBox crearStatusItem(String texto, String estado) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);

        Region dot = new Region();
        dot.getStyleClass().addAll("status-dot", "status-dot-" + estado);

        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Segoe UI", 11));
        lbl.setTextFill(Color.web(TemaManager.getTextMuted()));

        item.getChildren().addAll(dot, lbl);
        return item;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BADGES / CHIPS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un badge/etiqueta estilizada.
     * @param texto Texto del badge
     * @param tipo "primary", "success", "warning", "error", o null para default
     * @return Label con clases CSS de badge
     */
    public static Label crearBadge(String texto, String tipo) {
        Label badge = new Label(texto);
        badge.getStyleClass().add("badge");
        if (tipo != null && !tipo.isEmpty()) {
            badge.getStyleClass().add("badge-" + tipo);
        }
        badge.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        return badge;
    }

    /**
     * Crea un chip/tag interactivo.
     * @param texto Texto del chip
     * @param onClick Acción al hacer click
     * @return Label con clase CSS .chip
     */
    public static Label crearChip(String texto, Runnable onClick) {
        Label chip = new Label(texto);
        chip.getStyleClass().add("chip");
        chip.setFont(Font.font("Segoe UI", 12));
        if (onClick != null) {
            chip.setOnMouseClicked(e -> {
                if (chip.getStyleClass().contains("selected")) {
                    chip.getStyleClass().remove("selected");
                } else {
                    chip.getStyleClass().add("selected");
                }
                onClick.run();
            });
        }
        return chip;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // AVATAR
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un avatar con iniciales del nombre.
     * @param nombre Nombre completo (toma iniciales)
     * @param color Color de fondo (hex)
     * @param tamaño "sm", "md" (default), o "lg"
     * @return StackPane con círculo + iniciales
     */
    public static StackPane crearAvatar(String nombre, String color, String tamaño) {
        String iniciales = obtenerIniciales(nombre);

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("avatar");
        if ("sm".equals(tamaño)) avatar.getStyleClass().add("avatar-sm");
        else if ("lg".equals(tamaño)) avatar.getStyleClass().add("avatar-lg");

        avatar.setStyle("-fx-background-color: " + color + ";");

        Label lbl = new Label(iniciales);
        lbl.setTextFill(Color.WHITE);
        int fontSize = "sm".equals(tamaño) ? 11 : "lg".equals(tamaño) ? 18 : 14;
        lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, fontSize));

        avatar.getChildren().add(lbl);
        return avatar;
    }

    private static String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "?";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length >= 2) {
            return ("" + partes[0].charAt(0) + partes[partes.length - 1].charAt(0)).toUpperCase();
        }
        return nombre.substring(0, Math.min(2, nombre.length())).toUpperCase();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TOAST / NOTIFICACIONES FLOTANTES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un toast/notificación flotante que se auto-cierra.
     * Se agrega al StackPane raíz y se anima con slide-in desde arriba.
     *
     * @param rootStack StackPane raíz de la ventana
     * @param mensaje Texto del mensaje
     * @param tipo "success", "warning", "error", "info"
     * @param duracionMs Duración en ms antes de auto-cerrar (ej: 3000)
     */
    public static void mostrarToast(StackPane rootStack, String mensaje, String tipo, int duracionMs) {
        HBox toast = new HBox(12);
        toast.getStyleClass().addAll("toast", "toast-" + tipo);
        toast.setMaxWidth(450);
        toast.setMaxHeight(56);

        // Ícono según tipo
        String iconColor;
        switch (tipo) {
            case "success": iconColor = "#00C853"; break;
            case "warning": iconColor = "#FFB300"; break;
            case "error": iconColor = "#FF5252"; break;
            default: iconColor = "#5B9DF9"; break;
        }

        Circle iconDot = new Circle(5, Color.web(iconColor));
        StackPane iconWrap = new StackPane(iconDot);
        iconWrap.setMinWidth(20);

        Label lblMensaje = new Label(mensaje);
        lblMensaje.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        lblMensaje.setTextFill(Color.web(TemaManager.getText()));
        lblMensaje.setWrapText(true);

        // Botón cerrar
        Label cerrar = new Label("✕");
        cerrar.setFont(Font.font("Segoe UI", 14));
        cerrar.setTextFill(Color.web(TemaManager.getTextMuted()));
        cerrar.setCursor(javafx.scene.Cursor.HAND);
        cerrar.setOnMouseClicked(e -> cerrarToast(rootStack, toast));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toast.getChildren().addAll(iconWrap, lblMensaje, spacer, cerrar);

        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        StackPane.setMargin(toast, new Insets(20, 0, 0, 0));

        // Estado inicial (fuera de vista)
        toast.setTranslateY(-60);
        toast.setOpacity(0);
        rootStack.getChildren().add(toast);

        // Animación de entrada
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toast);
        slideIn.setToY(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setToValue(1);
        ParallelTransition entrada = new ParallelTransition(slideIn, fadeIn);
        entrada.play();

        // Auto-cerrar
        javafx.animation.PauseTransition pausa = new javafx.animation.PauseTransition(Duration.millis(duracionMs));
        pausa.setOnFinished(e -> cerrarToast(rootStack, toast));
        pausa.play();
    }

    private static void cerrarToast(StackPane rootStack, HBox toast) {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), toast);
        slideOut.setToY(-60);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), toast);
        fadeOut.setToValue(0);
        ParallelTransition salida = new ParallelTransition(slideOut, fadeOut);
        salida.setOnFinished(e -> rootStack.getChildren().remove(toast));
        salida.play();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TOOLBAR / HEADER MEJORADO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un header/toolbar con título, subtítulo y acciones.
     *
     * @param titulo Título principal
     * @param subtitulo Subtítulo (null para omitir)
     * @param acciones Nodos de acción a la derecha (botones)
     * @return HBox estilizado como toolbar
     */
    public static HBox crearToolbarHeader(String titulo, String subtitulo, Node... acciones) {
        HBox header = new HBox(16);
        header.getStyleClass().add("toolbar-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textos = new VBox(2);
        textos.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("toolbar-title");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblTitulo.setTextFill(Color.web(TemaManager.getText()));
        textos.getChildren().add(lblTitulo);

        if (subtitulo != null && !subtitulo.isEmpty()) {
            Label lblSub = new Label(subtitulo);
            lblSub.getStyleClass().add("toolbar-subtitle");
            lblSub.setFont(Font.font("Segoe UI", 13));
            lblSub.setTextFill(Color.web(TemaManager.getTextMuted()));
            textos.getChildren().add(lblSub);
        }

        header.getChildren().add(textos);

        // Spacer entre título y acciones
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().add(spacer);

        // Acciones
        if (acciones != null && acciones.length > 0) {
            HBox accionesBox = new HBox(8);
            accionesBox.setAlignment(Pos.CENTER_RIGHT);
            accionesBox.getChildren().addAll(acciones);
            header.getChildren().add(accionesBox);
        }

        return header;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ACCENT CARD / TARJETA CON ACENTO DE COLOR
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea una tarjeta con barra de color superior (accent bar).
     *
     * @param titulo Título de la tarjeta
     * @param descripcion Descripción
     * @param color Color del acento
     * @param icono Nodo ícono (puede ser null)
     * @return VBox con la tarjeta
     */
    public static VBox crearAccentCard(String titulo, String descripcion, String color, Node icono) {
        VBox card = new VBox(0);
        card.getStyleClass().add("accent-card");

        // Barra de color superior
        Region accentBar = new Region();
        accentBar.setMinHeight(4);
        accentBar.setMaxHeight(4);
        accentBar.setStyle("-fx-background-color: " + color + ";" +
                          "-fx-background-radius: 16 16 0 0;");

        // Header
        HBox headerBox = new HBox(10);
        headerBox.getStyleClass().add("accent-card-header");
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(16, 16, 0, 16));

        if (icono != null) {
            StackPane iconWrap = new StackPane(icono);
            iconWrap.setPadding(new Insets(8));
            iconWrap.getStyleClass().add("icon-wrapper");
            iconWrap.setStyle("-fx-background-color: " + color + "15;");
            headerBox.getChildren().add(iconWrap);
        }

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
        lblTitulo.setTextFill(Color.web(TemaManager.getText()));
        headerBox.getChildren().add(lblTitulo);

        // Cuerpo
        VBox body = new VBox(8);
        body.getStyleClass().add("accent-card-body");
        body.setPadding(new Insets(8, 16, 16, 16));

        if (descripcion != null && !descripcion.isEmpty()) {
            Label lblDesc = new Label(descripcion);
            lblDesc.setFont(Font.font("Segoe UI", 13));
            lblDesc.setTextFill(Color.web(TemaManager.getTextSecondary()));
            lblDesc.setWrapText(true);
            body.getChildren().add(lblDesc);
        }

        card.getChildren().addAll(accentBar, headerBox, body);

        // Hover con sombra de color
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web(color, 0.12));
        sombra.setRadius(10);
        sombra.setOffsetY(4);
        card.setEffect(sombra);

        card.setOnMouseEntered(e -> {
            sombra.setColor(Color.web(color, 0.25));
            sombra.setRadius(16);
            sombra.setOffsetY(6);
        });
        card.setOnMouseExited(e -> {
            sombra.setColor(Color.web(color, 0.12));
            sombra.setRadius(10);
            sombra.setOffsetY(4);
        });

        return card;
    }

    /**
     * Obtiene el body (VBox) de un AccentCard para agregar contenido dinámico.
     */
    public static VBox getAccentCardBody(VBox accentCard) {
        // La estructura es: accentBar(0), headerBox(1), body(2)
        if (accentCard.getChildren().size() > 2 && 
            accentCard.getChildren().get(2) instanceof VBox) {
            return (VBox) accentCard.getChildren().get(2);
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SKELETON LOADING
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un esqueleto de carga (placeholder visual mientras se cargan datos).
     *
     * @param width Ancho del esqueleto
     * @param height Alto del esqueleto
     * @param radius Radio de borde (6 para texto, 50 para círculo)
     * @return Region con la clase skeleton
     */
    public static Region crearSkeleton(double width, double height, double radius) {
        Region skeleton = new Region();
        skeleton.getStyleClass().add("skeleton");
        skeleton.setPrefSize(width, height);
        skeleton.setMinSize(width, height);
        skeleton.setMaxSize(width, height);
        skeleton.setStyle("-fx-background-radius: " + radius + ";");

        // Animación de parpadeo suave
        FadeTransition pulse = new FadeTransition(Duration.millis(1200), skeleton);
        pulse.setFromValue(0.4);
        pulse.setToValue(0.8);
        pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        // Guardar ref para parar al quitar del scene
        skeleton.getProperties().put("_skeletonAnim", pulse);

        return skeleton;
    }

    /**
     * Crea un grupo de esqueletos simulando una tarjeta de estadísticas.
     */
    public static VBox crearSkeletonStatCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("skeleton-card");
        card.setPadding(new Insets(16));
        card.setMinWidth(180);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(
            crearSkeleton(32, 32, 8),
            crearSkeleton(100, 14, 4)
        );

        Region valorSkeleton = crearSkeleton(80, 28, 4);

        card.getChildren().addAll(header, valorSkeleton);
        return card;
    }

    /**
     * Crea un grupo de esqueletos simulando filas de tabla.
     * @param filas Número de filas a simular
     */
    public static VBox crearSkeletonTable(int filas) {
        VBox table = new VBox(8);
        table.setPadding(new Insets(16));

        // Header
        HBox headerRow = new HBox(16);
        headerRow.getChildren().addAll(
            crearSkeleton(120, 12, 4),
            crearSkeleton(150, 12, 4),
            crearSkeleton(100, 12, 4),
            crearSkeleton(80, 12, 4)
        );
        table.getChildren().add(headerRow);

        // Filas
        for (int i = 0; i < filas; i++) {
            HBox row = new HBox(16);
            row.getChildren().addAll(
                crearSkeleton(120, 14, 4),
                crearSkeleton(150, 14, 4),
                crearSkeleton(100, 14, 4),
                crearSkeleton(80, 14, 4)
            );
            table.getChildren().add(row);
        }

        return table;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SECCIÓN CON ENCABEZADO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea una sección con título, descripción opcional y contenido.
     * Útil para organizar áreas del dashboard en bloques con título claro.
     *
     * @param titulo Título de la sección
     * @param descripcion Descripción (null para omitir)
     * @param contenido Contenido de la sección
     * @return VBox con la sección completa
     */
    public static VBox crearSeccionConHeader(String titulo, String descripcion, Node contenido) {
        VBox seccion = new VBox(12);
        seccion.getStyleClass().add("section");

        // Header de sección
        VBox headerSeccion = new VBox(4);
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("label-subtitulo");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        lblTitulo.setTextFill(Color.web(TemaManager.getText()));
        headerSeccion.getChildren().add(lblTitulo);

        if (descripcion != null && !descripcion.isEmpty()) {
            Label lblDesc = new Label(descripcion);
            lblDesc.getStyleClass().add("label-info");
            lblDesc.setFont(Font.font("Segoe UI", 12));
            lblDesc.setTextFill(Color.web(TemaManager.getTextMuted()));
            lblDesc.setWrapText(true);
            headerSeccion.getChildren().add(lblDesc);
        }

        // Línea divisoria sutil
        Region divider = new Region();
        divider.getStyleClass().add("divider");
        divider.setPrefHeight(1);
        divider.setMaxHeight(1);

        seccion.getChildren().addAll(headerSeccion, divider, contenido);
        return seccion;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMACIONES REUTILIZABLES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Aplica animación de entrada (fade-in + slide-up) a un nodo.
     *
     * @param nodo Nodo a animar
     * @param duracionMs Duración en ms
     * @param desplazamiento Píxeles de desplazamiento vertical (ej: 20)
     */
    public static void animarEntrada(Node nodo, int duracionMs, double desplazamiento) {
        nodo.setOpacity(0);
        nodo.setTranslateY(desplazamiento);

        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
        fade.setToValue(1);
        fade.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);

        TranslateTransition slide = new TranslateTransition(Duration.millis(duracionMs), nodo);
        slide.setToY(0);
        slide.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);

        ParallelTransition animacion = new ParallelTransition(fade, slide);
        animacion.setDelay(Duration.millis(50));
        animacion.play();
    }

    /**
     * Aplica animación de entrada escalonada a una lista de nodos.
     * Cada nodo entra con un delay incrementado, dando efecto cascada.
     *
     * @param nodos Lista de nodos a animar
     * @param duracionMs Duración de cada animación
     * @param delayEntreMs Delay entre cada nodo (ej: 60ms)
     */
    public static void animarEntradaEscalonada(java.util.List<Node> nodos, int duracionMs, int delayEntreMs) {
        for (int i = 0; i < nodos.size(); i++) {
            Node nodo = nodos.get(i);
            nodo.setOpacity(0);
            nodo.setTranslateY(15);

            FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
            fade.setToValue(1);
            fade.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);

            TranslateTransition slide = new TranslateTransition(Duration.millis(duracionMs), nodo);
            slide.setToY(0);
            slide.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);

            ParallelTransition animacion = new ParallelTransition(fade, slide);
            animacion.setDelay(Duration.millis(i * delayEntreMs));
            animacion.play();
        }
    }

    /**
     * Aplica animación de pulso a un nodo (scale up/down suave).
     * Útil para atraer atención a un elemento.
     */
    public static void animarPulso(Node nodo) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(600), nodo);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.setInterpolator(AnimacionesFX.EASE_IN_OUT);
        pulse.play();
    }

    /**
     * Aplica animación de "shake" a un nodo para indicar error.
     */
    public static void animarShake(Node nodo) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(80), nodo);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> nodo.setTranslateX(0));
        shake.play();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PROGRESS / INDICADOR DE PROGRESO MEJORADO  
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea una barra de progreso con label de porcentaje.
     *
     * @param progreso Valor de 0.0 a 1.0
     * @param color Color de la barra (hex)
     * @param ancho Ancho total de la barra
     * @return VBox con barra + label de porcentaje
     */
    public static VBox crearProgressBar(double progreso, String color, double ancho) {
        VBox container = new VBox(4);
        container.setAlignment(Pos.CENTER_LEFT);

        // Barra
        StackPane barContainer = new StackPane();
        barContainer.setPrefWidth(ancho);
        barContainer.setMaxHeight(8);
        barContainer.setMinHeight(8);

        Region track = new Region();
        track.setStyle("-fx-background-color: " + TemaManager.getBorder() + ";" +
                      "-fx-background-radius: 4;");
        track.setPrefWidth(ancho);

        Region fill = new Region();
        fill.setStyle("-fx-background-color: " + color + ";" +
                     "-fx-background-radius: 4;");
        fill.setPrefWidth(ancho * Math.max(0, Math.min(1, progreso)));
        fill.setMaxWidth(ancho * Math.max(0, Math.min(1, progreso)));

        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        barContainer.getChildren().addAll(track, fill);

        // Label
        int porcentaje = (int)(progreso * 100);
        Label lblPorcentaje = new Label(porcentaje + "%");
        lblPorcentaje.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        lblPorcentaje.setTextFill(Color.web(TemaManager.getTextMuted()));

        container.getChildren().addAll(barContainer, lblPorcentaje);
        return container;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // BOTÓN CERRAR (X) UNIFICADO — Estilo moderno con icono SVG
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Crea un botón de cerrar (X) con estilo moderno unificado.
     * Icono SVG rojo, fondo translúcido, hover rojo intenso con X blanca y sombra.
     * Incluye animaciones de escala al hover.
     * 
     * @param onClose acción a ejecutar al hacer clic (ej: stage::close)
     * @return botón configurado con todas las animaciones
     */
    public static Button crearBotonCerrar(Runnable onClose) {
        return crearBotonCerrar(onClose, 40);
    }

    /**
     * Crea un botón de cerrar (X) con tamaño personalizado.
     * 
     * @param onClose acción a ejecutar al hacer clic
     * @param size diámetro del botón (ej: 36, 40, 44)
     * @return botón configurado
     */
    public static Button crearBotonCerrar(Runnable onClose, int size) {
        Button btn = new Button();
        int iconSize = Math.max(14, size / 2 - 4);
        btn.setGraphic(new StackPane(IconosSVG.cerrar("#EF4444", iconSize)));
        btn.setPrefSize(size, size);
        btn.setMinSize(size, size);
        btn.setMaxSize(size, size);
        btn.setFocusTraversable(false);

        double radius = size / 2.0;
        String baseStyle = "-fx-background-color: #EF444415; -fx-background-radius: " + radius + "; " +
            "-fx-border-color: transparent; -fx-border-width: 0; -fx-cursor: hand; " +
            "-fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-insets: 0;";
        String hoverStyle = "-fx-background-color: #EF4444; -fx-background-radius: " + radius + "; " +
            "-fx-border-color: transparent; -fx-border-width: 0; -fx-cursor: hand; " +
            "-fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-insets: 0; " +
            "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.45), 10, 0, 0, 2);";

        btn.setStyle(baseStyle);

        btn.setOnMouseEntered(e -> {
            btn.setGraphic(new StackPane(IconosSVG.cerrar("#FFFFFF", iconSize)));
            btn.setStyle(hoverStyle);
            AnimacionesFX.hoverIn(btn, 1.12, 120);
        });
        btn.setOnMouseExited(e -> {
            btn.setGraphic(new StackPane(IconosSVG.cerrar("#EF4444", iconSize)));
            btn.setStyle(baseStyle);
            AnimacionesFX.hoverOut(btn, 120);
        });
        btn.setOnMousePressed(e -> AnimacionesFX.press(btn));
        btn.setOnMouseReleased(e -> AnimacionesFX.release(btn));
        btn.setOnAction(e -> {
            if (onClose != null) onClose.run();
        });

        return btn;
    }
}
