package com.biblioteca.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton de conexión JDBC a MySQL.
 * Ajusta USER y PASSWORD según tu instalación local.
 */
public class Conexion {

    private static Conexion instancia = null;
    private Connection conexion = null;

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/biblioteca_untec?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "user_bliblio";
    private static final String PASSWORD = "Biblio1234-";

    private Conexion() {
        try {
            Class.forName(DRIVER);
            this.conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[Conexion] Conexión a MySQL OK");
        } catch (ClassNotFoundException e) {
            System.err.println("[Conexion] Driver no encontrado");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[Conexion] Error de conexión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    public Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("[Conexion] Error al reconectar: " + e.getMessage());
            e.printStackTrace();
        }
        return conexion;
    }
}
