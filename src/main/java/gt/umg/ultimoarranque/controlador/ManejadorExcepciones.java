package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.excepcion.AccesoDenegadoException;
import gt.umg.ultimoarranque.excepcion.CredencialesInvalidasException;
import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;
import gt.umg.ultimoarranque.excepcion.IntegridadDatosException;
import gt.umg.ultimoarranque.excepcion.OperacionNoPermitidaException;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.excepcion.RegistroConDependenciasException;
import gt.umg.ultimoarranque.excepcion.RegistroDuplicadoException;
import gt.umg.ultimoarranque.excepcion.ReglaNegocioException;
import gt.umg.ultimoarranque.excepcion.TallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class ManejadorExcepciones extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorExcepciones.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail recursoNoEncontrado(RecursoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail credencialesInvalidas(CredencialesInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", ex.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail accesoDenegado(AccesoDenegadoException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(DatoInvalidoException.class)
    public ProblemDetail datoInvalido(DatoInvalidoException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Dato inválido", ex.getMessage());
    }

    @ExceptionHandler({
            RegistroDuplicadoException.class,
            RegistroConDependenciasException.class,
            OperacionNoPermitidaException.class,
            IntegridadDatosException.class
    })
    public ProblemDetail conflicto(TallerException ex) {
        return problema(HttpStatus.CONFLICT, "Conflicto con el estado actual", ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaNegocio(ReglaNegocioException ex) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio incumplida", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail errorInterno(Exception ex) {
        LOG.error("Error no controlado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado al procesar la solicitud");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), Objects.requireNonNullElse(error.getDefaultMessage(), "Valor inválido"));
        }
        ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos",
                "Revise los campos indicados");
        detalle.setProperty("errores", errores);
        return handleExceptionInternal(ex, detalle, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Solicitud mal formada",
                "El cuerpo de la solicitud no es un JSON válido o contiene valores no reconocidos");
        return handleExceptionInternal(ex, detalle, headers, status, request);
    }

    private ProblemDetail problema(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        return problema;
    }
}
