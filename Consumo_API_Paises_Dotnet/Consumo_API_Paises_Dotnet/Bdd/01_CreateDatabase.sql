-- ============================================================
-- Taller: Consumo de APIs Públicas en .NET con SQL Server
-- Sección 7: Creación de la base de datos
-- ============================================================

IF DB_ID('ApiPaisesDB') IS NULL
BEGIN
    CREATE DATABASE ApiPaisesDB;
END
GO

USE ApiPaisesDB;
GO

IF OBJECT_ID('dbo.Paises', 'U') IS NULL
BEGIN
    CREATE TABLE Paises
    (
        Id        INT IDENTITY(1,1) PRIMARY KEY,
        Nombre    NVARCHAR(100) NOT NULL,
        Capital   NVARCHAR(100) NULL,
        Region    NVARCHAR(100) NULL,
        SubRegion NVARCHAR(100) NULL,
        Poblacion BIGINT       NOT NULL DEFAULT 0,
        Area      FLOAT        NOT NULL DEFAULT 0,
        Moneda    NVARCHAR(100) NULL
    );
END
GO
