package de.lmu.cis.ocrd.calamari;

class Data {
    double[] PrimaryCharConfs;
    double PrimaryConf;
    double SecondaryConf;
    String PrimaryOCR;
    String SecondaryOCR;
    String GT;
    String PrimaryLine;
    String SecondaryLine;
    Alternative[] Alternatives;

    static class Alternative {
        String Token;
        double Conf;
    }
}
