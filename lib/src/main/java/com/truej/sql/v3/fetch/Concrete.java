package com.truej.sql.v3.fetch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public record Concrete(PreparedStatement stmt, ResultSet rs) { }