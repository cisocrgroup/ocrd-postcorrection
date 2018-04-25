package de.lmu.cis.ocrd.align;

public interface Label {
	public String getLabel();

	public boolean isSynchronization();

	public Label next(int id);
}
