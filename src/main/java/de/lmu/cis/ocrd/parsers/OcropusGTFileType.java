package de.lmu.cis.ocrd.parsers;

import java.nio.file.Path;
import java.util.regex.Matcher;

public class OcropusGTFileType implements OCRFileType {
	@Override
	public boolean check(Path path) {
		final Matcher m = OcropusFileType.pattern.matcher(path.getParent().getFileName().toString());
		if (m.matches()) {
		    return path.toString().toLowerCase().endsWith(".gt.txt");
		}
		return false;
	}
}
