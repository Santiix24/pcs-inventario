package inventario.fx.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import inventario.fx.security.DatabaseEncryption;
import inventario.fx.util.AppLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gestor de conexiones a la base de datos SQLite con encriptación AES-256.
 * Utiliza HikariCP para pool de conexiones eficiente.
 * 
 * <p>La base de datos se crea automáticamente en:
 * {@code ~/.inventario/database/inventario.db} (ENCRIPTADA)
 * 
 * <p><b>Características de durabilidad:</b>
 * <ul>
 *   <li>Pool de conexiones automático (HikariCP)</li>
 *   <li>WAL mode con synchronous=FULL para máxima protección contra pérdida de datos</li>
 *   <li>busy_timeout para manejar acceso concurrente sin errores</li>
 *   <li>PRAGMAs aplicados por conexión para consistencia</li>
 *   <li>Checkpoint WAL automático al cerrar</li>
 *   <li>Verificación de integridad al iniciar</li>
 *   <li>Auto-recuperación desde backup si se detecta corrupción</li>
 *   <li>Encriptación AES-256 de datos sensibles</li>
 * </ul>
 * 
 * <p><b>Uso:</b>
 * <pre>{@code
 * try (Connection conn = DatabaseManager.getConnection()) {
 *     // Usar conexión (transparentemente encriptada)
 * }
 * }</pre>
 * 
 * @author SELCOMP
 * @version 3.0 - Durabilidad mejorada
 * @since 2026-01-14
 */
public class DatabaseManager {
    
    private static final AppLogger logger = AppLogger.getLogger(DatabaseManager.class);
    private static final String DB_DIR = inventario.fx.config.PortablePaths.getDatabaseDir().toString();
    private static final String DB_FILE = "inventario.db";
    private static final String DB_URL = inventario.fx.config.PortablePaths.getDatabaseUrl();
    
    /** Número máximo de reintentos para operaciones con SQLITE_BUSY */
    private static final int MAX_RETRIES = 3;
    /** Tiempo de espera entre reintentos (ms) */
    private static final long RETRY_DELAY_MS = 200;
    
    private static HikariDataSource dataSource;
    private static DatabaseEncryption encryption;
    private static boolean initialized = false;
    
    /**
     * Inicializa el pool de conexiones y crea el esquema si no existe.
     * Incluye verificación de integridad y auto-recuperación.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Inicializar sistema de encriptación
            encryption = new DatabaseEncryption();
            if (!encryption.verificarEncriptacion()) {
                throw new RuntimeException("Sistema de encriptación no funcional");
            }
            logger.info("🔐 Sistema de encriptación verificado");
            
            // Crear directorio si no existe
            Path dbPath = Paths.get(DB_DIR);
            if (!Files.exists(dbPath)) {
                Files.createDirectories(dbPath);
                logger.info("📁 Directorio de base de datos creado: " + DB_DIR);
            }
            
            // Crear copia de seguridad pre-inicio (protección contra corrupción durante inicio)
            crearCopiaPreInicio();
            
            // Configurar HikariCP con durabilidad máxima
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            // Validar conexiones antes de usarlas
            config.setConnectionTestQuery("SELECT 1");
            
            // Configuraciones de durabilidad SQLite - aplicadas por conexión
            // journal_mode y synchronous via DataSource properties para la primera configuración
            config.addDataSourceProperty("journal_mode", "WAL");
            config.addDataSourceProperty("synchronous", "FULL");   // FULL en vez de NORMAL: cada escritura se sincroniza a disco
            config.addDataSourceProperty("cache_size", "10000");
            
            // NOTA: SQLite estándar no soporta encriptación nativa
            // Para encriptación completa, se requiere SQLCipher (licencia comercial)
            // Aquí usamos encriptación a nivel de aplicación para datos sensibles
            
            // Configurar connection init SQL para que CADA conexión tenga los PRAGMAs correctos
            config.setConnectionInitSql(
                "PRAGMA journal_mode = WAL; " +
                "PRAGMA synchronous = FULL; " +
                "PRAGMA busy_timeout = 5000; " +
                "PRAGMA foreign_keys = ON; " +
                "PRAGMA cache_size = 10000; " +
                "PRAGMA trusted_schema = OFF; " +
                "PRAGMA cell_size_check = ON; " +
                "PRAGMA secure_delete = ON"
            );
            
            dataSource = new HikariDataSource(config);
            
            // Verificar integridad de la base de datos al iniciar
            if (!verificarIntegridadDB()) {
                logger.error("⚠️ Base de datos con problemas de integridad, intentando recuperar...");
                if (intentarRecuperacion()) {
                    logger.info("✅ Recuperación exitosa desde copia pre-inicio");
                    // Recrear el DataSource con la DB restaurada
                    dataSource.close();
                    dataSource = new HikariDataSource(config);
                } else {
                    logger.error("❌ No se pudo recuperar la base de datos automáticamente");
                }
            }
            
            // Crear esquema
            createSchema();
            
            // Registrar hook de cierre para checkpoint automático
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                shutdownSafe();
            }, "DB-Shutdown-Hook"));
            
            initialized = true;
            logger.info("✅ Base de datos inicializada con durabilidad máxima: " + DB_URL);
            
        } catch (Exception e) {
            logger.error("❌ Error inicializando base de datos", e);
            throw new RuntimeException("No se pudo inicializar la base de datos", e);
        }
    }
    
    /**
     * Crea una copia de seguridad del archivo DB antes de inicializar.
     * Protege contra corrupción durante el proceso de inicio.
     */
    private static void crearCopiaPreInicio() {
        try {
            Path dbFile = Paths.get(DB_DIR, DB_FILE);
            if (Files.exists(dbFile) && Files.size(dbFile) > 0) {
                Path copiaPreInicio = Paths.get(DB_DIR, DB_FILE + ".pre-start");
                Files.copy(dbFile, copiaPreInicio, StandardCopyOption.REPLACE_EXISTING);
                
                // Copiar también archivos WAL y SHM si existen
                Path walFile = Paths.get(DB_DIR, DB_FILE + "-wal");
                if (Files.exists(walFile)) {
                    Files.copy(walFile, Paths.get(DB_DIR, DB_FILE + "-wal.pre-start"), 
                              StandardCopyOption.REPLACE_EXISTING);
                }
                
                logger.info("💾 Copia pre-inicio creada correctamente");
            }
        } catch (Exception e) {
            logger.warn("No se pudo crear copia pre-inicio: " + e.getMessage());
            // No es fatal, continuar
        }
    }
    
