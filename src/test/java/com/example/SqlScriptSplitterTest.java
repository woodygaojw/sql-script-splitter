package com.example;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SqlScriptSplitterTest {

    @Test
    void split() {
        String script = "select * from table1 where a='1;'; select * from table2;";
        SqlScriptSplitter sqlScriptSplitter = new SqlScriptSplitter(script);
        List<String> sqls = sqlScriptSplitter.split();
        assertThat(sqls).hasSize(2);
    }

}
