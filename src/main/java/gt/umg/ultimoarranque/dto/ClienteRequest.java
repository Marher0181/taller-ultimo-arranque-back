package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.documento.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotNull TipoDocumento tipoDocumento,
        @NotBlank @Size(max = 20) String numeroDocumento,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 20) String telefono,
        @Email @Size(max = 120) String correo,
        @Size(max = 200) String direccion) {
}