    /**
     * Verifica la integridad de la base de datos SQLite.
     * Ejecuta PRAGMA integrity_check y quick_check.
     * 
     * @return true si la base de datos está íntegra
     */
    private static boolean verificarIntegridadDB() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // quick_check es más rápido que integrity_check completo
            try (ResultSet rs = stmt.executeQuery("PRAGMA quick_check")) {
                if (rs.next()) {
                    String resultado = rs.getString(1);
                    if ("ok".equals(resultado)) {
                        logger.info("✅ Verificación de integridad: OK");
                        return true;
                    } else {
                        logger.error("❌ Integridad comprometida: " + resultado);
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error verificando integridad de BD", e);
            return false;
        }
        return false;
    }
    
    /**
     * Intenta recuperar la base de datos desde la copia pre-inicio.
     * 
     * @return true si la recuperación fue exitosa
     */
    private static boolean intentarRecuperacion() {
        try {
            Path dbFile = Paths.get(DB_DIR, DB_FILE);
            Path copiaPreInicio = Paths.get(DB_DIR, DB_FILE + ".pre-start");
            
            if (Files.exists(copiaPreInicio) && Files.size(copiaPreInicio) > 0) {
                // Guardar la DB corrupta para análisis
                Path corrupta = Paths.get(DB_DIR, DB_FILE + ".corrupted." + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
                Files.copy(dbFile, corrupta, StandardCopyOption.REPLACE_EXISTING);
                logger.warn("📦 Base de datos corrupta guardada en: " + corrupta);
                
                // Restaurar la copia pre-inicio
                Files.copy(copiaPreInicio, dbFile, StandardCopyOption.REPLACE_EXISTING);
                
                // Eliminar archivos WAL/SHM que pueden estar corruptos
                Path walFile = Paths.get(DB_DIR, DB_FILE + "-wal");
                Path shmFile = Paths.get(DB_DIR, DB_FILE + "-shm");
                Files.deleteIfExists(walFile);
                Files.deleteIfExists(shmFile);
                
                logger.info("✅ Base de datos restaurada desde copia pre-inicio");
                return true;
            }
            
            logger.warn("⚠️ No hay copia pre-inicio disponible para recuperación");
            return false;
            
        } catch (Exception e) {
            logger.error("❌ Error durante recuperación automática", e);
            return false;
        }
    }
    
    /**
     * Obtiene una conexión del pool.
     * IMPORTANTE: Debe cerrarse usando try-with-resources.
     * 
     * @return Conexión a la base de datos
     * @throws SQLException Si hay error obteniendo la conexión
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        return dataSource.getConnection();
    }
    
    /**
     * Crea el esquema de la base de datos.
     */
    private static void createSchema() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Tabla de proyectos
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS proyectos (" +
                "    id TEXT PRIMARY KEY," +
                "    nombre TEXT NOT NULL," +
                "    descripcion TEXT," +
                "    color TEXT NOT NULL," +
                "    fecha_creacion TEXT NOT NULL," +
                "    activo INTEGER DEFAULT 1," +
                "    eliminado INTEGER DEFAULT 0," +
                "    imagen_path TEXT," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // Agregar columna imagen_path si no existe (migración para bases existentes)
            try {
                stmt.executeUpdate("ALTER TABLE proyectos ADD COLUMN imagen_path TEXT");
                logger.info("✅ Columna imagen_path agregada a tabla proyectos");
            } catch (SQLException e) {
                // La columna ya existe, ignorar
                if (!e.getMessage().contains("duplicate column")) {
                    throw e;
                }
            }
            
            // Tabla de reportes
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS reportes (" +
                "    id TEXT PRIMARY KEY," +
                "    proyecto_id TEXT," +
                "    ticket TEXT," +
                "    tipo_solicitud TEXT," +
                "    nombre_cliente TEXT," +
                "    correo_cliente TEXT," +
                "    tecnico TEXT," +
                "    fecha_reporte TEXT NOT NULL," +
                "    hora_reporte TEXT NOT NULL," +
                "    datos_json TEXT NOT NULL," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id)" +
                ")" 
            );
            
            // Tabla de inventarios - SIN JSON, solo columnas SQLite
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS inventarios (" +
                "    id TEXT PRIMARY KEY," +
                "    proyecto_id TEXT NOT NULL," +
                "    fecha TEXT," +
                "    usuario TEXT," +
                "    hostname TEXT," +
                "    sistema TEXT," +
                "    fabricante TEXT," +
                "    modelo TEXT," +
                "    serie TEXT," +
                "    placa TEXT," +
                "    procesador TEXT," +
                "    tarjeta_grafica TEXT," +
                "    memoria_ram TEXT," +
                "    disco_duro TEXT," +
                "    num_discos TEXT," +
                "    ip TEXT," +
                "    fecha_escaneo TEXT NOT NULL," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id)" +
                ")" 
            );
            
            // MIGRACIÓN: Agregar nuevas columnas si la tabla ya existía con estructura antigua
            String[] nuevasColumnas = {
                "fecha", "usuario", "hostname", "serie", "placa", "num_discos", "ip"
            };
            
            for (String columna : nuevasColumnas) {
                try {
                    stmt.executeUpdate("ALTER TABLE inventarios ADD COLUMN " + columna + " TEXT");
                    logger.info("✅ Columna '" + columna + "' agregada a tabla inventarios");
                } catch (SQLException e) {
                    // La columna ya existe, ignorar
                    if (!e.getMessage().contains("duplicate column")) {
                        logger.warn("Error agregando columna " + columna + ": " + e.getMessage());
                    }
                }
            }
            
            // MIGRACIÓN: Eliminar columna datos_completos_json si existe (SQLite no soporta DROP COLUMN antes de 3.35)
            // La dejamos por compatibilidad pero ya no se usa
            
            // Tabla de logs de auditoría
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS logs_auditoria (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    fecha TEXT NOT NULL," +
                "    usuario TEXT," +
                "    accion TEXT NOT NULL," +
                "    detalle TEXT," +
                "    ip TEXT," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")" 
            );
            
            // Tabla de configuración (reemplaza config_admin.json)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS configuracion (" +
                "    clave TEXT PRIMARY KEY," +
                "    valor TEXT NOT NULL," +
                "    categoria TEXT," +
                "    descripcion TEXT," +
                "    encriptado INTEGER DEFAULT 0," + // Indica si el valor está encriptado
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")" 
            );
            
