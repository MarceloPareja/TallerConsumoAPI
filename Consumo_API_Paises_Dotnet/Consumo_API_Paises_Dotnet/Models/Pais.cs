using System.ComponentModel.DataAnnotations;

namespace Consumo_API_Paises_Dotnet.Models
{
    public class Pais
    {
        public int Id { get; set; }

        [Required(ErrorMessage = "El nombre es obligatorio")]
        [StringLength(100)]
        public string Nombre { get; set; } = string.Empty;

        [StringLength(100)]
        public string Capital { get; set; } = string.Empty;

        [StringLength(100)]
        public string Region { get; set; } = string.Empty;

        [StringLength(100)]
        public string SubRegion { get; set; } = string.Empty;

        public long Poblacion { get; set; }

        public double Area { get; set; }

        [StringLength(100)]
        public string Moneda { get; set; } = string.Empty;
    }
}
