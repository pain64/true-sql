# True-SQL

True-SQL is an ultimate sql database connector for Java.<br>
**FEATURES:**
- [ResultSet to DTO mapping. Grouped object-tree fetching](#resultset-to-dto-mapping-grouped-object-tree-fetching)
- [Compile-time query validation and DTO-generation](#compile-time-query-validation-and-dto-generation)
- Null-safety
- Full featured:
  - Generated keys
  - Update count
  - Batching
  - Transactions and connection pinning
  - Streaming fetching
  - Stored procedure call
  - Extra type bindings
- Multiple database schemas in module
- DB constraint violation checks
- 100% sql-injection safety guarantee
- Exceptional perfomance. Equal to JDBC
<br>
//TESTED ON ALL MAJOR DATABASES… COVERAGE 100% <br>
//some about philosophy <br>
//why better choice? <br>

## ResultSet to DTO mapping. Grouped object-tree fetching. 
<details>
  <summary>schema.sql - flyway</summary>
  ...
</details>
<details>
  <summary>build.gradle.kts</summary>
</details>
// folded: build.gradle.kts <br>
// main.java <br>
<br>

```java
record PgDb(DataSource w) implements DataSourceW {}
record User(long id, String name) {}
record Clinic(long id, String name, List<User> users) {}

@TrueSQL
void main() { // pg
    var userId = 42;
    var ds = new PgDb(new JdbcDataSource( connection string ));
    var user = ds.“select id, name from users where id = \{userId}”
        .fetchOne(User.class);

     var clinics = ds."""
         select
            c.id, c.name, u.id, u.name
         from clinics c
         left join clinic_users cu on cu.clinic_id = c.id
         left join user u on u.id = cu.user_id
     """.fetchList(Clinic.class);
}
```
NB<br>

## Compile-time query validation and DTO generation

During compilation, we send the queries and their parameters to the database to check whether the query can be executed successfully. i.e<br>
```java
ds."select * frm user".fetchNone();
//raise compiletime error… syntax error

ds."select * from yser".fetchNone();
//raise compiletime error… table doesnt exist

ds."select * from user where id = \{"oops"}".fetchNone();
//raise compiletime error… ……

```
