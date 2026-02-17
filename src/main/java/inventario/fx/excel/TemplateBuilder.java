package inventario.fx.excel;
import inventario.fx.util.AppLogger;
import inventario.fx.model.InventarioFXBase;
import inventario.fx.ui.panel.ReporteFormularioFX;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase auxiliar para construir el template Excel de mantenimiento.
 * Divide la lógica del GeneratedTemplate original en métodos más pequeños
 * para evitar el límite de 65535 bytes por método en la JVM.
 */
public class TemplateBuilder {
    
    private Workbook wb;
    private Sheet sh;
    private List<CellStyle> styles;
    private CellStyle style70b;
    
    // Variables de datos del formulario
    private int dia, mes, anio;
    private ReporteFormularioFX.DatosReporte datosForm;
    private InventarioFXBase.InfoPC sysInfo;
    
    // Datos procesados
    private String SYS_TIPO, SYS_MARCA, SYS_MODELO, SYS_CPU_SHORT, SYS_RAM_SHORT, SYS_DISK_SHORT;
    private String FORM_TICKET, FORM_CIUDAD, FORM_DIRECCION, FORM_NOMBRE, FORM_CORREO;
    private String FORM_TECNICO, FORM_SEDE, FORM_CONDICIONES, FORM_SERIAL, FORM_PLACA;
    private String FORM_PC_ENCIENDE, FORM_DISCO_DURO, FORM_CDDVD, FORM_BOTONES_PC, FORM_CONDICIONES_PC;
    private String FORM_MONITOR_ENCIENDE, FORM_PANTALLA, FORM_ONLY_ONE, FORM_BOTONES_MONITOR, FORM_CONDICIONES_MONITOR;
    private String FORM_TECLADO_ENCIENDE, FORM_TECLADO_FUNCIONA, FORM_BOTONES_TECLADO, FORM_CONDICIONES_TECLADO;
    private String FORM_MOUSE_ENCIENDE, FORM_MOUSE_FUNCIONA, FORM_BOTONES_MOUSE, FORM_CONDICIONES_MOUSE;
    private String FORM_OBSERVACIONES, FORM_TRABAJO_REALIZADO;
    
    public TemplateBuilder() {
        styles = new ArrayList<>();
    }
    
    /**
     * Método principal que coordina la construcción del template.
     */
    public void buildTemplate() throws Exception {
        // 1. Obtener datos del formulario
        initFormData();
        
        // 2. Crear workbook y sheet
        wb = new XSSFWorkbook();
        sh = wb.createSheet("Formato Mantenimiento");
        
        // 3. Procesar datos del sistema
        processSystemData();
        
        // 4. Configurar anchos de columnas
        setupColumnWidths();
        
        // 5. Crear filas con altura
        createRows();
        
        // 6. Crear estilos
        createAllStyles();
        
        // 7. Agregar regiones combinadas
        addMergedRegions();
        
        // 8. Poblar celdas
        populateAllCells();
        
        // 9. Escribir datos del formulario
        writeFormData();
        
        // 10. Aplicar bordes faltantes
        applyMissingBorders();
        
        // 11. Insertar imágenes
        insertImages();
        
        // 12. Guardar archivo
        saveFile();
    }
    
    /**
     * Inicializa los datos del formulario.
     */
    private void initFormData() {
        java.time.LocalDate fechaActual = java.time.LocalDate.now();
        dia = fechaActual.getDayOfMonth();
        mes = fechaActual.getMonthValue();
        anio = fechaActual.getYear();
        
        datosForm = ReporteFormularioFX.obtenerDatos();
        
        // DEBUG
        System.out.println("=== DATOS DEL FORMULARIO ===");
        System.out.println("Ciudad: " + datosForm.ciudad);
        System.out.println("Direccion: " + datosForm.direccion);
        System.out.println("Nombre: " + datosForm.nombre);
        System.out.println("Correo: " + datosForm.correo);
        System.out.println("Tecnico: " + datosForm.tecnico);
        System.out.println("Sede: " + datosForm.sede);
        System.out.println("Procedimiento: " + datosForm.trabajoRealizado);
        System.out.println("Observaciones: " + datosForm.observaciones);
        System.out.println("============================");
        
        // Usar fecha del formulario si está disponible
        if (!datosForm.dia.isEmpty()) {
            try { dia = Integer.parseInt(datosForm.dia); } catch (Exception e) {}
        }
        if (!datosForm.mes.isEmpty()) {
            try { mes = Integer.parseInt(datosForm.mes); } catch (Exception e) {}
        }
        if (!datosForm.anio.isEmpty()) {
            try { anio = Integer.parseInt(datosForm.anio); } catch (Exception e) {}
        }
        
        // Asignar variables de formulario
        FORM_TICKET = datosForm.ticket;
        FORM_CIUDAD = datosForm.ciudad;
        FORM_DIRECCION = datosForm.direccion;
        FORM_NOMBRE = datosForm.nombre;
        FORM_CORREO = datosForm.correo;
        FORM_TECNICO = datosForm.tecnico;
        FORM_SEDE = datosForm.sede;
        FORM_CONDICIONES = datosForm.condiciones;
        FORM_SERIAL = datosForm.serial;
        FORM_PLACA = datosForm.placa;
        
        FORM_PC_ENCIENDE = datosForm.pcEnciende;
        FORM_DISCO_DURO = datosForm.discoDuro;
        FORM_CDDVD = datosForm.cddvd;
        FORM_BOTONES_PC = datosForm.botonesPC;
        FORM_CONDICIONES_PC = datosForm.condicionesPC;
        
        FORM_MONITOR_ENCIENDE = datosForm.monitorEnciende;
        FORM_PANTALLA = datosForm.pantalla;
        FORM_ONLY_ONE = datosForm.onlyOne;
        FORM_BOTONES_MONITOR = datosForm.botonesMonitor;
        FORM_CONDICIONES_MONITOR = datosForm.condicionesMonitor;
        
        FORM_TECLADO_ENCIENDE = datosForm.tecladoEnciende;
        FORM_TECLADO_FUNCIONA = datosForm.tecladoFunciona;
        FORM_BOTONES_TECLADO = datosForm.botonesTeclado;
        FORM_CONDICIONES_TECLADO = datosForm.condicionesTeclado;
        
        FORM_MOUSE_ENCIENDE = datosForm.mouseEnciende;
        FORM_MOUSE_FUNCIONA = datosForm.mouseFunciona;
        FORM_BOTONES_MOUSE = datosForm.botonesMouse;
        FORM_CONDICIONES_MOUSE = datosForm.condicionesMouse;
        
        FORM_OBSERVACIONES = datosForm.observaciones;
        FORM_TRABAJO_REALIZADO = datosForm.trabajoRealizado;
    }
    
