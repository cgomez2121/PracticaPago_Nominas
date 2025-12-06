package generador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;
import static constantes.ConstantesNomina.*;

/**
 * Sub-Aplicación de generación del fichero de transferencias.
 * Actúa como productor: recibe datos por entrada estándar,
 * genera el archivo de transferencias y gestiona el fichero de bloqueo.
 */
public class SubApGenFT {

    public static void main(String[] args) {
        String nombreFichero = null;
        String ruta = null;
        int numTransferencias = -1;
        File archivoBloqueo = null;
        File archivoTransferencias = null;

        // LECTURA DE DATOS DESDE LA ENTRADA ESTÁNDAR
        // Se usa InputStreamReader con el charset para asegurar la codificación correcta (UTF-8)
        try (BufferedReader lectorEntrada = new BufferedReader(new InputStreamReader(System.in, CHARSET_ESTANDAR))) {
            nombreFichero = lectorEntrada.readLine();
            ruta = lectorEntrada.readLine();
            numTransferencias = Integer.parseInt(lectorEntrada.readLine());

            // Comprobación de argumentos recibidos
            if (nombreFichero == null || ruta == null || numTransferencias <= 0) {
                System.exit(ERROR_DATOS_INVALIDOS);
            }
            archivoTransferencias = new File(ruta + nombreFichero);
            archivoBloqueo = new File(ruta + FICHERO_BLOQUEO);

        } catch (IOException | NumberFormatException e) {
            // Captura errores de lectura o de conversión de número de transferencias
            System.exit(ERROR_DATOS_INVALIDOS);
        }


        // SIMULACIÓN DE RETRASO (1 a 5 segundos)
        try {
            long tiempoEspera = new Random().nextInt(4000) + 1000;
            Thread.sleep(tiempoEspera);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // FALLO ALEATORIO (30% de probabilidad de fallo)
        if (new Random().nextInt(100) + 1 <= 30) {
            System.exit(ERROR_GEN_ALEATORIO);
        }

        // CREACIÓN DEL FICHERO DE BLOQUEO
        try {
            // Intenta crear el archivo de bloqueo; si ya existe, salimos con error
            if (!archivoBloqueo.createNewFile()) {
                System.exit(ERROR_BLOQUEO_EXISTENTE);
            }
        } catch (IOException e) {
            System.exit(ERROR_LECTURA_ESCRITURA);
        }

        // GENERACIÓN DE LOS DATOS
        try (PrintWriter escritorDatos = new PrintWriter(new FileWriter(archivoTransferencias))) {
            Random aleatorio = new Random();
            for (int i = 0; i < numTransferencias; i++) {
                int primerDigito = aleatorio.nextBoolean() ? 1 : 2;
                long restoCuenta = 10000000L + aleatorio.nextInt(90000000);
                String cuenta = primerDigito + String.valueOf(restoCuenta);

                double cantidad = 1500 + (3000 - 1500) * aleatorio.nextDouble();

                // Garantiza que la salida tenga punto decimal
                String cantidadStr = String.format("%.2f", cantidad).replace(",", ".");

                // Escritura con formato estricto y delimitador
                escritorDatos.printf("%s%s%s%n", cuenta, CARACTER_DELIMITADOR, cantidadStr);

                Thread.sleep(10); // Retardo de 10ms entre transferencias
            }


        } catch (IOException e) {
            // Intenta limpiar el bloqueo si falla la escritura antes de salir
            if (archivoBloqueo.exists() && !archivoBloqueo.delete()) {
                // Si la escritura falló Y no pudimos borrar el bloqueo, salimos con un error
                System.exit(ERROR_LECTURA_ESCRITURA);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ELIMINACIÓN DEL FICHERO DE BLOQUEO
        // Si el archivo no existe (fue borrado por la excepción anterior o por otra causa) o si se borra con éxito
        if (!archivoBloqueo.exists() || archivoBloqueo.delete()) {
            System.exit(EXITO);
        } else {
            // Si el archivo de bloqueo existe pero la eliminación falló
            System.exit(ERROR_LECTURA_ESCRITURA);
        }
    }
}