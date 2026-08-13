package com.biblioteca.controller;

import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/prestamos")
public class PrestamoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private PrestamoDAO prestamoDAO;

    @Override
    public void init() throws ServletException {
        prestamoDAO = new PrestamoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        String vista = request.getParameter("vista");

        List<Prestamo> prestamos;
        if ("todos".equals(vista) && usuario.esAdmin()) {
            prestamos = prestamoDAO.listarTodos();
            request.setAttribute("vistaAdmin", true);
        } else {
            prestamos = prestamoDAO.listarPorUsuario(usuario.getId());
            request.setAttribute("vistaAdmin", false);
        }

        request.setAttribute("prestamos", prestamos);
        request.getRequestDispatcher("/prestamos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        String action = request.getParameter("action");

        if ("prestar".equals(action)) {
            try {
                int idLibro = Integer.parseInt(request.getParameter("idLibro"));
                boolean ok = prestamoDAO.prestar(usuario.getId(), idLibro);
                if (ok) {
                    session.setAttribute("mensaje", "¡Préstamo registrado exitosamente!");
                    session.setAttribute("tipoMensaje", "success");
                } else {
                    session.setAttribute("mensaje", "No se pudo realizar el préstamo.");
                    session.setAttribute("tipoMensaje", "danger");
                }
            } catch (NumberFormatException e) {
                session.setAttribute("mensaje", "ID de libro inválido.");
                session.setAttribute("tipoMensaje", "danger");
            }
            response.sendRedirect(request.getContextPath() + "/libros");
            return;

        } else if ("devolver".equals(action)) {
            try {
                int idPrestamo = Integer.parseInt(request.getParameter("idPrestamo"));
                Prestamo p = prestamoDAO.buscarPorId(idPrestamo);
                if (p != null && (p.getIdUsuario() == usuario.getId() || usuario.esAdmin())) {
                    boolean ok = prestamoDAO.devolver(idPrestamo);
                    if (ok) {
                        session.setAttribute("mensaje", "Devolución registrada correctamente.");
                        session.setAttribute("tipoMensaje", "success");
                    } else {
                        session.setAttribute("mensaje", "No se pudo registrar la devolución.");
                        session.setAttribute("tipoMensaje", "danger");
                    }
                } else {
                    session.setAttribute("mensaje", "No tiene permiso para esta acción.");
                    session.setAttribute("tipoMensaje", "warning");
                }
            } catch (NumberFormatException e) {
                session.setAttribute("mensaje", "ID de préstamo inválido.");
                session.setAttribute("tipoMensaje", "danger");
            }
            response.sendRedirect(request.getContextPath() + "/prestamos");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/prestamos");
    }
}
