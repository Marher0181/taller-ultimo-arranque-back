package gt.umg.ultimoarranque.modelo;

public enum NivelInventario {
    AGOTADO,
    BAJO,
    DISPONIBLE;

    public static NivelInventario evaluar(int existencia, int umbralBajo) {
        if (existencia == 0) {
            return AGOTADO;
        }
        return existencia <= umbralBajo ? BAJO : DISPONIBLE;
    }
}
