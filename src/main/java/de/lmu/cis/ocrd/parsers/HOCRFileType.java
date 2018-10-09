package de.lmu.cis.ocrd.parsers;

import java.nio.file.Path;

public class HOCRFileType implements OCRFileType {
	@Override
	public boolean check(Path path) {
		final String lcName = path.toString().toLowerCase();
		return lcName.contains("hocr") || lcName.endsWith(".html") || lcName.endsWith(".htm");
	}
}
