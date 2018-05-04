package de.lmu.cis.ocrd.parsers;

public class OcropusLlocsFileType implements XMLFileType {
	@Override
	public boolean check(String name) {
			return name.endsWith(".llocs");
	}
}
