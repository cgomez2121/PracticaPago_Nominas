package aplicacion_principal;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.InputMismatchException;
import informe_hilos.EstadisticasHilo;
import static constantes.ConstantesNomina.*;


/**
 * Programa principal, actúa como orquestador del sistema:
 * 1. Gestiona la entrada de datos del usuario.
 * 2. Lanza SubApProcFT (procesador) con argumentos.
 * 3. Lanza SubApGenFT (generador) y le comunica datos por su entrada estándar.
 * 4. Gestiona la finalización forzosa de procesos hijos.
 * 5. Captura los resultados finales del procesador e imprime el informe final con resultado general y por hilo.
 */
public class ApPrinc {

    public static void main(String[] args) {

        // CONSTRUCCIÓN DE COMANDOS BASE
        List<String> comandoBaseProc = List.of("java", "-cp", RUTA_AL_CLASS, PAQUETE_PROCESADOR + "." +  "SubApProcFT");
        List<String> comandoBaseGen = List.of("java", "-cp", RUTA_AL_CLASS, PAQUETE_GENERADOR + "." + "SubApGenFT");

        // OBTENCIÓN DE PARÁMETROS DEL USUARIO
        Scanner sc = new Scanner(System.in);
        String nombreFichero = NOMBRE_FICHERO_DEFECTO;
        String ruta = RUTA_DEFECTO;
        int numTransferencias = NUM_TRANSF_DEFECTO;
        int numHilos = NUM_HILOS_DEFECTO;

        boolean archivoExiste = false;

        // OBTENCIÓN DE DATOS DEL USUARIO
        try {
            System.out.println("APLICACIÓN PRINCIPAL (APPRINC)");

            System.out.print("Nombre del fichero de transferencias [" + NOMBRE_FICHERO_DEFECTO + "]: ");
            String entradaNombre = sc.nextLine();
            if (!entradaNombre.isEmpty()) { nombreFichero = entradaNombre; }

            System.out.print("Ruta de los ficheros [" + RUTA_DEFECTO + "]: ");
            String entradaRuta = sc.nextLine();
            if (!entradaRuta.isEmpty()) {
                ruta = entradaRuta;
                // Asegurar que la ruta termina con separador, si no está vacía
                if (!ruta.endsWith(File.separator)) {
                    ruta += File.separator;
                }
            }

            File archivoTransferencias = new File(ruta + nombreFichero);
            archivoExiste = archivoTransferencias.exists();

            if (!archivoExiste) {
                System.out.print("Número de transferencias a generar [" + NUM_TRANSF_DEFECTO + "]: ");
                String entradaTransf = sc.nextLine();
                if (!entradaTransf.isEmpty()) {
                    numTransferencias = Integer.parseInt(entradaTransf);
                }

                System.out.print("Número de hilos para el procesamiento [" + NUM_HILOS_DEFECTO + "]: ");
                String entradaHilos = sc.nextLine();
                if (!entradaHilos.isEmpty()) {
                    numHilos = Integer.parseInt(entradaHilos);
                }
            } else {
                System.out.println("[APPRINC] Fichero ya existe. Omitiendo generación y usando el archivo existente.");
            }
            sc.close(); // Se cierra el Scanner tras la lectura de todos los datos

        } catch (InputMismatchException | NumberFormatException e) {
            System.err.println("[APPRINC] ERROR: Entrada de usuario inválida. Debe introducir un número. Saliendo.");
            System.exit(ERROR_DATOS_INVALIDOS);
        } catch (Exception e) {
            System.err.println("[APPRINC] ERROR Inesperado: " + e.getMessage());
            System.exit(ERROR_INESPECIFICO);
        }

        Process procesoProc = null;
        Process procesoGen = null;
        int codigoSalidaProc = ERROR_INESPECIFICO;
        String resultadosProc = null;

        // CONSTRUCCIÓN FINAL DEL COMANDO PARA EL PROCESADOR (CON ARGUMENTOS)
        List<String> comandoProc = new ArrayList<>(comandoBaseProc);
        comandoProc.add(ruta);
        comandoProc.add(nombreFichero);
        comandoProc.add(String.valueOf(numHilos));

        try {
            // LANZAR SubApProcFT (PROCESADOR) - PRIMERO
            ProcessBuilder constructorProcesoProc = new ProcessBuilder(comandoProc);

            System.out.println("\n[APPRINC] 1. Lanzando Procesador (SubApProcFT) con argumentos...");
            procesoProc = constructorProcesoProc.start();
            System.out.println("[APPRINC] SubApProcFT lanzado. PID: " + procesoProc.pid() + " (Iniciando espera activa).");


            // LANZAR SubApGenFT (GENERADOR) SI ES NECESARIO
            if (!archivoExiste) {
                // Se usa redirectError.INHERIT para ver los mensajes por si hay error del generador
                ProcessBuilder constructorProcesoGen = new ProcessBuilder(comandoBaseGen);
                constructorProcesoGen.redirectError(ProcessBuilder.Redirect.INHERIT);

                System.out.println("[APPRINC] 2. Fichero NO encontrado. Lanzando Generador (SubApGenFT)...");
                procesoGen = constructorProcesoGen.start();

                // ENVIAR PARÁMETROS AL GENERADOR POR LA ENTRADA ESTÁNDAR
                try (OutputStream flujoSalida = procesoGen.getOutputStream()) {
                    // solo necesita: nombreFichero, ruta, numTransferencias
                    String datos = nombreFichero + "\n" + ruta + "\n" + numTransferencias + "\n";
                    // Se usa UTF-8 para garantizar que los caracteres especiales se pasen correctamente
                    flujoSalida.write(datos.getBytes(CHARSET_ESTANDAR));
                    flujoSalida.flush();
                    System.out.println("[APPRINC] Parámetros enviados a SubApGenFT por entrada estándar.");
                }

                // ESPERAR A QUE EL GENERADOR TERMINE
                int codigoSalidaGen = procesoGen.waitFor();

                // MANEJAR CÓDIGO DE SALIDA DEL GENERADOR
                if (codigoSalidaGen != EXITO) {
                    System.err.println(String.format("[APPRINC] ERROR: SubApGenFT finalizó con código %d. Matando SubApProcFT.", codigoSalidaGen));
                    // Si el generador falla, matamos al procesador que está esperando.
                    procesoProc.destroyForcibly();
                    procesoProc.waitFor(); // Esperar terminación forzosa
                    System.exit(ERROR_GEN_ALEATORIO); // Código de ApPrinc por fallo del generador
                } else {
                    System.out.println("[APPRINC] SubApGenFT finalizó con ÉXITO.");
                }
            } else {
                System.out.println("[APPRINC] 2. Generador omitido.");
            }

            // ESPERAR Y CAPTURAR RESULTADOS DEL PROCESADOR
            System.out.println("[APPRINC] 3. Esperando a que SubApProcFT finalice su procesamiento...");

            // CAPTURAR LA SALIDA DEL PROCESADOR (resultados)
            try (BufferedReader lectorResultados = new BufferedReader(
                    new InputStreamReader(procesoProc.getInputStream(), CHARSET_ESTANDAR))) {
                // Se lee la única línea de resultado enviada por System.out del hijo
                resultadosProc = lectorResultados.readLine();
            }

            // Esperar el código de salida final del procesador
            codigoSalidaProc = procesoProc.waitFor();

            if (codigoSalidaProc == EXITO && resultadosProc != null) {
                System.out.println("\n[APPRINC] ÉXITO: Flujo completado. Generando informe...");
                generarInformeFinal(resultadosProc);
            } else {
                System.err.println(String.format("\n[APPRINC] Formato erroneo, no se puede generar el informe, SubApProcFT finalizó con código %d." +
                        "", codigoSalidaProc));
            }


        } catch (IOException e) {
            System.err.println("[APPRINC] ERROR FATAL: Fallo al lanzar el subproceso (Verifique RUTA_AL_CLASS). " + e.getMessage());
            // Intento de limpieza de procesos
            if (procesoProc != null && procesoProc.isAlive()) {
                procesoProc.destroyForcibly(); }
            System.exit(ERROR_AP_PRINC_GEN_FAIL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[APPRINC] Proceso interrumpido. Terminación forzosa de procesos hijos.");
            // Limpieza de todos los procesos lanzados
            if (procesoProc != null && procesoProc.isAlive()) {
                procesoProc.destroyForcibly(); }
            if (procesoGen != null && procesoGen.isAlive()) {
                procesoGen.destroyForcibly(); }
            System.exit(ERROR_AP_PRINC_GEN_FAIL);
        }

        System.exit(codigoSalidaProc);
    }


    // ====== MÉTODOS AUXILIARES Y DE INFORME ======


    /**
     * Metodo para asegurar que no hay error por comas, usando punto decimal.
     * valor es el valor double a formatear para añadirle el (.).
     */
    private static String formatearSalida(double valor) {
        // Usamos String.format para el %.2f y luego reemplazamos la coma por punto.
        return String.format("%.2f", valor).replace(",", ".");
    }

    /**
     * Parsea la cadena de resultados devuelta por SubApProcFT y genera el informe final.
     * resultados es la cadena con los resultados finales.
     */
    private static void generarInformeFinal(String resultados) {
        String[] partes = resultados.split(CARACTER_DELIMITADOR);

        if (partes.length < 3) {
            System.err.println("[APPRINC] ERROR: Formato de resultados de SubApProcFT inválido.");
            return;
        }

        // RESULTADOS GENERALES
        // Se asegura el parseo correcto reemplazando comas por puntos
        double saldoInicial = Double.parseDouble(partes[0].replace(",", "."));
        double saldoFinal = Double.parseDouble(partes[1].replace(",", "."));
        int sinSaldoGlobal = Integer.parseInt(partes[2]);

        // ESTADÍSTICAS POR HILO
        List<EstadisticasHilo> estadisticasHilos = new ArrayList<>();

        // El resto de las partes (a partir del índice 3 del Array) son estadísticas por hilo.
        for (int i = 3; i < partes.length; i++) {
            String[] stats = partes[i].split(",");
            if (stats.length == 7) {
                EstadisticasHilo eh = new EstadisticasHilo();
                eh.nombreHilo = stats[0];
                eh.transferenciasProcesadas = Integer.parseInt(stats[1]);
                eh.internasProcesadas = Integer.parseInt(stats[2]);
                eh.externasProcesadas = Integer.parseInt(stats[3]);
                eh.sinSaldoProcesadas = Integer.parseInt(stats[4]);
                eh.importeProcesado = Double.parseDouble(stats[5].replace(",", "."));
                eh.importePagado = Double.parseDouble(stats[6].replace(",", "."));
                estadisticasHilos.add(eh);
            }
        }

        // CÁLCULO DE TOTALES GENERALES (A partir de los hilos)
        int totalProcesadas = 0;
        double importeTotalProcesado = 0.0;
        double importeTotalPagado = 0.0;
        int internasTotal = 0;
        int externasTotal = 0;

        for (EstadisticasHilo estadisticas : estadisticasHilos) {
            totalProcesadas += estadisticas.transferenciasProcesadas;
            importeTotalProcesado += estadisticas.importeProcesado;
            importeTotalPagado += estadisticas.importePagado;
            internasTotal += estadisticas.internasProcesadas;
            externasTotal += estadisticas.externasProcesadas;
        }

        // IMPRESIÓN DEL INFORME

        // RESUMEN GENERAL
        System.out.println("\n--- RESUMEN DEL PROCESAMIENTO GENERAL ---");
        System.out.printf("Saldo inicial de la cuenta: %s%n", formatearSalida(saldoInicial));
        System.out.printf("Número de transferencias procesadas: %d%n", totalProcesadas);
        System.out.printf("Número de transferencias internas: %d%n", internasTotal);
        System.out.printf("Número de transferencias externas: %d%n", externasTotal);
        System.out.printf("Número de transferencias sin saldo: %d%n", sinSaldoGlobal);
        System.out.printf("Número de transferencias pagadas: %d%n", internasTotal + externasTotal);
        System.out.printf("Importe total de las transferencias procesadas: %s%n", formatearSalida(importeTotalProcesado));
        System.out.printf("Importe total de las transferencias pagadas: %s%n", formatearSalida(importeTotalPagado));
        System.out.printf("Saldo final de la cuenta: %s%n", formatearSalida(saldoFinal));

        // DESGLOSE POR HILO
        for (EstadisticasHilo estadisticas : estadisticasHilos) {
            System.out.println("\n--- Resumen del procesamiento del PROCESADOR (" + estadisticas.nombreHilo + ") ---");
            System.out.printf("Número de transferencias procesadas: %d%n", estadisticas.transferenciasProcesadas);
            System.out.printf("Número de transferencias internas: %d%n", estadisticas.internasProcesadas);
            System.out.printf("Número de transferencias externas: %d%n", estadisticas.externasProcesadas);
            System.out.printf("Número de transferencias sin saldo: %d%n", estadisticas.sinSaldoProcesadas);
            System.out.printf("Número de transferencias pagadas: %d%n", estadisticas.internasProcesadas + estadisticas.externasProcesadas);
            System.out.printf("Importe de las transferencias procesadas: %s%n", formatearSalida(estadisticas.importeProcesado));
            System.out.printf("Importe total de las transferencias pagadas: %s%n", formatearSalida(estadisticas.importePagado));
        }
    }
}
