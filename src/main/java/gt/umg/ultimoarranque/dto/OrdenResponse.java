package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.Responsable;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import gt.umg.ultimoarranque.modelo.orden.OrdenTrabajo;
import gt.umg.ultimoarranque.modelo.orden.ResumenCostos;

import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponse(
        Long id,
        EstadoOrden estado,
        boolean admiteModificaciones,
        String descripcionProblema,
        VehiculoResponse vehiculo,
        MecanicoResponse mecanico,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFinalizacion,
        LocalDateTime fechaCancelacion,
        String motivoCancelacion,
        Responsable registradaPor,
        Responsable iniciadaPor,
        Responsable cerradaPor,
        List<DetalleServicioResponse> servicios,
        List<DetalleRepuestoResponse> repuestos,
        ResumenCostos costos,
        ComprobanteResponse comprobante) {

    public static OrdenResponse desde(OrdenTrabajo orden, ResumenCostos costos) {
        return new OrdenResponse(
                orden.getId(),
                orden.getEstado(),
                orden.admiteModificaciones(),
                orden.getDescripcionProblema(),
                VehiculoResponse.desde(orden.getVehiculo()),
                MecanicoResponse.desde(orden.getMecanico()),
                orden.getFechaCreacion(),
                orden.getFechaInicio(),
                orden.getFechaFinalizacion(),
                orden.getFechaCancelacion(),
                orden.getMotivoCancelacion(),
                orden.getRegistradaPor(),
                orden.getIniciadaPor(),
                orden.getCerradaPor(),
                orden.getServicios().stream().map(DetalleServicioResponse::desde).toList(),
                orden.getRepuestos().stream().map(DetalleRepuestoResponse::desde).toList(),
                costos,
                orden.getComprobante().map(ComprobanteResponse::desde).orElse(null));
    }
}
