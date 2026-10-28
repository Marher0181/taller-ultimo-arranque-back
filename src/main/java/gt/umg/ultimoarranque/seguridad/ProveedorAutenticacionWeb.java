package gt.umg.ultimoarranque.seguridad;

import gt.umg.ultimoarranque.excepcion.CredencialesInvalidasException;
import gt.umg.ultimoarranque.modelo.Usuario;
import gt.umg.ultimoarranque.servicio.AutenticacionService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Login del formulario web: reutiliza {@link AutenticacionService#iniciarSesion} (misma validación y bitácora que la API).
 * La versión del token queda en los detalles para que {@link FiltroSesionWeb} invalide la sesión si cambia.
 */
@Component
public class ProveedorAutenticacionWeb implements AuthenticationProvider {

    private final AutenticacionService autenticacionService;

    public ProveedorAutenticacionWeb(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @Override
    public Authentication authenticate(Authentication solicitud) {
        Usuario usuario;
        try {
            usuario = autenticacionService.iniciarSesion(solicitud.getName(), String.valueOf(solicitud.getCredentials()))
                    .usuario();
        } catch (CredencialesInvalidasException ex) {
            throw new BadCredentialsException(ex.getMessage(), ex);
        }
        UsernamePasswordAuthenticationToken autenticacion = UsernamePasswordAuthenticationToken.authenticated(
                UsuarioAutenticado.de(usuario), null, List.of(new SimpleGrantedAuthority(usuario.getRol().autoridad())));
        autenticacion.setDetails(usuario.getVersionToken());
        return autenticacion;
    }

    @Override
    public boolean supports(Class<?> tipo) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(tipo);
    }
}
