using Consumo_API_Paises_Dotnet.Models;
using Consumo_API_Paises_Dotnet.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Consumo_API_Paises_Dotnet.Controllers
{
    public class PaisController : Controller
    {
        private readonly AppDbContext _context;
        private readonly IPaisService _paisService;

        public PaisController(AppDbContext context, IPaisService paisService)
        {
            _context = context;
            _paisService = paisService;
        }

        // ===== Sección 14: Acciones principales =====

        public IActionResult Index()
        {
            ViewBag.TotalPaises = _context.Paises.Count();
            return View();
        }

        /// <summary>
        /// Consume REST Countries y guarda la información en SQL Server.
        /// </summary>
        public async Task<IActionResult> Importar()
        {
            // Evita volver a consultar la API y duplicar registros si ya se importaron.
            if (await _context.Paises.AnyAsync())
            {
                TempData["Mensaje"] = "La base de datos ya contiene países. No es necesario importarlos nuevamente.";
                return RedirectToAction(nameof(Lista));
            }

            try
            {
                var paises = await _paisService.ObtenerPaisesAsync();

                if (paises.Count == 0)
                {
                    TempData["Mensaje"] = "La API no devolvió información.";
                    return RedirectToAction(nameof(Lista));
                }

                _context.Paises.AddRange(paises);
                await _context.SaveChangesAsync();

                TempData["Mensaje"] = $"Se importaron {paises.Count} países correctamente.";
            }
            catch (HttpRequestException ex)
            {
                TempData["Error"] = ex.Message;
            }

            return RedirectToAction(nameof(Lista));
        }

        /// <summary>
        /// Actualiza los registros existentes con la información vigente de la API.
        /// Aplica el principio HTTP 304: si no hay diferencias, no se escribe en la BD.
        /// </summary>
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Actualizar()
        {
            var existentes = await _context.Paises.ToListAsync();

            if (existentes.Count == 0)
            {
                TempData["Mensaje"] = "No hay países registrados. Use 'Importar todos' para cargar la información.";
                return RedirectToAction(nameof(Lista));
            }

            try
            {
                var paisesApi = await _paisService.ObtenerPaisesAsync();

                if (paisesApi.Count == 0)
                {
                    TempData["Mensaje"] = "La API no devolvió información.";
                    return RedirectToAction(nameof(Lista));
                }

                // Clave natural: Nombre normalizado (el modelo no tiene código ISO).
                var porNombre = existentes
                    .GroupBy(p => Normalizar(p.Nombre))
                    .ToDictionary(g => g.Key, g => g.First());

                var actualizados = 0;
                var sinCambios = 0;
                var noEncontrados = 0;

                foreach (var paisApi in paisesApi)
                {
                    if (!porNombre.TryGetValue(Normalizar(paisApi.Nombre), out var paisBd))
                    {
                        // Solo se actualizan existentes: no se inserta.
                        noEncontrados++;
                        continue;
                    }

                    if (SonIguales(paisBd, paisApi))
                    {
                        sinCambios++;
                        continue;
                    }

                    // La entidad está trackeada: al asignar, EF la marca como modificada.
                    paisBd.Capital = paisApi.Capital;
                    paisBd.Region = paisApi.Region;
                    paisBd.SubRegion = paisApi.SubRegion;
                    paisBd.Poblacion = paisApi.Poblacion;
                    paisBd.Area = paisApi.Area;
                    paisBd.Moneda = paisApi.Moneda;
                    actualizados++;
                }

                if (actualizados == 0)
                {
                    // Principio 304: no hay cambios, no se llama a SaveChangesAsync().
                    TempData["Mensaje"] = sinCambios > 0
                        ? $"304 Not Modified: los {sinCambios} países ya estaban actualizados. No se modificó ningún registro."
                        : "304 Not Modified: ningún país de la API coincide con los registros de la BD.";
                    return RedirectToAction(nameof(Lista));
                }

                await _context.SaveChangesAsync();

                TempData["Mensaje"] =
                    $"{actualizados} países actualizados; {sinCambios} sin cambios (304); " +
                    $"{noEncontrados} de la API no existen en la BD.";
            }
            catch (HttpRequestException ex)
            {
                TempData["Error"] = ex.Message;
            }

            return RedirectToAction(nameof(Lista));
        }

        private static string Normalizar(string valor) =>
            (valor ?? string.Empty).Trim().ToUpperInvariant();

        private static bool SonIguales(Pais bd, Pais api) =>
            string.Equals(bd.Capital, api.Capital, StringComparison.OrdinalIgnoreCase) &&
            string.Equals(bd.Region, api.Region, StringComparison.OrdinalIgnoreCase) &&
            string.Equals(bd.SubRegion, api.SubRegion, StringComparison.OrdinalIgnoreCase) &&
            bd.Poblacion == api.Poblacion &&
            Math.Abs(bd.Area - api.Area) < 0.01 &&
            string.Equals(bd.Moneda, api.Moneda, StringComparison.OrdinalIgnoreCase);

        /// <summary>
        /// Listado con búsquedas (Actividad 3) y filtros dinámicos (Actividad 4).
        /// </summary>
        public async Task<IActionResult> Lista(string? nombre, string? region, string? capital, string? orden)
        {
            var consulta = _context.Paises.AsQueryable();

            if (!string.IsNullOrWhiteSpace(nombre))
            {
                consulta = consulta.Where(p => p.Nombre.Contains(nombre));
            }

            if (!string.IsNullOrWhiteSpace(region))
            {
                consulta = consulta.Where(p => p.Region == region);
            }

            if (!string.IsNullOrWhiteSpace(capital))
            {
                consulta = consulta.Where(p => p.Capital.Contains(capital));
            }

            consulta = orden switch
            {
                "nombre" => consulta.OrderBy(p => p.Nombre),
                "nombre_desc" => consulta.OrderByDescending(p => p.Nombre),
                "poblacion" => consulta.OrderByDescending(p => p.Poblacion),
                "poblacion_asc" => consulta.OrderBy(p => p.Poblacion),
                "area" => consulta.OrderByDescending(p => p.Area),
                _ => consulta.OrderBy(p => p.Region).ThenBy(p => p.Nombre)
            };

            ViewBag.Nombre = nombre;
            ViewBag.Region = region;
            ViewBag.Capital = capital;
            ViewBag.Orden = orden;
            ViewBag.Regiones = await _context.Paises
                .Where(p => p.Region != null && p.Region != "")
                .Select(p => p.Region)
                .Distinct()
                .OrderBy(r => r)
                .ToListAsync();

            return View(await consulta.ToListAsync());
        }

        // ===== Actividad 1: Consumir la API para un país específico =====

        public IActionResult BuscarPais()
        {
            return View();
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> BuscarPais(string nombre)
        {
            if (string.IsNullOrWhiteSpace(nombre))
            {
                ModelState.AddModelError(nameof(nombre), "Ingrese el nombre de un país.");
                return View();
            }

            try
            {
                var pais = await _paisService.ObtenerPaisPorNombreAsync(nombre);

                if (pais is null)
                {
                    ViewBag.Error = $"No se encontró información para '{nombre}'.";
                    return View();
                }

                ViewBag.Buscado = nombre;
                return View(pais);
            }
            catch (HttpRequestException ex)
            {
                ViewBag.Error = ex.Message;
                return View();
            }
        }

        // ===== Actividad 2: CRUD completo =====

        public async Task<IActionResult> Details(int id)
        {
            var pais = await _context.Paises.FindAsync(id);
            return pais is null ? NotFound() : View(pais);
        }

        public IActionResult Create()
        {
            return View();
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(Pais pais)
        {
            if (!ModelState.IsValid)
            {
                return View(pais);
            }

            _context.Paises.Add(pais);
            await _context.SaveChangesAsync();
            TempData["Mensaje"] = "País agregado correctamente.";

            return RedirectToAction(nameof(Lista));
        }

        public async Task<IActionResult> Edit(int id)
        {
            var pais = await _context.Paises.FindAsync(id);
            return pais is null ? NotFound() : View(pais);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, Pais pais)
        {
            if (id != pais.Id)
            {
                return BadRequest();
            }

            if (!ModelState.IsValid)
            {
                return View(pais);
            }

            try
            {
                _context.Update(pais);
                await _context.SaveChangesAsync();
                TempData["Mensaje"] = "País actualizado correctamente.";
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!await _context.Paises.AnyAsync(p => p.Id == id))
                {
                    return NotFound();
                }

                throw;
            }

            return RedirectToAction(nameof(Lista));
        }

        public async Task<IActionResult> Delete(int id)
        {
            var pais = await _context.Paises.FindAsync(id);
            return pais is null ? NotFound() : View(pais);
        }

        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var pais = await _context.Paises.FindAsync(id);
            if (pais is not null)
            {
                _context.Paises.Remove(pais);
                await _context.SaveChangesAsync();
                TempData["Mensaje"] = "País eliminado correctamente.";
            }

            return RedirectToAction(nameof(Lista));
        }

        // ===== Actividad 5: Reportes =====

        public async Task<IActionResult> Reportes()
        {
            var paises = await _context.Paises.AsNoTracking().ToListAsync();

            var modelo = new ReportesViewModel
            {
                PaisesPorRegion = paises
                    .GroupBy(p => string.IsNullOrWhiteSpace(p.Region) ? "N/D" : p.Region)
                    .Select(g => new RegionReporte { Region = g.Key, Total = g.Count() })
                    .OrderByDescending(r => r.Total)
                    .ToList(),

                PaisMasPoblado = paises.OrderByDescending(p => p.Poblacion).FirstOrDefault(),

                PromedioPorRegion = paises
                    .GroupBy(p => string.IsNullOrWhiteSpace(p.Region) ? "N/D" : p.Region)
                    .Select(g => new RegionPromedio
                    {
                        Region = g.Key,
                        PromedioPoblacion = g.Average(p => (double)p.Poblacion)
                    })
                    .OrderByDescending(r => r.PromedioPoblacion)
                    .ToList()
            };

            return View(modelo);
        }
    }
}
