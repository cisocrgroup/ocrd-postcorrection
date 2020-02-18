package de.lmu.cis.ocrd.ml;

import de.lmu.cis.ocrd.ml.features.*;

import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// ARFFWriter writes feature vectors into WEKAs ARFF (Attribute-Relation-File-Format).
// After all features have been added to this writer using `addFeature()`,
// make sure to write the header using `writeHeader()`, before writing any feature vectors.
public class ARFFWriter implements AutoCloseable {
	private String relation;
	private PrintWriter writer;
	private ArrayList<Feature> features = new ArrayList<>();
	private de.lmu.cis.ocrd.ml.features.FeatureSet fs;
	private boolean debugToken;

	private ARFFWriter(de.lmu.cis.ocrd.ml.features.FeatureSet fs) {
		this.fs = fs;
	}

	public static ARFFWriter fromFeatureSet(de.lmu.cis.ocrd.ml.features.FeatureSet fs) {
		ARFFWriter arff = new ARFFWriter(fs);
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

	String getRelation() {
		return relation;
	}

	private ARFFWriter addFeature(Feature feature) {
		this.features.add(feature);
		return this;
	}

	public ARFFWriter writeHeader(int n) {
		printf("%% Created by de.lmu.cis.ocrd.ml.ARFFWriter at %s\n",
				new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		printf("@RELATION\t%s\n", relation);
		for (Feature feature : features) {
			for (int i = 0; i < n; i++) {
				if (!feature.handlesOCR(i, n)) {
					continue;
				}
				final String attribute = String.format("%s_%d\t%s", feature.getName(), i+1, feature.getClasses());
				printf("@ATTRIBUTE\t%s\n", attribute);
			}
		}
		println("@DATA");
		return this;
	}

	private void debugToken(OCRToken token) {
		if (!debugToken) {
			return;
		}
		printf("%% %s\n", token.toString());
	}

	public void writeTokenWithFeatureSet(OCRToken token, FeatureSet fs, int n) {
		debugToken(token);
		writeFeatureVector(fs.calculateFeatureVector(token, n));
	}

	public void writeToken(OCRToken token, int n) {
		debugToken(token);
		writeFeatureVector(fs.calculateFeatureVector(token, n));
	}

	private void writeFeatureVector(de.lmu.cis.ocrd.ml.features.FeatureSet.Vector features) {
		features.writeCSVLine(writer);
	}

	private void printf(String fmt, Object...args) {
		writer.printf(fmt, args);
	}

	private void println(String str) {
		writer.println(str);
	}

	@Override
	public void close() {
		writer.flush();
		writer.close();
	}
}
