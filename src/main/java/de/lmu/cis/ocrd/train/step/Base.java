package de.lmu.cis.ocrd.train.step;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

public class Base {
	private final LM lm;
	private final List<Path> files;
	private final boolean withGT;
	private final Path dir;

	public Base(boolean withGT, String logLevel, String profile,
			String trigrams, String dir, List<String> files) {
		this.lm = new LM(withGT, profile, trigrams, files);
		this.withGT = withGT;
		this.dir = Paths.get(dir);
		this.files = new ArrayList<Path>();
		for (String file : files) {
			this.files.add(Paths.get(file));
		}
		setupLogger(logLevel.toUpperCase());
	}

	public LM getLM() {
		return lm;
	}

	public List<Path> getFiles() {
		return files;
	}

	public boolean isWithGT() {
		return withGT;
	}

	public Path getDir() {
		return dir;
	}

	private void setupLogger(String logLevel) {
		Configurator.currentConfig().level(Level.valueOf(logLevel)).activate();
		Logger.debug("current log level: {}", Logger.getLevel());
	}
}
