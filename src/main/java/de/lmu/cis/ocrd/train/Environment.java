package de.lmu.cis.ocrd.train;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.FileTypes;
import de.lmu.cis.ocrd.ml.CharacterNGrams;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.features.ArgumentFactory;
import de.lmu.cis.ocrd.profile.FileProfiler;
import de.lmu.cis.ocrd.profile.LocalProfiler;
import de.lmu.cis.ocrd.profile.Profile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// This class manages all paths and temporary files
// that are needed for the different training and evaluation steps.
public class Environment implements ArgumentFactory {
	private static final String resources = "resources";
	private static final String dLex = "dLex";
	private static final String dLexFS = "features.ser";
	private static final String trainingFile = "training.arff";
	private static final String testFile = "tokens.ser";
	private static final String evaluationFile = "evaluation.txt";
	private static final String model = "model.ser";
	private static final String dataFile = "data.json";
	private static final String configurationFile = "configuration.json";

	private final String path, name;
	private Path gt, masterOCR;
	private final List<Path> otherOCR = new ArrayList<>();
	private boolean copyTrainingFiles;
	private boolean debugTokenAlignment;
	private Configuration configuration;
	private Profile profile;
	private FreqMap masterOCRUnigrams, charTrigrams;
	private ArrayList<FreqMap> otherOCRUnigrams = new ArrayList<>();
	private Document masterOCRDocument, gtDocument;
	private ArrayList<Document> otherOCRDocuments = new ArrayList<>();

	public Environment(String base, String name) throws IOException {
		this.path = base;
		this.name = name;
		setupDirectories();
		setupTrainingDirectories(1);
	}

	private void setupDirectories() throws IOException {
		makeDirectoryIfNotExists(getPath());
		makeDirectoryIfNotExists(Paths.get(path, getResourcesDirectory().toString()));
		makeDirectoryIfNotExists(Paths.get(path, getDynamicLexiconTrainingDirectory().toString()));
	}

