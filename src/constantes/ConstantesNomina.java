package constantes;

/**
 * Clase que almacena todas las constantes del proyecto,
 * incluyendo códigos de salida, rutas de paquetes y valores por defecto.
 */
public class ConstantesNomina {

    // CÓDIGOS DE SALIDA DEL SISTEMA

    /**
     * Terminación correcta del proceso.
     */
    public static final int EXITO = 0;

    /**
     * Fallo del subproceso (ej. al leer/escribir archivos).
     */
    public static final int ERROR_LECTURA_ESCRITURA = 4;

    /**
     * Fallo en SubApGenFT: El fichero de bloqueo ya existía al intentar crearlo.
     */
    public static final int ERROR_BLOQUEO_EXISTENTE = 2;

    /**
     * Fallo en ApPrinc: El proceso SubApGenFT falló o terminó anormalmente.
     * Código usado por ApPrinc al salir.
     */
    public static final int ERROR_AP_PRINC_GEN_FAIL = 10;

    /**
     * Fallo en cualquier subproceso: Datos de entrada inválidos.
     */
    public static final int ERROR_DATOS_INVALIDOS = 6;

    /**
     * Fallo en SubApGenFT: La simulación de error aleatorio (30%) se activó.
     */
    public static final int ERROR_GEN_ALEATORIO = 7;

    /**
     * Error inespecífico/desconocido.
     */
    public static final int ERROR_INESPECIFICO = 99;


    // CONFIGURACIÓN DE EJECUCIÓN (CLASES Y PAQUETES)

    /**
     * Ruta al directorio de las clases compiladas (classpath).
     */
    public static final String RUTA_AL_CLASS = "out/production/TrabajoNominas";
    public static final String PAQUETE_GENERADOR = "generador";
    public static final String PAQUETE_PROCESADOR = "procesador";


    // VALORES POR DEFECTO Y DE ENTRADA

    /**
     * Nombre por defecto del fichero de transferencias.
     */
    public static final String NOMBRE_FICHERO_DEFECTO = "datos_transferencias.txt";

    /**
     * Ruta por defecto (directorio actual).
     */
    public static final String RUTA_DEFECTO = "./";

    /**
     * Número de transferencias por defecto.
     */
    public static final int NUM_TRANSF_DEFECTO = 100;

    /**
     * Número de hilos por defecto para el procesamiento.
     */
    public static final int NUM_HILOS_DEFECTO = 5;


    // NOMBRES DE ARCHIVOS UTILIZADOS

    /**
     * Nombre del archivo de bloqueo (para sincronización de procesos).
     */
    public static final String FICHERO_BLOQUEO = "transferencias.bloqueo";

    /**
     * Nombre del archivo de salida para transferencias internas.
     */
    public static final String FICHERO_INTERNAS = "transferencias_internas.txt";

    /**
     * Nombre del archivo de salida para transferencias externas.
     */
    public static final String FICHERO_EXTERNAS = "transferencias_externas.txt";

    /**
     * Nombre del archivo de salida para transferencias sin saldo.
     */
    public static final String FICHERO_SIN_SALDO = "transferencias_sin_saldo.txt";


    // CONFIGURACIÓN DE CARACTERES

    /**
     * Carácter delimitador utilizado en los archivos de transferencia.
     */
    public static final String CARACTER_DELIMITADOR = ";";

    /**
     * Codificación estándar utilizada para flujos de E/S (UTF-8).
     */
    public static final String CHARSET_ESTANDAR = "UTF-8";
}