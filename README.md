# Taller Mecánico "El Último Arranque" — Backend

API REST en **Spring Boot** para la gestión de órdenes de trabajo del taller: clientes, vehículos, mecánicos, repuestos, servicios, órdenes, comprobantes y reportes en PDF. Persistencia en **MySQL** mediante **JDBC explícito** (`DataSource` + `PreparedStatement`, transacciones manuales con `commit`/`rollback`). Seguridad con **Spring Security + JWT**, roles y bitácora de auditoría.

## Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 21 |
| Maven | 3.9 o superior (o el que trae IntelliJ / NetBeans) |
| MySQL | 8.0.16 o superior (necesario para las restricciones `CHECK`) |

Dependencias principales: Spring Boot 3.5.16 (Web, JDBC, Validation, Security, OAuth2 Resource Server), MySQL Connector/J, springdoc-openapi 2.9.1, OpenPDF 3.0.5.

## Puesta en marcha

1. Crear la base de datos y cargar los datos de prueba:
   ```bash
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS taller_ultimo_arranque CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci"
   mysql -u root -p taller_ultimo_arranque < database/01_esquema.sql
   mysql -u root -p taller_ultimo_arranque < database/02_datos_prueba.sql
   ```
2. Configurar credenciales (por defecto `root` / `root` en `localhost:3306`). Se pueden cambiar con variables de entorno, sin editar código:
   ```bash
   export DB_USER=root DB_PASSWORD=mi_clave DB_HOST=localhost DB_PORT=3306
   ```
3. Ejecutar:
   ```bash
   mvn spring-boot:run
   ```
4. Documentación interactiva: <http://localhost:8080/swagger-ui.html>. Inicie sesión con `POST /api/auth/login`, copie el `token` y péguelo en el botón **Authorize**.

Para pruebas unitarias: `mvn test`.

## Despliegue gratuito (Render + Aiven)

1. **Base de datos en Aiven** (plan gratis de MySQL). Con los datos de conexión del panel de Aiven, cargue los scripts en la base `defaultdb`:
   ```bash
   mysql -h <host> -P <puerto> -u avnadmin -p --ssl-mode=REQUIRED defaultdb < database/01_esquema.sql
   mysql -h <host> -P <puerto> -u avnadmin -p --ssl-mode=REQUIRED defaultdb < database/02_datos_prueba.sql
   ```
2. **Aplicación en Render**: suba el repositorio a GitHub y cree un *Web Service* (plan Free, runtime Docker). Render detecta el `Dockerfile`.
3. Variables de entorno en Render:

   | Variable | Valor |
   |---|---|
   | `DB_HOST` / `DB_PORT` | Host y puerto de Aiven |
   | `DB_NAME` | `defaultdb` |
   | `DB_USER` / `DB_PASSWORD` | Usuario y contraseña de Aiven |
   | `DB_SSL` | `true` |
   | `JWT_SECRET` | Cadena aleatoria de 32 caracteres o más (por ejemplo `openssl rand -base64 48`) |

   Render asigna el puerto con la variable `PORT`, que la aplicación ya lee.
4. Swagger queda en `https://<servicio>.onrender.com/swagger-ui.html`. En el plan gratis el servicio se apaga tras unos 15 minutos sin tráfico; la primera solicitud posterior tarda alrededor de un minuto.

Prueba local de la imagen: `docker build -t taller . && docker run -p 8080:8080 -e DB_HOST=host.docker.internal taller`.

## Configuración (`application.properties`)

| Propiedad | Valor por defecto | Uso |
|---|---|---|
| `taller.inventario.umbral-bajo` | `5` | Existencia máxima para considerar un repuesto con bajo inventario |
| `taller.facturacion.tasa-iva` | `0.12` | IVA que se suma al subtotal (mano de obra + repuestos) |
| `taller.cors.origenes-permitidos` | `*` | Orígenes permitidos para el front web |
| `taller.seguridad.jwt.secreto` | variable `JWT_SECRET` (valor de desarrollo por defecto) | Clave HMAC-SHA256 para firmar tokens, mínimo 32 caracteres |
| `taller.seguridad.jwt.duracion` | `30m` | Vigencia del token |
| `taller.seguridad.jwt.emisor` | `taller-ultimo-arranque` | Emisor (`iss`) validado en cada solicitud |

## Seguridad

### Usuarios de prueba

| Usuario | Contraseña | Rol | Mecánico vinculado |
|---|---|---|---|
| `admin` | `Admin2026!` | ADMIN | — |
| `recepcion` | `Recepcion2026!` | RECEPCION | — |
| `lramirez` | `Mecanico2026!` | MECANICO | MEC-001 |
| `paju` | `Mecanico2026!` | MECANICO | MEC-002 |

