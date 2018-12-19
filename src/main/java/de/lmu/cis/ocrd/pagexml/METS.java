package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class METS {
	public static METS open(Path path) throws Exception {
		java.io.File file = path.toFile();
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
		                                                .newDocumentBuilder();
		return new METS(builder.parse(file));
	}

	private static final String fileGrpFmt = "/mets/fileSec/fileGrp[@USE='%s']";
	private static final String fileXPATH = "./file";
	private static final String flocatXPATH = "./FLocat";

	private final Document xml;

	public METS(Document doc) {
		this.xml = doc;
	}

	public List<File> findFileGrpFiles(String use) {
		try {
			final String xpath = String.format(fileGrpFmt, use);
			List<File> files = new ArrayList<>();
			for (Node fg : XPathHelper.getNodes(xml, xpath)) {
				for (Node f : XPathHelper.getNodes(fg, fileXPATH)) {
					files.add(new File(f));
				}
			}
			return files;
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	public static class File {
		private final Node node;
		private File(Node node) {
			this.node = node;
		}

		public String getID() {
			return XPathHelper.getAttribute(node, "ID");
		}

		public String getMIMEType() {
			return XPathHelper.getAttribute(node, "MIMETYPE");
		}

		public String getGroupID() {
			return XPathHelper.getAttribute(node, "GROUPID");
		}

		public String getFLocat() {
			try {
				final Node flocat = XPathHelper.getNode(node, flocatXPATH);
				if (flocat == null) {
					throw new RuntimeException("cannot find FLocat");
				}
				return XPathHelper.getAttribute(flocat, "xlink:href");
			} catch (XPathExpressionException e) {
				throw new RuntimeException(e);
			}
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
	}
}
