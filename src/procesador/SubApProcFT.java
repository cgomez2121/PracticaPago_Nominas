package procesador;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import informe_hilos.EstadisticasHilo;
import logica_hilos.HiloProcesador;
import manejo_de_datos.GestorSaldo;
import manejo_de_datos.GestorResultados;
import manejo_de_datos.Transferencia;
import static constantes.ConstantesNomina.*;


/**
 * Sub-Aplicación de procesamiento del fichero de transferencias.
 * Actúa como consumidor: espera la creación del archivo, lo carga, y lo procesa
 * concurrentemente utilizando un pool de hilos y monitores compartidos.
 */
public final class SubApProcFT {

    // Monitores compartidos por todos los hilos procesadores
    private static GestorSaldo GESTOR_SALDO;
    private static GestorResultados GESTOR_RESULTADOS;

    public static void main(String[] args) {
        String ruta = RUTA_DEFECTO;
        String nombreFichero = NOMBRE_FICHERO_DEFECTO;
        int numHilos = NUM_HILOS_DEFECTO;
        List<Transferencia> listaTransferencias = new ArrayList<>(); // Lista local para la carga de datos

        // CÓDIGO PARA ASEGURAR CODIFICACIÓN DE SALIDA (UTF-8)
        try {
            // Aseguramos que System.out (el canal para enviar resultados a ApPrinc) use UTF-8.
            PrintStream out = new PrintStream(System.out, true, CHARSET_ESTANDAR);
            System.setOut(out);

        } catch (UnsupportedEncodingException e) {
            System.err.println("ERROR: Codificación " + CHARSET_ESTANDAR + " no soportada.");
            System.exit(ERROR_INESPECIFICO);
        }

        // LECTURA DE DATOS DESDE ARGUMENTOS
        if (args.length != 3) {
            System.err.println("ERROR: Número de argumentos incorrecto. Se esperan: ruta, nombre_fichero, num_hilos.");
            System.exit(ERROR_DATOS_INVALIDOS);
        }

        try {
            ruta = args[0];
            // Asegurar que la ruta termina con separador
            if (!ruta.endsWith(File.separator)) { ruta += File.separator; }
            nombreFichero = args[1];
            numHilos = Integer.parseInt(args[2]);
            if (numHilos <= 0) {
                System.err.println("ERROR: El número de hilos debe ser positivo.");
                System.exit(ERROR_DATOS_INVALIDOS);
            }
        } catch (NumberFormatException e) {
            System.err.println("ERROR: El número de hilos no es un entero válido.");
            System.exit(ERROR_DATOS_INVALIDOS);
        }

        File archivoTransferencias = new File(ruta + nombreFichero);
        File archivoBloqueo = new File(ruta + FICHERO_BLOQUEO);


        // LÓGICA DE ESPERA ACTIVA (POLLING)
        System.err.println("[SubApProcFT] Iniciando espera de archivo: " + archivoTransferencias.getName());
        while (true) {
            try {
                // Verificación de interrupción (si ApPrinc lo mata)
                if (Thread.interrupted()) {
                    System.err.println("[SubApProcFT] Interrupción recibida. Finalizando.");
                    System.exit(ERROR_AP_PRINC_GEN_FAIL);
                }

                // Condición de continuación: Archivo de transferencias existe Y NO hay archivo de bloqueo.
                if (archivoTransferencias.exists() && !archivoBloqueo.exists()) {
                    System.err.println("[SubApProcFT] Archivo encontrado y desbloqueado. Cargando datos.");
                    break;
                }

                Thread.sleep(1000); // Espera no activa (1 segundo)

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.exit(ERROR_AP_PRINC_GEN_FAIL);
            }
        }

        // CARGA DEL ARCHIVO A MEMORIA
        try (BufferedReader lector = new BufferedReader(new FileReader(archivoTransferencias))) {
            String linea;
            int numLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numLinea++;
                String[] partes = linea.split(CARACTER_DELIMITADOR);

                // VERIFICACIÓN DE FORMATO
                if (partes.length != 2) {
                    System.err.printf("[SubApProcFT] ERROR (Línea %d): Formato de campos incorrecto: %s. Abortando.%n", numLinea, linea);
                    System.exit(ERROR_DATOS_INVALIDOS); // TERMINACIÓN INMEDIATA
                }

                // VERIFICACIÓN DE PARSEO NUMÉRICO
                try {
                    String cuenta = partes[0];
                    String importeCadena = partes[1].replace(",", ".");
                    double importe = Double.parseDouble(importeCadena);

                    // Si el parseo es exitoso, se añade la transferencia.
                    listaTransferencias.add(new Transferencia(cuenta, importe));

                } catch (NumberFormatException e) {
                    System.err.printf("[SubApProcFT] ERROR (Línea %d): Importe (%s) no es un número válido. Abortando.%n", numLinea, partes[1]);
                    System.exit(ERROR_DATOS_INVALIDOS); // TERMINACIÓN INMEDIATA
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: Archivo no encontrado durante la carga.");
            System.exit(ERROR_LECTURA_ESCRITURA);
        } catch (IOException e) {
            System.err.println("ERROR: Fallo durante la lectura del archivo.");
            System.exit(ERROR_LECTURA_ESCRITURA);
        }

        // INICIALIZACIÓN DE MONITORES
        Random aleatorio = new Random();
        // Rango de saldo por transferencia: (1500, 2700)
        double saldoInicialAleatorio = (1500 + (2700 - 1500) * aleatorio.nextDouble());
        double saldoInicial = saldoInicialAleatorio * listaTransferencias.size();

        GESTOR_SALDO = new GestorSaldo(saldoInicial);
        GESTOR_RESULTADOS = new GestorResultados();
        System.err.printf("[SubApProcFT] Saldo inicial de la empresa: %.2f%n", saldoInicial);

        // PROCESAMIENTO CONCURRENTE (ExecutorService)
        ExecutorService piscinaHilos = Executors.newFixedThreadPool(numHilos);
        try {
            // Se lanza un hilo por cada transferencia cargada
            for (Transferencia t : listaTransferencias) {
                piscinaHilos.submit(new HiloProcesador(t, GESTOR_SALDO, GESTOR_RESULTADOS));
            }

            piscinaHilos.shutdown();
            // Esperamos un máximo de 60 segundos por si algún hilo se queda atascado
            if (!piscinaHilos.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("[SubApProcFT] Terminación forzada del pool de hilos.");
                piscinaHilos.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            piscinaHilos.shutdownNow();
            System.exit(ERROR_INESPECIFICO);
        }

        // GENERACIÓN DE SALIDAS Y REPORTE
        escribirResultadosAArchivos(GESTOR_RESULTADOS, ruta);

        // Construir la cadena de resultados para que ApPrinc la lea de System.out
        String resultados = construirCadenaResultados(GESTOR_SALDO, GESTOR_RESULTADOS);

        // Envía el resultado final al proceso padre por System.out
        System.out.println(resultados);

        // Vaciamos los buffers de salida antes de System.exit
        System.out.flush();

        System.exit(EXITO);
    }


    // ===== MÉTODOS AUXILIARES =====


    /**
     * Metodo para formatear double a String con punto decimal .
     * valor es el valor double a formatear.
     */
    private static String formatearSalida(double valor) {
        return String.format("%.2f", valor).replace(",", ".");
    }

    /**
     * Escribe los resultados a los archivos de clasificación.
     * gestorResultados contiene las listas de transferencias clasificadas.
     * ruta es la ruta donde se guardarán los archivos.
     */
    private static void escribirResultadosAArchivos(GestorResultados gestorResultados, String ruta) {
        escribirLista(FICHERO_INTERNAS, gestorResultados.getListaInternas(), ruta);
        escribirLista(FICHERO_EXTERNAS, gestorResultados.getListaExternas(), ruta);
        escribirLista(FICHERO_SIN_SALDO, gestorResultados.getListaSinSaldo(), ruta);
    }

    /**
     * Escribe una lista de transferencias en un archivo específico.
     * nombreArchivo es el nombre del archivo a crear.
     * lista es la lista de transferencias a escribir.
     * ruta es la ruta de destino.
     */
    private static void escribirLista(String nombreArchivo, List<Transferencia> lista, String ruta) {
        // Se usa try-with-resources para asegurar el cierre automático del recurso
        try (PrintWriter escritor = new PrintWriter(new FileWriter(ruta + nombreArchivo))) {
            for (Transferencia t : lista) {
                String importeStr = formatearSalida(t.getImporte());
                escritor.printf("%s%s%s%n", t.getCuentaBancaria(), CARACTER_DELIMITADOR, importeStr);
            }
        } catch (IOException e) {
            // Reportar el fallo de escritura
            System.err.println("[SubApProcFT] ERROR grave: Fallo al escribir el archivo de resultados " + nombreArchivo);
            System.exit(ERROR_LECTURA_ESCRITURA);
        }
    }

    /**
     * Construye una cadena con todos los resultados necesarios para el informe de ApPrinc.
     * gestorSaldo contiene el saldo inicial y final.
     * gestorResultados contiene las estadísticas por hilo.
     * Esto devuelve una cadena de texto con los resultados delimitados.
     */
    private static String construirCadenaResultados(GestorSaldo gestorSaldo, GestorResultados gestorResultados) {
        StringBuilder constructorCadena = new StringBuilder();

        // Datos Generales: Saldo Inicial; Saldo Final; Total Sin Saldo
        constructorCadena.append(formatearSalida(gestorSaldo.getSaldoInicial())).append(CARACTER_DELIMITADOR);
        constructorCadena.append(formatearSalida(gestorSaldo.getSaldo())).append(CARACTER_DELIMITADOR);
        constructorCadena.append(gestorResultados.getListaSinSaldo().size()).append(CARACTER_DELIMITADOR);

        // Estadísticas por Hilo
        boolean esPrimero = true;
        for (EstadisticasHilo estadisticas : gestorResultados.getEstadisticasHilos()) {
            if (!esPrimero) {
                constructorCadena.append(CARACTER_DELIMITADOR);
            }

            // Construye la cadena de estadísticas para este hilo: NombreHilo,P,I,E,S,ImpP,ImpG
            constructorCadena.append(estadisticas.nombreHilo).append(",");
            constructorCadena.append(estadisticas.transferenciasProcesadas).append(",");
            constructorCadena.append(estadisticas.internasProcesadas).append(",");
            constructorCadena.append(estadisticas.externasProcesadas).append(",");
            constructorCadena.append(estadisticas.sinSaldoProcesadas).append(",");
            constructorCadena.append(formatearSalida(estadisticas.importeProcesado)).append(",");
            constructorCadena.append(formatearSalida(estadisticas.importePagado));

            esPrimero = false;
        }

        return constructorCadena.toString();
    }
}