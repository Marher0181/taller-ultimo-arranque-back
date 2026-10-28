package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.excepcion.TallerException;
import gt.umg.ultimoarranque.seguridad.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

/**
 * Comportamiento común de los controladores web: errores de negocio y de validación regresan a la página anterior
 * con un mensaje (flash), en lugar del {@code problem+json} que usa la API.
 */
@ControllerAdvice(basePackageClasses = ManejadorExcepcionesWeb.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ManejadorExcepcionesWeb {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorExcepcionesWeb.class);

    @InitBinder
    public void configurarBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ModelAttribute("sesionActual")
    public UsuarioAutenticado sesionActual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario;
    }

    @ExceptionHandler(TallerException.class)
    public String errorDeNegocio(TallerException ex, HttpServletRequest request, RedirectAttributes atributos) {
        return volver(request, atributos, ex.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public String datosInvalidos(BindException ex, HttpServletRequest request, RedirectAttributes atributos) {
        String mensaje = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + (error.isBindingFailure() ? "valor inválido" : error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return volver(request, atributos, "Revise los datos ingresados. " + mensaje);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorInterno(Exception ex, Model model) {
        LOG.error("Error no controlado en la interfaz web", ex);
        model.addAttribute("mensaje", "Ocurrió un error inesperado al procesar la solicitud");
        return "error";
    }

    private static String volver(HttpServletRequest request, RedirectAttributes atributos, String mensaje) {
        atributos.addFlashAttribute("mensajeError", mensaje);
        String origen = request.getHeader(HttpHeaders.REFERER);
        if (origen == null) {
            return "redirect:/";
        }
        try {
            // Solo la ruta: evita redirigir a otro dominio con un Referer manipulado
            URI uri = new URI(origen);
            String ruta = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
            return "redirect:" + ruta + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        } catch (URISyntaxException ex) {
            return "redirect:/";
        }
    }
}
