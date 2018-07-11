package de.lmu.cis.ocrd.parsers;

import de.lmu.cis.ocrd.SimpleDocument;
import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.Entry;

import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextArchiveParser implements Parser {
    private static Pattern pattern = Pattern.compile(".*?([0-9]+).*?\\.txt$");
    private final Archive ar;
    private final OCRFileType ocrFileType;

    public TextArchiveParser(Archive ar, OCRFileType ocrFileType) {
        this.ar = ar;
        this.ocrFileType = ocrFileType;
    }

    @Override
    public SimpleDocument parse() throws Exception {
        final SimpleDocument doc = new SimpleDocument().withPath(ar.getName().toString());
        for (Entry entry : ar) {
            if (!ocrFileType.check(entry.getName())) {
                continue;
            }
            final Matcher m = pattern.matcher(entry.getName().toString());
            if (!m.matches()) {
                continue;
            }
            try (InputStreamReader r = new InputStreamReader(ar.open(entry))) {
                final int pageID = Integer.parseInt(m.group(1));
                final Parser parser = new TextParser(pageID, r);
                doc.add(parser.parse());
            }
        }
        ar.close();
        return doc;
    }
}
