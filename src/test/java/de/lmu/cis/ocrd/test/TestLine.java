package de.lmu.cis.ocrd.test;
import de.lmu.cis.ocrd.Line;
import de.lmu.cis.pocoweb.Token;

import java.util.List;

public class TestLine implements Line {
	private final int id, pageid;
	private final String norm;

	TestLine(int pageid, int id, String norm) {
		this.id = id;
		this.pageid = pageid;
		this.norm = norm;
	}

	@Override
	public int getLineId() {
		return id;
	}

	@Override
	public String getNormalized() {
		return norm;
	}

	@Override
	public List<Token> getTokens() {
		return null;
	}

	@Override
	public int getPageId() {
		return this.pageid;
	}

}
