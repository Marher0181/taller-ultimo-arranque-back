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
import gt.umg.ultimoarranque.excepcion.AccesoDenegadoException;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.modelo.consulta.OrdenResumen;
import gt.umg.ultimoarranque.modelo.orden.DetalleRepuesto;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import gt.umg.ultimoarranque.modelo.orden.OrdenTrabajo;
import gt.umg.ultimoarranque.seguridad.SesionUsuario;
import gt.umg.ultimoarranque.seguridad.UsuarioAutenticado;
import gt.umg.ultimoarranque.servicio.MecanicoService;
import gt.umg.ultimoarranque.servicio.OrdenTrabajoService;
import gt.umg.ultimoarranque.servicio.RepuestoService;
import gt.umg.ultimoarranque.servicio.ServicioTallerService;
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ordenes")
public class OrdenTrabajoWebController {

    private final OrdenTrabajoService ordenService;
    private final MecanicoService mecanicoService;
    private final ServicioTallerService servicioTallerService;
    private final RepuestoService repuestoService;
    private final NuevaOrdenCatalogos nuevaOrden;
    private final FormatoVista vista;
    private final SesionUsuario sesion;

    public OrdenTrabajoWebController(OrdenTrabajoService ordenService, MecanicoService mecanicoService,
                                     ServicioTallerService servicioTallerService, RepuestoService repuestoService,
                                     NuevaOrdenCatalogos nuevaOrden, FormatoVista vista, SesionUsuario sesion) {
        this.ordenService = ordenService;
        this.mecanicoService = mecanicoService;
        this.servicioTallerService = servicioTallerService;
        this.repuestoService = repuestoService;
        this.nuevaOrden = nuevaOrden;
        this.vista = vista;
        this.sesion = sesion;
    }

