package de.lmu.cis.ocrd.ml;

import java.io.*;

public class CharacterNGrams {
    public static FreqMap fromCSV(String path) throws Exception {
        try (InputStream is = new FileInputStream(new File(path))) {
            return fromCSV(is);
        }
    }

    public static FreqMap fromCSV(InputStream is) throws Exception {
        final FreqMap nGrams = new FreqMap();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length != 2) {
                    throw new Exception("invalid csv line: " + line);
                }
                nGrams.add(fields[1], Integer.parseInt(fields[0]));
            }
        }
        return nGrams;
    }
}
