package de.lmu.cis.ocrd.align;

public interface Label {
	String getLabel();

	boolean isSynchronization();

	Label next(int id);
}
