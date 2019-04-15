package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.FeatureSet;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import org.pmw.tinylog.Logger;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

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
		debug = "debug".equalsIgnoreCase(config.getLogLevel());
		fs = FeatureFactory
				.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getFeatures(getParameter().dleTraining.features), getFeatureClassFilter())
				.add(new DynamicLexiconGTFeature());
		for (int i = 0; i < getParameter().nOCR; i++) {
            try (ARFFWriter w = ARFFWriter
                    .fromFeatureSet(fs)
                    .withRelation("evaluate-dle")
                    .withDebugToken(debug)
                    .withWriter(openTagged(getParameter().dleTraining.evaluation, i+1))
                    .writeHeader(i+1)) {
                for (String ifg : config.mustGetInputFileGroups()) {
                    Logger.debug("input file group: {}", ifg);
                    List<OCRToken> tokens = readTokens(mets, ifg, new NoAdditionalLexicon());
                    lm.setTokens(tokens);
                    writeTokens(w, tokens, i);
                }
            }
            evaluate(i);
		}
		for (int i = 0; i < getParameter().nOCR; i++) {
		    for (String ifg : config.mustGetInputFileGroups()) {
                Logger.debug("input file group: {}", ifg);
                List<OCRToken> tokens = readTokens(mets, ifg, new NoAdditionalLexicon());
                lm.setTokens(tokens);
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
				if (token.isLexiconEntry()) {
					Logger.debug("skipping lexicon entry: {}", token.toString());
					continue;
				}
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

	private void writeTokens(ARFFWriter w, List<OCRToken> tokens, int i) {
		Logger.debug("evaluate({})", i);
        for (OCRToken token: tokens) {
            w.writeToken(token, i+1);
        }
	}

	private void evaluate(int i) throws Exception {
		final Path evalPath = tagPath(getParameter().dleTraining.evaluation, i + 1);
		final Path res = tagPath(getParameter().dleTraining.result, i + 1);
		final Path model = tagPath(getParameter().dleTraining.model, i+1);

		final LogisticClassifier c = LogisticClassifier.load(model);
		final Instances instances =
				new ConverterUtils.DataSource(evalPath.toString()).getDataSet();
		instances.setClassIndex(instances.numAttributes()-1);
		try (Writer w = new OutputStreamWriter(
				new FileOutputStream(res.toFile()), Charset.forName("UTF-8"))) {
			new DLEEvaluator(w, c, instances, i).evaluate();
		}
	}

	private Writer openTagged(String path, int i) throws Exception {
		final Path tmp = tagPath(path, i);
		return new BufferedWriter(new FileWriter(tmp.toFile()));
	}
}
