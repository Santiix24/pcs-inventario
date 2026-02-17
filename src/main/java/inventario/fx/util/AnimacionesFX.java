package inventario.fx.util;

import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;

/**
 * Biblioteca completa de animaciones y transiciones premium para JavaFX.
 * Proporciona interpoladores personalizados, efectos hover, transiciones
 * de entrada/salida, y animaciones reutilizables para toda la aplicación.
 *
 * @author SELCOMP
 * @version 2.0
 */
public class AnimacionesFX {

    // ═══════════════════════════════════════════════════════════════════════════
    // INTERPOLADORES PERSONALIZADOS (Curvas de animación premium)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Ease-out cúbico: desaceleración suave y natural */
    public static final Interpolator EASE_OUT_CUBIC = new Interpolator() {
        @Override protected double curve(double t) {
            return 1.0 - Math.pow(1.0 - t, 3);
        }
    };

    /** Ease-out cuártico: desaceleración más pronunciada */
    public static final Interpolator EASE_OUT_QUART = new Interpolator() {
        @Override protected double curve(double t) {
            return 1.0 - Math.pow(1.0 - t, 4);
        }
    };

    /** Ease-out quíntico: desaceleración muy pronunciada */
    public static final Interpolator EASE_OUT_QUINT = new Interpolator() {
        @Override protected double curve(double t) {
            return 1.0 - Math.pow(1.0 - t, 5);
        }
    };

    /** Ease-in cúbico: aceleración suave */
    public static final Interpolator EASE_IN_CUBIC = new Interpolator() {
        @Override protected double curve(double t) {
            return t * t * t;
        }
    };

    /** Ease-in-out suave: aceleración y desaceleración */
    public static final Interpolator EASE_IN_OUT = new Interpolator() {
        @Override protected double curve(double t) {
            return t < 0.5
                ? 4 * t * t * t
                : 1 - Math.pow(-2 * t + 2, 3) / 2;
        }
    };

    /** Ease-in-out cuártico */
    public static final Interpolator EASE_IN_OUT_QUART = new Interpolator() {
        @Override protected double curve(double t) {
            return t < 0.5
                ? 8 * t * t * t * t
                : 1 - Math.pow(-2 * t + 2, 4) / 2;
        }
    };

    /** Ease-out con rebote (overshoot sutil) - efecto "back" */
    public static final Interpolator EASE_OUT_BACK = new Interpolator() {
        private static final double C1 = 1.70158;
        private static final double C3 = C1 + 1;
        @Override protected double curve(double t) {
            return 1.0 + C3 * Math.pow(t - 1, 3) + C1 * Math.pow(t - 1, 2);
        }
    };

    /** Efecto spring/elástico (rebote suave al final) */
    public static final Interpolator SPRING = new Interpolator() {
        @Override protected double curve(double t) {
            double amplitude = 1.0;
            double period = 0.4;
            if (t == 0 || t == 1) return t;
            double s = period / 4;
            return amplitude * Math.pow(2, -10 * t) *
                   Math.sin((t - s) * (2 * Math.PI) / period) + 1;
        }
    };

