package gt.umg.ultimoarranque.modelo;

import java.util.Objects;

public abstract class Entidad {

    private Long id;

    protected Entidad(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public boolean esNueva() {
        return id == null;
    }

    public void asignarId(long id) {
        if (this.id != null) {
            throw new IllegalStateException(getClass().getSimpleName() + " ya tiene identificador " + this.id);
        }
        this.id = id;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        return id != null && id.equals(((Entidad) otro).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }
}
