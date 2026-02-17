package inventario.fx.icons;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.InputStream;

/**
 * Clase de utilidad para crear íconos vectoriales SVG profesionales.
 * Todos los íconos se escalan a un tamaño base de 16x16.
 * 
 * VERSIÓN 6.0 - Minimalista Elegante Profesional
 * 
 * Filosofía de diseño:
 * - MINIMALISTA: Formas esenciales, sin detalles innecesarios
 * - ELEGANTE: Proporciones equilibradas, aspecto refinado
 * - CONSISTENTE: Trazo uniforme 2px, esquinas redondeadas 1.5px
 * - RECONOCIBLE: Símbolos claros incluso en tamaños pequeños
 * - PROFESIONAL: Relleno sutil 10% para presencia visual
 */
public class IconosSVG {

    // ═══════════════════════════════════════════════════════════════════════
    // ÍCONOS DE NAVEGACIÓN Y SISTEMA (v6.0 - Minimalista Elegante)
    // ═══════════════════════════════════════════════════════════════════════

    /** Ícono de computadora - Monitor (Lucide 'monitor') */
    public static Node computadora(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial de Lucide: <rect width="20" height="14" x="2" y="3" rx="2"/><line x1="8" x2="16" y1="21" y2="21"/><line x1="12" x2="12" y1="17" y2="21"/>
        Rectangle monitor = new Rectangle(2*s, 3*s, 20*s, 14*s);
        monitor.setArcWidth(4*s);
        monitor.setArcHeight(4*s);
        monitor.setFill(Color.TRANSPARENT);
        monitor.setStroke(Color.web(color));
        monitor.setStrokeWidth(2*s);
        monitor.setStrokeLineCap(StrokeLineCap.ROUND);
        monitor.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Line base = new Line(8*s, 21*s, 16*s, 21*s);
        base.setStroke(Color.web(color));
        base.setStrokeWidth(2*s);
        base.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line soporte = new Line(12*s, 17*s, 12*s, 21*s);
        soporte.setStroke(Color.web(color));
        soporte.setStrokeWidth(2*s);
        soporte.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(monitor, soporte, base);
        return g;
    }

    /** Ícono de laptop - Portátil (Lucide 'laptop') */
    public static Node laptop(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial de Lucide: <path d="M20 16V7a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v9m16 0H4m16 0 1.28 2.55a1 1 0 0 1-.9 1.45H3.62a1 1 0 0 1-.9-1.45L4 16"/>
        Path laptop = new Path();
        laptop.getElements().addAll(
            new MoveTo(20*s, 16*s),
            new LineTo(20*s, 7*s),
            new CubicCurveTo(20*s, 5.9*s, 19.1*s, 5*s, 18*s, 5*s),
            new LineTo(6*s, 5*s),
            new CubicCurveTo(4.9*s, 5*s, 4*s, 5.9*s, 4*s, 7*s),
            new LineTo(4*s, 16*s),
            new MoveTo(20*s, 16*s),
            new LineTo(4*s, 16*s),
            new MoveTo(20*s, 16*s),
            new LineTo(21.28*s, 18.55*s),
            new CubicCurveTo(21.65*s, 19.24*s, 21.14*s, 20*s, 20.38*s, 20*s),
            new LineTo(3.62*s, 20*s),
            new CubicCurveTo(2.86*s, 20*s, 2.35*s, 19.24*s, 2.72*s, 18.55*s),
            new LineTo(4*s, 16*s)
        );
        laptop.setFill(Color.TRANSPARENT);
        laptop.setStroke(Color.web(color));
        laptop.setStrokeWidth(2*s);
        laptop.setStrokeLineCap(StrokeLineCap.ROUND);
        laptop.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().add(laptop);
        return g;
    }

    /** Ícono de monitor de pantalla (Lucide 'monitor') - para sección Monitor */
    public static Node monitor(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Monitor con pantalla más grande
        Rectangle pantalla = new Rectangle(2*s, 3*s, 20*s, 14*s);
        pantalla.setArcWidth(4*s);
        pantalla.setArcHeight(4*s);
        pantalla.setFill(Color.TRANSPARENT);
        pantalla.setStroke(Color.web(color));
        pantalla.setStrokeWidth(2*s);
        pantalla.setStrokeLineCap(StrokeLineCap.ROUND);
        pantalla.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Line base = new Line(8*s, 21*s, 16*s, 21*s);
        base.setStroke(Color.web(color));
        base.setStrokeWidth(2*s);
        base.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line soporte = new Line(12*s, 17*s, 12*s, 21*s);
        soporte.setStroke(Color.web(color));
        soporte.setStrokeWidth(2*s);
        soporte.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(pantalla, soporte, base);
        return g;
    }

    /** Ícono de teclado (Lucide 'keyboard') */
    public static Node teclado(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Rectángulo del teclado
        Rectangle carcasa = new Rectangle(2*s, 4*s, 20*s, 16*s);
        carcasa.setArcWidth(4*s);
        carcasa.setArcHeight(4*s);
        carcasa.setFill(Color.TRANSPARENT);
        carcasa.setStroke(Color.web(color));
        carcasa.setStrokeWidth(2*s);
        carcasa.setStrokeLineCap(StrokeLineCap.ROUND);
        carcasa.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Teclas superiores (fila 1)
        Line tecla1 = new Line(6*s, 8*s, 6.01*s, 8*s);
        tecla1.setStroke(Color.web(color));
        tecla1.setStrokeWidth(2*s);
        tecla1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line tecla2 = new Line(10*s, 8*s, 10.01*s, 8*s);
        tecla2.setStroke(Color.web(color));
        tecla2.setStrokeWidth(2*s);
        tecla2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line tecla3 = new Line(14*s, 8*s, 14.01*s, 8*s);
        tecla3.setStroke(Color.web(color));
        tecla3.setStrokeWidth(2*s);
        tecla3.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line tecla4 = new Line(18*s, 8*s, 18.01*s, 8*s);
        tecla4.setStroke(Color.web(color));
        tecla4.setStrokeWidth(2*s);
        tecla4.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Teclas medias (fila 2)
        Line tecla5 = new Line(6*s, 12*s, 6.01*s, 12*s);
        tecla5.setStroke(Color.web(color));
        tecla5.setStrokeWidth(2*s);
        tecla5.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line tecla6 = new Line(10*s, 12*s, 10.01*s, 12*s);
        tecla6.setStroke(Color.web(color));
        tecla6.setStrokeWidth(2*s);
        tecla6.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line tecla7 = new Line(14*s, 12*s, 14.01*s, 12*s);
        tecla7.setStroke(Color.web(color));
        tecla7.setStrokeWidth(2*s);
        tecla7.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line tecla8 = new Line(18*s, 12*s, 18.01*s, 12*s);
        tecla8.setStroke(Color.web(color));
        tecla8.setStrokeWidth(2*s);
        tecla8.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Barra espaciadora (fila 3)
        Line barra = new Line(9*s, 16*s, 15*s, 16*s);
        barra.setStroke(Color.web(color));
        barra.setStrokeWidth(2*s);
        barra.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(carcasa, tecla1, tecla2, tecla3, tecla4, tecla5, tecla6, tecla7, tecla8, barra);
        return g;
    }

    /** Ícono de servidor (Lucide 'server') */
    public static Node servidor(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial de Lucide: <rect width="20" height="8" x="2" y="2" rx="2" ry="2"/><rect width="20" height="8" x="2" y="14" rx="2" ry="2"/><line x1="6" x2="6.01" y1="6" y2="6"/><line x1="6" x2="6.01" y1="18" y2="18"/>
        Rectangle rack1 = new Rectangle(2*s, 2*s, 20*s, 8*s);
        rack1.setArcWidth(4*s);
        rack1.setArcHeight(4*s);
        rack1.setFill(Color.TRANSPARENT);
        rack1.setStroke(Color.web(color));
        rack1.setStrokeWidth(2*s);
        rack1.setStrokeLineCap(StrokeLineCap.ROUND);
        rack1.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Rectangle rack2 = new Rectangle(2*s, 14*s, 20*s, 8*s);
        rack2.setArcWidth(4*s);
        rack2.setArcHeight(4*s);
        rack2.setFill(Color.TRANSPARENT);
        rack2.setStroke(Color.web(color));
        rack2.setStrokeWidth(2*s);
        rack2.setStrokeLineCap(StrokeLineCap.ROUND);
        rack2.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Line led1 = new Line(6*s, 6*s, 6.01*s, 6*s);
        led1.setStroke(Color.web(color));
        led1.setStrokeWidth(2*s);
        led1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line led2 = new Line(6*s, 18*s, 6.01*s, 18*s);
        led2.setStroke(Color.web(color));
        led2.setStrokeWidth(2*s);
        led2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(rack1, rack2, led1, led2);
        return g;
    }

    /** Ícono de reportes (Lucide 'file-text') */
    public static Node reportes(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial de Lucide: <path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/><path d="M10 9H8"/><path d="M16 13H8"/><path d="M16 17H8"/>
        Path doc = new Path();
        doc.getElements().addAll(
            new MoveTo(15*s, 2*s),
            new LineTo(6*s, 2*s),
            new CubicCurveTo(4.9*s, 2*s, 4*s, 2.9*s, 4*s, 4*s),
            new LineTo(4*s, 20*s),
            new CubicCurveTo(4*s, 21.1*s, 4.9*s, 22*s, 6*s, 22*s),
            new LineTo(18*s, 22*s),
            new CubicCurveTo(19.1*s, 22*s, 20*s, 21.1*s, 20*s, 20*s),
            new LineTo(20*s, 7*s),
            new ClosePath(),
            new MoveTo(14*s, 2*s),
            new LineTo(14*s, 6*s),
            new CubicCurveTo(14*s, 7.1*s, 14.9*s, 8*s, 16*s, 8*s),
            new LineTo(20*s, 8*s)
        );
        doc.setFill(Color.TRANSPARENT);
        doc.setStroke(Color.web(color));
        doc.setStrokeWidth(2*s);
        doc.setStrokeLineCap(StrokeLineCap.ROUND);
        doc.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Line line1 = new Line(10*s, 9*s, 8*s, 9*s);
        line1.setStroke(Color.web(color));
        line1.setStrokeWidth(2*s);
        line1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line line2 = new Line(16*s, 13*s, 8*s, 13*s);
        line2.setStroke(Color.web(color));
        line2.setStrokeWidth(2*s);
        line2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line line3 = new Line(16*s, 17*s, 8*s, 17*s);
        line3.setStroke(Color.web(color));
        line3.setStrokeWidth(2*s);
        line3.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(doc, line1, line2, line3);
        return g;
    }

    /** Ícono de dashboard (Lucide 'layout-dashboard') */
    public static Node dashboard(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Rectángulo superior izquierdo
        Path rect1 = new Path();
        rect1.getElements().addAll(
            new MoveTo(5*s, 3*s),
            new LineTo(9*s, 3*s),
            new CubicCurveTo(9.55*s, 3*s, 10*s, 3.45*s, 10*s, 4*s),
            new LineTo(10*s, 10*s),
            new CubicCurveTo(10*s, 10.55*s, 9.55*s, 11*s, 9*s, 11*s),
            new LineTo(5*s, 11*s),
            new CubicCurveTo(4.45*s, 11*s, 4*s, 10.55*s, 4*s, 10*s),
            new LineTo(4*s, 4*s),
            new CubicCurveTo(4*s, 3.45*s, 4.45*s, 3*s, 5*s, 3*s),
            new ClosePath()
        );
        rect1.setFill(Color.TRANSPARENT);
        rect1.setStroke(Color.web(color));
        rect1.setStrokeWidth(2*s);
        rect1.setStrokeLineCap(StrokeLineCap.ROUND);
        rect1.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Rectángulo superior derecho
        Path rect2 = new Path();
        rect2.getElements().addAll(
            new MoveTo(15*s, 3*s),
            new LineTo(19*s, 3*s),
            new CubicCurveTo(19.55*s, 3*s, 20*s, 3.45*s, 20*s, 4*s),
            new LineTo(20*s, 7*s),
            new CubicCurveTo(20*s, 7.55*s, 19.55*s, 8*s, 19*s, 8*s),
            new LineTo(15*s, 8*s),
            new CubicCurveTo(14.45*s, 8*s, 14*s, 7.55*s, 14*s, 7*s),
            new LineTo(14*s, 4*s),
            new CubicCurveTo(14*s, 3.45*s, 14.45*s, 3*s, 15*s, 3*s),
            new ClosePath()
        );
        rect2.setFill(Color.TRANSPARENT);
        rect2.setStroke(Color.web(color));
        rect2.setStrokeWidth(2*s);
        rect2.setStrokeLineCap(StrokeLineCap.ROUND);
        rect2.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Rectángulo inferior izquierdo
        Path rect3 = new Path();
        rect3.getElements().addAll(
            new MoveTo(5*s, 16*s),
            new LineTo(9*s, 16*s),
            new CubicCurveTo(9.55*s, 16*s, 10*s, 16.45*s, 10*s, 17*s),
            new LineTo(10*s, 20*s),
            new CubicCurveTo(10*s, 20.55*s, 9.55*s, 21*s, 9*s, 21*s),
            new LineTo(5*s, 21*s),
            new CubicCurveTo(4.45*s, 21*s, 4*s, 20.55*s, 4*s, 20*s),
            new LineTo(4*s, 17*s),
            new CubicCurveTo(4*s, 16.45*s, 4.45*s, 16*s, 5*s, 16*s),
            new ClosePath()
        );
        rect3.setFill(Color.TRANSPARENT);
        rect3.setStroke(Color.web(color));
        rect3.setStrokeWidth(2*s);
        rect3.setStrokeLineCap(StrokeLineCap.ROUND);
        rect3.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Rectángulo inferior derecho (más alto)
        Path rect4 = new Path();
        rect4.getElements().addAll(
            new MoveTo(15*s, 13*s),
            new LineTo(19*s, 13*s),
            new CubicCurveTo(19.55*s, 13*s, 20*s, 13.45*s, 20*s, 14*s),
            new LineTo(20*s, 20*s),
            new CubicCurveTo(20*s, 20.55*s, 19.55*s, 21*s, 19*s, 21*s),
            new LineTo(15*s, 21*s),
            new CubicCurveTo(14.45*s, 21*s, 14*s, 20.55*s, 14*s, 20*s),
            new LineTo(14*s, 14*s),
            new CubicCurveTo(14*s, 13.45*s, 14.45*s, 13*s, 15*s, 13*s),
            new ClosePath()
        );
        rect4.setFill(Color.TRANSPARENT);
        rect4.setStroke(Color.web(color));
        rect4.setStrokeWidth(2*s);
        rect4.setStrokeLineCap(StrokeLineCap.ROUND);
        rect4.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(rect1, rect2, rect3, rect4);
        return g;
    }

