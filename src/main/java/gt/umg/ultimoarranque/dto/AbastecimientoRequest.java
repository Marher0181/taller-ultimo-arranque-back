package gt.umg.ultimoarranque.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AbastecimientoRequest(@NotNull @Positive Integer cantidad) {
}
