package logica_hilos;

import manejo_de_datos.GestorResultados;
import manejo_de_datos.GestorSaldo;
import manejo_de_datos.Transferencia;

/**
 * Esta clase implementa la interfaz Runnable para ser ejecutado
 * por un ExecutorService.
 * Contiene la lógica para procesar una única transferencia de forma segura.
 */
public class HiloProcesador implements Runnable {

    private final Transferencia transferencia;
    private final GestorSaldo gestorSaldo;
    private final GestorResultados gestorResultados;

    /**
     * Constructor del Hilo Procesador.
     * t Transferencia a procesar.
     * gs Monitor GestorSaldo.
     * gr Monitor GestorResultados.
     */
    public HiloProcesador(Transferencia t, GestorSaldo gs, GestorResultados gr) {
        this.transferencia = t;
        this.gestorSaldo = gs;
        this.gestorResultados = gr;
    }

    /**
     * Este metodo contiene la lógica de procesamiento de la transferencia, el descuento de saldo
     * y la clasificación de resultados.
     */
    @Override
    public void run() {
        // Control de procesamiento único.
        if (!transferencia.marcarComoProcesada()) {
            // Si retorna false, otro hilo ya procesó esta transferencia.
            return;
        }

        String nombreHilo = Thread.currentThread().getName();
        double importe = transferencia.getImporte();

        // Intentar pagar.
        // Se llama al metodo sincronizado descontar, que garantiza el descuento de saldo seguro.
        boolean pagada = gestorSaldo.descontar(importe);

        // Simulación de retardo para transferencias internas

        // en las transferencias internas, se introduce un retardo de 1s.
        if (transferencia.esInterna() && pagada) {
            try {
                Thread.sleep(1000); // Retardo de 1 segundo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Clasificación y almacenamiento
        if (!pagada) {
            // Si no se pudo pagar, se clasifica como sin saldo.
            gestorResultados.clasificarSinSaldo(transferencia);
        } else if (transferencia.esInterna()) {
            gestorResultados.clasificarInterna(transferencia);
        } else {
            // Si fue pagada y no es interna, es externa.
            gestorResultados.clasificarExterna(transferencia);
        }

        // Actualización de Estadísticas

        // Se reporta el resultado al monitor gestorResultados para que acumule el conteo por hilo.
        gestorResultados.actualizarEstadisticas(
                nombreHilo,
                importe,
                pagada,
                transferencia.esInterna()
        );
    }
}