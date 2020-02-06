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
	private final int gtIndex;
	private final int id;

	public BaseOCRToken(int id, Node node, List<String> linesNormalized, int gtIndex) throws Exception {
		// master ocr character confidences
		NodeList nodes = (NodeList) XPathHelper.CHILD_GLYPH_TEXT_EQUIV.evaluate(node, XPathConstants.NODESET);
		final List<Double> mConfs = new ArrayList<>(nodes.getLength());
		for (int i = 0; i < nodes.getLength(); i++) {
			mConfs.add(Double.parseDouble(nodes.item(i).getAttributes().getNamedItem("conf").getNodeValue()));
		}
		// words
		nodes = (NodeList) XPathHelper.CHILD_TEXT_EQUIV.evaluate(node, XPathConstants.NODESET);
		final List<OCRWord> words = new ArrayList<>(nodes.getLength());
		for (int i = 0; i < nodes.getLength() && i <= gtIndex && i < linesNormalized.size(); i++) {
			words.add(new OCRWord(nodes.item(i), linesNormalized.get(i), mConfs));
		}
		if (words.size() < (gtIndex - 1)) {
			throw new Exception("not enough aligned words for nOCR = " + gtIndex + ": " + words.size());
		}
		this.node = node;
		this.words = words;
		this.gtIndex = gtIndex;
		this.id = id;
	}

	@Override
	public String getID() {
		return Integer.toString(id);
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
		new TextRegion(node).prependNewTextEquiv()
				.addUnicode(new StringCorrector(getMasterOCR().getWordRaw()).correctWith(correction))
				.withConfidence(confidence)
				.withIndex(0)
				.withDataType("OCR-D-CIS-POST-CORRECTION")
				.withDataTypeDetails(getMasterOCR().getWordRaw());
	}
}
