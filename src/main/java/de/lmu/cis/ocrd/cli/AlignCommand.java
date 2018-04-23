package de.lmu.cis.ocrd.cli;

import java.util.ArrayList;

import de.lmu.cis.iba.LineAlignment;
import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.OCRLine;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.parsers.ABBYYXMLFileType;
import de.lmu.cis.ocrd.parsers.ABBYYXMLParserFactory;
import de.lmu.cis.ocrd.parsers.ArchiveParser;
import de.lmu.cis.ocrd.parsers.OcropusArchiveParser;

class AlignCommand implements Command {

	private class Doc implements Document {
		private final Document gt, ocr;

		public Doc(Document gt, Document ocr) {
			this.gt = gt;
			this.ocr = ocr;
		}

		@Override
		public void eachLine(Visitor v) throws Exception {
			this.gt.eachLine(v);
			this.ocr.eachLine(v);
		}
	}

	private void align(Document doc) throws Exception {
		LineAlignment lalignment = new LineAlignment(doc, 2);
		int i = 0;
		for (ArrayList<OCRLine> lines : lalignment) {
			System.out.println(++i + ": " + lines.get(0) + " <-> " + lines.get(1));
		}
	}

	@Override
	public void execute(Configuration config) throws Exception {
		final String[] args = config.getCommandLine().getArgs();
		if (args == null || args.length != 2) {
			throw new Exception("expected exactly two arguments: gt-archive ocr-archive");
		}
		Document gt = new ArchiveParser(new ABBYYXMLParserFactory(), new ABBYYXMLFileType(), new ZipArchive(args[0]))
				.parse();
		Document ocr = new OcropusArchiveParser(new ZipArchive(args[1])).parse();
		align(new Doc(gt, ocr));
	}
}
