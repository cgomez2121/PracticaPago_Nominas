package manejo_de_datos;

import informe_hilos.EstadisticasHilo;

import java.util.ArrayList;
import java.util.List;

/**
 * Monitor para el almacenamiento centralizado de resultados.
 * Reune las operaciones de escritura (clasificación de transferencias y actualización de estadísticas)
 * para evitar interferencia entre hilos y garantizar la consistencia de los datos.
 */
public class GestorResultados {
    // Estas listas son recursos compartidos que solo se modifican a través de métodos sincronizados.
    private final List<Transferencia> procesadasInternas = new ArrayList<>();
    private final List<Transferencia> procesadasExternas = new ArrayList<>();
    private final List<Transferencia> procesadasSinSaldo = new ArrayList<>();
    private final List<EstadisticasHilo> estadisticasHilos = new ArrayList<>();


    // Obtiene la lista de transferencias sin saldo.
    public synchronized List<Transferencia> getListaSinSaldo() {
        return procesadasSinSaldo;
    }

    // Obtiene la lista de transferencias internas procesadas.
    public synchronized List<Transferencia> getListaInternas() {
        return procesadasInternas;
    }

    // Obtiene la lista de transferencias externas procesadas.
    public synchronized List<Transferencia> getListaExternas() {
        return procesadasExternas;
    }

    // Obtiene la lista de estadísticas acumuladas por hilo.
    public synchronized List<EstadisticasHilo> getEstadisticasHilos() {
        return estadisticasHilos;
    }

    // --- Métodos de Clasificación ---

    /**
     * Clasifica una transferencia como interna.
     * t Transferencia procesada.
     */
    public synchronized void clasificarInterna(Transferencia t) {
        procesadasInternas.add(t);
    }

    /**
     * Clasifica una transferencia como externa.
     * t Transferencia procesada.
     */
    public synchronized void clasificarExterna(Transferencia t) {
        procesadasExternas.add(t);
    }

    /**
     * Clasifica una transferencia como sin saldo.
     * t Transferencia procesada.
     */
    public synchronized void clasificarSinSaldo(Transferencia t) {
        procesadasSinSaldo.add(t);
    }

    /**
     * Busca si ya existen estadísticas para este hilo y las actualiza, o crea un nuevo registro.
     * Toda la operación esta protegida y evita la interferencia al modificar la lista compartida de estadísticas.
     * nombreHilo es el nombre del hilo (ej. pool-1-thread-2).
     * importe es el importe de la transferencia procesada.
     * pagada es true si se pudo pagar.
     * esInterna es true si la transferencia es interna.
     */
    public synchronized void actualizarEstadisticas(String nombreHilo, double importe, boolean pagada, boolean esInterna) {
        EstadisticasHilo statsEncontrado = null;

        // Búsqueda del registro del hilo
        for (EstadisticasHilo stats : estadisticasHilos) {
            if (stats.nombreHilo.equals(nombreHilo)) {
                statsEncontrado = stats;
                break;
            }
        }

        // Si no existe, se crea un nuevo registro y se añade a la lista
        if (statsEncontrado == null) {
            statsEncontrado = new EstadisticasHilo();
            statsEncontrado.nombreHilo = nombreHilo;
            estadisticasHilos.add(statsEncontrado);
        }

        // Acumulación de los datos
        statsEncontrado.transferenciasProcesadas++;
        statsEncontrado.importeProcesado += importe;

        if (pagada) {
            statsEncontrado.importePagado += importe;
            if (esInterna) {
                statsEncontrado.internasProcesadas++;
            } else {
                // Si fue pagada y no es interna, es externa
                statsEncontrado.externasProcesadas++;
            }
        } else {
            // No fue pagada
            statsEncontrado.sinSaldoProcesadas++;
        }
    }

}

