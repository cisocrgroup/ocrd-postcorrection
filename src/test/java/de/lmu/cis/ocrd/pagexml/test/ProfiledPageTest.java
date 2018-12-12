package de.lmu.cis.ocrd.pagexml.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.TextEquiv;
import de.lmu.cis.ocrd.profile.Candidate;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProfiledPageTest {
	private Page page;

	@Before
	public void init() throws Exception {
		page = Page.open(Paths.get("src/test/resources/profiled-page.xml"));
	}

	@Test
	public void testFirstLineFirstWordTextEquivNumber() {
		assertThat(firstWord().size(), is(5));
	}

	@Test
	public void testGetIndex() {
		assertThat(firstWord().get(0).getIndex(), is(1));
	}

	@Test
	public void testGetConfidence() {
		assertThat(firstWord().get(0).getConfidence(), is(Double.parseDouble(
				"0.866034939912189")));
	}

	@Test
	public void testGetDataType() {
		assertThat(
				firstWord().get(0).getDataType(),
				is("ocrd-cis-word-alignment-master-ocr"));
	}

	@Test
	public void testGetDataTypeDetails() {
		final String ddt = firstWord().get(3).getDataTypeDetails();
		final Candidate candidate = new Gson().fromJson(ddt, Candidate.class);
		assertThat(candidate.Distance, is(1));
		assertThat(candidate.Suggestion, is("joannes"));
	}

	private List<TextEquiv> firstWord() {
		return page.getLines().get(0).getWords().get(0).getTextEquivs();
	}
}
