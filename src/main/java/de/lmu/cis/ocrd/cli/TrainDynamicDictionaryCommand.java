package de.lmu.cis.ocrd.cli;

import de.lmu.cis.iba.LineAlignment;
import de.lmu.cis.ocrd.*;
import de.lmu.cis.ocrd.align.TokenAlignment;
import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.ml.features.GTFeature;
import de.lmu.cis.ocrd.ml.features.IsLongerThan;
import de.lmu.cis.ocrd.ml.features.UnigramFeature;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainDynamicDictionaryCommand implements Command {
    @Override
    public void execute(Configuration config) throws Exception {
        if (config.getArgs().length != 2) {
            throw new Exception("usage: gt master-ocr [other-ocr...]");
        }
        execute(config.getArgs());
    }

    private static void execute(String[] args) throws Exception {
        Project project = new Project();
        project.put("masterOCR", FileTypes.openDocument(args[1]), true);
        project.put("gt", FileTypes.openDocument(args[0]), false);
        final FreqMap<String> ocrUnigrams = getOCRUnigrams(project);
        final FeatureSet features = getFeatureSet(ocrUnigrams);
        final ARFFWriter w = ARFFWriter.fromFeatureSet(features)
                .withRelation("dynamic-lexicon")
                .withWriter(new OutputStreamWriter(System.out));
        w.writeHeader();
        for (Token token : getTokens(project)) {
            System.out.println(token);
            System.out.flush();
            w.writeFeatureVector(features.calculateFeatureVector(token));
        }
    }

    private static FreqMap<String> getOCRUnigrams(Project project) throws Exception {
        final FreqMap<String> ocrUnigrams = new FreqMap<>();
        project.eachPage((page)->{
            LineAlignment lineAlignment = new LineAlignment(page, 2);
            for (ArrayList<OCRLine> line : lineAlignment) {
                TokenAlignment tokenAlignment = new TokenAlignment(line.get(0).line.getNormalized());
                tokenAlignment.add(line.get(1).line.getNormalized());
                for (TokenAlignment.Token token : tokenAlignment) {
                    ocrUnigrams.add(token.getMaster());
                }
            }
        });
        return ocrUnigrams;
    }

    private static FeatureSet getFeatureSet(FreqMap<String> ocrUnigrams) {
        final FeatureSet features = new FeatureSet();
        features.add(new UnigramFeature(ocrUnigrams, "MasterOCRUnigramRelativeFrequency"));
        features.add(new IsLongerThan(5, "MediumToken"));
        features.add(new IsLongerThan(10, "LongToken"));
        features.add(new IsLongerThan(15, "VeryLongToken"));
        features.add(new GTFeature());
        return features;
    }

    private static List<Token> getTokens(Project project) throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        project.eachPage((page)->{
            LineAlignment lineAlignment = new LineAlignment(page, 2);
            for (ArrayList<OCRLine> line : lineAlignment) {
                SimpleLine master = (SimpleLine) line.get(0).line;
                SimpleLine gt = (SimpleLine) line.get(1).line;
                TokenAlignment tokenAlignment = new TokenAlignment(master.getNormalized());
                tokenAlignment.add(gt.getNormalized());
                int offset = 0;
                System.out.println("ALING LINE: " + master.getNormalized());
                System.out.println("ALING LINE: " + gt.getNormalized());
                for (TokenAlignment.Token token : tokenAlignment) {
                    Optional<Word> masterToken = master.getWord(offset, token.getMaster());
                    assert masterToken.isPresent();
                    offset += token.getMaster().length();
                    System.out.println("ALIGN: " + token);
                    tokens.add(new Token(masterToken.get()).withGT(token.getAlignment(0)));
                    Token last = tokens.get(tokens.size() - 1);
                    System.out.println("ALIGN-token: " + last);
                }
                System.out.flush();
            }
        });
        return tokens;
    }
}
