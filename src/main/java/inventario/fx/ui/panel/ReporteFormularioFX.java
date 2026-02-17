package inventario.fx.ui.panel;
import inventario.fx.model.TemaManager;
import inventario.fx.model.InventarioFXBase;
import inventario.fx.icons.IconosSVG;
import inventario.fx.ui.dialog.DialogosFX;
import inventario.fx.ui.firma.FirmaDigitalCanvas;
import inventario.fx.excel.GeneratedTemplate;
import inventario.fx.excel.ExcelToPdfConverter;
import inventario.fx.util.AppLogger;

import inventario.fx.util.SVGUtil;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.function.Consumer;
import inventario.fx.util.AnimacionesFX;

/**
 * Formulario elegante y minimalista para generar reportes de mantenimiento.
 * Diseño premium con tema oscuro coherente con el Dashboard.
 */
public class ReporteFormularioFX {

    // ════════════════════════════════════════════════════════════════════════════
    // CLASE DE DATOS DEL REPORTE
    // ════════════════════════════════════════════════════════════════════════════
    
    public static class DatosReporte implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        // Tipo solicitud y fecha
        public String tipoSolicitud = "";
        public String dia = "";
        public String mes = "";
        public String anio = "";
        public String ticket = "";
        
        // Datos usuario
        public String ciudad = "";
        public String direccion = "";
        public String nombre = "";
        public String correo = "";
        public String tecnico = "";
        public String sede = "";
        
        // Hardware
        public String tipoDispositivo = "";
        public String marca = "";
        public String modelo = "";
        public String serial = "";
        public String placa = "";
        public String condiciones = "";
        
        // PC
        public String pcEnciende = "";
        public String discoDuro = "";
        public String cddvd = "";
        public String botonesPC = "";
        public String condicionesPC = "";
        public String procesador = "";
        public String memoriaRAM = "";
        public String discoDuroCapacidad = "";
        
        // Monitor
        public String monitorEnciende = "";
        public String pantalla = "";
        public String onlyOne = "";
        public String botonesMonitor = "";
        public String condicionesMonitor = "";
        
        // Teclado
        public String tecladoEnciende = "";
        public String tecladoFunciona = "";
        public String botonesTeclado = "";
        public String condicionesTeclado = "";
        
        // Mouse
        public String mouseEnciende = "";
        public String mouseFunciona = "";
        public String botonesMouse = "";
        public String condicionesMouse = "";
        
        // Software
        public String programasBasicos = "";
        public String otrosProgramas = "";
        
        // Procedimiento y observaciones
        public String trabajoRealizado = "";
        public String observaciones = "";
        
        // Firmas
        public String firmaTecnico = "";
        public String cedulaTecnico = "";
        public String firmaFuncionario = "";
        public String cedulaFuncionario = "";
        
        // Firmas digitales (imágenes)
        public String firmaTecnicoImagenPath = "";
        public String firmaFuncionarioImagenPath = "";
        
        // Imagen del proyecto
        public String proyectoImagenPath = "";
        
