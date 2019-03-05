package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.ml.features.OCRToken;
import de.lmu.cis.ocrd.pagexml.*;
import de.lmu.cis.ocrd.profile.Candidate;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CheckCorpusCommand implements Command{

	private final String prefix;
	private final Path dir;

	public CheckCorpusCommand() {
		this("OCR-D-PROFILE", "workspace");
	}

	private CheckCorpusCommand(String prefix, String dir) {
		this.prefix = prefix;
		this.dir = Paths.get(dir);
	}

	@Override
	public void execute(CommandLineArguments config) throws Exception {
		File[] files = dir.toFile().listFiles();
		if (files == null) {
			throw new Exception("empty directory: " + dir.toString());
		}
		for (File dir : files) {
			if (dir.isDirectory() && dir.getName().startsWith(prefix)) {
				Logger.info("checking directory: {}", dir.toString());
				check(dir);
			}
		}
	}

	private void check(File dir) throws Exception {
		File[] files = dir.listFiles();
		if (files == null) {
			throw new Exception("empty directory: " + dir.toString());
		}
		for (File xml : files) {
			Logger.info("checking file: {}", xml.toString());
			try (FileInputStream is = new FileInputStream(xml)) {
				check(Page.parse(is));

			}
		}
	}

	private void check(Page page) throws Exception {
		for (Line line : page.getLines()) {
			for (Word word : line.getWords()) {
				check(word);
			}
		}
	}

	private void check(Word word) throws Exception {
		for (String u : word.getUnicodeNormalized()) {
			if (u == null) {
				throw new Exception("unicode string is null");
			}
		}
		for (TextEquiv te : word.getTextEquivs()) {
			if (te.getDataTypeDetails() == null) {
				throw new Exception("data type details is null");
			}
			if (te.getDataType() == null) {
				throw new Exception("data type is null");
			}
			if (te.getIndex() < 0) {
				throw new Exception("invalid index: " + te.getIndex());
			}
			if (te.getConfidence() > 1.0 || te.getConfidence() < 0.0) {
				throw new Exception("invalid confidence: " + te.getConfidence());

		 	}
		}
		// TODO: use settings from parameter file
		check(new OCRTokenImpl(word, 2, 5, Profile.empty()));
	}

	private void check(OCRToken token) throws Exception {
		for (Candidate c : token.getAllProfilerCandidates()) {
			if (c == null) {
				throw new Exception("invalid candiate = null");
			}
			if (c.Suggestion == null) {
				throw new Exception("candidate suggestion is null");
			}
		}
		if (token.getGT() == null) {
			throw new Exception("ground truth is null");
		}
		if (token.getMasterOCR() == null) {
			throw new Exception("master ocr is null");
		}
		if (token.getSlaveOCR(1) == null) {
			throw new Exception("other ocr is null");
		}
	}

	@Override
	public String getName() {
		return "check-corpus";
	}
}
