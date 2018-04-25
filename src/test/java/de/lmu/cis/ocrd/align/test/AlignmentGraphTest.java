package de.lmu.cis.ocrd.align.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.align.Graph;

public class AlignmentGraphTest {

	private static String makeString(Graph g, int id) {
		StringBuilder builder = new StringBuilder();
		g.getTraverser().eachLabel(id, (label) -> {
			builder.append(label.getLabel());
		});
		return builder.toString();
	}

	@Test
	public void testOverlap() throws Exception {
		String a = "diee Presse";
		String b = "die Preſſe";
		Graph g = new Graph(a, b);
		// System.out.println(g.getStartNode().toDot());
		assertThat(makeString(g, 0), is('#' + a + '$'));
		assertThat(makeString(g, 1), is('#' + b + '$'));
	}

	@Test
	public void testSimple() throws Exception {
		String a = "die Presse";
		String b = "di Preſſe";
		Graph g = new Graph(a, b);
		// System.out.println(g.getStartNode().toDot());
		assertThat(makeString(g, 0), is('#' + a + '$'));
		assertThat(makeString(g, 1), is('#' + b + '$'));
	}
}
