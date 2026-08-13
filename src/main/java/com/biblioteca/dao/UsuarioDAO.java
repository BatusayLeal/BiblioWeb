package com.biblioteca.dao;

import com.biblioteca.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private Connection conn;

    public UsuarioDAO() {
        this.conn = Conexion.getInstancia().getConexion();
    }

    public Usuario login(String email, String password) {
        String sql = "SELECT id, nombre, email, password, rol, activo FROM usuario WHERE email = ? AND password = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
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

    public Usuario buscarPorId(int id) {
        String sql = "SELECT id, nombre, email, password, rol, activo FROM usuario WHERE id = ?";
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

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, email, password, rol, activo FROM usuario ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

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
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean actualizar(Usuario usuario) {
        // Si la password viene vacía, no se actualiza
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
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean eliminar(int id) {
        // No eliminar si tiene préstamos activos
        String check = "SELECT COUNT(*) FROM prestamo WHERE id_usuario = ? AND estado = 'ACTIVO'";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // tiene préstamos activos
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String sql = "DELETE FROM usuario WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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
