package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.util.Normalizer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathExpressionException;

public class TextEquiv {
	private final Node node;

	public TextEquiv(Node node) {
		this.node = node;
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
		try {
			final Node n = XPathHelper.getNode(node, "./Unicode");
			if (n == null || n.getFirstChild() == null || n.getFirstChild().getTextContent() == null) {
				return "";
			}
			return n.getFirstChild().getTextContent();
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	public String getUnicodeNormalized() {
		return Normalizer.normalize(getUnicode());
	}

	public TextEquiv withIndex(int i) {
        return setAttribute("index", new Integer(i).toString());
    }

    public TextEquiv withConfidence(double confidence) {
        return setAttribute("conf", new Double(confidence).toString());
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

	public static TextEquiv create(Document document) {
		final Node teNode = document.createElement("TextEquiv");
		return new TextEquiv(teNode);
	}

	private TextEquiv setAttribute(String key, String value) {
        ((Element) node).setAttribute(key, value);
        return this;
    }

    Node getNode() {
	    return node;
    }
}
