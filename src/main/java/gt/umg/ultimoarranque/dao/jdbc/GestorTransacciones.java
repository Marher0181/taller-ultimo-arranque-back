package gt.umg.ultimoarranque.dao.jdbc;

import gt.umg.ultimoarranque.excepcion.IntegridadDatosException;
import gt.umg.ultimoarranque.excepcion.PersistenciaException;
import gt.umg.ultimoarranque.excepcion.TallerException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class GestorTransacciones {

    private static final String PREFIJO_ERROR_INTEGRIDAD = "23";
    private static final int ERROR_RESTRICCION_CHECK = 3819;

    private final DataSource dataSource;

    public GestorTransacciones(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T consultar(OperacionJdbc<T> operacion) {
        try (Connection conexion = dataSource.getConnection()) {
            return operacion.ejecutar(conexion);
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    public <T> T enTransaccion(OperacionJdbc<T> operacion) {
        try (Connection conexion = dataSource.getConnection()) {
            conexion.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conexion);
                conexion.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                revertir(conexion, e);
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    public void ejecutarEnTransaccion(AccionJdbc accion) {
        enTransaccion(conexion -> {
            accion.ejecutar(conexion);
            return null;
        });
    }

    private void revertir(Connection conexion, Exception causa) {
        try {
            conexion.rollback();
        } catch (SQLException errorReversion) {
            causa.addSuppressed(errorReversion);
        }
    }

    private TallerException traducir(SQLException e) {
        String estado = e.getSQLState();
        boolean violaIntegridad = estado != null && estado.startsWith(PREFIJO_ERROR_INTEGRIDAD);
        if (violaIntegridad || e.getErrorCode() == ERROR_RESTRICCION_CHECK) {
            return new IntegridadDatosException("La operación viola una restricción de integridad de la base de datos", e);
        }
        return new PersistenciaException("Error al acceder a la base de datos", e);
    }
}
