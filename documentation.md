<a id="build-pass">https://img.shields.io/badge/Code%20Coverage-100%100-success?style=flat</a>
<a id="coverage">https://img.shields.io/badge/Code%20Coverage-100%100-success?style=flat</a>

# True-SQL
True-SQL is an ultimate sql database connector for Java.<br>
It's development is motivated by the pain of thousands of developers. Therefore, the main focus was to make true-sql powerful and convenient. It has no competitors. It is more convenient, easier to understand, faster and more secure than any other Java DB connector. These are irrefutable reasons why you should choose True-SQL.

##### Contact us via
tg: [Alex](), [Dmitry]()<br>
email: [truesqlbest@email.com]()

## FEATURES:
- [ResultSet to DTO mapping. Grouped object-tree fetching](#resultset-to-dto-mapping-grouped-object-tree-fetching)
- [Compile-time query validation and DTO-generation](#compile-time-query-validation-and-dto-generation)
- [Null-safety](#null-safety)
- [Full featured:](#full-featured)
  - [Generated keys](#generatedkeys)
  - [Update count](#updatecount)
  - [Batching](#batching)
  - [Transactions and connection pinning](#transactions-and-connection-pinning)
  - [Streaming fetching](#streaming-fetching)
  - [Stored procedure call](#stored-procedure-call)
  - [Extra type bindings](#extra-type-bindings)
- [Multiple database schemas in module](#multiple-database-schemas-in-one-module)
- [DB constraint violation checks](#db-constraint-violation-checks)
- [100% sql-injection safety guarantee](#100-sql-injection-safety-guarantee)
- [Exceptional perfomance. Equal to JDBC](#exceptional-performance-equal-to-jdbc)

#### More information about True-SQL
[A review article about ORM issues.]()<br>
[Article about True-SQL.]()<br>
[Our youtube channel about True-SQL.]()<br>
[In this telegram channel we answer questions about True-SQL.]()

#### TESTED ON ALL MAJOR DATABASES

[Coverage](#coverage) includes all databases tests with all true-sql functionality.
<table>
    <tr>
        <th>PostgreSQL</th>
        <th>MySQL</th>
        <th>MSSQL</th>
        <th>Oracle</th>
        <th>MariaDB</th>
        <th>HSQL</th>
        <th>DB2</th>
    </tr>
    <tr>
        <td>latest</td>
        <td>latest</td>
        <td>latest</td>
        <td>12c</td>
        <td>latest</td>
        <td>latest</td>
        <td>latest</td>
    </tr>
</table>

## Startup
Get artifacts here: [Maven repository link]().<br>
Have fun!
```java
//here declare DataSourceW or ConnectionW name of db
record PgDb(DataSource w) implements DataSourceW {};
//annotate your class or method with @TrueSQL
@TrueSQL
void main() {
    //create db instance
    var ds = new PgDb(new JdbcDataSource(connection string));
    var userId = 42;
    var name = ds."select name from users where id = \{userId}"
        .fetchOne(String.class);
}
```
In all tests above we using schema:<br>
<details>
  <summary>schema.sql - flyway</summary>
  ...
</details>

## ResultSet to DTO mapping. Grouped object-tree fetching.

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

ds."select * from user where id = \{123}".fetchNone();
//raise compiletime error… ……
```

Moreover, by communicating directly with the database, we can generate DTO in compile-time.
#### Simple row-wise select
```java
var user = ds."select id, name from user".g.fetchList(User.class);
```
<details>
    <summary>User</summary>
    
```java
record User(long id, String name) {};
```
</details>

#### List grouping

```java
var clinicAddresses = ds."""
    select distinct
        c.name as "name", 
        ca.address as "addresses."
    from clinic c 
        join clinic_adresses ca on c.id = ca.id_clinic
    """.g.fetchList(ClinicAddresses.class);
```
<details>
    <summary>ClinicAddresses</summary>
    
```java
record ClinicAddresses(String name, List<String> addresses) {};
```
</details>

#### Nested DTO

```java
var clinics = ds."""
    select
        c.id   as		"id",
        c.name as 		"name",
        u.id   as 		"User users.id",
        u.name as 		"     users.name",
    from clinic c
        left join clinic_users cu on cu.clinic_id = c.id
        left join user u on u.id = cu.user_id
    """.fetchList(Clinic.class);
```

<details>
    <summary>Clinic, User</summary>
    
```java
record Clinic(long id, String name, List<User> users) {
	record User(long id, String name) {}
}
```
</details>


#### RAMPAGE!

```java
var clinics = ds."""
    select
        c.id   as		“id”,
        ca.address as 	“addresses.”,
        c.name as 		“name”,
        u.id   as 		“User users.id”,
        u.name as 		“     users.name”,
        b.id   as 		“     users.Bill bills.id”,
        b.date as 		“     users.     bills.date”,
        b.amount as 	“     users.     bills.amount”
    from clinic c
        join clinic_addresses ca on ca.id = c.id
        left join clinic_users cu on cu.clinic_id = c.id
        left join user u on u.id = cu.user_id
        left join bill b on u.id = b.user_id
    """.fetchList(Clinic.class);

```

<details>
    <summary>Clinic, User, Bill</summary>

```java
record Clinic(long id, String name, List<String> addresses, List<User> users) {
	record User(long id, String name, List<Bill> bills) {
		record Bill(long id, Date date, BigDecimal amount) {}
 //!check what date should be   
    }
}
```
</details>

\\HERE GENERATED MAPPING CODE
NB:

## Null-safety
minimal overture about null-safety.
True-SQL forces you to take care about nulls. You will have to annotate all nullable values with org.jetbrains.annotations.Nullable.
db - columns - driver - columns (may be nullable?)
drivers can make mistakes. 
you can force Nullability of the field with …

## Full featured
some about full featured!

#### GeneratedKeys
NB: both Automapping and Generate DTO works with.

```java
var user = ds."insert into user values (\{"Pavel"})"
.asGeneratedKeys(“id”)
.g.fetchOne(Long.class);
```

#### UpdateCount
NB: update count will always be long.

```java
var updateCount = ds."
update user set name = \{"Paul"} where id % 2 == 0
".withUpdateCount.g.fetchNone();
```

#### Batching
NB:

```java
//List<Long>
var keys = ds.batch(
    List.of("Joe", "Ivan", "Mike"),
        s -> B."insert into user values(\{s})"
    ).asGeneratedKeys("id").fetchList(Long.class)
```

```java
record Discount(Date date, BigDecimal discount) {}

List<Discount> discounts = ...

//long[]???
var keys = ds.batch(
    discounts,
        date, discount_perc -> 
            B."update bill set discount = \{discount_perc} where date = \{date}"
    ).fetchNone()
```

#### Transactions and connection pinning
NB

```java
ds.withConnection(cn -> {
	cn."""
	create temp table temp_table as
	with t(s) as (values (‘a’), (‘b’))
    """.fetchNone();

    return cn."select * from temp_table".fetchList(String.class);
})
```

```java
cn.inTransaction(() -> {
    cn."insert into users values (1, ‘Joe’, ‘some@email.com’)".fetchNone;

    return cn."select name from users where id = 1".fetchOne(String.class);
})
```

#### Streaming fetching
NB:

```java
ds.withConnection(cn -> {
        try (
            var stream = cn."select name from users"
            .fetchStream(String.class)
        ) {
        //stream.toList();
        }
})
```

#### Stored procedure call
NB:

#### Extra type bindings

## Multiple database schemas in one module
NB:

```java
record PgDb(DataSource w) implements DataSourceW {};
record MSDb(DataSource w) implements DataSourceW {};
//...
```

## DB constraint violation checks
NB:

```java
try {
	ds."insert into users values(1, ‘John’, ‘privetic@mail.com’)".fetchNone();
} catch (ConstraintViolationException ex) {
	ex.when(
		new Constraint<>("users", "users_pk", () -> {
			throw new Handled();
        })
    );
}
```

## 100% sql-injection safety guarantee
??? all queries text are static. all parameters passes as PreparedStatement parameters.

## Exceptional performance. Equal to JDBC
because we generate equal jdbc code
??? here table with comparsion

