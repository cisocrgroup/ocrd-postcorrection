package de.lmu.cis.ocrd.ml.test.features.test;

import de.lmu.cis.ocrd.ml.BaseOCRToken;
import de.lmu.cis.ocrd.ml.CandidateOCRToken;
import de.lmu.cis.ocrd.ml.features.CandidateOCRPatternConfidenceFeature;
import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.test.MockBaseOCRToken;
import de.lmu.cis.ocrd.ml.test.MockOCRWord;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Profile;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CandidateOCRPatternConfidenceFeatureTest {
    private BaseOCRToken baseOCRToken;
    private List<Candidate> candidates;
    private Feature feature;

    @Before
    public void init() throws Exception {
        try (Reader r = new InputStreamReader(new GZIPInputStream(new FileInputStream(Paths.get("src/test/resources/workspace/profile.json.gz").toFile())))) {
            candidates = Profile.read(r).get("onoga").orElseThrow(Exception::new).Candidates;
        }
        baseOCRToken = new MockBaseOCRToken(1).addWord(new MockOCRWord().setWordNormalized("onoga").setCharConfidences(new double[]{.1,.1,.9,.9,.9}));
        feature = new CandidateOCRPatternConfidenceFeature("test");
    }

    @Test
    public void testHandlesMasterOCR() {
        assertThat(feature.handlesOCR(0, 2), is(true));
    }

    @Test
    public void testDoesNotHandleSlaveOCR() {
        assertThat(feature.handlesOCR(1, 2), is(false));
    }

    @Test
    public void testSubstitutionConfidence() {
        final Candidate candidate = candidates.get(2); // onoga -- j:on -> joga
        final Object res = feature.calculate(new CandidateOCRToken(baseOCRToken, candidate), 0, 2);
        assertThat(res, is(.1*.1));
    }

    @Test
    public void testInsertionConfidence() {
        final Candidate candidate = candidates.get(candidates.size() - 3); // onoga -- m:"":0,m:"":6 -> monogam
        final Object res = feature.calculate(new CandidateOCRToken(baseOCRToken, candidate), 0, 2);
        assertThat(res, is(.1*.9));
    }

    @Test
    public void testInsertionInMiddleConfidence() {
        final Candidate candidate = candidates.get(candidates.size() - 19); // onoga -- l:on:0,n:"":2 -> longa
        final Object res = feature.calculate(new CandidateOCRToken(baseOCRToken, candidate), 0, 2);
        assertThat(res, is(.1*.1*((.9+.9)/2)));
    }
}
