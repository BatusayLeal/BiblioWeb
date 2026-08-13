<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/includes/header.jsp">
    <jsp:param name="titulo" value="Gestión de Usuarios"/>
</jsp:include>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h2 class="mb-0"><i class="bi bi-people me-2"></i>Gestión de Usuarios</h2>
</div>

<!-- Formulario agregar usuario -->
<div class="form-agregar mb-4">
    <h5 class="mb-3"><i class="bi bi-person-plus me-2"></i>Crear nuevo usuario</h5>
    <form action="${pageContext.request.contextPath}/usuarios" method="post" class="row g-3">
        <input type="hidden" name="action" value="agregar">
        <div class="col-md-3">
            <input type="text" class="form-control" name="nombre" placeholder="Nombre completo *" required>
        </div>
        <div class="col-md-3">
            <input type="email" class="form-control" name="email" placeholder="Email *" required>
        </div>
        <div class="col-md-2">
            <input type="password" class="form-control" name="password" placeholder="Contraseña *" required>
        </div>
        <div class="col-md-2">
            <select class="form-select" name="rol" required>
                <option value="ESTUDIANTE">Estudiante</option>
                <option value="ADMIN">Administrador</option>
            </select>
        </div>
        <div class="col-md-2">
            <button type="submit" class="btn btn-success w-100">
                <i class="bi bi-plus-lg"></i> Crear
            </button>
        </div>
    </form>
</div>

<!-- Tabla de usuarios -->
<div class="card">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover mb-0 align-middle">
                <thead>
                    <tr>
                        <th style="width: 50px;">#</th>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Rol</th>
                        <th>Estado</th>
                        <th style="width: 140px;" class="text-center">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty usuarios}">
                            <tr>
                                <td colspan="6" class="text-center text-muted py-4">No hay usuarios registrados.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="u" items="${usuarios}">
                                <tr>
                                    <td><c:out value="${u.id}"/></td>
                                    <td><strong><c:out value="${u.nombre}"/></strong></td>
                                    <td><c:out value="${u.email}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.rol == 'ADMIN'}">
                                                <span class="badge bg-danger rounded-pill px-3">ADMIN</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary rounded-pill px-3">ESTUDIANTE</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.activo}">
                                                <span class="badge bg-success rounded-pill px-3">Activo</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-dark rounded-pill px-3">Inactivo</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <!-- Editar -->
                                        <button type="button" class="btn btn-sm btn-outline-warning"
                                                data-bs-toggle="modal"
                                                data-bs-target="#modalEditarUsuario"
                                                data-id="${u.id}"
                                                data-nombre="<c:out value='${u.nombre}'/>"
                                                data-email="<c:out value='${u.email}'/>"
                                                data-rol="${u.rol}"
                                                data-activo="${u.activo}"
                                                title="Editar usuario">
                                            <i class="bi bi-pencil"></i>
                                        </button>
                                        <!-- Eliminar (no se puede eliminar a sí mismo) -->
                                        <c:if test="${u.id != sessionScope.usuario.id}">
                                            <form action="${pageContext.request.contextPath}/usuarios" method="post" class="d-inline">
                                                <input type="hidden" name="action" value="eliminar">
                                                <input type="hidden" name="id" value="${u.id}">
                                                <button type="submit" class="btn btn-sm btn-outline-danger"
                                                        onclick="return confirm('¿Eliminar este usuario?');"
                                                        title="Eliminar usuario">
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
    Total: <strong><c:out value="${usuarios.size()}"/></strong> usuarios
</p>

<!-- Modal Editar Usuario -->
<div class="modal fade" id="modalEditarUsuario" tabindex="-1" aria-labelledby="modalEditarUsuarioLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/usuarios" method="post">
                <input type="hidden" name="action" value="editar">
                <input type="hidden" name="id" id="edit-uid">
                <div class="modal-header">
                    <h5 class="modal-title" id="modalEditarUsuarioLabel">
                        <i class="bi bi-pencil-square me-2"></i>Editar Usuario
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Nombre *</label>
                        <input type="text" class="form-control" name="nombre" id="edit-unombre" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Email *</label>
                        <input type="email" class="form-control" name="email" id="edit-uemail" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Nueva contraseña</label>
                        <input type="password" class="form-control" name="password" id="edit-upassword"
                               placeholder="Dejar vacío para no cambiar">
                        <div class="form-text">Solo complete si desea cambiar la contraseña.</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Rol *</label>
                        <select class="form-select" name="rol" id="edit-urol" required>
                            <option value="ESTUDIANTE">Estudiante</option>
                            <option value="ADMIN">Administrador</option>
                        </select>
                    </div>
                    <div class="mb-3 form-check">
                        <input type="checkbox" class="form-check-input" name="activo" id="edit-uactivo" value="1">
                        <label class="form-check-label" for="edit-uactivo">Usuario activo</label>
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
document.getElementById('modalEditarUsuario').addEventListener('show.bs.modal', function (event) {
    var btn = event.relatedTarget;
    document.getElementById('edit-uid').value = btn.getAttribute('data-id');
    document.getElementById('edit-unombre').value = btn.getAttribute('data-nombre') || '';
    document.getElementById('edit-uemail').value = btn.getAttribute('data-email') || '';
    document.getElementById('edit-upassword').value = '';
    document.getElementById('edit-urol').value = btn.getAttribute('data-rol') || 'ESTUDIANTE';
    document.getElementById('edit-uactivo').checked = (btn.getAttribute('data-activo') === 'true');
});
</script>

<jsp:include page="/includes/footer.jsp"/>
