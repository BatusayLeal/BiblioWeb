package com.biblioteca.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
* Esta anotación indica que este Servlet responderá cuando el usuario
* acceda a la URL: /logout
* Por ejemplo:
* http://localhost:8080/biblioteca/logout
*/

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /* doGet()
    * Este método se ejecuta cuando el navegador realiza una petición HTTP GET
    * hacia /logout.
    * Su función principal es cerrar la sesión del usuario.
    * */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            /*
            * Destruye la sesión actual.
            * Al hacerlo, se eliminan los datos almacenados
            * en la sesión, por ejemplo:
            * usuario rol correo permisos
            * En otras palabras: EL USUARIO QUEDA DESLOGUEADO.
            * */
            session.invalidate();
        }
        /*
        * sendRedirect() Le indica al navegador que debe dirigirse a otra URL.
        * request.getContextPath() obtiene el contexto de nuestra aplicación.
        * Si nuestra aplicación se llama "BiblioWeb", podría devolver:
        * /biblioweb
        * Por lo tanto: request.getContextPath() + "/index.jsp"
        * quedaría: /biblioteca/index.jsp
        * Finalmente, el usuario vuelve a la página de inicio/login.
        */
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            /*
            * Ejecutamos exactamente la misma lógica utilizada
            * para una petición GET:
            * 1. Obtener la sesión. * 2. Invalidarla. * 3. Redirigir al index.jsp.
            */
            doGet(request, response);
    }
}
