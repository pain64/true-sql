// this file will be generated with TrueSql annotation processor

package com.truej.sql.showcase;


import java.math.BigDecimal;
import java.util.List;

public class __06__GenerateDtoG {
    record User(long id, String name, String email) { }

    record Bill(Long id, String currency, BigDecimal money) { }
    record Patient(Long id, String name, List<Bill> bills) { }
    record Doctor(Long id, String name) { }
    record Clinic(
        Long id, String name,
        List<String> addresses,
        List<Doctor> doctors,
        List<Patient> patients
    ) { }
}
