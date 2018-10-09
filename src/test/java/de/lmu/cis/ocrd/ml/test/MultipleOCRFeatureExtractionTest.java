package de.lmu.cis.ocrd.ml.test;

import de.lmu.cis.iba.LineAlignment;
import de.lmu.cis.ocrd.*;
import de.lmu.cis.ocrd.align.Graph;
import de.lmu.cis.ocrd.align.TokenAlignment;
import de.lmu.cis.ocrd.ml.*;
import de.lmu.cis.ocrd.ml.features.*;
import de.lmu.cis.ocrd.parsers.StringParser;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThat;

public class MultipleOCRFeatureExtractionTest {
	private final String gt = "First second third alongtoken anotherevenlongertoken the";
	private final String mOCR = "first second ThIrD ALONGTOKEN anotherevenlongertoken the";
	private final String aOCR1 = "First second third alongtoken anotherevnlongertkn THE";
	private final String aOCR2 = "frst second third alongtoken anotherevnlongertkn ThE";
	private FeatureSet fs;
	private List<Token> tokens;

	private static void check(Instances got, Instances want) {
		assertThat(got.numAttributes(), is(want.numAttributes()));
		for (int i = 0; i < want.numAttributes(); i++) {
			assertThat(got.attributeToDoubleArray(i).length, is(want.attributeToDoubleArray(i).length));
			for (int j = 0; j < want.attributeToDoubleArray(i).length; j++) {
//                System.out.print(got.attributeToDoubleArray(i)[j] + " ");
				assertThat(got.attributeToDoubleArray(i)[j], is(want.attributeToDoubleArray(i)[j]));
			}
//            System.out.println();
		}
	}

	@Before
	public void init() throws Exception {
		final FreqMap ngrams = CharacterNGrams.fromCSV("src/test/resources/nGrams.csv");
		fs = new FeatureSet()
				.add(new TokenLengthClassFeature("TokenLength", 3, 8, 13))
				.add(new TokenCaseClassFeature("TokenCase"))
				.add(new SumOfMatchingAdditionalOCRsFeature("SumOfMatches"))
				.add(new MaxCharNGramsFeature("MaxCharNGramFeature", ngrams))
				.add(new MinCharNGramsFeature("MinCharNGramFeature", ngrams))
				.add(new DynamicLexiconGTFeature());
		final Document gtDoc = new StringParser(0, gt).parse().withPath("GT");
		final Document mOCRDoc = new StringParser(0, mOCR).parse().withPath("master OCR");
		final Document aOCR1Doc = new StringParser(0, aOCR1).parse().withPath("additional OCR 1");
		final Document aOCR2Doc = new StringParser(0, aOCR2).parse().withPath("additional OCR 2");
		final Project project = new Project()
				.put("master OCR", mOCRDoc, true)
				.put("GT", gtDoc, false)
				.put("additional OCR 1", aOCR1Doc, false)
				.put("additional OCR 2", aOCR2Doc, false);
		this.tokens = new ArrayList<>();
		project.eachPage((page) -> {
			final LineAlignment lineAlignment = new LineAlignment(page, 4);
			for (ArrayList<OCRLine> lines : lineAlignment) {
//            	System.out.println(lines.get(0).line.getNormalized());
//            	System.out.println(lines.get(1).line.getNormalized());
//            	System.out.println(lines.get(2).line.getNormalized());
//            	System.out.println(lines.get(3).line.getNormalized());

				final SimpleLine master = (SimpleLine) lines.get(0).line;
				final SimpleLine gtLine = (SimpleLine) lines.get(1).line;
				final SimpleLine add1Line = (SimpleLine) lines.get(2).line;
				final SimpleLine add2Line = (SimpleLine) lines.get(3).line;
				Graph g = new Graph(master.getNormalized(), add2Line.getNormalized());
//                System.out.println("xxx" + g.getStartNode().toString());
				TokenAlignment tokenAlignment = new TokenAlignment(master.getNormalized())
						.add(gtLine.getNormalized())
						.add(add1Line.getNormalized())
						.add(add2Line.getNormalized());
				int offset = 0;
				int offset2 = 0;
				int offset3 = 0;
				int tokenID = 0;
				for (TokenAlignment.Token token : tokenAlignment) {
					final Optional<Word> masterWord = master.getWord(offset, token.getMaster());
					final Optional<Word> add1Word = add1Line.getWord(offset2, token.getAlignment(1));
					final Optional<Word> add2Word = add2Line.getWord(offset3, token.getAlignment(2));
					assertThat(masterWord.isPresent(), is(true));
					offset += masterWord.get().toString().length();
					assertThat(add1Word.isPresent(), is(true));
					offset2 += add1Word.get().toString().length();
					assertThat(add2Word.isPresent(), is(true));
					offset3 += add2Word.get().toString().length();
					tokens.add(new Token(masterWord.get(), ++tokenID).withGT(token.getAlignment(0)).addOCR(add1Word.get()).addOCR(add2Word.get()));
				}
			}
		});
	}

	@Test
	public void testWithMasterOCR() throws Exception {
		runTest(1, "src/test/resources/multipleOCRFeatureExtraction1.arff");
	}

	@Test
	public void testWithOneAdditionalOCR() throws Exception {
		runTest(2, "src/test/resources/multipleOCRFeatureExtraction2.arff");
	}

	@Test
	public void testWithTwoAdditionalOCR() throws Exception {
		runTest(3, "src/test/resources/multipleOCRFeatureExtraction3.arff");
	}

	private void runTest(int n, String arffFile) throws Exception {
		//final OutputStreamWriter w = new OutputStreamWriter(System.out); // to update test files
		final StringWriter w = new StringWriter();
		final ARFFWriter arff = ARFFWriter.fromFeatureSet(fs)
				.withDebugToken(true)
				.withRelation("TestWithOnlyMasterOCR")
				.withWriter(w);
		arff.writeHeader(n);
		for (Token token : tokens) {
			assertThat(token.getNumberOfOtherOCRs(), is(2));
			assertThat(token.hasGT(), is(true));
			arff.writeToken(token);
			arff.writeFeatureVector(fs.calculateFeatureVector(token, n));
		}
		w.flush();
		final Instances got = new ConverterUtils.DataSource(IOUtils.toInputStream(w.toString(), Charset.defaultCharset())).getDataSet();
		ArffLoader loader = new ArffLoader();
		loader.setFile(new File(arffFile));
		final Instances want = new ConverterUtils.DataSource(loader).getDataSet();
		check(got, want);
	}
}
