package inventario.fx.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Gestor centralizado de temas para la aplicación SELCOMP.
 * Maneja el cambio entre modo oscuro y claro, y proporciona
 * todos los colores de la paleta de forma consistente.
 * 
 * @author SELCOMP
 * @version 1.0
 */
public class TemaManager {

    // ═══════════════════════════════════════════════════════════════════════════
    // SINGLETON Y ESTADO (Lazy initialization)
    // ═══════════════════════════════════════════════════════════════════════════
    
    private static TemaManager INSTANCE;
    private static final String PREF_KEY_DARK_MODE = "darkMode";
    private static Preferences prefs;
    
    // Estado del tema (sin usar JavaFX Property para inicialización temprana)
    private static boolean darkMode = true;
    private static boolean initialized = false;
    
    // Propiedad observable para detectar cambios de tema (se inicializa tarde)
    private BooleanProperty darkModeProperty;
    
    // Lista de escenas registradas para actualizar automáticamente
    private final List<Scene> registeredScenes = new ArrayList<>();
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PALETA DE COLORES - TEMA OSCURO (DEFAULT)
    // ═══════════════════════════════════════════════════════════════════════════
    
    // Fondos oscuros
    private static final String DARK_BG_DARKER = "#0A0A0A";
    private static final String DARK_BG_DARK = "#0D0D0D";
    private static final String DARK_BG = "#141414";
    private static final String DARK_BG_LIGHT = "#1A1A1A";
    private static final String DARK_SURFACE = "#1E1E1E";
    private static final String DARK_SURFACE_HOVER = "#252525";
    private static final String DARK_BORDER = "#2A2A2A";
    private static final String DARK_BORDER_LIGHT = "#3A3A3A";
    
    // Textos oscuros
    private static final String DARK_TEXT = "#F5F5F5";
    private static final String DARK_TEXT_SECONDARY = "#9E9E9E";
    private static final String DARK_TEXT_MUTED = "#8B8B8B";
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PALETA DE COLORES - TEMA CLARO (Mejorado con mejor contraste)
    // ═══════════════════════════════════════════════════════════════════════════
    
    // Fondos claros (paleta profesional con tono frío-neutro)
    private static final String LIGHT_BG_DARKER = "#DFE1E6";   // Fondo más oscuro para contraste
    private static final String LIGHT_BG_DARK = "#EBECF0";     // Fondo oscuro secundario
    private static final String LIGHT_BG = "#F4F5F7";          // Fondo principal (base)
    private static final String LIGHT_BG_LIGHT = "#FAFBFC";    // Fondo suave
    private static final String LIGHT_SURFACE = "#FFFFFF";     // Superficie blanca (cards)
    private static final String LIGHT_SURFACE_HOVER = "#F7F8FA"; // Hover sutil
    private static final String LIGHT_BORDER = "#DFE1E6";      // Borde principal (suave)
    private static final String LIGHT_BORDER_LIGHT = "#EBECF0"; // Borde suave
    
    // Textos claros (mejor contraste y legibilidad)
    private static final String LIGHT_TEXT = "#172B4D";        // Texto principal (azul-negro profundo)
    private static final String LIGHT_TEXT_SECONDARY = "#5E6C84"; // Texto secundario (gris azulado)
    private static final String LIGHT_TEXT_MUTED = "#7A869A";  // Texto atenuado (gris frío, WCAG AA 4.5:1)
    
    // ═══════════════════════════════════════════════════════════════════════════
    // COLORES COMPARTIDOS (Ambos temas)
    // ═══════════════════════════════════════════════════════════════════════════
    
