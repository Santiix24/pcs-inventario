package inventario.fx.database.repository;

import inventario.fx.model.AdminManager;
import inventario.fx.database.DatabaseManager;
import inventario.fx.util.AppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de proyectos en la base de datos.
 * Implementa el patrón Repository para abstraer el acceso a datos.
 * 
 * <p><b>IMPORTANTE:</b> Este repositorio es OPCIONAL y complementario.
 * El sistema actual con JSON sigue funcionando sin cambios.
 * 
 * <p><b>Operaciones soportadas:</b>
 * <ul>
 *   <li>CRUD completo de proyectos</li>
 *   <li>Búsqueda por nombre</li>
 *   <li>Filtrado por estado (activo/inactivo)</li>
 *   <li>Eliminación lógica (soft delete)</li>
 * </ul>
 * 
 * @author SELCOMP
 * @version 1.0
 * @since 2026-01-14
 */
public class ProyectoRepository {
    
    private static final AppLogger logger = AppLogger.getLogger(ProyectoRepository.class);
    private final DatabaseManager dbManager;
    
    /**
     * Constructor que recibe el DatabaseManager.
     * 
     * @param dbManager Administrador de base de datos
     */
    public ProyectoRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    /**
     * Guarda un proyecto en la base de datos con reintento automático.
     * Si ya existe, lo actualiza.
     * 
     * @param proyecto El proyecto a guardar
     * @return true si se guardó exitosamente
     */
    public boolean save(AdminManager.Proyecto proyecto) {
        String sql = "INSERT INTO proyectos (id, nombre, descripcion, color, fecha_creacion, activo, eliminado, imagen_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT(id) DO UPDATE SET " +
                     "nombre = excluded.nombre, " +
                     "descripcion = excluded.descripcion, " +
                     "color = excluded.color, " +
                     "activo = excluded.activo, " +
                     "imagen_path = excluded.imagen_path, " +
                     "updated_at = CURRENT_TIMESTAMP";
        
        try {
            return DatabaseManager.executeWithRetry(conn -> {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    System.out.println("[ProyectoRepository] 💾 Guardando proyecto: " + proyecto.getNombre() + " (ID: " + proyecto.getId() + ")");
                    
                    pstmt.setString(1, proyecto.getId());
                    pstmt.setString(2, proyecto.getNombre());
                    pstmt.setString(3, proyecto.getDescripcion());
                    pstmt.setString(4, proyecto.getColor());
                    pstmt.setString(5, proyecto.getFechaCreacion());
                    pstmt.setInt(6, proyecto.isActivo() ? 1 : 0);
                    pstmt.setInt(7, 0); // eliminado = false por defecto
                    pstmt.setString(8, proyecto.getImagenPath());
                    
                    int affected = pstmt.executeUpdate();
                    
                    if (affected > 0) {
                        logger.guardadoExitoso("Proyecto: " + proyecto.getNombre());
                        System.out.println("[ProyectoRepository] ✅ Proyecto guardado exitosamente: " + proyecto.getNombre());
                        return true;
                    } else {
                        System.err.println("[ProyectoRepository] ❌ No se afectaron filas al guardar proyecto: " + proyecto.getNombre());
                        return false;
                    }
                }
            });
        } catch (SQLException e) {
            logger.error("Error guardando proyecto: " + proyecto.getNombre(), e);
        }
        
