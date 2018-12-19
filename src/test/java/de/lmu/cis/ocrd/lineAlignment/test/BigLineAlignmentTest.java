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
import java.util.Set;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class BigLineAlignmentTest {
	private final String r1 = "src/test/resources/1841-DieGrenzboten-abbyy.zip";
	private final String r2 = "src/test/resources/1841-DieGrenzboten-tesseract.zip";
	private final String r3 = "src/test/resources/1841-DieGrenzboten-ocropus.zip";
	private Project project;
	private HashSet<String> gold;

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

	@Before
	public void init() throws Exception {
		Document d1 = new ArchiveParser(new ABBYYXMLParserFactory(), new ABBYYXMLFileType(), new ZipArchive(r1)).parse();
		Document d2 = new ArchiveParser(new HOCRParserFactory(), new HOCRFileType(), new ZipArchive(r2)).parse();
		Document d3 = new OcropusArchiveLlocsParser(new ZipArchive(r3)).parse();
		project = new Project().put("abbyy", d1, true).put("tesseract", d2).put("ocropus", d3);
		gold = new HashSet<>();
		try (BufferedReader r = new BufferedReader(
				new FileReader(new File("src/test/resources/lineAlignmentsBig.txt")))) {
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
		final Set<String> got = getLineAlignments();
		for (String gotStr : got) {
			// System.out.println("GOT_STR: " + gotStr);
			assertThat(gold.contains(gotStr), is(true));
		}
		for (String goldStr : gold) {
			// System.out.println("GOLD_STR: " + goldStr);
			assertThat(got.contains(goldStr), is(true));
		}
	}

	private Set<String> getLineAlignments() throws Exception {
		HashSet<String> set = new HashSet<>();
		project.eachPage((page) -> {
			LineAlignment_Fast la = new LineAlignment_Fast(page, 3);
			for (ArrayList<OCRLine> alignments : la) {
				set.add(makeID(alignments));
			}
		});
		return set;
	}
}
