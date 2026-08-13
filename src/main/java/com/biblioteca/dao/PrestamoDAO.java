package com.biblioteca.dao;

import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    private Connection conn;
    private LibroDAO libroDAO;

    public PrestamoDAO() {
        this.conn = Conexion.getInstancia().getConexion();
        this.libroDAO = new LibroDAO();
    }

    public boolean prestar(int idUsuario, int idLibro) {
        Libro libro = libroDAO.buscarPorId(idLibro);
        if (libro == null || !libro.isDisponible()) {
            return false;
        }

        String sql = "INSERT INTO prestamo (id_usuario, id_libro, fecha_prestamo, estado) VALUES (?, ?, CURDATE(), 'ACTIVO')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idLibro);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                libroDAO.actualizarDisponibilidad(idLibro, false);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean devolver(int idPrestamo) {
        Prestamo p = buscarPorId(idPrestamo);
        if (p == null || !p.esActivo()) {
            return false;
        }

        String sql = "UPDATE prestamo SET estado = 'DEVUELTO', fecha_devolucion = CURDATE() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                libroDAO.actualizarDisponibilidad(p.getIdLibro(), true);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Prestamo buscarPorId(int id) {
        String sql = "SELECT id, id_usuario, id_libro, fecha_prestamo, fecha_devolucion, estado FROM prestamo WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearBasico(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Prestamo> listarPorUsuario(int idUsuario) {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.id_usuario, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.estado, " +
                     "l.titulo, l.autor, l.isbn " +
                     "FROM prestamo p INNER JOIN libro l ON p.id_libro = l.id " +
                     "WHERE p.id_usuario = ? ORDER BY p.fecha_prestamo DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prestamo p = mapearBasico(rs);
                    Libro l = new Libro();
                    l.setId(rs.getInt("id_libro"));
                    l.setTitulo(rs.getString("titulo"));
                    l.setAutor(rs.getString("autor"));
                    l.setIsbn(rs.getString("isbn"));
                    p.setLibro(l);
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Prestamo> listarTodos() {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.id_usuario, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.estado, " +
                     "u.nombre AS nombre_usuario, u.email, l.titulo, l.autor " +
                     "FROM prestamo p " +
                     "INNER JOIN usuario u ON p.id_usuario = u.id " +
                     "INNER JOIN libro l ON p.id_libro = l.id " +
                     "ORDER BY p.fecha_prestamo DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Prestamo p = mapearBasico(rs);

                Usuario u = new Usuario();
                u.setId(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre_usuario"));
                u.setEmail(rs.getString("email"));
                p.setUsuario(u);

                Libro l = new Libro();
                l.setId(rs.getInt("id_libro"));
                l.setTitulo(rs.getString("titulo"));
                l.setAutor(rs.getString("autor"));
                p.setLibro(l);

                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Prestamo mapearBasico(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setId(rs.getInt("id"));
        p.setIdUsuario(rs.getInt("id_usuario"));
        p.setIdLibro(rs.getInt("id_libro"));
        p.setFechaPrestamo(rs.getDate("fecha_prestamo"));
        p.setFechaDevolucion(rs.getDate("fecha_devolucion"));
        p.setEstado(rs.getString("estado"));
        return p;
    }
}
