package gt.umg.ultimoarranque.modelo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class FechaHora {

    private FechaHora() {
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
