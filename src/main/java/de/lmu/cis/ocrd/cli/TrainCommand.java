package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.LogisticClassifier;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.OCRTokenWithCandidateImpl;
import de.lmu.cis.ocrd.profile.Candidate;
import org.pmw.tinylog.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TrainCommand extends AbstractMLCommand {

	private String[] ifgs; // input file groups
	private METS mets; // mets file
	private LM lm;
	private FeatureSet dleFS, rrFS, dmFS;
	private ARFFWriter dlew, rrw, dmw;
	private boolean debug;

	@Override
	public String getName() {
		return "train";
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		setParameter(config);
		this.ifgs = config.mustGetInputFileGroups();
		this.mets = METS.open(Paths.get(config.mustGetMETSFile()));
		this.debug = "DEBUG".equals(config.getLogLevel());
		this.lm = new LM(true, Paths.get(getParameter().trigrams));
		this.dleFS = FeatureFactory
				.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getFeatures(getParameter().dleTraining.features))
				.add(new DynamicLexiconGTFeature());
		this.rrFS = FeatureFactory
				.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getFeatures(getParameter().rrTraining.features))
				.add(new ReRankingGTFeature());
		// DM needs to be created separately (see below)
		for (int i = 0; i < getParameter().nOCR; i++) {
			// DLE
			final Path dleTrain = tagPath(getParameter().dleTraining.training,
					i+1);
			final Path dleModel = tagPath(getParameter().dleTraining.model, i+1);
			dlew = ARFFWriter
					.fromFeatureSet(dleFS)
					.withWriter(getWriter(dleTrain))
					.withDebugToken(debug)
					.withRelation("dle-train-" + (i+1))
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
				Logger.info("input file group (dle, rr): {}", ifg);
				final List<OCRToken> tokens =
						readTokens(mets.findFileGrpFiles(ifg));
				prepareDLEAndRR(tokens, i);
			}
			// Train models
			dlew.close();
			rrw.close();
			train(dleTrain, dleModel);
			train(rrTrain, rrModel);
		}
		// dm must be trained after rr has been trained.
		trainDM();
	}

	private void prepareDLEAndRR(List<OCRToken> tokens, int i) {
		Logger.info("prepareDLEAndRR({})", i);
		lm.setTokens(tokens);
		prepareDLE(tokens, i);
		prepareRR(tokens, i);
	}

	private void prepareDLE(List<OCRToken> tokens, int i) {
		tokens.forEach((token)->{
			final FeatureSet.Vector values =
					dleFS.calculateFeatureVector(token, i+1);
			dlew.writeFeatureVector(values);
		});
	}

	private void prepareRR(List<OCRToken> tokens, int i) {
		tokens.forEach((token)->{
			final List<Candidate> cs = token.getAllProfilerCandidates();
			Logger.debug("adding {} candidates", cs.size());
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
				rrw.writeFeatureVector(values);
			});
		});
	}

	private void prepareDM(List<OCRToken> tokens, int i) {
		tokens.forEach((token)->{
			final List<Candidate> cs = token.getAllProfilerCandidates();
			Logger.debug("adding {} candidates", cs.size());
			cs.forEach((c)->{
				OCRTokenWithCandidateImpl tc =
						new OCRTokenWithCandidateImpl(token, c);
				// Logger.debug("prepareDM: adding {} (Candidate: {}, GT: {})",
				// 		tc.getMasterOCR().toString(),
				// 		c.Suggestion,
				// 		tc.getGT().toString());
				final FeatureSet.Vector values =
						dmFS.calculateFeatureVector(tc, i+1);
				// Logger.debug(values);
				dmw.writeFeatureVector(values);
			});
		});
	}

	private void trainDM() throws Exception {
		for (int i = 0; i < getParameter().nOCR; i++) {
			final Path dmTrain = tagPath(getParameter().dmTraining.training, i+1);
			final Path dmModel = tagPath(getParameter().dmTraining.model, i+1);
			final Path rrModel = tagPath(getParameter().rrTraining.model, i+1);
			dmFS = FeatureFactory
					.getDefault()
					.withArgumentFactory(lm)
					.createFeatureSet(getFeatures(getParameter().dmTraining.features))
					.add(getDMConfidenceFeature(rrModel, rrFS))
					.add(new DecisionMakerGTFeature());
			dmw = ARFFWriter
					.fromFeatureSet(dmFS)
					.withWriter(getWriter(dmTrain))
					.withDebugToken(debug)
					.withRelation("dm-train-" + (i+1))
					.writeHeader(i+1);
			for (String ifg : ifgs) {
				Logger.info("input file group (dm): {}", ifg);
				final List<OCRToken> tokens =
						readTokens(mets.findFileGrpFiles(ifg));
				prepareDM(tokens, i);
			}
			dmw.close();
			train(dmTrain, dmModel);
		}
	}

	private static void train(Path src, Path dest) throws Exception {
		Logger.debug("training {} from {}", dest.toString(), src.toString());
		LogisticClassifier classifier = LogisticClassifier.train(src);
		classifier.save(dest);
	}


	private static Writer getWriter(Path path) throws Exception {
		return new BufferedWriter(new FileWriter(path.toFile()));
	}
}
