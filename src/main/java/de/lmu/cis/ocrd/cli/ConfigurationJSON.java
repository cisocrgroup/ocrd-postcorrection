package de.lmu.cis.ocrd.cli;

import com.google.gson.JsonObject;

// Data class that is used to map the
// JSON configuration.
public class ConfigurationJSON {
    public static class Profiler {
        public String getExecutable() {
            return executable;
        }

        public String getLanguageDirectory() {
            return languageDirectory;
        }

        private String executable;
        private String languageDirectory;
        private String language;

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

    public JsonObject[] getDynamicLexiconFeatures() {
		return dynamicLexiconFeatures;
	}

	private Profiler profiler;
	private LanguageModel languageModel;
	private JsonObject[] dynamicLexiconFeatures;

	public static ConfigurationJSON getDefault() {
		final ConfigurationJSON c = new ConfigurationJSON();
		c.profiler = new Profiler();
		c.profiler.executable = "/apps/profiler";
		c.profiler.languageDirectory = "/data/ProfilerLanguages";
		c.profiler.language = "german";
		c.languageModel = new LanguageModel();
		c.languageModel.characterTrigrams = "/data/languageModel/characterTrigrams.csv";
		c.dynamicLexiconFeatures = new JsonObject[0];
		return c;
	}
}
