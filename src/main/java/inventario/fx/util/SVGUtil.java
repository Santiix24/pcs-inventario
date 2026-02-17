package inventario.fx.util;

import inventario.fx.util.AppLogger;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;

/**
 * Utilidad para cargar y renderizar archivos SVG en JavaFX usando svgSalamander.
 * Permite escalar SVG sin pérdida de calidad y mantener la proporción.
 * 
 * @author SELCOMP
 * @version 1.0
 */
public class SVGUtil {

    private static final SVGUniverse svgUniverse = new SVGUniverse();

    /**
     * Carga un archivo SVG desde los recursos y crea un ImageView escalable.
     * La imagen se escalará manteniendo la proporción y conservando la calidad.
     *
     * @param resourcePath Ruta del recurso SVG (ej: "/icons/LogoSelcompSVG.svg")
     * @param width Ancho deseado en píxeles
     * @param height Alto deseado en píxeles
     * @return ImageView con el SVG renderizado, o null si hay error
     */
    public static ImageView loadSVG(String resourcePath, double width, double height) {
        try {
            // Cargar el SVG desde los recursos
            InputStream svgStream = SVGUtil.class.getResourceAsStream(resourcePath);
            if (svgStream == null) {
                System.err.println("❌ No se encontró el archivo SVG: " + resourcePath);
                return null;
            }

            // Crear URI único para el SVG
            URI uri = svgUniverse.loadSVG(svgStream, "svg_" + System.currentTimeMillis());
            SVGDiagram diagram = svgUniverse.getDiagram(uri);
            
            if (diagram == null) {
                System.err.println("❌ No se pudo cargar el diagrama SVG: " + resourcePath);
                return null;
            }

            // Obtener dimensiones originales del SVG
            float svgWidth = diagram.getWidth();
            float svgHeight = diagram.getHeight();

            // Calcular escala manteniendo proporción
            double scale = Math.min(width / svgWidth, height / svgHeight);
            int scaledWidth = (int) (svgWidth * scale);
            int scaledHeight = (int) (svgHeight * scale);

            // Renderizar SVG en BufferedImage con antialiasing
            BufferedImage bufferedImage = new BufferedImage(
                scaledWidth, 
                scaledHeight, 
                BufferedImage.TYPE_INT_ARGB
            );
            
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.scale(scale, scale);
            
            diagram.render(g2d);
            g2d.dispose();

            // Convertir a JavaFX Image
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            
            // Crear ImageView y configurar para que sea responsive
            ImageView imageView = new ImageView(fxImage);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);

            return imageView;

        } catch (Exception e) {
            AppLogger.getLogger(SVGUtil.class).error("❌ Error al cargar SVG: " + resourcePath, e);
            return null;
        }
    }

    /**
     * Carga un SVG y lo escala automáticamente al ancho especificado,
     * manteniendo la proporción original.
     *
     * @param resourcePath Ruta del recurso SVG
     * @param width Ancho deseado
     * @return ImageView con el SVG renderizado
     */
    public static ImageView loadSVG(String resourcePath, double width) {
        try {
            InputStream svgStream = SVGUtil.class.getResourceAsStream(resourcePath);
            if (svgStream == null) {
                return null;
            }

            URI uri = svgUniverse.loadSVG(svgStream, "svg_" + System.currentTimeMillis());
            SVGDiagram diagram = svgUniverse.getDiagram(uri);
            
            if (diagram == null) {
                return null;
            }

            float svgWidth = diagram.getWidth();
            float svgHeight = diagram.getHeight();
            double aspectRatio = svgHeight / svgWidth;
            double height = width * aspectRatio;

            return loadSVG(resourcePath, width, height);

        } catch (Exception e) {
            AppLogger.getLogger(SVGUtil.class).error("❌ Error al cargar SVG: " + resourcePath, e);
            return null;
        }
    }

    /**
     * Limpia el universo SVG para liberar memoria.
     * Usar cuando ya no se necesiten los SVG cargados.
     */
    public static void clear() {
        svgUniverse.clear();
    }
}
