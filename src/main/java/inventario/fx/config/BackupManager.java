package inventario.fx.config;

import inventario.fx.database.DatabaseManager;
import inventario.fx.util.AppLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Gestor de respaldos automáticos del sistema.
 * Crea copias de seguridad de archivos JSON y base de datos.
 * 
 * @author SELCOMP
 * @version 1.0
 * @since 2026-01-14
 */
public class BackupManager {
    
    private static final AppLogger logger = AppLogger.getLogger(BackupManager.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
    
    private final ConfigManager config;
    private final Path backupPath;
    
    public BackupManager() {
        this.config = ConfigManager.getInstance();
        this.backupPath = Paths.get(config.getString("backup.path"));
        inicializarDirectorio();
    }
    
    /**
     * Inicializa el directorio de backups.
     */
    private void inicializarDirectorio() {
        try {
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
                logger.info("📁 Directorio de backups creado: " + backupPath);
            }
        } catch (IOException e) {
            logger.error("Error creando directorio de backups", e);
        }
    }
    
    /**
     * Crea un backup completo del sistema.
     * Solo respalda la base de datos SQLite encriptada (no más archivos JSON).
     * 
     * @return Ruta del archivo de backup creado
     */
    public Path crearBackup() {
        if (!config.getBoolean("backup.enabled")) {
            logger.info("ℹ️ Backups deshabilitados en configuración");
            return null;
        }
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        Path backupDir = backupPath.resolve("backup_" + timestamp);
        
        try {
            Files.createDirectories(backupDir);
            
            // SOLO backup de base de datos encriptada (ya no usamos JSON)
            copiarBaseDatos(backupDir);
            
            // Backup de archivos Excel (CRÍTICO para restauración completa)
            copiarArchivosExcel(backupDir);
            
            // Backup de configuración de aplicación
            copiarConfiguracion(backupDir);
            
            // Backup de claves de encriptación (CRÍTICO)
            copiarClavesEncriptacion(backupDir);
            
            // Backup de reportes de mantenimiento (.dat) — CRÍTICO
            copiarReportesMantenimiento(backupDir);
            
            // Backup de borradores de formularios
            copiarBorradores(backupDir);
            
            // Backup de firmas digitales (CRÍTICO para reportes)
            copiarFirmasDigitales(backupDir);
            
            // Backup de master.key y config.properties adicionales
            copiarClavesAdicionales(backupDir);
            
            logger.info("💾 Backup creado exitosamente: " + backupDir);
            
            // Limpiar backups antiguos
            limpiarBackupsAntiguos();
            
            return backupDir;
            
        } catch (IOException e) {
            logger.error("Error creando backup", e);
            return null;
        }
    }
    
    /**
     * Copia las claves de encriptación (FUNDAMENTAL para recuperar datos).
     * Sin esto, el backup sería inútil ya que la DB está encriptada.
     */
    private void copiarClavesEncriptacion(Path destino) throws IOException {
        Path securityDir = PortablePaths.getSecurityDir();
        
        if (Files.exists(securityDir)) {
            Path targetSecurityDir = destino.resolve("security");
            Files.createDirectories(targetSecurityDir);
            
            // Copiar todas las claves
            try (var files = Files.list(securityDir)) {
                files.forEach(source -> {
                    try {
                        Path target = targetSecurityDir.resolve(source.getFileName());
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                        logger.debug("🔑 Clave copiada: " + source.getFileName());
                    } catch (IOException e) {
                        logger.warn("No se pudo copiar clave: " + source.getFileName(), e);
                    }
                });
            }
            
            logger.info("🔐 Claves de encriptación respaldadas");
        } else {
            logger.warn("⚠️ No se encontró directorio de claves de encriptación");
        }
    }
    
