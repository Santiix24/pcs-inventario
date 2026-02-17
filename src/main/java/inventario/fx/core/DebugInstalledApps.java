package inventario.fx.core;
import inventario.fx.model.InventarioFXBase;
import inventario.fx.util.AppLogger;

public class DebugInstalledApps {
    public static void main(String[] args) {
        try {
            InventarioFXBase.InfoPC info = InventarioFXBase.recopilarInfo();
            if (info == null) {
                System.out.println("No se obtuvo InfoPC (null)");
                return;
            }
            String csv = info.installedApps == null ? "" : info.installedApps.trim();
            System.out.println("=== Comienzo CSV InstalledApps (líneas limitadas) ===");
            if (csv.isEmpty()) {
                System.out.println("<VACÍO>");
            } else {
                String[] lines = csv.split("\n");
                int limit = Math.min(60, lines.length);
                for (int i = 0; i < limit; i++) {
                    System.out.println(lines[i]);
                }
                if (lines.length > limit) System.out.println("... (" + (lines.length - limit) + " líneas más)");
            }

            System.out.println("\n=== Verificación rápida de aplicaciones básicas ===");
            String lower = csv.toLowerCase();
            String[] basics = {"word", "excel", "powerpoint", "outlook", "access"};
            for (String b : basics) {
                System.out.printf("%s: %s\n", b, lower.contains(b) ? "DETECTADO" : "NO DETECTADO");
            }

        } catch (Throwable t) {
            AppLogger.getLogger(DebugInstalledApps.class).error("Error: " + t.getMessage(), t);
        }
    }
}
