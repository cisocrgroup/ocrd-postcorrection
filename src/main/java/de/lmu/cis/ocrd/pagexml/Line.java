package de.lmu.cis.ocrd.pagexml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public class Line extends TextRegion {
	private final List<Word> words;
	
	public Line(Node node) throws XPathExpressionException {
		super(node);
		this.words = getWordNodes(node);
	}

	public List<Word> getWords() {
		return words;
	}

	private static List<Word> getWordNodes(Node node) throws XPathExpressionException {
		ArrayList<Word> wordList = new ArrayList<>();
		for (Node n : XPathHelper.getNodes(node, "./Word")) {
			wordList.add(new Word(n));
		}
		return wordList;
	}
}
