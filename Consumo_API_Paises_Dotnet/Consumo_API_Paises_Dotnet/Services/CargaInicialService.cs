using Microsoft.EntityFrameworkCore;
using Consumo_API_Paises_Dotnet.Models;

namespace Consumo_API_Paises_Dotnet.Services;

public class CargaInicialService(IServiceScopeFactory scopes, IConfiguration config,
    ILogger<CargaInicialService> logger) : BackgroundService
{
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        if (!config.GetValue("SEED_ON_START", true)) return;
        foreach (var tipo in new[] { "países", "universidades" })
        {
            if (stoppingToken.IsCancellationRequested) return;
            using var scope = scopes.CreateScope();
            var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
            var importador = scope.ServiceProvider.GetRequiredService<ImportacionService>();
            try
            {
                var existe = tipo == "países" ? await db.Paises.AnyAsync(stoppingToken)
                    : await db.Universidades.AnyAsync(stoppingToken);
                if (existe) continue;
                var total = tipo == "países" ? await importador.ImportarPaisesAsync()
                    : await importador.ImportarUniversidadesAsync(config["UNIVERSIDADES_PAIS"] ?? "Ecuador");
                logger.LogInformation("Carga inicial: {Total} {Tipo} importados.", total, tipo);
            }
            catch (Exception ex)
            {
                logger.LogWarning(ex, "No se pudo completar la carga inicial de {Tipo}. Reintente desde la interfaz; la base sigue disponible.", tipo);
            }
        }
    }
}
