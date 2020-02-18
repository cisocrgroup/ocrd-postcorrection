package de.lmu.cis.ocrd.pagexml.test;

import de.lmu.cis.ocrd.pagexml.Page;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PageTest {
	private Page page;

	@Before
	public void init() throws Exception {
		page = Page.open(Paths.get("src/test/resources/page.xml"));
	}

	@Test
	public void pageHas24Lines() throws Exception {
		assertThat(page.getLines().size(), is(24));
	}

	@Test
	public void firstLineHas3Words() throws Exception {
		assertThat(page.getLines().get(0).getWords().size(), is(3));
	}

	@Test
	public void firstLineID() throws Exception {
		assertThat(page.getLines().get(0).getID(), is("tl_1"));
	}

	@Test
	public void firstLineUnicode() throws Exception {
		assertThat(page.getLines().get(0).getUnicode().get(0), is("Berliniſche Monatsſchrift."));
	}

	@Test
	public void secondLineHas2Words() throws Exception {
		assertThat(page.getLines().get(1).getWords().size(), is(2));
	}

	@Test
	public void secondLineID() throws Exception {
		assertThat(page.getLines().get(1).getID(), is("tl_2"));
	}

	@Test
	public void secondLineUnicode() throws Exception {
		assertThat(page.getLines().get(1).getUnicode().get(0), is("1784 ."));
	}

	@Test
	public void thirdLineHas5Words() throws Exception {
		assertThat(page.getLines().get(2).getWords().size(), is(5));
	}

	@Test
	public void thirdLineID() throws Exception {
		assertThat(page.getLines().get(2).getID(), is("tl_3"));
	}
	
	@Test
	public void thirdLineUnicode() throws Exception {
		assertThat(page.getLines().get(2).getUnicode().get(0), is("Zwoͤlftes Stuͤk . December ."));
	}

	@Test
	public void firstWordOfFirstLineID() throws Exception {
		assertThat(page.getLines().get(0).getWords().get(0).getID(), is("w_w1aab1b1b2b1b1ab1"));
	}

	@Test
	public void firstWordOfFirstLineUnicode() throws Exception {
		assertThat(page.getLines().get(0).getWords().get(0).getUnicode().get(0), is("Berliniſche"));
	}

	@Test
	public void secondWordOfSecondLineID() throws Exception {
		assertThat(page.getLines().get(1).getWords().get(1).getID(), is("word_1478541239125_799"));
	}

	@Test
	public void SecondWordOfSecondLineUnicode() throws Exception {
		assertThat(page.getLines().get(1).getWords().get(1).getUnicode().get(0), is("."));
	}

	@Test
	public void thirdWordOfThirdLineID() throws Exception {
		assertThat(page.getLines().get(2).getWords().get(2).getID(), is("word_1478541244021_801"));
	}

	@Test
	public void ThirdWordOfThirdLineUnicode() throws Exception {
		assertThat(page.getLines().get(2).getWords().get(2).getUnicode().get(0), is("."));
	}

	@Test
	public void FirstGlyphID() throws Exception {
		assertThat(page.getLines().get(2).getWords().get(1).getGlyphs().get(0).getID(), is("g1"));
	}

	@Test
	public void FirstGlyphUnicode() throws Exception {
		assertThat(page.getLines().get(2).getWords().get(1).getGlyphs().get(0).getUnicode().get(0), is("S"));
	}

	@Test
	public void SecondGlyphID() throws Exception {
		assertThat(page.getLines().get(2).getWords().get(1).getGlyphs().get(1).getID(), is("g2"));
	}

	@Test
	public void SecondGlyphUnicode() throws Exception {
		assertThat(page.getLines().get(2).getWords().get(1).getGlyphs().get(1).getUnicode().get(0), is("t"));
	}

	@Test
	public void ThirdGlyphID() throws Exception {
		assertThat(page.getLines().get(2).getWords().get(1).getGlyphs().get(2).getID(), is("g3"));
	}

	@Test
	public void ThirdGlyphUnicode() throws Exception {
		assertThat(page.getLines().get(2).getWords().get(1).getGlyphs().get(2).getUnicode().get(0), is("uͤ"));
	}

	@Test
	public void FourthGlyphID() throws Exception {
		assertThat(page.getLines().get(2).getWords().get(1).getGlyphs().get(3).getID(), is("g4"));
	}

	@Test
	public void FourthGlyphUnicode() throws Exception {
		assertThat(page.getLines().get(2).getWords().get(1).getGlyphs().get(3).getUnicode().get(0), is("k"));
	}

	@Test
	public void wordID() throws Exception {
		assertThat(page.getLines().get(2).getWords().get(1).getID(), is("word_1478541244022_802"));
	}
}
