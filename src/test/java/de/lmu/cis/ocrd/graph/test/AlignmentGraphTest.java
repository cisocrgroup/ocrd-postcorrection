package de.lmu.cis.ocrd.graph.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;

import de.lmu.cis.iba.Pairwise_LCS_Alignment;
import de.lmu.cis.iba.Pairwise_LCS_Alignment.AlignmentPair;
import de.lmu.cis.ocrd.graph.AlignmentGraph;
import de.lmu.cis.ocrd.graph.Traverser;

public class AlignmentGraphTest {
	private static ArrayList<AlignmentPair> align(String a, String b) {
		Pairwise_LCS_Alignment algn = new Pairwise_LCS_Alignment(a, b);
		algn.align();
		return algn.getAligmentPairs();
	}

	private static String makeString(Traverser t, int id) {
		StringBuilder builder = new StringBuilder();
		t.eachLabel(id, (label) -> {
			builder.append(label.getLabel());
		});
		return builder.toString();
	}

	@Test
	public void testOverlap() throws Exception {
		String a = "diee Presse";
		String b = "die Preſſe";
		ArrayList<Pairwise_LCS_Alignment.AlignmentPair> as = align(a, b);
		AlignmentGraph g = new AlignmentGraph(as, a, b);
		// System.out.println(g.getStartNode().toDot());
		assertThat(makeString(g.getTraverser(), 0), is('#' + a + '$'));
		assertThat(makeString(g.getTraverser(), 1), is('#' + b + '$'));
	}

	@Test
	public void testSimple() throws Exception {
		String a = "die Presse";
		String b = "di Preſſe";
		ArrayList<Pairwise_LCS_Alignment.AlignmentPair> as = align(a, b);
		AlignmentGraph g = new AlignmentGraph(as, a, b);
		// System.out.println(g.getStartNode().toDot());
		assertThat(makeString(g.getTraverser(), 0), is('#' + a + '$'));
		assertThat(makeString(g.getTraverser(), 1), is('#' + b + '$'));
	}
}
