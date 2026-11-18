using Consumo_API_Paises_Dotnet.Models;

namespace Consumo_API_Paises_Dotnet.Services
{
    /// <summary>
    /// Abstracción del consumo de la API de países (principio de inversión de dependencias).
    /// </summary>
    public interface IPaisService
    {
        Task<List<Pais>> ObtenerPaisesAsync();
        Task<Pais?> ObtenerPaisPorNombreAsync(string nombre);
    }
}
