package inventario.fx.database.repository;

import inventario.fx.database.DatabaseManager;
import inventario.fx.security.DatabaseEncryption;

import java.sql.*;
import java.util.Optional;

/**
 * Repositorio para gestionar configuraciones del sistema en SQLite.
 * Almacena pares clave-valor con metadatos como categoría, descripción y cifrado.
 *
 * <p>Tabla: configuracion
 * <ul>
 *   <li>clave (TEXT PRIMARY KEY)</li>
 *   <li>valor (TEXT)</li>
 *   <li>categoria (TEXT)</li>
 *   <li>descripcion (TEXT)</li>
 *   <li>encriptado (INTEGER)</li>
 *   <li>fecha_modificacion (TEXT)</li>
 * </ul>
 *
 * @author SELCOMP
 * @version 1.0
 * @since 2026-01-14
 */
public class ConfigRepository {

    /** Instancia compartida de encriptación para valores sensibles. */
    private static volatile DatabaseEncryption encryption;

    public ConfigRepository() {
        crearTabla();
    }

    /**
     * Obtiene (o crea) la instancia de DatabaseEncryption de forma thread-safe.
     */
    private static DatabaseEncryption getEncryption() {
        if (encryption == null) {
            synchronized (ConfigRepository.class) {
                if (encryption == null) {
                    encryption = new DatabaseEncryption();
                }
            }
        }
        return encryption;
    }

    /**
     * Crea la tabla de configuración si no existe.
     */
    private void crearTabla() {
        String sql = """
            CREATE TABLE IF NOT EXISTS configuracion (
                clave TEXT PRIMARY KEY,
                valor TEXT,
                categoria TEXT DEFAULT 'general',
                descripcion TEXT,
                encriptado INTEGER DEFAULT 0,
                fecha_modificacion TEXT DEFAULT (datetime('now', 'localtime'))
            )
            """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            System.err.println("[ConfigRepository] Error creando tabla configuracion: " + e.getMessage());
        }
    }

    /**
     * Obtiene el valor de una configuración por su clave.
     * Si el valor está marcado como encriptado, lo desencripta automáticamente.
     *
     * @param clave Clave de la configuración
     * @return Optional con el valor (ya desencriptado si corresponde)
     */
    public Optional<String> obtener(String clave) {
        String sql = "SELECT valor, encriptado FROM configuracion WHERE clave = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, clave);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String valor = rs.getString("valor");
                boolean encriptado = rs.getInt("encriptado") == 1;
                if (encriptado && valor != null && !valor.isEmpty()) {
                    try {
                        valor = getEncryption().desencriptar(valor);
                    } catch (Exception e) {
                        // Si falla la desencriptación, es probable que el valor sea legacy (texto plano)
                        // Lo devolvemos tal cual y se re-encriptará en la siguiente escritura
                        System.err.println("[ConfigRepository] Valor de '" + clave + "' no está encriptado, se migrará en la próxima escritura");
                    }
                }
                return Optional.ofNullable(valor);
            }
        } catch (Exception e) {
            System.err.println("[ConfigRepository] Error obteniendo config '" + clave + "': " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Guarda o actualiza una configuración.
     * Si {@code encriptado} es true, el valor se cifra con AES-256-GCM antes de almacenar.
     *
     * @param clave       Clave de la configuración
     * @param valor       Valor a guardar (se encripta si encriptado=true)
     * @param categoria   Categoría de la configuración
     * @param descripcion Descripción de la configuración
     * @param encriptado  Si el valor debe encriptarse antes de almacenarse
     */
    public void guardar(String clave, String valor, String categoria, String descripcion, boolean encriptado) {
        String valorAGuardar = valor;
        if (encriptado && valor != null && !valor.isEmpty()) {
            try {
                valorAGuardar = getEncryption().encriptar(valor);
            } catch (Exception e) {
                System.err.println("[ConfigRepository] Error encriptando valor de '" + clave + "': " + e.getMessage());
                // No guardar en texto plano si se solicitó cifrado
                return;
            }
        }

        String sql = """
            INSERT INTO configuracion (clave, valor, categoria, descripcion, encriptado, fecha_modificacion)
            VALUES (?, ?, ?, ?, ?, datetime('now', 'localtime'))
            ON CONFLICT(clave) DO UPDATE SET
                valor = excluded.valor,
                categoria = excluded.categoria,
                descripcion = excluded.descripcion,
                encriptado = excluded.encriptado,
                fecha_modificacion = datetime('now', 'localtime')
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, clave);
            pstmt.setString(2, valorAGuardar);
            pstmt.setString(3, categoria);
            pstmt.setString(4, descripcion);
            pstmt.setInt(5, encriptado ? 1 : 0);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("[ConfigRepository] Error guardando config '" + clave + "': " + e.getMessage());
        }
    }

    /**
     * Guarda una configuración simple (sin categoría ni cifrado).
     *
     * @param clave Clave de la configuración
     * @param valor Valor a guardar
     */
    public void guardar(String clave, String valor) {
        guardar(clave, valor, "general", null, false);
    }

    /**
     * Elimina una configuración por su clave.
     *
     * @param clave Clave a eliminar
     */
    public void eliminar(String clave) {
        String sql = "DELETE FROM configuracion WHERE clave = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, clave);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("[ConfigRepository] Error eliminando config '" + clave + "': " + e.getMessage());
        }
    }

    /**
     * Verifica si existe una configuración.
     *
     * @param clave Clave a verificar
     * @return true si existe
     */
    public boolean existe(String clave) {
        return obtener(clave).isPresent();
    }
}
