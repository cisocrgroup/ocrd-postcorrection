package de.lmu.cis.ocrd;

import java.util.ArrayList;
import java.util.List;

import de.lmu.cis.pocoweb.Token;

public class SimpleLine implements Line {

	private int lineID, pageID;
	private String line;

	public SimpleLine withLineId(int id) {
		this.lineID = id;
		return this;
	}

	public SimpleLine withPageId(int id) {
		this.pageID = id;
		return this;
	}

	public SimpleLine withOcr(String line) {
		this.line = normalize(line);
		return this;
	}

	@Override
	public int getLineId() {
		return this.lineID;
	}

	@Override
	public int getPageId() {
		return this.pageID;
	}

	@Override
	public String getNormalized() {
		return this.line;
	}

	@Override
	public Token getTokenAt(int i) {
		return getTokens().get(i);
	}

	@Override
	public List<Token> getTokens() {
		ArrayList<Token> tokens = new ArrayList<Token>();
		int tid = 0;
		for (String t : this.line.split("\\s+")) {
			tokens.add(new Token().withLineId(lineID).withPageId(pageID).withTokenId(tid).withOcr(t));
			tid++;
		}
		return tokens;
	}

	private static String normalize(String line) {
		return line.replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim();
	}
}
