package de.lmu.cis.ocrd.lineAlignment.test;

import de.lmu.cis.iba.LineAlignment;
import de.lmu.cis.ocrd.OCRLine;
import de.lmu.cis.ocrd.test.TestDocument;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
public class SimpleLineAlignmentTest {
	@Test
	public void testSimple() throws Exception {

		TestDocument doc = new TestDocument();
		doc.withLine("xxxabcyy", 1, 0, "abbyy", true);
		doc.withLine("ooobcall", 1, 1, "tess", false);
		doc.withLine("yyyabcxx", 1, 2, "abbyy", true);
		doc.withLine("nnnbcaii", 1, 3, "tess", false);
		doc.withLine("zzzabczz", 1, 4, "abbyy", true);
		doc.withLine("rrrrrbcaoo", 1, 5, "tess", false);

		LineAlignment la = align(doc, 3);

		ArrayList<HashSet<Integer>> correct_ids = new ArrayList<HashSet<Integer>>();
		correct_ids.add(new HashSet<Integer>(Arrays.asList(0, 2, 4)));
		correct_ids.add(new HashSet<Integer>(Arrays.asList(1, 3, 5)));

		ArrayList<HashSet<Integer>> aligned_ids = new ArrayList<HashSet<Integer>>();
		for (ArrayList<OCRLine> aligned_lines : la) {
			HashSet<Integer> aligned_id_set = new HashSet<>();
			for (OCRLine line : aligned_lines) {
				aligned_id_set.add(line.line.getLineId());

			}

			aligned_ids.add(aligned_id_set);

		}

		int correctly_aligned = this.countCorrectAlignments(aligned_ids, correct_ids);

		assertThat(correctly_aligned, is(2));

	}

	@Test
	public void testIdenticalStrings() throws Exception {

		TestDocument doc = new TestDocument();
		doc.withLine("xxxabcyy", 1, 0, "abbyy", true);
		doc.withLine("ooobcall", 1, 1, "abbyy", false);
		doc.withLine("xxxabcyy", 1, 2, "tess", true);
		doc.withLine("nnnbcaii", 1, 3, "abbyy", false);
		doc.withLine("xxxabcyy", 1, 4, "abbyy", true);
		doc.withLine("rrrrrbcaoo", 1, 5, "abbyy", false);

		LineAlignment la = align(doc, 3);

		ArrayList<HashSet<Integer>> correct_ids = new ArrayList<HashSet<Integer>>();
		correct_ids.add(new HashSet<Integer>(Arrays.asList(0, 2, 4)));
		correct_ids.add(new HashSet<Integer>(Arrays.asList(1, 3, 5)));

		ArrayList<HashSet<Integer>> aligned_ids = new ArrayList<HashSet<Integer>>();
		for (ArrayList<OCRLine> aligned_lines : la) {
			HashSet<Integer> aligned_id_set = new HashSet<>();
			for (OCRLine line : aligned_lines) {
				aligned_id_set.add(line.line.getLineId());

			}

			aligned_ids.add(aligned_id_set);

		}

		int correctly_aligned = this.countCorrectAlignments(aligned_ids, correct_ids);

		assertThat(correctly_aligned, is(2));

	}

	@Test
	public void testOCRStrings() throws Exception {

		TestDocument doc = new TestDocument();
		doc.withLine("Dmlschland md Belgien", 1, 0, "abbyy", true);
		doc.withLine("Deuiſchland und Belgien", 1, 1, "abbyy", true);
		doc.withLine("Deutschland und Belgien", 1, 2, "tess", true);
		doc.withLine("Was wir wollen", 1, 3, "abbyy", true);
		doc.withLine("Wir könnten die Erscheinung dieser Blätter mit wenigen Worten mo", 1, 4, "abbyy", true);
		doc.withLine("Wir köͤnnten die Erſcheinung dieſer Blätter mit wenigen Worten mo", 1, 5, "abbyy", true);
		doc.withLine("Wir könnten die Erscheinung dieser Blätter mit wenigen Worten mo", 1, 6, "abbyy", true);
		doc.withLine("Was wir wollen", 1, 7, "abbyy", true);
		doc.withLine("Was wir wollen", 1, 8, "abbyy", true);

		doc.withLine("Brüssel Wenige Städte in Europa bieten gleiche Vortheile der", 1, 9, "abbyy", true);
		doc.withLine("Bruſſel Wenige Stͤdte in Europa bieten gleiche Vortheiſe der", 1, 10, "abbyy", true);
		doc.withLine("e durch Lage und Verhältnisse Innerhalb achtzehn Stun", 1, 11, "abbyy", true);
		doc.withLine("x durch Lage und Verhͤltniſſe Vnnerhalb achtzehn Stun", 1, 12, "abbyy", true);
		doc.withLine("Brussel Wenige Stͤdte in Europa beeten gleiche Vortheiſe der", 1, 13, "abbyy", true);
		doc.withLine("y durch Lage und Verhältnisse Innerhalb achtzehn Stun", 1, 14, "abbyy", true);

		doc.withLine("holländischen Mitstaatc sobald dieses geglückt war und die Aufregung", 1, 15, "abbyy", true);
		doc.withLine("hollaͤndiſchen Mitſtaate ſobaſr dieſes geglickt war und die Aufregung", 1, 16, "abbyy", true);
		doc.withLine("holländischen Mitstaatez sobald dieses gegliickt war und die Aufregung", 1, 17, "abbyy", true);

		LineAlignment la = align(doc, 3);

		ArrayList<HashSet<Integer>> correct_ids = new ArrayList<HashSet<Integer>>();
		correct_ids.add(new HashSet<Integer>(Arrays.asList(0, 1, 2)));
		correct_ids.add(new HashSet<Integer>(Arrays.asList(3, 7, 8)));
		correct_ids.add(new HashSet<Integer>(Arrays.asList(4, 5, 6)));
		correct_ids.add(new HashSet<Integer>(Arrays.asList(9, 10, 13)));
		correct_ids.add(new HashSet<Integer>(Arrays.asList(11, 12, 14)));
		correct_ids.add(new HashSet<Integer>(Arrays.asList(15, 16, 17)));

		ArrayList<HashSet<Integer>> aligned_ids = new ArrayList<HashSet<Integer>>();
		for (ArrayList<OCRLine> aligned_lines : la) {
			HashSet<Integer> aligned_id_set = new HashSet<>();
			for (OCRLine line : aligned_lines) {
				aligned_id_set.add(line.line.getLineId());

			}

			aligned_ids.add(aligned_id_set);

		}

		int correctly_aligned = this.countCorrectAlignments(aligned_ids, correct_ids);

		assertThat(correctly_aligned, is(6));

	}

	private LineAlignment align(TestDocument doc, int n) {
		LineAlignment l_alignment = null;
		try {
			l_alignment = new LineAlignment(doc, n);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return l_alignment;
	}

	public int countCorrectAlignments(ArrayList<HashSet<Integer>> aligned_ids,
	                                  ArrayList<HashSet<Integer>> correct_ids) {
		int correctly_aligned = 0;

		for (HashSet<Integer> correct_id_set : correct_ids) {

			for (HashSet<Integer> aligned_id_set : aligned_ids) {
				if (aligned_id_set.equals(correct_id_set)) {
					correctly_aligned++;
				}
			}

		}
		return correctly_aligned;
	}
}
