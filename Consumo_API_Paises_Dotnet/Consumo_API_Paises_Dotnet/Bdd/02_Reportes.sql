-- ============================================================
-- Actividad 5: Reportes
-- ============================================================
USE ApiPaisesDB;
GO

-- Reporte 1: Cantidad de países por región
SELECT Region,
       COUNT(*) AS Total
FROM Paises
GROUP BY Region
ORDER BY Total DESC;
GO

-- Reporte 2: País más poblado
SELECT TOP 1 *
FROM Paises
ORDER BY Poblacion DESC;
GO

-- Reporte 3: Promedio poblacional por región
SELECT Region,
       AVG(CAST(Poblacion AS FLOAT)) AS PromedioPoblacion
FROM Paises
GROUP BY Region
ORDER BY PromedioPoblacion DESC;
GO
