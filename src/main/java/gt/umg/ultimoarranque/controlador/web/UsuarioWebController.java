package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.MecanicoResponse;
import gt.umg.ultimoarranque.dto.PasswordRequest;
import gt.umg.ultimoarranque.dto.UsuarioActualizacionRequest;
import gt.umg.ultimoarranque.dto.UsuarioRequest;
import gt.umg.ultimoarranque.dto.UsuarioResponse;
import gt.umg.ultimoarranque.modelo.Rol;
import gt.umg.ultimoarranque.modelo.Usuario;
import gt.umg.ultimoarranque.servicio.MecanicoService;
import gt.umg.ultimoarranque.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/usuarios")
public class UsuarioWebController {

    private static final String LISTA = "redirect:/usuarios";

    private final UsuarioService usuarioService;
    private final MecanicoService mecanicoService;

    public UsuarioWebController(UsuarioService usuarioService, MecanicoService mecanicoService) {
        this.usuarioService = usuarioService;
        this.mecanicoService = mecanicoService;
    }

    @GetMapping
    public String listar(Model model) {
        List<UsuarioResponse> usuarios = usuarioService.listar().stream().map(UsuarioResponse::desde).toList();
        List<MecanicoResponse> mecanicos = mecanicoService.listar().stream().map(MecanicoResponse::desde).toList();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("mecanicos", mecanicos);
        model.addAttribute("codigos", mecanicos.stream().collect(Collectors.toMap(MecanicoResponse::id, MecanicoResponse::codigo)));
        // Un mecánico, un usuario: qué usuario tiene ya cada mecánico
        Map<Long, Long> ocupados = usuarios.stream().filter(u -> u.idMecanico() != null)
                .collect(Collectors.toMap(UsuarioResponse::idMecanico, UsuarioResponse::id, (a, b) -> a));
        model.addAttribute("ocupados", ocupados);
        model.addAttribute("roles", Rol.values());
        return "usuarios";
    }

    @PostMapping
    public String registrar(@Valid UsuarioRequest solicitud, RedirectAttributes atributos) {
        usuarioService.registrar(solicitud);
        Aviso.exito(atributos, "Usuario creado");
        return LISTA;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid UsuarioActualizacionRequest solicitud,
                             RedirectAttributes atributos) {
        Usuario anterior = usuarioService.obtener(id);
        usuarioService.actualizar(id, solicitud);
        Aviso.exito(atributos, "Usuario actualizado", anterior.getRol() != solicitud.rol()
                ? anterior.getUsername() + " deberá iniciar sesión de nuevo." : null);
        return LISTA;
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable long id, RedirectAttributes atributos) {
        Aviso.exito(atributos, usuarioService.activar(id).getUsername() + " activado");
        return LISTA;
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable long id, RedirectAttributes atributos) {
        Aviso.exito(atributos, usuarioService.desactivar(id).getUsername() + " desactivado");
        return LISTA;
    }

    @PostMapping("/{id}/restablecer-password")
    public String restablecerPassword(@PathVariable long id, @Valid PasswordRequest solicitud,
                                      RedirectAttributes atributos) {
        Usuario usuario = usuarioService.restablecerPassword(id, solicitud.password());
        Aviso.exito(atributos, "Contraseña restablecida", "Entréguela a " + usuario.getNombre() + ".");
        return LISTA;
    }
}
