import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class OcropusZipArchiveTest extends BaseDocumentTest {

	@Test
	public void checkFirstLine() throws Exception {
		assertThat(findLine(179392, 0).getNormalized(), is("Deuiſchland und Belgien"));
	}

	@Test
	public void checkLastLine() throws Exception {
		assertThat(findLine(179492, 0x1F - 1).getNormalized(), is("a4"));
	}

	@Before
	public void readOcropusZipArchive() throws IOException {
		readArchive("src/test/resources/1841-DieGrenzboten-ocropus.zip");
	}
}
