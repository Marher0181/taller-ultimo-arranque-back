package gt.umg.ultimoarranque.seguridad;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import gt.umg.ultimoarranque.config.TallerProperties;
import gt.umg.ultimoarranque.modelo.Rol;
import gt.umg.ultimoarranque.servicio.AutenticacionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private static final String ALGORITMO_CLAVE = "HmacSHA256";
    private static final String[] RUTAS_PUBLICAS = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error"};
    private static final String ADMIN = Rol.ADMIN.name();
    private static final String RECEPCION = Rol.RECEPCION.name();
    private static final String MECANICO = Rol.MECANICO.name();

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http, AutenticacionService autenticacionService,
                                               RespuestaErrorSeguridad respuestaError) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(this::reglasDeAcceso)
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new ConvertidorJwtUsuario(autenticacionService)))
                        .authenticationEntryPoint(respuestaError)
                        .accessDeniedHandler(respuestaError))
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(respuestaError)
                        .accessDeniedHandler(respuestaError));
        return http.build();
    }

    @Bean
    public PasswordEncoder codificadorPassword() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder codificadorJwt(TallerProperties propiedades) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveSecreta(propiedades)));
    }

    @Bean
    public JwtDecoder decodificadorJwt(TallerProperties propiedades) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(claveSecreta(propiedades))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decodificador.setJwtValidator(JwtValidators.createDefaultWithIssuer(propiedades.seguridad().jwt().emisor()));
        return decodificador;
    }

    private void reglasDeAcceso(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry reglas) {
        reglas.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(RUTAS_PUBLICAS).permitAll()
                .requestMatchers("/api/auth/**").authenticated()
                .requestMatchers("/api/usuarios/**", "/api/bitacora/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/repuestos/**", "/api/servicios/**", "/api/ordenes/**")
                .hasAnyRole(ADMIN, RECEPCION, MECANICO)
                .requestMatchers("/api/ordenes/*/servicios/**").hasAnyRole(ADMIN, RECEPCION, MECANICO)
                .requestMatchers("/api/ordenes/*/iniciar", "/api/ordenes/*/repuestos/**", "/api/ordenes/*/finalizar")
                .hasAnyRole(ADMIN, MECANICO)
                .requestMatchers("/api/**").hasAnyRole(ADMIN, RECEPCION)
                .anyRequest().denyAll();
    }

    private static SecretKey claveSecreta(TallerProperties propiedades) {
        byte[] secreto = propiedades.seguridad().jwt().secreto().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(secreto, ALGORITMO_CLAVE);
    }
}
