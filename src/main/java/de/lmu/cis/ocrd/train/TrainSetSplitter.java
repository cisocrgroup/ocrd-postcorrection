package de.lmu.cis.ocrd.train;

import de.lmu.cis.ocrd.ml.Token;

public class TrainSetSplitter {
    public interface Visitor {
        void visit(Token token, boolean isTrain) throws Exception;
    }
    private final int n;

    public TrainSetSplitter(int n) {
        this.n = n;
    }

    public void eachToken(Tokenizer t, Visitor v) throws Exception {
        Counter c = new Counter(n);
        t.eachToken((token)->{
            c.inc(token);
            v.visit(token, c.isTrain());
        });
    }

    private class Counter {
        private final int n;
        private int i, lastPageID;
        Counter(int n) {
            this.i = 0;
            this.n = n;
            this.lastPageID = -1;
        }

        void inc(Token token) {
            if (token.getMasterOCR().getLine().getPageId() != lastPageID) {
                i++;
                lastPageID = token.getMasterOCR().getLine().getPageId();
            }
        }

        boolean isTrain() {
            return ((i+1) % n) == 0;
        }
    }
}
