using Consumo_API_Paises_Dotnet.Models;
using Microsoft.EntityFrameworkCore.Infrastructure;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable
namespace Consumo_API_Paises_Dotnet.Migrations;

[DbContext(typeof(AppDbContext))]
[Migration("20261122000100_AgregarUniversidades")]
public partial class AgregarUniversidades : Migration
{
    protected override void Up(MigrationBuilder migrationBuilder)
    {
        migrationBuilder.CreateTable(
            name: "Universidades",
            columns: table => new
            {
                Id = table.Column<int>(type: "int", nullable: false).Annotation("SqlServer:Identity", "1, 1"),
                Nombre = table.Column<string>(type: "nvarchar(300)", maxLength: 300, nullable: false),
                Dominio = table.Column<string>(type: "nvarchar(2000)", maxLength: 2000, nullable: false),
                SitioWeb = table.Column<string>(type: "nvarchar(4000)", maxLength: 4000, nullable: false),
                Pais = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: false)
            },
            constraints: table => table.PrimaryKey("PK_Universidades", x => x.Id));
        migrationBuilder.CreateIndex(name: "IX_Universidades_Pais_Nombre", table: "Universidades",
            columns: new[] { "Pais", "Nombre" }, unique: true);
    }
    protected override void Down(MigrationBuilder migrationBuilder) => migrationBuilder.DropTable("Universidades");
}
