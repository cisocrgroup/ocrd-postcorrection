package de.lmu.cis.ocrd.pagexml;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class Line extends TextRegion {
	List<Word> words;
	private Page parent;

	public Line(Node node, Page parent) {
		super(node);
		this.words = getWordNodes(node, this);
		this.parent = parent;
	}

	public List<Word> getWords() {
		return words;
	}

	private static List<Word> getWordNodes(Node node, Line parent) {
		ArrayList<Word> wordList = new ArrayList<>();
		for (Node n : XPathHelper.getNodes(node, "./Word")) {
			wordList.add(new Word(n, parent));
		}
		return wordList;
	}
}
