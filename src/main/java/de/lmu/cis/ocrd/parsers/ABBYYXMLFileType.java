package de.lmu.cis.ocrd.parsers;

public class ABBYYXMLFileType implements OCRFileType {

	@Override
	public boolean check(String name) {
		return name.contains("abbyy") || name.contains("ABBYY");
	}

}