    /**
     * Procesa los datos del sistema para obtener información del PC.
     */
    private void processSystemData() {
        try {
            sysInfo = InventarioFXBase.recopilarInfo();
        } catch (Throwable _ignore) {
            sysInfo = null;
        }
        
        // Tipo, marca, modelo
        SYS_TIPO = !datosForm.tipoDispositivo.isEmpty() ? datosForm.tipoDispositivo : 
            (sysInfo != null && sysInfo.deviceType != null) ? sysInfo.deviceType : "";
        SYS_MARCA = !datosForm.marca.isEmpty() ? datosForm.marca :
            (sysInfo != null && sysInfo.manufacturer != null) ? sysInfo.manufacturer : "";
        SYS_MODELO = !datosForm.modelo.isEmpty() ? datosForm.modelo :
            (sysInfo != null && sysInfo.modeloEquipo != null) ? sysInfo.modeloEquipo : "";
        
        // CPU abreviado
        SYS_CPU_SHORT = processCPU();
        
        // RAM y Disco
        processRAMAndDisk();
    }
    
    /**
     * Procesa y abrevia el nombre del CPU.
     */
    private String processCPU() {
        String sysCPU = (sysInfo != null && sysInfo.cpu != null) ? sysInfo.cpu : "";
        
        if (!datosForm.procesador.isEmpty()) {
            return datosForm.procesador;
        }
        
        String cpuShort = "";
        try {
            String _short = sysCPU.replaceAll("\\([^)]*\\)", "");
            int at = _short.indexOf('@');
            if (at >= 0) _short = _short.substring(0, at);
            _short = _short.replaceAll("(?i)\\bCPU\\b", "");
            _short = _short.replaceAll("[^\\p{L}\\p{N}\\.\\-\\s]", " ");
            _short = _short.replaceAll("\\s+", " ").trim();
            
            java.util.regex.Pattern pCoreModel = java.util.regex.Pattern.compile("(?i)\\bcore\\b.*?(i\\d)[\\s\\-]*(\\d{3,5}\\w*)");
            java.util.regex.Matcher mCoreModel = pCoreModel.matcher(_short);
            if (mCoreModel.find()) {
                cpuShort = "Core " + mCoreModel.group(1).toLowerCase() + "-" + mCoreModel.group(2);
            } else {
                java.util.regex.Pattern pCore = java.util.regex.Pattern.compile("(?i)\\bcore\\b.*?(i\\d)");
                java.util.regex.Matcher mCore = pCore.matcher(_short);
                if (mCore.find()) {
                    cpuShort = "Core " + mCore.group(1).toLowerCase();
                } else {
                    java.util.regex.Pattern pIntel = java.util.regex.Pattern.compile("(?i)\\bintel\\b.*?(i\\d)[\\s\\-]*(\\d{3,5}\\w*)?");
                    java.util.regex.Matcher mIntel = pIntel.matcher(_short);
                    if (mIntel.find()) {
                        cpuShort = mIntel.group(2) != null ? 
                            "Intel " + mIntel.group(1).toLowerCase() + "-" + mIntel.group(2) :
                            "Intel " + mIntel.group(1).toLowerCase();
                    } else {
                        java.util.regex.Pattern pRyzen = java.util.regex.Pattern.compile("(?i)\\bryzen\\b\\s*(\\d)[\\s\\-]*(\\d{4}\\w*)?");
                        java.util.regex.Matcher mRyzen = pRyzen.matcher(_short);
                        if (mRyzen.find()) {
                            cpuShort = mRyzen.group(2) != null ? 
                                "Ryzen " + mRyzen.group(1) + " " + mRyzen.group(2) :
                                "Ryzen " + mRyzen.group(1);
                        } else {
                            cpuShort = _short;
                        }
                    }
                }
            }
        } catch (Throwable _t) {
            cpuShort = sysCPU;
        }
        
        return cpuShort;
    }
    
    /**
     * Procesa y abrevia la RAM y el disco.
     */
    private void processRAMAndDisk() {
        // RAM
        if (!datosForm.memoriaRAM.isEmpty()) {
            SYS_RAM_SHORT = datosForm.memoriaRAM;
        } else {
            String ramTmp = (sysInfo != null && sysInfo.ram != null) ? sysInfo.ram : "";
            SYS_RAM_SHORT = parseStorageSize(ramTmp, true);
        }
        
        // Disco
        if (!datosForm.discoDuroCapacidad.isEmpty()) {
            SYS_DISK_SHORT = datosForm.discoDuroCapacidad;
        } else {
            String discosTmp = (sysInfo != null && sysInfo.discos != null) ? sysInfo.discos : "";
            SYS_DISK_SHORT = parseStorageSize(discosTmp, false);
        }
    }
    