    /** Ease-out elástico más sutil */
    public static final Interpolator EASE_OUT_ELASTIC = new Interpolator() {
        @Override protected double curve(double t) {
            if (t == 0 || t == 1) return t;
            return Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * (2 * Math.PI / 3)) + 1;
        }
    };

    /** Ease-out exponencial */
    public static final Interpolator EASE_OUT_EXPO = new Interpolator() {
        @Override protected double curve(double t) {
            return t == 1 ? 1 : 1 - Math.pow(2, -10 * t);
        }
    };

    /** Ease-out circular */
    public static final Interpolator EASE_OUT_CIRC = new Interpolator() {
        @Override protected double curve(double t) {
            return Math.sqrt(1 - Math.pow(t - 1, 2));
        }
    };

    // ═══════════════════════════════════════════════════════════════════════════
    // HOVER EFFECTS (Escala suave al pasar el mouse)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Animación de hover-in con escala predeterminada (1.03) y duración 150ms.
     */
    public static void hoverIn(Node nodo) {
        hoverIn(nodo, 1.03, 150);
    }

    /**
     * Animación de hover-in con escala y duración personalizadas.
     * @param nodo Nodo a animar
     * @param escala Escala objetivo (ej: 1.05)
     * @param duracionMs Duración en ms
     */
    public static void hoverIn(Node nodo, double escala, int duracionMs) {
        detenerAnimacionHover(nodo);
        ScaleTransition st = new ScaleTransition(Duration.millis(duracionMs), nodo);
        st.setToX(escala);
        st.setToY(escala);
        st.setInterpolator(EASE_OUT_CUBIC);
        nodo.getProperties().put("_hoverAnim", st);
        st.play();
    }

    /**
     * Animación de hover-out con duración predeterminada (150ms).
     */
    public static void hoverOut(Node nodo) {
        hoverOut(nodo, 150);
    }

    /**
     * Animación de hover-out con duración personalizada.
     */
    public static void hoverOut(Node nodo, int duracionMs) {
        detenerAnimacionHover(nodo);
        ScaleTransition st = new ScaleTransition(Duration.millis(duracionMs), nodo);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(EASE_OUT_CUBIC);
        nodo.getProperties().put("_hoverAnim", st);
        st.play();
    }

    /**
     * Animación de presión (click down) - reduce escala ligeramente.
     */
    public static void press(Node nodo) {
        detenerAnimacionHover(nodo);
        ScaleTransition st = new ScaleTransition(Duration.millis(80), nodo);
        st.setToX(0.95);
        st.setToY(0.95);
        st.setInterpolator(EASE_OUT_CUBIC);
        nodo.getProperties().put("_hoverAnim", st);
        st.play();
    }

    /**
     * Animación de soltar (click up) - restaura escala.
     */
    public static void release(Node nodo) {
        detenerAnimacionHover(nodo);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), nodo);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(EASE_OUT_BACK);
        nodo.getProperties().put("_hoverAnim", st);
        st.play();
    }

    private static void detenerAnimacionHover(Node nodo) {
        Object prev = nodo.getProperties().get("_hoverAnim");
        if (prev instanceof Animation) {
            ((Animation) prev).stop();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HOVER COMPLETO (Registra automáticamente enter/exit/press/release)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Registra animaciones hover completas en un nodo (enter, exit, press, release).
     * @param nodo Nodo al que aplicar efectos
     * @param escala Escala al hacer hover (ej: 1.05)
     * @param duracionMs Duración de la transición
     */
    public static void registrarHoverCompleto(Node nodo, double escala, int duracionMs) {
        nodo.setOnMouseEntered(e -> hoverIn(nodo, escala, duracionMs));
        nodo.setOnMouseExited(e -> hoverOut(nodo, duracionMs));
        nodo.setOnMousePressed(e -> press(nodo));
        nodo.setOnMouseReleased(e -> release(nodo));
    }

    /**
     * Registra hover simple (solo escala, sin press/release).
     */
    public static void registrarHover(Node nodo, double escala, int duracionMs) {
        nodo.setOnMouseEntered(e -> hoverIn(nodo, escala, duracionMs));
        nodo.setOnMouseExited(e -> hoverOut(nodo, duracionMs));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TRANSICIONES DE ENTRADA
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Fade-in simple.
     */
    public static FadeTransition fadeIn(Node nodo, int duracionMs) {
        nodo.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(duracionMs), nodo);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(EASE_OUT_CUBIC);
        ft.play();
        return ft;
    }

    /**
     * Fade-in con delay.
     */
    public static FadeTransition fadeIn(Node nodo, int duracionMs, int delayMs) {
        nodo.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(duracionMs), nodo);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delayMs));
        ft.setInterpolator(EASE_OUT_CUBIC);
        ft.play();
        return ft;
    }

    /**
     * Fade-out.
     */
    public static FadeTransition fadeOut(Node nodo, int duracionMs) {
        FadeTransition ft = new FadeTransition(Duration.millis(duracionMs), nodo);
        ft.setFromValue(nodo.getOpacity());
        ft.setToValue(0);
        ft.setInterpolator(EASE_IN_CUBIC);
        ft.play();
        return ft;
    }

    /**
     * Slide desde abajo + fade-in (entrada premium).
     */
    public static ParallelTransition slideUpFadeIn(Node nodo, int duracionMs, double distancia) {
        nodo.setOpacity(0);
        nodo.setTranslateY(distancia);

        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
        fade.setToValue(1);
        fade.setInterpolator(EASE_OUT_CUBIC);

        TranslateTransition slide = new TranslateTransition(Duration.millis(duracionMs), nodo);
        slide.setToY(0);
        slide.setInterpolator(EASE_OUT_CUBIC);

        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.play();
        return pt;
    }

    /**
     * Slide desde abajo + fade-in con delay.
     */
    public static ParallelTransition slideUpFadeIn(Node nodo, int duracionMs, double distancia, int delayMs) {
        nodo.setOpacity(0);
        nodo.setTranslateY(distancia);

        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
        fade.setToValue(1);
        fade.setInterpolator(EASE_OUT_CUBIC);

        TranslateTransition slide = new TranslateTransition(Duration.millis(duracionMs), nodo);
        slide.setToY(0);
        slide.setInterpolator(EASE_OUT_CUBIC);

        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.setDelay(Duration.millis(delayMs));
        pt.play();
        return pt;
    }

    /**
     * Slide desde la derecha + fade-in.
     */
    public static ParallelTransition slideLeftFadeIn(Node nodo, int duracionMs, double distancia) {
        nodo.setOpacity(0);
        nodo.setTranslateX(distancia);

        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
        fade.setToValue(1);
        fade.setInterpolator(EASE_OUT_CUBIC);

        TranslateTransition slide = new TranslateTransition(Duration.millis(duracionMs), nodo);
        slide.setToX(0);
        slide.setInterpolator(EASE_OUT_CUBIC);

        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.play();
        return pt;
    }

    /**
     * Slide desde la derecha + fade-in con delay.
     */
    public static ParallelTransition slideLeftFadeIn(Node nodo, int duracionMs, double distancia, int delayMs) {
        nodo.setOpacity(0);
        nodo.setTranslateX(distancia);

        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
        fade.setToValue(1);
        fade.setInterpolator(EASE_OUT_CUBIC);

        TranslateTransition slide = new TranslateTransition(Duration.millis(duracionMs), nodo);
        slide.setToX(0);
        slide.setInterpolator(EASE_OUT_CUBIC);

        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.setDelay(Duration.millis(delayMs));
        pt.play();
        return pt;
    }

    /**
     * Slide desde la izquierda + fade-in.
     */
    public static ParallelTransition slideRightFadeIn(Node nodo, int duracionMs, double distancia) {
        return slideLeftFadeIn(nodo, duracionMs, -distancia);
    }

    /**
     * Slide desde arriba + fade-in.
     */
    public static ParallelTransition slideDownFadeIn(Node nodo, int duracionMs, double distancia) {
        nodo.setOpacity(0);
        nodo.setTranslateY(-distancia);

        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
        fade.setToValue(1);
        fade.setInterpolator(EASE_OUT_CUBIC);

        TranslateTransition slide = new TranslateTransition(Duration.millis(duracionMs), nodo);
        slide.setToY(0);
        slide.setInterpolator(EASE_OUT_CUBIC);

        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.play();
        return pt;
    }

    /**
     * Scale-in + fade-in (zoom desde el centro).
     */
    public static ParallelTransition scaleIn(Node nodo, int duracionMs, double desdeEscala) {
        nodo.setOpacity(0);
        nodo.setScaleX(desdeEscala);
        nodo.setScaleY(desdeEscala);

        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
        fade.setToValue(1);
        fade.setInterpolator(EASE_OUT_CUBIC);

        ScaleTransition scale = new ScaleTransition(Duration.millis(duracionMs), nodo);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(EASE_OUT_BACK);

        ParallelTransition pt = new ParallelTransition(fade, scale);
        pt.play();
        return pt;
    }

    /**
     * Scale-in + fade-in con delay.
     */
    public static ParallelTransition scaleIn(Node nodo, int duracionMs, double desdeEscala, int delayMs) {
        nodo.setOpacity(0);
        nodo.setScaleX(desdeEscala);
        nodo.setScaleY(desdeEscala);

        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
        fade.setToValue(1);
        fade.setInterpolator(EASE_OUT_CUBIC);

        ScaleTransition scale = new ScaleTransition(Duration.millis(duracionMs), nodo);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(EASE_OUT_BACK);

        ParallelTransition pt = new ParallelTransition(fade, scale);
        pt.setDelay(Duration.millis(delayMs));
        pt.play();
        return pt;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TRANSICIONES DE SALIDA
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Fade-out + slide hacia abajo (salida premium).
     */
    public static ParallelTransition slideDownFadeOut(Node nodo, int duracionMs, double distancia) {
        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
        fade.setToValue(0);
        fade.setInterpolator(EASE_IN_CUBIC);

        TranslateTransition slide = new TranslateTransition(Duration.millis(duracionMs), nodo);
        slide.setToY(distancia);
        slide.setInterpolator(EASE_IN_CUBIC);

        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.play();
        return pt;
    }

    /**
     * Scale-out + fade-out (zoom out).
     */
    public static ParallelTransition scaleOut(Node nodo, int duracionMs, double hastaEscala) {
        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
        fade.setToValue(0);
        fade.setInterpolator(EASE_IN_CUBIC);

        ScaleTransition scale = new ScaleTransition(Duration.millis(duracionMs), nodo);
        scale.setToX(hastaEscala);
        scale.setToY(hastaEscala);
        scale.setInterpolator(EASE_IN_CUBIC);

        ParallelTransition pt = new ParallelTransition(fade, scale);
        pt.play();
        return pt;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMACIONES ESCALONADAS (Cascada / Stagger)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Anima una lista de nodos con entrada escalonada (slide-up + fade-in).
     * Cada nodo entra con un delay incremental, creando efecto cascada.
     *
     * @param nodos Lista de nodos a animar
     * @param duracionMs Duración de cada animación individual
     * @param delayEntreMs Delay entre cada nodo (ej: 50-80ms)
     * @param distancia Distancia del slide (ej: 20-30px)
     */
    public static void entradaEscalonada(List<Node> nodos, int duracionMs, int delayEntreMs, double distancia) {
        for (int i = 0; i < nodos.size(); i++) {
            Node nodo = nodos.get(i);
            nodo.setOpacity(0);
            nodo.setTranslateY(distancia);

            FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
            fade.setToValue(1);
            fade.setInterpolator(EASE_OUT_CUBIC);

            TranslateTransition slide = new TranslateTransition(Duration.millis(duracionMs), nodo);
            slide.setToY(0);
            slide.setInterpolator(EASE_OUT_CUBIC);

            ParallelTransition pt = new ParallelTransition(fade, slide);
            pt.setDelay(Duration.millis(i * delayEntreMs));
            pt.play();
        }
    }

    /**
     * Entrada escalonada con scale-in (zoom + fade en cascada).
     */
    public static void entradaEscalonadaScale(List<Node> nodos, int duracionMs, int delayEntreMs) {
        for (int i = 0; i < nodos.size(); i++) {
            Node nodo = nodos.get(i);
            nodo.setOpacity(0);
            nodo.setScaleX(0.8);
            nodo.setScaleY(0.8);

            FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
            fade.setToValue(1);
            fade.setInterpolator(EASE_OUT_CUBIC);

            ScaleTransition scale = new ScaleTransition(Duration.millis(duracionMs), nodo);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setInterpolator(EASE_OUT_BACK);

            ParallelTransition pt = new ParallelTransition(fade, scale);
            pt.setDelay(Duration.millis(i * delayEntreMs));
            pt.play();
        }
    }

    /**
     * Entrada escalonada desde la izquierda (slide-right + fade-in en cascada).
     */
    public static void entradaEscalonadaDesdeIzquierda(List<Node> nodos, int duracionMs, int delayEntreMs, double distancia) {
        for (int i = 0; i < nodos.size(); i++) {
            Node nodo = nodos.get(i);
            nodo.setOpacity(0);
            nodo.setTranslateX(-distancia);

            FadeTransition fade = new FadeTransition(Duration.millis(duracionMs), nodo);
            fade.setToValue(1);
            fade.setInterpolator(EASE_OUT_CUBIC);

            TranslateTransition slide = new TranslateTransition(Duration.millis(duracionMs), nodo);
            slide.setToX(0);
            slide.setInterpolator(EASE_OUT_CUBIC);

            ParallelTransition pt = new ParallelTransition(fade, slide);
            pt.setDelay(Duration.millis(i * delayEntreMs));
            pt.play();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMACIONES ESPECIALES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Animación de pulso sutil (para notificaciones, alertas).
     */
    public static ScaleTransition pulso(Node nodo, double escalaMax, int duracionMs, int ciclos) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(duracionMs), nodo);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(escalaMax);
        pulse.setToY(escalaMax);
        pulse.setCycleCount(ciclos * 2);
        pulse.setAutoReverse(true);
        pulse.setInterpolator(EASE_IN_OUT);
        pulse.play();
        return pulse;
    }

    /**
     * Animación de shake horizontal (para errores de validación).
     */
    public static TranslateTransition shake(Node nodo, double amplitud, int duracionMs) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(duracionMs / 6), nodo);
        shake.setFromX(0);
        shake.setByX(amplitud);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> nodo.setTranslateX(0));
        shake.play();
        return shake;
    }

    /**
     * Efecto de brillo/glow pulsante.
     */
    public static Timeline glowPulse(Node nodo, Color color, double radiusMin, double radiusMax, int duracionMs) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(radiusMin);
        glow.setSpread(0.3);
        nodo.setEffect(glow);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(glow.radiusProperty(), radiusMin, EASE_IN_OUT)),
            new KeyFrame(Duration.millis(duracionMs),
                new KeyValue(glow.radiusProperty(), radiusMax, EASE_IN_OUT))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.play();

        nodo.getProperties().put("_glowAnim", timeline);
        return timeline;
    }

    /**
     * Detiene el efecto glow pulsante.
     */
    public static void detenerGlow(Node nodo) {
        Object anim = nodo.getProperties().remove("_glowAnim");
        if (anim instanceof Timeline) {
            ((Timeline) anim).stop();
        }
        nodo.setEffect(null);
    }

    /**
     * Efecto de rebote (bounce) al aparecer.
     */
    public static void bounce(Node nodo, int duracionMs) {
        nodo.setScaleX(0);
        nodo.setScaleY(0);
        nodo.setOpacity(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(duracionMs), nodo);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(EASE_OUT_ELASTIC);

        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs * 0.4), nodo);
        fade.setToValue(1);

        ParallelTransition pt = new ParallelTransition(fade, scale);
        pt.play();
    }

    /**
     * Efecto de rebote con delay.
     */
    public static void bounce(Node nodo, int duracionMs, int delayMs) {
        nodo.setScaleX(0);
        nodo.setScaleY(0);
        nodo.setOpacity(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(duracionMs), nodo);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(EASE_OUT_ELASTIC);

        FadeTransition fade = new FadeTransition(Duration.millis(duracionMs * 0.4), nodo);
        fade.setToValue(1);

        ParallelTransition pt = new ParallelTransition(fade, scale);
        pt.setDelay(Duration.millis(delayMs));
        pt.play();
    }

    /**
     * Rotación suave (para spinners/loading).
     */
    public static RotateTransition spin(Node nodo, int duracionMs) {
        RotateTransition rt = new RotateTransition(Duration.millis(duracionMs), nodo);
        rt.setByAngle(360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.play();
        nodo.getProperties().put("_spinAnim", rt);
        return rt;
    }

    /**
     * Detiene la rotación.
     */
    public static void detenerSpin(Node nodo) {
        Object anim = nodo.getProperties().remove("_spinAnim");
        if (anim instanceof RotateTransition) {
            ((RotateTransition) anim).stop();
        }
        nodo.setRotate(0);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TRANSICIONES DE CONTENIDO (Cambio entre vistas)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Transición cross-fade entre dos nodos (uno sale, otro entra).
     * @param saliente Nodo que desaparece
     * @param entrante Nodo que aparece
     * @param duracionMs Duración total
     * @param onFinished Callback al terminar
     */
    public static void crossFade(Node saliente, Node entrante, int duracionMs, Runnable onFinished) {
        entrante.setOpacity(0);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(duracionMs / 2), saliente);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(EASE_IN_CUBIC);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(duracionMs / 2), entrante);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(EASE_OUT_CUBIC);

        fadeOut.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
            fadeIn.play();
        });
        fadeOut.play();
    }

    /**
     * Transición slide horizontal entre dos vistas (efecto carousel).
     * @param saliente Nodo que sale (se desliza a la izquierda)
     * @param entrante Nodo que entra (se desliza desde la derecha)
     * @param duracionMs Duración
     * @param ancho Ancho de la vista (para el desplazamiento)
     */
    public static void slideHorizontal(Node saliente, Node entrante, int duracionMs, double ancho) {
        entrante.setTranslateX(ancho);
        entrante.setOpacity(1);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(duracionMs), saliente);
        slideOut.setToX(-ancho * 0.3);
        slideOut.setInterpolator(EASE_IN_OUT);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(duracionMs), saliente);
        fadeOut.setToValue(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(duracionMs), entrante);
        slideIn.setToX(0);
        slideIn.setInterpolator(EASE_OUT_CUBIC);

        ParallelTransition pt = new ParallelTransition(slideOut, fadeOut, slideIn);
        pt.play();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMACIONES DE SOMBRA
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Anima la sombra de un nodo al hacer hover (eleva la tarjeta).
     */
    public static void elevarSombra(Node nodo, Color color, double radioInicial, double radioFinal, int duracionMs) {
        Effect currentEffect = nodo.getEffect();
        DropShadow shadow;
        if (currentEffect instanceof DropShadow) {
            shadow = (DropShadow) currentEffect;
        } else {
            shadow = new DropShadow();
            shadow.setColor(color);
            shadow.setRadius(radioInicial);
            nodo.setEffect(shadow);
        }

        Timeline tl = new Timeline(
            new KeyFrame(Duration.millis(duracionMs),
                new KeyValue(shadow.radiusProperty(), radioFinal, EASE_OUT_CUBIC),
                new KeyValue(shadow.offsetYProperty(), radioFinal / 3, EASE_OUT_CUBIC)
            )
        );
        tl.play();
    }

    /**
     * Restaura la sombra a su estado original.
     */
    public static void restaurarSombra(Node nodo, double radioInicial, int duracionMs) {
        Effect currentEffect = nodo.getEffect();
        if (currentEffect instanceof DropShadow) {
            DropShadow shadow = (DropShadow) currentEffect;
            Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(duracionMs),
                    new KeyValue(shadow.radiusProperty(), radioInicial, EASE_OUT_CUBIC),
                    new KeyValue(shadow.offsetYProperty(), radioInicial / 4, EASE_OUT_CUBIC)
                )
            );
            tl.play();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMACIONES DE PROGRESO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Anima una barra de progreso de 0 a un valor objetivo.
     */
    public static Timeline animarProgreso(javafx.scene.control.ProgressBar progressBar, 
                                           double objetivo, int duracionMs) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(progressBar.progressProperty(), 0, EASE_OUT_CUBIC)),
            new KeyFrame(Duration.millis(duracionMs),
                new KeyValue(progressBar.progressProperty(), objetivo, EASE_OUT_QUART))
        );
        tl.play();
        return tl;
    }

    /**
     * Anima una barra de progreso con delay.
     */
    public static Timeline animarProgreso(javafx.scene.control.ProgressBar progressBar, 
                                           double objetivo, int duracionMs, int delayMs) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.millis(delayMs),
                new KeyValue(progressBar.progressProperty(), 0)),
            new KeyFrame(Duration.millis(delayMs + duracionMs),
                new KeyValue(progressBar.progressProperty(), objetivo, EASE_OUT_QUART))
        );
        tl.play();
        return tl;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMACIÓN DE OPACIDAD TIPO "BREATHE" (Respiro suave)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Efecto de "respiración" (opacidad pulsante suave).
     * Ideal para indicadores de estado, elementos de carga.
     */
    public static FadeTransition breathe(Node nodo, double opMin, double opMax, int duracionMs) {
        FadeTransition ft = new FadeTransition(Duration.millis(duracionMs), nodo);
        ft.setFromValue(opMin);
        ft.setToValue(opMax);
        ft.setCycleCount(Animation.INDEFINITE);
        ft.setAutoReverse(true);
        ft.setInterpolator(EASE_IN_OUT);
        ft.play();
        nodo.getProperties().put("_breatheAnim", ft);
        return ft;
    }

    /**
     * Detiene el efecto de respiración.
     */
    public static void detenerBreathe(Node nodo) {
        Object anim = nodo.getProperties().remove("_breatheAnim");
        if (anim instanceof FadeTransition) {
            ((FadeTransition) anim).stop();
        }
        nodo.setOpacity(1);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Crea un Timeline con múltiples KeyFrames usando ease-out.
     * Permite definir animaciones complejas de forma declarativa.
     */
    public static Timeline crearTimeline(KeyFrame... frames) {
        Timeline tl = new Timeline(frames);
        return tl;
    }

    /**
     * Ejecuta un Runnable después de un delay (sin bloquear el hilo de UI).
     */
    public static PauseTransition delay(int ms, Runnable accion) {
        PauseTransition pause = new PauseTransition(Duration.millis(ms));
        pause.setOnFinished(e -> accion.run());
        pause.play();
        return pause;
    }

    /**
     * Detiene todas las animaciones almacenadas de un nodo.
     */
    public static void detenerTodo(Node nodo) {
        for (String key : new String[]{"_hoverAnim", "_glowAnim", "_spinAnim", "_breatheAnim", "_countAnim"}) {
            Object anim = nodo.getProperties().remove(key);
            if (anim instanceof Animation) {
                ((Animation) anim).stop();
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // ANIMACIONES DE DIÁLOGO — Entrada y salida unificadas
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Anima la entrada de un diálogo con fade + scale + slide up.
     * Debe llamarse DESPUÉS de stage.show() o mostrar la escena.
     * Prepara el nodo con opacity=0, scaleX/Y=0.92, translateY=18 y lo anima a su posición final.
     * 
     * @param card nodo raíz del contenido del diálogo
     */
    public static void animarEntradaDialogo(Node card) {
        animarEntradaDialogo(card, 0);
    }

    /**
     * Anima la entrada de un diálogo con fade + scale + slide up y delay opcional.
     */
    public static void animarEntradaDialogo(Node card, int delayMs) {
        card.setOpacity(0);
        card.setScaleX(0.92);
        card.setScaleY(0.92);
        card.setTranslateY(18);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(EASE_OUT_CUBIC);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(350), card);
        scaleIn.setFromX(0.92);
        scaleIn.setFromY(0.92);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setInterpolator(EASE_OUT_CUBIC);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), card);
        slideIn.setFromY(18);
        slideIn.setToY(0);
        slideIn.setInterpolator(EASE_OUT_CUBIC);

        javafx.animation.ParallelTransition entrada = new javafx.animation.ParallelTransition(fadeIn, scaleIn, slideIn);
        if (delayMs > 0) {
            entrada.setDelay(Duration.millis(delayMs));
        }
        entrada.play();
    }

    /**
     * Anima la salida de un diálogo con fade + scale + slide down, luego cierra el Stage.
     * 
     * @param dialog Stage del diálogo
     * @param card nodo raíz del contenido del diálogo
     */
    public static void animarSalidaDialogo(javafx.stage.Stage dialog, Node card) {
        animarSalidaDialogo(dialog, card, null);
    }

    /**
     * Anima la salida de un diálogo con callback opcional después de cerrar.
     */
    public static void animarSalidaDialogo(javafx.stage.Stage dialog, Node card, Runnable onClosed) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), card);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(EASE_IN_CUBIC);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(220), card);
        scaleOut.setToX(0.88);
        scaleOut.setToY(0.88);
        scaleOut.setInterpolator(EASE_IN_CUBIC);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), card);
        slideOut.setToY(10);
        slideOut.setInterpolator(EASE_IN_CUBIC);

        javafx.animation.ParallelTransition salida = new javafx.animation.ParallelTransition(fadeOut, scaleOut, slideOut);
        salida.setOnFinished(ev -> {
            dialog.close();
            if (onClosed != null) onClosed.run();
        });
        salida.play();
    }
}
