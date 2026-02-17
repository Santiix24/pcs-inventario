package inventario.fx.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;

public class ReadFechaChecker {
    public static void main(String[] args) throws Exception {
        String nombre = "FORMATO_RECREADO_FECHA_AUTOMATICA.xlsx";
        File f = new File(nombre);
        if (!f.exists()) {
            System.out.println("Archivo no encontrado: " + nombre);
            return;
        }
        try (FileInputStream fis = new FileInputStream(f); Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sh = wb.getSheet("Formato Mantenimiento");
            if (sh == null) sh = wb.getSheetAt(0);
            // Revisar varias filas/columnas para detectar pérdida de contenido o estilos
            int[] rowsToCheck = new int[]{6,7}; // revisar fila índice 6 (Excel 7) y 7 (Excel 8)
            int[] cols = new int[]{1, 10, 11, 12}; // columna 1 = B, 10=K,11=L,12=M
            DataFormatter df = new DataFormatter();
            System.out.println("Hoja: " + sh.getSheetName());
            for (int rowIdx : rowsToCheck) {
                Row row = sh.getRow(rowIdx);
                System.out.println("--- Fila índice: " + rowIdx + " (Excel: " + (rowIdx+1) + ") ---");
                for (int c : cols) {
                    System.out.println("--- Columna índice " + c + " (Excel: " + (c+1) + ") ---");
                    Cell cell = row == null ? null : row.getCell(c);
                    if (cell == null) {
                        System.out.println("Celda nula en esa posición.");
                        // buscar si pertenece a una región combinada y mostrar la celda superior-izquierda
                        for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                            CellRangeAddress cra = sh.getMergedRegion(i);
                            if (cra.isInRange(rowIdx, c)) {
                                System.out.println("Pertenece a región combinada: " + cra.formatAsString());
                                Row firstRow = sh.getRow(cra.getFirstRow());
                                Cell firstCell = firstRow == null ? null : firstRow.getCell(cra.getFirstColumn());
                                printCell(firstCell, df, wb);
                            }
                        }
                        continue;
                    }
                    printCell(cell, df, wb);
                    for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                        CellRangeAddress cra = sh.getMergedRegion(i);
                        if (cra.isInRange(rowIdx, c)) {
                            System.out.println("La celda está dentro de una región combinada: " + cra.formatAsString());
                        }
                    }
                }
            }
        }
    }

    private static void printCell(Cell cell, DataFormatter df, Workbook wb) {
        if (cell == null) {
            System.out.println("Celda nula (valor no encontrado).");
            return;
        }
        System.out.println("Tipo de celda: " + cell.getCellType());
        System.out.println("Valor formateado: '" + df.formatCellValue(cell) + "'");
        if (cell.getCellType() == CellType.NUMERIC) System.out.println("Valor numérico: " + cell.getNumericCellValue());
        if (cell.getCellType() == CellType.STRING) System.out.println("Valor texto: '" + cell.getStringCellValue() + "'");
        CellStyle cs = cell.getCellStyle();
        if (cs != null) {
            System.out.println("CellStyle aplicado.");
            try {
                Font f = wb.getFontAt(cs.getFontIndexAsInt());
                System.out.println("Fuente: bold=" + f.getBold() + ", colorIndex=" + f.getColor());
            } catch (Exception e) {
                System.out.println("No se pudo obtener fuente: " + e.getMessage());
            }
            // mostrar si la celda está vacía en cuanto a texto
            String text = df.formatCellValue(cell);
            if (text == null || text.trim().isEmpty()) System.out.println("La celda parece vacía (cadena vacía).");
        }
    }
}