    /** Paso del indicador de progreso (Recibida → En reparación → Finalizada). */
    public record Paso(String texto, String clase, String marca) {
    }

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoOrden estado,
                         @RequestParam(required = false) String buscar, Model model) {
        List<OrdenResumen> todas = ordenService.listar(null);
        Map<EstadoOrden, Long> conteo = new EnumMap<>(EstadoOrden.class);
        for (EstadoOrden e : EstadoOrden.values()) {
            conteo.put(e, todas.stream().filter(o -> o.estado() == e).count());
        }
        model.addAttribute("ordenes", estado == null ? todas : todas.stream().filter(o -> o.estado() == estado).toList());
        model.addAttribute("total", todas.size());
        model.addAttribute("conteo", conteo);
        model.addAttribute("estado", estado);
        model.addAttribute("estados", EstadoOrden.values());
        model.addAttribute("buscar", buscar);
        if (vista.puede(sesion.actual(), "ordenes.crear")) {
            nuevaOrden.agregar(model);
        }
        return "ordenes";
    }

    @PostMapping
    public String crear(@Valid OrdenCreacionRequest solicitud, RedirectAttributes atributos) {
        OrdenTrabajo orden = ordenService.crear(solicitud);
        Aviso.exito(atributos, "Orden " + vista.orden(orden.getId()) + " creada",
                "Queda pendiente hasta que el mecánico inicie la reparación.");
        return "redirect:/ordenes/" + orden.getId();
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable long id, Model model) {
        model.addAttribute("id", id);
        OrdenTrabajo orden;
        try {
            orden = ordenService.obtener(id);
        } catch (RecursoNoEncontradoException ex) {
            model.addAttribute("error", "Esta orden no existe.");
            return "orden-detalle";
        } catch (AccesoDenegadoException ex) {
            model.addAttribute("error", "Esta orden no está asignada a su usuario.");
            return "orden-detalle";
        }
        UsuarioAutenticado usuario = sesion.actual();
        OrdenResponse respuesta = OrdenResponse.desde(orden, ordenService.costosDe(orden));
        boolean activa = orden.admiteModificaciones();
        model.addAttribute("orden", respuesta);
        model.addAttribute("activa", activa);
        model.addAttribute("pasos", pasos(respuesta));
        model.addAttribute("unidadesRepuestos", orden.getRepuestos().stream().mapToInt(DetalleRepuesto::getCantidad).sum());
        model.addAttribute("puedeServicios", activa && vista.puede(usuario, "ordenes.servicios"));
        model.addAttribute("puedeRepuestos", orden.getEstado() == EstadoOrden.EN_PROCESO && vista.puede(usuario, "ordenes.trabajar"));
        if (activa && vista.puede(usuario, "ordenes.servicios")) {
            Set<Long> usados = respuesta.servicios().stream().map(s -> s.idServicio()).collect(Collectors.toSet());
            model.addAttribute("serviciosDisponibles", servicioTallerService.listar().stream()
                    .map(ServicioTallerResponse::desde).filter(s -> !usados.contains(s.id())).toList());
        }
        if (orden.getEstado() == EstadoOrden.EN_PROCESO && vista.puede(usuario, "ordenes.trabajar")) {
            int umbral = repuestoService.umbralBajo();
            model.addAttribute("catalogoRepuestos", repuestoService.listar(null).stream()
                    .map(repuesto -> RepuestoResponse.desde(repuesto, umbral)).toList());
        }
        if (activa && vista.puede(usuario, "ordenes.editar")) {
            model.addAttribute("mecanicos", mecanicoService.listar().stream().map(MecanicoResponse::desde).toList());
        }
        return "orden-detalle";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id, @Valid OrdenActualizacionRequest solicitud,
                             RedirectAttributes atributos) {
        ordenService.actualizar(id, solicitud);
        return volver(id, atributos, "Orden actualizada", null);
    }

    @PostMapping("/{id}/servicios")
    public String agregarServicio(@PathVariable long id, @Valid ServicioOrdenRequest solicitud,
                                  RedirectAttributes atributos) {
        ordenService.agregarServicio(id, solicitud.idServicio());
        return volver(id, atributos, "Servicio agregado", null);
    }

    @PostMapping("/{id}/servicios/{idServicio}/quitar")
    public String quitarServicio(@PathVariable long id, @PathVariable long idServicio, RedirectAttributes atributos) {
        ordenService.quitarServicio(id, idServicio);
        return volver(id, atributos, "Servicio quitado de la orden", null);
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable long id, RedirectAttributes atributos) {
        ordenService.iniciar(id);
        return volver(id, atributos, "Reparación iniciada", "Ya puede asignar repuestos a esta orden.");
    }

    @PostMapping("/{id}/repuestos")
    public String asignarRepuesto(@PathVariable long id, @Valid RepuestoOrdenRequest solicitud,
                                  RedirectAttributes atributos) {
        ordenService.asignarRepuesto(id, solicitud.idRepuesto(), solicitud.cantidad());
        int cantidad = solicitud.cantidad();
        return volver(id, atributos, "Repuesto asignado",
                cantidad + (cantidad == 1 ? " unidad descontada" : " unidades descontadas") + " del inventario.");
    }

    @PostMapping("/{id}/repuestos/{idDetalle}/retirar")
    public String retirarRepuesto(@PathVariable long id, @PathVariable long idDetalle, RedirectAttributes atributos) {
        int cantidad = ordenService.obtener(id).obtenerDetalleRepuesto(idDetalle).getCantidad();
        ordenService.retirarRepuesto(id, idDetalle);
        return volver(id, atributos, "Repuesto retirado",
                cantidad + (cantidad == 1 ? " unidad regresó" : " unidades regresaron") + " al inventario.");
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable long id, RedirectAttributes atributos) {
        ordenService.finalizar(id);
        return volver(id, atributos, "Orden finalizada", "El comprobante quedó emitido.");
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable long id, @Valid CancelacionRequest solicitud, RedirectAttributes atributos) {
        ordenService.cancelar(id, solicitud.motivo());
        return volver(id, atributos, "Orden cancelada", "Los repuestos asignados regresaron al inventario.");
    }

    @GetMapping("/{id}/comprobante/pdf")
    public ResponseEntity<byte[]> comprobantePdf(@PathVariable long id) {
        OrdenTrabajo orden = ordenService.obtener(id);
        return RespuestaPdf.de(new ComprobantePdf(orden, ordenService.comprobanteDe(orden)));
    }

    private static List<Paso> pasos(OrdenResponse orden) {
        if (orden.estado() == EstadoOrden.CANCELADA) {
            List<Paso> pasos = new ArrayList<>();
            pasos.add(new Paso("Recibida", "hecho", ""));
            if (orden.fechaInicio() != null) {
                pasos.add(new Paso("En reparación", "hecho", ""));
            }
            pasos.add(new Paso("Cancelada", "cancelado", ""));
            return pasos;
        }
        List<EstadoOrden> orden_ = List.of(EstadoOrden.PENDIENTE, EstadoOrden.EN_PROCESO, EstadoOrden.FINALIZADA);
        List<String> textos = List.of("Recibida", "En reparación", "Finalizada");
        int indice = orden_.indexOf(orden.estado());
        List<Paso> pasos = new ArrayList<>();
        for (int i = 0; i < textos.size(); i++) {
            boolean hecho = i < indice || orden.estado() == EstadoOrden.FINALIZADA;
            pasos.add(new Paso(textos.get(i), hecho ? "hecho" : i == indice ? "actual" : "", hecho ? "" : String.valueOf(i + 1)));
        }
        return pasos;
    }

    private static String volver(long id, RedirectAttributes atributos, String titulo, String detalle) {
        Aviso.exito(atributos, titulo, detalle);
        return "redirect:/ordenes/" + id;
    }
}
