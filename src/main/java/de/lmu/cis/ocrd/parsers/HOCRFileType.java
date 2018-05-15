package de.lmu.cis.ocrd.parsers;

public class HOCRFileType implements OCRFileType {

	@Override
	public boolean check(String name) {
		return name.contains("hocr") || name.contains("HOCR") || name.endsWith(".html") || name.endsWith(".htm");
	}
}
