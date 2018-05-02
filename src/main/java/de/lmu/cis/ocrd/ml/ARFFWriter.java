package de.lmu.cis.ocrd.ml;

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

    public static ARFFWriter fromFeatureSet(FeatureSet fs) {
        ARFFWriter arff = new ARFFWriter();
        fs.each((Feature f)->{
            arff.addFeature(f);
        });
        return arff;
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

    public void writeHeader() {
        writer.printf("%% Created by de.lmu.cis.ocrd.ml.ARFFWriter at %s\n", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        writer.printf("@RELATION\t%s\n", relation);
        for (Feature feature : features) {
            writer.printf("@ATTRIBUTE\t%s\tREAL\n", feature.getName());
        }
        writer.println("@DATA");
    }

    public void writeFeatureVector(List<Double> features) throws Exception {
        if (this.features.size() != features.size()) {
           throw new Exception("sizes of features and feature vector do not match: " + this.features.size() + " != " + features.size());
        }
        boolean first = true;
        for (double d : features) {
            if (first) {
                writer.printf("%f", d);
                first = false;
            } else {
                writer.printf(",%f", d);
            }
        }
        writer.println();
    }
}
