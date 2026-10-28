package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import gt.umg.ultimoarranque.modelo.bitacora.FiltroBitacora;
import gt.umg.ultimoarranque.servicio.BitacoraService;
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

    public BitacoraWebController(BitacoraService bitacoraService) {
        this.bitacoraService = bitacoraService;
    }

    @GetMapping
    public String consultar(@RequestParam(required = false) Accion accion,
                            @RequestParam(required = false) EntidadBitacora entidad,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                            Model model) {
        FiltroBitacora filtro = new FiltroBitacora(null, accion, entidad, null, desde, hasta, LIMITE);
        model.addAttribute("entradas", bitacoraService.consultar(filtro));
        model.addAttribute("filtro", filtro);
        model.addAttribute("acciones", Accion.values());
        model.addAttribute("entidades", EntidadBitacora.values());
        return "bitacora/lista";
    }
}
