import java.util.ArrayList;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.OCRLine;

public class TestDocument implements Document {

	private final ArrayList<OCRLine> lines;

	public TestDocument() {
		lines = new ArrayList<OCRLine>();
	}
	
	public TestDocument withLine(String line, int id, String ocr, boolean masterOCR) {
		lines.add(new OCRLine(ocr, new TestLine(id, line), 0, masterOCR));
		return this;
	}
	
	public int getLength() {
		return this.lines.size();
	}
	
	@Override
	public void eachLine(Visitor v) throws Exception {
		for (OCRLine line: lines) {
			v.visit(line);
		}
	}
}
