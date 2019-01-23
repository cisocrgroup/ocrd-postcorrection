package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class METS {
	public static METS open(java.io.File file) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		return new METS(builder.parse(file));
	}

	public static METS open(Path path) throws Exception {
		return open(path.toFile());
	}

	private static final String fileGrpFmt = "/mets/fileSec/fileGrp[@USE='%s']";
	private static final String fileSec = "/mets/fileSec";
	private static final String file = "./file";
	private static final String flocatXPATH = "./FLocat";

	private final Document xml;

	public METS(Document doc) {
		this.xml = doc;
	}

	public void save(Path path) throws Exception {
		save(path.toFile());
	}

	public void save(java.io.File file) throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(xml);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}

	public List<File> findFileGrpFiles(String use) {
		List<File> files = new ArrayList<>();
		for (Node fg: XPathHelper.getNodes(xml, String.format(fileGrpFmt, use))) {
			for (Node f : XPathHelper.getNodes(fg, file)) {
				files.add(new File(f));
			}
		}
		return files;
	}

	public File addFileToFileGrp(String use) {
		Node fileGrp = XPathHelper.getNode(xml, String.format(fileGrpFmt, use));
		if (fileGrp == null) {
			fileGrp = xml.createElement("mets:fileGrp");
			((Element) fileGrp).setAttribute("USE", use);
			Node fileSec = XPathHelper.getNode(xml, METS.fileSec);
			fileSec.appendChild(fileGrp);
		}
		Node file = xml.createElement("mets:file");
		fileGrp.appendChild(file);
		return new File(file);
	}

	public static class File {
		private final Node node;
		private File(Node node) {
			this.node = node;
		}

		public File withID(String id) {
			return setAttribute("ID", id);
		}

		public File withMIMEType(String mimeType) {
			return setAttribute("MIMETYPE", mimeType);
		}

		public File withGroupID(String groupID) {
			return setAttribute("GROUPID", groupID);
		}

		public File withFLocat(String flocat) {
			Element flocatNode = node.getOwnerDocument().createElement("mets:FLocat");
			flocatNode.setAttribute("xlink:href", flocat);
			node.appendChild(flocatNode);
			return this;
		}

		public String getID() {
			return XPathHelper.getAttribute(node, "ID").orElse("");
		}

		public String getMIMEType() {
			return XPathHelper.getAttribute(node, "MIMETYPE").orElse("");
		}

		public String getGroupID() {
			return XPathHelper.getAttribute(node, "GROUPID").orElse("");
		}

		public String getFLocat() {
			final Node flocat = XPathHelper.getNode(node, flocatXPATH);
			if (flocat == null) {
				throw new RuntimeException("cannot find FLocat");
			}
			return XPathHelper.getAttribute(flocat, "xlink:href").orElse("");
		}

		public InputStream open() throws Exception {
			URL url;
			try {
				url = new URL(getFLocat());
			} catch (MalformedURLException e) {
				return new FileInputStream(getFLocat());
			}
			if ("file".equals(url.getProtocol())) {
				return new FileInputStream(url.getPath());
			}
			return url.openStream();
		}

		private File setAttribute(String key, String value) {
			((Element) node).setAttribute(key, value);
			return this;
		}
	}
}
