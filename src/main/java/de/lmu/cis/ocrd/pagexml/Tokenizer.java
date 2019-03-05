package de.lmu.cis.ocrd.pagexml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tokenizer {
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private final List<String> left, right, alone;

    public Tokenizer(Map<String, String> symbols) throws Exception {
        left = new ArrayList<>();
        right = new ArrayList<>();
        alone = new ArrayList<>();
        for (Map.Entry<String, String> symbol: symbols.entrySet()) {
            switch (symbol.getValue().toLowerCase()) {
                case LEFT:
                    left.add(symbol.getKey());
                    break;
                case RIGHT:
                    right.add(symbol.getKey());
                    break;
                case "center":
                    alone.add(symbol.getKey());
                    break;
                default:
                    throw new Exception("bad orientation: " + symbol.getValue());
            }
        }
    }
    public Page tokenize(Page page) throws Exception {
        for (Line line: page.getLines()) {
            for (Word word: line.getWords()) {
                word.split(tokenize(word.getUnicode().get(0)));
            }
        }
        return page;
    }

    public String[] tokenize(String token) {
        for (String l: left) {
            token = token.replace(l, l+" ");
        }
        for (String r: right) {
            token = token.replace(r, " " + r);
        }
        for (String n: alone) {
            token = token.replace(n, " " + n + " ");
        }
        return token.trim().split("\\s+");
    }
}
