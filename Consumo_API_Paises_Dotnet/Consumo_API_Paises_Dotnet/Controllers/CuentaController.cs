using System.Security.Claims;
using Consumo_API_Paises_Dotnet.Models;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Consumo_API_Paises_Dotnet.Controllers
{
    public class CuentaController : Controller
    {
        private const string UsuarioValido = "MONSTER";
        private const string ClaveValida = "MONSTER9";

        [AllowAnonymous]
        [HttpGet]
        public IActionResult IniciarSesion(string? returnUrl = null)
        {
            if (User.Identity?.IsAuthenticated == true)
            {
                return RedirectToAction("Index", "Pais");
            }

            return View(new LoginViewModel { ReturnUrl = returnUrl });
        }

        [AllowAnonymous]
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> IniciarSesion(LoginViewModel modelo)
        {
            if (modelo.Usuario != UsuarioValido || modelo.Clave != ClaveValida)
            {
                ModelState.AddModelError(string.Empty, "El usuario o la clave no son correctos.");
                return View(modelo);
            }

            var identidad = new ClaimsIdentity(
                new[] { new Claim(ClaimTypes.Name, UsuarioValido) },
                CookieAuthenticationDefaults.AuthenticationScheme);

            await HttpContext.SignInAsync(
                CookieAuthenticationDefaults.AuthenticationScheme,
                new ClaimsPrincipal(identidad));

            if (!string.IsNullOrWhiteSpace(modelo.ReturnUrl) && Url.IsLocalUrl(modelo.ReturnUrl))
            {
                return Redirect(modelo.ReturnUrl);
            }

            return RedirectToAction("Index", "Pais");
        }

        [Authorize]
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> CerrarSesion()
        {
            await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
            return RedirectToAction(nameof(IniciarSesion));
        }
    }
}
