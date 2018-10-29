package de.lmu.cis.ocrd.train.step;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Base {
	private final LM lm;
	private final boolean withGT;
	private final ModelDir mdir;
	private final TmpDir tdir;
	private final Config config;

	public Base(boolean withGT, String logLevel, ModelDir mdir, TmpDir tdir, Config config) {
		this.lm = new LM(withGT, config.trigrams, config.trainingFiles);
		this.withGT = false;
		this.mdir = mdir;
		this.tdir = tdir;
		this.config = config;
		setupLogger(logLevel.toUpperCase());
	}

	public JsonObject[] getFeatures(Path features) throws Exception {
		JsonObject[] os;
		try (InputStream is = new FileInputStream(features.toFile())) {
			final String json = IOUtils.toString(is, Charset.forName("UTF-8"));
			os = new Gson().fromJson(json, JsonObject[].class);
		}
		return os;
	}

	public LM getLM() {
		return lm;
	}

	public Config getConfig() {
		return config;
	}

	public boolean isWithGT() {
		return withGT;
	}

	public ModelDir getModelDir() {
		return mdir;
	}

	public TmpDir getTmpDir() {
		return tdir;
	}

	private void setupLogger(String logLevel) {
		Configurator.currentConfig().level(Level.valueOf(logLevel)).activate();
		Logger.debug("current log level: {}", Logger.getLevel());
	}

	public static List<String> toList(String[] args, int skip) {
		return Arrays.stream(args).skip(skip).collect(Collectors.toList());
	}
}
