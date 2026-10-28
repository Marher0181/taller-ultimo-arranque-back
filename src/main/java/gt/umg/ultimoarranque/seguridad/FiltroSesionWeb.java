package gt.umg.ultimoarranque.seguridad;

import gt.umg.ultimoarranque.servicio.AutenticacionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Equivalente web de {@link ConvertidorJwtUsuario}: en cada solicitud compara la versión de token guardada en la sesión
 * con la de la base, así desactivar al usuario o cambiar su contraseña/rol cierra también su sesión web.
 */
public class FiltroSesionWeb extends OncePerRequestFilter {

    private final AutenticacionService autenticacionService;

    public FiltroSesionWeb(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws ServletException, IOException {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario
                && autenticacion.getDetails() instanceof Integer version
                && autenticacionService.validarSesion(usuario.id(), version).isEmpty()) {
            SecurityContextHolder.clearContext();
            HttpSession sesion = request.getSession(false);
            if (sesion != null) {
                sesion.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login?expirada");
            return;
        }
        cadena.doFilter(request, response);
    }
}
