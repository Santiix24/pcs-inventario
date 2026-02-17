package inventario.fx.database.repository;

import inventario.fx.model.AdminManager;
import inventario.fx.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio para gestionar logs de acceso del sistema en SQLite.
 * Registra accesos, operaciones y eventos de auditoría.
 *
 * <p>Tabla: logs_acceso
 * <ul>
 *   <li>id (INTEGER PRIMARY KEY AUTOINCREMENT)</li>
 *   <li>fecha (TEXT) — solo fecha "yyyy-MM-dd"</li>
 *   <li>hora (TEXT) — solo hora "HH:mm:ss"</li>
 *   <li>accion (TEXT)</li>
 *   <li>detalle (TEXT)</li>
 *   <li>usuario (TEXT)</li>
 *   <li>ip (TEXT)</li>
 * </ul>
 *
 * @author SELCOMP
 * @version 1.0
 * @since 2026-01-14
 */
public class LogAccesoRepository {

    public LogAccesoRepository() {
        crearTabla();
    }

    /**
     * Crea la tabla de logs de acceso si no existe.
     * También asegura que la columna {@code hora} exista para bases de datos antiguas.
     */
    private void crearTabla() {
        String sql = """
            CREATE TABLE IF NOT EXISTS logs_acceso (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha TEXT NOT NULL,
                hora TEXT DEFAULT '',
                accion TEXT NOT NULL,
                detalle TEXT,
                usuario TEXT,
                ip TEXT
            )
            """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            // Asegurar que la columna hora exista en bases de datos anteriores
            try {
                stmt.execute("ALTER TABLE logs_acceso ADD COLUMN hora TEXT DEFAULT ''");
            } catch (Exception ignored) {
                // La columna ya existe
            }
        } catch (Exception e) {
            System.err.println("[LogAccesoRepository] Error creando tabla logs_acceso: " + e.getMessage());
        }
    }

    /**
     * Guarda un nuevo log de acceso en la base de datos.
     * Separa la fecha completa ("yyyy-MM-dd HH:mm:ss") en fecha y hora
     * para compatibilidad con esquemas que requieren la columna {@code hora}.
     *
     * @param log Objeto LogAcceso a guardar
     */
    public void save(AdminManager.LogAcceso log) {
        String sql = "INSERT INTO logs_acceso (fecha, hora, accion, detalle, usuario, ip) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String fecha = log.getFecha(); // "yyyy-MM-dd HH:mm:ss"
            String soloFecha = fecha != null && fecha.length() >= 10 ? fecha.substring(0, 10) : fecha;
            String hora = fecha != null && fecha.length() >= 19 ? fecha.substring(11, 19) : "";
            pstmt.setString(1, soloFecha);
            pstmt.setString(2, hora);
            pstmt.setString(3, log.getAccion());
            pstmt.setString(4, log.getDetalle());
            pstmt.setString(5, log.getUsuario());
            pstmt.setString(6, log.getIp());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("[LogAccesoRepository] Error guardando log: " + e.getMessage());
        }
    }

    /**
     * Obtiene los últimos N logs de acceso.
     *
     * @param limite Número máximo de logs a retornar
     * @return Lista de LogAcceso ordenados por fecha descendente
     */
    public List<AdminManager.LogAcceso> findAll(int limite) {
        List<AdminManager.LogAcceso> logs = new ArrayList<>();
        String sql = "SELECT fecha, hora, accion, detalle, usuario, ip FROM logs_acceso ORDER BY id DESC LIMIT ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limite);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                AdminManager.LogAcceso log = new AdminManager.LogAcceso();
                String fecha = rs.getString("fecha");
                String hora = rs.getString("hora");
                if (hora != null && !hora.isEmpty()) {
                    log.setFecha(fecha + " " + hora);
                } else {
                    log.setFecha(fecha);
                }
                log.setAccion(rs.getString("accion"));
                log.setDetalle(rs.getString("detalle"));
                log.setUsuario(rs.getString("usuario"));
                log.setIp(rs.getString("ip"));
                logs.add(log);
            }
        } catch (Exception e) {
            System.err.println("[LogAccesoRepository] Error cargando logs: " + e.getMessage());
        }
        return logs;
    }

    /**
     * Elimina todos los logs de acceso.
     */
    public void deleteAll() {
        String sql = "DELETE FROM logs_acceso";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            System.err.println("[LogAccesoRepository] Error eliminando logs: " + e.getMessage());
        }
    }

    /**
     * Cuenta el total de logs almacenados.
     *
     * @return Número total de logs
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM logs_acceso";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("[LogAccesoRepository] Error contando logs: " + e.getMessage());
        }
        return 0;
    }
}
