package informe_hilos;

/**
 * Clase para almacenar todas las estadísticas acumuladas
 * por un único hilo procesador para su posterior uso en el informe final.
 */

public class EstadisticasHilo {

    // Nombre del hilo procesador (ej. pool-1-thread-2).
    public String nombreHilo;

    // Número total de transferencias que el hilo procesó.
    public int transferenciasProcesadas;

    // Número de transferencias internas que el hilo pudo pagar.
    public int internasProcesadas;

    // Número de transferencias externas que el hilo pudo pagar.
    public int externasProcesadas;

    // Número de transferencias que el hilo procesó y para las que no hubo saldo.
    public int sinSaldoProcesadas;

    // Suma total del importe de todas las transferencias procesadas (pagadas o no).
    public double importeProcesado;

    // Suma total del importe de las transferencias que fueron pagadas.
    public double importePagado;
}

