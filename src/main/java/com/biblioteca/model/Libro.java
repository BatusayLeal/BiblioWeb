package com.biblioteca.model;

import java.io.Serializable;

/**
 * Modelo (POJO) que representa un libro de la biblioteca.
 *
 * Cada instancia de esta clase corresponde a una fila de la tabla "libro".
 * Los Servlets y las JSP trabajan con objetos Libro, nunca con SQL directo.
 */
public class Libro implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;              // Clave primaria
    private String titulo;       // Título del libro
    private String autor;        // Autor
    private String isbn;         // Código ISBN (único)
    private int anio;            // Año de publicación
    private String editorial;    // Editorial
    private boolean disponible;  // true = se puede prestar

    /** Constructor vacío. */
    public Libro() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}
