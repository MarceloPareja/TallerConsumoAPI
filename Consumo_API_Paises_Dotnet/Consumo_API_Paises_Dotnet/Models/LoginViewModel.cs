using System.ComponentModel.DataAnnotations;

namespace Consumo_API_Paises_Dotnet.Models
{
    public class LoginViewModel
    {
        [Required(ErrorMessage = "Ingrese el usuario.")]
        [Display(Name = "Usuario")]
        public string Usuario { get; set; } = string.Empty;

        [Required(ErrorMessage = "Ingrese la clave.")]
        [DataType(DataType.Password)]
        [Display(Name = "Clave")]
        public string Clave { get; set; } = string.Empty;

        public string? ReturnUrl { get; set; }
    }
}
