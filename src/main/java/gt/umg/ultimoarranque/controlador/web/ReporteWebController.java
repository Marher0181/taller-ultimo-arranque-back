package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.controlador.RespuestaPdf;
import gt.umg.ultimoarranque.dto.MecanicoResponse;
import gt.umg.ultimoarranque.excepcion.TallerException;
import gt.umg.ultimoarranque.modelo.consulta.IngresoOrden;
import gt.umg.ultimoarranque.modelo.consulta.ReporteIngresos;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Pantalla de reportes con pestañas: cada una muestra el reporte y permite descargarlo en PDF. */
@Controller
@RequestMapping("/reportes")
public class ReporteWebController {

    private static final String PLACA_EJEMPLO = "P123ABC";

    private final ReporteService reporteService;
    private final MecanicoService mecanicoService;
    private final FormatoVista vista;

    public ReporteWebController(ReporteService reporteService, MecanicoService mecanicoService, FormatoVista vista) {
        this.reporteService = reporteService;
        this.mecanicoService = mecanicoService;
        this.vista = vista;
    }

    public record Pestana(String clave, String texto) {
    }

    @GetMapping
    public String reportes(@RequestParam(defaultValue = "estado") String pestana,
                           @RequestParam(required = false) EstadoOrden estado,
                           @RequestParam(required = false) String placa,
                           @RequestParam(required = false) Long idMecanico,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                           Model model) {
        String placaNormalizada = (placa == null ? PLACA_EJEMPLO : placa).replaceAll("[\\s-]", "").toUpperCase();
        LocalDate inicio = desde != null ? desde : LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        model.addAttribute("pestanas", List.of(
                new Pestana("estado", "Órdenes por estado"),
                new Pestana("historial", "Historial por placa"),
                new Pestana("inventario", "Repuestos con 5 o menos"),
                new Pestana("mecanico", "Órdenes por mecánico"),
                new Pestana("ingresos", "Ingresos")));
        model.addAttribute("pestana", pestana);
        model.addAttribute("estados", EstadoOrden.values());
        model.addAttribute("estado", estado);
        model.addAttribute("placa", placaNormalizada);
        model.addAttribute("idMecanico", idMecanico);
        model.addAttribute("desde", inicio);
        model.addAttribute("hasta", fin);
        UriComponentsBuilder pdf = UriComponentsBuilder.fromPath("/reportes");
        try {
            switch (pestana) {
                case "historial" -> {
                    pdf.path("/historial-vehiculo/pdf").queryParam("placa", placaNormalizada);
                    model.addAttribute("historial", reporteService.historialVehiculo(placaNormalizada));
                }
                case "inventario" -> {
                    pdf.path("/repuestos-bajo-inventario/pdf");
                    model.addAttribute("inventario", reporteService.repuestosBajoInventario());
                }
                case "mecanico" -> {
                    pdf.path("/ordenes-por-mecanico/pdf").queryParamIfPresent("idMecanico", Optional.ofNullable(idMecanico))
                            .queryParamIfPresent("estado", Optional.ofNullable(estado));
                    model.addAttribute("mecanicos", mecanicoService.listar().stream().map(MecanicoResponse::desde).toList());
                    model.addAttribute("porMecanico", reporteService.ordenesPorMecanico(idMecanico, estado));
                }
                case "ingresos" -> {
                    pdf.path("/ingresos/pdf").queryParam("desde", inicio).queryParam("hasta", fin);
                    ReporteIngresos ingresos = reporteService.ingresos(inicio, fin);
                    model.addAttribute("ingresos", ingresos);
                    agregarGrafico(model, ingresos);
                }
                default -> {
                    model.addAttribute("pestana", "estado");
                    pdf.path("/ordenes-por-estado/pdf").queryParamIfPresent("estado", Optional.ofNullable(estado));
                    var reporte = reporteService.ordenesPorEstado(estado);
                    model.addAttribute("porEstado", reporte);
                    model.addAttribute("totalOrdenes", reporte.conteoPorEstado().values().stream().mapToLong(Long::longValue).sum());
                }
            }
        } catch (TallerException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        model.addAttribute("urlPdf", pdf.build().encode().toUriString());
        return "reportes";
    }

    /** Facturado por semana (lunes a domingo) entre las fechas del reporte. */
    private void agregarGrafico(Model model, ReporteIngresos reporte) {
        Map<LocalDate, BigDecimal> semanas = new LinkedHashMap<>();
        for (LocalDate cursor = lunesDe(reporte.desde()); !cursor.isAfter(reporte.hasta()); cursor = cursor.plusWeeks(1)) {
            semanas.put(cursor, BigDecimal.ZERO);
        }
        for (IngresoOrden ingreso : reporte.detalle()) {
            semanas.merge(lunesDe(ingreso.fechaEmision().toLocalDate()), ingreso.total(), BigDecimal::add);
        }
        List<GraficoBarras.Entrada> entradas = semanas.entrySet().stream()
                .map(s -> new GraficoBarras.Entrada(vista.diaCorto(s.getKey()), s.getValue(),
                        "Semana del " + vista.diaCorto(s.getKey()) + ": " + vista.moneda(s.getValue())))
                .toList();
        model.addAttribute("grafico", GraficoBarras.de(entradas));
        entradas.stream().reduce((mejor, e) -> e.valor().compareTo(mejor.valor()) > 0 ? e : mejor)
                .ifPresent(mejor -> model.addAttribute("mejorSemana", mejor));
        model.addAttribute("rango", "del " + vista.fechaLarga(reporte.desde()) + " al " + vista.fechaLarga(reporte.hasta()));
    }

    private static LocalDate lunesDe(LocalDate dia) {
        return dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
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