### Funcionamiento

1. `POST /api/auth/login` valida usuario y contraseña (BCrypt) y devuelve un JWT firmado con HS256, válido 30 minutos.
2. Cada solicitud envía `Authorization: Bearer <token>`. Spring Security valida firma, expiración y emisor.
3. Además, en cada solicitud se consulta el usuario en la BD: si fue **desactivado**, cambió su **contraseña** o su **rol**, el token anterior deja de ser válido de inmediato (campo `version_token`).
4. Al vencer el token se debe iniciar sesión otra vez (no hay renovación automática).

### Permisos por rol

| Operación | ADMIN | RECEPCION | MECANICO |
|---|:-:|:-:|:-:|
| Usuarios y bitácora | ✔ | | |
| Clientes, vehículos, mecánicos | ✔ | ✔ | |
| Repuestos y servicios: consultar | ✔ | ✔ | ✔ |
| Repuestos y servicios: registrar, modificar, abastecer | ✔ | ✔ | |
| Órdenes: consultar | ✔ | ✔ | Solo las suyas |
| Órdenes: crear, modificar datos, cancelar | ✔ | ✔ | |
| Órdenes: agregar / quitar servicios | ✔ | ✔ | Solo las suyas |
| Órdenes: iniciar, asignar / retirar repuestos, finalizar | ✔ | | Solo las suyas |
| Reportes | ✔ | ✔ | |
| Perfil y cambio de contraseña propia | ✔ | ✔ | ✔ |

Reglas adicionales: un usuario MECANICO siempre está vinculado a un registro de `mecanico` (uno a uno); un ADMIN no puede cambiar su propio rol ni desactivarse; los usuarios no se eliminan, se desactivan para conservar la trazabilidad.

### Auditoría

- Cada orden guarda quién la **registró**, quién la **inició** y quién la **cerró** (finalizó o canceló). Se muestra en la respuesta JSON y en el comprobante PDF.
- La tabla `bitacora` registra usuario, acción, entidad, identificador, detalle, fecha e IP de cada operación de escritura, inicios de sesión y los intentos fallidos.
- La entrada de bitácora se escribe **en la misma transacción** que la operación: si la operación se revierte, tampoco queda registrada.

## Arquitectura (MVC)

```
gt.umg.ultimoarranque
├── controlador     Controlador: endpoints REST, manejo global de errores
├── seguridad       Spring Security, emisión/validación de JWT, usuario autenticado
├── vista
│   └── pdf         Vista: documentos PDF (comprobante y reportes)
├── dto             Vista: contratos JSON de entrada y salida (records)
├── servicio        Modelo: lógica de negocio y control transaccional
├── dao             Modelo: acceso a datos con JDBC y PreparedStatement
│   └── jdbc        JdbcDao<T>, GestorTransacciones, interfaces funcionales
├── modelo          Modelo: entidades de dominio y reglas
│   ├── documento   NIT / DPI con validación de dígito verificador
│   ├── orden       OrdenTrabajo + patrón State + detalles + comprobante
│   ├── bitacora    Acciones y entradas de auditoría
│   └── consulta    Modelos de lectura para reportes
├── excepcion       Jerarquía de excepciones del negocio
└── config          Propiedades, CORS, OpenAPI
```

Diagramas en `docs/`: clases del modelo, arquitectura, excepciones, estados de la orden y entidad-relación (fuente `.puml` y `.png`).

## Ciclo de vida de una orden

```
PENDIENTE ──iniciar──► EN_PROCESO ──finalizar──► FINALIZADA
    │                      │
    └──────cancelar────────┴──────────────────► CANCELADA
```

| Acción | PENDIENTE | EN_PROCESO | FINALIZADA / CANCELADA |
|---|---|---|---|
| Modificar mecánico / descripción | ✔ | ✔ | ✘ |
| Agregar / quitar servicios | ✔ | ✔ | ✘ |
| Asignar / retirar repuestos | ✘ | ✔ | ✘ |
| Finalizar (requiere al menos un servicio) | ✘ | ✔ | ✘ |
| Cancelar (reintegra repuestos) | ✔ | ✔ | ✘ |

## Endpoints

Base: `http://localhost:8080/api`. Todas las rutas, excepto `POST /auth/login`, requieren token.

