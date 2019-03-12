package de.lmu.cis.ocrd.profile;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class AdditionalFileLexicon implements AdditionalLexicon {
    private final Path path;

    public AdditionalFileLexicon(Path path) {
        this.path = path;
    }

    @Override
    public boolean use() {
        return true;
    }

    @Override
    public Set<String> entries() throws Exception {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile()), Charset.forName("UTF-8")))) {
            Set<String> set = new HashSet<>();
            String line;
            while ((line = in.readLine()) != null) {
                set.add(line);
            }
            return set;
        }
    }
}
