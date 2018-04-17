import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class OcropusZipArchiveTest extends BaseDocumentTest {

	public OcropusZipArchiveTest() {
		super("src/test/resources/1841-DieGrenzboten-ocropus.zip");
	}

	@Test
	public void checkFirstLine() throws Exception {
		assertThat(findLine(179392, 0).getNormalized(), is("Deuiſchland und Belgien"));
	}

	@Test
	public void checkLastLine() throws Exception {
		assertThat(findLine(179492, 0x1F - 1).getNormalized(), is("a4"));
	}
}