| Recurso | Método y ruta | Descripción |
|---|---|---|
| Autenticación | `POST /auth/login` | Iniciar sesión, devuelve el token |
| | `GET /auth/perfil` · `POST /auth/cambiar-password` | Perfil propio y cambio de contraseña (devuelve token nuevo) |
| Usuarios (ADMIN) | `GET /usuarios` · `GET /usuarios/{id}` · `POST /usuarios` · `PUT /usuarios/{id}` | Consultar, crear, modificar nombre / rol / mecánico vinculado |
| | `POST /usuarios/{id}/activar` · `POST /usuarios/{id}/desactivar` · `POST /usuarios/{id}/restablecer-password` | Estado y contraseña |
| Bitácora (ADMIN) | `GET /bitacora?idUsuario=&accion=&entidad=&idEntidad=&desde=&hasta=&limite=` | Consultar auditoría (más reciente primero) |
| Clientes | `GET /clientes?buscar=` | Listar / buscar por nombre o documento |
| | `GET /clientes/{id}` · `GET /clientes/{id}/vehiculos` | Consultar cliente y sus vehículos |
| | `POST /clientes` · `PUT /clientes/{id}` · `DELETE /clientes/{id}` | Registrar, modificar, eliminar |
| Vehículos | `GET /vehiculos?placa=` · `GET /vehiculos/{id}` · `GET /vehiculos/placa/{placa}` | Consultar |
| | `GET /vehiculos/{id}/ordenes` | Historial de órdenes |
| | `POST /vehiculos` · `PUT /vehiculos/{id}` · `DELETE /vehiculos/{id}` | Registrar, modificar, eliminar |
| Mecánicos | `GET /mecanicos` · `GET /mecanicos/{id}` · `GET /mecanicos/{id}/ordenes?estado=` | Consultar |
| | `POST /mecanicos` · `PUT /mecanicos/{id}` · `DELETE /mecanicos/{id}` | Registrar, modificar, eliminar |
| Repuestos | `GET /repuestos?nivel=AGOTADO\|BAJO\|DISPONIBLE` · `GET /repuestos/bajo-inventario` · `GET /repuestos/{id}` | Consultar |
| | `POST /repuestos` · `PUT /repuestos/{id}` · `POST /repuestos/{id}/abastecer` | Registrar, modificar, abastecer |
| Servicios | `GET /servicios` · `GET /servicios/{id}` · `POST /servicios` · `PUT /servicios/{id}` | Catálogo de servicios |
| Órdenes | `GET /ordenes?estado=` · `GET /ordenes/{id}` | Listar / detalle con costos |
| | `POST /ordenes` · `PUT /ordenes/{id}` | Crear / modificar datos |
| | `POST /ordenes/{id}/servicios` · `DELETE /ordenes/{id}/servicios/{idServicio}` | Servicios de la orden |
| | `POST /ordenes/{id}/iniciar` | Pasar a EN_PROCESO |
| | `POST /ordenes/{id}/repuestos` · `DELETE /ordenes/{id}/repuestos/{idDetalle}` | Asignar / retirar repuestos |
| | `POST /ordenes/{id}/finalizar` · `POST /ordenes/{id}/cancelar` | Cerrar la orden |
| | `GET /ordenes/{id}/comprobante` · `GET /ordenes/{id}/comprobante/pdf` | Comprobante JSON / PDF |

### Reportes (JSON para Android/Flutter y PDF)

| Reporte | JSON | PDF |
|---|---|---|
| Órdenes por estado | `GET /reportes/ordenes-por-estado?estado=` | `…/pdf` |
| Historial por placa | `GET /reportes/historial-vehiculo/{placa}` | `…/pdf` |
| Repuestos con existencia ≤ umbral | `GET /reportes/repuestos-bajo-inventario` | `…/pdf` |
| Órdenes por mecánico y estado | `GET /reportes/ordenes-por-mecanico?idMecanico=&estado=` | `…/pdf` |
| Ingresos por rango de fechas | `GET /reportes/ingresos?desde=2026-09-01&hasta=2026-10-31` | `…/pdf` |

Desde el emulador de Android, el backend se alcanza en `http://10.0.2.2:8080`.

### Ejemplo de flujo completo

```bash
RECEPCION=$(curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"recepcion","password":"Recepcion2026!"}' | jq -r .token)
MECANICO=$(curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"paju","password":"Mecanico2026!"}' | jq -r .token)

curl -X POST localhost:8080/api/ordenes -H "Authorization: Bearer $RECEPCION" -H "Content-Type: application/json" \
  -d '{"idVehiculo":5,"idMecanico":2,"descripcionProblema":"Pedal de freno esponjoso","serviciosIds":[4]}'
curl -X POST localhost:8080/api/ordenes/13/iniciar -H "Authorization: Bearer $MECANICO"
curl -X POST localhost:8080/api/ordenes/13/repuestos -H "Authorization: Bearer $MECANICO" \
  -H "Content-Type: application/json" -d '{"idRepuesto":8,"cantidad":1}'
curl -X POST localhost:8080/api/ordenes/13/finalizar -H "Authorization: Bearer $MECANICO"
curl -o comprobante.pdf localhost:8080/api/ordenes/13/comprobante/pdf -H "Authorization: Bearer $RECEPCION"
```

