package de.lmu.cis.ocrd.align.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.graph.AlignmentGraph;
import de.lmu.cis.ocrd.graph.LabelIterator;

public class AlignmentGraphIteratorTest {

	private static String makeString(AlignmentGraph g, int id) {
		StringBuilder builder = new StringBuilder();
		for (LabelIterator it = g.iterator(id); it.hasNext();) {
			builder.append(it.next().getChar());
		}
		return builder.toString();
	}

	@Test
	public void testOverlap() throws Exception {
		String a = "diee Presse";
		String b = "die Preſſe";
		AlignmentGraph g = new AlignmentGraph(a, b);
		// System.out.println(g.getStartNode().toDot());
		assertThat(makeString(g, 0), is('#' + a + '$'));
		assertThat(makeString(g, 1), is('#' + b + '$'));
	}

	@Test
	public void testSimple() throws Exception {
		String a = "die Presse";
		String b = "di Preſſe";
		AlignmentGraph g = new AlignmentGraph(a, b);
		// System.out.println(g.getStartNode().toDot());
		assertThat(makeString(g, 0), is('#' + a + '$'));
		assertThat(makeString(g, 1), is('#' + b + '$'));
	}
}
