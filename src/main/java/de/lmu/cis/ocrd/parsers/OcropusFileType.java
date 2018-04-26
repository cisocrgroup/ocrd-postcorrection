package de.lmu.cis.ocrd.parsers;

public class OcropusFileType implements XMLFileType {

	@Override
	public boolean check(String name) {
			return name.endsWith(".txt") && !name.endsWith(".gt.txt");
	}
}
