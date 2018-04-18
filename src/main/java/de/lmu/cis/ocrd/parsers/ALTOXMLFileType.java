package de.lmu.cis.ocrd.parsers;

public class ALTOXMLFileType implements XMLFileType {

	@Override
	public boolean check(String name) {
		return name.contains("alto") || name.contains("ALTO");
	}

}
