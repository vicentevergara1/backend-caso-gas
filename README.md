# Gas El Volcán — API Spring Boot (Evaluación Parcial 3)

Backend Java 17+ / Spring Boot 3.2.4 para la tienda React. Incluye MySQL/JPA, REST CRUD, Swagger, JWT, roles, sesiones renovables y boletas con detalle.

## Antes de iniciar

1. Tener Java 17 o Java 21, Maven 3.9+ y MySQL iniciado. También puedes abrir `pom.xml` directamente en IntelliJ IDEA o NetBeans.
2. Crear la base de datos (en MySQL Workbench o phpMyAdmin):

   ```sql
   CREATE DATABASE IF NOT EXISTS gas_el_volcan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. Configurar variables de entorno según `.env.example`. **El archivo `.env` no se importa automáticamente por Spring Boot**: define estas variables en la terminal o en la configuración Run del IDE. Es imprescindible `JWT_SECRET_BASE64`, un secreto nuevo y aleatorio de 32 bytes codificado en Base64. En Windows puedes ejecutar `powershell -File scripts/generar-secret.ps1` y copiar el resultado a `JWT_SECRET_BASE64` dentro de `.env`.
4. Para crear el usuario administrativo en la primera ejecución, define `ADMIN_EMAIL` y `ADMIN_PASSWORD` (mínimo 8 caracteres). De igual forma `SELLER_EMAIL` y `SELLER_PASSWORD` para vendedor. No publiques esas claves ni las incluyas en capturas.
5. Iniciar desde la raíz de este repositorio:

   ```bash
   mvn spring-boot:run
   ```

   En Windows también puedes ejecutar `Copy-Item .env.example .env` desde PowerShell, completar los datos del nuevo `.env` y ejecutar `powershell -File scripts/iniciar-backend.ps1`, que carga esas variables antes de iniciar Maven.

6. Abrir Swagger: `http://localhost:8080/swagger-ui/index.html`. En Authorize usa el **access token** devuelto por `/api/v1/auth/login` (Bearer JWT). La documentación describe también las rutas públicas.

## Roles

| Rol | Acceso |
| --- | --- |
| ADMIN | CRUD de productos, visualización de órdenes y usuarios, tienda |
| VENDEDOR | Lectura de catálogo, listados y detalles de órdenes; no puede modificar productos |
| CLIENTE | Catálogo, compra simulada y consulta de sus pedidos |
| Invitado | Consulta del catálogo, registro e inicio de sesión |

Los permisos se comprueban en **Spring Security**, incluso si alguien intenta llamar a un endpoint directamente.

## Autenticación y seguridad

- `POST /api/v1/auth/register`: registro de clientes (no permite autoasignarse rol admin).
- `POST /api/v1/auth/login`: verifica la contraseña con BCrypt y devuelve JWT de 15 minutos.
- `POST /api/v1/auth/refresh`: renueva la sesión (7 días) con cookie HttpOnly y rota el token de refresco almacenado como hash SHA-256.
- `POST /api/v1/auth/logout`: invalida el token de renovación y expira la cookie.
- `GET /api/v1/auth/me`: consulta los datos del usuario autenticado.

Para desarrollo local con Vite en `http://localhost:5173`, usa `FRONTEND_ORIGIN=http://localhost:5173`, `COOKIE_SAME_SITE=Lax` y `COOKIE_SECURE=false`. En una implementación HTTPS con frontend/backend en sitios distintos, configura el origen exacto, `COOKIE_SAME_SITE=None`, `COOKIE_SECURE=true` y HTTPS **antes de publicar**. No uses orígenes `*` con credenciales.

**No hay pasarela de pago.** `POST /api/v1/orders` registra una boleta `PENDIENTE`, usa precios obtenidos de la base de datos y descuenta stock de forma transaccional. No recibe ni guarda datos reales de tarjeta.

## Estructura

El proyecto se organiza en paquetes, como en NetBeans o IntelliJ:

