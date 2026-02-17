package inventario.fx.excel;

import org.apache.poi.ss.usermodel.*;
import java.util.List;

/**
 * Clase auxiliar que contiene la lógica de poblado de celdas para el template Excel.
 * Replica EXACTAMENTE la estructura de celdas y estilos de GeneratedTemplate.
 *
 * <p>Cada estilo tiene bordes específicos según su posición en regiones combinadas:
 * <ul>
 *   <li>Esquina sup-izq: T+L (styles 0,19,34,38,41,51,70)</li>
 *   <li>Borde superior: T (styles 1,2,3,20,35,46,52,68)</li>
 *   <li>Esquina sup-der: T+R (styles 4,9,21,27,37,47,53,69)</li>
 *   <li>Borde izquierdo: L (styles 5,42,54,60,71)</li>
 *   <li>Sin bordes: (styles 6,7,8,36,45,55)</li>
 *   <li>Borde derecho: R (styles 43,49,56,61)</li>
 *   <li>Esquina inf-izq: B+L (styles 12,23,39,57,62,90)</li>
 *   <li>Borde inferior: B (styles 10,13,14,24,40,58,63)</li>
 *   <li>Esquina inf-der: B+R (styles 11,15,25,59,64)</li>
 *   <li>Header izq: T+B+L (styles 16,31,72,75,77,80,82,85,87)</li>
 *   <li>Header medio: T+B (styles 17,29,65,73,78,83,88)</li>
 *   <li>Header der: T+B+R (styles 18,30,66,74,76,79,81,84,86,89)</li>
 *   <li>4 lados: T+B+L+R (styles 22,28,32,33,48)</li>
 * </ul>
 *
 * @author SELCOMP
 * @version 3.0 — Corregido mapeo exacto de estilos y bordes
 * @since 2026-02-11
 */
public class TemplateBuilderCells {

    // ═══════════════════════════════════════════════════════════════════════════
    // FILAS 1-8: ENCABEZADO (LOGO, TÍTULO, TIPO SOLICITUD, FECHA, TICKET)
    // ═══════════════════════════════════════════════════════════════════════════

