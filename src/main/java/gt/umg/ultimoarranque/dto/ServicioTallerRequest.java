package gt.umg.ultimoarranque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicioTallerRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 150) String descripcion,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal costoManoObra) {
}
