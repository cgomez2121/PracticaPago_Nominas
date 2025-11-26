package InformeHilos;

/**
 Almacena todas las estadisticas procesadas por un único hilo,
 lo necesitamos para el desglose del informe final hilo por hilo.
 */

public class EstadisticasHilo {
    public int transferenciasProcesadas;
    public int internasProcesadas;
    public int externasProcesadas;
    public int sinSaldoProcesadas;
    public double importeProcesado;
    public double importePagado;
    public String nombreHilo;
}
