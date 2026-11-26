-- Alternativa manual a la segunda migración de EF Core. Ejecutar después de 03.
IF NOT EXISTS (SELECT 1 FROM [__EFMigrationsHistory]
               WHERE [MigrationId] = N'20261122000100_AgregarUniversidades')
BEGIN
    BEGIN TRANSACTION;
    CREATE TABLE [Universidades] (
        [Id] int NOT NULL IDENTITY PRIMARY KEY,
        [Nombre] nvarchar(300) NOT NULL,
        [Dominio] nvarchar(2000) NOT NULL,
        [SitioWeb] nvarchar(4000) NOT NULL,
        [Pais] nvarchar(100) NOT NULL
    );
    CREATE UNIQUE INDEX [IX_Universidades_Pais_Nombre] ON [Universidades] ([Pais], [Nombre]);
    INSERT INTO [__EFMigrationsHistory] ([MigrationId], [ProductVersion])
    VALUES (N'20261122000100_AgregarUniversidades', N'10.0.0');
    COMMIT;
END;
GO
