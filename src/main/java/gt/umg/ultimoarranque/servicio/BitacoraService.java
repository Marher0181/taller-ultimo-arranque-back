package gt.umg.ultimoarranque.servicio;

import gt.umg.ultimoarranque.dao.BitacoraDao;
import gt.umg.ultimoarranque.dao.jdbc.GestorTransacciones;
import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;
import gt.umg.ultimoarranque.modelo.bitacora.EntradaBitacora;
import gt.umg.ultimoarranque.modelo.bitacora.FiltroBitacora;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BitacoraService {

    private static final int LIMITE_MAXIMO = 1000;

    private final GestorTransacciones transacciones;
    private final BitacoraDao bitacoraDao;

    public BitacoraService(GestorTransacciones transacciones, BitacoraDao bitacoraDao) {
        this.transacciones = transacciones;
        this.bitacoraDao = bitacoraDao;
    }

    public List<EntradaBitacora> consultar(FiltroBitacora filtro) {
        if (filtro.limite() < 1 || filtro.limite() > LIMITE_MAXIMO) {
            throw new DatoInvalidoException("El límite debe estar entre 1 y " + LIMITE_MAXIMO);
        }
        if (filtro.desde() != null && filtro.hasta() != null && filtro.desde().isAfter(filtro.hasta())) {
            throw new DatoInvalidoException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return transacciones.consultar(conexion -> bitacoraDao.listar(conexion, filtro));
    }
}
