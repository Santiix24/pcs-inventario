package inventario.fx.model;
import inventario.fx.config.PortablePaths;
import inventario.fx.core.SimuladorEntorno;

import org.apache.poi.poifs.crypt.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import inventario.fx.util.AppLogger;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Security;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Clase base abstracta que contiene toda la lógica de negocio del inventario.
 * Esta clase no tiene dependencias de UI (ni Swing ni JavaFX),
 * permitiendo ser reutilizada por cualquier implementación de interfaz gráfica.
 */
public abstract class InventarioFXBase {
    
    protected static final AppLogger logger = AppLogger.getLogger(InventarioFXBase.class);

    // === CONSTANTES ===
    protected static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    protected static final DateTimeFormatter FORMATO_FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    protected static final String[] ENCABEZADOS_SISTEMA = {
            "Fecha", "Usuario Sistema", "Hostname", "Sistema Operativo",
            "Modelo", "Marca", "Tipo Dispositivo",
            "BIOS", "CPU", "GPU", "RAM", "Memoria Instalada", "Tarjeta Red", "Discos", "Nº Discos", "IP"
    };
    
    protected static final String[] ENCABEZADOS_APLICACIONES = {
            "ID Grupo", "Nº", "Fecha", "Usuario Sistema", "Hostname", "IP", "Aplicación", "Versión", "Fabricante", "Fecha Instalación"
    };

    protected static final int MAX_COLUMNAS = 16383;
    protected static final String RUTA_IMAGENES = "/images/";
    protected static final String LOGO = "Selcomp.png";
    protected static final String LOGO1 = "Selcomp1.png";
    protected static final String ICONO = "Selcomp_logito.png";
    protected static final String LOGO_Excel = "Ecxel.png";
    protected static final String LOGO_PDF = "Pdf.png";
    protected static final String LOGO_CALENDARIO = "calendario.png";
    protected static final String LOGO_REGRESO = "regreso.png";

    // Contraseña de cifrado ahora es dinámica
    protected static String getExcelPassword() {
        return AdminManager.getExcelPassword();
    }
    
    // Se mantiene por compatibilidad pero usar getExcelPassword()
    // SEGURIDAD: No exponer contraseñas en constantes del código fuente
    @Deprecated
    protected static String getPassword() { return getExcelPassword(); }

    // === PROYECTOS ===
    protected static final String[] PROYECTOS_NOMBRE = {
            "1. Secretaría de Educación - Mesa de servicios",
            "2. Computadores para educar - Comercialización de equipos",
            "3. Ministerio de Salud - Outsourcing de TI",
            "4. Caja de retiro de las fuerzas militares - Asesorías",
            "5. CAR - Herramienta de gestión de TI",
            "6. Consejo superior de la Judicatura - Mesa de servicios",
            "7. Secretaría de educación - Administración de infraestructura",
            "8. Fondo nacional del ahorro - Comercialización de equipos"
    };

    public static String CURRENT_PROJECT = PROYECTOS_NOMBRE[0];

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // === MÉTODOS PARA OBTENER COLOR POR PROYECTO ===
    
    protected static short obtenerColorProyecto() {
        for (int i = 0; i < PROYECTOS_NOMBRE.length; i++) {
            if (PROYECTOS_NOMBRE[i].equals(CURRENT_PROJECT)) {
                switch (i) {
                    case 0: return IndexedColors.DARK_BLUE.getIndex();
                    case 1: return IndexedColors.DARK_GREEN.getIndex();
                    case 2: return IndexedColors.DARK_RED.getIndex();
                    case 3: return IndexedColors.INDIGO.getIndex();
                    case 4: return IndexedColors.DARK_TEAL.getIndex();
                    case 5: return IndexedColors.VIOLET.getIndex();
                    case 6: return IndexedColors.BLUE_GREY.getIndex();
                    case 7: return IndexedColors.BROWN.getIndex();
                    default: return IndexedColors.DARK_BLUE.getIndex();
                }
            }
        }
        return IndexedColors.DARK_BLUE.getIndex();
    }

    // === LÓGICA DE NEGOCIO ===
    
