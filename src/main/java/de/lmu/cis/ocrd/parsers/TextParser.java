package de.lmu.cis.ocrd.parsers;

import com.google.common.io.LineReader;
import de.lmu.cis.ocrd.SimpleDocument;
import de.lmu.cis.ocrd.SimpleLine;

import java.io.Reader;

public class TextParser implements Parser {
    private final Reader reader;
    private final int pageID;

    TextParser(int pageID, Reader reader) {
        this.reader = reader;
        this.pageID = pageID;
    }

    @Override
    public SimpleDocument parse() throws Exception {
        final SimpleDocument document = new SimpleDocument();
        final LineReader lr = new LineReader(reader);
        String line;
        int lineID = 0;
        while ((line = lr.readLine()) != null) {
            // skip empty lines
            if (line.trim().isEmpty()) {
                continue;
            }
            final SimpleLine l = SimpleLine.normalized(line, 0).withLineID(++lineID).withPageID(pageID);
            document.add(pageID, l);
        }
        return document;
    }
}
