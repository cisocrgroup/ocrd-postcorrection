package de.lmu.cis.ocrd.pagexml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import de.lmu.cis.ocrd.NormalizerTransducer;

public class TextRegion {
	protected final Node node;

	protected TextRegion(Node node) {
		this.node = node;
	}

	public String getID() {
		return getAttributeValue("id");
	}

	public String getAttributeValue(String name) {
		return this.node.getAttributes().getNamedItem(name).getTextContent();
	}

	public List<String> getUnicode() {
		try {
			List<String> stringList = new ArrayList<>();
			for (Node n : XPathHelper.getNodes(this.node,
					"./TextEquiv/Unicode")) {
				stringList.add(n.getFirstChild().getTextContent());
			}
			return stringList;
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getUnicodeNormalized() {
		final List<String> unicode = this.getUnicode();
		for (int i = 0; i < unicode.size(); i++) {
			unicode.set(i, NormalizerTransducer.normalize(unicode.get(i)));
		}
		return unicode;
	}
}
