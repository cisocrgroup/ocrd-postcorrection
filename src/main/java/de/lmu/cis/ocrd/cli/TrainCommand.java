package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.LogisticClassifier;
import de.lmu.cis.ocrd.ml.ModelZIP;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.OCRTokenWithCandidateImpl;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.NoAdditionalLexicon;
import org.pmw.tinylog.Logger;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TrainCommand extends AbstractMLCommand {

	private String[] ifgs; // input file groups
	private METS mets; // mets file
	private LM lm;
	private FeatureSet leFS, rrFS, dmFS;
	private ARFFWriter lew, rrw, dmw;
	private List<Double> rrConfidences;
	private boolean debug;

	@Override
	public String getName() {
		return "train";
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		config.setCommand(this);
		setParameter(config);
		this.ifgs = config.mustGetInputFileGroups();
		this.mets = METS.open(Paths.get(config.mustGetMETSFile()));
		this.debug = "DEBUG".equals(config.getLogLevel());
		this.lm = new LM(true, Paths.get(getParameter().trigrams));
		this.leFS = FeatureFactory
				.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getFeatures(getParameter().leTraining.features), getFeatureClassFilter())
				.add(new DynamicLexiconGTFeature());
		this.rrFS = FeatureFactory
				.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getFeatures(getParameter().rrTraining.features), getFeatureClassFilter())
				.add(new ReRankingGTFeature());
		// DM needs to be created separately (see below)
		for (int i = 0; i < getParameter().nOCR; i++) {
			// LE
			final Path leTrain = tagPath(getParameter().leTraining.training, i+1);
			final Path leModel = tagPath(getParameter().leTraining.model, i+1);
			lew = ARFFWriter
					.fromFeatureSet(leFS)
					.withWriter(getWriter(leTrain))
					.withDebugToken(debug)
					.withRelation("le-train-" + (i+1))
					.writeHeader(i+1);
			// RR
			final Path rrTrain = tagPath(getParameter().rrTraining.training, i+1);
			final Path rrModel = tagPath(getParameter().rrTraining.model, i+1);
			rrw = ARFFWriter
					.fromFeatureSet(rrFS)
					.withWriter(getWriter(rrTrain))
					.withDebugToken(debug)
					.withRelation("rr-train-" + (i+1))
					.writeHeader(i+1);
			for (String ifg : ifgs) {
				Logger.info("input file group (le, rr): {}", ifg);
				final List<OCRToken> tokens = readTokens(mets, ifg, new NoAdditionalLexicon());
				prepareLEAndRR(tokens, i);
			}
			// Train models
			lew.close();
			rrw.close();
			train(leTrain, leModel);
			train(rrTrain, rrModel);
		}
		// dm must be trained after rr has been trained.
		trainDM();
		// write zipped model
		writeModelZIP();
	}

	private void prepareLEAndRR(List<OCRToken> tokens, int i) {
		Logger.info("prepareLEAndRR({})", i);
		lm.setTokens(tokens);
		prepareLE(tokens, i);
		prepareRR(tokens, i);
	}

	private void prepareLE(List<OCRToken> tokens, int i) {
		for (OCRToken token : tokens) {
			if (token.isLexiconEntry()) {
				Logger.debug("skipping lexicon entry: {}", token.toString());
				continue;
			}
			lew.writeToken(token, i+1);
		}
	}

	private void prepareRR(List<OCRToken> tokens, int i) {
		tokens.forEach((token)->{
			final List<Candidate> cs = token.getAllProfilerCandidates();
			Logger.debug("token: '{}': adding {} candidates", token.toString(), cs.size());
			cs.forEach((c)->{
				OCRTokenWithCandidateImpl tc =
						new OCRTokenWithCandidateImpl(token, c);
				// Logger.debug("prepareRR: adding {} (Candidate: {}, GT: {})",
				// 		tc.getMasterOCR().toString(),
				// 		c.Suggestion,
				// 		tc.getGT().toString());
				final FeatureSet.Vector values =
						rrFS.calculateFeatureVector(tc, i+1);
				// Logger.debug(values);
				rrw.writeToken(tc, i+1);
			});
		});
	}

	private void trainDM() throws Exception {
		rrConfidences = new ArrayList<>(getParameter().maxCandidates);
		for (int i = 0; i < getParameter().maxCandidates; i++) {
			rrConfidences.add(0.0);
		}
		for (int i = 0; i < getParameter().nOCR; i++) {
			final Path dmTrain = tagPath(getParameter().dmTraining.training, i+1);
			final Path dmModel = tagPath(getParameter().dmTraining.model, i+1);
			final Path rrModel = tagPath(getParameter().rrTraining.model, i+1);
			final Path rrTrain = tagPath(getParameter().rrTraining.training, i+1);
			final LogisticClassifier c = LogisticClassifier.load(rrModel);
			final Instances instances =
					new ConverterUtils.DataSource(rrTrain.toString()).getDataSet();
			instances.setClassIndex(instances.numAttributes() - 1);
			final Iterator<Instance> is = instances.iterator();

			dmFS = new FeatureSet()
					.add(new DMBestRankFeature("dm-best-rank", null))
					.add(new DMDifferenceToNextRankFeature("dm-difference-to-next", null))
					.add(new DMGTFeature("dm-gt", null));
			// arff writer only needs names and types of the features.
			// the feature set is recalculated for each file group
			dmw = ARFFWriter
					.fromFeatureSet(dmFS)
					.withWriter(getWriter(dmTrain))
					.withDebugToken(debug)
					.withRelation("dm-train-" + (i+1))
					.writeHeader(i+1);

			for (String ifg : ifgs) {
				final List<OCRToken> tokens = readTokens(mets, ifg, new NoAdditionalLexicon());
				final Map<OCRToken, List<Ranking>> rankings = calculateRankings(tokens, is, c);

				dmFS = new FeatureSet()
						.add(new DMBestRankFeature("dm-best-rank", rankings))
						.add(new DMDifferenceToNextRankFeature("dm-difference-to-next", rankings))
						.add(new DMGTFeature("dm-gt", rankings));
				Logger.info("input file group (dm): {}", ifg);
				for (OCRToken token: tokens) {
					if (!rankings.containsKey(token)) {
						continue;
					}
					if (DMGTFeature.isValidForTraining(token, rankings)) {
						dmw.writeTokenWithFeatureSet(token, dmFS, i + 1);
					}
				}
			}
			dmw.close();
			train(dmTrain, dmModel);
		}
	}

	private void writeModelZIP() throws Exception {
		final ModelZIP model = new ModelZIP();
		for (int i = 0; i < getParameter().nOCR; i++) {
			model.addLEModel(tagPath(getParameter().leTraining.model, i+1), i);
			model.addRRModel(tagPath(getParameter().rrTraining.model, i+1), i);
			model.addDMModel(tagPath(getParameter().dmTraining.model, i+1), i);
		}
		model.setLEFeatureSet(Paths.get(getParameter().leTraining.features));
		model.setRRFeatureSet(Paths.get(getParameter().rrTraining.features));
		model.save(Paths.get(getParameter().model));
	}


	private static void train(Path src, Path dest) throws Exception {
		Logger.debug("training {} from {}", dest.toString(), src.toString());
		LogisticClassifier classifier = LogisticClassifier.train(src);
		classifier.save(dest);
	}


	private static Writer getWriter(Path path) throws Exception {
		if (path.getParent().toFile().mkdirs()) {
			Logger.info("created directory {}", path.getParent().toString());
		}
		return new BufferedWriter(new FileWriter(path.toFile()));
	}
}
