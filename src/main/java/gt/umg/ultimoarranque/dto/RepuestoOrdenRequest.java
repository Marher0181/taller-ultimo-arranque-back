package gt.umg.ultimoarranque.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RepuestoOrdenRequest(@NotNull Long idRepuesto, @NotNull @Positive Integer cantidad) {
}
