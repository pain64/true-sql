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
  - [Unfold parameters for "in-clause"](#unfold-parameters-for-in-clause)
- [Multiple database schemas in module](#multiple-database-schemas-in-one-module)
- [Extra type bindings](#extra-type-bindings)
- [DB constraint violation checks](#db-constraint-violation-checks)
- [100% sql-injection safety guarantee](#100-sql-injection-safety-guarantee)
- [Exceptional performance. Equal to JDBC](#exceptional-performance-equal-to-jdbc)

#### More information about TrueSql and community
IN PROGRESS [Article about TrueSql.]() <br>
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
        <td>12c+</td>
        <td>latest</td>
        <td>latest</td>
        <td>in progress</td>
    </tr>
</table>

## Get started
1. Get artifacts here: [Maven repository link](https://mvnrepository.com/artifact/net.truej/sql).<br>
2. Have fun!
<details>
  <summary>build.gradle.kts</summary>
  <a href="https://github.com/pain64/true-sql/blob/main/samples/api-showcase/build.gradle.kts">link</a>
  <br>...
</details>

```java
// declare DataSourceW or ConnectionW as connection configuration
@Configuration(
    checks = @CompileTimeChecks(
                url = "jdbc:postgresql://localhost:5432/test_db",
                username = "user",
                password = "userpassword"
    )
) public class PgDs extends DataSourceW {
    public PgDs(DataSource w) { super(w); }
}
// ! ANNOTATE YOUR CLASS WITH @TrueSql !
@TrueSql class Main {
    void main() {
        // create db instance
        var ds = new PgDs(new HikariDataSource() {{
            setJdbcUrl("jdbc:postgresql://localhost:5432/test_db");
            setUsername("user");
            setPassword("userpassword");
        }});
        // chill
        var name = ds.q("select name from users where id = ?", 42)
            .fetchOne(String.class);
    }
}
```
[schema.sql](https://github.com/pain64/true-sql/blob/main/lib/src/test/resources/schema/postgresql.sql) (also used for all examples below)

###### NB: Pass parameters one by one after query text.

## Connection configuration
If you want TrueSql to do compile time checks you need to configure DataSourceW or ConnectionW (**STRONGLY RECOMENDED**)

```java
import net.truej.sql.source.DataSourceW;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.CompileTimeChecks;

@Configuration(
    checks = @CompileTimeChecks(
        url = "jdbc:postgresql://localhost:5432/test_db",
        username = "user",
        password = "userpassword"
    )
) public class PgDs extends DataSourceW {
    public PgDs(DataSource w) { super(w); }
}
```

You can configure db connection with next ENV variables

    truesql.xxx.PgDs.url=null
    truesql.xxx.PgDs.username=null
    truesql.xxx.PgDs.password=null

To check configuration when build use flag

    ./gradlew build -Dtruesql.printConfig=true

###### NB: Without compile-time validation generate DTO and compile-time query validation don't work. asCall() and constraint violations checks don't works, but without compile-time verification.

## ResultSet to DTO mapping. Grouped object-tree fetching.
TrueSql has a set of fetchers, explore them with comma. You can map ResultSet (jdbc representation of query result) to DTO. TrueSql map fields into record according to the declare order:

```java
// declared outside of current method
record User(String name, BigDecimal amount) { }
record Report(String city, List<String> clinics, List<User> users) { }

ds.q("""
    select
        ci.name as city,
        cl.name as clinic,
        u.name as user,
        sum(b.amount) as amount
    from city ci
        join clinic cl on ci.id = cl.city_id
        join clinic_users clu on clu.clinic_id = cl.id
        join users u on clu.user_id = u.id
        join user_bills ub on ub.user_id = u.id
        join bill b on b.id = ub.bill_id
    group by ci.name, cl.name, u.name, u.info"""
).fetchList(Report.class)
```

###### NB: Group with all null fields will be droped.

All possibilities of grouped object-tree demonstrated below.

## Compile-time query validation and DTO generation
During compilation, we send queries and their parameters to the database to check whether the query can be executed successfully. i.e<br>
```java
ds.q("select * frm users").fetchNone();
// raise compiletime error... syntax error

ds.q("select * from ysers").fetchNone();
// raise compiletime error... table doesnt exist

ds.q("select name, id from user where id = ?", 123).fetchOne(String.class);
// raise compiletime error... wrong DTO
```

Moreover, by communicating directly with the database, we can generate DTO in compile-time. <br>
**Just add ".g" and mark up your query.**

### Simple row-wise select
```java
var user = ds.q("select id, name from users").g.fetchList(User.class);
```
<details>
    <summary>User</summary>
    
```java
record User(long id, String name) { }
```
</details>

### List grouping

```java
var clinicAddresses = ds.q("""
    select distinct
        ci.name as "city    ", 
        cl.name as "clinics."
    from city ci
        left join clinic cl on ci.id = cl.city_id
    """).g.fetchList(ClinicAddresses.class);
```
<details>
    <summary>CitiesClinics</summary>
    
```java
record CitiesClinics(String city, List<String> clinics) { }
```
</details>

### Nested DTO

```java
var clinics = ds.q("""
    select
        c.id   as		"id             ",
        c.name as 		"name           ",
        u.id   as 		"User users.id  ",
        u.name as 		"     users.name"
    from clinic c
        left join clinic_users cu on cu.clinic_id = c.id
        left join users u on u.id = cu.user_id
    """).g.fetchList(Clinic.class);
```

<details>
    <summary>Clinic, User</summary>
    
```java
record Clinic(long id, String name, List<User> users) {
	record User(Long id, String name) {}
}
```
</details>


### RAMPAGE!

```java
var clinics = ds.q("""
    select
        c.id   as       “id                          ”,
        ca.address as   “addresses.                  ”,
        c.name as       “name                        ”,
        u.id   as       “User users.id               ”,
        u.name as       “     users.name             ”,
        b.id   as       “     users.Bill bills.id    ”,
        b.date as       “     users.     bills.date  ”,
        b.amount as     “     users.     bills.amount”
    from clinic c
        join clinic_addresses ca on ca.id = c.id
        left join clinic_users cu on cu.clinic_id = c.id
        left join user u on u.id = cu.user_id
        left join bill b on u.id = b.user_id
    """).g.fetchList(Clinic.class);

```

<details>
    <summary>Clinic, User, Bill</summary>

```java
record Clinic(long id, String name, List<String> addresses, List<User> users) {
	record User(Long id, String name, List<Bill> bills) {
		record Bill(Long id, OffsetDateTime date, BigDecimal amount) {}
    }
}
```
</details>

###### NB: Queries stay static. All grouping work happens in Java.
<details>
    <summary>The code we generated for you!</summary>

```java

public static class Bill {
    @Nullable public final java.time.OffsetDateTime date;
    @Nullable public final java.math.BigDecimal amount;

    public Bill(
        java.time.OffsetDateTime date,
        java.math.BigDecimal amount
    ) {
        this.date = date;
        this.amount = amount;
    }

    @Override public boolean equals(Object other) {
        return this == other || (
            other instanceof Bill o &&
            java.util.Objects.equals(this.date, o.date) &&
            java.util.Objects.equals(this.amount, o.amount)
        );
    }

    @Override public int hashCode() {
        int h = 1;
        h = h * 59 + java.util.Objects.hashCode(this.date);
        h = h * 59 + java.util.Objects.hashCode(this.amount);
        return h;
    }
}
public static class User {
    @Nullable public final java.lang.String name;
    @Nullable public final java.lang.String info;
    public final List<Bill> bills;

    public User(
        java.lang.String name,
        java.lang.String info,
        List<Bill> bills
    ) {
        this.name = name;
        this.info = info;
        this.bills = bills;
    }

    @Override public boolean equals(Object other) {
        return this == other || (
            other instanceof User o &&
            java.util.Objects.equals(this.name, o.name) &&
            java.util.Objects.equals(this.info, o.info) &&
            java.util.Objects.equals(this.bills, o.bills)
        );
    }

    @Override public int hashCode() {
        int h = 1;
        h = h * 59 + java.util.Objects.hashCode(this.name);
        h = h * 59 + java.util.Objects.hashCode(this.info);
        h = h * 59 + java.util.Objects.hashCode(this.bills);
        return h;
    }
}
public static class Clinic {
    @NotNull public final java.lang.String city;
    public final List<java.lang.String> clinic;
    public final List<User> users;

    public Clinic(
        java.lang.String city,
        List<java.lang.String> clinic,
        List<User> users
    ) {
        this.city = city;
        this.clinic = clinic;
        this.users = users;
    }

    @Override public boolean equals(Object other) {
        return this == other || (
            other instanceof Clinic o &&
            java.util.Objects.equals(this.city, o.city) &&
            java.util.Objects.equals(this.clinic, o.clinic) &&
            java.util.Objects.equals(this.users, o.users)
        );
    }

    @Override public int hashCode() {
        int h = 1;
        h = h * 59 + java.util.Objects.hashCode(this.city);
        h = h * 59 + java.util.Objects.hashCode(this.clinic);
        h = h * 59 + java.util.Objects.hashCode(this.users);
        return h;
    }
}
public static 
List<Clinic> fetchList__line139__(
    
    ConnectionW source
)  {
    var buffer = new StringBuilder();
    
    buffer.append("""
        select
        ci.name  as "      city                   ",
        cl.name  as "      clinic.                ",
        u.name   as "User users .name            ",
        u.info   as "      users .info            ",
        b.date   as "      users .Bill bills.date ",
        b.amount as "      users .     bills.amount"
    from city ci
        join clinic       cl  on ci.id         = cl.city_id
        left join clinic_users clu on clu.clinic_id = cl.id
        left join users        u   on clu.user_id   = u.id
        left join user_bills   ub  on ub.user_id    = u.id
        left join bill         b   on b.id          = ub.bill_id
     """);
    
    var query = buffer.toString();
    
    try {
        var connection = source.w();
        try (var stmt = connection.prepareStatement(query)) {
        
            var n = 0;
            
            
            stmt.execute();
            
            var rs = stmt.getResultSet();
            
            record Row(
                java.lang.String c1,
                java.lang.String c2,
                java.lang.String c3,
                java.lang.String c4,
                java.time.OffsetDateTime c5,
                java.math.BigDecimal c6
            ) {}
            record G1(
                java.lang.String c1
            ) {}
            record G2(
                java.lang.String c3,
                java.lang.String c4
            ) {}
            
            
            var mapped = Stream.iterate(
                rs, t -> {
                    try {
                        return t.next();
                    } catch (SQLException e) {
                        throw source.mapException(e);
                    }
                }, t -> t
            ).map(t -> {
                try {
                    return
                        new Row (
                            new net.truej.sql.bindings.StringReadWrite().get(rs, 1),
                            new net.truej.sql.bindings.StringReadWrite().get(rs, 2),
                            new net.truej.sql.bindings.StringReadWrite().get(rs, 3),
                            new net.truej.sql.bindings.StringReadWrite().get(rs, 4),
                            new net.truej.sql.bindings.OffsetDateTimeReadWrite().get(rs, 5),
                            new net.truej.sql.bindings.BigDecimalReadWrite().get(rs, 6)
                        );
                } catch (SQLException e) {
                    throw source.mapException(e);
                }
            })
            .collect(
                java.util.stream.Collectors.groupingBy(
                    r -> new G1(
                        r.c1
                    ), java.util.LinkedHashMap::new, Collectors.toList()
                )
            ).entrySet().stream()
            .filter(g1 ->
                java.util.Objects.nonNull(g1.getKey().c1)
            ).map(g1 ->
                new Clinic(
                    EvenSoNullPointerException.check(g1.getKey().c1),
                    g1.getValue().stream().filter(r ->
                        java.util.Objects.nonNull(r.c2)
                    ).map(r ->
                        EvenSoNullPointerException.check(r.c2)
                    ).distinct().toList(),
                    g1.getValue().stream().collect(
                        java.util.stream.Collectors.groupingBy(
                            r -> new G2(
                                r.c3,
                                r.c4
                            ), java.util.LinkedHashMap::new, Collectors.toList()
                        )
                    ).entrySet().stream()
                    .filter(g2 ->
                        java.util.Objects.nonNull(g2.getKey().c3) ||
                        java.util.Objects.nonNull(g2.getKey().c4)
                    ).map(g2 ->
                        new User(
                            g2.getKey().c3,
                            g2.getKey().c4,
                            g2.getValue().stream().filter(r ->
                                java.util.Objects.nonNull(r.c5) ||
                                java.util.Objects.nonNull(r.c6)
                            ).map(r ->
                                new Bill(
                                    r.c5,
                                    r.c6
                                )
                            ).toList()
                        )
                    ).toList()
                )
            );
            return mapped.toList();
        
        }
    } catch (SQLException e) {
        throw source.mapException(e);
    }
}

```
</details>

## Null-safety
NullPointerException is a nightmare for every Java developer. It's important to catch null in the beginning. TrueSql get information about fields nullability from db driver. The war is end? [Unfortunately, not all databases driver calculates nullability right.](#table_with_proofs) In case you disagree with db driver, we print compilation time warning. If you found an example, you can create a ticket to db vendor and restore order!

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

### Fetch scalar
Use fetch method overload to tell TrueSql interpret column as Nullable or NotNull.

```java
import static com.truej.sql.v3.source.Parameters.Nullable;
import static com.truej.sql.v3.source.Parameters.NotNull;
// ...
var infos = ds.q("select info from users where id = ?", 42)
    .fetchOne(Nullable, String.class);

var names = ds.q("select info from users where info is not null")
    .fetchOneOrZero(NotNull, String.class);
```

### Fetch with your own DTO
Use org.jetrbrains.annotations in DTO.

```java
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
// consider name Nullablle/NotNull
record User(long id, @Nullable String info) { }
record Clinic(long id, @NotNull String info) { }
```

### Fetch with .g
By default TrueSql annotate fields in line with db driver. You can change it with next syntax:

```java
var user = ds.q("""
    select 
        id, 
        info as ":t? info" 
    from users
    """)
    .g.fetchList(User.class);
```

<details>
    <summary>generated User</summary>

```java
record User(Long id, @Nullable String name) { }
```
</details>


```java
var user = ds.q("""
    select 
        id, 
        name as ":t! name" 
    from users
    """)
    .g.fetchList(User.class);
```

<details>
    <summary>generated User</summary>

```java
record User(Long id, String name) { }
```
</details>

###### NB: driver can say "Unknown" nullability. Then TrueSql accept user decision and dont print warnings

## Full featured
We save and ***improve*** all necessary JDBC possibilities.<br>
**All features above remain in force!**

### GeneratedKeys

```java
var user = ds.q("insert into users(name) values (?)", "Pavel")
            .asGeneratedKeys("id").fetchOne(Long.class);
```

### UpdateCount

```java
var updateCount = ds.q("update users set name = ? where id % 2 == 0", "Paul")
    .withUpdateCount.fetchNone();
```

### Batching

```java
record Discount(Date date, BigDecimal discount) {}

var discounts = List.of(
    new DateDiscount(LocalDate.of(2024, 7, 1), new BigDecimal("0.2")),
    new DateDiscount(LocalDate.of(2024, 8, 1), new BigDecimal("0.15"))
);

var keys = cn.q(
        discounts,
            """
            update bill b
            set discount = ?
            where cast(b.date as date) = ?
            """,
        v -> new Object[]{v.discount, v.date}
    ).withUpdateCount.fetchNone()
```
###### NB: batching works with both asGeneratedKeys() and withUpdateCount

### Transactions and connection pinning
In case you want pin connection

```java
ds.withConnection(cn -> {
    cn.q("set time zone 'America/New_York'").fetchNone();

    return cn.q("select amount, date from bill").g.fetchOne(Bill.class);
    }
)
```

In case you need transaction mode
```java
try {
    cn.inTransaction(() -> {
        cn.q("insert into clinic values(?, ?, ?)", 4, "Paris St. Marie Hospital", 2)
            .fetchNone();

        // do something dangerous

        throw new ForceRollback();
    });
} catch (ForceRollback ex) {
    // null
    var name = cn.q("select name from clinic where id = ?", 4)
        .fetchOneOrZero(String.class)

}
```

### Streaming fetching
If you don't want to materialize all ResultSet rows, you could use fetchStream()
```java
ds.withConnection(cn -> {
    try (
        var stream = cn.q("select id, name from users")
            .g.fetchStream(User.class)
    ) {
    var users = stream.toList();
    }
})
```

### Stored procedure call
TrueSql provide next way to use stored procedures and fetch out parameters

```java
import static com.truej.sql.v3.source.Parameters.*;

ds.q("{ call ? = some_procedure(?, ?) }", out(Integer.class), 42, inout(42))
    .asCall().fetchOne(Integer.class)
```
###### NB: functions out(), inout() has JDBC sense. Example provided on HSQLDB.

### Unfold parameters for "in-clause"
TrueSql can dynamicly generate query with n parameters
```java
var ids = List.of(1, 2, 3);
ds.q("select id, name from users where id in (?)", unfold(ids))
    .g.fetchList(User.class);
```
And if you have N-width parameter use next
```java
var data = List.of(
            new User(1L, "Joe"),
            new User(2L, "Donald")
            );

ds.q("""
    select 
        case when info is null then 'null' else info end as info
    from users 
    where (id, name) in (?)
    """, unfold(data, u -> new Object[]{u.id, u.name})
.fetchList(String.class);
```

## Extra type bindings
Nowadays bind custom types is a casual need. TrueSql provides two ways to bind parameters. 

### JDBC as object binding
If bound type may be mapped as preparedStatement.getObject()/setObject()

```java
import com.truej.sql.v3.bindings.AsObjectReadWrite;

class PgPoint { }; // in pg jdbc library
class PgPointRW extends AsObjectReadWrite<PGPoint> { };

@Configuration(
    typeBindings = {
        @TypeBinding(
            rw = PgPointRW.class
        )
    }
) public class PgDs extends DataSourceW {
    public PgDs(DataSource w) { super(w); }
}
```

### JDBC extended binding
Implement TypeReadWrite interface

```java
abstract class PgEnumRW<T extends Enum<T>> implements TypeReadWrite<T> {

    public abstract Class<T> aClass();

    @Override public T get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        return Enum.valueOf(aClass(), rs.getString(columnIndex));
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, T value
    ) throws SQLException {
        stmt.setObject(parameterIndex, value, Types.OTHER);
    }

    @Override public T get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        return Enum.valueOf(aClass(), stmt.getString(parameterIndex));
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.OTHER);
    }
}
```

Add bind and compatibility check to configuration

```java
enum UserSex {MALE, FEMALE}
class PgUserSexRW extends PgEnumRW<UserSex> {
    @Override public Class<UserSex> aClass() { return UserSex.class; }
}

@Configuration(
    typeBindings = {
        @TypeBinding(
            // JDBC column sql type
            compatibleSqlType = Types.OTHER,
            // JDBC column sql type name provided by db
            compatibleSqlTypeName = "user_sex",
            rw = PgUserSexRW.class
        )
    }
) public class PgDs extends DataSourceW {
    public PgDs(DataSource w) { super(w); }
}
```

### Usage in fetch
If type exists in db

```java
var userSex = ds.q("select sex from users where id = ?", 42).fetchOne(UserSex.class);
```
Otherwise, use next syntax

```java
var UserSex = ds.q("""
    select 
        name, 
        sex as ":t UserSex sex" 
    from users
    """).g.fetchList(User.class);
```

Specify nullability only after type name

```java
// Nullable
var UserSex = ds.q("""
    select 
        name, 
        sex as ":t UserSex? sex" 
    from users
    """).g.fetchList(User.class);
```



### Type bindings comatibility check
TrueSql will check type binding compatibility in order with this table
<table>
    <tr>
        <td><b>SqlType\SqlTypeName</td>
        <td><b>No</td>
        <td><b>Specified</td>
    </tr>
    <tr>
        <td><b>No</td>
        <td>check bound className match<br>with JDBC getColumnClassName()</td>
        <td>check db sql type name match<br>with compatibleSqlTypeName</td>
    </tr>
    <tr>
        <td><b>Specified</td>
        <td>check JDBC sql type match with compatibleSqlType</td>
        <td>check both compatibleSqlType<br>and compatibleSqlTypeName</td>
    </tr>
</table>

## Multiple database schemas in one module

```java
// some @Configuration
public class PgDs extends DataSourceW {
    public PgDs(DataSource w) { super(w); }
}
// some @Configuration
public class MSDs extends DataSourceW {
    public MSDs(DataSource w) { super(w); }
}
```

## DB constraint violation checks
Here an example how you can wrap all SQLException's that may arise at DataSourceW or ConnectionW:

```java
import net.truej.sql.ConstraintViolationException;

public class MainDataSource extends DataSourceW {
    public MainDataSource(DataSource w) { super(w); }

    @Override public RuntimeException mapException(SQLException ex) {

        var pgUniqueConstraintCode = "23505";
        if (pgUniqueConstraintCode.equals(ex.getSQLState()) &&
            ex instanceof PSQLException pex) {
            return new ConstraintViolationException(
                pex.getServerErrorMessage().getTable(),
                pex.getServerErrorMessage().getConstraint()
            );
        }

        return DataSourceW.super.mapException(ex);
    }
}
```
If you use net.truej.sql.ConstraintViolationException then TrueSql will check constraint existence in database. The way you can catch wrapped DB constraint violations:

```java
try {
	ds.q("insert into users values(1, ‘John’, null)").fetchNone();
} catch (ConstraintViolationException ex) {
	ex.when(
		new Constraint<>("users", "users_pk", () -> {
			throw new Handled("User with id=1 already exists.");
        })
    );
}
```
###### NB: also has overloads with database and schema

## 100% sql-injection safety guarantee
1. In case of unfold feature, TrueSql only add parameters nests to query text. In other cases query text stay static.
2. All parameters passes as PreparedStatement parameters.

For these reasons, sql-injection can't happen.

## Exceptional performance. Equal to JDBC
TrueSql translates to pure JDBC. This means that no others can be any faster.

??? here table with comparsion with other frameworks
