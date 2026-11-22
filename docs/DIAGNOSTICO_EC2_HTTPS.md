# Gas El Volcán — Diagnóstico del backend en EC2 (sin cambios destructivos)

## Estado documentado antes del ZIP

- Spring Boot en EC2 respondió `HTTP 200` para `/api/v1/products` desde localhost:8080.
- Caddy en el dominio `54.242.154.29.nip.io` respondió `HTTP 200` en pruebas previas.
- Un OPTIONS local respondió con CORS para `https://vicentevergara1.github.io`.
- El frontend de GitHub Pages, compilado el 18/11, utilizaba la **IP antigua** `3.80.86.195.nip.io`. Eso no es un fallo de Spring Boot.
- EC2 estaba en una versión anterior (`4c6cf0a`); subir este ZIP o hacer push del repositorio **no** significa que el servidor haya actualizado su JAR.

## Antes de cambiar código o JAR

**GitHub**: corregir primero frontend en `.env.production` y revisar la validación de Actions.

**AWS / EC2**: consultar la IP pública asociada a la instancia, la asociación de Elastic IP (si existe), y los security groups. No cambiar IP solo para experimentar.

**Terminal SSH de EC2** (solo lectura):

```bash
sudo systemctl status gas-el-volcan --no-pager
sudo systemctl status caddy --no-pager
curl -sS -o /dev/null -w 'Backend: HTTP %{http_code}\n' http://127.0.0.1:8080/api/v1/products
sudo /usr/local/bin/caddy validate --config /etc/caddy/Caddyfile
```

**Windows PowerShell**, desde una conexión que no bloquee `nip.io` (reemplace la IP si AWS confirma otra):

```powershell
curl.exe -i https://54.242.154.29.nip.io/api/v1/products
curl.exe -i https://54.242.154.29.nip.io/api/v1/categories
```

No añadir `-k` a las pruebas públicas porque ocultaría un error real del certificado.

## Sesiones y autenticación

Para la web publicada, el backend debe tener `FRONTEND_ORIGIN=https://vicentevergara1.github.io`, `COOKIE_SECURE=true`, `COOKIE_SAME_SITE=None` si ambos sitios HTTPS están en dominios distintos. Revisar la configuración efectiva del **servicio en EC2**, no asumirla desde `.env.example`. Los navegadores pueden bloquear cookies de terceros; se debe probar con login y recarga de página antes de afirmar que la sesión persiste.

La implementación actual mantiene Spring Security, JWT y controles de roles. Este ZIP añade **solo** pruebas de regresión de categorías y CORS; no cambia los endpoints ni las credenciales, evita un redeploy innecesario para arreglar la URL antigua.
