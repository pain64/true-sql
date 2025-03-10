package net.truej.sql.compiler;

import com.sun.tools.javac.tree.JCTree;
import net.truej.sql.bindings.Standard;
import net.truej.sql.compiler.GLangParser.NullMode;

import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.List;

import static net.truej.sql.compiler.DatabaseNames.*;

class TypeChecker {
    static Standard.Binding getBindingForClass(
        JCTree tree, List<Standard.Binding> typeBindings,
        boolean isFullQualified, String javaClassName
    ) {
        return typeBindings.stream()
            .filter(b ->
                (isFullQualified && b.className().equals(javaClassName)) ||
                (!isFullQualified && b.className().endsWith(javaClassName))
            ).findFirst().orElseThrow(() ->
                new TrueSqlPlugin.ValidationException(
                    tree, "has no binding for type " + javaClassName
                ));
    }

    interface TypeMismatchHandler {
        RuntimeException onError(String typeKind, String expected, String has);
    }

    static void assertTypesCompatible(
        String onDatabase,
        int sqlType,
        String sqlTypeName,
        String sqlJavaClassName,
        int sqlScale,
        Standard.Binding javaBinding,
        TypeMismatchHandler handler
    ) {
        // vendor-specific
        if (
            onDatabase.equals(MYSQL_DB_NAME) ||
            onDatabase.equals(MARIA_DB_NAME)
        ) {
            if (
                sqlType == Types.BIGINT && (
                    javaBinding.className().equals(Long.class.getName()) ||
                    javaBinding.className().equals(long.class.getName())
                )
            ) return;
        }

        if (onDatabase.equals(ORACLE_DB_NAME)) {
            if (
                sqlTypeName.equals("DATE") &&
                javaBinding.className().equals(LocalDate.class.getName())
            )
                return;

            if (sqlTypeName.equals("UNSPECIFIED"))
                return;

            if (
                sqlTypeName.equals("NUMBER") &&
                (sqlScale == 0 || sqlScale == -127) && (
                    javaBinding.className().equals(Long.class.getName()) ||
                    javaBinding.className().equals(long.class.getName()) ||
                    javaBinding.className().equals(Integer.class.getName()) ||
                    javaBinding.className().equals(int.class.getName()) ||
                    javaBinding.className().equals(Short.class.getName()) ||
                    javaBinding.className().equals(short.class.getName()) ||
                    javaBinding.className().equals(Byte.class.getName()) ||
                    javaBinding.className().equals(byte.class.getName())
                )
            ) return;
        }

        if (onDatabase.equals(POSTGRESQL_DB_NAME)) {
            if (
                javaBinding.className().equals(OffsetDateTime.class.getName()) &&
                sqlTypeName.equals("timestamptz")
            ) return;

            if (
                javaBinding.className().equals(OffsetTime.class.getName()) &&
                sqlTypeName.equals("timetz")
            ) return;
        }

        if (onDatabase.equals(MSSQL_DB_NAME)) {
            if (
                javaBinding.className().equals(OffsetDateTime.class.getName()) &&
                sqlJavaClassName.equals("microsoft.sql.DateTimeOffset")
            )
                return;
        }

        if (onDatabase.equals(ORACLE_DB_NAME)) {
            if (
                javaBinding.className().equals(ZonedDateTime.class.getName()) &&
                sqlJavaClassName.equals("oracle.sql.TIMESTAMPTZ")
            )
                return;
        }

        // avoid JDBC 1.0 spec bug
        if (
            (
                javaBinding.className().equals(int.class.getName()) ||
                javaBinding.className().equals(Integer.class.getName())
            ) && (
                sqlJavaClassName.equals(int.class.getName()) ||
                sqlJavaClassName.equals(Integer.class.getName())
            )
        ) {

            if (sqlType == Types.TINYINT)
                throw handler.onError("type", javaBinding.className(), "java.lang.Byte");

            if (sqlType == Types.SMALLINT)
                throw handler.onError("type", javaBinding.className(), "java.lang.Short");
        }

        // respect Java autoboxing
        if (
            (
                (
                    javaBinding.className().equals(boolean.class.getName()) ||
                    javaBinding.className().equals(Boolean.class.getName())
                ) && (
                    sqlJavaClassName.equals(boolean.class.getName()) ||
                    sqlJavaClassName.equals(Boolean.class.getName())
                )
            ) || (
                (
                    javaBinding.className().equals(byte.class.getName()) ||
                    javaBinding.className().equals(Byte.class.getName())
                ) && (
                    sqlJavaClassName.equals(byte.class.getName()) ||
                    sqlJavaClassName.equals(Byte.class.getName()) ||
                    sqlType == Types.TINYINT
                )
            ) || (
                (
                    javaBinding.className().equals(short.class.getName()) ||
                    javaBinding.className().equals(Short.class.getName())
                ) && (
                    sqlJavaClassName.equals(short.class.getName()) ||
                    sqlJavaClassName.equals(Short.class.getName()) ||
                    sqlType == Types.SMALLINT
                )
            ) || (
                (
                    javaBinding.className().equals(int.class.getName()) ||
                    javaBinding.className().equals(Integer.class.getName())
                ) && (
                    sqlJavaClassName.equals(int.class.getName()) ||
                    sqlJavaClassName.equals(Integer.class.getName())
                )
            ) || (
                (
                    javaBinding.className().equals(long.class.getName()) ||
                    javaBinding.className().equals(Long.class.getName())
                ) && (
                    sqlJavaClassName.equals(long.class.getName()) ||
                    sqlJavaClassName.equals(Long.class.getName())
                )
            ) || (
                (
                    javaBinding.className().equals(float.class.getName()) ||
                    javaBinding.className().equals(Float.class.getName())
                ) && (
                    sqlJavaClassName.equals(float.class.getName()) ||
                    sqlJavaClassName.equals(Float.class.getName())
                )
            ) || (
                (
                    javaBinding.className().equals(double.class.getName()) ||
                    javaBinding.className().equals(Double.class.getName())
                ) && (
                    sqlJavaClassName.equals(double.class.getName()) ||
                    sqlJavaClassName.equals(Double.class.getName())
                )
            )
        ) return;

        if (
            javaBinding.compatibleSqlType() == null &&
            javaBinding.compatibleSqlTypeName() == null
        ) {
            if (!javaBinding.className().equals(sqlJavaClassName))
                throw handler.onError("type", javaBinding.className(), sqlJavaClassName);
        } else {
            if (
                javaBinding.compatibleSqlTypeName() != null &&
                !javaBinding.compatibleSqlTypeName().equalsIgnoreCase(sqlTypeName)
            )
                throw handler.onError(
                    "sql type name", javaBinding.compatibleSqlTypeName(), sqlTypeName
                );

            if (
                javaBinding.compatibleSqlType() != null &&
                javaBinding.compatibleSqlType() != sqlType
            )
                throw handler.onError(
                    "sql type id (java.sql.Types)",
                    "" + javaBinding.compatibleSqlType(), "" + sqlType
                );
        }
    }

