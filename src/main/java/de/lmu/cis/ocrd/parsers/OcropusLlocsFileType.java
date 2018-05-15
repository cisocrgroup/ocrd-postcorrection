package de.lmu.cis.ocrd.parsers;

public class OcropusLlocsFileType implements OCRFileType {
	@Override
	public boolean check(String name) {
			return name.endsWith(".llocs");
	}
}
