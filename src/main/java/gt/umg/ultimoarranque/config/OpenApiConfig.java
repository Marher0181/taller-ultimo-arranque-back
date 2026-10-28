package gt.umg.ultimoarranque.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "jwt";

    @Bean
    public OpenAPI documentacionApi() {
        SecurityScheme esquemaJwt = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        return new OpenAPI()
                .info(new Info()
                        .title("API Taller Mecánico \"El Último Arranque\"")
                        .version("1.1.0")
                        .description("Gestión de clientes, vehículos, mecánicos, repuestos, servicios y órdenes de trabajo. "
                                + "Inicie sesión en /api/auth/login y use el token con el botón Authorize."))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, esquemaJwt))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
