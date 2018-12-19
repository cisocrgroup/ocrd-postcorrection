package de.lmu.cis.ocrd.parsers;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcropusFileType implements OCRFileType {
	final static Pattern pattern = Pattern.compile("[0-9]+");

	@Override
	public boolean check(Path path) {
		final Matcher m = pattern.matcher(path.getParent().getFileName().toString());
		if (m.matches()) {
			final String lcName = path.toString().toLowerCase();
			return lcName.endsWith(".txt") && !lcName.endsWith(".gt.txt");
		}
		return false;
	}
}