    /** Ícono de estadísticas (Lucide 'chart-no-axes-column-increasing') */
    public static Node estadisticas(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial de Lucide: <line x1="12" x2="12" y1="20" y2="10"/><line x1="18" x2="18" y1="20" y2="4"/><line x1="6" x2="6" y1="20" y2="16"/>
        Line barra1 = new Line(12*s, 20*s, 12*s, 10*s);
        barra1.setStroke(Color.web(color));
        barra1.setStrokeWidth(2*s);
        barra1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line barra2 = new Line(18*s, 20*s, 18*s, 4*s);
        barra2.setStroke(Color.web(color));
        barra2.setStrokeWidth(2*s);
        barra2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line barra3 = new Line(6*s, 20*s, 6*s, 16*s);
        barra3.setStroke(Color.web(color));
        barra3.setStrokeWidth(2*s);
        barra3.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(barra3, barra1, barra2);
        return g;
    }

    /** Ícono de gráfico de línea (Lucide 'trending-up') */
    public static Node graficoLinea(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial de Lucide: <polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/>
        Path linea = new Path();
        linea.getElements().addAll(
            new MoveTo(22*s, 7*s),
            new LineTo(13.5*s, 15.5*s),
            new LineTo(8.5*s, 10.5*s),
            new LineTo(2*s, 17*s)
        );
        linea.setFill(Color.TRANSPARENT);
        linea.setStroke(Color.web(color));
        linea.setStrokeWidth(2*s);
        linea.setStrokeLineCap(StrokeLineCap.ROUND);
        linea.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Path flecha = new Path();
        flecha.getElements().addAll(
            new MoveTo(16*s, 7*s),
            new LineTo(22*s, 7*s),
            new LineTo(22*s, 13*s)
        );
        flecha.setFill(Color.TRANSPARENT);
        flecha.setStroke(Color.web(color));
        flecha.setStrokeWidth(2*s);
        flecha.setStrokeLineCap(StrokeLineCap.ROUND);
        flecha.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(linea, flecha);
        return g;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ÍCONOS DE ACCIONES (v6.0 - Minimalista Elegante)
    // ═══════════════════════════════════════════════════════════════════════

    /** Ícono de agregar - Plus (Lucide) */
    public static Node mas(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Línea horizontal
        Line h = new Line(12*s, 5*s, 12*s, 19*s);
        h.setStroke(Color.web(color));
        h.setStrokeWidth(2*s);
        h.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Línea vertical
        Line v = new Line(5*s, 12*s, 19*s, 12*s);
        v.setStroke(Color.web(color));
        v.setStrokeWidth(2*s);
        v.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(h, v);
        return g;
    }

    /** Ícono de descargar (Lucide 'download') */
    public static Node descargar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial: <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" x2="12" y1="15" y2="3"/>
        Path caja = new Path();
        caja.getElements().addAll(
            new MoveTo(21*s, 15*s),
            new LineTo(21*s, 19*s),
            new CubicCurveTo(21*s, 20.1*s, 20.1*s, 21*s, 19*s, 21*s),
            new LineTo(5*s, 21*s),
            new CubicCurveTo(3.9*s, 21*s, 3*s, 20.1*s, 3*s, 19*s),
            new LineTo(3*s, 15*s)
        );
        caja.setFill(Color.TRANSPARENT);
        caja.setStroke(Color.web(color));
        caja.setStrokeWidth(2*s);
        caja.setStrokeLineCap(StrokeLineCap.ROUND);
        caja.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Path flecha = new Path();
        flecha.getElements().addAll(
            new MoveTo(7*s, 10*s),
            new LineTo(12*s, 15*s),
            new LineTo(17*s, 10*s)
        );
        flecha.setFill(Color.TRANSPARENT);
        flecha.setStroke(Color.web(color));
        flecha.setStrokeWidth(2*s);
        flecha.setStrokeLineCap(StrokeLineCap.ROUND);
        flecha.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Line linea = new Line(12*s, 15*s, 12*s, 3*s);
        linea.setStroke(Color.web(color));
        linea.setStrokeWidth(2*s);
        linea.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(caja, flecha, linea);
        return g;
    }

    /** Ícono de subir/upload (Lucide 'upload') */
    public static Node subir(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial: <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" x2="12" y1="3" y2="15"/>
        Path caja = new Path();
        caja.getElements().addAll(
            new MoveTo(21*s, 15*s),
            new LineTo(21*s, 19*s),
            new CubicCurveTo(21*s, 20.1*s, 20.1*s, 21*s, 19*s, 21*s),
            new LineTo(5*s, 21*s),
            new CubicCurveTo(3.9*s, 21*s, 3*s, 20.1*s, 3*s, 19*s),
            new LineTo(3*s, 15*s)
        );
        caja.setFill(Color.TRANSPARENT);
        caja.setStroke(Color.web(color));
        caja.setStrokeWidth(2*s);
        caja.setStrokeLineCap(StrokeLineCap.ROUND);
        caja.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Path flecha = new Path();
        flecha.getElements().addAll(
            new MoveTo(17*s, 8*s),
            new LineTo(12*s, 3*s),
            new LineTo(7*s, 8*s)
        );
        flecha.setFill(Color.TRANSPARENT);
        flecha.setStroke(Color.web(color));
        flecha.setStrokeWidth(2*s);
        flecha.setStrokeLineCap(StrokeLineCap.ROUND);
        flecha.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Line linea = new Line(12*s, 3*s, 12*s, 15*s);
        linea.setStroke(Color.web(color));
        linea.setStrokeWidth(2*s);
        linea.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(caja, flecha, linea);
        return g;
    }

    /** Ícono de búsqueda (Lucide 'search') */
    public static Node buscar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial: <circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>
        Circle lupa = new Circle(11*s, 11*s, 8*s);
        lupa.setFill(Color.TRANSPARENT);
        lupa.setStroke(Color.web(color));
        lupa.setStrokeWidth(2*s);
        
