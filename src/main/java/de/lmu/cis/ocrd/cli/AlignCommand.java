package de.lmu.cis.ocrd.cli;

import java.util.ArrayList;

import de.lmu.cis.ocrd.FileTypes;
import de.lmu.cis.ocrd.align.TokenAlignment;
import org.pmw.tinylog.Logger;

import de.lmu.cis.iba.LineAlignment;
import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.OCRLine;
import de.lmu.cis.ocrd.Project;

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

	private static void align(Document doc, int n) throws Exception {
		Logger.debug("aligning lines ...");
		LineAlignment lalignment = new LineAlignment(doc, n);
		Logger.debug("done aligning lines");
		int l = 0;
		Logger.debug("iterating ...");
		for (ArrayList<OCRLine> lines : lalignment) {
		    l++;
            TokenAlignment tokenAlignment = new TokenAlignment(lines.get(0).line.getNormalized());
            System.out.println(l + ": " + lines.get(0).line.getNormalized());
		    for (int i = 1; i < lines.size(); i++) {
                System.out.println(l + ": " + lines.get(i).line.getNormalized());
                tokenAlignment.add(lines.get(i).line.getNormalized());
            }
            for (TokenAlignment.Token token : tokenAlignment) {
		        System.out.println(" * " + token);
            }
		}
		Logger.debug("done iterating");
	}
}
