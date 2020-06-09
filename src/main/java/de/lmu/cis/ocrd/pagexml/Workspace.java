package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.AbstractWorkspace;
import de.lmu.cis.ocrd.ml.BaseOCRTokenReader;
import de.lmu.cis.ocrd.ml.OCRTokenReader;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Workspace extends AbstractWorkspace {
    private final Path metsPath;
    private final METS mets;
    private final METSFileGroupReader fgr;

    public Workspace(Path mets, Parameters parameters) throws Exception {
        this.metsPath = mets;
        this.mets = METS.open(metsPath);
        this.fgr = new METSFileGroupReader(this.mets, parameters);
    }


    private void saveMETS() throws Exception {
        Logger.info("saving mets: {}", metsPath.toAbsolutePath().toString());
        mets.save(metsPath);
    }

    @Override
    public BaseOCRTokenReader getBaseOCRTokenReader(String ifg) throws Exception {
        return fgr.getBaseOCRTokenReader(ifg);
    }

    @Override
    public OCRTokenReader getNormalTokenReader(String ifg, Profile profile) throws Exception {
        return fgr.getNormalTokenReader(ifg, profile);
    }

    @Override
    public void write(String ifg, String ofg) throws Exception {
        for (Page page: getPages(ifg)) {
            putPageXML(page, ofg);
        }
        saveMETS();
    }

    @Override
    public void resetProfile(String ifg, Profile profile) throws Exception {
        fgr.setProfile(ifg, profile);
    }

    public List<Page> getPages(String ifg) throws Exception {
        return fgr.getPages(ifg);
    }

    private Path putPageXML(Page page, String ofg) throws Exception {
        page.correctLinesAndRegions();
        final Path workDir = metsPath.getParent();
        final Path name = getNewName(ofg, page.getPath().getFileName());
        final Path destination = workDir.resolve(Paths.get(ofg).resolve(name)).toAbsolutePath();
        final String id = getID(name);
        //noinspection ResultOfMethodCallIgnored
        destination.getParent().toFile().mkdirs();
        mets.addFileToFileGrp(ofg)
                .withFLocat(destination.toString())
                .withID(id)
                .withMIMEType(Page.MIME_TYPE);
        final METS.FPtr fptr = mets.addFPtr(page.getMetsFile());
        if (fptr != null) {
            fptr.withFileID(id);
        }
        page.save(destination);
        return destination;
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
