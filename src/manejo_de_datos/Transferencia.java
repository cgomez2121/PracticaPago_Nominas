package manejo_de_datos;

/**
 * Clase que representa una transferencia.
 * Controla que la transferencia se procese una única vez mediante exclusión mutua,
 * utilizando la propia instancia como monitor implícito para su estado 'procesada'
 */
public class Transferencia {
    private final String cuentaBancaria;
    private final double importe;
    private boolean procesada = false;

    /**
     * cuentaBancaria es el número de cuenta bancaria
     * importe es la cantidad a transferir
     */
    public Transferencia(String cuentaBancaria, double importe) {
        this.cuentaBancaria = cuentaBancaria;
        this.importe = importe;
    }

    // Obtiene el número de cuenta
    public String getCuentaBancaria() {
        return this.cuentaBancaria;
    }

    // Obtiene el importe de la transferencia
    public double getImporte() {
        return this.importe;
    }

    /**
     * Determina si la transferencia es interna.
     * Una transferencia se considera interna si su cuenta de destino empieza por '1'.
     * true si la transferencia es interna, false si es externa.
     */
    public boolean esInterna() {
        return this.cuentaBancaria.startsWith("1");
    }

    /**
     * Marca la transferencia como procesada, asegurando la exclusión mutua.
     * Solo un hilo puede ejecutar el metodo sobre una transferencia a la vez
     * true si el hilo que llega fue el primero en marcarla, false si ya estaba procesada.
     */
    public synchronized boolean marcarComoProcesada() {
        if (this.procesada) {
            return false;
        }
        this.procesada = true;
        return true;
    }
}

