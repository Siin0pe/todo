# Todo - API REST (Spring Boot)

Projet Maven/Spring Boot pour la gestion d'annonces.

## Depot GitHub

- URL du repo : `https://github.com/Siin0pe/todo`

## Stack technique

- Java 17 (CI valide aussi Java 21)
- Spring Boot 3.5.5
  - spring-boot-starter-web (API REST)
  - spring-boot-starter-data-jpa (JPA/Hibernate)
  - spring-boot-starter-security (authentification/autorisation)
  - spring-boot-starter-validation (validation DTO)
  - spring-boot-starter-actuator (health/info)
  - spring-boot-starter-aop (logging metier)
- Springdoc OpenAPI + Swagger UI
- PostgreSQL (runtime)
- H2 + Testcontainers (tests)
- MapStruct (mapping DTO <-> entites)
- JUnit 5 + Mockito
- Docker + Docker Compose
- GitHub Actions

## Livrables

- Projet Maven complet : `pom.xml`, `.mvn/`, `mvnw`, `mvnw.cmd`
- API Spring Boot : `src/main/java/com/example/todo`
- Scripts SQL : `sql/schema.sql`, `sql/seed.sql`, `sql/migration_add_annonce_version.sql`
- Tests automatises : `src/test/java`
- Pipeline CI : `.github/workflows/ci.yml`
- Collection Postman : `postman/todo-api.postman_collection.json`

## Architecture

Architecture en couches :

- Controllers REST Spring MVC : `src/main/java/com/example/todo/controller`
- Services metier : `src/main/java/com/example/todo/service`
- Repositories Spring Data JPA : `src/main/java/com/example/todo/repository`
- Entites : `src/main/java/com/example/todo/model`
- DTO : `src/main/java/com/example/todo/api/dto`
- Mappers MapStruct : `src/main/java/com/example/todo/api/mapper`
- Securite Spring/JWT : `src/main/java/com/example/todo/security/spring`
- Configuration applicative : `src/main/resources/application.yml`

Notes de migration :

- Des classes JAX-RS/JAAS legacy existent encore (`src/main/java/com/example/todo/api/security`, `src/main/java/com/example/todo/security/jaas`, `src/main/webapp/WEB-INF/web.xml`).
- Le flux principal actuellement actif est Spring MVC + Spring Security + JWT.

## Problemes rencontres

- Coexistence de code legacy (JAX-RS/JAAS) avec le nouveau flux Spring MVC/Spring Security.
- Mise en place de tests d'integration reproductibles localement et en CI sans dependre d'une base partagee.
- Documentation et examples API a maintenir alignes avec les DTO et les regles de securite JWT.

## Solutions apportees

- Conservation temporaire des composants legacy avec un flux principal isole sur Spring Boot, pour migration progressive.
- Usage de Testcontainers (`postgres:16-alpine`) pour les tests d'integration afin d'avoir un environnement deterministe.
- Integration Springdoc/OpenAPI + annotations Swagger sur controllers/DTO, et endpoints `/v3/api-docs` + `/swagger-ui`.

## Endpoints principaux

Endpoints publics :

- `POST /api/auth/login`
- `POST /api/register`
- `GET /v3/api-docs`
- `GET /v3/api-docs.yaml`
- `GET /swagger-ui` (redirection vers Swagger UI)
- `GET /actuator/health`
- `GET /actuator/info`

Endpoints proteges (`Authorization: Bearer <token>`) :

- `GET/POST /api/categories`
- `GET/PUT/DELETE /api/categories/{id}`
- `GET/POST /api/annonces`
- `GET/PUT/PATCH/DELETE /api/annonces/{id}`

## Execution locale

Prerequis :

- Java 17+
- Docker (recommande pour PostgreSQL local et tests d'integration Testcontainers)

Build + tests :

```bash
./mvnw clean verify
```

Sous Windows (PowerShell) :

```powershell
.\mvnw.cmd clean verify
```

Demarrer l'API (sans Docker Compose) :

```bash
./mvnw spring-boot:run
```

Configuration DB par defaut (`application.yml`) :

- `DB_URL=jdbc:postgresql://localhost:5432/todo`
- `DB_USERNAME=todo`
- `DB_PASSWORD=todo`

Variables de securite JWT :

- `TODO_SECURITY_JWT_SECRET` (defaut : `change-me-in-production`)
- `TODO_SECURITY_JWT_EXPIRATION_SECONDS` (defaut : `3600`)
- `TODO_SECURITY_ADMIN_USERS` (liste CSV des usernames admin)

## Docker

Lancer l'API + PostgreSQL :

```bash
docker compose up --build
```

Acces apres demarrage :

- API : `http://localhost:18080`
- Swagger UI : `http://localhost:18080/swagger-ui`
- OpenAPI JSON : `http://localhost:18080/v3/api-docs`

## Postman

- Collection : `postman/todo-api.postman_collection.json`
- Variables fournies : `baseUrl`, `token`, `authorId`, `categoryId`, `annonceId`.
- La requete `POST /api/auth/login` met automatiquement a jour `token` a partir du JSON de reponse.

## Tests et CI

Workflow : `.github/workflows/ci.yml`

Declenchement :

- `push` sur toutes les branches
- `pull_request` vers `main`

Pipeline :

- matrice Java `17` et `21`
- build + tests : `mvn -B clean verify`
- publication artefacts :
  - `master-annonce-jar`
  - `master-annonce-jacoco-report`
  - `master-annonce-docker-image`

Strategie base de donnees en CI :

- Testcontainers (`ApiIntegrationSpringTest` demarre `postgres:16-alpine`)
- pas de service PostgreSQL dedie dans le workflow
