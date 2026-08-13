package com.biblioteca.dao;

import com.biblioteca.model.Libro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    private Connection conn;

    public LibroDAO() {
        this.conn = Conexion.getInstancia().getConexion();
    }

    /**
     * Lista libros con ordenamiento dinámico.
     * @param soloDisponibles true = solo disponibles
     * @param orden campo: titulo | autor | anio
     * @param dir   dirección: asc | desc
     */
    public List<Libro> listar(boolean soloDisponibles, String orden, String dir) {
        List<Libro> lista = new ArrayList<>();

        // Validar columna permitida (evitar SQL injection)
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Libro> listarTodos() {
        return listar(false, "titulo", "asc");
    }

    public List<Libro> listarDisponibles() {
        return listar(true, "titulo", "asc");
    }

    public Libro buscarPorId(int id) {
        String sql = "SELECT id, titulo, autor, isbn, anio, editorial, disponible FROM libro WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean actualizar(Libro libro) {
        String sql = "UPDATE libro SET titulo=?, autor=?, isbn=?, anio=?, editorial=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getIsbn());
            ps.setInt(4, libro.getAnio());
            ps.setString(5, libro.getEditorial());
            ps.setInt(6, libro.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean actualizarDisponibilidad(int idLibro, boolean disponible) {
        String sql = "UPDATE libro SET disponible = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, disponible);
            ps.setInt(2, idLibro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM libro WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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
