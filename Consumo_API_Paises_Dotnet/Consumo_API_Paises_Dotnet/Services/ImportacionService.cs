using System.ComponentModel.DataAnnotations;
using System.Data;
using Consumo_API_Paises_Dotnet.Models;
using Microsoft.EntityFrameworkCore;

namespace Consumo_API_Paises_Dotnet.Services;

public class ImportacionService(AppDbContext db, IPaisService paises, IUniversidadService universidades)
{
    public async Task<int> ImportarPaisesAsync()
    {
        // Descargar todo antes de escribir: una página fallida no deja cargas parciales.
        var datos = await paises.ObtenerPaisesAsync();
        using var transaccion = await db.Database.BeginTransactionAsync(IsolationLevel.Serializable);
        var nombres = (await db.Paises.Select(p => p.Nombre).ToListAsync())
            .Select(Normalizar).ToHashSet();
        var nuevos = datos.Where(p => nombres.Add(Normalizar(p.Nombre))).ToList();
        foreach (var pais in nuevos) Validator.ValidateObject(pais, new ValidationContext(pais), true);
        db.Paises.AddRange(nuevos);
        await db.SaveChangesAsync();
        await transaccion.CommitAsync();
        return nuevos.Count;
    }

    public async Task<int> ImportarUniversidadesAsync(string pais)
    {
        var datos = await universidades.ObtenerPorPaisAsync(pais);
        using var transaccion = await db.Database.BeginTransactionAsync(IsolationLevel.Serializable);
        var existentes = await db.Universidades.Select(u => new { u.Pais, u.Nombre }).ToListAsync();
        var claves = existentes.Select(u => (Normalizar(u.Pais), Normalizar(u.Nombre))).ToHashSet();
        var nuevos = datos.Where(u => claves.Add((Normalizar(u.Pais), Normalizar(u.Nombre)))).ToList();
        foreach (var universidad in nuevos) Validator.ValidateObject(universidad, new ValidationContext(universidad), true);
        db.Universidades.AddRange(nuevos);
        await db.SaveChangesAsync();
        await transaccion.CommitAsync();
        return nuevos.Count;
    }

    private static string Normalizar(string valor) => valor.Trim().ToUpperInvariant();
}
