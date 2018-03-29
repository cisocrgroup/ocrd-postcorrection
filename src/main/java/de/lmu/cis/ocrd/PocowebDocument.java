package de.lmu.cis.ocrd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.lmu.cis.api.model.Book;
import de.lmu.cis.api.model.Page;
import de.lmu.cis.api.model.Project;
import de.lmu.cis.pocoweb.Client;

public class PocowebDocument implements Document {
	private final Project project;
	private final Client client;

	public PocowebDocument(Project project, Client client) {
		this.client = client;
		this.project = project;
	}

	@Override
	public void eachLine(Document.Visitor v) throws Exception {
		List<OCRLine> lines = new ArrayList<OCRLine>();
		// System.out.println("size: " + project.getBooks().size());
		boolean isMasterOCR = true;
		for (Book book : project.getBooks()) {
			// System.out.println("engine: " + book.getOcrEngine() +
			// ", size: " + book.getPageIds().size() +
			// ",user: " + book.getOcrUser());
			int pseq = 1;
			for (int pageId : book.getPageIds()) {
				Page page = client.getPage(book.getProjectId(), pageId);
				for (de.lmu.cis.api.model.Line line : page.getLines()) {
					lines.add(new OCRLine(book.getOcrEngine(), new PocowebLine(client, line), pseq, isMasterOCR));
				}
				pseq++;
			}
			isMasterOCR = false;
		}
		Collections.sort(lines);
		for (OCRLine line : lines) {
			v.visit(line);
		}
	}

}
