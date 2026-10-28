package gt.umg.ultimoarranque.modelo.consulta;

import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record OrdenResumen(
        Long id,
        EstadoOrden estado,
        String descripcionProblema,
        Long idVehiculo,
        String placa,
        String vehiculo,
        Long idCliente,
        String cliente,
        Long idMecanico,
        String mecanico,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaFinalizacion,
        BigDecimal total) {

    public static Map<EstadoOrden, Long> contarPorEstado(Collection<OrdenResumen> ordenes) {
        Map<EstadoOrden, Long> conteo = Arrays.stream(EstadoOrden.values())
                .collect(Collectors.toMap(Function.identity(), estado -> 0L, Long::sum, () -> new EnumMap<>(EstadoOrden.class)));
        ordenes.forEach(orden -> conteo.merge(orden.estado(), 1L, Long::sum));
        return conteo;
    }
}
