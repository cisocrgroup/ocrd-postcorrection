package de.lmu.cis.ocrd.train.step;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

public class ModelDir {
	private final Path dir;

	public ModelDir(Path dir) {
		this.dir = dir;
		File f = dir.toFile();
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	public void remove() {
		dir.toFile().delete();
	}

	public Path getDLEFeatures() {
		return makePath("dle-features.json");
	}

	public void putDLEFeatures(Path features) throws IOException {
		put(features, getDLEFeatures());
	}

	public Path getDLETraining(int n) {
		return makePath("dle-train-", n, ".arff");
	}

	public Path getDLEModel(int n) {
		return makePath("dle-model-", n, ".ser");
	}

	public Path getRRFeatures() {
		return makePath("rr-features.json");
	}

	public void putRRFeatures(Path features) throws IOException {
		put(features, getRRFeatures());
	}

	public Path getRRTraining(int n) {
		return makePath("rr-train-", n, ".arff");
	}

	public Path getRRModel(int n) {
		return makePath("rr-model-", n, ".ser");
	}

	private Path makePath(String name) {
		return Paths.get(dir.toString(), name);
	}

	private Path makePath(String prefix, int n, String suffix) {
		return Paths.get(dir.toString(), prefix + n + suffix);
	}

	private void put(Path source, Path dest) throws IOException {
		Logger.debug("copying {} to {}", source.toString(), dest.toString());
		FileUtils.copyFile(source.toFile(), dest.toFile());
	}
}
