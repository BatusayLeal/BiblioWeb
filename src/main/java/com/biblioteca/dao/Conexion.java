package com.biblioteca.dao;

import com.biblioteca.util.LoggerUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase que gestiona la conexión a la base de datos MySQL.
 *
 * Usa el patrón de diseño SINGLETON:
 * - Solo existe UNA instancia de esta clase en toda la aplicación.
 * - Así se evita abrir muchas conexiones innecesarias.
 *
 * Cómo se usa desde otras clases:
 *   Connection conn = Conexion.getInstancia().getConexion();
 */
public class Conexion {

    // La única instancia de esta clase (Singleton)
    private static Conexion instancia = null;

    // La conexión JDBC real hacia MySQL
    private Connection conexion = null;

    // Datos de conexión (cámbialos según tu entorno local)
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL =
            "jdbc:mysql://localhost:3306/biblioteca_untec?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "user_bliblio";
    private static final String PASSWORD = "Biblio1234-";

    /**
     * Constructor PRIVADO.
     * Nadie puede hacer "new Conexion()" desde fuera.
     * Solo se llama internamente la primera vez que se necesita.
     */
    private Conexion() {
        try {
            // Carga el driver de MySQL en memoria
            Class.forName(DRIVER);
            // Abre la conexión con usuario y contraseña
            this.conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            LoggerUtil.info("Conexion.java", "Conexion()", "Conexión a MySQL establecida correctamente");
                } catch (ClassNotFoundException e) {
            LoggerUtil.error("Conexion.java", "Conexion()", "Driver MySQL no encontrado", e);
                } catch (SQLException e) {
            LoggerUtil.error("Conexion.java", "Conexion()", "Error al conectar con MySQL", e);
        }
    }

    /**
     * Devuelve la única instancia de Conexion (Singleton).
     * Si todavía no existe, la crea.
     * "synchronized" evita que dos hilos creen dos instancias al mismo tiempo.
     */
    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    /**
     * Devuelve la conexión activa.
     * Si la conexión se cerró, intenta reconectar automáticamente.
     */
    public Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
                LoggerUtil.info("Conexion.java", "getConexion()", "Reconexión a MySQL realizada");
            }
        } catch (SQLException e) {
            LoggerUtil.error("Conexion.java", "getConexion()", "Error al obtener/reconectar", e);
        }
        return conexion;
    }
}
