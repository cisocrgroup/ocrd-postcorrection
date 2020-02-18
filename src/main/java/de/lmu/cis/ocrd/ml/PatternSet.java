package de.lmu.cis.ocrd.ml;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

public class PatternSet extends HashSet<String> {
    public static PatternSet read(InputStream is) throws Exception {
        PatternSet ps = new PatternSet();
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = r.readLine()) != null) {
            ps.read(line);
        }
        return ps;
    }

    private void read(String line) throws Exception {
        int pos = line.indexOf('#');
        if (pos != -1) {
            line = line.substring(0, pos).trim();
        }
        if (line.isEmpty()) {
            return;
        }
        pos = line.indexOf(':');
        if (pos == -1) {
            throw new Exception("invalid input line: " + line);
        }
        this.add(line);
    }
}