    // Colores de acento
    public static final String COLOR_PRIMARY = "#E63946";
    public static final String COLOR_PRIMARY_HOVER = "#FF4D5A";
    public static final String COLOR_PRIMARY_DARK = "#C62828";
    public static final String COLOR_SUCCESS = "#00C853";
    public static final String COLOR_SUCCESS_HOVER = "#00E676";
    public static final String COLOR_WARNING = "#FFB300";
    public static final String COLOR_WARNING_HOVER = "#FFC107";
    public static final String COLOR_DANGER = "#FF5252";
    public static final String COLOR_DANGER_HOVER = "#FF6B6B";
    public static final String COLOR_INFO = "#2196F3";
    public static final String COLOR_INFO_HOVER = "#42A5F5";
    
    // Colores para gráficos (paleta moderna)
    public static final String[] CHART_COLORS = {
        "#3B82F6", // Azul
        "#10B981", // Verde
        "#F59E0B", // Amarillo
        "#EF4444", // Rojo
        "#8B5CF6", // Púrpura
        "#EC4899", // Rosa
        "#06B6D4", // Cyan
        "#F97316"  // Naranja
    };
    
    // Colores para gráfico de línea
    public static final String LINE_COLOR = "#00D4FF";
    public static final String LINE_GLOW = "#00D4FF40";
    
    // ═══════════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR Y SINGLETON
    // ═══════════════════════════════════════════════════════════════════════════
    
    private TemaManager() {
        // Inicialización vacía - la carga real se hace en ensureInitialized()
    }

    // ─── Rutas al archivo portable de preferencias ─────────────────────────────
    private static java.nio.file.Path getConfigFile() {
        return inventario.fx.config.PortablePaths.getConfigProperties();
    }

    private static java.util.Properties loadProps() {
        java.util.Properties p = new java.util.Properties();
        java.nio.file.Path f = getConfigFile();
        if (java.nio.file.Files.exists(f)) {
            try (java.io.InputStream in = java.nio.file.Files.newInputStream(f)) {
                p.load(in);
            } catch (Exception e) {
                System.err.println("⚠ TemaManager: error leyendo config.properties: " + e.getMessage());
            }
        }
        return p;
    }

