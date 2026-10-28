package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.dto.CancelacionRequest;
import gt.umg.ultimoarranque.dto.ComprobanteResponse;
import gt.umg.ultimoarranque.dto.OrdenActualizacionRequest;
import gt.umg.ultimoarranque.dto.OrdenCreacionRequest;
import gt.umg.ultimoarranque.dto.OrdenResponse;
import gt.umg.ultimoarranque.dto.RepuestoOrdenRequest;
import gt.umg.ultimoarranque.dto.ServicioOrdenRequest;
import gt.umg.ultimoarranque.modelo.consulta.OrdenResumen;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import gt.umg.ultimoarranque.modelo.orden.OrdenTrabajo;
import gt.umg.ultimoarranque.servicio.OrdenTrabajoService;
import gt.umg.ultimoarranque.vista.pdf.ComprobantePdf;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenTrabajoController {

    private final OrdenTrabajoService ordenService;

    public OrdenTrabajoController(OrdenTrabajoService ordenService) {
        this.ordenService = ordenService;
    }

    @GetMapping
    public List<OrdenResumen> listar(@RequestParam(required = false) EstadoOrden estado) {
        return ordenService.listar(estado);
    }

    @GetMapping("/{id}")
    public OrdenResponse obtener(@PathVariable long id) {
        return responder(ordenService.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenResponse crear(@Valid @RequestBody OrdenCreacionRequest solicitud) {
        return responder(ordenService.crear(solicitud));
    }

    @PutMapping("/{id}")
    public OrdenResponse actualizar(@PathVariable long id, @Valid @RequestBody OrdenActualizacionRequest solicitud) {
        return responder(ordenService.actualizar(id, solicitud));
    }

    @PostMapping("/{id}/servicios")
    public OrdenResponse agregarServicio(@PathVariable long id, @Valid @RequestBody ServicioOrdenRequest solicitud) {
        return responder(ordenService.agregarServicio(id, solicitud.idServicio()));
    }

    @DeleteMapping("/{id}/servicios/{idServicio}")
    public OrdenResponse quitarServicio(@PathVariable long id, @PathVariable long idServicio) {
        return responder(ordenService.quitarServicio(id, idServicio));
    }

    @PostMapping("/{id}/iniciar")
    public OrdenResponse iniciar(@PathVariable long id) {
        return responder(ordenService.iniciar(id));
    }

    @PostMapping("/{id}/repuestos")
    public OrdenResponse asignarRepuesto(@PathVariable long id, @Valid @RequestBody RepuestoOrdenRequest solicitud) {
        return responder(ordenService.asignarRepuesto(id, solicitud.idRepuesto(), solicitud.cantidad()));
    }

    @DeleteMapping("/{id}/repuestos/{idDetalle}")
    public OrdenResponse retirarRepuesto(@PathVariable long id, @PathVariable long idDetalle) {
        return responder(ordenService.retirarRepuesto(id, idDetalle));
    }

    @PostMapping("/{id}/finalizar")
    public OrdenResponse finalizar(@PathVariable long id) {
        return responder(ordenService.finalizar(id));
    }

    @PostMapping("/{id}/cancelar")
    public OrdenResponse cancelar(@PathVariable long id, @Valid @RequestBody(required = false) CancelacionRequest solicitud) {
        return responder(ordenService.cancelar(id, solicitud == null ? null : solicitud.motivo()));
    }

    @GetMapping("/{id}/comprobante")
    public ComprobanteResponse obtenerComprobante(@PathVariable long id) {
        return ComprobanteResponse.desde(ordenService.obtenerComprobante(id));
    }

    @GetMapping("/{id}/comprobante/pdf")
    public ResponseEntity<byte[]> descargarComprobante(@PathVariable long id) {
        OrdenTrabajo orden = ordenService.obtener(id);
        return RespuestaPdf.de(new ComprobantePdf(orden, ordenService.comprobanteDe(orden)));
    }

    private OrdenResponse responder(OrdenTrabajo orden) {
        return OrdenResponse.desde(orden, ordenService.costosDe(orden));
    }
}
