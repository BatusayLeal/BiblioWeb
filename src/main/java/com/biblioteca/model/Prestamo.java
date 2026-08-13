package com.biblioteca.model;

import java.io.Serializable;
import java.sql.Date;

/**
 * Modelo (POJO) que representa un préstamo de un libro a un usuario.
 *
 * Relaciona:
 * - un Usuario (quién pidió el libro)
 * - un Libro (qué libro se prestó)
 *
 * El campo "estado" puede ser:
 * - "ACTIVO"   → el libro está prestado
 * - "DEVUELTO" → el libro ya fue devuelto
 *
 * Los objetos usuario y libro se rellenan con JOIN en el DAO
 * para poder mostrar nombre/título en las pantallas.
 */
public class Prestamo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int idUsuario;
    private int idLibro;
    private Date fechaPrestamo;
    private Date fechaDevolucion;  // null mientras esté activo
    private String estado;         // ACTIVO | DEVUELTO

    // Objetos relacionados (opcionales, se cargan en las consultas con JOIN)
    private Usuario usuario;
    private Libro libro;

    /** Constructor vacío. */
    public Prestamo() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdLibro() { return idLibro; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }

    public Date getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(Date fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public Date getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(Date fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    /** Indica si el préstamo sigue activo (el libro aún no se devolvió). */
    public boolean esActivo() {
        return "ACTIVO".equalsIgnoreCase(this.estado);
    }
}
