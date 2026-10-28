package gt.umg.ultimoarranque.seguridad;

import gt.umg.ultimoarranque.servicio.AutenticacionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.List;

public class ConvertidorJwtUsuario implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final int VERSION_DESCONOCIDA = -1;

    private final AutenticacionService autenticacionService;

    public ConvertidorJwtUsuario(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Number version = jwt.getClaim(ClaimsJwt.VERSION);
        long idUsuario = Long.parseLong(jwt.getSubject());
        UsuarioAutenticado usuario = autenticacionService
                .validarSesion(idUsuario, version == null ? VERSION_DESCONOCIDA : version.intValue())
                .orElseThrow(() -> new InvalidBearerTokenException("La sesión ya no es válida; inicie sesión nuevamente"));
        return new UsernamePasswordAuthenticationToken(usuario, jwt,
                List.of(new SimpleGrantedAuthority(usuario.rol().autoridad())));
    }
}
