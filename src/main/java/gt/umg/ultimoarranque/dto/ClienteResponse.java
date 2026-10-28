package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.Cliente;
import gt.umg.ultimoarranque.modelo.documento.TipoDocumento;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String documentoFormateado,
        String nombre,
        String telefono,
        String correo,
        String direccion,
        LocalDateTime fechaRegistro) {

    public static ClienteResponse desde(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getDocumento().getTipo(),
                cliente.getDocumento().getNumero(),
                cliente.getDocumento().formateado(),
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getCorreo(),
                cliente.getDireccion(),
                cliente.getFechaRegistro());
    }
}
