package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.Page;
import de.lmu.cis.ocrd.pagexml.Tokenizer;
import org.pmw.tinylog.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class TokenizerCommand implements Command {
    private Tokenizer tokenizer;
    static class Parameter {
        HashMap<String, String> symbols;
    }
    @Override
    public void execute(CommandLineArguments config) throws Exception {
        config.setCommand(this);
        final METS mets = METS.open(Paths.get(config.mustGetMETSFile()));
        final String ofg = config.mustGetSingleOutputFileGroup();
        final Parameter parameter = config.mustGetParameter(Parameter.class);
        tokenizer = new Tokenizer(parameter.symbols);

        for (String ifg: config.mustGetInputFileGroups()) {
            final List<METS.File> files = mets.findFileGrpFiles(ifg);
            for (METS.File file: files) {
                final Page newPage = tokenize(file);
                final Path flocat= Paths.get(file.getFLocat());
                final METS.File newFile = addFile(mets, newPage, ofg, flocat.getFileName().toString());
                final Path newFlocat = Paths.get(newFile.getFLocat());
                Files.createDirectories(newFlocat.getParent());
                newPage.save(newFlocat.toFile());
            }
        }
    }

    @Override
    public String getName() {
        return "tokenize";
    }

    private Page tokenize(METS.File file) throws Exception {
        Logger.info("tokenizing file: {}", file.getFLocat());
        try (InputStream is = file.openInputStream()) {
            final Page page = Page.parse(Paths.get(file.getFLocat()), is);
            return tokenizer.tokenize(page);
        }
    }

    private static METS.File addFile(METS mets, Page page, String ofg, String base) throws Exception {
        String id = base;
        int pos = id.lastIndexOf(".");
        if (pos > 0) {
            id = base.substring(0, pos);
        }
        return mets
                .addFileToFileGrp(ofg)
                .withMIMEType(Page.MIMEType)
                .withID(ofg + "-" + id)
                .withGroupID(ofg)
                .withFLocat(Paths.get(ofg, base).toString());
    }
}
