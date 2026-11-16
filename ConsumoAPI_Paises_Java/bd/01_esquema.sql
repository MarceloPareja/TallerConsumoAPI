-- Esquema del taller. La tabla queda vacía intencionalmente:
-- los datos se cargan desde la API o con el formulario CRUD.
CREATE DATABASE IF NOT EXISTS api_paises
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE api_paises;

CREATE TABLE IF NOT EXISTS pais(
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    capital VARCHAR(100),
    region VARCHAR(100),
    poblacion BIGINT,
    moneda VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
