package de.lmu.cis.ocrd.train;

import de.lmu.cis.iba.LineAlignment;
import de.lmu.cis.ocrd.*;
import de.lmu.cis.ocrd.align.TokenAlignment;
import de.lmu.cis.ocrd.ml.Token;

import java.util.ArrayList;
import java.util.Optional;

public class Tokenizer {
    public interface Visitor {
        void visit(Token token) throws Exception;
    }
    private final Environment environment;

    public Tokenizer(Environment environment) {
        this.environment = environment;
    }

    // TODO: ugly
    public void eachToken(Visitor v) throws Exception {
        final Project project = newProject();
        project.eachPage((page)->{
            LineAlignment lineAlignment = new LineAlignment(page, 2 + environment.getNumberOfOtherOCR());
            for (ArrayList<OCRLine> line : lineAlignment) {
                final SimpleLine master = (SimpleLine) line.get(0).line;
                final SimpleLine gt = (SimpleLine) line.get(1).line;
                final ArrayList<SimpleLine> otherOCRs = new ArrayList<>();

                TokenAlignment tokenAlignment = new TokenAlignment(master.getNormalized());
                tokenAlignment.add(gt.getNormalized());
                for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
                    otherOCRs.add((SimpleLine)line.get(i+2).line);
                    tokenAlignment.add(otherOCRs.get(i).getNormalized());
                }
                int offset = 0;
                final ArrayList<Integer> otherOffsets = new ArrayList<>();
                for (TokenAlignment.Token token : tokenAlignment) {
                    Optional<Word> masterToken = master.getWord(offset, token.getMaster());
                    assert masterToken.isPresent();
                    offset += token.getMaster().length();
                    for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
                        otherOffsets.add(0);
                    }
                    final Token theTrainingToken = new Token(masterToken.get()).withGT(token.getAlignment(0));
                    for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
                        final Optional<Word> theWord = otherOCRs.get(i).getWord(otherOffsets.get(i), token.getAlignment(i+1));
                        assert(theWord.isPresent());
                        theTrainingToken.addOCR(theWord.get());
                        otherOffsets.set(i, otherOffsets.get(i) + theWord.get().toString().length());
                    }
                    v.visit(theTrainingToken);
                }
            }
        });
    }

    private Project newProject() throws Exception {
        Project project = new Project();
        project.put("masterOCR", FileTypes.openDocument(environment.getMasterOCR().toString()));
        project.put("GT", FileTypes.openDocument(environment.getGT().toString()));
        for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
            project.put("otherOCR" + (i+1), FileTypes.openDocument(environment.getOtherOCR(i).toString()));
        }
        return project;
    }
}
