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

    private final ServicioTallerService servicioTallerService;

    public ServicioTallerWebController(ServicioTallerService servicioTallerService) {
        this.servicioTallerService = servicioTallerService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicios", servicioTallerService.listar().stream().map(ServicioTallerResponse::desde).toList());
        return "servicios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", null);
        return "servicios/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        model.addAttribute("form", ServicioTallerResponse.desde(servicioTallerService.obtener(id)));
        return "servicios/form";
    }

    @PostMapping
    public String registrar(@Valid ServicioTallerRequest solicitud, RedirectAttributes atributos) {
        servicioTallerService.registrar(solicitud);
        atributos.addFlashAttribute("mensajeExito", "Servicio registrado");
        return "redirect:/servicios";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid ServicioTallerRequest solicitud,
                             RedirectAttributes atributos) {
        servicioTallerService.actualizar(id, solicitud);
        atributos.addFlashAttribute("mensajeExito", "Servicio actualizado");
        return "redirect:/servicios";
    }
}
