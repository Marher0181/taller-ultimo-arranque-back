package gt.umg.ultimoarranque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordRequest(
        @NotBlank @Pattern(regexp = ReglasPassword.PATRON, message = ReglasPassword.MENSAJE) String password) {
}
