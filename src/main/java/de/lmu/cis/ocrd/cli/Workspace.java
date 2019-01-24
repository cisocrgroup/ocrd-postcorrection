package de.lmu.cis.ocrd.cli;

import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.Page;

import java.nio.file.Path;
import java.nio.file.Paths;

class Workspace {
    private final Path workDir;
    private final Path metsPath;
    private final METS mets;

    Workspace(Path mets) throws Exception {
        this.workDir = mets.getParent();
        this.metsPath = mets;
        this.mets = METS.open(mets);
    }

    METS getMETS() {
        return mets;
    }

    void save() throws Exception {
        mets.save(metsPath);
    }

    Path putPageXML(Page page, String ofg, String oldFileName) throws Exception {
        final Path name = getNewName(ofg, Paths.get(oldFileName));
        final Path destination = workDir.resolve(Paths.get(ofg).resolve(name));
        //noinspection ResultOfMethodCallIgnored
        destination.getParent().toFile().mkdirs();
        page.save(destination);
        mets.addFileToFileGrp(ofg)
                .withFLocat("file://" + destination.toAbsolutePath().toString())
                .withID(getID(name))
                .withMIMEType(Page.MIMEType);
        return destination.toAbsolutePath();
    }

    private String getID(Path name) {
        final String str = name.toString();
        final int i = str.lastIndexOf('.');
        if (i == -1) {
            return str;
        }
        return str.substring(0, i);
    }

    private Path getNewName(String ofg, Path oldName) {
        final String str = oldName.toString();
        final int i = str.lastIndexOf('-');
        if (i == -1) {
            return Paths.get(ofg + '-' + str);
        }
        return Paths.get(ofg + str.substring(i));
    }
}
