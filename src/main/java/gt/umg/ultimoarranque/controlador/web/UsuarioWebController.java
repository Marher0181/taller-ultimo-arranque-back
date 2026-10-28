package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.MecanicoResponse;
import gt.umg.ultimoarranque.dto.PasswordRequest;
import gt.umg.ultimoarranque.dto.UsuarioActualizacionRequest;
import gt.umg.ultimoarranque.dto.UsuarioRequest;
import gt.umg.ultimoarranque.dto.UsuarioResponse;
import gt.umg.ultimoarranque.modelo.Rol;
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

@Controller
@RequestMapping("/usuarios")
public class UsuarioWebController {

    private final UsuarioService usuarioService;
    private final MecanicoService mecanicoService;

    public UsuarioWebController(UsuarioService usuarioService, MecanicoService mecanicoService) {
        this.usuarioService = usuarioService;
        this.mecanicoService = mecanicoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar().stream().map(UsuarioResponse::desde).toList());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return formulario(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        return formulario(model, UsuarioResponse.desde(usuarioService.obtener(id)));
    }

    @PostMapping
    public String registrar(@Valid UsuarioRequest solicitud, RedirectAttributes atributos) {
        usuarioService.registrar(solicitud);
        atributos.addFlashAttribute("mensajeExito", "Usuario registrado");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid UsuarioActualizacionRequest solicitud,
                             RedirectAttributes atributos) {
        usuarioService.actualizar(id, solicitud);
        atributos.addFlashAttribute("mensajeExito", "Usuario actualizado");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable long id, RedirectAttributes atributos) {
        usuarioService.activar(id);
        atributos.addFlashAttribute("mensajeExito", "Usuario activado");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable long id, RedirectAttributes atributos) {
        usuarioService.desactivar(id);
        atributos.addFlashAttribute("mensajeExito", "Usuario desactivado");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/restablecer-password")
    public String restablecerPassword(@PathVariable long id, @Valid PasswordRequest solicitud,
                                      RedirectAttributes atributos) {
        usuarioService.restablecerPassword(id, solicitud.password());
        atributos.addFlashAttribute("mensajeExito", "Contraseña restablecida");
        return "redirect:/usuarios";
    }

    private String formulario(Model model, UsuarioResponse usuario) {
        model.addAttribute("form", usuario);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("mecanicos", mecanicoService.listar().stream().map(MecanicoResponse::desde).toList());
        return "usuarios/form";
    }
}
