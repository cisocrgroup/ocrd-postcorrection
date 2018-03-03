import de.lmu.cis.ocrd.graph.AlignmentGraph;
import de.lmu.cis.iba.Pairwise_LCS_Alignment;
import de.lmu.cis.iba.Pairwise_LCS_Alignment.AlignmentPair;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import de.lmu.cis.api.model.Book;
import de.lmu.cis.api.model.Project;
import de.lmu.cis.api.model.Projects;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class AlignmentGraphTest {
  @Test
  public void testSimple() throws Exception {
    String a = "die Presse";
    String b = "di Preſſe";
    ArrayList<Pairwise_LCS_Alignment.AlignmentPair> as = align(a, b);
    AlignmentGraph g = new AlignmentGraph(as, a, b);
    assertThat(g.getStartNode().traverse(0), is('#' + a + '$'));
    assertThat(g.getStartNode().traverse(1), is('#' + b + '$'));
  }
  // @Test
  // public void testOverlap() throws Exception {
  //   String a = "diee Presse";
  //   String b = "die Preſſe";
  //   ArrayList<Pairwise_LCS_Alignment.AlignmentPair> as = align(a, b);
  //   AlignmentGraph g = new AlignmentGraph(as, a, b);
  //   assertThat(g.getStartNode().traverse(0), is('#' + a + '$'));
  //   assertThat(g.getStartNode().traverse(1), is('#' + b + '$'));
  // }

  private ArrayList<AlignmentPair> align(String a, String b) {
    Pairwise_LCS_Alignment algn = new Pairwise_LCS_Alignment(a, b);
    algn.align();
    return algn.getAligmentPairs();
  }
}
