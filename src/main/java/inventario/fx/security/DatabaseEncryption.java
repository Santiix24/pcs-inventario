package inventario.fx.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

import inventario.fx.config.PortablePaths;

/**
 * Gestiona la encriptación AES-256-GCM para datos sensibles de la base de datos.
 * Genera y almacena claves de forma segura en el directorio del usuario.
 *
 * <p><b>Características:</b>
 * <ul>
 *   <li>Encriptación AES-256-GCM (autenticada)</li>
 *   <li>Generación segura de claves con SecureRandom</li>
 *   <li>Almacenamiento de clave en archivo protegido</li>
 *   <li>IV único por operación de encriptación</li>
 * </ul>
 *
 * @author SELCOMP
 * @version 1.0
 * @since 2026-01-14
 */
public class DatabaseEncryption {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String KEY_DIR = inventario.fx.config.PortablePaths.getSecurityDir().toString();
    private static final String KEY_FILE = "encryption.key";

    private SecretKey secretKey;
    private final SecureRandom secureRandom;

    /**
     * Constructor que inicializa el sistema de encriptación.
     * Carga o genera la clave AES-256 automáticamente.
     */
    public DatabaseEncryption() {
        this.secureRandom = new SecureRandom();
        try {
            this.secretKey = loadOrGenerateKey();
        } catch (Exception e) {
            System.err.println("❌ Error inicializando encriptación: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar el sistema de encriptación", e);
        }
    }

    /**
     * Verifica que el sistema de encriptación funciona correctamente.
     * Realiza una prueba de encriptar y desencriptar un texto de prueba.
     *
     * @return true si la encriptación funciona correctamente
     */
    public boolean verificarEncriptacion() {
        try {
            String textoOriginal = "VERIFICACION_ENCRIPTACION_" + System.currentTimeMillis();
            String encriptado = encriptar(textoOriginal);
            String desencriptado = desencriptar(encriptado);
            return textoOriginal.equals(desencriptado);
        } catch (Exception e) {
            System.err.println("⚠️ Verificación de encriptación fallida: " + e.getMessage());
            return false;
        }
    }

    /**
     * Encripta un texto usando AES-256-GCM.
     *
     * @param textoPlano Texto a encriptar
     * @return Texto encriptado en Base64 (IV + ciphertext)
     * @throws Exception Si hay error en la encriptación
     */
    public String encriptar(String textoPlano) throws Exception {
        if (textoPlano == null || textoPlano.isEmpty()) {
            return textoPlano;
        }

        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] cipherText = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

        // Combinar IV + ciphertext
        byte[] combined = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Desencripta un texto encriptado con AES-256-GCM.
     *
     * @param textoEncriptado Texto encriptado en Base64
     * @return Texto desencriptado
     * @throws Exception Si hay error en la desencriptación
     */
    public String desencriptar(String textoEncriptado) throws Exception {
        if (textoEncriptado == null || textoEncriptado.isEmpty()) {
            return textoEncriptado;
        }

        byte[] combined = Base64.getDecoder().decode(textoEncriptado);

        // Extraer IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, iv.length);

        // Extraer ciphertext
        byte[] cipherText = new byte[combined.length - iv.length];
        System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    /**
     * Carga la clave de encriptación existente o genera una nueva.
     * Protege el archivo de clave con permisos restrictivos.
     */
    private SecretKey loadOrGenerateKey() throws Exception {
        Path keyDir = Paths.get(KEY_DIR);
        Path keyFile = keyDir.resolve(KEY_FILE);

        if (Files.exists(keyFile)) {
            // Cargar clave existente
            byte[] keyBytes = null;
            try {
                String keyBase64 = new String(Files.readAllBytes(keyFile), StandardCharsets.UTF_8).trim();
                keyBytes = Base64.getDecoder().decode(keyBase64);
                
                // Validar tamaño de la clave (AES-256 = 32 bytes)
                if (keyBytes.length != KEY_SIZE / 8) {
                    throw new SecurityException("Tamaño de clave inválido: " + keyBytes.length + " bytes (esperado: " + (KEY_SIZE / 8) + ")");
                }
                
                return new SecretKeySpec(keyBytes, "AES");
            } finally {
                // Limpiar bytes de clave de memoria
                SecurityManager.limpiarDatos(keyBytes);
            }
        } else {
            // Generar nueva clave
            if (!Files.exists(keyDir)) {
                Files.createDirectories(keyDir);
            }

            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE, secureRandom);
            SecretKey newKey = keyGen.generateKey();

            // Guardar clave
            String keyBase64 = Base64.getEncoder().encodeToString(newKey.getEncoded());
            Files.write(keyFile, keyBase64.getBytes(StandardCharsets.UTF_8));

            // Proteger archivo de clave
            protegerArchivoClave(keyFile);

            System.out.println("🔑 Nueva clave de encriptación generada y protegida");
            return newKey;
        }
    }
    
    /**
     * Protege el archivo de clave con permisos restrictivos del sistema operativo.
     * En Windows: oculta el archivo y marca como solo lectura.
     * En Unix/Linux: establece permisos 600 (solo el propietario puede leer/escribir).
     */
    private void protegerArchivoClave(Path keyFile) {
        try {
            // Usar la protección centralizada con ACL
            PortablePaths.protegerArchivo(keyFile);

            String os = System.getProperty("os.name", "").toLowerCase();
            
            if (os.contains("win")) {
                // Windows: adicionalmente marcar como solo lectura
                try {
                    Files.setAttribute(keyFile, "dos:readonly", true);
                } catch (Exception e) {
                    System.err.println("⚠️ No se pudieron establecer atributos Windows: " + e.getMessage());
                }
            } else {
                // Unix/Linux/Mac: permisos 600
                try {
                    Set<PosixFilePermission> permisos = Set.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE
                    );
                    Files.setPosixFilePermissions(keyFile, permisos);
                } catch (UnsupportedOperationException e) {
                    System.err.println("⚠️ Sistema de archivos no soporta permisos POSIX");
                }
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Error protegiendo archivo de clave: " + e.getMessage());
        }
    }

    /**
     * Verifica si un texto está encriptado (formato Base64 válido).
     *
     * @param texto Texto a verificar
     * @return true si parece estar encriptado
     */
    public boolean estaEncriptado(String texto) {
        if (texto == null || texto.isEmpty()) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(texto);
            return decoded.length > GCM_IV_LENGTH;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
