package de.lmu.cis.ocrd.parsers;

import java.nio.file.Path;

public interface OCRFileType {
	boolean check(Path path);
}
