package de.lmu.cis.ocrd.train;

import de.lmu.cis.iba.LineAlignment_Fast;
import de.lmu.cis.ocrd.OCRLine;
import de.lmu.cis.ocrd.Project;
import de.lmu.cis.ocrd.SimpleLine;
import de.lmu.cis.ocrd.Word;
import de.lmu.cis.ocrd.align.TokenAlignment;
import de.lmu.cis.ocrd.ml.Token;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
	private static final String MASTER_OCR = "master_ocr";
	private static final String GT = "gt";
	private static final String OTHER_OCR_PREFIX = "other_ocr_";
	private static final Pattern OTHER_OCR_ID = Pattern.compile(OTHER_OCR_PREFIX + "(\\d+)");
	private final Environment environment;

	public Tokenizer(Environment environment) {
		this.environment = environment;
	}

	private static int getOCREngineNumericValue(String ocrEngine) {
		if (ocrEngine.equals(MASTER_OCR)) {
			return 0;
		}
		if (ocrEngine.equals(GT)) {
			return 1;
		}
		final Matcher m = OTHER_OCR_ID.matcher(ocrEngine);
		if (!m.matches()) {
			throw new RuntimeException("internal error: no such ocr engine: " + ocrEngine);
		}
		return Integer.parseInt(m.group(1)) + 1;
	}

	public void eachToken(Visitor v) throws Exception {
		newProject().eachPage((page) -> {
			// gather aligned lines and sort them
			final ArrayList<ArrayList<OCRLine>> lineAlignments = new ArrayList<>();
			LineAlignment_Fast lineAlignment = new LineAlignment_Fast(page, 2 + environment.getNumberOfOtherOCR());
			for (ArrayList<OCRLine> line : lineAlignment) {
				line.sort(Comparator.comparingInt((OCRLine a) -> getOCREngineNumericValue(a.ocrEngine)));
				lineAlignments.add(line);
			}
			lineAlignments.sort((ArrayList<OCRLine> a, ArrayList<OCRLine> b) -> {
				assert (a.size() > 0);
				assert (b.size() > 0);
				// compare line IDs of master OCR
				return a.get(0).line.getLineId() - b.get(0).line.getLineId();
			});
			// iterate over aligned lines in ascending order
			for (ArrayList<OCRLine> line : lineAlignments) {
				final SimpleLine master = (SimpleLine) line.get(0).line;
				final SimpleLine gt = (SimpleLine) line.get(1).line;
				final ArrayList<SimpleLine> otherOCRs = new ArrayList<>();

				Logger.debug("masterOCR:{}:{}: {}", master.getPageId(), master.getLineId(), master.getNormalized());
				Logger.debug("GT:{}:{}: {}", gt.getPageId(), gt.getLineId(), gt.getNormalized());
				for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
					Logger.debug("otherOCR:{}:{}: {}",
							line.get(i + 2).line.getPageId(), line.get(i + 2).line.getLineId(), line.get(i + 2).line.getNormalized());
				}

				TokenAlignment tokenAlignment = new TokenAlignment(master.getNormalized());
				tokenAlignment.add(gt.getNormalized());
				for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
					otherOCRs.add((SimpleLine) line.get(i + 2).line);
					tokenAlignment.add(otherOCRs.get(i).getNormalized());
				}
				eachTokenOnLineAlignment(tokenAlignment, master, otherOCRs, v);
			}
		});
	}

	private void eachTokenOnLineAlignment(TokenAlignment tokenAlignment, SimpleLine master, ArrayList<SimpleLine> otherOCRs, Visitor v) throws Exception {
		int offset = 0;
		final ArrayList<Integer> otherOffsets = new ArrayList<>();
		int tokenID = 0;
		for (TokenAlignment.Token token : tokenAlignment) {
			Logger.debug("Aligned token: {}", token);
			Optional<Word> masterToken = master.getWord(offset, token.getMaster());
			if (!masterToken.isPresent()) {
				continue;
			}
			offset += token.getMaster().length();
			for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
				otherOffsets.add(0);
			}
			final Token theTrainingToken = new Token(masterToken.get(), ++tokenID).withGT(token.getAlignment(0));
			for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
				Optional<Word> theWord = otherOCRs.get(i).getWord(otherOffsets.get(i), token.getAlignment(i + 1));
				if (!theWord.isPresent()) {
					theWord = Optional.of(Word.empty(master));
				}
				Logger.debug("theWord: {}", theWord);
				theTrainingToken.addOCR(theWord.get());
				otherOffsets.set(i, otherOffsets.get(i) + theWord.get().toString().length());
			}
			v.visit(theTrainingToken);
		}
	}

	private Project newProject() throws Exception {
		Project project = new Project();
		project.put(MASTER_OCR, environment.openMasterOCR(), true);
		project.put(GT, environment.openGT(), false);
		for (int i = 0; i < environment.getNumberOfOtherOCR(); i++) {
			project.put(OTHER_OCR_PREFIX + (i + 1), environment.openOtherOCR(i), false);
		}
		// project.eachLine((line)-> System.out.println("LINE: " + line.line.getNormalized()));
		return project;
	}

	public interface Visitor {
		void visit(Token token) throws Exception;
	}
}
