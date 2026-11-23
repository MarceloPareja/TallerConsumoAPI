using System.ComponentModel.DataAnnotations;

namespace Consumo_API_Paises_Dotnet.Models;

public class Universidad
{
    public int Id { get; set; }
    [Required, StringLength(300)]
    public string Nombre { get; set; } = string.Empty;
    [StringLength(2000)]
    public string Dominio { get; set; } = string.Empty;
    [StringLength(4000), Display(Name = "Sitio web")]
    public string SitioWeb { get; set; } = string.Empty;
    [Required, StringLength(100), Display(Name = "País")]
    public string Pais { get; set; } = string.Empty;
}
