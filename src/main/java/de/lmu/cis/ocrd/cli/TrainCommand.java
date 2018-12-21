package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.LM;
import de.lmu.cis.ocrd.ml.LogisticClassifier;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.OCRTokenImpl;
import de.lmu.cis.ocrd.pagexml.OCRTokenWithCandidateImpl;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.profile.Candidate;
import org.pmw.tinylog.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TrainCommand extends AbstractMLCommand {

	private String[] ifgs; // input file groups
	private METS mets; // mets file
	private AbstractMLCommand.Parameter parameter;
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
		this.parameter = getParameter(config);
		this.ifgs = config.mustGetInputFileGroups();
		this.mets = METS.open(Paths.get(config.mustGetMETSFile()));
		this.debug = "DEBUG".equals(config.getLogLevel());
		this.lm = new LM(true, Paths.get(parameter.trigrams));
		this.dleFS = FeatureFactory
				.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getFeatures(parameter.dleTraining.features))
				.add(new DynamicLexiconGTFeature());
		this.rrFS = FeatureFactory
				.getDefault()
				.withArgumentFactory(lm)
				.createFeatureSet(getFeatures(parameter.rrTraining.features))
				.add(new ReRankingGTFeature());
		// DM needs to be created separately (see below)
		for (int i = 0; i < parameter.nOCR; i++) {
			// DLE
			final Path dleTrain = tagPath(parameter.dleTraining.training,
					i+1);
			final Path dleModel = tagPath(parameter.dleTraining.model, i+1);
			dlew = ARFFWriter
					.fromFeatureSet(dleFS)
					.withWriter(getWriter(dleTrain))
					.withDebugToken(debug)
					.withRelation("dle-train-" + (i+1))
					.writeHeader(i+1);
			// RR
			final Path rrTrain = tagPath(parameter.rrTraining.training, i+1);
			final Path rrModel = tagPath(parameter.rrTraining.model, i+1);
			rrw = ARFFWriter
					.fromFeatureSet(rrFS)
					.withWriter(getWriter(rrTrain))
					.withDebugToken(debug)
					.withRelation("rr-train-" + (i+1))
					.writeHeader(i+1);
			// DM
			dmFS = FeatureFactory
					.getDefault()
					.withArgumentFactory(lm)
					.createFeatureSet(getFeatures(parameter.dmTraining.features))
					.add(getDMConfidenceFeature(rrModel, rrFS))
					.add(new DecisionMakerGTFeature());
			final Path dmTrain = tagPath(parameter.dmTraining.training, i+1);
			final Path dmModel = tagPath(parameter.dmTraining.model, i+1);
			dmw = ARFFWriter
					.fromFeatureSet(dmFS)
					.withWriter(getWriter(rrTrain))
					.withDebugToken(debug)
					.withRelation("dm-train-" + (i+1))
					.writeHeader(i+1);
			for (String ifg : ifgs) {
				Logger.info("input file group: {}", ifg);
				final List<METS.File> files = mets.findFileGrpFiles(ifg);
				prepare(files, i, parameter.nOCR);
			}
			// Train models
			dlew.close();
			rrw.close();
			dmw.close();
			train(dleTrain, dleModel);
			train(rrTrain, rrModel);
			train(dmTrain, dmModel);
		}
	}

	private void prepare(List<METS.File> files, int i, int n) throws Exception {
		Logger.info("prepare({}, {})", i, n);
		lm.setFiles(files);
		prepareDLE(files, i, n);
		prepareRR(files, i, n);
		prepareDM(files, i, n);
	}

	private void prepareDLE(List<METS.File> files, int i, int n) throws Exception {
		Logger.info("prepareDLE({}, {})", i, n);
		for (METS.File file : files) {
			try (InputStream is = file.open()) {
				prepareDLE(Page.parse(is), i, n);
			}
		}
	}

	private void prepareDLE(Page page, int i, int n) throws Exception {
		eachLongWord(page, (word, mOCR)-> {
			final OCRToken t = new OCRTokenImpl(word, n);
			Logger.debug("prepareDLE: adding {} (GT: {})",
					t.getMasterOCR().toString(),
					t.getGT().isPresent() ? t.getGT().get() : "-- missing --");
			final FeatureSet.Vector values = dleFS.calculateFeatureVector(t, i+1);
			Logger.debug(values);
			dlew.writeFeatureVector(values);
		});
	}

	private void prepareRR(List<METS.File> files, int i, int n) throws Exception {
		Logger.info("prepareRR({}, {})", i, n);
		for (METS.File file : files) {
			try (InputStream is = file.open()) {
				prepareRR(Page.parse(is), i, n);
			}
		}
	}

	private void prepareRR(Page page, int i, int n) throws Exception {
		eachLongWord(page, (word, mOCR)->{
			OCRTokenImpl t = new OCRTokenImpl(word, n);
			for (Candidate c : t.getAllProfilerCandidates()) {
				OCRTokenWithCandidateImpl tc =
						new OCRTokenWithCandidateImpl(word, n, c);
				Logger.debug("prepareRR: adding {} (Candidate: {}, GT: {})",
						tc.getMasterOCR().toString(),
						c.Suggestion,
						tc.getGT().toString());
				final FeatureSet.Vector values =
						rrFS.calculateFeatureVector(tc, i+1);
				Logger.debug(values);
				rrw.writeFeatureVector(values);
			}
		});
	}

	private void prepareDM(List<METS.File> files, int i, int n) throws Exception {
		Logger.info("prepareDM({}, {})", i, n);
		for (METS.File file : files) {
			try (InputStream is = file.open()) {
				prepareDM(Page.parse(is), i, n);
			}
		}
	}

	private void prepareDM(Page page, int i, int n) throws Exception {
		eachLongWord(page, (word, mOCR)->{
			OCRTokenImpl t = new OCRTokenImpl(word, n);
			for (Candidate c : t.getAllProfilerCandidates()) {
				OCRTokenWithCandidateImpl tc =
						new OCRTokenWithCandidateImpl(word, n, c);
				Logger.debug("prepareDM: adding {} (Candidate: {}, GT: {})",
						tc.getMasterOCR().toString(),
						c.Suggestion,
						tc.getGT().toString());
				final FeatureSet.Vector values =
						dmFS.calculateFeatureVector(tc, i+1);
				Logger.debug(values);
				dmw.writeFeatureVector(values);
			}
		});
	}

	private static void train(Path src, Path dest) throws Exception {
		Logger.debug("training {} from {}", dest.toString(), src.toString());
		LogisticClassifier classifier = LogisticClassifier.train(src);
		classifier.save(dest);
		// final ConverterUtils.DataSource ds = new ConverterUtils.DataSource(
		// 		src.toString());
		// final Instances train = ds.getDataSet();
		// // gt is last class
		// train.setClassIndex(train.numAttributes() - 1);
		// final Instances structure = ds.getStructure();
		// structure.setClassIndex(structure.numAttributes() - 1);
		// final AbstractClassifier sl = new SimpleLogistic();
		// sl.buildClassifier(train);
		// try (ObjectOutputStream out = new ObjectOutputStream(
		// 		new FileOutputStream(dest.toFile()))) {
		// 	out.writeObject(sl);
		// 	out.flush();
		// }
	}

	private static Writer getWriter(Path path) throws Exception {
		return new BufferedWriter(new FileWriter(path.toFile()));
	}
}
