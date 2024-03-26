package com.truej.sql.v3.fetch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Concrete {
    public final PreparedStatement stmt;
    public final ResultSet rs;

    public Concrete(PreparedStatement stmt, ResultSet rs) {
        this.stmt = stmt;
        this.rs = rs;
    }
}
