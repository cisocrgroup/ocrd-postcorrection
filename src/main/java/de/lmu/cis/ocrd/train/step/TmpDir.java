package de.lmu.cis.ocrd.train.step;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import de.lmu.cis.ocrd.pagexml.Line;
import de.lmu.cis.ocrd.pagexml.Page;

public class TmpDir {
	private final Path dir;

	public TmpDir(Path dir) {
		this.dir = dir;
		File f = dir.toFile();
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	public void remove() {
		dir.toFile().delete();
	}

	public Path getDLEList(int n) {
		return Paths.get(dir.toString(), "dle-list-" + n + ".txt");
	}

	public Path getDLEProfile() {
		return Paths.get(dir.toString(), "dle-profile.json");
	}

	public Path getDLEProfilerInput() {
		return Paths.get(dir.toString(), "dle-profile.txt");
	}

	public void putProfilerInputFile(boolean withGT, List<String> files,
			Path opath) throws Exception {
		final int i = withGT ? 1 : 0;
		try (BufferedWriter w = new BufferedWriter(
				new FileWriter(opath.toFile()))) {
			for (String file : files) {
				Page page = Page.open(Paths.get(file));
				for (Line line : page.getLines()) {
					w.write(line.getUnicodeNormalized().get(i));
					w.newLine();
				}
			}
		}
	}
}
