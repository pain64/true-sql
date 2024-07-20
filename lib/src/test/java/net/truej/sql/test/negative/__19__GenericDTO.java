package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Disabled
@ExtendWith(TrueSqlTests2.class)
//@TrueSqlTests2.Message(
//    kind = ERROR, text = "has no type binding for net.truej.sql.test.negative.__07__NoTypeBinding_.User.Trap"
//)
@TrueSql public class __19__GenericDTO {
//     class User<T> {
//        Long id;
//        String name;
//        @Nullable T info;
//        T t;
//
//        public User(Long id, String name, @Nullable T info) {
//            this.id = id;
//            this.name = name;
//            this.info = info;
//        }
//    }
//
//    class User2 extends User<String> {
//        public User2(Long id, String name, @Nullable String info) {
//            super(id, name, info);
//        }
//    }
//
//    @TestTemplate
//    public void test(MainDataSource ds) {
//        ds.q("select * from users").fetchList(User2.class);
//    }
}
