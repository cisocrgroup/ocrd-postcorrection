package de.lmu.cis.ocrd.cli;

import com.google.gson.Gson;
import de.lmu.cis.ocrd.align.Lines;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlignCommand extends AbstractIOCommand {
    public static class Parameter {
        public int n;
    }

    public AlignCommand() {
        super();
    }

    @Override
    public void execute(CommandLineArguments args) throws Exception {
        final Parameter p = args.mustGetParameter(Parameter.class);
        if (p.n <= 0) {
            throw new Exception("invalid n: " + p.n);
        }
        align(p.n);
    }

    @Override
    public String getName() {
        return "align";
    }

    private void align(int n) throws IOException {
        String[] lines = new String[n];
	    final List<Lines.Alignment> data = new ArrayList<>();
	    // read input
        while (readLines(lines)) {
        	data.add(Lines.align(lines));
        }
        println(new Gson().toJson(data));
        flush();
    }

    private boolean readLines(String[] lines) throws IOException {
        for (int i = 0; i < lines.length; i++) {
        	lines[i] = readLine();
            if (lines[i] == null) {
                return false;
            }
            lines[i] = lines[i].trim();
            lines[i] = lines[i].replace('#', ' ');
            lines[i] = lines[i].replace('$', ' ');
            Logger.info("read line: {}", lines[i]);
        }
        return true;
    }
}
