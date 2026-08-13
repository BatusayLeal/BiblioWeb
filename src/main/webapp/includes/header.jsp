<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    if (session.getAttribute("usuario") == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${param.titulo} - BiblioWeb</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/estilos.css" rel="stylesheet">
</head>
<body class="d-flex flex-column min-vh-100">

<nav class="navbar navbar-expand-lg navbar-dark" style="background-color: #1a365d;">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/libros">
            <i class="bi bi-book-half me-2"></i>BiblioWeb UNTEC
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/libros">
                        <i class="bi bi-journal-bookmark me-1"></i>Catálogo
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/prestamos">
                        <i class="bi bi-arrow-left-right me-1"></i>Mis Préstamos
                    </a>
                </li>
                <c:if test="${sessionScope.usuario.esAdmin()}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/prestamos?vista=todos">
                            <i class="bi bi-list-check me-1"></i>Todos los Préstamos
                        </a>
                    </li>
                </c:if>
            </ul>
            <ul class="navbar-nav">
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                        <i class="bi bi-person-circle me-1"></i>
                        <c:out value="${sessionScope.usuario.nombre}"/>
                        <span class="badge bg-info text-dark ms-1">${sessionScope.usuario.rol}</span>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><a class="dropdown-item" href="${pageContext.request.contextPath}/logout">
                            <i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión
                        </a></li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</nav>

<c:if test="${not empty sessionScope.mensaje}">
    <div class="container mt-3">
        <div class="alert alert-${sessionScope.tipoMensaje} alert-dismissible fade show" role="alert">
            <c:out value="${sessionScope.mensaje}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </div>
    <c:remove var="mensaje" scope="session"/>
    <c:remove var="tipoMensaje" scope="session"/>
</c:if>

<main class="container my-4 flex-grow-1">
