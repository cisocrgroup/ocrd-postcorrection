import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import de.lmu.cis.ocrd.archive.Archive;
import de.lmu.cis.ocrd.archive.Entry;
import de.lmu.cis.ocrd.archive.DirectoryArchive;
import de.lmu.cis.ocrd.parsers.ABBYYXMLFileType;
import de.lmu.cis.ocrd.parsers.ABBYYXMLParserFactory;
import de.lmu.cis.ocrd.parsers.ArchiveParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;

public class ArchiveDirectoryTest extends BaseDocumentTest {
		private static final String resource = "src/test/resources/test-dir";
		private Archive dir;

		@Before
		public void init() throws IOException {
				this.dir = new DirectoryArchive(resource);
		}

		@After
		public void close() throws IOException {
				this.dir.close();
		}

		@Test
		public void testDirectoryArchivePath() {
				assertThat(this.dir.getName().toString(), is(resource));
		}

		@Test
		public void testContainsFileA() {
				assertThat(find(resource + "/a/test.txt"), is(true));
		}

		@Test
		public void testContainsFileB() {
				assertThat(find(resource + "/b/test.txt"), is(true));
		}

		@Test
		public void testNotContainsFileC() {
				assertThat(find(resource + "/c/test.txt"), is(false));
		}

		@Test
		public void testFileAContent() throws IOException {
				assertThat(read(resource + "/a/test.txt"), is("test-dir/a/test.txt"));
		}

		@Test
		public void testFileBContent() throws IOException {
				assertThat(read(resource + "/b/test.txt"), is("test-dir/b/test.txt"));
		}

		private boolean find(String path) {
				for (Entry entry : dir) {
						if (path.equals(entry.getName().toString())) {
								return true;
						}
				}
				return false;
		}

		private String read(String path) throws IOException {
				for (Entry entry : dir) {
						if (path.equals(entry.getName().toString())) {
								try (InputStream is = dir.open(entry)) {
										return IOUtils.toString(is, Charset.defaultCharset()).trim();
								}
						}
				}
				return "";
		}
}
