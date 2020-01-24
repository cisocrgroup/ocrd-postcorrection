package de.lmu.cis.ocrd.pagexml;

import de.lmu.cis.ocrd.config.Parameters;
import de.lmu.cis.ocrd.ml.Rankings;
import de.lmu.cis.ocrd.ml.TokenReader;
import de.lmu.cis.ocrd.profile.Profile;
import org.pmw.tinylog.Logger;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

public class Workspace {
    private final Path workDir;
    private final Path metsPath;
    private final METS mets;
    private final METSFileGroupReader fgr;

    public Workspace(Path mets, Parameters parameters) throws Exception {
        this.workDir = mets.getParent();
        this.metsPath = mets;
        this.mets = METS.open(mets);
        this.fgr = new METSFileGroupReader(this.mets, parameters);
    }

    public METS getMETS() {
        return mets;
    }

    public void save() throws Exception {
        Logger.info("saving mets: {}", metsPath.toAbsolutePath().toString());
        mets.save(metsPath);
    }

    public WordReader getWordReader(String ifg) throws Exception {
        return fgr.getWordReader(ifg);
    }

    public TokenReader getNormalTokenReader(String ifg, Profile profile) throws Exception {
        return fgr.getNormalTokenReader(ifg, profile);
    }

    public TokenReader getCandidateTokenReader(String ifg, Profile profile) throws Exception {
        return fgr.getCandidateTokenReader(ifg, profile);
    }

    public TokenReader getRankedTokenReader(String ifg, Profile profile, Rankings rankings) throws Exception {
        return fgr.getRankedTokenReader(ifg, profile, rankings);
    }

    public void putWords(WordReader wordReader, String ofg) throws Exception {
        Page old = null;
        for (Word word: wordReader.readWords()) {
            final Page current = word.getParentLine().getParentPage();
            // write new pages
            if (current != old) {
                old = current;
                putPageXML(current, ofg);
            }
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

    public Path putProfile(Profile profile, String ofg) throws Exception {
        final Path dest = workDir.resolve(Paths.get(ofg).resolve(Paths.get("profile.json.gz")));
        //noinspection ResultOfMethodCallIgnored
        dest.getParent().toFile().mkdirs();
        try (java.io.OutputStream out = new GZIPOutputStream(new FileOutputStream(dest.toFile()))) {
            out.write(profile.toJSON().getBytes(StandardCharsets.UTF_8));
        }
        mets.addFileToFileGrp(ofg)
                .withMIMEType("application/json+gzip")
                .withFLocat(Paths.get(dest.getParent().getFileName().toString(), dest.getFileName().toString()).toString())
                .withID(ofg + "-" + "PROFILE");
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
