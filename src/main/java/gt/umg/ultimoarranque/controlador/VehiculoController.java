package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.dto.VehiculoRequest;
import gt.umg.ultimoarranque.dto.VehiculoResponse;
import gt.umg.ultimoarranque.modelo.consulta.OrdenResumen;
import gt.umg.ultimoarranque.servicio.VehiculoService;
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
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public List<VehiculoResponse> listar(@RequestParam(required = false) String placa) {
        return vehiculoService.listar(placa).stream().map(VehiculoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public VehiculoResponse obtener(@PathVariable long id) {
        return VehiculoResponse.desde(vehiculoService.obtener(id));
    }

    @GetMapping("/placa/{placa}")
    public VehiculoResponse obtenerPorPlaca(@PathVariable String placa) {
        return VehiculoResponse.desde(vehiculoService.obtenerPorPlaca(placa));
    }

    @GetMapping("/{id}/ordenes")
    public List<OrdenResumen> historial(@PathVariable long id) {
        return vehiculoService.historial(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoResponse registrar(@Valid @RequestBody VehiculoRequest solicitud) {
        return VehiculoResponse.desde(vehiculoService.registrar(solicitud));
    }

    @PutMapping("/{id}")
    public VehiculoResponse actualizar(@PathVariable long id, @Valid @RequestBody VehiculoRequest solicitud) {
        return VehiculoResponse.desde(vehiculoService.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id) {
        vehiculoService.eliminar(id);
    }
}
