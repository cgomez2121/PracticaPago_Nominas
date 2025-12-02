package manejo_de_datos;

import informe_hilos.EstadisticasHilo;

import java.util.ArrayList;
import java.util.List;

public class GestorResultados {
    private final List<Transferencia> procesadasInternas = new ArrayList<>();
    private final List<Transferencia> procesadasExternas = new ArrayList<>();
    private final List<Transferencia> procesadasSinSaldo = new ArrayList<>();
    // Lista para guardar las estadísticas
    private final List<EstadisticasHilo> estadisticasHilos = new ArrayList<>();

    // Getters
    public List<Transferencia> getListaSinSaldo() {
        return procesadasSinSaldo;
    }

    public List<Transferencia> getListaInternas() {
        return procesadasInternas;
    }

    public List<Transferencia> getListaExternas() {
        return procesadasExternas;
    }

    public List<EstadisticasHilo> getEstadisticasHilos() {
        return estadisticasHilos;
    }

    // Metodos para clasificar transferencias
    public synchronized void clasificarInterna(Transferencia t) {
        procesadasInternas.add(t);
    }

    public synchronized void clasificarExterna(Transferencia t) {
        procesadasExternas.add(t);
    }

    public synchronized void clasificarSinSaldo(Transferencia t) {
        procesadasSinSaldo.add(t);
    }

    /**
     Busca si ya existen estadísticas para este hilo. Si existen, las actualiza (suma).
     Si no, crea un nuevo registro.
     */
    public synchronized void actualizarEstadisticas(String nombreHilo, double importe, boolean pagada, boolean esInterna, boolean esExterna) {
        EstadisticasHilo statsEncontrado = null;

        // Búsqueda básica en la lista
        for (EstadisticasHilo stats : estadisticasHilos) {
            if (stats.nombreHilo.equals(nombreHilo)) {
                statsEncontrado = stats;
                break;
            }
        }

        // Si no existe, lo creamos y añadimos
        if (statsEncontrado == null) {
            statsEncontrado = new EstadisticasHilo();
            statsEncontrado.nombreHilo = nombreHilo;
            estadisticasHilos.add(statsEncontrado);
        }

        // Acumular los datos (suma simple)
        statsEncontrado.transferenciasProcesadas++;
        statsEncontrado.importeProcesado += importe;

        if (pagada) {
            statsEncontrado.importePagado += importe;
            if (esInterna) statsEncontrado.internasProcesadas++;
            if (esExterna) statsEncontrado.externasProcesadas++;
        } else {
            statsEncontrado.sinSaldoProcesadas++;
        }
    }
}
