package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.controlador.RespuestaPdf;
import gt.umg.ultimoarranque.dto.CancelacionRequest;
import gt.umg.ultimoarranque.dto.MecanicoResponse;
import gt.umg.ultimoarranque.dto.OrdenActualizacionRequest;
import gt.umg.ultimoarranque.dto.OrdenCreacionRequest;
import gt.umg.ultimoarranque.dto.OrdenResponse;
import gt.umg.ultimoarranque.dto.RepuestoOrdenRequest;
import gt.umg.ultimoarranque.dto.RepuestoResponse;
import gt.umg.ultimoarranque.dto.ServicioOrdenRequest;
import gt.umg.ultimoarranque.dto.ServicioTallerResponse;
import gt.umg.ultimoarranque.dto.VehiculoResponse;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import gt.umg.ultimoarranque.modelo.orden.OrdenTrabajo;
import gt.umg.ultimoarranque.seguridad.SesionUsuario;
import gt.umg.ultimoarranque.servicio.MecanicoService;
import gt.umg.ultimoarranque.servicio.OrdenTrabajoService;
import gt.umg.ultimoarranque.servicio.RepuestoService;
import gt.umg.ultimoarranque.servicio.ServicioTallerService;
import gt.umg.ultimoarranque.servicio.VehiculoService;
import gt.umg.ultimoarranque.vista.pdf.ComprobantePdf;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ordenes")
public class OrdenTrabajoWebController {

    private final OrdenTrabajoService ordenService;
    private final VehiculoService vehiculoService;
    private final MecanicoService mecanicoService;
    private final ServicioTallerService servicioTallerService;
    private final RepuestoService repuestoService;
    private final SesionUsuario sesion;

    public OrdenTrabajoWebController(OrdenTrabajoService ordenService, VehiculoService vehiculoService,
                                     MecanicoService mecanicoService, ServicioTallerService servicioTallerService,
                                     RepuestoService repuestoService, SesionUsuario sesion) {
        this.ordenService = ordenService;
        this.vehiculoService = vehiculoService;
        this.mecanicoService = mecanicoService;
        this.servicioTallerService = servicioTallerService;
        this.repuestoService = repuestoService;
        this.sesion = sesion;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoOrden estado, Model model) {
        model.addAttribute("ordenes", ordenService.listar(estado));
        model.addAttribute("estado", estado);
        model.addAttribute("estados", EstadoOrden.values());
        return "ordenes/lista";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long idVehiculo, Model model) {
        model.addAttribute("idVehiculoSugerido", idVehiculo);
        model.addAttribute("vehiculos", vehiculoService.listar(null).stream().map(VehiculoResponse::desde).toList());
        model.addAttribute("mecanicos", mecanicoService.listar().stream().map(MecanicoResponse::desde).toList());
        model.addAttribute("servicios", catalogoServicios());
        return "ordenes/nueva";
    }

    @PostMapping
    public String crear(@Valid OrdenCreacionRequest solicitud, RedirectAttributes atributos) {
        OrdenTrabajo orden = ordenService.crear(solicitud);
        atributos.addFlashAttribute("mensajeExito", "Orden #" + orden.getId() + " creada");
        return "redirect:/ordenes/" + orden.getId();
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable long id, Model model) {
        OrdenTrabajo orden = ordenService.obtener(id);
        model.addAttribute("orden", OrdenResponse.desde(orden, ordenService.costosDe(orden)));
        model.addAttribute("servicios", catalogoServicios());
        int umbral = repuestoService.umbralBajo();
        model.addAttribute("repuestos", repuestoService.listar(null).stream()
                .map(repuesto -> RepuestoResponse.desde(repuesto, umbral)).toList());
        if (!sesion.actual().esMecanico()) {
            model.addAttribute("mecanicos", mecanicoService.listar().stream().map(MecanicoResponse::desde).toList());
        }
        return "ordenes/detalle";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid OrdenActualizacionRequest solicitud,
                             RedirectAttributes atributos) {
        ordenService.actualizar(id, solicitud);
        return volver(id, atributos, "Orden actualizada");
    }

    @PostMapping("/{id}/servicios")
    public String agregarServicio(@PathVariable long id, @Valid ServicioOrdenRequest solicitud,
                                  RedirectAttributes atributos) {
        ordenService.agregarServicio(id, solicitud.idServicio());
        return volver(id, atributos, "Servicio agregado");
    }

    @PostMapping("/{id}/servicios/{idServicio}/quitar")
    public String quitarServicio(@PathVariable long id, @PathVariable long idServicio, RedirectAttributes atributos) {
        ordenService.quitarServicio(id, idServicio);
        return volver(id, atributos, "Servicio quitado");
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable long id, RedirectAttributes atributos) {
        ordenService.iniciar(id);
        return volver(id, atributos, "Orden iniciada");
    }

    @PostMapping("/{id}/repuestos")
    public String asignarRepuesto(@PathVariable long id, @Valid RepuestoOrdenRequest solicitud,
                                  RedirectAttributes atributos) {
        ordenService.asignarRepuesto(id, solicitud.idRepuesto(), solicitud.cantidad());
        return volver(id, atributos, "Repuesto asignado");
    }

    @PostMapping("/{id}/repuestos/{idDetalle}/retirar")
    public String retirarRepuesto(@PathVariable long id, @PathVariable long idDetalle, RedirectAttributes atributos) {
        ordenService.retirarRepuesto(id, idDetalle);
        return volver(id, atributos, "Repuesto retirado");
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable long id, RedirectAttributes atributos) {
        ordenService.finalizar(id);
        return volver(id, atributos, "Orden finalizada; comprobante emitido");
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable long id, @Valid CancelacionRequest solicitud, RedirectAttributes atributos) {
        ordenService.cancelar(id, solicitud.motivo());
        return volver(id, atributos, "Orden cancelada");
    }

    @GetMapping("/{id}/comprobante/pdf")
    public ResponseEntity<byte[]> comprobantePdf(@PathVariable long id) {
        OrdenTrabajo orden = ordenService.obtener(id);
        return RespuestaPdf.de(new ComprobantePdf(orden, ordenService.comprobanteDe(orden)));
    }

    private List<ServicioTallerResponse> catalogoServicios() {
        return servicioTallerService.listar().stream().map(ServicioTallerResponse::desde).toList();
    }

    private static String volver(long id, RedirectAttributes atributos, String mensaje) {
        atributos.addFlashAttribute("mensajeExito", mensaje);
        return "redirect:/ordenes/" + id;
    }
}
