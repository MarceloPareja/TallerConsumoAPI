using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using System.Text.Json.Serialization;
using Consumo_API_Paises_Dotnet.Models;

namespace Consumo_API_Paises_Dotnet.Services
{
    /// <summary>
    /// Servicio encargado únicamente de consumir la API REST Countries (v5).
    /// Se usa System.Text.Json (incluido en .NET) en lugar de dependencias externas.
    /// </summary>
    public class PaisApiService : IPaisService
    {
        // Solo se solicitan los campos que necesita el modelo.
        // Solo se solicitan los campos que necesita el modelo.
        private const string Campos =
            "names.common,capitals,region,subregion,population,area,currencies";

        private const string RecursoPaises = "countries/v5";

        private const int LimitePagina = 100;

        private readonly HttpClient _httpClient;
        private readonly string _apiKey;

        public PaisApiService(HttpClient httpClient, IConfiguration configuration)
        {
            _httpClient = httpClient;
            _apiKey = configuration["RESTCOUNTRIES_API_KEY"]
                ?? throw new InvalidOperationException(
                    "Falta la variable RESTCOUNTRIES_API_KEY en el archivo .development.env");
        }

        /// <summary>
        /// Recorre todas las páginas del endpoint de países (v5 pagina de 25 a 100 registros).
        /// </summary>
        public async Task<List<Pais>> ObtenerPaisesAsync()
        {
            var paises = new List<Pais>();
            var offset = 0;

            while (true)
            {
                var url = $"{RecursoPaises}?limit={LimitePagina}&offset={offset}&response_fields={Campos}";
                var respuesta = await ObtenerObjetosAsync(url);

                if (respuesta?.Objetos is null || respuesta.Objetos.Count == 0)
                {
                    break;
                }

                paises.AddRange(respuesta.Objetos.Select(Mapear));
                offset += respuesta.Objetos.Count;

                if (respuesta.Meta?.More != true)
                {
                    break;
                }
            }

            return paises;
        }

        /// <summary>
        /// Actividad 1: consulta un país específico (búsqueda por nombre).
        /// </summary>
        public async Task<Pais?> ObtenerPaisPorNombreAsync(string nombre)
        {
            if (string.IsNullOrWhiteSpace(nombre))
            {
                return null;
            }

            var url = $"{RecursoPaises}/name?q={Uri.EscapeDataString(nombre.Trim())}&response_fields={Campos}";
            var respuesta = await ObtenerObjetosAsync(url);

            var primero = respuesta?.Objetos?.FirstOrDefault();
            return primero is null ? null : Mapear(primero);
        }

        private async Task<DataDto?> ObtenerObjetosAsync(string url)
        {
            using var request = new HttpRequestMessage(HttpMethod.Get, url);
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _apiKey);

            using var response = await _httpClient.SendAsync(request);

            if (response.StatusCode == HttpStatusCode.NotFound)
            {
                return null;
            }

            if (response.StatusCode == HttpStatusCode.Unauthorized)
            {
                throw new HttpRequestException(
                    "La API rechazó la clave RESTCOUNTRIES_API_KEY (401). Verifique el archivo .development.env.");
            }

            if (!response.IsSuccessStatusCode)
            {
                throw new HttpRequestException(
                    $"Error al consumir REST Countries: {(int)response.StatusCode} {response.ReasonPhrase}");
            }

            var resultado = await response.Content
                .ReadFromJsonAsync<RespuestaDto>(OpcionesJson());

            return resultado?.Data;
        }

        private static JsonSerializerOptions OpcionesJson() => new()
        {
            PropertyNameCaseInsensitive = true
        };

        private static Pais Mapear(RestCountryDto dto) => new()
        {
            Nombre = dto.Names?.Common ?? "Sin nombre",
            Capital = dto.Capitals?.FirstOrDefault()?.Name ?? "Sin capital",
            Region = dto.Region ?? "N/D",
            SubRegion = dto.SubRegion ?? "N/D",
            Poblacion = dto.Population,
            Area = dto.Area?.Kilometers ?? 0,
            Moneda = dto.Currencies is { Count: > 0 }
                ? string.Join(", ", dto.Currencies
                    .Select(c => string.IsNullOrWhiteSpace(c.Name) ? c.Code : c.Name)
                    .Where(n => !string.IsNullOrWhiteSpace(n)))
                : "N/D"
        };

        // ===== DTOs de deserialización de REST Countries v5 =====

        private sealed class RespuestaDto
        {
            [JsonPropertyName("data")]
            public DataDto? Data { get; set; }
        }

        private sealed class DataDto
        {
            [JsonPropertyName("objects")]
            public List<RestCountryDto>? Objetos { get; set; }

            [JsonPropertyName("meta")]
            public MetaDto? Meta { get; set; }
        }

        private sealed class MetaDto
        {
            [JsonPropertyName("more")]
            public bool More { get; set; }
        }

        private sealed class RestCountryDto
        {
            [JsonPropertyName("names")]
            public NamesDto? Names { get; set; }

            [JsonPropertyName("capitals")]
            public List<CapitalDto>? Capitals { get; set; }

            [JsonPropertyName("region")]
            public string? Region { get; set; }

            [JsonPropertyName("subregion")]
            public string? SubRegion { get; set; }

            [JsonPropertyName("population")]
            public long Population { get; set; }

            [JsonPropertyName("area")]
            public AreaDto? Area { get; set; }

            [JsonPropertyName("currencies")]
            public List<CurrencyDto>? Currencies { get; set; }
        }

        private sealed class NamesDto
        {
            [JsonPropertyName("common")]
            public string? Common { get; set; }
        }

        private sealed class CapitalDto
        {
            [JsonPropertyName("name")]
            public string? Name { get; set; }
        }

        private sealed class AreaDto
        {
            [JsonPropertyName("kilometers")]
            public double Kilometers { get; set; }
        }

        private sealed class CurrencyDto
        {
            [JsonPropertyName("code")]
            public string? Code { get; set; }

            [JsonPropertyName("name")]
            public string? Name { get; set; }
        }
    }
}
