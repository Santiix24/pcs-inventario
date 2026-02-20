package inventario.fx.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

/**
 * Gestor centralizado de seguridad para toda la aplicación.
 * Proporciona hashing seguro de contraseñas con PBKDF2, protección contra
 * fuerza bruta, timeout de sesión, validación de fortaleza de contraseñas
 * y sanitización de entradas.
 *
 * <p><b>Características:</b>
 * <ul>
 *   <li>Hashing de contraseñas con PBKDF2-HMAC-SHA256 (310.000 iteraciones)</li>
 *   <li>Comparación de hashes en tiempo constante (previene timing attacks)</li>
 *   <li>Protección contra fuerza bruta con bloqueo exponencial</li>
 *   <li>Timeout de sesión automático configurable</li>
 *   <li>Validación de fortaleza de contraseñas</li>
 *   <li>Sanitización de entradas para logs y UI</li>
 *   <li>Limpieza segura de datos sensibles en memoria</li>
 * </ul>
 *
 * @author SELCOMP
 * @version 1.0
 * @since 2026-02-12
 */
public class SecurityManager {

    // ════════════════════════════════════════════════════════════════════════════
    // CONSTANTES DE HASHING
    // ════════════════════════════════════════════════════════════════════════════

    /** Algoritmo PBKDF2 con HMAC-SHA256 para hashing de contraseñas. */
    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";

    /** Iteraciones PBKDF2 — OWASP recomienda ≥310,000 para SHA-256. */
    private static final int PBKDF2_ITERATIONS = 310_000;

    /** Tamaño del hash resultante en bits. */
    private static final int HASH_KEY_LENGTH = 256;

    /** Tamaño del salt en bytes (128 bits). */
    private static final int SALT_LENGTH = 16;

    /** Prefijo que identifica hashes PBKDF2 almacenados. */
    private static final String HASH_PREFIX = "PBKDF2:";

    // ════════════════════════════════════════════════════════════════════════════
    // PROTECCIÓN CONTRA FUERZA BRUTA
    // ════════════════════════════════════════════════════════════════════════════

    /** Máximo de intentos fallidos antes de bloquear. */
    private static final int MAX_INTENTOS_FALLIDOS = 5;

    /** Tiempo base de bloqueo en milisegundos (30 segundos). */
    private static final long BLOQUEO_BASE_MS = 30_000;

    /** Multiplicador exponencial para bloqueos sucesivos. */
    private static final double BLOQUEO_MULTIPLICADOR = 2.0;

    /** Máximo tiempo de bloqueo: 30 minutos. */
    private static final long BLOQUEO_MAXIMO_MS = 30 * 60 * 1000;

    /** Registro de intentos fallidos por fuente (usuario/IP). */
    private static final ConcurrentHashMap<String, IntentosFallidos> intentosFallidos = new ConcurrentHashMap<>();

    // ════════════════════════════════════════════════════════════════════════════
    // TIMEOUT DE SESIÓN
    // ════════════════════════════════════════════════════════════════════════════

    /** Timeout de sesión por defecto: 30 minutos de inactividad. */
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000;

    /** Timestamp de la última actividad administrativa. */
    private static volatile long ultimaActividad = 0;

    /** Si hay sesión admin activa. */
    private static volatile boolean sesionActiva = false;

    /** Generador seguro de números aleatorios. */
    private static final SecureRandom secureRandom = new SecureRandom();

    // ════════════════════════════════════════════════════════════════════════════
    // HASHING DE CONTRASEÑAS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Genera un hash seguro PBKDF2 de una contraseña.
     * Incluye salt aleatorio de 128 bits.
     *
     * @param password Contraseña en texto plano
     * @return Hash en formato "PBKDF2:iteraciones:salt_base64:hash_base64"
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        try {
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_KEY_LENGTH
            );