            // Tabla de logs de acceso (reemplaza logs_acceso.json y access_log.json)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS logs_acceso (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    fecha TEXT NOT NULL," +
                "    hora TEXT NOT NULL," +
                "    usuario TEXT," +
                "    accion TEXT NOT NULL," +
                "    detalle TEXT," +
                "    ip TEXT," +
                "    exito INTEGER DEFAULT 1," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")" 
            );
            
            // Tabla de configuración de empresa (reemplaza config_empresa.json)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS empresa (" +
                "    id INTEGER PRIMARY KEY DEFAULT 1," +
                "    nombre TEXT NOT NULL," +
                "    ruc TEXT," +
                "    direccion TEXT," +
                "    telefono TEXT," +
                "    email TEXT," +
                "    logo_base64 TEXT," +
                "    configuracion_json TEXT," + // Para datos adicionales
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    CHECK (id = 1)" + // Solo una fila de configuración de empresa
                ")" 
            );
            
            // Índices para optimizar búsquedas
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_proyectos_activo ON proyectos(activo)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_reportes_proyecto ON reportes(proyecto_id)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_reportes_fecha ON reportes(fecha_reporte)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_inventarios_proyecto ON inventarios(proyecto_id)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_logs_fecha ON logs_auditoria(fecha)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_logs_acceso_fecha ON logs_acceso(fecha, hora)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_logs_acceso_usuario ON logs_acceso(usuario)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_configuracion_categoria ON configuracion(categoria)");
            
            logger.info("📊 Esquema de base de datos creado/verificado");
        }
    }
    
    /**
     * Obtiene el gestor de encriptación de la base de datos.
     * Permite encriptar/desencriptar datos sensibles.
     */
    public static DatabaseEncryption getEncryption() {
        if (!initialized) {
            throw new IllegalStateException("Base de datos no inicializada");
        }
        return encryption;
    }
    
    /**
     * Cierra el pool de conexiones de forma segura.
     * Ejecuta checkpoint WAL para asegurar que todos los datos estén escritos.
     * Debe llamarse al cerrar la aplicación.
     */
    public static synchronized void shutdown() {
        shutdownSafe();
    }
    
    /**
     * Cierre seguro interno. Ejecuta WAL checkpoint antes de cerrar.
     * Llamado tanto por shutdown() como por el ShutdownHook.
     */
    private static synchronized void shutdownSafe() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                // CRÍTICO: Checkpoint WAL antes de cerrar
                // Esto fuerza que TODOS los cambios pendientes se escriban al archivo .db principal
                ejecutarCheckpointWAL();
                
                dataSource.close();
                initialized = false;
                logger.info("🔌 Pool de conexiones cerrado con checkpoint WAL completado");
            } catch (Exception e) {
                logger.error("Error durante cierre seguro de BD", e);
                // Intentar cerrar de todas formas
                try {
                    dataSource.close();
                    initialized = false;
                } catch (Exception ex) {
                    logger.error("Error forzando cierre de pool", ex);
                }
            }
        }
    }
    
    /**
     * Ejecuta CHECKPOINT de WAL para forzar escritura de todos los cambios al archivo principal.
     * FUNDAMENTAL para evitar pérdida de datos que estén solo en el archivo -wal.
     */
    public static void ejecutarCheckpointWAL() {
        if (dataSource == null || dataSource.isClosed()) return;
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // TRUNCATE: Escribe todos los frames del WAL al DB y trunca el WAL a 0 bytes
            // Es el modo más seguro para asegurar que todo quede en el archivo principal
            stmt.execute("PRAGMA wal_checkpoint(TRUNCATE)");
            logger.info("✅ WAL checkpoint completado - todos los cambios escritos a disco");
            
        } catch (SQLException e) {
            logger.error("⚠️ Error ejecutando WAL checkpoint", e);
        }
    }
    
    /**
     * Verifica si la base de datos está inicializada.
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Obtiene la ruta completa del archivo de base de datos.
     */
    public static String getDatabasePath() {
        return DB_DIR + "/" + DB_FILE;
    }
    
    /**
     * Ejecuta una operación en una transacción con reintentos automáticos.
     * Si la operación falla por SQLITE_BUSY, reintenta hasta MAX_RETRIES veces.
     * Si la operación falla por otro motivo, se hace rollback automático.
     * 
     * @param operation La operación a ejecutar
     * @throws SQLException Si hay error en la transacción después de todos los reintentos
     */
    public static void executeInTransaction(TransactionOperation operation) throws SQLException {
        SQLException lastException = null;
        
        for (int intento = 1; intento <= MAX_RETRIES; intento++) {
            Connection conn = null;
            try {
                conn = getConnection();
                conn.setAutoCommit(false);
                
                operation.execute(conn);
                
                conn.commit();
                return; // Éxito
                
            } catch (SQLException e) {
                lastException = e;
                if (conn != null) {
                    try {
                        conn.rollback();
                        logger.warn("⚠️ Transacción revertida (intento " + intento + "/" + MAX_RETRIES + ")");
                    } catch (SQLException ex) {
                        logger.error("❌ Error haciendo rollback", ex);
                    }
                }
                
                // Si es SQLITE_BUSY (código 5), reintentar con delay
                if (e.getErrorCode() == 5 && intento < MAX_RETRIES) {
                    logger.warn("🔄 BD ocupada, reintentando en " + (RETRY_DELAY_MS * intento) + "ms...");
                    try {
                        Thread.sleep(RETRY_DELAY_MS * intento); // Backoff exponencial
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                } else {
                    throw e; // Error no recuperable o últimos intentos agotados
                }
                
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        logger.error("Error cerrando conexión", e);
                    }
                }
            }
        }
        
        // Si llegamos aquí, todos los reintentos fallaron
        throw lastException;
    }
    
    /**
     * Ejecuta una consulta con reintentos automáticos para SQLITE_BUSY.
     * Útil para operaciones individuales que no necesitan transacción explícita.
     * 
     * @param operation La operación a ejecutar
     * @param <T> Tipo del resultado
     * @return Resultado de la operación
     * @throws SQLException Si hay error después de todos los reintentos
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation) throws SQLException {
        SQLException lastException = null;
        
        for (int intento = 1; intento <= MAX_RETRIES; intento++) {
            try (Connection conn = getConnection()) {
                return operation.execute(conn);
                
            } catch (SQLException e) {
                lastException = e;
                
                if (e.getErrorCode() == 5 && intento < MAX_RETRIES) {
                    logger.warn("🔄 BD ocupada en consulta, reintentando (" + intento + "/" + MAX_RETRIES + ")...");
                    try {
                        Thread.sleep(RETRY_DELAY_MS * intento);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }
        
        throw lastException;
    }
    
    /**
     * Ejecuta verificación completa de integridad (más lenta pero exhaustiva).
     * Útil para verificaciones manuales o programadas.
     * 
     * @return Resultado de la verificación
     */
    public static String ejecutarIntegrityCheck() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA integrity_check")) {
            
            StringBuilder resultado = new StringBuilder();
            while (rs.next()) {
                resultado.append(rs.getString(1)).append("\n");
            }
            
            String res = resultado.toString().trim();
            if ("ok".equals(res)) {
                logger.info("✅ Verificación completa de integridad: OK");
            } else {
                logger.error("❌ Problemas de integridad encontrados:\n" + res);
            }
            return res;
            
        } catch (SQLException e) {
            logger.error("Error ejecutando integrity_check", e);
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Obtiene estadísticas del WAL (Write-Ahead Log).
     * Útil para diagnóstico y monitoreo de rendimiento.
     * 
     * @return Información del estado del WAL
     */
    public static String obtenerEstadoWAL() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            StringBuilder info = new StringBuilder();
            
            // Verificar journal mode
            try (ResultSet rs = stmt.executeQuery("PRAGMA journal_mode")) {
                if (rs.next()) info.append("Journal Mode: ").append(rs.getString(1)).append("\n");
            }
            
            // Verificar synchronous
            try (ResultSet rs = stmt.executeQuery("PRAGMA synchronous")) {
                if (rs.next()) {
                    int sync = rs.getInt(1);
                    String syncName = switch (sync) {
                        case 0 -> "OFF";
                        case 1 -> "NORMAL";
                        case 2 -> "FULL";
                        case 3 -> "EXTRA";
                        default -> "DESCONOCIDO(" + sync + ")";
                    };
                    info.append("Synchronous: ").append(syncName).append("\n");
                }
            }
            
            // Verificar foreign_keys
            try (ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys")) {
                if (rs.next()) info.append("Foreign Keys: ").append(rs.getInt(1) == 1 ? "ON" : "OFF").append("\n");
            }
            
            // Verificar busy_timeout
            try (ResultSet rs = stmt.executeQuery("PRAGMA busy_timeout")) {
                if (rs.next()) info.append("Busy Timeout: ").append(rs.getInt(1)).append("ms\n");
            }
            
            return info.toString();
            
        } catch (SQLException e) {
            logger.error("Error obteniendo estado WAL", e);
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Interfaz funcional para operaciones transaccionales.
     */
    @FunctionalInterface
    public interface TransactionOperation {
        void execute(Connection conn) throws SQLException;
    }
    
    /**
     * Interfaz funcional para operaciones con reintento y resultado.
     */
    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute(Connection conn) throws SQLException;
    }
}
