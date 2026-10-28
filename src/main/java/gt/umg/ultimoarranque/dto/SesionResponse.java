package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.seguridad.SesionIniciada;

import java.time.LocalDateTime;

public record SesionResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        LocalDateTime expiraEl,
        UsuarioResponse usuario) {

    private static final String TIPO_TOKEN = "Bearer";

    public static SesionResponse desde(SesionIniciada sesion) {
        return new SesionResponse(
                sesion.token().valor(),
                TIPO_TOKEN,
                sesion.token().duracionSegundos(),
                sesion.token().expiraEl(),
                UsuarioResponse.desde(sesion.usuario()));
    }
}
