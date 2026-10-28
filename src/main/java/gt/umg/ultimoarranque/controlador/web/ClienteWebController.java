package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.ClienteRequest;
import gt.umg.ultimoarranque.dto.ClienteResponse;
import gt.umg.ultimoarranque.modelo.Cliente;
import gt.umg.ultimoarranque.servicio.ClienteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteWebController {

    private static final String LISTA = "redirect:/clientes";

    private final ClienteService clienteService;

    public ClienteWebController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listar(null).stream().map(ClienteResponse::desde).toList());
        return "clientes";
    }

    @PostMapping
    public String registrar(@Valid ClienteRequest solicitud, RedirectAttributes atributos) {
        Cliente cliente = clienteService.registrar(solicitud);
        Aviso.exito(atributos, "Cliente registrado", cliente.getNombre());
        return LISTA;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid ClienteRequest solicitud, RedirectAttributes atributos) {
        Cliente cliente = clienteService.actualizar(id, solicitud);
        Aviso.exito(atributos, "Cliente actualizado", cliente.getNombre());
        return LISTA;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes atributos) {
        String nombre = clienteService.obtener(id).getNombre();
        clienteService.eliminar(id);
        Aviso.exito(atributos, "Cliente eliminado", nombre);
        return LISTA;
    }
}
