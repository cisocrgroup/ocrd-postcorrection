package de.lmu.cis.ocrd.lineAlignment.test;

import de.lmu.cis.iba.LineAlignment_Fast;
import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.OCRLine;
import de.lmu.cis.ocrd.Project;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.parsers.*;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SmallLineAlignmentTest {
    private final String r1 = "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
    private final String r2 = "src/test/resources/1841-DieGrenzboten-tesseract-small.zip";
    private final String r3 = "src/test/resources/1841-DieGrenzboten-ocropus-small.zip";
    private Project project;
    private HashSet<String> gold;

    @Before
    public void init() throws Exception {
        Document d1 = new ArchiveParser(new ABBYYXMLParserFactory(), new ABBYYXMLFileType(), new ZipArchive(r1)).parse();
        Document d2 = new ArchiveParser(new HOCRParserFactory(), new HOCRFileType(), new ZipArchive(r2)).parse();
        Document d3 = new OcropusArchiveLlocsParser(new ZipArchive(r3)).parse();
        project = new Project().put("abbyy", d1, true).put("tesseract", d2).put("ocropus", d3);
        gold = new HashSet<>();
        try (BufferedReader r = new BufferedReader(new FileReader(new File("src/test/resources/lineAlignmentsSmall.txt")))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.length() > 0 && line.charAt(0) != '#') {
                    gold.add(line);
                }
            }
        }
    }

    @Test
    public void testFast() throws Exception {
        project.eachPage((page)-> {
            LineAlignment_Fast la = new LineAlignment_Fast(page, 3);
            // LineAlignment la = new LineAlignment(page, 3);
            for (ArrayList<OCRLine> alignments : la) {
                final String x = makeID(alignments);
                // for (OCRLine line : alignments) {
                //     System.out.println("# " + line.line.getNormalized());
                // }
                // System.out.println(x);
                assertThat(gold.contains(x), is(true));
            }
        });
    }

    private static String makeID(ArrayList<OCRLine> alignments) {
        StringBuilder builder = new StringBuilder();
        // not empty!
        alignments.sort(Comparator.comparing(a -> a.ocrEngine));
        for (OCRLine line : alignments) {
            builder.append('|');
            builder.append(line.pageSeq);
            builder.append(':');
            builder.append(line.line.getLineId());
            builder.append(':');
            builder.append(line.ocrEngine);
        }
        return builder.toString();
    }
}
