package com.biblioteca.model;

import java.io.Serializable;

/**
 * Modelo (POJO) que representa a un usuario del sistema.
 *
 * Un POJO (Plain Old Java Object) es una clase simple que solo tiene:
 * - atributos (datos)
 * - constructores
 * - getters y setters
 *
 * No contiene lógica de base de datos ni de servlets.
 * Se usa para transportar información entre las capas (DAO ↔ Servlet ↔ JSP).
 *
 * Implements Serializable: permite que el objeto se guarde en la sesión HTTP.
 */
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;           // Clave primaria en la tabla usuario
    private String nombre;    // Nombre completo
    private String email;     // Correo (único en la BD)
    private String password;  // Contraseña (texto plano solo para aprendizaje)
    private String rol;       // "ADMIN" o "ESTUDIANTE"
    private boolean activo;   // true = puede iniciar sesión

    /** Constructor vacío: necesario para frameworks y para crear el objeto paso a paso. */
    public Usuario() {}

    // --- Getters y Setters ---
    // Permiten leer y modificar los atributos de forma controlada (encapsulación)

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /**
     * Método de ayuda: indica si este usuario es administrador.
     * Se usa en las JSP y Servlets para mostrar u ocultar menús.
     */
    public boolean esAdmin() {
        return "ADMIN".equalsIgnoreCase(this.rol);
    }
}
