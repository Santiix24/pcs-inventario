package inventario.fx.config;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.DosFileAttributeView;

/**
 * Gestiona TODAS las rutas del sistema de forma portable.
 * <p>
 * En modo portable (USB / EXE), los datos se almacenan JUNTO al ejecutable
 * dentro de una carpeta oculta {@code .datos/}. Esto permite que la app
 * se lleve en una USB sin dejar rastro en la PC anfitriona.
 * <p>
 * Estructura portable:
 * <pre>
 *   USB:\
 *     Inventario.exe          ← ejecutable
 *     Exportaciones\          ← carpetas visibles para el usuario
 *       {Proyecto}\
 *         Reportes\           ← PDFs de reportes exportados
 *         Proyectos\          ← Copias del Excel para el usuario
 *     .datos\                 ← OCULTO (Windows: attrib +h +s)
 *       database\
 *         inventario.db
 *       security\
 *         db.key
 *         encryption.key
 *       config\
 *         application.properties
 *       proyectos\            ← Excel cifrados (ACL: solo usuario actual)
 *       firmas\
 *       borradores\
 *       backups\
 *       logs\
 *       reportes_mantenimiento.dat
 *       master.key
 *       config.properties
 * </pre>
 */
public final class PortablePaths {

    // ════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ════════════════════════════════════════════════════════════════════════

    /** Nombre de la carpeta raíz de datos (oculta) */
    private static final String DATA_FOLDER = ".datos";

    /** Directorio raíz resuelto una sola vez al arrancar */
    private static final Path BASE;

    /** Raíz de datos: {BASE}/.datos/ */
    private static final Path DATA_ROOT;

    static {
        BASE = resolverDirectorioBase();
        DATA_ROOT = BASE.resolve(DATA_FOLDER);
        System.out.println("[PortablePaths] Directorio base: " + BASE);
        System.out.println("[PortablePaths] Datos en:        " + DATA_ROOT);
    }

    private PortablePaths() { /* Utility class */ }

