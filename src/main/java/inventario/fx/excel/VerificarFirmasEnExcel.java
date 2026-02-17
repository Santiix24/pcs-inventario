package inventario.fx.excel;

import inventario.fx.util.AppLogger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * Utilidad para verificar que las firmas digitales se hayan insertado correctamente
 * en el archivo Excel generado.
 */
public class VerificarFirmasEnExcel {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java VerificarFirmasEnExcel <ruta-al-archivo.xlsx>");
            System.out.println("\nEjemplo:");
            System.out.println("  java VerificarFirmasEnExcel reporte_mantenimiento.xlsx");
            return;
        }
        
        String archivoExcel = args[0];
        verificarFirmas(archivoExcel);
    }
    
    public static void verificarFirmas(String rutaArchivo) {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("  VERIFICACIÓN DE FIRMAS DIGITALES EN EXCEL");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("\nArchivo: " + rutaArchivo);
        
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            System.err.println("❌ ERROR: El archivo no existe");
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(archivo);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            
            XSSFSheet sheet = workbook.getSheetAt(0);
            System.out.println("✓ Hoja encontrada: " + sheet.getSheetName());
            
            // Obtener el drawing patriarch (contiene todas las imágenes)
            XSSFDrawing drawing = sheet.getDrawingPatriarch();
            
            if (drawing == null) {
                System.out.println("\n⚠ ADVERTENCIA: No se encontraron imágenes en el Excel");
                System.out.println("  Asegúrese de haber firmado en el formulario antes de generar el reporte");
                return;
            }
            
            List<XSSFShape> shapes = drawing.getShapes();
            System.out.println("\n✓ Total de imágenes encontradas: " + shapes.size());
            
            int firmaTecnicoEncontrada = 0;
            int firmaFuncionarioEncontrada = 0;
            int imagenProyecto = 0;
            int logoSelcomp = 0;
            
            System.out.println("\n─────────────────────────────────────────────────────────────");
            System.out.println("  ANÁLISIS DE IMÁGENES INSERTADAS");
            System.out.println("─────────────────────────────────────────────────────────────");
            
            for (int i = 0; i < shapes.size(); i++) {
                XSSFShape shape = shapes.get(i);
                
                if (shape instanceof XSSFPicture) {
                    XSSFPicture picture = (XSSFPicture) shape;
                    XSSFClientAnchor anchor = (XSSFClientAnchor) picture.getAnchor();
                    
                    int col1 = anchor.getCol1();
                    int row1 = anchor.getRow1();
                    int col2 = anchor.getCol2();
                    int row2 = anchor.getRow2();
                    
                    System.out.println("\n[" + (i + 1) + "] Imagen encontrada:");
                    System.out.println("    Posición: " + obtenerCelda(col1, row1) + ":" + obtenerCelda(col2-1, row2-1));
                    System.out.println("    Columnas: " + col1 + " → " + (col2-1));
                    System.out.println("    Filas: " + (row1+1) + " → " + row2);
                    
                    // Verificar si es la firma del técnico (B54:F57 = cols 1-5, rows 53-56)
                    if (col1 == 1 && row1 == 53 && (col2 == 6 || col2 == 5) && (row2 == 57 || row2 == 56)) {
                        System.out.println("    ✓ FIRMA TÉCNICO identificada correctamente");
                        System.out.println("      Ubicación esperada: B54:F57");
                        firmaTecnicoEncontrada++;
                    }
                    // Verificar si es la firma del funcionario (I54:M57 = cols 8-12, rows 53-56)
                    else if (col1 == 8 && row1 == 53 && (col2 == 13 || col2 == 12) && (row2 == 57 || row2 == 56)) {
                        System.out.println("    ✓ FIRMA FUNCIONARIO identificada correctamente");
                        System.out.println("      Ubicación esperada: I54:M57");
                        firmaFuncionarioEncontrada++;
                    }
                    // Verificar si es la imagen del proyecto (B2:D4 aprox)
                    else if (col1 == 1 && row1 >= 1 && row1 <= 2) {
                        System.out.println("    ✓ IMAGEN DEL PROYECTO identificada");
                        System.out.println("      Ubicación esperada: B2:D4");
                        imagenProyecto++;
                    }
                    // Verificar si es el logo Selcomp (K2 aprox)
                    else if (col1 == 10 && row1 >= 1 && row1 <= 2) {
                        System.out.println("    ✓ LOGO SELCOMP identificado");
                        System.out.println("      Ubicación esperada: K2:M4");
                        logoSelcomp++;
                    }
                    else {
                        System.out.println("⚠ Imagen en ubicación no identificada");
                    }
                    
                    // Información adicional de la imagen
                    XSSFPictureData pictureData = picture.getPictureData();
                    String mimeType = pictureData.getMimeType();
                    int tamanio = pictureData.getData().length;
                    
                    System.out.println("    Formato: " + mimeType);
                    System.out.println("    Tamaño: " + formatearTamanio(tamanio));
                }
            }
            
            // Resumen final
            System.out.println("\n═══════════════════════════════════════════════════════════════");
            System.out.println("  RESUMEN DE VERIFICACIÓN");
            System.out.println("═══════════════════════════════════════════════════════════════");
            
            System.out.println("\n[FIRMAS DIGITALES]");
            if (firmaTecnicoEncontrada > 0) {
                System.out.println("  ✅ Firma Técnico: ENCONTRADA en B54:F57");
            } else {
                System.out.println("  ❌ Firma Técnico: NO ENCONTRADA");
            }
            
            if (firmaFuncionarioEncontrada > 0) {
                System.out.println("  ✅ Firma Funcionario: ENCONTRADA en I54:M57");
            } else {
                System.out.println("  ❌ Firma Funcionario: NO ENCONTRADA");
            }
            
            System.out.println("\n[OTRAS IMÁGENES]");
            if (imagenProyecto > 0) {
                System.out.println("  ✓ Imagen del Proyecto: " + imagenProyecto + " encontrada(s)");
            }
            if (logoSelcomp > 0) {
                System.out.println("  ✓ Logo Selcomp: " + logoSelcomp + " encontrada(s)");
            }
            
            // Validación final
            System.out.println("\n─────────────────────────────────────────────────────────────");
            boolean todoOk = firmaTecnicoEncontrada > 0 && firmaFuncionarioEncontrada > 0;
            
            if (todoOk) {
                System.out.println("  ✅ VALIDACIÓN EXITOSA");
                System.out.println("  Todas las firmas digitales están presentes y correctamente ubicadas.");
            } else {
                System.out.println("  ⚠ VALIDACIÓN PARCIAL");
                if (firmaTecnicoEncontrada == 0 && firmaFuncionarioEncontrada == 0) {
                    System.out.println("  No se encontraron firmas digitales.");
                    System.out.println("  Asegúrese de firmar en el formulario antes de generar el reporte.");
                } else if (firmaTecnicoEncontrada == 0) {
                    System.out.println("  Falta la firma del técnico.");
                } else if (firmaFuncionarioEncontrada == 0) {
                    System.out.println("  Falta la firma del funcionario.");
                }
            }
            System.out.println("═══════════════════════════════════════════════════════════════\n");
            
        } catch (Exception e) {
            AppLogger.getLogger(VerificarFirmasEnExcel.class).error("❌ ERROR al verificar el archivo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convierte índices de columna/fila a notación de celda (ej: A1, B2)
     */
    private static String obtenerCelda(int col, int row) {
        StringBuilder sb = new StringBuilder();
        while (col >= 0) {
            sb.insert(0, (char)('A' + (col % 26)));
            col = col / 26 - 1;
        }
        sb.append(row + 1);
        return sb.toString();
    }
    
    /**
     * Formatea el tamaño del archivo en KB o MB
     */
    private static String formatearTamanio(int bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }
}
