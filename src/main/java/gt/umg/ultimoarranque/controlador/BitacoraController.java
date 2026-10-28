package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import gt.umg.ultimoarranque.modelo.bitacora.EntradaBitacora;
import gt.umg.ultimoarranque.modelo.bitacora.FiltroBitacora;
import gt.umg.ultimoarranque.servicio.BitacoraService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bitacora")
public class BitacoraController {

    private final BitacoraService bitacoraService;

    public BitacoraController(BitacoraService bitacoraService) {
        this.bitacoraService = bitacoraService;
    }

    @GetMapping
    public List<EntradaBitacora> consultar(
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) Accion accion,
            @RequestParam(required = false) EntidadBitacora entidad,
            @RequestParam(required = false) Long idEntidad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "200") int limite) {
        return bitacoraService.consultar(new FiltroBitacora(idUsuario, accion, entidad, idEntidad, desde, hasta, limite));
    }
}
