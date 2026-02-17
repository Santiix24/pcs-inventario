# PCS Inventario

Aplicación de escritorio para gestión de inventarios de equipos de cómputo, desarrollada con **Java 17** y **JavaFX 21**.

## Características

- Gestión de proyectos e inventarios de equipos
- Reportes de mantenimiento con firmas digitales
- Exportación a Excel y PDF
- Panel de administración con auditoría
- Base de datos SQLite cifrada (AES-256)
- Tema claro/oscuro
- Aplicación portable (no requiere instalación)
- Backup automático y restauración

## Tecnologías

| Componente | Versión |
|---|---|
| Java | 17 (Temurin) |
| JavaFX | 21 |
| SQLite | 3.x (via JDBC) |
| Apache POI | Excel generation |
| Build | Maven |
| EXE | Launch4j + jlink |

## Compilar

```bash
mvn clean compile
```

## Ejecutar (desarrollo)

```bash
mvn javafx:run
```

## Generar EXE portable

```bash
mvn clean package -DskipTests
```

El ejecutable se genera en `target/Inventario.exe` con un JRE embebido en `target/jre/`.

## Estructura del proyecto

```
src/main/java/inventario/fx/
├── config/       # Configuración, backup, rutas portables
├── core/         # Punto de entrada (MainApp, MainWindow)
├── database/     # DatabaseManager, repositorios
├── excel/        # Generación de Excel/PDF
├── icons/        # Iconos SVG inline
├── model/        # Modelos y managers
├── security/     # Cifrado AES, seguridad
├── service/      # Servicios (navegación, mantenimiento)
├── ui/           # Interfaz gráfica
│   ├── component/  # Componentes reutilizables
│   ├── dialog/     # Diálogos
│   ├── firma/      # Panel de firmas
│   └── panel/      # Paneles principales
└── util/         # Utilidades (logger, animaciones)
```

## Licencia

Uso interno.
