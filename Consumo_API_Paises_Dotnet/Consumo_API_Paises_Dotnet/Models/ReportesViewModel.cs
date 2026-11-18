namespace Consumo_API_Paises_Dotnet.Models
{
    public class RegionReporte
    {
        public string Region { get; set; } = string.Empty;
        public int Total { get; set; }
    }

    public class RegionPromedio
    {
        public string Region { get; set; } = string.Empty;
        public double PromedioPoblacion { get; set; }
    }

    public class ReportesViewModel
    {
        public List<RegionReporte> PaisesPorRegion { get; set; } = new();
        public Pais? PaisMasPoblado { get; set; }
        public List<RegionPromedio> PromedioPorRegion { get; set; } = new();
    }
}
