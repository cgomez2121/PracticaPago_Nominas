package Constantes;

/**
 Clase que almacena todas las constantes del proyecto.
 */
public class ConstantesNomina {

    // Códigos de salida
    public static final int EXITO = 0;
    public static final int ERROR_INESPECIFICO = 99;
    public static final int ERROR_LECTURA_ESCRITURA = 4;
    public static final int ERROR_BLOQUEO_EXISTENTE = 2;
    public static final int ERROR_AP_PRINC_GEN_FAIL = 5;
    public static final int ERROR_DATOS_INVALIDOS = 6;
    public static final int ERROR_GEN_ALEATORIO = 7;

    // Configuración de archivos
    public static final String RUTA_AL_CLASS = "out/production/PagoNominas";
    public static final String PAQUETE = "Constantes.";

    // Valores por defecto
    public static final String NOMBRE_FICHERO_DEFECTO = "datos_transferencias.txt";
    public static final String RUTA_DEFECTO = "./";
    public static final int NUM_TRANSF_DEFECTO = 100;
    public static final int NUM_HILOS_DEFECTO = 5;

    // Nombres de archivos utilizados
    public static final String FICHERO_TRANSFERENCIAS = NOMBRE_FICHERO_DEFECTO;
    public static final String FICHERO_BLOQUEO = "transferencias.bloqueo";
    public static final String FICHERO_INTERNAS = "transferencias_internas.txt";
    public static final String FICHERO_EXTERNAS = "transferencias_externas.txt";
    public static final String FICHERO_SIN_SALDO = "transferencias_sin_saldo.txt";

    // Configuración de caracteres
    public static final String CARACTER_DELIMITADOR = ";";
    public static final String CHARSET_ESTANDAR = "UTF-8";
}