    /**
     * Sanitiza un nombre para usarlo como parte de un nombre de archivo en Windows.
     * Reemplaza espacios por guiones bajos y elimina caracteres no válidos.
     */
    public static String sanitizarNombreArchivo(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return "";
        }
        // Reemplazar espacios por guiones bajos
        String sanitizado = nombre.replace(" ", "_");
        // Eliminar caracteres no válidos en Windows: \ / : * ? " < > |
        sanitizado = sanitizado.replaceAll("[\\\\/:*?\"<>|]", "");
        // Eliminar doble extensión .xlsx.xlsx si existe
        if (sanitizado.endsWith(".xlsx")) {
            sanitizado = sanitizado.substring(0, sanitizado.length() - 5);
        }
        return sanitizado;
    }

    public static Path obtenerCarpetaEjecutable() {
        // Usar PortablePaths para obtener la carpeta persistente de proyectos
        // (no ProtectionDomain, que con launch4j apunta a un directorio temporal)
        return inventario.fx.config.PortablePaths.getProyectosDir();
    }

    public static Path obtenerRutaExcel(String proyecto) {
        // Obtener el índice del proyecto (ej: "1" de "1. Secretaría de Educación")
        String indice = proyecto.split("\\.")[0];
        // Obtener el nombre del proyecto sin el índice
        String nombreProyecto = proyecto.substring(3).trim();
        
        // Buscar el proyecto en AdminManager para obtener la descripción
        int idx = Integer.parseInt(indice) - 1;
        AdminManager.Proyecto p = AdminManager.getProyectoPorIndice(idx);
        
        // Reemplazar TODOS los espacios por guiones bajos y sanitizar caracteres inválidos para Windows
        String nombreFormateado = sanitizarNombreArchivo(nombreProyecto);
        
        String nombreArchivo;
        if (p != null && p.getDescripcion() != null && !p.getDescripcion().isEmpty()) {
            String descripcionFormateada = sanitizarNombreArchivo(p.getDescripcion());
            // Formato con descripción: "indice - Nombre_Descripcion.xlsx"
            nombreArchivo = indice + " - " + nombreFormateado + "_" + descripcionFormateada + ".xlsx";
        } else {
            // Formato sin descripción: "indice - Nombre.xlsx"
            nombreArchivo = indice + " - " + nombreFormateado + ".xlsx";
        }
        
        Path rutaDirecta = obtenerCarpetaEjecutable().resolve("Inventario_" + nombreArchivo);
        
        // Si el archivo existe con el índice actual, usarlo
        if (Files.exists(rutaDirecta)) {
            return rutaDirecta;
        }
        
        // Si no existe, buscar por nombre del proyecto (para archivos con índice diferente)
        try {
            Path carpeta = obtenerCarpetaEjecutable();
            java.util.Optional<Path> archivoEncontrado = Files.list(carpeta)
                .filter(path -> {
                    String fileName = path.getFileName().toString().toLowerCase();
                    return fileName.startsWith("inventario_") && 
                           fileName.endsWith(".xlsx") &&
                           fileName.contains(nombreFormateado.toLowerCase());
                })
                .findFirst();
            
            if (archivoEncontrado.isPresent()) {
                return archivoEncontrado.get();
            }
        } catch (Exception e) {
            System.err.println("[obtenerRutaExcel] Error buscando archivo: " + e.getMessage());
        }
        
        // Devolver la ruta directa (aunque no exista, para creación de nuevos)
        return rutaDirecta;
    }

    protected static String limpiarNombreProyecto(String proyecto) {
        return proyecto.substring(3).trim();
    }

    /** Formatea un Date con el formatter thread-safe FORMATO_FECHA */
    protected static String formatearFecha(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(FORMATO_FECHA);
    }

    /** Formatea un Date con el formatter thread-safe FORMATO_FECHA_CORTA */
    protected static String formatearFechaCorta(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(FORMATO_FECHA_CORTA);
    }

    // === RECOPILACIÓN DE INFORMACIÓN DEL SISTEMA ===

    public static InfoPC recopilarInfo() {
        return recopilarInfoConProgreso(null);
    }

    /**
     * Recopila información del sistema reportando progreso real a través del consumer.
     * @param progreso Consumer que recibe mensajes de estado (puede ser null)
     */
    public static InfoPC recopilarInfoConProgreso(java.util.function.Consumer<String> progreso) {
        // Si hay simulación activa, devolver datos simulados
        if (SimuladorEntorno.estaActiva()) {
            System.out.println("🔄 [SIMULACIÓN] Usando datos de: " + SimuladorEntorno.getNombrePerfilActual());
            if (progreso != null) progreso.accept("Cargando perfil simulado...");
            return SimuladorEntorno.obtenerInfoSimulada();
        }
        
        if (progreso != null) progreso.accept("Inicializando hardware info...");
        // Modo real: obtener info del sistema actual
        SystemInfo si = new SystemInfo();
        InfoPC i = new InfoPC();
        // Generar ID único para este grupo de aplicaciones
        i.idGrupo = generarIdGrupo();
        i.fecha = new Date();

        if (progreso != null) progreso.accept("Detectando sistema operativo...");
        OperatingSystem os = si.getOperatingSystem();
        
        String sistemaCompleto = null;
        try {
            sistemaCompleto = getWmiOperatingSystemCaption();
        } catch (Exception ignored) { logger.warn("Error obteniendo nombre SO: " + ignored.getMessage()); }
        
        if (sistemaCompleto != null && !sistemaCompleto.isEmpty() && !sistemaCompleto.equals("Desconocido")) {
            i.sistema = sistemaCompleto.trim();
        } else {
            i.sistema = String.format("%s %s (%s)", os.getFamily(), os.getVersionInfo().getVersion(), os.getManufacturer());
        }
        
        i.hostname = os.getNetworkParams().getHostName();

        if (progreso != null) progreso.accept("Analizando procesador (CPU)...");
        CentralProcessor cpu = si.getHardware().getProcessor();
        i.cpu = String.format("%s (%d núcleos, %.1f GHz)", 
                cpu.getProcessorIdentifier().getName().trim(), 
                cpu.getLogicalProcessorCount(), 
                cpu.getMaxFreq() / 1_000_000_000.0);
        
        if (progreso != null) progreso.accept("Detectando tarjeta gráfica (GPU)...");
        List<GraphicsCard> gpus = si.getHardware().getGraphicsCards();
        if (!gpus.isEmpty()) {
            GraphicsCard g = gpus.get(0);
            i.gpu = String.format("%s (%s VRAM)", g.getName().trim(), formatBytes(g.getVRam()));
        } else {
            i.gpu = "Integrada / No detectada";
        }
        
        if (progreso != null) progreso.accept("Analizando memoria RAM...");
        GlobalMemory mem = si.getHardware().getMemory();
        i.ram = String.format("%s usada de %s total", formatBytes(mem.getTotal() - mem.getAvailable()), formatBytes(mem.getTotal()));
        
        // Información detallada de memoria RAM
        List<PhysicalMemory> memModules = mem.getPhysicalMemory();
        if (!memModules.isEmpty()) {
            StringBuilder ramInfo = new StringBuilder();
            long totalInstalada = 0;
            String tipoMem = "Desconocido";
            
            for (int m = 0; m < memModules.size(); m++) {
                PhysicalMemory pm = memModules.get(m);
                totalInstalada += pm.getCapacity();
                if (m == 0) {
                    tipoMem = pm.getMemoryType();
                }
                ramInfo.append(String.format("Módulo %d: %s (%s, %d MHz)",
                    m + 1,
                    formatBytes(pm.getCapacity()),
                    pm.getMemoryType(),
                    pm.getClockSpeed() / 1_000_000));
                if (m < memModules.size() - 1) ramInfo.append(" | ");
            }
            i.memoriaInstalada = ramInfo.toString();
        } else {
            i.memoriaInstalada = formatBytes(mem.getTotal()) + " (detalles no disponibles)";
        }
        
        if (progreso != null) progreso.accept("Leyendo información del BIOS...");
        Firmware fw = si.getHardware().getComputerSystem().getFirmware();
        String biosManufacturer = fw.getManufacturer() != null && !fw.getManufacturer().isEmpty() ? fw.getManufacturer() : "Desconocido";
        String biosVersion = fw.getVersion() != null && !fw.getVersion().isEmpty() ? fw.getVersion() : "Desconocida";
        String biosDate = fw.getReleaseDate() != null && !fw.getReleaseDate().isEmpty() ? fw.getReleaseDate() : "Desconocida";
        i.bios = String.format("%s | Versión: %s | Fecha: %s", biosManufacturer, biosVersion, biosDate);
        
        if (progreso != null) progreso.accept("Escaneando interfaces de red...");
        // Información de tarjeta de red
        List<NetworkIF> networkIFs = si.getHardware().getNetworkIFs();
        StringBuilder netInfo = new StringBuilder();
        int activeNets = 0;
        for (NetworkIF net : networkIFs) {
            if (net.getSpeed() > 0 && net.getIPv4addr().length > 0) {
                if (activeNets > 0) netInfo.append(" | ");
                netInfo.append(String.format("%s (%s, %s Mbps)",
                    net.getName(),
                    net.getIPv4addr().length > 0 ? net.getIPv4addr()[0] : "Sin IP",
                    net.getSpeed() / 1_000_000));
                activeNets++;
            }
        }
        i.tarjetaRed = activeNets > 0 ? netInfo.toString() : "No detectada";
        
        if (progreso != null) progreso.accept("Analizando discos de almacenamiento...");
        List<HWDiskStore> discos = si.getHardware().getDiskStores();
        i.numeroDiscos = discos.size();
        StringBuilder sb = new StringBuilder();
        for (int d = 0; d < discos.size(); d++) {
            HWDiskStore ds = discos.get(d);
            String tipo = ds.getModel().toLowerCase().contains("ssd") ? "SSD" : "HDD";
            sb.append(String.format("Disco %d (%s): %s - %s", d + 1, tipo, ds.getModel().trim(), formatBytes(ds.getSize())));
            if (d < discos.size() - 1) sb.append(" | ");
        }
        i.discos = sb.toString();

        if (progreso != null) progreso.accept("Obteniendo dirección IP...");
        i.ip = getActiveIPAddress();

        if (progreso != null) progreso.accept("Recopilando aplicaciones instaladas...");
        i.installedApps = getInstalledApps(os);

        if (progreso != null) progreso.accept("Detectando modelo del equipo...");
        i.userName = System.getProperty("user.name");
        
        String fabricanteTmp = si.getHardware().getComputerSystem().getManufacturer();
        String modeloTmp = si.getHardware().getComputerSystem().getModel();
        if (modeloTmp == null) modeloTmp = "";
        if (fabricanteTmp == null) fabricanteTmp = "";
        modeloTmp = modeloTmp.trim();
        fabricanteTmp = fabricanteTmp.trim();
        
        if (!modeloTmp.isEmpty()) {
            i.modeloEquipo = modeloTmp;
        } else if (!fabricanteTmp.isEmpty()) {
            i.modeloEquipo = fabricanteTmp;
        } else {
            i.modeloEquipo = "";
        }
        
        try {
            i.manufacturer = getWmiManufacturer();
        } catch (Exception ignored) {
            i.manufacturer = null;
        }
        try {
            i.deviceType = getChassisType();
        } catch (Exception ignored) {
            i.deviceType = null;
        }
        return i;
    }

    protected static String getWmiOperatingSystemCaption() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (!osName.contains("windows")) return "N/A";

            String cmd = "$ErrorActionPreference='SilentlyContinue'; (Get-WmiObject -Class Win32_OperatingSystem | Select-Object -ExpandProperty Caption) -join ','";
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-NoProfile", "-Command", cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) return line;
                }
            }
            p.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            // ignore
        }
        return "Desconocido";
    }

    protected static String getWmiManufacturer() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (!osName.contains("windows")) return "N/A";

            String cmd = "$ErrorActionPreference='SilentlyContinue'; (Get-WmiObject -Class Win32_ComputerSystem | Select-Object -ExpandProperty Manufacturer) -join ','";
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-NoProfile", "-Command", cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) return line;
                }
            }
            p.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            // ignore
        }
        return "Desconocido";
    }

    protected static String getChassisType() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (!osName.contains("windows")) return "N/A";

            String cmd = "$ErrorActionPreference='SilentlyContinue'; (Get-WmiObject -Class Win32_SystemEnclosure | ForEach-Object {$_.ChassisTypes}) -join ','";
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-NoProfile", "-Command", cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split("[,;\\s]+");
                    for (String part : parts) {
                        try {
                            int v = Integer.parseInt(part.trim());
                            if (v == 8 || v == 9 || v == 10 || v == 14) return "Notebook";
                            if (v == 3 || v == 4 || v == 5 || v == 6 || v == 7 || v == 15 || v == 16) return "Desktop";
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            p.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            // ignore
        }
        return "Desconocido";
    }

    // === GUARDAR EN EXCEL CIFRADO ===

    protected static boolean guardarEnExcelCifradoProyecto(InfoPC i) {
        Path rutaExcel = obtenerRutaExcel(CURRENT_PROJECT);
        Path temp = null;
        XSSFWorkbook wb = null;
        try {
            temp = Files.createTempFile(obtenerCarpetaEjecutable(), "inv_", ".xlsx");

            if (Files.exists(rutaExcel)) {
                wb = abrirCifradoProyecto(rutaExcel);
                if (wb == null) wb = new XSSFWorkbook();
            } else {
                wb = new XSSFWorkbook();
            }

            Sheet hoja = wb.getSheet("SystemInfo");
            if (hoja == null) {
                hoja = wb.createSheet("SystemInfo");
                crearEncabezadosConProyecto(wb, hoja);
            }

            int nuevaFila = hoja.getLastRowNum() + 1;
            Row fila = hoja.createRow(nuevaFila);
            fila.setHeight((short) 900);

            Map<String, Integer> columnas = new HashMap<>();
            Row header = hoja.getRow(1);
            if (header == null) {
                header = hoja.createRow(1);
                crearEncabezadosConProyecto(wb, hoja);
            }
            for (int c = 0; c < header.getLastCellNum(); c++) {
                Cell cell = header.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                columnas.put(cell.getStringCellValue().trim(), c);
            }

            CellStyle estilo = wb.createCellStyle();
            estilo.setWrapText(true);
            estilo.setVerticalAlignment(VerticalAlignment.TOP);

            setCell(fila, columnas.get("Fecha"), formatearFecha(i.fecha), estilo);
            setCell(fila, columnas.get("Usuario Sistema"), i.userName, estilo);
            setCell(fila, columnas.get("Hostname"), i.hostname, estilo);
            setCell(fila, columnas.get("Sistema Operativo"), i.sistema, estilo);
            setCell(fila, columnas.get("Modelo"), i.modeloEquipo, estilo);
            setCell(fila, columnas.get("Marca"), i.manufacturer != null ? i.manufacturer : "", estilo);
            setCell(fila, columnas.get("Tipo Dispositivo"), i.deviceType != null ? i.deviceType : "", estilo);
            setCell(fila, columnas.get("BIOS"), i.bios, estilo);
            setCell(fila, columnas.get("CPU"), i.cpu, estilo);
            setCell(fila, columnas.get("GPU"), i.gpu, estilo);
            setCell(fila, columnas.get("RAM"), i.ram, estilo);
            setCell(fila, columnas.get("Memoria Instalada"), i.memoriaInstalada != null ? i.memoriaInstalada : "", estilo);
            setCell(fila, columnas.get("Tarjeta Red"), i.tarjetaRed != null ? i.tarjetaRed : "", estilo);
            setCell(fila, columnas.get("Discos"), i.discos, estilo);
            setCell(fila, columnas.get("Nº Discos"), String.valueOf(i.numeroDiscos), estilo);
            setCell(fila, columnas.get("IP"), i.ip, estilo);

            autoSize(hoja, ENCABEZADOS_SISTEMA.length);
            hoja.setAutoFilter(new CellRangeAddress(1, 1, 0, ENCABEZADOS_SISTEMA.length - 1));
            hoja.createFreezePane(0, 2);

            createAppsSheet(wb, i.userName, i);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                wb.write(bos);
            } finally {
                wb.close();
            }

            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor encryptor = info.getEncryptor();
            encryptor.confirmPassword(getExcelPassword());

            POIFSFileSystem fs = new POIFSFileSystem();
            OutputStream os = encryptor.getDataStream(fs);
            os.write(bos.toByteArray());
            os.close();

            try (FileOutputStream fos = new FileOutputStream(temp.toFile())) {
                fs.writeFilesystem(fos);
            }
            fs.close();

            return reemplazarArchivoSinBackup(rutaExcel, temp);

        } catch (Exception e) {
            logger.error("Error: " + e.getMessage(), e);
            return false;
        } finally {
            if (temp != null && Files.exists(temp)) {
                try { Files.delete(temp); } catch (Exception ignored) {}
            }
            if (wb != null) try { wb.close(); } catch (Exception ignored) {}
        }
    }

    // === MÉTODOS AUXILIARES ===

    protected static void setCell(Row row, Integer colIndex, String value, CellStyle style) {
        if (row == null || colIndex == null || colIndex < 0) return;
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value != null ? value : "");
        if (style != null) cell.setCellStyle(style);
    }

    protected static void autoSize(Sheet sheet, int columnas) {
        if (sheet == null || columnas <= 0) return;
        final int MAX_WIDTH = 10000;
        for (int c = 0; c < columnas; c++) {
            try {
                sheet.autoSizeColumn(c);
                int w = sheet.getColumnWidth(c);
                if (w > MAX_WIDTH) sheet.setColumnWidth(c, MAX_WIDTH);
            } catch (Exception ignored) {}
        }
    }

    protected static void crearEncabezadosConProyecto(XSSFWorkbook wb, Sheet hoja) {
        Row titulo = hoja.createRow(0);
        titulo.setHeight((short) 1200);
        Cell cellTitulo = titulo.createCell(0);
        cellTitulo.setCellValue("INVENTARIO DE EQUIPOS - " + limpiarNombreProyecto(CURRENT_PROJECT).toUpperCase());

        CellStyle estiloTitulo = wb.createCellStyle();
        XSSFFont fontTitulo = wb.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 16);
        fontTitulo.setColor(IndexedColors.WHITE.getIndex());
        estiloTitulo.setFont(fontTitulo);
        estiloTitulo.setFillForegroundColor(obtenerColorProyecto());
        estiloTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloTitulo.setAlignment(HorizontalAlignment.CENTER);
        estiloTitulo.setVerticalAlignment(VerticalAlignment.CENTER);
        cellTitulo.setCellStyle(estiloTitulo);

        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, ENCABEZADOS_SISTEMA.length - 1));

        Row header = hoja.createRow(1);
        header.setHeight((short) 600);
        CellStyle estiloHeader = wb.createCellStyle();
        estiloHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloHeader.setAlignment(HorizontalAlignment.CENTER);
        estiloHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBold(true);
        estiloHeader.setFont(fontHeader);

        for (int i = 0; i < ENCABEZADOS_SISTEMA.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(ENCABEZADOS_SISTEMA[i]);
            c.setCellStyle(estiloHeader);
        }
    }

    public static XSSFWorkbook abrirCifradoProyecto(Path ruta) {
        return abrirCifrado(ruta, getExcelPassword());
    }
    
    /**
     * Abre un archivo Excel cifrado usando la contraseña proporcionada
     * IMPORTANTE: NO cerrar el POIFSFileSystem antes de que el workbook se use completamente
     */
    private static XSSFWorkbook abrirCifrado(Path ruta, String password) {
        if (!Files.exists(ruta)) return null;
        try {
            // Leer el archivo completo en memoria primero
            byte[] fileBytes = Files.readAllBytes(ruta);
            
            // Abrir desde memoria para evitar problemas de locks
            try (POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(fileBytes))) {
                EncryptionInfo info = new EncryptionInfo(fs);
                Decryptor d = info.getDecryptor();
                if (!d.verifyPassword(password)) {
                    return null;
                }
                
                // Leer el stream de datos desencriptado completamente en memoria
                try (InputStream dataStream = d.getDataStream(fs)) {
                    // Copiar todo a un ByteArrayOutputStream para tener una copia en memoria
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = dataStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    
                    // Crear el workbook desde la copia en memoria
                    // Esto asegura que el workbook es completamente independiente del archivo
                    return new XSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()));
                }
            }
        } catch (Exception e) {
            logger.error("[InventarioFXBase] Error abriendo archivo cifrado: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Guarda un workbook cifrado con contraseña en la ruta especificada
     * El archivo resultante se marca como oculto para mayor seguridad
     */
    protected static void guardarCifradoProyecto(Path ruta, XSSFWorkbook workbook) throws Exception {
        System.out.println("[InventarioFXBase] Iniciando guardado de: " + ruta.getFileName());
        
        // Verificar el contenido del workbook ANTES de cualquier operación
        System.out.println("[InventarioFXBase] === CONTENIDO DEL WORKBOOK ANTES DE GUARDAR ===");
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            int rowCount = 0;
            for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row != null) rowCount++;
            }
            System.out.println("[InventarioFXBase] Hoja '" + sheet.getSheetName() + "' - getLastRowNum=" + sheet.getLastRowNum() + ", filas reales=" + rowCount);
        }
        
        // Forzar recalcular todas las fórmulas y referencias
        workbook.setForceFormulaRecalculation(true);
        
        // Guardar en archivo temporal primero
        Path tempFile = Files.createTempFile("inventario_temp", ".xlsx");
        Path tempEncrypted = Files.createTempFile("inventario_encrypted", ".xlsx");
        
        try {
            // Escribir el workbook al archivo temporal
            try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
                workbook.write(fos);
                fos.flush();
                fos.getFD().sync(); // Forzar escritura a disco
            }
            
            System.out.println("[InventarioFXBase] Workbook escrito en temporal: " + tempFile);
            
            // Cifrar el archivo temporal
            POIFSFileSystem fs = new POIFSFileSystem();
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor enc = info.getEncryptor();
            enc.confirmPassword(getExcelPassword());
            
            try (InputStream is = Files.newInputStream(tempFile);
                 OutputStream os = enc.getDataStream(fs)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
            
            System.out.println("[InventarioFXBase] Archivo cifrado");
            
            // Guardar el archivo cifrado en temporal
            try (FileOutputStream fos = new FileOutputStream(tempEncrypted.toFile())) {
                fs.writeFilesystem(fos);
                fos.flush();
                fos.getFD().sync(); // Forzar escritura a disco
            }
            
            fs.close();
            
            System.out.println("[InventarioFXBase] Archivo cifrado guardado en: " + tempEncrypted);
            
            // Esperar un momento para asegurar que todo está escrito
            Thread.sleep(200);
            
            // ESTRATEGIA: Eliminar el archivo original primero, luego mover (no copiar)
            long tamañoOriginal = Files.exists(ruta) ? Files.size(ruta) : 0;
            System.out.println("[InventarioFXBase] Tamaño del archivo ORIGINAL: " + tamañoOriginal + " bytes");
            
            // Eliminar el archivo original si existe
            if (Files.exists(ruta)) {
                boolean eliminado = Files.deleteIfExists(ruta);
                System.out.println("[InventarioFXBase] Archivo original eliminado: " + eliminado);
                Thread.sleep(200); // Esperar a que el sistema operativo libere completamente el archivo
            }
            
            // Verificar tamaño del archivo temporal cifrado
            long tamañoTemp = Files.size(tempEncrypted);
            System.out.println("[InventarioFXBase] Tamaño del archivo temporal cifrado: " + tamañoTemp + " bytes");
            
            // Mover (no copiar) el archivo temporal cifrado a la ubicación final
            Files.move(tempEncrypted, ruta, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            System.out.println("[InventarioFXBase] ✓ Archivo movido de temporal a ubicación final");
            
            // Esperar y verificar tamaño final
            Thread.sleep(300); // Esperar más tiempo para asegurar que el sistema de archivos se sincroniza
            long tamañoFinal = Files.size(ruta);
            System.out.println("[InventarioFXBase] Tamaño del archivo FINAL: " + tamañoFinal + " bytes");
            
            // 🔒 SEGURIDAD: Ocultar y proteger el archivo en el sistema de archivos
            PortablePaths.protegerArchivo(ruta);
            
            System.out.println("[InventarioFXBase] ✓ Archivo guardado y cifrado: " + ruta.getFileName());
            
        } finally {
            // Eliminar archivos temporales
            try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
            try { Files.deleteIfExists(tempEncrypted); } catch (Exception ignored) {}
        }
    }
    
    /**
     * Oculta un archivo en el sistema operativo para mayor seguridad.
     * En Windows usa el atributo 'hidden', en Linux/Mac usa el prefijo '.'
     */
    private static void ocultarArchivo(Path archivo) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows: Establecer atributo oculto
                Files.setAttribute(archivo, "dos:hidden", true);
                logger.info("🔒 Archivo oculto en Windows: " + archivo.getFileName());
                
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Linux/Mac: Renombrar con prefijo '.' si no lo tiene
                String nombre = archivo.getFileName().toString();
                if (!nombre.startsWith(".")) {
                    Path directorioParent = archivo.getParent();
                    Path archivoOculto = directorioParent.resolve("." + nombre);
                    Files.move(archivo, archivoOculto, StandardCopyOption.REPLACE_EXISTING);
                    logger.info("🔒 Archivo oculto en Unix: ." + nombre);
                }
            }
            
        } catch (Exception e) {
            logger.warn("⚠️ No se pudo ocultar el archivo: " + archivo.getFileName(), e);
            // No lanzar excepción, el archivo ya está encriptado que es lo más importante
        }
    }

    // === APLICACIONES INSTALADAS ===

    public static String getInstalledApps(OperatingSystem os) {
        if (!os.getFamily().contains("Windows")) return "Solo Windows";

        List<AppInfo> apps = new ArrayList<>();
        apps.addAll(obtenerAppDelRegistro());
        apps.addAll(obtenerAppsDelStore());
        apps.addAll(obtenerEjecutablesPortables()); // Detectar .exe descargados/portables

        apps = apps.stream()
                .collect(Collectors.toMap(
                        a -> a.nombre.toLowerCase(),
                        a -> a,
                        (a1, a2) -> a1.version != null && !a1.version.isEmpty() ? a1 : a2
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(a -> a.nombre))
                .collect(Collectors.toList());

        String[][] basics = new String[][]{
                {"word", "Microsoft Word"},
                {"excel", "Microsoft Excel"},
                {"powerpoint", "Microsoft PowerPoint"},
                {"outlook", "Microsoft Outlook"},
        };
        
        Set<String> presentLower = apps.stream()
                .map(a -> a.nombre == null ? "" : a.nombre.toLowerCase())
                .collect(Collectors.toSet());

        String officeOverallVersion = "";
        for (AppInfo a : apps) {
            String n = a.nombre == null ? "" : a.nombre.toLowerCase();
            if (n.contains("microsoft office") || n.contains("office 365") || n.contains("microsoft 365") || n.contains("office")) {
                if (a.version != null && !a.version.trim().isEmpty()) {
                    officeOverallVersion = a.version.trim();
                    break;
                }
            }
        }

        for (String[] b : basics) {
            String keyword = b[0];
            String display = b[1];
            boolean found = false;
            for (AppInfo a : apps) {
                String n = a.nombre == null ? "" : a.nombre.toLowerCase();
                if (n.contains(keyword) || n.contains(display.toLowerCase())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                AppInfo ai = new AppInfo();
                ai.nombre = display;
                if (officeOverallVersion != null && !officeOverallVersion.isEmpty()) {
                    ai.version = officeOverallVersion;
                } else {
                    ai.version = "No instalada";
                }
                ai.publisher = "Microsoft";
                ai.installDate = "";
                apps.add(ai);
            }
        }

        apps = apps.stream().sorted(Comparator.comparing(a -> a.nombre)).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("\"DisplayName\",\"DisplayVersion\",\"Publisher\",\"InstallDate\"\n");

        for (AppInfo app : apps) {
            sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    escaparCsv(app.nombre),
                    escaparCsv(app.version != null ? app.version : ""),
                    escaparCsv(app.publisher != null ? app.publisher : ""),
                    escaparCsv(app.installDate != null ? app.installDate : "")));
        }

        return sb.toString();
    }

    protected static List<AppInfo> obtenerAppDelRegistro() {
        List<AppInfo> apps = new ArrayList<>();
        String[] rutas = {
                "HKLM:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*",
                "HKLM:\\Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*",
                "HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*"
        };

        try {
            StringBuilder rutasConcat = new StringBuilder();
            for (int i = 0; i < rutas.length; i++) {
                if (i > 0) rutasConcat.append(", ");
                rutasConcat.append(rutas[i]);
            }

            String cmd = String.format(
                    "$ErrorActionPreference='SilentlyContinue'; " +
                            "Get-ItemProperty %s | " +
                            "Where-Object {$_.DisplayName -and $_.DisplayName.Length -gt 0} | " +
                            "Select-Object DisplayName, DisplayVersion, Publisher, InstallDate | " +
                            "Sort-Object DisplayName | " +
                            "ConvertTo-Csv -NoTypeInformation -Delimiter ','",
                    rutasConcat.toString()
            );

            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-NoProfile", "-Command", cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean esEncabezado = true;

                while ((line = br.readLine()) != null) {
                    if (esEncabezado) {
                        esEncabezado = false;
                        continue;
                    }

                    List<String> campos = parseCsvLineAvanzado(line);
                    if (campos.size() >= 1 && !campos.get(0).isEmpty()) {
                        AppInfo app = new AppInfo();
                        app.nombre = campos.get(0);
                        app.version = campos.size() > 1 ? campos.get(1) : "";
                        app.publisher = campos.size() > 2 ? campos.get(2) : "";
                        app.installDate = campos.size() > 3 ? campos.get(3) : "";

                        if (!app.nombre.isEmpty()) {
                            apps.add(app);
                        }
                    }
                }
            }

            p.waitFor(30, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.err.println("Error capturando apps del registro: " + e.getMessage());
        }

        return apps;
    }

    protected static List<AppInfo> obtenerAppsDelStore() {
        List<AppInfo> apps = new ArrayList<>();

        try {
            String cmd = "$ErrorActionPreference='SilentlyContinue'; " +
                    "Get-AppxPackage -AllUsers | " +
                    "Where-Object {$_.Name -notmatch 'Microsoft\\.|Windows\\.' -and $_.Name} | " +
                    "Select-Object @{N='DisplayName';E={$_.Name}}, " +
                    "@{N='DisplayVersion';E={$_.Version}}, " +
                    "@{N='Publisher';E={$_.Publisher}} | " +
                    "ConvertTo-Csv -NoTypeInformation -Delimiter ','";

            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-NoProfile", "-Command", cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean esEncabezado = true;

                while ((line = br.readLine()) != null) {
                    if (esEncabezado) {
                        esEncabezado = false;
                        continue;
                    }

                    List<String> campos = parseCsvLineAvanzado(line);
                    if (campos.size() >= 1 && !campos.get(0).isEmpty()) {
                        AppInfo app = new AppInfo();
                        app.nombre = campos.get(0);
                        app.version = campos.size() > 1 ? campos.get(1) : "";
                        app.publisher = campos.size() > 2 ? campos.get(2) : "Microsoft Store";
                        app.installDate = "";

                        apps.add(app);
                    }
                }
            }

            p.waitFor(15, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.err.println("Error capturando apps del Store: " + e.getMessage());
        }

        return apps;
    }

    /**
     * Detecta ejecutables portables y descargados en carpetas comunes.
     * Busca en: Descargas, Escritorio, Program Files, AppData\Local, etc.
     */
    protected static List<AppInfo> obtenerEjecutablesPortables() {
        List<AppInfo> apps = new ArrayList<>();
        Set<String> nombresEncontrados = new HashSet<>();
        
        String userHome = System.getProperty("user.home");
        String programFiles = System.getenv("ProgramFiles");
        String programFilesX86 = System.getenv("ProgramFiles(x86)");
        String localAppData = System.getenv("LOCALAPPDATA");
        
        // Carpetas donde buscar ejecutables
        List<Path> carpetasBusqueda = new ArrayList<>();
        
        // Carpetas del usuario
        if (userHome != null) {
            carpetasBusqueda.add(Paths.get(userHome, "Downloads"));
            carpetasBusqueda.add(Paths.get(userHome, "Descargas"));
            carpetasBusqueda.add(Paths.get(userHome, "Desktop"));
            carpetasBusqueda.add(Paths.get(userHome, "Escritorio"));
            carpetasBusqueda.add(Paths.get(userHome, "Documents"));
            carpetasBusqueda.add(Paths.get(userHome, "Documentos"));
        }
        
        // Carpetas de programas portables comunes
        if (localAppData != null) {
            carpetasBusqueda.add(Paths.get(localAppData, "Programs"));
        }
        
        // Program Files (solo primer nivel para no tardar mucho)
        if (programFiles != null) {
            carpetasBusqueda.add(Paths.get(programFiles));
        }
        if (programFilesX86 != null) {
            carpetasBusqueda.add(Paths.get(programFilesX86));
        }
        
        // Ejecutables a ignorar (sistema, instaladores genéricos)
        Set<String> ignorar = new HashSet<>(Arrays.asList(
            "uninstall", "uninst", "setup", "install", "update", "updater",
            "helper", "crash", "reporter", "launcher", "elevated", "service"
        ));
        
        for (Path carpeta : carpetasBusqueda) {
            if (!Files.exists(carpeta) || !Files.isDirectory(carpeta)) continue;
            
            try {
                // Buscar en la carpeta y un nivel de subcarpetas
                try (var stream = Files.walk(carpeta, 2)) {
                    stream.filter(p -> p.toString().toLowerCase().endsWith(".exe"))
                          .filter(Files::isRegularFile)
                          .forEach(exePath -> {
                              try {
                                  String nombreArchivo = exePath.getFileName().toString();
                                  String nombreSinExt = nombreArchivo.replaceAll("(?i)\\.exe$", "");
                                  String nombreLower = nombreSinExt.toLowerCase();
                                  
                                  // Ignorar ejecutables de sistema/instaladores
                                  boolean esIgnorado = ignorar.stream().anyMatch(nombreLower::contains);
                                  if (esIgnorado) return;
                                  
                                  // Evitar duplicados
                                  if (nombresEncontrados.contains(nombreLower)) return;
                                  nombresEncontrados.add(nombreLower);
                                  
                                  // Obtener información del archivo
                                  AppInfo app = new AppInfo();
                                  app.nombre = nombreSinExt + " (Portable/Descargado)";
                                  
                                  // Intentar obtener versión del archivo
                                  app.version = obtenerVersionEjecutable(exePath);
                                  
                                  // Ubicación como publisher
                                  Path parentFolder = exePath.getParent();
                                  app.publisher = "Ubicación: " + (parentFolder != null ? parentFolder.getFileName().toString() : "Desconocida");
                                  
                                  // Fecha de modificación como fecha de instalación
                                  try {
                                      BasicFileAttributes attrs = Files.readAttributes(exePath, BasicFileAttributes.class);
                                      java.time.Instant instant = attrs.lastModifiedTime().toInstant();
                                      java.time.LocalDate fecha = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                                      app.installDate = fecha.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
                                  } catch (Exception e) {
                                      app.installDate = "";
                                  }
                                  
                                  apps.add(app);
                              } catch (Exception e) {
                                  // Ignorar errores individuales
                              }
                          });
                }
            } catch (Exception e) {
                System.err.println("Error buscando ejecutables en " + carpeta + ": " + e.getMessage());
            }
        }
        
        System.out.println("[Apps] Ejecutables portables/descargados encontrados: " + apps.size());
        return apps;
    }
    
    /**
     * Intenta obtener la versión de un ejecutable usando PowerShell.
     */
    protected static String obtenerVersionEjecutable(Path exePath) {
        try {
            String cmd = String.format(
                "(Get-Item '%s').VersionInfo.FileVersion",
                exePath.toString().replace("'", "''")
            );
            
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-NoProfile", "-Command", cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String version = br.readLine();
                p.waitFor(5, TimeUnit.SECONDS);
                
                if (version != null && !version.trim().isEmpty() && !version.contains("Exception")) {
                    return version.trim();
                }
            }
        } catch (Exception e) {
            // Ignorar
        }
        return "Portable";
    }

    public static List<String> parseCsvLineAvanzado(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean enComillas = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    campo.append('"');
                    i++;
                } else {
                    enComillas = !enComillas;
                }
            } else if (c == ',' && !enComillas) {
                result.add(campo.toString().trim());
                campo = new StringBuilder();
            } else {
                campo.append(c);
            }
        }
        result.add(campo.toString().trim());

        return result;
    }

    protected static void createAppsSheet(XSSFWorkbook wb, String user, InfoPC i) {
        Sheet s = wb.getSheet("InstalledApps");
        boolean esNuevaHoja = (s == null);
        
        if (esNuevaHoja) {
            s = wb.createSheet("InstalledApps");
        }

        CellStyle estiloTitulo = wb.createCellStyle();
        XSSFFont fontTitulo = wb.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 14);
        fontTitulo.setColor(IndexedColors.WHITE.getIndex());
        estiloTitulo.setFont(fontTitulo);
        estiloTitulo.setFillForegroundColor(obtenerColorProyecto());
        estiloTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloTitulo.setAlignment(HorizontalAlignment.CENTER);
        estiloTitulo.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle estiloHeader = wb.createCellStyle();
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBold(true);
        fontHeader.setFontHeightInPoints((short) 11);
        estiloHeader.setFont(fontHeader);
        estiloHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloHeader.setAlignment(HorizontalAlignment.CENTER);
        estiloHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        estiloHeader.setWrapText(true);

        CellStyle estiloNormal = wb.createCellStyle();
        estiloNormal.setWrapText(true);
        estiloNormal.setVerticalAlignment(VerticalAlignment.TOP);
        estiloNormal.setBorderBottom(BorderStyle.THIN);
        estiloNormal.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        
        // 🎨 Estilo para INICIO de grupo - VERDE MUY VISIBLE con bordes DOBLES
        CellStyle estiloInicio = wb.createCellStyle();
        estiloInicio.cloneStyleFrom(estiloNormal);
        estiloInicio.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex()); // Verde más brillante
        estiloInicio.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloInicio.setBorderTop(BorderStyle.DOUBLE); // Borde DOBLE superior
        estiloInicio.setTopBorderColor(IndexedColors.DARK_GREEN.getIndex());
        estiloInicio.setBorderBottom(BorderStyle.THIN);
        estiloInicio.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloInicio.setBorderLeft(BorderStyle.THICK); // Borde lateral grueso
        estiloInicio.setLeftBorderColor(IndexedColors.DARK_GREEN.getIndex());
        XSSFFont fontInicio = wb.createFont();
        fontInicio.setBold(true);
        fontInicio.setFontHeightInPoints((short) 11);
        fontInicio.setColor(IndexedColors.DARK_GREEN.getIndex());
        estiloInicio.setFont(fontInicio);
        
        // 🎨 Estilo para FIN de grupo - ROJO MUY VISIBLE con bordes DOBLES
        CellStyle estiloFin = wb.createCellStyle();
        estiloFin.cloneStyleFrom(estiloNormal);
        estiloFin.setFillForegroundColor(IndexedColors.CORAL.getIndex()); // Coral/Rojo claro muy visible
        estiloFin.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloFin.setBorderBottom(BorderStyle.DOUBLE); // Borde DOBLE inferior
        estiloFin.setBottomBorderColor(IndexedColors.RED.getIndex());
        estiloFin.setBorderLeft(BorderStyle.THICK); // Borde lateral grueso
        estiloFin.setLeftBorderColor(IndexedColors.RED.getIndex());
        XSSFFont fontFin = wb.createFont();
        fontFin.setBold(true);
        fontFin.setFontHeightInPoints((short) 11);
        fontFin.setColor(IndexedColors.DARK_RED.getIndex());
        estiloFin.setFont(fontFin);
        
        // 🎨 Estilos con fondo alternado para grupos MÁS VISIBLE
        CellStyle estiloGrupoClaro = wb.createCellStyle();
        estiloGrupoClaro.cloneStyleFrom(estiloNormal);
        estiloGrupoClaro.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        estiloGrupoClaro.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloGrupoClaro.setBorderLeft(BorderStyle.THIN);
        estiloGrupoClaro.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        
        CellStyle estiloGrupoOscuro = wb.createCellStyle();
        estiloGrupoOscuro.cloneStyleFrom(estiloNormal);
        estiloGrupoOscuro.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloGrupoOscuro.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloGrupoOscuro.setBorderLeft(BorderStyle.THIN);
        estiloGrupoOscuro.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());

        int cols = ENCABEZADOS_APLICACIONES.length;
        
        if (esNuevaHoja) {
            Row rowTitulo = s.createRow(0);
            rowTitulo.setHeight((short) 1200);
            Cell ct = rowTitulo.createCell(0);
            ct.setCellValue("APLICACIONES INSTALADAS - " + limpiarNombreProyecto(CURRENT_PROJECT).toUpperCase());
            ct.setCellStyle(estiloTitulo);
            s.addMergedRegion(new CellRangeAddress(0, 0, 0, Math.max(0, cols - 1)));

            Row header = s.createRow(1);
            header.setHeight((short) 600);
            for (int c = 0; c < cols; c++) {
                Cell cell = header.createCell(c);
                cell.setCellValue(ENCABEZADOS_APLICACIONES[c]);
                cell.setCellStyle(estiloHeader);
            }
        }

        String csv = (i != null && i.installedApps != null) ? i.installedApps.trim() : "";
        List<String[]> filas = new ArrayList<>();
        if (!csv.isEmpty()) {
            String[] lineas = csv.split("\\r?\\n");
            for (String linea : lineas) {
                if (linea == null) continue;
                String t = linea.trim();
                if (t.isEmpty()) continue;
                String low = t.toLowerCase();
                if (low.contains("displayname") && low.contains("displayversion")) continue;
                List<String> campos = parseCsvLineAvanzado(linea);
                if (campos == null || campos.isEmpty()) continue;
                filas.add(campos.toArray(new String[0]));
            }
            filas.sort(Comparator.comparing(a -> (a.length > 0 && a[0] != null) ? a[0].toLowerCase() : ""));
        }

        int filaIdx = s.getLastRowNum() + 1;
        if (esNuevaHoja) {
            filaIdx = 2;
        }
        
        String fecha = i != null && i.fecha != null ? formatearFecha(i.fecha) : formatearFecha(new Date());
        String idGrupo = (i != null && i.idGrupo != null) ? i.idGrupo : generarIdGrupo();
        
        // 📍 Marcar inicio y fin del grupo con estilos visuales
        int numApps = filas.size();
        
        // Determinar si este grupo debe tener fondo claro u oscuro (alternancia por grupo)
        // Contar cuántos grupos ya existen
        Set<String> gruposExistentes = new java.util.HashSet<>();
        for (int rowNum = 2; rowNum <= s.getLastRowNum(); rowNum++) {
            Row rowExist = s.getRow(rowNum);
            if (rowExist != null && rowExist.getCell(0) != null) {
                gruposExistentes.add(rowExist.getCell(0).getStringCellValue());
            }
        }
        boolean grupoOscuro = (gruposExistentes.size() % 2 == 1);
        
        for (int idx = 0; idx < filas.size(); idx++) {
            String[] campos = filas.get(idx);
            Row r = s.createRow(filaIdx++);
            
            // Determinar si es inicio o fin
            boolean esInicio = (idx == 0);
            boolean esFin = (idx == filas.size() - 1);
            String marcador = esInicio ? "🚩 INICIO" : (esFin ? "🏁 FIN" : "");
            
            // Estilo base según alternancia de grupo
            CellStyle estiloBase = grupoOscuro ? estiloGrupoOscuro : estiloGrupoClaro;
            
            // Columna 0: ID Grupo - con fondo especial si es inicio
            if (esInicio) {
                setCell(r, 0, idGrupo, estiloInicio);
            } else {
                setCell(r, 0, idGrupo, estiloBase);
            }
            
            // Columna 1: Número secuencial - especial en inicio/fin
            if (esInicio) {
                setCell(r, 1, String.valueOf(idx + 1), estiloInicio);
            } else if (esFin) {
                setCell(r, 1, String.valueOf(idx + 1), estiloFin);
            } else {
                setCell(r, 1, String.valueOf(idx + 1), estiloBase);
            }
            
            // Columnas restantes - aplicar estilo según inicio/fin/medio
            CellStyle estiloParaEstaFila = esInicio ? estiloInicio : (esFin ? estiloFin : estiloBase);
            
            // Columna 2: Fecha
            setCell(r, 2, fecha, estiloParaEstaFila);
            
            // Columna 3: Usuario Sistema
            setCell(r, 3, user != null ? user : "", estiloParaEstaFila);
            
            // Columna 4: Hostname
            setCell(r, 4, i != null && i.hostname != null ? i.hostname : "", estiloParaEstaFila);
            
            // Columna 5: IP
            setCell(r, 5, i != null && i.ip != null ? i.ip : "", estiloParaEstaFila);

            // Columnas 6-9: Datos de la aplicación
            setCell(r, 6, campos.length > 0 ? campos[0] : "", estiloParaEstaFila); // Aplicación
            setCell(r, 7, campos.length > 1 ? campos[1] : "", estiloParaEstaFila); // Versión
            setCell(r, 8, campos.length > 2 ? campos[2] : "", estiloParaEstaFila); // Fabricante
            setCell(r, 9, campos.length > 3 ? campos[3] : "", estiloParaEstaFila); // Fecha Instalación
            
            // 📍 Guardar marcador en comentario de celda para identificación posterior
            if (!marcador.isEmpty()) {
                Cell cellIdGrupo = r.getCell(0);
                Comment comment = cellIdGrupo.getCellComment();
                if (comment == null) {
                    CreationHelper factory = wb.getCreationHelper();
                    Drawing<?> drawing = s.createDrawingPatriarch();
                    ClientAnchor anchor = factory.createClientAnchor();
                    comment = drawing.createCellComment(anchor);
                    cellIdGrupo.setCellComment(comment);
                }
                CreationHelper factory = wb.getCreationHelper();
                comment.setString(factory.createRichTextString(marcador));
            }
        }

        if (s.getLastRowNum() >= 1) {
            s.setAutoFilter(new CellRangeAddress(1, 1, 0, Math.max(0, cols - 1)));
            s.createFreezePane(0, 2);
        }

        if (esNuevaHoja) {
            autoSize(s, cols);
        }
        
        System.out.println("📦 Grupo de aplicaciones guardado: " + idGrupo + " (" + numApps + " apps)");
    }

    protected static String escaparCsv(String valor) {
        if (valor == null) return "";
        return valor.replace("\"", "\"\"");
    }

    protected static String formatBytes(long bytes) {
        return FormatUtil.formatBytes(bytes).replace("i", "");
    }

    /**
     * Genera un ID único para agrupar las aplicaciones de un inventario.
     * Formato: YYYYMMDD-HHMMSS-XXX donde XXX son milisegundos
     */
    protected static String generarIdGrupo() {
        java.time.format.DateTimeFormatter sdf = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String timestamp = java.time.LocalDateTime.now().format(sdf);
        String millis = String.format("%03d", System.currentTimeMillis() % 1000);
        return timestamp + "-" + millis;
    }

    protected static String getActiveIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) { logger.warn("Error obteniendo interfaces de red: " + ignored.getMessage()); }
        return "Desconocida";
    }

    protected static boolean isFileLocked(Path path) {
        if (path == null || !Files.exists(path)) return false;
        try (FileChannel ch = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            return false;
        } catch (IOException e) {
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    protected static boolean reemplazarArchivoSinBackup(Path original, Path temp) {
        try {
            // En JavaFX, el manejo de diálogos de bloqueo se hace en la capa UI
            if (isFileLocked(original)) {
                return false;
            }
            Files.move(temp, original, StandardCopyOption.REPLACE_EXISTING);
            PortablePaths.protegerArchivo(original);
            return true;
        } catch (Exception e) {
            try {
                Files.deleteIfExists(temp);
            } catch (Exception ignored) {}
            return false;
        }
    }

    protected static Path resolveUniquePath(Path desired) {
        if (desired == null) return null;
        try {
            if (!Files.exists(desired)) return desired;
            String fileName = desired.getFileName().toString();
            String name = fileName;
            String ext = "";
            int dot = fileName.lastIndexOf('.');
            if (dot >= 0) {
                name = fileName.substring(0, dot);
                ext = fileName.substring(dot);
            }
            Path parent = desired.getParent();
            for (int i = 1; i < 10000; i++) {
                String alt = String.format("%s(%d)%s", name, i, ext);
                Path candidate = parent != null ? parent.resolve(alt) : Paths.get(alt);
                if (!Files.exists(candidate)) return candidate;
            }
        } catch (Exception ignored) {}
        return desired;
    }

    // === CLASES AUXILIARES ===

    public static class InfoPC {
        public Date fecha;
        public String userName, hostname, sistema, bios, cpu, gpu, ram, discos, ip, installedApps;
        public int numeroDiscos;
        public String modeloEquipo;
        public String manufacturer;
        public String deviceType;
        public String idGrupo; // ID único para agrupar aplicaciones de este inventario
        public String memoriaInstalada; // RAM física instalada en módulos
        public String tarjetaRed; // Información de tarjeta de red
    }

    protected static class AppInfo {
        String nombre;
        String version;
        String publisher;
        String installDate;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AppInfo)) return false;
            AppInfo appInfo = (AppInfo) o;
            return nombre.equalsIgnoreCase(appInfo.nombre);
        }

        @Override
        public int hashCode() {
            return nombre.toLowerCase().hashCode();
        }
    }
}
