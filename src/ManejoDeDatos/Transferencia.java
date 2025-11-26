package ManejoDeDatos;

/**
 Clase que representa una transferencia. Controla que se procese una única vez
 mediante exclusión mutua (monitor implícito).
 */
public class Transferencia {
    private final String cuentaBancaria;
    private final double importe;
    private boolean procesada = false;

    public Transferencia(String cuentaBancaria, double importe) {
        this.cuentaBancaria = cuentaBancaria;
        this.importe = importe;
    }

    // Getters y lógica de clasificación
    public String getCuentaBancaria() {
        return cuentaBancaria; }

    public double getImporte() {
        return importe; }

    public boolean esInterna() {

        return cuentaBancaria.startsWith("1");
    }

    /**
     Metodo que usa synchronized para asegurar exclusión mutua sobre la transferencia procesada.
     */
    public synchronized boolean marcarComoProcesada() {
        if (this.procesada) {
            return false;
        }
        this.procesada = true;
        return true;
    }


}

