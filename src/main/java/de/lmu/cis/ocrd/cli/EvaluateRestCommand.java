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

public class EvaluateRestCommand extends AbstractMLCommand {

	private AbstractMLCommand.Parameter parameter;
	private FeatureSet rrFS, dmFS;
	private boolean debug;

	@Override
	public String getName() {
		return "evaluate-rest";
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		parameter = getParameter(config);
		debug = "debug".equals(config.getLogLevel().toLowerCase());
		rrFS = FeatureFactory
				.getDefault()
				.createFeatureSet(getFeatures(parameter.rrTraining.features))
				.add(new ReRankingGTFeature());
		dmFS = FeatureFactory
				.getDefault()
				.createFeatureSet(getFeatures(parameter.dmTraining.features))
				.add(new DecisionMakerGTFeature());
		final LM lm = new LM(true, Paths.get(parameter.trigrams));
		final METS mets = METS.open(Paths.get(config.mustGetMETSFile()));
		for (String ifg : config.mustGetInputFileGroups()) {
			Logger.debug("input file group: {}", ifg);
			final List<METS.File> files = mets.findFileGrpFiles(ifg);
			lm.setFiles(files);
			for (int i = 0; i < parameter.nOCR; i++) {
				evaluateRR(files, i, parameter.nOCR);
				evaluateDM(files, i, parameter.nOCR);
			}
		}
	}

	private void evaluateRR(List<METS.File> files, int i, int n) throws Exception {
		Logger.debug("evaluateRR({}, {})", i, n);
		try (ARFFWriter w = ARFFWriter
				.fromFeatureSet(rrFS)
		        .withRelation("evaluate-rr")
				.withDebugToken(debug)
				.withWriter(openTagged(parameter.rrTraining.evaluation, i+1))
				.writeHeader(i+1)) {
			for (METS.File file : files) {
				try (InputStream is = file.open()) {
					prepare(Page.parse(is), rrFS, w, i, n);
				}
			}
			evaluate(parameter.rrTraining.evaluation,
					parameter.rrTraining.model, i);
		}
	}

	private void evaluateDM(List<METS.File> files, int i, int n) throws Exception {
		Logger.debug("evaluateDM({}, {})", i, n);
		try (ARFFWriter w = ARFFWriter
				.fromFeatureSet(rrFS)
				.withRelation("evaluate-dm")
				.withDebugToken(debug)
				.withWriter(openTagged(parameter.dmTraining.evaluation, i+1))
				.writeHeader(i+1)) {
			for (METS.File file : files) {
				try (InputStream is = file.open()) {
					prepare(Page.parse(is), dmFS, w, i, n);
				}
			}
			evaluate(parameter.dmTraining.evaluation,
					parameter.dmTraining.model, i);
		}
	}

	private void evaluate(String eval, String model, int i) throws Exception {
		final Path evalPath = tagPath(eval, i+1);
		final LogisticClassifier c = LogisticClassifier.load(tagPath(model,
				i+1));
		final String title = String.format("\nResults (%d)" +
						":\n=============\n"
					, i + 1);
		final String data = c.evaluate(title, evalPath);
		println(data);
	}

	private void prepare(Page page, FeatureSet fs, ARFFWriter w, int i,
	                      int n) throws Exception {
		eachLongWord(page, (word, mOCR)->{
			final OCRToken t = new OCRTokenImpl(word, n);
			Logger.debug("evaluate: adding candidates for {} (GT: {})",
					t.getMasterOCR().toString(),
					t.getGT().isPresent() ? t.getGT().get() : "-- missing --");
			for (Candidate c : t.getAllProfilerCandidates()) {
				OCRTokenWithCandidateImpl tc =
						new OCRTokenWithCandidateImpl(word, n, c);
				Logger.debug("evaluate: {} (Candidate: {}, GT: {}",
						tc.getMasterOCR().toString(),
						c.Suggestion,
						tc.getGT().toString());
				final FeatureSet.Vector values =
						fs.calculateFeatureVector(tc, i+1);
				Logger.debug(values);
				w.writeFeatureVector(values);
			}
		});
	}

	private Writer openTagged(String path, int i) throws Exception {
		final Path tmp = tagPath(path, i);
		return new BufferedWriter(new FileWriter(tmp.toFile()));
	}
}