    /**
     * Parsea un tamaño de almacenamiento y lo abrevia.
     */
    private String parseStorageSize(String input, boolean isRAM) {
        String result = "";
        try {
            java.util.regex.Pattern pSize = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)(?:\\s*)(GB|TB|MB)", java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = pSize.matcher(input);
            
            String lastNum = null;
            String lastUnit = null;
            
            if (isRAM) {
                while (m.find()) {
                    lastNum = m.group(1);
                    lastUnit = m.group(2);
                }
            } else {
                if (m.find()) {
                    lastNum = m.group(1);
                    lastUnit = m.group(2);
                }
            }
            
            if (lastNum != null && lastUnit != null) {
                double val = Double.parseDouble(lastNum.replace(',', '.'));
                String u = lastUnit.toUpperCase();
                double gb = u.equals("TB") ? val * 1024.0 : u.equals("GB") ? val : val / 1024.0;
                
                if (!isRAM && gb >= 1024.0) {
                    result = Math.max(1, Math.round(gb / 1024.0)) + "TB";
                } else {
                    result = Math.max(1, Math.round(gb)) + "GB";
                }
            }
        } catch (Throwable _t) {
            result = "";
        }
        return result;
    }
    
    /**
     * Configura los anchos de las columnas.
     */
    private void setupColumnWidths() {
        for (int i = 0; i <= 13; i++) {
            sh.setColumnWidth(i, 2560);
        }
        
        // Ocultar todas las columnas desde la O (14) hasta la última columna de Excel (16384)
        for (int i = 14; i < 16384; i++) {
            sh.setColumnHidden(i, true);
        }
    }
    
    /**
     * Crea las filas con su altura.
     */
    private void createRows() {
        for (int i = 1; i <= 59; i++) {
            Row row = sh.createRow(i);
            row.setHeight((short) 300);
        }
    }
    
    /**
     * Crea todos los estilos necesarios.
     */
    private void createAllStyles() {
        // Crear estilos 0-90
        for (int i = 0; i <= 90; i++) {
            styles.add(createStyle(i));
        }
        
        // Remover rellenos sólidos
        for (int i = 0; i <= 90; i++) {
            styles.get(i).setFillPattern(FillPatternType.NO_FILL);
        }
        
        // Configurar estilos de encabezado oscuro
        setupDarkHeaderStyles();
    }
    
    /**
     * Crea un estilo individual basado en su índice.
     */
    private CellStyle createStyle(int index) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        
        // Configuración base según índice
        StyleConfig config = getStyleConfig(index);
        
        font.setFontHeightInPoints(config.fontSize);
        font.setColor(config.fontColor);
        if (config.bold) font.setBold(true);
        
        style.setFont(font);
        style.setAlignment(config.hAlign);
        style.setVerticalAlignment(config.vAlign);
        if (config.wrapText) style.setWrapText(true);
        style.setFillForegroundColor(config.fillColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(config.borderTop);
        style.setBorderBottom(config.borderBottom);
        style.setBorderLeft(config.borderLeft);
        style.setBorderRight(config.borderRight);
        
        return style;
    }
    
    /**
     * Clase interna para configuración de estilos.
     */
    private static class StyleConfig {
        short fontSize = 11;
        short fontColor = 0;
        boolean bold = false;
        boolean wrapText = false;
        HorizontalAlignment hAlign = HorizontalAlignment.CENTER;
        VerticalAlignment vAlign = VerticalAlignment.CENTER;
        short fillColor = 0;
        BorderStyle borderTop = BorderStyle.NONE;
        BorderStyle borderBottom = BorderStyle.NONE;
        BorderStyle borderLeft = BorderStyle.NONE;
        BorderStyle borderRight = BorderStyle.NONE;
    }
    
    /**
     * Obtiene la configuración para un estilo específico.
     */
    private StyleConfig getStyleConfig(int index) {
        StyleConfig config = new StyleConfig();
        
        // Configuración base para todos los estilos
        switch (index) {
            case 0: case 1: case 5: case 6: case 12: case 13:
                config.wrapText = true;
                break;
            case 3: case 8: case 10:
                config.fontSize = 10;
                break;
            case 16: case 34: case 38: case 46: case 47: case 65: case 66:
                config.bold = true;
                break;
            case 50: case 72: case 73: case 74:
                config.fontColor = 8;
                break;
        }
        
        // Alineación vertical
        switch (index) {
            case 7: case 14: case 15: case 16: case 17: case 18: case 22:
            case 31: case 35: case 36: case 37: case 46: case 47: case 48:
            case 49: case 60: case 61: case 62: case 63: case 64: case 65:
            case 66: case 67: case 68: case 69: case 70: case 71: case 72:
            case 73: case 74: case 75: case 76: case 80: case 81: case 85:
            case 86: case 90:
                config.vAlign = VerticalAlignment.BOTTOM;
                break;
        }
        
        // Alineación horizontal
        switch (index) {
            case 4: case 7: case 9: case 11: case 48: case 60: case 61:
            case 62: case 63: case 64: case 67: case 68: case 69: case 80:
            case 81:
                config.hAlign = HorizontalAlignment.GENERAL;
                break;
            case 51: case 52: case 53: case 54: case 55: case 56: case 57:
            case 58: case 59: case 75: case 76: case 85: case 86:
                config.hAlign = HorizontalAlignment.LEFT;
                break;
        }
        
        // Colores de relleno
        switch (index) {
            case 19: case 20: case 21: case 23: case 24: case 25:
            case 80: case 81: case 82: case 83: case 84: case 85:
            case 86: case 87: case 88: case 89:
                config.fillColor = 64;
                break;
        }
        
        // Bordes (configuración simplificada)
        configureBorders(config, index);
        
        return config;
    }
    
    /**
     * Configura los bordes para un estilo.
     */
    private void configureBorders(StyleConfig config, int index) {
        // Bordes superiores
        int[] topBorders = {0, 1, 2, 3, 4, 16, 17, 18, 19, 20, 21, 22, 26, 27, 28, 29, 30, 31, 32, 33,
            34, 35, 37, 38, 41, 46, 47, 50, 51, 52, 53, 65, 66, 67, 68, 69, 70, 72, 73, 74, 75, 76,
            77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89};
        for (int i : topBorders) {
            if (index == i) config.borderTop = BorderStyle.THIN;
        }
        
        // Bordes inferiores
        int[] bottomBorders = {10, 11, 12, 13, 14, 15, 16, 17, 18, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33,
            39, 40, 48, 57, 58, 59, 62, 63, 64, 65, 66, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83,
            84, 85, 86, 87, 88, 89, 90};
        for (int i : bottomBorders) {
            if (index == i) config.borderBottom = BorderStyle.THIN;
        }
        
        // Bordes izquierdos
        int[] leftBorders = {0, 5, 12, 16, 19, 23, 26, 28, 31, 32, 33, 34, 38, 39, 41, 42, 44, 50, 51,
            54, 57, 60, 62, 67, 70, 71, 72, 75, 77, 80, 82, 85, 87, 90};
        for (int i : leftBorders) {
            if (index == i) config.borderLeft = BorderStyle.THIN;
        }
        
        // Bordes derechos
        int[] rightBorders = {4, 9, 11, 15, 18, 21, 25, 27, 30, 33, 37, 43, 44, 47, 49, 53, 56, 59, 61,
            64, 66, 69, 74, 76, 79, 81, 84, 86, 89};
        for (int i : rightBorders) {
            if (index == i) config.borderRight = BorderStyle.THIN;
        }
    }
    
    /**
     * Configura los estilos de encabezado con fondo oscuro.
     */
    private void setupDarkHeaderStyles() {
        try {
            XSSFColor darkColor = new XSSFColor(new byte[]{64, 64, 64}, ((XSSFWorkbook) wb).getStylesSource().getIndexedColors());
            
            // Estilos 16, 34, 38 con fondo oscuro y fuente blanca
            int[] darkStyles = {16, 34, 38};
            for (int idx : darkStyles) {
                // Crear nueva fuente blanca para este estilo
                Font whiteFont = wb.createFont();
                whiteFont.setFontHeightInPoints((short) 11);
                whiteFont.setColor(IndexedColors.WHITE.getIndex());
                whiteFont.setBold(true);
                
                ((XSSFCellStyle) styles.get(idx)).setFillForegroundColor(darkColor);
                styles.get(idx).setFillPattern(FillPatternType.SOLID_FOREGROUND);
                styles.get(idx).setFont(whiteFont);
            }
            
            // Crear style70b
            style70b = wb.createCellStyle();
            Font font70b = wb.createFont();
            font70b.setFontHeightInPoints((short) 11);
            font70b.setColor(IndexedColors.WHITE.getIndex());
            style70b.setFont(font70b);
            style70b.setAlignment(HorizontalAlignment.CENTER);
            style70b.setVerticalAlignment(VerticalAlignment.BOTTOM);
            ((XSSFCellStyle) style70b).setFillForegroundColor(darkColor);
            style70b.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style70b.setBorderTop(BorderStyle.THIN);
            style70b.setBorderLeft(BorderStyle.THIN);
            
        } catch (ClassCastException e) {
            // Fallback con color indexado
            short grayIdx = IndexedColors.GREY_50_PERCENT.getIndex();
            int[] darkStyles = {16, 34, 38};
            for (int idx : darkStyles) {
                styles.get(idx).setFillForegroundColor(grayIdx);
                styles.get(idx).setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            
            style70b = wb.createCellStyle();
            Font font70b = wb.createFont();
            font70b.setFontHeightInPoints((short) 11);
            font70b.setColor(IndexedColors.WHITE.getIndex());
            style70b.setFont(font70b);
            style70b.setAlignment(HorizontalAlignment.CENTER);
            style70b.setVerticalAlignment(VerticalAlignment.BOTTOM);
            style70b.setFillForegroundColor(grayIdx);
            style70b.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style70b.setBorderTop(BorderStyle.THIN);
            style70b.setBorderLeft(BorderStyle.THIN);
        }
    }
    
    /**
     * Agrega todas las regiones combinadas.
     */
    private void addMergedRegions() {
        // Definir todas las regiones combinadas como arrays [firstRow, lastRow, firstCol, lastCol]
        int[][] regions = {
            {1, 3, 10, 12}, {52, 52, 1, 5}, {4, 4, 10, 12}, {6, 7, 10, 10},
            {17, 18, 1, 2}, {17, 18, 3, 4}, {17, 18, 5, 6}, {17, 18, 7, 8},
            {8, 8, 1, 12}, {6, 7, 11, 11}, {6, 7, 12, 12}, {7, 7, 1, 3},
            {17, 18, 10, 12}, {17, 18, 9, 9}, {19, 21, 1, 2}, {19, 21, 3, 4},
            {19, 21, 5, 6}, {19, 21, 7, 8}, {19, 21, 9, 9}, {19, 21, 10, 12},
            {5, 6, 1, 9}, {7, 7, 4, 9}, {26, 26, 3, 6}, {23, 24, 7, 9},
            {25, 25, 7, 9}, {26, 26, 7, 8}, {23, 24, 10, 12}, {25, 25, 10, 12},
            {23, 24, 1, 2}, {22, 22, 1, 12}, {23, 23, 3, 6}, {24, 24, 3, 4},
            {24, 24, 5, 6}, {25, 25, 1, 2}, {25, 25, 3, 4}, {25, 25, 5, 6},
            {27, 27, 1, 12}, {28, 29, 1, 2}, {30, 30, 1, 2}, {28, 29, 3, 4},
            {28, 29, 5, 6}, {30, 30, 3, 4}, {30, 30, 5, 6}, {28, 29, 7, 9},
            {30, 30, 7, 9}, {28, 29, 10, 12}, {32, 33, 10, 12}, {34, 34, 1, 2},
            {34, 34, 3, 6}, {34, 34, 7, 9}, {34, 34, 10, 12}, {35, 35, 1, 12},
            {32, 33, 1, 2}, {9, 9, 3, 12}, {10, 10, 3, 12}, {11, 11, 3, 12},
            {12, 12, 3, 12}, {13, 13, 3, 12}, {14, 14, 3, 12}, {32, 33, 3, 6},
            {32, 33, 7, 9}, {30, 30, 10, 12}, {31, 31, 1, 12}, {9, 9, 1, 2},
            {10, 10, 1, 2}, {11, 11, 1, 2}, {12, 12, 1, 2}, {13, 13, 1, 2},
            {14, 14, 1, 2}, {26, 26, 10, 11}, {37, 37, 1, 2}, {38, 38, 1, 2},
            {39, 39, 1, 2}, {37, 37, 3, 4}, {37, 37, 5, 6}, {37, 37, 7, 8},
            {38, 38, 3, 4}, {38, 38, 5, 6}, {38, 38, 7, 8}, {39, 39, 3, 4},
            {52, 59, 6, 7}, {1, 4, 1, 3}, {1, 1, 4, 9}, {4, 4, 4, 9},
            {59, 59, 3, 5}, {58, 58, 10, 12}, {59, 59, 10, 12}, {15, 16, 1, 12},
            {26, 26, 1, 2}, {52, 52, 8, 12}, {46, 46, 1, 12}, {58, 58, 1, 2},
            {59, 59, 1, 2}, {57, 57, 8, 12}, {57, 57, 1, 5}, {59, 59, 8, 9},
            {58, 58, 8, 9}, {58, 58, 3, 5}, {39, 39, 5, 6}, {39, 39, 7, 8},
            {37, 39, 9, 12}, {36, 36, 9, 12}, {36, 36, 1, 8}, {40, 40, 1, 12},
            {41, 45, 1, 12}, {47, 51, 1, 12}, {53, 56, 1, 5}, {53, 56, 8, 12},
            {2, 3, 4, 9}
        };
        
        for (int[] region : regions) {
            sh.addMergedRegion(new CellRangeAddress(region[0], region[1], region[2], region[3]));
        }
    }
    
    /**
     * Pobla todas las celdas del template.
     */
    private void populateAllCells() {
        // Dividido en métodos separados para evitar exceder límite
        populateCellsRow1to10();
        populateCellsRow11to20();
        populateCellsRow21to30();
        populateCellsRow31to40();
        populateCellsRow41to50();
        populateCellsRow51to59();
    }
    
    // Los métodos populateCellsRowXtoY contienen la lógica de crear celdas
    // Se implementan en TemplateBuilderCells.java para mantener este archivo manejable
    
    private void populateCellsRow1to10() {
        TemplateBuilderCells.populateRow1to10(sh, styles, dia, mes, anio);
    }
    
    private void populateCellsRow11to20() {
        TemplateBuilderCells.populateRow11to20(sh, styles, SYS_TIPO, SYS_MARCA, SYS_MODELO);
    }
    
    private void populateCellsRow21to30() {
        TemplateBuilderCells.populateRow21to30(sh, styles, dia, mes, anio, SYS_CPU_SHORT, SYS_RAM_SHORT, SYS_DISK_SHORT);
    }
    
    private void populateCellsRow31to40() {
        TemplateBuilderCells.populateRow31to40(sh, styles);
    }
    
    private void populateCellsRow41to50() {
        TemplateBuilderCells.populateRow41to50(sh, styles);
    }
    
    private void populateCellsRow51to59() {
        TemplateBuilderCells.populateRow51to59(sh, styles, style70b);
    }
    
    /**
     * Escribe los datos del formulario en las celdas correspondientes.
     */
    private void writeFormData() {
        // Escribir aplicaciones esenciales
        try {
            AppsWriter.writeEssentialApps(sh, sysInfo, styles.get(35));
        } catch (Throwable _e) {}
        
        // Forzar escritura de fecha
        Row fechaRow = sh.getRow(6);
        if (fechaRow == null) fechaRow = sh.createRow(6);
        
        Cell cellDia = fechaRow.getCell(10);
        if (cellDia == null) cellDia = fechaRow.createCell(10);
        cellDia.setCellValue(dia);
        cellDia.setCellStyle(styles.get(32));
        
        Cell cellMes = fechaRow.getCell(11);
        if (cellMes == null) cellMes = fechaRow.createCell(11);
        cellMes.setCellValue(mes);
        cellMes.setCellStyle(styles.get(33));
        
        Cell cellAnio = fechaRow.getCell(12);
        if (cellAnio == null) cellAnio = fechaRow.createCell(12);
        cellAnio.setCellValue(anio);
        cellAnio.setCellStyle(styles.get(32));
        
        // Escribir B8
        Row b8row = sh.getRow(7);
        if (b8row == null) b8row = sh.createRow(7);
        Cell b8 = b8row.getCell(1);
        if (b8 == null) b8 = b8row.createCell(1);
        b8.setCellValue("TICKET N*");
        try { b8.setCellStyle(styles.get(28)); } catch (Exception e) {}
        
        // Datos del usuario
        ExcelCeldaHelper.escribirCelda(sh, "D10", FORM_CIUDAD);
        ExcelCeldaHelper.escribirCelda(sh, "D11", FORM_DIRECCION);
        ExcelCeldaHelper.escribirCelda(sh, "D12", FORM_NOMBRE);
        ExcelCeldaHelper.escribirCelda(sh, "D13", FORM_CORREO);
        ExcelCeldaHelper.escribirCelda(sh, "D14", FORM_TECNICO);
        ExcelCeldaHelper.escribirCelda(sh, "D15", FORM_SEDE);
        
        // Ticket
        ExcelCeldaHelper.escribirCelda(sh, "E8", FORM_TICKET);
        
        // Hardware — merges: B20:C22 (tipo), D20:E22 (marca), F20:G22 (modelo)
        ExcelCeldaHelper.escribirCelda(sh, "B20", SYS_TIPO);
        ExcelCeldaHelper.escribirCelda(sh, "D20", SYS_MARCA);
        ExcelCeldaHelper.escribirCelda(sh, "F20", SYS_MODELO);
        ExcelCeldaHelper.escribirCelda(sh, "H20", FORM_SERIAL);
        ExcelCeldaHelper.escribirCelda(sh, "J20", FORM_PLACA);
        ExcelCeldaHelper.escribirCelda(sh, "K20", FORM_CONDICIONES);
        
        // PC
        ExcelCeldaHelper.escribirCelda(sh, "B26", FORM_PC_ENCIENDE);
        ExcelCeldaHelper.escribirCelda(sh, "D26", FORM_DISCO_DURO);
        ExcelCeldaHelper.escribirCelda(sh, "F26", FORM_CDDVD);
        ExcelCeldaHelper.escribirCelda(sh, "H26", FORM_BOTONES_PC);
        ExcelCeldaHelper.escribirCelda(sh, "K26", FORM_CONDICIONES_PC);
        
        // Monitor
        ExcelCeldaHelper.escribirCelda(sh, "B31", FORM_MONITOR_ENCIENDE);
        ExcelCeldaHelper.escribirCelda(sh, "D31", FORM_PANTALLA);
        ExcelCeldaHelper.escribirCelda(sh, "F31", FORM_ONLY_ONE);
        ExcelCeldaHelper.escribirCelda(sh, "H31", FORM_BOTONES_MONITOR);
        ExcelCeldaHelper.escribirCelda(sh, "K31", FORM_CONDICIONES_MONITOR);
        
        // Teclado
        ExcelCeldaHelper.escribirCelda(sh, "B35", FORM_TECLADO_ENCIENDE);
        ExcelCeldaHelper.escribirCelda(sh, "D35", FORM_TECLADO_FUNCIONA);
        ExcelCeldaHelper.escribirCelda(sh, "H35", FORM_BOTONES_TECLADO);
        ExcelCeldaHelper.escribirCelda(sh, "K35", FORM_CONDICIONES_TECLADO);
        
        // Procedimiento y observaciones
        ExcelCeldaHelper.escribirCeldaConAjuste(sh, "B42", FORM_TRABAJO_REALIZADO);
        ExcelCeldaHelper.escribirCeldaConAjuste(sh, "B48", FORM_OBSERVACIONES);
        
        // Firmas
        ExcelCeldaHelper.escribirCelda(sh, "D59", datosForm.firmaTecnico);
        ExcelCeldaHelper.escribirCelda(sh, "D60", datosForm.cedulaTecnico);
        ExcelCeldaHelper.escribirCelda(sh, "K59", datosForm.firmaFuncionario);
        ExcelCeldaHelper.escribirCelda(sh, "K60", datosForm.cedulaFuncionario);
        
        System.out.println("=== DATOS ESCRITOS EN EXCEL (GENERADO) ===");
        System.out.println("Ciudad: " + FORM_CIUDAD);
        System.out.println("Nombre: " + FORM_NOMBRE);
        System.out.println("Procedimiento: " + FORM_TRABAJO_REALIZADO);
        System.out.println("Observaciones: " + FORM_OBSERVACIONES);
        System.out.println("==========================================");
    }
    
    /**
     * Aplica bordes faltantes a celdas específicas: E8, K6, L6, M6, K7, L7, M7, M27.
     */
    private void applyMissingBorders() {
        // E3 hasta J4 (filas 2-3 en 0-based, columnas 4-9) - Área combinada
        applyCellBorders(2, 4, 9);   // E3:J3
        applyCellBorders(3, 4, 9);   // E4:J4
        
        // E8 hasta I8 (fila 7 en 0-based, columnas 4-9) - Celda de TICKET valor
        applyCellBorders(7, 4, 9);
        
        // K6, L6, M6 (fila 5 en 0-based) - Encabezados Día, Mes, Año
        applyCellBorders(5, 10, 12);
        
        // K7, L7, M7 (fila 6 en 0-based) - Celdas de fecha valores
        applyCellBorders(6, 10, 12);
        
        // K8, L8, M8 (fila 7 en 0-based) - Valores día, mes, año
        applyCellBorders(7, 10, 12);
        
        // M27 (fila 26 en 0-based, columna 12 = M)
        applyCellBorders(26, 12, 12);
        
        // Datos del formulario D10:M15 (filas 9-14, columnas 3-12)
        applyCellBorders(9, 3, 12);   // D10:M10
        applyCellBorders(10, 3, 12);  // D11:M11
        applyCellBorders(11, 3, 12);  // D12:M12
        applyCellBorders(12, 3, 12);  // D13:M13
        applyCellBorders(13, 3, 12);  // D14:M14
        applyCellBorders(14, 3, 12);  // D15:M15
        
        // Checkboxes B18:I19 (filas 17-18, columnas 1-8)
        applyCellBorders(17, 1, 8);   // B18:I18
        applyCellBorders(18, 1, 8);   // B19:I19
        
        // Checkboxes J18:M19 (filas 17-18, columnas 9-12)
        applyCellBorders(17, 9, 12);  // J18:M18
        applyCellBorders(18, 9, 12);  // J19:M19
        
        // Checkboxes B20:I22 (filas 19-21, columnas 1-8)
        applyCellBorders(19, 1, 8);   // B20:I20
        applyCellBorders(20, 1, 8);   // B21:I21
        applyCellBorders(21, 1, 8);   // B22:I22
        
        // Checkboxes J20:M22 (filas 19-21, columnas 9-12)
        applyCellBorders(19, 9, 12);  // J20:M20
        applyCellBorders(20, 9, 12);  // J21:M21
        applyCellBorders(21, 9, 12);  // J22:M22
        
        // Fila 23 B23:M23 (fila 22, columnas 1-12) - Título "INSPECCIÓN DEL EQUIPO"
        applyCellBorders(22, 1, 12);
        
        // Celdas de PC B24:M27 (filas 23-26)
        applyCellBorders(23, 1, 12);  // B24:M24
        applyCellBorders(24, 1, 12);  // B25:M25
        applyCellBorders(25, 1, 12);  // B26:M26
        applyCellBorders(26, 1, 12);  // B27:M27
        
        // Bordes columna M parte superior (K2:M5)
        applyCellBorders(1, 10, 12);  // K2:M2
        applyCellBorders(2, 10, 12);  // K3:M3
        applyCellBorders(3, 10, 12);  // K4:M4
        applyCellBorders(4, 10, 12);  // K5:M5
        
        // Columna M desde fila 6 hasta fila 37 (excluir filas 38-40 donde AppsWriter escribe celdas fusionadas)
        for (int fila = 5; fila <= 36; fila++) {
            applyCellBorders(fila, 12, 12);  // Columna M
        }
        
        // Firmas B54:F57 y I54:M57 (filas 53-56)
        for (int fila = 53; fila <= 56; fila++) {
            applyCellBorders(fila, 1, 5);   // B54:F57
            applyCellBorders(fila, 8, 12);  // I54:M57
        }
    }
    
    /**
     * Aplica bordes a una celda o rango de celdas preservando el contenido y estilo existente.
     */
    private void applyCellBorders(int rowIdx, int startCol, int endCol) {
        Row row = sh.getRow(rowIdx);
        if (row == null) row = sh.createRow(rowIdx);
        
        for (int col = startCol; col <= endCol; col++) {
            Cell cell = row.getCell(col);
            if (cell == null) cell = row.createCell(col);
            
            // Clonar estilo existente y agregar bordes
            CellStyle existingStyle = cell.getCellStyle();
            CellStyle newStyle = wb.createCellStyle();
            newStyle.cloneStyleFrom(existingStyle);
            
            // Aplicar bordes
            newStyle.setBorderTop(BorderStyle.THIN);
            newStyle.setBorderBottom(BorderStyle.THIN);
            newStyle.setBorderLeft(BorderStyle.THIN);
            newStyle.setBorderRight(BorderStyle.THIN);
            
            cell.setCellStyle(newStyle);
        }
    }
    
    /**
     * Inserta las imágenes en el template.
     */
    private void insertImages() {
        // Crear un solo DrawingPatriarch para todas las imágenes
        Drawing<?> drawing = sh.createDrawingPatriarch();
        
        // Imagen del proyecto - Se ajusta automáticamente a las celdas B2:D4
        String proyectoImagenPath = datosForm.proyectoImagenPath;
        if (proyectoImagenPath != null && !proyectoImagenPath.isEmpty()) {
            try {
                InputStream imgProyectoStream = null;
                
                if (proyectoImagenPath.startsWith("/")) {
                    imgProyectoStream = GeneratedTemplate.class.getResourceAsStream(proyectoImagenPath);
                }
                
                if (imgProyectoStream == null) {
                    File imgFile = new File(proyectoImagenPath);
                    if (imgFile.exists()) {
                        imgProyectoStream = new FileInputStream(imgFile);
                    }
                }
                
                if (imgProyectoStream != null) {
                    byte[] bytesProyecto = IOUtils.toByteArray(imgProyectoStream);
                    int pictureType = proyectoImagenPath.toLowerCase().endsWith(".png") ? 
                        Workbook.PICTURE_TYPE_PNG : Workbook.PICTURE_TYPE_JPEG;
                    int pictureIdxProyecto = wb.addPicture(bytesProyecto, pictureType);
                    imgProyectoStream.close();
                    
                    // Deshabilitar caché de ImageIO para asegurar que se lean imágenes actualizadas
                    javax.imageio.ImageIO.setUseCache(false);
                    
                    // Obtener dimensiones originales de la imagen
                    javax.imageio.ImageIO.setUseCache(false);
                    java.awt.image.BufferedImage imgOriginal = javax.imageio.ImageIO.read(
                        new java.io.ByteArrayInputStream(bytesProyecto));
                    int imgAncho = imgOriginal.getWidth();
                    int imgAlto = imgOriginal.getHeight();
                    double aspectRatio = (double) imgAncho / imgAlto;
                    
                    // Calcular el área disponible para la imagen (B2:D4)
                    // Columnas B, C, D (índices 1, 2, 3) -> hasta columna E (índice 4)
                    double anchoColumnaB = sh.getColumnWidthInPixels(1);
                    double anchoColumnaC = sh.getColumnWidthInPixels(2);
                    double anchoColumnaD = sh.getColumnWidthInPixels(3);
                    double anchoTotalCeldas = anchoColumnaB + anchoColumnaC + anchoColumnaD;
                    
                    // Filas 2, 3, 4 (índices 1, 2, 3) -> hasta fila 5 (índice 4)
                    float altoFila2 = sh.getRow(1) != null ? sh.getRow(1).getHeightInPoints() : 20f;
                    float altoFila3 = sh.getRow(2) != null ? sh.getRow(2).getHeightInPoints() : 20f;
                    float altoFila4 = sh.getRow(3) != null ? sh.getRow(3).getHeightInPoints() : 20f;
                    double altoTotalCeldas = (altoFila2 + altoFila3 + altoFila4) * 96.0 / 72.0;
                    
                    // Margen pequeño
                    int margen = 5;
                    double anchoDisponible = anchoTotalCeldas - (margen * 2);
                    double altoDisponible = altoTotalCeldas - (margen * 2);
                    double aspectRatioArea = anchoDisponible / altoDisponible;
                    
                    // Calcular dimensiones finales manteniendo proporción
                    double imgAnchoFinal, imgAltoFinal;
                    if (aspectRatio > aspectRatioArea) {
                        // Imagen más ancha que el área - ajustar por ancho
                        imgAnchoFinal = anchoDisponible;
                        imgAltoFinal = anchoDisponible / aspectRatio;
                    } else {
                        // Imagen más alta que el área - ajustar por alto
                        imgAltoFinal = altoDisponible;
                        imgAnchoFinal = altoDisponible * aspectRatio;
                    }
                    
                    // Calcular offsets para centrar
                    double offsetX = margen + (anchoDisponible - imgAnchoFinal) / 2;
                    double offsetY = margen + (altoDisponible - imgAltoFinal) / 2 + 8; // +8 para bajar un poco
                    
                    // Usar ancla de DOS PUNTOS para definir exactamente el área de la imagen
                    // Esto FUERZA a la imagen a ajustarse al área definida
                    ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
                    anchor.setCol1(1); // Columna B (inicio)
                    anchor.setRow1(1); // Fila 2 (inicio)
                    anchor.setDx1((int)(Units.EMU_PER_PIXEL * offsetX));
                    anchor.setDy1((int)(Units.EMU_PER_PIXEL * offsetY));
                    
                    // Calcular posición final (col2, row2) basándose en el tamaño de la imagen
                    // La imagen terminará donde corresponda según su tamaño calculado
                    anchor.setCol2(4); // Columna E (fin - después de D)
                    anchor.setRow2(4); // Fila 5 (fin - después de fila 4)
                    
                    // Calcular dx2, dy2 para el punto final
                    double xFinal = offsetX + imgAnchoFinal;
                    double yFinal = offsetY + imgAltoFinal;
                    
                    // Ajustar dx2, dy2 para que la imagen termine en el lugar correcto
                    double restanteX = anchoTotalCeldas - xFinal;
                    double restanteY = altoTotalCeldas - yFinal;
                    
                    anchor.setDx2((int)(Units.EMU_PER_PIXEL * (-restanteX + margen)));
                    anchor.setDy2((int)(Units.EMU_PER_PIXEL * (-restanteY + margen)));
                    
                    // MOVE_AND_RESIZE permite que la imagen se ajuste al ancla
                    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                    
                    // Crear la imagen con el ancla de dos puntos usando el drawing compartido
                    drawing.createPicture(anchor, pictureIdxProyecto);
                    
                    System.out.println("✓ Imagen del proyecto ajustada en B2:D4: " + proyectoImagenPath);
                    System.out.println("  Original: " + imgAncho + "x" + imgAlto + 
                        " -> Área: " + (int)imgAnchoFinal + "x" + (int)imgAltoFinal + "px");
                }
            } catch (Exception imgProyectoEx) {
                AppLogger.getLogger(TemplateBuilder.class).error("Error al insertar imagen del proyecto: " + imgProyectoEx.getMessage(), imgProyectoEx);
            }
        }
        
        // Logo Selcomp
        try {
            InputStream imgStream = GeneratedTemplate.class.getResourceAsStream("/images/Selcomp3.png");
            if (imgStream != null) {
                byte[] bytes = IOUtils.toByteArray(imgStream);
                int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                imgStream.close();
                
                // Usar el mismo drawing para el logo Selcomp
                ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
                anchor.setCol1(10);
                anchor.setRow1(1);
                anchor.setDx1(Units.EMU_PER_PIXEL * 10);
                anchor.setDy1(Units.EMU_PER_PIXEL * 15);
                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
                
                Picture picture = drawing.createPicture(anchor, pictureIdx);
                picture.resize(0.22);
                
                System.out.println("✓ Imagen Selcomp.png insertada correctamente en K2:M4");
            }
        } catch (Exception imgEx) {
            System.err.println("Error al insertar imagen: " + imgEx.getMessage());
        }
        
        // ═══════════════════════════════════════════════════════════════════
        // FIRMAS DIGITALES
        // ═══════════════════════════════════════════════════════════════════
        
        System.out.println("[TemplateBuilder] Procesando firmas digitales...");
        System.out.println("[TemplateBuilder] Ruta firma técnico: " + datosForm.firmaTecnicoImagenPath);
        System.out.println("[TemplateBuilder] Ruta firma funcionario: " + datosForm.firmaFuncionarioImagenPath);
        
        // Firma del Técnico (B54:F57) - Sistema avanzado igual que imagen del proyecto
        String firmaTecnicoPath = datosForm.firmaTecnicoImagenPath;
        if (firmaTecnicoPath != null && !firmaTecnicoPath.isEmpty()) {
            try {
                File imgFile = new File(firmaTecnicoPath);
                System.out.println("[TemplateBuilder] Verificando archivo firma técnico: " + imgFile.getAbsolutePath());
                System.out.println("[TemplateBuilder] Archivo existe: " + imgFile.exists());
                if (imgFile.exists()) {
                    InputStream imgStream = new FileInputStream(imgFile);
                    byte[] bytes = IOUtils.toByteArray(imgStream);
                    int pictureType = firmaTecnicoPath.toLowerCase().endsWith(".png") ? 
                        Workbook.PICTURE_TYPE_PNG : Workbook.PICTURE_TYPE_JPEG;
                    int pictureIdx = wb.addPicture(bytes, pictureType);
                    imgStream.close();
                    
                    // Deshabilitar caché de ImageIO
                    javax.imageio.ImageIO.setUseCache(false);
                    
                    // Obtener dimensiones originales de la firma
                    java.awt.image.BufferedImage imgOriginal = javax.imageio.ImageIO.read(
                        new java.io.ByteArrayInputStream(bytes));
                    int imgAncho = imgOriginal.getWidth();
                    int imgAlto = imgOriginal.getHeight();
                    double aspectRatio = (double) imgAncho / imgAlto;
                    
                    // Calcular área disponible B54:F57 (columnas 1-5, filas 53-56)
                    double anchoColumnaB = sh.getColumnWidthInPixels(1);
                    double anchoColumnaC = sh.getColumnWidthInPixels(2);
                    double anchoColumnaD = sh.getColumnWidthInPixels(3);
                    double anchoColumnaE = sh.getColumnWidthInPixels(4);
                    double anchoColumnaF = sh.getColumnWidthInPixels(5);
                    double anchoTotalCeldas = anchoColumnaB + anchoColumnaC + anchoColumnaD + anchoColumnaE + anchoColumnaF;
                    
                    float altoFila54 = sh.getRow(53) != null ? sh.getRow(53).getHeightInPoints() : 20f;
                    float altoFila55 = sh.getRow(54) != null ? sh.getRow(54).getHeightInPoints() : 20f;
                    float altoFila56 = sh.getRow(55) != null ? sh.getRow(55).getHeightInPoints() : 20f;
                    float altoFila57 = sh.getRow(56) != null ? sh.getRow(56).getHeightInPoints() : 20f;
                    double altoTotalCeldas = (altoFila54 + altoFila55 + altoFila56 + altoFila57) * 96.0 / 72.0;
                    
                    // Margen reducido para firmas más grandes
                    int margen = 2;
                    double anchoDisponible = anchoTotalCeldas - (margen * 2);
                    double altoDisponible = altoTotalCeldas - (margen * 2);
                    double aspectRatioArea = anchoDisponible / altoDisponible;
                    
                    // Calcular dimensiones finales manteniendo proporción
                    double imgAnchoFinal, imgAltoFinal;
                    if (aspectRatio > aspectRatioArea) {
                        imgAnchoFinal = anchoDisponible;
                        imgAltoFinal = anchoDisponible / aspectRatio;
                    } else {
                        imgAltoFinal = altoDisponible;
                        imgAnchoFinal = altoDisponible * aspectRatio;
                    }
                    
                    // Factor de escala para hacer las firmas un 20% más grandes
                    double factorEscala = 1.20;
                    imgAnchoFinal *= factorEscala;
                    imgAltoFinal *= factorEscala;
                    
                    // Calcular offsets para centrar (permitir que se salga un poco)
                    double offsetX = margen + (anchoDisponible - imgAnchoFinal) / 2;
                    double offsetY = margen + (altoDisponible - imgAltoFinal) / 2;
                    
                    // Crear ancla con sistema de dos puntos
                    ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
                    anchor.setCol1(1);  // Columna B
                    anchor.setRow1(53); // Fila 54
                    anchor.setDx1((int)(Units.EMU_PER_PIXEL * offsetX));
                    anchor.setDy1((int)(Units.EMU_PER_PIXEL * offsetY));
                    
                    anchor.setCol2(6);  // Columna G (después de F)
                    anchor.setRow2(57); // Fila 58 (después de 57)
                    
                    double xFinal = offsetX + imgAnchoFinal;
                    double yFinal = offsetY + imgAltoFinal;
                    double restanteX = anchoTotalCeldas - xFinal;
                    double restanteY = altoTotalCeldas - yFinal;
                    
                    anchor.setDx2((int)(Units.EMU_PER_PIXEL * (-restanteX + margen)));
                    anchor.setDy2((int)(Units.EMU_PER_PIXEL * (-restanteY + margen)));
                    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                    
                    drawing.createPicture(anchor, pictureIdx);
                    System.out.println("✓ Firma del técnico insertada exitosamente en B54:F57");
                    System.out.println("  Original: " + imgAncho + "x" + imgAlto + 
                        " -> Ajustada: " + (int)imgAnchoFinal + "x" + (int)imgAltoFinal + "px");
                } else {
                    System.err.println("✗ Archivo de firma técnico no existe: " + imgFile.getAbsolutePath());
                }
            } catch (Exception ex) {
                AppLogger.getLogger(TemplateBuilder.class).error("Error al insertar firma del técnico: " + ex.getMessage(), ex);
            }
        }
        
        // Firma del Funcionario (I54:M57) - Sistema avanzado igual que imagen del proyecto
        String firmaFuncionarioPath = datosForm.firmaFuncionarioImagenPath;
        if (firmaFuncionarioPath != null && !firmaFuncionarioPath.isEmpty()) {
            try {
                File imgFile = new File(firmaFuncionarioPath);
                System.out.println("[TemplateBuilder] Verificando archivo firma funcionario: " + imgFile.getAbsolutePath());
                System.out.println("[TemplateBuilder] Archivo existe: " + imgFile.exists());
                if (imgFile.exists()) {
                    InputStream imgStream = new FileInputStream(imgFile);
                    byte[] bytes = IOUtils.toByteArray(imgStream);
                    int pictureType = firmaFuncionarioPath.toLowerCase().endsWith(".png") ? 
                        Workbook.PICTURE_TYPE_PNG : Workbook.PICTURE_TYPE_JPEG;
                    int pictureIdx = wb.addPicture(bytes, pictureType);
                    imgStream.close();
                    
                    // Deshabilitar caché de ImageIO
                    javax.imageio.ImageIO.setUseCache(false);
                    
                    // Obtener dimensiones originales de la firma
                    java.awt.image.BufferedImage imgOriginal = javax.imageio.ImageIO.read(
                        new java.io.ByteArrayInputStream(bytes));
                    int imgAncho = imgOriginal.getWidth();
                    int imgAlto = imgOriginal.getHeight();
                    double aspectRatio = (double) imgAncho / imgAlto;
                    
                    // Calcular área disponible I54:M57 (columnas 8-12, filas 53-56)
                    double anchoColumnaI = sh.getColumnWidthInPixels(8);
                    double anchoColumnaJ = sh.getColumnWidthInPixels(9);
                    double anchoColumnaK = sh.getColumnWidthInPixels(10);
                    double anchoColumnaL = sh.getColumnWidthInPixels(11);
                    double anchoColumnaM = sh.getColumnWidthInPixels(12);
                    double anchoTotalCeldas = anchoColumnaI + anchoColumnaJ + anchoColumnaK + anchoColumnaL + anchoColumnaM;
                    
                    float altoFila54 = sh.getRow(53) != null ? sh.getRow(53).getHeightInPoints() : 20f;
                    float altoFila55 = sh.getRow(54) != null ? sh.getRow(54).getHeightInPoints() : 20f;
                    float altoFila56 = sh.getRow(55) != null ? sh.getRow(55).getHeightInPoints() : 20f;
                    float altoFila57 = sh.getRow(56) != null ? sh.getRow(56).getHeightInPoints() : 20f;
                    double altoTotalCeldas = (altoFila54 + altoFila55 + altoFila56 + altoFila57) * 96.0 / 72.0;
                    
                    // Margen reducido para firmas más grandes
                    int margen = 2;
                    double anchoDisponible = anchoTotalCeldas - (margen * 2);
                    double altoDisponible = altoTotalCeldas - (margen * 2);
                    double aspectRatioArea = anchoDisponible / altoDisponible;
                    
                    // Calcular dimensiones finales manteniendo proporción
                    double imgAnchoFinal, imgAltoFinal;
                    if (aspectRatio > aspectRatioArea) {
                        imgAnchoFinal = anchoDisponible;
                        imgAltoFinal = anchoDisponible / aspectRatio;
                    } else {
                        imgAltoFinal = altoDisponible;
                        imgAnchoFinal = altoDisponible * aspectRatio;
                    }
                    
                    // Factor de escala para hacer las firmas un 20% más grandes
                    double factorEscala = 1.20;
                    imgAnchoFinal *= factorEscala;
                    imgAltoFinal *= factorEscala;
                    
                    // Calcular offsets para centrar (permitir que se salga un poco)
                    double offsetX = margen + (anchoDisponible - imgAnchoFinal) / 2;
                    double offsetY = margen + (altoDisponible - imgAltoFinal) / 2;
                    
                    // Crear ancla con sistema de dos puntos
                    ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
                    anchor.setCol1(8);  // Columna I
                    anchor.setRow1(53); // Fila 54
                    anchor.setDx1((int)(Units.EMU_PER_PIXEL * offsetX));
                    anchor.setDy1((int)(Units.EMU_PER_PIXEL * offsetY));
                    
                    anchor.setCol2(13); // Columna N (después de M)
                    anchor.setRow2(57); // Fila 58 (después de 57)
                    
                    double xFinal = offsetX + imgAnchoFinal;
                    double yFinal = offsetY + imgAltoFinal;
                    double restanteX = anchoTotalCeldas - xFinal;
                    double restanteY = altoTotalCeldas - yFinal;
                    
                    anchor.setDx2((int)(Units.EMU_PER_PIXEL * (-restanteX + margen)));
                    anchor.setDy2((int)(Units.EMU_PER_PIXEL * (-restanteY + margen)));
                    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                    
                    drawing.createPicture(anchor, pictureIdx);
                    System.out.println("✓ Firma del funcionario insertada exitosamente en I54:M57");
                    System.out.println("  Original: " + imgAncho + "x" + imgAlto + 
                        " -> Ajustada: " + (int)imgAnchoFinal + "x" + (int)imgAltoFinal + "px");
                } else {
                    System.err.println("✗ Archivo de firma funcionario no existe: " + imgFile.getAbsolutePath());
                }
            } catch (Exception ex) {
                AppLogger.getLogger(TemplateBuilder.class).error("Error al insertar firma del funcionario: " + ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Guarda el archivo Excel.
     */
    private void saveFile() {
        String rutaDestino = System.getProperty("reporte.rutaDestino", "");
        String nombreArchivo = !rutaDestino.isEmpty() ? rutaDestino : "FORMATO_RECREADO_FECHA_AUTOMATICA.xlsx";
        boolean escrito = false;
        
        try {
            try (FileOutputStream fos = new FileOutputStream(nombreArchivo)) {
                wb.write(fos);
            }
            escrito = true;
        } catch (java.io.FileNotFoundException fnf) {
            String alt = !rutaDestino.isEmpty() ? rutaDestino.replace(".xlsx", "_1.xlsx") : "FORMATO_RECREADO_FECHA_AUTOMATICA_1.xlsx";
            try {
                try (FileOutputStream fos = new FileOutputStream(alt)) {
                    wb.write(fos);
                }
                nombreArchivo = alt;
                escrito = true;
            } catch (Throwable t2) {
                System.err.println("No se pudo escribir el archivo: " + t2.getMessage());
            }
        } catch (Throwable t) {
            System.err.println("Error al escribir el archivo: " + t.getMessage());
        }
        
        if (escrito) {
            System.out.println("Archivo guardado: " + nombreArchivo);
        }
        
        try { wb.close(); } catch (Exception _e) {}
    }
    
    // Getters para acceso desde otras clases
    public List<CellStyle> getStyles() { return styles; }
    public Workbook getWorkbook() { return wb; }
    public Sheet getSheet() { return sh; }
}
