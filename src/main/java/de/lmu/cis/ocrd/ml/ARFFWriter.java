package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.Feature;
import de.lmu.cis.ocrd.ml.features.GTFeature;
import de.lmu.cis.ocrd.ml.features.NamedStringSetFeature;

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

    private ARFFWriter addFeature(Feature feature) {
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
                String attribute = String.format("%s_%d\tREAL", feature.getName(), i+1);
                if (feature instanceof GTFeature) {
                    attribute = getAttributeOfGTFeature((GTFeature) feature);
                } else if (feature instanceof NamedStringSetFeature) {
                    attribute = getAttributeOfNamedStringSetFeature((NamedStringSetFeature) feature);
                }
                writer.printf("@ATTRIBUTE\t%s\n", attribute);
            }
        }
        writer.println("@DATA");
    }

    public void writeToken(Token token) {
        if (!debugToken) {
            return;
        }
        writer.printf("%% {\"token\": \"%s\", \"pageID\"p: %d, \"lineID\": %d}\n",
                token.toString().replaceAll("\"", "\\\""),
                token.getMasterOCR().getLine().getPageId(), token.getMasterOCR().getLine().getLineId());
    }

    public void writeFeatureVector(List<Object> features) {
        boolean first = true;
        for (Object val : features) {
            if (first) {
                writer.print(val.toString());
                first = false;
            } else {
                writer.printf(",%s", val.toString());
            }
        }
        writer.println();
    }

    private String getAttributeOfGTFeature(GTFeature feature) {
        return String.format("%s\t{%s,%s}", feature.getName(), Boolean.toString(true), Boolean.toString(false));

    }

    private String getAttributeOfNamedStringSetFeature(NamedStringSetFeature feature) {
        StringBuilder builder = new StringBuilder(feature.getName());
        builder.append("\t{");
        boolean first = true;
        for (String s : feature.getSet()) {
            if (!first) {
                builder.append(',');
            }
            builder.append(s);
            first = false;
        }
        return builder.append('}').toString();
    }
}
