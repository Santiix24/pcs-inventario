package inventario.fx.core;
import inventario.fx.model.InventarioFXBase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.*;

/**
 * Simulador de entorno para pruebas de la aplicación de inventario.
 * Permite ejecutar la app con datos ficticios de diferentes sistemas operativos
 * y configuraciones de hardware, sin necesidad de estar en esas máquinas.
 *
 * <p>Uso desde línea de comandos:
 * <pre>
 *   mvn javafx:run -Djavafx.args="--simular=mac_m2_sonoma"
 *   mvn javafx:run -Djavafx.args="--simular=linux_ubuntu_desktop"
 * </pre>
 *
 * <p>Los perfiles de simulación se cargan desde archivos JSON en
 * {@code /simulacion/} dentro de los recursos de la aplicación.
 *
 * @author SELCOMP
 * @version 1.0
 * @since 2026-01-14
 */
public class SimuladorEntorno {

    private static boolean activa = false;
    private static String perfilActual = null;
    private static Map<String, PerfilSimulacion> perfiles = new HashMap<>();

    static {
        cargarPerfiles();
    }

    /**
     * Perfil de simulación con datos de un equipo ficticio.
     */
    public static class PerfilSimulacion {
        public String id;
        public String nombre;
        public String sistema;
        public String bios;
        public String cpu;
        public String gpu;
        public String ram;
        public String discos;
        public int numeroDiscos;
        public String ip;
        public String hostname;
        public String userName;
        public String modeloEquipo;
        public String manufacturer;
        public String deviceType;
        public String memoriaInstalada;
        public String tarjetaRed;
        public String installedApps;
    }

    /**
     * Carga los perfiles de simulación desde recursos o crea perfiles predeterminados.
     */
    private static void cargarPerfiles() {
        // Intentar cargar desde recursos
        try {
            InputStream is = SimuladorEntorno.class.getResourceAsStream("/simulacion/perfiles.json");
            if (is != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(is);
                Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    PerfilSimulacion perfil = mapper.treeToValue(entry.getValue(), PerfilSimulacion.class);
                    perfil.id = entry.getKey();
                    perfiles.put(entry.getKey(), perfil);
                }
                is.close();
                System.out.println("[SimuladorEntorno] ✅ Perfiles cargados: " + perfiles.size());
                return;
            }
        } catch (Exception e) {
            System.err.println("[SimuladorEntorno] ⚠️ Error cargando perfiles JSON: " + e.getMessage());
        }

