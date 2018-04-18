import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.lmu.cis.ocrd.parsers.ABBYYDocumentBuilder;

public class ABBYYZipArchiveTest extends BaseDocumentTest {

	public ABBYYZipArchiveTest() {
		super(new ABBYYDocumentBuilder("src/test/resources/1841-DieGrenzboten-abbyy.zip"));
	}

	@Test
	public void checkFirstLine() throws Exception {
		assertThat(findLine(179392, 1).getNormalized(), is("Dmlschland md Belgien"));
	}

	@Test
	public void checkLastLine() throws Exception {
		assertThat(findLine(179492, 0x1F).getNormalized(), is("14"));
	}
}