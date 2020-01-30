package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.BaseOCRTokenReader;
import de.lmu.cis.ocrd.ml.OCRTokenReader;
import de.lmu.cis.ocrd.ml.Rankings;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Workspace implements de.lmu.cis.ocrd.ml.Workspace {
    private final Path workDir;
    private final Path metsPath;
    private final METS mets;
    private final METSFileGroupReader fgr;

    public Workspace(Path mets, Parameters parameters) throws Exception {
        this.workDir = mets.getParent();
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
        putWords(fgr.readWords(ifg), ofg);
        saveMETS();
    }

    @Override
    public void resetProfile(String ifg, Profile profile) throws Exception {
        fgr.setProfile(ifg, profile);
    }

    private void putWords(List<Word> words, String ofg) throws Exception {
        Set<Page> changedPages = new HashSet<>();
        for (Word word: words) {
            final Page current = word.getParentLine().getParentPage();
            changedPages.add(word.getParentLine().getParentPage());
        }
        for (Page page: changedPages) {
            putPageXML(page, ofg);
        }
    }

    private Path putPageXML(Page page, String ofg) throws Exception {
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
