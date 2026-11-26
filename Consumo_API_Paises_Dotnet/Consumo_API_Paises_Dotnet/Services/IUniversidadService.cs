using Consumo_API_Paises_Dotnet.Models;

namespace Consumo_API_Paises_Dotnet.Services;

public interface IUniversidadService
{
    Task<List<Universidad>> ObtenerPorPaisAsync(string pais);
}
