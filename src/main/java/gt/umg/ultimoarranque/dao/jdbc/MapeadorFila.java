package gt.umg.ultimoarranque.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface MapeadorFila<T> {

    T mapear(ResultSet fila) throws SQLException;
}
