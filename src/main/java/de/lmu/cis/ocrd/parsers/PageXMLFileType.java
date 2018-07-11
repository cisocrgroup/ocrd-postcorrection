package de.lmu.cis.ocrd.parsers;

import java.nio.file.Path;

public class PageXMLFileType implements OCRFileType {
	@Override
	public boolean check(Path path) {
		return path.toString().toLowerCase().contains("page");
	}
}
