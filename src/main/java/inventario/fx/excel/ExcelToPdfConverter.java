package inventario.fx.excel;

import inventario.fx.util.AppLogger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.*;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Conversor de archivos Excel (.xlsx) a PDF.
 * Genera el PDF manteniendo el mismo diseóo del Excel,
 * ajustando todo el contenido a una sola página (equivalente a Ajustar hoja en una página de Excel).
 */
public class ExcelToPdfConverter {

    // Márgenes del PDF (en puntos)
    private static final float MARGIN_LEFT = 15;
    private static final float MARGIN_RIGHT = 15;
    private static final float MARGIN_TOP = 15;
    private static final float MARGIN_BOTTOM = 15;
    
    // Factor de escala para mayor resolución (calidad)
    private static final float SCALE_FACTOR = 2.0f;

    /**
     * Convierte un archivo Excel a PDF, ajustando el contenido a una sola página.
     * Equivalente a la opción Ajustar hoja en una página de Microsoft Excel.
     * 
     * @param excelPath Ruta del archivo Excel origen
     * @param pdfPath   Ruta del archivo PDF destino
     * @throws Exception Si ocurre algún error durante la conversión
     */
    public static void convertirExcelAPdf(String excelPath, String pdfPath) throws Exception {
        try (FileInputStream fis = new FileInputStream(excelPath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis);
             PDDocument document = new PDDocument()) {

            // Obtener la primera hoja (formato de mantenimiento)
            XSSFSheet sheet = workbook.getSheetAt(0);
            
            // Configurar la hoja para impresión en una página
            configurarImpresion(sheet);
            
            // Renderizar Excel a imagen para mantener el formato exacto
            BufferedImage excelImage = renderizarHojaExcel(workbook, sheet);
            
            // Crear página PDF con orientación vertical (carta)
            PDPage page = crearPaginaAjustada(excelImage);
            document.addPage(page);
            
            // Escribir la imagen en el PDF, ajustada a la página
            escribirImagenEnPdf(document, page, excelImage);
            
            // Guardar el PDF
            document.save(pdfPath);
            
            System.out.println("✓ PDF generado exitosamente: " + pdfPath);
        }
    }
    
