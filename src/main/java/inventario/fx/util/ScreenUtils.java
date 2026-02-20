package inventario.fx.util;

import javafx.stage.Screen;

/**
 * Utilidades para adaptar dimensiones de ventanas al tamano de la pantalla.
 *
 * Estrategia: usar el tamano original/preferido de cada ventana, pero
 * reducirlo si la pantalla del dispositivo es mas pequena que ese tamano.
 * En pantallas grandes el tamano queda exactamente igual que before.
 *
 * <pre>
 * Ejemplo de uso:
 *   dashboardStage.setWidth (ScreenUtils.w(1600));
 *   dashboardStage.setHeight(ScreenUtils.h(900));
 * </pre>
 */
public final class ScreenUtils {

    /** Margen que se deja entre la ventana y el borde de la pantalla (pixeles). */
    private static final double MARGIN = 40;

    private ScreenUtils() {}

    // ─────────────────────────────────────────────────────────────────────────
    //  INFORMACION DE PANTALLA
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ancho util de la pantalla principal (excluye barra de tareas).
     */
    public static double screenWidth() {
        return Screen.getPrimary().getVisualBounds().getWidth();
    }

    /**
     * Alto util de la pantalla principal (excluye barra de tareas).
     */
    public static double screenHeight() {
        return Screen.getPrimary().getVisualBounds().getHeight();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CALCULOS: TAMANO PREFERIDO, LIMITADO A LA PANTALLA
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Devuelve el ancho preferido, reduciendolo solo si la pantalla es
     * mas pequena que ese valor (menos el margen de seguridad).
     *
     * @param preferred ancho deseado en pixeles (el tamano "normal")
     * @return preferred si la pantalla es suficientemente grande; si no,
     *         screenWidth - MARGIN
     */
    public static double w(double preferred) {
        return Math.min(preferred, screenWidth() - MARGIN);
    }

    /**
     * Devuelve el alto preferido, reduciendolo solo si la pantalla es
     * mas pequena que ese valor (menos el margen de seguridad).
     *
     * @param preferred alto deseado en pixeles (el tamano "normal")
     * @return preferred si la pantalla es suficientemente grande; si no,
     *         screenHeight - MARGIN
     */
    public static double h(double preferred) {
        return Math.min(preferred, screenHeight() - MARGIN);
    }

    /**
     * Igual que {@link #w(double)} pero con un minimo garantizado.
     */
    public static double w(double preferred, double minVal) {
        return Math.max(minVal, w(preferred));
    }

    /**
     * Igual que {@link #h(double)} pero con un minimo garantizado.
     */
    public static double h(double preferred, double minVal) {
        return Math.max(minVal, h(preferred));
    }
}
