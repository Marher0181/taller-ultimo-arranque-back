package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioActualizacionRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotNull Rol rol,
        Long idMecanico) {
}
