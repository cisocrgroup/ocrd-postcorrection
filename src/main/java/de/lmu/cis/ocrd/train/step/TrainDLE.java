package de.lmu.cis.ocrd.train.step;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.pagexml.*;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.profile.ProfilerBuilder;
import org.pmw.tinylog.Logger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

// step: train dynamic lexicon extension.
public class TrainDLE extends Base {
	private ProfilerBuilder profilerBuilder;

	public TrainDLE(String logLevel, ModelDir mdir, TmpDir tdir,
			Config config) {
		super(true, logLevel, mdir, tdir, config);
		profilerBuilder = new LocalProfilerBuilder(config,
				getTmpDir().getDLEProfile());
	}

	public void setProfilerBuilder(ProfilerBuilder builder) {
		this.profilerBuilder = builder;
	}

	public void run() throws Exception {
		dleProfile();
		dlePrepare();
		dleTrain();
		rrPrepare();
	}

	private void dleProfile() throws Exception {
		Logger.info("DLE: profiling");
		Path profilerIn = getTmpDir().getDLEProfilerInput();
		getTmpDir().putProfilerInputFile(true, getConfig().trainingFiles,
				profilerIn);
		Profile profile =
				profilerBuilder.build().profile(new FileReader(profilerIn.toFile()));
		getLM().setProfile(profile);
		Logger.debug("DLE: profiling done");
	}

	//
	// Prepare the different arff files for the actual dle model training.
	//
	private void dlePrepare() throws Exception {
		getModelDir().putDLEFeatures(Paths.get(getConfig().dleFeatures));
		final FeatureSet fs = FeatureFactory.getDefault()
				.withArgumentFactory(getLM())
				.createFeatureSet(getFeatures(getModelDir().getDLEFeatures()))
				.add(new DynamicLexiconGTFeature());
		for (int i = 0; i < getLM().getNumberOfOtherOCRs() + 1; i++) {
			dlePrepare(fs, i);
		}
	}

	private void dlePrepare(FeatureSet fs, int i) throws Exception {
		Path tfile = getModelDir().getDLETraining(i + 1);
		Logger.info("DLE: preparing for {} OCR(s) to {}", i + 1,
				tfile.toString());
		try (ARFFWriter w = ARFFWriter.fromFeatureSet(fs).withWriter(
				new BufferedWriter(new FileWriter(tfile.toFile())))) {
			w.withDebugToken(true);
			w.withRelation("dle-train-" + (i + 1));
			w.writeHeader(i + 1);
			for (String file : getConfig().trainingFiles) {
				Page page = Page.open(Paths.get(file));
				dlePrepare(w, fs, i, page);
			}
		}
	}

	private void dlePrepare(ARFFWriter w, FeatureSet fs, int i, Page page)
			throws Exception {
		eachLongWord(page, (word, mOCR) -> {
			final OCRToken t = new OCRTokenImpl(word, true);
			Logger.debug("word({}): {} GT: {}", i + 1,
					word.getUnicodeNormalized().get(i), t.getGT().get());
			final FeatureSet.Vector values = fs.calculateFeatureVector(t,
					i + 1);
			Logger.debug(values);
			w.writeFeatureVector(values);
		});
	}

	//
	// Train the actual models.
	//
	private void dleTrain() throws Exception {
		for (int i = 0; i < getLM().getNumberOfOtherOCRs() + 1; i++) {
			Path tfile = getModelDir().getDLETraining(i + 1);
			Path mfile = getModelDir().getDLEModel(i + 1);
			Logger.info("DLE: training for {} OCR(s) from {} to {}", i,
					tfile.toString(), mfile.toString());
			train(tfile, mfile);
		}
	}

	//
	// Prepare the different arff files for the actual rr model training.
	//
	private void rrPrepare() throws Exception {
		getModelDir().putRRFeatures(Paths.get(getConfig().dleFeatures));
		final FeatureSet fs = FeatureFactory.getDefault()
				.withArgumentFactory(getLM())
				.createFeatureSet(getFeatures(getModelDir().getDLEFeatures()))
				.add(new DynamicLexiconGTFeature());
		for (int i = 0; i < getLM().getNumberOfOtherOCRs() + 1; i++) {
			rrPrepare(fs, i);
		}
	}

	private void rrPrepare(FeatureSet fs, int i) throws Exception {
		Path tfile = getModelDir().getRRTraining(i + 1);
		Logger.info("RR: preparing for {} OCR(s) to {}", i + 1,
				tfile.toString());
		try (ARFFWriter w = ARFFWriter.fromFeatureSet(fs).withWriter(
				new BufferedWriter(new FileWriter(tfile.toFile())))) {
			w.withDebugToken(true);
			w.withRelation("rr-train-" + (i + 1));
			w.writeHeader(i + 1);
			for (String file : getConfig().trainingFiles) {
				Page page = Page.open(Paths.get(file));
				rrPrepare(w, fs, i, page);
			}
		}
	}

	private void rrPrepare(ARFFWriter w, FeatureSet fs, int i, Page page)
			throws Exception {
		eachLongWord(page, (word, mOCR) -> {
			Optional<Candidates> cs = getLM().getProfile().get(mOCR);
			if (!cs.isPresent()) {
				return;
			}
			Logger.debug("word({}): {} GT: {}", i + 1,
					word.getUnicodeNormalized().get(i),
					word.getUnicodeNormalized().get(0));
			for (Candidate c : cs.get().Candidates) {
				final OCRToken t = new OCRTokenWithCandidateImpl(word, true, c);
				final FeatureSet.Vector values = fs.calculateFeatureVector(t,
						i + 1);
				Logger.debug(values);
				w.writeFeatureVector(values);
			}
		});
	}

	interface WordOperation {
		void apply(Word word, String mOCR) throws Exception;
	}

	private static void eachLongWord(Page page, WordOperation f)
			throws Exception {
		for (Line line : page.getLines()) {
			for (Word word : line.getWords()) {
				String mOCR = word.getUnicodeNormalized().get(1);
				if (mOCR.length() > 3) {
					f.apply(word, mOCR);
				}
			}
		}
	}

	private void train(Path tfile, Path mfile) throws Exception {
		Logger.debug("training {} from {}", mfile.toString(), tfile.toString());
		final ConverterUtils.DataSource ds = new ConverterUtils.DataSource(
				tfile.toString());
		final Instances train = ds.getDataSet();
		// gt is last class
		train.setClassIndex(train.numAttributes() - 1);
		final Instances structure = ds.getStructure();
		structure.setClassIndex(structure.numAttributes() - 1);
		final AbstractClassifier sl = new SimpleLogistic();
		sl.buildClassifier(train);
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(mfile.toFile()))) {
			out.writeObject(sl);
			out.flush();
		}
	}

}
