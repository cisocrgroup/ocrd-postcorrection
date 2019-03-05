package de.lmu.cis.ocrd.cli.test;

import de.lmu.cis.ocrd.cli.ModelZIP;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ModelZIPTest {
    private ModelZIP model;
    private Path tmpdir;
    private String[] names;

    @Before
    public void init() throws Exception {
        tmpdir = Files.createTempDirectory("ocrd-cis-java");
        model = new ModelZIP();
        names = new String[]{
                "a", "b", "c",
                "d", "e", "f",
                "g", "h", "i",
                "j", "k",
        };
        Path out;
        int j = 0;
        for (int i = 0; i < 3; i++) {
            out = writeTmpFile(names[j++]);
            model.addDLEModel(out, i);
        }
        for (int i = 0; i < 3; i++) {
            out = writeTmpFile(names[j++]);
            model.addRRModel(out, i);
        }
        for (int i = 0; i < 3; i++) {
            out = writeTmpFile(names[j++]);
            model.addDMModel(out, i);
        }
        out = writeTmpFile(names[j++]);
        model.setDLEFeatureSet(out);
        out = writeTmpFile(names[j]);
        model.setRRFeatureSet(out);
        model.save(Paths.get(tmpdir.toString(), "model.zip"));
        model = ModelZIP.open(Paths.get(tmpdir.toString(), "model.zip"));
    }

    @After
    public void close() throws IOException {
        model.close();
        //noinspection ResultOfMethodCallIgnored
        tmpdir.toFile().delete();
    }

    private Path writeTmpFile(String x) throws IOException {
        final Path out = Paths.get(tmpdir.toString(), x);
        try (PrintWriter os = new PrintWriter(out.toString())) {
            os.print(x);
        }
        return out;
    }

    private String readStringAndClose(InputStream is) throws IOException {
        try (InputStream iis = is) {
            return IOUtils.toString(iis, Charset.forName("UTF-8"));
        }
    }

    @Test
    public void testReadDLEModel1() throws Exception {
        final String got = readStringAndClose(model.openDLEModel(0));
        assertThat(got, is(names[0]));
    }

    @Test
    public void testReadDLEModel2() throws Exception {
        final String got = readStringAndClose(model.openDLEModel(1));
        assertThat(got, is(names[1]));
    }

    @Test
    public void testReadDLEModel3() throws Exception {
        final String got = readStringAndClose(model.openDLEModel(2));
        assertThat(got, is(names[2]));
    }

    @Test
    public void testReadRRModel1() throws Exception {
        final String got = readStringAndClose(model.openRRModel(0));
        assertThat(got, is(names[3]));
    }

    @Test
    public void testReadRRModel2() throws Exception {
        final String got = readStringAndClose(model.openRRModel(1));
        assertThat(got, is(names[4]));
    }

    @Test
    public void testReadRRModel3() throws Exception {
        final String got = readStringAndClose(model.openRRModel(2));
        assertThat(got, is(names[5]));
    }

    @Test
    public void testReadDMModel1() throws Exception {
        final String got = readStringAndClose(model.openDMModel(0));
        assertThat(got, is(names[6]));
    }

    @Test
    public void testReadDMModel2() throws Exception {
        final String got = readStringAndClose(model.openDMModel(1));
        assertThat(got, is(names[7]));
    }

    @Test
    public void testReadDMModel3() throws Exception {
        final String got = readStringAndClose(model.openDMModel(2));
        assertThat(got, is(names[8]));
    }

    @Test
    public void testReadDLEFeatureSet() throws Exception {
        final String got = readStringAndClose(model.openDLEFeatureSet());
        assertThat(got, is(names[9]));
    }

    @Test
    public void testReadRRFeatureSet() throws Exception {
        final String got = readStringAndClose(model.openRRFeatureSet());
        assertThat(got, is(names[10]));
    }
}
