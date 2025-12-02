package logica_hilos;

import manejo_de_datos.GestorResultados;
import manejo_de_datos.GestorSaldo;
import manejo_de_datos.Transferencia;

/**
 Hilo que procesa una única transferencia.
 Reporta el resultado al GestorResultados para que lo acumule.
 */
public class HiloProcesador implements Runnable {
    private final Transferencia transferencia;
    private final GestorSaldo gestorSaldo;
    private final GestorResultados gestorResultados;

    public HiloProcesador(Transferencia t, GestorSaldo gs, GestorResultados gr) {
        this.transferencia = t;
        this.gestorSaldo = gs;
        this.gestorResultados = gr;
    }

    @Override
    public void run() {
        // Control de Procesamiento único
        if (!transferencia.marcarComoProcesada()) {

            return;
        }

        String nombreHilo = Thread.currentThread().getName();
        double importe = transferencia.getImporte();
        boolean pagada = false;

        // Intentar pagar (GestorSaldo)
        boolean descuentoExitoso = gestorSaldo.descontar(importe);

        if (descuentoExitoso) {
            pagada = true;
        }

        // Simulación de retardo
        if (transferencia.esInterna() && pagada) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Clasificación en listas
        if (!pagada) {
            gestorResultados.clasificarSinSaldo(transferencia);
        } else if (transferencia.esInterna()) {
            gestorResultados.clasificarInterna(transferencia);
        } else {
            gestorResultados.clasificarExterna(transferencia);
        }

        // ACTUALIZACIÓN DE ESTADÍSTICAS

        gestorResultados.actualizarEstadisticas(
                nombreHilo,
                importe,
                pagada,
                transferencia.esInterna(),
                !transferencia.esInterna()
        );
    }
}