package de.lmu.cis.ocrd.pagexml;

import org.pmw.tinylog.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class METS {
	public static METS open(java.io.File file) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		return new METS(file.getPath(), builder.parse(file));
	}

	public static METS open(Path path) throws Exception {
		return open(path.toFile());
	}

	private static final String fileGrpFmt = "/mets/fileSec/fileGrp[@USE='%s']";
	private static final String fileSec = "/mets/fileSec";
	private static final String file = "./file";
	private static final String flocatXPATH = "./FLocat";

	private final Document xml;
	private final Path path;

	public METS(String path, Document doc) {
		this.path = Paths.get(path);
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
				files.add(new File(path.getParent(), f));
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
		return new File(path.getParent(), file);
	}

	public static class File {
		private final Node node;
		private final Path workspace;
		private File(Path workspace, Node node) {
			this.workspace = workspace;
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

		public InputStream openInputStream() throws Exception {
			URL url;
			final String flocat = getFLocat();
			Logger.info("flocat {}", flocat);
			try {
				url = new URL(flocat);
			} catch (MalformedURLException e) {
				return openLocalPath(Paths.get(flocat));
			}
			if ("file".equals(url.getProtocol())) {
				return openLocalPath(Paths.get(url.getPath()));
			}
			return url.openStream();
		}

		private InputStream openLocalPath(Path path) throws FileNotFoundException {
			Logger.info("opening flocat {}", path.toString());
			if (path.isAbsolute()) {
				return new FileInputStream(path.toFile());
			}
			// relative paths are assumed to be relative to the mets file.
			return new FileInputStream(Paths.get(workspace.toString(), path.toString()).toFile());
		}

		private File setAttribute(String key, String value) {
			((Element) node).setAttribute(key, value);
			return this;
		}
	}
}
