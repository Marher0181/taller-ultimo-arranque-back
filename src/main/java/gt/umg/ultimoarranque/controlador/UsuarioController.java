package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.dto.PasswordRequest;
import gt.umg.ultimoarranque.dto.UsuarioActualizacionRequest;
import gt.umg.ultimoarranque.dto.UsuarioRequest;
import gt.umg.ultimoarranque.dto.UsuarioResponse;
import gt.umg.ultimoarranque.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream().map(UsuarioResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable long id) {
        return UsuarioResponse.desde(usuarioService.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody UsuarioRequest solicitud) {
        return UsuarioResponse.desde(usuarioService.registrar(solicitud));
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable long id, @Valid @RequestBody UsuarioActualizacionRequest solicitud) {
        return UsuarioResponse.desde(usuarioService.actualizar(id, solicitud));
    }

    @PostMapping("/{id}/activar")
    public UsuarioResponse activar(@PathVariable long id) {
        return UsuarioResponse.desde(usuarioService.activar(id));
    }

    @PostMapping("/{id}/desactivar")
    public UsuarioResponse desactivar(@PathVariable long id) {
        return UsuarioResponse.desde(usuarioService.desactivar(id));
    }

    @PostMapping("/{id}/restablecer-password")
    public UsuarioResponse restablecerPassword(@PathVariable long id, @Valid @RequestBody PasswordRequest solicitud) {
        return UsuarioResponse.desde(usuarioService.restablecerPassword(id, solicitud.password()));
    }
}
