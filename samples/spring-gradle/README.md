### How to run this demo
You need PostgreSQL to be accessible at `localhost:5433` with user `postgres` and password `1234`:
```shell
docker run -e POSTGRES_PASSWORD=1234 -p 5433:5432 postgres
./gradlew flywayMigrate
```