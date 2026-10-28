package gt.umg.ultimoarranque.dto;

import jakarta.validation.constraints.NotNull;

public record ServicioOrdenRequest(@NotNull Long idServicio) {
}
