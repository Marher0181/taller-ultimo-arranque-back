package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Pattern(regexp = ReglasPassword.PATRON, message = ReglasPassword.MENSAJE) String password,
        @NotNull Rol rol,
        Long idMecanico) {
}
