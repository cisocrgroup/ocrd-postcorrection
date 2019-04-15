package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.pagexml.METS;
import de.lmu.cis.ocrd.pagexml.Page;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Workspace {
    private final Path workDir;
    private final Path metsPath;
    private final METS mets;

    public Workspace(Path mets) throws Exception {
        this.workDir = mets.getParent();
        this.metsPath = mets;
        this.mets = METS.open(mets);
    }

    public METS getMETS() {
        return mets;
    }

    public void save() throws Exception {
        mets.save(metsPath);
    }

    public Path putPageXML(Page page, String ofg) throws Exception {
        final Path name = getNewName(ofg, page.getPath().getFileName());
        final Path dest = workDir.resolve(Paths.get(ofg).resolve(name));
        //noinspection ResultOfMethodCallIgnored
        dest.getParent().toFile().mkdirs();
        page.save(dest);
        mets.addFileToFileGrp(ofg)
                .withFLocat("file://" + dest.toAbsolutePath().toString())
                .withID(getID(name))
                .withMIMEType(Page.MIMEType);
        return dest.toAbsolutePath();
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
