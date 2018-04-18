package de.lmu.cis.ocrd;

public interface Document {
	public interface Visitor {
		void visit(OCRLine t) throws Exception;
	}

	public void eachLine(Document.Visitor v) throws Exception;
}
