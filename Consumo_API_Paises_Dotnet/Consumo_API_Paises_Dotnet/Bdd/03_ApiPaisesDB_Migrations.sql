IF OBJECT_ID(N'[__EFMigrationsHistory]') IS NULL
BEGIN
    CREATE TABLE [__EFMigrationsHistory] (
        [MigrationId] nvarchar(150) NOT NULL,
        [ProductVersion] nvarchar(32) NOT NULL,
        CONSTRAINT [PK___EFMigrationsHistory] PRIMARY KEY ([MigrationId])
    );
END;
GO

BEGIN TRANSACTION;
IF NOT EXISTS (
    SELECT * FROM [__EFMigrationsHistory]
    WHERE [MigrationId] = N'20261116152111_InitialCreate'
)
BEGIN
    CREATE TABLE [Paises] (
        [Id] int NOT NULL IDENTITY,
        [Nombre] nvarchar(100) NOT NULL,
        [Capital] nvarchar(100) NOT NULL,
        [Region] nvarchar(100) NOT NULL,
        [SubRegion] nvarchar(100) NOT NULL,
        [Poblacion] bigint NOT NULL,
        [Area] float NOT NULL,
        [Moneda] nvarchar(100) NOT NULL,
        CONSTRAINT [PK_Paises] PRIMARY KEY ([Id])
    );
END;

IF NOT EXISTS (
    SELECT * FROM [__EFMigrationsHistory]
    WHERE [MigrationId] = N'20261116152111_InitialCreate'
)
BEGIN
    INSERT INTO [__EFMigrationsHistory] ([MigrationId], [ProductVersion])
    VALUES (N'20261116152111_InitialCreate', N'10.0.0');
END;

COMMIT;
GO

