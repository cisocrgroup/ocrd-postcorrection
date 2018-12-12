package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Node;

public class TextEquiv {
	private final Node node;


	public TextEquiv(Node node) {
		this.node = node;
	}

	public int getIndex() {
		return Integer.parseInt(XPathHelper.getAttribute(node, "index"));
	}

	public float getConfidence() {
		return Float.parseFloat(XPathHelper.getAttribute(node, "conf"));
	}

	public String getDataType() {
		return XPathHelper.getAttribute(node, "dataType");
	}

	public String getDataTypeDetails() {
		return XPathHelper.getAttribute(node, "dataTypeDetails");
	}
}