        // Crear perfiles predeterminados
        crearPerfilesPredeterminados();
    }

    /**
     * Crea perfiles de simulación predeterminados.
     */
    private static void crearPerfilesPredeterminados() {
        // Perfil Mac M2
        PerfilSimulacion mac = new PerfilSimulacion();
        mac.id = "mac_m2_sonoma";
        mac.nombre = "MacBook Pro M2 - macOS Sonoma";
        mac.sistema = "macOS Sonoma 14.2.1";
        mac.bios = "Apple M2 EFI";
        mac.cpu = "Apple M2 Pro 12-Core";
        mac.gpu = "Apple M2 Pro 19-Core GPU";
        mac.ram = "16 GB LPDDR5";
        mac.discos = "512GB Apple SSD (APFS)";
        mac.numeroDiscos = 1;
        mac.ip = "192.168.1.100";
        mac.hostname = "MacBook-Pro.local";
        mac.userName = "usuario_mac";
        mac.modeloEquipo = "MacBookPro18,3";
        mac.manufacturer = "Apple Inc.";
        mac.deviceType = "Laptop";
        mac.memoriaInstalada = "16 GB LPDDR5 (Unificada)";
        mac.tarjetaRed = "Broadcom BCM4387 Wi-Fi 6E";
        mac.installedApps = "Safari 17.2|Xcode 15.1|Final Cut Pro 10.7|Logic Pro 10.8";
        perfiles.put(mac.id, mac);

        // Perfil Linux Ubuntu
        PerfilSimulacion linux = new PerfilSimulacion();
        linux.id = "linux_ubuntu_desktop";
        linux.nombre = "PC Desktop - Ubuntu 22.04 LTS";
        linux.sistema = "Ubuntu 22.04.3 LTS (Jammy Jellyfish)";
        linux.bios = "American Megatrends UEFI v2.70";
        linux.cpu = "AMD Ryzen 7 5800X 8-Core 3.8 GHz";
        linux.gpu = "NVIDIA GeForce RTX 3070 8GB";
        linux.ram = "32 GB DDR4 3200 MHz";
        linux.discos = "1TB NVMe SSD (ext4) | 2TB HDD (ext4)";
        linux.numeroDiscos = 2;
        linux.ip = "192.168.1.50";
        linux.hostname = "ubuntu-workstation";
        linux.userName = "usuario_linux";
        linux.modeloEquipo = "Custom Build";
        linux.manufacturer = "ASUS";
        linux.deviceType = "Desktop";
        linux.memoriaInstalada = "2x16GB DDR4 Corsair Vengeance 3200MHz";
        linux.tarjetaRed = "Intel I225-V 2.5Gb Ethernet";
        linux.installedApps = "Firefox 120.0|LibreOffice 7.6|VS Code 1.85|GIMP 2.10";
        perfiles.put(linux.id, linux);

        // Perfil Windows Server
        PerfilSimulacion server = new PerfilSimulacion();
        server.id = "windows_server_2022";
        server.nombre = "Dell PowerEdge - Windows Server 2022";
        server.sistema = "Windows Server 2022 Datacenter";
        server.bios = "Dell Inc. UEFI v2.18.1";
        server.cpu = "Intel Xeon Gold 6348 28-Core 2.60 GHz";
        server.gpu = "Matrox G200eR2 (Integrada)";
        server.ram = "128 GB DDR4 ECC 3200 MHz";
        server.discos = "2x960GB SSD RAID1 | 4x4TB HDD RAID5";
        server.numeroDiscos = 6;
        server.ip = "10.0.0.5";
        server.hostname = "SRV-SELCOMP-01";
        server.userName = "Administrador";
        server.modeloEquipo = "PowerEdge R750";
        server.manufacturer = "Dell Inc.";
        server.deviceType = "Server";
        server.memoriaInstalada = "4x32GB DDR4 ECC Samsung";
        server.tarjetaRed = "Broadcom NetXtreme 25Gb SFP28";
        server.installedApps = "SQL Server 2022|IIS 10.0|.NET 8.0|PowerShell 7.4";
        perfiles.put(server.id, server);

        System.out.println("[SimuladorEntorno] ℹ️ " + perfiles.size() + " perfiles predeterminados creados");
    }

    /**
     * Activa la simulación con el perfil especificado.
     *
     * @param perfilId ID del perfil a activar
     * @return true si el perfil existe y se activó correctamente
     */
    public static boolean activar(String perfilId) {
        if (perfiles.containsKey(perfilId)) {
            activa = true;
            perfilActual = perfilId;
            System.out.println("🔄 [SIMULACIÓN] Perfil activado: " + perfiles.get(perfilId).nombre);
            return true;
        }
        return false;
    }

    /**
     * Desactiva la simulación.
     */
    public static void desactivar() {
        activa = false;
        perfilActual = null;
        System.out.println("🔄 [SIMULACIÓN] Desactivada - volviendo a modo real");
    }

    /**
     * Verifica si la simulación está activa.
     *
     * @return true si la simulación está activa
     */
    public static boolean estaActiva() {
        return activa;
    }

    /**
     * Obtiene el nombre del perfil actualmente activo.
     *
     * @return Nombre del perfil activo, o "Ninguno" si no hay simulación activa
     */
    public static String getNombrePerfilActual() {
        if (perfilActual != null && perfiles.containsKey(perfilActual)) {
            return perfiles.get(perfilActual).nombre;
        }
        return "Ninguno";
    }

    /**
     * Obtiene información simulada del sistema como objeto InfoPC.
     *
     * @return InfoPC con datos del perfil simulado
     */
    public static InventarioFXBase.InfoPC obtenerInfoSimulada() {
        InventarioFXBase.InfoPC info = new InventarioFXBase.InfoPC();

        if (perfilActual == null || !perfiles.containsKey(perfilActual)) {
            return info;
        }

        PerfilSimulacion p = perfiles.get(perfilActual);
        info.fecha = new Date();
        info.userName = p.userName;
        info.hostname = p.hostname;
        info.sistema = p.sistema;
        info.bios = p.bios;
        info.cpu = p.cpu;
        info.gpu = p.gpu;
        info.ram = p.ram;
        info.discos = p.discos;
        info.numeroDiscos = p.numeroDiscos;
        info.ip = p.ip;
        info.modeloEquipo = p.modeloEquipo;
        info.manufacturer = p.manufacturer;
        info.deviceType = p.deviceType;
        info.memoriaInstalada = p.memoriaInstalada;
        info.tarjetaRed = p.tarjetaRed;
        info.installedApps = p.installedApps;
        info.idGrupo = "SIM-" + p.id.toUpperCase();

        return info;
    }

    /**
     * Lista los IDs de todos los perfiles disponibles.
     *
     * @return String con los IDs separados por coma
     */
    public static String listarPerfiles() {
        return String.join(", ", perfiles.keySet());
    }

    /**
     * Obtiene los nombres descriptivos de todos los perfiles.
     *
     * @return Lista de nombres de perfiles
     */
    public static List<String> getNombresPerfiles() {
        List<String> nombres = new ArrayList<>();
        perfiles.values().forEach(p -> nombres.add(p.id + " → " + p.nombre));
        return nombres;
    }
}
