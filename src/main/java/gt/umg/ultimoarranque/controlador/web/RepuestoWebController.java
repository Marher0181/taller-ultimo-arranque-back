package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.AbastecimientoRequest;
import gt.umg.ultimoarranque.dto.RepuestoActualizacionRequest;
import gt.umg.ultimoarranque.dto.RepuestoRequest;
import gt.umg.ultimoarranque.dto.RepuestoResponse;
import gt.umg.ultimoarranque.modelo.NivelInventario;
import gt.umg.ultimoarranque.servicio.RepuestoService;
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
@RequestMapping("/repuestos")
public class RepuestoWebController {

    private final RepuestoService repuestoService;

    public RepuestoWebController(RepuestoService repuestoService) {
        this.repuestoService = repuestoService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) NivelInventario nivel, Model model) {
        int umbral = repuestoService.umbralBajo();
        model.addAttribute("repuestos", repuestoService.listar(nivel).stream()
                .map(repuesto -> RepuestoResponse.desde(repuesto, umbral)).toList());
        model.addAttribute("nivel", nivel);
        model.addAttribute("niveles", NivelInventario.values());
        return "repuestos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", null);
        return "repuestos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        model.addAttribute("form", RepuestoResponse.desde(repuestoService.obtener(id), repuestoService.umbralBajo()));
        return "repuestos/form";
    }

    @PostMapping
    public String registrar(@Valid RepuestoRequest solicitud, RedirectAttributes atributos) {
        repuestoService.registrar(solicitud);
        atributos.addFlashAttribute("mensajeExito", "Repuesto registrado");
        return "redirect:/repuestos";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid RepuestoActualizacionRequest solicitud,
                             RedirectAttributes atributos) {
        repuestoService.actualizar(id, solicitud);
        atributos.addFlashAttribute("mensajeExito", "Repuesto actualizado");
        return "redirect:/repuestos";
    }

    @PostMapping("/{id}/abastecer")
    public String abastecer(@PathVariable long id, @Valid AbastecimientoRequest solicitud, RedirectAttributes atributos) {
        repuestoService.abastecer(id, solicitud.cantidad());
        atributos.addFlashAttribute("mensajeExito", "Existencia actualizada");
        return "redirect:/repuestos";
    }
}