    public static void populateRow1to10(Sheet sh, List<CellStyle> styles, int dia, int mes, int anio) {
        try {
            // === FILA 2 (idx 1): ENCABEZADO PRINCIPAL ===
            // B2: Zona imagen proyecto (merge B2:D5) — NO poner texto, la imagen se superpone
            setCellWithStyle(sh, 1, 1, "", styles, 0);
            setCellWithStyle(sh, 1, 2, "", styles, 1);
            setCellWithStyle(sh, 1, 3, "", styles, 1);
            // E2: Título (merge E2:J2)
            setCell(sh, 1, 4, "FORMATO CONCEPTO DE MANTENIMIENTO", styles, 2);
            for (int c = 5; c <= 9; c++) setCellWithStyle(sh, 1, c, "", styles, 2);
            // K2: Logo Selcomp (merge K2:M4)
            setCellWithStyle(sh, 1, 10, "", styles, 3);
            setCellWithStyle(sh, 1, 11, "", styles, 3);
            setCellWithStyle(sh, 1, 12, "", styles, 4);

            // === FILA 3 (idx 2) ===
            setCellWithStyle(sh, 2, 1, "", styles, 5);
            setCellWithStyle(sh, 2, 2, "", styles, 6);
            setCellWithStyle(sh, 2, 3, "", styles, 6);
            for (int c = 4; c <= 9; c++) setCellWithStyle(sh, 2, c, "", styles, 7);
            setCellWithStyle(sh, 2, 10, "", styles, 8);
            setCellWithStyle(sh, 2, 11, "", styles, 8);
            setCellWithStyle(sh, 2, 12, "", styles, 9);

            // === FILA 4 (idx 3) ===
            setCellWithStyle(sh, 3, 1, "", styles, 5);
            setCellWithStyle(sh, 3, 2, "", styles, 6);
            setCellWithStyle(sh, 3, 3, "", styles, 6);
            for (int c = 4; c <= 9; c++) setCellWithStyle(sh, 3, c, "", styles, 7);
            setCellWithStyle(sh, 3, 10, "", styles, 10);
            setCellWithStyle(sh, 3, 11, "", styles, 10);
            setCellWithStyle(sh, 3, 12, "", styles, 11);

            // === FILA 5 (idx 4): TIPO DE SOLICITUD / FECHA ===
            setCellWithStyle(sh, 4, 1, "", styles, 12);
            setCellWithStyle(sh, 4, 2, "", styles, 13);
            setCellWithStyle(sh, 4, 3, "", styles, 13);
            setCell(sh, 4, 4, "TIPO DE SOLICITUD", styles, 14);
            for (int c = 5; c <= 8; c++) setCellWithStyle(sh, 4, c, "", styles, 14);
            setCellWithStyle(sh, 4, 9, "", styles, 15);
            setCell(sh, 4, 10, "FECHA", styles, 16);
            setCellWithStyle(sh, 4, 11, "", styles, 17);
            setCellWithStyle(sh, 4, 12, "", styles, 18);

            // === FILA 6 (idx 5): MANTENIMIENTO PREVENTIVO + Dia/Mes/Año ===
            setCell(sh, 5, 1, "MANTENIMIENTO PREVENTIVO", styles, 19);
            for (int c = 2; c <= 8; c++) setCellWithStyle(sh, 5, c, "", styles, 20);
            setCellWithStyle(sh, 5, 9, "", styles, 21);
            setCell(sh, 5, 10, "Dia", styles, 22);
            setCell(sh, 5, 11, "Mes", styles, 18);
            setCell(sh, 5, 12, "Año", styles, 22);

            // === FILA 7 (idx 6): Continuación merge B6:J7 + valores fecha ===
            // Bottom row del merge B6:J7. Sobreescribir con estilos de borde inferior.
            setCellWithStyle(sh, 6, 1, "", styles, 23);
            setCellWithStyle(sh, 6, 2, "", styles, 24);
            setCellWithStyle(sh, 6, 3, "", styles, 25);
            setCellWithStyle(sh, 6, 4, "", styles, 31);
            for (int c = 5; c <= 8; c++) setCellWithStyle(sh, 6, c, "", styles, 17);
            setCellWithStyle(sh, 6, 9, "", styles, 18);
            setCellNumeric(sh, 6, 10, dia, styles, 32);
            setCellNumeric(sh, 6, 11, mes, styles, 33);
            setCellNumeric(sh, 6, 12, anio, styles, 32);

            // === FILA 8 (idx 7): TICKET N* + área de ticket ===
            // B8:D8 merge, E8:J8 merge — TODOS con style28 (CENTER, all borders)
            setCell(sh, 7, 1, "TICKET N*", styles, 28);
            setCellWithStyle(sh, 7, 2, "", styles, 28);
            setCellWithStyle(sh, 7, 3, "", styles, 28);
            // E8-J8: estilo 28 para centrado y bordes completos
            for (int c = 4; c <= 9; c++) setCellWithStyle(sh, 7, c, "", styles, 28);
            // K8-M8: continuación de merges K7:K8, L7:L8, M7:M8
            setCellWithStyle(sh, 7, 10, "", styles, 32);
            setCellWithStyle(sh, 7, 11, "", styles, 33);
            setCellWithStyle(sh, 7, 12, "", styles, 32);

        } catch (Exception e) {
            System.err.println("[TemplateBuilderCells] Error filas 1-8: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FILAS 9-15: DATOS DEL USUARIO
    // ═══════════════════════════════════════════════════════════════════════════

    public static void populateRow11to20(Sheet sh, List<CellStyle> styles, String tipo, String marca, String modelo) {
        try {
            // === FILA 9 (idx 8): "DATOS DEL USUARIO" (merge B9:M9, fondo oscuro) ===
            setCell(sh, 8, 1, "DATOS DEL USUARIO", styles, 34);
            for (int c = 2; c <= 3; c++) setCellWithStyle(sh, 8, c, "", styles, 35);
            for (int c = 4; c <= 9; c++) setCellWithStyle(sh, 8, c, "", styles, 36);
            for (int c = 10; c <= 11; c++) setCellWithStyle(sh, 8, c, "", styles, 35);
            setCellWithStyle(sh, 8, 12, "", styles, 37);

            // === FILAS 10-15 (idx 9-14): Labels y datos ===
            String[] labels = {"CIUDAD", "DIRECCION", "NOMBRE", "CORREO", "TECNICO", "SEDE/OFICINA"};
            for (int i = 0; i < labels.length; i++) {
                int r = 9 + i;
                // B:C merge — label
                setCell(sh, r, 1, labels[i], styles, 31);
                setCellWithStyle(sh, r, 2, "", styles, 18);
                // D:M merge — datos (llenados por ExcelCeldaHelper después)
                setCellWithStyle(sh, r, 3, "", styles, 31);
                for (int c = 4; c <= 11; c++) setCellWithStyle(sh, r, c, "", styles, 17);
                setCellWithStyle(sh, r, 12, "", styles, 18);
            }

        } catch (Exception e) {
            System.err.println("[TemplateBuilderCells] Error filas 9-15: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FILAS 16-27: DESCRIPCIÓN HARDWARE + INSPECCIÓN PC
    // ═══════════════════════════════════════════════════════════════════════════

    public static void populateRow21to30(Sheet sh, List<CellStyle> styles,
                                          int dia, int mes, int anio,
                                          String cpu, String ram, String disco) {
        try {
            // === FILAS 16-17 (idx 15-16): "DESCRIPCION DEL HARDWARE" (merge B16:M17) ===
            // Fila superior
            setCell(sh, 15, 1, "DESCRIPCION DEL HARDWARE", styles, 38);
            for (int c = 2; c <= 11; c++) setCellWithStyle(sh, 15, c, "", styles, 2);
            setCellWithStyle(sh, 15, 12, "", styles, 27);
            // Fila inferior (continuación)
            setCellWithStyle(sh, 16, 1, "", styles, 39);
            for (int c = 2; c <= 11; c++) setCellWithStyle(sh, 16, c, "", styles, 40);
            setCellWithStyle(sh, 16, 12, "", styles, 33);

            // === FILAS 18-19 (idx 17-18): Sub-encabezados hardware ===
            // Fila superior (idx 17)
            setCell(sh, 17, 1, "TIPO DISPOSITIVO", styles, 41);
            setCellWithStyle(sh, 17, 2, "", styles, 27);
            setCell(sh, 17, 3, "MARCA", styles, 41);
            setCellWithStyle(sh, 17, 4, "", styles, 27);
            setCell(sh, 17, 5, "MODELO", styles, 41);
            setCellWithStyle(sh, 17, 6, "", styles, 27);
            setCell(sh, 17, 7, "SERIAL", styles, 41);
            setCellWithStyle(sh, 17, 8, "", styles, 27);
            setCell(sh, 17, 9, "PLACA", styles, 26);
            setCell(sh, 17, 10, "CONDICIONES", styles, 41);
            setCellWithStyle(sh, 17, 11, "", styles, 2);
            setCellWithStyle(sh, 17, 12, "", styles, 27);
            // Fila inferior (idx 18) — continuación
            setCellWithStyle(sh, 18, 1, "", styles, 39);
            setCellWithStyle(sh, 18, 2, "", styles, 33);
            setCellWithStyle(sh, 18, 3, "", styles, 39);
            setCellWithStyle(sh, 18, 4, "", styles, 33);
            setCellWithStyle(sh, 18, 5, "", styles, 39);
            setCellWithStyle(sh, 18, 6, "", styles, 33);
            setCellWithStyle(sh, 18, 7, "", styles, 39);
            setCellWithStyle(sh, 18, 8, "", styles, 33);
            setCellWithStyle(sh, 18, 9, "", styles, 32);
            setCellWithStyle(sh, 18, 10, "", styles, 39);
            setCellWithStyle(sh, 18, 11, "", styles, 40);
            setCellWithStyle(sh, 18, 12, "", styles, 33);

            // === FILAS 20-22 (idx 19-21): Datos dispositivo (merge 19-21 por bloques) ===
            // Fila superior (idx 19)
            setCellWithStyle(sh, 19, 1, "", styles, 42);
            setCellWithStyle(sh, 19, 2, "", styles, 42);
            setCellWithStyle(sh, 19, 3, "", styles, 42);
            setCellWithStyle(sh, 19, 4, "", styles, 42);
            setCellWithStyle(sh, 19, 5, "", styles, 42);
            setCellWithStyle(sh, 19, 6, "", styles, 42);
            setCellWithStyle(sh, 19, 7, "", styles, 41);
            setCellWithStyle(sh, 19, 8, "", styles, 27);
            setCellWithStyle(sh, 19, 9, "", styles, 26);
            setCellWithStyle(sh, 19, 10, "", styles, 41);
            setCellWithStyle(sh, 19, 11, "", styles, 2);
            setCellWithStyle(sh, 19, 12, "", styles, 27);
            // Fila media (idx 20)
            setCellWithStyle(sh, 20, 1, "", styles, 42);
            setCellWithStyle(sh, 20, 2, "", styles, 43);
            setCellWithStyle(sh, 20, 3, "", styles, 42);
            setCellWithStyle(sh, 20, 4, "", styles, 43);
            setCellWithStyle(sh, 20, 5, "", styles, 42);
            setCellWithStyle(sh, 20, 6, "", styles, 43);
            setCellWithStyle(sh, 20, 7, "", styles, 42);
            setCellWithStyle(sh, 20, 8, "", styles, 43);
            setCellWithStyle(sh, 20, 9, "", styles, 44);
            setCellWithStyle(sh, 20, 10, "", styles, 42);
            setCellWithStyle(sh, 20, 11, "", styles, 45);
            setCellWithStyle(sh, 20, 12, "", styles, 43);
            // Fila inferior (idx 21)
            setCellWithStyle(sh, 21, 1, "", styles, 39);
            setCellWithStyle(sh, 21, 2, "", styles, 33);
            setCellWithStyle(sh, 21, 3, "", styles, 39);
            setCellWithStyle(sh, 21, 4, "", styles, 33);
            setCellWithStyle(sh, 21, 5, "", styles, 39);
            setCellWithStyle(sh, 21, 6, "", styles, 33);
            setCellWithStyle(sh, 21, 7, "", styles, 39);
            setCellWithStyle(sh, 21, 8, "", styles, 33);
            setCellWithStyle(sh, 21, 9, "", styles, 32);
            setCellWithStyle(sh, 21, 10, "", styles, 39);
            setCellWithStyle(sh, 21, 11, "", styles, 40);
            setCellWithStyle(sh, 21, 12, "", styles, 33);

            // === FILA 23 (idx 22): "PC" (merge B23:M23, fondo oscuro) ===
            setCell(sh, 22, 1, "PC", styles, 34);
            for (int c = 2; c <= 11; c++) setCellWithStyle(sh, 22, c, "", styles, 46);
            setCellWithStyle(sh, 22, 12, "", styles, 47);

            // === FILAS 24-25 (idx 23-24): Sub-encabezados PC ===
            // Fila superior (idx 23)
            setCell(sh, 23, 1, "ENCIENDE?", styles, 41);
            setCellWithStyle(sh, 23, 2, "", styles, 27);
            setCell(sh, 23, 3, "Unidades", styles, 28);
            setCellWithStyle(sh, 23, 4, "", styles, 29);
            setCellWithStyle(sh, 23, 5, "", styles, 29);
            setCellWithStyle(sh, 23, 6, "", styles, 30);
            setCell(sh, 23, 7, "BOTONES COMPLETOS", styles, 41);
            setCellWithStyle(sh, 23, 8, "", styles, 2);
            setCellWithStyle(sh, 23, 9, "", styles, 27);
            setCell(sh, 23, 10, "CONDICIONES FISICAS", styles, 41);
            setCellWithStyle(sh, 23, 11, "", styles, 2);
            setCellWithStyle(sh, 23, 12, "", styles, 27);
            // Fila inferior (idx 24)
            setCellWithStyle(sh, 24, 1, "", styles, 39);
            setCellWithStyle(sh, 24, 2, "", styles, 33);
            setCell(sh, 24, 3, "DISCO DURO", styles, 31);
            setCellWithStyle(sh, 24, 4, "", styles, 18);
            setCell(sh, 24, 5, "CD/DVD", styles, 31);
            setCellWithStyle(sh, 24, 6, "", styles, 18);
            setCellWithStyle(sh, 24, 7, "", styles, 39);
            setCellWithStyle(sh, 24, 8, "", styles, 40);
            setCellWithStyle(sh, 24, 9, "", styles, 33);
            setCellWithStyle(sh, 24, 10, "", styles, 39);
            setCellWithStyle(sh, 24, 11, "", styles, 40);
            setCellWithStyle(sh, 24, 12, "", styles, 33);

            // === FILA 26 (idx 25): Valores SI/NO para PC ===
            setCell(sh, 25, 1, "SI/NO", styles, 28);
            setCellWithStyle(sh, 25, 2, "", styles, 30);
            setCell(sh, 25, 3, "SI/NO", styles, 28);
            setCellWithStyle(sh, 25, 4, "", styles, 30);
            setCell(sh, 25, 5, "SI/NO", styles, 28);
            setCellWithStyle(sh, 25, 6, "", styles, 30);
            setCell(sh, 25, 7, "SI/NO", styles, 28);
            setCellWithStyle(sh, 25, 8, "", styles, 29);
            setCellWithStyle(sh, 25, 9, "", styles, 30);
            setCellWithStyle(sh, 25, 10, "", styles, 41);
            setCellWithStyle(sh, 25, 11, "", styles, 2);
            setCellWithStyle(sh, 25, 12, "", styles, 27);

            // === FILA 27 (idx 26): PROCESADOR / MEMORIA RAM / DISCO DURO ===
            setCell(sh, 26, 1, "PROCESADOR", styles, 31);
            setCellWithStyle(sh, 26, 2, "", styles, 18);
            setCell(sh, 26, 3, cpu != null ? cpu : "", styles, 31);
            setCellWithStyle(sh, 26, 4, "", styles, 17);
            setCellWithStyle(sh, 26, 5, "", styles, 17);
            setCellWithStyle(sh, 26, 6, "", styles, 18);
            setCell(sh, 26, 7, "MEMORIA RAM", styles, 31);
            setCellWithStyle(sh, 26, 8, "", styles, 18);
            setCell(sh, 26, 9, ram != null ? ram : "", styles, 31);
            setCell(sh, 26, 10, "DISCO DURO", styles, 31);
            setCellWithStyle(sh, 26, 11, "", styles, 17);
            setCell(sh, 26, 12, disco != null ? disco : "", styles, 48);

        } catch (Exception e) {
            System.err.println("[TemplateBuilderCells] Error filas 16-27: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FILAS 28-35: INSPECCIÓN MONITOR + TECLADO
    // ═══════════════════════════════════════════════════════════════════════════

    public static void populateRow31to40(Sheet sh, List<CellStyle> styles) {
        try {
            // === FILA 28 (idx 27): "MONITOR" (merge B28:M28, fondo oscuro) ===
            setCell(sh, 27, 1, "MONITOR", styles, 34);
            for (int c = 2; c <= 9; c++) setCellWithStyle(sh, 27, c, "", styles, 35);
            setCellWithStyle(sh, 27, 10, "", styles, 36);
            setCellWithStyle(sh, 27, 11, "", styles, 36);
            setCellWithStyle(sh, 27, 12, "", styles, 49);

            // === FILAS 29-30 (idx 28-29): Sub-encabezados Monitor ===
            // Fila superior (idx 28)
            setCell(sh, 28, 1, "ENCIENDE?", styles, 41);
            setCellWithStyle(sh, 28, 2, "", styles, 27);
            setCell(sh, 28, 3, "PANTALLA", styles, 41);
            setCellWithStyle(sh, 28, 4, "", styles, 27);
            setCell(sh, 28, 5, "ONLY ONE", styles, 41);
            setCellWithStyle(sh, 28, 6, "", styles, 27);
            setCell(sh, 28, 7, "BOTONES COMPLETOS", styles, 41);
            setCellWithStyle(sh, 28, 8, "", styles, 2);
            setCellWithStyle(sh, 28, 9, "", styles, 27);
            setCell(sh, 28, 10, "CONDISIONES FISICAS", styles, 41);
            setCellWithStyle(sh, 28, 11, "", styles, 2);
            setCellWithStyle(sh, 28, 12, "", styles, 27);
            // Fila inferior (idx 29)
            setCellWithStyle(sh, 29, 1, "", styles, 39);
            setCellWithStyle(sh, 29, 2, "", styles, 33);
            setCellWithStyle(sh, 29, 3, "", styles, 39);
            setCellWithStyle(sh, 29, 4, "", styles, 33);
            setCellWithStyle(sh, 29, 5, "", styles, 39);
            setCellWithStyle(sh, 29, 6, "", styles, 33);
            setCellWithStyle(sh, 29, 7, "", styles, 39);
            setCellWithStyle(sh, 29, 8, "", styles, 40);
            setCellWithStyle(sh, 29, 9, "", styles, 33);
            setCellWithStyle(sh, 29, 10, "", styles, 39);
            setCellWithStyle(sh, 29, 11, "", styles, 40);
            setCellWithStyle(sh, 29, 12, "", styles, 33);

            // === FILA 31 (idx 30): SI/NO Monitor ===
            setCell(sh, 30, 1, "SI/NO", styles, 28);
            setCellWithStyle(sh, 30, 2, "", styles, 30);
            setCell(sh, 30, 3, "SI/NO", styles, 28);
            setCellWithStyle(sh, 30, 4, "", styles, 30);
            setCell(sh, 30, 5, "SI/NO", styles, 28);
            setCellWithStyle(sh, 30, 6, "", styles, 30);
            setCell(sh, 30, 7, "SI/NO", styles, 31);
            setCellWithStyle(sh, 30, 8, "", styles, 17);
            setCellWithStyle(sh, 30, 9, "", styles, 18);
            setCellWithStyle(sh, 30, 10, "", styles, 31);
            setCellWithStyle(sh, 30, 11, "", styles, 17);
            setCellWithStyle(sh, 30, 12, "", styles, 18);

            // === FILA 32 (idx 31): "TECLADO" (merge B32:M32, fondo oscuro) ===
            setCell(sh, 31, 1, "TECLADO", styles, 34);
            for (int c = 2; c <= 11; c++) setCellWithStyle(sh, 31, c, "", styles, 35);
            setCellWithStyle(sh, 31, 12, "", styles, 37);

            // === FILAS 33-34 (idx 32-33): Sub-encabezados Teclado ===
            // Fila superior (idx 32)
            setCell(sh, 32, 1, "ENCIENDE?", styles, 41);
            setCellWithStyle(sh, 32, 2, "", styles, 27);
            setCell(sh, 32, 3, "FUNCIONA CORRECTAMENTE?", styles, 41);
            setCellWithStyle(sh, 32, 4, "", styles, 2);
            setCellWithStyle(sh, 32, 5, "", styles, 2);
            setCellWithStyle(sh, 32, 6, "", styles, 27);
            setCell(sh, 32, 7, "BOTONES COMPLETOS", styles, 50);
            setCellWithStyle(sh, 32, 8, "", styles, 2);
            setCellWithStyle(sh, 32, 9, "", styles, 27);
            setCell(sh, 32, 10, "CONDICIONES FISICAS", styles, 41);
            setCellWithStyle(sh, 32, 11, "", styles, 2);
            setCellWithStyle(sh, 32, 12, "", styles, 27);
            // Fila inferior (idx 33)
            setCellWithStyle(sh, 33, 1, "", styles, 39);
            setCellWithStyle(sh, 33, 2, "", styles, 33);
            setCellWithStyle(sh, 33, 3, "", styles, 39);
            setCellWithStyle(sh, 33, 4, "", styles, 40);
            setCellWithStyle(sh, 33, 5, "", styles, 40);
            setCellWithStyle(sh, 33, 6, "", styles, 33);
            setCellWithStyle(sh, 33, 7, "", styles, 39);
            setCellWithStyle(sh, 33, 8, "", styles, 40);
            setCellWithStyle(sh, 33, 9, "", styles, 33);
            setCellWithStyle(sh, 33, 10, "", styles, 39);
            setCellWithStyle(sh, 33, 11, "", styles, 40);
            setCellWithStyle(sh, 33, 12, "", styles, 33);

            // === FILA 35 (idx 34): SI/NO Teclado ===
            setCell(sh, 34, 1, "SI/NO", styles, 28);
            setCellWithStyle(sh, 34, 2, "", styles, 30);
            setCell(sh, 34, 3, "SI/NO", styles, 28);
            setCellWithStyle(sh, 34, 4, "", styles, 29);
            setCellWithStyle(sh, 34, 5, "", styles, 29);
            setCellWithStyle(sh, 34, 6, "", styles, 30);
            setCell(sh, 34, 7, "SI/NO", styles, 28);
            setCellWithStyle(sh, 34, 8, "", styles, 29);
            setCellWithStyle(sh, 34, 9, "", styles, 30);
            setCellWithStyle(sh, 34, 10, "", styles, 28);
            setCellWithStyle(sh, 34, 11, "", styles, 29);
            setCellWithStyle(sh, 34, 12, "", styles, 30);

        } catch (Exception e) {
            System.err.println("[TemplateBuilderCells] Error filas 28-35: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FILAS 36-46: SOFTWARE + PROCEDIMIENTO REALIZADO
    // ═══════════════════════════════════════════════════════════════════════════

    public static void populateRow41to50(Sheet sh, List<CellStyle> styles) {
        try {
            // === FILA 36 (idx 35): "SOFTWARE" (merge B36:M36, fondo oscuro) ===
            setCell(sh, 35, 1, "SOFTWARE", styles, 34);
            for (int c = 2; c <= 11; c++) setCellWithStyle(sh, 35, c, "", styles, 35);
            setCellWithStyle(sh, 35, 12, "", styles, 37);

            // === FILA 37 (idx 36): Labels programas ===
            setCell(sh, 36, 1, "PROGRAMAS BASICOS", styles, 31);
            for (int c = 2; c <= 7; c++) setCellWithStyle(sh, 36, c, "", styles, 17);
            setCellWithStyle(sh, 36, 8, "", styles, 18);
            setCell(sh, 36, 9, "OTROS PROGRAMAS", styles, 31);
            for (int c = 10; c <= 11; c++) setCellWithStyle(sh, 36, c, "", styles, 17);
            setCellWithStyle(sh, 36, 12, "", styles, 18);

            // === FILAS 38-40 (idx 37-39): Grid de programas ===
            // Fila 38 (idx 37)
            setCellWithStyle(sh, 37, 1, "", styles, 31);
            setCellWithStyle(sh, 37, 2, "", styles, 18);
            setCellWithStyle(sh, 37, 3, "", styles, 31);
            setCellWithStyle(sh, 37, 4, "", styles, 18);
            setCellWithStyle(sh, 37, 5, "", styles, 31);
            setCellWithStyle(sh, 37, 6, "", styles, 18);
            setCellWithStyle(sh, 37, 7, "", styles, 31);
            setCellWithStyle(sh, 37, 8, "", styles, 18);
            setCellWithStyle(sh, 37, 9, "", styles, 51);
            setCellWithStyle(sh, 37, 10, "", styles, 52);
            setCellWithStyle(sh, 37, 11, "", styles, 52);
            setCellWithStyle(sh, 37, 12, "", styles, 53);
            // Fila 39 (idx 38)
            setCellWithStyle(sh, 38, 1, "", styles, 31);
            setCellWithStyle(sh, 38, 2, "", styles, 18);
            setCellWithStyle(sh, 38, 3, "", styles, 31);
            setCellWithStyle(sh, 38, 4, "", styles, 18);
            setCellWithStyle(sh, 38, 5, "", styles, 31);
            setCellWithStyle(sh, 38, 6, "", styles, 18);
            setCellWithStyle(sh, 38, 7, "", styles, 31);
            setCellWithStyle(sh, 38, 8, "", styles, 18);
            setCellWithStyle(sh, 38, 9, "", styles, 54);
            setCellWithStyle(sh, 38, 10, "", styles, 55);
            setCellWithStyle(sh, 38, 11, "", styles, 55);
            setCellWithStyle(sh, 38, 12, "", styles, 56);
            // Fila 40 (idx 39)
            setCellWithStyle(sh, 39, 1, "", styles, 31);
            setCellWithStyle(sh, 39, 2, "", styles, 18);
            setCellWithStyle(sh, 39, 3, "", styles, 31);
            setCellWithStyle(sh, 39, 4, "", styles, 18);
            setCellWithStyle(sh, 39, 5, "", styles, 31);
            setCellWithStyle(sh, 39, 6, "", styles, 18);
            setCellWithStyle(sh, 39, 7, "", styles, 31);
            setCellWithStyle(sh, 39, 8, "", styles, 18);
            setCellWithStyle(sh, 39, 9, "", styles, 57);
            setCellWithStyle(sh, 39, 10, "", styles, 58);
            setCellWithStyle(sh, 39, 11, "", styles, 58);
            setCellWithStyle(sh, 39, 12, "", styles, 59);

            // === FILA 41 (idx 40): "PROCEDIMIENTO REALIZADO" (merge B41:M41, fondo oscuro) ===
            setCell(sh, 40, 1, "PROCEDIMIENTO REALIZADO", styles, 34);
            for (int c = 2; c <= 11; c++) setCellWithStyle(sh, 40, c, "", styles, 46);
            setCellWithStyle(sh, 40, 12, "", styles, 47);

            // === FILAS 42-45 (idx 41-44): Área procedimiento (merge B42:M46) ===
            for (int r = 41; r <= 44; r++) {
                setCellWithStyle(sh, r, 1, "", styles, 60);
                for (int c = 2; c <= 11; c++) setCellWithStyle(sh, r, c, "", styles, 7);
                setCellWithStyle(sh, r, 12, "", styles, 61);
            }
            // === FILA 46 (idx 45): Fila inferior del área procedimiento ===
            setCellWithStyle(sh, 45, 1, "", styles, 62);
            for (int c = 2; c <= 11; c++) setCellWithStyle(sh, 45, c, "", styles, 63);
            setCellWithStyle(sh, 45, 12, "", styles, 64);

        } catch (Exception e) {
            System.err.println("[TemplateBuilderCells] Error filas 36-46: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FILAS 47-60: OBSERVACIONES + FIRMAS
    // ═══════════════════════════════════════════════════════════════════════════

    public static void populateRow51to59(Sheet sh, List<CellStyle> styles, CellStyle style70b) {
        try {
            // === FILA 47 (idx 46): "OBSERVACIONES" (merge B47:M47, fondo oscuro) ===
            setCell(sh, 46, 1, "OBSERVACIONES", styles, 16);
            for (int c = 2; c <= 11; c++) setCellWithStyle(sh, 46, c, "", styles, 65);
            setCellWithStyle(sh, 46, 12, "", styles, 66);

            // === FILA 48 (idx 47): Área observaciones — fila superior ===
            setCellWithStyle(sh, 47, 1, "", styles, 67);
            for (int c = 2; c <= 11; c++) setCellWithStyle(sh, 47, c, "", styles, 68);
            setCellWithStyle(sh, 47, 12, "", styles, 69);

            // === FILAS 49-51 (idx 48-50): Área observaciones — filas intermedias ===
            for (int r = 48; r <= 50; r++) {
                setCellWithStyle(sh, r, 1, "", styles, 60);
                for (int c = 2; c <= 11; c++) setCellWithStyle(sh, r, c, "", styles, 7);
                setCellWithStyle(sh, r, 12, "", styles, 61);
            }

            // === FILA 52 (idx 51): Área observaciones — fila inferior ===
            setCellWithStyle(sh, 51, 1, "", styles, 62);
            for (int c = 2; c <= 11; c++) setCellWithStyle(sh, 51, c, "", styles, 63);
            setCellWithStyle(sh, 51, 12, "", styles, 64);

            // === FILA 53 (idx 52): Separador firmas ===
            // B53 con estilo70b (fondo oscuro, fuente blanca)
            if (style70b != null) {
                Row row52 = sh.getRow(52);
                if (row52 == null) row52 = sh.createRow(52);
                Cell cellB53 = row52.getCell(1);
                if (cellB53 == null) cellB53 = row52.createCell(1);
                cellB53.setCellStyle(style70b);
            }
            setCellWithStyle(sh, 52, 2, "", styles, 35);
            setCellWithStyle(sh, 52, 3, "", styles, 35);
            setCellWithStyle(sh, 52, 4, "", styles, 35);
            setCellWithStyle(sh, 52, 5, "", styles, 37);
            setCellWithStyle(sh, 52, 6, "", styles, 70);
            setCellWithStyle(sh, 52, 7, "", styles, 37);
            setCell(sh, 52, 8, "FUNCIONARIO", styles, 34);
            setCellWithStyle(sh, 52, 9, "", styles, 35);
            setCellWithStyle(sh, 52, 10, "", styles, 35);
            setCellWithStyle(sh, 52, 11, "", styles, 35);
            setCellWithStyle(sh, 52, 12, "", styles, 37);

            // === FILAS 54-57 (idx 53-56): Áreas de firma ===
            for (int r = 53; r <= 56; r++) {
                setCellWithStyle(sh, r, 1, "", styles, 60);
                for (int c = 2; c <= 4; c++) setCellWithStyle(sh, r, c, "", styles, 7);
                setCellWithStyle(sh, r, 5, "", styles, 61);
                setCellWithStyle(sh, r, 6, "", styles, 71);
                setCellWithStyle(sh, r, 7, "", styles, 49);
                setCellWithStyle(sh, r, 8, "", styles, 60);
                for (int c = 9; c <= 11; c++) setCellWithStyle(sh, r, c, "", styles, 7);
                setCellWithStyle(sh, r, 12, "", styles, 61);
            }

            // === FILA 58 (idx 57): "FIRMA" labels ===
            setCell(sh, 57, 1, "FIRMA", styles, 72);
            setCellWithStyle(sh, 57, 2, "", styles, 73);
            setCellWithStyle(sh, 57, 3, "", styles, 73);
            setCellWithStyle(sh, 57, 4, "", styles, 73);
            setCellWithStyle(sh, 57, 5, "", styles, 74);
            setCellWithStyle(sh, 57, 6, "", styles, 71);
            setCellWithStyle(sh, 57, 7, "", styles, 49);
            setCell(sh, 57, 8, "FIRMA", styles, 31);
            setCellWithStyle(sh, 57, 9, "", styles, 17);
            setCellWithStyle(sh, 57, 10, "", styles, 17);
            setCellWithStyle(sh, 57, 11, "", styles, 17);
            setCellWithStyle(sh, 57, 12, "", styles, 18);

            // === FILA 59 (idx 58): NOMBRE labels ===
            setCell(sh, 58, 1, "NOMBRE", styles, 75);
            setCellWithStyle(sh, 58, 2, "", styles, 76);
            setCellWithStyle(sh, 58, 3, "", styles, 77);
            setCellWithStyle(sh, 58, 4, "", styles, 78);
            setCellWithStyle(sh, 58, 5, "", styles, 79);
            setCellWithStyle(sh, 58, 6, "", styles, 71);
            setCellWithStyle(sh, 58, 7, "", styles, 49);
            setCell(sh, 58, 8, "NOMBRE", styles, 80);
            setCellWithStyle(sh, 58, 9, "", styles, 81);
            setCellWithStyle(sh, 58, 10, "", styles, 82);
            setCellWithStyle(sh, 58, 11, "", styles, 83);
            setCellWithStyle(sh, 58, 12, "", styles, 84);

            // === FILA 60 (idx 59): CEDULA labels ===
            setCell(sh, 59, 1, "CEDULA", styles, 85);
            setCellWithStyle(sh, 59, 2, "", styles, 86);
            setCellWithStyle(sh, 59, 3, "", styles, 87);
            setCellWithStyle(sh, 59, 4, "", styles, 88);
            setCellWithStyle(sh, 59, 5, "", styles, 89);
            // G60,H60: borde inferior del merge G53:H60
            setCellWithStyle(sh, 59, 6, "", styles, 90);
            setCellWithStyle(sh, 59, 7, "", styles, 15);
            setCell(sh, 59, 8, "CEDULA", styles, 80);
            setCellWithStyle(sh, 59, 9, "", styles, 81);
            setCellWithStyle(sh, 59, 10, "", styles, 82);
            setCellWithStyle(sh, 59, 11, "", styles, 83);
            setCellWithStyle(sh, 59, 12, "", styles, 84);

        } catch (Exception e) {
            System.err.println("[TemplateBuilderCells] Error filas 47-60: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════════════════

    private static void setCell(Sheet sh, int rowNum, int colNum, String value, List<CellStyle> styles, int styleIdx) {
        Row row = sh.getRow(rowNum);
        if (row == null) row = sh.createRow(rowNum);
        Cell cell = row.getCell(colNum);
        if (cell == null) cell = row.createCell(colNum);
        cell.setCellValue(value);
        CellStyle style = getSafeStyle(styles, styleIdx);
        if (style != null) cell.setCellStyle(style);
    }

    private static void setCellWithStyle(Sheet sh, int rowNum, int colNum, String value, List<CellStyle> styles, int styleIdx) {
        Row row = sh.getRow(rowNum);
        if (row == null) row = sh.createRow(rowNum);
        Cell cell = row.getCell(colNum);
        if (cell == null) cell = row.createCell(colNum);
        if (value != null && !value.isEmpty()) cell.setCellValue(value);
        CellStyle style = getSafeStyle(styles, styleIdx);
        if (style != null) cell.setCellStyle(style);
    }

    private static void setCellNumeric(Sheet sh, int rowNum, int colNum, int value, List<CellStyle> styles, int styleIdx) {
        Row row = sh.getRow(rowNum);
        if (row == null) row = sh.createRow(rowNum);
        Cell cell = row.getCell(colNum);
        if (cell == null) cell = row.createCell(colNum);
        cell.setCellValue(value);
        CellStyle style = getSafeStyle(styles, styleIdx);
        if (style != null) cell.setCellStyle(style);
    }

    private static CellStyle getSafeStyle(List<CellStyle> styles, int idx) {
        if (styles != null && idx >= 0 && idx < styles.size()) return styles.get(idx);
        return null;
    }
}
