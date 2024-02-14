package com.truej.sql.showcase.gen;

import java.math.BigDecimal;

public class ExampleDto {
    public static class User {
        public final String name;
        public final int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    // Will be classes
    public record Doctor(long id, String name) {}
    public record Bank(long id, BigDecimal money) {}
    public record Patient(String name, Bank[] banks) {}
    public record Clinic(
        long id, String name,
        User[] users, Doctor[] doctors
    ) {}

}
