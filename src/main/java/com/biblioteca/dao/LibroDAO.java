package com.biblioteca.dao;

import com.biblioteca.model.Libro;
import com.biblioteca.util.LoggerUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) de Libro.
 *
 * Encapsula todas las operaciones SQL sobre la tabla "libro":
 * listar, buscar, insertar, actualizar, cambiar disponibilidad y eliminar.
 *
 * El parámetro de ordenamiento se valida con una lista blanca (whitelist)
 * para evitar SQL Injection en la cláusula ORDER BY.
 */
public class LibroDAO {

    private Connection conn;

    /**
     * Constructor: obtiene la conexión Singleton a MySQL.
     */
    public LibroDAO() {
        this.conn = Conexion.getInstancia().getConexion();
    }

    /**
     * Lista libros con filtro opcional de disponibilidad y ordenamiento dinámico.
     *
     * @param soloDisponibles true = solo libros disponibles
     * @param orden           campo de orden: titulo | autor | anio
     * @param dir             dirección: asc | desc
     * @return lista de libros (nunca null)
     */
    public List<Libro> listar(boolean soloDisponibles, String orden, String dir) {
        List<Libro> lista = new ArrayList<>();

        // Whitelist: solo se permiten estas columnas (protege contra SQL Injection)
        String columna;
        switch (orden == null ? "" : orden.toLowerCase()) {
            case "autor": columna = "autor"; break;
            case "anio":  columna = "anio";  break;
            default:      columna = "titulo"; break;
        }
        String direccion = "desc".equalsIgnoreCase(dir) ? "DESC" : "ASC";

        StringBuilder sql = new StringBuilder(
            "SELECT id, titulo, autor, isbn, anio, editorial, disponible FROM libro"
        );
        if (soloDisponibles) {
            sql.append(" WHERE disponible = 1");
        }
        sql.append(" ORDER BY ").append(columna).append(" ").append(direccion);

        try (PreparedStatement ps = conn.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            LoggerUtil.info("LibroDAO.java", "listar",
                    "SELECT listar OK - soloDisponibles=" + soloDisponibles
                    + " | orden=" + columna + " " + direccion
                    + " | cantidad=" + lista.size());
        } catch (SQLException e) {
            LoggerUtil.error("LibroDAO.java", "listar", "ERROR en SELECT listar", e);
        }
        return lista;
    }

    /** Atajo: lista todos los libros ordenados por título. */
    public List<Libro> listarTodos() {
        return listar(false, "titulo", "asc");
    }

    /** Atajo: lista solo los libros disponibles. */
    public List<Libro> listarDisponibles() {
        return listar(true, "titulo", "asc");
    }

    /**
     * Busca un libro por su ID.
     *
     * @param id clave primaria del libro
     * @return el Libro o null si no existe
     */
    public Libro buscarPorId(int id) {
        String sql = "SELECT id, titulo, autor, isbn, anio, editorial, disponible FROM libro WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LoggerUtil.info("LibroDAO.java", "buscarPorId", "SELECT libro id=" + id + " OK");
                    return mapear(rs);
                }
            }
            LoggerUtil.info("LibroDAO.java", "buscarPorId", "SELECT libro id=" + id + " - no encontrado");
        } catch (SQLException e) {
            LoggerUtil.error("LibroDAO.java", "buscarPorId", "ERROR SELECT libro id=" + id, e);
        }
        return null;
    }

    /**
     * Inserta un nuevo libro.
     *
     * @param libro datos del libro a guardar
     * @return ID generado por MySQL, o -1 si falló
     */
    public int insertar(Libro libro) {
        String sql = "INSERT INTO libro (titulo, autor, isbn, anio, editorial, disponible) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getIsbn());
            ps.setInt(4, libro.getAnio());
            ps.setString(5, libro.getEditorial());
            ps.setBoolean(6, libro.isDisponible());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        LoggerUtil.info("LibroDAO.java", "insertar",
                                "INSERT libro OK - id=" + id + " | titulo=" + libro.getTitulo());
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("LibroDAO.java", "insertar",
                    "ERROR INSERT libro titulo=" + libro.getTitulo(), e);
        }
        return -1;
    }

    /**
     * Actualiza los datos de un libro existente (título, autor, isbn, año, editorial).
     * No modifica el campo "disponible" (eso se hace con actualizarDisponibilidad).
     *
     * @param libro objeto con id y nuevos valores
     * @return true si se actualizó
     */
    public boolean actualizar(Libro libro) {
        String sql = "UPDATE libro SET titulo=?, autor=?, isbn=?, anio=?, editorial=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getIsbn());
            ps.setInt(4, libro.getAnio());
            ps.setString(5, libro.getEditorial());
            ps.setInt(6, libro.getId());
            boolean ok = ps.executeUpdate() > 0;
            LoggerUtil.info("LibroDAO.java", "actualizar",
                    "UPDATE libro id=" + libro.getId() + " | ok=" + ok + " | titulo=" + libro.getTitulo());
            return ok;
        } catch (SQLException e) {
            LoggerUtil.error("LibroDAO.java", "actualizar",
                    "ERROR UPDATE libro id=" + libro.getId(), e);
        }
        return false;
    }

    /**
     * Cambia solo el estado de disponibilidad de un libro
     * (true = disponible, false = prestado).
     * Se usa al prestar o devolver un libro.
     */
    public boolean actualizarDisponibilidad(int idLibro, boolean disponible) {
        String sql = "UPDATE libro SET disponible = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, disponible);
            ps.setInt(2, idLibro);
            boolean ok = ps.executeUpdate() > 0;
            LoggerUtil.info("LibroDAO.java", "actualizarDisponibilidad",
                    "UPDATE disponibilidad id=" + idLibro + " | disponible=" + disponible + " | ok=" + ok);
            return ok;
        } catch (SQLException e) {
            LoggerUtil.error("LibroDAO.java", "actualizarDisponibilidad",
                    "ERROR UPDATE disponibilidad id=" + idLibro, e);
        }
        return false;
    }

    /**
     * Elimina un libro por ID.
     * Puede fallar si hay préstamos asociados (restricción de clave foránea).
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM libro WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            LoggerUtil.info("LibroDAO.java", "eliminar", "DELETE libro id=" + id + " | ok=" + ok);
            return ok;
        } catch (SQLException e) {
            LoggerUtil.error("LibroDAO.java", "eliminar",
                    "ERROR DELETE libro id=" + id + " (posible FK con prestamo)", e);
        }
        return false;
    }

    /**
     * Convierte una fila del ResultSet en un objeto Libro.
     * Método privado de apoyo para no repetir código.
     */
    private Libro mapear(ResultSet rs) throws SQLException {
        Libro l = new Libro();
        l.setId(rs.getInt("id"));
        l.setTitulo(rs.getString("titulo"));
        l.setAutor(rs.getString("autor"));
        l.setIsbn(rs.getString("isbn"));
        l.setAnio(rs.getInt("anio"));
        l.setEditorial(rs.getString("editorial"));
        l.setDisponible(rs.getBoolean("disponible"));
        return l;
    }
}
