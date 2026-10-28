package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.dto.ClienteRequest;
import gt.umg.ultimoarranque.dto.ClienteResponse;
import gt.umg.ultimoarranque.dto.VehiculoResponse;
import gt.umg.ultimoarranque.servicio.ClienteService;
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
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(required = false) String buscar) {
        return clienteService.listar(buscar).stream().map(ClienteResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable long id) {
        return ClienteResponse.desde(clienteService.obtener(id));
    }

    @GetMapping("/{id}/vehiculos")
    public List<VehiculoResponse> listarVehiculos(@PathVariable long id) {
        return clienteService.listarVehiculos(id).stream().map(VehiculoResponse::desde).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse registrar(@Valid @RequestBody ClienteRequest solicitud) {
        return ClienteResponse.desde(clienteService.registrar(solicitud));
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable long id, @Valid @RequestBody ClienteRequest solicitud) {
        return ClienteResponse.desde(clienteService.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id) {
        clienteService.eliminar(id);
    }
}
