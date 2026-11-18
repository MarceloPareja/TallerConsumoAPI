using Microsoft.EntityFrameworkCore;

namespace Consumo_API_Paises_Dotnet.Models
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options)
            : base(options)
        {
        }

        public DbSet<Pais> Paises { get; set; }
    }
}
