package inventario.fx.service;

import inventario.fx.util.AppLogger;
import java.sql.*;

public class VerificarBackup {
    public static void main(String[] args) {
        // Base de datos actual
        String dbActual = inventario.fx.config.PortablePaths.getDatabaseFile().toString();
        
        // Backup antes de última restauración
        String dbBackup = inventario.fx.config.PortablePaths.getDatabaseDir().toString() + "/inventario_pre_restore_1769525459597.db";
        
        System.out.println("=".repeat(80));
        System.out.println("VERIFICACIÓN DE DATOS - BASE DE DATOS VS BACKUP");
        System.out.println("=".repeat(80));
        
        verificarDB("BASE DE DATOS ACTUAL", dbActual);
        System.out.println();
        verificarDB("BACKUP ANTES DE RESTAURAR (09:50)", dbBackup);
    }
    
    private static void verificarDB(String titulo, String dbPath) {
        System.out.println("\n" + titulo + ":");
        System.out.println("Archivo: " + dbPath);
        System.out.println("-".repeat(80));
        
        // Cargar driver SQLite
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERROR: Driver SQLite no encontrado");
            return;
        }
        
        String url = "jdbc:sqlite:" + dbPath;
        
        try (Connection conn = DriverManager.getConnection(url)) {
            
            // 1. Contar proyectos
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM proyectos")) {
                if (rs.next()) {
                    System.out.println("✓ PROYECTOS: " + rs.getInt(1));
                }
            }
            
            // 2. Contar inventarios  
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM inventarios")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("✓ INVENTARIOS (equipos escaneados): " + count);
                    
                    if (count == 0) {
                        System.out.println("  ⚠️  NO HAY DATOS DE INVENTARIO - Esta es la razón por la que no se crean Excel");
                    }
                }
            }
            
            // 3. Mostrar estructura de tabla inventarios
            System.out.println("\nEstructura tabla 'inventarios':");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA table_info(inventarios)")) {
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("name") + " (" + rs.getString("type") + ")");
                }
            }
            
            // 4. Si hay inventarios, mostrar ejemplo
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM inventarios LIMIT 1")) {
                if (rs.next()) {
                    System.out.println("\nEjemplo de inventario:");
                    System.out.println("  - Proyecto ID: " + rs.getString("proyecto_id"));
                    System.out.println("  - Sistema: " + rs.getString("sistema"));
                    System.out.println("  - Fabricante: " + rs.getString("fabricante"));
                    System.out.println("  - Modelo: " + rs.getString("modelo"));
                    System.out.println("  - CPU: " + rs.getString("procesador"));
                    System.out.println("  - RAM: " + rs.getString("memoria_ram"));
                }
            }
            
        } catch (SQLException e) {
            AppLogger.getLogger(VerificarBackup.class).error("❌ ERROR: " + e.getMessage(), e);
        }
    }
}
