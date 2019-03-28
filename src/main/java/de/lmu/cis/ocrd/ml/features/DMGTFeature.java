package de.lmu.cis.ocrd.ml.features;

import org.pmw.tinylog.Logger;

import java.util.List;
import java.util.Map;

public class DMGTFeature extends NamedBooleanFeature {
	private Map<OCRToken, List<Ranking>> rankings;
	public DMGTFeature(String name, Map<OCRToken, List<Ranking>> rankings) {
		super(name);
		this.rankings = rankings;
	}

	@Override
	public boolean handlesOCR(int i, int n) {
		return handlesOnlyMasterOCR(i, n);
	}

	@Override
	boolean doCalculate(OCRToken token, int i, int n) {
		assert(rankings.containsKey(token));
		List<Ranking> rs = rankings.get(token);
		assert(!rs.isEmpty());
		final String gt = token.getGT().orElse("");
		final boolean result =  gt.equals(rs.get(0).candidate.Suggestion);
		Logger.debug("{} result = {}", getName(), result);
		return result;
	}
}
