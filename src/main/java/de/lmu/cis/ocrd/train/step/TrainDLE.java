package de.lmu.cis.ocrd.train.step;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.features.DynamicLexiconGTFeature;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.pagexml.Line;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Word;

// step: train dynamic lexicon extension.
public class TrainDLE extends Base {
	private final String featuresPath;

	public TrainDLE(String[] args) throws Exception {
		this(args[0], args[1], args[2], args[3], args[4],
				Arrays.stream(args).skip(5).collect(Collectors.toList()));
	}

	public TrainDLE(String logLevel, String features, String profile,
			String trigrams, String dir, List<String> files) {
		super(true, logLevel, profile, trigrams, dir, files);
		this.featuresPath = features;
	}

	public void run() throws Exception {
		JsonObject[] os;
		try (InputStream is = new FileInputStream(
				Paths.get(featuresPath).toFile())) {
			final String json = IOUtils.toString(is, Charset.forName("UTF-8"));
			os = new Gson().fromJson(json, JsonObject[].class);
		}
		final FeatureSet fs = FeatureFactory.getDefault()
				.withArgumentFactory(getLM()).createFeatureSet(os)
				.add(new DynamicLexiconGTFeature());
		for (int i = 0; i < getLM().getNumberOfOtherOCRs() + 1; i++) {
			Path tfile = getTrainFile(i + 1);
			try (ARFFWriter w = ARFFWriter.fromFeatureSet(fs).withWriter(
					new BufferedWriter(new FileWriter(tfile.toFile())))) {
				w.writeHeader(i + 1);
				for (Path file : getFiles()) {
					Page page = Page.open(file);
					for (Line line : page.getLines()) {
						for (Word word : line.getWords()) {
							Logger.info("word: {}", word.getUnicode().get(0));
							Logger.debug("word: {}", word.getUnicode().get(0));
						}
					}
				}
			}
		}
	}

	public Path getTrainFile(int n) {
		return Paths.get(getDir().toString(), "dle_" + n + ".arff");
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 5) {
			throw new Exception(
					"Usage: logLevel features profile trigrams dir files...");
		}
		new TrainDLE(args).run();
	}
}
