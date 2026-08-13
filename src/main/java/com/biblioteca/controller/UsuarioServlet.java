package com.biblioteca.controller;

import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.model.Usuario;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Gestión de usuarios - SOLO ADMIN.
 * Listar, crear, editar y eliminar usuarios.
 */
@WebServlet("/usuarios")
public class UsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UsuarioDAO usuarioDAO;

    @Override
    public void init() throws ServletException {
        usuarioDAO = new UsuarioDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        Usuario actual = (Usuario) session.getAttribute("usuario");
        if (!actual.esAdmin()) {
            session.setAttribute("mensaje", "Acceso denegado. Solo administradores.");
            session.setAttribute("tipoMensaje", "danger");
            response.sendRedirect(request.getContextPath() + "/libros");
            return;
        }

        List<Usuario> usuarios = usuarioDAO.listarTodos();
        request.setAttribute("usuarios", usuarios);
        request.getRequestDispatcher("/usuarios.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        Usuario actual = (Usuario) session.getAttribute("usuario");
        if (!actual.esAdmin()) {
            session.setAttribute("mensaje", "Acceso denegado.");
            session.setAttribute("tipoMensaje", "danger");
            response.sendRedirect(request.getContextPath() + "/libros");
            return;
        }

        String action = request.getParameter("action");

        if ("agregar".equals(action)) {
            String nombre = request.getParameter("nombre");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String rol = request.getParameter("rol");

            if (nombre == null || nombre.isBlank() || email == null || email.isBlank()
                    || password == null || password.isBlank()) {
                session.setAttribute("mensaje", "Nombre, email y contraseña son obligatorios.");
                session.setAttribute("tipoMensaje", "warning");
            } else if (usuarioDAO.emailExiste(email.trim(), null)) {
                session.setAttribute("mensaje", "El email ya está registrado.");
                session.setAttribute("tipoMensaje", "warning");
            } else {
                // Solo permitir roles válidos
                if (!"ADMIN".equalsIgnoreCase(rol) && !"ESTUDIANTE".equalsIgnoreCase(rol)) {
                    rol = "ESTUDIANTE";
                }

                Usuario u = new Usuario();
                u.setNombre(nombre.trim());
                u.setEmail(email.trim());
                u.setPassword(password);
                u.setRol(rol.toUpperCase());
                u.setActivo(true);

                int id = usuarioDAO.insertar(u);
                if (id > 0) {
                    session.setAttribute("mensaje", "Usuario creado correctamente (ID: " + id + ").");
                    session.setAttribute("tipoMensaje", "success");
                } else {
                    session.setAttribute("mensaje", "Error al crear el usuario.");
                    session.setAttribute("tipoMensaje", "danger");
                }
            }

        } else if ("editar".equals(action)) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                String nombre = request.getParameter("nombre");
                String email = request.getParameter("email");
                String password = request.getParameter("password"); // puede venir vacío
                String rol = request.getParameter("rol");
                String activoStr = request.getParameter("activo");

                if (nombre == null || nombre.isBlank() || email == null || email.isBlank()) {
                    session.setAttribute("mensaje", "Nombre y email son obligatorios.");
                    session.setAttribute("tipoMensaje", "warning");
                } else if (usuarioDAO.emailExiste(email.trim(), id)) {
                    session.setAttribute("mensaje", "El email ya pertenece a otro usuario.");
                    session.setAttribute("tipoMensaje", "warning");
                } else {
                    if (!"ADMIN".equalsIgnoreCase(rol) && !"ESTUDIANTE".equalsIgnoreCase(rol)) {
                        rol = "ESTUDIANTE";
                    }

                    Usuario u = new Usuario();
                    u.setId(id);
                    u.setNombre(nombre.trim());
                    u.setEmail(email.trim());
                    u.setPassword(password); // si está vacío, el DAO no la cambia
                    u.setRol(rol.toUpperCase());
                    u.setActivo("1".equals(activoStr) || "on".equalsIgnoreCase(activoStr) || "true".equalsIgnoreCase(activoStr));

                    // No permitir que el admin se desactive a sí mismo
                    if (id == actual.getId() && !u.isActivo()) {
                        session.setAttribute("mensaje", "No puede desactivar su propia cuenta.");
                        session.setAttribute("tipoMensaje", "warning");
                    } else if (usuarioDAO.actualizar(u)) {
                        session.setAttribute("mensaje", "Usuario actualizado correctamente.");
                        session.setAttribute("tipoMensaje", "success");
                    } else {
                        session.setAttribute("mensaje", "Error al actualizar el usuario.");
                        session.setAttribute("tipoMensaje", "danger");
                    }
                }
            } catch (NumberFormatException e) {
                session.setAttribute("mensaje", "ID de usuario inválido.");
                session.setAttribute("tipoMensaje", "danger");
            }

        } else if ("eliminar".equals(action)) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));

                // No permitir eliminarse a sí mismo
                if (id == actual.getId()) {
                    session.setAttribute("mensaje", "No puede eliminar su propia cuenta.");
                    session.setAttribute("tipoMensaje", "warning");
                } else if (usuarioDAO.eliminar(id)) {
                    session.setAttribute("mensaje", "Usuario eliminado correctamente.");
                    session.setAttribute("tipoMensaje", "success");
                } else {
                    session.setAttribute("mensaje", "No se pudo eliminar (puede tener préstamos activos).");
                    session.setAttribute("tipoMensaje", "warning");
                }
            } catch (NumberFormatException e) {
                session.setAttribute("mensaje", "ID de usuario inválido.");
                session.setAttribute("tipoMensaje", "danger");
            }
        }

        response.sendRedirect(request.getContextPath() + "/usuarios");
    }
}
