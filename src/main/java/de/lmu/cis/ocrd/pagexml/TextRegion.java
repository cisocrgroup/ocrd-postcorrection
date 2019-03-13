package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.util.Normalizer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

	public TextRegion withID(String id) {
		((Element)node).setAttribute("id", id);
		return this;
	}

	public Coordinates getCoordinates() throws Exception {
		return Coordinates.fromString(
				((Element)XPathHelper.getNode(node, "./Coords")).getAttribute("points")
		);
	}

	public TextRegion withCoordinates(Coordinates coordinates) {
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

	public List<String> getUnicode() {
		List<String> stringList = new ArrayList<>();
		for (Node n : XPathHelper.getNodes(this.node, "./TextEquiv/Unicode")) {
			if (n != null && n.getFirstChild() != null) {
				String c = n.getFirstChild().getTextContent();
				if (c != null) {
					stringList.add(c);
				}
			}
		}
		return stringList;
	}

	public List<String> getUnicodeNormalized() {
		final List<String> unicode = this.getUnicode();
		for (int i = 0; i < unicode.size(); i++) {
			unicode.set(i, Normalizer.normalize(unicode.get(i)));
		}
		return unicode;
	}
}
