package inventario.fx.excel;
import inventario.fx.model.InventarioFXBase;
import inventario.fx.ui.panel.ReporteFormularioFX;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class GeneratedTemplate {
        // Obtener fecha actual
        java.time.LocalDate fechaActual = java.time.LocalDate.now();
        int dia = fechaActual.getDayOfMonth();
        int mes = fechaActual.getMonthValue();
        int anio = fechaActual.getYear();
    public static void main(String[] args) throws Exception {
        // Fecha actual
        java.time.LocalDate fechaActual = java.time.LocalDate.now();
        int dia = fechaActual.getDayOfMonth();
        int mes = fechaActual.getMonthValue();
        int anio = fechaActual.getYear();

        // ═══════════════════════════════════════════════════════════════════════════
        // OBTENER DATOS DEL FORMULARIO PRIMERO
        // ═══════════════════════════════════════════════════════════════════════════
        ReporteFormularioFX.DatosReporte datosForm = ReporteFormularioFX.obtenerDatos();
        
        // DEBUG: Imprimir datos recibidos
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
        
        // Ruta de destino del archivo
        String rutaDestino = System.getProperty("reporte.rutaDestino", "");
        System.out.println("Ruta destino: " + rutaDestino);

        // Crear nuevo libro y hoja de Excel
        Workbook wb = new XSSFWorkbook();
        Sheet sh = wb.createSheet("Formato Mantenimiento");
        
        // Los datos del formulario ya fueron obtenidos al inicio del método
        
        // Usar datos del formulario si están disponibles, sino usar datos del sistema
        final String FORM_TICKET = datosForm.ticket;
        final String FORM_CIUDAD = datosForm.ciudad;
        final String FORM_DIRECCION = datosForm.direccion;
        final String FORM_NOMBRE = datosForm.nombre;
        final String FORM_CORREO = datosForm.correo;
        final String FORM_TECNICO = datosForm.tecnico;
        final String FORM_SEDE = datosForm.sede;
        final String FORM_CONDICIONES = datosForm.condiciones;
        final String FORM_SERIAL = datosForm.serial;
        final String FORM_PLACA = datosForm.placa;
        
        // Datos PC del formulario
        final String FORM_PC_ENCIENDE = datosForm.pcEnciende;
        final String FORM_DISCO_DURO = datosForm.discoDuro;
        final String FORM_CDDVD = datosForm.cddvd;
        final String FORM_BOTONES_PC = datosForm.botonesPC;
        final String FORM_CONDICIONES_PC = datosForm.condicionesPC;
        
        // Datos Monitor del formulario
        final String FORM_MONITOR_ENCIENDE = datosForm.monitorEnciende;
        final String FORM_PANTALLA = datosForm.pantalla;
        final String FORM_ONLY_ONE = datosForm.onlyOne;
        final String FORM_BOTONES_MONITOR = datosForm.botonesMonitor;
        final String FORM_CONDICIONES_MONITOR = datosForm.condicionesMonitor;
        
        // Datos Teclado del formulario
        final String FORM_TECLADO_ENCIENDE = datosForm.tecladoEnciende;
        final String FORM_TECLADO_FUNCIONA = datosForm.tecladoFunciona;
        final String FORM_BOTONES_TECLADO = datosForm.botonesTeclado;
        final String FORM_CONDICIONES_TECLADO = datosForm.condicionesTeclado;
        
        // Datos Mouse del formulario
        final String FORM_MOUSE_ENCIENDE = datosForm.mouseEnciende;
        final String FORM_MOUSE_FUNCIONA = datosForm.mouseFunciona;
        final String FORM_BOTONES_MOUSE = datosForm.botonesMouse;
        final String FORM_CONDICIONES_MOUSE = datosForm.condicionesMouse;
        
        // Observaciones y trabajo
        final String FORM_OBSERVACIONES = datosForm.observaciones;
        final String FORM_TRABAJO_REALIZADO = datosForm.trabajoRealizado;
        
        // Fecha del formulario (si existe, sino usar fecha actual)
        if (!datosForm.nombre.isEmpty()) {
            try { dia = Integer.parseInt(datosForm.dia); } catch (Exception e) {}
            try { mes = Integer.parseInt(datosForm.mes); } catch (Exception e) {}
            try { anio = Integer.parseInt(datosForm.anio); } catch (Exception e) {}
        }
        
        // Obtener información del sistema (marca/modelo/tipo) para rellenar el template
        InventarioFXBase.InfoPC sysInfo = null;
        try {
            sysInfo = InventarioFXBase.recopilarInfo();
        } catch (Throwable _ignore) {
            sysInfo = null;
        }
        
        // Usar datos del formulario si están disponibles, sino datos del sistema
        final String SYS_TIPO = !datosForm.tipoDispositivo.isEmpty() ? datosForm.tipoDispositivo : 
            (sysInfo != null && sysInfo.deviceType != null) ? sysInfo.deviceType : "";
        final String SYS_MARCA = !datosForm.marca.isEmpty() ? datosForm.marca :
            (sysInfo != null && sysInfo.manufacturer != null) ? sysInfo.manufacturer : "";
        final String SYS_MODELO = !datosForm.modelo.isEmpty() ? datosForm.modelo :
            (sysInfo != null && sysInfo.modeloEquipo != null) ? sysInfo.modeloEquipo : "";
        final String SYS_CPU = (sysInfo != null && sysInfo.cpu != null) ? sysInfo.cpu : "";

        // Abreviación del CPU solo para la plantilla generada aquí.
        String _SYS_CPU_SHORT_TMP = "";
        {
            String _s = SYS_CPU == null ? "" : SYS_CPU;
            String _short = "";
            try {
                // Eliminar paréntesis y su contenido (R), (TM), etc.
                _short = _s.replaceAll("\\([^)]*\\)", "");
                // Quitar la parte después de '@' (frecuencia)
                int at = _short.indexOf('@');
                if (at >= 0) _short = _short.substring(0, at);
                // Quitar la palabra CPU y caracteres extraños
                _short = _short.replaceAll("(?i)\\bCPU\\b", "");
                _short = _short.replaceAll("[^\\p{L}\\p{N}\\.\\-\\s]", " ");
                _short = _short.replaceAll("\\s+", " ").trim();

                // Intentar detectar Intel Core (i3/i5/i7/i9) con número de modelo
                Pattern pCoreModel = Pattern.compile("(?i)\\bcore\\b.*?(i\\d)[\\s\\-]*(\\d{3,5}\\w*)");
                Matcher mCoreModel = pCoreModel.matcher(_short);
                if (mCoreModel.find()) {
                    // Ej: "Core i7-8700" o "Core i5 10400"
                    _SYS_CPU_SHORT_TMP = "Core " + mCoreModel.group(1).toLowerCase() + "-" + mCoreModel.group(2);
                } else {
                    // Intentar detectar solo Intel Core iX
                    Pattern pCore = Pattern.compile("(?i)\\bcore\\b.*?(i\\d)");
                    Matcher mCore = pCore.matcher(_short);
                    if (mCore.find()) {
                        _SYS_CPU_SHORT_TMP = "Core " + mCore.group(1).toLowerCase();
                    } else {
                        // Intel con iX más disperso
                        Pattern pIntel = Pattern.compile("(?i)\\bintel\\b.*?(i\\d)[\\s\\-]*(\\d{3,5}\\w*)?");
                        Matcher mIntel = pIntel.matcher(_short);
                        if (mIntel.find()) {
                            if (mIntel.group(2) != null) {
                                _SYS_CPU_SHORT_TMP = "Intel " + mIntel.group(1).toLowerCase() + "-" + mIntel.group(2);
                            } else {
                                _SYS_CPU_SHORT_TMP = "Intel " + mIntel.group(1).toLowerCase();
                            }
                        } else {
                            // AMD Ryzen con modelo
                            Pattern pRyzen = Pattern.compile("(?i)\\bryzen\\b\\s*(\\d)[\\s\\-]*(\\d{4}\\w*)?");
                            Matcher mRyzen = pRyzen.matcher(_short);
                            if (mRyzen.find()) {
                                if (mRyzen.group(2) != null) {
                                    _SYS_CPU_SHORT_TMP = "Ryzen " + mRyzen.group(1) + " " + mRyzen.group(2);
                                } else {
                                    _SYS_CPU_SHORT_TMP = "Ryzen " + mRyzen.group(1);
                                }
                            } else {
                                // Fallback: usar el nombre limpio completo
                                _SYS_CPU_SHORT_TMP = _short;
                            }
                        }
                    }
                }
            } catch (Throwable _t) {
                // En caso de fallo, usar la cadena completa
                _SYS_CPU_SHORT_TMP = SYS_CPU;
            }
        }
        final String SYS_CPU_SHORT = !datosForm.procesador.isEmpty() ? datosForm.procesador :
            (_SYS_CPU_SHORT_TMP == null ? "" : _SYS_CPU_SHORT_TMP);

        // Abreviación de RAM (tamaño aproximado) y Disco (coarse: TB o GB) para la plantilla
        // RAM: redondear al entero en GB y mostrar como "16GB" (sin decimales).
        // Disco: si >=1024GB mostrar en TB como "1TB"; si menor, en GB como "128GB".
        final String SYS_RAM_SHORT;
        final String SYS_DISK_SHORT;
        {
            // Usar datos del formulario si están disponibles
            if (!datosForm.memoriaRAM.isEmpty()) {
                SYS_RAM_SHORT = datosForm.memoriaRAM;
            } else {
                String ramTmp = (sysInfo != null && sysInfo.ram != null) ? sysInfo.ram : "";
                String ramShort = "";
                try {
                    java.util.regex.Pattern pSize = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)(?:\\s*)(GB|TB|MB)", java.util.regex.Pattern.CASE_INSENSITIVE);
                    java.util.regex.Matcher mRam = pSize.matcher(ramTmp);
                    String lastNum = null;
                    String lastUnit = null;
                    while (mRam.find()) {
                        lastNum = mRam.group(1);
                        lastUnit = mRam.group(2);
                    }
                    if (lastNum != null && lastUnit != null) {
                        double val = 0.0;
                        try { val = Double.parseDouble(lastNum.replace(',', '.')); } catch (Exception _e) { val = 0.0; }
                        String u = lastUnit.toUpperCase();
                        double gb = 0.0;
                        if (u.equals("TB")) gb = val * 1024.0;
                        else if (u.equals("GB")) gb = val;
                        else if (u.equals("MB")) gb = val / 1024.0;
                        long roundedGb = Math.max(1, Math.round(gb));
                        ramShort = String.valueOf(roundedGb) + "GB";
                    }
                } catch (Throwable _t) {
                    ramShort = "";
                }
                SYS_RAM_SHORT = ramShort;
            }
            
            // Usar datos del formulario para disco si están disponibles
            if (!datosForm.discoDuroCapacidad.isEmpty()) {
                SYS_DISK_SHORT = datosForm.discoDuroCapacidad;
            } else {
                String discosTmp = (sysInfo != null && sysInfo.discos != null) ? sysInfo.discos : "";
                String diskShort = "";
                try {
                    java.util.regex.Pattern pSize = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)(?:\\s*)(GB|TB|MB)", java.util.regex.Pattern.CASE_INSENSITIVE);
                    java.util.regex.Matcher mDisk = pSize.matcher(discosTmp);
                    if (mDisk.find()) {
                        String num = mDisk.group(1);
                        String unit = mDisk.group(2);
                        double val = 0.0;
                        try { val = Double.parseDouble(num.replace(',', '.')); } catch (Exception _e) { val = 0.0; }
                        String u = unit.toUpperCase();
                        double gb = 0.0;
                        if (u.equals("TB")) gb = val * 1024.0;
                        else if (u.equals("GB")) gb = val;
                        else if (u.equals("MB")) gb = val / 1024.0;
                        if (gb >= 1024.0) {
                            long tb = Math.max(1, Math.round(gb / 1024.0));
                            diskShort = String.valueOf(tb) + "TB";
                        } else {
                            long gbr = Math.max(1, Math.round(gb));
                            diskShort = String.valueOf(gbr) + "GB";
                        }
                    }
                } catch (Throwable _t) {
                    diskShort = "";
                }
                SYS_DISK_SHORT = diskShort;
            }
        }

        // Column widths
        sh.setColumnWidth(0,2560);
        sh.setColumnWidth(1,2560);
        sh.setColumnWidth(2,2560);
        sh.setColumnWidth(3,2560);
        sh.setColumnWidth(4,2560);
        sh.setColumnWidth(5,2560);
        sh.setColumnWidth(6,2560);
        sh.setColumnWidth(7,2560);
        sh.setColumnWidth(8,2560);
        sh.setColumnWidth(9,2560);
        sh.setColumnWidth(10,2560);
        sh.setColumnWidth(11,2560);
        sh.setColumnWidth(12,2560);
        sh.setColumnWidth(13,2560);

        // Row heights
        Row r1 = sh.createRow(1);
        r1.setHeight((short)300);
        Row r2 = sh.createRow(2);
        r2.setHeight((short)300);
        Row r3 = sh.createRow(3);
        r3.setHeight((short)300);
        Row r4 = sh.createRow(4);
        r4.setHeight((short)300);
        Row r5 = sh.createRow(5);
        r5.setHeight((short)300);
        Row r6 = sh.createRow(6);
        r6.setHeight((short)300);
        Row r7 = sh.createRow(7);
        r7.setHeight((short)300);
        Row r8 = sh.createRow(8);
        r8.setHeight((short)300);
        Row r9 = sh.createRow(9);
        r9.setHeight((short)300);
        Row r10 = sh.createRow(10);
        r10.setHeight((short)300);
        Row r11 = sh.createRow(11);
        r11.setHeight((short)300);
        Row r12 = sh.createRow(12);
        r12.setHeight((short)300);
        Row r13 = sh.createRow(13);
        r13.setHeight((short)300);
        Row r14 = sh.createRow(14);
        r14.setHeight((short)300);
        Row r15 = sh.createRow(15);
        r15.setHeight((short)300);
        Row r16 = sh.createRow(16);
        r16.setHeight((short)300);
        Row r17 = sh.createRow(17);
        r17.setHeight((short)300);
        Row r18 = sh.createRow(18);
        r18.setHeight((short)300);
        Row r19 = sh.createRow(19);
        r19.setHeight((short)300);
        Row r20 = sh.createRow(20);
        r20.setHeight((short)300);
        Row r21 = sh.createRow(21);
        r21.setHeight((short)300);
        Row r22 = sh.createRow(22);
        r22.setHeight((short)300);
        Row r23 = sh.createRow(23);
        r23.setHeight((short)300);
        Row r24 = sh.createRow(24);
        r24.setHeight((short)300);
        Row r25 = sh.createRow(25);
        r25.setHeight((short)300);
        Row r26 = sh.createRow(26);
        r26.setHeight((short)300);
        Row r27 = sh.createRow(27);
        r27.setHeight((short)300);
        Row r28 = sh.createRow(28);
        r28.setHeight((short)300);
        Row r29 = sh.createRow(29);
        r29.setHeight((short)300);
        Row r30 = sh.createRow(30);
        r30.setHeight((short)300);
        Row r31 = sh.createRow(31);
        r31.setHeight((short)300);
        Row r32 = sh.createRow(32);
        r32.setHeight((short)300);
        Row r33 = sh.createRow(33);
        r33.setHeight((short)300);
        Row r34 = sh.createRow(34);
        r34.setHeight((short)300);
        Row r35 = sh.createRow(35);
        r35.setHeight((short)300);
        Row r36 = sh.createRow(36);
        r36.setHeight((short)300);
        Row r37 = sh.createRow(37);
        r37.setHeight((short)300);
        Row r38 = sh.createRow(38);
        r38.setHeight((short)300);
        Row r39 = sh.createRow(39);
        r39.setHeight((short)300);
        Row r40 = sh.createRow(40);
        r40.setHeight((short)300);
        Row r41 = sh.createRow(41);
        r41.setHeight((short)300);
        Row r42 = sh.createRow(42);
        r42.setHeight((short)300);
        Row r43 = sh.createRow(43);
        r43.setHeight((short)300);
        Row r44 = sh.createRow(44);
        r44.setHeight((short)300);
        Row r45 = sh.createRow(45);
        r45.setHeight((short)300);
        Row r46 = sh.createRow(46);
        r46.setHeight((short)300);
        Row r47 = sh.createRow(47);
        r47.setHeight((short)300);
        Row r48 = sh.createRow(48);
        r48.setHeight((short)300);
        Row r49 = sh.createRow(49);
        r49.setHeight((short)300);
        Row r50 = sh.createRow(50);
        r50.setHeight((short)300);
        Row r51 = sh.createRow(51);
        r51.setHeight((short)300);
        Row r52 = sh.createRow(52);
        r52.setHeight((short)300);
        Row r53 = sh.createRow(53);
        r53.setHeight((short)300);
        Row r54 = sh.createRow(54);
        r54.setHeight((short)300);
        Row r55 = sh.createRow(55);
        r55.setHeight((short)300);
        Row r56 = sh.createRow(56);
        r56.setHeight((short)300);
        Row r57 = sh.createRow(57);
        r57.setHeight((short)300);
        Row r58 = sh.createRow(58);
        r58.setHeight((short)300);
        Row r59 = sh.createRow(59);
        r59.setHeight((short)300);

        // Styles
        CellStyle style0 = wb.createCellStyle();
        Font font0 = wb.createFont();
        font0.setFontHeightInPoints((short)11);
        font0.setColor((short)0);
        style0.setFont(font0);
        style0.setAlignment(HorizontalAlignment.CENTER);
        style0.setVerticalAlignment(VerticalAlignment.CENTER);
        style0.setWrapText(true);
        style0.setFillForegroundColor((short)0);
        style0.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style0.setBorderTop(BorderStyle.THIN);
        style0.setBorderBottom(BorderStyle.NONE);
        style0.setBorderLeft(BorderStyle.THIN);
        style0.setBorderRight(BorderStyle.NONE);

        CellStyle style1 = wb.createCellStyle();
        Font font1 = wb.createFont();
        font1.setFontHeightInPoints((short)11);
        font1.setColor((short)0);
        style1.setFont(font1);
        style1.setAlignment(HorizontalAlignment.CENTER);
        style1.setVerticalAlignment(VerticalAlignment.CENTER);
        style1.setWrapText(true);
        style1.setFillForegroundColor((short)0);
        style1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style1.setBorderTop(BorderStyle.THIN);
        style1.setBorderBottom(BorderStyle.NONE);
        style1.setBorderLeft(BorderStyle.NONE);
        style1.setBorderRight(BorderStyle.NONE);

        CellStyle style2 = wb.createCellStyle();
        Font font2 = wb.createFont();
        font2.setFontHeightInPoints((short)11);
        font2.setColor((short)0);
        style2.setFont(font2);
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);
        style2.setFillForegroundColor((short)0);
        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setBorderTop(BorderStyle.THIN);
        style2.setBorderBottom(BorderStyle.NONE);
        style2.setBorderLeft(BorderStyle.NONE);
        style2.setBorderRight(BorderStyle.NONE);

        CellStyle style3 = wb.createCellStyle();
        Font font3 = wb.createFont();
        font3.setFontHeightInPoints((short)10);
        font3.setColor((short)0);
        style3.setFont(font3);
        style3.setAlignment(HorizontalAlignment.CENTER);
        style3.setVerticalAlignment(VerticalAlignment.CENTER);
        style3.setFillForegroundColor((short)0);
        style3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style3.setBorderTop(BorderStyle.THIN);
        style3.setBorderBottom(BorderStyle.NONE);
        style3.setBorderLeft(BorderStyle.NONE);
        style3.setBorderRight(BorderStyle.NONE);

        CellStyle style4 = wb.createCellStyle();
        Font font4 = wb.createFont();
        font4.setFontHeightInPoints((short)11);
        font4.setColor((short)0);
        style4.setFont(font4);
        style4.setAlignment(HorizontalAlignment.GENERAL);
        style4.setVerticalAlignment(VerticalAlignment.CENTER);
        style4.setFillForegroundColor((short)0);
        style4.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style4.setBorderTop(BorderStyle.THIN);
        style4.setBorderBottom(BorderStyle.NONE);
        style4.setBorderLeft(BorderStyle.NONE);
        style4.setBorderRight(BorderStyle.THIN);

        CellStyle style5 = wb.createCellStyle();
        Font font5 = wb.createFont();
        font5.setFontHeightInPoints((short)11);
        font5.setColor((short)0);
        style5.setFont(font5);
        style5.setAlignment(HorizontalAlignment.CENTER);
        style5.setVerticalAlignment(VerticalAlignment.CENTER);
        style5.setWrapText(true);
        style5.setFillForegroundColor((short)0);
        style5.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style5.setBorderTop(BorderStyle.NONE);
        style5.setBorderBottom(BorderStyle.NONE);
        style5.setBorderLeft(BorderStyle.THIN);
        style5.setBorderRight(BorderStyle.NONE);

        CellStyle style6 = wb.createCellStyle();
        Font font6 = wb.createFont();
        font6.setFontHeightInPoints((short)11);
        font6.setColor((short)0);
        style6.setFont(font6);
        style6.setAlignment(HorizontalAlignment.CENTER);
        style6.setVerticalAlignment(VerticalAlignment.CENTER);
        style6.setWrapText(true);
        style6.setFillForegroundColor((short)0);
        style6.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style6.setBorderTop(BorderStyle.NONE);
        style6.setBorderBottom(BorderStyle.NONE);
        style6.setBorderLeft(BorderStyle.NONE);
        style6.setBorderRight(BorderStyle.NONE);

        CellStyle style7 = wb.createCellStyle();
        Font font7 = wb.createFont();
        font7.setFontHeightInPoints((short)11);
        font7.setColor((short)0);
        style7.setFont(font7);
        style7.setAlignment(HorizontalAlignment.GENERAL);
        style7.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style7.setFillForegroundColor((short)0);
        style7.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style7.setBorderTop(BorderStyle.NONE);
        style7.setBorderBottom(BorderStyle.NONE);
        style7.setBorderLeft(BorderStyle.NONE);
        style7.setBorderRight(BorderStyle.NONE);

        CellStyle style8 = wb.createCellStyle();
        Font font8 = wb.createFont();
        font8.setFontHeightInPoints((short)10);
        font8.setColor((short)0);
        style8.setFont(font8);
        style8.setAlignment(HorizontalAlignment.CENTER);
        style8.setVerticalAlignment(VerticalAlignment.CENTER);
        style8.setFillForegroundColor((short)0);
        style8.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style8.setBorderTop(BorderStyle.NONE);
        style8.setBorderBottom(BorderStyle.NONE);
        style8.setBorderLeft(BorderStyle.NONE);
        style8.setBorderRight(BorderStyle.NONE);

        CellStyle style9 = wb.createCellStyle();
        Font font9 = wb.createFont();
        font9.setFontHeightInPoints((short)11);
        font9.setColor((short)0);
        style9.setFont(font9);
        style9.setAlignment(HorizontalAlignment.GENERAL);
        style9.setVerticalAlignment(VerticalAlignment.CENTER);
        style9.setFillForegroundColor((short)0);
        style9.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style9.setBorderTop(BorderStyle.NONE);
        style9.setBorderBottom(BorderStyle.NONE);
        style9.setBorderLeft(BorderStyle.NONE);
        style9.setBorderRight(BorderStyle.THIN);

        CellStyle style10 = wb.createCellStyle();
        Font font10 = wb.createFont();
        font10.setFontHeightInPoints((short)10);
        font10.setColor((short)0);
        style10.setFont(font10);
        style10.setAlignment(HorizontalAlignment.CENTER);
        style10.setVerticalAlignment(VerticalAlignment.CENTER);
        style10.setFillForegroundColor((short)0);
        style10.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style10.setBorderTop(BorderStyle.NONE);
        style10.setBorderBottom(BorderStyle.THIN);
        style10.setBorderLeft(BorderStyle.NONE);
        style10.setBorderRight(BorderStyle.NONE);

        CellStyle style11 = wb.createCellStyle();
        Font font11 = wb.createFont();
        font11.setFontHeightInPoints((short)11);
        font11.setColor((short)0);
        style11.setFont(font11);
        style11.setAlignment(HorizontalAlignment.GENERAL);
        style11.setVerticalAlignment(VerticalAlignment.CENTER);
        style11.setFillForegroundColor((short)0);
        style11.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style11.setBorderTop(BorderStyle.NONE);
        style11.setBorderBottom(BorderStyle.THIN);
        style11.setBorderLeft(BorderStyle.NONE);
        style11.setBorderRight(BorderStyle.THIN);

        CellStyle style12 = wb.createCellStyle();
        Font font12 = wb.createFont();
        font12.setFontHeightInPoints((short)11);
        font12.setColor((short)0);
        style12.setFont(font12);
        style12.setAlignment(HorizontalAlignment.CENTER);
        style12.setVerticalAlignment(VerticalAlignment.CENTER);
        style12.setWrapText(true);
        style12.setFillForegroundColor((short)0);
        style12.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style12.setBorderTop(BorderStyle.NONE);
        style12.setBorderBottom(BorderStyle.THIN);
        style12.setBorderLeft(BorderStyle.THIN);
        style12.setBorderRight(BorderStyle.NONE);

        CellStyle style13 = wb.createCellStyle();
        Font font13 = wb.createFont();
        font13.setFontHeightInPoints((short)11);
        font13.setColor((short)0);
        style13.setFont(font13);
        style13.setAlignment(HorizontalAlignment.CENTER);
        style13.setVerticalAlignment(VerticalAlignment.CENTER);
        style13.setWrapText(true);
        style13.setFillForegroundColor((short)0);
        style13.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style13.setBorderTop(BorderStyle.NONE);
        style13.setBorderBottom(BorderStyle.THIN);
        style13.setBorderLeft(BorderStyle.NONE);
        style13.setBorderRight(BorderStyle.NONE);

        CellStyle style14 = wb.createCellStyle();
        Font font14 = wb.createFont();
        font14.setFontHeightInPoints((short)11);
        font14.setColor((short)0);
        style14.setFont(font14);
        style14.setAlignment(HorizontalAlignment.CENTER);
        style14.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style14.setFillForegroundColor((short)0);
        style14.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style14.setBorderTop(BorderStyle.NONE);
        style14.setBorderBottom(BorderStyle.THIN);
        style14.setBorderLeft(BorderStyle.NONE);
        style14.setBorderRight(BorderStyle.NONE);

        CellStyle style15 = wb.createCellStyle();
        Font font15 = wb.createFont();
        font15.setFontHeightInPoints((short)11);
        font15.setColor((short)0);
        style15.setFont(font15);
        style15.setAlignment(HorizontalAlignment.CENTER);
        style15.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style15.setFillForegroundColor((short)0);
        style15.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style15.setBorderTop(BorderStyle.NONE);
        style15.setBorderBottom(BorderStyle.THIN);
        style15.setBorderLeft(BorderStyle.NONE);
        style15.setBorderRight(BorderStyle.THIN);

        CellStyle style16 = wb.createCellStyle();
        Font font16 = wb.createFont();
        font16.setBold(true);
        font16.setFontHeightInPoints((short)11);
        font16.setColor((short)0);
        style16.setFont(font16);
        style16.setAlignment(HorizontalAlignment.CENTER);
        style16.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style16.setFillForegroundColor((short)0);
        style16.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style16.setBorderTop(BorderStyle.THIN);
        style16.setBorderBottom(BorderStyle.THIN);
        style16.setBorderLeft(BorderStyle.THIN);
        style16.setBorderRight(BorderStyle.NONE);

        CellStyle style17 = wb.createCellStyle();
        Font font17 = wb.createFont();
        font17.setFontHeightInPoints((short)11);
        font17.setColor((short)0);
        style17.setFont(font17);
        style17.setAlignment(HorizontalAlignment.CENTER);
        style17.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style17.setFillForegroundColor((short)0);
        style17.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style17.setBorderTop(BorderStyle.THIN);
        style17.setBorderBottom(BorderStyle.THIN);
        style17.setBorderLeft(BorderStyle.NONE);
        style17.setBorderRight(BorderStyle.NONE);

        CellStyle style18 = wb.createCellStyle();
        Font font18 = wb.createFont();
        font18.setFontHeightInPoints((short)11);
        font18.setColor((short)0);
        style18.setFont(font18);
        style18.setAlignment(HorizontalAlignment.CENTER);
        style18.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style18.setFillForegroundColor((short)0);
        style18.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style18.setBorderTop(BorderStyle.THIN);
        style18.setBorderBottom(BorderStyle.THIN);
        style18.setBorderLeft(BorderStyle.NONE);
        style18.setBorderRight(BorderStyle.THIN);

        CellStyle style19 = wb.createCellStyle();
        Font font19 = wb.createFont();
        font19.setFontHeightInPoints((short)11);
        font19.setColor((short)0);
        style19.setFont(font19);
        style19.setAlignment(HorizontalAlignment.CENTER);
        style19.setVerticalAlignment(VerticalAlignment.CENTER);
        style19.setFillForegroundColor((short)64);
        style19.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style19.setBorderTop(BorderStyle.THIN);
        style19.setBorderBottom(BorderStyle.NONE);
        style19.setBorderLeft(BorderStyle.THIN);
        style19.setBorderRight(BorderStyle.NONE);

        CellStyle style20 = wb.createCellStyle();
        Font font20 = wb.createFont();
        font20.setFontHeightInPoints((short)11);
        font20.setColor((short)0);
        style20.setFont(font20);
        style20.setAlignment(HorizontalAlignment.CENTER);
        style20.setVerticalAlignment(VerticalAlignment.CENTER);
        style20.setFillForegroundColor((short)64);
        style20.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style20.setBorderTop(BorderStyle.THIN);
        style20.setBorderBottom(BorderStyle.NONE);
        style20.setBorderLeft(BorderStyle.NONE);
        style20.setBorderRight(BorderStyle.NONE);

        CellStyle style21 = wb.createCellStyle();
        Font font21 = wb.createFont();
        font21.setFontHeightInPoints((short)11);
        font21.setColor((short)0);
        style21.setFont(font21);
        style21.setAlignment(HorizontalAlignment.CENTER);
        style21.setVerticalAlignment(VerticalAlignment.CENTER);
        style21.setFillForegroundColor((short)64);
        style21.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style21.setBorderTop(BorderStyle.THIN);
        style21.setBorderBottom(BorderStyle.NONE);
        style21.setBorderLeft(BorderStyle.NONE);
        style21.setBorderRight(BorderStyle.THIN);

        CellStyle style22 = wb.createCellStyle();
        Font font22 = wb.createFont();
        font22.setFontHeightInPoints((short)11);
        font22.setColor((short)0);
        style22.setFont(font22);
        style22.setAlignment(HorizontalAlignment.CENTER);
        style22.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style22.setFillForegroundColor((short)0);
        style22.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style22.setBorderTop(BorderStyle.THIN);
        style22.setBorderBottom(BorderStyle.THIN);
        style22.setBorderLeft(BorderStyle.THIN);
        style22.setBorderRight(BorderStyle.THIN);

        CellStyle style23 = wb.createCellStyle();
        Font font23 = wb.createFont();
        font23.setFontHeightInPoints((short)11);
        font23.setColor((short)0);
        style23.setFont(font23);
        style23.setAlignment(HorizontalAlignment.CENTER);
        style23.setVerticalAlignment(VerticalAlignment.CENTER);
        style23.setFillForegroundColor((short)64);
        style23.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style23.setBorderTop(BorderStyle.NONE);
        style23.setBorderBottom(BorderStyle.THIN);
        style23.setBorderLeft(BorderStyle.THIN);
        style23.setBorderRight(BorderStyle.NONE);

        CellStyle style24 = wb.createCellStyle();
        Font font24 = wb.createFont();
        font24.setFontHeightInPoints((short)11);
        font24.setColor((short)0);
        style24.setFont(font24);
        style24.setAlignment(HorizontalAlignment.CENTER);
        style24.setVerticalAlignment(VerticalAlignment.CENTER);
        style24.setFillForegroundColor((short)64);
        style24.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style24.setBorderTop(BorderStyle.NONE);
        style24.setBorderBottom(BorderStyle.THIN);
        style24.setBorderLeft(BorderStyle.NONE);
        style24.setBorderRight(BorderStyle.NONE);

        CellStyle style25 = wb.createCellStyle();
        Font font25 = wb.createFont();
        font25.setFontHeightInPoints((short)11);
        font25.setColor((short)0);
        style25.setFont(font25);
        style25.setAlignment(HorizontalAlignment.CENTER);
        style25.setVerticalAlignment(VerticalAlignment.CENTER);
        style25.setFillForegroundColor((short)64);
        style25.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style25.setBorderTop(BorderStyle.NONE);
        style25.setBorderBottom(BorderStyle.THIN);
        style25.setBorderLeft(BorderStyle.NONE);
        style25.setBorderRight(BorderStyle.THIN);

        CellStyle style26 = wb.createCellStyle();
        Font font26 = wb.createFont();
        font26.setFontHeightInPoints((short)11);
        font26.setColor((short)0);
        style26.setFont(font26);
        style26.setAlignment(HorizontalAlignment.CENTER);
        style26.setVerticalAlignment(VerticalAlignment.CENTER);
        style26.setFillForegroundColor((short)0);
        style26.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style26.setBorderTop(BorderStyle.THIN);
        style26.setBorderBottom(BorderStyle.NONE);
        style26.setBorderLeft(BorderStyle.THIN);
        style26.setBorderRight(BorderStyle.THIN);

        CellStyle style27 = wb.createCellStyle();
        Font font27 = wb.createFont();
        font27.setFontHeightInPoints((short)11);
        font27.setColor((short)0);
        style27.setFont(font27);
        style27.setAlignment(HorizontalAlignment.CENTER);
        style27.setVerticalAlignment(VerticalAlignment.CENTER);
        style27.setFillForegroundColor((short)0);
        style27.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style27.setBorderTop(BorderStyle.THIN);
        style27.setBorderBottom(BorderStyle.NONE);
        style27.setBorderLeft(BorderStyle.NONE);
        style27.setBorderRight(BorderStyle.THIN);

        CellStyle style28 = wb.createCellStyle();
        Font font28 = wb.createFont();
        font28.setFontHeightInPoints((short)11);
        font28.setColor((short)0);
        style28.setFont(font28);
        style28.setAlignment(HorizontalAlignment.CENTER);
        style28.setVerticalAlignment(VerticalAlignment.CENTER);
        style28.setFillForegroundColor((short)0);
        style28.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style28.setBorderTop(BorderStyle.THIN);
        style28.setBorderBottom(BorderStyle.THIN);
        style28.setBorderLeft(BorderStyle.THIN);
        style28.setBorderRight(BorderStyle.THIN);

        CellStyle style29 = wb.createCellStyle();
        Font font29 = wb.createFont();
        font29.setFontHeightInPoints((short)11);
        font29.setColor((short)0);
        style29.setFont(font29);
        style29.setAlignment(HorizontalAlignment.CENTER);
        style29.setVerticalAlignment(VerticalAlignment.CENTER);
        style29.setFillForegroundColor((short)0);
        style29.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style29.setBorderTop(BorderStyle.THIN);
        style29.setBorderBottom(BorderStyle.THIN);
        style29.setBorderLeft(BorderStyle.NONE);
        style29.setBorderRight(BorderStyle.NONE);

        CellStyle style30 = wb.createCellStyle();
        Font font30 = wb.createFont();
        font30.setFontHeightInPoints((short)11);
        font30.setColor((short)0);
        style30.setFont(font30);
        style30.setAlignment(HorizontalAlignment.CENTER);
        style30.setVerticalAlignment(VerticalAlignment.CENTER);
        style30.setFillForegroundColor((short)0);
        style30.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style30.setBorderTop(BorderStyle.THIN);
        style30.setBorderBottom(BorderStyle.THIN);
        style30.setBorderLeft(BorderStyle.NONE);
        style30.setBorderRight(BorderStyle.THIN);

        CellStyle style31 = wb.createCellStyle();
        Font font31 = wb.createFont();
        font31.setFontHeightInPoints((short)11);
        font31.setColor((short)0);
        style31.setFont(font31);
        style31.setAlignment(HorizontalAlignment.CENTER);
        style31.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style31.setFillForegroundColor((short)0);
        style31.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style31.setBorderTop(BorderStyle.THIN);
        style31.setBorderBottom(BorderStyle.THIN);
        style31.setBorderLeft(BorderStyle.THIN);
        style31.setBorderRight(BorderStyle.NONE);

        CellStyle style32 = wb.createCellStyle();
        Font font32 = wb.createFont();
        font32.setFontHeightInPoints((short)11);
        font32.setColor((short)0);
        style32.setFont(font32);
        style32.setAlignment(HorizontalAlignment.CENTER);
        style32.setVerticalAlignment(VerticalAlignment.CENTER);
        style32.setFillForegroundColor((short)0);
        style32.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style32.setBorderTop(BorderStyle.THIN);
        style32.setBorderBottom(BorderStyle.THIN);
        style32.setBorderLeft(BorderStyle.THIN);
        style32.setBorderRight(BorderStyle.THIN);

        CellStyle style33 = wb.createCellStyle();
        Font font33 = wb.createFont();
        font33.setFontHeightInPoints((short)11);
        font33.setColor((short)0);
        style33.setFont(font33);
        style33.setAlignment(HorizontalAlignment.CENTER);
        style33.setVerticalAlignment(VerticalAlignment.CENTER);
        style33.setFillForegroundColor((short)0);
        style33.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style33.setBorderTop(BorderStyle.THIN);
        style33.setBorderBottom(BorderStyle.THIN);
        style33.setBorderLeft(BorderStyle.THIN);
        style33.setBorderRight(BorderStyle.THIN);

        CellStyle style34 = wb.createCellStyle();
        Font font34 = wb.createFont();
        font34.setBold(true);
        font34.setFontHeightInPoints((short)11);
        font34.setColor((short)0);
        style34.setFont(font34);
        style34.setAlignment(HorizontalAlignment.CENTER);
        style34.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style34.setFillForegroundColor((short)0);
        style34.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style34.setBorderTop(BorderStyle.THIN);
        style34.setBorderBottom(BorderStyle.NONE);
        style34.setBorderLeft(BorderStyle.THIN);
        style34.setBorderRight(BorderStyle.NONE);

        CellStyle style35 = wb.createCellStyle();
        Font font35 = wb.createFont();
        font35.setFontHeightInPoints((short)11);
        font35.setColor((short)0);
        style35.setFont(font35);
        style35.setAlignment(HorizontalAlignment.CENTER);
        style35.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style35.setFillForegroundColor((short)0);
        style35.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style35.setBorderTop(BorderStyle.THIN);
        style35.setBorderBottom(BorderStyle.NONE);
        style35.setBorderLeft(BorderStyle.NONE);
        style35.setBorderRight(BorderStyle.NONE);

        CellStyle style36 = wb.createCellStyle();
        Font font36 = wb.createFont();
        font36.setFontHeightInPoints((short)11);
        font36.setColor((short)0);
        style36.setFont(font36);
        style36.setAlignment(HorizontalAlignment.CENTER);
        style36.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style36.setFillForegroundColor((short)0);
        style36.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style36.setBorderTop(BorderStyle.NONE);
        style36.setBorderBottom(BorderStyle.NONE);
        style36.setBorderLeft(BorderStyle.NONE);
        style36.setBorderRight(BorderStyle.NONE);

        CellStyle style37 = wb.createCellStyle();
        Font font37 = wb.createFont();
        font37.setFontHeightInPoints((short)11);
        font37.setColor((short)0);
        style37.setFont(font37);
        style37.setAlignment(HorizontalAlignment.CENTER);
        style37.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style37.setFillForegroundColor((short)0);
        style37.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style37.setBorderTop(BorderStyle.THIN);
        style37.setBorderBottom(BorderStyle.NONE);
        style37.setBorderLeft(BorderStyle.NONE);
        style37.setBorderRight(BorderStyle.THIN);

        CellStyle style38 = wb.createCellStyle();
        Font font38 = wb.createFont();
        font38.setBold(true);
        font38.setFontHeightInPoints((short)11);
        font38.setColor((short)0);
        style38.setFont(font38);
        style38.setAlignment(HorizontalAlignment.CENTER);
        style38.setVerticalAlignment(VerticalAlignment.CENTER);
        style38.setFillForegroundColor((short)0);
        style38.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style38.setBorderTop(BorderStyle.THIN);
        style38.setBorderBottom(BorderStyle.NONE);
        style38.setBorderLeft(BorderStyle.THIN);
        style38.setBorderRight(BorderStyle.NONE);

        CellStyle style39 = wb.createCellStyle();
        Font font39 = wb.createFont();
        font39.setFontHeightInPoints((short)11);
        font39.setColor((short)0);
        style39.setFont(font39);
        style39.setAlignment(HorizontalAlignment.CENTER);
        style39.setVerticalAlignment(VerticalAlignment.CENTER);
        style39.setFillForegroundColor((short)0);
        style39.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style39.setBorderTop(BorderStyle.NONE);
        style39.setBorderBottom(BorderStyle.THIN);
        style39.setBorderLeft(BorderStyle.THIN);
        style39.setBorderRight(BorderStyle.NONE);

        CellStyle style40 = wb.createCellStyle();
        Font font40 = wb.createFont();
        font40.setFontHeightInPoints((short)11);
        font40.setColor((short)0);
        style40.setFont(font40);
        style40.setAlignment(HorizontalAlignment.CENTER);
        style40.setVerticalAlignment(VerticalAlignment.CENTER);
        style40.setFillForegroundColor((short)0);
        style40.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style40.setBorderTop(BorderStyle.NONE);
        style40.setBorderBottom(BorderStyle.THIN);
        style40.setBorderLeft(BorderStyle.NONE);
        style40.setBorderRight(BorderStyle.NONE);

        CellStyle style41 = wb.createCellStyle();
        Font font41 = wb.createFont();
        font41.setFontHeightInPoints((short)11);
        font41.setColor((short)0);
        style41.setFont(font41);
        style41.setAlignment(HorizontalAlignment.CENTER);
        style41.setVerticalAlignment(VerticalAlignment.CENTER);
        style41.setFillForegroundColor((short)0);
        style41.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style41.setBorderTop(BorderStyle.THIN);
        style41.setBorderBottom(BorderStyle.NONE);
        style41.setBorderLeft(BorderStyle.THIN);
        style41.setBorderRight(BorderStyle.NONE);

        CellStyle style42 = wb.createCellStyle();
        Font font42 = wb.createFont();
        font42.setFontHeightInPoints((short)11);
        font42.setColor((short)0);
        style42.setFont(font42);
        style42.setAlignment(HorizontalAlignment.CENTER);
        style42.setVerticalAlignment(VerticalAlignment.CENTER);
        style42.setFillForegroundColor((short)0);
        style42.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style42.setBorderTop(BorderStyle.NONE);
        style42.setBorderBottom(BorderStyle.NONE);
        style42.setBorderLeft(BorderStyle.THIN);
        style42.setBorderRight(BorderStyle.NONE);

        CellStyle style43 = wb.createCellStyle();
        Font font43 = wb.createFont();
        font43.setFontHeightInPoints((short)11);
        font43.setColor((short)0);
        style43.setFont(font43);
        style43.setAlignment(HorizontalAlignment.CENTER);
        style43.setVerticalAlignment(VerticalAlignment.CENTER);
        style43.setFillForegroundColor((short)0);
        style43.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style43.setBorderTop(BorderStyle.NONE);
        style43.setBorderBottom(BorderStyle.NONE);
        style43.setBorderLeft(BorderStyle.NONE);
        style43.setBorderRight(BorderStyle.THIN);

        CellStyle style44 = wb.createCellStyle();
        Font font44 = wb.createFont();
        font44.setFontHeightInPoints((short)11);
        font44.setColor((short)0);
        style44.setFont(font44);
        style44.setAlignment(HorizontalAlignment.CENTER);
        style44.setVerticalAlignment(VerticalAlignment.CENTER);
        style44.setFillForegroundColor((short)0);
        style44.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style44.setBorderTop(BorderStyle.NONE);
        style44.setBorderBottom(BorderStyle.NONE);
        style44.setBorderLeft(BorderStyle.THIN);
        style44.setBorderRight(BorderStyle.THIN);

        CellStyle style45 = wb.createCellStyle();
        Font font45 = wb.createFont();
        font45.setFontHeightInPoints((short)11);
        font45.setColor((short)0);
        style45.setFont(font45);
        style45.setAlignment(HorizontalAlignment.CENTER);
        style45.setVerticalAlignment(VerticalAlignment.CENTER);
        style45.setFillForegroundColor((short)0);
        style45.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style45.setBorderTop(BorderStyle.NONE);
        style45.setBorderBottom(BorderStyle.NONE);
        style45.setBorderLeft(BorderStyle.NONE);
        style45.setBorderRight(BorderStyle.NONE);

        CellStyle style46 = wb.createCellStyle();
        Font font46 = wb.createFont();
        font46.setBold(true);
        font46.setFontHeightInPoints((short)11);
        font46.setColor((short)0);
        style46.setFont(font46);
        style46.setAlignment(HorizontalAlignment.CENTER);
        style46.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style46.setFillForegroundColor((short)0);
        style46.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style46.setBorderTop(BorderStyle.THIN);
        style46.setBorderBottom(BorderStyle.NONE);
        style46.setBorderLeft(BorderStyle.NONE);
        style46.setBorderRight(BorderStyle.NONE);

        CellStyle style47 = wb.createCellStyle();
        Font font47 = wb.createFont();
        font47.setBold(true);
        font47.setFontHeightInPoints((short)11);
        font47.setColor((short)0);
        style47.setFont(font47);
        style47.setAlignment(HorizontalAlignment.CENTER);
        style47.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style47.setFillForegroundColor((short)0);
        style47.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style47.setBorderTop(BorderStyle.THIN);
        style47.setBorderBottom(BorderStyle.NONE);
        style47.setBorderLeft(BorderStyle.NONE);
        style47.setBorderRight(BorderStyle.THIN);

        CellStyle style48 = wb.createCellStyle();
        Font font48 = wb.createFont();
        font48.setFontHeightInPoints((short)11);
        font48.setColor((short)0);
        style48.setFont(font48);
        style48.setAlignment(HorizontalAlignment.GENERAL);
        style48.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style48.setFillForegroundColor((short)0);
        style48.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style48.setBorderTop(BorderStyle.THIN);
        style48.setBorderBottom(BorderStyle.THIN);
        style48.setBorderLeft(BorderStyle.THIN);
        style48.setBorderRight(BorderStyle.THIN);

        CellStyle style49 = wb.createCellStyle();
        Font font49 = wb.createFont();
        font49.setFontHeightInPoints((short)11);
        font49.setColor((short)0);
        style49.setFont(font49);
        style49.setAlignment(HorizontalAlignment.CENTER);
        style49.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style49.setFillForegroundColor((short)0);
        style49.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style49.setBorderTop(BorderStyle.NONE);
        style49.setBorderBottom(BorderStyle.NONE);
        style49.setBorderLeft(BorderStyle.NONE);
        style49.setBorderRight(BorderStyle.THIN);

        CellStyle style50 = wb.createCellStyle();
        Font font50 = wb.createFont();
        font50.setFontHeightInPoints((short)11);
        font50.setColor((short)8);
        style50.setFont(font50);
        style50.setAlignment(HorizontalAlignment.CENTER);
        style50.setVerticalAlignment(VerticalAlignment.CENTER);
        style50.setFillForegroundColor((short)0);
        style50.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style50.setBorderTop(BorderStyle.THIN);
        style50.setBorderBottom(BorderStyle.NONE);
        style50.setBorderLeft(BorderStyle.THIN);
        style50.setBorderRight(BorderStyle.NONE);

        CellStyle style51 = wb.createCellStyle();
        Font font51 = wb.createFont();
        font51.setFontHeightInPoints((short)11);
        font51.setColor((short)0);
        style51.setFont(font51);
        style51.setAlignment(HorizontalAlignment.LEFT);
        style51.setVerticalAlignment(VerticalAlignment.CENTER);
        style51.setFillForegroundColor((short)0);
        style51.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style51.setBorderTop(BorderStyle.THIN);
        style51.setBorderBottom(BorderStyle.NONE);
        style51.setBorderLeft(BorderStyle.THIN);
        style51.setBorderRight(BorderStyle.NONE);

        CellStyle style52 = wb.createCellStyle();
        Font font52 = wb.createFont();
        font52.setFontHeightInPoints((short)11);
        font52.setColor((short)0);
        style52.setFont(font52);
        style52.setAlignment(HorizontalAlignment.LEFT);
        style52.setVerticalAlignment(VerticalAlignment.CENTER);
        style52.setFillForegroundColor((short)0);
        style52.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style52.setBorderTop(BorderStyle.THIN);
        style52.setBorderBottom(BorderStyle.NONE);
        style52.setBorderLeft(BorderStyle.NONE);
        style52.setBorderRight(BorderStyle.NONE);

        CellStyle style53 = wb.createCellStyle();
        Font font53 = wb.createFont();
        font53.setFontHeightInPoints((short)11);
        font53.setColor((short)0);
        style53.setFont(font53);
        style53.setAlignment(HorizontalAlignment.LEFT);
        style53.setVerticalAlignment(VerticalAlignment.CENTER);
        style53.setFillForegroundColor((short)0);
        style53.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style53.setBorderTop(BorderStyle.THIN);
        style53.setBorderBottom(BorderStyle.NONE);
        style53.setBorderLeft(BorderStyle.NONE);
        style53.setBorderRight(BorderStyle.THIN);

        CellStyle style54 = wb.createCellStyle();
        Font font54 = wb.createFont();
        font54.setFontHeightInPoints((short)11);
        font54.setColor((short)0);
        style54.setFont(font54);
        style54.setAlignment(HorizontalAlignment.LEFT);
        style54.setVerticalAlignment(VerticalAlignment.CENTER);
        style54.setFillForegroundColor((short)0);
        style54.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style54.setBorderTop(BorderStyle.NONE);
        style54.setBorderBottom(BorderStyle.NONE);
        style54.setBorderLeft(BorderStyle.THIN);
        style54.setBorderRight(BorderStyle.NONE);

        CellStyle style55 = wb.createCellStyle();
        Font font55 = wb.createFont();
        font55.setFontHeightInPoints((short)11);
        font55.setColor((short)0);
        style55.setFont(font55);
        style55.setAlignment(HorizontalAlignment.LEFT);
        style55.setVerticalAlignment(VerticalAlignment.CENTER);
        style55.setFillForegroundColor((short)0);
        style55.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style55.setBorderTop(BorderStyle.NONE);
        style55.setBorderBottom(BorderStyle.NONE);
        style55.setBorderLeft(BorderStyle.NONE);
        style55.setBorderRight(BorderStyle.NONE);

        CellStyle style56 = wb.createCellStyle();
        Font font56 = wb.createFont();
        font56.setFontHeightInPoints((short)11);
        font56.setColor((short)0);
        style56.setFont(font56);
        style56.setAlignment(HorizontalAlignment.LEFT);
        style56.setVerticalAlignment(VerticalAlignment.CENTER);
        style56.setFillForegroundColor((short)0);
        style56.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style56.setBorderTop(BorderStyle.NONE);
        style56.setBorderBottom(BorderStyle.NONE);
        style56.setBorderLeft(BorderStyle.NONE);
        style56.setBorderRight(BorderStyle.THIN);

        CellStyle style57 = wb.createCellStyle();
        Font font57 = wb.createFont();
        font57.setFontHeightInPoints((short)11);
        font57.setColor((short)0);
        style57.setFont(font57);
        style57.setAlignment(HorizontalAlignment.LEFT);
        style57.setVerticalAlignment(VerticalAlignment.CENTER);
        style57.setFillForegroundColor((short)0);
        style57.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style57.setBorderTop(BorderStyle.NONE);
        style57.setBorderBottom(BorderStyle.THIN);
        style57.setBorderLeft(BorderStyle.THIN);
        style57.setBorderRight(BorderStyle.NONE);

        CellStyle style58 = wb.createCellStyle();
        Font font58 = wb.createFont();
        font58.setFontHeightInPoints((short)11);
        font58.setColor((short)0);
        style58.setFont(font58);
        style58.setAlignment(HorizontalAlignment.LEFT);
        style58.setVerticalAlignment(VerticalAlignment.CENTER);
        style58.setFillForegroundColor((short)0);
        style58.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style58.setBorderTop(BorderStyle.NONE);
        style58.setBorderBottom(BorderStyle.THIN);
        style58.setBorderLeft(BorderStyle.NONE);
        style58.setBorderRight(BorderStyle.NONE);

        CellStyle style59 = wb.createCellStyle();
        Font font59 = wb.createFont();
        font59.setFontHeightInPoints((short)11);
        font59.setColor((short)0);
        style59.setFont(font59);
        style59.setAlignment(HorizontalAlignment.LEFT);
        style59.setVerticalAlignment(VerticalAlignment.CENTER);
        style59.setFillForegroundColor((short)0);
        style59.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style59.setBorderTop(BorderStyle.NONE);
        style59.setBorderBottom(BorderStyle.THIN);
        style59.setBorderLeft(BorderStyle.NONE);
        style59.setBorderRight(BorderStyle.THIN);

        CellStyle style60 = wb.createCellStyle();
        Font font60 = wb.createFont();
        font60.setFontHeightInPoints((short)11);
        font60.setColor((short)0);
        style60.setFont(font60);
        style60.setAlignment(HorizontalAlignment.GENERAL);
        style60.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style60.setFillForegroundColor((short)0);
        style60.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style60.setBorderTop(BorderStyle.NONE);
        style60.setBorderBottom(BorderStyle.NONE);
        style60.setBorderLeft(BorderStyle.THIN);
        style60.setBorderRight(BorderStyle.NONE);

        CellStyle style61 = wb.createCellStyle();
        Font font61 = wb.createFont();
        font61.setFontHeightInPoints((short)11);
        font61.setColor((short)0);
        style61.setFont(font61);
        style61.setAlignment(HorizontalAlignment.GENERAL);
        style61.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style61.setFillForegroundColor((short)0);
        style61.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style61.setBorderTop(BorderStyle.NONE);
        style61.setBorderBottom(BorderStyle.NONE);
        style61.setBorderLeft(BorderStyle.NONE);
        style61.setBorderRight(BorderStyle.THIN);

        CellStyle style62 = wb.createCellStyle();
        Font font62 = wb.createFont();
        font62.setFontHeightInPoints((short)11);
        font62.setColor((short)0);
        style62.setFont(font62);
        style62.setAlignment(HorizontalAlignment.GENERAL);
        style62.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style62.setFillForegroundColor((short)0);
        style62.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style62.setBorderTop(BorderStyle.NONE);
        style62.setBorderBottom(BorderStyle.THIN);
        style62.setBorderLeft(BorderStyle.THIN);
        style62.setBorderRight(BorderStyle.NONE);

        CellStyle style63 = wb.createCellStyle();
        Font font63 = wb.createFont();
        font63.setFontHeightInPoints((short)11);
        font63.setColor((short)0);
        style63.setFont(font63);
        style63.setAlignment(HorizontalAlignment.GENERAL);
        style63.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style63.setFillForegroundColor((short)0);
        style63.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style63.setBorderTop(BorderStyle.NONE);
        style63.setBorderBottom(BorderStyle.THIN);
        style63.setBorderLeft(BorderStyle.NONE);
        style63.setBorderRight(BorderStyle.NONE);

        CellStyle style64 = wb.createCellStyle();
        Font font64 = wb.createFont();
        font64.setFontHeightInPoints((short)11);
        font64.setColor((short)0);
        style64.setFont(font64);
        style64.setAlignment(HorizontalAlignment.GENERAL);
        style64.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style64.setFillForegroundColor((short)0);
        style64.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style64.setBorderTop(BorderStyle.NONE);
        style64.setBorderBottom(BorderStyle.THIN);
        style64.setBorderLeft(BorderStyle.NONE);
        style64.setBorderRight(BorderStyle.THIN);

        CellStyle style65 = wb.createCellStyle();
        Font font65 = wb.createFont();
        font65.setBold(true);
        font65.setFontHeightInPoints((short)11);
        font65.setColor((short)0);
        style65.setFont(font65);
        style65.setAlignment(HorizontalAlignment.CENTER);
        style65.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style65.setFillForegroundColor((short)0);
        style65.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style65.setBorderTop(BorderStyle.THIN);
        style65.setBorderBottom(BorderStyle.THIN);
        style65.setBorderLeft(BorderStyle.NONE);
        style65.setBorderRight(BorderStyle.NONE);

        CellStyle style66 = wb.createCellStyle();
        Font font66 = wb.createFont();
        font66.setBold(true);
        font66.setFontHeightInPoints((short)11);
        font66.setColor((short)0);
        style66.setFont(font66);
        style66.setAlignment(HorizontalAlignment.CENTER);
        style66.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style66.setFillForegroundColor((short)0);
        style66.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style66.setBorderTop(BorderStyle.THIN);
        style66.setBorderBottom(BorderStyle.THIN);
        style66.setBorderLeft(BorderStyle.NONE);
        style66.setBorderRight(BorderStyle.THIN);

        CellStyle style67 = wb.createCellStyle();
        Font font67 = wb.createFont();
        font67.setFontHeightInPoints((short)11);
        font67.setColor((short)0);
        style67.setFont(font67);
        style67.setAlignment(HorizontalAlignment.GENERAL);
        style67.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style67.setFillForegroundColor((short)0);
        style67.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style67.setBorderTop(BorderStyle.THIN);
        style67.setBorderBottom(BorderStyle.NONE);
        style67.setBorderLeft(BorderStyle.THIN);
        style67.setBorderRight(BorderStyle.NONE);

        CellStyle style68 = wb.createCellStyle();
        Font font68 = wb.createFont();
        font68.setFontHeightInPoints((short)11);
        font68.setColor((short)0);
        style68.setFont(font68);
        style68.setAlignment(HorizontalAlignment.GENERAL);
        style68.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style68.setFillForegroundColor((short)0);
        style68.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style68.setBorderTop(BorderStyle.THIN);
        style68.setBorderBottom(BorderStyle.NONE);
        style68.setBorderLeft(BorderStyle.NONE);
        style68.setBorderRight(BorderStyle.NONE);

        CellStyle style69 = wb.createCellStyle();
        Font font69 = wb.createFont();
        font69.setFontHeightInPoints((short)11);
        font69.setColor((short)0);
        style69.setFont(font69);
        style69.setAlignment(HorizontalAlignment.GENERAL);
        style69.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style69.setFillForegroundColor((short)0);
        style69.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style69.setBorderTop(BorderStyle.THIN);
        style69.setBorderBottom(BorderStyle.NONE);
        style69.setBorderLeft(BorderStyle.NONE);
        style69.setBorderRight(BorderStyle.THIN);

        CellStyle style70 = wb.createCellStyle();
        Font font70 = wb.createFont();
        font70.setFontHeightInPoints((short)11);
        font70.setColor((short)0);
        style70.setFont(font70);
        style70.setAlignment(HorizontalAlignment.CENTER);
        style70.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style70.setFillForegroundColor((short)0);
        style70.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style70.setBorderTop(BorderStyle.THIN);
        style70.setBorderBottom(BorderStyle.NONE);
        style70.setBorderLeft(BorderStyle.THIN);
        style70.setBorderRight(BorderStyle.NONE);

        CellStyle style71 = wb.createCellStyle();
        Font font71 = wb.createFont();
        font71.setFontHeightInPoints((short)11);
        font71.setColor((short)0);
        style71.setFont(font71);
        style71.setAlignment(HorizontalAlignment.CENTER);
        style71.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style71.setFillForegroundColor((short)0);
        style71.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style71.setBorderTop(BorderStyle.NONE);
        style71.setBorderBottom(BorderStyle.NONE);
        style71.setBorderLeft(BorderStyle.THIN);
        style71.setBorderRight(BorderStyle.NONE);

        CellStyle style72 = wb.createCellStyle();
        Font font72 = wb.createFont();
        font72.setFontHeightInPoints((short)11);
        font72.setColor((short)8);
        style72.setFont(font72);
        style72.setAlignment(HorizontalAlignment.CENTER);
        style72.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style72.setFillForegroundColor((short)0);
        style72.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style72.setBorderTop(BorderStyle.THIN);
        style72.setBorderBottom(BorderStyle.THIN);
        style72.setBorderLeft(BorderStyle.THIN);
        style72.setBorderRight(BorderStyle.NONE);

        CellStyle style73 = wb.createCellStyle();
        Font font73 = wb.createFont();
        font73.setFontHeightInPoints((short)11);
        font73.setColor((short)8);
        style73.setFont(font73);
        style73.setAlignment(HorizontalAlignment.CENTER);
        style73.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style73.setFillForegroundColor((short)0);
        style73.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style73.setBorderTop(BorderStyle.THIN);
        style73.setBorderBottom(BorderStyle.THIN);
        style73.setBorderLeft(BorderStyle.NONE);
        style73.setBorderRight(BorderStyle.NONE);

        CellStyle style74 = wb.createCellStyle();
        Font font74 = wb.createFont();
        font74.setFontHeightInPoints((short)11);
        font74.setColor((short)8);
        style74.setFont(font74);
        style74.setAlignment(HorizontalAlignment.CENTER);
        style74.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style74.setFillForegroundColor((short)0);
        style74.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style74.setBorderTop(BorderStyle.THIN);
        style74.setBorderBottom(BorderStyle.THIN);
        style74.setBorderLeft(BorderStyle.NONE);
        style74.setBorderRight(BorderStyle.THIN);

        CellStyle style75 = wb.createCellStyle();
        Font font75 = wb.createFont();
        font75.setFontHeightInPoints((short)11);
        font75.setColor((short)0);
        style75.setFont(font75);
        style75.setAlignment(HorizontalAlignment.LEFT);
        style75.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style75.setFillForegroundColor((short)0);
        style75.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style75.setBorderTop(BorderStyle.THIN);
        style75.setBorderBottom(BorderStyle.THIN);
        style75.setBorderLeft(BorderStyle.THIN);
        style75.setBorderRight(BorderStyle.NONE);

        CellStyle style76 = wb.createCellStyle();
        Font font76 = wb.createFont();
        font76.setFontHeightInPoints((short)11);
        font76.setColor((short)0);
        style76.setFont(font76);
        style76.setAlignment(HorizontalAlignment.LEFT);
        style76.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style76.setFillForegroundColor((short)0);
        style76.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style76.setBorderTop(BorderStyle.THIN);
        style76.setBorderBottom(BorderStyle.THIN);
        style76.setBorderLeft(BorderStyle.NONE);
        style76.setBorderRight(BorderStyle.THIN);

        CellStyle style77 = wb.createCellStyle();
        Font font77 = wb.createFont();
        font77.setBold(true);
        font77.setFontHeightInPoints((short)11);
        font77.setColor((short)0);
        style77.setFont(font77);
        style77.setAlignment(HorizontalAlignment.CENTER);
        style77.setVerticalAlignment(VerticalAlignment.CENTER);
        style77.setFillForegroundColor((short)0);
        style77.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style77.setBorderTop(BorderStyle.THIN);
        style77.setBorderBottom(BorderStyle.THIN);
        style77.setBorderLeft(BorderStyle.THIN);
        style77.setBorderRight(BorderStyle.NONE);

        CellStyle style78 = wb.createCellStyle();
        Font font78 = wb.createFont();
        font78.setBold(true);
        font78.setFontHeightInPoints((short)11);
        font78.setColor((short)0);
        style78.setFont(font78);
        style78.setAlignment(HorizontalAlignment.CENTER);
        style78.setVerticalAlignment(VerticalAlignment.CENTER);
        style78.setFillForegroundColor((short)0);
        style78.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style78.setBorderTop(BorderStyle.THIN);
        style78.setBorderBottom(BorderStyle.THIN);
        style78.setBorderLeft(BorderStyle.NONE);
        style78.setBorderRight(BorderStyle.NONE);

        CellStyle style79 = wb.createCellStyle();
        Font font79 = wb.createFont();
        font79.setBold(true);
        font79.setFontHeightInPoints((short)11);
        font79.setColor((short)0);
        style79.setFont(font79);
        style79.setAlignment(HorizontalAlignment.CENTER);
        style79.setVerticalAlignment(VerticalAlignment.CENTER);
        style79.setFillForegroundColor((short)0);
        style79.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style79.setBorderTop(BorderStyle.THIN);
        style79.setBorderBottom(BorderStyle.THIN);
        style79.setBorderLeft(BorderStyle.NONE);
        style79.setBorderRight(BorderStyle.THIN);

        CellStyle style80 = wb.createCellStyle();
        Font font80 = wb.createFont();
        font80.setFontHeightInPoints((short)11);
        font80.setColor((short)0);
        style80.setFont(font80);
        style80.setAlignment(HorizontalAlignment.GENERAL);
        style80.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style80.setFillForegroundColor((short)64);
        style80.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style80.setBorderTop(BorderStyle.THIN);
        style80.setBorderBottom(BorderStyle.THIN);
        style80.setBorderLeft(BorderStyle.THIN);
        style80.setBorderRight(BorderStyle.NONE);

        CellStyle style81 = wb.createCellStyle();
        Font font81 = wb.createFont();
        font81.setFontHeightInPoints((short)11);
        font81.setColor((short)0);
        style81.setFont(font81);
        style81.setAlignment(HorizontalAlignment.GENERAL);
        style81.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style81.setFillForegroundColor((short)64);
        style81.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style81.setBorderTop(BorderStyle.THIN);
        style81.setBorderBottom(BorderStyle.THIN);
        style81.setBorderLeft(BorderStyle.NONE);
        style81.setBorderRight(BorderStyle.THIN);

        CellStyle style82 = wb.createCellStyle();
        Font font82 = wb.createFont();
        font82.setFontHeightInPoints((short)11);
        font82.setColor((short)0);
        style82.setFont(font82);
        style82.setAlignment(HorizontalAlignment.CENTER);
        style82.setVerticalAlignment(VerticalAlignment.CENTER);
        style82.setFillForegroundColor((short)64);
        style82.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style82.setBorderTop(BorderStyle.THIN);
        style82.setBorderBottom(BorderStyle.THIN);
        style82.setBorderLeft(BorderStyle.THIN);
        style82.setBorderRight(BorderStyle.NONE);

        CellStyle style83 = wb.createCellStyle();
        Font font83 = wb.createFont();
        font83.setFontHeightInPoints((short)11);
        font83.setColor((short)0);
        style83.setFont(font83);
        style83.setAlignment(HorizontalAlignment.CENTER);
        style83.setVerticalAlignment(VerticalAlignment.CENTER);
        style83.setFillForegroundColor((short)64);
        style83.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style83.setBorderTop(BorderStyle.THIN);
        style83.setBorderBottom(BorderStyle.THIN);
        style83.setBorderLeft(BorderStyle.NONE);
        style83.setBorderRight(BorderStyle.NONE);

        CellStyle style84 = wb.createCellStyle();
        Font font84 = wb.createFont();
        font84.setFontHeightInPoints((short)11);
        font84.setColor((short)0);
        style84.setFont(font84);
        style84.setAlignment(HorizontalAlignment.CENTER);
        style84.setVerticalAlignment(VerticalAlignment.CENTER);
        style84.setFillForegroundColor((short)64);
        style84.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style84.setBorderTop(BorderStyle.THIN);
        style84.setBorderBottom(BorderStyle.THIN);
        style84.setBorderLeft(BorderStyle.NONE);
        style84.setBorderRight(BorderStyle.THIN);

        CellStyle style85 = wb.createCellStyle();
        Font font85 = wb.createFont();
        font85.setFontHeightInPoints((short)11);
        font85.setColor((short)0);
        style85.setFont(font85);
        style85.setAlignment(HorizontalAlignment.LEFT);
        style85.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style85.setFillForegroundColor((short)64);
        style85.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style85.setBorderTop(BorderStyle.THIN);
        style85.setBorderBottom(BorderStyle.THIN);
        style85.setBorderLeft(BorderStyle.THIN);
        style85.setBorderRight(BorderStyle.NONE);

        CellStyle style86 = wb.createCellStyle();
        Font font86 = wb.createFont();
        font86.setFontHeightInPoints((short)11);
        font86.setColor((short)0);
        style86.setFont(font86);
        style86.setAlignment(HorizontalAlignment.LEFT);
        style86.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style86.setFillForegroundColor((short)64);
        style86.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style86.setBorderTop(BorderStyle.THIN);
        style86.setBorderBottom(BorderStyle.THIN);
        style86.setBorderLeft(BorderStyle.NONE);
        style86.setBorderRight(BorderStyle.THIN);

        CellStyle style87 = wb.createCellStyle();
        Font font87 = wb.createFont();
        font87.setBold(true);
        font87.setFontHeightInPoints((short)11);
        font87.setColor((short)0);
        style87.setFont(font87);
        style87.setAlignment(HorizontalAlignment.CENTER);
        style87.setVerticalAlignment(VerticalAlignment.CENTER);
        style87.setFillForegroundColor((short)64);
        style87.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style87.setBorderTop(BorderStyle.THIN);
        style87.setBorderBottom(BorderStyle.THIN);
        style87.setBorderLeft(BorderStyle.THIN);
        style87.setBorderRight(BorderStyle.NONE);

        CellStyle style88 = wb.createCellStyle();
        Font font88 = wb.createFont();
        font88.setBold(true);
        font88.setFontHeightInPoints((short)11);
        font88.setColor((short)0);
        style88.setFont(font88);
        style88.setAlignment(HorizontalAlignment.CENTER);
        style88.setVerticalAlignment(VerticalAlignment.CENTER);
        style88.setFillForegroundColor((short)64);
        style88.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style88.setBorderTop(BorderStyle.THIN);
        style88.setBorderBottom(BorderStyle.THIN);
        style88.setBorderLeft(BorderStyle.NONE);
        style88.setBorderRight(BorderStyle.NONE);

        CellStyle style89 = wb.createCellStyle();
        Font font89 = wb.createFont();
        font89.setBold(true);
        font89.setFontHeightInPoints((short)11);
        font89.setColor((short)0);
        style89.setFont(font89);
        style89.setAlignment(HorizontalAlignment.CENTER);
        style89.setVerticalAlignment(VerticalAlignment.CENTER);
        style89.setFillForegroundColor((short)64);
        style89.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style89.setBorderTop(BorderStyle.THIN);
        style89.setBorderBottom(BorderStyle.THIN);
        style89.setBorderLeft(BorderStyle.NONE);
        style89.setBorderRight(BorderStyle.THIN);

        CellStyle style90 = wb.createCellStyle();
        Font font90 = wb.createFont();
        font90.setFontHeightInPoints((short)11);
        font90.setColor((short)0);
        style90.setFont(font90);
        style90.setAlignment(HorizontalAlignment.CENTER);
        style90.setVerticalAlignment(VerticalAlignment.BOTTOM);
        style90.setFillForegroundColor((short)0);
        style90.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style90.setBorderTop(BorderStyle.NONE);
        style90.setBorderBottom(BorderStyle.THIN);
        style90.setBorderLeft(BorderStyle.THIN);
        style90.setBorderRight(BorderStyle.NONE);

        // Remove solid fills from generated styles to avoid black backgrounds
        style0.setFillPattern(FillPatternType.NO_FILL);
        style1.setFillPattern(FillPatternType.NO_FILL);
        style2.setFillPattern(FillPatternType.NO_FILL);
        style3.setFillPattern(FillPatternType.NO_FILL);
        style4.setFillPattern(FillPatternType.NO_FILL);
        style5.setFillPattern(FillPatternType.NO_FILL);
        style6.setFillPattern(FillPatternType.NO_FILL);
        style7.setFillPattern(FillPatternType.NO_FILL);
        style8.setFillPattern(FillPatternType.NO_FILL);
        style9.setFillPattern(FillPatternType.NO_FILL);
        style10.setFillPattern(FillPatternType.NO_FILL);
        style11.setFillPattern(FillPatternType.NO_FILL);
        style12.setFillPattern(FillPatternType.NO_FILL);
        style13.setFillPattern(FillPatternType.NO_FILL);
        style14.setFillPattern(FillPatternType.NO_FILL);
        style15.setFillPattern(FillPatternType.NO_FILL);
        style16.setFillPattern(FillPatternType.NO_FILL);
        style17.setFillPattern(FillPatternType.NO_FILL);
        style18.setFillPattern(FillPatternType.NO_FILL);
        style19.setFillPattern(FillPatternType.NO_FILL);
        style20.setFillPattern(FillPatternType.NO_FILL);
        style21.setFillPattern(FillPatternType.NO_FILL);
        style22.setFillPattern(FillPatternType.NO_FILL);
        style23.setFillPattern(FillPatternType.NO_FILL);
        style24.setFillPattern(FillPatternType.NO_FILL);
        style25.setFillPattern(FillPatternType.NO_FILL);
        style26.setFillPattern(FillPatternType.NO_FILL);
        style27.setFillPattern(FillPatternType.NO_FILL);
        style28.setFillPattern(FillPatternType.NO_FILL);
        style29.setFillPattern(FillPatternType.NO_FILL);
        style30.setFillPattern(FillPatternType.NO_FILL);
        style31.setFillPattern(FillPatternType.NO_FILL);
        style32.setFillPattern(FillPatternType.NO_FILL);
        style33.setFillPattern(FillPatternType.NO_FILL);
        style34.setFillPattern(FillPatternType.NO_FILL);
        style35.setFillPattern(FillPatternType.NO_FILL);
        style36.setFillPattern(FillPatternType.NO_FILL);
        style37.setFillPattern(FillPatternType.NO_FILL);
        style38.setFillPattern(FillPatternType.NO_FILL);
        style39.setFillPattern(FillPatternType.NO_FILL);
        style40.setFillPattern(FillPatternType.NO_FILL);
        style41.setFillPattern(FillPatternType.NO_FILL);
        style42.setFillPattern(FillPatternType.NO_FILL);
        style43.setFillPattern(FillPatternType.NO_FILL);
        style44.setFillPattern(FillPatternType.NO_FILL);
        style45.setFillPattern(FillPatternType.NO_FILL);
        style46.setFillPattern(FillPatternType.NO_FILL);
        style47.setFillPattern(FillPatternType.NO_FILL);
        style48.setFillPattern(FillPatternType.NO_FILL);
        style49.setFillPattern(FillPatternType.NO_FILL);
        style50.setFillPattern(FillPatternType.NO_FILL);
        style51.setFillPattern(FillPatternType.NO_FILL);
        style52.setFillPattern(FillPatternType.NO_FILL);
        style53.setFillPattern(FillPatternType.NO_FILL);
        style54.setFillPattern(FillPatternType.NO_FILL);
        style55.setFillPattern(FillPatternType.NO_FILL);
        style56.setFillPattern(FillPatternType.NO_FILL);
        style57.setFillPattern(FillPatternType.NO_FILL);
        style58.setFillPattern(FillPatternType.NO_FILL);
        style59.setFillPattern(FillPatternType.NO_FILL);
        style60.setFillPattern(FillPatternType.NO_FILL);
        style61.setFillPattern(FillPatternType.NO_FILL);
        style62.setFillPattern(FillPatternType.NO_FILL);
        style63.setFillPattern(FillPatternType.NO_FILL);
        style64.setFillPattern(FillPatternType.NO_FILL);
        style65.setFillPattern(FillPatternType.NO_FILL);
        style66.setFillPattern(FillPatternType.NO_FILL);
        style67.setFillPattern(FillPatternType.NO_FILL);
        style68.setFillPattern(FillPatternType.NO_FILL);
        style69.setFillPattern(FillPatternType.NO_FILL);
        style70.setFillPattern(FillPatternType.NO_FILL);
        style71.setFillPattern(FillPatternType.NO_FILL);
        style72.setFillPattern(FillPatternType.NO_FILL);
        style73.setFillPattern(FillPatternType.NO_FILL);
        style74.setFillPattern(FillPatternType.NO_FILL);
        style75.setFillPattern(FillPatternType.NO_FILL);
        style76.setFillPattern(FillPatternType.NO_FILL);
        style77.setFillPattern(FillPatternType.NO_FILL);
        style78.setFillPattern(FillPatternType.NO_FILL);
        style79.setFillPattern(FillPatternType.NO_FILL);
        style80.setFillPattern(FillPatternType.NO_FILL);
        style81.setFillPattern(FillPatternType.NO_FILL);
        style82.setFillPattern(FillPatternType.NO_FILL);
        style83.setFillPattern(FillPatternType.NO_FILL);
        style84.setFillPattern(FillPatternType.NO_FILL);
        style85.setFillPattern(FillPatternType.NO_FILL);
        style86.setFillPattern(FillPatternType.NO_FILL);
        style87.setFillPattern(FillPatternType.NO_FILL);
        style88.setFillPattern(FillPatternType.NO_FILL);
        style89.setFillPattern(FillPatternType.NO_FILL);
        style90.setFillPattern(FillPatternType.NO_FILL);

        // Dark gray header (hex #404040) with white font for specific labels
        // Use XSSFColor for exact RGB and re-apply fill/font after the NO_FILL reset
        XSSFColor darkColor = new XSSFColor(new byte[]{(byte)64,(byte)64,(byte)64}, ((XSSFWorkbook)wb).getStylesSource().getIndexedColors());

        CellStyle style70b = null;
        try {
            ((XSSFCellStyle)style16).setFillForegroundColor(darkColor);
            style16.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            font16.setColor(IndexedColors.WHITE.getIndex());

            ((XSSFCellStyle)style34).setFillForegroundColor(darkColor);
            style34.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            font34.setColor(IndexedColors.WHITE.getIndex());

            ((XSSFCellStyle)style38).setFillForegroundColor(darkColor);
            style38.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            font38.setColor(IndexedColors.WHITE.getIndex());

            // create a variant of style70 with dark gray fill + white font for B53 only
            style70b = wb.createCellStyle();
            Font font70b = wb.createFont();
            font70b.setFontHeightInPoints((short)11);
            font70b.setColor(IndexedColors.WHITE.getIndex());
            style70b.setFont(font70b);
            style70b.setAlignment(HorizontalAlignment.CENTER);
            style70b.setVerticalAlignment(VerticalAlignment.BOTTOM);
            ((XSSFCellStyle)style70b).setFillForegroundColor(darkColor);
            style70b.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style70b.setBorderTop(BorderStyle.THIN);
            style70b.setBorderBottom(BorderStyle.NONE);
            style70b.setBorderLeft(BorderStyle.THIN);
            style70b.setBorderRight(BorderStyle.NONE);
        } catch (ClassCastException e) {
            // If cast to XSSFCellStyle not available, fall back to indexed gray
            short grayIdx = IndexedColors.GREY_50_PERCENT.getIndex();
            style16.setFillForegroundColor(grayIdx); style16.setFillPattern(FillPatternType.SOLID_FOREGROUND); font16.setColor(IndexedColors.WHITE.getIndex());
            style34.setFillForegroundColor(grayIdx); style34.setFillPattern(FillPatternType.SOLID_FOREGROUND); font34.setColor(IndexedColors.WHITE.getIndex());
            style38.setFillForegroundColor(grayIdx); style38.setFillPattern(FillPatternType.SOLID_FOREGROUND); font38.setColor(IndexedColors.WHITE.getIndex());
            // create fallback style70b using indexed gray for B53 only
            style70b = wb.createCellStyle();
            Font font70b = wb.createFont();
            font70b.setFontHeightInPoints((short)11);
            font70b.setColor(IndexedColors.WHITE.getIndex());
            style70b.setFont(font70b);
            style70b.setAlignment(HorizontalAlignment.CENTER);
            style70b.setVerticalAlignment(VerticalAlignment.BOTTOM);
            style70b.setFillForegroundColor(grayIdx);
            style70b.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style70b.setBorderTop(BorderStyle.THIN);
            style70b.setBorderBottom(BorderStyle.NONE);
            style70b.setBorderLeft(BorderStyle.THIN);
            style70b.setBorderRight(BorderStyle.NONE);
        }

        // Merged regions
        sh.addMergedRegion(new CellRangeAddress(1,3,10,12));  // K2:M4 para logo Selcomp
        sh.addMergedRegion(new CellRangeAddress(52,52,1,5));
        sh.addMergedRegion(new CellRangeAddress(4,4,10,12));
        sh.addMergedRegion(new CellRangeAddress(6,7,10,10));
        sh.addMergedRegion(new CellRangeAddress(17,18,1,2));
        sh.addMergedRegion(new CellRangeAddress(17,18,3,4));
        sh.addMergedRegion(new CellRangeAddress(17,18,5,6));
        sh.addMergedRegion(new CellRangeAddress(17,18,7,8));
        sh.addMergedRegion(new CellRangeAddress(8,8,1,12));
        sh.addMergedRegion(new CellRangeAddress(6,7,11,11));
        sh.addMergedRegion(new CellRangeAddress(6,7,12,12));
        sh.addMergedRegion(new CellRangeAddress(7,7,1,3));
        sh.addMergedRegion(new CellRangeAddress(17,18,10,12));
        sh.addMergedRegion(new CellRangeAddress(17,18,9,9));
        sh.addMergedRegion(new CellRangeAddress(19,21,1,2));
        sh.addMergedRegion(new CellRangeAddress(19,21,3,4));
        sh.addMergedRegion(new CellRangeAddress(19,21,5,6));
        sh.addMergedRegion(new CellRangeAddress(19,21,7,8));
        sh.addMergedRegion(new CellRangeAddress(19,21,9,9));
        sh.addMergedRegion(new CellRangeAddress(19,21,10,12));
        sh.addMergedRegion(new CellRangeAddress(5,6,1,9));
        sh.addMergedRegion(new CellRangeAddress(7,7,4,9));
        sh.addMergedRegion(new CellRangeAddress(26,26,3,6));
        sh.addMergedRegion(new CellRangeAddress(23,24,7,9));
        sh.addMergedRegion(new CellRangeAddress(25,25,7,9));
        sh.addMergedRegion(new CellRangeAddress(26,26,7,8));
        sh.addMergedRegion(new CellRangeAddress(23,24,10,12));
        sh.addMergedRegion(new CellRangeAddress(25,25,10,12));
        sh.addMergedRegion(new CellRangeAddress(23,24,1,2));
        sh.addMergedRegion(new CellRangeAddress(22,22,1,12));
        sh.addMergedRegion(new CellRangeAddress(23,23,3,6));
        sh.addMergedRegion(new CellRangeAddress(24,24,3,4));
        sh.addMergedRegion(new CellRangeAddress(24,24,5,6));
        sh.addMergedRegion(new CellRangeAddress(25,25,1,2));
        sh.addMergedRegion(new CellRangeAddress(25,25,3,4));
        sh.addMergedRegion(new CellRangeAddress(25,25,5,6));
        sh.addMergedRegion(new CellRangeAddress(27,27,1,12));
        sh.addMergedRegion(new CellRangeAddress(28,29,1,2));
        sh.addMergedRegion(new CellRangeAddress(30,30,1,2));
        sh.addMergedRegion(new CellRangeAddress(28,29,3,4));
        sh.addMergedRegion(new CellRangeAddress(28,29,5,6));
        sh.addMergedRegion(new CellRangeAddress(30,30,3,4));
        sh.addMergedRegion(new CellRangeAddress(30,30,5,6));
        sh.addMergedRegion(new CellRangeAddress(28,29,7,9));
        sh.addMergedRegion(new CellRangeAddress(30,30,7,9));
        sh.addMergedRegion(new CellRangeAddress(28,29,10,12));
        sh.addMergedRegion(new CellRangeAddress(32,33,10,12));
        sh.addMergedRegion(new CellRangeAddress(34,34,1,2));
        sh.addMergedRegion(new CellRangeAddress(34,34,3,6));
        sh.addMergedRegion(new CellRangeAddress(34,34,7,9));
        sh.addMergedRegion(new CellRangeAddress(34,34,10,12));
        sh.addMergedRegion(new CellRangeAddress(35,35,1,12));
        sh.addMergedRegion(new CellRangeAddress(32,33,1,2));
        sh.addMergedRegion(new CellRangeAddress(9,9,3,12));
        sh.addMergedRegion(new CellRangeAddress(10,10,3,12));
        sh.addMergedRegion(new CellRangeAddress(11,11,3,12));
        sh.addMergedRegion(new CellRangeAddress(12,12,3,12));
        sh.addMergedRegion(new CellRangeAddress(13,13,3,12));
        sh.addMergedRegion(new CellRangeAddress(14,14,3,12));
        sh.addMergedRegion(new CellRangeAddress(32,33,3,6));
        sh.addMergedRegion(new CellRangeAddress(32,33,7,9));
        sh.addMergedRegion(new CellRangeAddress(30,30,10,12));
        sh.addMergedRegion(new CellRangeAddress(31,31,1,12));
        sh.addMergedRegion(new CellRangeAddress(9,9,1,2));
        sh.addMergedRegion(new CellRangeAddress(10,10,1,2));
        sh.addMergedRegion(new CellRangeAddress(11,11,1,2));
        sh.addMergedRegion(new CellRangeAddress(12,12,1,2));
        sh.addMergedRegion(new CellRangeAddress(13,13,1,2));
        sh.addMergedRegion(new CellRangeAddress(14,14,1,2));
        sh.addMergedRegion(new CellRangeAddress(26,26,10,11));
        sh.addMergedRegion(new CellRangeAddress(37,37,1,2));
        sh.addMergedRegion(new CellRangeAddress(38,38,1,2));
        sh.addMergedRegion(new CellRangeAddress(39,39,1,2));
        sh.addMergedRegion(new CellRangeAddress(37,37,3,4));
        sh.addMergedRegion(new CellRangeAddress(37,37,5,6));
        sh.addMergedRegion(new CellRangeAddress(37,37,7,8));
        sh.addMergedRegion(new CellRangeAddress(38,38,3,4));
        sh.addMergedRegion(new CellRangeAddress(38,38,5,6));
        sh.addMergedRegion(new CellRangeAddress(38,38,7,8));
        sh.addMergedRegion(new CellRangeAddress(39,39,3,4));
        sh.addMergedRegion(new CellRangeAddress(52,59,6,7));
        sh.addMergedRegion(new CellRangeAddress(1,4,1,3));
        sh.addMergedRegion(new CellRangeAddress(1,1,4,9));
        sh.addMergedRegion(new CellRangeAddress(4,4,4,9));
        sh.addMergedRegion(new CellRangeAddress(59,59,3,5));
        sh.addMergedRegion(new CellRangeAddress(58,58,10,12));
        sh.addMergedRegion(new CellRangeAddress(59,59,10,12));
        sh.addMergedRegion(new CellRangeAddress(15,16,1,12));
        sh.addMergedRegion(new CellRangeAddress(26,26,1,2));
        sh.addMergedRegion(new CellRangeAddress(52,52,8,12));
        sh.addMergedRegion(new CellRangeAddress(46,46,1,12));
        sh.addMergedRegion(new CellRangeAddress(58,58,1,2));
        sh.addMergedRegion(new CellRangeAddress(59,59,1,2));
        sh.addMergedRegion(new CellRangeAddress(57,57,8,12));
        sh.addMergedRegion(new CellRangeAddress(57,57,1,5));
        sh.addMergedRegion(new CellRangeAddress(59,59,8,9));
        sh.addMergedRegion(new CellRangeAddress(58,58,8,9));
        sh.addMergedRegion(new CellRangeAddress(58,58,3,5));
        sh.addMergedRegion(new CellRangeAddress(39,39,5,6));
        sh.addMergedRegion(new CellRangeAddress(39,39,7,8));
        sh.addMergedRegion(new CellRangeAddress(37,39,9,12));
        sh.addMergedRegion(new CellRangeAddress(36,36,9,12));
        sh.addMergedRegion(new CellRangeAddress(36,36,1,8));
        sh.addMergedRegion(new CellRangeAddress(40,40,1,12));
        
        // Región combinada para PROCEDIMIENTO REALIZADO (B42:M46 = filas 41-45, columnas 1-12)
        sh.addMergedRegion(new CellRangeAddress(41,45,1,12));
        
        // Región combinada para OBSERVACIONES (B48:M52 = filas 47-51, columnas 1-12)
        sh.addMergedRegion(new CellRangeAddress(47,51,1,12));

        // Cells
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(1);
            cell.setCellValue("IMAGEN DEL PROYECTO");
            cell.setCellStyle(style0);
        }
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style1);
        }
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style1);
        }
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(4);
            cell.setCellValue("FORMATO CONCEPTO DE MANTENIMIENTO");
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(10);
            // Imagen de Selcomp se insertará después
            cell.setCellStyle(style3);
        }
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style3);
        }
        { Row row = sh.getRow(1); if (row==null) row = sh.createRow(1); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style4);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style5);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style6);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style6);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style8);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style8);
        }
        { Row row = sh.getRow(2); if (row==null) row = sh.createRow(2); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style9);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style5);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style6);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style6);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style10);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style10);
        }
        { Row row = sh.getRow(3); if (row==null) row = sh.createRow(3); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style11);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style12);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style13);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style13);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(4);
            cell.setCellValue("TIPO DE SOLICITUD");
            cell.setCellStyle(style14);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style14);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style14);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style14);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style14);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style15);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(10);
            cell.setCellValue("FECHA");
            cell.setCellStyle(style16);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(4); if (row==null) row = sh.createRow(4); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(1);
            cell.setCellValue("MANTENIMIENTO PREVENTIVO");
            cell.setCellStyle(style19);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style20);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style20);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style20);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style20);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style20);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style20);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style20);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style21);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(10);
            cell.setCellValue("Dia");
            cell.setCellStyle(style22);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(11);
            cell.setCellValue("Mes");
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(5); if (row==null) row = sh.createRow(5); Cell cell = row.createCell(12);
            cell.setCellValue("Año");
            cell.setCellStyle(style22);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style23);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style24);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style24);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style24);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style24);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style24);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style24);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style24);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style25);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style26);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style26);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(1);
            cell.setCellValue("TICKET N*");
            cell.setCellStyle(style28);
        }
        // Aplicar style28 a todas las celdas de la región combinada que contenga B (col 1) en la fila 6
        try {
            int rowIdx = 6;
            int colIdx = 1;
            for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                CellRangeAddress cra = sh.getMergedRegion(i);
                if (rowIdx >= cra.getFirstRow() && rowIdx <= cra.getLastRow()
                        && colIdx >= cra.getFirstColumn() && colIdx <= cra.getLastColumn()) {
                    for (int r = cra.getFirstRow(); r <= cra.getLastRow(); r++) {
                        Row rr = sh.getRow(r);
                        if (rr == null) rr = sh.createRow(r);
                        for (int c = cra.getFirstColumn(); c <= cra.getLastColumn(); c++) {
                            Cell cc = rr.getCell(c);
                            if (cc == null) cc = rr.createCell(c);
                            cc.setCellStyle(style28);
                        }
                    }
                    break;
                }
            }
        } catch (Exception _ignore) {}
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style29);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(10);
            cell.setCellValue(dia);
            cell.setCellStyle(style32);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(11);
            cell.setCellValue(mes);
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(6); if (row==null) row = sh.createRow(6); Cell cell = row.createCell(12);
            cell.setCellValue(anio);
            cell.setCellStyle(style32);
        }
        // Aplicar style32/style33 a todas las celdas de las regiones combinadas que incluyan las columnas de fecha (K=10, L=11, M=12)
        try {
            int[] cols = new int[]{10,11,12};
            for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                CellRangeAddress cra = sh.getMergedRegion(i);
                boolean intersects = false;
                for (int c : cols) { if (c >= cra.getFirstColumn() && c <= cra.getLastColumn()) { intersects = true; break; } }
                if (!intersects) continue;
                for (int r = cra.getFirstRow(); r <= cra.getLastRow(); r++) {
                    Row rr = sh.getRow(r);
                    if (rr == null) rr = sh.createRow(r);
                    for (int c = cra.getFirstColumn(); c <= cra.getLastColumn(); c++) {
                        Cell cc = rr.getCell(c);
                        if (cc == null) cc = rr.createCell(c);
                        if (c == 10 || c == 12) cc.setCellStyle(style32);
                        else if (c == 11) cc.setCellStyle(style33);
                    }
                }
            }
        } catch (Exception _ignore) {}
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(1);
            cell.setCellValue("DATOS DEL USUARIO");
            cell.setCellStyle(style34);
        }
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style36);
        }
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style36);
        }
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style36);
        }
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style36);
        }
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style36);
        }
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style36);
        }
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(8); if (row==null) row = sh.createRow(8); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style37);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(1);
            cell.setCellValue("CIUDAD");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(9); if (row==null) row = sh.createRow(9); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(1);
            cell.setCellValue("DIRECCION");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(10); if (row==null) row = sh.createRow(10); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(1);
            cell.setCellValue("NOMBRE");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(11); if (row==null) row = sh.createRow(11); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(1);
            cell.setCellValue("CORREO");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(12); if (row==null) row = sh.createRow(12); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(1);
            cell.setCellValue("TECNICO");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(13); if (row==null) row = sh.createRow(13); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(1);
            cell.setCellValue("SEDE/OFICINA");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(14); if (row==null) row = sh.createRow(14); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(1);
            cell.setCellValue("DESCRIPCION DEL HARDWARE");
            cell.setCellStyle(style38);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(15); if (row==null) row = sh.createRow(15); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(16); if (row==null) row = sh.createRow(16); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(1);
            cell.setCellValue("TIPO DISPOSITIVO");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(3);
            cell.setCellValue("MARCA");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(5);
            cell.setCellValue("MODELO");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(7);
            cell.setCellValue("SERIAL");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(9);
            cell.setCellValue("PLACA");
            cell.setCellStyle(style26);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(10);
            cell.setCellValue("CONDICIONES");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(17); if (row==null) row = sh.createRow(17); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(9);
            cell.setCellValue(dia);
            cell.setCellStyle(style32);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(18); if (row==null) row = sh.createRow(18); Cell cell = row.createCell(12);
            cell.setCellValue(mes);
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style26);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(19); if (row==null) row = sh.createRow(19); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { 
            int writeRow = 20, writeCol = 1;
            // si la celda está dentro de una región combinada, escribir en la celda superior-izquierda
            CellRangeAddress matchedCra = null;
            try {
                for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                    CellRangeAddress cra = sh.getMergedRegion(i);
                    if (cra.isInRange(writeRow, writeCol)) {
                        matchedCra = cra;
                        writeRow = cra.getFirstRow();
                        writeCol = cra.getFirstColumn();
                        break;
                    }
                }
            } catch (Exception _ignore) {}
            Row row = sh.getRow(writeRow); if (row==null) row = sh.createRow(writeRow); Cell cell = row.createCell(writeCol);
            // Tipo de informacion (capturado automáticamente desde systeminfo)
            cell.setCellValue(SYS_TIPO);
            // Aplicar estilo (bordes) a toda la región combinada si existe, sino solo a la celda
            if (matchedCra != null) {
                for (int r = matchedCra.getFirstRow(); r <= matchedCra.getLastRow(); r++) {
                    Row rr = sh.getRow(r); if (rr == null) rr = sh.createRow(r);
                    for (int c = matchedCra.getFirstColumn(); c <= matchedCra.getLastColumn(); c++) {
                        Cell cc = rr.getCell(c); if (cc == null) cc = rr.createCell(c);
                        cc.setCellStyle(style42);
                    }
                }
            } else {
                cell.setCellStyle(style42);
            }
        }
        { Row row = sh.getRow(20); if (row==null) row = sh.createRow(20); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style43);
        }
        { 
            int writeRow = 20, writeCol = 3;
            CellRangeAddress matchedCra = null;
            try {
                for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                    CellRangeAddress cra = sh.getMergedRegion(i);
                    if (cra.isInRange(writeRow, writeCol)) {
                        matchedCra = cra;
                        writeRow = cra.getFirstRow();
                        writeCol = cra.getFirstColumn();
                        break;
                    }
                }
            } catch (Exception _ignore) {}
            Row row = sh.getRow(writeRow); if (row==null) row = sh.createRow(writeRow); Cell cell = row.createCell(writeCol);
            // Marca (capturada automáticamente desde systeminfo)
            cell.setCellValue(SYS_MARCA);
            if (matchedCra != null) {
                for (int r = matchedCra.getFirstRow(); r <= matchedCra.getLastRow(); r++) {
                    Row rr = sh.getRow(r); if (rr == null) rr = sh.createRow(r);
                    for (int c = matchedCra.getFirstColumn(); c <= matchedCra.getLastColumn(); c++) {
                        Cell cc = rr.getCell(c); if (cc == null) cc = rr.createCell(c);
                        cc.setCellStyle(style42);
                    }
                }
            } else {
                cell.setCellStyle(style42);
            }
        }
        { Row row = sh.getRow(20); if (row==null) row = sh.createRow(20); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style43);
        }
        { 
            int writeRow = 20, writeCol = 5;
            CellRangeAddress matchedCra = null;
            try {
                for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                    CellRangeAddress cra = sh.getMergedRegion(i);
                    if (cra.isInRange(writeRow, writeCol)) {
                        matchedCra = cra;
                        writeRow = cra.getFirstRow();
                        writeCol = cra.getFirstColumn();
                        break;
                    }
                }
            } catch (Exception _ignore) {}
            Row row = sh.getRow(writeRow); if (row==null) row = sh.createRow(writeRow); Cell cell = row.createCell(writeCol);
            // Modelo (capturado automáticamente desde systeminfo)
            cell.setCellValue(SYS_MODELO);
            if (matchedCra != null) {
                for (int r = matchedCra.getFirstRow(); r <= matchedCra.getLastRow(); r++) {
                    Row rr = sh.getRow(r); if (rr == null) rr = sh.createRow(r);
                    for (int c = matchedCra.getFirstColumn(); c <= matchedCra.getLastColumn(); c++) {
                        Cell cc = rr.getCell(c); if (cc == null) cc = rr.createCell(c);
                        cc.setCellStyle(style42);
                    }
                }
            } else {
                cell.setCellStyle(style42);
            }
        }
        { Row row = sh.getRow(20); if (row==null) row = sh.createRow(20); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style43);
        }
        { Row row = sh.getRow(20); if (row==null) row = sh.createRow(20); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style42);
        }
        { Row row = sh.getRow(20); if (row==null) row = sh.createRow(20); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style43);
        }
        { Row row = sh.getRow(20); if (row==null) row = sh.createRow(20); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style44);
        }
        { Row row = sh.getRow(20); if (row==null) row = sh.createRow(20); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style42);
        }
        { Row row = sh.getRow(20); if (row==null) row = sh.createRow(20); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style45);
        }
        { Row row = sh.getRow(20); if (row==null) row = sh.createRow(20); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style43);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(9);
            cell.setCellValue(anio);
            cell.setCellStyle(style32);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(21); if (row==null) row = sh.createRow(21); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(1);
            cell.setCellValue("PC");
            cell.setCellStyle(style34);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(22); if (row==null) row = sh.createRow(22); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style47);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(1);
            cell.setCellValue("ENCIENDE?");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(3);
            cell.setCellValue("Unidades");
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style29);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style29);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(7);
            cell.setCellValue("BOTONES COMPLETOS");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(10);
            cell.setCellValue("CONDICIONES FISICAS");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(23); if (row==null) row = sh.createRow(23); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(3);
            cell.setCellValue("DISCO DURO");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(5);
            cell.setCellValue("CD/DVD");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(24); if (row==null) row = sh.createRow(24); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(1);
            cell.setCellValue("SI/NO");
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(3);
            cell.setCellValue("SI/NO");
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(5);
            cell.setCellValue("SI/NO");
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(7);
            cell.setCellValue("SI/NO");
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style29);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(25); if (row==null) row = sh.createRow(25); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(26); if (row==null) row = sh.createRow(26); Cell cell = row.createCell(1);
            cell.setCellValue("PROCESADOR");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(26); if (row==null) row = sh.createRow(26); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { 
            int writeRow = 26, writeCol = 3;
            CellRangeAddress matchedCra = null;
            try {
                for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                    CellRangeAddress cra = sh.getMergedRegion(i);
                    if (cra.isInRange(writeRow, writeCol)) {
                        matchedCra = cra;
                        writeRow = cra.getFirstRow();
                        writeCol = cra.getFirstColumn();
                        break;
                    }
                }
            } catch (Exception _ignore) {}
            Row row = sh.getRow(writeRow); if (row==null) row = sh.createRow(writeRow); Cell cell = row.createCell(writeCol);
            // Procesador (capturado automáticamente desde systeminfo) — versión abreviada para la plantilla
            cell.setCellValue(SYS_CPU_SHORT);
            if (matchedCra != null) {
                for (int r = matchedCra.getFirstRow(); r <= matchedCra.getLastRow(); r++) {
                    Row rr = sh.getRow(r); if (rr == null) rr = sh.createRow(r);
                    for (int c = matchedCra.getFirstColumn(); c <= matchedCra.getLastColumn(); c++) {
                        Cell cc = rr.getCell(c); if (cc == null) cc = rr.createCell(c);
                        cc.setCellStyle(style31);
                    }
                }
            } else {
                cell.setCellStyle(style31);
            }
        }
        { Row row = sh.getRow(26); if (row==null) row = sh.createRow(26); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(26); if (row==null) row = sh.createRow(26); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(26); if (row==null) row = sh.createRow(26); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(26); if (row==null) row = sh.createRow(26); Cell cell = row.createCell(7);
            cell.setCellValue("MEMORIA RAM");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(26); if (row==null) row = sh.createRow(26); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { 
            int writeRow = 26, writeCol = 9;
            CellRangeAddress matchedCra = null;
            try {
                for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                    CellRangeAddress cra = sh.getMergedRegion(i);
                    if (cra.isInRange(writeRow, writeCol)) {
                        matchedCra = cra;
                        writeRow = cra.getFirstRow();
                        writeCol = cra.getFirstColumn();
                        break;
                    }
                }
            } catch (Exception _ignore) {}
            Row row = sh.getRow(writeRow); if (row==null) row = sh.createRow(writeRow); Cell cell = row.createCell(writeCol);
            // Memoria RAM (abreviada)
            cell.setCellValue(SYS_RAM_SHORT);
            if (matchedCra != null) {
                for (int r = matchedCra.getFirstRow(); r <= matchedCra.getLastRow(); r++) {
                    Row rr = sh.getRow(r); if (rr == null) rr = sh.createRow(r);
                    for (int c = matchedCra.getFirstColumn(); c <= matchedCra.getLastColumn(); c++) {
                        Cell cc = rr.getCell(c); if (cc == null) cc = rr.createCell(c);
                        cc.setCellStyle(style31);
                    }
                }
            } else {
                cell.setCellStyle(style31);
            }
        }
        { Row row = sh.getRow(26); if (row==null) row = sh.createRow(26); Cell cell = row.createCell(10);
            cell.setCellValue("DISCO DURO");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(26); if (row==null) row = sh.createRow(26); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { 
            int writeRow = 26, writeCol = 12;
            CellRangeAddress matchedCra = null;
            try {
                for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                    CellRangeAddress cra = sh.getMergedRegion(i);
                    if (cra.isInRange(writeRow, writeCol)) {
                        matchedCra = cra;
                        writeRow = cra.getFirstRow();
                        writeCol = cra.getFirstColumn();
                        break;
                    }
                }
            } catch (Exception _ignore) {}
            Row row = sh.getRow(writeRow); if (row==null) row = sh.createRow(writeRow); Cell cell = row.createCell(writeCol);
            // Disco duro (tamaño abreviado)
            cell.setCellValue(SYS_DISK_SHORT);
            if (matchedCra != null) {
                for (int r = matchedCra.getFirstRow(); r <= matchedCra.getLastRow(); r++) {
                    Row rr = sh.getRow(r); if (rr == null) rr = sh.createRow(r);
                    for (int c = matchedCra.getFirstColumn(); c <= matchedCra.getLastColumn(); c++) {
                        Cell cc = rr.getCell(c); if (cc == null) cc = rr.createCell(c);
                        cc.setCellStyle(style48);
                    }
                }
            } else {
                cell.setCellStyle(style48);
            }
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(1);
            cell.setCellValue("MONITOR");
            cell.setCellStyle(style34);
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style36);
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style36);
        }
        { Row row = sh.getRow(27); if (row==null) row = sh.createRow(27); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style49);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(1);
            cell.setCellValue("ENCIENDE?");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(3);
            cell.setCellValue("PANTALLA");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(5);
            cell.setCellValue("ONLY ONE");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(7);
            cell.setCellValue("BOTONES COMPLETOS");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(10);
            cell.setCellValue("CONDISIONES FISICAS");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(28); if (row==null) row = sh.createRow(28); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(29); if (row==null) row = sh.createRow(29); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(1);
            cell.setCellValue("SI/NO");
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(3);
            cell.setCellValue("SI/NO");
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(5);
            cell.setCellValue("SI/NO");
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(7);
            cell.setCellValue("SI/NO");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(30); if (row==null) row = sh.createRow(30); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(1);
            cell.setCellValue("TECLADO");
            cell.setCellStyle(style34);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(31); if (row==null) row = sh.createRow(31); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style37);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(1);
            cell.setCellValue("ENCIENDE?");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(3);
            cell.setCellValue("FUNCIONA CORRECTAMENTE?");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(7);
            cell.setCellValue("BOTONES COMPLETOS");
            cell.setCellStyle(style50);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(10);
            cell.setCellValue("CONDICIONES FISICAS");
            cell.setCellStyle(style41);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style2);
        }
        { Row row = sh.getRow(32); if (row==null) row = sh.createRow(32); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style27);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style39);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style40);
        }
        { Row row = sh.getRow(33); if (row==null) row = sh.createRow(33); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style33);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(1);
            cell.setCellValue("SI/NO");
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(3);
            cell.setCellValue("SI/NO");
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style29);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style29);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(7);
            cell.setCellValue("SI/NO");
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style29);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style28);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style29);
        }
        { Row row = sh.getRow(34); if (row==null) row = sh.createRow(34); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style30);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(1);
            cell.setCellValue("SOFTWARE");
            cell.setCellStyle(style34);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(35); if (row==null) row = sh.createRow(35); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style37);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(1);
            cell.setCellValue("PROGRAMAS BASICOS");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(9);
            cell.setCellValue("OTROS PROGRAMAS");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(36); if (row==null) row = sh.createRow(36); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style51);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style52);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style52);
        }
        { Row row = sh.getRow(37); if (row==null) row = sh.createRow(37); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style53);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style54);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style55);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style55);
        }
        { Row row = sh.getRow(38); if (row==null) row = sh.createRow(38); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style56);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style57);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style58);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style58);
        }
        { Row row = sh.getRow(39); if (row==null) row = sh.createRow(39); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style59);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(1);
            cell.setCellValue("PROCEDIMIENTO REALIZADO");
            cell.setCellStyle(style34);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style46);
        }
        { Row row = sh.getRow(40); if (row==null) row = sh.createRow(40); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style47);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(41); if (row==null) row = sh.createRow(41); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(42); if (row==null) row = sh.createRow(42); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(43); if (row==null) row = sh.createRow(43); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(44); if (row==null) row = sh.createRow(44); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style62);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(45); if (row==null) row = sh.createRow(45); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style64);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(1);
            cell.setCellValue("OBSERVACIONES");
            cell.setCellStyle(style16);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style65);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style65);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style65);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style65);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style65);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style65);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style65);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style65);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style65);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style65);
        }
        { Row row = sh.getRow(46); if (row==null) row = sh.createRow(46); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style66);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style67);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style68);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style68);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style68);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style68);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style68);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style68);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style68);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style68);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style68);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style68);
        }
        { Row row = sh.getRow(47); if (row==null) row = sh.createRow(47); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style69);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(48); if (row==null) row = sh.createRow(48); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(49); if (row==null) row = sh.createRow(49); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(50); if (row==null) row = sh.createRow(50); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style62);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style63);
        }
        { Row row = sh.getRow(51); if (row==null) row = sh.createRow(51); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style64);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style70b);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style37);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style70);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style37);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(8);
            cell.setCellValue("FUNCIONARIO");
            cell.setCellStyle(style34);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style35);
        }
        { Row row = sh.getRow(52); if (row==null) row = sh.createRow(52); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style37);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style71);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style49);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(53); if (row==null) row = sh.createRow(53); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style71);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style49);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(54); if (row==null) row = sh.createRow(54); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style71);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style49);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(55); if (row==null) row = sh.createRow(55); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(1);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style71);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style49);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(8);
            // empty or unknown
            cell.setCellStyle(style60);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style7);
        }
        { Row row = sh.getRow(56); if (row==null) row = sh.createRow(56); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style61);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(1);
            cell.setCellValue("FIRMA");
            cell.setCellStyle(style72);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style73);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style73);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style73);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style74);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style71);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style49);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(8);
            cell.setCellValue("FIRMA");
            cell.setCellStyle(style31);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style17);
        }
        { Row row = sh.getRow(57); if (row==null) row = sh.createRow(57); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style18);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(1);
            cell.setCellValue("NOMBRE");
            cell.setCellStyle(style75);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style76);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style77);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style78);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style79);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style71);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style49);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(8);
            cell.setCellValue("NOMBRE");
            cell.setCellStyle(style80);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style81);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style82);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style83);
        }
        { Row row = sh.getRow(58); if (row==null) row = sh.createRow(58); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style84);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(1);
            cell.setCellValue("CEDULA");
            cell.setCellStyle(style85);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(2);
            // empty or unknown
            cell.setCellStyle(style86);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(3);
            // empty or unknown
            cell.setCellStyle(style87);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(4);
            // empty or unknown
            cell.setCellStyle(style88);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(5);
            // empty or unknown
            cell.setCellStyle(style89);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(6);
            // empty or unknown
            cell.setCellStyle(style90);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(7);
            // empty or unknown
            cell.setCellStyle(style15);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(8);
            cell.setCellValue("CEDULA");
            cell.setCellStyle(style80);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(9);
            // empty or unknown
            cell.setCellStyle(style81);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(10);
            // empty or unknown
            cell.setCellStyle(style82);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(11);
            // empty or unknown
            cell.setCellStyle(style83);
        }
        { Row row = sh.getRow(59); if (row==null) row = sh.createRow(59); Cell cell = row.createCell(12);
            // empty or unknown
            cell.setCellStyle(style84);
        }

        // Escribir aplicaciones esenciales con helper (minimiza tamaño del método main)
        try {
            AppsWriter.writeEssentialApps(sh, sysInfo, style35);
        } catch (Throwable _e) { /* no bloquear el guardado del archivo */ }

        // Forzar la escritura de la fecha justo antes de guardar el archivo
        Row fechaRow = sh.getRow(6);
        if (fechaRow == null) fechaRow = sh.createRow(6);
        Cell cellDia = fechaRow.getCell(10);
        if (cellDia == null) cellDia = fechaRow.createCell(10);
        cellDia.setCellValue(dia);
        cellDia.setCellStyle(style32);
        Cell cellMes = fechaRow.getCell(11);
        if (cellMes == null) cellMes = fechaRow.createCell(11);
        cellMes.setCellValue(mes);
        cellMes.setCellStyle(style33);
        Cell cellAnio = fechaRow.getCell(12);
        if (cellAnio == null) cellAnio = fechaRow.createCell(12);
        cellAnio.setCellValue(anio);
        cellAnio.setCellStyle(style32);
            // No modificar otras celdas dentro de regiones combinadas; sólo asegurar
            // la existencia de la celda superior-izquierda en cualquier región que
            // intersecte las columnas de fecha.
            int[] cols = new int[]{10,11,12};
            for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                org.apache.poi.ss.util.CellRangeAddress cra = sh.getMergedRegion(i);
                boolean intersects = false;
                for (int c : cols) {
                    if (c >= cra.getFirstColumn() && c <= cra.getLastColumn()) { intersects = true; break; }
                }
                if (!intersects) continue;
                int fr = cra.getFirstRow();
                int fc = cra.getFirstColumn();
                Row row = sh.getRow(fr);
                if (row == null) row = sh.createRow(fr);
                Cell top = row.getCell(fc);
                if (top == null) top = row.createCell(fc);
            }

            // Escribir texto fijo en B8 (fila índice 7, columna 1)
            Row b8row = sh.getRow(7);
            if (b8row == null) b8row = sh.createRow(7);
            Cell b8 = b8row.getCell(1);
            if (b8 == null) b8 = b8row.createCell(1);
            b8.setCellValue("TICKET N*");
            try { b8.setCellStyle(style28); } catch (Exception e) { }
            // Asegurar que la región combinada que incluye E8 (col 4) tenga los mismos bordes
            try {
                int rIdx = 7; int cIdx = 4;
                for (int i = 0; i < sh.getNumMergedRegions(); i++) {
                    CellRangeAddress cra = sh.getMergedRegion(i);
                    if (cra.isInRange(rIdx, cIdx)) {
                        for (int r = cra.getFirstRow(); r <= cra.getLastRow(); r++) {
                            Row rr = sh.getRow(r); if (rr == null) rr = sh.createRow(r);
                            for (int c = cra.getFirstColumn(); c <= cra.getLastColumn(); c++) {
                                Cell cc = rr.getCell(c); if (cc == null) cc = rr.createCell(c);
                                try { cc.setCellStyle(style28); } catch (Exception _e) {}
                            }
                        }
                        break;
                    }
                }
            } catch (Exception _ignore) {}
        // ═══════════════════════════════════════════════════════════════════════════
        // ESCRIBIR DATOS DEL FORMULARIO EN LAS CELDAS CORRESPONDIENTES
        // ═══════════════════════════════════════════════════════════════════════════
        
        // DATOS DEL USUARIO (filas 9-14, columna D = columna 3)
        ExcelCeldaHelper.escribirCelda(sh, "D10", FORM_CIUDAD);       // Ciudad
        ExcelCeldaHelper.escribirCelda(sh, "D11", FORM_DIRECCION);    // Dirección
        ExcelCeldaHelper.escribirCelda(sh, "D12", FORM_NOMBRE);       // Nombre
        ExcelCeldaHelper.escribirCelda(sh, "D13", FORM_CORREO);       // Correo
        ExcelCeldaHelper.escribirCelda(sh, "D14", FORM_TECNICO);      // Técnico
        ExcelCeldaHelper.escribirCelda(sh, "D15", FORM_SEDE);         // Sede/Oficina
        
        // TICKET
        ExcelCeldaHelper.escribirCelda(sh, "E8", FORM_TICKET);        // Ticket
        
        // DESCRIPCIÓN DEL HARDWARE (fila 19, columnas H, J, K)
        ExcelCeldaHelper.escribirCelda(sh, "H20", FORM_SERIAL);       // Serial
        ExcelCeldaHelper.escribirCelda(sh, "J20", FORM_PLACA);        // Placa
        ExcelCeldaHelper.escribirCelda(sh, "K20", FORM_CONDICIONES);  // Condiciones HW
        
        // TIPO, MARCA, MODELO
        ExcelCeldaHelper.escribirCelda(sh, "D20", SYS_TIPO);          // Tipo
        ExcelCeldaHelper.escribirCelda(sh, "E20", SYS_MARCA);         // Marca
        ExcelCeldaHelper.escribirCelda(sh, "F20", SYS_MODELO);        // Modelo
        
        // ═══════════════════════════════════════════════════════════════════════════
        // DATOS DEL PC (Fila 26)
        // ═══════════════════════════════════════════════════════════════════════════
        ExcelCeldaHelper.escribirCelda(sh, "B26", FORM_PC_ENCIENDE);       // Enciende? SI/NO
        ExcelCeldaHelper.escribirCelda(sh, "D26", FORM_DISCO_DURO);        // Disco Duro
        ExcelCeldaHelper.escribirCelda(sh, "F26", FORM_CDDVD);             // CD/DVD
        ExcelCeldaHelper.escribirCelda(sh, "H26", FORM_BOTONES_PC);        // Botones
        ExcelCeldaHelper.escribirCelda(sh, "K26", FORM_CONDICIONES_PC);    // Condiciones Físicas PC
        
        // ═══════════════════════════════════════════════════════════════════════════
        // DATOS DEL MONITOR (Fila 31)
        // ═══════════════════════════════════════════════════════════════════════════
        ExcelCeldaHelper.escribirCelda(sh, "B31", FORM_MONITOR_ENCIENDE);     // Enciende? SI/NO
        ExcelCeldaHelper.escribirCelda(sh, "D31", FORM_PANTALLA);             // Pantalla
        ExcelCeldaHelper.escribirCelda(sh, "F31", FORM_ONLY_ONE);             // Only One
        ExcelCeldaHelper.escribirCelda(sh, "H31", FORM_BOTONES_MONITOR);      // Botones Completos
        ExcelCeldaHelper.escribirCelda(sh, "K31", FORM_CONDICIONES_MONITOR);  // Condiciones Físicas Monitor
        
        // ═══════════════════════════════════════════════════════════════════════════
        // DATOS DEL TECLADO (Fila 35)
        // ═══════════════════════════════════════════════════════════════════════════
        ExcelCeldaHelper.escribirCelda(sh, "B35", FORM_TECLADO_ENCIENDE);     // Enciende? SI/NO
        ExcelCeldaHelper.escribirCelda(sh, "D35", FORM_TECLADO_FUNCIONA);     // Funciona Correctamente?
        ExcelCeldaHelper.escribirCelda(sh, "H35", FORM_BOTONES_TECLADO);      // Botones Completos
        ExcelCeldaHelper.escribirCelda(sh, "K35", FORM_CONDICIONES_TECLADO);  // Condiciones Físicas Teclado
        
        // PROCEDIMIENTO REALIZADO (B42 a M46 - con ajuste de texto)
        ExcelCeldaHelper.escribirCeldaConAjuste(sh, "B42", FORM_TRABAJO_REALIZADO);
        
        // OBSERVACIONES (B48 a M52 - con ajuste de texto)
        ExcelCeldaHelper.escribirCeldaConAjuste(sh, "B48", FORM_OBSERVACIONES);
        
        // FIRMAS
        ExcelCeldaHelper.escribirCelda(sh, "D59", datosForm.firmaTecnico);      // Nombre Técnico
        ExcelCeldaHelper.escribirCelda(sh, "D60", datosForm.cedulaTecnico);     // Cédula Técnico
        ExcelCeldaHelper.escribirCelda(sh, "K59", datosForm.firmaFuncionario);  // Nombre Funcionario
        ExcelCeldaHelper.escribirCelda(sh, "K60", datosForm.cedulaFuncionario); // Cédula Funcionario
        
        System.out.println("=== DATOS ESCRITOS EN EXCEL (GENERADO) ===");
        System.out.println("Ciudad: " + FORM_CIUDAD);
        System.out.println("Nombre: " + FORM_NOMBRE);
        System.out.println("Procedimiento: " + FORM_TRABAJO_REALIZADO);
        System.out.println("Observaciones: " + FORM_OBSERVACIONES);
        System.out.println("==========================================");
        
        // ═══════════════════════════════════════════════════════════════════════════
        // INSERTAR IMAGEN DEL LOGO SELCOMP
        // ═══════════════════════════════════════════════════════════════════════════
        try {
            // Cargar imagen desde resources
            InputStream imgStream = GeneratedTemplate.class.getResourceAsStream("/images/Selcomp3.png");
            if (imgStream != null) {
                byte[] bytes = IOUtils.toByteArray(imgStream);
                int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                imgStream.close();
                
                // Crear el área de dibujo
                Drawing<?> drawing = sh.createDrawingPatriarch();
                
                // Crear anchor solo para posicionar (NO redimensionar)
                // La imagen se coloca en K2 y mantiene sus proporciones originales
                ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
                anchor.setCol1(10);  // Columna K (índice 10)
                anchor.setRow1(1);   // Fila 2 (índice 1)
                anchor.setDx1(Units.EMU_PER_PIXEL * 10);  // Pequeño offset para centrar
                anchor.setDy1(Units.EMU_PER_PIXEL * 15);  // Offset vertical para centrar
                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
                
                // Insertar imagen y escalar manteniendo proporciones originales
                Picture picture = drawing.createPicture(anchor, pictureIdx);
                // resize(scale) mantiene la proporción original de la imagen
                // 0.22 = 22% del tamaño original (más pequeña para caber en K2:M4)
                picture.resize(0.22);
                
                System.out.println("✓ Imagen Selcomp.png insertada correctamente en K2:M4");
            } else {
                System.err.println("⚠ No se encontró la imagen Selcomp.png en resources/images/");
            }
        } catch (Exception imgEx) {
            System.err.println("Error al insertar imagen: " + imgEx.getMessage());
        }

        // Usar ruta del formulario si existe, sino nombre por defecto (rutaDestino ya definida al inicio)
        String nombreArchivo = !rutaDestino.isEmpty() ? rutaDestino : "FORMATO_RECREADO_FECHA_AUTOMATICA.xlsx";
        boolean escrito = false;
        try {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(nombreArchivo)) { wb.write(fos); }
            escrito = true;
        } catch (java.io.FileNotFoundException fnf) {
            // Archivo posiblemente abierto por otra aplicación; intentar con nombre alternativo
            String alt = !rutaDestino.isEmpty() ? rutaDestino.replace(".xlsx", "_1.xlsx") : "FORMATO_RECREADO_FECHA_AUTOMATICA_1.xlsx";
            try {
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(alt)) { wb.write(fos); }
                nombreArchivo = alt;
                escrito = true;
            } catch (Throwable t2) {
                System.err.println("No se pudo escribir el archivo (archivo abierto?): " + t2.getMessage());
            }
        } catch (Throwable t) {
            System.err.println("Error al escribir el archivo: " + t.getMessage());
        }

        if (escrito) {
            System.out.println("Fecha escrita en K7, L7, M7: " + dia + "/" + mes + "/" + anio + " en archivo: " + nombreArchivo);
        } else {
            System.err.println("No se escribió ningún archivo. Cierra 'FORMATO_RECREADO_FECHA_AUTOMATICA.xlsx' si está abierto y vuelve a intentar.");
        }
        try { wb.close(); } catch (Exception _e) {}
    }
}