    private static void saveProps(java.util.Properties p) {
        java.nio.file.Path f = getConfigFile();
        try {
            java.nio.file.Files.createDirectories(f.getParent());
            try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(f)) {
                p.store(out, "SELCOMP — Preferencias portables");
            }
        } catch (Exception e) {
            System.err.println("⚠ TemaManager: error guardando config.properties: " + e.getMessage());
        }
    }

    private static void ensureInitialized() {
        if (!initialized) {
            initialized = true;
            try {
                java.util.Properties p = loadProps();
                // Migración: si hay valor en Preferences (Registro), usarlo y migrar
                if (!p.containsKey(PREF_KEY_DARK_MODE)) {
                    try {
                        prefs = Preferences.userNodeForPackage(TemaManager.class);
                        darkMode = prefs.getBoolean(PREF_KEY_DARK_MODE, true);
                        prefs.remove(PREF_KEY_DARK_MODE);
                    } catch (Exception ex) {
                        darkMode = true;
                    }
                } else {
                    darkMode = Boolean.parseBoolean(p.getProperty(PREF_KEY_DARK_MODE, "true"));
                }
            } catch (Exception e) {
                System.err.println("⚠ No se pudieron cargar preferencias de tema: " + e.getMessage());
                darkMode = true;
            }
        }
    }
    
    public static TemaManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TemaManager();
        }
        return INSTANCE;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // API PÚBLICA - CONTROL DE TEMA
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Verifica si el modo oscuro está activo.
     */
    public static boolean isDarkMode() {
        ensureInitialized();
        return darkMode;
    }
    
    /**
     * Activa el modo oscuro.
     */
    public static void setDarkMode() {
        ensureInitialized();
        darkMode = true;
        guardarPreferencia();
        getInstance().actualizarEscenasRegistradas();
    }
    
    /**
     * Activa el modo claro.
     */
    public static void setLightMode() {
        ensureInitialized();
        darkMode = false;
        guardarPreferencia();
        getInstance().actualizarEscenasRegistradas();
    }
    
    /**
     * Alterna entre modo oscuro y claro.
     */
    public static void toggleTheme() {
        ensureInitialized();
        darkMode = !darkMode;
        guardarPreferencia();
        getInstance().actualizarEscenasRegistradas();
    }
    
    private static void guardarPreferencia() {
        try {
            java.util.Properties p = loadProps();
            p.setProperty(PREF_KEY_DARK_MODE, String.valueOf(darkMode));
            saveProps(p);
        } catch (Exception e) {
            System.err.println("⚠ No se pudo guardar preferencia de tema: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene la propiedad observable del modo oscuro.
     * (Se inicializa bajo demanda cuando JavaFX está disponible)
     */
    public static BooleanProperty darkModePropertyStatic() {
        TemaManager tm = getInstance();
        if (tm.darkModeProperty == null) {
            tm.darkModeProperty = new SimpleBooleanProperty(darkMode);
        }
        return tm.darkModeProperty;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // API PÚBLICA - COLORES DE FONDO
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Fondo más oscuro/claro (para contenedores principales) */
    public static String getBgDarker() {
        return isDarkMode() ? DARK_BG_DARKER : LIGHT_BG_DARKER;
    }
    
    /** Fondo oscuro (principal) */
    public static String getBgDark() {
        return isDarkMode() ? DARK_BG_DARK : LIGHT_BG_DARK;
    }
    
    /** Fondo base */
    public static String getBg() {
        return isDarkMode() ? DARK_BG : LIGHT_BG;
    }
    
    /** Fondo ligeramente más claro */
    public static String getBgLight() {
        return isDarkMode() ? DARK_BG_LIGHT : LIGHT_BG_LIGHT;
    }
    
    /** Superficie (cards, paneles) */
    public static String getSurface() {
        return isDarkMode() ? DARK_SURFACE : LIGHT_SURFACE;
    }
    
    /** Superficie hover */
    public static String getSurfaceHover() {
        return isDarkMode() ? DARK_SURFACE_HOVER : LIGHT_SURFACE_HOVER;
    }
    
    /** Color de borde */
    public static String getBorder() {
        return isDarkMode() ? DARK_BORDER : LIGHT_BORDER;
    }
    
    /** Color de borde claro */
    public static String getBorderLight() {
        return isDarkMode() ? DARK_BORDER_LIGHT : LIGHT_BORDER_LIGHT;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // API PÚBLICA - COLORES DE TEXTO
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Texto principal */
    public static String getText() {
        return isDarkMode() ? DARK_TEXT : LIGHT_TEXT;
    }
    
    /** Texto secundario */
    public static String getTextSecondary() {
        return isDarkMode() ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY;
    }
    
    /** Texto atenuado */
    public static String getTextMuted() {
        return isDarkMode() ? DARK_TEXT_MUTED : LIGHT_TEXT_MUTED;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // API PÚBLICA - COLORES COMO Color (JavaFX)
    // ═══════════════════════════════════════════════════════════════════════════
    
    public static Color getBgDarkerColor() {
        return Color.web(getBgDarker());
    }
    
    public static Color getBgDarkColor() {
        return Color.web(getBgDark());
    }
    
    public static Color getBgColor() {
        return Color.web(getBg());
    }
    
    public static Color getBgLightColor() {
        return Color.web(getBgLight());
    }
    
    public static Color getSurfaceColor() {
        return Color.web(getSurface());
    }
    
    public static Color getSurfaceHoverColor() {
        return Color.web(getSurfaceHover());
    }
    
    public static Color getBorderColor() {
        return Color.web(getBorder());
    }
    
    public static Color getTextColor() {
        return Color.web(getText());
    }
    
    public static Color getTextSecondaryColor() {
        return Color.web(getTextSecondary());
    }
    
    public static Color getTextMutedColor() {
        return Color.web(getTextMuted());
    }
    
    public static Color getPrimaryColor() {
        return Color.web(COLOR_PRIMARY);
    }
    
    public static Color getSuccessColor() {
        return Color.web(COLOR_SUCCESS);
    }
    
    public static Color getWarningColor() {
        return Color.web(COLOR_WARNING);
    }
    
    public static Color getDangerColor() {
        return Color.web(COLOR_DANGER);
    }
    
    public static Color getInfoColor() {
        return Color.web(COLOR_INFO);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // API PÚBLICA - GESTIÓN DE CSS
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene la ruta del archivo CSS según el tema actual.
     */
    public static String getCssPath() {
        return isDarkMode() ? "/styles/inventario-dark.css" : "/styles/inventario-light.css";
    }
    
    /**
     * Aplica el tema actual a una escena.
     */
    public static void aplicarTema(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String cssPath = getCssPath();
        try {
            String css = TemaManager.class.getResource(cssPath).toExternalForm();
            scene.getStylesheets().add(css);

            // Si estamos en modo claro, cargar además los fixes específicos
            if (!isDarkMode()) {
                try {
                    String fixPath = "/styles/inventario-light-fixes.css";
                    String fix = TemaManager.class.getResource(fixPath).toExternalForm();
                    if (!scene.getStylesheets().contains(fix)) {
                        scene.getStylesheets().add(fix);
                    }
                } catch (Exception ex) {
                    System.err.println("⚠ No se pudo cargar el CSS de correcciones para modo claro: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ No se pudo cargar el CSS: " + cssPath);
        }
    }
    
    /**
     * Registra una escena para actualización automática de tema.
     */
    public static void registrarEscena(Scene scene) {
        TemaManager manager = getInstance();
        if (scene != null && !manager.registeredScenes.contains(scene)) {
            manager.registeredScenes.add(scene);
            aplicarTema(scene);
        }
    }
    
    /**
     * Desregistra una escena.
     */
    public static void desregistrarEscena(Scene scene) {
        getInstance().registeredScenes.remove(scene);
    }
    
    /**
     * Actualiza todas las escenas registradas con el tema actual.
     */
    private void actualizarEscenasRegistradas() {
        // Limpiar escenas nulas
        registeredScenes.removeIf(s -> s == null || s.getWindow() == null);
        
        for (Scene scene : registeredScenes) {
            aplicarTema(scene);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // API PÚBLICA - ESTILOS INLINE COMUNES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Genera estilo para un contenedor principal.
     */
    public static String getMainContainerStyle() {
        return "-fx-background-color: " + getBgDark() + ";";
    }
    
    /**
     * Genera estilo para una card/panel.
     */
    public static String getCardStyle() {
        if (isDarkMode()) {
            return "-fx-background-color: " + getSurface() + ";" +
                   "-fx-background-radius: 12;" +
                   "-fx-border-color: " + getBorder() + ";" +
                   "-fx-border-radius: 12;" +
                   "-fx-border-width: 1;";
        } else {
            return "-fx-background-color: #FFFFFF;" +
                   "-fx-background-radius: 12;" +
                   "-fx-border-color: " + getBorder() + ";" +
                   "-fx-border-radius: 12;" +
                   "-fx-border-width: 1;" +
                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);";
        }
    }
    
    /**
     * Genera estilo para una card con efecto hover.
     */
    public static String getCardHoverStyle() {
        if (isDarkMode()) {
            return "-fx-background-color: " + getSurfaceHover() + ";" +
                   "-fx-background-radius: 12;" +
                   "-fx-border-color: " + getBorderLight() + ";" +
                   "-fx-border-radius: 12;" +
                   "-fx-border-width: 1;";
        } else {
            return "-fx-background-color: #FFFFFF;" +
                   "-fx-background-radius: 12;" +
                   "-fx-border-color: " + COLOR_PRIMARY + "40;" +
                   "-fx-border-radius: 12;" +
                   "-fx-border-width: 1;" +
                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 14, 0, 0, 4);";
        }
    }
    
    /**
     * Genera estilo para un campo de texto.
     */
    public static String getTextFieldStyle() {
        return "-fx-background-color: " + getBgLight() + ";" +
               "-fx-text-fill: " + getText() + ";" +
               "-fx-prompt-text-fill: " + getTextMuted() + ";" +
               "-fx-background-radius: 8;" +
               "-fx-border-color: " + getBorder() + ";" +
               "-fx-border-radius: 8;" +
               "-fx-border-width: 1;" +
               "-fx-padding: 10 12;";
    }
    
    /**
     * Genera estilo para un botón primario.
     */
    public static String getPrimaryButtonStyle() {
        return "-fx-background-color: " + COLOR_PRIMARY + ";" +
               "-fx-text-fill: white;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 8;" +
               "-fx-cursor: hand;" +
               "-fx-padding: 12 24;";
    }
    
    /**
     * Genera estilo para un botón secundario.
     */
    public static String getSecondaryButtonStyle() {
        return "-fx-background-color: " + getSurface() + ";" +
               "-fx-text-fill: " + getText() + ";" +
               "-fx-background-radius: 8;" +
               "-fx-border-color: " + getBorder() + ";" +
               "-fx-border-radius: 8;" +
               "-fx-border-width: 1;" +
               "-fx-cursor: hand;" +
               "-fx-padding: 12 24;";
    }
    
    /**
     * Genera estilo para un botón fantasma (transparente).
     */
    public static String getGhostButtonStyle() {
        return "-fx-background-color: transparent;" +
               "-fx-text-fill: " + getText() + ";" +
               "-fx-background-radius: 8;" +
               "-fx-cursor: hand;" +
               "-fx-padding: 8 16;";
    }
    
    /**
     * Genera estilo para el header de una sección.
     */
    public static String getHeaderStyle() {
        return "-fx-background-color: " + getBg() + ";" +
               "-fx-border-color: " + getBorder() + ";" +
               "-fx-border-width: 0 0 1 0;";
    }
    
    /**
     * Genera estilo para ScrollPane.
     */
    public static String getScrollPaneStyle() {
        return "-fx-background: " + getBgDark() + ";" +
               "-fx-background-color: " + getBgDark() + ";";
    }
    
    /**
     * Genera estilo para un panel con gradiente y bordes.
     */
    public static String getPanelStyle() {
        if (isDarkMode()) {
            return "-fx-background-color: linear-gradient(to bottom, " + getBgLight() + ", " + getBg() + ");" +
                   "-fx-background-radius: 16;" +
                   "-fx-border-color: " + getBorder() + ";" +
                   "-fx-border-radius: 16;" +
                   "-fx-border-width: 1;";
        } else {
            return "-fx-background-color: " + getSurface() + ";" +
                   "-fx-background-radius: 16;" +
                   "-fx-border-color: " + getBorder() + ";" +
                   "-fx-border-radius: 16;" +
                   "-fx-border-width: 1;" +
                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);";
        }
    }
    
    /**
     * Genera estilo para un input/campo de búsqueda.
     */
    public static String getInputStyle() {
        if (isDarkMode()) {
            return "-fx-background-color: linear-gradient(to bottom, " + getSurface() + ", " + getBgLight() + ");" +
                   "-fx-background-radius: 12;" +
                   "-fx-border-color: " + getBorder() + ";" +
                   "-fx-border-radius: 12;" +
                   "-fx-border-width: 1;";
        } else {
            return "-fx-background-color: " + getSurface() + ";" +
                   "-fx-background-radius: 12;" +
                   "-fx-border-color: " + getBorder() + ";" +
                   "-fx-border-radius: 12;" +
                   "-fx-border-width: 1;";
        }
    }
    
    /**
     * Genera estilo para un input con focus.
     */
    public static String getInputFocusStyle() {
        if (isDarkMode()) {
            return "-fx-background-color: linear-gradient(to bottom, " + getSurfaceHover() + ", " + getSurface() + ");" +
                   "-fx-background-radius: 12;" +
                   "-fx-border-color: #505050;" +
                   "-fx-border-radius: 12;" +
                   "-fx-border-width: 1.5;";
        } else {
            return "-fx-background-color: " + getSurface() + ";" +
                   "-fx-background-radius: 12;" +
                   "-fx-border-color: " + COLOR_PRIMARY + ";" +
                   "-fx-border-radius: 12;" +
                   "-fx-border-width: 1.5;";
        }
    }
    
    /**
     * Genera estilo para una barra flotante.
     */
    public static String getFloatingBarStyle() {
        if (isDarkMode()) {
            return "-fx-background-color: linear-gradient(to right, " + getSurface() + ", " + getSurfaceHover() + ");" +
                   "-fx-background-radius: 30;" +
                   "-fx-border-color: " + getBorderLight() + ";" +
                   "-fx-border-radius: 30;" +
                   "-fx-border-width: 1;";
        } else {
            return "-fx-background-color: " + getSurface() + ";" +
                   "-fx-background-radius: 30;" +
                   "-fx-border-color: " + getBorder() + ";" +
                   "-fx-border-radius: 30;" +
                   "-fx-border-width: 1;" +
                   "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 15, 0, 0, 5);";
        }
    }
    
    /**
     * Genera estilo para tooltips.
     */
    public static String getTooltipStyle() {
        if (isDarkMode()) {
            return "-fx-background-color: #1A1A1A;" +
                   "-fx-text-fill: white;" +
                   "-fx-font-size: 12px;" +
                   "-fx-background-radius: 8;" +
                   "-fx-border-color: " + getBorder() + ";" +
                   "-fx-border-radius: 8;" +
                   "-fx-padding: 8 12;" +
                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 12, 0, 0, 4);";
        } else {
            return "-fx-background-color: #FFFFFF;" +
                   "-fx-text-fill: " + getText() + ";" +
                   "-fx-font-size: 12px;" +
                   "-fx-background-radius: 8;" +
                   "-fx-border-color: " + getBorder() + ";" +
                   "-fx-border-radius: 8;" +
                   "-fx-padding: 8 14;" +
                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 16, 0, 0, 6);";
        }
    }
    
    /**
     * Genera estilo para un botón cerrar pequeño.
     */
    public static String getCloseButtonStyle() {
        return "-fx-background-color: " + getBorder() + ";" +
               "-fx-background-radius: 50;" +
               "-fx-cursor: hand;";
    }
    
    /**
     * Genera estilo para un botón cerrar pequeño hover.
     */
    public static String getCloseButtonHoverStyle() {
        return "-fx-background-color: " + getBorderLight() + ";" +
               "-fx-background-radius: 50;" +
               "-fx-cursor: hand;";
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Convierte un color hex a formato rgba con opacidad.
     */
    public static String hexToRgba(String hex, double opacity) {
        Color color = Color.web(hex);
        return String.format("rgba(%d, %d, %d, %.2f)",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255),
            opacity);
    }
    
    /**
     * Obtiene un color con opacidad aplicada.
     */
    public static Color withOpacity(String hex, double opacity) {
        Color base = Color.web(hex);
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), opacity);
    }
    
    /**
     * Imprime información del tema actual en consola.
     */
    public static void printThemeInfo() {
        System.out.println("═".repeat(50));
        System.out.println("🎨 TEMA ACTUAL: " + (isDarkMode() ? "OSCURO" : "CLARO"));
        System.out.println("═".repeat(50));
        System.out.println("  Fondo principal: " + getBgDark());
        System.out.println("  Superficie: " + getSurface());
        System.out.println("  Texto: " + getText());
        System.out.println("  Borde: " + getBorder());
        System.out.println("  Primario: " + COLOR_PRIMARY);
        System.out.println("═".repeat(50));
    }
}
