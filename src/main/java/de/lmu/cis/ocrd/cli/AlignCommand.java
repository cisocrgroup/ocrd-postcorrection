package de.lmu.cis.ocrd.cli;

import de.lmu.cis.iba.LineAlignment;
import de.lmu.cis.ocrd.FileTypes;
import de.lmu.cis.ocrd.OCRLine;
import de.lmu.cis.ocrd.Page;
import de.lmu.cis.ocrd.Project;
import de.lmu.cis.ocrd.align.TokenAlignment;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;

class AlignCommand implements Command {

	@Override
	public void execute(Configuration config) throws Exception {
		final String[] args = config.getArgs();
		if (args == null || args.length < 2) {
			throw new Exception("expected at least two arguments: master other ...");
		}
		align(args);
	}

	private static void align(String[] args) throws Exception {
		assert(args.length > 1);
		Project project = new Project();
		project.put(args[0], FileTypes.openDocument(args[0]), true);
		for (int i = 1; i < args.length; i++) {
		    project.put(args[i], FileTypes.openDocument(args[i]), false);
        }
        align(project, args.length);
	}

	private static void align(Project project, int n) throws Exception {
		project.eachPage((page)->{
			align(page, n);
		});
	}

	private static void align(Page page, int n) throws Exception {
		Logger.debug("aligning lines for page {}...", page.getPageSeq());
		LineAlignment lineAlignment = new LineAlignment(page, n);
		Logger.debug("done aligning lines");
		int l = 0;
		for (ArrayList<OCRLine> lines : lineAlignment) {
			l++;
			TokenAlignment tokenAlignment = new TokenAlignment(lines.get(0).line.getNormalized());
			System.out.println(page.getPageSeq() + ":" + l + ": " + lines.get(0).line.getNormalized());
			for (int i = 1; i < lines.size(); i++) {
				System.out.println(page.getPageSeq() + ":" + l + ": " + lines.get(i).line.getNormalized());
				tokenAlignment.add(lines.get(i).line.getNormalized());
			}
			for (TokenAlignment.Token token : tokenAlignment) {
				System.out.println(" * " + token);
			}
		}
	}
}