        return false;
    }
    
    /**
     * Busca un proyecto por su ID.
     * Solo retorna proyectos activos (no eliminados).
     * 
     * @param id El ID del proyecto
     * @return Optional con el proyecto si existe y no está eliminado
     */
    public Optional<AdminManager.Proyecto> findById(String id) {
        String sql = "SELECT * FROM proyectos WHERE id = ? AND eliminado = 0";
        
        try (Connection conn = (dbManager != null ? dbManager.getConnection() : DatabaseManager.getConnection());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProyecto(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error buscando proyecto por ID: " + id, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Busca un proyecto por su nombre.
     * 
     * @param nombre El nombre del proyecto
     * @return Optional con el proyecto si existe
     */
    public Optional<AdminManager.Proyecto> findByNombre(String nombre) {
        String sql = "SELECT * FROM proyectos WHERE nombre = ? AND eliminado = 0";
        
        try (Connection conn = (dbManager != null ? dbManager.getConnection() : DatabaseManager.getConnection());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombre);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProyecto(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error buscando proyecto por nombre: " + nombre, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtiene todos los proyectos activos.
     * 
     * @return Lista de proyectos activos
     */
    public List<AdminManager.Proyecto> findAllActive() {
        return findAll(true, false);
    }
    
    /**
     * Obtiene todos los proyectos.
     * 
     * @param soloActivos Si es true, solo devuelve proyectos activos
     * @param incluirEliminados Si es true, incluye proyectos eliminados
     * @return Lista de proyectos
     */
    public List<AdminManager.Proyecto> findAll(boolean soloActivos, boolean incluirEliminados) {
        List<AdminManager.Proyecto> proyectos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM proyectos WHERE 1=1");
        
        if (soloActivos) {
            sql.append(" AND activo = 1");
        }
        if (!incluirEliminados) {
            sql.append(" AND eliminado = 0");
        }
        
        // IMPORTANTE: Ordenar por fecha de creación ASCENDENTE para mantener el orden original
        // Esto evita que los proyectos cambien de posición cuando se editan
        sql.append(" ORDER BY fecha_creacion ASC");
        
        try (Connection conn = (dbManager != null ? dbManager.getConnection() : DatabaseManager.getConnection());
             PreparedStatement stmt = conn.prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                proyectos.add(mapResultSetToProyecto(rs));
            }
            
            logger.info("📋 Proyectos cargados desde DB: " + proyectos.size());
            
        } catch (SQLException e) {
            logger.error("Error obteniendo todos los proyectos", e);
        }
        
        return proyectos;
    }
    
    /**
     * Elimina un proyecto lógicamente (soft delete).
     * 
     * @param id El ID del proyecto a eliminar
     * @return true si se eliminó exitosamente
     */
    public boolean delete(String id) {
        String sql = "UPDATE proyectos SET eliminado = 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            return DatabaseManager.executeWithRetry(conn -> {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, id);
                    int affected = pstmt.executeUpdate();
                    if (affected > 0) {
                        logger.eliminadoExitoso("Proyecto ID: " + id);
                        return true;
                    }
                    return false;
                }
            });
        } catch (SQLException e) {
            logger.error("Error eliminando proyecto: " + id, e);
        }
        
        return false;
    }
    
    /**
     * Restaura un proyecto eliminado.
     * 
     * @param id El ID del proyecto a restaurar
     * @return true si se restauró exitosamente
     */
    public boolean restore(String id) {
        String sql = "UPDATE proyectos SET eliminado = 0, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            return DatabaseManager.executeWithRetry(conn -> {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, id);
                    int affected = pstmt.executeUpdate();
                    if (affected > 0) {
                        logger.info("♻️ Proyecto restaurado: " + id);
                        return true;
                    }
                    return false;
                }
            });
        } catch (SQLException e) {
            logger.error("Error restaurando proyecto: " + id, e);
        }
        
        return false;
    }
    
    /**
     * Cuenta el total de proyectos activos.
     * 
     * @return Número de proyectos activos
     */
    public int countActive() {
        String sql = "SELECT COUNT(*) FROM proyectos WHERE activo = 1 AND eliminado = 0";
        
        try (Connection conn = (dbManager != null ? dbManager.getConnection() : DatabaseManager.getConnection());
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            logger.error("Error contando proyectos activos", e);
        }
        
        return 0;
    }
    
    /**
     * Mapea un ResultSet a un objeto Proyecto.
     */
    private AdminManager.Proyecto mapResultSetToProyecto(ResultSet rs) throws SQLException {
        AdminManager.Proyecto proyecto = new AdminManager.Proyecto();
        proyecto.setId(rs.getString("id"));
        proyecto.setNombre(rs.getString("nombre"));
        proyecto.setDescripcion(rs.getString("descripcion"));
        proyecto.setColor(rs.getString("color"));
        proyecto.setFechaCreacion(rs.getString("fecha_creacion"));
        proyecto.setActivo(rs.getInt("activo") == 1);
        proyecto.setImagenPath(rs.getString("imagen_path"));
        // El campo eliminado solo existe en BD, no en la clase Proyecto
        return proyecto;
    }
    
    /**
     * Sincroniza proyectos desde JSON a la base de datos usando una transacción.
     * Todos los proyectos se guardan en una sola transacción para atomicidad.
     * Útil para migración inicial.
     * 
     * @param proyectosDesdeJSON Lista de proyectos del JSON
     * @return Número de proyectos sincronizados
     */
    public int syncFromJSON(List<AdminManager.Proyecto> proyectosDesdeJSON) {
        final int[] synced = {0};
        
        try {
            DatabaseManager.executeInTransaction(conn -> {
                String sql = "INSERT INTO proyectos (id, nombre, descripcion, color, fecha_creacion, activo, eliminado, imagen_path) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                             "ON CONFLICT(id) DO UPDATE SET " +
                             "nombre = excluded.nombre, descripcion = excluded.descripcion, " +
                             "color = excluded.color, activo = excluded.activo, " +
                             "imagen_path = excluded.imagen_path, updated_at = CURRENT_TIMESTAMP";
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (AdminManager.Proyecto proyecto : proyectosDesdeJSON) {
                        pstmt.setString(1, proyecto.getId());
                        pstmt.setString(2, proyecto.getNombre());
                        pstmt.setString(3, proyecto.getDescripcion());
                        pstmt.setString(4, proyecto.getColor());
                        pstmt.setString(5, proyecto.getFechaCreacion());
                        pstmt.setInt(6, proyecto.isActivo() ? 1 : 0);
                        pstmt.setInt(7, 0);
                        pstmt.setString(8, proyecto.getImagenPath());
                        pstmt.addBatch();
                    }
                    int[] results = pstmt.executeBatch();
                    for (int r : results) {
                        if (r >= 0 || r == Statement.SUCCESS_NO_INFO) synced[0]++;
                    }
                }
            });
        } catch (SQLException e) {
            logger.error("Error sincronizando proyectos desde JSON", e);
        }
        
        logger.info("🔄 Proyectos sincronizados desde JSON a DB: " + synced[0]);
        return synced[0];
    }
}
