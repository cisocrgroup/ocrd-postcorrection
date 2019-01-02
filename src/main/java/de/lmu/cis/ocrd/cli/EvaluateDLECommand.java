package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.LogisticClassifier;
import de.lmu.cis.ocrd.ml.Prediction;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.FeatureSet;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.OCRTokenImpl;
import de.lmu.cis.ocrd.pagexml.Page;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EvaluateDLECommand extends AbstractMLCommand {
	private AbstractMLCommand.Parameter parameter;
	private FeatureSet fs;
	private boolean debug;

	@Override
	public String getName() {
		return "evaluate-dle";
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		final METS mets = METS.open(Paths.get(config.mustGetMETSFile()));
		parameter = getParameter(config);
		final LM lm = new LM(true, Paths.get(parameter.trigrams));
		debug = "debug".equals(config.getLogLevel().toLowerCase());
		fs = FeatureFactory
				.getDefault()
				.createFeatureSet(getFeatures(parameter.dleTraining.features))
				.add(new DynamicLexiconGTFeature());
		for (String ifg : config.mustGetInputFileGroups()) {
			Logger.debug("input file group: {}", ifg);
			List<METS.File> files = mets.findFileGrpFiles(ifg);
			lm.setFiles(files);
			for (int i = 0; i < parameter.nOCR; i++) {
				evaluate(files, i, parameter.nOCR);
				writeDLE(files, i, parameter.nOCR);
			}
		}
	}

	private void writeDLE(List<METS.File> files, int i, int n) throws Exception {
		Logger.debug("writeDLE({}, {})", i, n);
		final Path dlePath = tagPath(parameter.dleTraining.dynamicLexicon, i + 1);
		final LogisticClassifier c =
				LogisticClassifier.load(tagPath(parameter.dleTraining.model,
						i+1));
		try (Writer out = new OutputStreamWriter(
				new FileOutputStream(dlePath.toFile()),
				     Charset.forName("UTF-8"))) {
			for (METS.File file : files) {
				try (InputStream is = file.open()) {
					writeDLE(c, Page.parse(is), i, n, out);
				}
			}
			out.flush();
		}
	}

	private void writeDLE(LogisticClassifier c, Page page, int i, int n,
	                      Writer out) throws Exception {
		eachLongWord(page, (word, mOCR)->{
			final OCRToken t = new OCRTokenImpl(word, n);
			FeatureSet.Vector values = fs.calculateFeatureVector(t, i+1);
			final Prediction p = c.predict(values);
			if (p.getPrediction()) {
				out.write(mOCR);
				out.write('\n');
			}
		});
	}

	private void evaluate(List<METS.File> files, int i, int n) throws Exception {
		Logger.debug("evaluate({}, {})", i, n);
		try (ARFFWriter w = ARFFWriter
				.fromFeatureSet(fs)
				.withRelation("evaluate-dle")
				.withDebugToken(debug)
				.withWriter(openTagged(parameter.dleTraining.evaluation, i+1))
				.writeHeader(i+1)) {
			for (METS.File file : files) {
				try (InputStream is = file.open()) {
					prepare(Page.parse(is), w, i, n);
				}
			}
		}
		evaluate(i);
	}

	private void evaluate(int i) throws Exception {
		final Path evalPath = tagPath(parameter.dleTraining.evaluation, i + 1);
		final LogisticClassifier c =
				LogisticClassifier.load(tagPath(parameter.dleTraining.model,
						i + 1));
		final String title = String.format("\nResults (%d):\n=============\n"
				, i + 1);
		final String data = c.evaluate(title, evalPath);
		println(data);
	}

	private void prepare(Page page, ARFFWriter w, int i, int n) throws Exception {
		eachLongWord(page, (word, mOCR)->{
			final OCRToken t = new OCRTokenImpl(word, n);
			Logger.debug("prepareDLE: adding {} (GT: {})",
					t.getMasterOCR().toString(),
					t.getGT().isPresent() ? t.getGT().get() : "-- missing --");
			w.writeToken(t, i+1);
		});
	}

	private Writer openTagged(String path, int i) throws Exception {
		final Path tmp = tagPath(path, i);
		return new BufferedWriter(new FileWriter(tmp.toFile()));
	}
}
