package gt.umg.ultimoarranque.modelo.bitacora;

import gt.umg.ultimoarranque.modelo.FechaHora;

import java.time.LocalDateTime;

public record EntradaBitacora(
        Long id,
        LocalDateTime fecha,
        Long idUsuario,
        String usuario,
        Accion accion,
        EntidadBitacora entidad,
        Long idEntidad,
        String detalle,
        String direccionIp) {

    private static final int LONGITUD_MAXIMA_DETALLE = 500;

    public static EntradaBitacora nueva(Long idUsuario, String usuario, Accion accion, EntidadBitacora entidad,
                                        Long idEntidad, String detalle, String direccionIp) {
        return new EntradaBitacora(null, FechaHora.ahora(), idUsuario, usuario, accion, entidad, idEntidad,
                recortar(detalle), direccionIp);
    }

    private static String recortar(String detalle) {
        if (detalle == null || detalle.length() <= LONGITUD_MAXIMA_DETALLE) {
            return detalle;
        }
        return detalle.substring(0, LONGITUD_MAXIMA_DETALLE);
    }
}
