package gt.umg.ultimoarranque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambioPasswordRequest(
        @NotBlank String passwordActual,
        @NotBlank @Pattern(regexp = ReglasPassword.PATRON, message = ReglasPassword.MENSAJE) String passwordNueva) {
}
