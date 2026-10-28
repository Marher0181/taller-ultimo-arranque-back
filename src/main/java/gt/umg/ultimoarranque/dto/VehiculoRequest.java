package gt.umg.ultimoarranque.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehiculoRequest(
        @NotBlank @Size(max = 10) String placa,
        @NotBlank @Size(max = 50) String marca,
        @NotBlank @Size(max = 50) String modelo,
        @NotNull @Min(1900) @Max(2100) Integer anio,
        @Size(max = 30) String color,
        @NotNull Long idCliente) {
}
