package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.ClienteResponse;
import gt.umg.ultimoarranque.dto.VehiculoRequest;
import gt.umg.ultimoarranque.dto.VehiculoResponse;
import gt.umg.ultimoarranque.servicio.ClienteService;
import gt.umg.ultimoarranque.servicio.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehiculos")
public class VehiculoWebController {

    private static final String LISTA = "redirect:/vehiculos";

    private final VehiculoService vehiculoService;
    private final ClienteService clienteService;

    public VehiculoWebController(VehiculoService vehiculoService, ClienteService clienteService) {
        this.vehiculoService = vehiculoService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vehiculos", vehiculoService.listar(null).stream().map(VehiculoResponse::desde).toList());
        model.addAttribute("clientes", clienteService.listar(null).stream().map(ClienteResponse::desde).toList());
        return "vehiculos";
    }

    @PostMapping
    public String registrar(@Valid VehiculoRequest solicitud, RedirectAttributes atributos) {
        Aviso.exito(atributos, "Vehículo registrado", vehiculoService.registrar(solicitud).getPlaca());
        return LISTA;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid VehiculoRequest solicitud, RedirectAttributes atributos) {
        Aviso.exito(atributos, "Vehículo actualizado", vehiculoService.actualizar(id, solicitud).getPlaca());
        return LISTA;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes atributos) {
        String placa = vehiculoService.obtener(id).getPlaca();
        vehiculoService.eliminar(id);
        Aviso.exito(atributos, "Vehículo eliminado", placa);
        return LISTA;
    }
}