    /**
     * @deprecated Ya no se usan archivos JSON. Los datos están en SQLite encriptado.
     * Método mantenido por compatibilidad con backups antiguos.
     */
    @Deprecated
    private void copiarArchivosJSON(Path destino) throws IOException {
        Path sourceDir = PortablePaths.getBase();
        
        List<String> archivosJSON = List.of(
            "proyectos.json",
            "config_admin.json",
            "logs_acceso.json",
            "access_log.json"
        );
        
        logger.warn("⚠️ Método copiarArchivosJSON está deprecado. Use base de datos SQLite.");
        
        for (String archivo : archivosJSON) {
            Path source = sourceDir.resolve(archivo);
            if (Files.exists(source)) {
                Path target = destino.resolve("legacy").resolve(archivo);
                Files.createDirectories(target.getParent());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                logger.debug("📄 Archivo legacy copiado: " + archivo);
            }
        }
    }
    
    /**
     * Copia la base de datos al directorio de backup.
     * CRÍTICO: Ejecuta CHECKPOINT para forzar escritura de WAL antes de copiar.
     * Verifica integridad y checksum SHA-256 de la copia.
     */
    private void copiarBaseDatos(Path destino) throws IOException {
        if (!config.getBoolean("db.enabled")) {
            return;
        }
        
        String dbPath = config.getString("db.path");
        if (dbPath != null) {
            Path source = Paths.get(dbPath);
            if (Files.exists(source)) {
                // CRÍTICO: Forzar checkpoint de WAL antes de copiar
                // Esto asegura que TODOS los cambios estén en el archivo .db principal
                ejecutarCheckpointWAL();
                
                // Verificar tamaño del archivo antes de copiar
                long tamañoOriginal = Files.size(source);
                logger.info("📊 Base de datos original: " + tamañoOriginal + " bytes");
                
                // Calcular checksum SHA-256 del original
                String checksumOriginal = calcularSHA256(source);
                logger.info("🔒 SHA-256 original: " + checksumOriginal);
                
                Path target = destino.resolve("inventario.db");
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                
                // Verificar que la copia se realizó correctamente
                long tamañoCopia = Files.size(target);
                String checksumCopia = calcularSHA256(target);
                
                logger.info("💾 Base de datos copiada: " + tamañoCopia + " bytes");
                logger.info("🔒 SHA-256 copia: " + checksumCopia);
                
                if (tamañoOriginal == tamañoCopia && checksumOriginal.equals(checksumCopia)) {
                    logger.info("✅ Verificación de copia exitosa (tamaño + checksum SHA-256)");
                    
                    // Guardar checksum para verificación futura
                    Path checksumFile = destino.resolve("inventario.db.sha256");
                    Files.writeString(checksumFile, checksumCopia);
                    
                } else {
                    logger.error("⚠️ ADVERTENCIA: Verificación de copia fallida!");
                    if (tamañoOriginal != tamañoCopia) {
                        logger.error("  Tamaño: original=" + tamañoOriginal + " copia=" + tamañoCopia);
                    }
                    if (!checksumOriginal.equals(checksumCopia)) {
                        logger.error("  SHA-256 no coincide: posible corrupción durante copia");
                    }
                }
                
                // Verificar integridad de la copia con SQLite
                verificarIntegridadBackup(target);
                
            } else {
                logger.error("❌ Base de datos no encontrada: " + source);
            }
        }
    }
    
