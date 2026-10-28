package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.UsuarioResponse;
import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import gt.umg.ultimoarranque.modelo.bitacora.FiltroBitacora;
import gt.umg.ultimoarranque.servicio.BitacoraService;
import gt.umg.ultimoarranque.servicio.UsuarioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/bitacora")
public class BitacoraWebController {

    private static final int LIMITE = 200;

    private final BitacoraService bitacoraService;
    private final UsuarioService usuarioService;

    public BitacoraWebController(BitacoraService bitacoraService, UsuarioService usuarioService) {
        this.bitacoraService = bitacoraService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String consultar(@RequestParam(required = false) Long idUsuario,
                            @RequestParam(required = false) Accion accion,
                            @RequestParam(required = false) EntidadBitacora entidad,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                            Model model) {
        FiltroBitacora filtro = new FiltroBitacora(idUsuario, accion, entidad, null, desde, hasta, LIMITE);
        model.addAttribute("registros", bitacoraService.consultar(filtro));
        model.addAttribute("filtro", filtro);
        model.addAttribute("usuarios", usuarioService.listar().stream().map(UsuarioResponse::desde).toList());
        model.addAttribute("acciones", Accion.values());
        model.addAttribute("entidades", EntidadBitacora.values());
        return "bitacora";
    }
}
