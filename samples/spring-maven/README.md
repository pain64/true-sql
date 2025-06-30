You need PostgreSQL to be accessible at `localhost:5433` with user `postgres` and password `1234`:
```shell
docker run -e POSTGRES_PASSWORD=1234 -p 5433:5432 postgres
./mvnw liquibase:update
```

**NB:** check that IDEA delegates build and execution to Maven

**NB:** check that maven JRE is also version 21
![check_idea_settings.png](check_idea_settings.png)