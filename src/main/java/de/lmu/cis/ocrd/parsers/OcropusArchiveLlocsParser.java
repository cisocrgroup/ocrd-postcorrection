package de.lmu.cis.ocrd.parsers;

import de.lmu.cis.ocrd.Line;
import de.lmu.cis.ocrd.SimpleLine;
import de.lmu.cis.ocrd.archive.Archive;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class OcropusArchiveLlocsParser extends OcropusArchiveParser {
    public OcropusArchiveLlocsParser(Archive ar) {
        super(ar, new OcropusLlocsFileType());
    }
    @Override
    protected Line readLine(InputStream is,  int pageID, int lineID) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder str = new StringBuilder();
        ArrayList<Double> cs = new ArrayList<>();
        String line;
        while ((line = r.readLine()) != null) {
            final String[] fields = line.split("\t+");
            if (fields.length < 3) {
                throw new Exception("invalid llocs line: " + line);
            }
            str.append(fields[0]);
            cs.add(Double.parseDouble(fields[2]));
        }
        return SimpleLine.normalized(str.toString(), cs).withLineID(lineID).withPageID(pageID);
    }
}
