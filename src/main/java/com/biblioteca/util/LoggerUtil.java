package com.biblioteca.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad de registro (logging) de la aplicación BiblioWeb.
 *
 * Qué hace:
 * - Escribe cada evento importante en el archivo log/biblioweb.log
 * - Siempre AGREGA al final del archivo (no borra lo anterior)
 * - Registra fecha, hora, clase Java, método y descripción de la operación
 * - También registra errores con el detalle de la excepción
 *
 * Ubicación del archivo:
 * - Si corre bajo Tomcat: {catalina.base}/log/biblioweb.log
 * - Si no: {user.dir}/log/biblioweb.log
 *
 * Formato de cada línea:
 *   2026-08-13 08:40:15 | LibroDAO.java | insertar | INSERT ejecutado OK - titulo=Clean Code
 */
public final class LoggerUtil {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Object LOCK = new Object();
    private static File archivoLog;

    // Constructor privado: esta clase solo tiene métodos estáticos
    private LoggerUtil() {
    }

    /**
     * Obtiene (y crea si no existe) el archivo de log.
     * La carpeta "log" se crea automáticamente.
     */
    private static File obtenerArchivoLog() {
        if (archivoLog != null) {
            return archivoLog;
        }
        // Preferir la carpeta base de Tomcat; si no existe, usar el directorio de trabajo
        String base = System.getProperty("catalina.base");
        if (base == null || base.isBlank()) {
            base = System.getProperty("user.dir");
        }
        File carpeta = new File(base, "log");
        if (!carpeta.exists()) {
            carpeta.mkdirs(); // crea la carpeta log si no existe
        }
        archivoLog = new File(carpeta, "biblioweb.log");
        return archivoLog;
    }

    /**
     * Registra un evento normal (SELECT, INSERT, UPDATE, DELETE, etc.).
     *
     * @param clase   nombre del archivo .java (ej: "LibroDAO.java")
     * @param metodo  nombre del método que se está ejecutando
     * @param mensaje descripción de la operación de base de datos
     */
    public static void info(String clase, String metodo, String mensaje) {
        escribir("INFO", clase, metodo, mensaje, null);
    }

    /**
     * Registra un error (fallo de base de datos, excepción, etc.).
     *
     * @param clase   nombre del archivo .java
     * @param metodo  nombre del método donde ocurrió el error
     * @param mensaje descripción breve del error
     * @param ex      la excepción (puede ser null)
     */
    public static void error(String clase, String metodo, String mensaje, Exception ex) {
        escribir("ERROR", clase, metodo, mensaje, ex);
    }

    /**
     * Escribe una línea en el archivo de log (modo append = no borra lo anterior).
     * Está sincronizado para que varios hilos no escriban al mismo tiempo.
     */
    private static void escribir(String nivel, String clase, String metodo, String mensaje, Exception ex) {
        synchronized (LOCK) {
            try {
                File log = obtenerArchivoLog();
                // true = append (agregar al final sin borrar)
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(log, true))) {
                    String linea = String.format("%s | %s | %s | %s | %s",
                            LocalDateTime.now().format(FORMATO),
                            clase,
                            metodo,
                            nivel,
                            mensaje);
                    bw.write(linea);
                    bw.newLine();

                    // Si hay excepción, agregar el detalle debajo
                    if (ex != null) {
                        StringWriter sw = new StringWriter();
                        ex.printStackTrace(new PrintWriter(sw));
                        bw.write("    → " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                        bw.newLine();
                    }
                }
            } catch (IOException e) {
                // Si falla el log, al menos mostrar en consola del servidor
                System.err.println("[LoggerUtil] No se pudo escribir en el log: " + e.getMessage());
            }
        }
    }
}