        Line mango = new Line(21*s, 21*s, 16.7*s, 16.7*s);
        mango.setStroke(Color.web(color));
        mango.setStrokeWidth(2*s);
        mango.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(lupa, mango);
        return g;
    }

    /** Ícono de cerrar - X (Lucide) */
    public static Node cerrar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        Line l1 = new Line(18*s, 6*s, 6*s, 18*s);
        l1.setStroke(Color.web(color));
        l1.setStrokeWidth(2*s);
        l1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line l2 = new Line(6*s, 6*s, 18*s, 18*s);
        l2.setStroke(Color.web(color));
        l2.setStrokeWidth(2*s);
        l2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(l1, l2);
        return g;
    }

    /** Ícono de editar (Lucide 'pencil') */
    public static Node editar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial: <path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"/><path d="m15 5 4 4"/>
        Path lapiz = new Path();
        lapiz.getElements().addAll(
            new MoveTo(21.174*s, 6.812*s),
            new CubicCurveTo(21.567*s, 6.419*s, 21.567*s, 5.781*s, 21.174*s, 5.388*s),
            new CubicCurveTo(20.781*s, 4.995*s, 20.143*s, 4.995*s, 19.75*s, 5.388*s),
            new LineTo(17.188*s, 2.825*s),
            new CubicCurveTo(16.795*s, 2.433*s, 16.157*s, 2.433*s, 15.764*s, 2.825*s),
            new CubicCurveTo(15.371*s, 3.218*s, 15.371*s, 3.856*s, 15.764*s, 4.249*s),
            new LineTo(3.842*s, 16.174*s),
            new CubicCurveTo(3.658*s, 16.358*s, 3.525*s, 16.585*s, 3.455*s, 16.834*s),
            new LineTo(2.134*s, 21.186*s),
            new CubicCurveTo(2.065*s, 21.436*s, 2.146*s, 21.702*s, 2.341*s, 21.898*s),
            new CubicCurveTo(2.537*s, 22.093*s, 2.803*s, 22.174*s, 3.053*s, 22.105*s),
            new LineTo(7.406*s, 20.785*s),
            new CubicCurveTo(7.654*s, 20.715*s, 7.882*s, 20.582*s, 8.066*s, 20.398*s),
            new ClosePath()
        );
        lapiz.setFill(Color.TRANSPARENT);
        lapiz.setStroke(Color.web(color));
        lapiz.setStrokeWidth(2*s);
        lapiz.setStrokeLineCap(StrokeLineCap.ROUND);
        lapiz.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Line punta = new Line(15*s, 5*s, 19*s, 9*s);
        punta.setStroke(Color.web(color));
        punta.setStrokeWidth(2*s);
        punta.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(lapiz, punta);
        return g;
    }
        
    /** Ícono de eliminar - Papelera (Lucide 'Trash-2') */
    public static Node eliminar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Cuerpo de la papelera
        Path cuerpo = new Path();
        cuerpo.getElements().addAll(
            new MoveTo(5*s, 7*s),
            new LineTo(6*s, 21*s),
            new LineTo(18*s, 21*s),
            new LineTo(19*s, 7*s)
        );
        cuerpo.setFill(Color.TRANSPARENT);
        cuerpo.setStroke(Color.web(color));
        cuerpo.setStrokeWidth(2*s);
        cuerpo.setStrokeLineCap(StrokeLineCap.ROUND);
        cuerpo.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Línea superior
        Line tapa = new Line(3*s, 7*s, 21*s, 7*s);
        tapa.setStroke(Color.web(color));
        tapa.setStrokeWidth(2*s);
        tapa.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Asa superior
        Path asa = new Path();
        asa.getElements().addAll(
            new MoveTo(8*s, 7*s),
            new LineTo(8*s, 4*s),
            new LineTo(16*s, 4*s),
            new LineTo(16*s, 7*s)
        );
        asa.setFill(Color.TRANSPARENT);
        asa.setStroke(Color.web(color));
        asa.setStrokeWidth(2*s);
        asa.setStrokeLineCap(StrokeLineCap.ROUND);
        asa.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Líneas internas
        Line l1 = new Line(10*s, 11*s, 10*s, 17*s);
        l1.setStroke(Color.web(color));
        l1.setStrokeWidth(2*s);
        l1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line l2 = new Line(14*s, 11*s, 14*s, 17*s);
        l2.setStroke(Color.web(color));
        l2.setStrokeWidth(2*s);
        l2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(cuerpo, tapa, asa, l1, l2);
        return g;
    }

    /** Ícono de guardar (Lucide 'save') */
    public static Node guardar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial: <path d="M15.2 3a2 2 0 0 1 1.4.6l3.8 3.8a2 2 0 0 1 .6 1.4V19a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z"/><path d="M17 21v-7a1 1 0 0 0-1-1H8a1 1 0 0 0-1 1v7"/><path d="M7 3v4a1 1 0 0 0 1 1h7"/>
        Path diskette = new Path();
        diskette.getElements().addAll(
            new MoveTo(15.2*s, 3*s),
            new CubicCurveTo(15.73*s, 3*s, 16.2*s, 3.21*s, 16.6*s, 3.6*s),
            new LineTo(20.4*s, 7.4*s),
            new CubicCurveTo(20.79*s, 7.8*s, 21*s, 8.27*s, 21*s, 8.8*s),
            new LineTo(21*s, 19*s),
            new CubicCurveTo(21*s, 20.1*s, 20.1*s, 21*s, 19*s, 21*s),
            new LineTo(5*s, 21*s),
            new CubicCurveTo(3.9*s, 21*s, 3*s, 20.1*s, 3*s, 19*s),
            new LineTo(3*s, 5*s),
            new CubicCurveTo(3*s, 3.9*s, 3.9*s, 3*s, 5*s, 3*s),
            new ClosePath()
        );
        diskette.setFill(Color.TRANSPARENT);
        diskette.setStroke(Color.web(color));
        diskette.setStrokeWidth(2*s);
        diskette.setStrokeLineCap(StrokeLineCap.ROUND);
        diskette.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Path interior = new Path();
        interior.getElements().addAll(
            new MoveTo(17*s, 21*s),
            new LineTo(17*s, 14*s),
            new CubicCurveTo(17*s, 13.45*s, 16.55*s, 13*s, 16*s, 13*s),
            new LineTo(8*s, 13*s),
            new CubicCurveTo(7.45*s, 13*s, 7*s, 13.45*s, 7*s, 14*s),
            new LineTo(7*s, 21*s)
        );
        interior.setFill(Color.TRANSPARENT);
        interior.setStroke(Color.web(color));
        interior.setStrokeWidth(2*s);
        interior.setStrokeLineCap(StrokeLineCap.ROUND);
        interior.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Path tapa = new Path();
        tapa.getElements().addAll(
            new MoveTo(7*s, 3*s),
            new LineTo(7*s, 7*s),
            new CubicCurveTo(7*s, 7.55*s, 7.45*s, 8*s, 8*s, 8*s),
            new LineTo(15*s, 8*s)
        );
        tapa.setFill(Color.TRANSPARENT);
        tapa.setStroke(Color.web(color));
        tapa.setStrokeWidth(2*s);
        tapa.setStrokeLineCap(StrokeLineCap.ROUND);
        tapa.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(diskette, interior, tapa);
        return g;
    }

    /** Ícono de salir (Lucide 'log-out') */
    public static Node salir(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial: <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/>
        Path puerta = new Path();
        puerta.getElements().addAll(
            new MoveTo(9*s, 21*s),
            new LineTo(5*s, 21*s),
            new CubicCurveTo(3.9*s, 21*s, 3*s, 20.1*s, 3*s, 19*s),
            new LineTo(3*s, 5*s),
            new CubicCurveTo(3*s, 3.9*s, 3.9*s, 3*s, 5*s, 3*s),
            new LineTo(9*s, 3*s)
        );
        puerta.setFill(Color.TRANSPARENT);
        puerta.setStroke(Color.web(color));
        puerta.setStrokeWidth(2*s);
        puerta.setStrokeLineCap(StrokeLineCap.ROUND);
        puerta.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Path flecha = new Path();
        flecha.getElements().addAll(
            new MoveTo(16*s, 17*s),
            new LineTo(21*s, 12*s),
            new LineTo(16*s, 7*s)
        );
        flecha.setFill(Color.TRANSPARENT);
        flecha.setStroke(Color.web(color));
        flecha.setStrokeWidth(2*s);
        flecha.setStrokeLineCap(StrokeLineCap.ROUND);
        flecha.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Line lineaFlecha = new Line(21*s, 12*s, 9*s, 12*s);
        lineaFlecha.setStroke(Color.web(color));
        lineaFlecha.setStrokeWidth(2*s);
        lineaFlecha.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(puerta, flecha, lineaFlecha);
        return g;
    }

    /** Ícono de cerrar sesión (Lucide 'log-out') */
    public static Node cerrarSesion(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Mismo diseño que 'salir' pero con nombre diferente
        Path puerta = new Path();
        puerta.getElements().addAll(
            new MoveTo(9*s, 21*s),
            new LineTo(5*s, 21*s),
            new CubicCurveTo(3.9*s, 21*s, 3*s, 20.1*s, 3*s, 19*s),
            new LineTo(3*s, 5*s),
            new CubicCurveTo(3*s, 3.9*s, 3.9*s, 3*s, 5*s, 3*s),
            new LineTo(9*s, 3*s)
        );
        puerta.setFill(Color.TRANSPARENT);
        puerta.setStroke(Color.web(color));
        puerta.setStrokeWidth(2*s);
        puerta.setStrokeLineCap(StrokeLineCap.ROUND);
        puerta.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Path flecha = new Path();
        flecha.getElements().addAll(
            new MoveTo(16*s, 17*s),
            new LineTo(21*s, 12*s),
            new LineTo(16*s, 7*s)
        );
        flecha.setFill(Color.TRANSPARENT);
        flecha.setStroke(Color.web(color));
        flecha.setStrokeWidth(2*s);
        flecha.setStrokeLineCap(StrokeLineCap.ROUND);
        flecha.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Line lineaFlecha = new Line(21*s, 12*s, 9*s, 12*s);
        lineaFlecha.setStroke(Color.web(color));
        lineaFlecha.setStrokeWidth(2*s);
        lineaFlecha.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(puerta, flecha, lineaFlecha);
        return g;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ÍCONOS DE INFORMACIÓN Y DATOS (v6.0 - Minimalista Elegante)
    // ═══════════════════════════════════════════════════════════════════════

    /** Ícono de usuario - Persona (Lucide 'User') */
    public static Node usuario(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Círculo de la cabeza
        Circle cabeza = new Circle(12*s, 8*s, 4*s);
        cabeza.setFill(Color.TRANSPARENT);
        cabeza.setStroke(Color.web(color));
        cabeza.setStrokeWidth(2*s);
        
        // Path del cuerpo simplificado
        Path cuerpo = new Path();
        cuerpo.getElements().addAll(
            new MoveTo(20*s, 21*s),
            new LineTo(20*s, 19*s),
            new CubicCurveTo(20*s, 15*s, 17*s, 14*s, 12*s, 14*s),
            new CubicCurveTo(7*s, 14*s, 4*s, 15*s, 4*s, 19*s),
            new LineTo(4*s, 21*s)
        );
        cuerpo.setFill(Color.TRANSPARENT);
        cuerpo.setStroke(Color.web(color));
        cuerpo.setStrokeWidth(2*s);
        cuerpo.setStrokeLineCap(StrokeLineCap.ROUND);
        cuerpo.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(cabeza, cuerpo);
        return g;
    }

    /** Ícono de calendario (Lucide 'Calendar') */
    public static Node calendario(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Rectángulo principal
        Rectangle cuerpo = new Rectangle(3*s, 6*s, 18*s, 15*s);
        cuerpo.setArcWidth(2*s);
        cuerpo.setArcHeight(2*s);
        cuerpo.setFill(Color.TRANSPARENT);
        cuerpo.setStroke(Color.web(color));
        cuerpo.setStrokeWidth(2*s);
        cuerpo.setStrokeLineCap(StrokeLineCap.ROUND);
        cuerpo.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Línea divisoria horizontal
        Line header = new Line(3*s, 10*s, 21*s, 10*s);
        header.setStroke(Color.web(color));
        header.setStrokeWidth(2*s);
        
        // Gancho izquierdo
        Line gancho1 = new Line(8*s, 3*s, 8*s, 6*s);
        gancho1.setStroke(Color.web(color));
        gancho1.setStrokeWidth(2*s);
        gancho1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Gancho derecho
        Line gancho2 = new Line(16*s, 3*s, 16*s, 6*s);
        gancho2.setStroke(Color.web(color));
        gancho2.setStrokeWidth(2*s);
        gancho2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(cuerpo, header, gancho1, gancho2);
        return g;
    }

    /** Ícono de reloj (Lucide 'Clock') */
    public static Node reloj(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Círculo del reloj
        Circle circulo = new Circle(12*s, 12*s, 9*s);
        circulo.setFill(Color.TRANSPARENT);
        circulo.setStroke(Color.web(color));
        circulo.setStrokeWidth(2*s);
        
        // Manecilla de hora (hacia arriba)
        Line hora = new Line(12*s, 12*s, 12*s, 7*s);
        hora.setStroke(Color.web(color));
        hora.setStrokeWidth(2*s);
        hora.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Manecilla de minutos (hacia derecha)
        Line minutos = new Line(12*s, 12*s, 16*s, 12*s);
        minutos.setStroke(Color.web(color));
        minutos.setStrokeWidth(2*s);
        minutos.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(circulo, hora, minutos);
        return g;
    }

    /** Ícono de ubicación - Pin (Lucide 'Map-Pin') */
    public static Node ubicacion(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path del pin simplificado
        Path pin = new Path();
        pin.getElements().addAll(
            new MoveTo(21*s, 10*s),
            new CubicCurveTo(21*s, 4*s, 16.97*s, 1*s, 12*s, 1*s),
            new CubicCurveTo(7.03*s, 1*s, 3*s, 4*s, 3*s, 10*s),
            new CubicCurveTo(3*s, 13*s, 12*s, 23*s, 12*s, 23*s),
            new CubicCurveTo(12*s, 23*s, 21*s, 13*s, 21*s, 10*s)
        );
        pin.setFill(Color.TRANSPARENT);
        pin.setStroke(Color.web(color));
        pin.setStrokeWidth(2*s);
        pin.setStrokeLineCap(StrokeLineCap.ROUND);
        pin.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Círculo interior
        Circle interior = new Circle(12*s, 10*s, 3*s);
        interior.setFill(Color.TRANSPARENT);
        interior.setStroke(Color.web(color));
        interior.setStrokeWidth(2*s);
        
        g.getChildren().addAll(pin, interior);
        return g;
    }

    /** Ícono de herramienta (Lucide 'wrench') */
    public static Node herramienta(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial Lucide wrench: <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
        Path llave = new Path();
        llave.getElements().addAll(
            // Inicio en 14.7, 6.3
            new MoveTo(14.7*s, 6.3*s),
            // Arco: a1 1 0 0 0 0 1.4 (cuadrado pequeño superior)
            new ArcTo(1*s, 1*s, 0, 14.7*s, 7.7*s, false, false),
            // Línea l1.6 1.6
            new LineTo(16.3*s, 9.3*s),
            // Arco: a1 1 0 0 0 1.4 0
            new ArcTo(1*s, 1*s, 0, 17.7*s, 9.3*s, false, false),
            // Línea l3.77-3.77 (diagonal)
            new LineTo(21.47*s, 5.53*s),
            // Arco grande: a6 6 0 0 1-7.94 7.94 (curva principal)
            new ArcTo(6*s, 6*s, 0, 13.53*s, 13.47*s, false, true),
            // Línea l-6.91 6.91 (diagonal hacia abajo)
            new LineTo(6.62*s, 20.38*s),
            // Arco: a2.12 2.12 0 0 1-3-3
            new ArcTo(2.12*s, 2.12*s, 0, 3.62*s, 17.38*s, false, true),
            // Línea l6.91-6.91 (diagonal hacia arriba)
            new LineTo(10.53*s, 10.47*s),
            // Arco grande: a6 6 0 0 1 7.94-7.94
            new ArcTo(6*s, 6*s, 0, 18.47*s, 2.53*s, false, true),
            // Línea l-3.76 3.76
            new LineTo(14.71*s, 6.29*s),
            new ClosePath()
        );
        llave.setFill(Color.TRANSPARENT);
        llave.setStroke(Color.web(color));
        llave.setStrokeWidth(2*s);
        llave.setStrokeLineCap(StrokeLineCap.ROUND);
        llave.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().add(llave);
        return g;
    }

    /** Ícono de fábrica (Lucide 'Factory') */
    public static Node fabrica(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path del edificio
        Path edificio = new Path();
        edificio.getElements().addAll(
            new MoveTo(2*s, 20*s),
            new LineTo(2*s, 11*s),
            new LineTo(8*s, 15*s),
            new LineTo(8*s, 11*s),
            new LineTo(14*s, 15*s),
            new LineTo(14*s, 11*s),
            new LineTo(22*s, 15*s),
            new LineTo(22*s, 20*s)
        );
        edificio.setFill(Color.TRANSPARENT);
        edificio.setStroke(Color.web(color));
        edificio.setStrokeWidth(2*s);
        edificio.setStrokeLineCap(StrokeLineCap.ROUND);
        edificio.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Chimenea
        Path chimenea = new Path();
        chimenea.getElements().addAll(
            new MoveTo(4*s, 4*s),
            new LineTo(4*s, 8*s),
            new LineTo(7*s, 8*s),
            new LineTo(7*s, 4*s)
        );
        chimenea.setFill(Color.TRANSPARENT);
        chimenea.setStroke(Color.web(color));
        chimenea.setStrokeWidth(2*s);
        chimenea.setStrokeLineCap(StrokeLineCap.ROUND);
        chimenea.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(edificio, chimenea);
        return g;
    }

    /** Ícono de ciudad (Lucide 'building-2') */
    public static Node ciudad(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial: <path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z"/><path d="M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2"/><path d="M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2"/><path d="M10 6h4"/><path d="M10 10h4"/><path d="M10 14h4"/><path d="M10 18h4"/>
        
        // Edificio principal (centro)
        Path principal = new Path();
        principal.getElements().addAll(
            new MoveTo(6*s, 22*s),
            new LineTo(6*s, 4*s),
            new CubicCurveTo(6*s, 2.9*s, 6.9*s, 2*s, 8*s, 2*s),
            new LineTo(16*s, 2*s),
            new CubicCurveTo(17.1*s, 2*s, 18*s, 2.9*s, 18*s, 4*s),
            new LineTo(18*s, 22*s),
            new ClosePath()
        );
        principal.setFill(Color.TRANSPARENT);
        principal.setStroke(Color.web(color));
        principal.setStrokeWidth(2*s);
        principal.setStrokeLineCap(StrokeLineCap.ROUND);
        principal.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Edificio izquierdo
        Path izquierdo = new Path();
        izquierdo.getElements().addAll(
            new MoveTo(6*s, 12*s),
            new LineTo(4*s, 12*s),
            new CubicCurveTo(2.9*s, 12*s, 2*s, 12.9*s, 2*s, 14*s),
            new LineTo(2*s, 20*s),
            new CubicCurveTo(2*s, 21.1*s, 2.9*s, 22*s, 4*s, 22*s),
            new LineTo(6*s, 22*s)
        );
        izquierdo.setFill(Color.TRANSPARENT);
        izquierdo.setStroke(Color.web(color));
        izquierdo.setStrokeWidth(2*s);
        izquierdo.setStrokeLineCap(StrokeLineCap.ROUND);
        izquierdo.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Edificio derecho
        Path derecho = new Path();
        derecho.getElements().addAll(
            new MoveTo(18*s, 9*s),
            new LineTo(20*s, 9*s),
            new CubicCurveTo(21.1*s, 9*s, 22*s, 9.9*s, 22*s, 11*s),
            new LineTo(22*s, 20*s),
            new CubicCurveTo(22*s, 21.1*s, 21.1*s, 22*s, 20*s, 22*s),
            new LineTo(18*s, 22*s)
        );
        derecho.setFill(Color.TRANSPARENT);
        derecho.setStroke(Color.web(color));
        derecho.setStrokeWidth(2*s);
        derecho.setStrokeLineCap(StrokeLineCap.ROUND);
        derecho.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Ventanas (4 líneas horizontales)
        Line v1 = new Line(10*s, 6*s, 14*s, 6*s);
        v1.setStroke(Color.web(color));
        v1.setStrokeWidth(2*s);
        v1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line v2 = new Line(10*s, 10*s, 14*s, 10*s);
        v2.setStroke(Color.web(color));
        v2.setStrokeWidth(2*s);
        v2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line v3 = new Line(10*s, 14*s, 14*s, 14*s);
        v3.setStroke(Color.web(color));
        v3.setStrokeWidth(2*s);
        v3.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line v4 = new Line(10*s, 18*s, 14*s, 18*s);
        v4.setStroke(Color.web(color));
        v4.setStrokeWidth(2*s);
        v4.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(principal, izquierdo, derecho, v1, v2, v3, v4);
        return g;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ÍCONOS DE ARCHIVOS Y DOCUMENTOS (v6.0 - Minimalista Elegante)
    // ═══════════════════════════════════════════════════════════════════════

    /** Ícono de documento genérico (Lucide 'File') */
    public static Node documento(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path del documento simplificado
        Path doc = new Path();
        doc.getElements().addAll(
            new MoveTo(14*s, 2*s),
            new LineTo(6*s, 2*s),
            new CubicCurveTo(4.9*s, 2*s, 4*s, 2.9*s, 4*s, 4*s),
            new LineTo(4*s, 20*s),
            new CubicCurveTo(4*s, 21.1*s, 4.9*s, 22*s, 6*s, 22*s),
            new LineTo(18*s, 22*s),
            new CubicCurveTo(19.1*s, 22*s, 20*s, 21.1*s, 20*s, 20*s),
            new LineTo(20*s, 8*s),
            new ClosePath(),
            new MoveTo(14*s, 2*s),
            new LineTo(14*s, 8*s),
            new LineTo(20*s, 8*s)
        );
        doc.setFill(Color.TRANSPARENT);
        doc.setStroke(Color.web(color));
        doc.setStrokeWidth(2*s);
        doc.setStrokeLineCap(StrokeLineCap.ROUND);
        doc.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().add(doc);
        return g;
    }

    /** Ícono de PDF blanco - Misma forma que pdf() pero en blanco, ideal para botones con color */
    public static Node pdfBlanco(double size) {
        Group g = new Group();
        double s = size / 16.0;
        double f = 16.0 / 48.0;
        
        // Logo Acrobat blanco (mismas curvas Bézier exactas que pdf())
        Path logo = new Path();
        logo.getElements().addAll(
            new MoveTo(34.841*f*s, 26.799*f*s),
            new CubicCurveTo(33.149*f*s, 25.042*f*s, 28.527*f*s, 25.758*f*s, 27.421*f*s, 25.888*f*s),
            new CubicCurveTo(25.794*f*s, 24.326*f*s, 24.687*f*s, 22.438*f*s, 24.297*f*s, 21.787*f*s),
            new CubicCurveTo(24.883*f*s, 20.030*f*s, 25.273*f*s, 18.272*f*s, 25.338*f*s, 16.385*f*s),
            new CubicCurveTo(25.338*f*s, 14.758*f*s, 24.687*f*s, 13.0*f*s, 22.865*f*s, 13.0*f*s),
            new CubicCurveTo(22.214*f*s, 13.0*f*s, 21.628*f*s, 13.391*f*s, 21.303*f*s, 13.911*f*s),
            new CubicCurveTo(20.522*f*s, 15.278*f*s, 20.847*f*s, 18.012*f*s, 22.084*f*s, 20.810*f*s),
            new CubicCurveTo(21.368*f*s, 22.828*f*s, 20.717*f*s, 24.780*f*s, 18.895*f*s, 28.230*f*s),
            new CubicCurveTo(17.007*f*s, 29.011*f*s, 13.037*f*s, 30.834*f*s, 12.712*f*s, 32.786*f*s),
            new CubicCurveTo(12.582*f*s, 33.372*f*s, 12.777*f*s, 33.958*f*s, 13.233*f*s, 34.413*f*s),
            new CubicCurveTo(13.688*f*s, 34.805*f*s, 14.273*f*s, 35.0*f*s, 14.859*f*s, 35.0*f*s),
            new CubicCurveTo(17.267*f*s, 35.0*f*s, 19.610*f*s, 31.680*f*s, 21.238*f*s, 28.882*f*s),
            new CubicCurveTo(22.605*f*s, 28.426*f*s, 24.753*f*s, 27.775*f*s, 26.901*f*s, 27.385*f*s),
            new CubicCurveTo(29.439*f*s, 29.598*f*s, 31.652*f*s, 29.923*f*s, 32.824*f*s, 29.923*f*s),
            new CubicCurveTo(34.386*f*s, 29.923*f*s, 34.972*f*s, 29.272*f*s, 35.167*f*s, 28.686*f*s),
            new CubicCurveTo(35.492*f*s, 28.036*f*s, 35.297*f*s, 27.320*f*s, 34.841*f*s, 26.799*f*s),
            new ClosePath(),
            new MoveTo(33.214*f*s, 27.905*f*s),
            new CubicCurveTo(33.149*f*s, 28.361*f*s, 32.563*f*s, 28.816*f*s, 31.522*f*s, 28.556*f*s),
            new CubicCurveTo(30.285*f*s, 28.231*f*s, 29.179*f*s, 27.645*f*s, 28.202*f*s, 26.864*f*s),
            new CubicCurveTo(29.048*f*s, 26.734*f*s, 30.936*f*s, 26.539*f*s, 32.303*f*s, 26.799*f*s),
            new CubicCurveTo(32.824*f*s, 26.929*f*s, 33.344*f*s, 27.254*f*s, 33.214*f*s, 27.905*f*s),
            new ClosePath(),
            new MoveTo(22.344*f*s, 14.497*f*s),
            new CubicCurveTo(22.474*f*s, 14.302*f*s, 22.669*f*s, 14.172*f*s, 22.865*f*s, 14.172*f*s),
            new CubicCurveTo(23.451*f*s, 14.172*f*s, 23.581*f*s, 14.888*f*s, 23.581*f*s, 15.474*f*s),
            new CubicCurveTo(23.516*f*s, 16.841*f*s, 23.256*f*s, 18.208*f*s, 22.800*f*s, 19.510*f*s),
            new CubicCurveTo(21.824*f*s, 16.905*f*s, 22.019*f*s, 15.083*f*s, 22.344*f*s, 14.497*f*s),
            new ClosePath(),
            new MoveTo(22.214*f*s, 27.124*f*s),
            new CubicCurveTo(22.735*f*s, 26.083*f*s, 23.451*f*s, 24.260*f*s, 23.711*f*s, 23.479*f*s),
            new CubicCurveTo(24.297*f*s, 24.455*f*s, 25.273*f*s, 25.627*f*s, 25.794*f*s, 26.148*f*s),
            new CubicCurveTo(25.794*f*s, 26.213*f*s, 23.776*f*s, 26.604*f*s, 22.214*f*s, 27.124*f*s),
            new ClosePath(),
            new MoveTo(18.374*f*s, 29.728*f*s),
            new CubicCurveTo(16.877*f*s, 32.201*f*s, 15.315*f*s, 33.764*f*s, 14.469*f*s, 33.764*f*s),
            new CubicCurveTo(14.339*f*s, 33.764*f*s, 14.209*f*s, 33.699*f*s, 14.078*f*s, 33.634*f*s),
            new CubicCurveTo(13.883*f*s, 33.504*f*s, 13.818*f*s, 33.309*f*s, 13.883*f*s, 33.048*f*s),
            new CubicCurveTo(14.078*f*s, 32.136*f*s, 15.770*f*s, 30.899*f*s, 18.374*f*s, 29.728*f*s),
            new ClosePath()
        );
        logo.setFill(Color.WHITE);
        logo.setStroke(Color.rgb(255, 255, 255, 0.4));
        logo.setStrokeWidth(0.25 * s);
        logo.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Escalar el logo para que ocupe más espacio y se vea más nítido/HD
        logo.setScaleX(1.65);
        logo.setScaleY(1.65);
        
        g.getChildren().add(logo);
        return g;
    }

    /** Ícono de Excel blanco - Misma forma que excel() pero todo en blanco, ideal para botones con color */
    public static Node excelBlanco(double size) {
        Group g = new Group();
        double s = size / 16.0;
        
        // Panel derecho blanco (outline) con celdas - igual que excel()
        Rectangle panelDer = new Rectangle(9*s, 2*s, 6*s, 12*s);
        panelDer.setFill(Color.rgb(255,255,255, 0.15));
        panelDer.setStroke(Color.WHITE);
        panelDer.setStrokeWidth(0.7*s);
        
        // Filas de celdas blancas (mismas posiciones que excel())
        for (int i = 0; i < 4; i++) {
            Rectangle fila = new Rectangle(10.5*s, (3.5 + i*2.5)*s, 3.5*s, 1.3*s);
            fila.setFill(Color.rgb(255,255,255, 0.85));
            fila.setArcWidth(0.5*s);
            fila.setArcHeight(0.5*s);
            g.getChildren().add(fila);
        }
        
        // Panel izquierdo trapezoidal blanco (misma forma polyline que excel())
        Polyline panelIzq = new Polyline(
            1*s, 2.5*s,
            9*s, 1*s,
            9*s, 15*s,
            1*s, 13.5*s,
            1*s, 2.5*s
        );
        panelIzq.setFill(Color.rgb(255,255,255, 0.95));
        panelIzq.setStroke(Color.WHITE);
        panelIzq.setStrokeWidth(0.3*s);
        
        // "X" sobre el panel izquierdo - mismo color del fondo del botón para contraste
        Line x1 = new Line(3*s, 5.5*s, 7*s, 10.5*s);
        x1.setStroke(Color.web("#00C853"));
        x1.setStrokeWidth(1.5*s);
        x1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line x2 = new Line(7*s, 5.5*s, 3*s, 10.5*s);
        x2.setStroke(Color.web("#00C853"));
        x2.setStrokeWidth(1.5*s);
        x2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(panelDer, panelIzq, x1, x2);
        return g;
    }

    /** Ícono de PDF - Estilo Adobe Acrobat (réplica exacta del SVG) */
    public static Node pdf(String color, double size) {
        Group g = new Group();
        double s = size / 16.0;  // Misma escala que Excel
        
        // Fondo rojo redondeado que ocupa todo el espacio
        Rectangle fondo = new Rectangle(0, 0, 16*s, 16*s);
        fondo.setArcWidth(2.5*s);
        fondo.setArcHeight(2.5*s);
        fondo.setFill(Color.web("#e53935"));
        
        // Logo Acrobat - Escalado para ocupar bien el espacio (original 48x48 -> 16x16)
        double f = 16.0 / 48.0;  // Factor de escala
        Path logo = new Path();
        logo.getElements().addAll(
            new MoveTo(34.841*f*s, 26.799*f*s),
            new CubicCurveTo(33.149*f*s, 25.042*f*s, 28.527*f*s, 25.758*f*s, 27.421*f*s, 25.888*f*s),
            new CubicCurveTo(25.794*f*s, 24.326*f*s, 24.687*f*s, 22.438*f*s, 24.297*f*s, 21.787*f*s),
            new CubicCurveTo(24.883*f*s, 20.030*f*s, 25.273*f*s, 18.272*f*s, 25.338*f*s, 16.385*f*s),
            new CubicCurveTo(25.338*f*s, 14.758*f*s, 24.687*f*s, 13.0*f*s, 22.865*f*s, 13.0*f*s),
            new CubicCurveTo(22.214*f*s, 13.0*f*s, 21.628*f*s, 13.391*f*s, 21.303*f*s, 13.911*f*s),
            new CubicCurveTo(20.522*f*s, 15.278*f*s, 20.847*f*s, 18.012*f*s, 22.084*f*s, 20.810*f*s),
            new CubicCurveTo(21.368*f*s, 22.828*f*s, 20.717*f*s, 24.780*f*s, 18.895*f*s, 28.230*f*s),
            new CubicCurveTo(17.007*f*s, 29.011*f*s, 13.037*f*s, 30.834*f*s, 12.712*f*s, 32.786*f*s),
            new CubicCurveTo(12.582*f*s, 33.372*f*s, 12.777*f*s, 33.958*f*s, 13.233*f*s, 34.413*f*s),
            new CubicCurveTo(13.688*f*s, 34.805*f*s, 14.273*f*s, 35.0*f*s, 14.859*f*s, 35.0*f*s),
            new CubicCurveTo(17.267*f*s, 35.0*f*s, 19.610*f*s, 31.680*f*s, 21.238*f*s, 28.882*f*s),
            new CubicCurveTo(22.605*f*s, 28.426*f*s, 24.753*f*s, 27.775*f*s, 26.901*f*s, 27.385*f*s),
            new CubicCurveTo(29.439*f*s, 29.598*f*s, 31.652*f*s, 29.923*f*s, 32.824*f*s, 29.923*f*s),
            new CubicCurveTo(34.386*f*s, 29.923*f*s, 34.972*f*s, 29.272*f*s, 35.167*f*s, 28.686*f*s),
            new CubicCurveTo(35.492*f*s, 28.036*f*s, 35.297*f*s, 27.320*f*s, 34.841*f*s, 26.799*f*s),
            new ClosePath(),
            
            new MoveTo(33.214*f*s, 27.905*f*s),
            new CubicCurveTo(33.149*f*s, 28.361*f*s, 32.563*f*s, 28.816*f*s, 31.522*f*s, 28.556*f*s),
            new CubicCurveTo(30.285*f*s, 28.231*f*s, 29.179*f*s, 27.645*f*s, 28.202*f*s, 26.864*f*s),
            new CubicCurveTo(29.048*f*s, 26.734*f*s, 30.936*f*s, 26.539*f*s, 32.303*f*s, 26.799*f*s),
            new CubicCurveTo(32.824*f*s, 26.929*f*s, 33.344*f*s, 27.254*f*s, 33.214*f*s, 27.905*f*s),
            new ClosePath(),
            
            new MoveTo(22.344*f*s, 14.497*f*s),
            new CubicCurveTo(22.474*f*s, 14.302*f*s, 22.669*f*s, 14.172*f*s, 22.865*f*s, 14.172*f*s),
            new CubicCurveTo(23.451*f*s, 14.172*f*s, 23.581*f*s, 14.888*f*s, 23.581*f*s, 15.474*f*s),
            new CubicCurveTo(23.516*f*s, 16.841*f*s, 23.256*f*s, 18.208*f*s, 22.800*f*s, 19.510*f*s),
            new CubicCurveTo(21.824*f*s, 16.905*f*s, 22.019*f*s, 15.083*f*s, 22.344*f*s, 14.497*f*s),
            new ClosePath(),
            
            new MoveTo(22.214*f*s, 27.124*f*s),
            new CubicCurveTo(22.735*f*s, 26.083*f*s, 23.451*f*s, 24.260*f*s, 23.711*f*s, 23.479*f*s),
            new CubicCurveTo(24.297*f*s, 24.455*f*s, 25.273*f*s, 25.627*f*s, 25.794*f*s, 26.148*f*s),
            new CubicCurveTo(25.794*f*s, 26.213*f*s, 23.776*f*s, 26.604*f*s, 22.214*f*s, 27.124*f*s),
            new ClosePath(),
            
            new MoveTo(18.374*f*s, 29.728*f*s),
            new CubicCurveTo(16.877*f*s, 32.201*f*s, 15.315*f*s, 33.764*f*s, 14.469*f*s, 33.764*f*s),
            new CubicCurveTo(14.339*f*s, 33.764*f*s, 14.209*f*s, 33.699*f*s, 14.078*f*s, 33.634*f*s),
            new CubicCurveTo(13.883*f*s, 33.504*f*s, 13.818*f*s, 33.309*f*s, 13.883*f*s, 33.048*f*s),
            new CubicCurveTo(14.078*f*s, 32.136*f*s, 15.770*f*s, 30.899*f*s, 18.374*f*s, 29.728*f*s),
            new ClosePath()
        );
        logo.setFill(Color.WHITE);
        logo.setStroke(null);
        
        g.getChildren().addAll(fondo, logo);
        return g;
    }

    /** Ícono de Excel - Panel verde con X y celdas (como el SVG original) */
    public static Node excel(String color, double size) {
        Group g = new Group();
        double s = size / 16.0;
        
        // Panel derecho blanco/claro con celdas
        Rectangle panelDer = new Rectangle(9*s, 2*s, 6*s, 12*s);
        panelDer.setFill(Color.WHITE);
        panelDer.setStroke(Color.web(color));
        panelDer.setStrokeWidth(0.5*s);
        
        // Filas de celdas
        for (int i = 0; i < 5; i++) {
            Rectangle fila = new Rectangle(11*s, (3.5 + i*2.2)*s, 3.5*s, 1.5*s);
            fila.setFill(Color.web(color));
            g.getChildren().add(fila);
        }
        
        // Panel izquierdo (área principal del Excel)
        Polyline panelIzq = new Polyline(
            1*s, 2.5*s,
            9*s, 1*s,
            9*s, 15*s,
            1*s, 13.5*s,
            1*s, 2.5*s
        );
        panelIzq.setFill(Color.web(color));
        panelIzq.setStroke(Color.web(color));
        
        // "X" blanca de Excel
        Line x1 = new Line(3*s, 5.5*s, 7*s, 10.5*s);
        x1.setStroke(Color.WHITE);
        x1.setStrokeWidth(1.5*s);
        x1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line x2 = new Line(7*s, 5.5*s, 3*s, 10.5*s);
        x2.setStroke(Color.WHITE);
        x2.setStrokeWidth(1.5*s);
        x2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(panelDer, panelIzq, x1, x2);
        return g;
    }

    /** Ícono de Excel con colores originales de Microsoft (verde #185C37 / #33C481 / blanco) */
    public static Node excelOriginal(double size) {
        Group g = new Group();
        double s = size / 16.0;

        // Colores oficiales de Microsoft Excel
        Color verdeOscuro = Color.web("#185C37");
        Color verdeMedio = Color.web("#21A366");
        Color verdeClaro = Color.web("#33C481");

        // Panel derecho blanco con borde verde claro y celdas verdes
        Rectangle panelDer = new Rectangle(9*s, 2*s, 6*s, 12*s);
        panelDer.setFill(Color.WHITE);
        panelDer.setStroke(verdeMedio);
        panelDer.setStrokeWidth(0.5*s);

        // Filas de celdas con tonos de verde alternados
        Color[] coloresFila = {verdeClaro, verdeMedio, verdeClaro, verdeMedio, verdeClaro};
        for (int i = 0; i < 5; i++) {
            Rectangle fila = new Rectangle(10.2*s, (3.2 + i*2.2)*s, 4*s, 1.4*s);
            fila.setFill(coloresFila[i]);
            fila.setArcWidth(0.3*s);
            fila.setArcHeight(0.3*s);
            g.getChildren().add(fila);
        }

        // Panel izquierdo trapezoidal verde oscuro (área principal del logo)
        Polyline panelIzq = new Polyline(
            1*s, 2.5*s,
            9*s, 1*s,
            9*s, 15*s,
            1*s, 13.5*s,
            1*s, 2.5*s
        );
        panelIzq.setFill(verdeOscuro);
        panelIzq.setStroke(verdeOscuro);
        panelIzq.setStrokeWidth(0.3*s);

        // "X" blanca sobre el panel verde oscuro
        Line x1 = new Line(3*s, 5.5*s, 7*s, 10.5*s);
        x1.setStroke(Color.WHITE);
        x1.setStrokeWidth(1.6*s);
        x1.setStrokeLineCap(StrokeLineCap.ROUND);

        Line x2 = new Line(7*s, 5.5*s, 3*s, 10.5*s);
        x2.setStroke(Color.WHITE);
        x2.setStrokeWidth(1.6*s);
        x2.setStrokeLineCap(StrokeLineCap.ROUND);

        g.getChildren().addAll(panelDer, panelIzq, x1, x2);
        return g;
    }

    /** Ícono de CSV - Documento con líneas (Lucide 'Sheet') */
    public static Node csv(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Rectángulo exterior
        Rectangle rect = new Rectangle(3*s, 3*s, 18*s, 18*s);
        rect.setArcWidth(2*s);
        rect.setArcHeight(2*s);
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(Color.web(color));
        rect.setStrokeWidth(2*s);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Línea vertical
        Line vertical = new Line(12*s, 3*s, 12*s, 21*s);
        vertical.setStroke(Color.web(color));
        vertical.setStrokeWidth(2*s);
        
        // Línea horizontal superior
        Line h1 = new Line(3*s, 9*s, 21*s, 9*s);
        h1.setStroke(Color.web(color));
        h1.setStrokeWidth(2*s);
        
        // Línea horizontal inferior
        Line h2 = new Line(3*s, 15*s, 21*s, 15*s);
        h2.setStroke(Color.web(color));
        h2.setStrokeWidth(2*s);
        
        g.getChildren().addAll(rect, vertical, h1, h2);
        return g;
    }

    /** Ícono de carpeta con signo más (Lucide 'folder-plus') */
    public static Node carpetaMas(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Carpeta base
        Path folder = new Path();
        folder.getElements().addAll(
            new MoveTo(4*s, 20*s),
            new CubicCurveTo(2.9*s, 20*s, 2*s, 19.1*s, 2*s, 18*s),
            new LineTo(2*s, 6*s),
            new CubicCurveTo(2*s, 4.9*s, 2.9*s, 4*s, 4*s, 4*s),
            new LineTo(9*s, 4*s),
            new LineTo(11*s, 7*s),
            new LineTo(20*s, 7*s),
            new CubicCurveTo(21.1*s, 7*s, 22*s, 7.9*s, 22*s, 9*s),
            new LineTo(22*s, 18*s),
            new CubicCurveTo(22*s, 19.1*s, 21.1*s, 20*s, 20*s, 20*s),
            new ClosePath()
        );
        folder.setFill(Color.TRANSPARENT);
        folder.setStroke(Color.web(color));
        folder.setStrokeWidth(2*s);
        folder.setStrokeLineCap(StrokeLineCap.ROUND);
        folder.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Signo más en el centro
        Line vertical = new Line(12*s, 11*s, 12*s, 16*s);
        vertical.setStroke(Color.web(color));
        vertical.setStrokeWidth(2*s);
        vertical.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line horizontal = new Line(9.5*s, 13.5*s, 14.5*s, 13.5*s);
        horizontal.setStroke(Color.web(color));
        horizontal.setStrokeWidth(2*s);
        horizontal.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(folder, vertical, horizontal);
        return g;
    }

    /** Ícono de carpeta (Lucide 'Folder') */
    public static Node carpeta(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path de la carpeta simplificado
        Path folder = new Path();
        folder.getElements().addAll(
            new MoveTo(4*s, 20*s),
            new CubicCurveTo(2.9*s, 20*s, 2*s, 19.1*s, 2*s, 18*s),
            new LineTo(2*s, 6*s),
            new CubicCurveTo(2*s, 4.9*s, 2.9*s, 4*s, 4*s, 4*s),
            new LineTo(9*s, 4*s),
            new LineTo(11*s, 7*s),
            new LineTo(20*s, 7*s),
            new CubicCurveTo(21.1*s, 7*s, 22*s, 7.9*s, 22*s, 9*s),
            new LineTo(22*s, 18*s),
            new CubicCurveTo(22*s, 19.1*s, 21.1*s, 20*s, 20*s, 20*s),
            new ClosePath()
        );
        folder.setFill(Color.TRANSPARENT);
        folder.setStroke(Color.web(color));
        folder.setStrokeWidth(2*s);
        folder.setStrokeLineCap(StrokeLineCap.ROUND);
        folder.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().add(folder);
        return g;
    }

    /** Ícono de lista (Lucide 'List') */
    public static Node lista(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Línea 1
        Line l1 = new Line(8*s, 6*s, 21*s, 6*s);
        l1.setStroke(Color.web(color));
        l1.setStrokeWidth(2*s);
        l1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Línea 2
        Line l2 = new Line(8*s, 12*s, 21*s, 12*s);
        l2.setStroke(Color.web(color));
        l2.setStrokeWidth(2*s);
        l2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Línea 3
        Line l3 = new Line(8*s, 18*s, 21*s, 18*s);
        l3.setStroke(Color.web(color));
        l3.setStrokeWidth(2*s);
        l3.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Viñeta 1
        Line v1 = new Line(3*s, 6*s, 3.01*s, 6*s);
        v1.setStroke(Color.web(color));
        v1.setStrokeWidth(2*s);
        v1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Viñeta 2
        Line v2 = new Line(3*s, 12*s, 3.01*s, 12*s);
        v2.setStroke(Color.web(color));
        v2.setStrokeWidth(2*s);
        v2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Viñeta 3
        Line v3 = new Line(3*s, 18*s, 3.01*s, 18*s);
        v3.setStroke(Color.web(color));
        v3.setStrokeWidth(2*s);
        v3.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(l1, l2, l3, v1, v2, v3);
        return g;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ÍCONOS DE ALERTAS Y ESTADOS (v6.0 - Minimalista Elegante)
    // ═══════════════════════════════════════════════════════════════════════

    /** Ícono de éxito - Check (Lucide 'Check-Circle') */
    public static Node check(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Círculo
        Circle circulo = new Circle(12*s, 12*s, 10*s);
        circulo.setFill(Color.TRANSPARENT);
        circulo.setStroke(Color.web(color));
        circulo.setStrokeWidth(2*s);
        
        // Check mark
        Path checkMark = new Path();
        checkMark.getElements().addAll(
            new MoveTo(9*s, 12*s),
            new LineTo(11*s, 14*s),
            new LineTo(15*s, 10*s)
        );
        checkMark.setFill(Color.TRANSPARENT);
        checkMark.setStroke(Color.web(color));
        checkMark.setStrokeWidth(2*s);
        checkMark.setStrokeLineCap(StrokeLineCap.ROUND);
        checkMark.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(circulo, checkMark);
        return g;
    }

    /** Ícono de selección múltiple (Lucide 'Check-Check') */
    public static Node checkMultiple(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Primer check
        Path check1 = new Path();
        check1.getElements().addAll(
            new MoveTo(18*s, 6*s),
            new LineTo(11.5*s, 12.5*s),
            new LineTo(9*s, 10*s)
        );
        check1.setFill(Color.TRANSPARENT);
        check1.setStroke(Color.web(color));
        check1.setStrokeWidth(2*s);
        check1.setStrokeLineCap(StrokeLineCap.ROUND);
        check1.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Segundo check
        Path check2 = new Path();
        check2.getElements().addAll(
            new MoveTo(9*s, 12*s),
            new LineTo(5.5*s, 15.5*s),
            new LineTo(3*s, 13*s)
        );
        check2.setFill(Color.TRANSPARENT);
        check2.setStroke(Color.web(color));
        check2.setStrokeWidth(2*s);
        check2.setStrokeLineCap(StrokeLineCap.ROUND);
        check2.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(check1, check2);
        return g;
    }

    /** Ícono de error (Lucide 'X-Circle') */
    public static Node error(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Círculo
        Circle circulo = new Circle(12*s, 12*s, 10*s);
        circulo.setFill(Color.TRANSPARENT);
        circulo.setStroke(Color.web(color));
        circulo.setStrokeWidth(2*s);
        
        // X
        Line l1 = new Line(15*s, 9*s, 9*s, 15*s);
        l1.setStroke(Color.web(color));
        l1.setStrokeWidth(2*s);
        l1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line l2 = new Line(9*s, 9*s, 15*s, 15*s);
        l2.setStroke(Color.web(color));
        l2.setStrokeWidth(2*s);
        l2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(circulo, l1, l2);
        return g;
    }

    /** Ícono de advertencia (Lucide 'Alert-Triangle') */
    public static Node advertencia(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Triángulo
        Path triangulo = new Path();
        triangulo.getElements().addAll(
            new MoveTo(10.29*s, 3.86*s),
            new LineTo(1.82*s, 18*s),
            new javafx.scene.shape.QuadCurveTo(1*s, 19.5*s, 2.5*s, 20.5*s),
            new LineTo(21.5*s, 20.5*s),
            new javafx.scene.shape.QuadCurveTo(23*s, 19.5*s, 22.18*s, 18*s),
            new LineTo(13.71*s, 3.86*s),
            new javafx.scene.shape.QuadCurveTo(12*s, 1.5*s, 10.29*s, 3.86*s)
        );
        triangulo.setFill(Color.TRANSPARENT);
        triangulo.setStroke(Color.web(color));
        triangulo.setStrokeWidth(2*s);
        triangulo.setStrokeLineCap(StrokeLineCap.ROUND);
        triangulo.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Exclamación - línea
        Line exclamacion = new Line(12*s, 9*s, 12*s, 13*s);
        exclamacion.setStroke(Color.web(color));
        exclamacion.setStrokeWidth(2*s);
        exclamacion.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Exclamación - punto
        Line punto = new Line(12*s, 17*s, 12.01*s, 17*s);
        punto.setStroke(Color.web(color));
        punto.setStrokeWidth(2*s);
        punto.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(triangulo, exclamacion, punto);
        return g;
    }

    /** Ícono de información (Lucide 'Info') */
    public static Node info(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Círculo
        Circle circulo = new Circle(12*s, 12*s, 10*s);
        circulo.setFill(Color.TRANSPARENT);
        circulo.setStroke(Color.web(color));
        circulo.setStrokeWidth(2*s);
        
        // Punto superior (usando Line para mantener consistencia)
        Line puntoI = new Line(12*s, 8*s, 12.01*s, 8*s);
        puntoI.setStroke(Color.web(color));
        puntoI.setStrokeWidth(2*s);
        puntoI.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Línea vertical
        Line cuerpoI = new Line(12*s, 12*s, 12*s, 16*s);
        cuerpoI.setStroke(Color.web(color));
        cuerpoI.setStrokeWidth(2*s);
        cuerpoI.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(circulo, puntoI, cuerpoI);
        return g;
    }

    /** Ícono de pregunta (Lucide 'Help-Circle') */
    public static Node pregunta(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Círculo
        Circle circulo = new Circle(12*s, 12*s, 10*s);
        circulo.setFill(Color.TRANSPARENT);
        circulo.setStroke(Color.web(color));
        circulo.setStrokeWidth(2*s);
        
        // Signo de interrogación (path curvo)
        Path interrogacion = new Path();
        interrogacion.getElements().addAll(
            new MoveTo(9.09*s, 9*s),
            new javafx.scene.shape.QuadCurveTo(9.09*s, 7.5*s, 10*s, 7*s),
            new javafx.scene.shape.QuadCurveTo(12*s, 6*s, 14*s, 7*s),
            new javafx.scene.shape.QuadCurveTo(15*s, 7.5*s, 15*s, 9*s),
            new javafx.scene.shape.QuadCurveTo(15*s, 10.5*s, 12*s, 12*s)
        );
        interrogacion.setFill(Color.TRANSPARENT);
        interrogacion.setStroke(Color.web(color));
        interrogacion.setStrokeWidth(2*s);
        interrogacion.setStrokeLineCap(StrokeLineCap.ROUND);
        interrogacion.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Punto inferior
        Line punto = new Line(12*s, 17*s, 12.01*s, 17*s);
        punto.setStroke(Color.web(color));
        punto.setStrokeWidth(2*s);
        punto.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(circulo, interrogacion, punto);
        return g;
    }

    /** Ícono de candado (Lucide 'lock') */
    public static Node candado(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Cuerpo del candado (rectángulo)
        Rectangle cuerpo = new Rectangle(3*s, 11*s, 18*s, 11*s);
        cuerpo.setArcWidth(4*s);
        cuerpo.setArcHeight(4*s);
        cuerpo.setFill(Color.TRANSPARENT);
        cuerpo.setStroke(Color.web(color));
        cuerpo.setStrokeWidth(2*s);
        
        // Arco superior
        Path arco = new Path();
        arco.getElements().addAll(
            new MoveTo(7*s, 11*s),
            new LineTo(7*s, 7*s),
            new ArcTo(5*s, 5*s, 0, 17*s, 7*s, true, true),
            new LineTo(17*s, 11*s)
        );
        arco.setFill(Color.TRANSPARENT);
        arco.setStroke(Color.web(color));
        arco.setStrokeWidth(2*s);
        arco.setStrokeLineCap(StrokeLineCap.ROUND);
        arco.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(cuerpo, arco);
        return g;
    }
    
    /** Icono de ojo (Lucide 'eye') - para mostrar/ocultar contrasenas */
    public static Node ojo(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Forma del ojo (path eliptico)
        Path ojoPath = new Path();
        ojoPath.getElements().addAll(
            new MoveTo(2*s, 12*s),
            new CubicCurveTo(2*s, 12*s, 5*s, 5*s, 12*s, 5*s),
            new CubicCurveTo(19*s, 5*s, 22*s, 12*s, 22*s, 12*s),
            new CubicCurveTo(22*s, 12*s, 19*s, 19*s, 12*s, 19*s),
            new CubicCurveTo(5*s, 19*s, 2*s, 12*s, 2*s, 12*s)
        );
        ojoPath.setFill(Color.TRANSPARENT);
        ojoPath.setStroke(Color.web(color));
        ojoPath.setStrokeWidth(2*s);
        ojoPath.setStrokeLineCap(StrokeLineCap.ROUND);
        ojoPath.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Pupila (circulo central)
        Circle pupila = new Circle(12*s, 12*s, 3*s);
        pupila.setFill(Color.TRANSPARENT);
        pupila.setStroke(Color.web(color));
        pupila.setStrokeWidth(2*s);
        
        g.getChildren().addAll(ojoPath, pupila);
        return g;
    }
    
    /** Icono de ojo cerrado (Lucide 'eye-off') - para ocultar contrasenas */
    public static Node ojoCerrado(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Forma del ojo parcial
        Path ojoPath = new Path();
        ojoPath.getElements().addAll(
            new MoveTo(17.94*s, 17.94*s),
            new CubicCurveTo(16.23*s, 19.24*s, 14.21*s, 20*s, 12*s, 20*s),
            new CubicCurveTo(5*s, 20*s, 2*s, 12*s, 2*s, 12*s),
            new CubicCurveTo(2*s, 12*s, 3.73*s, 8.55*s, 6.06*s, 6.06*s)
        );
        ojoPath.setFill(Color.TRANSPARENT);
        ojoPath.setStroke(Color.web(color));
        ojoPath.setStrokeWidth(2*s);
        ojoPath.setStrokeLineCap(StrokeLineCap.ROUND);
        ojoPath.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Otra parte del ojo
        Path ojoPath2 = new Path();
        ojoPath2.getElements().addAll(
            new MoveTo(9.88*s, 9.88*s),
            new CubicCurveTo(10.42*s, 9.34*s, 11.18*s, 9*s, 12*s, 9*s),
            new CubicCurveTo(13.66*s, 9*s, 15*s, 10.34*s, 15*s, 12*s),
            new CubicCurveTo(15*s, 12.82*s, 14.66*s, 13.58*s, 14.12*s, 14.12*s)
        );
        ojoPath2.setFill(Color.TRANSPARENT);
        ojoPath2.setStroke(Color.web(color));
        ojoPath2.setStrokeWidth(2*s);
        ojoPath2.setStrokeLineCap(StrokeLineCap.ROUND);
        ojoPath2.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Arco superior
        Path arcoSup = new Path();
        arcoSup.getElements().addAll(
            new MoveTo(10*s, 4*s),
            new CubicCurveTo(10.66*s, 3.87*s, 11.33*s, 3.8*s, 12*s, 3.8*s),
            new CubicCurveTo(19*s, 3.8*s, 22*s, 12*s, 22*s, 12*s),
            new CubicCurveTo(22*s, 12*s, 21.17*s, 13.73*s, 19.69*s, 15.69*s)
        );
        arcoSup.setFill(Color.TRANSPARENT);
        arcoSup.setStroke(Color.web(color));
        arcoSup.setStrokeWidth(2*s);
        arcoSup.setStrokeLineCap(StrokeLineCap.ROUND);
        arcoSup.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Linea diagonal (tachado)
        Line linea = new Line(2*s, 2*s, 22*s, 22*s);
        linea.setStroke(Color.web(color));
        linea.setStrokeWidth(2*s);
        linea.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(ojoPath, ojoPath2, arcoSup, linea);
        return g;
    }
    /** Ícono de edificio/oficina (Lucide 'building-2') */
    public static Node edificio(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Edificio principal
        Path edificioPath = new Path();
        edificioPath.getElements().addAll(
            new MoveTo(6*s, 22*s),
            new LineTo(6*s, 4*s),
            new LineTo(18*s, 4*s),
            new LineTo(18*s, 22*s)
        );
        edificioPath.setFill(Color.TRANSPARENT);
        edificioPath.setStroke(Color.web(color));
        edificioPath.setStrokeWidth(2*s);
        edificioPath.setStrokeLineCap(StrokeLineCap.ROUND);
        edificioPath.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Base
        Line base = new Line(2*s, 22*s, 22*s, 22*s);
        base.setStroke(Color.web(color));
        base.setStrokeWidth(2*s);
        base.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Ventanas - Fila 1
        Rectangle v1 = new Rectangle(9*s, 7*s, 2*s, 2.5*s);
        v1.setFill(Color.TRANSPARENT);
        v1.setStroke(Color.web(color));
        v1.setStrokeWidth(1.5*s);
        
        Rectangle v2 = new Rectangle(13*s, 7*s, 2*s, 2.5*s);
        v2.setFill(Color.TRANSPARENT);
        v2.setStroke(Color.web(color));
        v2.setStrokeWidth(1.5*s);
        
        // Ventanas - Fila 2
        Rectangle v3 = new Rectangle(9*s, 12*s, 2*s, 2.5*s);
        v3.setFill(Color.TRANSPARENT);
        v3.setStroke(Color.web(color));
        v3.setStrokeWidth(1.5*s);
        
        Rectangle v4 = new Rectangle(13*s, 12*s, 2*s, 2.5*s);
        v4.setFill(Color.TRANSPARENT);
        v4.setStroke(Color.web(color));
        v4.setStrokeWidth(1.5*s);
        
        // Puerta
        Rectangle puerta = new Rectangle(10*s, 17*s, 4*s, 5*s);
        puerta.setFill(Color.TRANSPARENT);
        puerta.setStroke(Color.web(color));
        puerta.setStrokeWidth(1.5*s);
        
        g.getChildren().addAll(edificioPath, base, v1, v2, v3, v4, puerta);
        return g;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ÍCONOS MISCELÁNEOS (v6.0 - Minimalista Elegante)
    // ═══════════════════════════════════════════════════════════════════════

    /** Ícono de escudo - Seguridad (Lucide 'Shield-Check') */
    public static Node escudo(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Forma del escudo
        Path escudo = new Path();
        escudo.getElements().addAll(
            new MoveTo(12*s, 22*s),
            new javafx.scene.shape.QuadCurveTo(7*s, 19*s, 5*s, 13*s),
            new javafx.scene.shape.QuadCurveTo(3*s, 7*s, 5*s, 5*s),
            new LineTo(12*s, 2*s),
            new LineTo(19*s, 5*s),
            new javafx.scene.shape.QuadCurveTo(21*s, 7*s, 19*s, 13*s),
            new javafx.scene.shape.QuadCurveTo(17*s, 19*s, 12*s, 22*s)
        );
        escudo.setStroke(Color.web(color));
        escudo.setStrokeWidth(2*s);
        escudo.setStrokeLineCap(StrokeLineCap.ROUND);
        escudo.setStrokeLineJoin(StrokeLineJoin.ROUND);
        escudo.setFill(Color.TRANSPARENT);
        
        // Check interno
        Path check = new Path();
        check.getElements().addAll(
            new MoveTo(9*s, 12*s),
            new LineTo(11*s, 14*s),
            new LineTo(15*s, 10*s)
        );
        check.setStroke(Color.web(color));
        check.setStrokeWidth(2*s);
        check.setStrokeLineCap(StrokeLineCap.ROUND);
        check.setStrokeLineJoin(StrokeLineJoin.ROUND);
        check.setFill(Color.TRANSPARENT);
        
        g.getChildren().addAll(escudo, check);
        return g;
    }

    /** Ícono de rayo (Lucide 'Zap') */
    public static Node rayo(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Forma del rayo
        Path rayo = new Path();
        rayo.getElements().addAll(
            new MoveTo(13*s, 2*s),
            new LineTo(3*s, 14*s),
            new LineTo(10*s, 14*s),
            new LineTo(11*s, 22*s),
            new LineTo(21*s, 10*s),
            new LineTo(14*s, 10*s),
            new ClosePath()
        );
        rayo.setStroke(Color.web(color));
        rayo.setStrokeWidth(2*s);
        rayo.setStrokeLineCap(StrokeLineCap.ROUND);
        rayo.setStrokeLineJoin(StrokeLineJoin.ROUND);
        rayo.setFill(Color.TRANSPARENT);
        
        g.getChildren().add(rayo);
        return g;
    }

    /** Ícono de flecha izquierda (Lucide 'Arrow-Left') */
    public static Node flechaIzquierda(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Línea horizontal
        Line linea = new Line(19*s, 12*s, 5*s, 12*s);
        linea.setStroke(Color.web(color));
        linea.setStrokeWidth(2*s);
        linea.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Punta de flecha
        Path punta = new Path();
        punta.getElements().addAll(
            new MoveTo(12*s, 5*s),
            new LineTo(5*s, 12*s),
            new LineTo(12*s, 19*s)
        );
        punta.setStroke(Color.web(color));
        punta.setStrokeWidth(2*s);
        punta.setStrokeLineCap(StrokeLineCap.ROUND);
        punta.setStrokeLineJoin(StrokeLineJoin.ROUND);
        punta.setFill(Color.TRANSPARENT);
        
        g.getChildren().addAll(linea, punta);
        return g;
    }

    /** Ícono de paquete - Caja (Lucide 'Package') */
    public static Node paquete(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Parte superior del paquete
        Path superior = new Path();
        superior.getElements().addAll(
            new MoveTo(12*s, 2*s),
            new LineTo(21*s, 7*s),
            new LineTo(21*s, 17*s),
            new LineTo(12*s, 22*s),
            new LineTo(3*s, 17*s),
            new LineTo(3*s, 7*s),
            new ClosePath()
        );
        superior.setStroke(Color.web(color));
        superior.setStrokeWidth(2*s);
        superior.setStrokeLineCap(StrokeLineCap.ROUND);
        superior.setStrokeLineJoin(StrokeLineJoin.ROUND);
        superior.setFill(Color.TRANSPARENT);
        
        // Línea divisoria central
        Line central = new Line(12*s, 12*s, 12*s, 22*s);
        central.setStroke(Color.web(color));
        central.setStrokeWidth(2*s);
        central.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Líneas de conexión
        Path conexion = new Path();
        conexion.getElements().addAll(
            new MoveTo(3*s, 7*s),
            new LineTo(12*s, 12*s),
            new LineTo(21*s, 7*s)
        );
        conexion.setStroke(Color.web(color));
        conexion.setStrokeWidth(2*s);
        conexion.setStrokeLineCap(StrokeLineCap.ROUND);
        conexion.setStrokeLineJoin(StrokeLineJoin.ROUND);
        conexion.setFill(Color.TRANSPARENT);
        
        g.getChildren().addAll(superior, central, conexion);
        return g;
    }

    /** Ícono de cursor - Apuntar (Lucide 'Mouse-Pointer') */
    public static Node apuntar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Cursor
        Path cursor = new Path();
        cursor.getElements().addAll(
            new MoveTo(3*s, 3*s),
            new LineTo(10*s, 21*s),
            new LineTo(13*s, 13*s),
            new LineTo(21*s, 10*s),
            new ClosePath()
        );
        cursor.setStroke(Color.web(color));
        cursor.setStrokeWidth(2*s);
        cursor.setStrokeLineCap(StrokeLineCap.ROUND);
        cursor.setStrokeLineJoin(StrokeLineJoin.ROUND);
        cursor.setFill(Color.TRANSPARENT);
        
        g.getChildren().add(cursor);
        return g;
    }
    
    /** Ícono de clic (Lucide 'Mouse-Pointer-Click') */
    public static Node clic(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Cursor
        Path cursor = new Path();
        cursor.getElements().addAll(
            new MoveTo(3*s, 3*s),
            new LineTo(9*s, 19*s),
            new LineTo(12*s, 12*s),
            new LineTo(19*s, 9*s),
            new ClosePath()
        );
        cursor.setStroke(Color.web(color));
        cursor.setStrokeWidth(2*s);
        cursor.setStrokeLineCap(StrokeLineCap.ROUND);
        cursor.setStrokeLineJoin(StrokeLineJoin.ROUND);
        cursor.setFill(Color.TRANSPARENT);
        
        // Líneas de clic (ondas)
        Line onda1 = new Line(14*s, 4*s, 20*s, 4*s);
        onda1.setStroke(Color.web(color));
        onda1.setStrokeWidth(2*s);
        onda1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line onda2 = new Line(17*s, 7*s, 21*s, 7*s);
        onda2.setStroke(Color.web(color));
        onda2.setStrokeWidth(2*s);
        onda2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line onda3 = new Line(4*s, 14*s, 4*s, 20*s);
        onda3.setStroke(Color.web(color));
        onda3.setStrokeWidth(2*s);
        onda3.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line onda4 = new Line(7*s, 17*s, 7*s, 21*s);
        onda4.setStroke(Color.web(color));
        onda4.setStrokeWidth(2*s);
        onda4.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(cursor, onda1, onda2, onda3, onda4);
        return g;
    }

    /** Ícono de hoja de cálculo (Lucide 'Sheet') */
    public static Node hojaCalculo(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Marco del documento
        Rectangle marco = new Rectangle(3*s, 3*s, 18*s, 18*s);
        marco.setArcWidth(2*s);
        marco.setArcHeight(2*s);
        marco.setFill(Color.TRANSPARENT);
        marco.setStroke(Color.web(color));
        marco.setStrokeWidth(2*s);
        marco.setStrokeLineCap(StrokeLineCap.ROUND);
        marco.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Línea vertical central
        Line v = new Line(12*s, 3*s, 12*s, 21*s);
        v.setStroke(Color.web(color));
        v.setStrokeWidth(2*s);
        
        // Líneas horizontales
        Line h1 = new Line(3*s, 9*s, 21*s, 9*s);
        h1.setStroke(Color.web(color));
        h1.setStrokeWidth(2*s);
        
        Line h2 = new Line(3*s, 15*s, 21*s, 15*s);
        h2.setStroke(Color.web(color));
        h2.setStrokeWidth(2*s);
        
        g.getChildren().addAll(marco, v, h1, h2);
        return g;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ÍCONOS DE TEMA (CLARO / OSCURO)
    // ═══════════════════════════════════════════════════════════════════════

    /** Ícono de sol - Para modo claro (Lucide 'Sun') */
    public static Node sol(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Centro del sol (círculo)
        Circle centro = new Circle(12*s, 12*s, 4*s);
        centro.setFill(Color.TRANSPARENT);
        centro.setStroke(Color.web(color));
        centro.setStrokeWidth(2*s);
        
        // Rayos del sol
        double[][] rayos = {
            {12, 1, 12, 3},      // arriba
            {12, 21, 12, 23},    // abajo
            {1, 12, 3, 12},      // izquierda
            {21, 12, 23, 12},    // derecha
            {4.22, 4.22, 5.64, 5.64},    // diagonal superior izquierda
            {18.36, 5.64, 19.78, 4.22},  // diagonal superior derecha
            {4.22, 19.78, 5.64, 18.36},  // diagonal inferior izquierda
            {18.36, 18.36, 19.78, 19.78} // diagonal inferior derecha
        };
        
        for (double[] rayo : rayos) {
            Line linea = new Line(rayo[0]*s, rayo[1]*s, rayo[2]*s, rayo[3]*s);
            linea.setStroke(Color.web(color));
            linea.setStrokeWidth(2*s);
            linea.setStrokeLineCap(StrokeLineCap.ROUND);
            g.getChildren().add(linea);
        }
        
        g.getChildren().add(centro);
        return g;
    }

    /** Ícono de luna - Para modo oscuro (Lucide 'Moon') */
    public static Node luna(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Luna creciente simplificada
        Path luna = new Path();
        luna.getElements().addAll(
            new MoveTo(21*s, 12.79*s),
            new CubicCurveTo(19.21*s, 18.5*s, 13.5*s, 22.08*s, 7.79*s, 20.29*s),
            new CubicCurveTo(2.08*s, 18.5*s, -1.21*s, 12.79*s, 1.71*s, 7.21*s),
            new CubicCurveTo(4.63*s, 1.63*s, 10.5*s, -0.79*s, 16*s, 1.5*s),
            new CubicCurveTo(12.5*s, 4*s, 10*s, 8*s, 10*s, 12*s),
            new CubicCurveTo(10*s, 16*s, 12.5*s, 20*s, 16*s, 22.5*s),
            new CubicCurveTo(18*s, 20*s, 21*s, 16.5*s, 21*s, 12.79*s)
        );
        
        luna.setFill(Color.TRANSPARENT);
        luna.setStroke(Color.web(color));
        luna.setStrokeWidth(2*s);
        luna.setStrokeLineCap(StrokeLineCap.ROUND);
        luna.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().add(luna);
        return g;
    }

    /** Ícono de tema (combina sol y luna) - Para alternar tema */
    public static Node temaAlternar(String color, double size, boolean esTemaOscuro) {
        return esTemaOscuro ? sol(color, size) : luna(color, size);
    }

    /** Ícono de administrador (Lucide 'user-cog') */
    public static Node admin(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial: <circle cx="18" cy="15" r="3"/><circle cx="9" cy="7" r="4"/><path d="M10 15H6a4 4 0 0 0-4 4v2"/><path d="m21.7 16.4-.9-.3"/><path d="m15.2 13.9-.9-.3"/><path d="m16.6 18.7.3-.9"/><path d="m19.1 12.2.3-.9"/><path d="m19.6 18.7-.4-1"/><path d="m16.8 12.3-.4-1"/><path d="m14.3 16.6 1-.4"/><path d="m20.7 13.8 1-.4"/>
        
        // Círculo engranaje (derecha)
        Circle engranaje = new Circle(18*s, 15*s, 3*s);
        engranaje.setFill(Color.TRANSPARENT);
        engranaje.setStroke(Color.web(color));
        engranaje.setStrokeWidth(2*s);
        
        // Cabeza del usuario
        Circle cabeza = new Circle(9*s, 7*s, 4*s);
        cabeza.setFill(Color.TRANSPARENT);
        cabeza.setStroke(Color.web(color));
        cabeza.setStrokeWidth(2*s);
        
        // Cuerpo del usuario
        Path cuerpo = new Path();
        cuerpo.getElements().addAll(
            new MoveTo(10*s, 15*s),
            new LineTo(6*s, 15*s),
            new CubicCurveTo(3.79*s, 15*s, 2*s, 16.79*s, 2*s, 19*s),
            new LineTo(2*s, 21*s)
        );
        cuerpo.setFill(Color.TRANSPARENT);
        cuerpo.setStroke(Color.web(color));
        cuerpo.setStrokeWidth(2*s);
        cuerpo.setStrokeLineCap(StrokeLineCap.ROUND);
        cuerpo.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Dientes del engranaje (8 líneas pequeñas)
        Line d1 = new Line(21.7*s, 16.4*s, 20.8*s, 16.1*s);
        d1.setStroke(Color.web(color));
        d1.setStrokeWidth(2*s);
        d1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line d2 = new Line(15.2*s, 13.9*s, 14.3*s, 13.6*s);
        d2.setStroke(Color.web(color));
        d2.setStrokeWidth(2*s);
        d2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line d3 = new Line(16.6*s, 18.7*s, 16.9*s, 17.8*s);
        d3.setStroke(Color.web(color));
        d3.setStrokeWidth(2*s);
        d3.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line d4 = new Line(19.1*s, 12.2*s, 19.4*s, 11.3*s);
        d4.setStroke(Color.web(color));
        d4.setStrokeWidth(2*s);
        d4.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line d5 = new Line(19.6*s, 18.7*s, 19.2*s, 17.7*s);
        d5.setStroke(Color.web(color));
        d5.setStrokeWidth(2*s);
        d5.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line d6 = new Line(16.8*s, 12.3*s, 16.4*s, 11.3*s);
        d6.setStroke(Color.web(color));
        d6.setStrokeWidth(2*s);
        d6.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line d7 = new Line(14.3*s, 16.6*s, 15.3*s, 16.2*s);
        d7.setStroke(Color.web(color));
        d7.setStrokeWidth(2*s);
        d7.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line d8 = new Line(20.7*s, 13.8*s, 21.7*s, 13.4*s);
        d8.setStroke(Color.web(color));
        d8.setStrokeWidth(2*s);
        d8.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(engranaje, cabeza, cuerpo, d1, d2, d3, d4, d5, d6, d7, d8);
        return g;
    }

    /** Ícono de agregar (Lucide 'Plus-Circle') */
    public static Node agregar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Círculo
        Circle circulo = new Circle(12*s, 12*s, 10*s);
        circulo.setFill(Color.TRANSPARENT);
        circulo.setStroke(Color.web(color));
        circulo.setStrokeWidth(2*s);
        
        // Línea vertical
        Line vertical = new Line(12*s, 8*s, 12*s, 16*s);
        vertical.setStroke(Color.web(color));
        vertical.setStrokeWidth(2*s);
        vertical.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Línea horizontal
        Line horizontal = new Line(8*s, 12*s, 16*s, 12*s);
        horizontal.setStroke(Color.web(color));
        horizontal.setStrokeWidth(2*s);
        horizontal.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(circulo, vertical, horizontal);
        return g;
    }

    /** Ícono de papelera (Lucide 'Trash-2') */
    public static Node papelera(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Línea superior
        Line tapa = new Line(3*s, 6*s, 21*s, 6*s);
        tapa.setStroke(Color.web(color));
        tapa.setStrokeWidth(2*s);
        tapa.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Asa superior
        Path asa = new Path();
        asa.getElements().addAll(
            new MoveTo(8*s, 6*s),
            new LineTo(8*s, 4*s),
            new javafx.scene.shape.QuadCurveTo(8*s, 3*s, 9*s, 3*s),
            new LineTo(15*s, 3*s),
            new javafx.scene.shape.QuadCurveTo(16*s, 3*s, 16*s, 4*s),
            new LineTo(16*s, 6*s)
        );
        asa.setFill(Color.TRANSPARENT);
        asa.setStroke(Color.web(color));
        asa.setStrokeWidth(2*s);
        asa.setStrokeLineCap(StrokeLineCap.ROUND);
        asa.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Cuerpo de la papelera
        Path cuerpo = new Path();
        cuerpo.getElements().addAll(
            new MoveTo(5*s, 6*s),
            new LineTo(6*s, 20*s),
            new javafx.scene.shape.QuadCurveTo(6*s, 21*s, 7*s, 21*s),
            new LineTo(17*s, 21*s),
            new javafx.scene.shape.QuadCurveTo(18*s, 21*s, 18*s, 20*s),
            new LineTo(19*s, 6*s)
        );
        cuerpo.setFill(Color.TRANSPARENT);
        cuerpo.setStroke(Color.web(color));
        cuerpo.setStrokeWidth(2*s);
        cuerpo.setStrokeLineCap(StrokeLineCap.ROUND);
        cuerpo.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Líneas internas
        Line l1 = new Line(10*s, 11*s, 10*s, 17*s);
        l1.setStroke(Color.web(color));
        l1.setStrokeWidth(2*s);
        l1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line l2 = new Line(14*s, 11*s, 14*s, 17*s);
        l2.setStroke(Color.web(color));
        l2.setStrokeWidth(2*s);
        l2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(cuerpo, tapa, asa, l1, l2);
        return g;
    }

    /** Ícono de importar (Lucide 'Upload') */
    public static Node importar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Línea vertical de la flecha
        Line flechaLinea = new Line(12*s, 15*s, 12*s, 3*s);
        flechaLinea.setStroke(Color.web(color));
        flechaLinea.setStrokeWidth(2*s);
        flechaLinea.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Punta de flecha hacia arriba
        Path punta = new Path();
        punta.getElements().addAll(
            new MoveTo(7*s, 8*s),
            new LineTo(12*s, 3*s),
            new LineTo(17*s, 8*s)
        );
        punta.setStroke(Color.web(color));
        punta.setStrokeWidth(2*s);
        punta.setStrokeLineCap(StrokeLineCap.ROUND);
        punta.setStrokeLineJoin(StrokeLineJoin.ROUND);
        punta.setFill(Color.TRANSPARENT);
        
        // Base/bandeja
        Path caja = new Path();
        caja.getElements().addAll(
            new MoveTo(21*s, 15*s),
            new LineTo(21*s, 19*s),
            new javafx.scene.shape.QuadCurveTo(21*s, 21*s, 19*s, 21*s),
            new LineTo(5*s, 21*s),
            new javafx.scene.shape.QuadCurveTo(3*s, 21*s, 3*s, 19*s),
            new LineTo(3*s, 15*s)
        );
        caja.setStroke(Color.web(color));
        caja.setStrokeWidth(2*s);
        caja.setStrokeLineCap(StrokeLineCap.ROUND);
        caja.setStrokeLineJoin(StrokeLineJoin.ROUND);
        caja.setFill(Color.TRANSPARENT);
        
        g.getChildren().addAll(caja, flechaLinea, punta);
        return g;
    }

    /** Ícono de filtro (Lucide 'Filter') */
    public static Node filtro(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial: <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
        Path filtro = new Path();
        filtro.getElements().addAll(
            new MoveTo(22*s, 3*s),
            new LineTo(2*s, 3*s),
            new LineTo(10*s, 12.46*s),
            new LineTo(10*s, 19*s),
            new LineTo(14*s, 21*s),
            new LineTo(14*s, 12.46*s),
            new ClosePath()
        );
        filtro.setFill(Color.TRANSPARENT);
        filtro.setStroke(Color.web(color));
        filtro.setStrokeWidth(2*s);
        filtro.setStrokeLineCap(StrokeLineCap.ROUND);
        filtro.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().add(filtro);
        return g;
    }

    /** Ícono de actualizar/refrescar (Lucide 'Refresh-Cw') */
    public static Node actualizar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Path oficial: <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/><path d="M21 3v5h-5"/><path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/><path d="M8 16H3v5"/>
        
        // Arco superior
        Path arcoSup = new Path();
        arcoSup.getElements().addAll(
            new MoveTo(3*s, 12*s),
            new ArcTo(9*s, 9*s, 0, 12*s, 3*s, false, true),
            new CubicCurveTo(14.5*s, 3*s, 16.8*s, 4*s, 18.74*s, 5.74*s),
            new LineTo(21*s, 8*s)
        );
        arcoSup.setFill(Color.TRANSPARENT);
        arcoSup.setStroke(Color.web(color));
        arcoSup.setStrokeWidth(2*s);
        arcoSup.setStrokeLineCap(StrokeLineCap.ROUND);
        arcoSup.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Flecha superior
        Path flechaSup = new Path();
        flechaSup.getElements().addAll(
            new MoveTo(21*s, 3*s),
            new LineTo(21*s, 8*s),
            new LineTo(16*s, 8*s)
        );
        flechaSup.setFill(Color.TRANSPARENT);
        flechaSup.setStroke(Color.web(color));
        flechaSup.setStrokeWidth(2*s);
        flechaSup.setStrokeLineCap(StrokeLineCap.ROUND);
        flechaSup.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Arco inferior
        Path arcoInf = new Path();
        arcoInf.getElements().addAll(
            new MoveTo(21*s, 12*s),
            new ArcTo(9*s, 9*s, 0, 12*s, 21*s, false, true),
            new CubicCurveTo(9.5*s, 21*s, 7.2*s, 20*s, 5.26*s, 18.26*s),
            new LineTo(3*s, 16*s)
        );
        arcoInf.setFill(Color.TRANSPARENT);
        arcoInf.setStroke(Color.web(color));
        arcoInf.setStrokeWidth(2*s);
        arcoInf.setStrokeLineCap(StrokeLineCap.ROUND);
        arcoInf.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Flecha inferior
        Path flechaInf = new Path();
        flechaInf.getElements().addAll(
            new MoveTo(8*s, 16*s),
            new LineTo(3*s, 16*s),
            new LineTo(3*s, 21*s)
        );
        flechaInf.setFill(Color.TRANSPARENT);
        flechaInf.setStroke(Color.web(color));
        flechaInf.setStrokeWidth(2*s);
        flechaInf.setStrokeLineCap(StrokeLineCap.ROUND);
        flechaInf.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(arcoSup, flechaSup, arcoInf, flechaInf);
        return g;
    }

    /** Ícono de imagen/foto (Lucide 'Image') */
    public static Node imagen(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Rectángulo exterior (marco de la imagen)
        Path marco = new Path();
        marco.getElements().addAll(
            new MoveTo(21*s, 15*s),
            new LineTo(21*s, 19*s),
            new javafx.scene.shape.QuadCurveTo(21*s, 21*s, 19*s, 21*s),
            new LineTo(5*s, 21*s),
            new javafx.scene.shape.QuadCurveTo(3*s, 21*s, 3*s, 19*s),
            new LineTo(3*s, 5*s),
            new javafx.scene.shape.QuadCurveTo(3*s, 3*s, 5*s, 3*s),
            new LineTo(19*s, 3*s),
            new javafx.scene.shape.QuadCurveTo(21*s, 3*s, 21*s, 5*s),
            new LineTo(21*s, 15*s)
        );
        marco.setFill(Color.TRANSPARENT);
        marco.setStroke(Color.web(color));
        marco.setStrokeWidth(2*s);
        marco.setStrokeLineCap(StrokeLineCap.ROUND);
        marco.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Círculo (sol/punto de interés)
        Circle sol = new Circle(8.5*s, 8.5*s, 2.5*s);
        sol.setFill(Color.TRANSPARENT);
        sol.setStroke(Color.web(color));
        sol.setStrokeWidth(2*s);
        
        // Montaña/Paisaje
        Path montana = new Path();
        montana.getElements().addAll(
            new MoveTo(21*s, 15*s),
            new LineTo(16*s, 10*s),
            new LineTo(12*s, 14*s),
            new LineTo(8*s, 10*s),
            new LineTo(3*s, 15*s)
        );
        montana.setFill(Color.TRANSPARENT);
        montana.setStroke(Color.web(color));
        montana.setStrokeWidth(2*s);
        montana.setStrokeLineCap(StrokeLineCap.ROUND);
        montana.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(marco, sol, montana);
        return g;
    }

    /** Ícono de estrella/sparkle (Lucide 'Sparkles') */
    public static Node estrella(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Estrella principal
        Path estrella1 = new Path();
        estrella1.getElements().addAll(
            new MoveTo(12*s, 3*s),
            new LineTo(13.4*s, 8.6*s),
            new LineTo(19*s, 10*s),
            new LineTo(13.4*s, 11.4*s),
            new LineTo(12*s, 17*s),
            new LineTo(10.6*s, 11.4*s),
            new LineTo(5*s, 10*s),
            new LineTo(10.6*s, 8.6*s),
            new ClosePath()
        );
        estrella1.setFill(Color.TRANSPARENT);
        estrella1.setStroke(Color.web(color));
        estrella1.setStrokeWidth(2*s);
        estrella1.setStrokeLineCap(StrokeLineCap.ROUND);
        estrella1.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Estrella pequeña inferior derecha
        Path estrella2 = new Path();
        estrella2.getElements().addAll(
            new MoveTo(19*s, 16*s),
            new LineTo(19.5*s, 18*s),
            new LineTo(21.5*s, 18.5*s),
            new LineTo(19.5*s, 19*s),
            new LineTo(19*s, 21*s),
            new LineTo(18.5*s, 19*s),
            new LineTo(16.5*s, 18.5*s),
            new LineTo(18.5*s, 18*s),
            new ClosePath()
        );
        estrella2.setFill(Color.TRANSPARENT);
        estrella2.setStroke(Color.web(color));
        estrella2.setStrokeWidth(1.5*s);
        estrella2.setStrokeLineCap(StrokeLineCap.ROUND);
        estrella2.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(estrella1, estrella2);
        return g;
    }

    /** Icono de paleta de colores (Lucide 'droplet' simplificado) */
    public static Node paleta(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Círculo principal
        Circle circulo = new Circle(12*s, 12*s, 8*s);
        circulo.setFill(Color.TRANSPARENT);
        circulo.setStroke(Color.web(color));
        circulo.setStrokeWidth(2*s);
        
        // Puntos de color internos (más grandes y visibles)
        Circle punto1 = new Circle(9*s, 9*s, 1.5*s);
        punto1.setFill(Color.web(color));
        
        Circle punto2 = new Circle(15*s, 9*s, 1.5*s);
        punto2.setFill(Color.web(color));
        
        Circle punto3 = new Circle(12*s, 15*s, 1.5*s);
        punto3.setFill(Color.web(color));
        
        g.getChildren().addAll(circulo, punto1, punto2, punto3);
        return g;
    }

    /** Icono de memoria/CPU (Lucide 'cpu') */
    public static Node memoria(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // CPU central
        Rectangle cpu = new Rectangle(4*s, 4*s, 16*s, 16*s);
        cpu.setArcWidth(2*s);
        cpu.setArcHeight(2*s);
        cpu.setFill(Color.TRANSPARENT);
        cpu.setStroke(Color.web(color));
        cpu.setStrokeWidth(2*s);
        cpu.setStrokeLineCap(StrokeLineCap.ROUND);
        cpu.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Cuadro interno
        Rectangle inner = new Rectangle(9*s, 9*s, 6*s, 6*s);
        inner.setFill(Color.TRANSPARENT);
        inner.setStroke(Color.web(color));
        inner.setStrokeWidth(2*s);
        inner.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(cpu, inner);
        
        // Pines superiores
        for (int i = 0; i < 3; i++) {
            Line pin = new Line((9 + i*3)*s, 1*s, (9 + i*3)*s, 4*s);
            pin.setStroke(Color.web(color));
            pin.setStrokeWidth(2*s);
            pin.setStrokeLineCap(StrokeLineCap.ROUND);
            g.getChildren().add(pin);
        }
        
        // Pines inferiores
        for (int i = 0; i < 3; i++) {
            Line pin = new Line((9 + i*3)*s, 20*s, (9 + i*3)*s, 23*s);
            pin.setStroke(Color.web(color));
            pin.setStrokeWidth(2*s);
            pin.setStrokeLineCap(StrokeLineCap.ROUND);
            g.getChildren().add(pin);
        }
        
        // Pines izquierdos
        for (int i = 0; i < 3; i++) {
            Line pin = new Line(1*s, (9 + i*3)*s, 4*s, (9 + i*3)*s);
            pin.setStroke(Color.web(color));
            pin.setStrokeWidth(2*s);
            pin.setStrokeLineCap(StrokeLineCap.ROUND);
            g.getChildren().add(pin);
        }
        
        // Pines derechos
        for (int i = 0; i < 3; i++) {
            Line pin = new Line(20*s, (9 + i*3)*s, 23*s, (9 + i*3)*s);
            pin.setStroke(Color.web(color));
            pin.setStrokeWidth(2*s);
            pin.setStrokeLineCap(StrokeLineCap.ROUND);
            g.getChildren().add(pin);
        }
        
        return g;
    }

    /** Icono de limpiar/escoba (Lucide 'trash-2' simplificado para limpiar) */
    public static Node limpiar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Escoba/cepillo path
        Path path = new Path();
        path.getElements().addAll(
            new MoveTo(8*s, 3*s),
            new LineTo(16*s, 3*s),
            new MoveTo(12*s, 3*s),
            new LineTo(12*s, 8*s),
            new MoveTo(5*s, 8*s),
            new LineTo(19*s, 8*s),
            new LineTo(18*s, 21*s),
            new LineTo(6*s, 21*s),
            new LineTo(5*s, 8*s)
        );
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Color.web(color));
        path.setStrokeWidth(2*s);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        // Lineas internas
        Line l1 = new Line(10*s, 12*s, 10*s, 17*s);
        l1.setStroke(Color.web(color));
        l1.setStrokeWidth(2*s);
        l1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        Line l2 = new Line(14*s, 12*s, 14*s, 17*s);
        l2.setStroke(Color.web(color));
        l2.setStrokeWidth(2*s);
        l2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(path, l1, l2);
        return g;
    }

    /** Icono de verificar con circulo (Lucide 'check-circle') */
    public static Node verificar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Circulo
        Circle circle = new Circle(12*s, 12*s, 10*s);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web(color));
        circle.setStrokeWidth(2*s);
        
        // Check
        Polyline check = new Polyline(
            9*s, 12*s,
            11*s, 14*s,
            15*s, 10*s
        );
        check.setFill(Color.TRANSPARENT);
        check.setStroke(Color.web(color));
        check.setStrokeWidth(2*s);
        check.setStrokeLineCap(StrokeLineCap.ROUND);
        check.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(circle, check);
        return g;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NUEVOS ÍCONOS PARA FUNCIONALIDADES AVANZADAS
    // ═══════════════════════════════════════════════════════════════════════
    
    /** Ícono de lupa - Búsqueda (Lucide 'search') */
    public static Node lupa(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        Circle circulo = new Circle(11*s, 11*s, 8*s);
        circulo.setFill(Color.TRANSPARENT);
        circulo.setStroke(Color.web(color));
        circulo.setStrokeWidth(2*s);
        
        Line linea = new Line(21*s, 21*s, 16.65*s, 16.65*s);
        linea.setStroke(Color.web(color));
        linea.setStrokeWidth(2*s);
        linea.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(circulo, linea);
        return g;
    }
    
    /** Ícono de copiar (Lucide 'copy') */
    public static Node copiar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        Rectangle rect1 = new Rectangle(9*s, 9*s, 13*s, 13*s);
        rect1.setArcWidth(4*s);
        rect1.setArcHeight(4*s);
        rect1.setFill(Color.TRANSPARENT);
        rect1.setStroke(Color.web(color));
        rect1.setStrokeWidth(2*s);
        
        Path rect2 = new Path();
        rect2.getElements().addAll(
            new MoveTo(5*s, 15*s),
            new LineTo(5*s, 5*s),
            new CubicCurveTo(5*s, 3.9*s, 5.9*s, 3*s, 7*s, 3*s),
            new LineTo(15*s, 3*s)
        );
        rect2.setFill(Color.TRANSPARENT);
        rect2.setStroke(Color.web(color));
        rect2.setStrokeWidth(2*s);
        rect2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(rect2, rect1);
        return g;
    }
    
    /** Ícono de duplicar (Lucide 'files') */
    public static Node duplicar(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        Path archivo1 = new Path();
        archivo1.getElements().addAll(
            new MoveTo(15*s, 2*s),
            new LineTo(15*s, 8*s),
            new LineTo(21*s, 8*s)
        );
        archivo1.setFill(Color.TRANSPARENT);
        archivo1.setStroke(Color.web(color));
        archivo1.setStrokeWidth(2*s);
        archivo1.setStrokeLineCap(StrokeLineCap.ROUND);
        archivo1.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        Rectangle rect1 = new Rectangle(3*s, 7*s, 12*s, 13*s);
        rect1.setArcWidth(4*s);
        rect1.setArcHeight(4*s);
        rect1.setFill(Color.TRANSPARENT);
        rect1.setStroke(Color.web(color));
        rect1.setStrokeWidth(2*s);
        
        Path rect2 = new Path();
        rect2.getElements().addAll(
            new MoveTo(15*s, 2*s),
            new LineTo(9*s, 2*s),
            new CubicCurveTo(7.9*s, 2*s, 7*s, 2.9*s, 7*s, 4*s),
            new LineTo(7*s, 7*s)
        );
        rect2.setFill(Color.TRANSPARENT);
        rect2.setStroke(Color.web(color));
        rect2.setStrokeWidth(2*s);
        rect2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().addAll(rect1, rect2, archivo1);
        return g;
    }
    
    /** Ícono de menos/guion (Lucide 'Minus') - Para estado intermedio de checkbox */
    public static Node menos(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Línea horizontal
        Path linea = new Path();
        linea.getElements().addAll(
            new MoveTo(5*s, 12*s),
            new LineTo(19*s, 12*s)
        );
        linea.setFill(Color.TRANSPARENT);
        linea.setStroke(Color.web(color));
        linea.setStrokeWidth(2.5*s);
        linea.setStrokeLineCap(StrokeLineCap.ROUND);
        
        g.getChildren().add(linea);
        return g;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ICONOS PARA NOTIFICACIONES
    // ═══════════════════════════════════════════════════════════════════════
    
    /** Ícono CheckCircle - Para notificaciones de éxito */
    public static javafx.scene.shape.SVGPath crearIconoCheckCircle(String color, int size) {
        javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
        path.setContent("M22 11.08V12a10 10 0 1 1-5.93-9.14M22 4L12 14.01l-3-3");
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Color.web(color));
        path.setStrokeWidth(2);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return path;
    }
    
    /** Ícono XCircle - Para notificaciones de error */
    public static javafx.scene.shape.SVGPath crearIconoXCircle(String color, int size) {
        javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
        path.setContent("M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm3.707 12.293a1 1 0 1 1-1.414 1.414L12 13.414l-2.293 2.293a1 1 0 0 1-1.414-1.414L10.586 12 8.293 9.707a1 1 0 0 1 1.414-1.414L12 10.586l2.293-2.293a1 1 0 0 1 1.414 1.414L13.414 12l2.293 2.293z");
        path.setFill(Color.web(color));
        path.setStroke(Color.TRANSPARENT);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return path;
    }
    
    /** Ícono AlertTriangle - Para notificaciones de advertencia */
    public static javafx.scene.shape.SVGPath crearIconoAlertTriangle(String color, int size) {
        javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
        path.setContent("M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0zM12 9v4M12 17h.01");
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Color.web(color));
        path.setStrokeWidth(2);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return path;
    }
    
    /** Ícono Info - Para notificaciones informativas */
    public static javafx.scene.shape.SVGPath crearIconoInfo(String color, int size) {
        javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
        path.setContent("M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 6a1 1 0 0 1 1 1v5a1 1 0 1 1-2 0V9a1 1 0 0 1 1-1zm0-3a1 1 0 1 1 0 2 1 1 0 0 1 0-2z");
        path.setFill(Color.web(color));
        path.setStroke(Color.TRANSPARENT);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return path;
    }
    
    /** Ícono X - Para botón de cerrar notificaciones */
    public static javafx.scene.shape.SVGPath crearIconoX(String color, int size) {
        javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
        path.setContent("M18 6L6 18M6 6l12 12");
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Color.web(color));
        path.setStrokeWidth(2);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return path;
    }

    /** Ícono de apagado/power (Lucide 'Power') */
    public static Node power(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;
        
        // Línea vertical superior
        Line linea = new Line(12*s, 2*s, 12*s, 6*s);
        linea.setStroke(Color.web(color));
        linea.setStrokeWidth(2*s);
        linea.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Arco semicircular (icono power)
        Path arco = new Path();
        arco.getElements().addAll(
            new MoveTo(18.36*s, 6.64*s),
            new CubicCurveTo(19.6*s, 7.8*s, 20.4*s, 9.3*s, 20.74*s, 10.96*s),
            new CubicCurveTo(21.5*s, 14.5*s, 19.7*s, 18.06*s, 16.8*s, 19.96*s),
            new CubicCurveTo(13.9*s, 21.86*s, 10.1*s, 21.86*s, 7.2*s, 19.96*s),
            new CubicCurveTo(4.3*s, 18.06*s, 2.5*s, 14.5*s, 3.26*s, 10.96*s),
            new CubicCurveTo(3.6*s, 9.3*s, 4.4*s, 7.8*s, 5.64*s, 6.64*s)
        );
        arco.setFill(Color.TRANSPARENT);
        arco.setStroke(Color.web(color));
        arco.setStrokeWidth(2*s);
        arco.setStrokeLineCap(StrokeLineCap.ROUND);
        arco.setStrokeLineJoin(StrokeLineJoin.ROUND);
        
        g.getChildren().addAll(linea, arco);
        return g;
    }

    /** Ícono de bandera (Lucide 'Flag') - Para marcar fin de grupo */
    public static Node bandera(String color, double size) {
        Group g = new Group();
        double s = size / 24.0;

        // Mástil vertical
        Line mastil = new Line(4*s, 2*s, 4*s, 22*s);
        mastil.setStroke(Color.web(color));
        mastil.setStrokeWidth(2*s);
        mastil.setStrokeLineCap(StrokeLineCap.ROUND);

        // Bandera triangular/curvada
        Path flag = new Path();
        flag.getElements().addAll(
            new MoveTo(4*s, 2*s),
            new CubicCurveTo(8*s, 4*s, 12*s, 0*s, 16*s, 2*s),
            new LineTo(20*s, 4*s),
            new CubicCurveTo(16*s, 6*s, 12*s, 10*s, 8*s, 8*s),
            new LineTo(4*s, 15*s),
            new ClosePath()
        );
        flag.setFill(Color.web(color));
        flag.setStroke(Color.web(color));
        flag.setStrokeWidth(0.5*s);
        flag.setOpacity(0.85);

        g.getChildren().addAll(flag, mastil);
        return g;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ═══════════════════════════════════════════════════════════════════════

    /** Crear un StackPane con el ícono centrado */
    public static StackPane envolverIcono(Node icono, double size) {
        StackPane wrapper = new StackPane(icono);
        wrapper.setPrefSize(size, size);
        wrapper.setMinSize(size, size);
        wrapper.setMaxSize(size, size);
        return wrapper;
    }
}
