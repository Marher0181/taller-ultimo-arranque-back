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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehiculos")
public class VehiculoWebController {

    private final VehiculoService vehiculoService;
    private final ClienteService clienteService;

    public VehiculoWebController(VehiculoService vehiculoService, ClienteService clienteService) {
        this.vehiculoService = vehiculoService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String placa, Model model) {
        model.addAttribute("vehiculos", vehiculoService.listar(placa).stream().map(VehiculoResponse::desde).toList());
        model.addAttribute("placa", placa);
        return "vehiculos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long idCliente, Model model) {
        model.addAttribute("idClienteSugerido", idCliente);
        return formulario(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        return formulario(model, VehiculoResponse.desde(vehiculoService.obtener(id)));
    }

    @PostMapping
    public String registrar(@Valid VehiculoRequest solicitud, RedirectAttributes atributos) {
        vehiculoService.registrar(solicitud);
        atributos.addFlashAttribute("mensajeExito", "Vehículo registrado");
        return "redirect:/vehiculos";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid VehiculoRequest solicitud, RedirectAttributes atributos) {
        vehiculoService.actualizar(id, solicitud);
        atributos.addFlashAttribute("mensajeExito", "Vehículo actualizado");
        return "redirect:/vehiculos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes atributos) {
        vehiculoService.eliminar(id);
        atributos.addFlashAttribute("mensajeExito", "Vehículo eliminado");
        return "redirect:/vehiculos";
    }

    private String formulario(Model model, VehiculoResponse vehiculo) {
        model.addAttribute("form", vehiculo);
        model.addAttribute("clientes", clienteService.listar(null).stream().map(ClienteResponse::desde).toList());
        return "vehiculos/form";
    }
}
