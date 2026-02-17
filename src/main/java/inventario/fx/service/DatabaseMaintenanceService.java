package inventario.fx.service;

import inventario.fx.database.DatabaseManager;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Servicio para operaciones de mantenimiento de la base de datos SQLite.
 * Extrae la logica de negocio que estaba embebida en MantenimientoFX.
 */
public class DatabaseMaintenanceService {

    private DatabaseMaintenanceService() {}

    /** Resultado de una operacion de mantenimiento */
    public static class ResultadoOperacion {
        public final boolean exito;
        public final String mensaje;
        public final long duracionMs;

        public ResultadoOperacion(boolean exito, String mensaje, long duracionMs) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.duracionMs = duracionMs;
        }
    }

    /**
     * Ejecuta una operacion SQL individual (VACUUM, ANALYZE, REINDEX, etc.)
     */
    public static ResultadoOperacion ejecutarOperacion(String nombre, String sql) {
        long start = System.currentTimeMillis();
        try {
            if ("INTEGRITY".equals(nombre)) {
                return ejecutarIntegrityCheck(start);
            }
            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
            long dur = System.currentTimeMillis() - start;
            return new ResultadoOperacion(true, "Completado exitosamente", dur);
        } catch (Exception ex) {
            long dur = System.currentTimeMillis() - start;
            return new ResultadoOperacion(false, "Error: " + ex.getMessage(), dur);
        }
    }

    /**
     * Ejecuta PRAGMA integrity_check y devuelve el resultado.
     */
    private static ResultadoOperacion ejecutarIntegrityCheck(long startTime) {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("PRAGMA integrity_check")) {
            String res = rs.next() ? rs.getString(1) : "desconocido";
            boolean ok = "ok".equalsIgnoreCase(res);
            long dur = System.currentTimeMillis() - startTime;
            String msg = ok ? "Integridad verificada (OK)" : "Problema detectado: " + res;
            return new ResultadoOperacion(ok, msg, dur);
        } catch (Exception ex) {
            long dur = System.currentTimeMillis() - startTime;
            return new ResultadoOperacion(false, "Error: " + ex.getMessage(), dur);
        }
    }

    /**
     * Ejecuta el WAL checkpoint (fuerza escritura de cambios pendientes).
     */
    public static ResultadoOperacion ejecutarWalCheckpoint() {
        return ejecutarOperacion("CHECKPOINT", "PRAGMA wal_checkpoint(TRUNCATE)");
    }

    /**
     * Compacta la base de datos (VACUUM + ANALYZE en secuencia).
     */
    public static ResultadoOperacion compactarBaseDatos() {
        long start = System.currentTimeMillis();
        try {
            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("VACUUM");
                stmt.execute("ANALYZE");
            }
            long dur = System.currentTimeMillis() - start;
            return new ResultadoOperacion(true, "Base de datos compactada y analizada", dur);
        } catch (Exception ex) {
            long dur = System.currentTimeMillis() - start;
            return new ResultadoOperacion(false, "Error: " + ex.getMessage(), dur);
        }
    }

    /**
     * Operaciones de mantenimiento y sus sentencias SQL.
     * Formato: {nombre, descripcion, color, sql}
     */
    public static String[][] getOperaciones() {
        return new String[][] {
            {"VACUUM",     "Compactando...",            "#8B5CF6", "VACUUM"},
            {"ANALYZE",    "Analizando...",             "#10B981", "ANALYZE"},
            {"OPTIMIZE",   "Optimizando...",            "#F59E0B", "PRAGMA optimize"},
            {"REINDEX",    "Reindexando...",            "#EC4899", "REINDEX"},
            {"CHECKPOINT", "Guardando...",              "#6366F1", "PRAGMA wal_checkpoint(TRUNCATE)"},
            {"INTEGRITY",  "Verificando integridad...", "#EF4444", "PRAGMA integrity_check"}
        };
    }

    /**
     * Ejecuta todas las operaciones de mantenimiento en secuencia.
     * Devuelve array de resultados en el mismo orden que getOperaciones().
     */
    public static ResultadoOperacion[] ejecutarMantenimientoCompleto() {
        String[][] ops = getOperaciones();
        ResultadoOperacion[] resultados = new ResultadoOperacion[ops.length];
        for (int i = 0; i < ops.length; i++) {
            resultados[i] = ejecutarOperacion(ops[i][0], ops[i][3]);
        }
        return resultados;
    }
}
