package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.controlador.RespuestaPdf;
import gt.umg.ultimoarranque.dto.MecanicoResponse;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import gt.umg.ultimoarranque.servicio.MecanicoService;
import gt.umg.ultimoarranque.servicio.ReporteService;
import gt.umg.ultimoarranque.vista.pdf.HistorialVehiculoPdf;
import gt.umg.ultimoarranque.vista.pdf.IngresosPdf;
import gt.umg.ultimoarranque.vista.pdf.OrdenesPorMecanicoPdf;
import gt.umg.ultimoarranque.vista.pdf.ReporteOrdenesPorEstadoPdf;
import gt.umg.ultimoarranque.vista.pdf.RepuestosBajoInventarioPdf;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/** Página de reportes: cada formulario abre el PDF correspondiente (los mismos que expone la API). */
@Controller
@RequestMapping("/reportes")
public class ReporteWebController {

    private final ReporteService reporteService;
    private final MecanicoService mecanicoService;

    public ReporteWebController(ReporteService reporteService, MecanicoService mecanicoService) {
        this.reporteService = reporteService;
        this.mecanicoService = mecanicoService;
    }

    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("estados", EstadoOrden.values());
        model.addAttribute("mecanicos", mecanicoService.listar().stream().map(MecanicoResponse::desde).toList());
        model.addAttribute("hoy", LocalDate.now());
        return "reportes/inicio";
    }

    @GetMapping("/ordenes-por-estado/pdf")
    public ResponseEntity<byte[]> ordenesPorEstado(@RequestParam(required = false) EstadoOrden estado) {
        return RespuestaPdf.de(new ReporteOrdenesPorEstadoPdf(reporteService.ordenesPorEstado(estado)));
    }

    @GetMapping("/historial-vehiculo/pdf")
    public ResponseEntity<byte[]> historialVehiculo(@RequestParam String placa) {
        return RespuestaPdf.de(new HistorialVehiculoPdf(reporteService.historialVehiculo(placa)));
    }

    @GetMapping("/repuestos-bajo-inventario/pdf")
    public ResponseEntity<byte[]> repuestosBajoInventario() {
        return RespuestaPdf.de(new RepuestosBajoInventarioPdf(reporteService.repuestosBajoInventario()));
    }

    @GetMapping("/ordenes-por-mecanico/pdf")
    public ResponseEntity<byte[]> ordenesPorMecanico(@RequestParam(required = false) Long idMecanico,
                                                     @RequestParam(required = false) EstadoOrden estado) {
        return RespuestaPdf.de(new OrdenesPorMecanicoPdf(reporteService.ordenesPorMecanico(idMecanico, estado), estado));
    }

    @GetMapping("/ingresos/pdf")
    public ResponseEntity<byte[]> ingresos(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return RespuestaPdf.de(new IngresosPdf(reporteService.ingresos(desde, hasta)));
    }
}
