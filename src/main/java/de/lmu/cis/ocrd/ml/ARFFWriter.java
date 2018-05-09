package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.Feature;

import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// ARFFWriter writes feature vectors into WEKAs ARFF (Attribute-Relation-File-Format).
// After all features have been added to this writer using `addFeature()`,
// make sure to write the header using `writeHeader()`, before writing any feature vectors.
public class ARFFWriter {
    private String relation;
    private PrintWriter writer;
    private ArrayList<Feature> features = new ArrayList<>();
    private boolean debugToken;

    public static ARFFWriter fromFeatureSet(FeatureSet fs) {
        ARFFWriter arff = new ARFFWriter();
        for (Feature f : fs) {
           arff.addFeature(f);
        }
        return arff;
    }

    public ARFFWriter withDebugToken(boolean debugToken) {
        this.debugToken = debugToken;
        return this;
    }

    public ARFFWriter withRelation(String relation) {
        this.relation = relation;
        return this;
    }

    public ARFFWriter withWriter(Writer writer) {
        this.writer = new PrintWriter(writer);
        return this;
    }

    public ARFFWriter addFeature(Feature feature) {
        this.features.add(feature);
        return this;
    }

    public void writeHeader(int n) {
        writer.printf("%% Created by de.lmu.cis.ocrd.ml.ARFFWriter at %s\n", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        writer.printf("@RELATION\t%s\n", relation);
        for (Feature feature : features) {
            for (int i = 0; i < n; i++) {
                if (!feature.handlesOCR(i, n)) {
                    continue;
                }
                if (i > 0) {
                    writer.printf("@ATTRIBUTE\t%s%d\tREAL\n", feature.getName(), i);
                } else {
                    writer.printf("@ATTRIBUTE\t%s\tREAL\n", feature.getName());
                }
            }
        }
        writer.println("@DATA");
    }

    public void writeToken(Token token) {
        if (!debugToken) {
            return;
        }
        writer.printf("%% %s\n", token.toString());
    }

    public void writeFeatureVector(List<Double> features) throws Exception {
        boolean first = true;
        for (Double d : features) {
            if (first) {
                writer.print(d.toString());
                first = false;
            } else {
                writer.printf(",%s", d.toString());
            }
        }
        writer.println();
    }
}
