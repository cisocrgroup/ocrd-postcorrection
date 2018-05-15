package de.lmu.cis.ocrd.parsers;

public class OcropusFileType implements OCRFileType {

	@Override
	public boolean check(String name) {
			return name.endsWith(".txt") && !name.endsWith(".gt.txt");
	}
}
