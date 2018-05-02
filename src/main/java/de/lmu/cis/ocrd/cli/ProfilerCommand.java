package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.profile.Profiler;
import org.pmw.tinylog.Logger;

import java.io.StringReader;

class ProfilerCommand implements Command {
    private final String[] args = new String[] {
            "--types", "--sourceFormat", "TXT"
    };

    @Override
    public void execute(Configuration config) throws Exception {
        StringReader input = new StringReader("one token two tokens");
        Profiler profiler = new Profiler()
                .withExecutable("/home/flo/devel/work/Profiler/build/bin/profiler")
                .withLanguage("german")
                // .withStdin(new InputStreamReader(System.in))
                .withStdin(input)
                .withLanguageDirectory("/home/flo/langs")
                .withArgs(args);
        Logger.info("profiler command: {}", profiler.toString());
        Profile profile = profiler.run();
        Logger.debug("profile: {}", profile.toJSON());
        System.out.println(profile.toJSON());
    }
}
