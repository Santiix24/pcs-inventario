package inventario.fx.excel;
import inventario.fx.model.InventarioFXBase;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.RegionUtil;

public class AppsWriter {
    /**
     * Escribe hasta 12 aplicaciones esenciales en la hoja en las celdas B38..H40.
     * Se espera que el caller pase el estilo a usar para las celdas.
     */
    public static void writeEssentialApps(Sheet sh, InventarioFXBase.InfoPC sysInfo, CellStyle style) {
        try {
                java.util.List<String> preferidas = java.util.Arrays.asList(
                    "Word","Excel","PowerPoint","Outlook",
                    "Microsoft Teams","Zoom","Slack","Edge",
                    "Chrome","Firefox","Explorador","Adobe Reader"
                );

                // Substrings que queremos excluir completamente (no aparecerán como items)
                java.util.List<String> bannedSubstrings = java.util.Arrays.asList(
                    "aplicaciones de microsoft",
                    "aplicaciones de microsoft 365",
                    "microsoft 365",
                    "microsoft office",
                    "temurin",
                    "jdk",
                    "hotspot",
                    "eclipse temurin",
                    // entradas de drivers/herramientas de chipset que no queremos mostrar
                    "chipset",
                    "management engine",
                    "trusted connect",
                    // excluir Launch4j y similares de utilidades de empaquetado
                    "launch4j",
                    "launch 4j",
                    // Intel RST y utilidades de sistema específicas
                    "rstdowngradeguard",
                    "optanedowngradeguard",
                    "downgrade guard",
                    "rapid storage",
                    "storage technology",
                    // Componentes de sistema no deseados
                    "updater",
                    "helper",
                    // Herramientas de desarrollo/sistema no deseadas
                    "sdk",
                    ".net framework",
                    "dotnet",
                    "redistributable",
                    "vcredist",
                    "directx",
                    // Componentes de hardware/drivers
                    "realtek",
                    "nvidia driver",
                    "nvidia geforce",
                    "amd catalyst",
                    "radeon software",
                    "bluetooth driver",
                    "wireless driver",
                    "lan driver",
                    "audio driver",
                    "sound driver",
                    "graphics driver",
                    // Utilidades de fabricantes
                    "support assistant",
                    "system update",
                    // Componentes internos de Windows
                    "windows sdk",
                    "windows kit"
                );
                    // excluir VS Code y MySQL (instaladores/servidores)
                    java.util.List<String> extraBanned = java.util.Arrays.asList(
                        "visual studio code",
                        "vscode",
                        "mysql",
                        "mysql installer",
                        "mysql server",
                        "mysql workbench"
                    );
                    java.util.List<String> tmp = new java.util.ArrayList<>(bannedSubstrings);
                    tmp.addAll(extraBanned);
                    bannedSubstrings = tmp;
                    // añadir patrones para excluir redistribuibles de Visual C++
                    java.util.List<String> moreVc = java.util.Arrays.asList(
                        "visual c++",
                        "vc++",
                        "visual c++ redistributable",
                        "vc++ redistributable",
                        "microsoft visual c++",
                        "microsoft vc++"
                    );
                    // combinar listas
                    java.util.List<String> combined = new java.util.ArrayList<>(bannedSubstrings);
                    combined.addAll(moreVc);
                    bannedSubstrings = combined;
                    // añadir filtros contra Click-to-Run, Optane u otras utilidades que no queremos mostrar
                    java.util.List<String> clickOptane = java.util.Arrays.asList(
                        "click-to-run",
                        "click to run",
                        "office 16 click",
                        "optanedowngradeguard",
                        "optane"
                    );
                    bannedSubstrings.addAll(clickOptane);

                    // Marcas de antivirus a detectar explícitamente (mostrarlas si se detectan)
                    java.util.Map<String,String> avBrands = new java.util.LinkedHashMap<>();
                    avBrands.put("bitdefender", "Bitdefender");
                    avBrands.put("kaspersky", "Kaspersky");
                    avBrands.put("mcafee", "McAfee");
                    avBrands.put("avast", "Avast");
                    avBrands.put("avg", "AVG");
                    avBrands.put("avira", "Avira");
                    avBrands.put("trend micro", "Trend Micro");
                    avBrands.put("eset", "ESET");
                    avBrands.put("sophos", "Sophos");
                    avBrands.put("windows defender", "Microsoft Defender");
                    avBrands.put("microsoft defender", "Microsoft Defender");

            String appsCsv = "";
            try {
                if (sysInfo != null && sysInfo.installedApps != null && !sysInfo.installedApps.isEmpty()) appsCsv = sysInfo.installedApps;
                else appsCsv = InventarioFXBase.getInstalledApps(null);
            } catch (Throwable _t) { appsCsv = ""; }

            java.util.Set<String> encontrados = new java.util.LinkedHashSet<>();
            // candidatos a "aplicaciones externas" (nombres limpios que no coinciden con la lista blanca)
            java.util.Set<String> externalCandidates = new java.util.LinkedHashSet<>();
            if (appsCsv != null && !appsCsv.isEmpty()) {
                String[] lines = appsCsv.split("\n");
                for (int li = 1; li < lines.length; li++) {
                    String line = lines[li].trim();
                    if (line.isEmpty()) continue;
                    java.util.List<String> cols = InventarioFXBase.parseCsvLineAvanzado(line);
                    if (cols.size() > 0) {
                        String name = cols.get(0).trim();
                        if (name.isEmpty()) continue;
                        String lower = name.toLowerCase();
                        // Si se detecta una entrada global de Office/Microsoft 365, marcar sus apps principales
                        if (lower.contains("microsoft") && (lower.contains("365") || lower.contains("office") || lower.contains("office 365") || lower.contains("office suite"))) {
                            encontrados.add("Word");
                            encontrados.add("Excel");
                            encontrados.add("PowerPoint");
                            encontrados.add("Outlook");
                            // No añadir la entrada genérica como item separado
                            continue;
                        }
                        // Detectar marcas de antivirus explícitas antes de filtrar generics
                        boolean avAdded = false;
                        for (java.util.Map.Entry<String,String> e : avBrands.entrySet()) {
                            if (lower.contains(e.getKey())) {
                                encontrados.add(e.getValue());
                                avAdded = true;
                                break;
                            }
                        }
                        if (avAdded) continue;

                        // Filtrar cadenas que no queremos mostrar como aplicaciones individuales
                        boolean banned = false;
                        for (String b : bannedSubstrings) {
                            if (lower.contains(b)) { banned = true; break; }
                        }
                        if (banned) {
                            // Si es una mención genérica o una utilidad no deseada, no la añadimos
                            continue;
                        }
                        if (lower.contains("word")) encontrados.add("Word");
                        else if (lower.contains("excel")) encontrados.add("Excel");
                        else if (lower.contains("powerp") || lower.contains("power point") || lower.contains("powerpoint")) encontrados.add("PowerPoint");
                        else if (lower.contains("outlook")) encontrados.add("Outlook");
                        else if (lower.contains("teams")) encontrados.add("Microsoft Teams");
                        else if (lower.contains("zoom")) encontrados.add("Zoom");
                        else if (lower.contains("slack")) encontrados.add("Slack");
                        else if (lower.contains("edge") || lower.contains("microsoft edge")) encontrados.add("Edge");
                        else if (lower.contains("chrome") || lower.contains("google chrome")) encontrados.add("Chrome");
                        else if (lower.contains("firefox")) encontrados.add("Firefox");
                        else if (lower.contains("onedrive") || lower.contains("one drive")) encontrados.add("OneDrive");
                        else if (lower.contains("explorer") || lower.contains("explorador") || lower.contains("file explorer")) encontrados.add("Explorador");
                        else if (lower.contains("adobe") || lower.contains("acrobat") || lower.contains("reader") || lower.contains("pdf")) encontrados.add("Adobe Reader");
                        else if (lower.contains("antivirus") || lower.contains("defender") || lower.contains("trend micro") || lower.contains("avast") || lower.contains("kaspersky") || lower.contains("mcafee") || lower.contains("avira")) {
                            // Detectamos antivirus pero no lo mostramos como item genérico (filtro solicitado)
                            // Si quieres listar marcas específicas, podemos mapearlas individualmente.
                        }
                        else {
                            String limpio = name.replaceAll("\\s+"," ").trim();
                            if (!limpio.isEmpty()) {
                                if (limpio.length() > 60) limpio = limpio.substring(0,60).trim();
                                encontrados.add(limpio);
                                externalCandidates.add(limpio);
                            }
                        }
                    }
                }
            }

            // Lista blanca estricta: sólo mostrar estas aplicaciones (en este orden)
                java.util.List<String> listaBlanca = java.util.Arrays.asList(
                    "Word","Excel","PowerPoint","Outlook",
                    "Microsoft Teams","Chrome","Edge","Firefox",
                    "Adobe Reader","Zoom","Slack","Explorador"
                );
            // Construir lista final siguiendo el orden solicitado y poniendo Antivirus al final
            java.util.List<String> finalDetected = new java.util.ArrayList<>();

            // Helper: función para añadir si existe
            java.util.function.Consumer<String> addIfExists = (name) -> {
                if (encontrados.contains(name) && !finalDetected.contains(name)) finalDetected.add(name);
            };

            // 1-4 Office
            addIfExists.accept("Word");
            addIfExists.accept("Excel");
            addIfExists.accept("PowerPoint");
            addIfExists.accept("Outlook");
            // 5 Microsoft Teams
            addIfExists.accept("Microsoft Teams");
            // 6 Navegadores: mostrar todos los que estén instalados
            addIfExists.accept("Edge");
            addIfExists.accept("Chrome");
            addIfExists.accept("Firefox");
            // 7 Adobe Reader
            addIfExists.accept("Adobe Reader");
            // 8 Antivirus: añadir la primera marca detectada (si hay)
            for (String avKey : avBrands.keySet()) {
                String avName = avBrands.get(avKey);
                if (encontrados.contains(avName)) {
                    if (!finalDetected.contains(avName)) finalDetected.add(avName);
                    break;
                }
            }

            // Construir la lista de "aplicaciones externas": candidatos que NO formen parte
            // de la lista blanca ni ya estén en finalDetected ni sean marcas AV.
            java.util.List<String> externalApps = new java.util.ArrayList<>();
            for (String cand : externalCandidates) {
                if (finalDetected.contains(cand)) continue;
                if (listaBlanca.contains(cand)) continue;
                boolean isAv = false;
                for (String av : avBrands.values()) { if (av.equalsIgnoreCase(cand)) { isAv = true; break; } }
                if (isAv) continue;
                // Filtrar con la misma lista de banned
                String low = cand.toLowerCase();
                boolean isBanned = false;
                for (String b : bannedSubstrings) { if (low.contains(b)) { isBanned = true; break; } }
                if (isBanned) continue;
                externalApps.add(cand);
            }

            // Formatear texto para J38: cada app en una línea; si no hay externas, mostrar "No".
            String externalText;
            if (externalApps.isEmpty()) externalText = "No";
            else externalText = String.join("\n", externalApps);

            // Escribir hasta 12 entradas compactadas en B38..H40
            int[] cols = new int[]{1,3,5,7};
            int idx = 0;

            // Preparar un estilo derivado que incluya borde izquierdo para la columna B
            CellStyle leftStyle = null;
            try {
                if (style != null) {
                    leftStyle = sh.getWorkbook().createCellStyle();
                    leftStyle.cloneStyleFrom(style);
                    leftStyle.setBorderLeft(BorderStyle.THIN);
                }
            } catch (Throwable _t) {
                leftStyle = style; // fallback
            }

            for (int r = 37; r <= 39; r++) {
                Row row = sh.getRow(r); if (row == null) row = sh.createRow(r);
                for (int c : cols) {
                    Cell cell = row.getCell(c); if (cell == null) cell = row.createCell(c);
                    String val = "";
                    if (idx < finalDetected.size()) val = finalDetected.get(idx);
                    cell.setCellValue(val != null ? val : "");
                    if (style != null) {
                        try {
                            // Columna B (índice 1) debe tener borde izquierdo
                            if (c == 1 && leftStyle != null) cell.setCellStyle(leftStyle);
                            else cell.setCellStyle(style);
                        } catch (Exception _e) {}
                    }
                    idx++;
                    if (idx >= 12) break;
                }
                if (idx >= 12) break;
            }

            // Reemplazar la fusión J38:M40 por fusiones en pares (J-K y L-M por cada fila 38..40)
            try {
                // Primero eliminar merges que intersecten J38:M40 (rows 37..39, cols 9..12)
                for (int i = sh.getNumMergedRegions() - 1; i >= 0; i--) {
                    org.apache.poi.ss.util.CellRangeAddress ra = sh.getMergedRegion(i);
                    if (ra.getFirstRow() <= 39 && ra.getLastRow() >= 37 && ra.getFirstColumn() <= 12 && ra.getLastColumn() >= 9) {
                        try { sh.removeMergedRegion(i); } catch (Exception _e) {}
                    }
                }

                // Añadir nuevas fusiones: (J-K) y (L-M) en filas 37..39
                for (int r = 37; r <= 39; r++) {
                    try { sh.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(r, r, 9, 10)); } catch (Exception _e) {}
                    try { sh.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(r, r, 11, 12)); } catch (Exception _e) {}
                }

                // Preparar estilo para las celdas externas: wrapText y alineación
                CellStyle extStyle = null;
                if (style != null) {
                    extStyle = sh.getWorkbook().createCellStyle();
                    try { extStyle.cloneStyleFrom(style); } catch (Exception _t) {}
                    try { extStyle.setWrapText(true); } catch (Exception _t) {}
                    try { extStyle.setAlignment(HorizontalAlignment.LEFT); } catch (Exception _t) {}
                    try { extStyle.setVerticalAlignment(VerticalAlignment.TOP); } catch (Exception _t) {}
                    try { extStyle.setBorderTop(BorderStyle.NONE); } catch (Exception _t) {}
                    try { extStyle.setBorderLeft(BorderStyle.NONE); } catch (Exception _t) {}
                    try { extStyle.setBorderRight(BorderStyle.NONE); } catch (Exception _t) {}
                    try { extStyle.setBorderBottom(BorderStyle.NONE); } catch (Exception _t) {}
                    // además, asegurarnos de remover bordes de las fusiones internas si existen
                    try {
                        for (int rr = 37; rr <= 39; rr++) {
                            org.apache.poi.ss.util.CellRangeAddress r1 = new org.apache.poi.ss.util.CellRangeAddress(rr, rr, 9, 10);
                            org.apache.poi.ss.util.CellRangeAddress r2 = new org.apache.poi.ss.util.CellRangeAddress(rr, rr, 11, 12);
                            try { RegionUtil.setBorderTop(BorderStyle.NONE, r1, sh); } catch (Throwable __t) {}
                            try { RegionUtil.setBorderBottom(BorderStyle.NONE, r1, sh); } catch (Throwable __t) {}
                            try { RegionUtil.setBorderLeft(BorderStyle.NONE, r1, sh); } catch (Throwable __t) {}
                            try { RegionUtil.setBorderRight(BorderStyle.NONE, r1, sh); } catch (Throwable __t) {}
                            try { RegionUtil.setBorderTop(BorderStyle.NONE, r2, sh); } catch (Throwable __t) {}
                            try { RegionUtil.setBorderBottom(BorderStyle.NONE, r2, sh); } catch (Throwable __t) {}
                            try { RegionUtil.setBorderLeft(BorderStyle.NONE, r2, sh); } catch (Throwable __t) {}
                            try { RegionUtil.setBorderRight(BorderStyle.NONE, r2, sh); } catch (Throwable __t) {}
                        }
                    } catch (Throwable __t) { /* ignore */ }
                }

                // Orden de escritura en las celdas fusionadas: J38-K38, L38-M38, J39-K39, L39-M39, J40-K40, L40-M40
                java.util.List<int[]> targets = new java.util.ArrayList<>();
                targets.add(new int[]{37,9}); targets.add(new int[]{37,11});
                targets.add(new int[]{38,9}); targets.add(new int[]{38,11});
                targets.add(new int[]{39,9}); targets.add(new int[]{39,11});

                // Combinar externalApps con un pool de fallback a partir de 'encontrados'
                java.util.List<String> combinedExternalList = new java.util.ArrayList<>();
                for (String s : externalApps) if (!combinedExternalList.contains(s)) combinedExternalList.add(s);
                for (String cand : encontrados) {
                    if (combinedExternalList.size() >= 6) break;
                    if (finalDetected.contains(cand)) continue;
                    if (listaBlanca.contains(cand)) continue;
                    boolean isAv = false;
                    for (String av : avBrands.values()) { if (av.equalsIgnoreCase(cand)) { isAv = true; break; } }
                    if (isAv) continue;
                    if (combinedExternalList.contains(cand)) continue;
                    String low = cand.toLowerCase();
                    // Filtrar con la misma lista de banned
                    boolean isBanned = false;
                    for (String b : bannedSubstrings) { if (low.contains(b)) { isBanned = true; break; } }
                    if (isBanned) continue;
                    combinedExternalList.add(cand);
                }

                // Si aún no hay suficientes, intentar rellenar desde la lista cruda de installedApps
                java.util.List<String> rawCandidates = new java.util.ArrayList<>();
                try {
                    if (appsCsv != null && !appsCsv.isEmpty()) {
                        String[] rawLines = appsCsv.split("\n");
                        for (int ri = 1; ri < rawLines.length; ri++) {
                            String rawLine = rawLines[ri].trim();
                            if (rawLine.isEmpty()) continue;
                            java.util.List<String> colsRaw = InventarioFXBase.parseCsvLineAvanzado(rawLine);
                            if (colsRaw.size() > 0) {
                                String rawName = colsRaw.get(0).trim();
                                if (rawName.isEmpty()) continue;
                                if (!rawCandidates.contains(rawName)) rawCandidates.add(rawName);
                            }
                        }
                    }
                } catch (Throwable __t) { /* ignore parsing raw fallback errors */ }

                for (String raw : rawCandidates) {
                    if (combinedExternalList.size() >= 6) break;
                    if (raw == null) continue;
                    String cand = raw.trim();
                    if (cand.isEmpty()) continue;
                    // evitar duplicados y elementos ya incluidos en finalDetected o whitelist o AV
                    if (combinedExternalList.contains(cand)) continue;
                    if (finalDetected.contains(cand)) continue;
                    boolean skip = false;
                    for (String w : listaBlanca) if (w.equalsIgnoreCase(cand)) { skip = true; break; }
                    if (skip) continue;
                    boolean isAvRaw = false;
                    for (String av : avBrands.values()) { if (av.equalsIgnoreCase(cand)) { isAvRaw = true; break; } }
                    if (isAvRaw) continue;
                    String low = cand.toLowerCase();
                    // Filtrar con la misma lista de banned
                    boolean isBanned = false;
                    for (String b : bannedSubstrings) { if (low.contains(b)) { isBanned = true; break; } }
                    if (isBanned) continue;
                    combinedExternalList.add(cand);
                }

                // Ya no rellenamos con el marcador "Sin programas"; si aún faltan celdas, dejarlas vacías

                // DEBUG: imprimir listas para entender contenido actual
                try {
                    System.out.println("[AppsWriter] externalApps=" + externalApps);
                    System.out.println("[AppsWriter] encontrados=" + encontrados);
                    System.out.println("[AppsWriter] combinedExternalList=" + combinedExternalList);
                    System.out.println("[AppsWriter] rawCandidates=" + rawCandidates);
                } catch (Throwable _t) {}

                // Hoja "Análisis de Aplicaciones" con layout de 3 columnas lado a lado
                try {
                    org.apache.poi.ss.usermodel.Workbook wb = sh.getWorkbook();
                    Sheet an = wb.getSheet("Análisis de Aplicaciones");
                    if (an == null) an = wb.createSheet("Análisis de Aplicaciones");

                    // --- Estilos ---
                    // Estilo encabezado info (negrita)
                    CellStyle infoLabelStyle = wb.createCellStyle();
                    Font infoLabelFont = wb.createFont();
                    infoLabelFont.setBold(true);
                    infoLabelFont.setFontHeightInPoints((short) 10);
                    infoLabelStyle.setFont(infoLabelFont);

                    // Estilo valor info (normal)
                    CellStyle infoValueStyle = wb.createCellStyle();
                    Font infoValueFont = wb.createFont();
                    infoValueFont.setFontHeightInPoints((short) 10);
                    infoValueStyle.setFont(infoValueFont);

                    // Estilo encabezado de sección (negrita, fondo gris claro)
                    CellStyle sectionStyle = wb.createCellStyle();
                    Font sectionFont = wb.createFont();
                    sectionFont.setBold(true);
                    sectionFont.setFontHeightInPoints((short) 10);
                    sectionStyle.setFont(sectionFont);
                    sectionStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    sectionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    // Estilo número (centrado)
                    CellStyle numStyle = wb.createCellStyle();
                    Font numFont = wb.createFont();
                    numFont.setFontHeightInPoints((short) 9);
                    numStyle.setFont(numFont);
                    numStyle.setAlignment(HorizontalAlignment.RIGHT);

                    // Estilo nombre de app
                    CellStyle appStyle = wb.createCellStyle();
                    Font appFont = wb.createFont();
                    appFont.setFontHeightInPoints((short) 9);
                    appStyle.setFont(appFont);

                    // --- Obtener nombre del proyecto ---
                    String proyectoNombre = "";
                    try {
                        proyectoNombre = InventarioFXBase.CURRENT_PROJECT;
                        if (proyectoNombre == null) proyectoNombre = "";
                    } catch (Throwable __t) {}

                    String grupoId = "";
                    try {
                        if (sysInfo != null && sysInfo.idGrupo != null) grupoId = sysInfo.idGrupo;
                    } catch (Throwable __t) {}

                    // --- Fila 0: ORIGEN DE DATOS ---
                    Row r0 = an.createRow(0);
                    Cell c0a = r0.createCell(0);
                    c0a.setCellValue("\uD83D\uDCCB ORIGEN DE DATOS:");
                    c0a.setCellStyle(infoLabelStyle);
                    Cell c0b = r0.createCell(1);
                    c0b.setCellValue("Excel del Inventario (Proyecto: " + proyectoNombre + ")");
                    c0b.setCellStyle(infoValueStyle);

                    // --- Fila 1: GRUPO ID ---
                    Row r1 = an.createRow(1);
                    Cell c1a = r1.createCell(0);
                    c1a.setCellValue("\uD83D\uDD11 GRUPO ID:");
                    c1a.setCellStyle(infoLabelStyle);
                    Cell c1b = r1.createCell(1);
                    c1b.setCellValue(grupoId);
                    c1b.setCellStyle(infoValueStyle);

                    // --- Fila 2: TOTAL ORIGINALES ---
                    Row r2 = an.createRow(2);
                    Cell c2a = r2.createCell(0);
                    c2a.setCellValue("\uD83D\uDCCA TOTAL ORIGINALES:");
                    c2a.setCellStyle(infoLabelStyle);
                    Cell c2b = r2.createCell(2);
                    c2b.setCellValue(rawCandidates.size());
                    c2b.setCellStyle(infoValueStyle);

                    // --- Fila 4: Encabezados de sección ---
                    Row r4 = an.createRow(4);
                    Cell secAll = r4.createCell(0);
                    secAll.setCellValue("\uD83D\uDCC1 TODAS LAS APLICACIONES");
                    secAll.setCellStyle(sectionStyle);
                    r4.createCell(1).setCellStyle(sectionStyle); // extender fondo

                    Cell secExt = r4.createCell(3);
                    secExt.setCellValue("⚙\uFE0F PROGRAMAS EXTERNOS");
                    secExt.setCellStyle(sectionStyle);
                    r4.createCell(4).setCellStyle(sectionStyle);

                    Cell secEnc = r4.createCell(6);
                    secEnc.setCellValue("✅ APLICACIONES ENCONTRADAS");
                    secEnc.setCellStyle(sectionStyle);
                    r4.createCell(7).setCellStyle(sectionStyle);
                    r4.createCell(8).setCellStyle(sectionStyle);

                    // --- Construir lista de "encontrados" ordenada ---
                    java.util.List<String> encontradosList = new java.util.ArrayList<>(encontrados);

                    // --- Datos: 3 columnas lado a lado empezando en fila 5 ---
                    int maxRows = Math.max(rawCandidates.size(),
                                  Math.max(externalApps.size(), encontradosList.size()));

                    for (int i = 0; i < maxRows; i++) {
                        Row dataRow = an.createRow(5 + i);

                        // Columna A-B: TODAS LAS APLICACIONES
                        if (i < rawCandidates.size()) {
                            Cell cn = dataRow.createCell(0);
                            cn.setCellValue((i + 1) + ".");
                            cn.setCellStyle(numStyle);
                            Cell cv = dataRow.createCell(1);
                            cv.setCellValue(rawCandidates.get(i));
                            cv.setCellStyle(appStyle);
                        }

                        // Columna D-E: PROGRAMAS EXTERNOS
                        if (i < externalApps.size()) {
                            Cell cn = dataRow.createCell(3);
                            cn.setCellValue((i + 1) + ".");
                            cn.setCellStyle(numStyle);
                            Cell cv = dataRow.createCell(4);
                            cv.setCellValue(externalApps.get(i));
                            cv.setCellStyle(appStyle);
                        }

                        // Columna G-H: APLICACIONES ENCONTRADAS
                        if (i < encontradosList.size()) {
                            Cell cn = dataRow.createCell(6);
                            cn.setCellValue((i + 1) + ".");
                            cn.setCellStyle(numStyle);
                            Cell cv = dataRow.createCell(7);
                            cv.setCellValue(encontradosList.get(i));
                            cv.setCellStyle(appStyle);
                        }
                    }

                    // Autoajustar columnas principales
                    int[] autoCols = {0, 1, 3, 4, 6, 7};
                    for (int c : autoCols) {
                        try { an.autoSizeColumn(c); } catch (Throwable __t) {}
                    }
                    // Columnas separadoras más angostas
                    try { an.setColumnWidth(2, 512); } catch (Throwable __t) {}
                    try { an.setColumnWidth(5, 512); } catch (Throwable __t) {}

                } catch (Throwable _t) { /* ignore analysis sheet errors */ }

                for (int ti = 0; ti < targets.size(); ti++) {
                    int rr = targets.get(ti)[0];
                    int cc = targets.get(ti)[1];
                    Row row = sh.getRow(rr); if (row == null) row = sh.createRow(rr);
                    Cell cell = row.getCell(cc); if (cell == null) cell = row.createCell(cc);
                    String v = combinedExternalList.get(ti);
                    cell.setCellValue(v != null ? v : "");
                    if (extStyle != null) try { cell.setCellStyle(extStyle); } catch (Exception _e) {}
                }

                // Dibujar únicamente el borde exterior alrededor del área J38:M40
                try {
                    org.apache.poi.ss.util.CellRangeAddress outer = new org.apache.poi.ss.util.CellRangeAddress(37, 39, 9, 12);
                    try { RegionUtil.setBorderTop(BorderStyle.THIN, outer, sh); } catch (Throwable _t) {}
                    try { RegionUtil.setBorderBottom(BorderStyle.THIN, outer, sh); } catch (Throwable _t) {}
                    try { RegionUtil.setBorderLeft(BorderStyle.THIN, outer, sh); } catch (Throwable _t) {}
                    try { RegionUtil.setBorderRight(BorderStyle.THIN, outer, sh); } catch (Throwable _t) {}
                } catch (Throwable _t) { /* ignore outer border errors */ }

            } catch (Throwable _t) {
                // evitar bloqueo en guardado
            }
        } catch (Throwable _e) {
            // evitar que falle el guardado del workbook
        }
    }
}
