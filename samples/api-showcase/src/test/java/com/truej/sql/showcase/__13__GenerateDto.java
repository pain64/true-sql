package com.truej.sql.showcase;

import com.truej.sql.v3.GenerateDto;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static com.truej.sql.v3.TrueJdbc.g;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.truej.sql.showcase.__13__GenerateDto.Dto.*;

@GenerateDto public class __13__GenerateDto {
    @Test void test(DataSource ds) {
        assertEquals(
            Stmt."select id, name, email from users where id = \{42}"
                .fetchOne(ds, g(User.class))
            , "Joe"
        );
    }
}
