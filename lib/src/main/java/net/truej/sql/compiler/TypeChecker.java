package net.truej.sql.compiler;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.JCDiagnostic;
import net.truej.sql.bindings.Standard;

import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static net.truej.sql.compiler.DatabaseNames.*;

class TypeChecker {
    static Standard.Binding getBindingForClass(
        JCTree tree, List<Standard.Binding> typeBindings,
        String javaClassName, GLangParser.NullMode nullMode
    ) {
        return typeBindings.stream()
            .filter(b ->
                b.className().equals(javaClassName) ||
                b.className().endsWith(javaClassName)
            ).findFirst().orElseThrow(() -> new TrueSqlPlugin.ValidationException(
                tree, "has no binding for type " + javaClassName
            ));
    }

    interface TypeMismatchExceptionMaker {
        RuntimeException make(String typeKing, String excepted, String has);
    }

    static void assertTypesCompatible(
        String onDatabase,
        int sqlType,
        String sqlTypeName,
        String sqlJavaClassName,
        int sqlScale,
        Standard.Binding javaBinding,
        TypeMismatchExceptionMaker exceptionMaker
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
                sqlScale == 0 && (
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
        }

        // avoid JDBC 1.0 spec bug
        if (
            (
                javaBinding.className().equals(Byte.class.getName()) &&
                sqlType == Types.TINYINT
            ) ||
            (
                javaBinding.className().equals(Short.class.getName()) &&
                sqlType == Types.SMALLINT
            )
        ) return;

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
                throw exceptionMaker.make("type", javaBinding.className(), "java.lang.Byte");

            if (sqlType == Types.SMALLINT)
                throw exceptionMaker.make("type", javaBinding.className(), "java.lang.Short");
        }

        // respect Java autoboxing
        if (
            (
                (
                    javaBinding.className().equals(boolean.class.getName()) ||
                    javaBinding.className().equals(Boolean.class.getName())
                ) && (
                    sqlJavaClassName.equals(boolean.class.getName()) ||
                    sqlJavaClassName.equals(Boolean.class.getName()) ||
                    sqlType == Types.BOOLEAN
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
                    javaBinding.className().equals(char.class.getName()) ||
                    javaBinding.className().equals(Character.class.getName())
                ) && (
                    sqlJavaClassName.equals(char.class.getName()) ||
                    sqlJavaClassName.equals(Character.class.getName()) ||
                    sqlType == Types.CHAR
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
                    sqlJavaClassName.equals(Integer.class.getName()) ||
                    sqlType == Types.INTEGER
                )
            ) || (
                (
                    javaBinding.className().equals(long.class.getName()) ||
                    javaBinding.className().equals(Long.class.getName())
                ) && (
                    sqlJavaClassName.equals(long.class.getName()) ||
                    sqlJavaClassName.equals(Long.class.getName()) ||
                    sqlType == Types.BIGINT
                )
            ) || (
                (
                    javaBinding.className().equals(float.class.getName()) ||
                    javaBinding.className().equals(Float.class.getName())
                ) && (
                    sqlJavaClassName.equals(float.class.getName()) ||
                    sqlJavaClassName.equals(Float.class.getName()) ||
                    sqlType == Types.FLOAT
                )
            ) || (
                (
                    javaBinding.className().equals(double.class.getName()) ||
                    javaBinding.className().equals(Double.class.getName())
                ) && (
                    sqlJavaClassName.equals(double.class.getName()) ||
                    sqlJavaClassName.equals(Double.class.getName()) ||
                    sqlType == Types.DOUBLE
                )
            )
        ) return;

