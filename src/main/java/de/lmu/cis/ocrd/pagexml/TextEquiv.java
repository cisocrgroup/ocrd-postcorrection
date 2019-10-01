package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.util.Normalizer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TextEquiv {
	private final Node node;

	TextEquiv(Node node) {
		this.node = node;
	}

	public TextRegion getParentTextRegion() {
		return new TextRegion(node.getParentNode());
	}

	public int getIndex() {
		return Integer.parseInt(XPathHelper.getAttribute(node, "index").orElse("0"));
	}

	public double getConfidence() {
		return Double.parseDouble(XPathHelper.getAttribute(node, "conf").orElse("0.0"));
	}

	public String getDataType() {
		return XPathHelper.getAttribute(node, "dataType").orElse("");
	}

	public String getDataTypeDetails() {
		return XPathHelper.getAttribute(node, "dataTypeDetails").orElse("");
	}

	public String getUnicode() {
		final Node n = XPathHelper.getNode(node, "./Unicode");
		if (n == null || n.getFirstChild() == null || n.getFirstChild().getTextContent() == null) {
			return "";
		}
		return n.getFirstChild().getTextContent();
	}

	String getUnicodeNormalized() {
		return Normalizer.normalize(getUnicode());
	}

	public TextEquiv withIndex(int i) {
        return setAttribute("index", Integer.toString(i));
    }

    public TextEquiv withConfidence(double confidence) {
        return setAttribute("conf", Double.toString(confidence));
    }

    public TextEquiv withDataType(String dataType) {
        return setAttribute("dataType", dataType);
    }

    public TextEquiv withDataTypeDetails(String dataTypeDetails) {
	    return setAttribute("dataTypeDetails", dataTypeDetails);
    }

	public TextEquiv addUnicode(String unicode) {
		final Node u = node.getOwnerDocument().createTextNode(unicode);
		node.appendChild(u);
		return this;
	}

	private TextEquiv setAttribute(String key, String value) {
		((Element) node).setAttribute(key, value);
		return this;
	}

    Node getNode() {
	    return node;
    }
}
