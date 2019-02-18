package de.lmu.cis.ocrd.pagexml.test;

import de.lmu.cis.ocrd.pagexml.Tokenizer;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TokenizerTest {
    private Tokenizer tokenizer;

    @Before
    public void init() throws Exception {
        Map<String, String> symbols = new HashMap<>();
        symbols.put(",", "left");
        symbols.put(".", "left");
        symbols.put(";", "left");
        symbols.put(":", "left");
        symbols.put("/", "left");
        symbols.put("-", "left");
        symbols.put("?", "left");
        symbols.put("!", "left");
        symbols.put(")", "left");
        symbols.put("(", "right");
        symbols.put("|", "none");
        tokenizer = new Tokenizer(symbols);
    }

    @Test
    public void testLeft() {
        final String test = "a.b,c?";
        final String[] want = {"a.", "b,", "c?"};
        check(test, want);
    }

    @Test
    public void testLeftRight() {
        final String test = "(a)b.(c)";
        final String[] want = {"(a)", "b.", "(c)"};
        check(test, want);
    }

    @Test
    public void testNone() {
        final String test = "|a|b|";
        final String[] want = {"|", "a", "|", "b", "|"};
        check(test, want);
    }

    private void check(String test, String[] want) {
        final String[] got = tokenizer.tokenize(test);
        assertThat(got, is(want));

    }
}