        if (
            javaBinding.compatibleSqlType() == null &&
            javaBinding.compatibleSqlTypeName() == null
        ) {
            if (!javaBinding.className().equals(sqlJavaClassName))
                throw exceptionMaker.make("type", javaBinding.className(), sqlJavaClassName);
        } else {
            if (javaBinding.compatibleSqlTypeName() != null) {
                if (!javaBinding.compatibleSqlTypeName().equals(sqlTypeName))
                    throw exceptionMaker.make(
                        "sql type name", javaBinding.compatibleSqlTypeName(), sqlTypeName
                    );
            }

            if (javaBinding.compatibleSqlType() != null) {
                if (javaBinding.compatibleSqlType() != sqlType)
                    throw exceptionMaker.make(
                        "sql type id (java.sql.Types)",
                        "" + javaBinding.compatibleSqlType(), "" + sqlType
                    );
            }
        }
    }

    // FIXME: {OK, WARNING} isNullabilityCompatible
    // TODO: для warning сделать отдельный канал 
    static void assertNullabilityCompatible(
        JCTree.JCCompilationUnit cu, CompilerMessages messages, JCTree tree,
        String fieldName, GLangParser.NullMode fieldNullMode,
        int columnIndex, GLangParser.ColumnMetadata column
    ) {
        enum ReportMode {WARNING, ERROR}
        interface Reporter {
            void report(ReportMode mode, GLangParser.NullMode you, GLangParser.NullMode driver);
        }

        var doReport = (Reporter) (m, you, driver) -> {
            var message = "nullability mismatch for column " + (columnIndex + 1) +
                          (fieldName != null ? " (for field `" + fieldName + "`) " : "") +
                          " your decision: " + you + " driver infers: " + driver;
            switch (m) {
                case WARNING:
                    messages.write(
                        cu, tree, JCDiagnostic.DiagnosticType.WARNING, message
                    );
                    break;
                case ERROR:
                    throw new TrueSqlPlugin.ValidationException(tree, message);
            }
        };

        switch (fieldNullMode) {
            case EXACTLY_NULLABLE:
                switch (column.nullMode()) {
                    case EXACTLY_NULLABLE,
                         DEFAULT_NOT_NULL: { } break;
                    case EXACTLY_NOT_NULL:
                        doReport.report(
                            ReportMode.WARNING,
                            GLangParser.NullMode.EXACTLY_NULLABLE,
                            GLangParser.NullMode.EXACTLY_NOT_NULL
                        );
                }
                break;
            case DEFAULT_NOT_NULL:
                switch (column.nullMode()) {
                    case EXACTLY_NULLABLE:
                        doReport.report(
                            ReportMode.ERROR,
                            GLangParser.NullMode.DEFAULT_NOT_NULL,
                            GLangParser.NullMode.EXACTLY_NULLABLE
                        );
                        break;
                    case DEFAULT_NOT_NULL,
                         EXACTLY_NOT_NULL: { } break;
                }
                break;
            case EXACTLY_NOT_NULL:
                switch (column.nullMode()) {
                    case EXACTLY_NULLABLE:
                        doReport.report(
                            ReportMode.WARNING,
                            GLangParser.NullMode.EXACTLY_NOT_NULL,
                            GLangParser.NullMode.EXACTLY_NULLABLE
                        );
                        break;
                    case DEFAULT_NOT_NULL,
                         EXACTLY_NOT_NULL: { } break;
                }
                break;
        }
    }

    static String inferType(
        ConfigurationParser.ParsedConfiguration parsedConfig,
        String onDatabase,
        GLangParser.ColumnMetadata column,
        GLangParser.NullMode nullMode
    ) {
        // нужно как-то понять что пользователь не создал конфликт по биндам ???
        if (
            nullMode == GLangParser.NullMode.DEFAULT_NOT_NULL ||
            nullMode == GLangParser.NullMode.EXACTLY_NOT_NULL
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
                column.javaClassName().equals(Character.class.getName()) ||
                column.sqlType() == Types.CHAR
            )
                return char.class.getName();

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

        // shim for new java.time API (JSR-310)
        return switch (column.sqlType()) {
            case Types.DATE -> "java.time.LocalDate";
            case Types.TIME -> "java.time.LocalTime";
            case Types.TIMESTAMP ->
                // https://github.com/pgjdbc/pgjdbc/pull/3006
                column.sqlTypeName().equals("timestamptz") // postgresql
                    ? "java.time.OffsetDateTime"
                    : "java.time.LocalDateTime";
            case Types.TIME_WITH_TIMEZONE -> "java.time.OffsetTime";
            case Types.TIMESTAMP_WITH_TIMEZONE ->
                column.javaClassName().equals("java.time.ZonedDateTime")
                    ? column.javaClassName() : "java.time.OffsetDateTime";
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
