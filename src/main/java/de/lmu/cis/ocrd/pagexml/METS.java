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
	private static final String fileGrpAll = "/mets/fileSec/fileGrp";
	private static final String fileXPATH = "./file";
	private static final String flocatXPATH = "./FLocat";

	private final Document xml;
	private final List<Node> fileGrps;

	public METS(Document doc) {
		this.xml = doc;
		this.fileGrps = getFileGrps();
	}

	public List<File> findFileGrpFiles(String use) {
		List<File> files = new ArrayList<>();
		for (Node f: getFileGrps(use)) {
			files.add(new File(f));
		}
		return files;
	}

	public FileGrp addFileGrp(String use) {
		return new FileGrp(xml);
	}

	private List<Node> getFileGrps(String use) {
		try {
			return XPathHelper.getNodes(xml, String.format(fileGrpFmt, use));
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	private List<Node> getFileGrps() {
		try {
			return XPathHelper.getNodes(xml, fileGrpAll);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	public static class FileGrp {
		private final Node node;
		FileGrp(Node node) {
			this.node = node;
		}
	}

	public static class File {
		private final Node node;
		private File(Node node) {
			this.node = node;
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
			try {
				final Node flocat = XPathHelper.getNode(node, flocatXPATH);
				if (flocat == null) {
					throw new RuntimeException("cannot find FLocat");
				}
				return XPathHelper.getAttribute(flocat, "xlink:href").orElse("");
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
