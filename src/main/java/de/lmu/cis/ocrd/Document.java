package de.lmu.cis.ocrd;

public interface Document {
	public void eachLine(Document.Visitor v) throws Exception;

	public interface Visitor {
		void visit(OCRLine t) throws Exception;
	}
}
