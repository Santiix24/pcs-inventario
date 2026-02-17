package inventario.fx.service;

import inventario.fx.model.AdminManager;
import inventario.fx.util.AppLogger;

import java.nio.file.*;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Servicio para resolver rutas de archivos Excel de proyectos.
 * Centraliza la logica de busqueda que antes estaba duplicada en
 * AdminPanelFX, DashboardFX e InventarioFX.
 *
 * Patron de busqueda (en orden):
 *   1. Inventario_N - NombreLimpio.xlsx
 *   2. Inventario_N - NombreLimpio_DescripcionLimpia.xlsx
 *   3. Cualquier archivo que empiece con "inventario_N -" y termine en .xlsx
 */
public class ProyectoFileService {

    private ProyectoFileService() {} // Utility class

    /**
     * Limpia un nombre de proyecto para usarlo como parte de un nombre de archivo.
     * Elimina caracteres no validos y reemplaza espacios por guiones bajos.
     */
    public static String limpiarNombre(String nombre) {
        if (nombre == null) return "";
        return nombre
            .replaceAll("[\\\\/:*?\"<>|]", "")
            .replace(" ", "_")
            .trim();
    }

    /**
     * Genera el nombre de archivo Excel esperado para un proyecto.
     * Formato: Inventario_N - NombreLimpio.xlsx
     *
     * @param indiceProyecto indice 1-based del proyecto
     * @param nombreProyecto nombre del proyecto (se limpia automaticamente)
     * @return nombre del archivo (sin ruta)
     */
    public static String generarNombreArchivo(int indiceProyecto, String nombreProyecto) {
        return "Inventario_" + indiceProyecto + " - " + limpiarNombre(nombreProyecto) + ".xlsx";
    }

    /**
     * Resuelve la ruta al archivo Excel de un proyecto con logica de fallback.
     * Busca en el directorio de trabajo actual.
     *
     * @param proyecto   el proyecto
     * @param indexBase0 indice 0-based del proyecto en la lista
     * @return la ruta al archivo Excel, o null si no se encuentra
     */
    public static Path resolverRutaExcel(AdminManager.Proyecto proyecto, int indexBase0) {
        return resolverRutaExcel(proyecto, indexBase0, inventario.fx.config.PortablePaths.getProyectosDir());
    }

    /**
     * Resuelve la ruta al archivo Excel de un proyecto con logica de fallback.
     *
     * @param proyecto   el proyecto
     * @param indexBase0 indice 0-based del proyecto en la lista
     * @param carpeta    carpeta donde buscar
     * @return la ruta al archivo Excel, o null si no se encuentra
     */
    public static Path resolverRutaExcel(AdminManager.Proyecto proyecto, int indexBase0, Path carpeta) {
        if (proyecto == null || carpeta == null) return null;

        int indice = indexBase0 + 1;
        String nombreLimpio = limpiarNombre(proyecto.getNombre());

        // 1) Nombre estandar: Inventario_N - NombreLimpio.xlsx
        String nombreArchivo = generarNombreArchivo(indice, proyecto.getNombre());
        Path ruta = carpeta.resolve(nombreArchivo);
        if (Files.exists(ruta)) return ruta;

        // 2) Con descripcion: Inventario_N - NombreLimpio_DescLimpia.xlsx
        if (proyecto.getDescripcion() != null && !proyecto.getDescripcion().isEmpty()) {
            String descLimpia = limpiarNombre(proyecto.getDescripcion());
            nombreArchivo = "Inventario_" + indice + " - " + nombreLimpio + "_" + descLimpia + ".xlsx";
            ruta = carpeta.resolve(nombreArchivo);
            if (Files.exists(ruta)) return ruta;
        }

        // 3) Busqueda flexible por patron
        try (Stream<Path> archivos = Files.list(carpeta)) {
            String prefijo = "inventario_" + indice + " -";
            Optional<Path> encontrado = archivos
                .filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    return name.startsWith(prefijo) && name.endsWith(".xlsx");
                })
                .findFirst();
            if (encontrado.isPresent()) return encontrado.get();
        } catch (Exception ignored) { AppLogger.getLogger(ProyectoFileService.class).warn("Error buscando archivo Excel: " + ignored.getMessage()); }

        return null;
    }

    /**
     * Verifica si un proyecto tiene archivo Excel asociado.
     */
    public static boolean tieneExcel(AdminManager.Proyecto proyecto, int indexBase0) {
        return resolverRutaExcel(proyecto, indexBase0) != null;
    }
}
