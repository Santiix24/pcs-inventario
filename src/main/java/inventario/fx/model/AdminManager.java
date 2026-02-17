package inventario.fx.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;
import inventario.fx.database.repository.ConfigRepository;
import inventario.fx.database.repository.ProyectoRepository;
import inventario.fx.database.repository.LogAccesoRepository;
import inventario.fx.database.DatabaseManager;
import inventario.fx.util.AppLogger;
import inventario.fx.security.SecurityManager;
import inventario.fx.config.PortablePaths;

/**
 * Gestor centralizado del sistema de administración y gestión de proyectos.
 * 
 * <p>Esta clase proporciona funcionalidades completas para la gestión del sistema de inventario,
 * incluyendo autenticación, gestión de proyectos, configuración empresarial y auditoría.
 * 
 * <p><b>Funcionalidades principales:</b>
 * <ul>
 *   <li>Autenticación de administradores con contraseña personalizable</li>
 *   <li>Gestión completa de proyectos (CRUD)</li>
 *   <li>Configuración de datos empresariales</li>
 *   <li>Gestión de rutas de trabajo del sistema</li>
 *   <li>Sistema de logs de acceso y auditoría</li>
 *   <li>Estadísticas del sistema en tiempo real</li>
 *   <li>Cifrado de archivos Excel con clave personalizable</li>
 * </ul>
 * 
 * <p><b>Persistencia de datos:</b>
 * Los datos se almacenan en formato JSON en los siguientes archivos:
 * <ul>
 *   <li>{@code proyectos.json} - Lista de proyectos creados</li>
 *   <li>{@code config_admin.json} - Configuración empresarial y rutas</li>
 *   <li>{@code logs_acceso.json} - Registro de accesos y operaciones</li>
 * </ul>
 * 
 * <p><b>Seguridad:</b>
 * <ul>
 *   <li>Contraseña de administrador almacenada en Preferences (cifrada por el SO)</li>
 *   <li>Clave de cifrado personalizable para archivos Excel</li>
 *   <li>Registro de todas las operaciones administrativas</li>
 * </ul>
 * 
 * <p><b>Ejemplo de uso:</b>
 * <pre>{@code
 * // Autenticación
 * LoginResult result = AdminManager.login(miContrasena);
 * if (result.isExito()) {
 *     System.out.println("Acceso concedido");
 * }
 * 
 * // Crear proyecto
 * Proyecto proyecto = new Proyecto("Mi Proyecto", "#3B82F6");
 * AdminManager.agregarProyecto(proyecto);
 * 
 * // Obtener estadísticas
 * EstadisticasSistema stats = AdminManager.getEstadisticasSistema();
 * System.out.println("Total proyectos: " + stats.totalProyectos);
 * }</pre>
 * 
 * @author SELCOMP
 * @version 2.0
 * @since 2024
 * @see Proyecto
 * @see ConfiguracionEmpresa
 * @see RutasTrabajo
 * @see LogAcceso
 * @see EstadisticasSistema
 */
public class AdminManager {
    
    private static final String PREF_ADMIN_PASSWORD = "admin_password";
    private static final String PREF_ENCRYPTION_KEY = "encryption_key";
    /** Fuente de intentos de login para protección anti-brute-force. */
    private static final String LOGIN_SOURCE = "admin_login";
    private static final Preferences prefs = Preferences.userNodeForPackage(AdminManager.class);
    private static ConfigRepository configRepo = new ConfigRepository();
    private static ProyectoRepository proyectoRepo;
    private static LogAccesoRepository logRepo;
    private static inventario.fx.config.BackupManager backupManager;
    private static long ultimoBackup = 0;
    private static final long BACKUP_INTERVAL = 3600000; // 1 hora en milisegundos
    
    private static boolean adminMode = false;
    private static List<Proyecto> proyectos = new ArrayList<>();
    private static List<AdminListener> listeners = new ArrayList<>();
    private static ConfiguracionEmpresa configEmpresa = new ConfiguracionEmpresa();
    private static RutasTrabajo rutasTrabajo = new RutasTrabajo();
    private static List<LogAcceso> logsAcceso = new ArrayList<>();
    
    // Colores disponibles para proyectos (paleta ampliada)
    public static final String[] COLORES_PROYECTO = {
        // Fila 1 - Colores principales
        "#3B82F6", // Azul
        "#10B981", // Verde
        "#EF4444", // Rojo
        "#8B5CF6", // Violeta
        "#14B8A6", // Teal
        "#F59E0B", // Ámbar
        "#6366F1", // Índigo
        "#EC4899", // Rosa
        "#06B6D4", // Cyan
        "#84CC16", // Lima
        // Fila 2 - Colores adicionales
        "#F97316", // Naranja
        "#A855F7", // Púrpura
        "#22C55E", // Verde esmeralda
        "#0EA5E9", // Celeste
        "#E11D48", // Rojo rosa
        "#FACC15", // Amarillo
        "#64748B", // Gris azulado
        "#059669", // Verde oscuro
        "#7C3AED", // Violeta intenso
        "#DC2626"  // Rojo intenso
    };
    
