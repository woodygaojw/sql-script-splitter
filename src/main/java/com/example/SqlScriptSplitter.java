package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 按分号拆分sql脚本，不支持包含存储过程的脚本。<br>
 * 这里解决的问题是，不能只简单地按分号拆分，需要识别注释和sqL字符串值里的分号不做拆分处理。<br>
 * 注释的处理：过滤单行注释（'--'、'#'、'//'）和注释块，保留hint
 */
public class SqlScriptSplitter {

    private static final char SINGLE_QUOTE = '\'';
    private static final char DOUBLE_QUOTE = '"';
    private static final char BACK_TICK = '`';

    private final char[] buf;
    private final int length;
    private final StringBuilder sqlBuf = new StringBuilder();
    private final List<String> sqlList = new ArrayList<>();
    private int pos = 0;

    public SqlScriptSplitter(String script) {
        if (script == null || script.isEmpty()) {
            buf = null;
            length = 0;
        } else {
            buf = script.toCharArray();
            length = buf.length;
        }
    }

    public List<String> split() {

        if (length == 0) {
            return Collections.emptyList();
        }

        while (pos < length) {
            if (matchToken(';')) {
                addSql();
                pos++;
            } else if (matchToken(SINGLE_QUOTE)) {
                processQuotes(SINGLE_QUOTE);
            } else if (matchToken(DOUBLE_QUOTE)) {
                processQuotes(DOUBLE_QUOTE);
            } else if (matchToken(BACK_TICK)) {
                processQuotes(BACK_TICK);
            } else if (matchToken("--") || matchToken('#') || matchToken("//")) {
                processLineComment();
            } else if (matchToken("/*+")) {
                processHint();
            } else if (matchToken("/*")) {
                processBlockComment();
            } else {
                appendChar();
            }
        }

        addSql();

        return sqlList;
    }

    /**
     * 将当前字符添加到sqlBuffer中，并将pos指向下一个字符
     */
    private void appendChar() {
        char c = buf[pos];
        sqlBuf.append(c);
        pos++;
    }

    /**
     * 将n个字符添加到sqLBuffer中，并修攻pos
     */
    private void appendChar(int n) {
        for (int i = 0; i < n; i++) {
            appendChar();
        }
    }

    /**
     * 从当前位置开始匹配token(单字符)，如果匹配则返回true，否则返回false。不改变当前位置pos的值。
     */
    private boolean matchToken(char token) {
        return buf[pos] == token;
    }

    /**
     * 从当前位置开始匹配token，如果匹配则返回true，否则返回false。不改变当前位置pos的值。
     */
    private boolean matchToken(String token) {

        int len = token.length();

        //剩余字符数小于token的长度，直接返回false
        if (length - pos < len) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            if (buf[pos + i] != token.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将当前的sqL语句添加到sqlList中
     */
    private void addSql() {
        if (sqlBuf.length() == 0) {
            return;
        }
        String sql = sqlBuf.toString().trim();
        System.out.println("###" + sql);
        if (!sql.isEmpty()) {
            sqlList.add(sql);
        }

        sqlBuf.setLength(0);
    }

    /**
     * 处理引号
     */
    private void processQuotes(char quote) {
        do {
            appendChar();
            if (matchToken(quote)) {
                appendChar();
                break;
            }
        } while (pos < length);
    }

    /**
     * 处理单行注释块，跳过当前行后面的内容
     */
    private void processLineComment() {
        while (pos < length) {
            char c = buf[pos];
            if (c == '\n' || c == '\r') {
                return;
            }
            pos++;
        }
    }

    /**
     * 处理hint，保留hint内容
     */
    private void processHint() {
        appendChar(3);
        while (pos < length) {
            if (matchToken("*/")) {
                appendChar(2);
                break;
            }
            appendChar();
        }
    }

    /**
     * 处理注释块，跳过注释块内容
     */
    private void processBlockComment() {
        pos += 2;
        while (pos < length) {
            if (matchToken("*/")) {
                pos += 2;
                break;
            }
            pos++;
        }
    }

}
