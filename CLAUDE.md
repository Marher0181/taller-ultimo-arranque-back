# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Spring Boot 3.5 / Java 21 REST API plus a server-rendered Thymeleaf web UI for a mechanic workshop ("El Último Arranque"): clients, vehicles, mechanics, parts, services, work orders, receipts and PDF reports. MySQL via **explicit JDBC** (no JPA, no `@Transactional`). JWT auth. All code, identifiers, messages and docs are in **Spanish**; keep new code in Spanish too. `README.md` has the full endpoint list, role/permission matrix and order state table.

## Commands

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS taller_ultimo_arranque"
mysql -u root -p taller_ultimo_arranque < database/01_esquema.sql        # (re)create tables, drops existing data
mysql -u root -p taller_ultimo_arranque < database/02_datos_prueba.sql   # seed data (users: admin/Admin2026!, recepcion/Recepcion2026!, paju/Mecanico2026!)
mvn spring-boot:run                               # http://localhost:8080, Swagger at /swagger-ui.html
mvn test                                          # unit tests (no DB needed)
mvn test -Dtest=RepuestoTest                      # single class
mvn test -Dtest=RepuestoTest#nombreDelMetodo      # single method
```

DB connection via env vars `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (default root/root@localhost:3306). `DB_SSL=true` for hosted MySQL, `PORT` sets the HTTP port, `JWT_SECRET` overrides the dev signing key. SQL scripts don't name a database (so they run on hosted DBs like Aiven's `defaultdb`); pass it on the `mysql` command line. Deployment (Dockerfile → Render, MySQL → Aiven) is documented in README. `spring.sql.init.mode=never`: schema changes go in `database/01_esquema.sql` by hand (and seed data in `02_datos_prueba.sql` must stay consistent with it).

## Architecture

Package root `gt.umg.ultimoarranque`, layered as `controlador` → `servicio` → `dao` → MySQL, with `modelo` holding rich domain objects and `dto` holding request/response records.

**Transactions and data access (the non-obvious part):**
- Services never touch `DataSource` directly. They call `GestorTransacciones.consultar(conexion -> ...)` for reads or `enTransaccion(conexion -> ...)` for writes; it handles commit/rollback and translates `SQLException` into `IntegridadDatosException` (SQLState `23xxx` / MySQL CHECK error 3819 → HTTP 409) or `PersistenciaException` (→ 500).
- Every DAO method takes the `Connection` as its first parameter so multiple DAOs share one transaction. DAOs extend `JdbcDao<T>` (implement `mapear(ResultSet)`; use `consultarLista`, `consultarUno`, `insertarConClave`, `ejecutarActualizacion`, `marcadores(...)` for `IN (?, ?)` lists). Always `PreparedStatement`, money as `DECIMAL`/`BigDecimal`.
- Write paths lock rows with `SELECT … FOR UPDATE` (`ordenDao.bloquearPorId`, `repuestoDao.bloquearVarios` — locks taken in ascending id order via `TreeSet` to avoid deadlocks).
- Audit: every write calls `Auditoria.registrar(conexion, Accion, EntidadBitacora, id, detalle)` **inside the same transaction**, so rolled-back operations leave no `bitacora` entry. New write operations must do the same (add the `Accion` enum value if needed).

**Domain model:** business rules live in `modelo`, not services. Services load entities, call domain methods, then persist. `OrdenTrabajo` uses the State pattern (`modelo/orden/Estado*`: PENDIENTE → EN_PROCESO → FINALIZADA, or → CANCELADA); each state decides which operations are allowed and throws otherwise. `Repuesto` stock changes only through `descontar`/`abastecer`/`reintegrar`. NIT/DPI validation (check digit) lives in `modelo/documento`.

**Security:** two layers.
1. URL/role rules in `seguridad/ConfiguracionSeguridad.java` (ADMIN, RECEPCION, MECANICO).
2. Row-level: a MECANICO may only act on orders assigned to their linked `mecanico` — enforced in `OrdenTrabajoService.verificarAcceso`, not in the security config.

Tokens are HS256 JWTs; each request also re-reads the user from DB and compares `version_token`, so deactivation / password or role change invalidates old tokens immediately. Get the current user via the `SesionUsuario` interface (`sesion.actual()`), not Spring's `SecurityContextHolder`.

**Errors:** throw subclasses of `excepcion.TallerException`; `controlador/ManejadorExcepciones` maps them to `application/problem+json` with the status codes listed in README (404 not found, 409 conflict/state, 422 business rule, etc.).

**Web UI (Thymeleaf):** `controlador/web/*WebController` + `src/main/resources/templates` (Pico.css from CDN, layout in `templates/fragmentos.html`). Same services as the API; forms are only GET/POST, so deletes/actions are `POST /x/{id}/accion`. A second `SecurityFilterChain` (`@Order(2)`, session + form login + CSRF) covers everything outside `/api/**`; `ProveedorAutenticacionWeb` reuses `AutenticacionService.iniciarSesion` and `FiltroSesionWeb` re-checks `version_token` per request. `ManejadorExcepcionesWeb` (scoped to `controlador.web`) turns `TallerException`/validation errors into a flash `mensajeError` + redirect to the Referer path, and exposes `sesionActual` to templates. Records are read in templates with method syntax (`${c.nombre()}`). Keep the web URL rules in `reglasDeAccesoWeb` aligned with the API rules.

**PDFs:** `vista/pdf`, Template Method — subclass `DocumentoPdf` and implement `agregarContenido()`. Reports are exposed as both JSON (for the Android client) and `/pdf` variants under `/api/reportes/**`.

Config values (`taller.inventario.umbral-bajo`, `taller.facturacion.tasa-iva`, JWT settings, CORS) bind through `config/TallerProperties`.

## Tests

Only plain JUnit domain tests under `src/test/.../modelo` (no Spring context, no DB). Domain logic is the cheapest place to test.

## Git

The repo intentionally has **no commits**: commits are meant to be made by the team members. Do not commit unless explicitly asked.
