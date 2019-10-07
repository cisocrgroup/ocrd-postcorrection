package de.lmu.cis.ocrd.ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class CharacterNGrams {
	public static FreqMap fromCSV(String path) throws Exception {
		try (InputStream is = new FileInputStream(new File(path))) {
			final FreqMap map = new FreqMap();
			return addFromCSV(is, map);
		}
	}

	static FreqMap addFromCSV(String path, FreqMap map) throws Exception {
		if (path.endsWith(".gz")) {
			return addFromZippedCSV(path, map);
		}
		try (InputStream is = new FileInputStream(new File(path))) {
			return addFromCSV(is, map);
		}
	}

	private static FreqMap addFromZippedCSV(String path, FreqMap map) throws Exception {
		try (InputStream is = new GZIPInputStream(new FileInputStream(new File(path)))) {
			return addFromCSV(is, map);
		}
	}

	private static FreqMap addFromCSV(InputStream is, FreqMap map) throws Exception {
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
