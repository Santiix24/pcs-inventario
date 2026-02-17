package inventario.fx.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Clase auxiliar con métodos para leer, escribir y manipular celdas de Excel.
 * Separada de GeneratedTemplate para mejor organización del código.
 */
public class ExcelCeldaHelper {

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS PARA ESCRIBIR EN CELDAS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Escribe un valor String en una celda de la hoja de Excel.
     * Maneja automáticamente las celdas combinadas.
     * 
     * @param sh    Hoja de Excel
     * @param celda Referencia de celda (ej: "A1", "D10", "K59")
     * @param valor Valor a escribir
     */
    public static void escribirCelda(Sheet sh, String celda, String valor) {
        if (valor == null || valor.isEmpty()) return;
        try {
            int[] coords = parsearCelda(celda);
            int rowNum = coords[0];
            int colNum = coords[1];
            
            // Buscar si está en región combinada
            int[] finalCoords = buscarCeldaCombinada(sh, rowNum, colNum);
            int finalRow = finalCoords[0];
            int finalCol = finalCoords[1];
            
            Row r = sh.getRow(finalRow);
            if (r == null) r = sh.createRow(finalRow);
            Cell c = r.getCell(finalCol);
            if (c == null) c = r.createCell(finalCol);
            c.setCellValue(valor);
        } catch (Exception e) {
            System.err.println("Error escribiendo celda " + celda + ": " + e.getMessage());
        }
    }

    /**
     * Escribe un valor String en una celda con ajuste de texto (wrap text).
     * Útil para celdas con texto largo que debe ajustarse dentro del área.
     * Incluye bordes y ajusta la altura de las filas automáticamente.
     * 
     * @param sh    Hoja de Excel
     * @param celda Referencia de celda (ej: "A1", "B42")
     * @param valor Valor a escribir
     */
    public static void escribirCeldaConAjuste(Sheet sh, String celda, String valor) {
        if (valor == null || valor.isEmpty()) return;
        try {
            int[] coords = parsearCelda(celda);
            int rowNum = coords[0];
            int colNum = coords[1];
            
            // Buscar si está en región combinada y obtener sus límites
            int[] finalCoords = buscarCeldaCombinada(sh, rowNum, colNum);
            int finalRow = finalCoords[0];
            int finalCol = finalCoords[1];
            
            // Obtener los límites de la región combinada
            int[] regionLimits = obtenerLimitesRegionCombinada(sh, rowNum, colNum);
            int firstRow = regionLimits[0];
            int lastRow = regionLimits[1];
            int firstCol = regionLimits[2];
            int lastCol = regionLimits[3];
            
            Row r = sh.getRow(finalRow);
            if (r == null) r = sh.createRow(finalRow);
            Cell c = r.getCell(finalCol);
            if (c == null) c = r.createCell(finalCol);
            
            // Crear estilo con ajuste de texto y bordes
            CellStyle style = sh.getWorkbook().createCellStyle();
            style.setWrapText(true);  // Activar ajuste de texto
            style.setVerticalAlignment(VerticalAlignment.TOP);  // Alinear arriba
            style.setAlignment(HorizontalAlignment.LEFT);  // Alinear a la izquierda
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            c.setCellStyle(style);
            c.setCellValue(valor);
            
            // Calcular altura necesaria basada en el texto
            if (lastRow > firstRow) {
                int numFilas = lastRow - firstRow + 1;
                int anchoTotalColumnas = 0;
                for (int col = firstCol; col <= lastCol; col++) {
                    anchoTotalColumnas += sh.getColumnWidth(col);
                }
                // Aproximar caracteres por línea (256 unidades = 1 carácter aprox)
                int caracteresAncho = anchoTotalColumnas / 256;
                if (caracteresAncho < 1) caracteresAncho = 80; // Default
                
                // Contar líneas necesarias
                int lineas = 1;
                String[] partes = valor.split("\n");
                for (String parte : partes) {
                    lineas += (parte.length() / caracteresAncho);
                }
                lineas += partes.length - 1; // Por los saltos de línea explícitos
                
                // Altura por línea (aprox 15 puntos = 300 twips)
                int alturaPorLinea = 320;
                int alturaTotal = lineas * alturaPorLinea;
                int alturaPorFila = alturaTotal / numFilas;
                
                // Establecer altura mínima de 300 y máxima razonable
                alturaPorFila = Math.max(300, Math.min(alturaPorFila, 1500));
                
                // Aplicar altura a todas las filas de la región combinada
                for (int fila = firstRow; fila <= lastRow; fila++) {
                    Row row = sh.getRow(fila);
                    if (row == null) row = sh.createRow(fila);
                    row.setHeight((short) alturaPorFila);
                }
            }
        } catch (Exception e) {
            System.err.println("Error escribiendo celda con ajuste " + celda + ": " + e.getMessage());
        }
    }
    