            SecretKeyFactory factory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // Limpiar la spec de memoria
            spec.clearPassword();

            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return HASH_PREFIX + PBKDF2_ITERATIONS + ":" + saltBase64 + ":" + hashBase64;

        } catch (Exception e) {
            throw new RuntimeException("Error generando hash de contraseña", e);
        }
    }

    /**
     * Verifica una contraseña contra un hash almacenado.
     * Usa comparación en tiempo constante para prevenir timing attacks.
     *
     * @param password   Contraseña en texto plano a verificar
     * @param storedHash Hash almacenado en formato PBKDF2
     * @return true si la contraseña coincide
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }

        // Si el hash almacenado no tiene el prefijo PBKDF2, es una contraseña legacy (texto plano)
        if (!storedHash.startsWith(HASH_PREFIX)) {
            // Comparación en tiempo constante incluso para legacy
            return MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
            );
        }

        try {
            // Parsear hash almacenado: "PBKDF2:iteraciones:salt:hash"
            String[] parts = storedHash.substring(HASH_PREFIX.length()).split(":");
            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

            // Generar hash de la contraseña proporcionada
            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, iterations, expectedHash.length * 8
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
            byte[] actualHash = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();

            // Comparación en tiempo constante
            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (Exception e) {
            System.err.println("[SecurityManager] Error verificando contraseña: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si un hash almacenado usa el formato moderno PBKDF2.
     * Si no, necesita migración.
     *
     * @param storedHash Hash almacenado
     * @return true si usa formato PBKDF2 moderno
     */
    public static boolean isHashModerno(String storedHash) {
        return storedHash != null && storedHash.startsWith(HASH_PREFIX);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PROTECCIÓN CONTRA FUERZA BRUTA
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Verifica si una fuente está bloqueada por exceder intentos fallidos.
     *
     * @param fuente Identificador de la fuente (usuario, IP, etc.)
     * @return true si la fuente está bloqueada
     */
    public static boolean estaBloqueado(String fuente) {
        IntentosFallidos registro = intentosFallidos.get(fuente);
        if (registro == null) {
            return false;
        }

        if (registro.intentos.get() >= MAX_INTENTOS_FALLIDOS) {
            long tiempoBloqueo = calcularTiempoBloqueo(registro.bloqueosConsecutivos);
            long tiempoTranscurrido = System.currentTimeMillis() - registro.ultimoIntento;

            if (tiempoTranscurrido < tiempoBloqueo) {
                return true;
            } else {
                // El bloqueo expiró, reiniciar intentos pero mantener contador de bloqueos
                registro.intentos.set(0);
                return false;
            }
        }
        return false;
    }

    /**
     * Obtiene el tiempo restante de bloqueo en segundos.
     *
     * @param fuente Identificador de la fuente
     * @return Segundos restantes de bloqueo, o 0 si no está bloqueado
     */
    public static long getTiempoBloqueoRestante(String fuente) {
        IntentosFallidos registro = intentosFallidos.get(fuente);
        if (registro == null || registro.intentos.get() < MAX_INTENTOS_FALLIDOS) {
            return 0;
        }

        long tiempoBloqueo = calcularTiempoBloqueo(registro.bloqueosConsecutivos);
        long tiempoTranscurrido = System.currentTimeMillis() - registro.ultimoIntento;
        long restante = tiempoBloqueo - tiempoTranscurrido;

        return Math.max(0, restante / 1000);
    }

    /**
     * Obtiene el número de intentos fallidos restantes antes del bloqueo.
     *
     * @param fuente Identificador de la fuente
     * @return Número de intentos restantes
     */
    public static int getIntentosRestantes(String fuente) {
        IntentosFallidos registro = intentosFallidos.get(fuente);
        if (registro == null) {
            return MAX_INTENTOS_FALLIDOS;
        }
        return Math.max(0, MAX_INTENTOS_FALLIDOS - registro.intentos.get());
    }

    /**
     * Registra un intento fallido de autenticación.
     *
     * @param fuente Identificador de la fuente
     */
    public static void registrarIntentoFallido(String fuente) {
        intentosFallidos.compute(fuente, (key, registro) -> {
            if (registro == null) {
                registro = new IntentosFallidos();
            }
            int intentos = registro.intentos.incrementAndGet();
            registro.ultimoIntento = System.currentTimeMillis();

            if (intentos >= MAX_INTENTOS_FALLIDOS) {
                registro.bloqueosConsecutivos++;
                System.err.println("[SecurityManager] ALERTA: Cuenta bloqueada para '" +
                    sanitizarParaLog(fuente) + "' — bloqueo #" + registro.bloqueosConsecutivos);
            }

            return registro;
        });
        persistirEstadoBF(); // Guardar en disco para sobrevivir reinicios
    }

    /**
     * Reinicia los intentos fallidos tras autenticación exitosa.
     *
     * @param fuente Identificador de la fuente
     */
    public static void reiniciarIntentos(String fuente) {
        intentosFallidos.remove(fuente);
        persistirEstadoBF(); // Limpiar estado en disco tras login exitoso
    }

    /**
     * Calcula el tiempo de bloqueo exponencial.
     */
    private static long calcularTiempoBloqueo(int bloqueosConsecutivos) {
        long tiempo = (long) (BLOQUEO_BASE_MS * Math.pow(BLOQUEO_MULTIPLICADOR, bloqueosConsecutivos - 1));
        return Math.min(tiempo, BLOQUEO_MAXIMO_MS);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // TIMEOUT DE SESIÓN
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Inicia una sesión administrativa.
     * Registra el timestamp de actividad.
     */
    public static void iniciarSesion() {
        sesionActiva = true;
        ultimaActividad = System.currentTimeMillis();
    }

    /**
     * Registra actividad para resetear el timeout de sesión.
     * Llamar cada vez que el admin realice una operación.
     */
    public static void registrarActividad() {
        if (sesionActiva) {
            ultimaActividad = System.currentTimeMillis();
        }
    }

    /**
     * Cierra la sesión administrativa.
     */
    public static void cerrarSesion() {
        sesionActiva = false;
        ultimaActividad = 0;
    }

    /**
     * Verifica si la sesión ha expirado por inactividad.
     *
     * @return true si la sesión ha expirado
     */
    public static boolean sesionExpirada() {
        if (!sesionActiva) {
            return true;
        }

        long tiempoInactivo = System.currentTimeMillis() - ultimaActividad;
        if (tiempoInactivo > SESSION_TIMEOUT_MS) {
            cerrarSesion();
            // No logear detalles de sesión a consola
            return true;
        }
        return false;
    }

    /**
     * Obtiene el tiempo restante de sesión en minutos.
     *
     * @return Minutos restantes, o 0 si no hay sesión activa
     */
    public static long getMinutosRestantesSesion() {
        if (!sesionActiva) {
            return 0;
        }
        long restante = SESSION_TIMEOUT_MS - (System.currentTimeMillis() - ultimaActividad);
        return Math.max(0, restante / 60000);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // VALIDACIÓN DE FORTALEZA DE CONTRASEÑA
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Resultado de la validación de fortaleza de una contraseña.
     */
    public static class ResultadoValidacion {
        private final boolean valida;
        private final String mensaje;
        private final int fortaleza; // 0-100

        public ResultadoValidacion(boolean valida, String mensaje, int fortaleza) {
            this.valida = valida;
            this.mensaje = mensaje;
            this.fortaleza = fortaleza;
        }

        public boolean isValida() { return valida; }
        public String getMensaje() { return mensaje; }
        public int getFortaleza() { return fortaleza; }
    }

    /**
     * Valida la fortaleza de una contraseña nueva.
     * Requisitos mínimos: 8 caracteres, mayúscula, minúscula, número, carácter especial.
     *
     * @param password Contraseña a validar
     * @return Resultado con validez, mensaje descriptivo y puntuación 0-100
     */
    public static ResultadoValidacion validarFortaleza(String password) {
        if (password == null || password.isEmpty()) {
            return new ResultadoValidacion(false, "La contraseña no puede estar vacía", 0);
        }

        int puntuacion = 0;
        StringBuilder problemas = new StringBuilder();

        // Longitud mínima: 8 caracteres
        if (password.length() < 8) {
            problemas.append("Mínimo 8 caracteres. ");
        } else {
            puntuacion += 20;
            if (password.length() >= 12) puntuacion += 10;
            if (password.length() >= 16) puntuacion += 10;
        }

        // Mayúsculas
        if (!password.matches(".*[A-Z].*")) {
            problemas.append("Requiere al menos una mayúscula. ");
        } else {
            puntuacion += 15;
        }

        // Minúsculas
        if (!password.matches(".*[a-z].*")) {
            problemas.append("Requiere al menos una minúscula. ");
        } else {
            puntuacion += 15;
        }

        // Números
        if (!password.matches(".*[0-9].*")) {
            problemas.append("Requiere al menos un número. ");
        } else {
            puntuacion += 15;
        }

        // Caracteres especiales
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            problemas.append("Requiere al menos un carácter especial. ");
        } else {
            puntuacion += 15;
        }

        // Penalizar patrones comunes
        String lower = password.toLowerCase();
        if (lower.contains("password") || lower.contains("123456") ||
            lower.contains("admin") || lower.contains("qwerty") ||
            lower.contains("abc123")) {
            puntuacion = Math.max(0, puntuacion - 30);
            problemas.append("Evitar patrones comunes. ");
        }

        // Verificar secuencias repetidas
        if (password.matches(".*(.)\\1{2,}.*")) {
            puntuacion = Math.max(0, puntuacion - 10);
            problemas.append("Evitar caracteres repetidos consecutivos. ");
        }

        puntuacion = Math.min(100, puntuacion);

        boolean valida = problemas.length() == 0 && puntuacion >= 50;
        String mensaje = valida ? "Contraseña segura" : problemas.toString().trim();

        return new ResultadoValidacion(valida, mensaje, puntuacion);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SANITIZACIÓN DE ENTRADAS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Sanitiza un texto para uso seguro en logs.
     * Elimina caracteres de control, limita longitud y escapa caracteres peligrosos.
     *
     * @param input Texto a sanitizar
     * @return Texto sanitizado seguro para logs
     */
    public static String sanitizarParaLog(String input) {
        if (input == null) {
            return "[null]";
        }

        // Eliminar caracteres de control y no imprimibles
        String sanitizado = input.replaceAll("[\\p{Cc}\\p{Cn}]", "");

        // Limitar longitud
        if (sanitizado.length() > 200) {
            sanitizado = sanitizado.substring(0, 200) + "...[truncado]";
        }

        // Escapar caracteres que podrían inyectar en logs
        sanitizado = sanitizado
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");

        return sanitizado;
    }

    /**
     * Sanitiza un texto para prevenir inyección SQL (defensa en profundidad).
     * NOTA: Siempre usar PreparedStatement como protección principal.
     *
     * @param input Texto a sanitizar
     * @return Texto sanitizado
     */
    public static String sanitizarParaSQL(String input) {
        if (input == null) {
            return null;
        }

        // Escapar comillas simples (defensa en profundidad, PreparedStatement es la protección principal)
        return input
            .replace("'", "''")
            .replace("\\", "\\\\")
            .replace("\0", "");
    }

    /**
     * Valida que un texto no contenga caracteres peligrosos para rutas de archivos.
     *
     * @param input Texto a validar
     * @return true si el texto es seguro para usar en nombres de archivo
     */
    public static boolean esNombreArchivoSeguro(String input) {
        if (input == null || input.isEmpty() || input.length() > 255) {
            return false;
        }

        // Caracteres prohibidos en nombres de archivo
        return !input.matches(".*[\\\\/:*?\"<>|\\x00-\\x1F].*") &&
               !input.equals(".") && !input.equals("..") &&
               !input.startsWith(" ") && !input.endsWith(" ") &&
               !input.endsWith(".");
    }

    /**
     * Sanitiza un nombre de archivo eliminando caracteres peligrosos.
     *
     * @param input Nombre de archivo a sanitizar
     * @return Nombre sanitizado
     */
    public static String sanitizarNombreArchivo(String input) {
        if (input == null || input.isEmpty()) {
            return "sin_nombre";
        }

        return input
            .replaceAll("[\\\\/:*?\"<>|\\x00-\\x1F]", "")
            .replaceAll("^[. ]+", "")
            .replaceAll("[. ]+$", "")
            .trim();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PERSISTENCIA ANTI-BRUTE-FORCE (sobrevive reinicios del exe)
    // ════════════════════════════════════════════════════════════════════════════

    private static volatile java.nio.file.Path bruteForceFile = null;

    /**
     * Inicializa la persistencia de contadores brute-force desde archivo.
     * Llamar una vez al arrancar la app.
     */
    public static synchronized void inicializarPersistencia(java.nio.file.Path bfPath) {
        bruteForceFile = bfPath;
        // Cargar estado anterior
        if (!java.nio.file.Files.exists(bfPath)) return;
        try {
            java.util.Properties p = new java.util.Properties();
            try (java.io.InputStream in = java.nio.file.Files.newInputStream(bfPath)) {
                p.load(in);
            }
            p.stringPropertyNames().forEach(fuente -> {
                String[] parts = p.getProperty(fuente, "").split(",");
                if (parts.length == 3) {
                    try {
                        IntentosFallidos reg = new IntentosFallidos();
                        reg.intentos.set(Integer.parseInt(parts[0].trim()));
                        reg.bloqueosConsecutivos = Integer.parseInt(parts[1].trim());
                        reg.ultimoIntento = Long.parseLong(parts[2].trim());
                        // Solo cargar si el bloqueo aún es vigente (no expirado hace >24h)
                        long transcurrido = System.currentTimeMillis() - reg.ultimoIntento;
                        if (reg.intentos.get() >= MAX_INTENTOS_FALLIDOS && transcurrido < 24 * 3600_000L) {
                            intentosFallidos.put(fuente, reg);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            });
        } catch (Exception e) {
            System.err.println("[SecurityManager] Error cargando estado brute-force: " + e.getMessage());
        }
    }

    private static void persistirEstadoBF() {
        if (bruteForceFile == null) return;
        try {
            java.util.Properties p = new java.util.Properties();
            intentosFallidos.forEach((fuente, reg) ->
                p.setProperty(fuente, reg.intentos.get() + "," + reg.bloqueosConsecutivos + "," + reg.ultimoIntento));
            java.nio.file.Files.createDirectories(bruteForceFile.getParent());
            try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(bruteForceFile)) {
                p.store(out, null);
            }
        } catch (Exception e) {
            System.err.println("[SecurityManager] Error guardando estado brute-force: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // LIMPIEZA DE DATOS SENSIBLES
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Limpia un array de caracteres de forma segura (sobrescribe con ceros).
     * Usar para contraseñas después de procesarlas.
     *
     * @param datos Array a limpiar
     */
    public static void limpiarDatos(char[] datos) {
        if (datos != null) {
            java.util.Arrays.fill(datos, '\0');
        }
    }

    /**
     * Limpia un array de bytes de forma segura (sobrescribe con ceros).
     *
     * @param datos Array a limpiar
     */
    public static void limpiarDatos(byte[] datos) {
        if (datos != null) {
            java.util.Arrays.fill(datos, (byte) 0);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // MIGRACIÓN DE CONTRASEÑAS LEGACY
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Migra una contraseña almacenada en texto plano al formato PBKDF2 hasheado.
     *
     * @param prefs Preferences donde está almacenada la contraseña
     * @param prefKey Clave en Preferences
     * @param defaultPlaintext Valor por defecto en texto plano
     * @return true si se realizó la migración
     */
    public static boolean migrarPasswordSiNecesario(Preferences prefs, String prefKey, String defaultPlaintext) {
        try {
            String stored = prefs.get(prefKey, defaultPlaintext);

            // Si ya está hasheada, no migrar
            if (isHashModerno(stored)) {
                return false;
            }

            // Hashear la contraseña existente (texto plano)
            String hashedPassword = hashPassword(stored);
            prefs.put(prefKey, hashedPassword);
            // Migración completada silenciosamente
            return true;

        } catch (Exception e) {
            System.err.println("[SecurityManager] Error migrando contraseña: " + e.getMessage());
            return false;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CLASE INTERNA - REGISTRO DE INTENTOS FALLIDOS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Registro interno de intentos fallidos de autenticación por fuente.
     */
    private static class IntentosFallidos {
        final AtomicInteger intentos = new AtomicInteger(0);
        volatile long ultimoIntento = 0;
        volatile int bloqueosConsecutivos = 0;
    }
}
