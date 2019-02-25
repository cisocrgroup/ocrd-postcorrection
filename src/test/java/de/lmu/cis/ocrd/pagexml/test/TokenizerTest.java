package de.lmu.cis.ocrd.pagexml.test;

import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Tokenizer;
import de.lmu.cis.ocrd.pagexml.Word;
import org.junit.Before;
import org.junit.Test;
import org.pmw.tinylog.Logger;

import java.nio.file.Paths;
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
        checkTokenization(test, want);
    }

    @Test
    public void testLeftRight() {
        final String test = "(a)b.(c)";
        final String[] want = {"(a)", "b.", "(c)"};
        checkTokenization(test, want);
    }

    @Test
    public void testNone() {
        final String test = "|a|b|";
        final String[] want = {"|", "a", "|", "b", "|"};
        checkTokenization(test, want);
    }

    private void checkTokenization(String test, String[] want) {
        final String[] got = tokenizer.tokenize(test);
        assertThat(got, is(want));

    }

    @Test
    public void testTokenizePageXML1stWord() throws Exception {
        checkToken(0, 0, "e", "word0000");
    }

    @Test
    public void testTokenizePageXML2ndWord() throws Exception {
        checkToken(0, 1, "Sener", "_0001");
    }

    @Test
    public void testTokenizePageXML3rdWord() throws Exception {
        checkToken(0, 2, "ibus", "_0002");
    }

    @Test
    public void testTokenizePageXML4thWord() throws Exception {
        checkToken(0, 3, "nom/", "_0001");
    }

    @Test
    public void testTokenizePageXML5thWord() throws Exception {
        checkToken(0, 4, "inum.", "_0002");
    }

    private void checkToken(int iLine, int iWord, String token, String idSuf) throws Exception {
        Page page = Page.open(Paths.get("src/test/resources/tokenize_page.xml"));
        page = tokenizer.tokenize(page);
        final Word word = page.getLines().get(iLine).getWords().get(iWord);
        assertThat(word.getUnicode().get(0), is(token));
        Logger.info("word '{}', ID: {}", word.getUnicode().get(0), word.getID());
        Logger.info("suf = {}", idSuf);
        assertThat(word.getID().endsWith(idSuf), is(true));
    }
}
