package de.lmu.cis.ocrd;

public interface Document {
	void eachLine(Document.Visitor v) throws Exception;

	interface Visitor {
		void visit(OCRLine t) throws Exception;
	}
}
