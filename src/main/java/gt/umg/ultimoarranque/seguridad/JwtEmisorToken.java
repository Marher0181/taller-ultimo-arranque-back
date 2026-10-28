package gt.umg.ultimoarranque.seguridad;

import gt.umg.ultimoarranque.config.TallerProperties;
import gt.umg.ultimoarranque.modelo.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class JwtEmisorToken implements EmisorToken {

    private final JwtEncoder codificador;
    private final Duration duracion;
    private final String emisor;

    public JwtEmisorToken(JwtEncoder codificador, TallerProperties propiedades) {
        this.codificador = codificador;
        this.duracion = propiedades.seguridad().jwt().duracion();
        this.emisor = propiedades.seguridad().jwt().emisor();
    }

    @Override
    public TokenEmitido emitir(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(duracion);
        JwtClaimsSet reclamos = JwtClaimsSet.builder()
                .issuer(emisor)
                .issuedAt(ahora)
                .expiresAt(expiracion)
                .subject(String.valueOf(usuario.getId()))
                .claim(ClaimsJwt.USUARIO, usuario.getUsername())
                .claim(ClaimsJwt.ROL, usuario.getRol().name())
                .claim(ClaimsJwt.VERSION, usuario.getVersionToken())
                .build();
        JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = codificador.encode(JwtEncoderParameters.from(encabezado, reclamos)).getTokenValue();
        return new TokenEmitido(valor, duracion.toSeconds(), LocalDateTime.ofInstant(expiracion, ZoneId.systemDefault()));
    }
}