    interface NullabilityMismatchHandler {
        void onMismatch(boolean isWarningOnly, NullMode sqlNullMode, NullMode javaNullMode);
    }

    static void assertNullabilityCompatible(
        NullMode sqlNullMode, NullMode javaNullMode, NullabilityMismatchHandler handler
    ) {
        enum Decision {OK, WARN, ERROR}
        var decision = Decision.OK;

        switch (javaNullMode) {
            case EXACTLY_NULLABLE:
                switch (sqlNullMode) {
                    case EXACTLY_NULLABLE,
                         DEFAULT_NOT_NULL: { } break;
                    case EXACTLY_NOT_NULL:
                        decision = Decision.WARN;
                        break;
                }
                break;
            case DEFAULT_NOT_NULL:
                switch (sqlNullMode) {
                    case EXACTLY_NULLABLE:
                        decision = Decision.ERROR;
                        break;
                    case DEFAULT_NOT_NULL,
                         EXACTLY_NOT_NULL: { } break;
                }
                break;
            case EXACTLY_NOT_NULL:
                switch (sqlNullMode) {
                    case EXACTLY_NULLABLE:
                        decision = Decision.WARN;
                        break;
                    case DEFAULT_NOT_NULL,
                         EXACTLY_NOT_NULL: { } break;
                }
                break;
        }

        if (decision != Decision.OK)
            handler.onMismatch(decision == Decision.WARN, sqlNullMode, javaNullMode);
    }

    static String inferType(
        ConfigurationParser.ParsedConfiguration parsedConfig,
        String onDatabase,
        GLangParser.ColumnMetadata column,
        NullMode nullMode
    ) {
        // нужно как-то понять что пользователь не создал конфликт по биндам ???
        if (
            nullMode == NullMode.DEFAULT_NOT_NULL ||
            nullMode == NullMode.EXACTLY_NOT_NULL
        ) {
            if (
                column.javaClassName().equals(Boolean.class.getName()) ||
                column.sqlType() == Types.BOOLEAN
            )
                return boolean.class.getName();

            if (
                column.javaClassName().equals(Byte.class.getName()) ||
                column.sqlType() == Types.TINYINT
            )
                return byte.class.getName();

            if (
                column.javaClassName().equals(Short.class.getName()) ||
                column.sqlType() == Types.SMALLINT
            )
                return short.class.getName();

            if (
                column.javaClassName().equals(Integer.class.getName()) ||
                column.sqlType() == Types.INTEGER
            )
                return int.class.getName();

            if (
                column.javaClassName().equals(Long.class.getName()) ||
                column.sqlType() == Types.BIGINT
            )
                return long.class.getName();

            if (
                column.javaClassName().equals(Float.class.getName()) ||
                column.sqlType() == Types.FLOAT
            )
                return float.class.getName();

            if (
                column.javaClassName().equals(Double.class.getName()) ||
                column.sqlType() == Types.DOUBLE
            )
                return double.class.getName();
        }

        // vendor-specific

        if (onDatabase.equals(MSSQL_DB_NAME)) {
            if (column.javaClassName().equals("microsoft.sql.DateTimeOffset"))
                return "java.time.OffsetDateTime";
        }

        if (onDatabase.equals(ORACLE_DB_NAME)) {
            if (column.javaClassName().equals("oracle.sql.TIMESTAMPTZ"))
                return "java.time.ZonedDateTime";
        }

        if (onDatabase.equals(POSTGRESQL_DB_NAME)) {
            if (column.sqlTypeName().equals("timetz"))
                return "java.time.OffsetTime";
            // https://github.com/pgjdbc/pgjdbc/pull/3006
            if (column.sqlTypeName().equals("timestamptz"))
                return "java.time.OffsetDateTime";
        }

        // shim for new java.time API (JSR-310)
        return switch (column.sqlType()) {
            case Types.DATE -> "java.time.LocalDate";
            case Types.TIME -> "java.time.LocalTime";
            case Types.TIMESTAMP -> "java.time.LocalDateTime";
            case Types.TIMESTAMP_WITH_TIMEZONE -> "java.time.OffsetDateTime";
            default -> parsedConfig.typeBindings().stream()
                .filter(b ->
                    column.sqlTypeName().equals(b.compatibleSqlTypeName())
                )
                .findFirst()
                .map(Standard.Binding::className)
                .orElse(column.javaClassName());
        };
    }
}
