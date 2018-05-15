package de.lmu.cis.ocrd.parsers;

public class OcropusGTFileType implements OCRFileType {
	@Override
	public boolean check(String name) {
			return name.endsWith(".gt.txt");
	}
}
