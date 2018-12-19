package de.lmu.cis.ocrd.train.step;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

public class Config {
	public String profiler;
	public String profilerLanguageDir;
	public String profilerLanguage;
	public String trigrams;

	public String dleFeatures;
	public String rrFeatures;

	public List<String> trainingFiles;
	public List<String> evaluationFiles;

	public static Config fromJSON(Path path) throws IOException {
		try (FileInputStream is = new FileInputStream(path.toFile())) {
			return fromJSON(is);
		}
	}

	public static Config fromJSON(FileInputStream is) throws IOException {
		StringWriter out = new StringWriter();
		IOUtils.copy(is, out, Charset.forName("UTF-8"));
		return fromJSON(out.toString());
	}

	public static Config fromJSON(String json) {
		return new Gson().fromJson(json, Config.class);
	}
}
