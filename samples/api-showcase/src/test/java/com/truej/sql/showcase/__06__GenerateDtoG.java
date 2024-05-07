// this file will be generated with TrueSql annotation processor

package com.truej.sql.showcase;

import com.truej.sql.v3.Group;

import java.math.BigDecimal;
import java.util.List;

public class __06__GenerateDtoG {
    record User(long id, String name, String email) {}
    record Bank(long id, BigDecimal money) { }
    record Patient(long id, String name, @Group List<Bank> banks) { }
    record Doctor(long id, String name) { }
    record Clinic(
        long id, String name,
        @Group List<Patient> patients,
        @Group List<Doctor> doctors
    ) { }
}
