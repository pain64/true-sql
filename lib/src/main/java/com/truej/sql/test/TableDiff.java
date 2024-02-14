package com.truej.sql.test;

public class TableDiff {
    public final String tableName;
    public final String rowsDiff;

    public TableDiff(String tableName, String rowsDiff) {
        this.tableName = tableName;
        this.rowsDiff = rowsDiff;
    }
}
