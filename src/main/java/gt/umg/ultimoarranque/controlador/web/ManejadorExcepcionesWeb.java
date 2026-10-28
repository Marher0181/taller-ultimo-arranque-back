package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.excepcion.AccesoDenegadoException;
import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;
import gt.umg.ultimoarranque.excepcion.IntegridadDatosException;
import gt.umg.ultimoarranque.excepcion.OperacionNoPermitidaException;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.excepcion.RegistroConDependenciasException;
import gt.umg.ultimoarranque.excepcion.RegistroDuplicadoException;
import gt.umg.ultimoarranque.excepcion.ReglaNegocioException;
import gt.umg.ultimoarranque.excepcion.TallerException;
import gt.umg.ultimoarranque.seguridad.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Comportamiento común de los controladores web. Un error de negocio o de validación regresa a la página anterior y
 * reabre el diálogo o formulario que lo produjo con lo que el usuario había escrito, igual que hacía el front Angular.
 */
@ControllerAdvice(basePackageClasses = ManejadorExcepcionesWeb.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ManejadorExcepcionesWeb {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorExcepcionesWeb.class);
    private static final String CAMPO_FORMULARIO = "_formulario";

    private final FormatoVista vista;

    public ManejadorExcepcionesWeb(FormatoVista vista) {
        this.vista = vista;
    }

    @InitBinder
    public void configurarBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /** Formatos y permisos para las plantillas: {@code ${vista.moneda(...)}} (los {@code @bean} no se permiten en atributos). */
    @ModelAttribute("vista")
    public FormatoVista vista() {
        return vista;
    }

    @ModelAttribute("sesionActual")
    public UsuarioAutenticado sesionActual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario;
    }

    /** Hora a la que vence la sesión si no hay actividad ("Sesión activa hasta las …"). */
    @ModelAttribute("sesionExpira")
    public LocalDateTime sesionExpira(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return null;
        }
        Instant vence = Instant.ofEpochMilli(sesion.getLastAccessedTime()).plusSeconds(sesion.getMaxInactiveInterval());
        return LocalDateTime.ofInstant(vence, ZoneId.systemDefault());
    }

    @ExceptionHandler(TallerException.class)
    public String errorDeNegocio(TallerException ex, HttpServletRequest request, RedirectAttributes atributos) {
        return volver(request, atributos, titulo(ex), ex.getMessage(), Map.of());
    }

    @ExceptionHandler(BindException.class)
    public String datosInvalidos(BindException ex, HttpServletRequest request, RedirectAttributes atributos) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.isBindingFailure() ? "Valor inválido" : error.getDefaultMessage());
        }
        return volver(request, atributos, "Datos de entrada inválidos", "Revise los campos indicados", errores);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorInterno(Exception ex, Model model) {
        LOG.error("Error no controlado en la interfaz web", ex);
        model.addAttribute("mensaje", "Ocurrió un error inesperado al procesar la solicitud");
        return "error";
    }

    private static String titulo(TallerException ex) {
        return switch (ex) {
            case RecursoNoEncontradoException ignorada -> "Recurso no encontrado";
            case AccesoDenegadoException ignorada -> "Acceso denegado";
            case DatoInvalidoException ignorada -> "Dato inválido";
            case RegistroDuplicadoException ignorada -> "Conflicto con el estado actual";
            case RegistroConDependenciasException ignorada -> "Conflicto con el estado actual";
            case OperacionNoPermitidaException ignorada -> "Conflicto con el estado actual";
            case IntegridadDatosException ignorada -> "Conflicto con el estado actual";
            case ReglaNegocioException ignorada -> "Regla de negocio incumplida";
            default -> "No se pudo completar la operación";
        };
    }

    private static String volver(HttpServletRequest request, RedirectAttributes atributos, String titulo, String mensaje,
                                 Map<String, String> errores) {
        Map<String, Object> reabrir = new LinkedHashMap<>();
        reabrir.put("titulo", titulo);
        reabrir.put("mensaje", mensaje);
        reabrir.put("errores", errores);
        reabrir.put("formulario", request.getParameter(CAMPO_FORMULARIO));
        reabrir.put("accion", request.getRequestURI());
        Map<String, String[]> valores = new LinkedHashMap<>();
        request.getParameterMap().forEach((nombre, valor) -> {
            // Las contraseñas no regresan al navegador
            if (!nombre.equals("_csrf") && !nombre.toLowerCase().contains("password")) {
                valores.put(nombre, valor);
            }
        });
        reabrir.put("valores", valores);
        atributos.addFlashAttribute("reabrir", reabrir);
        return "redirect:" + paginaAnterior(request);
    }

    private static String paginaAnterior(HttpServletRequest request) {
        String origen = request.getHeader(HttpHeaders.REFERER);
        if (origen == null) {
            return "/";
        }
        try {
            // Solo la ruta: evita redirigir a otro dominio con un Referer manipulado
            URI uri = new URI(origen);
            String ruta = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
            return ruta + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        } catch (URISyntaxException ex) {
            return "/";
        }
    }
}
