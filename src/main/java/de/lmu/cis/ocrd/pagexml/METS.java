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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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

	private METS(String path, Document doc) {
		this.path = Paths.get(path);
		this.xml = doc;
	}

	public void save(Path path) throws Exception {
		save(path.toFile());
	}

	public void save(java.io.File file) throws Exception {
		if (!file.createNewFile()) {
			if (!file.delete()) {
				throw new Exception("cannot overwrite file: " + file.getPath());
			}
		}
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(xml);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}

	public List<METS.File> findFileGrpFiles(String use) {
		List<METS.File> files = new ArrayList<>();
		for (Node fg: XPathHelper.getNodes(xml, String.format(fileGrpFmt, use))) {
			for (Node f : XPathHelper.getNodes(fg, file)) {
				files.add(new METS.File(path.getParent(), f));
			}
		}
		return files;
	}

	public METS.File addFileToFileGrp(String use) {
		Node fileGrp = XPathHelper.getNode(xml, String.format(fileGrpFmt, use));
		if (fileGrp == null) {
			fileGrp = xml.createElement("mets:fileGrp");
			((Element) fileGrp).setAttribute("USE", use);
			Node fileSec = XPathHelper.getNode(xml, METS.fileSec);
			fileSec.appendChild(fileGrp);
		}
		Node file = xml.createElement("mets:file");
		fileGrp.appendChild(file);
		return new METS.File(path.getParent(), file);
	}

	public FPtr addFPtr(METS.File file) {
		final Node fptr = file.findFPtr();
		if (fptr == null) {
			return null;
		}
		Element newNode = fptr.getOwnerDocument().createElement("mets:fptr");
		fptr.getParentNode().appendChild(newNode);
		return new FPtr(newNode);
	}

	public static class File {
		private final Node node;
		private final Path workspace;
		private File(Path workspace, Node node) {
			this.workspace = workspace;
			this.node = node;
			setAttribute("LOCTYPE", "OTHER").setAttribute("OTHERLOCTYPE", "FILE");
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

		public String getLocType() { return XPathHelper.getAttribute(node, "LOCTYPE").orElse("OTHER");}

		public String getOtherLocType() { return XPathHelper.getAttribute(node, "OTHERLOCTYPE").orElse("FILE");}

		public Node findFPtr() {
			final String xpath = String.format("/mets/structMap/div/div/fptr[@FILEID='%s']", getID());
			return XPathHelper.getNode(node.getOwnerDocument(), xpath);
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
			if (path.isAbsolute()) {
				Logger.debug("opening absolute file path {}", path.toString());
				return new FileInputStream(path.toFile());
			}
			// relative paths are assumed to be relative to the mets file.
			final Path relative = Paths.get(workspace.toString(), path.toString());
			Logger.debug("opening relative file path {}", relative.toString());
			return new FileInputStream(relative.toFile());
		}

		private File setAttribute(String key, String value) {
			((Element) node).setAttribute(key, value);
			return this;
		}
	}

	public static class FPtr {
		private final Node node;
		public FPtr(Node node) {
			this.node = node;
		}

		public FPtr withFileID(String fileID) {
			((Element) node).setAttribute("FILEID", fileID);
			return this;
		}

		public String getFileID() {
			return ((Element) node).getAttribute("FILEID");
		}
	}
}
