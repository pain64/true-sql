<a id="coverage">![Coverage](.github/badges/jacoco_coverage.svg)</a>

# TrueSql
TrueSql is an ultimate sql database connector for Java.<br>
It's development is motivated by the pain of thousands of developers. Therefore, the main focus was to make TrueSql powerful and convenient. It has no competitors. It is more convenient, easier to understand, faster and more secure than any other Java DB connector. These are irrefutable reasons why you should choose TrueSql.

##### Contact us via
tg: [Alex](), [Dmitry]()<br>
email: [truesqlbest@email.com]()

## FEATURES
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

#### More information about TrueSql
[A review article about ORM issues.]()<br>
[Article about TrueSql.]()<br>
[Our youtube channel about TrueSql.]()<br>
[In this telegram channel we answer questions about TrueSql.]()

#### TESTED ON ALL MAJOR DATABASES

[Coverage](#coverage) includes all databases tests with all TrueSql functionality.
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

## Get started
1. Get artifacts here: [Maven repository link]().<br>
2. Have fun!
```java
// declare DataSourceW or ConnectionW as connection configuration
record PgDb(DataSource w) implements DataSourceW {};
// annotate your class or method with @TrueSQL
@TrueSQL void main() {
    // create db instance
    var ds = new PgDb(new JdbcDataSource("localhost:5432"));
    var userId = 42;
    // chill
    var name = ds.q("select name from users where id = ?", userId)
        .fetchOne(String.class);
}
```
<details>
  <summary>build.gradle.kts</summary>
  ...
</details>

<details>
  <summary>schema.sql (also used for all examples below)</summary>
  ...
</details>

## ResultSet to DTO mapping. Grouped object-tree fetching.
TrueSql has a set of fetchers, explore them with comma. You can map ResultSet (jdbc representation of query result) to DTO. TrueSql map fields according to the declare order. 

```java
record User(long id, String name) {};
record Clinic(long id, String name, List<User> users) {};

var userId = 42;
var user = ds.q("select id, name from users where id = ?", userId)
    .fetchOne(User.class);

var clinics = ds.q("""
    select
        c.id, 
        c.name, 
        u.id, 
        u.name
    from clinics c
        left join clinic_users cu on cu.clinic_id = c.id
        left join user u on u.id = cu.user_id
    """).fetchList(Clinic.class);
```

###### NB: fetchOne() expects exactly one, otherwise raise RuntimeError.
All possibilities of grouped object-tree demonstrated below.

## Compile-time query validation and DTO generation
During compilation, we send queries and their parameters to the database to check whether the query can be executed successfully. i.e<br>
```java
ds.q("select * frm user").fetchNone();
//raise compiletime error... syntax error

ds.q("select * from yser").fetchNone();
//raise compiletime error... table doesnt exist

ds.q("select name, id from user where id = ?", 123).fetchOne(String.class);
//raise compiletime error... wrong DTO
```

Moreover, by communicating directly with the database, we can generate DTO in compile-time. <br>
**Just add ".g" and mark up your query.**

#### Simple row-wise select
```java
var user = ds.q("select id, name from user").g.fetchList(User.class);
```
<details>
    <summary>User</summary>
    
```java
record User(long id, String name) {};
```
</details>

#### List grouping

```java
var clinicAddresses = ds.q("""
    select distinct
        c.name as "name", 
        ca.address as "addresses."
    from clinic c 
        join clinic_adresses ca on c.id = ca.id_clinic
    """).g.fetchList(ClinicAddresses.class);
```
<details>
    <summary>ClinicAddresses</summary>
    
```java
record ClinicAddresses(String name, List<String> addresses) {};
```
</details>

#### Nested DTO

```java
var clinics = ds.q("""
    select
        c.id   as		"id",
        c.name as 		"name",
        u.id   as 		"User users.id",
        u.name as 		"     users.name",
    from clinic c
        left join clinic_users cu on cu.clinic_id = c.id
        left join user u on u.id = cu.user_id
    """).fetchList(Clinic.class);
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
var clinics = ds.q("""
    select
        c.id   as       “id”,
        ca.address as   “addresses.”,
        c.name as       “name”,
        u.id   as       “User users.id”,
        u.name as       “     users.name”,
        b.id   as       “     users.Bill bills.id”,
        b.date as       “     users.     bills.date”,
        b.amount as     “     users.     bills.amount”
    from clinic c
        join clinic_addresses ca on ca.id = c.id
        left join clinic_users cu on cu.clinic_id = c.id
        left join user u on u.id = cu.user_id
        left join bill b on u.id = b.user_id
    """).fetchList(Clinic.class);

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

###### NB: Queries stay static. All grouping work happens in Java.
<details>
    <summary>The code we generated for you!</summary>

```java
//
```
</details>

## Null-safety
NullPointerException is a nightmare for every Java developer. It's important to catch null in the beginning. TrueSql get information about fields nullability from db driver. The war is end? [Unfortunately, not all databases calculates nullability right.](#table_with_proofs) In case you disagree with db driver, we print compilation time warning. If you found an example, you can create a ticket to db vendor and restore order!

This table represents all possible situations.  
<table>
    <tr>
        <td><b>you\driver</td>
        <td><b>Nullable</td>
        <td><b>NotNull</td>
    </tr>
    <tr>
        <td><b>Not annotated</td>
        <td background="red">comptime error,<br> must annotate</td>
        <td>good</td>
    </tr>
    <tr>
        <td><b>Nullable</td>
        <td>good</td>
        <td>warning</td>
    </tr>
    <tr>
        <td><b>NotNull</td>
        <td>warning</td>
        <td>good</td>
    </tr>
</table>

##### Fetch scalar
Use fetch method overload to tell TrueSql interpret column as Nullable or NotNull.

```java
import static com.truej.sql.v3.source.Parameters.Nullable;
import static com.truej.sql.v3.source.Parameters.NotNull;
//...
var infos = ds.q("select info from users where id = ?")
    .fetchOne(Nullable, String.class, 42);

var names = ds.q("select info from users where info is not null")
    .fetchOneOrZero(NotNull, String.class);
```
###### NB?
##### Fetch with your own DTO
Use org.jetrbrains.annotations in DTO.

```java
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//consider name Nullablle/NotNull
record User(long id, @Nullable String info) { };
record Clinic(long id, @NotNull String info) { };
```

##### Fetch with .g
By default TrueSql annotate fields in line with db driver. You can change it with next syntax:

```java
var user = ds.q("select id, info as ":t?" from users")
    .fetchList(User.class);
```

<details>
    <summary>generated User</summary>

```java
record UserG(long id, @Nullable String name) { }
```
</details>


```java
var user = ds.q("select id, name as ":t!" from users")
    .fetchList(User.class);
```

<details>
    <summary>generated User</summary>

```java
record UserG(long id, String name) { }
```
</details>

## Full featured
We save and ***improve*** all necessary JDBC possibilities.<br>
**All features above remain in force!**

#### GeneratedKeys

```java
var user = ds.q("insert into user values (?)", "Pavel")
    .asGeneratedKeys("id").fetchOne(Long.class);
```

#### UpdateCount

```java
var updateCount = ds.q("update user set name = ? where id % 2 == 0", "Paul")
    .withUpdateCount.fetchNone();
```
###### NB: update count will always be long.

#### Batching

```java
//List<Long>
var keys = ds.q(
    List.of("Joe", "Ivan", "Mike"),
        "insert into user values(?)",
            s -> new Object[]{s}
    ).asGeneratedKeys("id").fetchList(Long.class)
```

<br>

```java
record Discount(Date date, BigDecimal discount) {}

List<Discount> discounts = ...

var keys = ds.q(
    discounts,
        "update bill set discount = ? where date = ?",
        d -> new Object[]{d.date, d.discount_perc}
    ).fetchNone()
```

#### Transactions and connection pinning
In case you want pin connection

```java
ds.withConnection(cn -> {
	cn.q("""
	create temp table temp_table as
	with t(s) as (values (‘a’), (‘b’))
    """).fetchNone();

    return cn.q("select * from temp_table").fetchList(String.class);
})
```

In case you need transaction mode
```java
cn.inTransaction(() -> {
    cn.q("insert into users values (1, ‘Joe’, ‘some@email.com’)").fetchNone();

    return cn.q("select name from users where id = 1").fetchOne(String.class);
})
```

#### Streaming fetching
If you don't want to materialize all ResultSet rows, you could use fetchStream()
```java
ds.withConnection(cn -> {
    try (
        var stream = cn.q("select id, name from users").g
            .fetchStream(User.class)
    ) {
    //stream.toList();
    }
})
```

#### Stored procedure call
lalala

#### Extra type bindings
lalala

## Multiple database schemas in one module

```java
record PgDb(DataSource w) implements DataSourceW {};
record MSDb(DataSource w) implements DataSourceW {};
```
###### NB:

## DB constraint violation checks
The way you can catch DB constraint violations.

```java
try {
	ds.q("insert into users values(1, ‘John’, ‘privetic@mail.com’)").fetchNone();
} catch (ConstraintViolationException ex) {
	ex.when(
		new Constraint<>("users", "users_pk", () -> {
			throw new Handled("User with id=1 already exists.");
        })
    );
}
```

## 100% sql-injection safety guarantee
1. All queries text are static.
2. All parameters passes as PreparedStatement parameters.

For these reasons, sql-injection can't happen.

## Exceptional performance. Equal to JDBC
TrueSql translates to pure JDBC. This means that no others can be any faster.

??? here table with comparsion with other frameworks
