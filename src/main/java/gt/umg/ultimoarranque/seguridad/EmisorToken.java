package gt.umg.ultimoarranque.seguridad;

import gt.umg.ultimoarranque.modelo.Usuario;

public interface EmisorToken {

    TokenEmitido emitir(Usuario usuario);
}
