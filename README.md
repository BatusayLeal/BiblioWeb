# BiblioWeb – Biblioteca Digital

** Trabajo Módulo 5 – Desarrollo de aplicaciones web dinámicas Java**

Aplicación web con **Jakarta EE 10 / Servlet 6.0**, lista para **Tomcat 11** y **Java 25**.

---

## URL de acceso

```
http://localhost:8080/BiblioWeb
```

---

## Stack tecnológico

| Componente       | Versión / Tecnología              |
|------------------|-----------------------------------|
| Java             | **25**                            |
| Servidor         | **Apache Tomcat 11**              |
| API              | **Jakarta Servlet 6.0**           |
| Vista            | JSP + **Jakarta JSTL 3.0**        |
| Acceso a datos   | JDBC + DAO + Singleton            |
| Base de datos    | MySQL 8 (local)                   |
| Build            | Maven (WAR)                       |
| IDE              | VS Code                           |

---

## Estructura del proyecto

```
BiblioWeb/
├── pom.xml
├── sql/schema.sql
├── src/main/java/com/biblioteca/
│   ├── model/
│   ├── dao/
│   └── controller/
└── src/main/webapp/
    ├── WEB-INF/web.xml
    ├── css/
    ├── includes/
    ├── index.jsp
    ├── libros.jsp
    └── prestamos.jsp
```

---

## Cómo desplegar

### 1. Base de datos
Si aún no la tienes creada:

```bash
mysql -u root -p < sql/schema.sql
```

### 2. Credenciales MySQL
Edita `src/main/java/com/biblioteca/dao/Conexion.java` y cambia:

```java
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL =
            "jdbc:mysql://localhost:3306/biblioteca_untec?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "usuario de BD";
    private static final String PASSWORD = "password de BD";
```

### 3. Generar el WAR

```bash
mvn clean package
```

El archivo generado será:

```
target/BiblioWeb.war
```

### 4. Desplegar en Tomcat 11

Copia `BiblioWeb.war` a la carpeta `webapps/` de Tomcat 11 y arranca el servidor.

Luego abre:

**http://localhost:8080/BiblioWeb**

---

## Credenciales de prueba

| Email                        | Password  | Rol        |
|------------------------------|-----------|------------|
| admin@untec.edu              | admin123  | ADMIN      |
| maria.gonzalez@untec.edu     | maria123  | ESTUDIANTE |
| carlos.perez@untec.edu       | carlos123 | ESTUDIANTE |

---

## Funcionalidades

- Login / Logout con sesión hacia MySQL por JDBC
- Catálogo de libros (todos / disponibles / Orden asc-desc Titulo Autor y Año)
- Solicitar préstamo
- Ver mis préstamos como historial y devolver dejando registro de fecha de devolucion
- **ADMIN**: agregar, eliminar, modificar libros y usuarios + ver todos los préstamos
  **ESTUDIANTE**: Listar catalogo de libros y solicitar prestamos, historial y devolucion de prestamos

## Notas importantes

- Todas las dependencias son **Jakarta** (no se usa `javax.*`).
- Compilado para **Java 25**.
- Context path configurado como **BiblioWeb**.
- 10 libros de ejemplo, **sin préstamos iniciales**.

---

## Sistema de log

Cada operación de base de datos (SELECT, INSERT, UPDATE, DELETE) y los errores
se registran automáticamente en log "[$CATALINA_PATH]/log/biblioweb.log" con el siguiente formato:

  ```bash
    2026-08-26 18:29:24 | UsuarioDAO.java | login | INFO | SELECT login OK - email=admin@untec.edu | id=1 | rol=ADMIN
    2026-08-26 18:29:24 | LibroDAO.java | listar | INFO | SELECT listar OK - soloDisponibles=false | orden=titulo ASC | cantidad=11
    2026-08-26 18:29:45 | LibroDAO.java | buscarPorId | INFO | SELECT libro id=10 OK
    2026-08-26 18:29:45 | LibroDAO.java | actualizarDisponibilidad | INFO | UPDATE disponibilidad id=10 | disponible=false | ok=true
    2026-08-26 18:29:45 | PrestamoDAO.java | prestar | INFO | INSERT prestamo OK - usuario=1 | libro=10
    2026-08-26 18:29:45 | LibroDAO.java | listar | INFO | SELECT listar OK - soloDisponibles=false | orden=titulo ASC | cantidad=11
    2026-08-26 18:29:57 | UsuarioDAO.java | login | INFO | SELECT login OK - email=maria.gonzalez@untec.edu | id=2 | rol=ESTUDIANTE
    2026-08-26 18:29:57 | LibroDAO.java | listar | INFO | SELECT listar OK - soloDisponibles=false | orden=titulo ASC | cantidad=11
  ```

Si no corre bajo Tomcat, el archivo se crea en:

```
{user.dir}/log/biblioweb.log
```

El archivo **nunca se borra**: cada evento se agrega al final (modo append).
