package de.lmu.cis.ocrd.archive;

import java.io.IOException;

public class InvalidEntryException extends IOException {

	private static final long serialVersionUID = 1L;

	public InvalidEntryException(Entry entry) {
		super("invalid entry: " + entry.getName().toString());
	}
}
