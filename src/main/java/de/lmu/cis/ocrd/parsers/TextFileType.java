package de.lmu.cis.ocrd.parsers;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class TextFileType implements OCRFileType {
    private final static Pattern pattern = Pattern.compile(".*?[0-9]+.*?\\.txt$");
    @Override
    public boolean check(Path path) {
        // make sure that this is *not* ocropus ../pageID/...txt
        if (OcropusFileType.pattern.matcher(path.getParent().getFileName().toString()).matches()) {
            return false;
        }
        // it should be .../...pageID.txt
        return pattern.matcher(path.toString().toLowerCase()).matches();
    }
}
