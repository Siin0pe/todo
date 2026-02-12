# Todo - API REST Annonce

Projet Jakarta EE (JAX-RS + JPA) exposant une API REST pour la ressource `Annonce`.

## Endpoints

- `GET /api/annonces` : liste paginee
- `GET /api/annonces/{id}` : detail
- `POST /api/annonces` : creation
- `PUT /api/annonces/{id}` : mise a jour
- `PATCH /api/annonces/{id}` : mise a jour partielle
- `DELETE /api/annonces/{id}` : suppression

## Structure

- REST : `src/main/java/com/example/todo/api`
- Service : `src/main/java/com/example/todo/service`
- Repository : `src/main/java/com/example/todo/repository`
- Model : `src/main/java/com/example/todo/model`
- Config JAX-RS : `src/main/webapp/WEB-INF/web.xml`
