package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.profile.Profile;
import de.lmu.cis.ocrd.profile.Profiler;

import java.io.InputStreamReader;

class ProfilerCommand implements Command {
    private final String[] args = new String[] {
            "--types", "--inputFormat", "TXT"
    }
    @Override
    void execute(Configuration config) throws Exception {
        Profile profile = new Profiler()
                .withExecutable("/home/flo/devel/work/Profiler/build/bin/profiler")
                .withLanguage("german")
                .withStdin(new InputStreamReader(System.in))
                .withLanguageDirectory("/home/flo/langs")
                .withArgs(args)
                .run();
        System.out.println(profile.toJSON());
    }
}
