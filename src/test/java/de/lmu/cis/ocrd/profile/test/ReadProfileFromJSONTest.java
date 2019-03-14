package de.lmu.cis.ocrd.profile.test;

import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Profile;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
public class ReadProfileFromJSONTest {
	private static final String resource = "src/test/resources/profile-test.json";
	private Profile profile;

	@Before
	public void init() throws Exception {
		this.profile = Profile.read(resource);
	}

	@Test
	public void testContainsFirstToken() {
		assertThat(profile.containsKey("vnheilfolles"), is(true));
	}

	@Test
	public void testContainsFirstTokenIgnoreCase() {
		assertThat(profile.containsKey("Vnheilfolles"), is(true));
	}

	@Test
	public void testContainsHandlesNullKey() {
		assertThat(profile.containsKey(null), is(false));
	}

	@Test
	public void testContainsSecondToken() {
		assertThat(profile.containsKey("waſſer"), is(true));
	}

	@Test
	public void testContainsSecondTokenIgnoreCase() {
		assertThat(profile.containsKey("Waſſer"), is(true));
	}

	@Test
	public void testGetHandlesNull() {
		assertThat(profile.get(null), is(Optional.empty()));
	}

	@Test
	public void testGetNotFoundReturnsNull() {
		assertThat(profile.get("null"), is(Optional.empty()));
	}

	@Test
	public void testNotContainsToken() {
		assertThat(profile.containsKey("null"), is(false));
	}


	@Test
	public void testProfileContainsWasser() {
		assertThat(profile.containsKey("Wasser"), is(true));
	}

	@Test
	public void testWasserIsLexiconEntry() throws Exception {
		assertThat(profile.get("Wasser").orElseThrow(()->new Exception("missing entry")).Candidates[0].isLexiconEntry(), is(true));
	}

	@Test
	public void testNumberOfCandidatesForFirstToken() {
		assertThat(profile.get("Vnheilfolles").get().Candidates.length, is(41));
	}

	@Test
	public void testNumberOfCandidatesForFirstTokenIgnoresCase() {
		assertThat(profile.get("vnheilfolles").get().Candidates.length, is(41));
	}

	@Test
	public void testNumberOfCandidatesForSecondToken() {
		assertThat(profile.get("Waſſer").get().Candidates.length, is(6));
	}

	@Test
	public void testNumberOfCandidatesForSecondTokenIgnoresCase() {
		assertThat(profile.get("waſſer").get().Candidates.length, is(6));
	}

	@Test
	public void testProfileContainsTwoTypes() {
		assertThat(profile.size(), is(3));
	}

	@Test
	public void testCandidatesAreSortedInDescendingOrderOfVoteWeights() {
		double prev = Double.MAX_VALUE;
		for (Candidate candidate : profile.get("vnheilfolles").get().Candidates) {
			assertThat(candidate.Weight <= prev, is(true));
			prev = candidate.Weight;
		}
	}

	@Test
	public void testToJSON() throws Exception {
		final String json = profile.toJSON();
		final Profile other = Profile.fromJSON(json);
		profile = other;
		testNotContainsToken();
		testNumberOfCandidatesForFirstToken();
		testNumberOfCandidatesForFirstTokenIgnoresCase();
		testNumberOfCandidatesForSecondToken();
		testNumberOfCandidatesForSecondTokenIgnoresCase();
		testCandidatesAreSortedInDescendingOrderOfVoteWeights();
	}

	@Test
	public void testJSONFromProfiler() throws Exception {
		final Profile profile = Profile.read("src/test/resources/profile-test2.json");
		for (String test: new String[]{"dicke", "marmor", "bodens"}) {
			assertThat(profile.get(test).isPresent(), is(true));
			assertThat(profile.get(test).get().OCR, is(test));
			assertThat(profile.get(test).get().Candidates.length, is(1));
			assertThat(profile.get(test).get().Candidates[0].Suggestion, is(test));
		}
	}
}
