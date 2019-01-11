package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.LogisticClassifier;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.FeatureSet;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.ml.features.ReRankingGTFeature;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.OCRTokenWithCandidateImpl;
import de.lmu.cis.ocrd.profile.Candidate;
import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class EvaluateRRDMCommand extends AbstractMLCommand {

	private FeatureSet rrFS, dmFS;
	private LM lm;
	private List<Double> rrConfidences;
	private boolean debug;

	@Override
	public String getName() {
		return "evaluate-rrdm";
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		setParameter(config);
		debug = "debug".equals(config.getLogLevel().toLowerCase());
		lm = new LM(true, Paths.get(getParameter().trigrams));
		rrFS = FeatureFactory
				.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getFeatures(getParameter().rrTraining.features))
				.add(new ReRankingGTFeature());
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
				final List<Candidate> cs = token.getAllProfilerCandidates();
				Logger.debug("adding {} candidates (rr)", cs.size());
				cs.forEach((c)->{
					w.writeToken(new OCRTokenWithCandidateImpl(token, c), i+1);
				});
			}
		}
		evaluate(getParameter().rrTraining.evaluation,
				getParameter().rrTraining.model,
				getParameter().rrTraining.result, i);
	}

	private void evaluateDM(List<OCRToken> tokens, int i) throws Exception {
		Logger.debug("evaluateDM({})", i);
		final Path rrModel = tagPath(getParameter().rrTraining.model, i+1);
		final Path rrTrain = tagPath(getParameter().rrTraining.evaluation, i+1);
		final LogisticClassifier c = LogisticClassifier.load(rrModel);
		final Instances instances =
				new ConverterUtils.DataSource(rrTrain.toString()).getDataSet();
		instances.setClassIndex(instances.numAttributes() - 1);
		Iterator<Instance> is = instances.iterator();
		rrConfidences = new ArrayList<>(getParameter().maxCandidates);
		for (int j = 0; j < getParameter().maxCandidates; j++) {
			rrConfidences.add(0.0);
		}
		dmFS = makeDMFeatureSet(rrConfidences);
		try (ARFFWriter w = ARFFWriter
				.fromFeatureSet(dmFS)
				.withRelation("evaluate-dm")
				.withDebugToken(debug)
				.withWriter(openTagged(getParameter().dmTraining.evaluation, i+1))
				.writeHeader(i+1)) {
			for (OCRToken token: tokens) {
				Optional<FeatureSet.Vector> values =
						calculateDMFeatureVector(
								token, dmFS, rrConfidences, c, is, i);
				if (!values.isPresent()) {
					continue;
				}
				w.writeFeatureVector(values.get());
			}
		}
		evaluate(getParameter().dmTraining.evaluation,
				getParameter().dmTraining.model,
				getParameter().dmTraining.result, i);
	}

	private void evaluate(String eval, String model, String res, int i) throws Exception {
		final Path evalPath = tagPath(eval, i+1);
		final Path modelPath = tagPath(model, i+1);
		final Path resultPath = tagPath(res, i+1);
		Logger.debug("evaluating {} ({}) {}", evalPath, modelPath, resultPath);
		final LogisticClassifier c = LogisticClassifier.load(modelPath);
		final String title = String.format("\nResults (%d)" +
						":\n=============\n"
					, i + 1);
		final String data = c.evaluate(title, evalPath);
		FileUtils.writeStringToFile(resultPath.toFile(), data,
				Charset.forName("UTF-8"));
	}

	private Writer openTagged(String path, int i) throws Exception {
		final Path tmp = tagPath(path, i);
		return new BufferedWriter(new FileWriter(tmp.toFile()));
	}
}