	private void makeDirectoryIfNotExists(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			return;
		}
		Files.createDirectory(path);
	}

	private void setupTrainingDirectories(int n) throws IOException {
		Files.createDirectory(Paths.get(path, getDynamicLexiconTrainingDirectory(n).toString()));
	}

	public String getName() {
		return name;
	}

	public Path getPath() {
		return Paths.get(path, name);
	}

	public Path fullPath(Path path) {
		return Paths.get(this.path, path.toString());
	}

	public Path getGT() {
		return gt;
	}

	public Environment withGT(String gt) throws IOException {
		if (!copyTrainingFiles) {
			this.gt = Paths.get(gt);
		} else {
			this.gt = copy(Paths.get(gt));
		}
		return this;
	}

	Document openGT() throws Exception {
		if (gtDocument == null) {
			gtDocument = FileTypes.openDocument(getOCRPath(getGT()).toString());
		}
		return gtDocument;
	}

	public Path getMasterOCR() {
		return masterOCR;
	}

	public Environment withMasterOCR(String masterOCR) throws IOException {
		if (!copyTrainingFiles) {
			this.masterOCR = Paths.get(masterOCR);
		} else {
			this.masterOCR = copy(Paths.get(masterOCR));
		}
		return this;
	}

	Document openMasterOCR() throws Exception {
		if (masterOCRDocument == null) {
			masterOCRDocument = FileTypes.openDocument(getOCRPath(getMasterOCR()).toString());
		}
		return masterOCRDocument;
	}

	private Path getOCRPath(Path ocr) {
		return copyTrainingFiles ? fullPath(ocr) : ocr;
	}

	public Environment addOtherOCR(String otherOCR) throws IOException {
		if (!copyTrainingFiles) {
			this.otherOCR.add(Paths.get(otherOCR));
		} else {
			this.otherOCR.add(copy(Paths.get(otherOCR)));
		}
		setupTrainingDirectories(1 + this.otherOCR.size());
		return this;
	}

	public Path getOtherOCR(int i) {
		return otherOCR.get(i);
	}

	Document openOtherOCR(int i) throws Exception {
		while (otherOCRDocuments.size() <= i) {
			otherOCRDocuments.add(null);
		}
		if (otherOCRDocuments.get(i) == null) {
			otherOCRDocuments.set(i, FileTypes.openDocument(getOCRPath(getOtherOCR(i)).toString()));
		}
		return otherOCRDocuments.get(i);
	}

	public int getNumberOfOtherOCR() {
		return otherOCR.size();
	}

	public Environment withCopyTrainingFiles(boolean copy) {
		this.copyTrainingFiles = copy;
		return this;
	}

	public Environment withDebugTokenAlignment(boolean debug) {
		this.debugTokenAlignment = debug;
		return this;
	}

	public boolean isDebugTokenAlignment() {
		return debugTokenAlignment;
	}


	public Environment withConfiguration(Configuration c) throws IOException {
		try (OutputStream out = new FileOutputStream(fullPath(getConfigurationFile()).toFile())) {
			IOUtils.write(c.toJSON(), out, Charset.forName("UTF-8"));
		}
		return this;
	}

	public Configuration openConfiguration() throws IOException {
		if (configuration == null) {
			try (InputStream in = new FileInputStream(fullPath(getConfigurationFile()).toFile())) {
				final String json = IOUtils.toString(in, Charset.forName("UTF-8"));
				configuration = Configuration.fromJSON(json);
			}
		}
		return configuration;
	}

	public Path getConfigurationFile() {
		return Paths.get(getResourcesDirectory().toString(), configurationFile);
	}

	public void remove() throws IOException {
		FileUtils.deleteDirectory(getPath().toFile());
	}

	private Data newData() {
		Data data = new Data();
		data.configuration = getConfigurationFile().toString();
		data.gt = gt.toString();
		data.masterOCR = masterOCR.toString();
		data.dynamicLexiconFeatureSet = getDynamicLexiconFeatureSet().toString();
		data.dynamicLexiconTrainingFiles = new String[1 + otherOCR.size()];
		data.dynamicLexiconEvaluationFiles = new String[1 + otherOCR.size()];
		data.dynamicLexiconModelFiles = new String[1 + otherOCR.size()];
		data.dynamicLexiconTestFiles = new String[1 + otherOCR.size()];
		data.dynamicLexiconTrainingFiles[0] = getDynamicLexiconTrainingFile(1).toString();
		data.dynamicLexiconModelFiles[0] = getDynamicLexiconModel(1).toString();
		data.dynamicLexiconTrainingFiles[0] = getDynamicLexiconTrainingFile(1).toString();
		data.dynamicLexiconEvaluationFiles[0] = getDynamicLexiconEvaluationFile(1).toString();
		data.dynamicLexiconTestFiles[0] = getDynamicLexiconTestFile(1).toString();
		data.otherOCR = new String[otherOCR.size()];
		for (int i = 0; i < otherOCR.size(); i++) {
			data.dynamicLexiconTrainingFiles[i + 1] = getDynamicLexiconTrainingFile(i + 2).toString();
			data.dynamicLexiconEvaluationFiles[i + 1] = getDynamicLexiconEvaluationFile(i + 2).toString();
			data.dynamicLexiconModelFiles[i + 1] = getDynamicLexiconModel(i + 2).toString();
			data.dynamicLexiconTestFiles[i + 1] = getDynamicLexiconTestFile(i + 2).toString();
			data.otherOCR[i] = otherOCR.get(i).toString();
		}
		data.copyTrainingFiles = this.copyTrainingFiles;
		data.debugTokenAlignment = this.debugTokenAlignment;
		data.data = getDataFile().toString();
		return data;
	}

	public Data openData() throws IOException {
		return Data.fromJSON(fullPath(getDataFile()));
	}

	public void writeData() throws IOException {
		try (PrintWriter out = new PrintWriter(fullPath(getDataFile()).toFile())) {
			out.println(newData().toJSON());
		}
	}

	private Path copy(Path path) throws IOException {
		final Path target = Paths.get(fullPath(getResourcesDirectory()).toString(), path.getFileName().toString());
		if (Files.isDirectory(path)) {
			FileUtils.copyDirectory(path.toFile(), target.toFile());
		} else {
			FileUtils.copyFile(path.toFile(), target.toFile());
		}
		return Paths.get(getResourcesDirectory().toString(), path.getFileName().toString());
	}

	public Path getResourcesDirectory() {
		return Paths.get(name, resources);
	}

	public Path getDynamicLexiconTrainingDirectory() {
		return Paths.get(name, dLex);
	}

	public Path getDynamicLexiconTrainingDirectory(int n) {
		return Paths.get(getDynamicLexiconTrainingDirectory().toString(), Integer.toString(n));
	}

	public Path getDynamicLexiconFeatureSet() {
		return Paths.get(getDynamicLexiconTrainingDirectory().toString(), dLexFS);
	}

	public Path getDynamicLexiconTrainingFile(int n) {
		return Paths.get(getDynamicLexiconTrainingDirectory(n).toString(), trainingFile);
	}

	public Path getDataFile() {
		return Paths.get(getResourcesDirectory().toString(), dataFile);
	}

	public void zipTo(Path zip) throws IOException {
		writeData();
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip.toFile()))) {
			eachFile((p)-> putZIPEntry(out, fullPath(p), p.toString()));
			out.finish();
		}
	}

	private void putZIPEntry(ZipOutputStream out, Path path, String e) throws IOException {
		try {
			ZipEntry entry = new ZipEntry(e);
			out.putNextEntry(entry);
			IOUtils.copy(new FileInputStream(path.toFile()), out);
		} finally {
			out.closeEntry();
		}
	}

	private Path getLocalProfileOutputPath() {
		return Paths.get(getResourcesDirectory().toString(), getMasterOCR().getFileName().toString() + ".profile.json");
	}

	private Path getLocalProfileInputPath() {
		return Paths.get(getResourcesDirectory().toString(), getMasterOCR().getFileName().toString() + ".profile.txt");
	}

	public Path getDynamicLexiconTestFile(int n) {
		return Paths.get(getDynamicLexiconTrainingDirectory(n).toString(), testFile);
	}

	public Path getDynamicLexiconModel(int n) {
		return Paths.get(getDynamicLexiconTrainingDirectory(n).toString(), model);
	}

	public Path getDynamicLexiconEvaluationFile(int i) {
		return Paths.get(getDynamicLexiconTrainingDirectory(i).toString(), evaluationFile);
	}

	private interface EachFileCallback {
		void apply(Path path) throws IOException;
	}

	private void eachFile(EachFileCallback f) throws IOException {
		final Data data = newData();
		applyIfFileExists(f, Paths.get(data.configuration));
		applyIfFileExists(f, Paths.get(data.data));
		applyIfFileExists(f, Paths.get(data.dynamicLexiconFeatureSet));
		for (int i = 0; i < data.dynamicLexiconTrainingFiles.length; i++) {
			applyIfFileExists(f, Paths.get(data.dynamicLexiconTrainingFiles[i]));
			applyIfFileExists(f, Paths.get(data.dynamicLexiconTestFiles[i]));
			applyIfFileExists(f, Paths.get(data.dynamicLexiconEvaluationFiles[i]));
		}
		if (data.copyTrainingFiles) {
			applyIfFileExists(f, Paths.get(data.masterOCR));
			applyIfFileExists(f, Paths.get(data.gt));
			for (int i = 0; i < data.otherOCR.length; i++) {
				applyIfFileExists(f, Paths.get(data.otherOCR[i]));
			}
		}
	}

	private void applyIfFileExists(EachFileCallback f, Path path) throws IOException {
		if (Files.exists(fullPath(path))) {
			f.apply(path);
		}
	}

	// Data class for the data of the training environment.
	public static class Data {
		public String gt;
		public String masterOCR;
		public String dynamicLexiconFeatureSet;
		public String data;
		String configuration;
		public String[] otherOCR;
		public String[] dynamicLexiconTrainingFiles;
		public String[] dynamicLexiconEvaluationFiles;
		public String[] dynamicLexiconTestFiles;
		public String[] dynamicLexiconModelFiles;
		public boolean copyTrainingFiles, debugTokenAlignment;

		static Data fromJSON(Path path) throws IOException {
			try (InputStream in = new FileInputStream(path.toFile())) {
				return fromJSON(in);
			}
		}

		private static Data fromJSON(InputStream in) throws IOException {
			final String json = IOUtils.toString(in, Charsets.UTF_8);
			return new Gson().fromJson(json, Data.class);
		}

		String toJSON() {
			return new Gson().toJson(this);
		}
	}

	@Override
	public Profile getProfile() throws Exception {
		if (profile == null) {
			profile = loadProfile();
		}
		return profile;
	}

	private Profile loadProfile() throws Exception {
		final Path path = fullPath(getLocalProfileOutputPath());
		if (Files.exists(path)) {
			return new FileProfiler(path).profile();
		}
		final Path input = fullPath(getLocalProfileInputPath());
		writeDocument(openMasterOCR(), input);
		final Profile profile = new LocalProfiler()
				.withLanguage(openConfiguration().getProfiler().getLanguage())
				.withLanguageDirectory(openConfiguration().getProfiler().getLanguageDirectory())
				.withExecutable(openConfiguration().getProfiler().getExecutable())
				.withArgs(openConfiguration().getProfiler().getArguments())
				.withOutputPath(getLocalProfileOutputPath())
				.withInputPath(input)
				.profile();
		try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path.toFile()))) {
			out.write(new Gson().toJson(profile));
		}
		return profile;
	}

	@Override
	public FreqMap getMasterOCRUnigrams() throws Exception {
		if (masterOCRUnigrams == null) {
			masterOCRUnigrams = loadUnigrams(openMasterOCR());
		}
		return masterOCRUnigrams;
	}

	@Override
	public FreqMap getOtherOCRUnigrams(int i) throws Exception {
		while (otherOCRUnigrams.size() <= i) {
			otherOCRUnigrams.add(null);
		}
		if (otherOCRUnigrams.get(i) == null) {
			otherOCRUnigrams.set(i, loadUnigrams(openOtherOCR(i)));
		}
		return otherOCRUnigrams.get(i);
	}

	@Override
	public int getNumberOfOtherOCRs() {
		return otherOCRUnigrams.size();
	}

	@Override
	public FreqMap getCharacterTrigrams() throws Exception {
		if (charTrigrams == null) {
			charTrigrams = CharacterNGrams.fromCSV(openConfiguration().getLanguageModel().getCharacterTrigrams());
		}
		return charTrigrams;
	}

	private FreqMap loadUnigrams(Document document) throws Exception {
		FreqMap freqMap = new FreqMap();
		document.eachLine((line) -> {
			for (String token : line.line.getNormalized().split("\\s+")) {
				freqMap.add(token);
			}
		});
		return freqMap;
	}

	private static void writeDocument(Document doc, Path out) throws Exception {
		try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(out.toFile()))) {
			doc.eachLine((line) -> {
				w.write(line.line.getNormalized());
				w.write('\n');
			});
		}
	}
}
