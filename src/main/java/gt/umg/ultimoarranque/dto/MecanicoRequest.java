package gt.umg.ultimoarranque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MecanicoRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 80) String especialidad,
        @Size(max = 20) String telefono) {
}
