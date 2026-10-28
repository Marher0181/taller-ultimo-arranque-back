package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.dto.AbastecimientoRequest;
import gt.umg.ultimoarranque.dto.RepuestoActualizacionRequest;
import gt.umg.ultimoarranque.dto.RepuestoRequest;
import gt.umg.ultimoarranque.dto.RepuestoResponse;
import gt.umg.ultimoarranque.modelo.NivelInventario;
import gt.umg.ultimoarranque.modelo.Repuesto;
import gt.umg.ultimoarranque.servicio.RepuestoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/repuestos")
public class RepuestoController {

    private final RepuestoService repuestoService;

    public RepuestoController(RepuestoService repuestoService) {
        this.repuestoService = repuestoService;
    }

    @GetMapping
    public List<RepuestoResponse> listar(@RequestParam(required = false) NivelInventario nivel) {
        return responder(repuestoService.listar(nivel));
    }

    @GetMapping("/bajo-inventario")
    public List<RepuestoResponse> listarBajoInventario() {
        return responder(repuestoService.listarBajoInventario());
    }

    @GetMapping("/{id}")
    public RepuestoResponse obtener(@PathVariable long id) {
        return responder(repuestoService.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepuestoResponse registrar(@Valid @RequestBody RepuestoRequest solicitud) {
        return responder(repuestoService.registrar(solicitud));
    }

    @PutMapping("/{id}")
    public RepuestoResponse actualizar(@PathVariable long id, @Valid @RequestBody RepuestoActualizacionRequest solicitud) {
        return responder(repuestoService.actualizar(id, solicitud));
    }

    @PostMapping("/{id}/abastecer")
    public RepuestoResponse abastecer(@PathVariable long id, @Valid @RequestBody AbastecimientoRequest solicitud) {
        return responder(repuestoService.abastecer(id, solicitud.cantidad()));
    }

    private RepuestoResponse responder(Repuesto repuesto) {
        return RepuestoResponse.desde(repuesto, repuestoService.umbralBajo());
    }

    private List<RepuestoResponse> responder(List<Repuesto> repuestos) {
        return repuestos.stream().map(this::responder).toList();
    }
}
