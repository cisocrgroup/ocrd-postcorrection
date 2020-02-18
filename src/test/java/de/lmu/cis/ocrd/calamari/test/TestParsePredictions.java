package de.lmu.cis.ocrd.calamari.test;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.calamari.Predictions;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestParsePredictions {
    private Predictions predictions;
    private static final Path json = Paths.get("src/test/resources/calamari/06205.json");

    @Before
    public void init() throws IOException {
        try (Reader r = new FileReader(json.toFile())) {
            predictions = new Gson().fromJson(r, Predictions.class);
        }
    }

    @Test
    public void testNumberOfPredictions() {
        assertThat(predictions.getPredictions().size(), is(5));
    }

    @Test
    public void testVotedSentence() {
        assertThat(predictions.getPredictions().get(0).getSentence(), is("und Unterdruͤckung durch Fremde, Unempfindlichkeit"));
    }

    @Test
    public void testIsVotedResult() {
        assertThat(predictions.getPredictions().get(0).isVotedResult(), is(true));
    }

    @Test
    public void testGetId() {
        assertThat(predictions.getPredictions().get(0).getId(), is("voted"));
    }

    @Test
    public void testGetLinePath() {
        assertThat(predictions.getPredictions().get(0).getLinePath(), is(""));
    }

    @Test
    public void testGetAvgCharProbability() {
        assertThat(predictions.getPredictions().get(0).getAvgCharProbability(), is(.9999793171882629));
    }

    @Test
    public void testGetPositionsNotEmpty() {
        assertThat(predictions.getPredictions().get(0).getPositions().isEmpty(), is(false));
    }

    @Test
    public void testPositionsGetLocalStart() {
        assertThat(predictions.getPredictions().get(0).getPositions().get(0).getLocalStart(), is(0));
    }

    @Test
    public void testPositionsGetLocalEnd() {
        assertThat(predictions.getPredictions().get(0).getPositions().get(0).getLocalEnd(), is(0));
    }

    @Test
    public void testPositionsGetGlobalStart() {
        assertThat(predictions.getPredictions().get(0).getPositions().get(0).getGlobalStart(), is(5));
    }

    @Test
    public void testPositionsGetGlobalEnd() {
        assertThat(predictions.getPredictions().get(0).getPositions().get(0).getGlobalEnd(), is(21));
    }

    @Test
    public void testPositionsGetCharsNotEmpty() {
        assertThat(predictions.getPredictions().get(0).getPositions().get(0).getChars().isEmpty(), is(false));
    }

    @Test
    public void testCharsGetLabel() {
        assertThat(predictions.getPredictions().get(0).getPositions().get(0).getChars().get(0).getLabel(), is(0));
    }

    @Test
    public void testCharsGetChar() {
        assertThat(predictions.getPredictions().get(0).getPositions().get(0).getChars().get(0).getChar(), is("u"));
    }

    @Test
    public void testCharsGetProbability() {
        assertThat(predictions.getPredictions().get(0).getPositions().get(0).getChars().get(0).getProbability(), is(.9999248385429382));
    }
}
