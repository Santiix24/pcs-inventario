package inventario.fx.core;

/**
 * Clase lanzadora que NO extiende Application.
 * <p>
 * Esto es necesario para ejecutar JavaFX desde un fat JAR (jar-with-dependencies).
 * Si la clase main extiende Application directamente, el runtime de JavaFX
 * exige que los módulos estén en el module-path. Con esta clase intermedia,
 * JavaFX funciona correctamente desde el classpath (fat JAR / .exe portable).
 * </p>
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
