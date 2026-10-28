package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.ServicioTallerRequest;
import gt.umg.ultimoarranque.dto.ServicioTallerResponse;
import gt.umg.ultimoarranque.servicio.ServicioTallerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/servicios")
public class ServicioTallerWebController {

    private static final String LISTA = "redirect:/servicios";

    private final ServicioTallerService servicioTallerService;

    public ServicioTallerWebController(ServicioTallerService servicioTallerService) {
        this.servicioTallerService = servicioTallerService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicios", servicioTallerService.listar().stream().map(ServicioTallerResponse::desde).toList());
        return "servicios";
    }

    @PostMapping
    public String registrar(@Valid ServicioTallerRequest solicitud, RedirectAttributes atributos) {
        Aviso.exito(atributos, "Servicio registrado", servicioTallerService.registrar(solicitud).getDescripcion());
        return LISTA;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid ServicioTallerRequest solicitud,
                             RedirectAttributes atributos) {
        Aviso.exito(atributos, "Servicio actualizado", servicioTallerService.actualizar(id, solicitud).getDescripcion());
        return LISTA;
    }
}
