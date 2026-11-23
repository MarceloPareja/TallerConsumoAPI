namespace Consumo_API_Paises_Dotnet.Infrastructure;

public static class VariablesEntorno
{
    public static void Cargar()
    {
        // El entorno del proceso (Docker/servidor) siempre prevalece.
        var existentes = Environment.GetEnvironmentVariables().Keys.Cast<string>().ToHashSet();
        var leidas = new HashSet<string>();
        foreach (var nombre in new[] { ".env", ".development.env" })
        foreach (var inicio in new[] { Directory.GetCurrentDirectory(), AppContext.BaseDirectory })
        {
            var directorio = new DirectoryInfo(inicio);
            for (var nivel = 0; nivel < 6 && directorio is not null; nivel++, directorio = directorio.Parent)
            {
                {
                    var archivo = Path.Combine(directorio.FullName, nombre);
                    if (!File.Exists(archivo)) continue;
                    foreach (var linea in File.ReadLines(archivo))
                    {
                        var texto = linea.Trim().TrimStart('\uFEFF');
                        if (texto.Length == 0 || texto.StartsWith('#')) continue;
                        var partes = texto.Split('=', 2);
                        if (partes.Length != 2) continue;
                        var clave = partes[0].Trim();
                        if (clave.Length == 0 || existentes.Contains(clave) || !leidas.Add(clave)) continue;
                        Environment.SetEnvironmentVariable(clave, partes[1].Trim().Trim('"', '\''));
                    }
                }
            }
        }
    }
}
