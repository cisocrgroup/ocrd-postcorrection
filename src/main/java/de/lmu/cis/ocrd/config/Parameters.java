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
    private String trigrams = "";
    private List<String> filterClasses;
    private int nOCR = 0;
    private int maxCandidates = 0;
    private boolean runLE = false;
    private boolean runDM = false;
    private boolean ocropus = false;

    public void setLEFeatures(List<JsonObject> leFeatures) {
        this.leFeatures = leFeatures;
    }

    public void setRRFeatures(List<JsonObject> rrFeatures) {
        this.rrFeatures = rrFeatures;
    }

    public void setDMFeatures(List<JsonObject> dmFeatures) {
        this.dmFeatures = dmFeatures;
    }

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

    public FeatureClassFilter getFilterClasses() {
        return new FeatureClassFilter(filterClasses);
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

    public boolean isOcropus() {return ocropus;}

    public void setOcropus(boolean ocropus) {this.ocropus = ocropus;}

    public Path getModel() {
        return Paths.get(dir, "model.zip");
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
