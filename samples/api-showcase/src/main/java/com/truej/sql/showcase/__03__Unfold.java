package com.truej.sql.showcase;

import java.util.List;

import static com.truej.sql.v3.source.Parameters.*;

public class __03__Unfold {

    void testUnfold(MainDataSource ds) {
        var ids = List.of(1, 2, 3);
        ds.q("select * from users where id in (.1)", unfold(ids))
            .fetchNone();
    }

    void testUnfold2(MainDataSource ds) {
        ds.q("select v from t1 where (id, v) in = (.1)",
            unfold2(List.of(
                new Pair<>(1, "a"), new Pair<>(2, "b")
            ))
        ).fetchList(String.class);
    }
}
