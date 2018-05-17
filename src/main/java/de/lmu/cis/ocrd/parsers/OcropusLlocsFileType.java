package de.lmu.cis.ocrd.parsers;

import java.nio.file.Path;

public class OcropusLlocsFileType implements OCRFileType {
	@Override
	public boolean check(Path path) {
		return path.toString().toLowerCase().endsWith(".llocs");
	}
}
