package net.truej.sql.test;


import net.truej.sql.TrueSql;
import net.truej.sql.compiler.IntArrayNullableRW;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(POSTGRESQL)
@TrueSql public class __21__ArrayTest {
    @TestTemplate public void testInt(@NotNull MainDataSource cn) {
        var result = cn.q("""
            select
                ARRAY[1,2,3]::int[] as "privet"
            """).fetchOne(int[].class);

        Assertions.assertArrayEquals(result, new int[] {1, 2, 3});
    }

    @TestTemplate public void testIntNullable(@NotNull MainDataSource cn) {
        var result = cn.q("""
            select
                ARRAY[1,2,3]::int[] as "privet"
            """).fetchOne(Integer[].class);

        Assertions.assertArrayEquals(result, new Integer[] {1, 2, 3});
    }

    @TestTemplate public void testLong(@NotNull MainDataSource cn) {
        var result = cn.q("""
            select
                ARRAY[1,2,3]::bigint[] as "privet"
            """).fetchOne(long[].class);

        Assertions.assertArrayEquals(result, new long[] {1, 2, 3});
    }

    @TestTemplate public void testFloat(@NotNull MainDataSource cn) {
        var result = cn.q("""
            select
                ARRAY[1,2,3]::float4[] as "privet"
            """).fetchOne(float[].class);

        Assertions.assertArrayEquals(result, new float[] {1, 2, 3});
    }

    @TestTemplate public void testDouble(@NotNull MainDataSource cn) {
        var result = cn.q("""
            select
                ARRAY[1,2,3]::float8[] as "privet"
            """).fetchOne(double[].class);

        Assertions.assertArrayEquals(result, new double[] {1, 2, 3});
    }

    @TestTemplate public void testShort(@NotNull MainDataSource cn) {
        var result = cn.q("""
            select
                ARRAY[1,2,3]::int2[] as "privet"
            """).fetchOne(short[].class);

        Assertions.assertArrayEquals(result, new short[] {1, 2, 3});
    }

    @TestTemplate public void testBoolean(@NotNull MainDataSource cn) {
        var result = cn.q("""
            select
                ARRAY[1,0,1]::boolean[] as "privet"
            """).fetchOne(boolean[].class);

        Assertions.assertArrayEquals(result, new boolean[] {true, false, true});
    }
}
