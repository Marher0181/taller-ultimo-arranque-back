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

    private static final String LISTA = "redirect:/mecanicos";

    private final MecanicoService mecanicoService;

    public MecanicoWebController(MecanicoService mecanicoService) {
        this.mecanicoService = mecanicoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("mecanicos", mecanicoService.listar().stream().map(MecanicoResponse::desde).toList());
        return "mecanicos";
    }

    @PostMapping
    public String registrar(@Valid MecanicoRequest solicitud, RedirectAttributes atributos) {
        Aviso.exito(atributos, "Mecánico registrado", mecanicoService.registrar(solicitud).getNombre());
        return LISTA;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid MecanicoRequest solicitud, RedirectAttributes atributos) {
        Aviso.exito(atributos, "Mecánico actualizado", mecanicoService.actualizar(id, solicitud).getNombre());
        return LISTA;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes atributos) {
        String nombre = mecanicoService.obtener(id).getNombre();
        mecanicoService.eliminar(id);
        Aviso.exito(atributos, "Mecánico eliminado", nombre);
        return LISTA;
    }
}
