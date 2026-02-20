package inventario.fx.core;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Clase lanzadora que NO extiende Application.
 * <p>
 * Esto es necesario para ejecutar JavaFX desde un fat JAR (jar-with-dependencies).
 * Si la clase main extiende Application directamente, el runtime de JavaFX
 * exige que los módulos estén en el module-path. Con esta clase intermedia,
 * JavaFX funciona correctamente desde el classpath (fat JAR / .exe portable).
 * </p>
 * <p>
 * Además, en la primera ejecución desde un .exe copiado a una nueva ubicación,
 * extrae automáticamente el JRE portátil empotrado ({@code jre.zip}) y lo oculta.
 * En ejecuciones posteriores, Launch4j encuentra {@code ./jre} y lo usa directo.
 * </p>
 */
public class Launcher {

    private static final String JRE_ZIP_RESOURCE = "/jre.zip";
    private static final String JRE_FOLDER = "jre";

    public static void main(String[] args) {
        extractJreIfNeeded();
        MainApp.main(args);
    }

    // ════════════════════════════════════════════════════════════════════
    //  AUTO-EXTRACCIÓN DEL JRE PORTÁTIL
    // ════════════════════════════════════════════════════════════════════

    /**
     * Si no existe {@code ./jre} junto al ejecutable (.exe / .jar), extrae
     * el JRE empotrado desde {@code jre.zip} (recurso dentro del JAR) y lo
     * marca como carpeta oculta en Windows.
     * <p>
     * Esto permite copiar SOLO el .exe a cualquier carpeta y que funcione.
     * La primera vez usa el Java del sistema (registry), extrae el JRE,
     * y las siguientes veces Launch4j lo encuentra en {@code ./jre}.
     */
    private static void extractJreIfNeeded() {
        try {
            Path exeDir = resolveExeDirectory();
            if (exeDir == null) return; // Modo desarrollo, no extraer

            Path jreDir = exeDir.resolve(JRE_FOLDER);
            if (Files.isDirectory(jreDir)) return; // Ya existe, nada que hacer

            // Buscar jre.zip empotrado en el JAR
            InputStream zipIn = Launcher.class.getResourceAsStream(JRE_ZIP_RESOURCE);
            if (zipIn == null) return; // No hay JRE empotrado (modo dev)

            System.out.println("[Launcher] Primera ejecucion - extrayendo JRE portable...");
            long start = System.currentTimeMillis();

            try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipIn))) {
                byte[] buf = new byte[8192];
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path dest = jreDir.resolve(entry.getName()).normalize();
                    // Seguridad: no permitir zip-slip
                    if (!dest.startsWith(jreDir)) {
                        zis.closeEntry();
                        continue;
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(dest);
                    } else {
                        Files.createDirectories(dest.getParent());
                        try (OutputStream os = Files.newOutputStream(dest)) {
                            int n;
                            while ((n = zis.read(buf)) > 0) os.write(buf, 0, n);
                        }
                    }
                    zis.closeEntry();
                }
            }

            // Ocultar la carpeta jre en Windows (Hidden + System)
            try {
                // Intentar con atributo DOS nativo (más confiable que attrib)
                java.nio.file.attribute.DosFileAttributeView attrs =
                    Files.getFileAttributeView(jreDir, java.nio.file.attribute.DosFileAttributeView.class);
                if (attrs != null) {
                    attrs.setHidden(true);
                    attrs.setSystem(true);
                }
            } catch (Exception ignored) { /* No-Windows o sin permisos */ }
            try {
                // Doble seguro: llamar attrib.exe con ruta completa
                String winDir = System.getenv("WINDIR");
                if (winDir == null) winDir = "C:\\Windows";
                new ProcessBuilder(winDir + "\\System32\\attrib.exe", "+h", "+s", jreDir.toString())
                        .redirectErrorStream(true).start().waitFor();
            } catch (Exception ignored) { /* Ignorar si falla */ }

            long ms = System.currentTimeMillis() - start;
            System.out.printf("[Launcher] JRE extraido y oculto en %dms: %s%n", ms, jreDir);

        } catch (Exception e) {
            System.err.println("[Launcher] Error extrayendo JRE: " + e.getMessage());
        }
    }

    /**
     * Resuelve el directorio donde está el ejecutable (.exe o .jar).
     * Retorna {@code null} en modo desarrollo (target/classes).
     */
    private static Path resolveExeDirectory() {
        try {
            // 1. Directorio de trabajo (launch4j con <chdir>. lo fija al dir del exe)
            Path workDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

            // 2. Propiedad app.home inyectada opcionalmente por el lanzador
            String appHome = System.getProperty("app.home");
            if (appHome != null && !appHome.isBlank()) {
                Path p = Paths.get(appHome).toAbsolutePath().normalize();
                if (Files.isDirectory(p)) return p;
            }

            // 3. Ubicación del JAR/clase principal
            try {
                Path loc = Paths.get(
                        Launcher.class.getProtectionDomain()
                                .getCodeSource().getLocation().toURI()
                ).toAbsolutePath().normalize();

                // Si es un archivo .jar o .exe, el directorio padre es donde vive
                if (Files.isRegularFile(loc)) return loc.getParent();

                // Si es directorio de clases (ej. target/classes), estamos en dev
                if (loc.toString().contains("target")) return null;
            } catch (Exception ignored) { }

            // 4. Fallback: user.dir (funciona bien con launch4j <chdir>.)
            return workDir;
        } catch (Exception e) {
            return null;
        }
    }
}
