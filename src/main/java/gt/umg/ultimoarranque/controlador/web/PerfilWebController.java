package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.CambioPasswordRequest;
import gt.umg.ultimoarranque.dto.UsuarioResponse;
import gt.umg.ultimoarranque.servicio.AutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/perfil")
public class PerfilWebController {

    private final AutenticacionService autenticacionService;

    public PerfilWebController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @GetMapping
    public String perfil(Model model) {
        model.addAttribute("perfil", UsuarioResponse.desde(autenticacionService.perfil()));
        return "perfil";
    }

    /** Cambiar la contraseña incrementa la versión del token: se cierra la sesión y se pide iniciar de nuevo. */
    @PostMapping("/password")
    public String cambiarPassword(@Valid CambioPasswordRequest solicitud, HttpServletRequest request) {
        autenticacionService.cambiarPassword(solicitud.passwordActual(), solicitud.passwordNueva());
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();
        return "redirect:/login?cambio";
    }
}
