package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.modelo.consulta.HistorialVehiculo;
import gt.umg.ultimoarranque.modelo.consulta.OrdenesPorMecanico;
import gt.umg.ultimoarranque.modelo.consulta.ReporteIngresos;
import gt.umg.ultimoarranque.modelo.consulta.ReporteInventarioBajo;
import gt.umg.ultimoarranque.modelo.consulta.ReporteOrdenesPorEstado;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import gt.umg.ultimoarranque.servicio.ReporteService;
import gt.umg.ultimoarranque.vista.pdf.HistorialVehiculoPdf;
import gt.umg.ultimoarranque.vista.pdf.IngresosPdf;
import gt.umg.ultimoarranque.vista.pdf.OrdenesPorMecanicoPdf;
import gt.umg.ultimoarranque.vista.pdf.ReporteOrdenesPorEstadoPdf;
import gt.umg.ultimoarranque.vista.pdf.RepuestosBajoInventarioPdf;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/ordenes-por-estado")
    public ReporteOrdenesPorEstado ordenesPorEstado(@RequestParam(required = false) EstadoOrden estado) {
        return reporteService.ordenesPorEstado(estado);
    }

    @GetMapping("/ordenes-por-estado/pdf")
    public ResponseEntity<byte[]> ordenesPorEstadoPdf(@RequestParam(required = false) EstadoOrden estado) {
        return RespuestaPdf.de(new ReporteOrdenesPorEstadoPdf(reporteService.ordenesPorEstado(estado)));
    }

    @GetMapping("/historial-vehiculo/{placa}")
    public HistorialVehiculo historialVehiculo(@PathVariable String placa) {
        return reporteService.historialVehiculo(placa);
    }

    @GetMapping("/historial-vehiculo/{placa}/pdf")
    public ResponseEntity<byte[]> historialVehiculoPdf(@PathVariable String placa) {
        return RespuestaPdf.de(new HistorialVehiculoPdf(reporteService.historialVehiculo(placa)));
    }

    @GetMapping("/repuestos-bajo-inventario")
    public ReporteInventarioBajo repuestosBajoInventario() {
        return reporteService.repuestosBajoInventario();
    }

    @GetMapping("/repuestos-bajo-inventario/pdf")
    public ResponseEntity<byte[]> repuestosBajoInventarioPdf() {
        return RespuestaPdf.de(new RepuestosBajoInventarioPdf(reporteService.repuestosBajoInventario()));
    }

    @GetMapping("/ordenes-por-mecanico")
    public List<OrdenesPorMecanico> ordenesPorMecanico(@RequestParam(required = false) Long idMecanico,
                                                       @RequestParam(required = false) EstadoOrden estado) {
        return reporteService.ordenesPorMecanico(idMecanico, estado);
    }

    @GetMapping("/ordenes-por-mecanico/pdf")
    public ResponseEntity<byte[]> ordenesPorMecanicoPdf(@RequestParam(required = false) Long idMecanico,
                                                        @RequestParam(required = false) EstadoOrden estado) {
        return RespuestaPdf.de(new OrdenesPorMecanicoPdf(reporteService.ordenesPorMecanico(idMecanico, estado), estado));
    }

    @GetMapping("/ingresos")
    public ReporteIngresos ingresos(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.ingresos(desde, hasta);
    }

    @GetMapping("/ingresos/pdf")
    public ResponseEntity<byte[]> ingresosPdf(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return RespuestaPdf.de(new IngresosPdf(reporteService.ingresos(desde, hasta)));
    }
}
