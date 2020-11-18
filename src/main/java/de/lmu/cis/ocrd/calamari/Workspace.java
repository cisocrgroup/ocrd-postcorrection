package de.lmu.cis.ocrd.calamari;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Workspace extends AbstractWorkspace {
    public static final String EXTENSION = ".alt.json";
    private final Parameters parameters;
    private Map<String, List<BaseOCRToken>> baseTokens;
    private Map<String, List<OCRToken>> ocrTokens;

    public Workspace(Parameters parameters) {
        this.parameters = parameters;
        this.baseTokens = new HashMap<>();
        this.ocrTokens = new HashMap<>();
    }

    private List<BaseOCRToken> walk(Path dir) throws IOException {
        final List <BaseOCRToken> ret = new ArrayList<>();
        File[] files = dir.toFile().listFiles((d, name) -> name.toLowerCase().endsWith(EXTENSION));
        int id = 0;
        for (File file: files) {
            try (Reader r = new FileReader(file)) {
                Data[] data;
                data = new Gson().fromJson(r, Data[].class);
                for (Data d: data) {
                    ++id;
                    ret.add(new Token(d, id));
                }
            }
        }
        if (parameters.getMaxTokens() > 0) {
            return ret.subList(0, Math.min(ret.size(), parameters.getMaxTokens()));
        }
        return ret;
    }

    @Override
    public BaseOCRTokenReader getBaseOCRTokenReader(String ifg) throws Exception {
        if (!this.baseTokens.containsKey(ifg)) {
            final List<BaseOCRToken> tmp = walk(Paths.get(ifg));
            this.baseTokens.put(ifg, tmp);
        }
        return new BaseOCRTokenReaderImpl(this.baseTokens.get(ifg));
    }

    @Override
    public OCRTokenReader getNormalTokenReader(String ifg, Profile profile) throws Exception {
        if (!this.ocrTokens.containsKey(ifg)) {
            List<OCRToken> tmp = new ArrayList<>();
            getBaseOCRTokenReader(ifg).read().forEach(t -> tmp.add(makeCandidateOCRToken(t, profile)));
            this.ocrTokens.put(ifg, tmp);
        }
        return new OCRTokenReaderImpl(this.ocrTokens.get(ifg));
    }

    @Override
    public void resetProfile(String ifg, Profile profile) throws Exception {
        List<OCRToken> tokens = getNormalTokenReader(ifg, profile).read();
        for (int i = 0; i < tokens.size(); i++) {
            final BaseOCRToken token = ((AbstractOCRToken)tokens.get(i)).getBase();
            tokens.set(i, makeCandidateOCRToken(token, profile));
        }
        ocrTokens.put(ifg, tokens);
    }

    @Override
    public void write(String ifg, String ofg) {}

    private OCRToken makeCandidateOCRToken(BaseOCRToken token, Profile profile) {
        final Optional<Candidates> maybeCandidates = profile.get(token.getMasterOCR().getWordNormalized());
        return maybeCandidates.map(candidates -> new CandidatesOCRToken(token, parameters.getMaxCandidates(), candidates.Candidates)).orElseGet(() -> new CandidatesOCRToken(token));
    }
}