    // ════════════════════════════════════════════════════════════════════════
    //  RESOLUCIÓN DEL DIRECTORIO BASE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Determina dónde vive la aplicación.
     * Orden de prioridad:
     * <ol>
     *   <li>{@code app.home} — inyectada manualmente por lanzador.</li>
     *   <li>{@code user.dir} — con {@code <chdir>.} en Launch4j siempre apunta al directorio del .exe.</li>
     *   <li>CodeSource — solo si NO es un directorio temporal del sistema.</li>
     * </ol>
     */
    private static Path resolverDirectorioBase() {
        // 1. Propiedad app.home inyectada explícitamente
        String appHome = System.getProperty("app.home");
        if (appHome != null && !appHome.isBlank()) {
            Path p = Paths.get(appHome).toAbsolutePath().normalize();
            if (Files.isDirectory(p)) return p;
        }

        // 2. user.dir — Launch4j con <chdir>. lo fija al dir del .exe (más confiable que CodeSource)
        try {
            Path workDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
            if (Files.isDirectory(workDir) && !esDirTemporal(workDir)) {
                return workDir;
            }
        } catch (Exception ignored) { }

        // 3. CodeSource — funciona en desarrollo y cuando el JAR no está en temp
        try {
            Path jarPath = Paths.get(
                PortablePaths.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()
            ).toAbsolutePath().normalize();

            // Si la ruta parece un directorio temporal de Launch4j o del sistema, ignorarla
            if (!esDirTemporal(jarPath)) {
                if (Files.isRegularFile(jarPath)) {
                    return jarPath.getParent();
                }
                // En desarrollo: target/classes → subir 2 niveles
                if (jarPath.toString().contains("target")) {
                    return jarPath.getParent().getParent();
                }
                if (Files.isDirectory(jarPath)) return jarPath;
            }
        } catch (Exception e) {
            System.err.println("[PortablePaths] No se pudo resolver JAR path: " + e.getMessage());
        }

        // 4. Fallback final: user.dir aunque sea temp (mejor que nada)
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    /** Retorna true si el path está dentro de un directorio temporal del sistema. */
    private static boolean esDirTemporal(Path p) {
        String s = p.toString().toLowerCase();
        return s.contains("\\temp\\") || s.contains("\\tmp\\")
            || s.contains("/temp/") || s.contains("/tmp/")
            || s.contains("launch4j");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  GETTERS DE RUTAS
    // ════════════════════════════════════════════════════════════════════════

    /** Directorio donde está el ejecutable / proyecto */
    public static Path getBase() { return BASE; }

    /** Carpeta raíz de datos: {BASE}/.datos/ */
    public static Path getDataRoot() { return DATA_ROOT; }

    // ── Base de datos ──
    public static Path getDatabaseDir() { return DATA_ROOT.resolve("database"); }
    public static Path getDatabaseFile() { return getDatabaseDir().resolve("inventario.db"); }
    public static String getDatabaseUrl() { return "jdbc:sqlite:" + getDatabaseFile(); }

    // ── Seguridad ──
    public static Path getSecurityDir() { return DATA_ROOT.resolve("security"); }
    public static Path getDbKeyFile() { return getSecurityDir().resolve("db.key"); }
    public static Path getEncryptionKeyFile() { return getSecurityDir().resolve("encryption.key"); }
    public static Path getMasterKeyFile() { return DATA_ROOT.resolve("master.key"); }

    // ── Configuración ──
    public static Path getConfigDir() { return DATA_ROOT.resolve("config"); }
    public static Path getApplicationProperties() { return getConfigDir().resolve("application.properties"); }
    public static Path getConfigProperties() { return DATA_ROOT.resolve("config.properties"); }

    // ── Reportes y borradores ──
    public static Path getReportesFile() { return DATA_ROOT.resolve("reportes_mantenimiento.dat"); }
    public static Path getReportesBak() { return DATA_ROOT.resolve("reportes_mantenimiento.dat.bak"); }
    public static Path getBorradoresDir() { return DATA_ROOT.resolve("borradores"); }

    // ── Firmas digitales ──
    public static Path getFirmasDir() { return DATA_ROOT.resolve("firmas"); }

    // ── Backups ──
    public static Path getBackupsDir() { return DATA_ROOT.resolve("backups"); }

    // ── Logs ──
    public static Path getLogsDir() { return DATA_ROOT.resolve("logs"); }

    // ── Proyectos / Excel ──
    /** Carpeta donde se almacenan los archivos Excel de los proyectos (OCULTA dentro de .datos) */
    public static Path getProyectosDir() { return DATA_ROOT.resolve("proyectos"); }

    // ── Exportaciones ──
    /** Carpeta raíz de exportaciones: {BASE}/Exportaciones/ */
    public static Path getExportacionesDir() { return BASE.resolve("Exportaciones"); }

    /**
     * Carpeta de exportación por proyecto: {BASE}/Exportaciones/{nombreProyecto}/
     * <p>Crea la carpeta automáticamente si no existe.</p>
     *
     * @param nombreProyecto Nombre del proyecto (puede incluir prefijo "N. ")
     * @return Path a la carpeta de exportación del proyecto
     */
    public static Path getExportDir(String nombreProyecto) {
        String limpio = limpiarNombre(nombreProyecto);
        Path dir = getExportacionesDir().resolve(limpio);
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                System.out.println("[PortablePaths] Carpeta de exportación creada: " + dir);
            }
        } catch (IOException e) {
            System.err.println("[PortablePaths] No se pudo crear carpeta de exportación: " + dir + " → " + e.getMessage());
        }
        return dir;
    }

