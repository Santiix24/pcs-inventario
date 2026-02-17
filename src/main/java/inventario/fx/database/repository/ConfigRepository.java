package inventario.fx.database.repository;

import inventario.fx.database.DatabaseManager;

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

    public ConfigRepository() {
        crearTabla();
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
     *
     * @param clave Clave de la configuración
     * @return Optional con el valor si existe
     */
    public Optional<String> obtener(String clave) {
        String sql = "SELECT valor FROM configuracion WHERE clave = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, clave);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.ofNullable(rs.getString("valor"));
            }
        } catch (Exception e) {
            System.err.println("[ConfigRepository] Error obteniendo config '" + clave + "': " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Guarda o actualiza una configuración.
     *
     * @param clave       Clave de la configuración
     * @param valor       Valor a guardar
     * @param categoria   Categoría de la configuración
     * @param descripcion Descripción de la configuración
     * @param encriptado  Si el valor debe marcarse como encriptado
     */
    public void guardar(String clave, String valor, String categoria, String descripcion, boolean encriptado) {
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
            pstmt.setString(2, valor);
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
