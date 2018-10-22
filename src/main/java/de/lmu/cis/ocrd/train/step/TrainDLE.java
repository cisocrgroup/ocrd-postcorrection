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
public class TrainDLE {
	private final LM lm;
	private final String featuresPath, dir;
	private final List<String> files;

	public TrainDLE(String[] args) throws Exception {
		this(args[0], args[1], args[2], args[3], Arrays.stream(args).skip(4).collect(Collectors.toList()));
	}

	public TrainDLE(String features, String profile, String trigrams, String dir, List<String> files) {
		this.featuresPath = features;
		this.dir = dir;
		this.files = files;
		this.lm = new LM(true, profile, trigrams, files);
	}

	public void run() throws Exception {
		JsonObject[] os;
		try (InputStream is = new FileInputStream(Paths.get(featuresPath).toFile())) {
			final String json = IOUtils.toString(is, Charset.forName("UTF-8"));
			os = new Gson().fromJson(json, JsonObject[].class);
		}
		final FeatureSet fs = FeatureFactory.getDefault().withArgumentFactory(lm).createFeatureSet(os)
				.add(new DynamicLexiconGTFeature());
		for (int i = 0; i < lm.getNumberOfOtherOCRs() + 1; i++) {
			Path tfile = Paths.get(dir, "dle_" + (i + 1) + ".arff");
			try (ARFFWriter w = ARFFWriter.fromFeatureSet(fs)
					.withWriter(new BufferedWriter(new FileWriter(tfile.toFile())))) {
				w.writeHeader(i + 1);
				for (String file : files) {
					Page page = Page.open(Paths.get(file));
					for (Line line : page.getLines()) {
						for (Word word : line.getWords()) {

						}
					}
				}
			}
		}

	}

	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			throw new Exception("Usage: features profile trigrams dir files...");
		}
		new TrainDLE(args).run();
	}
}
