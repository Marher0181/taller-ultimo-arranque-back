package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.dto.ServicioTallerRequest;
import gt.umg.ultimoarranque.dto.ServicioTallerResponse;
import gt.umg.ultimoarranque.servicio.ServicioTallerService;
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
@RequestMapping("/api/servicios")
public class ServicioTallerController {

    private final ServicioTallerService servicioTallerService;

    public ServicioTallerController(ServicioTallerService servicioTallerService) {
        this.servicioTallerService = servicioTallerService;
    }

    @GetMapping
    public List<ServicioTallerResponse> listar() {
        return servicioTallerService.listar().stream().map(ServicioTallerResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public ServicioTallerResponse obtener(@PathVariable long id) {
        return ServicioTallerResponse.desde(servicioTallerService.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioTallerResponse registrar(@Valid @RequestBody ServicioTallerRequest solicitud) {
        return ServicioTallerResponse.desde(servicioTallerService.registrar(solicitud));
    }

    @PutMapping("/{id}")
    public ServicioTallerResponse actualizar(@PathVariable long id, @Valid @RequestBody ServicioTallerRequest solicitud) {
        return ServicioTallerResponse.desde(servicioTallerService.actualizar(id, solicitud));
    }
}
