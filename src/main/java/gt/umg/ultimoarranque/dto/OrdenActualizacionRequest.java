package gt.umg.ultimoarranque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrdenActualizacionRequest(
        @NotNull Long idMecanico,
        @NotBlank @Size(max = 500) String descripcionProblema) {
}