    static {
        cargarProyectos();
        
        // Inicializar sistema de respaldos
        try {
            backupManager = new inventario.fx.config.BackupManager();
            System.out.println("[AdminManager] 💾 Sistema de respaldos inicializado");
        } catch (Exception e) {
            System.err.println("[AdminManager] ⚠️ No se pudo inicializar sistema de respaldos: " + e.getMessage());
        }
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // CLASE PROYECTO
    // ════════════════════════════════════════════════════════════════════════════
    
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class Proyecto {
        private String id;
        private String nombre;
        private String descripcion;
        private String color;
        private String fechaCreacion;
        private boolean activo;
        private String imagenPath;  // Ruta de imagen/logo específico del proyecto
        
        public Proyecto() {
            this.id = UUID.randomUUID().toString().substring(0, 8);
            this.fechaCreacion = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            this.activo = true;
        }
        
        public Proyecto(String nombre, String descripcion, String color) {
            this();
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.color = color;
        }
        
        // Getters y Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public String getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
        
        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
        
        public String getImagenPath() { return imagenPath != null ? imagenPath : ""; }
        public void setImagenPath(String imagenPath) { this.imagenPath = imagenPath; }
        
        /**
         * Crea una copia profunda del proyecto.
         * Útil para editar sin modificar el original hasta confirmar.
         */
        public Proyecto copy() {
            Proyecto copia = new Proyecto();
            copia.id = this.id;
            copia.nombre = this.nombre;
            copia.descripcion = this.descripcion;
            copia.color = this.color;
            copia.fechaCreacion = this.fechaCreacion;
            copia.activo = this.activo;
            copia.imagenPath = this.imagenPath;
            return copia;
        }
        
        /**
         * Copia los valores de otro proyecto a este.
         */
        public void copyFrom(Proyecto otro) {
            this.nombre = otro.nombre;
            this.descripcion = otro.descripcion;
            this.color = otro.color;
            this.imagenPath = otro.imagenPath;
            // NO copiar: id, fechaCreacion, activo
        }
        
        @Override
        public String toString() {
            return nombre;
        }
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // INTERFACE LISTENER
    // ════════════════════════════════════════════════════════════════════════════
    
    public interface AdminListener {
        void onAdminModeChanged(boolean isAdmin);
        void onProjectsChanged();
    }
    
    public static void addListener(AdminListener listener) {
        listeners.add(listener);
    }
    
    public static void removeListener(AdminListener listener) {
        listeners.remove(listener);
    }
    
    private static void notifyAdminModeChanged() {
        for (AdminListener listener : listeners) {
            listener.onAdminModeChanged(adminMode);
        }
    }
    
    private static void notifyProjectsChanged() {
        for (AdminListener listener : listeners) {
            listener.onProjectsChanged();
        }
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // AUTENTICACIÓN SEGURA (PBKDF2 + Protección anti-brute-force)
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Verifica si es la primera ejecución y el administrador debe configurar su contraseña.
     * @return true si no hay contraseña almacenada y se necesita configuración inicial
     */
    public static boolean needsInitialPasswordSetup() {
        return prefs.get(PREF_ADMIN_PASSWORD, null) == null;
    }
    
    /**
     * Configura la contraseña de administrador por primera vez.
     * Solo funciona si no hay contraseña almacenada previamente.
     * 
     * @param password  Contraseña a establecer
     * @return Resultado de validación con éxito y mensaje
     */
    public static SecurityManager.ResultadoValidacion setupInitialPassword(String password) {
        if (!needsInitialPasswordSetup()) {
            return new SecurityManager.ResultadoValidacion(false, 
                "La contraseña ya fue configurada. Use 'Cambiar Contraseña' para modificarla.", 0);
        }
        
        SecurityManager.ResultadoValidacion validacion = SecurityManager.validarFortaleza(password);
        if (!validacion.isValida()) {
            return validacion;
        }
        
        String hash = SecurityManager.hashPassword(password);
        prefs.put(PREF_ADMIN_PASSWORD, hash);
        System.out.println("[AdminManager] Contraseña inicial configurada con PBKDF2");
        registrarLog("Setup inicial", "Contraseña de administrador configurada por primera vez");
        
        return new SecurityManager.ResultadoValidacion(true, 
            "Contraseña configurada exitosamente", validacion.getFortaleza());
    }
    
    /**
     * Obtiene el hash almacenado de la contraseña de administrador.
     * Si aún está en texto plano (legacy), la migra automáticamente a PBKDF2.
     * @return Hash almacenado o null si no hay contraseña configurada
     */
    private static String getAdminPasswordHash() {
        String stored = prefs.get(PREF_ADMIN_PASSWORD, null);
        
        if (stored == null) {
            // No hay contraseña configurada — requiere setup inicial
            return null;
        }
        
        // Migración automática: si está en texto plano, hashearla
        if (!SecurityManager.isHashModerno(stored)) {
            String hash = SecurityManager.hashPassword(stored);
            prefs.put(PREF_ADMIN_PASSWORD, hash);
            System.out.println("[AdminManager] Contraseña legacy migrada a PBKDF2");
            return hash;
        }
        
        return stored;
    }
    
    /**
     * Intenta autenticar al administrador con protección anti-brute-force.
     * 
     * @param password Contraseña en texto plano
     * @return Resultado del intento de login
     */
    public static LoginResult login(String password) {
        // Verificar si la cuenta está bloqueada por brute force
        if (SecurityManager.estaBloqueado(LOGIN_SOURCE)) {
            long segundosRestantes = SecurityManager.getTiempoBloqueoRestante(LOGIN_SOURCE);
            registrarLog("Login bloqueado", "Cuenta bloqueada por intentos fallidos — " + segundosRestantes + "s restantes");
            return new LoginResult(false, 
                "Cuenta bloqueada. Intenta de nuevo en " + segundosRestantes + " segundos.",
                0, segundosRestantes);
        }

        if (password == null || password.isEmpty()) {
            return new LoginResult(false, "La contraseña no puede estar vacía",
                SecurityManager.getIntentosRestantes(LOGIN_SOURCE), 0);
        }

        String storedHash = getAdminPasswordHash();
        
        if (storedHash == null) {
            // No hay contraseña configurada — el usuario debe completar el setup inicial
            return new LoginResult(false, 
                "No hay contraseña configurada. Configure una contraseña para continuar.",
                SecurityManager.getIntentosRestantes(LOGIN_SOURCE), 0);
        }
        
        if (SecurityManager.verifyPassword(password, storedHash)) {
            // Login exitoso
            adminMode = true;
            SecurityManager.reiniciarIntentos(LOGIN_SOURCE);
            SecurityManager.iniciarSesion();
            notifyAdminModeChanged();
            registrarLog("Login admin", "Acceso administrativo concedido");
            return new LoginResult(true, "Acceso concedido", MAX_LOGIN_ATTEMPTS, 0);
        } else {
            // Login fallido
            SecurityManager.registrarIntentoFallido(LOGIN_SOURCE);
            int restantes = SecurityManager.getIntentosRestantes(LOGIN_SOURCE);
            registrarLog("Login fallido", "Intento de acceso administrativo fallido — intentos restantes: " + restantes);
            
            String mensaje = restantes > 0 
                ? "Contraseña incorrecta. " + restantes + " intentos restantes."
                : "Cuenta bloqueada por exceder intentos.";
            
            long bloqueo = SecurityManager.getTiempoBloqueoRestante(LOGIN_SOURCE);
            return new LoginResult(false, mensaje, restantes, bloqueo);
        }
    }
    
    /** Máximo de intentos de login (constante para referencia en UI). */
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    
    /**
     * Resultado de un intento de login con información detallada.
     */
    public static class LoginResult {
        private final boolean exito;
        private final String mensaje;
        private final int intentosRestantes;
        private final long segundosBloqueo;
        
        public LoginResult(boolean exito, String mensaje, int intentosRestantes, long segundosBloqueo) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.intentosRestantes = intentosRestantes;
            this.segundosBloqueo = segundosBloqueo;
        }
        
        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public int getIntentosRestantes() { return intentosRestantes; }
        public long getSegundosBloqueo() { return segundosBloqueo; }
    }
    
    /**
     * Cierra sesión de administrador y limpia la sesión de seguridad.
     */
    public static void logout() {
        adminMode = false;
        SecurityManager.cerrarSesion();
        notifyAdminModeChanged();
        registrarLog("Logout admin", "Sesión administrativa cerrada");
    }
    
    /**
     * Verifica si el administrador está en modo admin y la sesión no ha expirado.
     * Si la sesión expiró, cierra automáticamente.
     */
    public static boolean isAdminMode() {
        if (adminMode && SecurityManager.sesionExpirada()) {
            adminMode = false;
            notifyAdminModeChanged();
            registrarLog("Sesión expirada", "Sesión administrativa cerrada por inactividad");
            return false;
        }
        // Registrar actividad para resetear timeout
        if (adminMode) {
            SecurityManager.registrarActividad();
        }
        return adminMode;
    }
    
    /**
     * Obtiene los minutos restantes de la sesión admin activa.
     * @return Minutos restantes o 0 si no hay sesión
     */
    public static long getMinutosSesionRestantes() {
        return SecurityManager.getMinutosRestantesSesion();
    }
    
    /**
     * Cambia la contraseña de administrador con validación de fortaleza.
     * Requiere la contraseña actual para verificar identidad.
     * La contraseña nueva se almacena hasheada con PBKDF2.
     * 
     * @param contrasenaActual Contraseña actual para verificación
     * @param contrasenaNueva  Nueva contraseña en texto plano
     * @return Resultado con éxito y mensaje descriptivo
     */
    public static SecurityManager.ResultadoValidacion cambiarContrasena(String contrasenaActual, String contrasenaNueva) {
        // Verificar contraseña actual
        String storedHash = getAdminPasswordHash();
        if (storedHash == null || !SecurityManager.verifyPassword(contrasenaActual, storedHash)) {
            registrarLog("Cambio contraseña fallido", "Contraseña actual incorrecta");
            return new SecurityManager.ResultadoValidacion(false, "La contraseña actual es incorrecta", 0);
        }
        
        // Validar fortaleza de la nueva contraseña
        SecurityManager.ResultadoValidacion validacion = SecurityManager.validarFortaleza(contrasenaNueva);
        if (!validacion.isValida()) {
            return validacion;
        }
        
        // Hashear y guardar nueva contraseña
        String nuevoHash = SecurityManager.hashPassword(contrasenaNueva);
        prefs.put(PREF_ADMIN_PASSWORD, nuevoHash);
        registrarLog("Cambio contraseña", "Contraseña de administrador actualizada exitosamente");
        
        return new SecurityManager.ResultadoValidacion(true, "Contraseña actualizada exitosamente", validacion.getFortaleza());
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // CONTRASEÑA DE CIFRADO EXCEL
    // ════════════════════════════════════════════════════════════════════════════
    
    private static final String PREF_EXCEL_PASSWORD = "excel_password";
    
    /**
     * Obtiene la contraseña actual para cifrar archivos Excel.
     * Si no existe, genera una contraseña aleatoria segura y la almacena.
     */
    public static String getExcelPassword() {
        // Buscar primero en SQLite encriptado, luego en Preferences
        return configRepo.obtener("excel.password")
            .orElseGet(() -> {
                // Si no está en SQLite, intentar migrar desde Preferences
                String oldPassword = prefs.get(PREF_EXCEL_PASSWORD, null);
                if (oldPassword != null && !oldPassword.isEmpty()) {
                    configRepo.guardar("excel.password", oldPassword, "seguridad", 
                        "Contraseña de cifrado de Excel (migrada)", true);
                    // Limpiar de Preferences después de migrar
                    prefs.remove(PREF_EXCEL_PASSWORD);
                    return oldPassword;
                }
                // Primera ejecución: generar contraseña aleatoria segura
                String generada = generarPasswordSegura(16);
                configRepo.guardar("excel.password", generada, "seguridad",
                    "Contraseña de cifrado de Excel (generada automáticamente)", true);
                System.out.println("[AdminManager] Contraseña de Excel generada automáticamente");
                return generada;
            });
    }
    
    /**
     * Genera una contraseña aleatoria segura con la longitud especificada.
     * Incluye mayúsculas, minúsculas, números y caracteres especiales.
     */
    private static String generarPasswordSegura(int longitud) {
        String mayusculas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String minusculas = "abcdefghijklmnopqrstuvwxyz";
        String numeros = "0123456789";
        String especiales = "!@#$%^&*()-_=+";
        String todos = mayusculas + minusculas + numeros + especiales;
        
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(longitud);
        
        // Asegurar al menos un carácter de cada tipo
        sb.append(mayusculas.charAt(random.nextInt(mayusculas.length())));
        sb.append(minusculas.charAt(random.nextInt(minusculas.length())));
        sb.append(numeros.charAt(random.nextInt(numeros.length())));
        sb.append(especiales.charAt(random.nextInt(especiales.length())));
        
        // Rellenar el resto
        for (int i = 4; i < longitud; i++) {
            sb.append(todos.charAt(random.nextInt(todos.length())));
        }
        
        // Mezclar para que los primeros 4 no sean predecibles
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars);
    }
    
    /**
     * Verifica si la contraseña de Excel ha sido configurada
     */
    public static boolean tieneExcelPasswordPersonalizada() {
        return configRepo.obtener("excel.password").isPresent();
    }
    
    /**
     * Cambia la contraseña de cifrado de Excel
     */
    public static boolean cambiarExcelPassword(String passwordActual, String passwordNueva) {
        String actual = getExcelPassword();
        if (actual.equals(passwordActual)) {
            configRepo.guardar("excel.password", passwordNueva, "seguridad", 
                "Contraseña de cifrado de Excel", true);
            registrarLog("Seguridad", "Contraseña de Excel actualizada");
            return true;
        }
        return false;
    }
    
    /**
     * Establece la contraseña de Excel validando que sea admin.
     * @deprecated Usar {@link #cambiarExcelPassword(String, String)} con validación.
     */
    @Deprecated
    public static void setExcelPassword(String passwordNueva) {
        if (!isAdminMode()) {
            System.err.println("[AdminManager] Intento de cambiar contraseña Excel sin modo admin");
            registrarLog("Seguridad", "Intento no autorizado de cambiar contraseña Excel");
            return;
        }
        configRepo.guardar("excel.password", passwordNueva, "seguridad",
            "Contraseña de cifrado de Excel", true);
        registrarLog("Seguridad", "Contraseña de Excel actualizada (método legacy)");
    }
    
    /**
     * Genera una nueva contraseña aleatoria para Excel.
     * Requiere modo administrador activo.
     * 
     * @return La nueva contraseña generada, o null si no tiene permisos
     */
    public static String regenerarExcelPassword() {
        if (!isAdminMode()) {
            System.err.println("[AdminManager] Intento no autorizado de regenerar contraseña Excel");
            return null;
        }
        String nueva = generarPasswordSegura(16);
        configRepo.guardar("excel.password", nueva, "seguridad",
            "Contraseña de cifrado de Excel (regenerada)", true);
        registrarLog("Seguridad", "Contraseña de Excel regenerada");
        return nueva;
    }
    
    /**
     * Recarga los proyectos desde el archivo JSON
     */
    public static void recargarProyectos() {
        cargarProyectos();
        notifyProjectsChanged();
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // GESTIÓN DE PROYECTOS
    // ════════════════════════════════════════════════════════════════════════════
    
    public static List<Proyecto> getProyectos() {
        return new ArrayList<>(proyectos);
    }
    
    public static List<Proyecto> getProyectosActivos() {
        return proyectos.stream()
                .filter(Proyecto::isActivo)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public static String[] getNombresProyectos() {
        return getProyectosActivos().stream()
                .map(p -> (getProyectosActivos().indexOf(p) + 1) + ". " + p.getNombre())
                .toArray(String[]::new);
    }
    
    public static Proyecto getProyectoPorIndice(int index) {
        List<Proyecto> activos = getProyectosActivos();
        if (index >= 0 && index < activos.size()) {
            return activos.get(index);
        }
        return null;
    }
    
    public static Proyecto getProyectoPorId(String id) {
        return proyectos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    public static int getIndiceProyecto(Proyecto proyecto) {
        return getProyectosActivos().indexOf(proyecto);
    }
    
    public static boolean agregarProyecto(Proyecto proyecto) {
        return agregarProyecto(proyecto, false);
    }
    
    /**
     * Agrega un proyecto con opción de detectar duplicados.
     * @param proyecto El proyecto a agregar
     * @param autoRenombreSiDuplicado Si es true, agrega sufijo numérico si el nombre ya existe
     */
    public static boolean agregarProyecto(Proyecto proyecto, boolean autoRenombreSiDuplicado) {
        try {
            // Verificar si ya existe un proyecto con el mismo nombre
            if (autoRenombreSiDuplicado) {
                String nombreOriginal = proyecto.getNombre();
                String nombreCandidato = nombreOriginal;
                int sufijo = 1;
                
                // Buscar nombre único agregando sufijo numérico
                while (true) {
                    final String nombreFinalTemp = nombreCandidato;
                    boolean existe = proyectos.stream().anyMatch(p -> p.getNombre().equals(nombreFinalTemp));
                    if (!existe) {
                        break;
                    }
                    nombreCandidato = nombreOriginal + " (" + sufijo + ")";
                    sufijo++;
                }
                
                if (!nombreCandidato.equals(nombreOriginal)) {
                    System.out.println("[AdminManager] ⚠️ Proyecto duplicado detectado: " + nombreOriginal);
                    System.out.println("[AdminManager] 🔄 Renombrado a: " + nombreCandidato);
                    proyecto.setNombre(nombreCandidato);
                }
            }
            
            // Guardar en SQLite
            boolean guardado = proyectoRepo.save(proyecto);
            if (guardado) {
                // Agregar a la lista en memoria
                proyectos.add(proyecto);
                notifyProjectsChanged();
                System.out.println("[AdminManager] ✅ Proyecto agregado en SQLite: " + proyecto.getNombre() + 
                                 " (Total: " + proyectos.size() + " proyectos)");
                
                // Crear backup automático si ha pasado suficiente tiempo
                crearBackupAutomatico("Proyecto agregado: " + proyecto.getNombre());
                
                return true;
            } else {
                System.err.println("[AdminManager] ❌ Error guardando proyecto en SQLite");
                return false;
            }
        } catch (Exception e) {
            AppLogger.getLogger(AdminManager.class).error("Error al agregar proyecto: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Actualiza un proyecto preservando todos sus datos.
     * Esta versión recibe el nombre anterior para detectar cambios correctamente.
     * IMPORTANTE: No recarga la lista desde la BD para mantener el orden original.
     * 
     * @param proyecto El proyecto con los nuevos datos
     * @param nombreAnterior El nombre que tenía el proyecto antes de editarlo
     * @return true si se actualizó correctamente
     */
    public static boolean actualizarProyectoConNombreAnterior(Proyecto proyecto, String nombreAnterior) {
        try {
            boolean cambioNombre = nombreAnterior != null && 
                                   proyecto.getNombre() != null && 
                                   !nombreAnterior.trim().equals(proyecto.getNombre().trim());
            
            // Obtener el índice del proyecto ANTES de cualquier operación
            int indiceProyecto = -1;
            Proyecto proyectoEnLista = null;
            for (int i = 0; i < proyectos.size(); i++) {
                if (proyectos.get(i).getId().equals(proyecto.getId())) {
                    indiceProyecto = i;
                    proyectoEnLista = proyectos.get(i);
                    break;
                }
            }
            
            if (indiceProyecto < 0) {
                System.err.println("[AdminManager] ❌ Proyecto no encontrado en la lista: " + proyecto.getId());
                return false;
            }
            
            System.out.println("[AdminManager] 📝 Actualizando proyecto en posición " + (indiceProyecto + 1) + 
                              ": " + nombreAnterior + " → " + proyecto.getNombre());
            
            // Si cambió el nombre, renombrar archivo Excel ANTES de actualizar la BD
            if (cambioNombre) {
                try {
                    System.out.println("[AdminManager] 🔄 Renombrando Excel para proyecto en índice " + (indiceProyecto + 1));
                    System.out.println("[AdminManager]    De: '" + nombreAnterior + "'");
                    System.out.println("[AdminManager]    A: '" + proyecto.getNombre() + "'");
                    renombrarArchivoExcelConIndice(nombreAnterior, proyecto.getNombre(), indiceProyecto + 1);
                } catch (Exception ex) {
                    AppLogger.getLogger(AdminManager.class).error("Error al renombrar Excel: " + ex.getMessage(), ex);
                    // Continuar con la actualización aunque falle el renombrado del Excel
                }
            } else {
                System.out.println("[AdminManager] ℹ️ No hubo cambio de nombre, saltando renombre de Excel");
            }
            
            // Actualizar en SQLite
            boolean actualizado = proyectoRepo.save(proyecto); // save hace UPSERT
            if (actualizado) {
                // Actualizar en la lista en memoria EN LA MISMA POSICIÓN
                // Reemplazar el proyecto completo en la lista
                proyectos.set(indiceProyecto, proyecto);
                
                notifyProjectsChanged();
                System.out.println("[AdminManager] ✅ Proyecto actualizado en SQLite: " + proyecto.getNombre() +
                    (cambioNombre ? " (nombre cambiado de: " + nombreAnterior + ")" : ""));
                
                // Crear backup automático si ha pasado suficiente tiempo
                crearBackupAutomatico("Proyecto actualizado: " + proyecto.getNombre());
                
                return true;
            } else {
                System.err.println("[AdminManager] ❌ Error: proyectoRepo.save() retornó false para proyecto: " + proyecto.getId());
            }
            return false;
        } catch (Exception e) {
            AppLogger.getLogger(AdminManager.class).error("Error al actualizar proyecto: " + e.getMessage(), e);
            return false;
        }
    }
    
    public static boolean actualizarProyecto(Proyecto proyecto) {
        try {
            // Obtener proyecto anterior para comparar nombres
            Proyecto proyectoAnterior = getProyectoPorId(proyecto.getId());
            String nombreAnterior = proyectoAnterior != null ? proyectoAnterior.getNombre() : null;
            boolean cambioNombre = nombreAnterior != null && !nombreAnterior.equals(proyecto.getNombre());
            
            // Actualizar en SQLite
            boolean actualizado = proyectoRepo.save(proyecto); // save hace UPSERT
            if (actualizado) {
                // Si cambió el nombre, renombrar archivo Excel asociado
                if (cambioNombre) {
                    try {
                        renombrarArchivoExcel(proyectoAnterior, proyecto);
                    } catch (Exception ex) {
                        System.err.println("[AdminManager] ⚠ Error al renombrar Excel: " + ex.getMessage());
                        // No fallar la actualización por esto
                    }
                }
                
                // Actualizar en la lista en memoria
                for (int i = 0; i < proyectos.size(); i++) {
                    if (proyectos.get(i).getId().equals(proyecto.getId())) {
                        proyectos.set(i, proyecto);
                        break;
                    }
                }
                notifyProjectsChanged();
                System.out.println("[AdminManager] ✅ Proyecto actualizado en SQLite: " + proyecto.getNombre());
                
                // Crear backup automático si ha pasado suficiente tiempo
                crearBackupAutomatico("Proyecto actualizado: " + proyecto.getNombre());
                
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("[AdminManager] ❌ Error al actualizar proyecto: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Renombra el archivo Excel cuando se cambia el nombre del proyecto.
     * Versión mejorada que recibe los nombres directamente para evitar problemas de referencia.
     * NO CREA nuevo archivo - solo RENOMBRA el existente para preservar todos los datos.
     * 
     * @param nombreAnterior Nombre original del proyecto
     * @param nombreNuevo Nuevo nombre del proyecto  
     * @param indiceProyecto Índice del proyecto (1-based)
     */
    private static void renombrarArchivoExcelConIndice(String nombreAnterior, String nombreNuevo, int indiceProyecto) throws Exception {
        if (nombreAnterior == null || nombreNuevo == null || nombreAnterior.equals(nombreNuevo)) {
            System.out.println("[AdminManager] ℹ️ No hay cambio de nombre, saltando renombrado");
            return; // No hay cambio de nombre
        }
        
        // Usar el directorio de trabajo actual (donde se ejecuta la app)
        java.nio.file.Path directorioTrabajo = inventario.fx.config.PortablePaths.getProyectosDir();
        
        System.out.println("[AdminManager] 📂 Buscando Excel en: " + directorioTrabajo.toAbsolutePath());
        System.out.println("[AdminManager] 📝 Renombrando: '" + nombreAnterior + "' → '" + nombreNuevo + "' (índice: " + indiceProyecto + ")");
        
        // Listar todos los archivos Excel que empiezan con el índice
        System.out.println("[AdminManager] � Archivos en directorio:");
        try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(directorioTrabajo)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".xlsx"))
                  .filter(p -> !p.getFileName().toString().startsWith("~$"))
                  .forEach(p -> System.out.println("    - " + p.getFileName()));
        } catch (Exception e) {
            System.err.println("[AdminManager] ⚠️ Error listando archivos: " + e.getMessage());
        }
        
        java.nio.file.Path archivoExistente = null;
        
        // Buscar el archivo por patrón primero (más flexible)
        System.out.println("[AdminManager] 🔍 Buscando archivos con patrón: Inventario_" + indiceProyecto + " -*.xlsx");
        try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(directorioTrabajo)) {
            archivoExistente = stream
                .filter(p -> p.getFileName().toString().startsWith("Inventario_" + indiceProyecto + " -"))
                .filter(p -> p.getFileName().toString().endsWith(".xlsx"))
                .filter(p -> !p.getFileName().toString().startsWith("~$")) // Ignorar archivos temporales
                .findFirst()
                .orElse(null);
            if (archivoExistente != null) {
                System.out.println("[AdminManager] ✓ Encontrado archivo Excel: " + archivoExistente.getFileName());
            }
        } catch (Exception e) {
            System.err.println("[AdminManager] ⚠️ Error buscando por patrón: " + e.getMessage());
        }
        
        if (archivoExistente != null && java.nio.file.Files.exists(archivoExistente)) {
            // Obtener proyecto para acceder a la descripción
            Proyecto proyectoActual = null;
            for (Proyecto p : proyectos) {
                int idx = proyectos.indexOf(p);
                if (idx == (indiceProyecto - 1)) {
                    proyectoActual = p;
                    break;
                }
            }
            
            // Crear nuevo nombre usando la MISMA lógica que obtenerRutaExcel
            String nombreLimpioNuevo = nombreNuevo
                .replaceAll("[\\\\/:*?\"<>|]", "")
                .replace(" ", "_")
                .trim();
            
            String nuevoNombreArchivo;
            if (proyectoActual != null && proyectoActual.getDescripcion() != null && !proyectoActual.getDescripcion().isEmpty()) {
                String descripcionFormateada = proyectoActual.getDescripcion()
                    .replaceAll("[\\\\/:*?\"<>|]", "")
                    .replace(" ", "_")
                    .trim();
                // Formato con descripción (igual que obtenerRutaExcel)
                nuevoNombreArchivo = "Inventario_" + indiceProyecto + " - " + nombreLimpioNuevo + "_" + descripcionFormateada + ".xlsx";
            } else {
                // Formato sin descripción
                nuevoNombreArchivo = "Inventario_" + indiceProyecto + " - " + nombreLimpioNuevo + ".xlsx";
            }
            
            java.nio.file.Path archivoNuevo = directorioTrabajo.resolve(nuevoNombreArchivo);
            
            System.out.println("[AdminManager] 📝 Renombrando:");
            System.out.println("    Desde: " + archivoExistente.getFileName());
            System.out.println("    Hacia: " + archivoNuevo.getFileName());
            System.out.println("    Con descripción: " + (proyectoActual != null && proyectoActual.getDescripcion() != null && !proyectoActual.getDescripcion().isEmpty()));
            
            // Renombrar archivo (preserva TODOS los datos)
            java.nio.file.Files.move(archivoExistente, archivoNuevo, 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            PortablePaths.protegerArchivo(archivoNuevo);
            
            System.out.println("[AdminManager] ✅ Excel renombrado exitosamente");
        } else {
            System.err.println("[AdminManager] ❌ No se encontró archivo Excel para renombrar");
            System.err.println("[AdminManager]    Proyecto: " + nombreAnterior + " (índice: " + indiceProyecto + ")");
            System.err.println("[AdminManager]    Carpeta: " + directorioTrabajo);
        }
    }
    
    /**
     * Renombra el archivo Excel cuando se cambia el nombre del proyecto.
     * NO CREA nuevo archivo - solo RENOMBRA el existente para preservar todos los datos.
     */
    private static void renombrarArchivoExcel(Proyecto proyectoAnterior, Proyecto proyectoNuevo) throws Exception {
        // Buscar archivo Excel con nombre antiguo
        String nombreLimpioAnterior = proyectoAnterior.getNombre()
            .replaceAll("[\\\\/:*?\"<>|]", "")
            .replace(" ", "_")
            .trim();
        
        // Buscar índice del proyecto
        int indiceProyecto = getProyectosActivos().indexOf(proyectoNuevo) + 1;
        
        // Posibles nombres del archivo anterior
        String[] posiblesNombresAnteriores = {
            "Inventario_" + indiceProyecto + " - " + nombreLimpioAnterior + ".xlsx",
            "Inventario_" + indiceProyecto + " - " + proyectoAnterior.getNombre().trim().replace(" ", "_") + ".xlsx"
        };
        
        java.nio.file.Path archivoExistente = null;
        java.nio.file.Path directorioTrabajo = inventario.fx.config.PortablePaths.getProyectosDir();
        
        // Buscar el archivo existente
        for (String nombreAnterior : posiblesNombresAnteriores) {
            java.nio.file.Path candidato = directorioTrabajo.resolve(nombreAnterior);
            if (java.nio.file.Files.exists(candidato)) {
                archivoExistente = candidato;
                break;
            }
        }
        
        // Si no se encontró, buscar por patrón
        if (archivoExistente == null) {
            try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(directorioTrabajo)) {
                archivoExistente = stream
                    .filter(p -> p.getFileName().toString().startsWith("Inventario_" + indiceProyecto + " -"))
                    .filter(p -> p.getFileName().toString().endsWith(".xlsx"))
                    .findFirst()
                    .orElse(null);
            }
        }
        
        if (archivoExistente != null && java.nio.file.Files.exists(archivoExistente)) {
            // Crear nuevo nombre
            String nombreLimpioNuevo = proyectoNuevo.getNombre()
                .replaceAll("[\\\\/:*?\"<>|]", "")
                .replace(" ", "_")
                .trim();
            
            String nuevoNombreArchivo = "Inventario_" + indiceProyecto + " - " + nombreLimpioNuevo + ".xlsx";
            java.nio.file.Path archivoNuevo = directorioTrabajo.resolve(nuevoNombreArchivo);
            
            // Renombrar archivo (preserva TODOS los datos)
            java.nio.file.Files.move(archivoExistente, archivoNuevo, 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            PortablePaths.protegerArchivo(archivoNuevo);
            
            System.out.println("[AdminManager] ✅ Excel renombrado: " + archivoExistente.getFileName() + 
                             " → " + archivoNuevo.getFileName());
        } else {
            System.out.println("[AdminManager] ℹ No se encontró archivo Excel para renombrar (puede no existir aún)");
        }
    }
    
    public static boolean eliminarProyecto(String id) {
        try {
            // 0. Obtener nombre del proyecto ANTES de eliminarlo (para buscar reportes)
            Proyecto proyecto = null;
            int indice = -1;
            List<Proyecto> activos = getProyectosActivos();
            for (int i = 0; i < activos.size(); i++) {
                if (activos.get(i).getId().equals(id)) {
                    proyecto = activos.get(i);
                    indice = i;
                    break;
                }
            }
            String nombreFormateado = (proyecto != null && indice >= 0) 
                ? (indice + 1) + ". " + proyecto.getNombre() 
                : null;
            
            // 1. Eliminar inventarios asociados de SQLite
            try (java.sql.Connection conn = DatabaseManager.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM inventarios WHERE proyecto_id = ?")) {
                stmt.setString(1, id);
                int deleted = stmt.executeUpdate();
                System.out.println("[AdminManager] ✅ " + deleted + " inventarios eliminados de SQLite para proyecto: " + id);
            } catch (Exception e) {
                System.err.println("[AdminManager] ⚠️ Error eliminando inventarios: " + e.getMessage());
            }
            
            // 2. Eliminar reportes de mantenimiento asociados al proyecto
            if (nombreFormateado != null) {
                try {
                    int reportesEliminados = inventario.fx.ui.panel.GestionReportesFX.eliminarReportesPorProyecto(nombreFormateado);
                    if (reportesEliminados > 0) {
                        System.out.println("[AdminManager] ✅ " + reportesEliminados + " reportes de mantenimiento eliminados para: " + nombreFormateado);
                    }
                    // También intentar con solo el nombre (sin índice) por si hay variaciones
                    if (proyecto != null) {
                        int extras = inventario.fx.ui.panel.GestionReportesFX.eliminarReportesPorProyecto(proyecto.getNombre());
                        if (extras > 0) {
                            System.out.println("[AdminManager] ✅ " + extras + " reportes adicionales eliminados (por nombre sin índice)");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[AdminManager] ⚠️ Error eliminando reportes del proyecto: " + e.getMessage());
                }
            }
            
            // 3. Eliminar borradores asociados al proyecto
            try {
                eliminarBorradoresProyecto(nombreFormateado != null ? nombreFormateado : (proyecto != null ? proyecto.getNombre() : ""));
            } catch (Exception e) {
                System.err.println("[AdminManager] ⚠️ Error eliminando borradores: " + e.getMessage());
            }
            
            // 4. Eliminar proyecto de SQLite (permanente)
            boolean eliminado = proyectoRepo.delete(id);
            if (eliminado) {
                // Eliminar de la lista en memoria
                proyectos.removeIf(p -> p.getId().equals(id));
                notifyProjectsChanged();
                System.out.println("[AdminManager] ✅ Proyecto eliminado completamente: " + id + 
                    (nombreFormateado != null ? " (" + nombreFormateado + ")" : ""));
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("[AdminManager] ❌ Error al eliminar proyecto: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Elimina borradores de formularios asociados a un proyecto.
     */
    private static void eliminarBorradoresProyecto(String nombreProyecto) {
        if (nombreProyecto == null || nombreProyecto.isEmpty()) return;
        
        try {
            java.nio.file.Path borradoresDir = inventario.fx.config.PortablePaths.getBorradoresDir();
            if (!java.nio.file.Files.exists(borradoresDir)) return;
            
            String nombreSinIndice = nombreProyecto.replaceFirst("^\\d+\\.\\s*", "");
            int eliminados = 0;
            
            try (var files = java.nio.file.Files.list(borradoresDir)) {
                for (java.nio.file.Path archivo : files.toList()) {
                    if (!java.nio.file.Files.isRegularFile(archivo)) continue;
                    if (!archivo.getFileName().toString().endsWith(".properties")) continue;
                    
                    try {
                        java.util.Properties props = new java.util.Properties();
                        try (var is = java.nio.file.Files.newInputStream(archivo)) {
                            props.load(is);
                        }
                        String borProyecto = props.getProperty("proyectoNombre", "");
                        String borSinIndice = borProyecto.replaceFirst("^\\d+\\.\\s*", "");
                        
                        if (borProyecto.equals(nombreProyecto) || borSinIndice.equals(nombreSinIndice)) {
                            java.nio.file.Files.delete(archivo);
                            eliminados++;
                        }
                    } catch (Exception e) {
                        // Ignorar archivos que no se pueden leer
                    }
                }
            }
            
            if (eliminados > 0) {
                System.out.println("[AdminManager] ✅ " + eliminados + " borradores eliminados del proyecto: " + nombreProyecto);
            }
        } catch (Exception e) {
            System.err.println("[AdminManager] Error eliminando borradores: " + e.getMessage());
        }
    }
    
    public static boolean eliminarProyectoPermanente(String id) {
        // Igual que eliminarProyecto - eliminación permanente
        return eliminarProyecto(id);
    }
    
    public static boolean restaurarProyecto(String id) {
        // Ya no hay proyectos inactivos, siempre se eliminan permanente
        return false;
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // PERSISTENCIA (SQLite)
    // ════════════════════════════════════════════════════════════════════════════
    
    private static void cargarProyectos() {
        try {
            // Inicializar base de datos
            DatabaseManager.initialize();
            proyectoRepo = new ProyectoRepository(null); // null porque DatabaseManager es estático
            logRepo = new LogAccesoRepository();
            
            // Cargar proyectos desde SQLite
            proyectos = proyectoRepo.findAll(false, false); // Todos los proyectos activos
            System.out.println("[AdminManager] ✅ Proyectos cargados desde SQLite: " + proyectos.size());
            
            // Cargar logs desde SQLite
            logsAcceso = logRepo.findAll(500); // Últimos 500 logs
            System.out.println("[AdminManager] ✅ Logs cargados desde SQLite: " + logsAcceso.size());
            
            // Si no hay proyectos, NO crear proyectos por defecto
            if (proyectos.isEmpty()) {
                System.out.println("[AdminManager] ℹ️ No hay proyectos. Los proyectos se crearán al importar archivos.");
            }
        } catch (Exception e) {
            AppLogger.getLogger(AdminManager.class).error("Error cargando datos desde SQLite: " + e.getMessage(), e);
            proyectos = new ArrayList<>();
            logsAcceso = new ArrayList<>();
        }
    }
    
    private static void inicializarProyectosPorDefecto() {
        // NO crear proyectos por defecto - los proyectos rotan y no son fijos
        proyectos = new ArrayList<>();
        System.out.println("[AdminManager] ℹ️ Sistema iniciado sin proyectos (los proyectos se crean al importar)");
    }
    

    
    // ════════════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════════════════════════
    
    public static String getColorProyecto(int index) {
        if (index >= 0 && index < COLORES_PROYECTO.length) {
            return COLORES_PROYECTO[index];
        }
        return COLORES_PROYECTO[0];
    }
    
    public static String getSiguienteColor() {
        int index = proyectos.size() % COLORES_PROYECTO.length;
        return COLORES_PROYECTO[index];
    }
    
    /**
     * Obtiene un color aleatorio de la paleta de colores de proyectos.
     * @return Color hexadecimal aleatorio
     */
    public static String getColorAleatorio() {
        int index = new java.util.Random().nextInt(COLORES_PROYECTO.length);
        return COLORES_PROYECTO[index];
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // COLORES RECIENTES
    // ════════════════════════════════════════════════════════════════════════════
    
    private static final int MAX_COLORES_RECIENTES = 5;
    private static final String PREF_COLORES_RECIENTES = "colores_recientes";
    
    /**
     * Obtiene los colores personalizados usados recientemente
     */
    public static java.util.List<String> getColoresRecientes() {
        String data = prefs.get(PREF_COLORES_RECIENTES, "");
        java.util.List<String> colores = new java.util.ArrayList<>();
        if (!data.isEmpty()) {
            for (String c : data.split(",")) {
                if (!c.trim().isEmpty()) {
                    colores.add(c.trim().toUpperCase());
                }
            }
        }
        return colores;
    }
    
    /**
     * Agrega un color a los recientes (si es personalizado, no predefinido)
     */
    public static void agregarColorReciente(String color) {
        if (color == null || color.isEmpty()) return;
        String colorUpper = color.toUpperCase();
        
        // Verificar si es un color predefinido
        for (String c : COLORES_PROYECTO) {
            if (c.equalsIgnoreCase(colorUpper)) {
                return; // No guardar colores predefinidos
            }
        }
        
        java.util.List<String> recientes = getColoresRecientes();
        
        // Si ya existe, removerlo para moverlo al inicio
        recientes.remove(colorUpper);
        
        // Agregar al inicio
        recientes.add(0, colorUpper);
        
        // Limitar cantidad
        while (recientes.size() > MAX_COLORES_RECIENTES) {
            recientes.remove(recientes.size() - 1);
        }
        
        // Guardar
        prefs.put(PREF_COLORES_RECIENTES, String.join(",", recientes));
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // CLASES DE CONFIGURACIÓN
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Configuración de datos de empresa
     */
    public static class ConfiguracionEmpresa {
        private String nombre = "Mi Empresa S.A.";
        private String email = "contacto@empresa.com";
        private String telefono = "+506 0000-0000";
        private String direccion = "San José, Costa Rica";
        private String logoPath = "";
        private String nit = "";
        private String sitioWeb = "";
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
        public String getLogoPath() { return logoPath; }
        public void setLogoPath(String logoPath) { this.logoPath = logoPath; }
        public String getNit() { return nit; }
        public void setNit(String nit) { this.nit = nit; }
        public String getSitioWeb() { return sitioWeb; }
        public void setSitioWeb(String sitioWeb) { this.sitioWeb = sitioWeb; }
    }
    
    /**
     * Configuración de rutas de trabajo
     */
    public static class RutasTrabajo {
        private String carpetaProyectos = inventario.fx.config.PortablePaths.getProyectosDir().toString();
        private String carpetaReportes = inventario.fx.config.PortablePaths.getDataRoot().resolve("reportes").toString();
        private String carpetaBackups = inventario.fx.config.PortablePaths.getBackupsDir().toString();
        private String carpetaExportaciones = inventario.fx.config.PortablePaths.getExportacionesDir().toString();
        private String carpetaDatabase = inventario.fx.config.PortablePaths.getDatabaseDir().toString();
        private String carpetaExcels = inventario.fx.config.PortablePaths.getProyectosDir().toString();
        
        public String getCarpetaProyectos() { return carpetaProyectos; }
        public void setCarpetaProyectos(String carpetaProyectos) { this.carpetaProyectos = carpetaProyectos; }
        public String getCarpetaReportes() { return carpetaReportes; }
        public void setCarpetaReportes(String carpetaReportes) { this.carpetaReportes = carpetaReportes; }
        public String getCarpetaBackups() { return carpetaBackups; }
        public void setCarpetaBackups(String carpetaBackups) { this.carpetaBackups = carpetaBackups; }
        public String getCarpetaExportaciones() { return carpetaExportaciones; }
        public void setCarpetaExportaciones(String carpetaExportaciones) { this.carpetaExportaciones = carpetaExportaciones; }
        public String getCarpetaDatabase() { return carpetaDatabase; }
        public void setCarpetaDatabase(String carpetaDatabase) { this.carpetaDatabase = carpetaDatabase; }
        public String getCarpetaExcels() { return carpetaExcels; }
        public void setCarpetaExcels(String carpetaExcels) { this.carpetaExcels = carpetaExcels; }
    }
    
    /**
     * Registro de acceso/actividad
     */
    public static class LogAcceso {
        private String fecha;
        private String accion;
        private String detalle;
        private String usuario;
        private String ip;
        
        public LogAcceso() {}
        
        public LogAcceso(String accion, String detalle) {
            this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.accion = accion;
            this.detalle = detalle;
            this.usuario = System.getProperty("user.name");
            this.ip = obtenerIP();
        }
        
        private String obtenerIP() {
            try {
                return java.net.InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                return "127.0.0.1";
            }
        }
        
        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
        public String getAccion() { return accion; }
        public void setAccion(String accion) { this.accion = accion; }
        public String getDetalle() { return detalle; }
        public void setDetalle(String detalle) { this.detalle = detalle; }
        public String getUsuario() { return usuario; }
        public void setUsuario(String usuario) { this.usuario = usuario; }
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE CONFIGURACIÓN DE EMPRESA
    // ════════════════════════════════════════════════════════════════════════════
    
    public static ConfiguracionEmpresa getConfigEmpresa() {
        return configEmpresa;
    }
    
    public static void guardarConfigEmpresa(ConfiguracionEmpresa config) {
        configEmpresa = config;
        guardarConfiguracion();
        registrarLog("Configuración empresa", "Datos de empresa actualizados");
    }
    
    public static RutasTrabajo getRutasTrabajo() {
        return rutasTrabajo;
    }
    
    public static void guardarRutasTrabajo(RutasTrabajo rutas) {
        rutasTrabajo = rutas;
        guardarConfiguracion();
        registrarLog("Configuración rutas", "Rutas de trabajo actualizadas");
    }
    
    private static void cargarConfiguracion() {
        // TODO: Migrar ConfiguracionEmpresa y RutasTrabajo a SQLite si es necesario
        // Por ahora se mantienen en memoria, se pueden agregar a tabla configuracion
        System.out.println("[AdminManager] ℹ️ Configuración en memoria (migrar a SQLite si es necesario)");
    }
    
    private static void guardarConfiguracion() {
        // TODO: Guardar en SQLite tabla configuracion o tabla empresa
        System.out.println("[AdminManager] ℹ️ Configuración guardada en memoria (migrar a SQLite si es necesario)");
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE LOGS DE ACCESO
    // ════════════════════════════════════════════════════════════════════════════
    
    /**
     * Registra un log de auditoría con sanitización de entradas.
     */
    public static void registrarLog(String accion, String detalle) {
        try {
            // Sanitizar entradas para prevenir log injection
            String accionSanitizada = SecurityManager.sanitizarParaLog(accion);
            String detalleSanitizado = SecurityManager.sanitizarParaLog(detalle);
            
            LogAcceso log = new LogAcceso(accionSanitizada, detalleSanitizado);
            
            // Guardar en SQLite
            if (logRepo != null) {
                logRepo.save(log);
            }
            
            // Mantener también en memoria para acceso rápido (caché)
            logsAcceso.add(0, log);
            while (logsAcceso.size() > 500) {
                logsAcceso.remove(logsAcceso.size() - 1);
            }
        } catch (Exception e) {
            System.err.println("[AdminManager] Error registrando log: " + e.getMessage());
        }
    }
    
    public static List<LogAcceso> getLogsAcceso() {
        // Recargar desde SQLite para tener datos frescos
        if (logRepo != null) {
            logsAcceso = logRepo.findAll(500);
        }
        return new ArrayList<>(logsAcceso);
    }
    
    public static List<LogAcceso> getLogsAcceso(int limite) {
        // Recargar desde SQLite
        if (logRepo != null) {
            return logRepo.findAll(limite);
        }
        return logsAcceso.subList(0, Math.min(limite, logsAcceso.size()));
    }
    
    public static void limpiarLogs() {
        logsAcceso.clear();
        if (logRepo != null) {
            logRepo.deleteAll();
        }
    }
    
    public static String exportarLogs() {
        StringBuilder sb = new StringBuilder();
        sb.append("REGISTRO DE ACCESOS - Sistema de Inventario\n");
        sb.append("Exportado: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        sb.append("═".repeat(80)).append("\n\n");
        
        for (LogAcceso log : logsAcceso) {
            sb.append(String.format("[%s] %s - %s\n", log.getFecha(), log.getAccion(), log.getDetalle()));
            sb.append(String.format("   Usuario: %s | IP: %s\n\n", log.getUsuario(), log.getIp()));
        }
        
        return sb.toString();
    }
    
    // Logs ahora se guardan directamente en SQLite mediante LogAccesoRepository
    
    // ════════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE CLAVE DE CIFRADO
    // ════════════════════════════════════════════════════════════════════════════
    
    public static boolean tieneClaveConfigrada() {
        String clave = prefs.get(PREF_ENCRYPTION_KEY, "");
        return !clave.isEmpty();
    }
    
    /**
     * Verifica una clave de cifrado usando hash PBKDF2 y comparación en tiempo constante.
     */
    public static boolean verificarClaveCifrado(String clave) {
        String storedHash = prefs.get(PREF_ENCRYPTION_KEY, "");
        if (storedHash.isEmpty() || clave == null) {
            return false;
        }
        // Soporte legacy (texto plano) + formato moderno (PBKDF2)
        if (SecurityManager.isHashModerno(storedHash)) {
            return SecurityManager.verifyPassword(clave, storedHash);
        }
        // Legacy: comparar en tiempo constante y migrar
        boolean coincide = java.security.MessageDigest.isEqual(
            storedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8),
            clave.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        if (coincide) {
            // Migrar a formato seguro
            prefs.put(PREF_ENCRYPTION_KEY, SecurityManager.hashPassword(clave));
            System.out.println("[AdminManager] Clave de cifrado migrada a PBKDF2");
        }
        return coincide;
    }
    
    /**
     * Guarda la clave de cifrado hasheada con PBKDF2. Requiere modo administrador.
     */
    public static void guardarClaveCifrado(String nuevaClave) {
        if (!isAdminMode()) {
            System.err.println("[AdminManager] Intento no autorizado de cambiar clave de cifrado");
            registrarLog("Seguridad", "Intento no autorizado de cambiar clave de cifrado");
            return;
        }
        // Almacenar hasheada con PBKDF2 — nunca en texto plano
        String hash = SecurityManager.hashPassword(nuevaClave);
        prefs.put(PREF_ENCRYPTION_KEY, hash);
        registrarLog("Seguridad", "Clave de cifrado actualizada");
    }
    
    /**
     * Verifica si hay una clave de cifrado configurada.
     * No expone la clave — solo indica si existe.
     */
    public static String getClaveCifrado() {
        if (!isAdminMode()) {
            System.err.println("[AdminManager] Acceso a clave de cifrado denegado — no es admin");
            return "";
        }
        SecurityManager.registrarActividad();
        // Devolver indicador de que existe, no la clave en sí
        String stored = prefs.get(PREF_ENCRYPTION_KEY, "");
        return stored.isEmpty() ? "" : "[CONFIGURADA]";
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // ESTADÍSTICAS DEL SISTEMA
    // ════════════════════════════════════════════════════════════════════════════
    
    public static class EstadisticasSistema {
        private long memoriaUsada;
        private long memoriaTotal;
        private long memoriaLibre;
        private double porcentajeMemoria;
        private long discoLibre;
        private long discoTotal;
        private double porcentajeDisco;
        private int procesadores;
        private String so;
        private String arquitectura;
        private String versionJava;
        private String versionJavaFX;
        private String usuario;
        private String directorioTrabajo;
        private String tiempoEjecucion;
        
        // Getters
        public long getMemoriaUsada() { return memoriaUsada; }
        public long getMemoriaTotal() { return memoriaTotal; }
        public long getMemoriaLibre() { return memoriaLibre; }
        public double getPorcentajeMemoria() { return porcentajeMemoria; }
        public long getDiscoLibre() { return discoLibre; }
        public long getDiscoTotal() { return discoTotal; }
        public double getPorcentajeDisco() { return porcentajeDisco; }
        public int getProcesadores() { return procesadores; }
        public String getSo() { return so; }
        public String getArquitectura() { return arquitectura; }
        public String getVersionJava() { return versionJava; }
        public String getVersionJavaFX() { return versionJavaFX; }
        public String getUsuario() { return usuario; }
        public String getDirectorioTrabajo() { return directorioTrabajo; }
        public String getTiempoEjecucion() { return tiempoEjecucion; }
        
        // Setters
        public void setMemoriaUsada(long v) { this.memoriaUsada = v; }
        public void setMemoriaTotal(long v) { this.memoriaTotal = v; }
        public void setMemoriaLibre(long v) { this.memoriaLibre = v; }
        public void setPorcentajeMemoria(double v) { this.porcentajeMemoria = v; }
        public void setDiscoLibre(long v) { this.discoLibre = v; }
        public void setDiscoTotal(long v) { this.discoTotal = v; }
        public void setPorcentajeDisco(double v) { this.porcentajeDisco = v; }
        public void setProcesadores(int v) { this.procesadores = v; }
        public void setSo(String v) { this.so = v; }
        public void setArquitectura(String v) { this.arquitectura = v; }
        public void setVersionJava(String v) { this.versionJava = v; }
        public void setVersionJavaFX(String v) { this.versionJavaFX = v; }
        public void setUsuario(String v) { this.usuario = v; }
        public void setDirectorioTrabajo(String v) { this.directorioTrabajo = v; }
        public void setTiempoEjecucion(String v) { this.tiempoEjecucion = v; }
    }
    
    private static long tiempoInicio = System.currentTimeMillis();
    
    public static EstadisticasSistema getEstadisticasSistema() {
        EstadisticasSistema stats = new EstadisticasSistema();
        Runtime runtime = Runtime.getRuntime();
        
        // Memoria
        stats.setMemoriaTotal(runtime.maxMemory() / (1024 * 1024));
        stats.setMemoriaUsada((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        stats.setMemoriaLibre(stats.getMemoriaTotal() - stats.getMemoriaUsada());
        stats.setPorcentajeMemoria((stats.getMemoriaUsada() * 100.0) / stats.getMemoriaTotal());
        
        // Disco
        File disco = new File(".");
        stats.setDiscoTotal(disco.getTotalSpace() / (1024 * 1024 * 1024));
        stats.setDiscoLibre(disco.getFreeSpace() / (1024 * 1024 * 1024));
        stats.setPorcentajeDisco(100 - (stats.getDiscoLibre() * 100.0 / stats.getDiscoTotal()));
        
        // Sistema
        stats.setProcesadores(runtime.availableProcessors());
        stats.setSo(System.getProperty("os.name") + " " + System.getProperty("os.version"));
        stats.setArquitectura(System.getProperty("os.arch"));
        stats.setVersionJava(System.getProperty("java.version"));
        stats.setVersionJavaFX(System.getProperty("javafx.runtime.version", "21.0.2"));
        stats.setUsuario(System.getProperty("user.name"));
        stats.setDirectorioTrabajo(inventario.fx.config.PortablePaths.getBase().toString());
        
        // Tiempo de ejecución formateado
        long segundos = (System.currentTimeMillis() - tiempoInicio) / 1000;
        stats.setTiempoEjecucion(formatearTiempoEjecucion(segundos));
        
        return stats;
    }
    
    public static String formatearTiempoEjecucion(long segundos) {
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long segs = segundos % 60;
        
        if (horas > 0) {
            return String.format("%dh %dm %ds", horas, minutos, segs);
        } else if (minutos > 0) {
            return String.format("%dm %ds", minutos, segs);
        } else {
            return String.format("%ds", segs);
        }
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // SISTEMA DE RESPALDOS AUTOMÁTICOS (Directo - sin depender de BackupManager)
    // ════════════════════════════════════════════════════════════════════════════
    
    private static final Path BACKUP_DIR = inventario.fx.config.PortablePaths.getBackupsDir();
    private static final Path DB_PATH = inventario.fx.config.PortablePaths.getDatabaseFile();
    
    /**
     * Crea un backup automático si ha pasado suficiente tiempo desde el último.
     * Operaciones directas de copia de archivos — no depende de BackupManager.
     * 
     * @param razon Razón del backup (para logs)
     */
    private static void crearBackupAutomatico(String razon) {
        long ahora = System.currentTimeMillis();
        long tiempoTranscurrido = ahora - ultimoBackup;
        
        // Solo crear backup si ha pasado el intervalo configurado (1 hora por defecto)
        if (tiempoTranscurrido >= BACKUP_INTERVAL || ultimoBackup == 0) {
            new Thread(() -> {
                try {
                    System.out.println("[AdminManager] 💾 Creando backup automático: " + razon);
                    Path resultado = ejecutarBackupDirecto("auto");
                    if (resultado != null) {
                        ultimoBackup = System.currentTimeMillis();
                        System.out.println("[AdminManager] ✅ Backup automático creado: " + resultado.getFileName());
                    } else {
                        System.err.println("[AdminManager] ⚠️ Backup automático no se completó");
                    }
                } catch (Exception e) {
                    System.err.println("[AdminManager] ⚠️ Error creando backup automático: " + e.getMessage());
                }
            }, "Admin-BackupAutomatico").start();
        }
    }
    
    /**
     * Fuerza la creación de un backup inmediatamente.
     * Útil antes de operaciones críticas o al cerrar la aplicación.
     * 
     * @return Path del backup creado o null si falló
     */
    public static Path crearBackupManual() {
        try {
            System.out.println("[AdminManager] 💾 Creando backup manual...");
            Path resultado = ejecutarBackupDirecto("manual");
            if (resultado != null) {
                ultimoBackup = System.currentTimeMillis();
                System.out.println("[AdminManager] ✅ Backup manual creado: " + resultado.getFileName());
            }
            return resultado;
        } catch (Exception e) {
            AppLogger.getLogger(AdminManager.class).error("Error creando backup manual: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Ejecuta las operaciones de backup directamente copiando archivos.
     * WAL checkpoint → copiar DB (con verificación) → copiar Excel → copiar seguridad → copiar config.
     * 
     * @param tipo "auto" o "manual" — se usa en el nombre de la carpeta
     * @return Path de la carpeta de backup creada, o null si falló
     */
    private static Path ejecutarBackupDirecto(String tipo) {
        try {
            // Verificar que la base de datos existe
            if (!Files.exists(DB_PATH)) {
                System.err.println("[AdminManager] ❌ No se encontró la base de datos: " + DB_PATH);
                return null;
            }
            
            // Crear carpeta de backup con timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path carpetaBackup = BACKUP_DIR.resolve("backup_" + tipo + "_" + timestamp);
            Files.createDirectories(carpetaBackup);
            
            // 1. WAL checkpoint — forzar escritura de cambios pendientes al DB
            try {
                try (java.sql.Connection conn = DatabaseManager.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA wal_checkpoint(TRUNCATE)");
                }
                System.out.println("[AdminManager]   ✓ WAL checkpoint completado");
            } catch (Exception e) {
                System.out.println("[AdminManager]   ⚠ WAL checkpoint falló (continuando): " + e.getMessage());
            }
            
            // 2. Copiar base de datos
            Path dbDestino = carpetaBackup.resolve("inventario.db");
            Files.copy(DB_PATH, dbDestino, StandardCopyOption.REPLACE_EXISTING);
            long tamOriginal = Files.size(DB_PATH);
            long tamCopia = Files.size(dbDestino);
            if (tamCopia < tamOriginal * 0.9) {
                System.err.println("[AdminManager] ⚠️ La copia de DB parece incompleta (" + tamCopia + " vs " + tamOriginal + ")");
            }
            System.out.println("[AdminManager]   ✓ DB copiada (" + (tamCopia / 1024) + " KB)");
            
            // 3. Copiar archivos Excel
            try {
                if (rutasTrabajo != null && rutasTrabajo.getCarpetaProyectos() != null) {
                    Path carpetaExcel = Paths.get(rutasTrabajo.getCarpetaProyectos());
                    if (Files.exists(carpetaExcel)) {
                        Path excelDestino = carpetaBackup.resolve("excel");
                        Files.createDirectories(excelDestino);
                        int copiados = 0;
                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(carpetaExcel, "*.xlsx")) {
                            for (Path archivo : stream) {
                                Files.copy(archivo, excelDestino.resolve(archivo.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                                copiados++;
                            }
                        }
                        System.out.println("[AdminManager]   ✓ " + copiados + " archivos Excel copiados");
                    }
                }
            } catch (Exception e) {
                System.out.println("[AdminManager]   ⚠ Error copiando Excel (continuando): " + e.getMessage());
            }
            
            // 4. Copiar claves de seguridad
            try {
                Path securityDir = inventario.fx.config.PortablePaths.getSecurityDir();
                if (Files.exists(securityDir)) {
                    Path secDestino = carpetaBackup.resolve("security");
                    Files.createDirectories(secDestino);
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(securityDir)) {
                        for (Path archivo : stream) {
                            if (Files.isRegularFile(archivo)) {
                                Files.copy(archivo, secDestino.resolve(archivo.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                    System.out.println("[AdminManager]   ✓ Claves de seguridad copiadas");
                }
            } catch (Exception e) {
                System.out.println("[AdminManager]   ⚠ Error copiando seguridad (continuando): " + e.getMessage());
            }
            
            // 5. Copiar configuración
            try {
                Path configFile = inventario.fx.config.PortablePaths.getApplicationProperties();
                if (Files.exists(configFile)) {
                    Path configDestino = carpetaBackup.resolve("config");
                    Files.createDirectories(configDestino);
                    Files.copy(configFile, configDestino.resolve("application.properties"), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[AdminManager]   ✓ Configuración copiada");
                }
            } catch (Exception e) {
                System.out.println("[AdminManager]   ⚠ Error copiando config (continuando): " + e.getMessage());
            }
            
            // Verificación final: el backup debe contener al menos la BD
            if (!Files.exists(dbDestino) || Files.size(dbDestino) == 0) {
                System.err.println("[AdminManager] ❌ Backup inválido — eliminando carpeta");
                try {
                    Files.walk(carpetaBackup)
                        .sorted(java.util.Comparator.reverseOrder())
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });
                } catch (Exception ignored) {}
                return null;
            }
            
            return carpetaBackup;
            
        } catch (Exception e) {
            AppLogger.getLogger(AdminManager.class).error("Error ejecutando backup directo: " + e.getMessage(), e);
            return null;
        }
    }
}

