package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.Mecanico;

public record MecanicoResponse(Long id, String codigo, String nombre, String especialidad, String telefono) {

    public static MecanicoResponse desde(Mecanico mecanico) {
        return new MecanicoResponse(
                mecanico.getId(),
                mecanico.getCodigo(),
                mecanico.getNombre(),
                mecanico.getEspecialidad(),
                mecanico.getTelefono());
    }
}
