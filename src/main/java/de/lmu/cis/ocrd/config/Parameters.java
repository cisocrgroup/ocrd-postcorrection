package de.lmu.cis.ocrd.config;

import com.google.gson.JsonObject;
import de.lmu.cis.ocrd.ml.features.FeatureClassFilter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Parameters {
    private Profiler profiler = new Profiler();
    private List<JsonObject> leFeatures = new ArrayList<>();
    private List<JsonObject> rrFeatures = new ArrayList<>();
    private List<JsonObject> dmFeatures = new ArrayList<>();
    private String dir = "";
    private String model = "";
    private String trigrams = "";
    private String ocropusImageExtension = "";
    private List<String> filterClasses = new ArrayList<>();
    private List<String> ocropusOCRExtensions = new ArrayList<>();
    private String dmTrainingType;
    private long seed;
    private int nOCR = 0;
    private int maxCandidates = 0;
    private int maxTokens = 0;
    private boolean runLE = false;
    private boolean runDM = false;

    public void setLEFeatures(List<JsonObject> leFeatures) {
        this.leFeatures = leFeatures;
    }

    public void setRRFeatures(List<JsonObject> rrFeatures) {
        this.rrFeatures = rrFeatures;
    }

    public void setDMFeatures(List<JsonObject> dmFeatures) {
        this.dmFeatures = dmFeatures;
    }

    public Path getCacheDir() {return Paths.get(dir, "cache");}

    public Path getDir() {
        return Paths.get(dir);
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public Path getTrigrams() {
        return Paths.get(trigrams);
    }

    public void setTrigrams(String trigrams) {
        this.trigrams = trigrams;
    }

    public int getMaxTokens() {return maxTokens;}

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public FeatureClassFilter getClassFilter() {
        return new FeatureClassFilter(filterClasses);
    }

    public List<String> getFilterClasses() {
        return filterClasses;
    }

    public void setFilterClasses(List<String> filterClasses) {
        this.filterClasses = filterClasses;
    }

    public int getNOCR() {
        return nOCR;
    }

    public void setNOCR(int nOCR) {
        this.nOCR = nOCR;
    }

    public int getMaxCandidates() {
        return maxCandidates;
    }

    public void setMaxCandidates(int maxCandidates) {
        this.maxCandidates = maxCandidates;
    }

    public boolean isRunLE() {
        return runLE;
    }

    public void setRunLE(boolean runLE) {
        this.runLE = runLE;
    }

    public boolean isRunDM() {
        return runDM;
    }

    public void setRunDM(boolean runDM) {
        this.runDM = runDM;
    }

    public String getDMTrainingType() {return dmTrainingType;}

    public void setDMTrainingType(String t) {
        this.dmTrainingType = t;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public String getOcropusImageExtension() {
        return ocropusImageExtension;
    }

    public void setOcropusImageExtension(String extension) {
        ocropusImageExtension = extension;
    }

    public List<String> getOcropusOCRExtensions() {
        return ocropusOCRExtensions;
    }

    public void setOcropusOCRExtensions(List<String> extensions) {
        ocropusOCRExtensions = extensions;
    }

    public boolean isOcropus() {
        return ocropusOCRExtensions != null
                && !ocropusOCRExtensions.isEmpty()
                && ocropusImageExtension != null
                && !ocropusImageExtension.isEmpty();
    }

    public boolean isCalamari(String ext) {
        return ocropusOCRExtensions != null
                && ocropusOCRExtensions.size() == 1
                && ocropusOCRExtensions.get(0).endsWith(ext);
    }

    public Path getModel() {
        if (model == null || "".equals(model)) {
            return Paths.get(dir, "model.zip");
        }
        return Paths.get(model);
    }

    public LexiconTrainingResource getLETraining() {
        return new LexiconTrainingResource("le", dir, leFeatures);
    }
    public TrainingResource getRRTraining() {
        return new TrainingResource("rr", dir, rrFeatures);
    }

    public ProtocolTrainingResource getDMTraining() {
        return new ProtocolTrainingResource("dm", dir, dmFeatures);
    }

    public Profiler getProfiler() {
        return profiler;
    }

    public void setProfiler(Profiler profiler) {
        this.profiler = profiler;
    }
}
