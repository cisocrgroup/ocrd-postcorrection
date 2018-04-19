import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import de.lmu.cis.ocrd.profile.Profile;

public class ReadProfileFromJSONTest {
	private static final String resource = "src/test/resources/profile-test.json";
	private Profile profile;

	@Before
	public void init() throws IOException {
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
		assertNull(profile.get(null));
	}

	@Test
	public void testGetNotFoundReturnsNull() {
		assertNull(profile.get("null"));
	}

	@Test
	public void testNotContainsToken() {
		assertThat(profile.containsKey("null"), is(false));
	}

	@Test
	public void testNumberOfCandidatesForFirstToken() {
		assertThat(profile.get("Vnheilfolles").Candidates.length, is(41));
	}

	@Test
	public void testNumberOfCandidatesForSecondToken() {
		assertThat(profile.get("Waſſer").Candidates.length, is(6));
	}

	@Test
	public void testProfileContainsTwoTypes() {
		assertThat(profile.size(), is(2));
	}
}
