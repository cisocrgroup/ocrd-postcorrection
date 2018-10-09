package de.lmu.cis.ocrd.train;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

// Data class that is used to map the
// JSON data.
public class Configuration {
	private Profiler profiler;
	private LanguageModel languageModel;
	private DynamicLexiconTraining dynamicLexiconTraining;

	public static Configuration getDefault() {
		final Configuration c = new Configuration();
		c.profiler = new Profiler();
		c.profiler.executable = "/apps/profiler";
		c.profiler.languageDirectory = "/data/ProfilerLanguages";
		c.profiler.language = "german";
		c.profiler.arguments = new String[0];
		c.languageModel = new LanguageModel();
		c.languageModel.characterTrigrams = "/data/languageModel/characterTrigrams.csv";
		c.dynamicLexiconTraining = new DynamicLexiconTraining();
		c.dynamicLexiconTraining.features = new JsonObject[0];
		c.dynamicLexiconTraining.debugTrainingTokens = true;
		c.dynamicLexiconTraining.copyTrainingFiles = true;
		c.dynamicLexiconTraining.testEvaluationFraction = 10;
		return c;
	}

	public static Configuration fromJSON(Path path) throws IOException {
		try (InputStream in = new FileInputStream(path.toFile())) {
			final String json = org.apache.commons.io.IOUtils.toString(in, Charset.forName("UTF-8"));
			return fromJSON(json);
		}
	}

	public static Configuration fromJSON(String json) {
		return new Gson().fromJson(json, Configuration.class);
	}

	public Profiler getProfiler() {
		return profiler;
	}

	public LanguageModel getLanguageModel() {
		return languageModel;
	}

	public DynamicLexiconTraining getDynamicLexiconTrainig() {
		return dynamicLexiconTraining;
	}

	public String toJSON() {
		return new Gson().toJson(this);
	}

	public static class Profiler {
		private String executable;
		private String languageDirectory;
		private String language;
		private String[] arguments;

		public String[] getArguments() {
			return arguments;
		}

		public String getExecutable() {
			return executable;
		}

		public String getLanguageDirectory() {
			return languageDirectory;
		}

		public String getLanguage() {
			return language;
		}
	}

	public static class LanguageModel {
		private String characterTrigrams;

		public String getCharacterTrigrams() {
			return characterTrigrams;
		}
	}

	public static class DynamicLexiconTraining {
		private JsonObject[] features;
		private boolean copyTrainingFiles, debugTrainingTokens;
		private int testEvaluationFraction;

		public int getTestEvaluationFraction() {
			return testEvaluationFraction;
		}

		public boolean isCopyTrainingFiles() {
			return copyTrainingFiles;
		}

		public boolean isDebugTrainingTokens() {
			return debugTrainingTokens;
		}

		public JsonObject[] getFeatures() {
			return features;
		}
	}
}
