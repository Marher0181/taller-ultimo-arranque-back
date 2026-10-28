package gt.umg.ultimoarranque.modelo;

import java.time.LocalDateTime;

public record DatosCuenta(boolean activo, int versionToken, LocalDateTime fechaCreacion, LocalDateTime ultimoAcceso) {

    public static DatosCuenta nueva() {
        return new DatosCuenta(true, 0, FechaHora.ahora(), null);
    }
}
