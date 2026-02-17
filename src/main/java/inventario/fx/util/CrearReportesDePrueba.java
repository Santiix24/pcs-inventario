package inventario.fx.util;

import inventario.fx.ui.panel.GestionReportesFX;
import inventario.fx.ui.panel.ReporteFormularioFX;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Utilidad temporal para insertar 6 reportes de prueba en el archivo .dat.
 * 2 buenos (equipo en buen estado), 2 medios (estado regular), 2 malos (equipo dañado).
 * 
 * Ejecutar una sola vez y luego eliminar este archivo.
 */
public class CrearReportesDePrueba {

    /**
     * ObjectInputStream que redirige la clase serializada con el paquete antiguo
     * (inventario.fx.GestionReportesFX$*) al paquete actual (inventario.fx.ui.panel.*).
     */
    private static class MigratingObjectInputStream extends ObjectInputStream {
        MigratingObjectInputStream(InputStream in) throws IOException { super(in); }

        @Override
        protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
            ObjectStreamClass desc = super.readClassDescriptor();
            String name = desc.getName();
            // Redirigir clases del paquete antiguo al nuevo
            if (name.startsWith("inventario.fx.GestionReportesFX")) {
                String newName = name.replace("inventario.fx.GestionReportesFX", "inventario.fx.ui.panel.GestionReportesFX");
                return ObjectStreamClass.lookup(Class.forName(newName));
            }
            if (name.startsWith("inventario.fx.ReporteFormularioFX")) {
                String newName = name.replace("inventario.fx.ReporteFormularioFX", "inventario.fx.ui.panel.ReporteFormularioFX");
                return ObjectStreamClass.lookup(Class.forName(newName));
            }
            return desc;
        }
    }

    public static void main(String[] args) throws Exception {
        Path datFile = inventario.fx.config.PortablePaths.getReportesFile();
        Path bakFile = Paths.get(datFile + ".bak");

        // ── 0. Listar proyectos existentes desde SQLite ──
        System.out.println("=== Proyectos en la base de datos ===");
        try {
            inventario.fx.database.DatabaseManager.initialize();
            var repo = new inventario.fx.database.repository.ProyectoRepository(null);
            var proyectos = repo.findAll(false, false);
            for (int i = 0; i < proyectos.size(); i++) {
                var p = proyectos.get(i);
                System.out.println("  " + (i+1) + ". " + p.getNombre() + " [id=" + p.getId() + ", activo=" + p.isActivo() + "]");
            }
            if (proyectos.isEmpty()) {
                System.out.println("  (no hay proyectos)");
            }
        } catch (Exception e) {
            System.out.println("  Error listando proyectos: " + e.getMessage());
        }
        System.out.println();

        // ── 1. Leer reportes existentes (con migración de nombres de clase) ──
        List<GestionReportesFX.ReporteItem> todos = new ArrayList<>();
        if (Files.exists(datFile)) {
            try (MigratingObjectInputStream ois = new MigratingObjectInputStream(Files.newInputStream(datFile))) {
                @SuppressWarnings("unchecked")
                List<GestionReportesFX.ReporteItem> existentes = (List<GestionReportesFX.ReporteItem>) ois.readObject();
                todos.addAll(existentes);
                System.out.println("Reportes existentes cargados: " + existentes.size());
            }
        }

        // ── 1.5 Eliminar los 6 reportes de prueba anteriores (Proyecto Demo) ──
        todos.removeIf(r -> {
            String proy = r.getProyecto();
            return proy != null && proy.contains("Proyecto Demo");
        });
        System.out.println("Reportes tras limpieza de Demo: " + todos.size());

        // ── 2. Crear 6 reportes de prueba ──
        // Proyecto "Antonio" (índice 1 en la lista activa)
        String proyecto = "1. Antonio";

        // ===== BUENOS (2) =====
        todos.add(crearReporte(
            proyecto, "Mantenimiento Preventivo", "TK-2026-0101",
            "San José", "Edificio Central, Piso 3", "María López", "mlopez@empresa.com",
            "Carlos Ramírez", "Sede Central",
            "Desktop", "Dell", "OptiPlex 7090", "SN-DELL-7090-A1", "PL-001",
            "Buenas condiciones generales",
            // PC
            "Sí", "Funcional", "Funcional", "Todos funcionales", "Buen estado", "Intel i7-11700", "16 GB DDR4", "512 GB SSD NVMe",
            // Monitor
            "Sí", "Sin rayones ni manchas", "Funcional", "Todos funcionales", "Excelente estado",
            // Teclado
            "Sí", "Todas las teclas responden", "Todos funcionales", "Buen estado",
            // Mouse
            "Sí", "Click y scroll funcionan", "Todos funcionales", "Buen estado",
            // Software
            "Windows 11 Pro, Office 365, Antivirus", "Chrome, Teams, Zoom",
            // Trabajo / Obs
            "Se realizó limpieza interna y externa. Se actualizó sistema operativo y drivers. Se verificó antivirus actualizado. Equipo en óptimas condiciones.",
            "Equipo en excelente estado. No requiere intervención adicional. Usuario satisfecho con el mantenimiento.",
            "10", "02", "2026"
        ));

        todos.add(crearReporte(
            proyecto, "Mantenimiento Preventivo", "TK-2026-0102",
            "Heredia", "Oficina Regional Norte", "José García", "jgarcia@empresa.com",
            "Carlos Ramírez", "Sede Norte",
            "Laptop", "Lenovo", "ThinkPad T14s Gen 3", "SN-LEN-T14S-B2", "PL-002",
            "Equipo bien cuidado, sin daños visibles",
            // PC
            "Sí", "Funcional", "N/A", "Todos funcionales", "Excelente estado", "AMD Ryzen 7 PRO 6850U", "32 GB DDR5", "1 TB SSD NVMe",
            // Monitor
            "Sí", "Pantalla IPS sin defectos", "Funcional", "Todos funcionales", "Excelente estado",
            // Teclado
            "Sí", "Retroiluminado, funciona correctamente", "Todos funcionales", "Excelente estado",
            // Mouse
            "Sí", "TrackPad y TrackPoint funcionales", "Todos funcionales", "Buen estado",
            // Software
            "Windows 11 Pro, Office 365, Antivirus Corporativo", "Visual Studio Code, Docker Desktop, Git",
            // Trabajo / Obs
            "Mantenimiento preventivo completo. Limpieza de ventiladores. Actualización de BIOS y firmware. Verificación de batería: 92% de salud.",
            "Laptop en estado óptimo. Batería con buena vida útil. Se recomienda próximo mantenimiento en 6 meses.",
            "11", "02", "2026"
        ));

        // ===== MEDIOS (2) =====
        todos.add(crearReporte(
            proyecto, "Mantenimiento Correctivo", "TK-2026-0201",
            "Alajuela", "Sucursal Oeste, Planta Baja", "Ana Mora", "amora@empresa.com",
            "Luis Herrera", "Sede Oeste",
            "Desktop", "HP", "ProDesk 400 G7", "SN-HP-400G7-C3", "PL-003",
            "Presenta algunos signos de desgaste",
            // PC
            "Sí", "Funcional pero lento", "No tiene", "Power funcional, Reset atascado", "Estado regular", "Intel i5-10500", "8 GB DDR4", "256 GB SSD SATA",
            // Monitor
            "Sí", "Leve amarillamiento en esquina inferior", "Funcional", "Botón de menú no responde", "Estado aceptable",
            // Teclado
            "Sí", "Tecla Enter desgastada, resto funcional", "Funcionales", "Desgaste visible en teclas frecuentes",
            // Mouse
            "Sí", "Scroll con resistencia intermitente", "Click derecho responde", "Desgaste en superficie",
            // Software
            "Windows 10 Pro, Office 2019", "Chrome (desactualizado), Adobe Reader",
            // Trabajo / Obs
            "Se limpió internamente. Se detectó acumulación moderada de polvo. Se actualizó Windows y Chrome. RAM insuficiente para carga actual del usuario.",
            "Se recomienda ampliar RAM a 16 GB. Reemplazar teclado en próximo ciclo. Monitor funcional pero con signos de envejecimiento. Revisión en 3 meses.",
            "12", "02", "2026"
        ));

        todos.add(crearReporte(
            proyecto, "Soporte Técnico", "TK-2026-0202",
            "Cartago", "Oficina Paraíso", "Roberto Sánchez", "rsanchez@empresa.com",
            "Luis Herrera", "Sede Sur",
            "Laptop", "Acer", "Aspire 5 A515", "SN-ACER-A515-D4", "PL-004",
            "Carcasa con rayones, stickers no autorizados",
            // PC
            "Sí", "Funcional", "N/A", "Funcionales", "Estado regular, bisagra ligeramente floja", "Intel i5-1135G7", "8 GB DDR4", "512 GB HDD + 128 GB SSD",
            // Monitor
            "Sí", "Pixel muerto visible en esquina superior derecha", "Funcional", "Funcionales", "Aceptable con defecto menor",
            // Teclado
            "Sí", "Tecla Ñ y tilde requieren presión extra", "Funcionales", "Desgaste moderado",
            // Mouse
            "Sí", "TouchPad funcional con retraso ocasional", "Funcionales", "Superficie desgastada",
            // Software
            "Windows 10 Home (debería ser Pro), Office 365", "Software no autorizado detectado",
            // Trabajo / Obs
            "Se eliminó software no autorizado. Se configuró política de grupo. Bisagra ajustada pero requiere revisión futura. Se migró de HDD a SSD como disco principal.",
            "Equipo funcional pero con varios puntos de atención. Se reportó pixel muerto. Bisagra necesitará reemplazo próximamente. Cambiar licencia a Win Pro.",
            "13", "02", "2026"
        ));

        // ===== MALOS (2) =====
        todos.add(crearReporte(
            proyecto, "Mantenimiento Correctivo", "TK-2026-0301",
            "Puntarenas", "Oficina Regional Pacífico", "Laura Jiménez", "ljimenez@empresa.com",
            "Carlos Ramírez", "Pacífico Central",
            "Desktop", "Genérico", "Ensamblado Custom", "SN-GEN-CUSTOM-E5", "PL-005",
            "Equipo en mal estado, sobrecalentamiento constante",
            // PC
            "Con dificultad", "Ruidos anormales, sectores dañados", "No funciona", "Power intermitente, Reset roto", "Mal estado general", "Intel i3-4170 (obsoleto)", "4 GB DDR3", "500 GB HDD (con sectores dañados)",
            // Monitor
            "Intermitente", "Parpadeo constante, líneas horizontales", "No tiene", "Ninguno funciona", "Muy deteriorado",
            // Teclado
            "Sí", "15 teclas no responden incluyendo F1-F5", "Solo Enter y Espacio", "Muy deteriorado, suciedad acumulada",
            // Mouse
            "Intermitente", "Click izquierdo falla 50% del tiempo", "Solo rueda", "Cable pelado, superficie rota",
            // Software
            "Windows 7 (sin soporte), Office 2010 pirata", "Múltiples virus detectados",
            // Trabajo / Obs
            "EQUIPO NO VIABLE. Disco duro con 847 sectores dañados. Fuente de poder con voltajes fuera de rango. Se detectaron 23 amenazas de malware. SO sin soporte desde 2020. No se pudo completar limpieza por riesgo de fallo total.",
            "RECOMENDACIÓN: REEMPLAZO TOTAL INMEDIATO. El equipo representa un riesgo de seguridad (Windows 7 + malware) y de pérdida de datos (HDD dañado). No es viable reparar. Se sugiere dar de baja y reemplazar por equipo nuevo.",
            "14", "02", "2026"
        ));

        todos.add(crearReporte(
            proyecto, "Mantenimiento Correctivo", "TK-2026-0302",
            "Limón", "Sede Caribe", "Pedro Vargas", "pvargas@empresa.com",
            "Luis Herrera", "Caribe",
            "Laptop", "HP", "ProBook 440 G3", "SN-HP-440G3-F6", "PL-006",
            "Daño por líquido, corrosión visible en placa",
            // PC  
            "No enciende", "No se puede verificar", "N/A", "No responden", "Daño severo por líquido", "Intel i5-6200U", "8 GB DDR3L", "500 GB HDD",
            // Monitor
            "No enciende", "N/A — equipo no enciende", "N/A", "N/A", "No se puede evaluar",
            // Teclado
            "No enciende", "N/A — corrosión visible bajo teclas", "N/A", "Corrosión severa, teclas pegajosas",
            // Mouse
            "No enciende", "N/A", "N/A", "TouchPad con daño por líquido",
            // Software
            "No se puede verificar — equipo no enciende", "N/A",
            // Trabajo / Obs
            "EQUIPO INSERVIBLE. Daño por derrame de líquido (café según usuario). Corrosión extensa en placa madre, módulos RAM con oxidación. Conector de carga con residuos. Se intentó encender con fuente externa sin éxito. Placa madre no responde.",
            "RECOMENDACIÓN: DAR DE BAJA. Equipo con daño irreparable por líquido. Costo de reparación (placa madre + teclado) supera el valor del equipo. Se recomienda rescatar HDD para recuperación de datos y destruir el resto de forma segura.",
            "15", "02", "2026"
        ));

        // ── 3. Backup + guardar ──
        if (Files.exists(datFile)) {
            Files.copy(datFile, bakFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Backup creado: " + bakFile);
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(datFile))) {
            oos.writeObject(todos);
            oos.flush();
        }

        // ── 4. Verificar ──
        try (ObjectInputStream verify = new ObjectInputStream(Files.newInputStream(datFile))) {
            @SuppressWarnings("unchecked")
            List<GestionReportesFX.ReporteItem> check = (List<GestionReportesFX.ReporteItem>) verify.readObject();
            System.out.println("Verificación OK: " + check.size() + " reportes totales");
            System.out.println("(6 nuevos agregados)");
            
            // Mostrar resumen de los 6 nuevos
            int start = check.size() - 6;
            for (int i = start; i < check.size(); i++) {
                GestionReportesFX.ReporteItem r = check.get(i);
                System.out.printf("  [%s] %s — %s — %s — %s/%s/%s%n",
                    r.getId(), r.getTicket(), r.getNombre(), r.getTipo(),
                    r.getDatos().dia, r.getDatos().mes, r.getDatos().anio);
            }
        }

        System.out.println("\n¡Listo! Los 6 reportes de prueba fueron insertados.");
    }

    private static GestionReportesFX.ReporteItem crearReporte(
            String proyecto, String tipoSolicitud, String ticket,
            String ciudad, String direccion, String nombre, String correo,
            String tecnico, String sede,
            String tipoDisp, String marca, String modelo, String serial, String placa,
            String condiciones,
            // PC
            String pcEnciende, String disco, String cddvd, String botonesPC, String condPC,
            String procesador, String ram, String discoCapacidad,
            // Monitor
            String monEnciende, String pantalla, String onlyOne, String botonesMon, String condMon,
            // Teclado
            String tecEnciende, String tecFunciona, String botonesTec, String condTec,
            // Mouse
            String mouseEnciende, String mouseFunciona, String botonesMouse, String condMouse,
            // Software
            String programas, String otros,
            // Trabajo
            String trabajo, String observaciones,
            // Fecha
            String dia, String mes, String anio) {

        ReporteFormularioFX.DatosReporte d = new ReporteFormularioFX.DatosReporte();
        d.proyectoNombre = proyecto;
        d.tipoSolicitud = tipoSolicitud;
        d.ticket = ticket;
        d.dia = dia;
        d.mes = mes;
        d.anio = anio;

        d.ciudad = ciudad;
        d.direccion = direccion;
        d.nombre = nombre;
        d.correo = correo;
        d.tecnico = tecnico;
        d.sede = sede;

        d.tipoDispositivo = tipoDisp;
        d.marca = marca;
        d.modelo = modelo;
        d.serial = serial;
        d.placa = placa;
        d.condiciones = condiciones;

        d.pcEnciende = pcEnciende;
        d.discoDuro = disco;
        d.cddvd = cddvd;
        d.botonesPC = botonesPC;
        d.condicionesPC = condPC;
        d.procesador = procesador;
        d.memoriaRAM = ram;
        d.discoDuroCapacidad = discoCapacidad;

        d.monitorEnciende = monEnciende;
        d.pantalla = pantalla;
        d.onlyOne = onlyOne;
        d.botonesMonitor = botonesMon;
        d.condicionesMonitor = condMon;

        d.tecladoEnciende = tecEnciende;
        d.tecladoFunciona = tecFunciona;
        d.botonesTeclado = botonesTec;
        d.condicionesTeclado = condTec;

        d.mouseEnciende = mouseEnciende;
        d.mouseFunciona = mouseFunciona;
        d.botonesMouse = botonesMouse;
        d.condicionesMouse = condMouse;

        d.programasBasicos = programas;
        d.otrosProgramas = otros;

        d.trabajoRealizado = trabajo;
        d.observaciones = observaciones;

        return new GestionReportesFX.ReporteItem(d);
    }
}
