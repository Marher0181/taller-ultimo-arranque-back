package gt.umg.ultimoarranque.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
public class RespuestaErrorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper mapeador;

    public RespuestaErrorSeguridad(ObjectMapper mapeador) {
        this.mapeador = mapeador;
    }

    @Override
    public void commence(HttpServletRequest solicitud, HttpServletResponse respuesta, AuthenticationException ex)
            throws IOException {
        respuesta.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        escribir(solicitud, respuesta, HttpStatus.UNAUTHORIZED, "No autenticado",
                "Debe iniciar sesión; el token no se envió, es inválido o expiró");
    }

    @Override
    public void handle(HttpServletRequest solicitud, HttpServletResponse respuesta, AccessDeniedException ex)
            throws IOException {
        escribir(solicitud, respuesta, HttpStatus.FORBIDDEN, "Acceso denegado",
                "Su rol no tiene permiso para realizar esta operación");
    }

    private void escribir(HttpServletRequest solicitud, HttpServletResponse respuesta, HttpStatus estado,
                          String titulo, String detalle) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        problema.setInstance(URI.create(solicitud.getRequestURI()));
        respuesta.setStatus(estado.value());
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        mapeador.writeValue(respuesta.getOutputStream(), problema);
    }
}
