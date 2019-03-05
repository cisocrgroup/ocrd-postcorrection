package de.lmu.cis.ocrd.pagexml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public class Line extends TextRegion {
	List<Word> words;

	public Line(Node node) throws XPathExpressionException {
		super(node);
		this.words = getWordNodes(node, this);
	}

	public List<Word> getWords() {
		return words;
	}

	private static List<Word> getWordNodes(Node node, Line parent) throws XPathExpressionException {
		ArrayList<Word> wordList = new ArrayList<>();
		for (Node n : XPathHelper.getNodes(node, "./Word")) {
			wordList.add(new Word(n, parent));
		}
		return wordList;
	}
}