    /**
     * Configura la hoja de Excel para impresión ajustada a una página.
     */
    private static void configurarImpresion(XSSFSheet sheet) {
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);  // Ajustar a 1 página de ancho
        ps.setFitHeight((short) 1); // Ajustar a 1 página de alto
        sheet.setFitToPage(true);
    }
    
    /**
     * Encuentra la última fila con datos en la hoja.
     */
    private static int encontrarUltimaFilaConDatos(XSSFSheet sheet) {
        int lastRow = 0;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        String value = obtenerTextocelda(cell);
                        if (value != null && !value.trim().isEmpty()) {
                            lastRow = i;
                        }
                    }
                }
            }
        }
        return lastRow;
    }
    
    /**
     * Encuentra la última columna con datos en la hoja.
     */
    private static int encontrarUltimaColumnaConDatos(XSSFSheet sheet) {
        int lastCol = 0;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getLastCellNum() > lastCol) {
                // Verificar que realmente hay datos en esas columnas
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        String value = obtenerTextocelda(cell);
                        if (value != null && !value.trim().isEmpty() && j > lastCol) {
                            lastCol = j;
                        }
                    }
                }
            }
        }
        return lastCol;
    }

    /**
     * Renderiza una hoja de Excel a una imagen BufferedImage con alta calidad.
     */
    private static BufferedImage renderizarHojaExcel(XSSFWorkbook workbook, XSSFSheet sheet) {
        // Determinar el área de impresión real
        int lastRow = encontrarUltimaFilaConDatos(sheet);
        int lastCol = encontrarUltimaColumnaConDatos(sheet);
        
        // Asegurar mínimos para el formato de mantenimiento (columnas A-N = 0-13)
        lastRow = Math.max(lastRow, 62);
        lastCol = Math.max(lastCol, 13); // Columnas A-N (0-13) para incluir todo el formato
        
        // Agregar columna extra para el borde derecho
        lastCol += 1;
        
        // Calcular anchos de columnas y alturas de filas
        float[] colWidths = new float[lastCol + 1];
        float totalWidth = 0;
        for (int i = 0; i <= lastCol; i++) {
            // Saltar columnas ocultas
            if (sheet.isColumnHidden(i)) {
                colWidths[i] = 0;
                continue;
            }
            float width = sheet.getColumnWidthInPixels(i);
            // Asegurar un ancho mínimo para cada columna
            if (width < 5) width = 5;
            colWidths[i] = width * SCALE_FACTOR;
            totalWidth += colWidths[i];
        }
        
        // Agregar padding extra al ancho total para el borde derecho
        totalWidth += 20 * SCALE_FACTOR;
        
        float[] rowHeights = new float[lastRow + 1];
        for (int i = 0; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            float height = (row != null) ? row.getHeightInPoints() : sheet.getDefaultRowHeightInPoints();
            rowHeights[i] = height * 1.33f * SCALE_FACTOR; // Puntos a píxeles
        }
        
        // Obtener regiones combinadas para manejo especial
        List<CellRangeAddress> mergedRegions = new ArrayList<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            mergedRegions.add(sheet.getMergedRegion(i));
        }
        
        // ═══════════════════════════════════════════════════════════════
        // PRE-CÁLCULO: Ajustar alturas de filas para celdas con mucho texto
        // ═══════════════════════════════════════════════════════════════
        ajustarAlturasParaTextoLargo(workbook, sheet, mergedRegions, colWidths, rowHeights, lastRow, lastCol);
        
        // Calcular altura total DESPUÉS del ajuste
        float totalHeight = 0;
        for (int i = 0; i <= lastRow; i++) {
            totalHeight += rowHeights[i];
        }
        
        // Agregar padding extra a la altura
        totalHeight += 20 * SCALE_FACTOR;
        
        // Padding para márgenes internos
        float paddingLeft = 5 * SCALE_FACTOR;
        float paddingTop = 5 * SCALE_FACTOR;
        
        // Crear imagen con dimensiones calculadas (agregar padding)
        int imgWidth = (int) (totalWidth + paddingLeft * 2);
        int imgHeight = (int) (totalHeight + paddingTop * 2);
        
        // Asegurar tamaño mínimo
        imgWidth = Math.max(imgWidth, (int)(800 * SCALE_FACTOR));
        imgHeight = Math.max(imgHeight, (int)(1000 * SCALE_FACTOR));
        
        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Configurar renderizado de alta calidad
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                             java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, 
                             java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, 
                             java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        
        // Fondo blanco
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, imgWidth, imgHeight);
        
        // Dibujar celdas (comenzar con padding)
        float y = paddingTop;
        for (int rowIdx = 0; rowIdx <= lastRow; rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            float x = paddingLeft;
            
            for (int colIdx = 0; colIdx <= lastCol; colIdx++) {
                // Saltar columnas ocultas
                if (sheet.isColumnHidden(colIdx)) {
                    continue;
                }
                
                Cell cell = (row != null) ? row.getCell(colIdx) : null;
                float cellWidth = colWidths[colIdx];
                float cellHeight = rowHeights[rowIdx];
                
                // Verificar si esta celda es parte de una región combinada
                CellRangeAddress mergedRegion = getMergedRegion(mergedRegions, rowIdx, colIdx);
                
                if (mergedRegion != null) {
                    // Solo dibujar si es la celda superior izquierda de la región
                    if (rowIdx == mergedRegion.getFirstRow() && colIdx == mergedRegion.getFirstColumn()) {
                        // Calcular dimensiones de la región combinada
                        float mergedWidth = 0;
                        for (int c = mergedRegion.getFirstColumn(); c <= mergedRegion.getLastColumn(); c++) {
                            mergedWidth += colWidths[c];
                        }
                        float mergedHeight = 0;
                        for (int r = mergedRegion.getFirstRow(); r <= mergedRegion.getLastRow(); r++) {
                            mergedHeight += rowHeights[r];
                        }
                        
                        dibujarCelda(g2d, workbook, cell, x, y, mergedWidth, mergedHeight);
                    }
                } else {
                    dibujarCelda(g2d, workbook, cell, x, y, cellWidth, cellHeight);
                }
                
                x += cellWidth;
            }
            y += rowHeights[rowIdx];
        }
        
        // Dibujar bordes completos de regiones combinadas para asegurar consistencia
        dibujarBordesRegionsesCombinadas(g2d, mergedRegions, colWidths, rowHeights, paddingLeft, paddingTop);
        
        // Dibujar imágenes del Excel (Drawing/Pictures)
        dibujarImagenesExcel(g2d, sheet, colWidths, rowHeights, paddingLeft, paddingTop);
        
        g2d.dispose();
        return image;
    }

    /**
     * Obtiene la región combinada que contiene una celda específica.
     */
    private static CellRangeAddress getMergedRegion(List<CellRangeAddress> regions, int row, int col) {
        for (CellRangeAddress region : regions) {
            if (region.isInRange(row, col)) {
                return region;
            }
        }
        return null;
    }

    /**
     * Dibuja una celda individual en el graphics context con bordes, estilos y texto multi-línea.
     */
    private static void dibujarCelda(Graphics2D g2d, XSSFWorkbook workbook, Cell cell, 
                                      float x, float y, float width, float height) {
        int ix = (int) x;
        int iy = (int) y;
        int iw = (int) width;
        int ih = (int) height;
        
        // Color de fondo
        Color bgColor = Color.WHITE;
        if (cell != null) {
            CellStyle style = cell.getCellStyle();
            if (style != null && style instanceof XSSFCellStyle) {
                XSSFCellStyle xStyle = (XSSFCellStyle) style;
                XSSFColor fillColor = xStyle.getFillForegroundXSSFColor();
                if (fillColor != null) {
                    byte[] rgb = fillColor.getRGB();
                    if (rgb != null && rgb.length >= 3) {
                        bgColor = new Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
                    }
                }
            }
        }
        
        // Dibujar fondo
        g2d.setColor(bgColor);
        g2d.fillRect(ix, iy, iw, ih);
        
        // Dibujar bordes según el estilo de la celda
        if (cell != null) {
            CellStyle style = cell.getCellStyle();
            if (style != null) {
                dibujarBordesCelda(g2d, style, ix, iy, iw, ih);
            } else {
                g2d.setColor(new Color(200, 200, 200));
                g2d.setStroke(new BasicStroke(0.5f * SCALE_FACTOR));
                g2d.drawRect(ix, iy, iw, ih);
            }
        } else {
            g2d.setColor(new Color(230, 230, 230));
            g2d.setStroke(new BasicStroke(0.5f * SCALE_FACTOR));
            g2d.drawRect(ix, iy, iw, ih);
        }
        
        // Dibujar texto (con soporte multi-línea)
        if (cell != null) {
            String texto = obtenerTextocelda(cell);
            if (texto != null && !texto.isEmpty()) {
                // Configurar fuente con escala
                java.awt.Font font = obtenerFuenteJava(workbook, cell);
                java.awt.Font scaledFont = font.deriveFont(font.getSize() * SCALE_FACTOR);
                g2d.setFont(scaledFont);
                
                // Color del texto
                Color textColor = Color.BLACK;
                CellStyle style = cell.getCellStyle();
                if (style != null && style instanceof XSSFCellStyle) {
                    XSSFCellStyle xStyle = (XSSFCellStyle) style;
                    XSSFColor fontColor = xStyle.getFont().getXSSFColor();
                    if (fontColor != null) {
                        byte[] rgb = fontColor.getRGB();
                        if (rgb != null && rgb.length >= 3) {
                            textColor = new Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
                        }
                    }
                }
                g2d.setColor(textColor);
                
                java.awt.FontMetrics fm = g2d.getFontMetrics();
                int padding = (int)(3 * SCALE_FACTOR);
                int maxTextWidth = iw - (padding * 2);
                
                // Determinar si la celda necesita texto multi-línea
                boolean wrapText = (style != null && style.getWrapText());
                boolean hasNewlines = texto.contains("\n");
                boolean textOverflows = fm.stringWidth(texto) > maxTextWidth;
                
                if ((wrapText || hasNewlines) && (hasNewlines || textOverflows)) {
                    // ═══ MODO MULTI-LÍNEA ═══
                    dibujarTextoMultilinea(g2d, texto, ix, iy, iw, ih, padding, fm, style);
                } else {
                    // ═══ MODO LÍNEA SIMPLE ═══
                    int textWidth = fm.stringWidth(texto);
                    
                    // Alineación horizontal
                    int textX = ix + padding;
                    if (style != null) {
                        HorizontalAlignment align = style.getAlignment();
                        if (align == HorizontalAlignment.CENTER) {
                            textX = ix + (iw - textWidth) / 2;
                        } else if (align == HorizontalAlignment.RIGHT) {
                            textX = ix + iw - textWidth - padding;
                        }
                    }
                    
                    // Centrar verticalmente
                    int textY = iy + (ih + fm.getAscent() - fm.getDescent()) / 2;
                    
                    // Recortar texto solo si no cabe y no tiene wrapText
                    if (textWidth > maxTextWidth) {
                        StringBuilder sb = new StringBuilder();
                        for (char c : texto.toCharArray()) {
                            if (fm.stringWidth(sb.toString() + c + "...") < maxTextWidth) {
                                sb.append(c);
                            } else {
                                break;
                            }
                        }
                        texto = sb.toString() + (sb.length() < texto.length() ? "..." : "");
                        textX = ix + padding;
                    }
                    
                    g2d.drawString(texto, textX, textY);
                }
            }
        }
    }
    
    /**
     * Dibuja texto con soporte multi-línea dentro de una celda.
     * Respeta saltos de línea (\n) y hace word-wrap automático.
     */
    private static void dibujarTextoMultilinea(Graphics2D g2d, String texto, 
            int ix, int iy, int iw, int ih, int padding, java.awt.FontMetrics fm, CellStyle style) {
        
        int maxTextWidth = iw - (padding * 2);
        int lineHeight = fm.getHeight();
        
        // Dividir el texto en líneas respetando \n y haciendo word-wrap
        List<String> lineas = dividirTextoEnLineas(texto, fm, maxTextWidth);
        
        int totalTextHeight = lineas.size() * lineHeight;
        
        // Calcular posición Y inicial según alineación vertical
        int startY;
        VerticalAlignment vAlign = (style != null) ? style.getVerticalAlignment() : VerticalAlignment.CENTER;
        
        switch (vAlign) {
            case TOP:
                startY = iy + padding + fm.getAscent();
                break;
            case BOTTOM:
                startY = iy + ih - totalTextHeight - padding + fm.getAscent();
                break;
            case CENTER:
            default:
                startY = iy + (ih - totalTextHeight) / 2 + fm.getAscent();
                break;
        }
        
        // Alineación horizontal
        HorizontalAlignment hAlign = (style != null) ? style.getAlignment() : HorizontalAlignment.LEFT;
        
        // Dibujar cada línea
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            int lineY = startY + (i * lineHeight);
            
            // No dibujar líneas que se salen por abajo de la celda
            if (lineY > iy + ih - padding) break;
            
            int lineWidth = fm.stringWidth(linea);
            int textX;
            
            switch (hAlign) {
                case CENTER:
                    textX = ix + (iw - lineWidth) / 2;
                    break;
                case RIGHT:
                    textX = ix + iw - lineWidth - padding;
                    break;
                case LEFT:
                case GENERAL:
                default:
                    textX = ix + padding;
                    break;
            }
            
            g2d.drawString(linea, textX, lineY);
        }
    }
    
    /**
     * Divide un texto en líneas que caben dentro del ancho especificado,
     * respetando saltos de línea explícitos (\n) y haciendo word-wrap.
     */
    private static List<String> dividirTextoEnLineas(String texto, java.awt.FontMetrics fm, int maxWidth) {
        List<String> result = new ArrayList<>();
        
        // Primero dividir por saltos de línea explícitos
        String[] parrafos = texto.split("\n", -1);
        
        for (String parrafo : parrafos) {
            if (parrafo.trim().isEmpty()) {
                result.add("");
                continue;
            }
            
            // Word-wrap cada párrafo
            String[] palabras = parrafo.split("(?<=\\s)|(?=\\s)"); // Preservar espacios
            StringBuilder lineaActual = new StringBuilder();
            
            for (String palabra : palabras) {
                String prueba = lineaActual.toString() + palabra;
                if (fm.stringWidth(prueba) <= maxWidth || lineaActual.length() == 0) {
                    lineaActual.append(palabra);
                } else {
                    // La línea actual está llena, guardarla
                    result.add(lineaActual.toString().trim());
                    lineaActual = new StringBuilder(palabra.trim());
                }
            }
            
            // Agregar última línea del párrafo
            if (lineaActual.length() > 0) {
                result.add(lineaActual.toString().trim());
            }
        }
        
        return result;
    }
    
    /**
     * Pre-calcula y ajusta las alturas de las filas para acomodar celdas con mucho texto.
     * Escanea celdas con wrapText o texto largo y expande las filas necesarias.
     */
    private static void ajustarAlturasParaTextoLargo(XSSFWorkbook workbook, XSSFSheet sheet, 
            List<CellRangeAddress> mergedRegions, float[] colWidths, float[] rowHeights, 
            int lastRow, int lastCol) {
        
        // Crear un Graphics2D temporal para medir texto
        BufferedImage tmpImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D tmpG2d = tmpImg.createGraphics();
        tmpG2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, 
                                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        // Escanear celdas en regiones combinadas (donde suele haber más texto)
        for (CellRangeAddress region : mergedRegions) {
            int firstRow = region.getFirstRow();
            int firstCol = region.getFirstColumn();
            
            Row row = sheet.getRow(firstRow);
            if (row == null) continue;
            Cell cell = row.getCell(firstCol);
            if (cell == null) continue;
            
            String texto = obtenerTextocelda(cell);
            if (texto == null || texto.isEmpty()) continue;
            
            CellStyle style = cell.getCellStyle();
            boolean wrapText = (style != null && style.getWrapText());
            boolean hasNewlines = texto.contains("\n");
            
            if (!wrapText && !hasNewlines) continue;
            
            // Calcular ancho disponible de la región combinada
            float mergedWidth = 0;
            for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                if (c < colWidths.length) mergedWidth += colWidths[c];
            }
            int padding = (int)(3 * SCALE_FACTOR);
            int maxTextWidth = (int)(mergedWidth - (padding * 2));
            if (maxTextWidth <= 0) continue;
            
            // Configurar fuente para medición
            java.awt.Font font = obtenerFuenteJava(workbook, cell);
            java.awt.Font scaledFont = font.deriveFont(font.getSize() * SCALE_FACTOR);
            tmpG2d.setFont(scaledFont);
            java.awt.FontMetrics fm = tmpG2d.getFontMetrics();
            
            // Calcular cuántas líneas necesita
            List<String> lineas = dividirTextoEnLineas(texto, fm, maxTextWidth);
            int lineHeight = fm.getHeight();
            float neededHeight = lineas.size() * lineHeight + (padding * 2);
            
            // Calcular altura actual de la región combinada
            float currentRegionHeight = 0;
            for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
                if (r < rowHeights.length) currentRegionHeight += rowHeights[r];
            }
            
            // Si necesita más espacio, distribuir la diferencia entre las filas de la región
            if (neededHeight > currentRegionHeight) {
                float extraHeight = neededHeight - currentRegionHeight;
                int numRows = region.getLastRow() - region.getFirstRow() + 1;
                float extraPerRow = extraHeight / numRows;
                
                for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
                    if (r < rowHeights.length) {
                        rowHeights[r] += extraPerRow;
                    }
                }
                
                System.out.println("[PDF] Ajustadas filas " + (region.getFirstRow()+1) + "-" + 
                    (region.getLastRow()+1) + " para acomodar " + lineas.size() + " líneas de texto" +
                    " (+" + (int)extraHeight + "px)");
            }
        }
        
        // También escanear celdas individuales (no combinadas) con mucho texto
        for (int rowIdx = 0; rowIdx <= lastRow; rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null) continue;
            
            for (int colIdx = 0; colIdx <= lastCol; colIdx++) {
                // Saltar si está en región combinada
                if (getMergedRegion(mergedRegions, rowIdx, colIdx) != null) continue;
                
                Cell cell = row.getCell(colIdx);
                if (cell == null) continue;
                
                String texto = obtenerTextocelda(cell);
                if (texto == null || texto.isEmpty()) continue;
                
                CellStyle style = cell.getCellStyle();
                boolean wrapText = (style != null && style.getWrapText());
                boolean hasNewlines = texto.contains("\n");
                
                if (!wrapText && !hasNewlines) continue;
                
                float cellWidth = (colIdx < colWidths.length) ? colWidths[colIdx] : 0;
                int padding = (int)(3 * SCALE_FACTOR);
                int maxTextWidth = (int)(cellWidth - (padding * 2));
                if (maxTextWidth <= 0) continue;
                
                java.awt.Font font = obtenerFuenteJava(workbook, cell);
                java.awt.Font scaledFont = font.deriveFont(font.getSize() * SCALE_FACTOR);
                tmpG2d.setFont(scaledFont);
                java.awt.FontMetrics fm = tmpG2d.getFontMetrics();
                
                List<String> lineas = dividirTextoEnLineas(texto, fm, maxTextWidth);
                int lineHeight = fm.getHeight();
                float neededHeight = lineas.size() * lineHeight + (padding * 2);
                
                if (neededHeight > rowHeights[rowIdx]) {
                    rowHeights[rowIdx] = neededHeight;
                }
            }
        }
        
        tmpG2d.dispose();
    }
    
    /**
     * Dibuja los bordes de una celda según su estilo.
     */
    private static void dibujarBordesCelda(Graphics2D g2d, CellStyle style, int x, int y, int w, int h) {
        // Color y grosor consistentes para todos los bordes
        Color borderColor = new Color(128, 128, 128); // Gris medio consistente
        Color defaultBorderColor = new Color(210, 210, 210); // Más sutil para bordes por defecto
        float grosoBordeUniforme = 1.0f * SCALE_FACTOR; // Grosor uniforme para todos
        
        boolean hasTopBorder = style.getBorderTop() != BorderStyle.NONE;
        boolean hasBottomBorder = style.getBorderBottom() != BorderStyle.NONE;
        boolean hasLeftBorder = style.getBorderLeft() != BorderStyle.NONE;
        boolean hasRightBorder = style.getBorderRight() != BorderStyle.NONE;
        
        // Borde superior
        if (hasTopBorder) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(grosoBordeUniforme));
            g2d.drawLine(x, y, x + w, y);
        }
        
        // Borde inferior
        if (hasBottomBorder) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(grosoBordeUniforme));
            g2d.drawLine(x, y + h, x + w, y + h);
        }
        
        // Borde izquierdo
        if (hasLeftBorder) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(grosoBordeUniforme));
            g2d.drawLine(x, y, x, y + h);
        }
        
        // Borde derecho
        if (hasRightBorder) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(grosoBordeUniforme));
            g2d.drawLine(x + w, y, x + w, y + h);
        }
        
        // Si no tiene ningún borde definido, dibujar un borde completo muy sutil
        if (!hasTopBorder && !hasBottomBorder && !hasLeftBorder && !hasRightBorder) {
            g2d.setColor(defaultBorderColor);
            g2d.setStroke(new BasicStroke(0.5f * SCALE_FACTOR));
            g2d.drawRect(x, y, w, h);
        }
        
        // Restaurar stroke por defecto
        g2d.setStroke(new BasicStroke(1.0f));
    }
    
    /**
     * Verifica si una región combinada necesita borde derecho especial.
     * Basado en las regiones del GeneratedTemplate.java que terminan en columna M (12).
     * 
     * Regiones que necesitan borde derecho (última columna de cada región):
     * - Filas 9-14 (rows 10-15 Excel): combinaciones que van hasta col 12
     * - Fila 15-16 (row 16-17 Excel): B16:M17 hasta col 12
     * - Filas 17-21 (rows 18-22): varias combinaciones hasta col 12
     * - Y muchas más...
     */
    private static boolean necesitaBordeDerecho(int firstRow, int lastCol) {
        // Columnas: A=0, B=1, C=2, D=3, E=4, F=5, G=6, H=7, I=8, J=9, K=10, L=11, M=12
        
        // Todas las celdas combinadas que terminan en columna M (12) necesitan borde derecho
        if (lastCol == 12) {
            // Filas 9-14 (D10:M10 hasta D15:M15)
            if (firstRow >= 9 && firstRow <= 14) return true;
            
            // Fila 15-16 (B16:M17)
            if (firstRow == 15) return true;
            
            // Filas 17-21: varias combinaciones hasta M
            if (firstRow >= 17 && firstRow <= 21) return true;
            
            // Fila 22 (B23:M23)
            if (firstRow == 22) return true;
            
            // Filas 23-25 (rows 24-26): combinaciones hasta M
            if (firstRow >= 23 && firstRow <= 25) return true;
            
            // Fila 26 (M27)
            if (firstRow == 26) return true;
            
            // Fila 27 (B28:M28)
            if (firstRow == 27) return true;
            
            // Filas 28-30 (rows 29-31): combinaciones hasta M
            if (firstRow >= 28 && firstRow <= 30) return true;
            
            // Fila 31 (B32:M32)
            if (firstRow == 31) return true;
            
            // Filas 32-34 (rows 33-35): combinaciones hasta M
            if (firstRow >= 32 && firstRow <= 34) return true;
            
            // Fila 35 (B36:M36)
            if (firstRow == 35) return true;
            
            // Filas 36-39 (rows 37-40): combinaciones hasta M
            if (firstRow >= 36 && firstRow <= 39) return true;
            
            // Fila 40 (B41:M41)
            if (firstRow == 40) return true;
            
            // Filas 41-45 (B42:M46)
            if (firstRow == 41) return true;
            
            // Fila 46 (B47:M47)
            if (firstRow == 46) return true;
            
            // Filas 47-51 (B48:M52)
            if (firstRow == 47) return true;
            
            // Filas 52, 57 (rows 53, 58)
            if (firstRow == 52 || firstRow == 57) return true;
            
            // Filas 58-59 (rows 59-60)
            if (firstRow >= 58 && firstRow <= 59) return true;
        }
        
        // Tambión verificar combinaciones específicas que terminan en otras columnas
        // según la lista original del usuario
        
        return false;
    }
    
    /**
     * Dibuja los bordes derechos de las celdas combinadas específicas.
     */
    private static void dibujarBordesDerechosCeldasCombinadas(Graphics2D g2d, XSSFSheet sheet, 
            List<CellRangeAddress> mergedRegions, float[] colWidths, float[] rowHeights, 
            float paddingLeft, float paddingTop, int lastRow, int lastCol) {
        
        Color borderColor = Color.BLACK;
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1.0f * SCALE_FACTOR));
        
        // Iterar sobre todas las regiones combinadas
        for (CellRangeAddress region : mergedRegions) {
            int firstRow = region.getFirstRow();
            int firstCol = region.getFirstColumn();
            int lastRegionCol = region.getLastColumn();
            int lastRegionRow = region.getLastRow();
            
            // Verificar si esta región combinada necesita borde derecho
            // Usamos lastRegionCol porque el usuario especificó la última columna de cada combinación
            if (necesitaBordeDerecho(firstRow, lastRegionCol)) {
                // Calcular posición X del borde derecho
                float x = paddingLeft;
                for (int c = 0; c <= lastRegionCol; c++) {
                    x += colWidths[c];
                }
                
                // Calcular posición Y inicial
                float yStart = paddingTop;
                for (int r = 0; r < firstRow; r++) {
                    yStart += rowHeights[r];
                }
                
                // Calcular altura total de la región combinada
                float regionHeight = 0;
                for (int r = firstRow; r <= lastRegionRow; r++) {
                    regionHeight += rowHeights[r];
                }
                
                // Dibujar línea del borde derecho
                g2d.drawLine((int)x, (int)yStart, (int)x, (int)(yStart + regionHeight));
            }
        }
    }
    
    /**
     * Verifica si una región combinada necesita borde inferior especial.
     * Celdas combinadas: G53 (fila 52), B18, D18, F18 (fila 17)
     */
    private static boolean necesitaBordeInferior(int firstRow, int lastRow, int firstCol, int lastCol) {
        // Columnas: A=0, B=1, C=2, D=3, E=4, F=5, G=6, H=7
        // Filas: índice 0-based
        
        // B18 (fila 17, col 1) - combinación B18:C19 = (17,18,1,2) -> lastRow=18, lastCol=2
        if (lastRow == 18 && lastCol == 2) return true;
        
        // D18 (fila 17, col 3) - combinación D18:E19 = (17,18,3,4) -> lastRow=18, lastCol=4
        if (lastRow == 18 && lastCol == 4) return true;
        
        // F18 (fila 17, col 5) - combinación F18:G19 = (17,18,5,6) -> lastRow=18, lastCol=6
        if (lastRow == 18 && lastCol == 6) return true;
        
        // H18 (fila 17, col 7) - combinación H18:I19 = (17,18,7,8) -> lastRow=18, lastCol=8
        if (lastRow == 18 && lastCol == 8) return true;
        
        // G53 (fila 52, col 6) - combinación G53:H60 = (52,59,6,7) -> lastRow=59, lastCol=7
        // Pero el borde inferior debe estar en la fila 52, no en la 59
        // Buscar combinación que empiece en fila 52 y columna 6
        if (firstRow == 52 && firstCol == 6) return true;
        
        return false;
    }
    
    /**
     * Dibuja los bordes inferiores de las celdas combinadas específicas.
     */
    private static void dibujarBordesInferioresCeldasCombinadas(Graphics2D g2d, XSSFSheet sheet, 
            List<CellRangeAddress> mergedRegions, float[] colWidths, float[] rowHeights, 
            float paddingLeft, float paddingTop, int lastRow, int lastCol) {
        
        Color borderColor = Color.BLACK;
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1.0f * SCALE_FACTOR));
        
        // Iterar sobre todas las regiones combinadas
        for (CellRangeAddress region : mergedRegions) {
            int firstRow = region.getFirstRow();
            int firstCol = region.getFirstColumn();
            int lastRegionCol = region.getLastColumn();
            int lastRegionRow = region.getLastRow();
            
            // Verificar si esta región combinada necesita borde inferior
            if (necesitaBordeInferior(firstRow, lastRegionRow, firstCol, lastRegionCol)) {
                // Calcular posición X inicial
                float xStart = paddingLeft;
                for (int c = 0; c < firstCol; c++) {
                    xStart += colWidths[c];
                }
                
                // Calcular ancho total de la región combinada
                float regionWidth = 0;
                for (int c = firstCol; c <= lastRegionCol; c++) {
                    regionWidth += colWidths[c];
                }
                
                // Calcular posición Y del borde inferior
                float y = paddingTop;
                for (int r = 0; r <= lastRegionRow; r++) {
                    y += rowHeights[r];
                }
                
                // Dibujar línea del borde inferior
                g2d.drawLine((int)xStart, (int)y, (int)(xStart + regionWidth), (int)y);
            }
        }
    }
    
    /**
     * Dibuja bordes completos para todas las regiones combinadas.
     */
    private static void dibujarBordesRegionsesCombinadas(Graphics2D g2d, List<CellRangeAddress> mergedRegions,
            float[] colWidths, float[] rowHeights, float paddingLeft, float paddingTop) {
        
        Color borderColor = new Color(128, 128, 128);
        float grosorBorde = 1.0f * SCALE_FACTOR;
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(grosorBorde));
        
        for (CellRangeAddress region : mergedRegions) {
            // Calcular posición X inicial
            float x = paddingLeft;
            for (int c = 0; c < region.getFirstColumn(); c++) {
                x += colWidths[c];
            }
            
            // Calcular posición Y inicial
            float y = paddingTop;
            for (int r = 0; r < region.getFirstRow(); r++) {
                y += rowHeights[r];
            }
            
            // Calcular ancho de la región
            float width = 0;
            for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                width += colWidths[c];
            }
            
            // Calcular alto de la región
            float height = 0;
            for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
                height += rowHeights[r];
            }
            
            // Dibujar rectángulo completo de la región combinada
            g2d.drawRect((int)x, (int)y, (int)width, (int)height);
        }
    }
    
    /**
     * Dibuja las imágenes del Excel (Drawing/Pictures) en el BufferedImage.
     */
    private static void dibujarImagenesExcel(Graphics2D g2d, XSSFSheet sheet, float[] colWidths, float[] rowHeights, 
            float paddingLeft, float paddingTop) {
        try {
            // Deshabilitar caché de ImageIO para asegurar que se lean imágenes actualizadas
            javax.imageio.ImageIO.setUseCache(false);
            
            // Obtener el drawing patriarch (contiene todas las imágenes)
            XSSFDrawing drawing = sheet.getDrawingPatriarch();
            if (drawing == null) {
                System.out.println("[PDF] No se encontraron imágenes en el Excel");
                return;
            }
            
            // Iterar sobre todas las formas/imágenes
            for (XSSFShape shape : drawing.getShapes()) {
                if (shape instanceof XSSFPicture) {
                    XSSFPicture picture = (XSSFPicture) shape;
                    XSSFClientAnchor anchor = (XSSFClientAnchor) picture.getAnchor();
                    
                    // Obtener los datos de la imagen
                    XSSFPictureData pictureData = picture.getPictureData();
                    byte[] imageBytes = pictureData.getData();
                    
                    // Convertir bytes a BufferedImage
                    java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(imageBytes);
                    java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(bis);
                    
                    if (img != null) {
                        // Calcular posición en píxeles
                        float x = paddingLeft;
                        for (int c = 0; c < anchor.getCol1(); c++) {
                            x += colWidths[c];
                        }
                        x += (anchor.getDx1() / (float) Units.EMU_PER_PIXEL) * SCALE_FACTOR;
                        
                        float y = paddingTop;
                        for (int r = 0; r < anchor.getRow1(); r++) {
                            y += rowHeights[r];
                        }
                        y += (anchor.getDy1() / (float) Units.EMU_PER_PIXEL) * SCALE_FACTOR;
                        
                        // Calcular dimensiones finales
                        float width = 0;
                        for (int c = anchor.getCol1(); c < anchor.getCol2(); c++) {
                            width += colWidths[c];
                        }
                        width += (anchor.getDx2() / (float) Units.EMU_PER_PIXEL) * SCALE_FACTOR;
                        width -= (anchor.getDx1() / (float) Units.EMU_PER_PIXEL) * SCALE_FACTOR;
                        
                        float height = 0;
                        for (int r = anchor.getRow1(); r < anchor.getRow2(); r++) {
                            height += rowHeights[r];
                        }
                        height += (anchor.getDy2() / (float) Units.EMU_PER_PIXEL) * SCALE_FACTOR;
                        height -= (anchor.getDy1() / (float) Units.EMU_PER_PIXEL) * SCALE_FACTOR;
                        
                        // Dibujar la imagen con mejor calidad
                        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                                           java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        g2d.drawImage(img, (int)x, (int)y, (int)width, (int)height, null);
                        
                        System.out.println("✓ Imagen renderizada en PDF: " + 
                            (int)x + "," + (int)y + " " + (int)width + "x" + (int)height + " (" +
                            img.getWidth() + "x" + img.getHeight() + "px originales)");
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.getLogger(ExcelToPdfConverter.class).error("Error al renderizar imágenes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convierte índice de color a Color de Java.
     */
    private static Color obtenerColorBorde(short colorIndex, Color defaultColor) {
        if (colorIndex == IndexedColors.AUTOMATIC.getIndex() || colorIndex == 0) {
            return defaultColor;
        }
        IndexedColors ic = IndexedColors.fromInt(colorIndex);
        if (ic != null) {
            // Intentar mapear colores conocidos
            switch (ic) {
                case BLACK: return Color.BLACK;
                case WHITE: return Color.WHITE;
                case RED: return Color.RED;
                case BLUE: return Color.BLUE;
                case GREEN: return Color.GREEN;
                case GREY_50_PERCENT: return Color.GRAY;
                case GREY_25_PERCENT: return new Color(192, 192, 192);
                default: return defaultColor;
            }
        }
        return defaultColor;
    }
    
    /**
     * Obtiene el grosor del borde según el estilo.
     */
    private static float obtenerGrosoBorde(BorderStyle borderStyle) {
        // Usar grosor consistente para todos los bordes
        switch (borderStyle) {
            case THIN: return 1.0f;
            case MEDIUM: return 1.0f;  // Cambiado de 1.5f a 1.0f para consistencia
            case THICK: return 1.0f;   // Cambiado de 2.0f a 1.0f para consistencia
            case DOUBLE: return 1.0f;  // Cambiado de 2.0f a 1.0f para consistencia
            case HAIR: return 0.8f;    // Cambiado de 0.5f a 0.8f para mejor visibilidad
            default: return 1.0f;
        }
    }

    /**
     * Obtiene el texto de una celda.
     */
    private static String obtenerTextocelda(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val)) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    /**
     * Convierte la fuente de Excel a fuente de Java AWT.
     */
    private static java.awt.Font obtenerFuenteJava(XSSFWorkbook workbook, Cell cell) {
        if (cell == null) {
            return new java.awt.Font("Arial", java.awt.Font.PLAIN, 10);
        }
        
        try {
            CellStyle style = cell.getCellStyle();
            if (style != null) {
                Font excelFont = workbook.getFontAt(style.getFontIndex());
                int javaStyle = java.awt.Font.PLAIN;
                if (excelFont.getBold()) javaStyle |= java.awt.Font.BOLD;
                if (excelFont.getItalic()) javaStyle |= java.awt.Font.ITALIC;
                
                String fontName = excelFont.getFontName();
                if (fontName == null || fontName.isEmpty()) fontName = "Arial";
                
                int fontSize = (int) (excelFont.getFontHeightInPoints());
                if (fontSize < 6) fontSize = 10;
                
                return new java.awt.Font(fontName, javaStyle, fontSize);
            }
        } catch (Exception e) {
            // Ignorar errores de fuente
        }
        
        return new java.awt.Font("Arial", java.awt.Font.PLAIN, 10);
    }

    /**
     * Crea una página PDF con el tamaóo ajustado para contener la imagen.
     */
    private static PDPage crearPaginaAjustada(BufferedImage image) {
        // Usar tamaóo carta en orientación retrato o paisaje según las proporciones
        float pageWidth, pageHeight;
        
        if (image.getWidth() > image.getHeight()) {
            // Orientación horizontal (landscape)
            pageWidth = PDRectangle.LETTER.getHeight();
            pageHeight = PDRectangle.LETTER.getWidth();
        } else {
            // Orientación vertical (portrait)
            pageWidth = PDRectangle.LETTER.getWidth();
            pageHeight = PDRectangle.LETTER.getHeight();
        }
        
        PDRectangle pageSize = new PDRectangle(pageWidth, pageHeight);
        return new PDPage(pageSize);
    }

    /**
     * Escribe la imagen del Excel en la página PDF, ajustándola para que quepa completa.
     */
    private static void escribirImagenEnPdf(PDDocument document, PDPage page, BufferedImage image) 
            throws IOException {
        
        PDRectangle pageSize = page.getMediaBox();
        float availableWidth = pageSize.getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
        float availableHeight = pageSize.getHeight() - MARGIN_TOP - MARGIN_BOTTOM;
        
        // Calcular escala para ajustar a la página
        float scaleX = availableWidth / image.getWidth();
        float scaleY = availableHeight / image.getHeight();
        float scale = Math.min(scaleX, scaleY); // Usar la escala menor para que quepa todo
        
        float scaledWidth = image.getWidth() * scale;
        float scaledHeight = image.getHeight() * scale;
        
        // Centrar la imagen en la página
        float x = MARGIN_LEFT + (availableWidth - scaledWidth) / 2;
        float y = MARGIN_BOTTOM + (availableHeight - scaledHeight) / 2;
        
        // Convertir BufferedImage a formato que PDFBox pueda usar
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageBytes, "excel_render");
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.drawImage(pdImage, x, y, scaledWidth, scaledHeight);
        }
    }

    /**
     * Mótodo alternativo que usa la API de renderizado nativo de POI (si está disponible).
     * Este mótodo intenta usar las capacidades de POI para renderizar a PDF de forma más fiel.
     */
    public static void convertirExcelAPdfAlternativo(String excelPath, String pdfPath) throws Exception {
        // Por ahora, usar el mótodo principal
        convertirExcelAPdf(excelPath, pdfPath);
    }
}
