package de.lmu.cis.ocrd.graph;

public interface Label {
	public String getLabel();

	public boolean isSynchronization();

	public Label next(int id);
}
