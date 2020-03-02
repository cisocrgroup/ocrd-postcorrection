package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.List;
import java.util.StringJoiner;

public class OCRWord implements de.lmu.cis.ocrd.ml.OCRWord {
	private final String word, raw;
	private final List<Double> masterOCRCharConfidences;
	private final double wordConfidence;
	private final String line;
	private final boolean isGT;

	private OCRWord(TextEquiv te, String line, List<Double> masterOCRCharConfidences) throws XPathExpressionException {
		this.line = line;
		this.wordConfidence = te.getConfidence();
		this.word = te.getUnicodeNormalized();
		this.raw = te.getUnicode();
		this.masterOCRCharConfidences = masterOCRCharConfidences;
		this.isGT = isGT(te);
	}

	OCRWord(Node node, String line, List<Double> mConfs) throws XPathExpressionException {
		this(new TextEquiv(node), line, mConfs);
	}

	boolean isGT() {
		return isGT;
	}

	private static boolean isGT(TextEquiv te) {
		// TODO: data type details should contain embedded json e.g. `{"gt": true, "name": "ground truth", ...}`
		return te.getDataTypeDetails().startsWith("OCR-D-GT");
	}

	@Override
	public String getLineNormalized() {
		return line;
	}

	@Override
	public double getConfidence() {
		return wordConfidence;
	}

	@Override
	public double getCharacterConfidenceAt(int i) {
		if (i < masterOCRCharConfidences.size()) {
			return masterOCRCharConfidences.get(i);
		}
		return 0.0;
	}

	@Override
	public String getWordRaw() {
		return raw;
	}

	@Override
	public String getWordNormalized() {
		return word;
	}

	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(",");
		for (int i = 0; i < word.length(); i++) {
			sj.add(Double.toString(getCharacterConfidenceAt(i)));
		}
		return getWordNormalized() + "|" + sj.toString();
	}

}
