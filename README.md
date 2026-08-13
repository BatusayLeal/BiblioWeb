# BiblioWeb – Biblioteca Digital UNTEC

**Módulo 5 – Desarrollo de aplicaciones web dinámicas Java**

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

```bash de creacion, el archivo **sql/schema.sql** contiene la estructura de la bD con los ejemplos  
mysql -u root -p < sql/schema.sql
```

### 2. Credenciales MySQL
Edita `src/main/java/com/biblioteca/dao/Conexion.java` y cambia:

```java
    private static final String USER = "user_bliblio";
    private static final String PASSWORD = "Biblio1234-";
```
por las credenciales de conexion a la base de datos mysql **biblioteca_untec**

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
| ana.silva@untec.edu          | ana123    | ESTUDIANTE |

---

## Funcionalidades

- Login / Logout con sesión (Rol ADMIN o Estudiante)
- Catálogo de libros (todos / disponibles / Orden asc-des Titulo-Autor-Año)
- Solicitar préstamo
- Ver mis préstamos y devolver
- **ADMIN**: agregar/ eliminar y Modificar libros y usuarios + ver todos los préstamos

---

## Notas importantes

- Todas las dependencias son **Jakarta** (no se usa `javax.*`).
- Compilado para **Java 25**.
- Context path configurado como **BiblioWeb**.
- 10 libros de ejemplo, **sin préstamos iniciales**.
- 1 admin y 3 usuarios de ejemplos
