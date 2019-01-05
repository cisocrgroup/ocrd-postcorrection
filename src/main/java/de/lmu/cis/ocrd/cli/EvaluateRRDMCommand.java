package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.LogisticClassifier;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.OCRTokenWithCandidateImpl;
import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EvaluateRRDMCommand extends AbstractMLCommand {

	private FeatureSet rrFS, dmFS;
	private boolean debug;

	@Override
	public String getName() {
		return "evaluate-rrdm";
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		setParameter(config);
		debug = "debug".equals(config.getLogLevel().toLowerCase());
		rrFS = FeatureFactory
				.getDefault()
				.createFeatureSet(getFeatures(getParameter().rrTraining.features))
				.add(new ReRankingGTFeature());
		dmFS = FeatureFactory
				.getDefault()
				.createFeatureSet(getFeatures(getParameter().dmTraining.features))
				.add(new DecisionMakerGTFeature());
		final LM lm = new LM(true, Paths.get(getParameter().trigrams));
		final METS mets = METS.open(Paths.get(config.mustGetMETSFile()));
		for (String ifg : config.mustGetInputFileGroups()) {
			Logger.debug("input file group: {}", ifg);
			final List<OCRToken> tokens =
					readTokens(mets.findFileGrpFiles(ifg));
			lm.setTokens(tokens);
			for (int i = 0; i < getParameter().nOCR; i++) {
				evaluateRR(tokens, i);
				evaluateDM(tokens, i);
			}
		}
	}

	private void evaluateRR(List<OCRToken> tokens, int i) throws Exception {
		Logger.debug("evaluateRR({})", i);
		try (ARFFWriter w = ARFFWriter
				.fromFeatureSet(rrFS)
		        .withRelation("evaluate-rr")
				.withDebugToken(debug)
				.withWriter(openTagged(getParameter().rrTraining.evaluation, i+1))
				.writeHeader(i+1)) {
			for (OCRToken token: tokens) {
				token.getAllProfilerCandidates().forEach((c)->{
					w.writeToken(new OCRTokenWithCandidateImpl(token, c), i+1);
				});
			}
			evaluate(getParameter().rrTraining.evaluation,
					getParameter().rrTraining.model,
					getParameter().rrTraining.result, i);
		}
	}

	private void evaluateDM(List<OCRToken> tokens, int i) throws Exception {
		Logger.debug("evaluateDM({})", i);
		try (ARFFWriter w = ARFFWriter
				.fromFeatureSet(rrFS)
				.withRelation("evaluate-dm")
				.withDebugToken(debug)
				.withWriter(openTagged(getParameter().dmTraining.evaluation, i+1))
				.writeHeader(i+1)) {
			for (OCRToken token: tokens) {
				token.getAllProfilerCandidates().forEach((c) -> {
					w.writeToken(new OCRTokenWithCandidateImpl(token, c), i + 1);
				});
			}
			evaluate(getParameter().dmTraining.evaluation,
					getParameter().dmTraining.model,
					getParameter().dmTraining.result, i);
		}
	}

	private void evaluate(String eval, String model, String res, int i) throws Exception {
		final Path evalPath = tagPath(eval, i+1);
		final LogisticClassifier c = LogisticClassifier.load(tagPath(model,
				i+1));
		final String title = String.format("\nResults (%d)" +
						":\n=============\n"
					, i + 1);
		final String data = c.evaluate(title, evalPath);
		FileUtils.writeStringToFile(new File(res), data,
				Charset.forName("UTF-8"));
	}

	private Writer openTagged(String path, int i) throws Exception {
		final Path tmp = tagPath(path, i);
		return new BufferedWriter(new FileWriter(tmp.toFile()));
	}
}
