package de.lmu.cis.ocrd.ml.test.features.test;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.features.LevenshteinDistanceFeature;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LevenshteinDistanceFeatureTest {
    private LevenshteinDistanceFeature f;

    @Before
    public void init() {
        final JsonObject o = new JsonObject();
        o.addProperty("name", "LevenshteinDistanceFeature");
        o.addProperty("maxThreshold", 3);
        this.f = new LevenshteinDistanceFeature(o, null);
    }

    @Test
    public void testName() {

        assertThat(f.getName(), is("LevenshteinDistanceFeature"));
    }

    @Test
    public void testThreshold() {
        assertThat(f.getThreshold(), is(3));
    }

    @Test
    public void testNotHandlesMasterOCR() {
        assertThat(f.handlesOCR(0, 3), is(false));
    }

    @Test
    public void testNotHandlesFirstSlaveOCR() {
        assertThat(f.handlesOCR(1, 3), is(true));
    }

    @Test
    public void testNotHandlesSecondSlaveOCR() {
        assertThat(f.handlesOCR(2, 3), is(true));
    }
}
