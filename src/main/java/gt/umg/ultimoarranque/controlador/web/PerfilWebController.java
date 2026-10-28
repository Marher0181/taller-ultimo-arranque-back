package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.CambioPasswordRequest;
import gt.umg.ultimoarranque.dto.UsuarioResponse;
import gt.umg.ultimoarranque.modelo.Usuario;
import gt.umg.ultimoarranque.seguridad.UsuarioAutenticado;
import gt.umg.ultimoarranque.servicio.AutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilWebController {

    private final AutenticacionService autenticacionService;
    private final HttpSessionSecurityContextRepository repositorioContexto = new HttpSessionSecurityContextRepository();

    public PerfilWebController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @GetMapping
    public String perfil(Model model) {
        model.addAttribute("perfil", UsuarioResponse.desde(autenticacionService.perfil()));
        return "perfil";
    }

    /**
     * Cambiar la contraseña incrementa la versión del token: esta sesión se actualiza con la versión nueva para seguir
     * abierta, y las de otros equipos quedan invalidadas por {@code FiltroSesionWeb}.
     */
    @PostMapping("/password")
    public String cambiarPassword(@Valid CambioPasswordRequest solicitud, HttpServletRequest request,
                                  HttpServletResponse response, RedirectAttributes atributos) {
        Usuario usuario = autenticacionService.cambiarPassword(solicitud.passwordActual(), solicitud.passwordNueva())
                .usuario();
        Authentication actual = SecurityContextHolder.getContext().getAuthentication();
        UsernamePasswordAuthenticationToken renovada = UsernamePasswordAuthenticationToken.authenticated(
                UsuarioAutenticado.de(usuario), null, actual.getAuthorities());
        renovada.setDetails(usuario.getVersionToken());
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(renovada);
        SecurityContextHolder.setContext(contexto);
        repositorioContexto.saveContext(contexto, request, response);
        Aviso.exito(atributos, "Contraseña cambiada", "Las sesiones abiertas en otros equipos se cerraron.");
        return "redirect:/perfil";
    }
}
