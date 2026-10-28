package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.dto.CambioPasswordRequest;
import gt.umg.ultimoarranque.dto.LoginRequest;
import gt.umg.ultimoarranque.dto.SesionResponse;
import gt.umg.ultimoarranque.dto.UsuarioResponse;
import gt.umg.ultimoarranque.servicio.AutenticacionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;

    public AutenticacionController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @PostMapping("/login")
    public SesionResponse iniciarSesion(@Valid @RequestBody LoginRequest solicitud) {
        return SesionResponse.desde(autenticacionService.iniciarSesion(solicitud.username(), solicitud.password()));
    }

    @GetMapping("/perfil")
    public UsuarioResponse perfil() {
        return UsuarioResponse.desde(autenticacionService.perfil());
    }

    @PostMapping("/cambiar-password")
    public SesionResponse cambiarPassword(@Valid @RequestBody CambioPasswordRequest solicitud) {
        return SesionResponse.desde(
                autenticacionService.cambiarPassword(solicitud.passwordActual(), solicitud.passwordNueva()));
    }
}
