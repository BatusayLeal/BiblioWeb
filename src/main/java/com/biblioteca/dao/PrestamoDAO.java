package com.biblioteca.dao;

import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import com.biblioteca.util.LoggerUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Prestamo.
 *
 * Gestiona el ciclo de vida de un préstamo:
 * - prestar: INSERT en prestamo + UPDATE libro.disponible = false
 * - devolver: UPDATE prestamo.estado = DEVUELTO + UPDATE libro.disponible = true
 * - listar por usuario o todos (con JOIN para mostrar título/autor)
 */
public class PrestamoDAO {

    private Connection conn;
    private LibroDAO libroDAO;

    /**
     * Constructor: obtiene conexión y crea el LibroDAO auxiliar
     * (necesario para cambiar la disponibilidad del libro).
     */
    public PrestamoDAO() {
        this.conn = Conexion.getInstancia().getConexion();
        this.libroDAO = new LibroDAO();
    }

    /**
     * Registra un nuevo préstamo.
     * Pasos:
     * 1. Verifica que el libro exista y esté disponible.
     * 2. INSERT en la tabla prestamo.
     * 3. Marca el libro como no disponible.
     *
     * @param idUsuario quien pide el libro
     * @param idLibro   libro solicitado
     * @return true si el préstamo se registró correctamente
     */
    public boolean prestar(int idUsuario, int idLibro) {
        Libro libro = libroDAO.buscarPorId(idLibro);
        if (libro == null || !libro.isDisponible()) {
            LoggerUtil.info("PrestamoDAO.java", "prestar",
                    "PRESTAR cancelado - libro id=" + idLibro + " no disponible o no existe");
            return false;
        }

        String sql = "INSERT INTO prestamo (id_usuario, id_libro, fecha_prestamo, estado) VALUES (?, ?, CURDATE(), 'ACTIVO')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idLibro);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                libroDAO.actualizarDisponibilidad(idLibro, false);
                LoggerUtil.info("PrestamoDAO.java", "prestar",
                        "INSERT prestamo OK - usuario=" + idUsuario + " | libro=" + idLibro);
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.error("PrestamoDAO.java", "prestar",
                    "ERROR INSERT prestamo usuario=" + idUsuario + " libro=" + idLibro, e);
        }
        return false;
    }

    /**
     * Registra la devolución de un préstamo.
     * Pasos:
     * 1. Busca el préstamo y verifica que esté ACTIVO.
     * 2. UPDATE estado = DEVUELTO y fecha_devolucion = hoy.
     * 3. Marca el libro como disponible otra vez.
     *
     * @param idPrestamo identificador del préstamo a devolver
     * @return true si se registró la devolución
     */
    public boolean devolver(int idPrestamo) {
        Prestamo p = buscarPorId(idPrestamo);
        if (p == null || !p.esActivo()) {
            LoggerUtil.info("PrestamoDAO.java", "devolver",
                    "DEVOLVER cancelado - prestamo id=" + idPrestamo + " no existe o ya devuelto");
            return false;
        }

        String sql = "UPDATE prestamo SET estado = 'DEVUELTO', fecha_devolucion = CURDATE() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                libroDAO.actualizarDisponibilidad(p.getIdLibro(), true);
                LoggerUtil.info("PrestamoDAO.java", "devolver",
                        "UPDATE devolución OK - prestamo id=" + idPrestamo + " | libro=" + p.getIdLibro());
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.error("PrestamoDAO.java", "devolver",
                    "ERROR UPDATE devolución prestamo id=" + idPrestamo, e);
        }
        return false;
    }

    /**
     * Busca un préstamo por su ID.
     */
    public Prestamo buscarPorId(int id) {
        String sql = "SELECT id, id_usuario, id_libro, fecha_prestamo, fecha_devolucion, estado FROM prestamo WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LoggerUtil.info("PrestamoDAO.java", "buscarPorId", "SELECT prestamo id=" + id + " OK");
                    return mapearBasico(rs);
                }
            }
            LoggerUtil.info("PrestamoDAO.java", "buscarPorId", "SELECT prestamo id=" + id + " - no encontrado");
        } catch (SQLException e) {
            LoggerUtil.error("PrestamoDAO.java", "buscarPorId", "ERROR SELECT prestamo id=" + id, e);
        }
        return null;
    }

    /**
     * Lista los préstamos de un usuario concreto.
     * Usa INNER JOIN con la tabla libro para traer título y autor en la misma consulta.
     */
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
            LoggerUtil.info("PrestamoDAO.java", "listarPorUsuario",
                    "SELECT prestamos usuario=" + idUsuario + " | cantidad=" + lista.size());
        } catch (SQLException e) {
            LoggerUtil.error("PrestamoDAO.java", "listarPorUsuario",
                    "ERROR SELECT prestamos usuario=" + idUsuario, e);
        }
        return lista;
    }

    /**
     * Lista TODOS los préstamos del sistema (vista de administrador).
     * JOIN con usuario y libro para mostrar nombres completos.
     */
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
            LoggerUtil.info("PrestamoDAO.java", "listarTodos",
                    "SELECT todos los prestamos OK - cantidad=" + lista.size());
        } catch (SQLException e) {
            LoggerUtil.error("PrestamoDAO.java", "listarTodos", "ERROR SELECT listarTodos", e);
        }
        return lista;
    }

    /**
     * Convierte las columnas básicas de un ResultSet en un objeto Prestamo.
     */
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
