package gt.umg.ultimoarranque.servicio;

import gt.umg.ultimoarranque.config.TallerProperties;
import gt.umg.ultimoarranque.dao.MecanicoDao;
import gt.umg.ultimoarranque.dao.OrdenConsultaDao;
import gt.umg.ultimoarranque.dao.RepuestoDao;
import gt.umg.ultimoarranque.dao.VehiculoDao;
import gt.umg.ultimoarranque.dao.jdbc.GestorTransacciones;
import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.modelo.Mecanico;
import gt.umg.ultimoarranque.modelo.NivelInventario;
import gt.umg.ultimoarranque.modelo.Vehiculo;
import gt.umg.ultimoarranque.modelo.consulta.FiltroOrdenes;
import gt.umg.ultimoarranque.modelo.consulta.HistorialVehiculo;
import gt.umg.ultimoarranque.modelo.consulta.OrdenResumen;
import gt.umg.ultimoarranque.modelo.consulta.OrdenesPorMecanico;
import gt.umg.ultimoarranque.modelo.consulta.ReporteIngresos;
import gt.umg.ultimoarranque.modelo.consulta.ReporteInventarioBajo;
import gt.umg.ultimoarranque.modelo.consulta.ReporteOrdenesPorEstado;
import gt.umg.ultimoarranque.modelo.consulta.RepuestoInventario;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final GestorTransacciones transacciones;
    private final OrdenConsultaDao ordenConsultaDao;
    private final VehiculoDao vehiculoDao;
    private final MecanicoDao mecanicoDao;
    private final RepuestoDao repuestoDao;
    private final int umbralBajo;

    public ReporteService(GestorTransacciones transacciones, OrdenConsultaDao ordenConsultaDao, VehiculoDao vehiculoDao,
                          MecanicoDao mecanicoDao, RepuestoDao repuestoDao, TallerProperties propiedades) {
        this.transacciones = transacciones;
        this.ordenConsultaDao = ordenConsultaDao;
        this.vehiculoDao = vehiculoDao;
        this.mecanicoDao = mecanicoDao;
        this.repuestoDao = repuestoDao;
        this.umbralBajo = propiedades.inventario().umbralBajo();
    }

    public ReporteOrdenesPorEstado ordenesPorEstado(EstadoOrden estado) {
        List<OrdenResumen> todas = transacciones.consultar(conexion ->
                ordenConsultaDao.listar(conexion, FiltroOrdenes.porEstado(null)));
        List<OrdenResumen> filtradas = estado == null
                ? todas
                : todas.stream().filter(orden -> orden.estado() == estado).toList();
        return new ReporteOrdenesPorEstado(estado, OrdenResumen.contarPorEstado(todas), filtradas);
    }

    public HistorialVehiculo historialVehiculo(String placa) {
        return transacciones.consultar(conexion -> {
            Vehiculo vehiculo = vehiculoDao.buscarPorPlaca(conexion, placa)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", Vehiculo.normalizarPlaca(placa)));
            List<OrdenResumen> ordenes = ordenConsultaDao.listar(conexion, FiltroOrdenes.porVehiculo(vehiculo.getId()));
            BigDecimal totalFacturado = ordenes.stream()
                    .map(OrdenResumen::total)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new HistorialVehiculo(vehiculo.getPlaca(), vehiculo.descripcion(), vehiculo.getColor(),
                    vehiculo.getPropietario().getNombre(), vehiculo.getPropietario().identificacion(),
                    ordenes.size(), totalFacturado, ordenes);
        });
    }

    public ReporteInventarioBajo repuestosBajoInventario() {
        List<RepuestoInventario> repuestos = transacciones.consultar(conexion ->
                        repuestoDao.listarConExistenciaHasta(conexion, umbralBajo)).stream()
                .map(repuesto -> RepuestoInventario.de(repuesto, umbralBajo))
                .toList();
        int agotados = (int) repuestos.stream().filter(repuesto -> repuesto.nivel() == NivelInventario.AGOTADO).count();
        return new ReporteInventarioBajo(umbralBajo, agotados, repuestos);
    }

    public List<OrdenesPorMecanico> ordenesPorMecanico(Long idMecanico, EstadoOrden estado) {
        return transacciones.consultar(conexion -> {
            List<Mecanico> mecanicos = idMecanico == null
                    ? mecanicoDao.listar(conexion)
                    : List.of(mecanicoDao.buscarPorId(conexion, idMecanico)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Mecánico", idMecanico)));
            Map<Long, List<OrdenResumen>> ordenesPorMecanico = ordenConsultaDao
                    .listar(conexion, FiltroOrdenes.porMecanico(idMecanico, estado)).stream()
                    .collect(Collectors.groupingBy(OrdenResumen::idMecanico));
            return mecanicos.stream()
                    .map(mecanico -> resumirMecanico(mecanico, ordenesPorMecanico.getOrDefault(mecanico.getId(), List.of())))
                    .toList();
        });
    }

    public ReporteIngresos ingresos(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new DatoInvalidoException("Debe indicar el rango de fechas (desde y hasta)");
        }
        if (desde.isAfter(hasta)) {
            throw new DatoInvalidoException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return ReporteIngresos.de(desde, hasta, transacciones.consultar(conexion ->
                ordenConsultaDao.listarIngresos(conexion, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay())));
    }

    private OrdenesPorMecanico resumirMecanico(Mecanico mecanico, List<OrdenResumen> ordenes) {
        return new OrdenesPorMecanico(mecanico.getId(), mecanico.getCodigo(), mecanico.getNombre(),
                mecanico.getEspecialidad(), OrdenResumen.contarPorEstado(ordenes), ordenes);
    }
}
