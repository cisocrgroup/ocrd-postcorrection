package de.lmu.cis.ocrd.calamari;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.profile.Candidates;
import de.lmu.cis.ocrd.profile.Profile;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Workspace extends AbstractWorkspace {
    public static final String EXTENSION = ".alt.json";
    private final Path dir;
    private final Parameters parameters;
    private List<BaseOCRToken> baseTokens;
    private List<OCRToken> ocrTokens;

    public Workspace(Path dir, Parameters parameters) {
        this.dir = dir;
        this.parameters = parameters;
    }

    private void walk() throws IOException {
        this.baseTokens = new ArrayList<>();
        File[] files = dir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(EXTENSION));
        int id = 0;
        for (File file: files) {
            try (Reader r = new FileReader(file)) {
                Data[] data;
                data = new Gson().fromJson(r, Data[].class);
                for (Data d: data) {
                    ++id;
                    this.baseTokens.add(new Token(d, id));
                }
            }
        }
    }

    @Override
    public BaseOCRTokenReader getBaseOCRTokenReader(String ifg) throws Exception {
        if (this.baseTokens == null) {
            walk();
        }
        return new BaseOCRTokenReaderImpl(this.baseTokens);
    }

    @Override
    public OCRTokenReader getNormalTokenReader(String ifg, Profile profile) throws Exception {
        if (this.ocrTokens == null) {
            this.ocrTokens = new ArrayList<>();
            getBaseOCRTokenReader(null).read().forEach(t -> ocrTokens.add(makeCandidateOCRToken(t, profile)));
        }
        return new OCRTokenReaderImpl(this.ocrTokens);
    }

    @Override
    public void resetProfile(String ifg, Profile profile) throws Exception {
        List<OCRToken> tokens = getNormalTokenReader(null, profile).read();
        for (int i = 0; i < tokens.size(); i++) {
            final BaseOCRToken token = ((AbstractOCRToken)tokens.get(i)).getBase();
            tokens.set(i, makeCandidateOCRToken(token, profile));
        }
        ocrTokens = tokens;
    }

    @Override
    public void write(String ifg, String ofg) {}

    private OCRToken makeCandidateOCRToken(BaseOCRToken token, Profile profile) {
        final Optional<Candidates> maybeCandidates = profile.get(token.getMasterOCR().getWordNormalized());
        return maybeCandidates.map(candidates -> new CandidatesOCRToken(token, parameters.getMaxCandidates(), candidates.Candidates)).orElseGet(() -> new CandidatesOCRToken(token));
    }
}
