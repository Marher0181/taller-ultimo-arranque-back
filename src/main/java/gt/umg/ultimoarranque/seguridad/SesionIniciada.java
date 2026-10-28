package gt.umg.ultimoarranque.seguridad;

import gt.umg.ultimoarranque.modelo.Usuario;

public record SesionIniciada(TokenEmitido token, Usuario usuario) {
}
