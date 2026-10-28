package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.Vehiculo;

public record VehiculoResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        int anio,
        String color,
        Long idCliente,
        String nombreCliente) {

    public static VehiculoResponse desde(Vehiculo vehiculo) {
        return new VehiculoResponse(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getAnio(),
                vehiculo.getColor(),
                vehiculo.getPropietario().getId(),
                vehiculo.getPropietario().getNombre());
    }
}
