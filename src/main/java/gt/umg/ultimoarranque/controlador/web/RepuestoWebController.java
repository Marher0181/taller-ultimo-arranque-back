package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.AbastecimientoRequest;
import gt.umg.ultimoarranque.dto.RepuestoActualizacionRequest;
import gt.umg.ultimoarranque.dto.RepuestoRequest;
import gt.umg.ultimoarranque.dto.RepuestoResponse;
import gt.umg.ultimoarranque.modelo.NivelInventario;
import gt.umg.ultimoarranque.modelo.Repuesto;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/repuestos")
public class RepuestoWebController {

    private final RepuestoService repuestoService;

    public RepuestoWebController(RepuestoService repuestoService) {
        this.repuestoService = repuestoService;
    }

    /** Pestañas Todos / Agotados / Bajo inventario / Disponibles con su conteo. */
    @GetMapping
    public String listar(@RequestParam(required = false) NivelInventario nivel, Model model) {
        int umbral = repuestoService.umbralBajo();
        List<RepuestoResponse> todos = repuestoService.listar(null).stream()
                .map(repuesto -> RepuestoResponse.desde(repuesto, umbral)).toList();
        Map<String, Long> conteo = new HashMap<>();
        for (NivelInventario n : NivelInventario.values()) {
            conteo.put(n.name(), todos.stream().filter(r -> r.nivelInventario() == n).count());
        }
        model.addAttribute("repuestos", nivel == null ? todos : todos.stream().filter(r -> r.nivelInventario() == nivel).toList());
        model.addAttribute("total", todos.size());
        model.addAttribute("conteo", conteo);
        model.addAttribute("nivel", nivel);
        model.addAttribute("umbral", umbral);
        return "repuestos";
    }

    @PostMapping
    public String registrar(@Valid RepuestoRequest solicitud, RedirectAttributes atributos) {
        Aviso.exito(atributos, "Repuesto registrado", repuestoService.registrar(solicitud).getNombre());
        return "redirect:/repuestos";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid RepuestoActualizacionRequest solicitud,
                             RedirectAttributes atributos) {
        Aviso.exito(atributos, "Repuesto actualizado", repuestoService.actualizar(id, solicitud).getNombre());
        return "redirect:/repuestos";
    }

    @PostMapping("/{id}/abastecer")
    public String abastecer(@PathVariable long id, @Valid AbastecimientoRequest solicitud, RedirectAttributes atributos) {
        Repuesto repuesto = repuestoService.abastecer(id, solicitud.cantidad());
        Aviso.exito(atributos, "Repuesto abastecido",
                repuesto.getNombre() + ": ahora hay " + repuesto.getExistencia() + " unidades.");
        return "redirect:/repuestos";
    }
}
