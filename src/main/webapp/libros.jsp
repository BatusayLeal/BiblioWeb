<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="titulo" value="Catálogo de Libros"/>
</jsp:include>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h2 class="mb-0"><i class="bi bi-journal-bookmark me-2"></i>Catálogo de Libros</h2>
    <div class="btn-group">
        <a href="${pageContext.request.contextPath}/libros?orden=${ordenActivo}&dir=${dirActivo}" 
           class="btn btn-outline-primary ${filtroActivo == 'todos' ? 'active' : ''}">Todos</a>
        <a href="${pageContext.request.contextPath}/libros?filtro=disponibles&orden=${ordenActivo}&dir=${dirActivo}" 
           class="btn btn-outline-success ${filtroActivo == 'disponibles' ? 'active' : ''}">Disponibles</a>
    </div>
</div>

<c:if test="${sessionScope.usuario.esAdmin()}">
    <div class="form-agregar mb-4">
        <h5 class="mb-3"><i class="bi bi-plus-circle me-2"></i>Agregar nuevo libro</h5>
        <form action="${pageContext.request.contextPath}/libros" method="post" class="row g-3">
            <input type="hidden" name="action" value="agregar">
            <div class="col-md-4">
                <input type="text" class="form-control" name="titulo" placeholder="Título *" required>
            </div>
            <div class="col-md-3">
                <input type="text" class="form-control" name="autor" placeholder="Autor *" required>
            </div>
            <div class="col-md-2">
                <input type="text" class="form-control" name="isbn" placeholder="ISBN">
            </div>
            <div class="col-md-1">
                <input type="number" class="form-control" name="anio" placeholder="Año" min="1900" max="2030">
            </div>
            <div class="col-md-2">
                <button type="submit" class="btn btn-success w-100">
                    <i class="bi bi-plus-lg"></i> Agregar
                </button>
            </div>
        </form>
    </div>
</c:if>