    /**
     * Obtiene los límites de una región combinada.
     * 
     * @param sh  Hoja de Excel
     * @param fila Número de fila
     * @param columna Número de columna
     * @return Array [primeraFila, ultimaFila, primeraColumna, ultimaColumna]
     */
    public static int[] obtenerLimitesRegionCombinada(Sheet sh, int fila, int columna) {
        for (int i = 0; i < sh.getNumMergedRegions(); i++) {
            CellRangeAddress cra = sh.getMergedRegion(i);
            if (cra.isInRange(fila, columna)) {
                return new int[]{cra.getFirstRow(), cra.getLastRow(), 
                                 cra.getFirstColumn(), cra.getLastColumn()};
            }
        }
        return new int[]{fila, fila, columna, columna};
    }

    /**
     * Escribe un valor numérico en una celda de la hoja de Excel.
     * 
     * @param sh    Hoja de Excel
     * @param celda Referencia de celda (ej: "A1", "D10")
     * @param valor Valor numérico a escribir
     */
    public static void escribirCelda(Sheet sh, String celda, double valor) {
        try {
            int[] coords = parsearCelda(celda);
            int rowNum = coords[0];
            int colNum = coords[1];
            
            int[] finalCoords = buscarCeldaCombinada(sh, rowNum, colNum);
            int finalRow = finalCoords[0];
            int finalCol = finalCoords[1];
            
            Row r = sh.getRow(finalRow);
            if (r == null) r = sh.createRow(finalRow);
            Cell c = r.getCell(finalCol);
            if (c == null) c = r.createCell(finalCol);
            c.setCellValue(valor);
        } catch (Exception e) {
            System.err.println("Error escribiendo celda " + celda + ": " + e.getMessage());
        }
    }

    /**
     * Escribe un valor entero en una celda de la hoja de Excel.
     * 
     * @param sh    Hoja de Excel
     * @param celda Referencia de celda
     * @param valor Valor entero a escribir
     */
    public static void escribirCelda(Sheet sh, String celda, int valor) {
        escribirCelda(sh, celda, (double) valor);
    }

    /**
     * Escribe en una celda usando índices de fila y columna directamente.
     * 
     * @param sh     Hoja de Excel
     * @param fila   Número de fila (0-based)
     * @param columna Número de columna (0-based)
     * @param valor  Valor a escribir
     */
    public static void escribirCelda(Sheet sh, int fila, int columna, String valor) {
        if (valor == null || valor.isEmpty()) return;
        try {
            int[] finalCoords = buscarCeldaCombinada(sh, fila, columna);
            int finalRow = finalCoords[0];
            int finalCol = finalCoords[1];
            
            Row r = sh.getRow(finalRow);
            if (r == null) r = sh.createRow(finalRow);
            Cell c = r.getCell(finalCol);
            if (c == null) c = r.createCell(finalCol);
            c.setCellValue(valor);
        } catch (Exception e) {
            System.err.println("Error escribiendo celda [" + fila + "," + columna + "]: " + e.getMessage());
        }
    }

