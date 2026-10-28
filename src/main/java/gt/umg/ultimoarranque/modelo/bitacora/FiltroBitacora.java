package gt.umg.ultimoarranque.modelo.bitacora;

import java.time.LocalDate;

public record FiltroBitacora(
        Long idUsuario,
        Accion accion,
        EntidadBitacora entidad,
        Long idEntidad,
        LocalDate desde,
        LocalDate hasta,
        int limite) {
}
