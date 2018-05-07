package de.lmu.cis.ocrd.cli;

import com.google.gson.JsonObject;

// Data class that is used to map the
// JSON configuration.
class ConfigurationJSON {
	public String getProfilerCommand() {
		return profilerCommand;
	}

	public JsonObject[] getDynamicLexiconFeatures() {
		return dynamicLexiconFeatures;
	}

	public String getCharacterNGrams() {

		return characterNGrams;
	}

	private String profilerCommand;
	private String characterNGrams;
	private JsonObject[] dynamicLexiconFeatures;

	public static ConfigurationJSON getDefault() {
		final ConfigurationJSON c = new ConfigurationJSON();
		c.profilerCommand = "/apps/profiler";
		c.characterNGrams = "/data/characterNGrams.csv";
		c.dynamicLexiconFeatures = new JsonObject[0];
		return c;
	}
}
