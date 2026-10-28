package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.MecanicoRequest;
import gt.umg.ultimoarranque.dto.MecanicoResponse;
import gt.umg.ultimoarranque.servicio.MecanicoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mecanicos")
public class MecanicoWebController {

    private final MecanicoService mecanicoService;

    public MecanicoWebController(MecanicoService mecanicoService) {
        this.mecanicoService = mecanicoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("mecanicos", mecanicoService.listar().stream().map(MecanicoResponse::desde).toList());
        return "mecanicos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", null);
        return "mecanicos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        model.addAttribute("form", MecanicoResponse.desde(mecanicoService.obtener(id)));
        return "mecanicos/form";
    }

    @PostMapping
    public String registrar(@Valid MecanicoRequest solicitud, RedirectAttributes atributos) {
        mecanicoService.registrar(solicitud);
        atributos.addFlashAttribute("mensajeExito", "Mecánico registrado");
        return "redirect:/mecanicos";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid MecanicoRequest solicitud, RedirectAttributes atributos) {
        mecanicoService.actualizar(id, solicitud);
        atributos.addFlashAttribute("mensajeExito", "Mecánico actualizado");
        return "redirect:/mecanicos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes atributos) {
        mecanicoService.eliminar(id);
        atributos.addFlashAttribute("mensajeExito", "Mecánico eliminado");
        return "redirect:/mecanicos";
    }
}
