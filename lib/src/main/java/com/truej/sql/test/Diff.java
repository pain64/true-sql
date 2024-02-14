package com.truej.sql.test;

public class Diff {
    // public final TableDiff[] tableDiffs;

    public Diff(String tableDiff) {

    }

    public interface DatabaseOperation {}
    public record Insert(String tableName, String... columnsValue) implements DatabaseOperation {}
    public record Delete(String tableName, String... columnsValue) implements DatabaseOperation {}

    public Diff(DatabaseOperation... operations) {

    }
}
