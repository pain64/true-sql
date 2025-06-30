package com.example.demo;

import net.truej.sql.TrueSql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.DemoApplication.MainDb;
import com.example.demo.ControllerG.*;

@TrueSql @RestController public class Controller {

    @Autowired MainDb ds;

    @GetMapping("/") List<User> hello() {
        return ds.q("select * from users").g.fetchList(User.class);
    }
}
