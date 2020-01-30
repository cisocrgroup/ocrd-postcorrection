package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class BaseOCRToken implements de.lmu.cis.ocrd.ml.BaseOCRToken {

	// private final Word word;
	private final List<OCRWord> words;
	private final int gtIndex;

	public BaseOCRToken(Word word, int gtIndex) throws Exception {
		this.gtIndex = gtIndex;
		// this.word = word;
		this.words = getWords(word, gtIndex);
	}

	private static List<OCRWord> getWords(Word word, int gtIndex) throws Exception {
		List<OCRWord> words = new ArrayList<>();
		final List<TextEquiv> tes = word.getTextEquivs();
		if (tes.isEmpty()) {
			throw new Exception("empty word");
		}
//		final List<Double> mConfs = new ArrayList<>();
//		for (Glyph g : word.getGlyphs()) {
//			final List<TextEquiv> gtes = g.getTextEquivs();
//			if (gtes == null || gtes.isEmpty()) {
//				mConfs.add(0.0);
//			} else {
//				mConfs.add(gtes.get(0).getConfidence());
//			}
//		}
		final List<Double> mConfs = word.getCharConfidences();
		if (mConfs.isEmpty()) {
			throw new Exception("empty character confidences");
		}
		final List<String> normLines = word.getParentLine().getUnicodeNormalized();
		for (int i = 0; i <= gtIndex && i < tes.size() && i < normLines.size(); i++) {
			words.add(new OCRWord(tes.get(i), normLines.get(i), mConfs));
		}
		if (words.size() < gtIndex) { // 0, ..., gtIndex-1, gtIndex
			throw new Exception("missing words with gtIndex = " + gtIndex);
		}
		return words;
	}



	public BaseOCRToken(Node node, String line, int gtIndex) throws XPathExpressionException {
		// master ocr character confidences
		NodeList nodes = (NodeList) XPathHelper.CHILD_GLYPH_TEXT_EQUIV.evaluate(node, XPathConstants.NODESET);
		final List<Double> mConfs = new ArrayList<>(nodes.getLength());
		for (int i = 0; i < nodes.getLength(); i++) {
			mConfs.add(Double.parseDouble(nodes.item(i).getAttributes().getNamedItem("conf").getNodeValue()));
		}
		// words
		nodes = (NodeList) XPathHelper.CHILD_TEXT_EQUIV.evaluate(node, XPathConstants.NODESET);
		final List<OCRWord> words = new ArrayList<>(nodes.getLength());
		for (int i = 0; i < nodes.getLength(); i++) {
			words.add(new OCRWord(new TextEquiv(nodes.item(i)), line, mConfs));
		}
		this.gtIndex = gtIndex;
		this.words = words;
	}

	@Override
	public int getNOCR() {
		return gtIndex;
	}

	@Override
	public de.lmu.cis.ocrd.ml.OCRWord getMasterOCR() {
		if (0 < words.size()) {
			return words.get(0);
		}
		return EmptyWord.instance;
	}

	@Override
	public de.lmu.cis.ocrd.ml.OCRWord getSlaveOCR(int i) {
		if (i+1 < words.size()) {
			return words.get(i+1);
		}
		return EmptyWord.instance;
	}

	@Override
	public Optional<String> getGT() {
		if (gtIndex <= 0 || gtIndex >= words.size()) {
			return Optional.empty();
		}
		return Optional.of(words.get(gtIndex).getWordNormalized());
	}

	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(",");
		sj.add("mOCR:" + getMasterOCR().toString());
		for (int i = 1; i < getNOCR(); i++) {
			sj.add("OCR" + (i + 1) + ":" + getSlaveOCR(i - 1).toString());
		}
		if (getGT().isPresent()) {
			sj.add("gt:" + getGT().get());
		}
		return sj.toString();
	}

	@Override
	public void correct(String correction, double confidence) {
		throw new RuntimeException("correct: not implemented");
//		word.prependNewTextEquiv()
//				.addUnicode(new StringCorrector(getMasterOCR().getWordRaw()).correctWith(correction))
//				.withConfidence(confidence)
//				.withIndex(0)
//				.withDataType("OCR-D-CIS-POST-CORRECTION")
//				.withDataTypeDetails(getMasterOCR().getWordRaw());
	}
}