    /**
     * Verifica la integridad de un archivo de base de datos SQLite de backup.
     * Abre la copia de backup y ejecuta PRAGMA quick_check para confirmar
     * que los datos son legibles y consistentes.
     * 
     * @param dbBackupPath Ruta al archivo de backup
     */
    private void verificarIntegridadBackup(Path dbBackupPath) {
        String backupUrl = "jdbc:sqlite:" + dbBackupPath.toAbsolutePath();
        
        try (Connection conn = java.sql.DriverManager.getConnection(backupUrl);
             Statement stmt = conn.createStatement()) {
            
            try (ResultSet rs = stmt.executeQuery("PRAGMA quick_check")) {
                if (rs.next()) {
                    String resultado = rs.getString(1);
                    if ("ok".equals(resultado)) {
                        logger.info("✅ Integridad del backup verificada: OK");
                    } else {
                        logger.error("❌ Backup con problemas de integridad: " + resultado);
                    }
                }
            }
            
            // Verificar que las tablas principales existen
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name IN " +
                    "('proyectos', 'reportes', 'inventarios', 'configuracion')")) {
                if (rs.next()) {
                    int tablasEncontradas = rs.getInt(1);
                    logger.info("📊 Tablas verificadas en backup: " + tablasEncontradas + "/4");
                    if (tablasEncontradas < 4) {
                        logger.warn("⚠️ Backup incompleto: faltan " + (4 - tablasEncontradas) + " tablas");
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("⚠️ Error verificando integridad del backup: " + e.getMessage());
        }
    }
    
    /**
     * Calcula el hash SHA-256 de un archivo.
     * Usado para verificar integridad de copias de backup.
     * 
     * @param filePath Ruta al archivo
     * @return Hash SHA-256 en formato hexadecimal
     */
    private String calcularSHA256(Path filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.error("Error calculando SHA-256: " + e.getMessage());
            return "ERROR";
        }
    }
    