    /**
     * Carpeta de exportación de REPORTES por proyecto: {BASE}/Exportaciones/{nombreProyecto}/Reportes/
     * <p>Crea la carpeta automáticamente si no existe.</p>
     *
     * @param nombreProyecto Nombre del proyecto (puede incluir prefijo "N. ")
     * @return Path a la carpeta de reportes del proyecto
     */
    public static Path getExportReportesDir(String nombreProyecto) {
        Path dir = getExportDir(nombreProyecto).resolve("Reportes");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            System.err.println("[PortablePaths] No se pudo crear carpeta de reportes: " + dir + " → " + e.getMessage());
        }
        return dir;
    }

    /**
     * Carpeta de exportación de PROYECTOS (Excel) por proyecto: {BASE}/Exportaciones/{nombreProyecto}/Proyectos/
     * <p>Crea la carpeta automáticamente si no existe.</p>
     *
     * @param nombreProyecto Nombre del proyecto (puede incluir prefijo "N. ")
     * @return Path a la carpeta de archivos Excel del proyecto
     */
    public static Path getExportProyectosDir(String nombreProyecto) {
        Path dir = getExportDir(nombreProyecto).resolve("Proyectos");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            System.err.println("[PortablePaths] No se pudo crear carpeta de proyectos: " + dir + " → " + e.getMessage());
        }
        return dir;
    }

    /**
     * Carpeta de exportación para logs: {BASE}/Exportaciones/Logs/
     * <p>Crea la carpeta automáticamente si no existe.</p>
     */
    public static Path getExportLogsDir() {
        Path dir = getExportacionesDir().resolve("Logs");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                System.out.println("[PortablePaths] Carpeta de exportación de logs creada: " + dir);
            }
        } catch (IOException e) {
            System.err.println("[PortablePaths] No se pudo crear carpeta de logs: " + dir + " → " + e.getMessage());
        }
        return dir;
    }

    /**
     * Limpia el nombre del proyecto removiendo el prefijo numérico ("1. Antonio" → "Antonio")
     * y caracteres no válidos para nombres de carpeta.
     */
    private static String limpiarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return "General";
        // Remover prefijo "N. " (ej: "1. Antonio" → "Antonio")
        String limpio = nombre.replaceFirst("^\\d+\\.\\s*", "").trim();
        // Remover caracteres no válidos para nombres de carpeta en Windows
        limpio = limpio.replaceAll("[<>:\"/\\\\|?*]", "_");
        return limpio.isEmpty() ? "General" : limpio;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  INICIALIZACIÓN DE CARPETAS + OCULTARLAS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Crea TODA la estructura de carpetas necesaria y oculta la carpeta raíz.
     * Debe llamarse UNA VEZ al arrancar la aplicación.
     */
    public static void inicializar() throws IOException {
        Path[] carpetas = {
            DATA_ROOT,
            getDatabaseDir(),
            getSecurityDir(),
            getConfigDir(),
            getBorradoresDir(),
            getFirmasDir(),
            getBackupsDir(),
            getLogsDir(),
            getProyectosDir()
        };

        for (Path carpeta : carpetas) {
            if (!Files.exists(carpeta)) {
                Files.createDirectories(carpeta);
                System.out.println("[PortablePaths] Carpeta creada: " + carpeta);
            }
        }

        // Ocultar la carpeta raíz de datos en Windows
        ocultarEnWindows(DATA_ROOT);

        // 🔒 Proteger carpetas sensibles (ACL: solo usuario actual)
        protegerCarpeta(getSecurityDir());    // Claves de cifrado
        protegerCarpeta(getDatabaseDir());    // Base de datos SQLite
        protegerCarpeta(getProyectosDir());   // Excel cifrados

        // Ocultar la carpeta de proyectos (la carpeta sí, los archivos dentro NO)
        ocultarEnWindows(getProyectosDir());

        // Migrar carpeta antigua Proyectos/ -> .datos/proyectos/ si existe
        migrarCarpetaProyectos();

        System.out.println("[PortablePaths] ✅ Estructura de carpetas inicializada");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  OCULTAR ARCHIVOS / CARPETAS EN WINDOWS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Establece los atributos «Hidden» y «System» en un archivo o carpeta.
     * Solo funciona en Windows; en otros SO es un no-op.
     */
    public static void ocultarEnWindows(Path path) {
        if (!esWindows() || !Files.exists(path)) return;
        try {
            DosFileAttributeView view = Files.getFileAttributeView(path, DosFileAttributeView.class);
            if (view != null) {
                view.setHidden(true);
                view.setSystem(true);
            }
        } catch (IOException e) {
            System.err.println("[PortablePaths] No se pudo ocultar: " + path + " → " + e.getMessage());
        }
    }

    /**
     * Oculta un archivo específico dentro de la carpeta de datos.
     * Útil para archivos Excel encriptados, reportes, etc.
     */
    public static void ocultarArchivo(Path archivo) {
        ocultarEnWindows(archivo);
    }

    /**
     * Oculta todos los archivos dentro de un directorio (solo Hidden, sin System).
     */
    public static void ocultarContenido(Path directorio) {
        if (!esWindows() || !Files.isDirectory(directorio)) return;
        try (var stream = Files.list(directorio)) {
            stream.forEach(p -> {
                try {
                    DosFileAttributeView view = Files.getFileAttributeView(p, DosFileAttributeView.class);
                    if (view != null) view.setHidden(true);
                } catch (IOException ignored) {}
            });
        } catch (IOException e) {
            System.err.println("[PortablePaths] Error ocultando contenido de: " + directorio);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PROTECCIÓN DE CARPETAS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Protege una carpeta sensible: la oculta y deshabilita la herencia de
     * permisos en Windows, dejando SOLO el acceso del usuario actual.
     * Usa {@code icacls} para manipular las ACL de forma segura.
     */
    public static void protegerCarpeta(Path carpeta) {
        if (!esWindows() || !Files.exists(carpeta)) return;
        try {
            String ruta = carpeta.toAbsolutePath().toString();
            String usuario = System.getProperty("user.name");

            // 1. Deshabilitar herencia copiando las ACLs explícitas
            ejecutarComando("icacls", ruta, "/inheritance:d");

            // 2. Remover «Everyone» y «Users» (grupos genéricos)
            ejecutarComando("icacls", ruta, "/remove:g", "Everyone", "/T", "/Q");
            ejecutarComando("icacls", ruta, "/remove:g", "Users", "/T", "/Q");
            ejecutarComando("icacls", ruta, "/remove:g", "Usuarios", "/T", "/Q");

            // 3. Asegurar acceso completo al usuario actual y a SYSTEM
            ejecutarComando("icacls", ruta, "/grant", usuario + ":(OI)(CI)F", "/T", "/Q");
            ejecutarComando("icacls", ruta, "/grant", "SYSTEM:(OI)(CI)F", "/T", "/Q");

            System.out.println("[PortablePaths] \uD83D\uDD12 Carpeta protegida: " + carpeta.getFileName());
        } catch (Exception e) {
            System.err.println("[PortablePaths] No se pudo proteger carpeta: " + carpeta + " \u2192 " + e.getMessage());
        }
    }

    /**
     * Protege un archivo individual sensible (claves, configs cifrados).
     * En Windows: oculta el archivo y restringe ACL al usuario actual + SYSTEM.
     * En otros SO: no-op (la protección viene del directorio).
     */
    public static void protegerArchivo(Path archivo) {
        if (!esWindows() || archivo == null || !Files.exists(archivo)) return;
        try {
            String ruta = archivo.toAbsolutePath().toString();
            String usuario = System.getProperty("user.name");

            // 1. Deshabilitar herencia de permisos
            ejecutarComando("icacls", ruta, "/inheritance:d");

            // 2. Remover grupos genéricos
            ejecutarComando("icacls", ruta, "/remove:g", "Everyone", "/Q");
            ejecutarComando("icacls", ruta, "/remove:g", "Users", "/Q");
            ejecutarComando("icacls", ruta, "/remove:g", "Usuarios", "/Q");

            // 3. Solo el usuario actual y SYSTEM tienen acceso
            ejecutarComando("icacls", ruta, "/grant", usuario + ":F", "/Q");
            ejecutarComando("icacls", ruta, "/grant", "SYSTEM:F", "/Q");

            // 4. Ocultar el archivo
            ocultarEnWindows(archivo);
        } catch (Exception e) {
            System.err.println("[PortablePaths] No se pudo proteger archivo: " + archivo + " → " + e.getMessage());
        }
    }

    /** Ejecuta un comando del sistema sin esperar salida. */
    private static void ejecutarComando(String... cmd) {
        try {
            new ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start()
                .waitFor();
        } catch (Exception ignored) { }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILIDADES
    // ════════════════════════════════════════════════════════════════════════

    private static boolean esWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    /**
     * Migra archivos Excel de proyectos a {BASE}/.datos/proyectos/.
     * Busca en dos ubicaciones antiguas:
     * <ol>
     *   <li>{BASE}/Proyectos/ — carpeta antigua con nombre explícito</li>
     *   <li>{BASE}/*.xlsx — archivos sueltos junto al ejecutable (Inventario_*.xlsx)</li>
     * </ol>
     */
    private static void migrarCarpetaProyectos() {
        Path carpetaNueva = getProyectosDir();

        // 1. Migrar desde carpeta Proyectos/ si existe
        Path carpetaVieja = BASE.resolve("Proyectos");
        if (Files.exists(carpetaVieja) && Files.isDirectory(carpetaVieja)) {
            System.out.println("[PortablePaths] \uD83D\uDD04 Migrando Proyectos/ \u2192 .datos/proyectos/");
            moverExcelsDeCarpeta(carpetaVieja, carpetaNueva);

            // Eliminar carpeta vieja si quedó vacía
            try (var contenido = Files.list(carpetaVieja)) {
                if (contenido.findFirst().isEmpty()) {
                    Files.delete(carpetaVieja);
                    System.out.println("[PortablePaths] \u2705 Carpeta vieja Proyectos/ eliminada");
                }
            } catch (IOException e) {
                System.err.println("[PortablePaths] No se pudo eliminar carpeta vieja: " + e.getMessage());
            }
        }

        // 2. Migrar Excel sueltos de la carpeta base (Inventario_*.xlsx, inv_*.xlsx)
        try (var archivos = Files.list(BASE)) {
            archivos
                .filter(p -> Files.isRegularFile(p))
                .filter(p -> {
                    String nombre = p.getFileName().toString().toLowerCase();
                    return nombre.endsWith(".xlsx") &&
                           (nombre.startsWith("inventario_") || nombre.startsWith("inv_"));
                })
                .forEach(archivo -> {
                    try {
                        Path destino = carpetaNueva.resolve(archivo.getFileName());
                        if (!Files.exists(destino)) {
                            Files.move(archivo, destino, StandardCopyOption.REPLACE_EXISTING);
                            protegerArchivo(destino);
                            System.out.println("[PortablePaths]   Movido de BASE: " + archivo.getFileName());
                        }
                    } catch (IOException e) {
                        System.err.println("[PortablePaths]   Error moviendo " + archivo.getFileName() + ": " + e.getMessage());
                    }
                });
        } catch (IOException e) {
            System.err.println("[PortablePaths] Error listando carpeta base: " + e.getMessage());
        }
    }

    /** Mueve todos los archivos de una carpeta al destino, protegiendo cada uno. */
    private static void moverExcelsDeCarpeta(Path origen, Path destino) {
        try (var archivos = Files.list(origen)) {
            archivos.forEach(archivo -> {
                try {
                    Path dest = destino.resolve(archivo.getFileName());
                    if (!Files.exists(dest)) {
                        Files.move(archivo, dest, StandardCopyOption.REPLACE_EXISTING);
                        protegerArchivo(dest);
                        System.out.println("[PortablePaths]   Movido: " + archivo.getFileName());
                    }
                } catch (IOException e) {
                    System.err.println("[PortablePaths]   Error moviendo " + archivo.getFileName() + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println("[PortablePaths] Error listando " + origen + ": " + e.getMessage());
        }
    }

    /**
     * Migra datos del formato antiguo (~/.inventario/) al nuevo (.datos/).
     * Solo se ejecuta si la carpeta antigua existe Y la nueva está vacía.
     */
    public static void migrarDatosAntiguos() {
        Path oldRoot = Paths.get(System.getProperty("user.home"), ".inventario");
        if (!Files.exists(oldRoot) || !Files.isDirectory(oldRoot)) return;

        // Solo migrar si no hay datos nuevos
        if (Files.exists(getDatabaseFile())) {
            System.out.println("[PortablePaths] Datos portables ya existen, no se migra.");
            return;
        }

        System.out.println("[PortablePaths] 🔄 Migrando datos desde " + oldRoot + " a " + DATA_ROOT);
        try {
            copiarRecursivo(oldRoot, DATA_ROOT);
            System.out.println("[PortablePaths] ✅ Migración completada");
        } catch (IOException e) {
            System.err.println("[PortablePaths] ❌ Error migrando: " + e.getMessage());
        }
    }

    private static void copiarRecursivo(Path origen, Path destino) throws IOException {
        try (var stream = Files.walk(origen)) {
            stream.forEach(source -> {
                try {
                    Path target = destino.resolve(origen.relativize(source));
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    System.err.println("[PortablePaths] Error copiando " + source + ": " + e.getMessage());
                }
            });
        }
    }

    /**
     * Información de diagnóstico para depuración.
     */
    public static String getInfoDiagnostico() {
        StringBuilder sb = new StringBuilder();
        sb.append("Base: ").append(BASE).append("\n");
        sb.append("Datos: ").append(DATA_ROOT).append("\n");
        sb.append("DB: ").append(getDatabaseFile()).append(" [").append(Files.exists(getDatabaseFile()) ? "✅" : "❌").append("]\n");
        sb.append("Security: ").append(getSecurityDir()).append(" [").append(Files.exists(getSecurityDir()) ? "✅" : "❌").append("]\n");
        sb.append("Firmas: ").append(getFirmasDir()).append(" [").append(Files.exists(getFirmasDir()) ? "✅" : "❌").append("]\n");
        sb.append("Backups: ").append(getBackupsDir()).append(" [").append(Files.exists(getBackupsDir()) ? "✅" : "❌").append("]\n");
        sb.append("Proyectos: ").append(getProyectosDir()).append(" [").append(Files.exists(getProyectosDir()) ? "✅" : "❌").append("]\n");
        return sb.toString();
    }
}
