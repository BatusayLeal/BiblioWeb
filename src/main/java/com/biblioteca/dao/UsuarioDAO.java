package com.biblioteca.dao;

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
 * DAO (Data Access Object) de Usuario.
 *
 * Esta clase se encarga de TODAS las operaciones de base de datos
 * relacionadas con la tabla "usuario":
 * - login (SELECT)
 * - listar, buscar (SELECT)
 * - insertar (INSERT)
 * - actualizar (UPDATE)
 * - eliminar (DELETE)
 *
 * Ningún Servlet habla directamente con SQL: siempre pasan por este DAO.
 * Así se separa la lógica de negocio de la lógica de datos (patrón DAO).
 */
public class UsuarioDAO {

    // Conexión reutilizada (obtenida del Singleton)
    private Connection conn;

    /**
     * Constructor: obtiene la conexión compartida a MySQL.
     */
    public UsuarioDAO() {
        this.conn = Conexion.getInstancia().getConexion();
    }

    /**
     * Intenta autenticar un usuario con email y contraseña.
     * Ejecuta un SELECT y devuelve el Usuario si las credenciales son correctas.
     *
     * @param email    correo del usuario
     * @param password contraseña en texto plano (solo educativo)
     * @return el Usuario encontrado, o null si no coincide
     */
    public Usuario login(String email, String password) {
        String sql = "SELECT id, nombre, email, password, rol, activo FROM usuario WHERE email = ? AND password = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Los "?" se reemplazan de forma segura (evita SQL Injection)
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = mapear(rs);
                    LoggerUtil.info("UsuarioDAO.java", "login",
                            "SELECT login OK - email=" + email + " | id=" + u.getId() + " | rol=" + u.getRol());
                    return u;
                }
            }
            LoggerUtil.info("UsuarioDAO.java", "login",
                    "SELECT login FALLIDO - email=" + email + " (credenciales incorrectas o inactivo)");
        } catch (SQLException e) {
            LoggerUtil.error("UsuarioDAO.java", "login", "ERROR en SELECT login - email=" + email, e);
        }
        return null;
    }

    /**
     * Busca un usuario por su ID (clave primaria).
     *
     * @param id identificador del usuario
     * @return el Usuario o null si no existe
     */
    public Usuario buscarPorId(int id) {
        String sql = "SELECT id, nombre, email, password, rol, activo FROM usuario WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LoggerUtil.info("UsuarioDAO.java", "buscarPorId", "SELECT por id=" + id + " OK");
                    return mapear(rs);
                }
            }
            LoggerUtil.info("UsuarioDAO.java", "buscarPorId", "SELECT por id=" + id + " - no encontrado");
        } catch (SQLException e) {
            LoggerUtil.error("UsuarioDAO.java", "buscarPorId", "ERROR SELECT id=" + id, e);
        }
        return null;
    }

    /**
     * Devuelve la lista completa de usuarios ordenados por nombre.
     *
     * @return lista de Usuario (puede estar vacía, nunca null)
     */
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, email, password, rol, activo FROM usuario ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            LoggerUtil.info("UsuarioDAO.java", "listarTodos",
                    "SELECT listar todos OK - cantidad=" + lista.size());
        } catch (SQLException e) {
            LoggerUtil.error("UsuarioDAO.java", "listarTodos", "ERROR en SELECT listarTodos", e);
        }
        return lista;
    }

    /**
     * Verifica si un email ya está registrado.
     * Útil antes de insertar o actualizar para no violar la restricción UNIQUE.
     *
     * @param email     email a comprobar
     * @param excluirId si no es null, ignora ese id (caso de edición del mismo usuario)
     * @return true si el email ya existe
     */
    public boolean emailExiste(String email, Integer excluirId) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE email = ?" +
                     (excluirId != null ? " AND id <> ?" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            if (excluirId != null) {
                ps.setInt(2, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean existe = rs.getInt(1) > 0;
                    LoggerUtil.info("UsuarioDAO.java", "emailExiste",
                            "SELECT emailExiste email=" + email + " | existe=" + existe);
                    return existe;
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("UsuarioDAO.java", "emailExiste", "ERROR SELECT emailExiste", e);
        }
        return false;
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param usuario objeto con nombre, email, password y rol
     * @return el ID generado por MySQL, o -1 si falló
     */
    public int insertar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, email, password, rol, activo) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getPassword());
            ps.setString(4, usuario.getRol());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        LoggerUtil.info("UsuarioDAO.java", "insertar",
                                "INSERT usuario OK - id=" + id + " | email=" + usuario.getEmail() + " | rol=" + usuario.getRol());
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("UsuarioDAO.java", "insertar",
                    "ERROR INSERT usuario email=" + usuario.getEmail(), e);
        }
        return -1;
    }

    /**
     * Actualiza los datos de un usuario existente.
     * Si la contraseña viene vacía, NO se modifica (se mantiene la anterior).
     *
     * @param usuario objeto con id y los nuevos valores
     * @return true si se actualizó al menos una fila
     */
    public boolean actualizar(Usuario usuario) {
        boolean cambiaPassword = usuario.getPassword() != null && !usuario.getPassword().isBlank();
        String sql = cambiaPassword
            ? "UPDATE usuario SET nombre=?, email=?, password=?, rol=?, activo=? WHERE id=?"
            : "UPDATE usuario SET nombre=?, email=?, rol=?, activo=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, usuario.getNombre());
            ps.setString(i++, usuario.getEmail());
            if (cambiaPassword) {
                ps.setString(i++, usuario.getPassword());
            }
            ps.setString(i++, usuario.getRol());
            ps.setBoolean(i++, usuario.isActivo());
            ps.setInt(i, usuario.getId());
            boolean ok = ps.executeUpdate() > 0;
            LoggerUtil.info("UsuarioDAO.java", "actualizar",
                    "UPDATE usuario id=" + usuario.getId() + " | ok=" + ok + " | cambioPassword=" + cambiaPassword);
            return ok;
        } catch (SQLException e) {
            LoggerUtil.error("UsuarioDAO.java", "actualizar",
                    "ERROR UPDATE usuario id=" + usuario.getId(), e);
        }
        return false;
    }

    /**
     * Elimina un usuario por ID.
     * Primero verifica que no tenga préstamos activos (para no romper integridad).
     *
     * @param id identificador del usuario a borrar
     * @return true si se eliminó, false si tiene préstamos activos o falló
     */
    public boolean eliminar(int id) {
        // Paso 1: comprobar préstamos activos
        String check = "SELECT COUNT(*) FROM prestamo WHERE id_usuario = ? AND estado = 'ACTIVO'";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    LoggerUtil.info("UsuarioDAO.java", "eliminar",
                            "DELETE cancelado - usuario id=" + id + " tiene préstamos activos");
                    return false;
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("UsuarioDAO.java", "eliminar", "ERROR al verificar préstamos id=" + id, e);
            return false;
        }

        // Paso 2: borrar
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            LoggerUtil.info("UsuarioDAO.java", "eliminar", "DELETE usuario id=" + id + " | ok=" + ok);
            return ok;
        } catch (SQLException e) {
            LoggerUtil.error("UsuarioDAO.java", "eliminar", "ERROR DELETE usuario id=" + id, e);
        }
        return false;
    }

    /**
     * Método privado auxiliar: convierte una fila del ResultSet en un objeto Usuario.
     * Evita repetir el mismo código en varios métodos.
     */
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRol(rs.getString("rol"));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }
}
