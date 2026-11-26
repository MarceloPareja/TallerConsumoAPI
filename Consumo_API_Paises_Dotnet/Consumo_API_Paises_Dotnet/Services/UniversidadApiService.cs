using System.Net.Http.Json;
using System.Text.Json;
using System.Text.Json.Serialization;
using Consumo_API_Paises_Dotnet.Models;

namespace Consumo_API_Paises_Dotnet.Services;

public class UniversidadApiService(HttpClient cliente) : IUniversidadService
{
    public async Task<List<Universidad>> ObtenerPorPaisAsync(string pais)
    {
        if (string.IsNullOrWhiteSpace(pais)) throw new ArgumentException("Ingrese un país.");
        try
        {
            var datos = await cliente.GetFromJsonAsync<List<UniversidadDto>>(
                $"search?country={Uri.EscapeDataString(pais.Trim())}")
                ?? throw new HttpRequestException("La API de universidades devolvió una respuesta vacía.");
            return datos.Where(d => !string.IsNullOrWhiteSpace(d.Name) && !string.IsNullOrWhiteSpace(d.Country))
                .Select(d => new Universidad
                {
                    Nombre = d.Name!.Trim(), Pais = d.Country!.Trim(),
                    Dominio = string.Join(", ", d.Domains ?? []),
                    SitioWeb = string.Join(", ", d.WebPages ?? [])
                }).ToList();
        }
        catch (JsonException ex) { throw new HttpRequestException("JSON inválido de la API de universidades.", ex); }
        catch (TaskCanceledException ex) { throw new HttpRequestException("La API de universidades agotó el tiempo de espera.", ex); }
    }

    private sealed class UniversidadDto
    {
        [JsonPropertyName("name")] public string? Name { get; set; }
        [JsonPropertyName("country")] public string? Country { get; set; }
        [JsonPropertyName("domains")] public List<string>? Domains { get; set; }
        [JsonPropertyName("web_pages")] public List<string>? WebPages { get; set; }
    }
}
