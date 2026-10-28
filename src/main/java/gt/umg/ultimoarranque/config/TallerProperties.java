package gt.umg.ultimoarranque.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "taller")
public record TallerProperties(
        @NotNull @Valid Inventario inventario,
        @NotNull @Valid Facturacion facturacion,
        @NotNull @Valid Cors cors,
        @NotNull @Valid Seguridad seguridad) {

    public record Inventario(@PositiveOrZero int umbralBajo) {
    }

    public record Facturacion(@NotNull @DecimalMin("0.00") @DecimalMax("1.00") BigDecimal tasaIva) {
    }

    public record Cors(@NotEmpty List<String> origenesPermitidos) {
    }

    public record Seguridad(@NotNull @Valid Jwt jwt) {
    }

    public record Jwt(
            @NotBlank @Size(min = 32) String secreto,
            @NotNull Duration duracion,
            @NotBlank String emisor) {
    }
}
