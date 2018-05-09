package de.lmu.cis.ocrd.cli;

import de.lmu.cis.iba.LineAlignment;
import de.lmu.cis.ocrd.*;
import de.lmu.cis.ocrd.align.TokenAlignment;
import de.lmu.cis.ocrd.ml.ARFFWriter;
import de.lmu.cis.ocrd.ml.FeatureSet;
import de.lmu.cis.ocrd.ml.FreqMap;
import de.lmu.cis.ocrd.ml.Token;
import de.lmu.cis.ocrd.ml.features.FeatureFactory;
import de.lmu.cis.ocrd.ml.features.GTFeature;
import de.lmu.cis.ocrd.ml.features.TokenLengthFeature;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainDynamicDictionaryCommand implements Command {
    @Override
    public String getName() {
        return "train";
    }

    @Override
    public void execute(Configuration config) throws Exception {
        if (config.getArgs().length != 2) {
            throw new Exception("usage: gt master-ocr [other-ocr...]");
        }
        Project project = new Project();
        project.put("masterOCR", FileTypes.openDocument(config.getArgs()[1]), true);
        project.put("gt", FileTypes.openDocument(config.getArgs()[0]), false);
        // final FreqMap<String> ocrUnigrams = getOCRUnigrams(project);
        // final FeatureSet features = getFeatureSet(ocrUnigrams);
        OutputStreamWriter osw = new OutputStreamWriter(System.out);
        final List<Token> tokens = getTokens(project);
        final ArgumentFactory factory = new ArgumentFactory(config.getParameters(), tokens);
        final FeatureSet fs = FeatureFactory.getDefault().withArgumentFactory(factory).createFeatureSet(config.getParameters().getDynamicLexiconFeatures()).add(new GTFeature());
        final ARFFWriter w = ARFFWriter.fromFeatureSet(fs)
                .withRelation("dynamic-lexicon")
                .withWriter(osw);
        w.writeHeader(1);
        for (Token token : tokens) {
            w.writeFeatureVector(fs.calculateFeatureVector(token));
        }
        osw.close();
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
        // features.add(new UnigramFeature(ocrUnigrams, "MasterOCRUnigramRelativeFrequency"));
        features.add(new TokenLengthFeature(5, 9, "MediumToken"));
        features.add(new TokenLengthFeature(10, 14, "LongToken"));
        features.add(new TokenLengthFeature(15, Integer.MAX_VALUE, "VeryLongToken"));
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
                for (TokenAlignment.Token token : tokenAlignment) {
                    Optional<Word> masterToken = master.getWord(offset, token.getMaster());
                    assert masterToken.isPresent();
                    offset += token.getMaster().length();
                    tokens.add(new Token(masterToken.get()).withGT(token.getAlignment(0)));
                }
            }
        });
        return tokens;
    }
}
