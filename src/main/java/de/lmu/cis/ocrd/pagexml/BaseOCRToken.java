package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.util.StringCorrector;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class BaseOCRToken implements de.lmu.cis.ocrd.ml.BaseOCRToken {

	private final Node node;
	private final List<OCRWord> words;
	private final int id;

	public BaseOCRToken(int id, Node node, List<String> linesNormalized) throws Exception {
		// master ocr character confidences
		NodeList nodes = (NodeList) XPathHelper.CHILD_GLYPH_TEXT_EQUIV.evaluate(node, XPathConstants.NODESET);
		final List<Double> mConfs = new ArrayList<>(nodes.getLength());
		for (int i = 0; i < nodes.getLength(); i++) {
			mConfs.add(Double.parseDouble(nodes.item(i).getAttributes().getNamedItem("conf").getNodeValue()));
		}
		// words
		nodes = (NodeList) XPathHelper.CHILD_TEXT_EQUIV.evaluate(node, XPathConstants.NODESET);
		final List<OCRWord> words = new ArrayList<>(nodes.getLength());
		for (int i = 0; i < nodes.getLength() && i < linesNormalized.size(); i++) {
			words.add(new OCRWord(nodes.item(i), linesNormalized.get(i), mConfs));
		}
		if (words.isEmpty()) {
			throw new Exception("too few text equivs for ocr token");
		}
		this.node = node;
		this.words = words;
		this.id = id;
	}

	@Override
	public String getID() {
		return Integer.toString(id);
	}

	@Override
	public int getNOCR() {
		if (getLastWord().isGT()) {
			return words.size() - 1; // last word is ground truth
		}
		// no ground truth -- all words are ocr tokens
		return words.size();
	}

	private OCRWord getLastWord() {
		return words.get(words.size() - 1);
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
		if (getLastWord().isGT()) {
			return Optional.of(getLastWord().getWordNormalized());
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(",");
		sj.add("id:" + getID());
		sj.add("mOCR:" + getMasterOCR().getWordNormalized());
		for (int i = 1; i < words.size(); i++) {
		    if (words.get(i).isGT()) {
                sj.add("GT:" + words.get(i).getWordNormalized());
            } else {
		    	sj.add("OCR:" + (i+1) + ":" + words.get(i).getWordNormalized());
            }
		}
		return sj.toString();
	}

	@Override
	public void correct(String correction, double confidence) {
		new TextRegion(node).prependNewTextEquiv()
				.addUnicode(new StringCorrector(getMasterOCR().getWordRaw()).correctWith(correction))
				.withConfidence(confidence)
				.withIndex(0)
				.withDataType("OCR-D-CIS-POST-CORRECTION")
				.withDataTypeDetails(getMasterOCR().getWordRaw());
	}
}
