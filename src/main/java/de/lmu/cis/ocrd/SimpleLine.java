package de.lmu.cis.ocrd;

import java.util.ArrayList;
import java.util.List;

import de.lmu.cis.pocoweb.Token;

public class SimpleLine implements Line {

	private static String normalize(String line) {
		return line.replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim();
	}

	private int lineID, pageID;
	private String line;

	private ArrayList<Double> cs;

	public double getConfidenceAt(int i) {
		return cs.get(i);
	}

	@Override
	public int getLineId() {
		return this.lineID;
	}

	@Override
	public String getNormalized() {
		return this.line;
	}

	@Override
	public int getPageId() {
		return this.pageID;
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

	public SimpleLine withConfidences(ArrayList<Double> cs) {
		this.cs = cs;
		return this;
	}

	public SimpleLine withLineId(int id) {
		this.lineID = id;
		return this;
	}

	public SimpleLine withOcr(String line) {
		this.line = normalize(line);
		return this;
	}

	public SimpleLine withPageId(int id) {
		this.pageID = id;
		return this;
	}
}
