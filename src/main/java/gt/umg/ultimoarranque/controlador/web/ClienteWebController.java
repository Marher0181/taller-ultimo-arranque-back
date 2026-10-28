package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.ClienteRequest;
import gt.umg.ultimoarranque.dto.ClienteResponse;
import gt.umg.ultimoarranque.modelo.documento.TipoDocumento;
import gt.umg.ultimoarranque.servicio.ClienteService;
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
@RequestMapping("/clientes")
public class ClienteWebController {

    private final ClienteService clienteService;

    public ClienteWebController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("clientes", clienteService.listar(buscar).stream().map(ClienteResponse::desde).toList());
        model.addAttribute("buscar", buscar);
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return formulario(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        return formulario(model, ClienteResponse.desde(clienteService.obtener(id)));
    }

    @PostMapping
    public String registrar(@Valid ClienteRequest solicitud, RedirectAttributes atributos) {
        clienteService.registrar(solicitud);
        atributos.addFlashAttribute("mensajeExito", "Cliente registrado");
        return "redirect:/clientes";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid ClienteRequest solicitud, RedirectAttributes atributos) {
        clienteService.actualizar(id, solicitud);
        atributos.addFlashAttribute("mensajeExito", "Cliente actualizado");
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes atributos) {
        clienteService.eliminar(id);
        atributos.addFlashAttribute("mensajeExito", "Cliente eliminado");
        return "redirect:/clientes";
    }

    private String formulario(Model model, ClienteResponse cliente) {
        model.addAttribute("form", cliente);
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        return "clientes/form";
    }
}
