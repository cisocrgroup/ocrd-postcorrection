package de.lmu.cis.iba;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import com.google.gson.Gson;

import de.lmu.cis.api.model.Book;
import de.lmu.cis.api.model.Project;
import de.lmu.cis.ocrd.Config;
import de.lmu.cis.ocrd.OCRLine;
import de.lmu.cis.ocrd.PocowebDocument;
import de.lmu.cis.ocrd.align.Graph;
import de.lmu.cis.pocoweb.Client;

class Main {
	private static final int N = 3;

	static ArrayList<Node> sinks = new ArrayList();

	public static void main(String[] args) {
		try (Client client = Client.login(Config.getInstance().getPocowebURL(), Config.getInstance().getPocowebUser(),
				Config.getInstance().getPocowebPass());) {
			Book book = new Book().withOcrEngine("abbyy").withOcrUser("test-ocr-user").withAuthor("Grenzboten")
					.withTitle("Die Grenzboten").withYear(1841);
			Project project;
			try (InputStream is = new FileInputStream("src/test/resources/1841-DieGrenzboten-abbyy-small.zip");) {
				project = client.newProject(book, is);
			}
			book = new Book().withOcrEngine("tesseract").withOcrUser("test-ocr-user").withAuthor("Grenzboten")
					.withTitle("Die Grenzboten").withYear(1841);
			try (InputStream is = new FileInputStream(
					"src/test/resources/1841-DieGrenzboten-tesseract-small-with-error.zip");) {
				project = client.addBook(project, book, is);
			}
			book = new Book().withOcrEngine("ocropus").withOcrUser("test-ocr-user").withAuthor("Grenzboten")
					.withTitle("Die Grenzboten").withYear(1841);
			try (InputStream is = new FileInputStream("src/test/resources/1841-DieGrenzboten-ocropus-small.zip");) {
				project = client.addBook(project, book, is);
			}
			PocowebDocument doc = new PocowebDocument(project, client);

			LineAlignment l_alignment = new LineAlignment(doc, 3);
			for (ArrayList<OCRLine> aligned_lines : l_alignment) {
				for (OCRLine line : aligned_lines) {
					System.out.println(line);
				}
			}
			for (ArrayList<OCRLine> aligned_lines : l_alignment) {
				OCRLine mocr = null;
				ArrayList<OCRLine> other = new ArrayList<OCRLine>();
				for (OCRLine line : aligned_lines) {
					if (line.isMasterOCR) {
						mocr = line;
					} else {
						other.add(line);
					}
				}
				if (mocr == null) {
					throw new Exception("no master ocr");
				}
				for (OCRLine line : other) {
					LCS_Alignment_Pairwise alignment = new LCS_Alignment_Pairwise(mocr.line.getNormalized(),
							line.line.getNormalized());
					alignment.align();
					ArrayList<LCS_Alignment_Pairwise.AlignmentPair> as = alignment.getAligmentPairs();
					System.out.println(new Gson().toJson(as));
					if (as.size() > 2) {
						new Graph(alignment, mocr.line.getNormalized(), line.line.getNormalized());
						// System.out.println(g.getStartNode().toDot());
						return;
					}
					// System.out.println(mocr);
					// System.out.println(line);
					// System.out.println(alignment.LCS_to_JSONString());
				}
			}
			// alignment.align();
			// String json = alignment.LCS_to_JSONString();
			// System.out.println(json);

			client.deleteProject(project);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error: " + e);
		}
	}

	private static String patternsToString(String[] patterns) {
		String prefix = "[";
		String res = "";
		if (patterns == null || patterns.length == 0) {
			res += "[]";
		} else {
			for (String p : patterns) {
				res += prefix + p;
				prefix = ",";
			}
			res += "]";
		}
		return res;
	}
}
