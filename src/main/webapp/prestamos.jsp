<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="titulo" value="Préstamos"/>
</jsp:include>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h2 class="mb-0">
        <i class="bi bi-arrow-left-right me-2"></i>
        <c:choose>
            <c:when test="${vistaAdmin}">Todos los Préstamos</c:when>
            <c:otherwise>Mis Préstamos</c:otherwise>
        </c:choose>
    </h2>
    <c:if test="${sessionScope.usuario.esAdmin()}">
        <div class="btn-group">
            <a href="${pageContext.request.contextPath}/prestamos" 
               class="btn btn-outline-primary ${!vistaAdmin ? 'active' : ''}">Mis préstamos</a>
            <a href="${pageContext.request.contextPath}/prestamos?vista=todos" 
               class="btn btn-outline-secondary ${vistaAdmin ? 'active' : ''}">Todos</a>
        </div>
    </c:if>
</div>

<div class="card">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover mb-0 align-middle">
                <thead>
                    <tr>
                        <th>#</th>
                        <c:if test="${vistaAdmin}"><th>Usuario</th></c:if>
                        <th>Libro</th>
                        <th>Fecha préstamo</th>
                        <th>Fecha devolución</th>
                        <th>Estado</th>
                        <th class="text-center">Acción</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty prestamos}">
                            <tr>
                                <td colspan="${vistaAdmin ? 7 : 6}" class="text-center text-muted py-5">
                                    <i class="bi bi-inbox" style="font-size: 2rem;"></i><br>
                                    No hay préstamos registrados.
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="p" items="${prestamos}">
                                <tr>
                                    <td><c:out value="${p.id}"/></td>
                                    <c:if test="${vistaAdmin}">
                                        <td>
                                            <c:out value="${p.usuario.nombre}"/><br>
                                            <small class="text-muted"><c:out value="${p.usuario.email}"/></small>
                                        </td>
                                    </c:if>
                                    <td>
                                        <strong><c:out value="${p.libro.titulo}"/></strong><br>
                                        <small class="text-muted"><c:out value="${p.libro.autor}"/></small>
                                    </td>
                                    <td>
                                        <fmt:formatDate value="${p.fechaPrestamo}" pattern="dd/MM/yyyy"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty p.fechaDevolucion}">
                                                <fmt:formatDate value="${p.fechaDevolucion}" pattern="dd/MM/yyyy"/>
                                            </c:when>
                                            <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.estado == 'ACTIVO'}">
                                                <span class="badge bg-warning text-dark rounded-pill px-3">Activo</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-success rounded-pill px-3">Devuelto</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <c:if test="${p.estado == 'ACTIVO'}">
                                            <form action="${pageContext.request.contextPath}/prestamos" method="post" class="d-inline">
                                                <input type="hidden" name="action" value="devolver">
                                                <input type="hidden" name="idPrestamo" value="${p.id}">
                                                <button type="submit" class="btn btn-sm btn-success"
                                                        onclick="return confirm('¿Confirmar devolución?');">
                                                    <i class="bi bi-check2-circle"></i> Devolver
                                                </button>
                                            </form>
                                        </c:if>
                                        <c:if test="${p.estado != 'ACTIVO'}">
                                            <span class="text-muted small">—</span>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<p class="text-muted mt-3 small">
    Total: <strong><c:out value="${prestamos.size()}"/></strong> registros
</p>

<jsp:include page="/includes/footer.jsp"/>