    /**
     * Escribe un valor numérico usando índices de fila y columna.
     * 
     * @param sh     Hoja de Excel
     * @param fila   Número de fila (0-based)
     * @param columna Número de columna (0-based)
     * @param valor  Valor numérico a escribir
     */
    public static void escribirCelda(Sheet sh, int fila, int columna, double valor) {
        try {
            int[] finalCoords = buscarCeldaCombinada(sh, fila, columna);
            int finalRow = finalCoords[0];
            int finalCol = finalCoords[1];
            
            Row r = sh.getRow(finalRow);
            if (r == null) r = sh.createRow(finalRow);
            Cell c = r.getCell(finalCol);
            if (c == null) c = r.createCell(finalCol);
            c.setCellValue(valor);
        } catch (Exception e) {
            System.err.println("Error escribiendo celda [" + fila + "," + columna + "]: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS PARA LEER/CAPTURAR CELDAS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lee el valor de una celda como String.
     * 
     * @param sh    Hoja de Excel
     * @param celda Referencia de celda (ej: "A1", "D10")
     * @return Valor de la celda como String, o vacío si no existe
     */
    public static String leerCelda(Sheet sh, String celda) {
        try {
            int[] coords = parsearCelda(celda);
            int rowNum = coords[0];
            int colNum = coords[1];
            
            int[] finalCoords = buscarCeldaCombinada(sh, rowNum, colNum);
            int finalRow = finalCoords[0];
            int finalCol = finalCoords[1];
            
            Row r = sh.getRow(finalRow);
            if (r == null) return "";
            Cell c = r.getCell(finalCol);
            if (c == null) return "";
            
            return obtenerValorCeldaComoString(c);
        } catch (Exception e) {
            System.err.println("Error leyendo celda " + celda + ": " + e.getMessage());
            return "";
        }
    }

    /**
     * Lee el valor de una celda usando índices de fila y columna.
     * 
     * @param sh     Hoja de Excel
     * @param fila   Número de fila (0-based)
     * @param columna Número de columna (0-based)
     * @return Valor de la celda como String
     */
    public static String leerCelda(Sheet sh, int fila, int columna) {
        try {
            int[] finalCoords = buscarCeldaCombinada(sh, fila, columna);
            int finalRow = finalCoords[0];
            int finalCol = finalCoords[1];
            
            Row r = sh.getRow(finalRow);
            if (r == null) return "";
            Cell c = r.getCell(finalCol);
            if (c == null) return "";
            
            return obtenerValorCeldaComoString(c);
        } catch (Exception e) {
            System.err.println("Error leyendo celda [" + fila + "," + columna + "]: " + e.getMessage());
            return "";
        }
    }

    /**
     * Lee el valor numérico de una celda.
     * 
     * @param sh    Hoja de Excel
     * @param celda Referencia de celda
     * @return Valor numérico, o 0 si no es numérico o no existe
     */
    public static double leerCeldaNumero(Sheet sh, String celda) {
        try {
            int[] coords = parsearCelda(celda);
            int rowNum = coords[0];
            int colNum = coords[1];
            
            int[] finalCoords = buscarCeldaCombinada(sh, rowNum, colNum);
            int finalRow = finalCoords[0];
            int finalCol = finalCoords[1];
            
            Row r = sh.getRow(finalRow);
            if (r == null) return 0;
            Cell c = r.getCell(finalCol);
            if (c == null) return 0;
            
            if (c.getCellType() == CellType.NUMERIC) {
                return c.getNumericCellValue();
            } else if (c.getCellType() == CellType.STRING) {
                try {
                    return Double.parseDouble(c.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Error leyendo número de celda " + celda + ": " + e.getMessage());
            return 0;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES INTERNOS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Parsea una referencia de celda (ej: "C8") a coordenadas [fila, columna].
     * 
     * @param celda Referencia de celda estilo Excel
     * @return Array [fila, columna] (0-based)
     */
    public static int[] parsearCelda(String celda) {
        String colStr = celda.replaceAll("[0-9]", "").toUpperCase();
        int rowNum = Integer.parseInt(celda.replaceAll("[A-Za-z]", "")) - 1;
        int colNum = 0;
        for (int i = 0; i < colStr.length(); i++) {
            colNum = colNum * 26 + (colStr.charAt(i) - 'A' + 1);
        }
        colNum--;
        return new int[]{rowNum, colNum};
    }

    /**
     * Convierte índices de fila y columna a referencia de celda.
     * 
     * @param fila    Número de fila (0-based)
     * @param columna Número de columna (0-based)
     * @return Referencia de celda estilo Excel (ej: "A1", "D10")
     */
    public static String coordenadasACelda(int fila, int columna) {
        StringBuilder sb = new StringBuilder();
        int col = columna;
        while (col >= 0) {
            sb.insert(0, (char) ('A' + col % 26));
            col = col / 26 - 1;
        }
        sb.append(fila + 1);
        return sb.toString();
    }

    /**
     * Busca si una celda pertenece a una región combinada y retorna 
     * la celda principal de la región.
     * 
     * @param sh  Hoja de Excel
     * @param fila Número de fila (0-based)
     * @param columna Número de columna (0-based)
     * @return Array [filaFinal, columnaFinal] de la celda principal
     */
    public static int[] buscarCeldaCombinada(Sheet sh, int fila, int columna) {
        for (int i = 0; i < sh.getNumMergedRegions(); i++) {
            CellRangeAddress cra = sh.getMergedRegion(i);
            if (cra.isInRange(fila, columna)) {
                return new int[]{cra.getFirstRow(), cra.getFirstColumn()};
            }
        }
        return new int[]{fila, columna};
    }

    /**
     * Obtiene el valor de una celda como String, sin importar su tipo.
     * 
     * @param cell Celda de Excel
     * @return Valor como String
     */
    public static String obtenerValorCeldaComoString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return cell.getCellFormula();
                    }
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * Verifica si una celda está vacía.
     * 
     * @param sh    Hoja de Excel
     * @param celda Referencia de celda
     * @return true si está vacía o no existe
     */
    public static boolean celdaVacia(Sheet sh, String celda) {
        String valor = leerCelda(sh, celda);
        return valor == null || valor.trim().isEmpty();
    }

    /**
     * Copia el valor de una celda a otra.
     * 
     * @param sh         Hoja de Excel
     * @param celdaOrigen Celda origen
     * @param celdaDestino Celda destino
     */
    public static void copiarCelda(Sheet sh, String celdaOrigen, String celdaDestino) {
        String valor = leerCelda(sh, celdaOrigen);
        escribirCelda(sh, celdaDestino, valor);
    }

    /**
     * Escribe fecha en celdas separadas (día, mes, año).
     * 
     * @param sh       Hoja de Excel
     * @param celdaDia Celda para el día
     * @param celdaMes Celda para el mes
     * @param celdaAnio Celda para el año
     * @param dia      Día
     * @param mes      Mes
     * @param anio     Año
     */
    public static void escribirFecha(Sheet sh, String celdaDia, String celdaMes, String celdaAnio, 
                                      int dia, int mes, int anio) {
        escribirCelda(sh, celdaDia, dia);
        escribirCelda(sh, celdaMes, mes);
        escribirCelda(sh, celdaAnio, anio);
    }

    /**
     * Escribe la fecha actual en las celdas especificadas.
     * 
     * @param sh       Hoja de Excel
     * @param celdaDia Celda para el día
     * @param celdaMes Celda para el mes
     * @param celdaAnio Celda para el año
     */
    public static void escribirFechaActual(Sheet sh, String celdaDia, String celdaMes, String celdaAnio) {
        java.time.LocalDate fechaActual = java.time.LocalDate.now();
        escribirFecha(sh, celdaDia, celdaMes, celdaAnio, 
                      fechaActual.getDayOfMonth(), 
                      fechaActual.getMonthValue(), 
                      fechaActual.getYear());
    }
}
