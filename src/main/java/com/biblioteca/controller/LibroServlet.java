package com.biblioteca.controller;

import com.biblioteca.dao.LibroDAO;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Usuario;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/libros")
public class LibroServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private LibroDAO libroDAO;

    @Override
    public void init() throws ServletException {
        libroDAO = new LibroDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String filtro = request.getParameter("filtro");
        String orden = request.getParameter("orden");   // titulo | autor | anio
        String dir   = request.getParameter("dir");     // asc | desc

        // Valores por defecto
        if (orden == null || orden.isBlank()) orden = "titulo";
        if (dir == null || dir.isBlank()) dir = "asc";

        boolean soloDisponibles = "disponibles".equalsIgnoreCase(filtro);
        List<Libro> libros = libroDAO.listar(soloDisponibles, orden, dir);

        request.setAttribute("filtroActivo", soloDisponibles ? "disponibles" : "todos");
        request.setAttribute("ordenActivo", orden.toLowerCase());
        request.setAttribute("dirActivo", dir.toLowerCase());
        request.setAttribute("libros", libros);
        request.getRequestDispatcher("/libros.jsp").forward(request, response);
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

        if (!usuario.esAdmin()) {
            session.setAttribute("mensaje", "No tiene permisos para esta acción.");
            session.setAttribute("tipoMensaje", "danger");
            response.sendRedirect(request.getContextPath() + "/libros");
            return;
        }

        if ("agregar".equals(action)) {
            String titulo = request.getParameter("titulo");
            String autor = request.getParameter("autor");
            String isbn = request.getParameter("isbn");
            String anioStr = request.getParameter("anio");
            String editorial = request.getParameter("editorial");

            if (titulo == null || titulo.trim().isEmpty() || autor == null || autor.trim().isEmpty()) {
                session.setAttribute("mensaje", "Título y autor son obligatorios.");
                session.setAttribute("tipoMensaje", "warning");
            } else {
                Libro libro = new Libro();
                libro.setTitulo(titulo.trim());
                libro.setAutor(autor.trim());
                libro.setIsbn(isbn != null ? isbn.trim() : null);
                try {
                    libro.setAnio(anioStr != null && !anioStr.isEmpty() ? Integer.parseInt(anioStr) : 0);
                } catch (NumberFormatException e) {
                    libro.setAnio(0);
                }
                libro.setEditorial(editorial != null ? editorial.trim() : null);
                libro.setDisponible(true);

                int id = libroDAO.insertar(libro);
                if (id > 0) {
                    session.setAttribute("mensaje", "Libro agregado correctamente (ID: " + id + ").");
                    session.setAttribute("tipoMensaje", "success");
                } else {
                    session.setAttribute("mensaje", "Error al agregar el libro.");
                    session.setAttribute("tipoMensaje", "danger");
                }
            }
        } else if ("editar".equals(action)) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                String titulo = request.getParameter("titulo");
                String autor = request.getParameter("autor");
                String isbn = request.getParameter("isbn");
                String anioStr = request.getParameter("anio");
                String editorial = request.getParameter("editorial");

                if (titulo == null || titulo.trim().isEmpty() || autor == null || autor.trim().isEmpty()) {
                    session.setAttribute("mensaje", "Título y autor son obligatorios.");
                    session.setAttribute("tipoMensaje", "warning");
                } else {
                    Libro libro = new Libro();
                    libro.setId(id);
                    libro.setTitulo(titulo.trim());
                    libro.setAutor(autor.trim());
                    libro.setIsbn(isbn != null ? isbn.trim() : null);
                    try {
                        libro.setAnio(anioStr != null && !anioStr.isEmpty() ? Integer.parseInt(anioStr) : 0);
                    } catch (NumberFormatException e) {
                        libro.setAnio(0);
                    }
                    libro.setEditorial(editorial != null ? editorial.trim() : null);

                    if (libroDAO.actualizar(libro)) {
                        session.setAttribute("mensaje", "Libro actualizado correctamente.");
                        session.setAttribute("tipoMensaje", "success");
                    } else {
                        session.setAttribute("mensaje", "Error al actualizar el libro.");
                        session.setAttribute("tipoMensaje", "danger");
                    }
                }
            } catch (NumberFormatException e) {
                session.setAttribute("mensaje", "ID de libro inválido.");
                session.setAttribute("tipoMensaje", "danger");
            }
        } else if ("eliminar".equals(action)) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                if (libroDAO.eliminar(id)) {
                    session.setAttribute("mensaje", "Libro eliminado correctamente.");
                    session.setAttribute("tipoMensaje", "success");
                } else {
                    session.setAttribute("mensaje", "No se pudo eliminar el libro.");
                    session.setAttribute("tipoMensaje", "warning");
                }
            } catch (NumberFormatException e) {
                session.setAttribute("mensaje", "ID de libro inválido.");
                session.setAttribute("tipoMensaje", "danger");
            }
        }

        response.sendRedirect(request.getContextPath() + "/libros");
    }
}
