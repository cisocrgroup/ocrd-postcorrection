package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.Feature;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.Token;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.StringWriter;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ARFFWriterTest {
    private class MockFeature implements Feature {
        private final String name;
        private final double n;
        public MockFeature(String name, double n) {
            this.name = name;
            this.n = n;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public double calculate(Token token) {
            return token.getMasterOCR().length() * n;
        }
    }

    private Instances is;

    @Before
    public void init() throws Exception {
        FeatureSet fs = new FeatureSet()
        .add(new MockFeature("a", 1))
        .add(new MockFeature("b", 2));
        StringWriter str = new StringWriter();
        ARFFWriter arff = ARFFWriter.fromFeatureSet(fs).withRelation("test").withWriter(str);
        arff.writeHeader();
        arff.writeFeatureVector(fs.calculateFeatureVector(new Token("a")));
        arff.writeFeatureVector(fs.calculateFeatureVector(new Token("aa")));
        arff.writeFeatureVector(fs.calculateFeatureVector(new Token("aaa")));
        // System.out.println(str.toString());
        this.is = new DataSource(IOUtils.toInputStream(str.toString(), Charset.defaultCharset())).getDataSet();
    }

    @Test
    public void testNumAttributes() {
        assertThat(is.numAttributes(), is(2));
    }

    @Test
    public void testSize() {
        assertThat(is.size(), is(3));
    }

    @Test
    public void testValues() {
        double[][] want = new double[][] {{1.0, 2.0, 3.0}, {2.0, 4.0, 6.0}};
        for (int i = 0; i < want.length; i++) {
            assertThat(is.attributeToDoubleArray(i).length, is(want[i].length));
            for (int j = 0; j < want[i].length; j++) {
                assertThat(is.attributeToDoubleArray(i)[j], is(want[i][j]));
            }
        }
    }
}
