package gt.umg.ultimoarranque.seguridad;

import java.time.LocalDateTime;

public record TokenEmitido(String valor, long duracionSegundos, LocalDateTime expiraEl) {
}
