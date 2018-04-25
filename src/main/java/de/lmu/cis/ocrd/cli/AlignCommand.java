package de.lmu.cis.ocrd.cli;

import java.util.ArrayList;

import org.pmw.tinylog.Logger;

import de.lmu.cis.iba.LineAlignment;
import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.OCRLine;
import de.lmu.cis.ocrd.Project;
import de.lmu.cis.ocrd.align.Graph;
import de.lmu.cis.ocrd.archive.ZipArchive;
import de.lmu.cis.ocrd.parsers.ABBYYXMLFileType;
import de.lmu.cis.ocrd.parsers.ABBYYXMLParserFactory;
import de.lmu.cis.ocrd.parsers.ArchiveParser;
import de.lmu.cis.ocrd.parsers.OcropusArchiveParser;

class AlignCommand implements Command {

	@Override
	public void execute(Configuration config) throws Exception {
		final String[] args = config.getArgs();
		if (args == null || args.length != 2) {
			throw new Exception("expected exactly two arguments: gt-archive ocr-archive");
		}
		Document gt = new ArchiveParser(new ABBYYXMLParserFactory(), new ABBYYXMLFileType(), new ZipArchive(args[0]))
				.parse();
		Document ocr = new OcropusArchiveParser(new ZipArchive(args[1])).parse();
		align(new Project().put("abbyy", gt).put("ocropus", ocr));
	}

	private void align(Document doc) throws Exception {
		Logger.info("aligning lines ...");
		LineAlignment lalignment = new LineAlignment(doc, 2);
		Logger.info("done aligning lines");
		int i = 0;
		Logger.info("iterating ...");
		for (ArrayList<OCRLine> lines : lalignment) {
			System.out
					.println(++i + ": " + lines.get(0).line.getNormalized() + "|" + lines.get(1).line.getNormalized());
			new Graph(lines.get(0).line.getNormalized(), lines.get(1).line.getNormalized()).getTokenizer()
					.eachPair((a, b) -> {
						System.out.println(a + "|" + b);
					});
		}
		Logger.info("done iterating");
	}
}
