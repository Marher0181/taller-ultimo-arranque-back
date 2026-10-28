package gt.umg.ultimoarranque.dao.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class JdbcDao<T> {

    protected abstract T mapear(ResultSet fila) throws SQLException;

    protected List<T> consultarLista(Connection conexion, String sql, Object... parametros) throws SQLException {
        return consultar(conexion, sql, this::mapear, parametros);
    }

    protected Optional<T> consultarUno(Connection conexion, String sql, Object... parametros) throws SQLException {
        return consultarLista(conexion, sql, parametros).stream().findFirst();
    }

    protected <R> List<R> consultar(Connection conexion, String sql, MapeadorFila<R> mapeador,
                                    Object... parametros) throws SQLException {
        try (PreparedStatement sentencia = preparar(conexion, sql, parametros);
             ResultSet filas = sentencia.executeQuery()) {
            List<R> resultado = new ArrayList<>();
            while (filas.next()) {
                resultado.add(mapeador.mapear(filas));
            }
            return resultado;
        }
    }

    protected boolean existe(Connection conexion, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement sentencia = preparar(conexion, sql, parametros);
             ResultSet filas = sentencia.executeQuery()) {
            return filas.next();
        }
    }

    protected long insertarConClave(Connection conexion, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(sentencia, parametros);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("La base de datos no devolvió la clave generada");
                }
                return claves.getLong(1);
            }
        }
    }

    protected int ejecutarActualizacion(Connection conexion, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement sentencia = preparar(conexion, sql, parametros)) {
            return sentencia.executeUpdate();
        }
    }

    protected static String marcadores(Collection<?> valores) {
        return String.join(", ", Collections.nCopies(valores.size(), "?"));
    }

    protected static LocalDateTime fechaHora(ResultSet fila, String columna) throws SQLException {
        Timestamp valor = fila.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private PreparedStatement preparar(Connection conexion, String sql, Object... parametros) throws SQLException {
        PreparedStatement sentencia = conexion.prepareStatement(sql);
        try {
            asignarParametros(sentencia, parametros);
            return sentencia;
        } catch (SQLException e) {
            sentencia.close();
            throw e;
        }
    }

    private void asignarParametros(PreparedStatement sentencia, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            asignar(sentencia, i + 1, parametros[i]);
        }
    }

    private void asignar(PreparedStatement sentencia, int indice, Object valor) throws SQLException {
        switch (valor) {
            case null -> sentencia.setNull(indice, Types.NULL);
            case Enum<?> enumerado -> sentencia.setString(indice, enumerado.name());
            case String texto -> sentencia.setString(indice, texto);
            case Integer entero -> sentencia.setInt(indice, entero);
            case Long largo -> sentencia.setLong(indice, largo);
            case BigDecimal monto -> sentencia.setBigDecimal(indice, monto);
            case LocalDateTime fecha -> sentencia.setTimestamp(indice, Timestamp.valueOf(fecha));
            case LocalDate fecha -> sentencia.setDate(indice, Date.valueOf(fecha));
            default -> sentencia.setObject(indice, valor);
        }
    }
}