    /**
     * Ejecuta CHECKPOINT de SQLite WAL para forzar escritura de todos los cambios.
     * FUNDAMENTAL: Sin esto, los cambios recientes pueden quedarse en el archivo WAL
     * y no aparecer en el backup.
     */
    private void ejecutarCheckpointWAL() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            logger.info("🔄 Ejecutando CHECKPOINT de WAL...");
            stmt.execute("PRAGMA wal_checkpoint(TRUNCATE)");
            logger.info("✅ CHECKPOINT completado - todos los cambios escritos al archivo principal");
            
        } catch (Exception e) {
            logger.error("⚠️ Error ejecutando checkpoint WAL", e);
            // Continuar de todas formas - el backup puede ser parcial pero es mejor que nada
        }
    }
    
    /**
     * Copia todos los archivos Excel del proyecto al backup.
     * CRÍTICO: Sin los Excel, los backups legacy no pueden restaurarse.
     */
    private void copiarArchivosExcel(Path destino) throws IOException {
        // BUSCAR EN LA CARPETA DE PROYECTOS CONFIGURADA
        String carpetaProyectos = config.getString("workspace.projects");
        if (carpetaProyectos == null || carpetaProyectos.isEmpty()) {
            carpetaProyectos = PortablePaths.getProyectosDir().toString();
        }
        
        Path sourceDir = Paths.get(carpetaProyectos);
        logger.debug("📁 Buscando Excel en: " + sourceDir.toAbsolutePath());
        
        if (!Files.exists(sourceDir)) {
            logger.warn("⚠️ Carpeta de proyectos no existe: " + sourceDir);
            return;
        }
        
        // Buscar todos los archivos Excel de inventario
        try (var files = Files.list(sourceDir)) {
            List<Path> excelFiles = files
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xlsx"))
                .filter(p -> p.getFileName().toString().contains("Inventario"))
                .toList();
            
            if (!excelFiles.isEmpty()) {
                Path excelDir = destino.resolve("excel");
                Files.createDirectories(excelDir);
                
                int count = 0;
                for (Path excelFile : excelFiles) {
                    Path target = excelDir.resolve(excelFile.getFileName());
                    Files.copy(excelFile, target, StandardCopyOption.REPLACE_EXISTING);
                    count++;
                    logger.debug("📊 Excel copiado: " + excelFile.getFileName());
                }
                
                logger.info("📊 " + count + " archivos Excel respaldados");
            } else {
                logger.debug("ℹ️ No hay archivos Excel para respaldar");
            }
        } catch (IOException e) {
            logger.warn("⚠️ Error al buscar archivos Excel: " + e.getMessage());
        }
    }
    
    /**
     * Copia el archivo de reportes de mantenimiento (.dat) al backup.
     * CRÍTICO: Este archivo contiene todos los reportes serializados.
     * Sin él, se pierden TODOS los reportes de mantenimiento.
     */
    private void copiarReportesMantenimiento(Path destino) throws IOException {
        Path reportesFile = PortablePaths.getReportesFile();
        
        Path source = Files.exists(reportesFile) ? reportesFile : null;
        
        if (source != null) {
            Path target = destino.resolve("reportes_mantenimiento.dat");
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            long size = Files.size(source);
            logger.info("📋 Reportes de mantenimiento copiados: " + size + " bytes");
            
            // También copiar el backup (.bak) si existe
            Path bakFile = Paths.get(source.toString() + ".bak");
            if (Files.exists(bakFile)) {
                Files.copy(bakFile, destino.resolve("reportes_mantenimiento.dat.bak"), StandardCopyOption.REPLACE_EXISTING);
                logger.debug("📋 Backup de reportes (.bak) también copiado");
            }
        } else {
            logger.warn("⚠️ No se encontró archivo de reportes de mantenimiento");
        }
    }
    
    /**
     * Copia la carpeta de borradores de formularios al backup.
     * Los borradores son formularios en progreso que el usuario no ha finalizado.
     */
    private void copiarBorradores(Path destino) throws IOException {
        Path borradoresDir = PortablePaths.getBorradoresDir();
        
        if (Files.exists(borradoresDir) && Files.isDirectory(borradoresDir)) {
            Path targetDir = destino.resolve("borradores");
            Files.createDirectories(targetDir);
            
            int count = 0;
            try (var files = Files.list(borradoresDir)) {
                for (Path src : files.toList()) {
                    if (Files.isRegularFile(src)) {
                        Files.copy(src, targetDir.resolve(src.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        count++;
                    }
                }
            }
            
            if (count > 0) {
                logger.info("📝 " + count + " borradores respaldados");
            } else {
                logger.debug("ℹ️ Carpeta de borradores vacía");
            }
        } else {
            logger.debug("ℹ️ No hay carpeta de borradores");
        }
    }
    
    /**
     * Copia el archivo de configuración al directorio de backup.
     */
    private void copiarConfiguracion(Path destino) throws IOException {
        Path source = PortablePaths.getApplicationProperties();
        
        if (Files.exists(source)) {
            Path target = destino.resolve("application.properties");
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("⚙️ Configuración copiada");
        }
    }
    
    /**
     * Copia todas las firmas digitales (PNG) al backup.
     * CRÍTICO: Sin las firmas, los reportes pierden las imágenes de firma del técnico y funcionario.
     */
    private void copiarFirmasDigitales(Path destino) throws IOException {
        Path firmasDir = PortablePaths.getFirmasDir();
        
        if (Files.exists(firmasDir) && Files.isDirectory(firmasDir)) {
            Path targetDir = destino.resolve("firmas");
            Files.createDirectories(targetDir);
            
            int count = 0;
            try (var files = Files.list(firmasDir)) {
                for (Path src : files.toList()) {
                    if (Files.isRegularFile(src)) {
                        Files.copy(src, targetDir.resolve(src.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        count++;
                    }
                }
            }
            
            if (count > 0) {
                logger.info("✍️ " + count + " firmas digitales respaldadas");
            } else {
                logger.debug("ℹ️ No hay firmas digitales para respaldar");
            }
        } else {
            logger.debug("ℹ️ No hay carpeta de firmas");
        }
    }
    
    /**
     * Copia claves adicionales: master.key y config.properties.
     * El master.key es fundamental para la encriptación del sistema.
     */
    private void copiarClavesAdicionales(Path destino) throws IOException {
        // master.key
        Path masterKey = PortablePaths.getMasterKeyFile();
        if (Files.exists(masterKey)) {
            Files.copy(masterKey, destino.resolve("master.key"), StandardCopyOption.REPLACE_EXISTING);
            logger.info("🔑 Master key respaldada");
        }
        
        // config.properties (configuración adicional)
        Path configProps = PortablePaths.getConfigProperties();
        if (Files.exists(configProps)) {
            Files.copy(configProps, destino.resolve("config.properties"), StandardCopyOption.REPLACE_EXISTING);
            logger.debug("⚙️ config.properties respaldado");
        }
    }
    
    /**
     * Limpia backups antiguos según la configuración.
     */
    private void limpiarBackupsAntiguos() {
        int keepLast = config.getInt("backup.keepLast", 7);
        
        try (Stream<Path> backups = Files.list(backupPath)) {
            List<Path> backupList = backups
                .filter(Files::isDirectory)
                .filter(p -> p.getFileName().toString().startsWith("backup_"))
                .sorted(Comparator.<Path, String>comparing(p -> p.getFileName().toString()).reversed())
                .toList();
            
            if (backupList.size() > keepLast) {
                for (int i = keepLast; i < backupList.size(); i++) {
                    eliminarDirectorio(backupList.get(i));
                    logger.info("🗑️ Backup antiguo eliminado: " + backupList.get(i).getFileName());
                }
            }
            
        } catch (IOException e) {
            logger.error("Error limpiando backups antiguos", e);
        }
    }
    
    /**
     * Elimina un directorio y su contenido.
     */
    private void eliminarDirectorio(Path directorio) throws IOException {
        try (Stream<Path> walk = Files.walk(directorio)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        logger.error("Error eliminando: " + path, e);
                    }
                });
        }
    }
    
    /**
     * Restaura un backup específico.
     * Restaura la base de datos encriptada, configuración, claves de encriptación Y archivos Excel.
     * 
     * @param backupDir Directorio del backup a restaurar
     * @return true si se restauró exitosamente
     */
    public boolean restaurarBackup(Path backupDir) {
        if (!Files.exists(backupDir) || !Files.isDirectory(backupDir)) {
            logger.error("Directorio de backup no válido: " + backupDir);
            return false;
        }
        
        try {
            logger.info("🔄 Restaurando backup desde: " + backupDir);
            
            // CRÍTICO: Restaurar claves de encriptación PRIMERO
            restaurarClavesEncriptacion(backupDir);
            
            // Restaurar base de datos encriptada
            restaurarBaseDatos(backupDir);
            
            // CRÍTICO: Restaurar archivos Excel (faltaba en versión anterior)
            restaurarArchivosExcel(backupDir);
            
            // Restaurar configuración de aplicación
            restaurarConfiguracion(backupDir);
            
            // Restaurar reportes de mantenimiento (.dat)
            restaurarReportesMantenimiento(backupDir);
            
            // Restaurar borradores de formularios
            restaurarBorradores(backupDir);
            
            // Restaurar firmas digitales
            restaurarFirmasDigitales(backupDir);
            
            // Restaurar claves adicionales (master.key, config.properties)
            restaurarClavesAdicionales(backupDir);
            
            // Verificar si hay archivos JSON legacy
            Path legacyDir = backupDir.resolve("legacy");
            if (Files.exists(legacyDir)) {
                logger.warn("⚠️ Este backup contiene archivos JSON legacy. Considere migrar a SQLite.");
                restaurarArchivosJSON(backupDir.resolve("legacy"));
            }
            
            logger.info("✅ Backup restaurado exitosamente");
            return true;
            
        } catch (IOException e) {
            logger.error("Error restaurando backup", e);
            return false;
        }
    }
    
    /**
     * Restaura las claves de encriptación desde el backup.
     * CRÍTICO: Sin estas claves, la base de datos encriptada es inutilizable.
     */
    private void restaurarClavesEncriptacion(Path source) throws IOException {
        Path backupSecurityDir = source.resolve("security");
        if (!Files.exists(backupSecurityDir)) {
            logger.warn("⚠️ No se encontraron claves de encriptación en el backup");
            return;
        }
        
        Path targetSecurityDir = PortablePaths.getSecurityDir();
        Files.createDirectories(targetSecurityDir);
        
        try (var files = Files.list(backupSecurityDir)) {
            files.forEach(source_file -> {
                try {
                    Path target = targetSecurityDir.resolve(source_file.getFileName());
                    Files.copy(source_file, target, StandardCopyOption.REPLACE_EXISTING);
                    logger.debug("🔑 Clave restaurada: " + source_file.getFileName());
                } catch (IOException e) {
                    logger.error("Error restaurando clave: " + source_file.getFileName(), e);
                }
            });
        }
        
        logger.info("🔐 Claves de encriptación restauradas");
    }
    
    /**
     * @deprecated Restaura archivos JSON legacy desde el backup.
     */
    @Deprecated
    private void restaurarArchivosJSON(Path source) throws IOException {
        Path destDir = PortablePaths.getBase();
        
        logger.warn("⚠️ Restaurando archivos JSON legacy");
        
        try (Stream<Path> files = Files.list(source)) {
            files.filter(p -> p.toString().endsWith(".json"))
                .forEach(file -> {
                    try {
                        Path target = destDir.resolve(file.getFileName());
                        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                        logger.debug("📄 Restaurado (legacy): " + file.getFileName());
                    } catch (IOException e) {
                        logger.error("Error restaurando: " + file.getFileName(), e);
                    }
                });
        }
    }
    
    /**
     * Restaura la base de datos desde el backup.
     * Verifica checksum SHA-256 e integridad SQLite antes de aplicar.
     */
    private void restaurarBaseDatos(Path source) throws IOException {
        Path dbBackup = source.resolve("inventario.db");
        if (Files.exists(dbBackup)) {
            // Verificar checksum SHA-256 si existe
            Path checksumFile = source.resolve("inventario.db.sha256");
            if (Files.exists(checksumFile)) {
                String checksumGuardado = Files.readString(checksumFile).trim();
                String checksumActual = calcularSHA256(dbBackup);
                
                if (!checksumGuardado.equals(checksumActual)) {
                    logger.error("❌ CHECKSUM SHA-256 NO COINCIDE - Backup posiblemente corrupto!");
                    logger.error("  Esperado: " + checksumGuardado);
                    logger.error("  Actual:   " + checksumActual);
                    throw new IOException("Backup corrupto: checksum SHA-256 no coincide");
                }
                logger.info("✅ Checksum SHA-256 del backup verificado");
            } else {
                logger.warn("⚠️ No se encontró archivo de checksum, verificando solo integridad SQLite");
            }
            
            // Verificar integridad SQLite del backup antes de restaurar
            verificarIntegridadBackup(dbBackup);
            
            String dbPath = config.getString("db.path");
            if (dbPath != null) {
                Path target = Paths.get(dbPath);
                
                // Crear copia de seguridad del DB actual antes de sobreescribir
                if (Files.exists(target)) {
                    Path backupActual = Paths.get(target.toString() + ".pre-restore." + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
                    Files.copy(target, backupActual, StandardCopyOption.REPLACE_EXISTING);
                    logger.info("💾 Copia de seguridad pre-restauración creada: " + backupActual.getFileName());
                }
                
                long tamañoBackup = Files.size(dbBackup);
                logger.info("📊 Base de datos en backup: " + tamañoBackup + " bytes");
                
                Files.copy(dbBackup, target, StandardCopyOption.REPLACE_EXISTING);
                
                // Eliminar archivos WAL/SHM antiguos que podrían causar conflictos
                Path walFile = Paths.get(target.toString() + "-wal");
                Path shmFile = Paths.get(target.toString() + "-shm");
                Files.deleteIfExists(walFile);
                Files.deleteIfExists(shmFile);
                
                // Verificar restauración
                long tamañoRestaurado = Files.size(target);
                String checksumRestaurado = calcularSHA256(target);
                String checksumBackup = calcularSHA256(dbBackup);
                
                logger.info("💾 Base de datos restaurada: " + tamañoRestaurado + " bytes");
                
                if (tamañoBackup == tamañoRestaurado && checksumBackup.equals(checksumRestaurado)) {
                    logger.info("✅ Verificación de restauración exitosa (tamaño + checksum)");
                } else {
                    logger.error("⚠️ ADVERTENCIA: Verificación de restauración fallida!");
                }
            }
        } else {
            logger.warn("⚠️ No se encontró archivo de base de datos en el backup");
        }
    }
    
    /**
     * Restaura los archivos Excel desde el backup.
     * CRÍTICO: Sin los Excel, los datos de los proyectos se pierden.
     */
    private void restaurarArchivosExcel(Path source) throws IOException {
        Path excelBackupDir = source.resolve("excel");
        if (!Files.exists(excelBackupDir)) {
            logger.warn("⚠️ No se encontraron archivos Excel en el backup");
            return;
        }
        
        // Determinar carpeta de destino para Excel
        String carpetaProyectos = config.getString("workspace.projects");
        if (carpetaProyectos == null || carpetaProyectos.isEmpty()) {
            carpetaProyectos = PortablePaths.getProyectosDir().toString();
        }
        
        Path targetDir = Paths.get(carpetaProyectos);
        Files.createDirectories(targetDir);
        
        int count = 0;
        try (var files = Files.list(excelBackupDir)) {
            for (Path excelFile : files.toList()) {
                if (excelFile.getFileName().toString().toLowerCase().endsWith(".xlsx")) {
                    Path target = targetDir.resolve(excelFile.getFileName());
                    Files.copy(excelFile, target, StandardCopyOption.REPLACE_EXISTING);
                    count++;
                    logger.debug("📊 Excel restaurado: " + excelFile.getFileName());
                }
            }
        }
        
        logger.info("✅ " + count + " archivos Excel restaurados");
    }
    
    /**
     * Restaura el archivo de reportes de mantenimiento desde el backup.
     */
    private void restaurarReportesMantenimiento(Path source) throws IOException {
        Path reportesBackup = source.resolve("reportes_mantenimiento.dat");
        if (!Files.exists(reportesBackup)) {
            logger.warn("⚠️ No se encontró reportes_mantenimiento.dat en el backup");
            return;
        }
        
        // Restaurar en la ubicación portable
        Path target = PortablePaths.getReportesFile();
        
        // Backup de seguridad del archivo actual antes de sobrescribir
        if (Files.exists(target)) {
            Path preRestore = Paths.get(target.toString() + ".pre-restore." +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            Files.copy(target, preRestore, StandardCopyOption.REPLACE_EXISTING);
            logger.info("💾 Backup pre-restauración de reportes: " + preRestore.getFileName());
        }
        
        Files.copy(reportesBackup, target, StandardCopyOption.REPLACE_EXISTING);
        logger.info("📋 Reportes de mantenimiento restaurados: " + Files.size(target) + " bytes");
        
        // Restaurar .bak si existe
        Path bakBackup = source.resolve("reportes_mantenimiento.dat.bak");
        if (Files.exists(bakBackup)) {
            Files.copy(bakBackup, Paths.get(target.toString() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            logger.debug("📋 Backup de reportes (.bak) restaurado");
        }
    }
    
    /**
     * Restaura la carpeta de borradores desde el backup.
     */
    private void restaurarBorradores(Path source) throws IOException {
        Path borradoresBackup = source.resolve("borradores");
        if (!Files.exists(borradoresBackup)) {
            logger.warn("⚠️ No se encontró carpeta de borradores en el backup");
            return;
        }
        
        Path targetDir = PortablePaths.getBorradoresDir();
        Files.createDirectories(targetDir);
        
        int count = 0;
        try (var files = Files.list(borradoresBackup)) {
            for (Path src : files.toList()) {
                if (Files.isRegularFile(src)) {
                    Files.copy(src, targetDir.resolve(src.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    count++;
                }
            }
        }
        
        logger.info("📝 " + count + " borradores restaurados");
    }
    
    /**
     * Restaura las firmas digitales desde el backup.
     */
    private void restaurarFirmasDigitales(Path source) throws IOException {
        Path firmasBackup = source.resolve("firmas");
        if (!Files.exists(firmasBackup)) {
            logger.warn("⚠️ No se encontró carpeta de firmas en el backup");
            return;
        }
        
        Path targetDir = PortablePaths.getFirmasDir();
        Files.createDirectories(targetDir);
        
        int count = 0;
        try (var files = Files.list(firmasBackup)) {
            for (Path src : files.toList()) {
                if (Files.isRegularFile(src)) {
                    Files.copy(src, targetDir.resolve(src.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    count++;
                }
            }
        }
        
        logger.info("✍️ " + count + " firmas digitales restauradas");
    }
    
    /**
     * Restaura claves adicionales: master.key y config.properties.
     */
    private void restaurarClavesAdicionales(Path source) throws IOException {
        // master.key
        Path masterKeyBackup = source.resolve("master.key");
        if (Files.exists(masterKeyBackup)) {
            Files.copy(masterKeyBackup, PortablePaths.getMasterKeyFile(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("🔑 Master key restaurada");
        }
        
        // config.properties
        Path configBackup = source.resolve("config.properties");
        if (Files.exists(configBackup)) {
            Files.copy(configBackup, PortablePaths.getConfigProperties(), StandardCopyOption.REPLACE_EXISTING);
            logger.debug("⚙️ config.properties restaurado");
        }
    }
    
    /**
     * Restaura la configuración desde el backup.
     */
    private void restaurarConfiguracion(Path source) throws IOException {
        Path configBackup = source.resolve("application.properties");
        if (Files.exists(configBackup)) {
            Path target = PortablePaths.getApplicationProperties();
            Files.copy(configBackup, target, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("⚙️ Configuración restaurada");
            config.recargar();
        }
    }
    
    /**
     * Lista todos los backups disponibles.
     */
    public List<BackupInfo> listarBackups() {
        List<BackupInfo> backups = new ArrayList<>();
        
        try (Stream<Path> files = Files.list(backupPath)) {
            files.filter(Files::isDirectory)
                .filter(p -> p.getFileName().toString().startsWith("backup_"))
                .forEach(path -> {
                    try {
                        backups.add(new BackupInfo(path));
                    } catch (IOException e) {
                        logger.error("Error leyendo info de backup: " + path, e);
                    }
                });
        } catch (IOException e) {
            logger.error("Error listando backups", e);
        }
        
        backups.sort(Comparator.comparing(BackupInfo::getTimestamp).reversed());
        return backups;
    }
    
    /**
     * Información de un backup.
     */
    public static class BackupInfo {
        private final Path path;
        private final String timestamp;
        private final long size;
        private final int fileCount;
        
        public BackupInfo(Path path) throws IOException {
            this.path = path;
            this.timestamp = path.getFileName().toString().replace("backup_", "");
            
            try (Stream<Path> files = Files.walk(path)) {
                this.fileCount = (int) files.filter(Files::isRegularFile).count();
            }
            
            try (Stream<Path> files = Files.walk(path)) {
                this.size = files.filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
            }
        }
        
        public Path getPath() { return path; }
        public String getTimestamp() { return timestamp; }
        public long getSize() { return size; }
        public int getFileCount() { return fileCount; }
        
        public String getSizeFormatted() {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        }
        
        @Override
        public String toString() {
            return String.format("%s - %d archivos - %s", timestamp, fileCount, getSizeFormatted());
        }
    }
}
