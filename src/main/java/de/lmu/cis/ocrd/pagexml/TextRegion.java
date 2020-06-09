package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.util.Normalizer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

public class TextRegion {
	protected final Node node;

	protected TextRegion(Node node) {
		this.node = node;
	}

	public String getID() {
		return ((Element)node).getAttribute("id");
	}

	TextRegion withID(String id) {
		((Element)node).setAttribute("id", id);
		return this;
	}

	Coordinates getCoordinates() throws Exception {
		return Coordinates.fromString(
				((Element)XPathHelper.getNode(node, "./Coords")).getAttribute("points")
		);
	}

	TextRegion withCoordinates(Coordinates coordinates) {
		((Element)XPathHelper.getNode(node, "./Coords")).setAttribute("points", coordinates.toString());
		return this;
	}

	public List<TextEquiv> getTextEquivs() {
		final List<TextEquiv> tes = new ArrayList<>();
		for (Node n : XPathHelper.getNodes(this.node, "./TextEquiv")) {
			tes.add(new TextEquiv(n));
		}
		return tes;
	}

	public TextEquiv appendNewTextEquiv() {
		final TextEquiv te = new TextEquiv(node.getOwnerDocument().createElement("TextEquiv"));
		node.appendChild(te.getNode());
		return te;
	}

	TextEquiv prependNewTextEquiv() {
		final TextEquiv te = new TextEquiv(node.getOwnerDocument().createElement("TextEquiv"));
		node.insertBefore(te.getNode(), node.getFirstChild());
		return te;
	}

	public List<String> getUnicode() throws XPathExpressionException {
		List<String> stringList = new ArrayList<>();
		NodeList nodes = (NodeList) XPathHelper.CHILD_TEXT_EQUIV_UNICODE.evaluate(node, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i) != null && nodes.item(i).getFirstChild() != null) {
				String c = nodes.item(i).getFirstChild().getTextContent();
				if (c != null) {
					stringList.add(c);
				}
			}
		}
		return stringList;
	}

	public List<String> getUnicodeNormalized() throws XPathExpressionException {
		final List<String> unicode = this.getUnicode();
		for (int i = 0; i < unicode.size(); i++) {
			unicode.set(i, Normalizer.normalize(unicode.get(i)));
		}
		return unicode;
	}

	void incrementTextEquivIndices() {
		for (TextEquiv textEquiv: getTextEquivs()) {
			final int index = textEquiv.getIndex();
			if (index > 0) { // increment indices bigger than 0
				textEquiv.withIndex(index + 1);
			}
		}
	}


	public Node getNode() {
		return node;
	}
}