<div class="card">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover mb-0 align-middle">
                <thead>
                    <tr>
                        <th style="width: 50px;">#</th>
                        <!-- Ordenar por Título -->
                        <th>
                            <c:set var="dirTitulo" value="${ordenActivo == 'titulo' and dirActivo == 'asc' ? 'desc' : 'asc'}"/>
                            <a href="${pageContext.request.contextPath}/libros?filtro=${filtroActivo}&orden=titulo&dir=${dirTitulo}"
                               class="text-white text-decoration-none">
                                Título
                                <c:if test="${ordenActivo == 'titulo'}">
                                    <i class="bi bi-caret-${dirActivo == 'asc' ? 'up' : 'down'}-fill"></i>
                                </c:if>
                                <c:if test="${ordenActivo != 'titulo'}">
                                    <i class="bi bi-arrow-down-up" style="opacity:0.5;font-size:0.8em;"></i>
                                </c:if>
                            </a>
                        </th>
                        <!-- Ordenar por Autor -->
                        <th>
                            <c:set var="dirAutor" value="${ordenActivo == 'autor' and dirActivo == 'asc' ? 'desc' : 'asc'}"/>
                            <a href="${pageContext.request.contextPath}/libros?filtro=${filtroActivo}&orden=autor&dir=${dirAutor}"
                               class="text-white text-decoration-none">
                                Autor
                                <c:if test="${ordenActivo == 'autor'}">
                                    <i class="bi bi-caret-${dirActivo == 'asc' ? 'up' : 'down'}-fill"></i>
                                </c:if>
                                <c:if test="${ordenActivo != 'autor'}">
                                    <i class="bi bi-arrow-down-up" style="opacity:0.5;font-size:0.8em;"></i>
                                </c:if>
                            </a>
                        </th>
                        <th>ISBN</th>
                        <!-- Ordenar por Año -->
                        <th>
                            <c:set var="dirAnio" value="${ordenActivo == 'anio' and dirActivo == 'asc' ? 'desc' : 'asc'}"/>
                            <a href="${pageContext.request.contextPath}/libros?filtro=${filtroActivo}&orden=anio&dir=${dirAnio}"
                               class="text-white text-decoration-none">
                                Año
                                <c:if test="${ordenActivo == 'anio'}">
                                    <i class="bi bi-caret-${dirActivo == 'asc' ? 'up' : 'down'}-fill"></i>
                                </c:if>
                                <c:if test="${ordenActivo != 'anio'}">
                                    <i class="bi bi-arrow-down-up" style="opacity:0.5;font-size:0.8em;"></i>
                                </c:if>
                            </a>
                        </th>
                        <th>Estado</th>
                        <th style="width: 180px;" class="text-center">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty libros}">
                            <tr>
                                <td colspan="7" class="text-center text-muted py-4">No hay libros para mostrar.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="libro" items="${libros}">
                                <tr>
                                    <td><c:out value="${libro.id}"/></td>
                                    <td>
                                        <strong><c:out value="${libro.titulo}"/></strong>
                                        <c:if test="${not empty libro.editorial}">
                                            <br><small class="text-muted"><c:out value="${libro.editorial}"/></small>
                                        </c:if>
                                    </td>
                                    <td><c:out value="${libro.autor}"/></td>
                                    <td><code><c:out value="${libro.isbn}"/></code></td>
                                    <td><c:out value="${libro.anio}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${libro.disponible}">
                                                <span class="badge badge-disponible rounded-pill px-3">Disponible</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-prestado rounded-pill px-3">Prestado</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <c:if test="${libro.disponible}">
                                            <form action="${pageContext.request.contextPath}/prestamos" method="post" class="d-inline">
                                                <input type="hidden" name="action" value="prestar">
                                                <input type="hidden" name="idLibro" value="${libro.id}">
                                                <button type="submit" class="btn btn-sm btn-primary"
                                                        onclick="return confirm('¿Confirmar préstamo?');">
                                                    <i class="bi bi-bookmark-plus"></i> Prestar
                                                </button>
                                            </form>
                                        </c:if>
                                        <c:if test="${sessionScope.usuario.esAdmin()}">
                                            <!-- Botón Editar -->
                                            <button type="button" class="btn btn-sm btn-outline-warning"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#modalEditar"
                                                    data-id="${libro.id}"
                                                    data-titulo="<c:out value='${libro.titulo}'/>"
                                                    data-autor="<c:out value='${libro.autor}'/>"
                                                    data-isbn="<c:out value='${libro.isbn}'/>"
                                                    data-anio="${libro.anio}"
                                                    data-editorial="<c:out value='${libro.editorial}'/>"
                                                    title="Editar libro">
                                                <i class="bi bi-pencil"></i>
                                            </button>
                                            <!-- Botón Eliminar -->
                                            <form action="${pageContext.request.contextPath}/libros" method="post" class="d-inline">
                                                <input type="hidden" name="action" value="eliminar">
                                                <input type="hidden" name="id" value="${libro.id}">
                                                <button type="submit" class="btn btn-sm btn-outline-danger"
                                                        onclick="return confirm('¿Eliminar este libro?');"
                                                        title="Eliminar libro">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </form>
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
    Total: <strong><c:out value="${libros.size()}"/></strong> libros
</p>

<!-- Modal Editar Libro (solo ADMIN) -->
<c:if test="${sessionScope.usuario.esAdmin()}">
<div class="modal fade" id="modalEditar" tabindex="-1" aria-labelledby="modalEditarLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/libros" method="post">
                <input type="hidden" name="action" value="editar">
                <input type="hidden" name="id" id="edit-id">
                <div class="modal-header">
                    <h5 class="modal-title" id="modalEditarLabel">
                        <i class="bi bi-pencil-square me-2"></i>Editar Libro
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Título *</label>
                        <input type="text" class="form-control" name="titulo" id="edit-titulo" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Autor *</label>
                        <input type="text" class="form-control" name="autor" id="edit-autor" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">ISBN</label>
                        <input type="text" class="form-control" name="isbn" id="edit-isbn">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Año</label>
                        <input type="number" class="form-control" name="anio" id="edit-anio" min="1900" max="2030">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Editorial</label>
                        <input type="text" class="form-control" name="editorial" id="edit-editorial">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-warning">
                        <i class="bi bi-check-lg me-1"></i>Guardar cambios
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
document.getElementById('modalEditar').addEventListener('show.bs.modal', function (event) {
    var button = event.relatedTarget;
    document.getElementById('edit-id').value = button.getAttribute('data-id');
    document.getElementById('edit-titulo').value = button.getAttribute('data-titulo') || '';
    document.getElementById('edit-autor').value = button.getAttribute('data-autor') || '';
    document.getElementById('edit-isbn').value = button.getAttribute('data-isbn') || '';
    document.getElementById('edit-anio').value = button.getAttribute('data-anio') || '';
    document.getElementById('edit-editorial').value = button.getAttribute('data-editorial') || '';
});
</script>
</c:if>

<jsp:include page="/includes/footer.jsp"/>
