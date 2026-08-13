-- =====================================================
-- BiblioWeb - Biblioteca Digital UNTEC
-- Schema MySQL 8 | 10 libros | SIN préstamos iniciales
-- =====================================================

CREATE DATABASE IF NOT EXISTS biblioteca_untec
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE biblioteca_untec;

DROP TABLE IF EXISTS prestamo;
DROP TABLE IF EXISTS libro;
DROP TABLE IF EXISTS usuario;

CREATE TABLE usuario (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    email       VARCHAR(120) NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    rol         VARCHAR(20)  NOT NULL DEFAULT 'ESTUDIANTE',
    activo      TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_alta  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE libro (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    titulo      VARCHAR(200) NOT NULL,
    autor       VARCHAR(120) NOT NULL,
    isbn        VARCHAR(20)  UNIQUE,
    anio        INT,
    editorial   VARCHAR(100),
    disponible  TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_alta  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE prestamo (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario       INT NOT NULL,
    id_libro         INT NOT NULL,
    fecha_prestamo   DATE NOT NULL,
    fecha_devolucion DATE NULL,
    estado           VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_prestamo_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id),
    CONSTRAINT fk_prestamo_libro   FOREIGN KEY (id_libro)   REFERENCES libro(id)
) ENGINE=InnoDB;

-- Usuarios de prueba
INSERT INTO usuario (nombre, email, password, rol) VALUES
('Administrador UNTEC', 'admin@untec.edu', 'admin123', 'ADMIN'),
('María González',      'maria.gonzalez@untec.edu', 'maria123', 'ESTUDIANTE'),
('Carlos Pérez',        'carlos.perez@untec.edu', 'carlos123', 'ESTUDIANTE'),
('Ana Silva',           'ana.silva@untec.edu', 'ana123', 'ESTUDIANTE');

-- 10 libros (todos disponibles, sin préstamos)
INSERT INTO libro (titulo, autor, isbn, anio, editorial, disponible) VALUES
('Clean Code', 'Robert C. Martin', '978-0132350884', 2008, 'Prentice Hall', 1),
('Effective Java', 'Joshua Bloch', '978-0134685991', 2018, 'Addison-Wesley', 1),
('Design Patterns', 'Erich Gamma et al.', '978-0201633610', 1994, 'Addison-Wesley', 1),
('Java Concurrency in Practice', 'Brian Goetz', '978-0321349602', 2006, 'Addison-Wesley', 1),
('Spring in Action', 'Craig Walls', '978-1617294945', 2018, 'Manning', 1),
('Head First Design Patterns', 'Eric Freeman', '978-0596007126', 2004, 'O''Reilly', 1),
('Refactoring', 'Martin Fowler', '978-0134757599', 2018, 'Addison-Wesley', 1),
('The Pragmatic Programmer', 'David Thomas', '978-0135957059', 2019, 'Addison-Wesley', 1),
('Domain-Driven Design', 'Eric Evans', '978-0321125217', 2003, 'Addison-Wesley', 1),
('Building Microservices', 'Sam Newman', '978-1492034025', 2021, 'O''Reilly', 1);
