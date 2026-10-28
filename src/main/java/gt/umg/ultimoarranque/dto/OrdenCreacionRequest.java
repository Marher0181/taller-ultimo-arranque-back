package gt.umg.ultimoarranque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrdenCreacionRequest(
        @NotNull Long idVehiculo,
        @NotNull Long idMecanico,
        @NotBlank @Size(max = 500) String descripcionProblema,
        List<@NotNull Long> serviciosIds) {

    public List<Long> serviciosSolicitados() {
        return serviciosIds == null ? List.of() : serviciosIds.stream().distinct().toList();
    }
}
