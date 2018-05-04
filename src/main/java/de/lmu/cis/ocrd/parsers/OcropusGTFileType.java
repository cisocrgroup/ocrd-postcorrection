package de.lmu.cis.ocrd.parsers;

public class OcropusGTFileType implements XMLFileType {
	@Override
	public boolean check(String name) {
			return name.endsWith(".gt.txt");
	}
}