### Errores

Todas las respuestas de error usan `application/problem+json` (RFC 7807):

| Código | Causa |
|---|---|
| 400 | Validación de campos, JSON mal formado, NIT/DPI/placa inválidos, contraseña actual incorrecta |
| 401 | Sin token, token inválido o expirado, credenciales incorrectas |
| 403 | El rol no tiene permiso, o el mecánico intenta operar una orden que no es suya |
| 404 | Recurso inexistente |
| 409 | Registro duplicado, eliminación con dependencias, transición de estado no permitida, violación de restricciones de la BD |
| 422 | Regla de negocio incumplida (inventario insuficiente, finalizar sin servicios) |
| 500 | Error de base de datos o inesperado |

## Requerimientos → implementación

| Requerimiento | Implementación |
|---|---|
| a) Clientes, identificador único, no eliminar con vehículos | `ClienteService`, `DocumentoIdentidad` (NIT/DPI), `uq_cliente_documento` |
| b) Vehículos, placa única, historial, no eliminar con órdenes | `VehiculoService`, `uq_vehiculo_placa`, `GET /vehiculos/{id}/ordenes` |
| c) Mecánicos, no eliminar con órdenes | `MecanicoService` |
| d) Repuestos, existencia sin negativos, abastecer, agotados/bajos | `Repuesto` (`descontar`, `abastecer`, `nivelInventario`), `ck_repuesto_existencia` |
| e) Servicios con costo de mano de obra | `ServicioTallerService`, selección opcional al crear la orden |
| f) Flujo de órdenes y transacción de repuestos | `OrdenTrabajo` + `EstadoOrdenTrabajo`, `OrdenTrabajoService` con `SELECT … FOR UPDATE` |
| g) MySQL, JDBC, `PreparedStatement`, `DECIMAL` | `JdbcDao<T>`, `GestorTransacciones`, `database/01_esquema.sql` |
| h) Web MVC | Paquetes `controlador` / `vista` / `servicio` + `dao` + `modelo` |
| i) POO | Ver sección siguiente |
| j) Reportes PDF | `vista.pdf.*` (patrón Template Method en `DocumentoPdf`) |
| k) Android | Reportes en JSON bajo `/api/reportes/**`, protegidos con el mismo token |
| Seguridad y auditoría (adicional) | Paquete `seguridad`, `AutenticacionService`, `UsuarioService`, `Auditoria`, tabla `bitacora` |

### Programación orientada a objetos

| Concepto | Dónde se observa |
|---|---|
| Encapsulamiento | Atributos privados, existencia de `Repuesto` solo cambia vía `abastecer` / `descontar` / `reintegrar`; listas de la orden expuestas como no modificables |
| Herencia | `Entidad → Persona → Cliente / Mecanico / Usuario`; `DocumentoIdentidad → Nit / Dpi`; `EstadoOrdenTrabajo → EstadoActivo → …`; `JdbcDao<T> → *Dao`; `DocumentoPdf → *Pdf`; jerarquía de excepciones |
| Polimorfismo | Cada estado de la orden decide qué operaciones permite; `identificacion()` / `rol()` de `Persona` en el comprobante; interfaces `EmisorToken` y `SesionUsuario` con implementación Spring (inversión de dependencias); `getTipo()` y `formateado()` de NIT y DPI (cada uno con su propia validación); `mapear()` de cada DAO; `agregarContenido()` de cada PDF |
| Composición | `OrdenTrabajo` contiene sus `DetalleServicio`, `DetalleRepuesto` y `Comprobante`; `Cliente` contiene su `DocumentoIdentidad` |
| Colecciones | `List`, `Map<Long, Repuesto>` al cancelar, `TreeSet` para bloquear en orden, `EnumMap` para conteos, `groupingBy` en reportes |
| Excepciones | Excepciones propias del negocio, `rollback` ante cualquier fallo, traducción de `SQLException`, respuesta HTTP según el tipo |

## Datos de prueba

`02_datos_prueba.sql` carga 4 usuarios (uno por rol y dos mecánicos), 6 clientes (NIT y DPI válidos), 5 mecánicos, 8 vehículos, 12 repuestos (uno agotado y varios con existencia ≤ 5), 8 servicios y 12 órdenes en los cuatro estados, con comprobantes de septiembre y octubre de 2026, sus responsables y la bitácora correspondiente. Las existencias ya reflejan los repuestos consumidos.

## Repositorio

El proyecto incluye un repositorio Git inicializado en la rama `main`, **sin commits**, para que los commits sean de los integrantes del equipo.