        // Proyecto asociado
        public String proyectoNombre = "";
    }

    // ════════════════════════════════════════════════════════════════════════════
    // MÉTODO PARA OBTENER DATOS (usado por GeneratedTemplate)
    // ════════════════════════════════════════════════════════════════════════════
    
    public static DatosReporte obtenerDatos() {
        DatosReporte datos = new DatosReporte();
        
        datos.tipoSolicitud = System.getProperty("reporte.tipoSolicitud", "");
        datos.dia = System.getProperty("reporte.dia", "");
        datos.mes = System.getProperty("reporte.mes", "");
        datos.anio = System.getProperty("reporte.anio", "");
        datos.ticket = System.getProperty("reporte.ticket", "");
        
        datos.ciudad = System.getProperty("reporte.ciudad", "");
        datos.direccion = System.getProperty("reporte.direccion", "");
        datos.nombre = System.getProperty("reporte.nombre", "");
        datos.correo = System.getProperty("reporte.correo", "");
        datos.tecnico = System.getProperty("reporte.tecnico", "");
        datos.sede = System.getProperty("reporte.sede", "");
        
        datos.tipoDispositivo = System.getProperty("reporte.tipoDispositivo", "");
        datos.marca = System.getProperty("reporte.marca", "");
        datos.modelo = System.getProperty("reporte.modelo", "");
        datos.serial = System.getProperty("reporte.serial", "");
        datos.placa = System.getProperty("reporte.placa", "");
        datos.condiciones = System.getProperty("reporte.condicionesHW", "");
        
        datos.pcEnciende = System.getProperty("reporte.pcEnciende", "");
        datos.discoDuro = System.getProperty("reporte.discoDuro", "");
        datos.cddvd = System.getProperty("reporte.cdDvd", "");
        datos.botonesPC = System.getProperty("reporte.botonesPC", "");
        datos.condicionesPC = System.getProperty("reporte.condicionesPC", "");
        datos.procesador = System.getProperty("reporte.procesador", "");
        datos.memoriaRAM = System.getProperty("reporte.memoriaRAM", "");
        datos.discoDuroCapacidad = System.getProperty("reporte.discoDuroCapacidad", "");
        
        datos.monitorEnciende = System.getProperty("reporte.monitorEnciende", "");
        datos.pantalla = System.getProperty("reporte.pantalla", "");
        datos.onlyOne = System.getProperty("reporte.onlyOne", "");
        datos.botonesMonitor = System.getProperty("reporte.botonesMonitor", "");
        datos.condicionesMonitor = System.getProperty("reporte.condicionesMonitor", "");
        
        datos.tecladoEnciende = System.getProperty("reporte.tecladoEnciende", "");
        datos.tecladoFunciona = System.getProperty("reporte.tecladoFunciona", "");
        datos.botonesTeclado = System.getProperty("reporte.botonesTeclado", "");
        datos.condicionesTeclado = System.getProperty("reporte.condicionesTeclado", "");
        
        datos.mouseEnciende = System.getProperty("reporte.mouseEnciende", "");
        datos.mouseFunciona = System.getProperty("reporte.mouseFunciona", "");
        datos.botonesMouse = System.getProperty("reporte.botonesMouse", "");
        datos.condicionesMouse = System.getProperty("reporte.condicionesMouse", "");
        
        datos.programasBasicos = System.getProperty("reporte.programasBasicos", "");
        datos.otrosProgramas = System.getProperty("reporte.otrosProgramas", "");
        
        datos.trabajoRealizado = System.getProperty("reporte.procedimiento", "");
        datos.observaciones = System.getProperty("reporte.observaciones", "");
        
        datos.firmaTecnico = System.getProperty("reporte.firmaTecnico", "");
        datos.cedulaTecnico = System.getProperty("reporte.cedulaTecnico", "");
        datos.firmaFuncionario = System.getProperty("reporte.firmaFuncionario", "");
        datos.cedulaFuncionario = System.getProperty("reporte.cedulaFuncionario", "");
        
        // Rutas de imágenes de firmas
        datos.firmaTecnicoImagenPath = System.getProperty("reporte.firmaTecnicoImagenPath", "");
        datos.firmaFuncionarioImagenPath = System.getProperty("reporte.firmaFuncionarioImagenPath", "");
        
        // Imagen del proyecto
        datos.proyectoImagenPath = System.getProperty("reporte.proyectoImagenPath", "");
        
        return datos;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PALETA DE COLORES - USANDO TEMA MANAGER (SOPORTE LIGHT/DARK)
    // ════════════════════════════════════════════════════════════════════════════
    private static String COLOR_BG_DARK() { return TemaManager.getBgDark(); }
    private static String COLOR_BG() { return TemaManager.getBg(); }
    private static String COLOR_BG_LIGHT() { return TemaManager.getBgLight(); }
    private static String COLOR_SURFACE() { return TemaManager.getSurface(); }
    private static String COLOR_BORDER() { return TemaManager.getBorder(); }
    private static final String COLOR_PRIMARY = TemaManager.COLOR_PRIMARY;
    private static final String COLOR_SUCCESS = TemaManager.COLOR_SUCCESS;
    private static final String COLOR_INFO = TemaManager.COLOR_INFO;
    private static String COLOR_TEXT() { return TemaManager.getText(); }
    private static String COLOR_TEXT_SECONDARY() { return TemaManager.getTextSecondary(); }
    private static String COLOR_TEXT_MUTED() { return TemaManager.getTextMuted(); }

    // ════════════════════════════════════════════════════════════════════════════
    // CAMPOS DEL FORMULARIO
    // ════════════════════════════════════════════════════════════════════════════
    
    // Tipo de solicitud y fecha
    private static ComboBox<String> cmbTipoSolicitud;
    private static TextField txtDia, txtMes, txtAnio;
    private static TextField txtTicket;
    
    // Componentes de firma digital
    private static FirmaDigitalCanvas canvasFirmaTecnico;
    private static FirmaDigitalCanvas canvasFirmaFuncionario;
    
    // Datos del usuario
    private static TextField txtCiudad, txtDireccion, txtNombre, txtCorreo, txtTecnico, txtSede;
    
    // Descripción del Hardware
    private static TextField txtTipoDispositivo, txtMarca, txtModelo, txtSerial, txtPlaca, txtCondicionesHW;
    
    // PC
    private static ComboBox<String> cmbPcEnciende, cmbDiscoDuro, cmbCdDvd, cmbBotonesPC;
    private static TextField txtCondicionesPC, txtProcesador, txtMemoriaRam, txtDiscoDuroTxt;
    
    // Monitor
    private static ComboBox<String> cmbMonitorEnciende, cmbPantalla, cmbOnlyOne, cmbBotonesMonitor;
    private static TextField txtCondicionesMonitor;
    
    // Teclado
    private static ComboBox<String> cmbTecladoEnciende, cmbTecladoFunciona, cmbBotonesTeclado;
    private static TextField txtCondicionesTeclado;
    
    // Mouse
    private static ComboBox<String> cmbMouseEnciende, cmbMouseFunciona, cmbBotonesMouse;
    private static TextField txtCondicionesMouse;
    
    // Software
    private static TextArea txtProgramasBasicos, txtOtrosProgramas;
    
    // Procedimiento y Observaciones
    private static TextArea txtProcedimiento, txtObservaciones;
    
    // Firmas
    private static TextField txtFirmaTecnico, txtCedulaTecnico;
    private static TextField txtFirmaFuncionario, txtCedulaFuncionario;

    private static Stage formStage;
    private static ScrollPane scrollPaneFormulario;
    
    // Modo de operación
    private static boolean modoGestion = false;
    private static Consumer<DatosReporte> callbackDatos = null;
    private static DatosReporte datosEdicion = null;
    private static boolean guardadoExitoso = false;

    // ════════════════════════════════════════════════════════════════════════════
    // MOSTRAR PARA GESTIÓN (SIN EXPORTAR ARCHIVO)
    // ════════════════════════════════════════════════════════════════════════════
    
    public static void mostrarParaGestion(Stage parent, Consumer<DatosReporte> callback) {
        modoGestion = true;
        callbackDatos = callback;
        datosEdicion = null;
        guardadoExitoso = false;
        borradorRestaurado = null;
        mostrarFormularioInterno(parent);
    }
    
    public static void mostrarParaEdicion(Stage parent, DatosReporte datos, Consumer<DatosReporte> callback) {
        modoGestion = true;
        callbackDatos = callback;
        datosEdicion = datos;
        guardadoExitoso = false;
        borradorRestaurado = null;
        mostrarFormularioInterno(parent);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // MOSTRAR FORMULARIO (ORIGINAL CON EXPORTACIÓN)
    // ════════════════════════════════════════════════════════════════════════════
    
    public static void mostrar(Stage parent, Consumer<Boolean> callback) {
        modoGestion = false;
        callbackDatos = null;
        datosEdicion = null;
        guardadoExitoso = false;
        borradorRestaurado = null;
        mostrarFormularioInterno(parent);
    }
    
    private static void mostrarFormularioInterno(Stage parent) {
        formStage = new Stage();
        formStage.initModality(Modality.NONE); // Ventana completamente independiente
        // Sin initOwner — aparece como ventana separada en la barra de tareas
        formStage.setTitle("SELCOMP — Reporte de Mantenimiento");
        formStage.setWidth(1100);
        formStage.setHeight(1000);
        formStage.setMinWidth(900);
        formStage.setMinHeight(850);

        // Icono Selcomp
        try {
            InputStream iconStream = ReporteFormularioFX.class.getResourceAsStream("/images/Selcomp_logito.png");
            if (iconStream != null) {
                formStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");

        // Header
        root.setTop(crearHeader());

        // Contenido con scroll
        scrollPaneFormulario = new ScrollPane();
        scrollPaneFormulario.setFitToWidth(true);
        scrollPaneFormulario.setStyle(
            "-fx-background: " + COLOR_BG_DARK() + ";" +
            "-fx-background-color: " + COLOR_BG_DARK() + ";"
        );
        scrollPaneFormulario.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPaneFormulario.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox contenido = crearContenidoFormulario();
        scrollPaneFormulario.setContent(contenido);
        root.setCenter(scrollPaneFormulario);

        // Footer con botones según modo
        if (modoGestion) {
            root.setBottom(crearFooterGestion());
        } else {
            root.setBottom(crearFooter(null));
        }

        Scene scene = new Scene(root);
        
        // Animación de entrada suave con scale + fade + slide
        root.setOpacity(0);
        root.setScaleX(0.95);
        root.setScaleY(0.95);
        root.setTranslateY(15);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(450), root);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), root);
        slideIn.setToY(0);
        slideIn.setInterpolator(AnimacionesFX.EASE_OUT_CUBIC);

        formStage.setScene(scene);
        formStage.centerOnScreen();
        formStage.show();
        new ParallelTransition(fadeIn, scaleIn, slideIn).play();

        // Auto-guardar borrador silenciosamente al cerrar
        formStage.setOnCloseRequest(e -> guardarBorradorAlCerrar());

        // Inicializar fecha actual o cargar datos de edición
        if (datosEdicion != null) {
            cargarDatosEdicion();
        } else {
            LocalDate hoy = LocalDate.now();
            txtDia.setText(String.valueOf(hoy.getDayOfMonth()));
            txtMes.setText(String.valueOf(hoy.getMonthValue()));
            txtAnio.setText(String.valueOf(hoy.getYear()));
            
            // Cargar datos del sistema en segundo plano
            cargarDatosSistemaAsync();
        }
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // FOOTER PARA MODO GESTIÓN (SIN EXPORTACIÓN)
    // ════════════════════════════════════════════════════════════════════════════
    
    private static HBox crearFooterGestion() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15, 30, 20, 30));
        footer.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 1 0 0 0;"
        );

        // Botón Ver Borradores (izquierda)
        Button btnBorradores = crearBotonBorradores();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnLimpiar = crearBotonSecundario("Limpiar");
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        Button btnCancelar = crearBotonSecundario("Cancelar");
        btnCancelar.setOnAction(e -> {
            formStage.close();
            if (callbackDatos != null) callbackDatos.accept(null);
        });

        Button btnGuardar = crearBotonPrimarioConIcono("Guardar en Sistema", IconosSVG.guardar("#FFFFFF", 20));
        btnGuardar.setOnAction(e -> {
            System.out.println("[ReporteFormularioFX] Botón Guardar presionado");
            System.out.println("[ReporteFormularioFX] Nombre: '" + txtNombre.getText() + "'");
            System.out.println("[ReporteFormularioFX] Técnico: '" + txtTecnico.getText() + "'");
            if (validarFormulario()) {
                System.out.println("[ReporteFormularioFX] Validación OK, guardando...");
                guardarEnSistema();
            } else {
                System.out.println("[ReporteFormularioFX] Validación FALLÓ");
            }
        });

        footer.getChildren().addAll(btnBorradores, spacer, btnLimpiar, btnCancelar, btnGuardar);
        return footer;
    }
    
    private static void guardarEnSistema() {
        System.out.println("[ReporteFormularioFX] guardarEnSistema() iniciado");
        
        DatosReporte datos = recopilarDatosFormulario();
        System.out.println("[ReporteFormularioFX] Datos recopilados, ticket: " + datos.ticket);
        System.out.println("[ReporteFormularioFX] Proyecto: " + datos.proyectoNombre);
        
        // Marcar que se guardó exitosamente ANTES de cerrar para evitar que
        // guardarBorradorAlCerrar() interfiera con el guardado
        guardadoExitoso = true;
        
        // Cerrar formulario PRIMERO
        formStage.close();
        eliminarBorrador();
        
        // Luego enviar datos al callback (esto garantiza que el callback se ejecute)
        if (callbackDatos != null) {
            System.out.println("[ReporteFormularioFX] Callback existe, enviando datos");
            callbackDatos.accept(datos);
        } else {
            System.out.println("[ReporteFormularioFX] WARNING: callbackDatos es NULL!");
        }
    }
    
    // Convertir valor a valor limpio para guardar (ahora ya son limpios)
    private static String limpiarValorSiNo(String valor) {
        if (valor == null) return "";
        if (valor.contains("SI")) return "SI";
        if (valor.contains("NO")) return "NO";
        if (valor.contains("N/A")) return "N/A";
        return valor;
    }
    
    // Convertir valor guardado a valor para mostrar en ComboBox
    private static String convertirValorSiNo(String valorGuardado) {
        if (valorGuardado == null || valorGuardado.isEmpty()) return "SI";
        if (valorGuardado.equals("SI") || valorGuardado.contains("SI")) return "SI";
        if (valorGuardado.equals("NO") || valorGuardado.contains("NO")) return "NO";
        if (valorGuardado.equals("N/A") || valorGuardado.contains("N/A")) return "N/A";
        return "SI";
    }
    
    private static DatosReporte recopilarDatosFormulario() {
        DatosReporte datos = new DatosReporte();
        
        datos.tipoSolicitud = cmbTipoSolicitud.getValue();
        datos.dia = txtDia.getText();
        datos.mes = txtMes.getText();
        datos.anio = txtAnio.getText();
        datos.ticket = txtTicket.getText();
        
        datos.ciudad = txtCiudad.getText();
        datos.direccion = txtDireccion.getText();
        datos.nombre = txtNombre.getText();
        datos.correo = txtCorreo.getText();
        datos.tecnico = txtTecnico.getText();
        datos.sede = txtSede.getText();
        
        datos.tipoDispositivo = txtTipoDispositivo.getText();
        datos.marca = txtMarca.getText();
        datos.modelo = txtModelo.getText();
        datos.serial = txtSerial.getText();
        datos.placa = txtPlaca.getText();
        datos.condiciones = txtCondicionesHW.getText();
        
        // Guardar valores limpios (sin iconos) para compatibilidad
        datos.pcEnciende = limpiarValorSiNo(cmbPcEnciende.getValue());
        datos.discoDuro = limpiarValorSiNo(cmbDiscoDuro.getValue());
        datos.cddvd = limpiarValorSiNo(cmbCdDvd.getValue());
        datos.botonesPC = limpiarValorSiNo(cmbBotonesPC.getValue());
        datos.condicionesPC = txtCondicionesPC.getText();
        datos.procesador = txtProcesador.getText();
        datos.memoriaRAM = txtMemoriaRam.getText();
        datos.discoDuroCapacidad = txtDiscoDuroTxt.getText();
        
        datos.monitorEnciende = limpiarValorSiNo(cmbMonitorEnciende.getValue());
        datos.pantalla = limpiarValorSiNo(cmbPantalla.getValue());
        datos.onlyOne = limpiarValorSiNo(cmbOnlyOne.getValue());
        datos.botonesMonitor = limpiarValorSiNo(cmbBotonesMonitor.getValue());
        datos.condicionesMonitor = txtCondicionesMonitor.getText();
        
        datos.tecladoEnciende = limpiarValorSiNo(cmbTecladoEnciende.getValue());
        datos.tecladoFunciona = limpiarValorSiNo(cmbTecladoFunciona.getValue());
        datos.botonesTeclado = limpiarValorSiNo(cmbBotonesTeclado.getValue());
        datos.condicionesTeclado = txtCondicionesTeclado.getText();
        
        // Mouse ya no se usa en la UI, pero mantenemos compatibilidad
        datos.mouseEnciende = "";
        datos.mouseFunciona = "";
        datos.botonesMouse = "";
        datos.condicionesMouse = "";
        
        datos.programasBasicos = txtProgramasBasicos.getText();
        datos.otrosProgramas = txtOtrosProgramas.getText();
        
        datos.trabajoRealizado = txtProcedimiento.getText();
        datos.observaciones = txtObservaciones.getText();
        
        datos.firmaTecnico = txtFirmaTecnico.getText();
        datos.cedulaTecnico = txtCedulaTecnico.getText();
        datos.firmaFuncionario = txtFirmaFuncionario.getText();
        datos.cedulaFuncionario = txtCedulaFuncionario.getText();
        
        // Guardar firmas digitales como imágenes
        System.out.println("[ReporteFormulario] Verificando firmas digitales...");
        System.out.println("[ReporteFormulario] Canvas técnico existe: " + (canvasFirmaTecnico != null));
        System.out.println("[ReporteFormulario] Canvas funcionario existe: " + (canvasFirmaFuncionario != null));
        
        if (canvasFirmaTecnico != null && canvasFirmaTecnico.tieneFirma()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            datos.firmaTecnicoImagenPath = canvasFirmaTecnico.guardarFirma("firma_tecnico_" + timestamp);
            System.out.println("[ReporteFormulario] Firma técnico guardada: " + datos.firmaTecnicoImagenPath);
        } else {
            System.out.println("[ReporteFormulario] NO se guardó firma técnico - " + 
                (canvasFirmaTecnico == null ? "canvas es null" : "no tiene firma"));
        }
        
        if (canvasFirmaFuncionario != null && canvasFirmaFuncionario.tieneFirma()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            datos.firmaFuncionarioImagenPath = canvasFirmaFuncionario.guardarFirma("firma_funcionario_" + timestamp);
            System.out.println("[ReporteFormulario] Firma funcionario guardada: " + datos.firmaFuncionarioImagenPath);
        } else {
            System.out.println("[ReporteFormulario] NO se guardó firma funcionario - " + 
                (canvasFirmaFuncionario == null ? "canvas es null" : "no tiene firma"));
        }
        
        // Obtener proyecto: en edición, preservar datos originales; en creación, usar propiedad del sistema
        if (datosEdicion != null) {
            datos.proyectoNombre = datosEdicion.proyectoNombre != null ? datosEdicion.proyectoNombre : System.getProperty("reporte.proyectoNombre", "");
            datos.proyectoImagenPath = datosEdicion.proyectoImagenPath != null ? datosEdicion.proyectoImagenPath : System.getProperty("reporte.proyectoImagenPath", "");
        } else {
            datos.proyectoNombre = System.getProperty("reporte.proyectoNombre", "");
            datos.proyectoImagenPath = System.getProperty("reporte.proyectoImagenPath", "");
        }
        
        System.out.println("[ReporteFormulario] Guardando reporte para proyecto: " + datos.proyectoNombre);
        System.out.println("[ReporteFormulario] Imagen del proyecto: " + 
            (datos.proyectoImagenPath != null && !datos.proyectoImagenPath.isEmpty() ? datos.proyectoImagenPath : "NO ESTABLECIDA"));
        
        return datos;
    }
    
    private static void cargarDatosEdicion() {
        if (datosEdicion == null) return;
        
        cmbTipoSolicitud.setValue(datosEdicion.tipoSolicitud != null ? datosEdicion.tipoSolicitud : "MANTENIMIENTO PREVENTIVO");
        txtDia.setText(datosEdicion.dia != null ? datosEdicion.dia : "");
        txtMes.setText(datosEdicion.mes != null ? datosEdicion.mes : "");
        txtAnio.setText(datosEdicion.anio != null ? datosEdicion.anio : "");
        txtTicket.setText(datosEdicion.ticket != null ? datosEdicion.ticket : "");
        
        txtCiudad.setText(datosEdicion.ciudad != null ? datosEdicion.ciudad : "");
        txtDireccion.setText(datosEdicion.direccion != null ? datosEdicion.direccion : "");
        txtNombre.setText(datosEdicion.nombre != null ? datosEdicion.nombre : "");
        txtCorreo.setText(datosEdicion.correo != null ? datosEdicion.correo : "");
        txtTecnico.setText(datosEdicion.tecnico != null ? datosEdicion.tecnico : "");
        txtSede.setText(datosEdicion.sede != null ? datosEdicion.sede : "");
        
        txtTipoDispositivo.setText(datosEdicion.tipoDispositivo != null ? datosEdicion.tipoDispositivo : "");
        txtMarca.setText(datosEdicion.marca != null ? datosEdicion.marca : "");
        txtModelo.setText(datosEdicion.modelo != null ? datosEdicion.modelo : "");
        txtSerial.setText(datosEdicion.serial != null ? datosEdicion.serial : "");
        txtPlaca.setText(datosEdicion.placa != null ? datosEdicion.placa : "");
        txtCondicionesHW.setText(datosEdicion.condiciones != null ? datosEdicion.condiciones : "");
        
        // Cargar valores para los ComboBox Sí/No
        if (datosEdicion.pcEnciende != null) cmbPcEnciende.setValue(convertirValorSiNo(datosEdicion.pcEnciende));
        if (datosEdicion.discoDuro != null) cmbDiscoDuro.setValue(convertirValorSiNo(datosEdicion.discoDuro));
        if (datosEdicion.cddvd != null) cmbCdDvd.setValue(convertirValorSiNo(datosEdicion.cddvd));
        if (datosEdicion.botonesPC != null) cmbBotonesPC.setValue(convertirValorSiNo(datosEdicion.botonesPC));
        txtCondicionesPC.setText(datosEdicion.condicionesPC != null ? datosEdicion.condicionesPC : "");
        txtProcesador.setText(datosEdicion.procesador != null ? datosEdicion.procesador : "");
        txtMemoriaRam.setText(datosEdicion.memoriaRAM != null ? datosEdicion.memoriaRAM : "");
        txtDiscoDuroTxt.setText(datosEdicion.discoDuroCapacidad != null ? datosEdicion.discoDuroCapacidad : "");
        
        if (datosEdicion.monitorEnciende != null) cmbMonitorEnciende.setValue(convertirValorSiNo(datosEdicion.monitorEnciende));
        if (datosEdicion.pantalla != null) cmbPantalla.setValue(convertirValorSiNo(datosEdicion.pantalla));
        if (datosEdicion.onlyOne != null) cmbOnlyOne.setValue(convertirValorSiNo(datosEdicion.onlyOne));
        if (datosEdicion.botonesMonitor != null) cmbBotonesMonitor.setValue(convertirValorSiNo(datosEdicion.botonesMonitor));
        txtCondicionesMonitor.setText(datosEdicion.condicionesMonitor != null ? datosEdicion.condicionesMonitor : "");
        
        if (datosEdicion.tecladoEnciende != null) cmbTecladoEnciende.setValue(convertirValorSiNo(datosEdicion.tecladoEnciende));
        if (datosEdicion.tecladoFunciona != null) cmbTecladoFunciona.setValue(convertirValorSiNo(datosEdicion.tecladoFunciona));
        if (datosEdicion.botonesTeclado != null) cmbBotonesTeclado.setValue(convertirValorSiNo(datosEdicion.botonesTeclado));
        txtCondicionesTeclado.setText(datosEdicion.condicionesTeclado != null ? datosEdicion.condicionesTeclado : "");
        
        // Mouse ya no se usa pero mantenemos compatibilidad para datos antiguos
        // Los campos de mouse siguen existiendo pero no se muestran en la UI
        
        txtProgramasBasicos.setText(datosEdicion.programasBasicos != null ? datosEdicion.programasBasicos : "");
        txtOtrosProgramas.setText(datosEdicion.otrosProgramas != null ? datosEdicion.otrosProgramas : "");
        
        txtProcedimiento.setText(datosEdicion.trabajoRealizado != null ? datosEdicion.trabajoRealizado : "");
        txtObservaciones.setText(datosEdicion.observaciones != null ? datosEdicion.observaciones : "");
        
        txtFirmaTecnico.setText(datosEdicion.firmaTecnico != null ? datosEdicion.firmaTecnico : "");
        txtCedulaTecnico.setText(datosEdicion.cedulaTecnico != null ? datosEdicion.cedulaTecnico : "");
        txtFirmaFuncionario.setText(datosEdicion.firmaFuncionario != null ? datosEdicion.firmaFuncionario : "");
        txtCedulaFuncionario.setText(datosEdicion.cedulaFuncionario != null ? datosEdicion.cedulaFuncionario : "");
        
        // Cargar firmas digitales
        if (datosEdicion.firmaTecnicoImagenPath != null && !datosEdicion.firmaTecnicoImagenPath.isEmpty()) {
            if (canvasFirmaTecnico != null) {
                canvasFirmaTecnico.cargarFirmaDesdeArchivo(datosEdicion.firmaTecnicoImagenPath);
            }
        }
        
        if (datosEdicion.firmaFuncionarioImagenPath != null && !datosEdicion.firmaFuncionarioImagenPath.isEmpty()) {
            if (canvasFirmaFuncionario != null) {
                canvasFirmaFuncionario.cargarFirmaDesdeArchivo(datosEdicion.firmaFuncionarioImagenPath);
            }
        }
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // CARGAR DATOS DEL SISTEMA (ASÍNCRONO)
    // ════════════════════════════════════════════════════════════════════════════
    
    private static void cargarDatosSistemaAsync() {
        // Ejecutar en hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                // Recopilar información del sistema
                InventarioFXBase.InfoPC info = InventarioFXBase.recopilarInfo();
                
                // Actualizar UI en el hilo de JavaFX
                javafx.application.Platform.runLater(() -> {
                    cargarDatosEnFormulario(info);
                });
                
                // Cargar programas en segundo plano (esto toma más tiempo)
                cargarProgramasAsync();
                
            } catch (Exception e) {
                System.err.println("Error al cargar datos del sistema: " + e.getMessage());
            }
        }, "Reporte-CargarInfoSistema").start();
    }
    
    private static void cargarProgramasAsync() {
        try {
            // Obtener nombre del proyecto actual desde la propiedad del sistema
            String proyectoNombre = System.getProperty("reporte.proyectoNombre", "");
            
            if (proyectoNombre.isEmpty()) {
                System.err.println("[ReporteFormulario] No hay proyecto seleccionado para cargar aplicaciones");
                return;
            }
            
            // Obtener ruta del Excel del proyecto
            java.nio.file.Path rutaExcel = InventarioFXBase.obtenerRutaExcel(proyectoNombre);
            if (rutaExcel == null || !java.nio.file.Files.exists(rutaExcel)) {
                System.err.println("[ReporteFormulario] No se encontró el archivo Excel del proyecto: " + proyectoNombre);
                return;
            }
            
            System.out.println("[ReporteFormulario] Leyendo aplicaciones del Excel: " + rutaExcel);
            
            // Abrir Excel cifrado
            org.apache.poi.xssf.usermodel.XSSFWorkbook wb = InventarioFXBase.abrirCifradoProyecto(rutaExcel);
            if (wb == null) {
                System.err.println("[ReporteFormulario] No se pudo abrir el Excel cifrado");
                return;
            }
            
            // Obtener hoja de aplicaciones instaladas
            org.apache.poi.ss.usermodel.Sheet hojaApps = wb.getSheet("InstalledApps");
            if (hojaApps == null || hojaApps.getLastRowNum() < 2) {
                System.err.println("[ReporteFormulario] No hay datos de aplicaciones en el Excel");
                wb.close();
                return;
            }
            
            // Detectar columnas importantes (ID Grupo, Aplicación)
            org.apache.poi.ss.usermodel.Row headerRow = hojaApps.getRow(1);
            int colIdGrupo = 0; // Por defecto columna 0
            int colApp = 4; // Por defecto columna 4
            
            if (headerRow != null) {
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(c);
                    if (cell != null) {
                        String valor = cell.getStringCellValue().toLowerCase().trim();
                        if (valor.contains("id") && valor.contains("grupo")) {
                            colIdGrupo = c;
                        } else if (valor.contains("aplicación") || valor.contains("aplicacion") || valor.equals("app")) {
                            colApp = c;
                        }
                    }
                }
            }
            
            // Buscar el ID del último grupo (inventario más reciente)
            String ultimoIdGrupo = null;
            for (int i = hojaApps.getLastRowNum(); i >= 2; i--) {
                org.apache.poi.ss.usermodel.Row row = hojaApps.getRow(i);
                if (row == null) continue;
                
                org.apache.poi.ss.usermodel.Cell cellId = row.getCell(colIdGrupo);
                if (cellId != null && cellId.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                    String idGrupo = cellId.getStringCellValue().trim();
                    if (!idGrupo.isEmpty() && !idGrupo.equals("🚩 INICIO") && !idGrupo.equals("🏁 FIN")) {
                        ultimoIdGrupo = idGrupo;
                        break;
                    }
                }
            }
            
            if (ultimoIdGrupo == null) {
                System.err.println("[ReporteFormulario] No se encontró ningún ID de grupo válido");
                wb.close();
                return;
            }
            
            System.out.println("[ReporteFormulario] Usando aplicaciones del grupo: " + ultimoIdGrupo);
            
            // Procesar aplicaciones desde el Excel
            StringBuilder programasBasicos = new StringBuilder();
            StringBuilder otrosProgramas = new StringBuilder();
            java.util.Set<String> appsYaAgregadas = new java.util.HashSet<>();
            
            // Leer todas las filas (saltar fila 0 título y fila 1 encabezados)
            for (int i = 2; i <= hojaApps.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = hojaApps.getRow(i);
                if (row == null) continue;
                
                // Verificar que la fila pertenece al último grupo
                org.apache.poi.ss.usermodel.Cell cellId = row.getCell(colIdGrupo);
                if (cellId == null) continue;
                
                String idGrupoFila = "";
                if (cellId.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                    idGrupoFila = cellId.getStringCellValue().trim();
                }
                
                // Solo procesar aplicaciones del último grupo
                if (!idGrupoFila.equals(ultimoIdGrupo)) {
                    continue;
                }
                
                org.apache.poi.ss.usermodel.Cell cellApp = row.getCell(colApp);
                if (cellApp == null) continue;
                
                String nombre = "";
                if (cellApp.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                    nombre = cellApp.getStringCellValue().trim();
                } else if (cellApp.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                    nombre = String.valueOf((int)cellApp.getNumericCellValue());
                }
                
                if (nombre.isEmpty() || appsYaAgregadas.contains(nombre)) continue;
                
                // Evitar duplicados
                appsYaAgregadas.add(nombre);
                
                // Clasificar programas
                String nombreLower = nombre.toLowerCase();
                
                // PROGRAMAS BÁSICOS: Software de oficina/administrativo
                if (nombreLower.contains("office") || nombreLower.contains("365") ||
                    nombreLower.contains("word") || nombreLower.contains("excel") ||
                    nombreLower.contains("powerpoint") || nombreLower.contains("outlook") ||
                    nombreLower.contains("access") || nombreLower.contains("onenote") ||
                    nombreLower.contains("acrobat") || nombreLower.contains("adobe reader") ||
                    nombreLower.contains("pdf") || nombreLower.contains("foxit") ||
                    nombreLower.contains("chrome") || nombreLower.contains("firefox") ||
                    nombreLower.contains("edge") || nombreLower.contains("opera") ||
                    nombreLower.contains("teams") || nombreLower.contains("zoom") ||
                    nombreLower.contains("skype") || nombreLower.contains("slack") ||
                    nombreLower.contains("antivirus") || nombreLower.contains("defender") ||
                    nombreLower.contains("norton") || nombreLower.contains("mcafee") ||
                    nombreLower.contains("winrar") || nombreLower.contains("7-zip") ||
                    nombreLower.contains("winzip")) {
                    
                    if (programasBasicos.length() > 0) programasBasicos.append(", ");
                    programasBasicos.append(nombre);
                } 
                // OTROS PROGRAMAS: Software especializado (diseño, desarrollo, ingeniería)
                else if (nombreLower.contains("photoshop") || nombreLower.contains("illustrator") ||
                         nombreLower.contains("indesign") || nombreLower.contains("premiere") ||
                         nombreLower.contains("after effects") || nombreLower.contains("lightroom") ||
                         nombreLower.contains("autocad") || nombreLower.contains("revit") ||
                         nombreLower.contains("sketchup") || nombreLower.contains("3ds max") ||
                         nombreLower.contains("blender") || nombreLower.contains("maya") ||
                         nombreLower.contains("visual studio") || nombreLower.contains("eclipse") ||
                         nombreLower.contains("intellij") || nombreLower.contains("pycharm") ||
                         nombreLower.contains("android studio") || nombreLower.contains("xcode") ||
                         nombreLower.contains("sql server") || nombreLower.contains("mysql") ||
                         nombreLower.contains("oracle") || nombreLower.contains("postgres") ||
                         nombreLower.contains("mongodb") || nombreLower.contains("redis") ||
                         nombreLower.contains("docker") || nombreLower.contains("git") ||
                         nombreLower.contains("nodejs") || nombreLower.contains("python") ||
                         nombreLower.contains("matlab") || nombreLower.contains("spss") ||
                         nombreLower.contains("tableau") || nombreLower.contains("power bi") ||
                         nombreLower.contains("sap") || nombreLower.contains("salesforce") ||
                         nombreLower.contains("arcgis") || nombreLower.contains("qgis") ||
                         nombreLower.contains("corel") || nombreLower.contains("camtasia") ||
                         nombreLower.contains("vegas") || nombreLower.contains("davinci")) {
                    
                    // Software especializado
                    if (otrosProgramas.length() > 0) otrosProgramas.append(", ");
                    otrosProgramas.append(nombre);
                }
                // Ignorar: updates, runtimes, drivers, componentes del sistema
            }
            
            wb.close();
            
            // Limitar texto para no ser muy largo
            String basicos = programasBasicos.toString();
            String otros = otrosProgramas.toString();
            if (basicos.length() > 1000) basicos = basicos.substring(0, 1000) + "...";
            if (otros.length() > 1000) otros = otros.substring(0, 1000) + "...";
            
            final String basicosFinal = basicos;
            final String otrosFinal = otros;
            
            System.out.println("[ReporteFormulario] Programas básicos encontrados: " + (!basicos.isEmpty() ? basicos.substring(0, Math.min(100, basicos.length())) : "ninguno"));
            System.out.println("[ReporteFormulario] Otros programas encontrados: " + (!otros.isEmpty() ? otros.substring(0, Math.min(100, otros.length())) : "ninguno"));
            
            // Actualizar UI
            javafx.application.Platform.runLater(() -> {
                if (!basicosFinal.isEmpty() && txtProgramasBasicos.getText().isEmpty()) {
                    txtProgramasBasicos.setText(basicosFinal);
                }
                if (!otrosFinal.isEmpty() && txtOtrosProgramas.getText().isEmpty()) {
                    txtOtrosProgramas.setText(otrosFinal);
                }
            });
            
        } catch (Exception e) {
            AppLogger.getLogger(ReporteFormularioFX.class).error("[ReporteFormulario] Error al cargar programas desde Excel: " + e.getMessage(), e);
        }
    }
    
    private static void cargarDatosEnFormulario(InventarioFXBase.InfoPC info) {
        if (info == null) return;
        
        try {
            // Tipo de dispositivo
            if (info.deviceType != null && !info.deviceType.isEmpty()) {
                txtTipoDispositivo.setText(info.deviceType);
            }
            
            // Marca/Fabricante
            if (info.manufacturer != null && !info.manufacturer.isEmpty()) {
                txtMarca.setText(info.manufacturer);
            }
            
            // Modelo
            if (info.modeloEquipo != null && !info.modeloEquipo.isEmpty()) {
                txtModelo.setText(info.modeloEquipo);
            }
            
            // Procesador (extraer nombre corto pero con modelo)
            if (info.cpu != null && !info.cpu.isEmpty()) {
                String cpuCorto = info.cpu;
                // Quitar los paréntesis con info de núcleos y GHz al final
                int idx = cpuCorto.lastIndexOf("(");
                if (idx > 0) {
                    cpuCorto = cpuCorto.substring(0, idx).trim();
                }
                // Limpiar (R), (TM), etc.
                cpuCorto = cpuCorto.replaceAll("\\([^)]*\\)", "").replaceAll("\\s+", " ").trim();
                // Quitar @ y frecuencia si quedó
                int at = cpuCorto.indexOf("@");
                if (at > 0) {
                    cpuCorto = cpuCorto.substring(0, at).trim();
                }
                // Quitar "CPU" si aparece
                cpuCorto = cpuCorto.replaceAll("(?i)\\bCPU\\b", "").replaceAll("\\s+", " ").trim();
                txtProcesador.setText(cpuCorto);
            }
            
            // RAM (extraer total)
            if (info.ram != null && !info.ram.isEmpty()) {
                String ram = info.ram;
                int idxDe = ram.indexOf(" de ");
                if (idxDe > 0) {
                    String total = ram.substring(idxDe + 4).replace(" total", "").trim();
                    txtMemoriaRam.setText(total);
                } else {
                    txtMemoriaRam.setText(ram);
                }
            }
            
            // Disco duro
            if (info.discos != null && !info.discos.isEmpty()) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?\\s*(?:GB|TB|MB))");
                java.util.regex.Matcher m = p.matcher(info.discos);
                if (m.find()) {
                    txtDiscoDuroTxt.setText(m.group(1));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error cargando datos en formulario: " + e.getMessage());
        }
    }
    
    // ════════════════════════════════════════════════════════════════════════════
    // CARGAR DATOS DEL SISTEMA (LEGACY - ya no se usa)
    // ════════════════════════════════════════════════════════════════════════════
    
    private static void cargarDatosSistema() {
        try {
            InventarioFXBase.InfoPC info = InventarioFXBase.recopilarInfo();
            if (info != null) {
                // Tipo de dispositivo
                if (info.deviceType != null && !info.deviceType.isEmpty()) {
                    txtTipoDispositivo.setText(info.deviceType);
                }
                
                // Marca/Fabricante
                if (info.manufacturer != null && !info.manufacturer.isEmpty()) {
                    txtMarca.setText(info.manufacturer);
                }
                
                // Modelo
                if (info.modeloEquipo != null && !info.modeloEquipo.isEmpty()) {
                    txtModelo.setText(info.modeloEquipo);
                }
                
                // Procesador (extraer nombre corto pero con modelo)
                if (info.cpu != null && !info.cpu.isEmpty()) {
                    String cpuCorto = info.cpu;
                    // Quitar los paréntesis con info de núcleos y GHz al final
                    int idx = cpuCorto.lastIndexOf("(");
                    if (idx > 0) {
                        cpuCorto = cpuCorto.substring(0, idx).trim();
                    }
                    // Limpiar (R), (TM), etc.
                    cpuCorto = cpuCorto.replaceAll("\\([^)]*\\)", "").replaceAll("\\s+", " ").trim();
                    // Quitar @ y frecuencia si quedó
                    int at = cpuCorto.indexOf("@");
                    if (at > 0) {
                        cpuCorto = cpuCorto.substring(0, at).trim();
                    }
                    // Quitar "CPU" si aparece
                    cpuCorto = cpuCorto.replaceAll("(?i)\\bCPU\\b", "").replaceAll("\\s+", " ").trim();
                    txtProcesador.setText(cpuCorto);
                }
                
                // RAM (extraer total)
                if (info.ram != null && !info.ram.isEmpty()) {
                    // Formato: "X GB usada de Y GB total"
                    String ram = info.ram;
                    int idxDe = ram.indexOf(" de ");
                    if (idxDe > 0) {
                        String total = ram.substring(idxDe + 4).replace(" total", "").trim();
                        txtMemoriaRam.setText(total);
                    } else {
                        txtMemoriaRam.setText(ram);
                    }
                }
                
                // Disco duro
                if (info.discos != null && !info.discos.isEmpty()) {
                    // Extraer tamaño del primer disco
                    String discos = info.discos;
                    // Buscar patrón como "500 GB" o "1 TB"
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?\\s*(?:GB|TB|MB))");
                    java.util.regex.Matcher m = p.matcher(discos);
                    if (m.find()) {
                        txtDiscoDuroTxt.setText(m.group(1));
                    }
                }
                
                // Hostname/Nombre del equipo
                if (info.hostname != null && !info.hostname.isEmpty()) {
                    // Puedes usar esto para otro campo si es necesario
                }
                
                // Usuario del sistema
                if (info.userName != null && !info.userName.isEmpty()) {
                    // Puedes prellenar el campo de nombre si está vacío
                    // txtNombre.setText(info.userName);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar datos del sistema: " + e.getMessage());
            // No mostrar error al usuario, simplemente continuar sin datos automáticos
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // HEADER
    // ════════════════════════════════════════════════════════════════════════════
    
    private static HBox crearHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 0 0 1 0;"
        );

        // Icono de herramienta (mantenimiento)
        StackPane iconWrapper = new StackPane();
        iconWrapper.setPadding(new Insets(10));
        iconWrapper.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + "20;" +
            "-fx-background-radius: 12;"
        );
        iconWrapper.getChildren().add(IconosSVG.herramienta(COLOR_PRIMARY, 28));

        // Título
        VBox titleBox = new VBox(2);
        Label titulo = new Label("FORMATO DE REPORTE DE MANTENIMIENTO");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titulo.setTextFill(Color.web(COLOR_TEXT()));
        
        Label subtitulo = new Label("Complete los campos");
        subtitulo.setFont(Font.font("Segoe UI", 12));
        subtitulo.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        
        titleBox.getChildren().addAll(titulo, subtitulo);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Logo SELCOMP en SVG
        javafx.scene.image.ImageView logoSVG = SVGUtil.loadSVG("/icons/LogoSelcompSVG.svg", 160);
        if (logoSVG != null) {
            logoSVG.setPreserveRatio(true);
            logoSVG.setFitHeight(120);
            header.getChildren().addAll(iconWrapper, titleBox, spacer, logoSVG);
        } else {
            // Fallback si no se encuentra el SVG
            Label selcompLabel = new Label("SELCOMP");
            selcompLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            selcompLabel.setTextFill(Color.web(COLOR_PRIMARY));
            header.getChildren().addAll(iconWrapper, titleBox, spacer, selcompLabel);
        }
        return header;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // CONTENIDO DEL FORMULARIO
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearContenidoFormulario() {
        VBox contenido = new VBox(20);
        contenido.setPadding(new Insets(25, 30, 25, 30));
        contenido.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");

        javafx.scene.Node sec1 = crearSeccionTipoSolicitud();
        javafx.scene.Node sec2 = crearSeccionDatosUsuario();
        javafx.scene.Node sec3 = crearSeccionDescripcionHardware();
        javafx.scene.Node sec4 = crearSeccionPC();
        javafx.scene.Node sec5 = crearSeccionMonitor();
        javafx.scene.Node sec6 = crearSeccionTeclado();
        javafx.scene.Node sec7 = crearSeccionMouse();
        javafx.scene.Node sec8 = crearSeccionSoftware();
        javafx.scene.Node sec9 = crearSeccionProcedimiento();
        javafx.scene.Node sec10 = crearSeccionObservaciones();
        javafx.scene.Node sec11 = crearSeccionFirmas();

        contenido.getChildren().addAll(sec1, sec2, sec3, sec4, sec5, sec6, sec7, sec8, sec9, sec10, sec11);
        
        // Animación escalonada de secciones del formulario
        AnimacionesFX.entradaEscalonada(
            java.util.Arrays.asList(sec1, sec2, sec3, sec4, sec5, sec6, sec7, sec8, sec9, sec10, sec11), 
            350, 60, 20);

        return contenido;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SECCIÓN: TIPO DE SOLICITUD
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionTipoSolicitud() {
        VBox seccion = crearCardSeccionConIcono("TIPO DE SOLICITUD", IconosSVG.lista(COLOR_PRIMARY, 18));
        
        HBox contenido = new HBox(30);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(15, 20, 15, 20));

        // Tipo de solicitud
        VBox tipoBox = new VBox(6);
        Label lblTipo = crearLabelCampo("Tipo de Mantenimiento");
        cmbTipoSolicitud = crearComboBox("MANTENIMIENTO PREVENTIVO", "MANTENIMIENTO CORRECTIVO", "MANTENIMIENTO LÓGICO", "INSTALACIÓN", "DIAGNÓSTICO");
        cmbTipoSolicitud.setPrefWidth(280);
        tipoBox.getChildren().addAll(lblTipo, cmbTipoSolicitud);

        // Fecha
        VBox fechaBox = new VBox(6);
        Label lblFecha = crearLabelCampo("Fecha");
        HBox fechaFields = new HBox(10);
        fechaFields.setAlignment(Pos.CENTER_LEFT);
        
        txtDia = crearTextField("Día", 60);
        txtMes = crearTextField("Mes", 60);
        txtAnio = crearTextField("Año", 80);
        
        fechaFields.getChildren().addAll(
            crearCampoConLabel(txtDia, "Día"),
            crearCampoConLabel(txtMes, "Mes"),
            crearCampoConLabel(txtAnio, "Año")
        );
        fechaBox.getChildren().addAll(lblFecha, fechaFields);

        // Ticket
        VBox ticketBox = new VBox(6);
        Label lblTicket = crearLabelCampo("Ticket N°");
        txtTicket = crearTextField("Número", 120);
        ticketBox.getChildren().addAll(lblTicket, txtTicket);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        contenido.getChildren().addAll(tipoBox, fechaBox, spacer, ticketBox);
        seccion.getChildren().add(contenido);
        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SECCIÓN: DATOS DEL USUARIO
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionDatosUsuario() {
        VBox seccion = crearCardSeccionConIcono("DATOS DEL USUARIO", IconosSVG.usuario(COLOR_PRIMARY, 18));
        
        GridPane grid = crearGridPane();
        
        txtCiudad = crearTextField("Ciudad", 0);
        txtDireccion = crearTextField("Dirección", 0);
        txtNombre = crearTextField("Nombre completo", 0);
        txtCorreo = crearTextField("correo@ejemplo.com", 0);
        txtTecnico = crearTextField("Nombre del técnico", 0);
        txtSede = crearTextField("Sede u oficina", 0);

        // Fila 0
        Label lblCiudad = crearLabelCampo("Ciudad"); lblCiudad.setMinWidth(80);
        Label lblDireccion = crearLabelCampo("Dirección"); lblDireccion.setMinWidth(80);
        grid.add(lblCiudad, 0, 0);
        grid.add(txtCiudad, 1, 0);
        grid.add(lblDireccion, 2, 0);
        grid.add(txtDireccion, 3, 0);

        // Fila 1
        Label lblNombre = crearLabelCampo("Nombre"); lblNombre.setMinWidth(80);
        Label lblCorreo = crearLabelCampo("Correo"); lblCorreo.setMinWidth(80);
        grid.add(lblNombre, 0, 1);
        grid.add(txtNombre, 1, 1);
        grid.add(lblCorreo, 2, 1);
        grid.add(txtCorreo, 3, 1);

        // Fila 2
        Label lblTecnico = crearLabelCampo("Técnico"); lblTecnico.setMinWidth(80);
        Label lblSede = crearLabelCampo("Sede/Oficina"); lblSede.setMinWidth(80);
        grid.add(lblTecnico, 0, 2);
        grid.add(txtTecnico, 1, 2);
        grid.add(lblSede, 2, 2);
        grid.add(txtSede, 3, 2);

        GridPane.setHgrow(txtCiudad, Priority.ALWAYS);
        GridPane.setHgrow(txtDireccion, Priority.ALWAYS);
        GridPane.setHgrow(txtNombre, Priority.ALWAYS);
        GridPane.setHgrow(txtCorreo, Priority.ALWAYS);
        GridPane.setHgrow(txtTecnico, Priority.ALWAYS);
        GridPane.setHgrow(txtSede, Priority.ALWAYS);

        seccion.getChildren().add(grid);
        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SECCIÓN: DESCRIPCIÓN DEL HARDWARE
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionDescripcionHardware() {
        VBox seccion = crearCardSeccionConIcono("DESCRIPCIÓN DEL HARDWARE", IconosSVG.servidor(COLOR_PRIMARY, 18));
        
        GridPane grid = crearGridPane();
        grid.setHgap(15);
        
        txtTipoDispositivo = crearTextField("Tipo", 0);
        txtMarca = crearTextField("Marca", 0);
        txtModelo = crearTextField("Modelo", 0);
        txtSerial = crearTextField("Serial", 0);
        txtPlaca = crearTextField("Placa", 0);
        txtCondicionesHW = crearTextField("Condiciones", 0);

        // Fila 1
        grid.add(crearLabelCampo("Tipo Dispositivo"), 0, 0);
        grid.add(txtTipoDispositivo, 1, 0);
        grid.add(crearLabelCampo("Marca"), 2, 0);
        grid.add(txtMarca, 3, 0);
        grid.add(crearLabelCampo("Modelo"), 4, 0);
        grid.add(txtModelo, 5, 0);

        // Fila 2
        grid.add(crearLabelCampo("Serial"), 0, 1);
        grid.add(txtSerial, 1, 1);
        grid.add(crearLabelCampo("Placa"), 2, 1);
        grid.add(txtPlaca, 3, 1);
        grid.add(crearLabelCampo("Condiciones"), 4, 1);
        grid.add(txtCondicionesHW, 5, 1);

        for (int i = 0; i < 6; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(i % 2 == 0 ? Priority.NEVER : Priority.ALWAYS);
            if (i % 2 == 0) col.setMinWidth(100);
            grid.getColumnConstraints().add(col);
        }

        seccion.getChildren().add(grid);
        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SECCIÓN: PC
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionPC() {
        VBox seccion = crearCardSeccionConIcono("PC", IconosSVG.computadora(COLOR_PRIMARY, 18));
        
        VBox contenido = new VBox(15);
        contenido.setPadding(new Insets(15, 20, 15, 20));

        // Fila 1: Enciende, Disco Duro, CD/DVD, Botones, Condiciones
        HBox fila1 = new HBox(20);
        fila1.setAlignment(Pos.CENTER_LEFT);
        
        cmbPcEnciende = crearComboBoxSiNo();
        cmbDiscoDuro = crearComboBoxSiNo();
        cmbCdDvd = crearComboBoxSiNo();
        cmbBotonesPC = crearComboBoxSiNo();
        txtCondicionesPC = crearTextField("Condiciones físicas", 200);

        fila1.getChildren().addAll(
            crearCampoVertical("¿Enciende?", cmbPcEnciende),
            crearCampoVertical("Disco Duro", cmbDiscoDuro),
            crearCampoVertical("CD/DVD", cmbCdDvd),
            crearCampoVertical("Botones Completos", cmbBotonesPC),
            crearCampoVertical("Condiciones Físicas", txtCondicionesPC)
        );

        // Fila 2: Procesador, Memoria RAM, Disco Duro
        HBox fila2 = new HBox(20);
        fila2.setAlignment(Pos.CENTER_LEFT);
        
        txtProcesador = crearTextField("Intel Core i5...", 200);
        txtMemoriaRam = crearTextField("8 GB", 150);
        txtDiscoDuroTxt = crearTextField("500 GB SSD", 150);

        fila2.getChildren().addAll(
            crearCampoVertical("Procesador", txtProcesador),
            crearCampoVertical("Memoria RAM", txtMemoriaRam),
            crearCampoVertical("Disco Duro", txtDiscoDuroTxt)
        );

        contenido.getChildren().addAll(fila1, fila2);
        seccion.getChildren().add(contenido);
        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SECCIÓN: MONITOR
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionMonitor() {
        VBox seccion = crearCardSeccionConIcono("MONITOR", IconosSVG.monitor(COLOR_PRIMARY, 18));
        
        HBox contenido = new HBox(20);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(15, 20, 15, 20));

        cmbMonitorEnciende = crearComboBoxSiNo();
        cmbPantalla = crearComboBoxSiNo();
        cmbOnlyOne = crearComboBoxSiNo();
        cmbBotonesMonitor = crearComboBoxSiNo();
        txtCondicionesMonitor = crearTextField("Condiciones físicas", 200);

        contenido.getChildren().addAll(
            crearCampoVertical("¿Enciende?", cmbMonitorEnciende),
            crearCampoVertical("Pantalla", cmbPantalla),
            crearCampoVertical("Only One", cmbOnlyOne),
            crearCampoVertical("Botones Completos", cmbBotonesMonitor),
            crearCampoVertical("Condiciones Físicas", txtCondicionesMonitor)
        );

        seccion.getChildren().add(contenido);
        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SECCIÓN: TECLADO
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionTeclado() {
        VBox seccion = crearCardSeccionConIcono("TECLADO", IconosSVG.teclado(COLOR_PRIMARY, 18));
        
        HBox contenido = new HBox(20);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(15, 20, 15, 20));

        cmbTecladoEnciende = crearComboBoxSiNo();
        cmbTecladoFunciona = crearComboBoxSiNo();
        cmbBotonesTeclado = crearComboBoxSiNo();
        txtCondicionesTeclado = crearTextField("Condiciones físicas", 200);

        contenido.getChildren().addAll(
            crearCampoVertical("¿Enciende?", cmbTecladoEnciende),
            crearCampoVertical("¿Funciona Correctamente?", cmbTecladoFunciona),
            crearCampoVertical("Botones Completos", cmbBotonesTeclado),
            crearCampoVertical("Condiciones Físicas", txtCondicionesTeclado)
        );

        seccion.getChildren().add(contenido);
        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SECCIÓN: MOUSE
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionMouse() {
        VBox seccion = crearCardSeccion("MOUSE");
        
        HBox contenido = new HBox(20);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(15, 20, 15, 20));

        cmbMouseEnciende = crearComboBoxSiNo();
        cmbMouseFunciona = crearComboBoxSiNo();
        cmbBotonesMouse = crearComboBoxSiNo();
        txtCondicionesMouse = crearTextField("Condiciones físicas", 200);

        contenido.getChildren().addAll(
            crearCampoVertical("¿Enciende?", cmbMouseEnciende),
            crearCampoVertical("¿Funciona Correctamente?", cmbMouseFunciona),
            crearCampoVertical("Botones Completos", cmbBotonesMouse),
            crearCampoVertical("Condiciones Físicas", txtCondicionesMouse)
        );

        seccion.getChildren().add(contenido);
        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SECCIÓN: SOFTWARE
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionSoftware() {
        VBox seccion = crearCardSeccionConIcono("SOFTWARE", IconosSVG.paquete(COLOR_PRIMARY, 18));
        
        HBox contenido = new HBox(20);
        contenido.setPadding(new Insets(15, 20, 15, 20));

        VBox progBasicosBox = new VBox(6);
        progBasicosBox.setPrefWidth(400);
        Label lblBasicos = crearLabelCampo("Programas Básicos");
        txtProgramasBasicos = crearTextArea("Windows 10, Office 365, Antivirus...", 80);
        progBasicosBox.getChildren().addAll(lblBasicos, txtProgramasBasicos);
        HBox.setHgrow(progBasicosBox, Priority.ALWAYS);

        VBox otrosProgBox = new VBox(6);
        otrosProgBox.setPrefWidth(400);
        Label lblOtros = crearLabelCampo("Otros Programas");
        txtOtrosProgramas = crearTextArea("Software especializado...", 80);
        otrosProgBox.getChildren().addAll(lblOtros, txtOtrosProgramas);
        HBox.setHgrow(otrosProgBox, Priority.ALWAYS);

        contenido.getChildren().addAll(progBasicosBox, otrosProgBox);
        seccion.getChildren().add(contenido);
        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SECCIÓN: PROCEDIMIENTO REALIZADO
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionProcedimiento() {
        VBox seccion = crearCardSeccionConIcono("PROCEDIMIENTO REALIZADO", IconosSVG.documento(COLOR_PRIMARY, 18));
        
        VBox contenido = new VBox(6);
        contenido.setPadding(new Insets(15, 20, 15, 20));

        txtProcedimiento = crearTextArea("Describa detalladamente el procedimiento realizado...", 120);
        contenido.getChildren().add(txtProcedimiento);

        seccion.getChildren().add(contenido);
        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SECCIÓN: OBSERVACIONES
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionObservaciones() {
        VBox seccion = crearCardSeccionConIcono("OBSERVACIONES", IconosSVG.info(COLOR_PRIMARY, 18));
        
        VBox contenido = new VBox(6);
        contenido.setPadding(new Insets(15, 20, 15, 20));

        txtObservaciones = crearTextArea("Observaciones adicionales...", 100);
        contenido.getChildren().add(txtObservaciones);

        seccion.getChildren().add(contenido);
        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SECCIÓN: FIRMAS
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearSeccionFirmas() {
        VBox seccion = crearCardSeccionConIcono("FIRMAS DIGITALES", IconosSVG.editar(COLOR_PRIMARY, 18));
        
        HBox contenido = new HBox(40);
        contenido.setPadding(new Insets(15, 20, 20, 20));
        contenido.setAlignment(Pos.CENTER);

        // ═══ FIRMA TÉCNICO ═══
        VBox firmasTecnico = new VBox(12);
        firmasTecnico.setAlignment(Pos.CENTER);
        firmasTecnico.setPrefWidth(420);
        
        Label lblTecnicoTitle = new Label("TÉCNICO");
        lblTecnicoTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTecnicoTitle.setTextFill(Color.web(COLOR_PRIMARY));
        
        // Canvas de firma digital
        canvasFirmaTecnico = new FirmaDigitalCanvas();
        
        // Campos de texto
        txtFirmaTecnico = crearTextField("Nombre del técnico", 400);
        txtCedulaTecnico = crearTextField("Cédula del técnico", 400);
        
        HBox nombreTecBox = new HBox(10);
        nombreTecBox.setAlignment(Pos.CENTER_LEFT);
        Label lblNombreTec = crearLabelCampo("Nombre:");
        lblNombreTec.setMinWidth(60);
        nombreTecBox.getChildren().addAll(lblNombreTec, txtFirmaTecnico);
        
        HBox cedulaTecBox = new HBox(10);
        cedulaTecBox.setAlignment(Pos.CENTER_LEFT);
        Label lblCedulaTec = crearLabelCampo("Cédula:");
        lblCedulaTec.setMinWidth(60);
        cedulaTecBox.getChildren().addAll(lblCedulaTec, txtCedulaTecnico);
        
        firmasTecnico.getChildren().addAll(
            lblTecnicoTitle, 
            canvasFirmaTecnico, 
            nombreTecBox, 
            cedulaTecBox
        );

        // Separador vertical
        Region separador = new Region();
        separador.setPrefWidth(1);
        separador.setMinHeight(250);
        separador.setStyle("-fx-background-color: " + COLOR_BORDER() + ";");

        // ═══ FIRMA FUNCIONARIO ═══
        VBox firmasFuncionario = new VBox(12);
        firmasFuncionario.setAlignment(Pos.CENTER);
        firmasFuncionario.setPrefWidth(420);
        
        Label lblFuncTitle = new Label("FUNCIONARIO");
        lblFuncTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblFuncTitle.setTextFill(Color.web(COLOR_PRIMARY));
        
        // Canvas de firma digital
        canvasFirmaFuncionario = new FirmaDigitalCanvas();
        
        // Campos de texto
        txtFirmaFuncionario = crearTextField("Nombre del funcionario", 400);
        txtCedulaFuncionario = crearTextField("Cédula del funcionario", 400);
        
        HBox nombreFuncBox = new HBox(10);
        nombreFuncBox.setAlignment(Pos.CENTER_LEFT);
        Label lblNombreFunc = crearLabelCampo("Nombre:");
        lblNombreFunc.setMinWidth(60);
        nombreFuncBox.getChildren().addAll(lblNombreFunc, txtFirmaFuncionario);
        
        HBox cedulaFuncBox = new HBox(10);
        cedulaFuncBox.setAlignment(Pos.CENTER_LEFT);
        Label lblCedulaFunc = crearLabelCampo("Cédula:");
        lblCedulaFunc.setMinWidth(60);
        cedulaFuncBox.getChildren().addAll(lblCedulaFunc, txtCedulaFuncionario);
        
        firmasFuncionario.getChildren().addAll(
            lblFuncTitle, 
            canvasFirmaFuncionario, 
            nombreFuncBox, 
            cedulaFuncBox
        );

        contenido.getChildren().addAll(firmasTecnico, separador, firmasFuncionario);
        seccion.getChildren().add(contenido);
        return seccion;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SELECTOR DE FORMATO DE EXPORTACIÓN
    // ════════════════════════════════════════════════════════════════════════════
    
    private static ComboBox<String> cmbFormatoExportacion;
    
    // Opciones de formato de exportación
    private static final String FORMATO_EXCEL = "Excel (.xlsx)";
    private static final String FORMATO_PDF = "PDF (.pdf)";
    private static final String FORMATO_AMBOS = "Excel + PDF";
    
    // ════════════════════════════════════════════════════════════════════════════
    // FOOTER CON BOTONES
    // ════════════════════════════════════════════════════════════════════════════
    
    private static HBox crearFooter(Consumer<Boolean> callback) {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15, 30, 20, 30));
        footer.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 1 0 0 0;"
        );

        // Botón Ver Borradores (izquierda)
        Button btnBorradores = crearBotonBorradores();

        // Selector de formato de exportación
        Label lblFormato = new Label("Formato:");
        lblFormato.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        lblFormato.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        
        cmbFormatoExportacion = new ComboBox<>();
        cmbFormatoExportacion.getItems().addAll(FORMATO_EXCEL, FORMATO_PDF, FORMATO_AMBOS);
        cmbFormatoExportacion.setValue(FORMATO_EXCEL);
        cmbFormatoExportacion.setPrefWidth(140);
        cmbFormatoExportacion.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;"
        );
        
        HBox formatoBox = new HBox(8);
        formatoBox.setAlignment(Pos.CENTER_LEFT);
        formatoBox.getChildren().addAll(lblFormato, cmbFormatoExportacion);
        
        // Espaciador para empujar los botones a la derecha
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnLimpiar = crearBotonSecundario("Limpiar");
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        Button btnCancelar = crearBotonSecundario("Cancelar");
        btnCancelar.setOnAction(e -> {
            formStage.close();
            if (callback != null) callback.accept(false);
        });

        Button btnGuardar = crearBotonPrimario("Guardar Reporte");
        btnGuardar.setOnAction(e -> {
            if (validarFormulario()) {
                guardarReporte(callback);
            }
        });

        footer.getChildren().addAll(btnBorradores, formatoBox, spacer, btnLimpiar, btnCancelar, btnGuardar);
        return footer;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // COMPONENTES REUTILIZABLES
    // ════════════════════════════════════════════════════════════════════════════
    
    private static VBox crearCardSeccion(String titulo) {
        return crearCardSeccionConIcono(titulo, null);
    }
    
    private static VBox crearCardSeccionConIcono(String titulo, javafx.scene.Node icono) {
        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 8;"
        );
        
        // Efecto de sombra sutil
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        card.setEffect(shadow);

        // Header de la sección
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-background-radius: 8 8 0 0;"
        );

        // Agregar icono si existe
        if (icono != null) {
            header.getChildren().add(icono);
        }

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblTitulo.setTextFill(Color.web(COLOR_PRIMARY));
        
        header.getChildren().add(lblTitulo);
        card.getChildren().add(header);

        return card;
    }

    private static TextField crearTextField(String placeholder, double width) {
        TextField field = new TextField();
        field.setPromptText(placeholder);
        if (width > 0) field.setPrefWidth(width);
        field.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-text-fill: " + COLOR_TEXT() + ";" +
            "-fx-prompt-text-fill: " + COLOR_TEXT_MUTED() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 8 12;"
        );
        
        // Efecto hover/focus
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            // No sobreescribir si tiene aviso activo
            if ("aviso".equals(field.getUserData())) return;
            if (newVal) {
                field.setStyle(
                    "-fx-background-color: " + COLOR_SURFACE() + ";" +
                    "-fx-text-fill: " + COLOR_TEXT() + ";" +
                    "-fx-prompt-text-fill: " + COLOR_TEXT_MUTED() + ";" +
                    "-fx-border-color: " + COLOR_PRIMARY + ";" +
                    "-fx-border-radius: 4;" +
                    "-fx-background-radius: 4;" +
                    "-fx-padding: 8 12;"
                );
            } else {
                field.setStyle(
                    "-fx-background-color: " + COLOR_SURFACE() + ";" +
                    "-fx-text-fill: " + COLOR_TEXT() + ";" +
                    "-fx-prompt-text-fill: " + COLOR_TEXT_MUTED() + ";" +
                    "-fx-border-color: " + COLOR_BORDER() + ";" +
                    "-fx-border-radius: 4;" +
                    "-fx-background-radius: 4;" +
                    "-fx-padding: 8 12;"
                );
            }
        });
        
        return field;
    }

    private static TextArea crearTextArea(String placeholder, double height) {
        TextArea area = new TextArea();
        area.setPromptText(placeholder);
        area.setPrefHeight(height);
        area.setWrapText(true);
        area.setStyle(
            "-fx-control-inner-background: " + COLOR_SURFACE() + ";" +
            "-fx-text-fill: " + COLOR_TEXT() + ";" +
            "-fx-prompt-text-fill: " + COLOR_TEXT_MUTED() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;"
        );
        return area;
    }

    private static ComboBox<String> crearComboBox(String... opciones) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(opciones);
        combo.setValue(opciones[0]);
        combo.setEditable(true); // Permitir edición
        combo.setDisable(false); // Asegurar que esté habilitado
        
        String colorTexto = COLOR_TEXT();
        combo.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;"
        );
        
        // Aplicar color al texto interno del ComboBox
        combo.setButtonCell(new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + colorTexto + "; -fx-font-weight: bold; -fx-font-size: 13px;");
                }
            }
        });
        
        return combo;
    }

    private static ComboBox<String> crearComboBoxSiNo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("SI", "NO", "N/A");
        combo.setValue("N/A");
        combo.setPrefWidth(100);
        
        // Colores para el borde y texto según selección
        String colorVerde = TemaManager.COLOR_SUCCESS;
        String colorRojo = TemaManager.COLOR_DANGER;
        String colorGris = TemaManager.getTextMuted();
        
        actualizarEstiloComboSiNo(combo, colorGris);
        
        // Listener para cambiar color según selección
        combo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.equals("SI")) {
                    actualizarEstiloComboSiNo(combo, colorVerde);
                } else if (newVal.equals("NO")) {
                    actualizarEstiloComboSiNo(combo, colorRojo);
                } else {
                    actualizarEstiloComboSiNo(combo, colorGris);
                }
            }
        });
        
        return combo;
    }
    
    private static void actualizarEstiloComboSiNo(ComboBox<String> combo, String color) {
        combo.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: " + color + ";"
        );
        // Aplicar color al texto interno del ComboBox
        combo.setButtonCell(new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(Color.web(color));
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                }
            }
        });
    }

    private static Label crearLabelCampo(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        label.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        return label;
    }

    private static VBox crearCampoVertical(String label, Control campo) {
        VBox box = new VBox(6);
        Label lbl = crearLabelCampo(label);
        box.getChildren().addAll(lbl, campo);
        return box;
    }

    private static VBox crearCampoConLabel(TextField field, String label) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", 9));
        lbl.setTextFill(Color.web(COLOR_TEXT_MUTED()));
        box.getChildren().addAll(field, lbl);
        return box;
    }

    private static GridPane crearGridPane() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 20, 15, 20));
        return grid;
    }

    private static void agregarFilaGrid(GridPane grid, int row, String lbl1, Control ctrl1, String lbl2, Control ctrl2) {
        Label label1 = crearLabelCampo(lbl1);
        label1.setMinWidth(80);
        Label label2 = crearLabelCampo(lbl2);
        label2.setMinWidth(80);
        
        grid.add(label1, 0, row);
        grid.add(ctrl1, 1, row);
        grid.add(label2, 2, row);
        grid.add(ctrl2, 3, row);
        
        GridPane.setHgrow(ctrl1, Priority.ALWAYS);
        GridPane.setHgrow(ctrl2, Priority.ALWAYS);
    }

    private static Button crearBotonBorradores() {
        Button btn = new Button("Borradores");
        btn.setGraphic(IconosSVG.documento(COLOR_INFO, 16));
        btn.setGraphicTextGap(6);
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        btn.setPadding(new Insets(8, 16, 8, 14));
        
        String baseStyle = 
            "-fx-background-color: " + COLOR_INFO + "12;" +
            "-fx-text-fill: " + COLOR_INFO + ";" +
            "-fx-border-color: " + COLOR_INFO + "40;" +
            "-fx-border-radius: 6; -fx-background-radius: 6;" +
            "-fx-cursor: hand;";
        btn.setStyle(baseStyle);
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + COLOR_INFO + "25;" +
                "-fx-text-fill: " + COLOR_INFO + ";" +
                "-fx-border-color: " + COLOR_INFO + ";" +
                "-fx-border-radius: 6; -fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.05, 120);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
            AnimacionesFX.hoverOut(btn, 120);
        });
        
        btn.setOnAction(e -> mostrarDialogoBorradores());
        
        // Badge de cantidad
        java.util.List<BorradorInfo> lista = listarBorradores();
        if (!lista.isEmpty()) {
            Label badge = new Label(String.valueOf(lista.size()));
            badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
            badge.setStyle(
                "-fx-background-color: " + COLOR_INFO + ";" +
                "-fx-text-fill: white;" +
                "-fx-padding: 1 5;" +
                "-fx-background-radius: 10;" +
                "-fx-min-width: 16; -fx-min-height: 16;" +
                "-fx-alignment: center;"
            );
            
            StackPane btnWithBadge = new StackPane();
            btnWithBadge.getChildren().addAll(btn, badge);
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            StackPane.setMargin(badge, new Insets(-4, -4, 0, 0));
            
            // En lugar de devolver el StackPane, ponemos el badge en el graphic
            HBox graphic = new HBox(4);
            graphic.setAlignment(Pos.CENTER);
            graphic.getChildren().addAll(IconosSVG.documento(COLOR_INFO, 16), badge);
            btn.setGraphic(graphic);
        }
        
        return btn;
    }

    private static Button crearBotonPrimario(String texto) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setPadding(new Insets(10, 25, 10, 25));
        btn.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: derive(" + COLOR_PRIMARY + ", 20%);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.05, 120);
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 120);
        });
        
        return btn;
    }

    private static Button crearBotonPrimarioConIcono(String texto, javafx.scene.Node icono) {
        Button btn = new Button(texto);
        btn.setGraphic(icono);
        btn.setGraphicTextGap(8);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setPadding(new Insets(10, 25, 10, 25));
        btn.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: derive(" + COLOR_PRIMARY + ", 20%);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.05, 120);
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 120);
        });
        
        return btn;
    }

    private static Button crearBotonSecundario(String texto) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        btn.setPadding(new Insets(10, 20, 10, 20));
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + COLOR_TEXT_SECONDARY() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + COLOR_SURFACE() + ";" +
                "-fx-text-fill: " + COLOR_TEXT() + ";" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(btn, 1.05, 120);
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + COLOR_TEXT_SECONDARY() + ";" +
                "-fx-border-color: " + COLOR_BORDER() + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverOut(btn, 120);
        });
        
        return btn;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // LÓGICA DEL FORMULARIO
    // ════════════════════════════════════════════════════════════════════════════
    
    private static boolean validarFormulario() {
        // Limpiar errores previos
        limpiarTodosLosErrores();
        
        java.util.List<String> camposVacios = new java.util.ArrayList<>();
        java.util.List<javafx.scene.control.Control> controles = new java.util.ArrayList<>();
        
        // === DATOS DEL USUARIO ===
        if (txtCiudad != null && txtCiudad.getText().trim().isEmpty()) { marcarCampoAviso(txtCiudad); controles.add(txtCiudad); camposVacios.add("Ciudad"); }
        if (txtDireccion != null && txtDireccion.getText().trim().isEmpty()) { marcarCampoAviso(txtDireccion); controles.add(txtDireccion); camposVacios.add("Direcci\u00f3n"); }
        if (txtNombre != null && txtNombre.getText().trim().isEmpty()) { marcarCampoAviso(txtNombre); controles.add(txtNombre); camposVacios.add("Nombre"); }
        if (txtCorreo != null && txtCorreo.getText().trim().isEmpty()) { marcarCampoAviso(txtCorreo); controles.add(txtCorreo); camposVacios.add("Correo"); }
        if (txtTecnico != null && txtTecnico.getText().trim().isEmpty()) { marcarCampoAviso(txtTecnico); controles.add(txtTecnico); camposVacios.add("T\u00e9cnico"); }
        if (txtSede != null && txtSede.getText().trim().isEmpty()) { marcarCampoAviso(txtSede); controles.add(txtSede); camposVacios.add("Sede/Oficina"); }
        
        // === TICKET ===
        if (txtTicket != null && txtTicket.getText().trim().isEmpty()) { marcarCampoAviso(txtTicket); controles.add(txtTicket); camposVacios.add("Ticket N\u00b0"); }
        
        // === HARDWARE ===
        if (txtTipoDispositivo != null && txtTipoDispositivo.getText().trim().isEmpty()) { marcarCampoAviso(txtTipoDispositivo); controles.add(txtTipoDispositivo); camposVacios.add("Tipo Dispositivo"); }
        if (txtMarca != null && txtMarca.getText().trim().isEmpty()) { marcarCampoAviso(txtMarca); controles.add(txtMarca); camposVacios.add("Marca"); }
        if (txtModelo != null && txtModelo.getText().trim().isEmpty()) { marcarCampoAviso(txtModelo); controles.add(txtModelo); camposVacios.add("Modelo"); }
        if (txtSerial != null && txtSerial.getText().trim().isEmpty()) { marcarCampoAviso(txtSerial); controles.add(txtSerial); camposVacios.add("Serial"); }
        if (txtPlaca != null && txtPlaca.getText().trim().isEmpty()) { marcarCampoAviso(txtPlaca); controles.add(txtPlaca); camposVacios.add("Placa"); }
        if (txtCondicionesHW != null && txtCondicionesHW.getText().trim().isEmpty()) { marcarCampoAviso(txtCondicionesHW); controles.add(txtCondicionesHW); camposVacios.add("Condiciones HW"); }
        
        // === PC ===
        if (txtProcesador != null && txtProcesador.getText().trim().isEmpty()) { marcarCampoAviso(txtProcesador); controles.add(txtProcesador); camposVacios.add("Procesador"); }
        if (txtMemoriaRam != null && txtMemoriaRam.getText().trim().isEmpty()) { marcarCampoAviso(txtMemoriaRam); controles.add(txtMemoriaRam); camposVacios.add("Memoria RAM"); }
        if (txtDiscoDuroTxt != null && txtDiscoDuroTxt.getText().trim().isEmpty()) { marcarCampoAviso(txtDiscoDuroTxt); controles.add(txtDiscoDuroTxt); camposVacios.add("Disco Duro"); }
        if (txtCondicionesPC != null && txtCondicionesPC.getText().trim().isEmpty()) { marcarCampoAviso(txtCondicionesPC); controles.add(txtCondicionesPC); camposVacios.add("Condiciones PC"); }
        
        // === MONITOR ===
        if (txtCondicionesMonitor != null && txtCondicionesMonitor.getText().trim().isEmpty()) { marcarCampoAviso(txtCondicionesMonitor); controles.add(txtCondicionesMonitor); camposVacios.add("Condiciones Monitor"); }
        
        // === TECLADO ===
        if (txtCondicionesTeclado != null && txtCondicionesTeclado.getText().trim().isEmpty()) { marcarCampoAviso(txtCondicionesTeclado); controles.add(txtCondicionesTeclado); camposVacios.add("Condiciones Teclado"); }
        
        // === MOUSE ===
        if (txtCondicionesMouse != null && txtCondicionesMouse.getText().trim().isEmpty()) { marcarCampoAviso(txtCondicionesMouse); controles.add(txtCondicionesMouse); camposVacios.add("Condiciones Mouse"); }
        
        // === SOFTWARE ===
        if (txtProgramasBasicos != null && txtProgramasBasicos.getText().trim().isEmpty()) { marcarCampoAviso(txtProgramasBasicos); controles.add(txtProgramasBasicos); camposVacios.add("Programas B\u00e1sicos"); }
        if (txtOtrosProgramas != null && txtOtrosProgramas.getText().trim().isEmpty()) { marcarCampoAviso(txtOtrosProgramas); controles.add(txtOtrosProgramas); camposVacios.add("Otros Programas"); }
        
        // === PROCEDIMIENTO Y OBSERVACIONES ===
        if (txtProcedimiento != null && txtProcedimiento.getText().trim().isEmpty()) { marcarCampoAviso(txtProcedimiento); controles.add(txtProcedimiento); camposVacios.add("Procedimiento"); }
        if (txtObservaciones != null && txtObservaciones.getText().trim().isEmpty()) { marcarCampoAviso(txtObservaciones); controles.add(txtObservaciones); camposVacios.add("Observaciones"); }
        
        // === FIRMAS ===
        if (txtFirmaTecnico != null && txtFirmaTecnico.getText().trim().isEmpty()) { marcarCampoAviso(txtFirmaTecnico); controles.add(txtFirmaTecnico); camposVacios.add("Nombre T\u00e9cnico (firma)"); }
        if (txtCedulaTecnico != null && txtCedulaTecnico.getText().trim().isEmpty()) { marcarCampoAviso(txtCedulaTecnico); controles.add(txtCedulaTecnico); camposVacios.add("C\u00e9dula T\u00e9cnico"); }
        if (txtFirmaFuncionario != null && txtFirmaFuncionario.getText().trim().isEmpty()) { marcarCampoAviso(txtFirmaFuncionario); controles.add(txtFirmaFuncionario); camposVacios.add("Nombre Funcionario (firma)"); }
        if (txtCedulaFuncionario != null && txtCedulaFuncionario.getText().trim().isEmpty()) { marcarCampoAviso(txtCedulaFuncionario); controles.add(txtCedulaFuncionario); camposVacios.add("C\u00e9dula Funcionario"); }
        
        if (!camposVacios.isEmpty()) {
            // Mostrar aviso con opci\u00f3n de continuar
            String lista = String.join(", ", camposVacios);
            Alert aviso = new Alert(Alert.AlertType.CONFIRMATION);
            aviso.initOwner(formStage);
            aviso.setTitle("Campos vac\u00edos");
            aviso.setHeaderText(camposVacios.size() + " campo(s) sin llenar:");
            aviso.setContentText(lista + "\n\n\u00bfDesea guardar de todas formas?");
            
            ButtonType btnSi = new ButtonType("S\u00ed, guardar");
            ButtonType btnNo = new ButtonType("No, completar", ButtonBar.ButtonData.CANCEL_CLOSE);
            aviso.getButtonTypes().setAll(btnSi, btnNo);
            
            java.util.Optional<ButtonType> resultado = aviso.showAndWait();
            if (!resultado.isPresent() || resultado.get() == btnNo) {
                // Scroll al primer campo vac\u00edo despu\u00e9s de cerrar el di\u00e1logo
                javafx.scene.control.Control primerCampo = controles.get(0);
                scrollAlCampo(primerCampo);
                return false;
            }
            return true; // Si, guardar
        }
        return true;
    }

    /**
     * Hace scroll al campo indicado dentro del formulario.
     */
    private static void scrollAlCampo(javafx.scene.control.Control campo) {
        if (scrollPaneFormulario == null || scrollPaneFormulario.getContent() == null) return;
        
        // Esperar a que el layout se actualice antes de calcular posiciones
        javafx.application.Platform.runLater(() -> {
            javafx.application.Platform.runLater(() -> {
                try {
                    double contentHeight = scrollPaneFormulario.getContent().getBoundsInLocal().getHeight();
                    double viewportHeight = scrollPaneFormulario.getViewportBounds().getHeight();
                    
                    if (contentHeight > viewportHeight) {
                        // Obtener posición Y del campo relativa al contenido
                        javafx.geometry.Bounds campoEnScene = campo.localToScene(campo.getBoundsInLocal());
                        javafx.geometry.Bounds contenidoEnScene = scrollPaneFormulario.getContent().localToScene(
                            scrollPaneFormulario.getContent().getBoundsInLocal());
                        
                        double campoY = campoEnScene.getMinY() - contenidoEnScene.getMinY();
                        // Centrar el campo en el viewport
                        double targetVvalue = (campoY - viewportHeight / 3.0) / (contentHeight - viewportHeight);
                        targetVvalue = Math.max(0, Math.min(1, targetVvalue));
                        scrollPaneFormulario.setVvalue(targetVvalue);
                    }
                    
                    campo.requestFocus();
                } catch (Exception e) {
                    campo.requestFocus();
                }
            });
        });
    }

    /**
     * Marca un campo con borde #EB0045 para avisar que est\u00e1 vac\u00edo.
     */
    private static void marcarCampoAviso(javafx.scene.control.Control campo) {
        String avisoBorder = TemaManager.COLOR_PRIMARY;
        String avisoBg = TemaManager.COLOR_PRIMARY + "10";
        
        // Marcar con bandera para que el focus listener no sobreescriba
        campo.setUserData("aviso");
        
        if (campo instanceof TextField) {
            campo.setStyle(
                "-fx-background-color: " + avisoBg + ";" +
                "-fx-text-fill: " + COLOR_TEXT() + ";" +
                "-fx-prompt-text-fill: " + COLOR_TEXT_MUTED() + ";" +
                "-fx-border-color: " + avisoBorder + ";" +
                "-fx-border-radius: 4;" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 8 12;" +
                "-fx-border-width: 1.5;"
            );
        } else if (campo instanceof TextArea) {
            campo.setStyle(
                "-fx-control-inner-background: " + avisoBg + ";" +
                "-fx-text-fill: " + COLOR_TEXT() + ";" +
                "-fx-border-color: " + avisoBorder + ";" +
                "-fx-border-radius: 4;" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 8 12;" +
                "-fx-border-width: 1.5;"
            );
        }
        
        // Limpiar aviso cuando el usuario escriba
        if (campo instanceof TextField tf) {
            tf.textProperty().addListener((obs, oldV, newV) -> {
                if (newV != null && !newV.trim().isEmpty()) {
                    tf.setUserData(null);
                    restaurarEstiloCampo(tf);
                }
            });
        } else if (campo instanceof TextArea ta) {
            ta.textProperty().addListener((obs, oldV, newV) -> {
                if (newV != null && !newV.trim().isEmpty()) {
                    ta.setUserData(null);
                    restaurarEstiloCampoArea(ta);
                }
            });
        }
    }
    
    // marcarCampoError ya no se usa, se reemplaz\u00f3 por marcarCampoAviso
    
    /**
     * Restaura el estilo normal de un TextField.
     */
    private static void restaurarEstiloCampo(TextField field) {
        field.setUserData(null);
        field.setStyle(
            "-fx-background-color: " + COLOR_SURFACE() + ";" +
            "-fx-text-fill: " + COLOR_TEXT() + ";" +
            "-fx-prompt-text-fill: " + COLOR_TEXT_MUTED() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 8 12;"
        );
    }
    
    /**
     * Restaura el estilo normal de un TextArea.
     */
    private static void restaurarEstiloCampoArea(TextArea area) {
        area.setUserData(null);
        area.setStyle(
            "-fx-control-inner-background: " + COLOR_SURFACE() + ";" +
            "-fx-text-fill: " + COLOR_TEXT() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 8 12;"
        );
    }
    
    /**
     * Limpia el estilo de error de todos los campos del formulario.
     */
    private static void limpiarTodosLosErrores() {
        TextField[] camposTxt = {
            txtNombre, txtTecnico, txtCiudad, txtDireccion, txtSede, txtCorreo,
            txtTicket,
            txtTipoDispositivo, txtMarca, txtModelo, txtSerial, txtPlaca, txtCondicionesHW,
            txtProcesador, txtMemoriaRam, txtDiscoDuroTxt, txtCondicionesPC,
            txtCondicionesMonitor, txtCondicionesTeclado, txtCondicionesMouse,
            txtFirmaTecnico, txtCedulaTecnico, txtFirmaFuncionario, txtCedulaFuncionario
        };
        for (TextField f : camposTxt) {
            if (f != null) restaurarEstiloCampo(f);
        }
        TextArea[] camposArea = {txtProgramasBasicos, txtOtrosProgramas, txtProcedimiento, txtObservaciones};
        for (TextArea a : camposArea) {
            if (a != null) restaurarEstiloCampoArea(a);
        }
    }

    private static void mostrarError(String mensaje) {
        DialogosFX.mostrarAlerta(formStage, "Validación", mensaje, Alert.AlertType.WARNING);
    }

    private static void limpiarFormulario() {
        // Tipo solicitud
        cmbTipoSolicitud.setValue("MANTENIMIENTO PREVENTIVO");
        LocalDate hoy = LocalDate.now();
        txtDia.setText(String.valueOf(hoy.getDayOfMonth()));
        txtMes.setText(String.valueOf(hoy.getMonthValue()));
        txtAnio.setText(String.valueOf(hoy.getYear()));
        txtTicket.clear();

        // Datos usuario
        txtCiudad.clear(); txtDireccion.clear(); txtNombre.clear();
        txtCorreo.clear(); txtTecnico.clear(); txtSede.clear();

        // Hardware
        txtTipoDispositivo.clear(); txtMarca.clear(); txtModelo.clear();
        txtSerial.clear(); txtPlaca.clear(); txtCondicionesHW.clear();

        // PC - Valores limpios (N/A por defecto)
        cmbPcEnciende.setValue("N/A"); cmbDiscoDuro.setValue("N/A");
        cmbCdDvd.setValue("N/A"); cmbBotonesPC.setValue("N/A");
        txtCondicionesPC.clear(); txtProcesador.clear();
        txtMemoriaRam.clear(); txtDiscoDuroTxt.clear();

        // Monitor
        cmbMonitorEnciende.setValue("N/A"); cmbPantalla.setValue("N/A");
        cmbOnlyOne.setValue("N/A"); cmbBotonesMonitor.setValue("N/A");
        txtCondicionesMonitor.clear();

        // Teclado
        cmbTecladoEnciende.setValue("N/A"); cmbTecladoFunciona.setValue("N/A");
        cmbBotonesTeclado.setValue("N/A"); txtCondicionesTeclado.clear();

        // Software
        txtProgramasBasicos.clear(); txtOtrosProgramas.clear();

        // Procedimiento y Observaciones
        txtProcedimiento.clear(); txtObservaciones.clear();

        // Firmas
        txtFirmaTecnico.clear(); txtCedulaTecnico.clear();
        txtFirmaFuncionario.clear(); txtCedulaFuncionario.clear();
        
        // Limpiar canvas de firmas digitales
        if (canvasFirmaTecnico != null) {
            canvasFirmaTecnico.limpiar();
        }
        if (canvasFirmaFuncionario != null) {
            canvasFirmaFuncionario.limpiar();
        }
        
        // No eliminamos borradores al limpiar - el usuario los gestiona desde el panel de borradores
    }

    private static void guardarReporte(Consumer<Boolean> callback) {
        String formatoSeleccionado = cmbFormatoExportacion.getValue();
        boolean generarExcel = formatoSeleccionado.equals(FORMATO_EXCEL) || formatoSeleccionado.equals(FORMATO_AMBOS);
        boolean generarPdf = formatoSeleccionado.equals(FORMATO_PDF) || formatoSeleccionado.equals(FORMATO_AMBOS);
        
        String nombreBase = "Reporte_Mantenimiento_" + txtNombre.getText().replaceAll("\\s+", "_");
        
        // Guardar automáticamente en carpeta del proyecto
        String proyectoNombre = System.getProperty("reporte.proyectoNombre", "");
        java.nio.file.Path dirProyecto = inventario.fx.config.PortablePaths.getExportReportesDir(proyectoNombre);
        
        // Determinar extensión principal
        String extPrincipal = formatoSeleccionado.equals(FORMATO_PDF) ? ".pdf" : ".xlsx";
        File archivo = new File(dirProyecto.toFile(), nombreBase + extPrincipal);
        
        // Si ya existe, agregar sufijo numérico
        int contador = 1;
        while (archivo.exists()) {
            archivo = new File(dirProyecto.toFile(), nombreBase + "_" + contador + extPrincipal);
            contador++;
        }
        
        try {
            guardarDatosEnPropiedades();
            
            String rutaExcel;
            String rutaPdf = null;
            
            if (formatoSeleccionado.equals(FORMATO_PDF)) {
                // Si solo quiere PDF, generamos Excel temporal y luego lo convertimos
                File tempExcel = File.createTempFile("temp_reporte_", ".xlsx");
                tempExcel.deleteOnExit();
                rutaExcel = tempExcel.getAbsolutePath();
                rutaPdf = archivo.getAbsolutePath();
            } else {
                rutaExcel = archivo.getAbsolutePath();
                if (generarPdf) {
                    // Si también quiere PDF, generar en la misma ubicación
                    rutaPdf = rutaExcel.replace(".xlsx", ".pdf");
                }
            }
            
            // Generar el Excel (siempre se genera primero como base)
            System.setProperty("reporte.rutaDestino", rutaExcel);
            GeneratedTemplate.main(new String[]{});
            
            // Si se pidió PDF, convertir el Excel a PDF
            if (generarPdf && rutaPdf != null) {
                try {
                    System.out.println("Generando PDF desde Excel: " + rutaExcel);
                    ExcelToPdfConverter.convertirExcelAPdf(rutaExcel, rutaPdf);
                    System.out.println("✓ PDF generado: " + rutaPdf);
                } catch (Exception pdfEx) {
                    AppLogger.getLogger(ReporteFormularioFX.class).error("Error al generar PDF: " + pdfEx.getMessage(), pdfEx);
                    // Mostrar advertencia pero no fallar si el Excel se generó correctamente
                    if (generarExcel) {
                        DialogosFX.mostrarAlerta(
                            formStage,
                            "Advertencia",
                            "El Excel se guardó correctamente, pero hubo un error al generar el PDF:\n" + pdfEx.getMessage(),
                            Alert.AlertType.WARNING
                        );
                    } else {
                        throw pdfEx; // Si solo quería PDF, propagar el error
                    }
                }
            }
            
            // Construir mensaje de éxito
            StringBuilder detallesExito = new StringBuilder();
            if (generarExcel && !formatoSeleccionado.equals(FORMATO_PDF)) {
                detallesExito.append("• Excel: ").append(archivo.getName()).append("\n");
            }
            if (generarPdf && rutaPdf != null) {
                File pdfFile = new File(rutaPdf);
                if (pdfFile.exists()) {
                    detallesExito.append("• PDF: ").append(pdfFile.getName()).append("\n");
                }
            }
            detallesExito.append("\nUbicación: ").append(dirProyecto.getFileName());
            
            DialogosFX.mostrarExito(
                formStage,
                "Reporte guardado",
                "El reporte se ha generado exitosamente.",
                detallesExito.toString()
            );
            
            formStage.close();
            eliminarBorrador();
            if (callback != null) callback.accept(true);
        } catch (Exception ex) {
            AppLogger.getLogger(ReporteFormularioFX.class).error("Error: " + ex.getMessage(), ex);
            DialogosFX.mostrarAlerta(
                formStage,
                "Error",
                "Error al guardar el reporte:\n" + ex.getMessage(),
                Alert.AlertType.ERROR
            );
        }
    }

    private static void guardarDatosEnPropiedades() {
        // Tipo solicitud
        System.setProperty("reporte.tipoSolicitud", cmbTipoSolicitud.getValue());
        System.setProperty("reporte.dia", txtDia.getText());
        System.setProperty("reporte.mes", txtMes.getText());
        System.setProperty("reporte.anio", txtAnio.getText());
        System.setProperty("reporte.ticket", txtTicket.getText());

        // Datos usuario
        System.setProperty("reporte.ciudad", txtCiudad.getText());
        System.setProperty("reporte.direccion", txtDireccion.getText());
        System.setProperty("reporte.nombre", txtNombre.getText());
        System.setProperty("reporte.correo", txtCorreo.getText());
        System.setProperty("reporte.tecnico", txtTecnico.getText());
        System.setProperty("reporte.sede", txtSede.getText());

        // Hardware
        System.setProperty("reporte.tipoDispositivo", txtTipoDispositivo.getText());
        System.setProperty("reporte.marca", txtMarca.getText());
        System.setProperty("reporte.modelo", txtModelo.getText());
        System.setProperty("reporte.serial", txtSerial.getText());
        System.setProperty("reporte.placa", txtPlaca.getText());
        System.setProperty("reporte.condicionesHW", txtCondicionesHW.getText());

        // PC
        System.setProperty("reporte.pcEnciende", cmbPcEnciende.getValue());
        System.setProperty("reporte.discoDuro", cmbDiscoDuro.getValue());
        System.setProperty("reporte.cdDvd", cmbCdDvd.getValue());
        System.setProperty("reporte.botonesPC", cmbBotonesPC.getValue());
        System.setProperty("reporte.condicionesPC", txtCondicionesPC.getText());
        System.setProperty("reporte.procesador", txtProcesador.getText());
        System.setProperty("reporte.memoriaRAM", txtMemoriaRam.getText());
        System.setProperty("reporte.discoDuroCapacidad", txtDiscoDuroTxt.getText());

        // Monitor
        System.setProperty("reporte.monitorEnciende", cmbMonitorEnciende.getValue());
        System.setProperty("reporte.pantalla", cmbPantalla.getValue());
        System.setProperty("reporte.onlyOne", cmbOnlyOne.getValue());
        System.setProperty("reporte.botonesMonitor", cmbBotonesMonitor.getValue());
        System.setProperty("reporte.condicionesMonitor", txtCondicionesMonitor.getText());

        // Teclado
        System.setProperty("reporte.tecladoEnciende", cmbTecladoEnciende.getValue());
        System.setProperty("reporte.tecladoFunciona", cmbTecladoFunciona.getValue());
        System.setProperty("reporte.botonesTeclado", cmbBotonesTeclado.getValue());
        System.setProperty("reporte.condicionesTeclado", txtCondicionesTeclado.getText());

        // Mouse
        System.setProperty("reporte.mouseEnciende", cmbMouseEnciende.getValue());
        System.setProperty("reporte.mouseFunciona", cmbMouseFunciona.getValue());
        System.setProperty("reporte.botonesMouse", cmbBotonesMouse.getValue());
        System.setProperty("reporte.condicionesMouse", txtCondicionesMouse.getText());

        // Software
        System.setProperty("reporte.programasBasicos", txtProgramasBasicos.getText());
        System.setProperty("reporte.otrosProgramas", txtOtrosProgramas.getText());

        // Procedimiento y Observaciones
        System.setProperty("reporte.procedimiento", txtProcedimiento.getText());
        System.setProperty("reporte.observaciones", txtObservaciones.getText());

        // Firmas
        System.setProperty("reporte.firmaTecnico", txtFirmaTecnico.getText());
        System.setProperty("reporte.cedulaTecnico", txtCedulaTecnico.getText());
        System.setProperty("reporte.firmaFuncionario", txtFirmaFuncionario.getText());
        System.setProperty("reporte.cedulaFuncionario", txtCedulaFuncionario.getText());
        
        // IMPORTANTE: Guardar las rutas de las imágenes de firmas
        if (canvasFirmaTecnico != null && canvasFirmaTecnico.tieneFirma()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String rutaFirmaTecnico = canvasFirmaTecnico.guardarFirma("firma_tecnico_" + timestamp);
            if (rutaFirmaTecnico != null && !rutaFirmaTecnico.isEmpty()) {
                System.setProperty("reporte.firmaTecnicoImagenPath", rutaFirmaTecnico);
                System.out.println("[guardarDatosEnPropiedades] Firma técnico guardada: " + rutaFirmaTecnico);
            }
        } else {
            System.setProperty("reporte.firmaTecnicoImagenPath", "");
        }
        
        if (canvasFirmaFuncionario != null && canvasFirmaFuncionario.tieneFirma()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String rutaFirmaFuncionario = canvasFirmaFuncionario.guardarFirma("firma_funcionario_" + timestamp);
            if (rutaFirmaFuncionario != null && !rutaFirmaFuncionario.isEmpty()) {
                System.setProperty("reporte.firmaFuncionarioImagenPath", rutaFirmaFuncionario);
                System.out.println("[guardarDatosEnPropiedades] Firma funcionario guardada: " + rutaFirmaFuncionario);
            }
        } else {
            System.setProperty("reporte.firmaFuncionarioImagenPath", "");
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SISTEMA DE BORRADORES v2.0 — SECCIÓN DEDICADA + MULTI-BORRADOR
    // ════════════════════════════════════════════════════════════════════════════

    private static final String BORRADOR_DIR = inventario.fx.config.PortablePaths.getBorradoresDir().toString();

    /**
     * Representación de un borrador guardado con metadatos.
     */
    private static class BorradorInfo {
        String archivo;
        String nombre;
        String tecnico;
        String ticket;
        String tipo;
        java.time.LocalDateTime fecha;
        int camposLlenos;
        
        String getResumen() {
            StringBuilder sb = new StringBuilder();
            if (ticket != null && !ticket.isBlank()) sb.append("Ticket: ").append(ticket);
            if (nombre != null && !nombre.isBlank()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(nombre);
            }
            if (tecnico != null && !tecnico.isBlank()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append("Téc: ").append(tecnico);
            }
            if (sb.length() == 0) sb.append("Borrador sin datos clave");
            return sb.toString();
        }
        
        String getFechaFormateada() {
            if (fecha == null) return "Fecha desconocida";
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return fecha.format(fmt);
        }
    }

    /**
     * Lista todos los borradores guardados, ordenados por fecha (más reciente primero).
     */
    private static java.util.List<BorradorInfo> listarBorradores() {
        java.util.List<BorradorInfo> lista = new java.util.ArrayList<>();
        try {
            java.nio.file.Path dir = java.nio.file.Paths.get(BORRADOR_DIR);
            if (!java.nio.file.Files.exists(dir)) return lista;
            
            try (java.util.stream.Stream<java.nio.file.Path> archivos = java.nio.file.Files.list(dir)) {
                archivos.filter(p -> p.toString().endsWith(".properties"))
                    .forEach(p -> {
                        try {
                            BorradorInfo info = cargarInfoBorrador(p);
                            if (info != null) lista.add(info);
                        } catch (Exception ignored) {}
                    });
            }
            
            // Ordenar por fecha descendente
            lista.sort((a, b) -> {
                if (a.fecha == null && b.fecha == null) return 0;
                if (a.fecha == null) return 1;
                if (b.fecha == null) return -1;
                return b.fecha.compareTo(a.fecha);
            });
        } catch (Exception e) {
            System.err.println("[Borradores] Error listando: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Carga la información de un borrador sin restaurarlo.
     */
    private static BorradorInfo cargarInfoBorrador(java.nio.file.Path archivo) {
        try {
            java.util.Properties props = new java.util.Properties();
            try (java.io.InputStream is = java.nio.file.Files.newInputStream(archivo)) {
                props.load(is);
            }
            if (props.isEmpty()) return null;
            
            BorradorInfo info = new BorradorInfo();
            info.archivo = archivo.getFileName().toString();
            info.nombre = props.getProperty("nombre", "");
            info.tecnico = props.getProperty("tecnico", "");
            info.ticket = props.getProperty("ticket", "");
            info.tipo = props.getProperty("tipoSolicitud", "");
            
            // Parsear fecha del nombre del archivo o del comentario
            String fechaStr = props.getProperty("_fechaGuardado", "");
            if (!fechaStr.isBlank()) {
                try {
                    info.fecha = java.time.LocalDateTime.parse(fechaStr);
                } catch (Exception ignored) {
                    info.fecha = java.time.LocalDateTime.now();
                }
            } else {
                // Extraer del timestamp del nombre
                try {
                    java.nio.file.attribute.BasicFileAttributes attrs = 
                        java.nio.file.Files.readAttributes(archivo, java.nio.file.attribute.BasicFileAttributes.class);
                    info.fecha = java.time.LocalDateTime.ofInstant(
                        attrs.lastModifiedTime().toInstant(), java.time.ZoneId.systemDefault());
                } catch (Exception ignored) {
                    info.fecha = java.time.LocalDateTime.now();
                }
            }
            
            // Contar campos llenos
            int count = 0;
            for (String key : props.stringPropertyNames()) {
                if (!key.startsWith("_") && !props.getProperty(key, "").isBlank()) count++;
            }
            info.camposLlenos = count;
            
            return info;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Guarda un borrador silenciosamente al cerrar formulario (sin diálogo).
     */
    private static void guardarBorradorAlCerrar() {
        // No guardar borrador si el formulario se cerró porque se guardó exitosamente
        if (guardadoExitoso) {
            System.out.println("[Borradores] Omitido: guardado exitoso, no se crea borrador");
            return;
        }
        try {
            DatosReporte datos = recopilarDatosFormulario();
            if (datos == null || estaVacio(datos)) return;
            guardarBorradorInterno(datos, false);
            System.out.println("[Borradores] Guardado automáticamente al cerrar");
        } catch (Exception e) {
            System.err.println("[Borradores] Error al guardar al cerrar: " + e.getMessage());
        }
    }

    /**
     * Lógica interna de guardado de borrador.
     */
    private static void guardarBorradorInterno(DatosReporte datos, boolean mostrarFeedback) {
        try {
            java.nio.file.Path dir = java.nio.file.Paths.get(BORRADOR_DIR);
            java.nio.file.Files.createDirectories(dir);
            
            // Serializar a properties
            java.util.Properties props = new java.util.Properties();
            props.setProperty("_fechaGuardado", java.time.LocalDateTime.now().toString());
            
            if (datos.tipoSolicitud != null) props.setProperty("tipoSolicitud", datos.tipoSolicitud);
            if (datos.dia != null) props.setProperty("dia", datos.dia);
            if (datos.mes != null) props.setProperty("mes", datos.mes);
            if (datos.anio != null) props.setProperty("anio", datos.anio);
            if (datos.ciudad != null) props.setProperty("ciudad", datos.ciudad);
            if (datos.direccion != null) props.setProperty("direccion", datos.direccion);
            if (datos.nombre != null) props.setProperty("nombre", datos.nombre);
            if (datos.correo != null) props.setProperty("correo", datos.correo);
            if (datos.tecnico != null) props.setProperty("tecnico", datos.tecnico);
            if (datos.sede != null) props.setProperty("sede", datos.sede);
            if (datos.ticket != null) props.setProperty("ticket", datos.ticket);
            if (datos.tipoDispositivo != null) props.setProperty("tipoDispositivo", datos.tipoDispositivo);
            if (datos.marca != null) props.setProperty("marca", datos.marca);
            if (datos.modelo != null) props.setProperty("modelo", datos.modelo);
            if (datos.serial != null) props.setProperty("serial", datos.serial);
            if (datos.placa != null) props.setProperty("placa", datos.placa);
            if (datos.condiciones != null) props.setProperty("condiciones", datos.condiciones);
            if (datos.procesador != null) props.setProperty("procesador", datos.procesador);
            if (datos.memoriaRAM != null) props.setProperty("memoriaRAM", datos.memoriaRAM);
            if (datos.discoDuroCapacidad != null) props.setProperty("discoDuroCapacidad", datos.discoDuroCapacidad);
            if (datos.condicionesPC != null) props.setProperty("condicionesPC", datos.condicionesPC);
            if (datos.condicionesMonitor != null) props.setProperty("condicionesMonitor", datos.condicionesMonitor);
            if (datos.condicionesTeclado != null) props.setProperty("condicionesTeclado", datos.condicionesTeclado);
            if (datos.condicionesMouse != null) props.setProperty("condicionesMouse", datos.condicionesMouse);
            if (datos.programasBasicos != null) props.setProperty("programasBasicos", datos.programasBasicos);
            if (datos.otrosProgramas != null) props.setProperty("otrosProgramas", datos.otrosProgramas);
            if (datos.trabajoRealizado != null) props.setProperty("trabajoRealizado", datos.trabajoRealizado);
            if (datos.observaciones != null) props.setProperty("observaciones", datos.observaciones);
            if (datos.firmaTecnico != null) props.setProperty("firmaTecnico", datos.firmaTecnico);
            if (datos.cedulaTecnico != null) props.setProperty("cedulaTecnico", datos.cedulaTecnico);
            if (datos.firmaFuncionario != null) props.setProperty("firmaFuncionario", datos.firmaFuncionario);
            if (datos.cedulaFuncionario != null) props.setProperty("cedulaFuncionario", datos.cedulaFuncionario);
            
            // Campos ComboBox Sí/No — PC
            if (datos.pcEnciende != null) props.setProperty("pcEnciende", datos.pcEnciende);
            if (datos.discoDuro != null) props.setProperty("discoDuro", datos.discoDuro);
            if (datos.cddvd != null) props.setProperty("cddvd", datos.cddvd);
            if (datos.botonesPC != null) props.setProperty("botonesPC", datos.botonesPC);
            
            // Campos ComboBox Sí/No — Monitor
            if (datos.monitorEnciende != null) props.setProperty("monitorEnciende", datos.monitorEnciende);
            if (datos.pantalla != null) props.setProperty("pantalla", datos.pantalla);
            if (datos.onlyOne != null) props.setProperty("onlyOne", datos.onlyOne);
            if (datos.botonesMonitor != null) props.setProperty("botonesMonitor", datos.botonesMonitor);
            
            // Campos ComboBox Sí/No — Teclado
            if (datos.tecladoEnciende != null) props.setProperty("tecladoEnciende", datos.tecladoEnciende);
            if (datos.tecladoFunciona != null) props.setProperty("tecladoFunciona", datos.tecladoFunciona);
            if (datos.botonesTeclado != null) props.setProperty("botonesTeclado", datos.botonesTeclado);
            
            // Campos ComboBox Sí/No — Mouse
            if (datos.mouseEnciende != null) props.setProperty("mouseEnciende", datos.mouseEnciende);
            if (datos.mouseFunciona != null) props.setProperty("mouseFunciona", datos.mouseFunciona);
            if (datos.botonesMouse != null) props.setProperty("botonesMouse", datos.botonesMouse);
            
            // Firmas digitales y proyecto
            if (datos.firmaTecnicoImagenPath != null && !datos.firmaTecnicoImagenPath.isEmpty())
                props.setProperty("firmaTecnicoImagenPath", datos.firmaTecnicoImagenPath);
            if (datos.firmaFuncionarioImagenPath != null && !datos.firmaFuncionarioImagenPath.isEmpty())
                props.setProperty("firmaFuncionarioImagenPath", datos.firmaFuncionarioImagenPath);
            if (datos.proyectoImagenPath != null && !datos.proyectoImagenPath.isEmpty())
                props.setProperty("proyectoImagenPath", datos.proyectoImagenPath);
            if (datos.proyectoNombre != null && !datos.proyectoNombre.isEmpty())
                props.setProperty("proyectoNombre", datos.proyectoNombre);
            
            // Nombre único con timestamp
            String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nombreArchivo = "borrador_" + timestamp + ".properties";
            
            java.nio.file.Path archivo = dir.resolve(nombreArchivo);
            try (java.io.OutputStream os = java.nio.file.Files.newOutputStream(archivo)) {
                props.store(os, "Borrador SELCOMP - " + java.time.LocalDateTime.now());
            }
            
            System.out.println("[Borradores] Guardado: " + nombreArchivo);
            
            if (mostrarFeedback) {
                mostrarToast("Borrador guardado correctamente", COLOR_SUCCESS);
            }
            
            // Limpiar borradores antiguos (mantener máximo 20)
            limpiarBorradoresAntiguos(20);
            
        } catch (Exception e) {
            System.err.println("[Borradores] Error guardando: " + e.getMessage());
            if (mostrarFeedback) {
                mostrarToast("Error al guardar borrador", TemaManager.COLOR_DANGER);
            }
        }
    }

    /**
     * Mantiene solo los N borradores más recientes.
     */
    private static void limpiarBorradoresAntiguos(int maxBorradores) {
        try {
            java.util.List<BorradorInfo> lista = listarBorradores();
            if (lista.size() > maxBorradores) {
                for (int i = maxBorradores; i < lista.size(); i++) {
                    java.nio.file.Path archivo = java.nio.file.Paths.get(BORRADOR_DIR, lista.get(i).archivo);
                    java.nio.file.Files.deleteIfExists(archivo);
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Restaura un borrador específico en los campos del formulario.
     */
    private static boolean restaurarBorradorDesdeArchivo(String nombreArchivo) {
        try {
            java.nio.file.Path archivo = java.nio.file.Paths.get(BORRADOR_DIR, nombreArchivo);
            if (!java.nio.file.Files.exists(archivo)) return false;
            
            java.util.Properties props = new java.util.Properties();
            try (java.io.InputStream is = java.nio.file.Files.newInputStream(archivo)) {
                props.load(is);
            }
            
            if (props.isEmpty()) return false;
            
            // Cargar datos en los campos (con null-safety)
            if (cmbTipoSolicitud != null && props.containsKey("tipoSolicitud")) {
                String val = props.getProperty("tipoSolicitud");
                if (cmbTipoSolicitud.getItems().contains(val)) cmbTipoSolicitud.setValue(val);
            }
            if (txtDia != null && props.containsKey("dia")) txtDia.setText(props.getProperty("dia"));
            if (txtMes != null && props.containsKey("mes")) txtMes.setText(props.getProperty("mes"));
            if (txtAnio != null && props.containsKey("anio")) txtAnio.setText(props.getProperty("anio"));
            if (txtCiudad != null && props.containsKey("ciudad")) txtCiudad.setText(props.getProperty("ciudad"));
            if (txtDireccion != null && props.containsKey("direccion")) txtDireccion.setText(props.getProperty("direccion"));
            if (txtNombre != null && props.containsKey("nombre")) txtNombre.setText(props.getProperty("nombre"));
            if (txtCorreo != null && props.containsKey("correo")) txtCorreo.setText(props.getProperty("correo"));
            if (txtTecnico != null && props.containsKey("tecnico")) txtTecnico.setText(props.getProperty("tecnico"));
            if (txtSede != null && props.containsKey("sede")) txtSede.setText(props.getProperty("sede"));
            if (txtTicket != null && props.containsKey("ticket")) txtTicket.setText(props.getProperty("ticket"));
            if (txtTipoDispositivo != null && props.containsKey("tipoDispositivo")) txtTipoDispositivo.setText(props.getProperty("tipoDispositivo"));
            if (txtMarca != null && props.containsKey("marca")) txtMarca.setText(props.getProperty("marca"));
            if (txtModelo != null && props.containsKey("modelo")) txtModelo.setText(props.getProperty("modelo"));
            if (txtSerial != null && props.containsKey("serial")) txtSerial.setText(props.getProperty("serial"));
            if (txtPlaca != null && props.containsKey("placa")) txtPlaca.setText(props.getProperty("placa"));
            if (txtCondicionesHW != null && props.containsKey("condiciones")) txtCondicionesHW.setText(props.getProperty("condiciones"));
            if (txtProcesador != null && props.containsKey("procesador")) txtProcesador.setText(props.getProperty("procesador"));
            if (txtMemoriaRam != null && props.containsKey("memoriaRAM")) txtMemoriaRam.setText(props.getProperty("memoriaRAM"));
            if (txtDiscoDuroTxt != null && props.containsKey("discoDuroCapacidad")) txtDiscoDuroTxt.setText(props.getProperty("discoDuroCapacidad"));
            if (txtCondicionesPC != null && props.containsKey("condicionesPC")) txtCondicionesPC.setText(props.getProperty("condicionesPC"));
            if (txtCondicionesMonitor != null && props.containsKey("condicionesMonitor")) txtCondicionesMonitor.setText(props.getProperty("condicionesMonitor"));
            if (txtCondicionesTeclado != null && props.containsKey("condicionesTeclado")) txtCondicionesTeclado.setText(props.getProperty("condicionesTeclado"));
            if (txtCondicionesMouse != null && props.containsKey("condicionesMouse")) txtCondicionesMouse.setText(props.getProperty("condicionesMouse"));
            if (txtProgramasBasicos != null && props.containsKey("programasBasicos")) txtProgramasBasicos.setText(props.getProperty("programasBasicos"));
            if (txtOtrosProgramas != null && props.containsKey("otrosProgramas")) txtOtrosProgramas.setText(props.getProperty("otrosProgramas"));
            if (txtProcedimiento != null && props.containsKey("trabajoRealizado")) txtProcedimiento.setText(props.getProperty("trabajoRealizado"));
            if (txtObservaciones != null && props.containsKey("observaciones")) txtObservaciones.setText(props.getProperty("observaciones"));
            if (txtFirmaTecnico != null && props.containsKey("firmaTecnico")) txtFirmaTecnico.setText(props.getProperty("firmaTecnico"));
            if (txtCedulaTecnico != null && props.containsKey("cedulaTecnico")) txtCedulaTecnico.setText(props.getProperty("cedulaTecnico"));
            if (txtFirmaFuncionario != null && props.containsKey("firmaFuncionario")) txtFirmaFuncionario.setText(props.getProperty("firmaFuncionario"));
            if (txtCedulaFuncionario != null && props.containsKey("cedulaFuncionario")) txtCedulaFuncionario.setText(props.getProperty("cedulaFuncionario"));
            
            // Restaurar ComboBox Sí/No — PC
            if (cmbPcEnciende != null && props.containsKey("pcEnciende")) cmbPcEnciende.setValue(convertirValorSiNo(props.getProperty("pcEnciende")));
            if (cmbDiscoDuro != null && props.containsKey("discoDuro")) cmbDiscoDuro.setValue(convertirValorSiNo(props.getProperty("discoDuro")));
            if (cmbCdDvd != null && props.containsKey("cddvd")) cmbCdDvd.setValue(convertirValorSiNo(props.getProperty("cddvd")));
            if (cmbBotonesPC != null && props.containsKey("botonesPC")) cmbBotonesPC.setValue(convertirValorSiNo(props.getProperty("botonesPC")));
            
            // Restaurar ComboBox Sí/No — Monitor
            if (cmbMonitorEnciende != null && props.containsKey("monitorEnciende")) cmbMonitorEnciende.setValue(convertirValorSiNo(props.getProperty("monitorEnciende")));
            if (cmbPantalla != null && props.containsKey("pantalla")) cmbPantalla.setValue(convertirValorSiNo(props.getProperty("pantalla")));
            if (cmbOnlyOne != null && props.containsKey("onlyOne")) cmbOnlyOne.setValue(convertirValorSiNo(props.getProperty("onlyOne")));
            if (cmbBotonesMonitor != null && props.containsKey("botonesMonitor")) cmbBotonesMonitor.setValue(convertirValorSiNo(props.getProperty("botonesMonitor")));
            
            // Restaurar ComboBox Sí/No — Teclado
            if (cmbTecladoEnciende != null && props.containsKey("tecladoEnciende")) cmbTecladoEnciende.setValue(convertirValorSiNo(props.getProperty("tecladoEnciende")));
            if (cmbTecladoFunciona != null && props.containsKey("tecladoFunciona")) cmbTecladoFunciona.setValue(convertirValorSiNo(props.getProperty("tecladoFunciona")));
            if (cmbBotonesTeclado != null && props.containsKey("botonesTeclado")) cmbBotonesTeclado.setValue(convertirValorSiNo(props.getProperty("botonesTeclado")));
            
            return true;
        } catch (Exception e) {
            System.err.println("[Borradores] Error restaurando: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un borrador específico.
     */
    private static void eliminarBorradorArchivo(String nombreArchivo) {
        try {
            java.nio.file.Path archivo = java.nio.file.Paths.get(BORRADOR_DIR, nombreArchivo);
            java.nio.file.Files.deleteIfExists(archivo);
        } catch (Exception ignored) {}
    }

    /** Nombre del archivo de borrador restaurado (para eliminar solo ese al guardar). */
    private static String borradorRestaurado = null;
    
    /**
     * Elimina únicamente el borrador que fue restaurado en esta sesión.
     * Ya no borra TODOS los borradores al guardar un reporte.
     */
    private static void eliminarBorrador() {
        if (borradorRestaurado != null && !borradorRestaurado.isEmpty()) {
            try {
                java.nio.file.Path archivo = java.nio.file.Paths.get(BORRADOR_DIR, borradorRestaurado);
                java.nio.file.Files.deleteIfExists(archivo);
                System.out.println("[Borradores] Borrador restaurado eliminado: " + borradorRestaurado);
            } catch (Exception e) {
                System.err.println("[Borradores] Error eliminando borrador restaurado: " + e.getMessage());
            }
            borradorRestaurado = null;
        }
        // Los demás borradores se conservan — el usuario los gestiona desde el panel de borradores
    }

    /**
     * Verifica si un DatosReporte está completamente vacío.
     */
    private static boolean estaVacio(DatosReporte datos) {
        return (datos.nombre == null || datos.nombre.isBlank()) &&
               (datos.correo == null || datos.correo.isBlank()) &&
               (datos.tecnico == null || datos.tecnico.isBlank()) &&
               (datos.ciudad == null || datos.ciudad.isBlank()) &&
               (datos.marca == null || datos.marca.isBlank()) &&
               (datos.serial == null || datos.serial.isBlank()) &&
               (datos.ticket == null || datos.ticket.isBlank());
    }

    // ════════════════════════════════════════════════════════════════════════════
    // DIÁLOGO DE BORRADORES — SECCIÓN DEDICADA
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Muestra un toast de notificación en el formulario.
     */
    private static void mostrarToast(String mensaje, String color) {
        if (formStage == null || formStage.getScene() == null) return;
        try {
            BorderPane rootPane = (BorderPane) formStage.getScene().getRoot();
            
            Label notif = new Label(mensaje);
            notif.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-padding: 10 20;" +
                "-fx-background-radius: 24;" +
                "-fx-font-size: 13;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
            );
            
            StackPane overlay = new StackPane(notif);
            overlay.setAlignment(Pos.TOP_CENTER);
            overlay.setPadding(new Insets(12, 0, 0, 0));
            overlay.setPickOnBounds(false);
            
            // Insertar como overlay sobre el center
            javafx.scene.Node center = rootPane.getCenter();
            StackPane wrapper = new StackPane(center, overlay);
            rootPane.setCenter(wrapper);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), notif);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);
            fadeIn.play();
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), notif);
            fadeOut.setFromValue(1); fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.millis(2500));
            fadeOut.setOnFinished(ev -> {
                rootPane.setCenter(center);
            });
            fadeOut.play();
        } catch (Exception ignored) {}
    }

    /**
     * Muestra el diálogo/panel de gestión de borradores.
     */
    private static void mostrarDialogoBorradores() {
        java.util.List<BorradorInfo> borradores = listarBorradores();
        
        // Dialog personalizado
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(formStage);
        dialog.setTitle("Borradores Guardados");
        dialog.setWidth(580);
        dialog.setHeight(520);
        dialog.setResizable(false);
        
        try {
            InputStream iconStream = ReporteFormularioFX.class.getResourceAsStream("/images/Selcomp_logito.png");
            if (iconStream != null) dialog.getIcons().add(new Image(iconStream));
        } catch (Exception ignored) {}
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + COLOR_BG_DARK() + ";");
        
        // ── Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        StackPane iconBg = new StackPane();
        iconBg.setPadding(new Insets(8));
        iconBg.setStyle(
            "-fx-background-color: " + COLOR_INFO + "18;" +
            "-fx-background-radius: 10;"
        );
        iconBg.getChildren().add(IconosSVG.documento(COLOR_INFO, 22));
        
        VBox headerTexto = new VBox(2);
        Label tituloDialog = new Label("Borradores Guardados");
        tituloDialog.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        tituloDialog.setTextFill(Color.web(COLOR_TEXT()));
        
        Label subtDialog = new Label(borradores.isEmpty() ? "No hay borradores" : borradores.size() + " borrador(es) disponible(s)");
        subtDialog.setFont(Font.font("Segoe UI", 12));
        subtDialog.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
        
        headerTexto.getChildren().addAll(tituloDialog, subtDialog);
        
        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);
        
        // Botón eliminar todos
        Button btnEliminarTodos = new Button("Eliminar Todos");
        btnEliminarTodos.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        btnEliminarTodos.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TemaManager.COLOR_DANGER + ";" +
            "-fx-border-color: " + TemaManager.COLOR_DANGER + ";" +
            "-fx-border-radius: 6; -fx-background-radius: 6;" +
            "-fx-padding: 6 12; -fx-cursor: hand;"
        );
        btnEliminarTodos.setVisible(!borradores.isEmpty());
        
        header.getChildren().addAll(iconBg, headerTexto, spacerHeader, btnEliminarTodos);
        
        // ── Lista de borradores
        VBox listaBorradores = new VBox(8);
        listaBorradores.setPadding(new Insets(16, 24, 16, 24));
        
        if (borradores.isEmpty()) {
            // Estado vacío
            VBox emptyState = new VBox(12);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(40, 20, 40, 20));
            
            StackPane emptyIcon = new StackPane();
            emptyIcon.setPadding(new Insets(16));
            emptyIcon.setStyle(
                "-fx-background-color: " + COLOR_SURFACE() + ";" +
                "-fx-background-radius: 50;"
            );
            emptyIcon.getChildren().add(IconosSVG.documento(COLOR_TEXT_MUTED(), 36));
            
            Label emptyTitulo = new Label("Sin borradores");
            emptyTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            emptyTitulo.setTextFill(Color.web(COLOR_TEXT_SECONDARY()));
            
            Label emptyDesc = new Label("Guarda un borrador con el botón del formulario\npara continuar después.");
            emptyDesc.setFont(Font.font("Segoe UI", 12));
            emptyDesc.setTextFill(Color.web(COLOR_TEXT_MUTED()));
            emptyDesc.setWrapText(true);
            emptyDesc.setAlignment(Pos.CENTER);
            emptyDesc.setStyle("-fx-text-alignment: center;");
            
            emptyState.getChildren().addAll(emptyIcon, emptyTitulo, emptyDesc);
            listaBorradores.getChildren().add(emptyState);
        } else {
            for (BorradorInfo borrador : borradores) {
                HBox card = crearCardBorrador(borrador, dialog);
                listaBorradores.getChildren().add(card);
            }
        }
        
        ScrollPane scroll = new ScrollPane(listaBorradores);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background: " + COLOR_BG_DARK() + ";" +
            "-fx-background-color: " + COLOR_BG_DARK() + ";"
        );
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        
        // ── Footer
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 24, 16, 24));
        footer.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-width: 1 0 0 0;"
        );
        
        Button btnCerrar = crearBotonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> dialog.close());
        footer.getChildren().add(btnCerrar);
        
        root.getChildren().addAll(header, scroll, footer);
        
        // Eliminar todos
        btnEliminarTodos.setOnAction(e -> {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar");
            confirmacion.setHeaderText("¿Eliminar todos los borradores?");
            confirmacion.setContentText("Esta acción no se puede deshacer.");
            java.util.Optional<ButtonType> result = confirmacion.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                eliminarBorrador();
                dialog.close();
                mostrarToast("Todos los borradores eliminados", COLOR_INFO);
            }
        });
        
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.setOnShown(e -> inventario.fx.util.AnimacionesFX.animarEntradaDialogo(root));
        dialog.showAndWait();
    }

    /**
     * Crea una card de borrador para el diálogo de borradores.
     */
    private static HBox crearCardBorrador(BorradorInfo borrador, Stage dialog) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle(
            "-fx-background-color: " + COLOR_BG() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + COLOR_BORDER() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        // Efecto sombra sutil
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.08));
        shadow.setRadius(4);
        shadow.setOffsetY(1);
        card.setEffect(shadow);
        
        // Icono de documento
        StackPane iconWrapper = new StackPane();
        iconWrapper.setPadding(new Insets(8));
        iconWrapper.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + "15;" +
            "-fx-background-radius: 8;"
        );
        iconWrapper.getChildren().add(IconosSVG.documento(COLOR_PRIMARY, 18));
        
        // Info del borrador
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Label lblResumen = new Label(borrador.getResumen());
        lblResumen.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        lblResumen.setTextFill(Color.web(COLOR_TEXT()));
        lblResumen.setMaxWidth(320);
        lblResumen.setEllipsisString("...");
        
        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        
        Label lblFecha = new Label(borrador.getFechaFormateada());
        lblFecha.setFont(Font.font("Segoe UI", 11));
        lblFecha.setTextFill(Color.web(COLOR_TEXT_MUTED()));
        
        Label lblCampos = new Label(borrador.camposLlenos + " campos");
        lblCampos.setFont(Font.font("Segoe UI", 11));
        lblCampos.setStyle(
            "-fx-text-fill: " + COLOR_INFO + ";" +
            "-fx-background-color: " + COLOR_INFO + "15;" +
            "-fx-padding: 2 8;" +
            "-fx-background-radius: 10;"
        );
        
        if (borrador.tipo != null && !borrador.tipo.isBlank()) {
            Label lblTipo = new Label(borrador.tipo);
            lblTipo.setFont(Font.font("Segoe UI", 10));
            lblTipo.setStyle(
                "-fx-text-fill: " + COLOR_TEXT_SECONDARY() + ";" +
                "-fx-background-color: " + COLOR_SURFACE() + ";" +
                "-fx-padding: 2 8;" +
                "-fx-background-radius: 10;"
            );
            metaRow.getChildren().addAll(lblFecha, lblCampos, lblTipo);
        } else {
            metaRow.getChildren().addAll(lblFecha, lblCampos);
        }
        
        info.getChildren().addAll(lblResumen, metaRow);
        
        // Botones de acción
        Button btnCargar = new Button("Cargar");
        btnCargar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        btnCargar.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 6 14;" +
            "-fx-cursor: hand;"
        );
        
        Button btnEliminar = new Button();
        btnEliminar.setGraphic(IconosSVG.eliminar(TemaManager.COLOR_DANGER, 14));
        btnEliminar.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-padding: 6;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 6;"
        );
        
        // Hover effects
        String cardStyleBase = card.getStyle();
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + COLOR_SURFACE() + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + COLOR_PRIMARY + "40;" +
                "-fx-border-radius: 10;" +
                "-fx-cursor: hand;"
            );
            AnimacionesFX.hoverIn(card, 1.02, 150);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(cardStyleBase);
            AnimacionesFX.hoverOut(card, 150);
        });
        
        btnEliminar.setOnMouseEntered(e -> {
            btnEliminar.setStyle(
                "-fx-background-color: " + TemaManager.COLOR_DANGER + "15;" +
                "-fx-padding: 6;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 6;"
            );
            AnimacionesFX.hoverIn(btnEliminar, 1.1, 120);
        });
        btnEliminar.setOnMouseExited(e -> {
            btnEliminar.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-padding: 6;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 6;"
            );
            AnimacionesFX.hoverOut(btnEliminar, 120);
        });
        
        // Acciones
        btnCargar.setOnAction(e -> {
            if (restaurarBorradorDesdeArchivo(borrador.archivo)) {
                borradorRestaurado = borrador.archivo; // Recordar cuál borrador fue restaurado
                dialog.close();
                mostrarToast("Borrador restaurado", COLOR_SUCCESS);
            } else {
                mostrarToast("Error al cargar borrador", TemaManager.COLOR_DANGER);
            }
        });
        
        btnEliminar.setOnAction(e -> {
            eliminarBorradorArchivo(borrador.archivo);
            // Refrescar diálogo
            dialog.close();
            javafx.application.Platform.runLater(() -> mostrarDialogoBorradores());
        });
        
        card.getChildren().addAll(iconWrapper, info, btnCargar, btnEliminar);
        return card;
    }

}
