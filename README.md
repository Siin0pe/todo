# Todo - API REST Annonce

Projet Maven Jakarta EE (JAX-RS + JPA/Hibernate) pour la gestion d'annonces.

## Livrables

- Projet Maven complet : `pom.xml`, `.mvn/`, `mvnw`, `mvnw.cmd`
- README (ce document) avec architecture, problemes rencontres et solutions
- Scripts SQL : `sql/schema.sql`, `sql/seed.sql`
- Tests automatises : `src/test/java`
- Collection Postman : `postman/todo-api.postman_collection.json`

## Architecture

Architecture en couches :

- API REST (ressources JAX-RS) : `src/main/java/com/example/todo/api`
- Service (regles metier) : `src/main/java/com/example/todo/service`
- Repository (acces JPA) : `src/main/java/com/example/todo/repository`
- Model (entites) : `src/main/java/com/example/todo/model`
- Securite (token + filtre) : `src/main/java/com/example/todo/api/security`
- Auth login JAAS (`MasterAnnonceLogin`) puis emission token stateless : `src/main/java/com/example/todo/service/JaasAuthService.java`
- Configuration servlet : `src/main/webapp/WEB-INF/web.xml` (`/api/*`)

## Endpoints principaux

- Auth : `POST /api/register`, `POST /api/login`
- Categories : `GET/POST /api/categories`, `GET/PUT/DELETE /api/categories/{id}`
- Annonces : `GET/POST /api/annonces`, `GET/PUT/PATCH/DELETE /api/annonces/{id}`
- OpenAPI : `GET /api/openapi`, `GET /api/openapi.json`, `GET /api/openapi.yaml`
- Swagger UI : `GET /swagger-ui.html`

## Problemes rencontres et solutions

- Separation tests unitaires vs integration
  - Probleme : execution trop longue et diagnostics moins lisibles si tout tourne ensemble.
  - Solution : profils Maven dedies (`unit-tests`, `integration-tests`, `all-tests`) avec Surefire/Failsafe.

- Validation et erreurs API
  - Probleme : retours d'erreur heterogenes.
  - Solution : DTO valides (`jakarta.validation`) + exception mappers centralises pour des reponses JSON coherentes.

- Documentation API exploitable
  - Probleme : difficultes de verification manuelle sans contrat clair.
  - Solution : exposition OpenAPI + page Swagger UI + collection Postman prete a l'emploi.

## Scripts SQL

- `sql/schema.sql` : schema PostgreSQL (tables, contraintes, index, FK)
- `sql/seed.sql` : jeu de donnees minimal pour demarrage rapide

## Lancer le projet

Prerequis :

- Java 8+
- Maven 3.9+ (ou wrapper `mvnw`)
- PostgreSQL (base `todo`)

Commandes :

```bash
./mvnw clean package
```

### Activer JAAS au demarrage Tomcat

Configurer l'option JVM suivante dans Tomcat :

```bash
-Djava.security.auth.login.config=/chemin/vers/jaas.conf
```

Exemple (Linux/macOS) dans `setenv.sh` :

```bash
export CATALINA_OPTS="$CATALINA_OPTS -Djava.security.auth.login.config=/opt/tomcat/webapps/todo/WEB-INF/classes/jaas.conf"
```

Exemple (Windows) dans `setenv.bat` :

```bat
set "CATALINA_OPTS=%CATALINA_OPTS% -Djava.security.auth.login.config=C:\tomcat\webapps\todo\WEB-INF\classes\jaas.conf"
```

Preuve de chargement au demarrage :

- Listener de demarrage : `src/main/java/com/example/todo/security/JaasBootstrapListener.java`
- Log attendu si OK : `jaas_config_loaded ... loginEntries=1 tokenEntries=1`

### Flow d'authentification (JAAS + token stateless)

1. `POST /api/login` avec `login/password`.
2. Le backend lance `LoginContext("MasterAnnonceLogin", callbackHandler)` via `JaasAuthService`.
3. Si succes JAAS, le `Subject` contient `UserPrincipal` (+ roles) et le serveur genere un token opaque (`UUID`).
4. Le client envoie ensuite `Authorization: Bearer <token>` a chaque requete protegee.
5. Le filtre JAX-RS `@Secured` lance `LoginContext("MasterAnnonceToken", callbackHandlerWithToken)` a chaque requete.
6. Si token invalide/absent : `401 Unauthorized`.
7. Si token valide : le `Subject` est reconstruit et injecte dans un `SecurityContext` de requete.
8. La couche service exploite l'identite courante (userId/roles) sans session HTTP serveur.

Codes HTTP d'autorisation :

- `401` : token absent/invalide (filtre `AuthFilter`).
- `403` : utilisateur authentifie mais action interdite par regle metier (ex: non-auteur, role insuffisant).

## Tests automatises

Conventions :

- unitaires : `*UnitTest.java`
- integration : `*IT.java`

Commandes :

```bash
./mvnw test -Punit-tests
./mvnw verify -Pintegration-tests
./mvnw verify -Pall-tests
```

## Postman

Importer `postman/todo-api.postman_collection.json`.

Variables de collection :

- `baseUrl` (par defaut `http://localhost:8080/todo/api`)
- `token`, `userId`, `categoryId`, `annonceId` (alimentees automatiquement par les scripts de test Postman)
