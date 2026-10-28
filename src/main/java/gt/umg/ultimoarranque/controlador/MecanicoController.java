package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.dto.MecanicoRequest;
import gt.umg.ultimoarranque.dto.MecanicoResponse;
import gt.umg.ultimoarranque.modelo.consulta.OrdenResumen;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import gt.umg.ultimoarranque.servicio.MecanicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/mecanicos")
public class MecanicoController {

    private final MecanicoService mecanicoService;

    public MecanicoController(MecanicoService mecanicoService) {
        this.mecanicoService = mecanicoService;
    }

    @GetMapping
    public List<MecanicoResponse> listar() {
        return mecanicoService.listar().stream().map(MecanicoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public MecanicoResponse obtener(@PathVariable long id) {
        return MecanicoResponse.desde(mecanicoService.obtener(id));
    }

    @GetMapping("/{id}/ordenes")
    public List<OrdenResumen> listarOrdenes(@PathVariable long id, @RequestParam(required = false) EstadoOrden estado) {
        return mecanicoService.listarOrdenes(id, estado);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MecanicoResponse registrar(@Valid @RequestBody MecanicoRequest solicitud) {
        return MecanicoResponse.desde(mecanicoService.registrar(solicitud));
    }

    @PutMapping("/{id}")
    public MecanicoResponse actualizar(@PathVariable long id, @Valid @RequestBody MecanicoRequest solicitud) {
        return MecanicoResponse.desde(mecanicoService.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id) {
        mecanicoService.eliminar(id);
    }
}
