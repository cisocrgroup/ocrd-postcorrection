package de.lmu.cis.ocrd.ml;

import java.io.*;

public class CharacterNGrams {
	public static FreqMap fromCSV(String path) throws Exception {
		try (InputStream is = new FileInputStream(new File(path))) {
			final FreqMap map = new FreqMap();
			return addFromCSV(is, map);
		}
	}

	public static FreqMap addFromCSV(String path, FreqMap map) throws Exception {
		try (InputStream is = new FileInputStream(new File(path))) {
			return addFromCSV(is, map);
		}
	}

	public static FreqMap addFromCSV(InputStream is, FreqMap map) throws Exception {
		try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = r.readLine()) != null) {
				String[] fields = line.split(",");
				if (fields.length != 2) {
					throw new Exception("invalid csv line: " + line);
				}
				map.add(fields[1], Integer.parseInt(fields[0]));
			}
		}
		return map;
	}
}
