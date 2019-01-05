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
import org.pmw.tinylog.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EvaluateDLECommand extends AbstractMLCommand {
	private FeatureSet fs;
	private boolean debug;

	@Override
	public String getName() {
		return "evaluate-dle";
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		final METS mets = METS.open(Paths.get(config.mustGetMETSFile()));
		setParameter(config);
		final LM lm = new LM(true, Paths.get(getParameter().trigrams));
		debug = "debug".equals(config.getLogLevel().toLowerCase());
		fs = FeatureFactory
				.getDefault()
				.createFeatureSet(getFeatures(getParameter().dleTraining.features))
				.add(new DynamicLexiconGTFeature());
		for (String ifg : config.mustGetInputFileGroups()) {
			Logger.debug("input file group: {}", ifg);
			List<OCRToken> tokens = readTokens(mets.findFileGrpFiles(ifg));
			lm.setTokens(tokens);
			for (int i = 0; i < getParameter().nOCR; i++) {
				evaluate(tokens, i);
				writeDLE(tokens, i);
			}
		}
	}

	private void writeDLE(List<OCRToken> tokens, int i) throws Exception {
		Logger.debug("writeDLE({})", i);
		final Path dlePath = tagPath(getParameter().dleTraining.dynamicLexicon, i + 1);
		final LogisticClassifier c =
				LogisticClassifier.load(tagPath(getParameter().dleTraining.model,
						i+1));
		try (Writer out = new OutputStreamWriter(
				new FileOutputStream(dlePath.toFile()),
				     Charset.forName("UTF-8"))) {
			for (OCRToken token : tokens) {
				FeatureSet.Vector values = fs.calculateFeatureVector(token, i + 1);
				final Prediction p = c.predict(values);
				if (p.getPrediction()) {
					out.write(token.getMasterOCR().getWord());
					out.write('\n');
				}
			}
			out.flush();
		}
	}

	private void evaluate(List<OCRToken> tokens, int i) throws Exception {
		Logger.debug("evaluate({})", i);
		try (ARFFWriter w = ARFFWriter
				.fromFeatureSet(fs)
				.withRelation("evaluate-dle")
				.withDebugToken(debug)
				.withWriter(openTagged(getParameter().dleTraining.evaluation, i+1))
				.writeHeader(i+1)) {
			for (OCRToken token: tokens) {
				w.writeToken(token, i+1);
			}
		}
		evaluate(i);
	}

	private void evaluate(int i) throws Exception {
		final Path evalPath = tagPath(getParameter().dleTraining.evaluation, i + 1);
		final LogisticClassifier c =
				LogisticClassifier.load(tagPath(getParameter().dleTraining.model,
						i + 1));
		final String title = String.format("\nResults (%d):\n=============\n"
				, i + 1);
		final String data = c.evaluate(title, evalPath);
		println(data);
	}

	private Writer openTagged(String path, int i) throws Exception {
		final Path tmp = tagPath(path, i);
		return new BufferedWriter(new FileWriter(tmp.toFile()));
	}
}
