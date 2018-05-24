package de.lmu.cis.ocrd.train;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

// Data class that is used to map the
// JSON data.
public class Configuration {
    public static class Profiler {
        private String executable;
        private String languageDirectory;
        private String language;
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
    public Profiler getProfiler() {
        return profiler;
    }

    public static class LanguageModel {
        private String characterTrigrams;
        public String getCharacterTrigrams() {
            return characterTrigrams;
        }
    }
    public LanguageModel getLanguageModel() {
        return languageModel;
    }

    public static class DynamicLexiconTraining {
        private JsonObject[] features;
        private boolean copyTrainingFiles, debugTrainingTokens;
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
    public DynamicLexiconTraining getDynamicLexiconTrainig() {
        return dynamicLexiconTraining;
    }

	private Profiler profiler;
	private LanguageModel languageModel;
	private DynamicLexiconTraining dynamicLexiconTraining;

	public static Configuration getDefault() {
		final Configuration c = new Configuration();
		c.profiler = new Profiler();
		c.profiler.executable = "/apps/profiler";
		c.profiler.languageDirectory = "/data/ProfilerLanguages";
		c.profiler.language = "german";
		c.languageModel = new LanguageModel();
		c.languageModel.characterTrigrams = "/data/languageModel/characterTrigrams.csv";
		c.dynamicLexiconTraining = new DynamicLexiconTraining();
		c.dynamicLexiconTraining.features = new JsonObject[0];
        c.dynamicLexiconTraining.debugTrainingTokens = true;
        c.dynamicLexiconTraining.copyTrainingFiles = true;
		return c;
	}

	public static Configuration fromJSON(String json) {
		return new Gson().fromJson(json, Configuration.class);
	}

	public String toJson() {
		return new Gson().toJson(this);
	}
}
