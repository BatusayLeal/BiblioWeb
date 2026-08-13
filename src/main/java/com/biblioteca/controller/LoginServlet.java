package com.biblioteca.controller;

import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.model.Usuario;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet controlador del login.
 *
 * En el patrón MVC, un Servlet actúa como CONTROLADOR:
 * 1. Recibe la petición HTTP del navegador (GET o POST)
 * 2. Llama al DAO para obtener/validar datos
 * 3. Guarda el resultado en la sesión o request
 * 4. Redirige o reenvía a una JSP (la VISTA)
 *
 * @WebServlet("/login") indica la URL que activa este servlet:
 *   http://localhost:8080/BiblioWeb/login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UsuarioDAO usuarioDAO;

    /**
     * init() se ejecuta UNA sola vez cuando el servlet se carga en Tomcat.
     * Aquí se crea el DAO que se reutilizará en cada petición.
     */
    @Override
    public void init() throws ServletException {
        usuarioDAO = new UsuarioDAO();
    }

    /**
     * doGet: cuando el usuario abre /login en el navegador.
     * Si ya hay sesión activa, lo manda al catálogo.
     * Si no, muestra el formulario de login (index.jsp).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuario") != null) {
            response.sendRedirect(request.getContextPath() + "/libros");
            return;
        }
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    /**
     * doPost: cuando el usuario envía el formulario de login.
     * 1. Lee email y password del formulario
     * 2. Llama a UsuarioDAO.login()
     * 3. Si es correcto → crea HttpSession y redirige al catálogo
     * 4. Si falla → vuelve a index.jsp con mensaje de error
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Debe ingresar email y contraseña.");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return;
        }

        Usuario usuario = usuarioDAO.login(email.trim(), password);

        if (usuario != null) {
            // Crear sesión HTTP: el servidor "recuerda" al usuario entre peticiones
            HttpSession session = request.getSession(true);
            session.setAttribute("usuario", usuario);
            session.setMaxInactiveInterval(30 * 60); // 30 minutos de inactividad
            response.sendRedirect(request.getContextPath() + "/libros");
        } else {
            request.setAttribute("error", "Credenciales incorrectas.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
}
