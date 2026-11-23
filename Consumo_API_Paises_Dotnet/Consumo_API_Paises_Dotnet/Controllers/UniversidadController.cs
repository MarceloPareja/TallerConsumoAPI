using System.ComponentModel.DataAnnotations;
using Consumo_API_Paises_Dotnet.Models;
using Consumo_API_Paises_Dotnet.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Consumo_API_Paises_Dotnet.Controllers;

public class UniversidadController(AppDbContext db, ImportacionService importador,
    IConfiguration config) : Controller
{
    public async Task<IActionResult> Index(string? pais, string? nombre)
    {
        ViewBag.Pais = pais ?? config["UNIVERSIDADES_PAIS"] ?? "Ecuador";
        ViewBag.Nombre = nombre;
        var consulta = db.Universidades.AsNoTracking();
        if (!string.IsNullOrWhiteSpace(pais)) consulta = consulta.Where(u => u.Pais.Contains(pais));
        if (!string.IsNullOrWhiteSpace(nombre)) consulta = consulta.Where(u => u.Nombre.Contains(nombre));
        return View(await consulta.OrderBy(u => u.Pais).ThenBy(u => u.Nombre).ToListAsync());
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Importar(string pais)
    {
        if (string.IsNullOrWhiteSpace(pais) || pais.Trim().Length > 100)
        {
            TempData["Error"] = "Ingrese un país de hasta 100 caracteres.";
            return RedirectToAction(nameof(Index));
        }
        try
        {
            var cantidad = await importador.ImportarUniversidadesAsync(pais);
            TempData["Mensaje"] = $"Se importaron {cantidad} universidades nuevas. Los registros existentes se conservaron.";
        }
        catch (Exception ex) when (ex is HttpRequestException or ValidationException or DbUpdateException)
        {
            TempData["Error"] = "No se pudo importar. Compruebe la API y los datos e intente nuevamente.";
            HttpContext.RequestServices.GetRequiredService<ILogger<UniversidadController>>().LogWarning(ex, "Error importando universidades");
        }
        return RedirectToAction(nameof(Index), new { pais });
    }
}
