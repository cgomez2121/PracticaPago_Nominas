package ManejoDeDatos;

/**
 Monitor para la variable compartida 'saldo'. Protege la operación importantisima de descuento del saldo principal
 de la empresa.
 */
public class GestorSaldo {
    private double saldoInicial;
    private double saldo;

    public GestorSaldo(double saldoInicial) {
        this.saldoInicial = saldoInicial;
        this.saldo = saldoInicial;
    }

    public double getSaldo() {
        return saldo;}

    public double getSaldoInicial() {
        return saldoInicial; }

    /**
     Realiza la operación crítica de descuento de forma thread-safe (synchronized).
     */
    public synchronized boolean descontar(double importe) {
        if (this.saldo >= importe) {
            this.saldo -= importe;
            return true;
        }
        return false;
    }


}
