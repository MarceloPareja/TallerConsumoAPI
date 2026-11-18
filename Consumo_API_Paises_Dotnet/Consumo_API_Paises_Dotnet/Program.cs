using Consumo_API_Paises_Dotnet.Models;
using Consumo_API_Paises_Dotnet.Services;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.EntityFrameworkCore;

CargarVariablesDesarrollo();

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllersWithViews();
builder.Services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(options =>
    {
        options.LoginPath = "/Cuenta/IniciarSesion";
        options.AccessDeniedPath = "/Cuenta/IniciarSesion";
        options.Cookie.Name = "Paises.Auth";
        options.SlidingExpiration = true;
        options.ExpireTimeSpan = TimeSpan.FromHours(8);
    });
builder.Services.AddAuthorization(options =>
{
    options.FallbackPolicy = new Microsoft.AspNetCore.Authorization.AuthorizationPolicyBuilder()
        .RequireAuthenticatedUser()
        .Build();
});

builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlServer(
        builder.Configuration.GetConnectionString("SqlServerConnection")));

// Cliente HTTP tipado para consumir REST Countries (v5).
builder.Services.AddHttpClient<IPaisService, PaisApiService>(client =>
{
    client.BaseAddress = new Uri("https://api.restcountries.com/");
});

var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseRouting();

app.UseAuthentication();
app.UseAuthorization();

app.MapStaticAssets().AllowAnonymous();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Pais}/{action=Index}/{id?}")
    .WithStaticAssets();

app.Run();

/// <summary>
/// Carga las variables del archivo .development.env (por ejemplo RESTCOUNTRIES_API_KEY)
/// para que estén disponibles en IConfiguration.
/// </summary>
static void CargarVariablesDesarrollo()
{
    var archivos = new List<string>();

    foreach (var inicio in new[] { Directory.GetCurrentDirectory(), AppContext.BaseDirectory })
    {
        var directorio = new DirectoryInfo(inicio);

        // Busca .development.env en el directorio actual y hasta 5 niveles superiores,
        // de modo que funcione tanto con `dotnet run` como desde Visual Studio.
        for (var nivel = 0; nivel < 5 && directorio is not null; nivel++, directorio = directorio.Parent)
        {
            archivos.Add(Path.Combine(directorio.FullName, ".development.env"));
        }
    }

    foreach (var archivo in archivos.Distinct().Where(File.Exists))
    {
        foreach (var linea in File.ReadAllLines(archivo))
        {
            var texto = linea.Trim();
            if (texto.Length == 0 || texto.StartsWith('#'))
            {
                continue;
            }

            var partes = texto.Split('=', 2);
            if (partes.Length != 2)
            {
                continue;
            }

            var clave = partes[0].Trim();
            var valor = partes[1].Trim().Trim('"', '\'');

            if (!string.IsNullOrEmpty(clave))
            {
                Environment.SetEnvironmentVariable(clave, valor);
            }
        }
    }
}
