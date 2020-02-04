package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.BaseOCRTokenReader;
import de.lmu.cis.ocrd.ml.OCRTokenReader;
import de.lmu.cis.ocrd.ml.Rankings;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Workspace implements de.lmu.cis.ocrd.ml.Workspace {
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
    public OCRTokenReader getCandidateTokenReader(String ifg, Profile profile) throws Exception {
        return fgr.getCandidateTokenReader(ifg, profile);
    }

    @Override
    public OCRTokenReader getRankedTokenReader(String ifg, Profile profile, Rankings rankings) throws Exception {
        return fgr.getRankedTokenReader(ifg, profile, rankings);
    }

    @Override
    public void write(String ifg, String ofg) throws Exception {
        for (Page page: fgr.getPages(ifg)) {
            putPageXML(page, ofg);
        }
        saveMETS();
    }

    @Override
    public void resetProfile(String ifg, Profile profile) throws Exception {
        fgr.setProfile(ifg, profile);
    }

    private Path putPageXML(Page page, String ofg) throws Exception {
        final Path workDir = metsPath.getParent();
        final Path name = getNewName(ofg, page.getPath().getFileName());
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
