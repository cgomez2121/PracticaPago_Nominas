package manejo_de_datos;

/**
 * Monitor para la variable compartida 'saldo'.
 * Protege el saldo principal de la empresa de condiciones de carrera durante las operaciones de descuento.
 */
public class GestorSaldo {
    private double saldoInicial;
    private double saldo;


     // saldoInicial es el saldo inicial de la cuenta de la empresa.
    public GestorSaldo(double saldoInicial) {
        this.saldoInicial = saldoInicial;
        this.saldo = saldoInicial;
    }


     // Devuelve el saldo actual.
    public double getSaldo() {
        return this.saldo;
    }

    // Devuelve el saldo inicial.
    public double getSaldoInicial() {
        return this.saldoInicial;
    }

    /**
     * Realiza la operación crítica de descuento de forma segura.
     * Esta es la sección crítica de la clase: se verifica y se modifica el saldo de forma protegida entre hilos.
     * importe es la cantidad a descontar.
     * true si el descuento fue exitoso (había saldo), false en caso contrario.
     */
    public synchronized boolean descontar(double importe) {
        // Se asegura que haya saldo suficiente antes de realizar la modificación
        if (this.saldo >= importe) {
            this.saldo -= importe; // Escritura crítica
            return true;
        }
        return false;
    }
}