```text
src/main/java/cl/duoc/gaselvolcan/
├── GasElVolcanApplication.java
├── config/
│   ├── InitialData.java
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AdminController.java
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── OrderController.java
│   └── ProductController.java
├── dto/
│   └── ApiModels.java
├── exception/
│   └── ApiErrors.java
├── model/
│   ├── Category.java
│   ├── OrderEntity.java
│   ├── OrderLine.java
│   ├── Product.java
│   ├── RefreshSession.java
│   └── UserAccount.java
├── repository/
│   ├── CategoryRepository.java
│   ├── OrderRepository.java
│   ├── ProductRepository.java
│   ├── RefreshSessionRepository.java
│   └── UserRepository.java
└── services/
    ├── AuthenticationService.java
    ├── OrderService.java
    └── ProductService.java
```

- `model`: entidades JPA relacionadas con las tablas MySQL.
- `repository`: interfaces JPA para consultar y guardar datos.
- `services`: lógica de negocio, autenticación y compras.
- `controller`: endpoints REST que utiliza React.
- `dto`: datos de entrada y salida de la API, con validaciones.
- `exception`: respuestas consistentes ante errores de la API.
- `config`: configuración de JWT, CORS, Swagger y datos iniciales. `InitialData` crea las cuatro categorías y los 14 productos de ejemplo si la base está vacía.

`GasElVolcanApplication` permanece en `cl.duoc.gaselvolcan` para que Spring Boot descubra los componentes de todos los subpaquetes. Los nombres de los endpoints y los datos que recibe React no cambiaron.

## Pruebas

```bash
mvn test
```

Incluye pruebas con MockMvc/H2 para catálogo público, denegación anónima y prohibición de acceso de Cliente al CRUD administrativo. Se recomienda complementar con pruebas manuales end-to-end y evidencias de Swagger antes de la entrega.

## Errores comunes

- `JWT_SECRET_BASE64`: generar un secreto real de 32 bytes y cargar la variable antes de iniciar.
- `Communications link failure`: revisar MySQL, DB_URL, DB_USER y DB_PASSWORD.
- `401`: iniciar sesión o renovar token.
- `403`: usuario autenticado, pero rol insuficiente.
- CORS/cookies: comprobar el origen exacto, HTTP/HTTPS y `COOKIE_SAME_SITE`.

## Crear un repositorio independiente en GitHub

Este ZIP contiene **solo el backend**. `pom.xml`, `src/`, `.gitignore` y `README.md` están en la raíz, por lo que no debes subirlo dentro de una carpeta `backend/` del repositorio de React.

1. En GitHub, crea un repositorio vacío llamado, por ejemplo, `CASO-GAS-BACKEND` en tu cuenta `vicentevergara1`. No selecciones README ni `.gitignore` en GitHub, porque este proyecto ya los incluye.
2. Abre una terminal desde la carpeta descomprimida `gas-el-volcan-backend` y ejecuta:

   ```bash
   git init
   git add .
   git commit -m "Backend Spring Boot Gas El Volcan EP3"
   git branch -M main
   git remote add origin https://github.com/vicentevergara1/CASO-GAS-BACKEND.git
   git push -u origin main
   ```

3. Comprueba que en GitHub aparezca `pom.xml` directamente en el primer nivel y no aparezca `.env` ni `target/`.
4. El archivo `.github/workflows/backend-ci.yml` ejecuta pruebas del backend en cada push a `main`. La ejecución real debe revisarse en la pestaña Actions; no confundas tener el workflow con haber aprobado los tests.
5. Usa el enlace público de este repositorio como **enlace del backend** en la entrega. Tu repositorio React sigue siendo independiente.

Si lo subes a un servicio de hosting, define allí las variables de entorno y configura `FRONTEND_ORIGIN` con el origen exacto de tu web. GitHub Pages aloja el frontend, pero no ejecuta una aplicación Java Spring Boot.

## Diagnóstico de publicación (noviembre 2026)

El fallo original de GitHub Pages se relaciona con una URL de API anterior compilada en Vite, no con un error demostrado de Spring Boot. Se añadieron pruebas automáticas de categorías y CORS, sin modificar la lógica productiva. Consulta `docs/DIAGNOSTICO_EC2_HTTPS.md` antes de actualizar un servicio en EC2. El backend requiere redeploy explícito para usar un nuevo JAR; un push a GitHub no actualiza por sí solo el proceso Java instalado en AWS.
