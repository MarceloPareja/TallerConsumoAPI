using Consumo_API_Paises_Dotnet.Infrastructure;
using Consumo_API_Paises_Dotnet.Models;
using Consumo_API_Paises_Dotnet.Services;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.Data.SqlClient;
using Microsoft.EntityFrameworkCore;

VariablesEntorno.Cargar();
var builder = WebApplication.CreateBuilder(args);
builder.Services.AddControllersWithViews(options => options.SuppressImplicitRequiredAttributeForNonNullableReferenceTypes = true);
builder.Services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme).AddCookie(options =>
{
    options.LoginPath = "/Cuenta/IniciarSesion";
    options.AccessDeniedPath = "/Cuenta/IniciarSesion";
    options.Cookie.Name = "Paises.Auth";
    options.SlidingExpiration = true;
    options.ExpireTimeSpan = TimeSpan.FromHours(8);
});
builder.Services.AddAuthorization(options =>
    options.FallbackPolicy = new Microsoft.AspNetCore.Authorization.AuthorizationPolicyBuilder()
        .RequireAuthenticatedUser().Build());

var conexion = builder.Configuration.GetConnectionString("SqlServerConnection");
if (string.IsNullOrWhiteSpace(conexion))
{
    var password = builder.Configuration["DB_PASSWORD"];
    conexion = new SqlConnectionStringBuilder
    {
        DataSource = builder.Configuration["DB_HOST"] ?? "127.0.0.1,1433",
        InitialCatalog = builder.Configuration["DB_NAME"] ?? "ApiPaisesDB",
        UserID = builder.Configuration["DB_USER"] ?? "sa",
        Password = string.IsNullOrEmpty(password) ? builder.Configuration["MSSQL_SA_PASSWORD"] ?? "" : password,
        TrustServerCertificate = true,
        ConnectTimeout = 15
    }.ConnectionString;
}
builder.Services.AddDbContext<AppDbContext>(options => options.UseSqlServer(conexion));
builder.Services.AddHttpClient<IPaisService, PaisApiService>(client =>
{
    client.BaseAddress = new Uri(builder.Configuration["RESTCOUNTRIES_BASE_URL"] ?? "https://api.restcountries.com/");
    client.Timeout = TimeSpan.FromSeconds(30);
});
builder.Services.AddHttpClient<IUniversidadService, UniversidadApiService>(client =>
{
    client.BaseAddress = new Uri(builder.Configuration["UNIVERSIDADES_BASE_URL"] ?? "http://universities.hipolabs.com/");
    client.Timeout = TimeSpan.FromSeconds(30);
});
builder.Services.AddScoped<ImportacionService>();
builder.Services.AddHostedService<CargaInicialService>();
var app = builder.Build();

if (builder.Configuration.GetValue("APPLY_MIGRATIONS", true))
{
    using var scope = app.Services.CreateScope();
    var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    // Compose espera el healthcheck; estos reintentos también permiten arrancar localmente.
    for (var intento = 1; ; intento++)
    {
        try { await db.Database.MigrateAsync(); break; }
        catch (SqlException) when (intento < 12)
        {
            app.Logger.LogWarning("SQL Server no está disponible todavía. Intento {Intento}/12.", intento);
            await Task.Delay(TimeSpan.FromSeconds(5));
        }
    }
}
if (!app.Environment.IsDevelopment()) app.UseExceptionHandler("/Home/Error");
// El contenedor publica HTTP local. Un despliegue HTTPS puede usar un proxy inverso.
app.UseRouting();
app.UseAuthentication();
app.UseAuthorization();
app.MapStaticAssets().AllowAnonymous();
app.MapGet("/health", async (AppDbContext db) =>
    await db.Database.CanConnectAsync() ? Results.Ok(new { estado = "listo" }) : Results.StatusCode(503)).AllowAnonymous();
app.MapControllerRoute("default", "{controller=Pais}/{action=Index}/{id?}").WithStaticAssets();
app.Run();
